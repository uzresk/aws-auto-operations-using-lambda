package jp.gr.java_conf.uzresk.aws.ope.image;

import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;

import com.amazonaws.ClientConfiguration;
import com.amazonaws.auth.DefaultAWSCredentialsProviderChain;
import com.amazonaws.regions.RegionUtils;
import com.amazonaws.services.ec2.AmazonEC2;
import com.amazonaws.services.ec2.AmazonEC2Async;
import com.amazonaws.services.ec2.AmazonEC2AsyncClient;
import com.amazonaws.services.ec2.model.DeleteSnapshotRequest;
import com.amazonaws.services.ec2.model.DescribeSnapshotsRequest;
import com.amazonaws.services.ec2.model.DescribeSnapshotsResult;
import com.amazonaws.services.ec2.model.Filter;
import com.amazonaws.services.ec2.model.Snapshot;
import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.LambdaLogger;
import com.amazonaws.services.lambda.runtime.RequestStreamHandler;
import com.fasterxml.jackson.databind.ObjectMapper;

import jp.gr.java_conf.uzresk.aws.ope.image.model.DeregisterImageRequest;

public class DeregisterImageFunction implements RequestStreamHandler {

	private static ClientConfiguration cc = new ClientConfiguration();

	@Override
	public void handleRequest(InputStream is, OutputStream os, Context context) {

		LambdaLogger logger = context.getLogger();
		String regionName = System.getenv("AWS_DEFAULT_REGION");
		AmazonEC2Async client = RegionUtils.getRegion(regionName).createClient(AmazonEC2AsyncClient.class,
				new DefaultAWSCredentialsProviderChain(), cc);
		try {
			ObjectMapper om = new ObjectMapper();
			DeregisterImageRequest event = om.readValue(is, DeregisterImageRequest.class);
			String imageId = event.getDetail().getRequestParameters().getImageId();

			logger.log("Deregister AMI parge snapshot Start. ImageId[" + imageId + "]");

			List<Snapshot> snapshots = describeSnapshot(client, imageId, context);
			if (snapshots.size() == 0) {
				logger.log("Target of snapshot there is nothing.");
			} else {
				snapshots.stream().forEach(s -> pargeSnapshot(client, s.getSnapshotId(), context));
			}
			logger.log("[SUCCESS][DeregisterImage]has been completed successfully." + imageId);
		} catch (Exception e) {
			logger.log("[ERROR][DeregisterImage]An unexpected error has occurred. message[" + e.getMessage() + "]");
		} finally {
			client.shutdown();
		}
	}

	private List<Snapshot> describeSnapshot(AmazonEC2Async client, String imageId, Context context) {

		Filter filter = new Filter().withName("description")
				.withValues("Created by CreateImage(*) for " + imageId + " from *");
		DescribeSnapshotsRequest request = new DescribeSnapshotsRequest().withFilters(filter);
		DescribeSnapshotsResult result = client.describeSnapshots(request);
		return result.getSnapshots();
	}

	private void pargeSnapshot(AmazonEC2 ec2, String snapshotId, Context context) {

		DeleteSnapshotRequest request = new DeleteSnapshotRequest(snapshotId);
		ec2.deleteSnapshot(request);
		context.getLogger().log("Parge Snapshot. snapshotId[" + snapshotId + "]");
	}
}
