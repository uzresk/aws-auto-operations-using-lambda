package jp.gr.java_conf.uzresk.aws.ope.instance.model;

import lombok.Data;

@Data
public class InstanceCheckStateRequest {

	private String queueName;

	private String sqsEndpoint;

	private int numberOfMessages = 10;

}
