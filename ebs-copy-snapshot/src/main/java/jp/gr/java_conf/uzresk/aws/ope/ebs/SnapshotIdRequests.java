package jp.gr.java_conf.uzresk.aws.ope.ebs;

import java.util.List;

import lombok.Data;

@Data
public class SnapshotIdRequests {

	private List<SnapshotIdRequest> volumeIdRequests;

}
