EBS Snapshot acquisition and generation management
==================================================

Get the EBSSnapshot, perform the generation management.
To identify the target of volume to get a snapshot There are three ways.
1. To specify the volume ID in json
2. To specify multiple volume ID in json
3. To specify the tag that has been granted to the EBS in json


Usage
-----

To create a role that is set to LambdaFunction (LambdaEBSSnapshotRole)

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
- Role: LambdaEBSSnapshotRole
- Memory: 512
- Timeout: According to the number of target

create EBS Snapshot from volume-id
---

Setting the Cloudwatch event

- EventSource: Schedule（Any Timing）
- Target：Lambda function -> EBSSnapshotFromVolumeId
- Configure input: constant(JSON text)

    {
      "volumeId": "vol-xxxxxxxxxxxxxxxxx",
      "generationCount": "2"
    }

create EBS Snapshot from multiple volume-id
---

Setting the Cloudwatch event

- EventSource: Schedule（Any Timing）
- Target：Lambda function -> EBSSnapshotFromVolumeId
- Configure input: constant(JSON text)

    {
      "volumeIdRequests": [
        {
          "volumeId": "vol-xxxxxxxxxxxxxxxxx",
          "generationCount": "2"
        },
        {
          "volumeId": "vol-yyyyyyyyyyyyyyyyy",
          "generationCount": "3"
        }
      ]
    }

create EBS Snapshot from tag name
---

Setting the Cloudwatch event

- EventSource: Schedule（Any Timing）
- Target：Lambda function -> EBSSnapshotFromVolumeId
- Configure input: constant(JSON text)

    {
      "tagName": "test",
      "generationCount": 2
    }

Setting the Backup tag and GenerationCount tag in Volume to be EBSSnapshot target.

- The GenerationCount to set the number of generation management. If you omit the GenerationCount, generation management number is 10 by default
- VolumeTags examples

    "Backup":"test",



