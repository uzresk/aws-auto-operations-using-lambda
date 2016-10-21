EBS Snapshot acquisition and generation management
==================================================

Usage
-----

To create a role that is set to LambdaFunction (EBSSnapshotExecLambdaRole)

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
                    "ec2:DescribeVolumes",
                    "ec2:DescribeSnapshots",
                    "ec2:CreateSnapshot",
                    "ec2:CreateTags",
                    "ec2:DescribeTags",
                    "ec2:DeleteSnapshot"
                ],
                "Resource": [
                    "*"
                ]
            }
        ]
    }

To create a LambdaFunction

- Name: EBSSnapshot
- Runtime: Java8
- Handler: jp.gr.java_conf.uzresk.aws.ope.ebs.EBSSnapshot::create
- Role: EBSSnapshotExecLambdaRole
- Memory: 512
- Timeout: According to the number of target

Setting the Cloudwatch event

- EventSource: Schedule（Any Timing）
- Target：Lambda function -> EBSSnapshot
- Configure input: constant(JSON text)

    {"target":"DB"}

Setting the Backup tag and GenerationCount tag in Volume to be EBSSnapshot target.

- The GenerationCount to set the number of generation management. If you omit the GenerationCount, generation management number is 10 by default
- VolumeTags examples

    "Backup":"DB", "GenerationCount":"2"



