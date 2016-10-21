package jp.gr.java_conf.uzresk.aws.ope.ebs;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

import com.amazonaws.ClientConfiguration;
import com.amazonaws.auth.DefaultAWSCredentialsProviderChain;
import com.amazonaws.regions.RegionUtils;
import com.amazonaws.services.ec2.AmazonEC2;
import com.amazonaws.services.ec2.AmazonEC2Async;
import com.amazonaws.services.ec2.AmazonEC2AsyncClient;
import com.amazonaws.services.ec2.model.CreateSnapshotRequest;
import com.amazonaws.services.ec2.model.CreateSnapshotResult;
import com.amazonaws.services.ec2.model.CreateTagsRequest;
import com.amazonaws.services.ec2.model.DeleteSnapshotRequest;
import com.amazonaws.services.ec2.model.DescribeSnapshotsRequest;
import com.amazonaws.services.ec2.model.DescribeSnapshotsResult;
import com.amazonaws.services.ec2.model.DescribeVolumesRequest;
import com.amazonaws.services.ec2.model.DescribeVolumesResult;
import com.amazonaws.services.ec2.model.Filter;
import com.amazonaws.services.ec2.model.Snapshot;
import com.amazonaws.services.ec2.model.SnapshotState;
import com.amazonaws.services.ec2.model.Tag;
import com.amazonaws.services.ec2.model.Volume;
import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.LambdaLogger;

public class EBSSnapshot {

	private static final int DEFAULT_GENERATION_COUNT = 10;

	private static final String EBS_TAG_BACKUP = "Backup";

	private static final String EBS_TAG_GENERATION_COUNT = "GenerationCount";

	private static ClientConfiguration cc = new ClientConfiguration();

	public void create(Target target, Context context) {

		LambdaLogger logger = context.getLogger();
		logger.log("EBS Snapsthot Start. backup target[" + target.getTarget() + "]");

		String regionName = System.getenv("AWS_DEFAULT_REGION");

		AmazonEC2Async client = RegionUtils.getRegion(regionName).createClient(AmazonEC2AsyncClient.class,
				new DefaultAWSCredentialsProviderChain(), cc);

		List<Volume> volumes = describeBackupVolumes(client, target);

		volumes.stream()
				.forEach(s -> snapshot(client, s.getVolumeId(), s.getTags().stream()
						.filter(tags -> EBS_TAG_GENERATION_COUNT.equals(tags.getKey())).findFirst().get().getValue(),
						context));

		client.shutdown();
		logger.log("EBS Snapsthot End.");
	}

	private List<Volume> describeBackupVolumes(AmazonEC2Async client, Target target) {

		Filter tagKey = new Filter().withName("tag-key").withValues(EBS_TAG_BACKUP);
		Filter tagValue = new Filter().withName("tag-value").withValues(target.getTarget());
		DescribeVolumesRequest req = new DescribeVolumesRequest().withFilters(tagKey, tagValue);
		DescribeVolumesResult result = client.describeVolumes(req);

		return result.getVolumes();
	}

	private void snapshot(AmazonEC2Async client, String volumeId, String generationCountStr, Context context) {

		LambdaLogger logger = context.getLogger();

		// To get the snapshot
		String lambdaFunctionName = context.getFunctionName();
		String description = "Created by CloudWatchEvents schedule Lambda(" + lambdaFunctionName + ")";
		Future<CreateSnapshotResult> result = client
				.createSnapshotAsync(new CreateSnapshotRequest(volumeId, description));
		Snapshot snapshot = null;
		try {
			// waiting for snapshot
			while (!result.isDone()) {
				Thread.sleep(500);
			}
			snapshot = result.get().getSnapshot();
		} catch (InterruptedException | ExecutionException e) {
			logger.log("ERROR:create snapshot:" + e.getMessage());
		}

		String snapshotId = snapshot.getSnapshotId();
		logger.log("EBS snapshot created. SnapshotId[" + snapshotId + "] VolumeId[" + volumeId + "]");

		// add a tag to the snapshot
		attachSnapshotTags(client, volumeId, snapshotId);

		int generationCount = DEFAULT_GENERATION_COUNT;
		try {
			generationCount = Integer.parseInt(generationCountStr);
		} catch (Exception e) {
			logger.log("generation count error. [" + generationCountStr + "]");
		}
		pargeEbsSnapshot(client, volumeId, generationCount, context);
	}

	private void attachSnapshotTags(AmazonEC2Async client, String volumeId, String snapshotId) {

		List<Tag> tags = new ArrayList<Tag>();
		tags.add(new Tag("VolumeId", volumeId));
		tags.add(new Tag("BackupType", "auto"));
		String requestDate = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmm"));
		tags.add(new Tag("RequestDate", requestDate));

		CreateTagsRequest snapshotTagsRequest = new CreateTagsRequest().withResources(snapshotId);
		snapshotTagsRequest.setTags(tags);
		client.createTags(snapshotTagsRequest);
	}

	public void pargeEbsSnapshot(AmazonEC2Async client, String volumeId, int generationCount, Context context) {

		LambdaLogger logger = context.getLogger();
		logger.log("Parge snapshot start. VolumeId[" + volumeId + "] generationCount[" + generationCount + "]");

		// volume id をキーにsnapshotの一覧を取得します。
		Filter filter = new Filter().withName("volume-id").withValues(volumeId);
		filter.withName("tag:VolumeId").withValues(volumeId);
		DescribeSnapshotsRequest snapshotRequest = new DescribeSnapshotsRequest().withFilters(filter);
		DescribeSnapshotsResult snapshotResult = client.describeSnapshots(snapshotRequest);

		// snapshot作成開始日でソートします。（古い→新しい）
		List<Snapshot> snapshots = snapshotResult.getSnapshots();
		Collections.sort(snapshots, new SnapshotComparator());

		// 世代管理保持数 < snapshotの数の場合、対象をpargeします。
		int snapshotSize = snapshots.size();
		if (generationCount < snapshotSize) {
			for (int i = 0; i < snapshotSize - generationCount; i++) {
				Snapshot snapshot = snapshots.get(i);
				// （念のため）snapshotのステータスが完了しているものだけをparge対象とする。
				if (SnapshotState.Completed.toString().equals(snapshot.getState())) {
					String snapshotId = snapshot.getSnapshotId();
					pargeSnapshot(client, snapshotId);
					logger.log("Parge EBS snapshot. snapshotId[" + snapshotId + "] volumeId[" + volumeId + "]");
				}
			}
		}
	}

	private class SnapshotComparator implements Comparator<Snapshot> {
		@Override
		public int compare(Snapshot o1, Snapshot o2) {
			Date startDateO1 = o1.getStartTime();
			Date startDateO2 = o2.getStartTime();
			return startDateO1.compareTo(startDateO2);
		}
	}

	private void pargeSnapshot(AmazonEC2 ec2, String snapshotId) {
		DeleteSnapshotRequest request = new DeleteSnapshotRequest(snapshotId);
		ec2.deleteSnapshot(request);
	}
}
