EBS Copy Snapshot acquisition and generation management
==================================================

Get the EBS Copy Snapshot, perform the generation management.
To identify the target of volume to get a snapshot There are three ways.
- 1. To specify the volume ID in json
- 2. To specify multiple volume ID in json
- 3. To specify the snapshot ID in json

Usage
-----

To create a role that is set to LambdaFunction (LambdaEBSCopySnapshotRole)

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
                    "ec2:CopySnapshot",
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


create EBS CopySnapshot from volume-id
---

To create a LambdaFunction

- Name: EBSCopySnapshotFromVolumeId
- Runtime: Java8
- Handler: jp.gr.java_conf.uzresk.aws.ope.ebs.EBSCopySnapshot::copySnapshotFromVolumeId
- Role: LambdaEBSCopySnapshotRole
- Memory: 512
- Timeout: According to the number of target

Setting the Cloudwatch event

- EventSource: Schedule（Any Timing）
- Target：Lambda function -> EBSCopySnapshotFromVolumeId
- Configure input: constant(JSON text)

    {
      "volumeId": "vol-xxxxxxxxxxxxxxxxx",
      "destinationRegion": "ap-northeast-1",
      "generationCount": "2"
    }

create EBS CopySnapshot from multiple volume-id
---

To create a LambdaFunction

- Name: EBSCopySnapshotFromVolumeIds
- Runtime: Java8
- Handler: jp.gr.java_conf.uzresk.aws.ope.ebs.EBSCopySnapshot::copySnapshotFromVolumeIds
- Role: LambdaEBSCopySnapshotRole
- Memory: 512
- Timeout: According to the number of target

Setting the Cloudwatch event

- EventSource: Schedule（Any Timing）
- Target：Lambda function -> EBSCopySnapshotFromVolumeIds
- Configure input: constant(JSON text)

    {
      "volumeIdRequests": [
        {
          "volumeId": "vol-xxxxxxxxxxxxxxxxx",
          "destinationRegion" : "ap-northeast-1",
          "generationCount": "2"
        },
        {
          "volumeId": "vol-yyyyyyyyyyyyyyyyy",
          "destinationRegion" : "ap-northeast-1",
          "generationCount": "3"
        }
      ]
    }

create EBS Snapshot from tag name
---

To create a LambdaFunction

- Name: EBSCopySnapshotFromSnapshotId
- Runtime: Java8
- Handler: jp.gr.java_conf.uzresk.aws.ope.ebs.EBSCopySnapshot::copySnapshotFromSnapshotId
- Role: LambdaEBSCopySnapshotRole
- Memory: 512
- Timeout: According to the number of target

Setting the Cloudwatch event

- EventSource: Schedule（Any Timing）
- Target：Lambda function -> EBSCopySnapshotFromSnapshotId
- Configure input: constant(JSON text)

    {
      "sourceSnapshotId": "snap-xxxxxxxxxxxxxxx",
      "destinationRegion": "ap-northeast-1",
      "generationCount": "3"
    }


