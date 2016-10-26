package jp.gr.java_conf.uzresk.aws.ope.ami;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import com.amazonaws.ClientConfiguration;

import jp.gr.java_conf.uzresk.aws.ope.TestContext;

@RunWith(JUnit4.class)
public class CreateAMITest {

	@Test
	public void createAMIRequestTest() {

		CreateAMIRequest createAMIRequest = new CreateAMIRequest();
		createAMIRequest.setInstanceId("i-xxxxxxxxxxxxxxxx");
		createAMIRequest.setAmiName("test");
		createAMIRequest.setNoReboot(true);
		createAMIRequest.setSqsEndpoint("https://sqs.ap-northeast-1.amazonaws.com");
		createAMIRequest.setQueueName("CreateAMIQueue");
		createAMIRequest.setGenerationCount(2);

		ClientConfiguration cc = new ClientConfiguration();
		cc.setProxyHost("PROXY_HOST");
		cc.setProxyPort(8080);

		CreateAMI createAMI = new CreateAMI();
		createAMI.setClientConfiguration(cc);
		createAMI.request(createAMIRequest, new TestContext());
	}

	@Test
	public void checkStateAndPargeAMI() {
		CheckAMIStateRequest request = new CheckAMIStateRequest();
		request.setSqsEndpoint("https://sqs.ap-northeast-1.amazonaws.com");
		request.setQueueName("CreateAMIQueue");
		request.setNumberOfMessages(10);

		ClientConfiguration cc = new ClientConfiguration();
		cc.setProxyHost("PROXY_HOST");
		cc.setProxyPort(8080);

		CreateAMI createAMI = new CreateAMI();
		createAMI.setClientConfiguration(cc);
//		createAMI.checkStateAndPargeAMI(request, new TestContext());
	}

	@Test
	public void createQueue() {

		CreateAMIRequest createAMIRequest = new CreateAMIRequest();
		createAMIRequest.setInstanceId("i-xxxxxxxxxxxxxxxx");
		createAMIRequest.setAmiName("test");
		createAMIRequest.setNoReboot(true);
		createAMIRequest.setSqsEndpoint("https://sqs.ap-northeast-1.amazonaws.com");
		createAMIRequest.setQueueName("CreateAMIQueue");
		createAMIRequest.setGenerationCount(1);
		String createAMIRequestDate = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmm"));
		createAMIRequest.setCreateAMIRequestDate(createAMIRequestDate);
		createAMIRequest.setImageId("ami-e154f480");
		createAMIRequest.setSendMessageTimeMillis(System.currentTimeMillis());

		ClientConfiguration cc = new ClientConfiguration();
		cc.setProxyHost("PROXY_HOST");
		cc.setProxyPort(8080);

		CreateAMI createAMI = new CreateAMI();
		createAMI.setClientConfiguration(cc);
//		createAMI.createQueueMessage(createAMIRequest, new TestContext());
	}


}