package jp.gr.java_conf.uzresk.aws.ope.image;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import com.amazonaws.ClientConfiguration;

import jp.gr.java_conf.uzresk.aws.ope.image.model.ImageCreateRequest;

@RunWith(JUnit4.class)
public class ImageCreateTest {

	@Test
	public void createAMIRequestTest() {

		ImageCreateRequest createAMIRequest = new ImageCreateRequest();
		createAMIRequest.setInstanceId("i-xxxxxxxxxxxxxxxx");
		createAMIRequest.setAmiName("test");
		createAMIRequest.setNoReboot(true);
		createAMIRequest.setSqsEndpoint("https://sqs.ap-northeast-1.amazonaws.com");
		createAMIRequest.setQueueName("CreateAMIQueue");
		createAMIRequest.setGenerationCount(2);

		ClientConfiguration cc = new ClientConfiguration();
		cc.setProxyHost("PROXY_HOST");
		cc.setProxyPort(8080);

		ImageCreateFunction createAMI = new ImageCreateFunction();
		createAMI.setClientConfiguration(cc);
		// createAMI.request(createAMIRequest, new TestContext());
	}
}