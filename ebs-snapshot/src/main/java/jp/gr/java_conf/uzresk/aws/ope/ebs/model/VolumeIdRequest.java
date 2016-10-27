package jp.gr.java_conf.uzresk.aws.ope.ebs.model;

import lombok.Data;

@Data
public class VolumeIdRequest {

	private String volumeId;

	private int generationCount = 5;
}
