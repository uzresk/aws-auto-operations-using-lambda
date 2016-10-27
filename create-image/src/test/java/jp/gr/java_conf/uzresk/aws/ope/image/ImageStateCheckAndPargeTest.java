package jp.gr.java_conf.uzresk.aws.ope.image;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import com.amazonaws.ClientConfiguration;

@RunWith(JUnit4.class)
public class ImageStateCheckAndPargeTest {

	@Test
	public void checkStateAndPargeAMI() {
		ImageStateCheckAndPargeRequest request = new ImageStateCheckAndPargeRequest();
		request.setSqsEndpoint("https://sqs.ap-northeast-1.amazonaws.com");
		request.setQueueName("CreateAMIQueue");
		request.setNumberOfMessages(10);

		ClientConfiguration cc = new ClientConfiguration();
		cc.setProxyHost("PROXY_HOST");
		cc.setProxyPort(8080);

		ImageCreateFunction createAMI = new ImageCreateFunction();
		createAMI.setClientConfiguration(cc);
		// createAMI.checkStateAndPargeAMI(request, new TestContext());
	}

	@Test
	public void createQueue() {

		ImageCreateRequest createAMIRequest = new ImageCreateRequest();
		createAMIRequest.setInstanceId("i-xxxxxxxxxxxxxxxx");
		createAMIRequest.setAmiName("test");
		createAMIRequest.setNoReboot(true);
		createAMIRequest.setSqsEndpoint("https://sqs.ap-northeast-1.amazonaws.com");
		createAMIRequest.setQueueName("CreateAMIQueue");
		createAMIRequest.setGenerationCount(1);
		String createAMIRequestDate = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmm"));
		createAMIRequest.setCreateAMIRequestDate(createAMIRequestDate);
		createAMIRequest.setImageId("ami-xxxxxxx");
		createAMIRequest.setSendMessageTimeMillis(System.currentTimeMillis());

		ClientConfiguration cc = new ClientConfiguration();
		cc.setProxyHost("PROXY_HOST");
		cc.setProxyPort(8080);

		ImageCreateFunction createAMI = new ImageCreateFunction();
		createAMI.setClientConfiguration(cc);
		// createAMI.createQueueMessage(createAMIRequest, new TestContext());
	}

}