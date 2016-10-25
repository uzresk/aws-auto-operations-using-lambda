package jp.gr.java_conf.uzresk.aws.ope.ebs;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import jp.gr.java_conf.uzresk.aws.ope.TestContext;

@RunWith(JUnit4.class)
public class EBSSnapshotTest {

	@Test
	public void copyVolumeIdFromVolumeId() {

		VolumeIdRequest volumeIdRequest = new VolumeIdRequest();
		volumeIdRequest.setVolumeId("vol-xxxxxxxxxxxxxxxxxxxxxxx");
		volumeIdRequest.setGenerationCount(3);

//		EBSSnapshot ebsSnapshot = new EBSSnapshot();
//		ebsSnapshot.createSnapshotFromVolumeId(volumeIdRequest, new TestContext());
	}

}
