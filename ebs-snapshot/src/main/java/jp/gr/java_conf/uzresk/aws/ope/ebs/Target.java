package jp.gr.java_conf.uzresk.aws.ope.ebs;

public class Target {

	private String target;

	public void setTarget(String target) {
		this.target = target;
	}

	public String getTarget() {
		return this.target;
	}

	@Override
	public String toString() {
		return "Condition [target=" + target + "]";
	}
}
