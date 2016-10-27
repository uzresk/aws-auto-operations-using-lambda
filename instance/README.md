Start and stop of the instance
==================================================

Description
-----

Make the start and stop of the instance with the specified schedule in CloudWatchEvent.

When a parameter to you set the QueueName and Endpoint,
request of the start or stop of the instance is to create a message in and thrown successfully SQS.

You can be sure that the messages that have been made to SQS
regularly instance by polling has been successfully start or stop.

Usage
-----

To create a role that is set to LambdaFunction (LambdaInstanceRole)

    {
        "Version": "2012-10-17",
        "Statement": [
            {
                "Effect": "Allow",
                "Action": [
                    "logs:CreateLogGroup",
                    "logs:CreateLogStream",
                    "logs:PutLogEvents"
                ],
                "Resource": [
                    "arn:aws:logs:*:*:*"
                ]
            },
            {
                "Effect": "Allow",
                "Action": [
                    "ec2:DescribeInstances",
                    "ec2:StartInstances",
                    "ec2:StopInstances"
                ],
                "Resource": [
                    "*"
                ]
            },
            {
                "Effect": "Allow",
                "Action": [
                    "sqs:CreateQueue",
                    "sqs:SendMessage",
                    "sqs:ReceiveMessage",
                    "sqs:DeleteMessage"
                ],
                "Resource": [
                    "*"
                ]
            }
        ]
    }

InstanceStart/InstanceStop
-----

To create a LambdaFunction

- Name: InstanceStartRequest
- Runtime: Java8
- Handler: jp.gr.java_conf.uzresk.aws.ope.instance.InstanceStartFunction::request or jp.gr.java_conf.uzresk.aws.ope.instance.InstanceStopFunction::request
- Role: InstanceStartFunctionRole or InstanceStopFunctionRole
- Memory: 512
- Timeout: According to the number of target

Setting the Cloudwatch event

- EventSource: Schedule（Any Timing）
- Target：Lambda function -> InstanceStartRequest or InstanceStopRequest
- Configure input: constant(JSON text)

- InstanceStart

    {
      "instanceId": "i-xxxxxxxx",
      "queueName": "InstanceStartQueue",
      "sqsEndpoint": "https://sqs.ap-northeast-1.amazonaws.com",
      "instanceStateCheckTimeoutSec": 300
    }

- InstanceStop

    {
      "instanceId": "i-xxxxxxxx",
      "queueName": "InstanceStopQueue",
      "sqsEndpoint": "https://sqs.ap-northeast-1.amazonaws.com",
      "instanceStateCheckTimeoutSec":300
    }

If you want to use the queue, queueName, please set the sqsEndpoint.
If the Queue does not exist, create automatically a queue to the region specified in the sqsEndpoint.


instanceStateCheckTimeoutSec after the start or stop request is thrown,
the instance is to specify a grace period of until the running or stopped.
If more than this to determine as an error.

Start and Stop of Multiple Instances
-----

To create a LambdaFunction

- Name: InstanceStartRequests
- Runtime: Java8
- Handler: jp.gr.java_conf.uzresk.aws.ope.instance.InstanceStartFunction::requests or jp.gr.java_conf.uzresk.aws.ope.instance.InstanceStopFunction::requests
- Role: InstanceStartFunctionRole or InstanceStopFunctionRole
- Memory: 512
- Timeout: According to the number of target

Setting the Cloudwatch event

- EventSource: Schedule（Any Timing）
- Target：Lambda function -> InstanceStartRequest or InstanceStopRequest
- Configure input: constant(JSON text)

- InstanceStart

    {
      "instanceRequests": [
        {
          "instanceId": "i-xxxxxxxx",
          "queueName": "InstanceStartQueue",
          "sqsEndpoint": "https://sqs.ap-northeast-1.amazonaws.com",
          "instanceStateCheckTimeoutSec": 300
        },
        {
          "instanceId": "i-yyyyyyyy",
          "queueName": "InstanceStartQueue",
          "sqsEndpoint": "https://sqs.ap-northeast-1.amazonaws.com",
          "instanceStateCheckTimeoutSec": 300
        }
      ]
    }

- InstanceStop

    {
      "instanceRequests": [
        {
          "instanceId": "i-xxxxxxxx",
          "queueName": "InstanceStopQueue",
          "sqsEndpoint": "https://sqs.ap-northeast-1.amazonaws.com",
          "instanceStateCheckTimeoutSec": 300
        },
        {
          "instanceId": "i-yyyyyyyy",
          "queueName": "InstanceStopQueue",
          "sqsEndpoint": "https://sqs.ap-northeast-1.amazonaws.com",
          "instanceStateCheckTimeoutSec": 300
        }
      ]
    }

CheckInstanceStateRunning
-----

To create a LambdaFunction

- Name: InstanceCheckStateRunning
- Runtime: Java8
- Handler: jp.gr.java_conf.uzresk.aws.ope.instance.InstanceStartFunction::checkInstanceState
- Role: InstanceStartFunctionRole
- Memory: 512
- Timeout: According to the number of target

Setting the Cloudwatch event

- EventSource: Schedule 1-minute intervals
- Target：Lambda function -> InstanceCheckStateRunning
- Configure input: constant(JSON text)

    {
      "queueName": "InstanceStartQueue",
      "sqsEndpoint": "https://sqs.ap-northeast-1.amazonaws.com",
      "maxNumberOfMessages":10
    }

CheckInstanceStateStopped
-----

To create a LambdaFunction

- Name: InstanceCheckStateStopped
- Runtime: Java8
- Handler: jp.gr.java_conf.uzresk.aws.ope.instance.InstanceStopFunction::checkInstanceState
- Role: InstanceStopFunctionRole
- Memory: 512
- Timeout: According to the number of target

Setting the Cloudwatch event

- EventSource: Schedule 1-minute intervals
- Target：Lambda function -> InstanceCheckStateStopped
- Configure input: constant(JSON text)

    {
      "queueName": "InstanceStopQueue",
      "sqsEndpoint": "https://sqs.ap-northeast-1.amazonaws.com",
      "maxNumberOfMessages":10
    }


maxNumberOfMessages specifies the number of messages to be retrieved from the queue.
If you make of ten instance start or stop at the same time will make the confirmation of the status at the same time if you specify 10.

Monitoring
----

Run the results are output to CloudWatchLogs. You'll be able to monitor if you define a filter and Alerm to CloudWatchLogs.

ex)

    [SUCCESS][i-cc16f452][InstanceStopRequest]Stop request of the instance has completed successfully.

    [ERROR][checkInstanceStatus][running]message[Status check of the instance has timed out.InstanceRequest [instanceId=i-cc16f452,

