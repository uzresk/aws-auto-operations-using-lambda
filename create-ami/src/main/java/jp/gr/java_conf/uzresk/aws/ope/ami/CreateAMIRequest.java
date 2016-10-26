package jp.gr.java_conf.uzresk.aws.ope.ami;

import lombok.Data;

@Data
public class CreateAMIRequest {

	private String instanceId;

	private String amiName;

	private boolean noReboot = false;

	private String queueName;

	private String sqsEndpoint;

	private int amiCreatedCheckTimeoutSec = 300;

	private int generationCount = 5;

	private String createAMIRequestDate;

	private String imageId;

	private long sendMessageTimeMillis;

}
