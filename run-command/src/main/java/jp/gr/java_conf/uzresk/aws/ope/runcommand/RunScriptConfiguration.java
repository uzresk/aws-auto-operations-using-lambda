package jp.gr.java_conf.uzresk.aws.ope.runcommand;

import java.util.List;
import java.util.Map;

public class RunScriptConfiguration {

	private String documentName;

	private List<String> instanceIds;

	private Map<String, List<String>> parameters;

	private String outputS3BucketName;

	private String outputS3KeyPrefix;

	private String serviceRoleArn;

	private String notificationArn;

	private List<String> notificationEvents;

	public void setDocumentName(String documentName) {
		this.documentName = documentName;
	}

	public String getDocumentName() {
		return this.documentName;
	}

	public void setInstanceIds(List<String> instanceIds) {
		this.instanceIds = instanceIds;
	}

	public List<String> getInstanceIds() {
		return this.instanceIds;
	}

	public void setParameters(Map<String, List<String>> parameters) {
		this.parameters = parameters;
	}

	public Map<String, List<String>> getParameters() {
		return this.parameters;
	}

	public void setServiceRoleArn(String serviceRoleArn) {
		this.serviceRoleArn = serviceRoleArn;
	}

	public String getServiceRoleArn() {
		return this.serviceRoleArn;
	}

	public void setOutputS3BucketName(String outputS3BucketName) {
		this.outputS3BucketName = outputS3BucketName;
	}

	public String getOutputS3BucketName() {
		return this.outputS3BucketName;
	}

	public void setNotificationArn(String notificationArn) {
		this.notificationArn = notificationArn;
	}

	public String getNotificationArn() {
		return this.notificationArn;
	}

	public void setOutputS3KeyPrefix(String outputS3KeyPrefix) {
		this.outputS3KeyPrefix = outputS3KeyPrefix;
	}

	public String getOutputS3KeyPrefix() {
		return this.outputS3KeyPrefix;
	}

	public void setNotificationEvents(List<String> notificationEvents) {
		this.notificationEvents = notificationEvents;
	}

	public List<String> getNotificationEvents() {
		return this.notificationEvents;
	}

	@Override
	public String toString() {
		return "RunScriptConfiguration [documentName=" + documentName + ", instanceIds=" + instanceIds + ", parameters="
				+ parameters + ", outputS3BucketName=" + outputS3BucketName + ", outputS3KeyPrefix=" + outputS3KeyPrefix
				+ ", serviceRoleArn=" + serviceRoleArn + ", notificationArn=" + notificationArn
				+ ", notificationEvents=" + notificationEvents + "]";
	}

}
