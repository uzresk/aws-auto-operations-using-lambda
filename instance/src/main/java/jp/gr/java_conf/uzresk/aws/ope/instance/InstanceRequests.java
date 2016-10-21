package jp.gr.java_conf.uzresk.aws.ope.instance;

import java.util.List;

public class InstanceRequests {

	private List<InstanceRequest> instanceRequests;

	public void setInstanceRequests(List<InstanceRequest> instanceRequests) {
		this.instanceRequests = instanceRequests;
	}

	public List<InstanceRequest> getInstanceRequest() {
		return this.instanceRequests;
	}

	@Override
	public String toString() {
		return "InstanceRequests [instanceRequests=" + instanceRequests + "]";
	}
}
