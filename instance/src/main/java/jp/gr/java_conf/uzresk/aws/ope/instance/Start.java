package jp.gr.java_conf.uzresk.aws.ope.instance;

import java.util.Arrays;
import java.util.concurrent.Future;

import com.amazonaws.services.ec2.AmazonEC2Async;
import com.amazonaws.services.ec2.model.StartInstancesRequest;
import com.amazonaws.services.ec2.model.StartInstancesResult;
import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.LambdaLogger;
import com.amazonaws.services.sqs.AmazonSQSAsync;
import com.amazonaws.services.sqs.model.ReceiveMessageRequest;
import com.amazonaws.services.sqs.model.ReceiveMessageResult;

public class Start extends InstanceOperation {

	public void requests(InstanceRequests instanceRequests, Context context) {
		for (InstanceRequest instanceRequest : instanceRequests.getInstanceRequest()) {
			request(instanceRequest, context);
		}
	}

	public void request(InstanceRequest instanceRequest, Context context) {

		LambdaLogger logger = context.getLogger();

		try {
			String instanceState = getInstanceStateName(instanceRequest, context);
			// instance can not be found.
			if (instanceState == null) {
				return;
			}

			if (!"stopped".equals(instanceState)) {
				logger.log("instance state is not stopped.");
			} else {
				// start ec2 instance request.
				startInstance(instanceRequest, context);
			}

			// create queue
			createQueueMessage(instanceRequest, context);

			logger.log("[SUCCESS][" + instanceRequest.getInstanceId() + "][StartInstanceRequest]"
					+ "Start request of the instance has completed successfully." + instanceRequest);

		} catch (Exception e) {
			logger.log("[ERROR][" + instanceRequest.getInstanceId() + "][StartInstanceRequest] message["
					+ e.getMessage() + "] stackTrace[" + getStackTrace(e) + "]" + instanceRequest);
		}
	}

	public void checkInstanceState(CheckInstanceStateRequest checkInstanceStateRequest, Context context) {

		LambdaLogger logger = context.getLogger();

		final String queueName = checkInstanceStateRequest.getQueueName();
		final String sqsEndpoint = checkInstanceStateRequest.getSqsEndpoint();

		// illegal parameter
		if (queueName == null || sqsEndpoint == null) {
			logger.log("[ERROR][checkInstanceStatus][running]QueueName or SQSEndpoint is not found Parameter. ");
			throw new IllegalArgumentException("QueueName or SQSEndpoint is not found Parameter. "
					+ "CheckInstanceStateRequest[" + checkInstanceStateRequest + "]");
		}

		AmazonSQSAsync client = createSQSClient();
		client.setEndpoint(sqsEndpoint);

		// Only the specified number, reliably acquired
		int numberOfMessages = checkInstanceStateRequest.getNumberOfMessages();
		for (int i = 0; i < numberOfMessages; i++) {
			try {
				String queueUrl = client.createQueue(queueName).getQueueUrl();

				ReceiveMessageRequest req = new ReceiveMessageRequest(queueUrl).withVisibilityTimeout(5)
						.withMaxNumberOfMessages(checkInstanceStateRequest.getNumberOfMessages());
				Future<ReceiveMessageResult> result = client.receiveMessageAsync(req);
				while (!result.isDone()) {
					Thread.sleep(100);
				}
				result.get().getMessages().stream()
						.forEach(s -> checkInstanceState(s, "running", checkInstanceStateRequest, context));

			} catch (Exception e) {
				logger.log("[ERROR][checkInstanceStatus][running]message[" + e.getMessage() + "] stackTrace["
						+ getStackTrace(e) + "] CheckInstanceStateRequest[" + checkInstanceStateRequest + "]");

			} finally {
				client.shutdown();
			}
		}
	}

	StartInstancesResult startInstance(InstanceRequest instanceRequest, Context context) {
		AmazonEC2Async client = createEC2Client();
		try {
			StartInstancesRequest req = new StartInstancesRequest();
			req.setInstanceIds(Arrays.asList(instanceRequest.getInstanceId()));
			Future<StartInstancesResult> result = client.startInstancesAsync(req);
			while (!result.isDone()) {
				Thread.sleep(100);
			}
			return result.get();

		} catch (Exception e) {
			throw new RuntimeException("unexpected error has occured in the start instance request.", e);
		} finally {
			client.shutdown();
		}
	}

}
