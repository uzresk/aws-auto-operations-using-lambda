AWS operation using the Lambda
==============================

Tool to automate the AWS of operation using the Lambda.


Provides
-----------------------------
- [Instance Start/Stop/CheckState](./instance/README.md)
    - Make the start and stop of the instance(multiple instances) with the specified schedule in CloudWatchEvent.
    - When a parameter to you set the QueueName and Endpoint, request of the start or stop of the instance is to create a message in and thrown successfully SQS.
    - You can be sure that the messages that have been made to SQS regularly instance by polling has been successfully start or stop.
- [EBSSnapshot](./ebs-snapshot/README.md)
    - Do the acquisition and generation management of ebssnapshot at the timing set by the CloudWatchEvent.
    - To identify the target of volume to get a snapshot There are three ways.
        - 1. To specify the volume ID in json
        - 2. To specify multiple volume ID in json
        - 3. To specify the tag that has been granted to the EBS in json
- [Create Image(AMI)](./create-image/README.md)
    - Do AMI creation and generation management along the set schedule in CloudWatchEvents. 
        - 1. To specify the instance ID in json
        - 2. To specify multiple instance ID in json
- [EBSCopySnapshot](./ebs-copy-snapshot/README.md)
    - Get the EBS Copy Snapshot, perform the generation management.
    - To identify the target of volume to get a snapshot There are three ways.
        - 1. To specify the volume ID in json
        - 2. To specify multiple volume ID in json
        - 3. To specify the snapshot ID in json
- [DeregisterImage](./deregister-image/README.md)
     - At the timing of the AMI is deregistration to remove the EBS snapshot
- [RunCommand](./run-command/README.md)
     - Run ShellScript, Powershell using AWS RunCommand
     - On a regular basis by combining the Cloudwatch events can be used to execute commands on EC2

Setting of common
-----------------------------
It may be called multiple times and call the Lambda from CloudwatchEvent.
In order to prevent the creation or deletion of unexpected resources, it has secured the idempotence using DynamoDB.

Please be sure that you create the following table

- table_name: lambda_locks
- primary_key: function_name(String)
- partition_key: key(String)

VolumeID is stored if EBSSnapshot the key. If the creation of Image instanceID is stored


Author
----------------------------
[uzresk](https://twitter.com/uzresk)

License
----------------------------
MIT
