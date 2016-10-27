package jp.gr.java_conf.uzresk.aws.ope.image;

import lombok.Data;

@Data
public class ImageStateCheckAndPargeRequest {

	private String queueName;

	private String sqsEndpoint;

	private int numberOfMessages = 1;

}
