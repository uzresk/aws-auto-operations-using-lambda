package jp.gr.java_conf.uzresk.aws.ope.instance;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.List;
import java.util.concurrent.Future;

import com.amazonaws.ClientConfiguration;
import com.amazonaws.auth.DefaultAWSCredentialsProviderChain;
import com.amazonaws.regions.RegionUtils;
import com.amazonaws.services.ec2.AmazonEC2Async;
import com.amazonaws.services.ec2.AmazonEC2AsyncClient;
import com.amazonaws.services.ec2.model.DescribeInstancesRequest;
import com.amazonaws.services.ec2.model.DescribeInstancesResult;
import com.amazonaws.services.ec2.model.Instance;
import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.LambdaLogger;
import com.amazonaws.services.sqs.AmazonSQSAsync;
import com.amazonaws.services.sqs.AmazonSQSAsyncClient;
import com.amazonaws.services.sqs.model.CreateQueueRequest;
import com.amazonaws.services.sqs.model.Message;
import com.amazonaws.services.sqs.model.SendMessageRequest;
import com.amazonaws.services.sqs.model.SendMessageResult;
import com.fasterxml.jackson.databind.ObjectMapper;

public class InstanceOperation {

	private ClientConfiguration cc;

	public String getInstanceStateName(InstanceRequest instanceRequest, Context context) {

		AmazonEC2Async client = createEC2Client();
		try {
			DescribeInstancesResult result = client
					.describeInstances(new DescribeInstancesRequest().withInstanceIds(instanceRequest.getInstanceId()));
			List<Instance> instances = result.getReservations().get(0).getInstances();
			if (instances.size() != 1) {
				throw new RuntimeException("instance can not be found.");
			}
			return instances.get(0).getState().getName();

		} finally {
			client.shutdown();
		}
	}

	public void checkInstanceState(Message message, String stateName,
			InstanceCheckStateRequest checkInstanceStateRequest, Context context) {

		LambdaLogger logger = context.getLogger();

		ObjectMapper om = new ObjectMapper();
		InstanceRequest instanceRequest;
		try {
			instanceRequest = om.readValue(message.getBody(), InstanceRequest.class);
		} catch (IOException e) {
			deleteQueueMessage(message, checkInstanceStateRequest, context);
			throw new RuntimeException("SQS message could not be parsed");
		}

		long sendMessageTime = instanceRequest.getSendMessageTimeMillis()
				+ 1000 * instanceRequest.getInstanceStateCheckTimeoutSec();
		long now = System.currentTimeMillis();

		// Status check of the instance has timed out or not
		if (sendMessageTime < now) {
			deleteQueueMessage(message, checkInstanceStateRequest, context);
			throw new RuntimeException("Status check of the instance has timed out. " + instanceRequest);
		}

		AmazonEC2Async client = createEC2Client();
		try {
			DescribeInstancesResult result = client
					.describeInstances(new DescribeInstancesRequest().withInstanceIds(instanceRequest.getInstanceId()));
			List<Instance> instances = result.getReservations().get(0).getInstances();
			if (instances.size() != 1) {
				deleteQueueMessage(message, checkInstanceStateRequest, context);
				throw new RuntimeException("instance can not be found.");
			}
			Instance instance = instances.get(0);

			if (stateName.equals(instance.getState().getName())) {
				deleteQueueMessage(message, checkInstanceStateRequest, context);
				logger.log("[SUCCESS][" + instanceRequest.getInstanceId() + "][checkInstanceState][" + stateName
						+ "] Status check of the instance is completed successfully. " + instanceRequest + "]");
			}

		} finally {
			client.shutdown();
		}
	}

	SendMessageResult createQueueMessage(InstanceRequest instanceRequest, Context context) {

		LambdaLogger logger = context.getLogger();

		final String queueName = instanceRequest.getQueueName();
		final String sqsEndpoint = instanceRequest.getSqsEndpoint();

		if (queueName == null || sqsEndpoint == null) {
			logger.log("skip create queue. instanceRequest[" + instanceRequest + "]");
			return null;
		}

		AmazonSQSAsync client = createSQSClient();
		client.setEndpoint(sqsEndpoint);

		try {
			CreateQueueRequest req = new CreateQueueRequest(queueName);
			String queueUrl = client.createQueue(req).getQueueUrl();

			instanceRequest.setSendMessageTimeMillis(System.currentTimeMillis());

			SendMessageRequest sendMessage = new SendMessageRequest();
			sendMessage.setQueueUrl(queueUrl);
			ObjectMapper om = new ObjectMapper();
			sendMessage.setMessageBody(om.writeValueAsString(instanceRequest));

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

	protected void deleteQueueMessage(Message message, InstanceCheckStateRequest checkInstanceStateRequest,
			Context context) {

		AmazonSQSAsync client = createSQSClient();
		client.setEndpoint(checkInstanceStateRequest.getSqsEndpoint());
		try {
			String queueUrl = client.createQueue(checkInstanceStateRequest.getQueueName()).getQueueUrl();
			client.deleteMessage(queueUrl, message.getReceiptHandle());
		} catch (Exception e) {
			final String msg = "can not delete message. checkInstanceStateRequest[" + checkInstanceStateRequest + "]";
			throw new RuntimeException(msg, e);
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
