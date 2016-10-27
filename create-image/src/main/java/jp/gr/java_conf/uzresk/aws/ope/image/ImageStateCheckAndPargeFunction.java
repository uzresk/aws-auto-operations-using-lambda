package jp.gr.java_conf.uzresk.aws.ope.image;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.Future;

import com.amazonaws.ClientConfiguration;
import com.amazonaws.auth.DefaultAWSCredentialsProviderChain;
import com.amazonaws.regions.RegionUtils;
import com.amazonaws.services.ec2.AmazonEC2;
import com.amazonaws.services.ec2.AmazonEC2Async;
import com.amazonaws.services.ec2.AmazonEC2AsyncClient;
import com.amazonaws.services.ec2.model.BlockDeviceMapping;
import com.amazonaws.services.ec2.model.CreateTagsRequest;
import com.amazonaws.services.ec2.model.CreateTagsResult;
import com.amazonaws.services.ec2.model.DeleteSnapshotRequest;
import com.amazonaws.services.ec2.model.DeregisterImageRequest;
import com.amazonaws.services.ec2.model.DescribeImagesRequest;
import com.amazonaws.services.ec2.model.DescribeImagesResult;
import com.amazonaws.services.ec2.model.Filter;
import com.amazonaws.services.ec2.model.Image;
import com.amazonaws.services.ec2.model.ImageState;
import com.amazonaws.services.ec2.model.Tag;
import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.LambdaLogger;
import com.amazonaws.services.sqs.AmazonSQSAsync;
import com.amazonaws.services.sqs.AmazonSQSAsyncClient;
import com.amazonaws.services.sqs.model.Message;
import com.amazonaws.services.sqs.model.ReceiveMessageRequest;
import com.amazonaws.services.sqs.model.ReceiveMessageResult;
import com.fasterxml.jackson.databind.ObjectMapper;

import jp.gr.java_conf.uzresk.aws.ope.image.model.ImageCreateRequest;
import jp.gr.java_conf.uzresk.aws.ope.image.model.ImageStateCheckAndPargeRequest;

public class ImageStateCheckAndPargeFunction {

	private ClientConfiguration cc;

	public void request(ImageStateCheckAndPargeRequest request, Context context) {

		LambdaLogger logger = context.getLogger();

		final String queueName = request.getQueueName();
		final String sqsEndpoint = request.getSqsEndpoint();

		// illegal parameter
		if (queueName == null || sqsEndpoint == null) {
			logger.log("[ERROR][ImageStateCheckAndParge]QueueName or SQSEndpoint is not found Parameter. ");
			throw new IllegalArgumentException(
					"QueueName or SQSEndpoint is not found Parameter. " + "[" + request + "]");
		}

		// Only the specified number, reliably acquired
		int numberOfMessages = request.getNumberOfMessages();
		for (int i = 0; i < numberOfMessages; i++) {
			AmazonSQSAsync client = createSQSClient();
			client.setEndpoint(sqsEndpoint);
			try {
				String queueUrl = client.createQueue(queueName).getQueueUrl();

				ReceiveMessageRequest req = new ReceiveMessageRequest(queueUrl).withVisibilityTimeout(5)
						.withMaxNumberOfMessages(request.getNumberOfMessages());
				Future<ReceiveMessageResult> result = client.receiveMessageAsync(req);
				while (!result.isDone()) {
					Thread.sleep(100);
				}
				result.get().getMessages().stream().forEach(s -> imageStateCheckAndParge(s, request, context));

			} catch (Exception e) {
				logger.log("[ERROR][ImageStateCheckAndParge] message[" + e.getMessage() + "] stackTrace["
						+ getStackTrace(e) + "] [" + request + "]");

			} finally {
				client.shutdown();
			}
		}
	}

	void imageStateCheckAndParge(Message message, ImageStateCheckAndPargeRequest request, Context context) {

		LambdaLogger logger = context.getLogger();

		ObjectMapper om = new ObjectMapper();
		ImageCreateRequest createAMIRequest;
		try {
			createAMIRequest = om.readValue(message.getBody(), ImageCreateRequest.class);
		} catch (IOException e) {
			deleteQueueMessage(message, request, context);
			throw new RuntimeException("SQS message could not be parsed");
		}

		long sendMessageTime = createAMIRequest.getSendMessageTimeMillis()
				+ 1000 * createAMIRequest.getImageCreatedTimeoutSec();
		long now = System.currentTimeMillis();

		// Status check of the instance has timed out or not
		if (sendMessageTime < now) {
			deleteQueueMessage(message, request, context);
			throw new RuntimeException("Status check of the instance has timed out. " + createAMIRequest);
		}

		AmazonEC2Async client = createEC2Client();
		try {
			String imageState = getImageState(client, createAMIRequest, context);
			if (ImageState.Available.toString().equals(imageState)) {
				createImageTags(client, createAMIRequest, context);
				pargeImage(client, createAMIRequest, context);
				deleteQueueMessage(message, request, context);
			}
			logger.log("[SUCCESS][" + createAMIRequest.getInstanceId() + "] "
					+ "Creation of AMI, additional tags, generation management has completed successfully. ["
					+ createAMIRequest + "]");
		} finally {
			client.shutdown();
		}

	}

	void deleteQueueMessage(Message message, ImageStateCheckAndPargeRequest request, Context context) {

		AmazonSQSAsync client = createSQSClient();
		client.setEndpoint(request.getSqsEndpoint());
		try {
			String queueUrl = client.createQueue(request.getQueueName()).getQueueUrl();
			client.deleteMessage(queueUrl, message.getReceiptHandle());
		} catch (Exception e) {
			final String msg = "can not delete message. [" + request + "]";
			throw new RuntimeException(msg, e);
		} finally {
			client.shutdown();
		}
	}

	void createImageTags(AmazonEC2Async client, ImageCreateRequest imageCreateRequest, Context context) {

		try {
			// LambdaLogger logger = context.getLogger();
			String instanceId = imageCreateRequest.getInstanceId();
			String imageId = imageCreateRequest.getImageId();

			List<Tag> tags = new ArrayList<Tag>();
			tags.add(new Tag("InstanceId", instanceId));
			tags.add(new Tag("BackupType", "image"));
			String requestDate = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmm"));
			tags.add(new Tag("RequestDate", requestDate));

			// Tag to AMI
			CreateTagsRequest createTagsRequest = new CreateTagsRequest().withResources(imageId);
			createTagsRequest.setTags(tags);
			Future<CreateTagsResult> amiTagsResult = client.createTagsAsync(createTagsRequest);
			while (!amiTagsResult.isDone()) {
				Thread.sleep(100);
			}

			// Tag to EBS Snapshot
			tags.add(new Tag("ImageId", imageId)); // snapshotにはimageIdを付けておく。

			List<String> snapshotIds = getSnapshotIdsFromImageId(client, imageCreateRequest, context);
			CreateTagsRequest snapshotTagsRequest = new CreateTagsRequest().withResources(snapshotIds);

			snapshotTagsRequest.setTags(tags);
			Future<CreateTagsResult> snapshotTagsResult = client.createTagsAsync(snapshotTagsRequest);
			while (!snapshotTagsResult.isDone()) {
				Thread.sleep(100);
			}
		} catch (Exception e) {
			context.getLogger().log("[ERROR][ImageStateCheckAndParge] message[" + e.getMessage() + "] stackTrace["
					+ getStackTrace(e) + "] [" + imageCreateRequest + "]");
		}

	}

	List<String> getSnapshotIdsFromImageId(AmazonEC2Async client, ImageCreateRequest request, Context context) {

		// LambdaLogger logger = context.getLogger();
		String imageId = request.getImageId();

		DescribeImagesResult result = client.describeImages(new DescribeImagesRequest().withImageIds(imageId));

		List<String> snapshotIds = new ArrayList<String>();

		for (Image image : result.getImages()) {
			for (BlockDeviceMapping block : image.getBlockDeviceMappings()) {
				snapshotIds.add(block.getEbs().getSnapshotId());
			}
		}
		return snapshotIds;
	}

	String getImageState(AmazonEC2Async client, ImageCreateRequest createAMIRequest, Context context) {
		// LambdaLogger logger = context.getLogger();
		String imageId = createAMIRequest.getImageId();

		DescribeImagesRequest req = new DescribeImagesRequest().withImageIds(imageId);
		DescribeImagesResult result = client.describeImages(req);
		return result.getImages().get(0).getState();
	}

	void pargeImage(AmazonEC2Async client, ImageCreateRequest request, Context context) {

		LambdaLogger logger = context.getLogger();
		String instanceId = request.getInstanceId();
		int generationCount = request.getGenerationCount();

		// Find Image use tag(InstanceId)
		Filter filter = new Filter();
		filter.withName("tag:InstanceId").withValues(instanceId);
		DescribeImagesRequest describeImageRequest = new DescribeImagesRequest().withFilters(filter);
		DescribeImagesResult result = client.describeImages(describeImageRequest);

		// Sort in ascending order of RequestDate
		List<Image> images = result.getImages();
		Collections.sort(images, new ImagesComparator());

		// debug
		logger.log("image size." + images.size());
		for (Image image : images) {
			logger.log(image.getTags().toString());
		}

		List<String> pargeList = new ArrayList<>();
		int imagesSize = images.size();
		if (generationCount < imagesSize) {
			for (int i = 0; i < imagesSize - generationCount; i++) {
				Image image = images.get(i);
				if (ImageState.Available.toString().equals(image.getState())) {
					pargeImages(client, image);
					String imageId = image.getImageId();
					logger.log("parge image. imageId[" + imageId + "] instanceId[" + instanceId + "] generationCount["
							+ generationCount + "]");
					pargeList.add(imageId);
				}
			}
		}
	}

	private class ImagesComparator implements Comparator<Image> {
		@Override
		public int compare(Image o1, Image o2) {
			long o1RequestDate = 0;
			for (Tag tag : o1.getTags()) {
				if ("RequestDate".equals(tag.getKey())) {
					o1RequestDate = Long.parseLong(tag.getValue());
				}
			}
			long o2RequestDate = 0;
			for (Tag tag : o2.getTags()) {
				if ("RequestDate".equals(tag.getKey())) {
					o2RequestDate = Long.parseLong(tag.getValue());
				}
			}
			if (o1RequestDate - o2RequestDate > 0) {
				return 1;
			} else if (o1RequestDate - o2RequestDate < 0) {
				return -1;
			} else {
				return 0;
			}
		}
	}

	private void pargeImages(AmazonEC2 ec2, Image image) {
		String imageId = image.getImageId();
		ec2.deregisterImage(new DeregisterImageRequest(imageId));
		for (BlockDeviceMapping block : image.getBlockDeviceMappings()) {
			String snapshotId = block.getEbs().getSnapshotId();
			ec2.deleteSnapshot(new DeleteSnapshotRequest().withSnapshotId(snapshotId));
		}
	}

	protected AmazonEC2Async createEC2Client() {
		String regionName = System.getenv("AWS_DEFAULT_REGION");

		return RegionUtils.getRegion(regionName).createClient(AmazonEC2AsyncClient.class,
				new DefaultAWSCredentialsProviderChain(), cc);
	}

	protected AmazonSQSAsync createSQSClient() {
		String regionName = System.getenv("AWS_DEFAULT_REGION");
		return RegionUtils.getRegion(regionName).createClient(AmazonSQSAsyncClient.class,
				new DefaultAWSCredentialsProviderChain(), cc);
	}

	void setClientConfiguration(ClientConfiguration cc) {
		this.cc = cc;
	}

	ClientConfiguration getClientConfiguration() {
		if (this.cc == null) {
			return new ClientConfiguration();
		}
		return this.cc;
	}

	protected String getStackTrace(Exception e) {
		StringWriter sw = new StringWriter();
		PrintWriter pw = new PrintWriter(sw);
		e.printStackTrace(pw);
		pw.flush();
		return sw.toString();
	}
}
