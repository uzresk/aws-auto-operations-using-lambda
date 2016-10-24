package jp.gr.java_conf.uzresk.aws.ope.ebs;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import jp.gr.java_conf.uzresk.aws.ope.TestContext;

@RunWith(JUnit4.class)
public class EBSCopySnapshotTest {

	@Test
	public void copySnapshotFromSnapshotId() {

		SnapshotIdRequest snapshotIdRequest = new SnapshotIdRequest();
		snapshotIdRequest.setSourceSnapshotId("snap-043983f6eeaaf4583");
		snapshotIdRequest.setDestinationRegion("ap-northeast-1");
		snapshotIdRequest.setGenerationCount(2);

		EBSCopySnapshot ebsCopySnapshot = new EBSCopySnapshot();
		ebsCopySnapshot.copySnapshotFromSnapshotId(snapshotIdRequest, new TestContext());
	}

}
