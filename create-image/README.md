Perform image(AMI) generation and generation management of
==================================================

Description
-----

Do AMI creation and generation management along the set schedule in CloudWatchEvents.
In Two Step
- 1. Sending a message to the AMI creation and SQS
- 2. Received the message of the SQS, perform the generation management to check the status of the AMI

Usage
-----

To create a role that is set to LambdaFunction (LambdaImageCreatePargeRole)

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
                    "ec2:DescribeSnapshots",
                    "ec2:CreateSnapshot",
                    "ec2:DeleteSnapshot",
                    "ec2:CreateTags",
                    "ec2:DescribeTags",
                    "ec2:DescribeImages",
                    "ec2:CreateImage",
                    "ec2:DeregisterImage"
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
            },
            {
                "Effect": "Allow",
                "Action": [
                    "dynamodb:PutItem",
                    "dynamodb:UpdateItem"
                ],
                "Resource": [
                    "arn:aws:dynamodb:ap-northeast-1:xxxxxxxxxxxx:table/lambda_locks"
                ]
            }
        ]
    }

Create Image from instanceId
-----

To create a LambdaFunction

- Name: ImageCreate
- Runtime: Java8
- Handler: jp.gr.java_conf.uzresk.aws.ope.image.ImageCreateFunction::request
- Role: LambdaImageCreatePargeRole
- Memory: 512
- Timeout: According to the number of target

Setting the Cloudwatch event

- EventSource: Schedule（Any Timing）
- Target：Lambda function -> ImageCreate
- Configure input: constant(JSON text)

    {
      "instanceId": "i-xxxxxxxxxxxxxxxxxx",
      "amiName": "lambda",
      "noReboot": true,
      "generationCount": 2,
      "sqsEndpoint": "https://sqs.ap-northeast-1.amazonaws.com",
      "queueName": "CreateAMIQueue",
      "imageCreatedTimeoutSec": 6000
    }


If you want to use the queue, queueName, please set the sqsEndpoint.
If the Queue does not exist, create automatically a queue to the region specified in the sqsEndpoint.


imageCreatedTimeoutSec after the create image request is thrown,
the image is to specify a grace period of until the available
If more than this to determine as an error.

Create Images from multiple instanceIds
-----

- Name: ImageCreateFromInstanceIds
- Runtime: Java8
- Handler: jp.gr.java_conf.uzresk.aws.ope.image.ImageCreateFunction::requests
- Role: LambdaImageCreatePargeRole
- Memory: 512
- Timeout: According to the number of target

Setting the Cloudwatch event

- EventSource: Schedule（Any Timing）
- Target：Lambda function -> ImageCreate
- Configure input: constant(JSON text)

    {
      "imageCreateRequests": [
        {
          "instanceId": "i-xxxxxxxxxxxxxxxxxx",
          "amiName": "lambda",
          "noReboot": true,
          "generationCount": 2,
          "sqsEndpoint": "https://sqs.ap-northeast-1.amazonaws.com",
          "queueName": "CreateAMIQueue",
          "imageCreatedTimeoutSec": 6000
        },
        {
          "instanceId": "i-yyyyyyyyyyyyyyyyyy",
          "amiName": "lambda",
          "noReboot": true,
          "generationCount": 2,
          "sqsEndpoint": "https://sqs.ap-northeast-1.amazonaws.com",
          "queueName": "CreateAMIQueue",
          "imageCreatedTimeoutSec": 6000
        }
      ]
    }

ImageStateCheckAndParge
-----

To create a LambdaFunction

- Name: ImageStateCheckAndParge
- Runtime: Java8
- Handler: jp.gr.java_conf.uzresk.aws.ope.image.ImageStateCheckAndPargeFunction::request
- Role: LambdaImageCreatePargeRole
- Memory: 512
- Timeout: According to the number of target

Setting the Cloudwatch event

- EventSource: Schedule 1-minute intervals
- Target：Lambda function -> ImageStateCheckAndParge
- Configure input: constant(JSON text)

    {
      "sqsEndpoint": "https://sqs.ap-northeast-1.amazonaws.com",
      "queueName": "CreateImageQueue",
      "numberOfMessage": 10
    }

maxNumberOfMessages specifies the number of messages to be retrieved from the queue.
If you make of ten instance start or stop at the same time will make the confirmation of the status at the same time if you specify 10.

Monitoring
----

Run the results are output to CloudWatchLogs. You'll be able to monitor if you define a filter and Alerm to CloudWatchLogs.

ex)

    [SUCCESS][i-01abd4db05e166adb] Creation of AMI, additional tags, generation management has completed successfully.

    [ERROR][checkStateAndPargeImage] message[You are not authorized to perform this operation.
