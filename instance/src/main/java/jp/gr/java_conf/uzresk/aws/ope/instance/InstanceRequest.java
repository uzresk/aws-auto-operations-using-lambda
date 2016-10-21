package jp.gr.java_conf.uzresk.aws.ope.instance;

public class InstanceRequest {

	private String instanceId;

	private String queueName;

	private String sqsEndpoint;

	private int instanceStateCheckTimeoutSec = 300;

	private long sendMessageTimeMillis;

	public void setInstanceId(String instanceId) {
		this.instanceId = instanceId;
	}

	public String getInstanceId() {
		return this.instanceId;
	}

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

	public void setInstanceStateCheckTimeoutSec(int instanceStateCheckTimeoutSec) {
		this.instanceStateCheckTimeoutSec = instanceStateCheckTimeoutSec;
	}

	public int getInstanceStateCheckTimeoutSec() {
		return this.instanceStateCheckTimeoutSec;
	}

	public void setSendMessageTimeMillis(long sendMessageTimeMillis) {
		this.sendMessageTimeMillis = sendMessageTimeMillis;
	}

	public long getSendMessageTimeMillis() {
		return this.sendMessageTimeMillis;
	}

	@Override
	public String toString() {
		return "InstanceRequest [instanceId=" + instanceId + ", queueName=" + queueName + ", sqsEndpoint=" + sqsEndpoint
				+ ", instanceStateCheckTimeoutSec=" + instanceStateCheckTimeoutSec + ", sendMessageTimeMillis="
				+ sendMessageTimeMillis + "]";
	}

}
