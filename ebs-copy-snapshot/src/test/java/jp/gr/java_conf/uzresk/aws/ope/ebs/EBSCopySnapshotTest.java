package jp.gr.java_conf.uzresk.aws.ope.ebs;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import jp.gr.java_conf.uzresk.aws.ope.ebs.model.SnapshotIdRequest;
import jp.gr.java_conf.uzresk.aws.ope.ebs.model.VolumeIdRequest;

@RunWith(JUnit4.class)
public class EBSCopySnapshotTest {

	@Test
	public void copySnapshotFromSnapshotId() {

		SnapshotIdRequest snapshotIdRequest = new SnapshotIdRequest();
		snapshotIdRequest.setSourceSnapshotId("snap-xxxxxxxxxxxxxxxxxxx");
		snapshotIdRequest.setDestinationRegion("ap-northeast-1");
		snapshotIdRequest.setGenerationCount(2);

//		EBSCopySnapshot ebsCopySnapshot = new EBSCopySnapshot();
//		ebsCopySnapshot.copySnapshotFromSnapshotId(snapshotIdRequest, new TestContext());
	}

	@Test
	public void copySnapshotFromVolumeId() {

		VolumeIdRequest volumeIdRequest = new VolumeIdRequest();
		volumeIdRequest.setVolumeId("vol-xxxxxxxxxxxxxxxxx");
		volumeIdRequest.setDestinationRegion("ap-northeast-1");
		volumeIdRequest.setGenerationCount(2);

//		EBSCopySnapshot ebsCopySnapshot = new EBSCopySnapshot();
//		ebsCopySnapshot.copySnapshotFromVolumeId(volumeIdRequest, new TestContext());
	}
}
