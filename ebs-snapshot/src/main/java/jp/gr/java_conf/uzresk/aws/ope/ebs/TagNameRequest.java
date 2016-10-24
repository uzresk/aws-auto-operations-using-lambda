package jp.gr.java_conf.uzresk.aws.ope.ebs;

import lombok.Data;

@Data
public class TagNameRequest {

	private String tagName;

	private int generationCount = 5;

}
