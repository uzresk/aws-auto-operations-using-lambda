package jp.gr.java_conf.uzresk.aws.ope.ebs;

import java.io.PrintWriter;
import java.io.StringWriter;
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
import com.amazonaws.services.ec2.model.CopySnapshotRequest;
import com.amazonaws.services.ec2.model.CopySnapshotResult;
import com.amazonaws.services.ec2.model.CreateTagsRequest;
import com.amazonaws.services.ec2.model.DeleteSnapshotRequest;
import com.amazonaws.services.ec2.model.DescribeSnapshotsRequest;
import com.amazonaws.services.ec2.model.DescribeSnapshotsResult;
import com.amazonaws.services.ec2.model.Filter;
import com.amazonaws.services.ec2.model.Snapshot;
import com.amazonaws.services.ec2.model.SnapshotState;
import com.amazonaws.services.ec2.model.Tag;
import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.LambdaLogger;

import jp.gr.java_conf.uzresk.aws.lambda.LambdaLock;
import jp.gr.java_conf.uzresk.aws.ope.ebs.model.SnapshotIdRequest;
import jp.gr.java_conf.uzresk.aws.ope.ebs.model.SnapshotIdRequests;
import jp.gr.java_conf.uzresk.aws.ope.ebs.model.VolumeIdRequest;
import jp.gr.java_conf.uzresk.aws.ope.ebs.model.VolumeIdRequests;

public class EBSCopySnapshotFunction {

	private static ClientConfiguration cc = new ClientConfiguration();

	public void copySnapshotFromSnapshotId(SnapshotIdRequest snapshotIdRequest, Context context) {

		LambdaLogger logger = context.getLogger();
		logger.log("copy ebs snapshot from snapshot id Start. backup target[" + snapshotIdRequest + "]");

		copySnapshot(snapshotIdRequest.getSourceSnapshotId(), snapshotIdRequest.getDestinationRegion(),
				snapshotIdRequest.getGenerationCount(), context);
	}

	public void copySnapshotFromSnapshotIds(SnapshotIdRequests snapshotIdRequests, Context context) {
		for (SnapshotIdRequest snapshotIdRequest : snapshotIdRequests.getSnapshotIdRequests()) {
			copySnapshotFromSnapshotId(snapshotIdRequest, context);
		}
	}

	/**
	 * Find the snapshot the volumeId the key, to get a copy of the latest
	 * Snapshot
	 *
	 * @param volumeIdRequest
	 *            VolumeIdRequest
	 * @param context
	 *            Context
	 */
	public void copySnapshotFromVolumeId(VolumeIdRequest volumeIdRequest, Context context) {

		LambdaLogger logger = context.getLogger();

		String regionName = System.getenv("AWS_DEFAULT_REGION");
		AmazonEC2Async client = RegionUtils.getRegion(regionName).createClient(AmazonEC2AsyncClient.class,
				new DefaultAWSCredentialsProviderChain(), cc);

		try {
			String volumeId = volumeIdRequest.getVolumeId();

			boolean isLockAcquisition = new LambdaLock().lock(volumeId, context);
			if (!isLockAcquisition) {
				logger.log("[ERROR][EBSCopySnapshot][" + volumeId + "]You can not acquire a lock.");
				return;
			}

			List<Filter> filters = new ArrayList<>();
			filters.add(new Filter().withName("volume-id").withValues(volumeId));
			filters.add(new Filter().withName("tag:VolumeId").withValues(volumeId));
			filters.add(new Filter().withName("tag:BackupType").withValues("snapshot"));
			DescribeSnapshotsRequest snapshotRequest = new DescribeSnapshotsRequest().withFilters(filters);
			DescribeSnapshotsResult snapshotResult = client.describeSnapshots(snapshotRequest);

			// sort and get latest snapshot
			List<Snapshot> snapshots = snapshotResult.getSnapshots();
			Collections.sort(snapshots, new SnapshotComparator());
			int snapshotsSize = snapshots.size();
			if (snapshotsSize == 0) {
				throw new RuntimeException("The copy source snapshot can not be found");
			}
			Snapshot snapshot = snapshots.get(snapshots.size() - 1);

			String sourceSnapshotId = snapshot.getSnapshotId();

			copySnapshot(sourceSnapshotId, volumeIdRequest.getDestinationRegion(), volumeIdRequest.getGenerationCount(),
					context);

		} catch (Exception e) {
			logger.log("[ERROR][EBSCopySnapshot][" + volumeIdRequest.getVolumeId() + "] message[" + e.getMessage()
					+ "] stackTrace[" + getStackTrace(e) + "]");
		} finally {
			client.shutdown();
		}
	}

	public void copySnapshotFromVolumeIds(VolumeIdRequests volumeIdRequests, Context context) {
		for (VolumeIdRequest volumeIdReuest : volumeIdRequests.getVolumeIdRequests()) {
			copySnapshotFromVolumeId(volumeIdReuest, context);
		}
	}

	void copySnapshot(String sourceSnapshotId, String destinationRegion, int generationCount, Context context) {

		LambdaLogger logger = context.getLogger();

		String regionName = System.getenv("AWS_DEFAULT_REGION");
		AmazonEC2Async client = RegionUtils.getRegion(regionName).createClient(AmazonEC2AsyncClient.class,
				new DefaultAWSCredentialsProviderChain(), cc);

		try {
			// To get the snapshot
			String lambdaFunctionName = context.getFunctionName();
			String description = "[Copied " + sourceSnapshotId + " from " + regionName + "] Created by Lambda("
					+ lambdaFunctionName + ")";

			Future<CopySnapshotResult> result = client.copySnapshotAsync(
					new CopySnapshotRequest().withSourceSnapshotId(sourceSnapshotId).withSourceRegion(regionName)
							.withDescription(description).withDestinationRegion(destinationRegion));
			String snapshotId = null;
			try {
				// waiting for snapshot
				while (!result.isDone()) {
					Thread.sleep(500);
				}
				snapshotId = result.get().getSnapshotId();
			} catch (InterruptedException | ExecutionException e) {
				throw new RuntimeException("copy snapshot error. " + e.getMessage());
			}

			logger.log("EBS snapshot created. snapshotId[" + snapshotId + "] from sourceSnapshoId[" + sourceSnapshotId
					+ "]");

			// add a tag to the snapshot
			attachSnapshotTags(client, sourceSnapshotId, snapshotId);

			pargeEbsSnapshot(client, sourceSnapshotId, snapshotId, generationCount, context);

			logger.log(
					"[SUCCESS][EBSSnapshot][" + sourceSnapshotId + "] EBS Copy Snapshot has completed successfully.");

		} catch (Exception e) {
			logger.log("[ERROR][EBSSnapshot][" + sourceSnapshotId + "] message[" + e.getMessage() + "] stackTrace["
					+ getStackTrace(e) + "]");
		} finally {
			client.shutdown();
		}
	}

	void attachSnapshotTags(AmazonEC2Async client, String sourceSnapshotId, String snapshotId) {

		DescribeSnapshotsResult result = client
				.describeSnapshots(new DescribeSnapshotsRequest().withSnapshotIds(sourceSnapshotId));
		List<Snapshot> snapshots = result.getSnapshots();
		if (snapshots.size() != 1) {
			throw new RuntimeException("snapshot can not found. sourceSnapshotId[" + snapshotId + "]");
		}
		List<Tag> sourceSnapshotTags = snapshots.get(0).getTags();

		List<Tag> tags = new ArrayList<Tag>();
		tags.addAll(sourceSnapshotTags);
		tags.add(new Tag("SourceSnapshotId", sourceSnapshotId));
		tags.add(new Tag("BackupType", "copy-snapshot")); // overwrite

		CreateTagsRequest snapshotTagsRequest = new CreateTagsRequest().withResources(snapshotId);
		snapshotTagsRequest.setTags(tags);
		client.createTags(snapshotTagsRequest);
	}

	void pargeEbsSnapshot(AmazonEC2Async client, String sourceSnapshotId, String snapshotId, int generationCount,
			Context context) {

		LambdaLogger logger = context.getLogger();
		logger.log(
				"Parge snapshot start. SnapshotId[" + sourceSnapshotId + "] generationCount[" + generationCount + "]");

		// get volumeId from tags
		String volumeId = getVolumeIdFromTag(client, snapshotId);

		// describe filter tag VolumeId
		List<Filter> filters = new ArrayList<>();
		filters.add(new Filter().withName("tag:VolumeId").withValues(volumeId));
		filters.add(new Filter().withName("tag:BackupType").withValues("copy-snapshot"));
		DescribeSnapshotsRequest snapshotRequest = new DescribeSnapshotsRequest().withFilters(filters);
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
					String pargeSnapshotId = snapshot.getSnapshotId();
					pargeSnapshot(client, pargeSnapshotId);
					logger.log("Parge EBS snapshot. snapshotId[" + pargeSnapshotId + "]");
				}
			}
		}
	}

	String getVolumeIdFromTag(AmazonEC2Async client, String snapshotId) {
		List<Tag> snapshotTag = client.describeSnapshots(new DescribeSnapshotsRequest().withSnapshotIds(snapshotId))
				.getSnapshots().get(0).getTags();
		String volumeId = null;
		for (Tag tag : snapshotTag) {
			if ("VolumeId".equals(tag.getKey())) {
				volumeId = tag.getValue();
			}
		}
		if (volumeId == null) {
			throw new RuntimeException("volumeId can not found snapshot. snapshotId[" + snapshotId + "]");
		}
		return volumeId;

	}

	private class SnapshotComparator implements Comparator<Snapshot> {
		@Override
		public int compare(Snapshot o1, Snapshot o2) {
			Date startDateO1 = o1.getStartTime();
			Date startDateO2 = o2.getStartTime();
			return startDateO1.compareTo(startDateO2);
		}
	}

	void pargeSnapshot(AmazonEC2 ec2, String snapshotId) {
		DeleteSnapshotRequest request = new DeleteSnapshotRequest(snapshotId);
		ec2.deleteSnapshot(request);
	}

	private String getStackTrace(Exception e) {
		StringWriter sw = new StringWriter();
		PrintWriter pw = new PrintWriter(sw);
		e.printStackTrace(pw);
		pw.flush();
		return sw.toString();
	}
}
