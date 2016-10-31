Run ShellScript, Powershell using AWS RunCommand
==================================================

Description
-----
On a regular basis by combining the Cloudwatch events can be used to execute commands on EC2


Usage
-----

To create a role that is set to LambdaFunction (RunScriptRole)

    {
        "Version": "2012-10-17",
        "Statement": [
            {
                "Effect": "Allow",
                "Action": "iam:PassRole",
                "Resource": "*"
            },
            {
                "Effect": "Allow",
                "Action": [
                    "ssm:DescribeAssociation",
                    "ssm:GetDocument",
                    "ssm:ListAssociations",
                    "ssm:UpdateAssociationStatus",
                    "ssm:UpdateInstanceInformation",
                    "ssm:SendCommand"
                ],
                "Resource": "*"
            },
            {
                "Effect": "Allow",
                "Action": [
                    "ec2messages:AcknowledgeMessage",
                    "ec2messages:DeleteMessage",
                    "ec2messages:FailMessage",
                    "ec2messages:GetEndpoint",
                    "ec2messages:GetMessages",
                    "ec2messages:SendReply"
                ],
                "Resource": "*"
            },
            {
                "Effect": "Allow",
                "Action": [
                    "cloudwatch:PutMetricData"
                ],
                "Resource": "*"
            },
            {
                "Effect": "Allow",
                "Action": [
                    "ec2:DescribeInstanceStatus"
                ],
                "Resource": "*"
            },
            {
                "Effect": "Allow",
                "Action": [
                    "ds:CreateComputer",
                    "ds:DescribeDirectories"
                ],
                "Resource": "*"
            },
            {
                "Effect": "Allow",
                "Action": [
                    "logs:CreateLogGroup",
                    "logs:CreateLogStream",
                    "logs:DescribeLogGroups",
                    "logs:DescribeLogStreams",
                    "logs:PutLogEvents"
                ],
                "Resource": "*"
            },
            {
                "Effect": "Allow",
                "Action": [
                    "s3:PutObject",
                    "s3:GetObject",
                    "s3:AbortMultipartUpload",
                    "s3:ListMultipartUploadParts",
                    "s3:ListBucketMultipartUploads"
                ],
                "Resource": "*"
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



(Optional)If you want to save the results of executing the command to S3Bucket

- create bucket in US Standard.

(Optional)If you want to SNS notifications

- Create SNS Topic.
- There is a need to create a role for Lambda to SNS notification receives the execution result of the SSM

  1.http://docs.aws.amazon.com/AWSEC2/latest/WindowsGuide/rc-sns-notifications.html#rc-iam-notifications
  2.add policy

    {
        "Version": "2012-10-17",
        "Statement": [
            {
                "Effect": "Allow",
                "Action": "iam:PassRole",
                "Resource": "*"
            }
        ]
    }

To create a LambdaFunction

- Name: RunScript
- Runtime: Java8
- Handler: jp.gr.java_conf.uzresk.aws.ope.runcommand.RunScriptFunction::execute
- Role: RunScriptRole
- Memory: 512
- Timeout: According to the number of target

Setting the Cloudwatch event

- EventSource: Schedule（Any Timing）
- Target：Lambda function -> RunScript
- Configure input: constant(JSON text)

  If you want to run the powershell

```
     {
         "documentName": "AWS-RunPowerShellScript",
         "instanceIds": ["i-xxxxxx"],
         "parameters":{"commands":["ipconfig"],"workingDirectory": ["C:\\"],"executionTimeout":["600"]},
         "outputS3BucketName":"bucketname",
         "outputS3BucketPrefix":"bucketnameprefix",
         "serviceRoleArn":"arn:aws:iam::xxxxxxxxxxxxx:role/RunCommandSNS",
         "notificationArn":"arn:aws:sns:ap-northeast-1:xxxxxxxxxxxx:xxxxxxxx",
         "notificationEvents":["Success"]
     }
```

  If you want to run the shell script

```
    {
    "documentName": "AWS-RunShellScript",
    "instanceIds": ["i-cc16f452"],
    "parameters":{"commands":["ifconfig"],"workingDirectory": [""],"executionTimeout":["600"]},
    "outputS3BucketName":"bucketname",
    "outputS3BucketPrefix":"bucketnameprefix",
    "serviceRoleArn":"arn:aws:iam::xxxxxxxxxxxxx:role/RunCommandSNS",
    "notificationArn":"arn:aws:sns:ap-northeast-1:xxxxxxxxxxxx:xxxxxxxx",
    "notificationEvents":["Success"]
    }
```



