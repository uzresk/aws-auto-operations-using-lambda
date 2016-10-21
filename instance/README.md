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

To create a role that is set to LambdaFunction (StartInstanceFunctionRole)

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

StartInstance/StopInstance
-----

To create a LambdaFunction

- Name: StartInstanceRequest
- Runtime: Java8
- Handler: jp.gr.java_conf.uzresk.aws.ope.instance.Start::request or jp.gr.java_conf.uzresk.aws.ope.instance.Start::request
- Role: StartInstanceFunctionRole or StopInstanceFunctionRole
- Memory: 512
- Timeout: According to the number of target

Setting the Cloudwatch event

- EventSource: Schedule（Any Timing）
- Target：Lambda function -> StartInstanceRequest or StopInstanceRequest
- Configure input: constant(JSON text)

- StartInstance

    {
      "instanceId": "i-xxxxxxxx",
      "queueName": "StartInstanceQueue",
      "sqsEndpoint": "https://sqs.ap-northeast-1.amazonaws.com",
      "instanceStateCheckTimeoutSec": 300
    }

- StopInstance

    {
      "instanceId": "i-xxxxxxxx",
      "queueName": "StopInstanceQueue",
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

- Name: StartInstanceRequests
- Runtime: Java8
- Handler: jp.gr.java_conf.uzresk.aws.ope.instance.Start::requests or jp.gr.java_conf.uzresk.aws.ope.instance.Start::requests
- Role: StartInstanceFunctionRole or StopInstanceFunctionRole
- Memory: 512
- Timeout: According to the number of target

Setting the Cloudwatch event

- EventSource: Schedule（Any Timing）
- Target：Lambda function -> StartInstanceRequest or StopInstanceRequest
- Configure input: constant(JSON text)

- StartInstance

    {
      "instanceRequests": [
        {
          "instanceId": "i-xxxxxxxx",
          "queueName": "StartInstanceQueue",
          "sqsEndpoint": "https://sqs.ap-northeast-1.amazonaws.com",
          "instanceStateCheckTimeoutSec": 300
        },
        {
          "instanceId": "i-yyyyyyyy",
          "queueName": "StartInstanceQueue",
          "sqsEndpoint": "https://sqs.ap-northeast-1.amazonaws.com",
          "instanceStateCheckTimeoutSec": 300
        }
      ]
    }
    
- StopInstance

    {
      "instanceRequests": [
        {
          "instanceId": "i-xxxxxxxx",
          "queueName": "StopInstanceQueue",
          "sqsEndpoint": "https://sqs.ap-northeast-1.amazonaws.com",
          "instanceStateCheckTimeoutSec": 300
        },
        {
          "instanceId": "i-yyyyyyyy",
          "queueName": "StopInstanceQueue",
          "sqsEndpoint": "https://sqs.ap-northeast-1.amazonaws.com",
          "instanceStateCheckTimeoutSec": 300
        }
      ]
    }

CheckInstanceStateRunning
-----

To create a LambdaFunction

- Name: CheckInstanceStateRunning
- Runtime: Java8
- Handler: jp.gr.java_conf.uzresk.aws.ope.instance.Start::checkInstanceState
- Role: StartInstanceFunctionRole
- Memory: 512
- Timeout: According to the number of target

Setting the Cloudwatch event

- EventSource: Schedule 1-minute intervals
- Target：Lambda function -> CheckInstanceStateRunning
- Configure input: constant(JSON text)

    {
      "queueName": "StartInstanceQueue",
      "sqsEndpoint": "https://sqs.ap-northeast-1.amazonaws.com",
      "maxNumberOfMessages":10
    }

CheckInstanceStateStopped
-----

To create a LambdaFunction

- Name: CheckInstanceStateStopped
- Runtime: Java8
- Handler: jp.gr.java_conf.uzresk.aws.ope.instance.Stop::checkInstanceState
- Role: StopInstanceFunctionRole
- Memory: 512
- Timeout: According to the number of target

Setting the Cloudwatch event

- EventSource: Schedule 1-minute intervals
- Target：Lambda function -> CheckInstanceStateStopped
- Configure input: constant(JSON text)

    {
      "queueName": "StopInstanceQueue",
      "sqsEndpoint": "https://sqs.ap-northeast-1.amazonaws.com",
      "maxNumberOfMessages":10
    }


maxNumberOfMessages specifies the number of messages to be retrieved from the queue.
If you make of ten instance start or stop at the same time will make the confirmation of the status at the same time if you specify 10.

Monitoring
----

Run the results are output to CloudWatchLogs. You'll be able to monitor if you define a filter and Alerm to CloudWatchLogs.

ex)

[SUCCESS][i-cc16f452][StopInstanceRequest]Stop request of the instance has completed successfully.

[ERROR][checkInstanceStatus][running]message[Status check of the instance has timed out.InstanceRequest [instanceId=i-cc16f452,

