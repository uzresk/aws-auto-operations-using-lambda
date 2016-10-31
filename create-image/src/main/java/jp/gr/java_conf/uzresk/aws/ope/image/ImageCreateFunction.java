package jp.gr.java_conf.uzresk.aws.ope.image;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.Future;

import com.amazonaws.ClientConfiguration;
import com.amazonaws.auth.DefaultAWSCredentialsProviderChain;
import com.amazonaws.regions.RegionUtils;
import com.amazonaws.services.ec2.AmazonEC2Async;
import com.amazonaws.services.ec2.AmazonEC2AsyncClient;
import com.amazonaws.services.ec2.model.CreateImageRequest;
import com.amazonaws.services.ec2.model.CreateImageResult;
import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.LambdaLogger;
import com.amazonaws.services.sqs.AmazonSQSAsync;
import com.amazonaws.services.sqs.AmazonSQSAsyncClient;
import com.amazonaws.services.sqs.model.CreateQueueRequest;
import com.amazonaws.services.sqs.model.SendMessageRequest;
import com.amazonaws.services.sqs.model.SendMessageResult;
import com.fasterxml.jackson.databind.ObjectMapper;

import jp.gr.java_conf.uzresk.aws.lambda.LambdaLock;
import jp.gr.java_conf.uzresk.aws.ope.image.model.ImageCreateRequest;
import jp.gr.java_conf.uzresk.aws.ope.image.model.ImageCreateRequests;

public class ImageCreateFunction {

	private ClientConfiguration cc;

	public void request(ImageCreateRequest request, Context context) {

		LambdaLogger logger = context.getLogger();

		try {
			String instanceId = request.getInstanceId();
			if (instanceId == null) {
				throw new IllegalArgumentException("instance id is null.");
			}
			if (request.getAmiName() == null) {
				throw new IllegalArgumentException("AMI Name is null.");
			}

			boolean isLockAcquisition = new LambdaLock().lock(instanceId, context);
			if (!isLockAcquisition) {
				logger.log("[ERROR][CreateImage][" + instanceId + "]You can not acquire a lock.");
				return;
			}

			String createAMIRequestDate = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmm"));
			request.setCreateAMIRequestDate(createAMIRequestDate);
			request.setAmiName(request.getAmiName() + "-" + request.getInstanceId() + "-" + createAMIRequestDate);

			String imageId = createAMI(request, context);
			request.setImageId(imageId);

			createQueueMessage(request, context);

			logger.log("[SUCCESS][" + request.getInstanceId() + "][CreateAMIRequest]"
					+ "Start request of the instance has completed successfully." + request);

		} catch (Exception e) {
			logger.log("[ERROR][" + request.getInstanceId() + "][CreateAMIRequest] message[" + e.getMessage()
					+ "] stackTrace[" + getStackTrace(e) + "]" + request);
		}
	}

	public void requests(ImageCreateRequests requests, Context context) {
		for (ImageCreateRequest request : requests.getImageCreateRequests()) {
			request(request, context);
		}
	}

	String createAMI(ImageCreateRequest request, Context context) {

		LambdaLogger logger = context.getLogger();

		AmazonEC2Async client = createEC2Client();

		String imageId = null;
		try {
			Future<CreateImageResult> result = client
					.createImageAsync(new CreateImageRequest(request.getInstanceId(), request.getAmiName())
							.withNoReboot(request.isNoReboot()));
			while (!result.isDone()) {
				Thread.sleep(1000);
			}
			imageId = result.get().getImageId();

			logger.log("AMI Create Request End. instanceId[" + request.getInstanceId() + "] noReboot["
					+ request.isNoReboot() + "] imageId[" + imageId + "]");
		} catch (Exception e) {
			throw new RuntimeException("An unexpected error at the time of AMI creation has occurred", e);
		} finally {
			client.shutdown();
		}
		return imageId;
	}

	SendMessageResult createQueueMessage(ImageCreateRequest request, Context context) {

		LambdaLogger logger = context.getLogger();

		final String queueName = request.getQueueName();
		final String sqsEndpoint = request.getSqsEndpoint();

		if (queueName == null || sqsEndpoint == null) {
			logger.log("skip create queue. [" + request + "]");
			return null;
		}

		AmazonSQSAsync client = createSQSClient();
		client.setEndpoint(sqsEndpoint);
		request.setSendMessageTimeMillis(System.currentTimeMillis());

		try {
			CreateQueueRequest req = new CreateQueueRequest(queueName);
			String queueUrl = client.createQueue(req).getQueueUrl();

			SendMessageRequest sendMessage = new SendMessageRequest();
			sendMessage.setQueueUrl(queueUrl);
			ObjectMapper om = new ObjectMapper();
			sendMessage.setMessageBody(om.writeValueAsString(request));

			Future<SendMessageResult> result = client.sendMessageAsync(sendMessage);
			while (!result.isDone()) {
				Thread.sleep(100);
			}
			return result.get();

		} catch (Exception e) {
			throw new RuntimeException("unexpected error occured in the create queue request.", e);
		} finally {
			client.shutdown();
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
