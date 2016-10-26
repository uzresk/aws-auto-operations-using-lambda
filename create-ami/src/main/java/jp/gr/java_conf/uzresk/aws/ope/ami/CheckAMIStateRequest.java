package jp.gr.java_conf.uzresk.aws.ope.ami;

import lombok.Data;

@Data
public class CheckAMIStateRequest {

	private String queueName;

	private String sqsEndpoint;

	private int numberOfMessages = 1;

}
