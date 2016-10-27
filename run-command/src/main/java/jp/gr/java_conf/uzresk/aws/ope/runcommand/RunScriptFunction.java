package jp.gr.java_conf.uzresk.aws.ope.runcommand;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

import com.amazonaws.ClientConfiguration;
import com.amazonaws.auth.DefaultAWSCredentialsProviderChain;
import com.amazonaws.regions.RegionUtils;
import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.LambdaLogger;
import com.amazonaws.services.simplesystemsmanagement.AWSSimpleSystemsManagementAsync;
import com.amazonaws.services.simplesystemsmanagement.AWSSimpleSystemsManagementAsyncClient;
import com.amazonaws.services.simplesystemsmanagement.model.CommandStatus;
import com.amazonaws.services.simplesystemsmanagement.model.NotificationConfig;
import com.amazonaws.services.simplesystemsmanagement.model.SendCommandRequest;
import com.amazonaws.services.simplesystemsmanagement.model.SendCommandResult;

import jp.gr.java_conf.uzresk.aws.ope.runcommand.model.RunScriptRequest;

public class RunScriptFunction {

	private ClientConfiguration cc;

	public void execute(RunScriptRequest rc, Context context) {

		LambdaLogger logger = context.getLogger();
		logger.log("Run Command Start. Run Configuration:[" + rc + "]");

		String regionName = System.getenv("AWS_DEFAULT_REGION");

		AWSSimpleSystemsManagementAsync client = RegionUtils.getRegion(regionName).createClient(
				AWSSimpleSystemsManagementAsyncClient.class, new DefaultAWSCredentialsProviderChain(),
				getClientConfiguration());

		try {

			SendCommandRequest req = new SendCommandRequest();
			req.setInstanceIds(rc.getInstanceIds());
			req.setDocumentName(rc.getDocumentName());
			req.setParameters(rc.getParameters());

			req.setOutputS3BucketName(rc.getOutputS3BucketName());
			req.setOutputS3KeyPrefix(rc.getOutputS3KeyPrefix());

			// SNS settings
			if (isValidSNSSettings(rc, context)) {
				req.setServiceRoleArn(rc.getServiceRoleArn());
				NotificationConfig nc = new NotificationConfig();
				nc.setNotificationArn(rc.getNotificationArn());
				nc.setNotificationEvents(rc.getNotificationEvents());
				req.setNotificationConfig(nc);
			}

			Future<SendCommandResult> result = client.sendCommandAsync(req);

			SendCommandResult r;
			while (!result.isDone()) {
				Thread.sleep(100);
			}
			r = result.get();
			if (CommandStatus.Failed.name().equals(r.getCommand().getStatus())) {
				logger.log("[ERROR] execution failure. Commands[" + r.toString() + "]");
			} else {
				logger.log("[SUCCESS]Execution of RunCommand has completed successfully.[" + rc + "]");
			}
		} catch (InterruptedException | ExecutionException e) {
			logger.log("[ERROR] execution run commands." + e.getMessage());

		} finally {
			client.shutdown();
		}

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

	private boolean isValidSNSSettings(RunScriptRequest rc, Context context) {

		LambdaLogger logger = context.getLogger();

		if (rc.getServiceRoleArn() != null && rc.getNotificationArn() != null && rc.getNotificationEvents() != null) {
			return true;
		} else {
			logger.log("Since the A setting of is missing, skip the SNS[" + rc + "]");
			return false;
		}
	}
}
