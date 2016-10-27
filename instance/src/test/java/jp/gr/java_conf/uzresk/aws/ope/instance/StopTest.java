package jp.gr.java_conf.uzresk.aws.ope.instance;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import com.amazonaws.ClientConfiguration;

import jp.gr.java_conf.uzresk.aws.ope.instance.model.InstanceRequest;

@RunWith(JUnit4.class)
public class StopTest {

    @Test
    public void stop() {

        InstanceRequest instanceRequest = new InstanceRequest();
        instanceRequest.setInstanceId("i-xxxxxxxx");
        instanceRequest.setSqsEndpoint("https://sqs.ap-northeast-1.amazonaws.com");
        instanceRequest.setQueueName("StopInstanceQueue");

        ClientConfiguration cc = new ClientConfiguration();
        cc.setProxyHost("PROXY_HOST");
        cc.setProxyPort(8080);

        InstanceStopFunction stop = new InstanceStopFunction();
        stop.setClientConfiguration(cc);
//		stop.stop(instanceRequest, new TestContext());
    }

    @Test
    public void pollUntilStopped() {

        InstanceRequest instanceRequest = new InstanceRequest();
        instanceRequest.setInstanceId("i-xxxxxxxx");
        instanceRequest.setSqsEndpoint("https://sqs.ap-northeast-1.amazonaws.com");
        instanceRequest.setQueueName("StopInstanceQueue");
        instanceRequest.setInstanceStateCheckTimeoutSec(2);

        ClientConfiguration cc = new ClientConfiguration();
        cc.setProxyHost("PROXY_HOST");
        cc.setProxyPort(8080);

        InstanceStopFunction stop = new InstanceStopFunction();
        stop.setClientConfiguration(cc);
//		stop.checkStatusInstance(instanceRequest, new TestContext());
    }
    @Test
    public void createQueue() {

        InstanceRequest instanceRequest = new InstanceRequest();
        instanceRequest.setInstanceId("i-xxxxxxxx");

        ClientConfiguration cc = new ClientConfiguration();
        cc.setProxyHost("PROXY_HOST");
        cc.setProxyPort(8080);

        InstanceStopFunction stop = new InstanceStopFunction();
        stop.setClientConfiguration(cc);
        // stop.createQueue(instanceRequest, new TestContext());
    }

    @Test
    public void sns() {

        InstanceRequest instanceRequest = new InstanceRequest();
        instanceRequest.setInstanceId("i-xxxxxxxx");

        ClientConfiguration cc = new ClientConfiguration();
        cc.setProxyHost("PROXY_HOST");
        cc.setProxyPort(8080);

        InstanceStopFunction stop = new InstanceStopFunction();
        stop.setClientConfiguration(cc);
        // stop.snsOnSuccess(instanceRequest, "test message", "subject", new
        // TestContext());
    }

}