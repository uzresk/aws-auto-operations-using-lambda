DeregisterAMI: At the timing of the AMI is deregistration to remove the EBS snapshot
====================================================================================

Usage
-----

To create a role that is set to LambdaFunction (DeregisterAMIExecLambdaRole)

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
                    "ec2:DeleteSnapshot"
                ],
                "Resource": [
                    "*"
                ]
            }
        ]
    }

To create a LambdaFunction

- Name: DeleteSnapshotWhenDeregisterAMI
- Runtime: Java8
- Handler: jp.gr.java_conf.uzresk.aws.ope.ami.DeregisterAMI::handleRequest
- Role: DeregisterAMIExecRole
- Memory: 512
- Timeout: According to the number of target

Setting the Cloudwatch event

- EventSource: AWS API call
- Service name: EC2
- Specific operations: DeregisterImage
- Target：Lambda function -> DeregisterAMI
- Configure input: Matched event
