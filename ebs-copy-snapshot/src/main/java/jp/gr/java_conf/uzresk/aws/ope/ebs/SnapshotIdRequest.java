package jp.gr.java_conf.uzresk.aws.ope.ebs;

import lombok.Data;

@Data
public class SnapshotIdRequest {

	private String sourceSnapshotId;

	private String destinationRegion;

	private int generationCount = 5;
}
