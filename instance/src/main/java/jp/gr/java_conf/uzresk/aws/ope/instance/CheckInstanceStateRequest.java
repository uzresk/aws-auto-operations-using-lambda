package jp.gr.java_conf.uzresk.aws.ope.instance;

public class CheckInstanceStateRequest {

	private String queueName;

	private String sqsEndpoint;

	private int numberOfMessages = 1;

	public void setQueueName(String queueName) {
		this.queueName = queueName;
	}

	public String getQueueName() {
		return this.queueName;
	}

	public void setSqsEndpoint(String sqsEndpoint) {
		this.sqsEndpoint = sqsEndpoint;
	}

	public String getSqsEndpoint() {
		return this.sqsEndpoint;
	}

	public void setNumberOfMessages(int maxNumberOfMessages) {
		this.numberOfMessages = maxNumberOfMessages;
	}

	public int getNumberOfMessages() {
		return this.numberOfMessages;
	}

	@Override
	public String toString() {
		return "CheckInstanceStateRequest [queueName=" + queueName + ", sqsEndpoint=" + sqsEndpoint
				+ ", numberOfMessages=" + numberOfMessages + "]";
	}
}
