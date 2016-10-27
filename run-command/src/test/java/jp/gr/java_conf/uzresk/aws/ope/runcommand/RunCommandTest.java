package jp.gr.java_conf.uzresk.aws.ope.runcommand;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import com.amazonaws.ClientConfiguration;
import com.amazonaws.services.simplesystemsmanagement.model.NotificationEvent;

@RunWith(JUnit4.class)
public class RunCommandTest {

	@Test
	public void shell() {

		RunScriptRequest rc = new RunScriptRequest();
		rc.setDocumentName("AWS-RunShellScript");
		rc.setInstanceIds(Arrays.asList("i-xxxxxxxx"));

		Map<String, List<String>> parameter = new HashMap<>();
		parameter.put("commands", Arrays.asList("ifconfig"));
		parameter.put("workingDirectory", null);
		parameter.put("executionTimeout", Arrays.asList("600"));
		rc.setParameters(parameter);

		rc.setOutputS3BucketName("xxxxxxxxxxxxxxxxxx");
		rc.setOutputS3KeyPrefix("i-xxxxxxxx");
		rc.setServiceRoleArn("arn:aws:iam::xxxxxxxxxxxxx:role/RunCommandSNS");
		rc.setNotificationArn("arn:aws:sns:ap-northeast-1:xxxxxxxxxx:xxxxxxxxxxxxxx");
		rc.setNotificationEvents(Arrays.asList(NotificationEvent.Success.name()));

		ClientConfiguration cc = new ClientConfiguration();
		cc.setProxyHost("PROXY_HOST");
		cc.setProxyPort(8080);

		RunScriptFunction runCommand = new RunScriptFunction();
		runCommand.setClientConfiguration(cc);
		// runCommand.execute(rc, new TestContext());

	}

	@Test
	public void powershell() {

		RunScriptRequest rc = new RunScriptRequest();
		rc.setDocumentName("AWS-RunPowerShellScript");
		rc.setInstanceIds(Arrays.asList("i-xxxxxxxx"));

		Map<String, List<String>> parameter = new HashMap<>();
		parameter.put("commands", Arrays.asList("Get-Process | Export-CSV process.txt"));
		parameter.put("workingDirectory", Arrays.asList("C:\\"));
		parameter.put("executionTimeout", Arrays.asList("600"));
		rc.setParameters(parameter);

		rc.setOutputS3BucketName("xxxxxxxxxxxxxxxxxx");
		rc.setOutputS3KeyPrefix("i-xxxxxxxx");
		rc.setServiceRoleArn("arn:aws:iam::xxxxxxxxxxxxx:role/RunCommandSNS");
		rc.setNotificationArn("arn:aws:sns:ap-northeast-1:xxxxxxxxxx:xxxxxxxxxxxxxx");
		rc.setNotificationEvents(Arrays.asList(NotificationEvent.Success.name()));

		ClientConfiguration cc = new ClientConfiguration();
		cc.setProxyHost("PROXY_HOST");
		cc.setProxyPort(8080);

		RunScriptFunction runCommand = new RunScriptFunction();
		runCommand.setClientConfiguration(cc);
		// runCommand.execute(rc, new TestContext());
	}
}
