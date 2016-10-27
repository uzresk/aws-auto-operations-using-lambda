package jp.gr.java_conf.uzresk.aws.ope.image.model;

import lombok.Data;

@Data
public class ImageCreateRequest {

	private String instanceId;

	private String amiName;

	private boolean noReboot = false;

	private String queueName;

	private String sqsEndpoint;

	private int imageCreatedTimeoutSec = 300;

	private int generationCount = 5;

	private String createAMIRequestDate;

	private String imageId;

	private long sendMessageTimeMillis;

}
