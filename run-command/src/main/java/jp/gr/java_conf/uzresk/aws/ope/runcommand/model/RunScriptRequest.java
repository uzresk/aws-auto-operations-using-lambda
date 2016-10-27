package jp.gr.java_conf.uzresk.aws.ope.runcommand.model;

import java.util.List;
import java.util.Map;

import lombok.Data;

@Data
public class RunScriptRequest {

	private String documentName;

	private List<String> instanceIds;

	private Map<String, List<String>> parameters;

	private String outputS3BucketName;

	private String outputS3KeyPrefix;

	private String serviceRoleArn;

	private String notificationArn;

	private List<String> notificationEvents;

}
