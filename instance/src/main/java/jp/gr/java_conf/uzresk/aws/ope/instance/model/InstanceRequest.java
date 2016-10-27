package jp.gr.java_conf.uzresk.aws.ope.instance.model;

import lombok.Data;

@Data
public class InstanceRequest {

	private String instanceId;

	private String queueName;

	private String sqsEndpoint;

	private int instanceStateCheckTimeoutSec = 300;

	private long sendMessageTimeMillis;

}
