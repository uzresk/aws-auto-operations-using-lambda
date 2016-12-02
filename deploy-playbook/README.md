Deploy using ansible playbook
==============================

Automatically set IAM role/policy, dynamodb table, lambda function, cloudwatchevents using ansible.

Required
---

ansible >= 2.2.0.0

Setup
---

install ansible 2.2,aws cli, boto, boto3. If it is troublesome, please use the Docker container containing ansible 2.2, aws cli, boto, boto 3

```
docker run -v /vagrant:/vagrant -it --name tmp uzresk/centos7-ansible-serverspec:ansible-v2.2.0.0-1 /bin/bash --login
```

set up aws credentials(~/.aws/credentials), 

```~/.aws/credentials
[default]
aws_access_key_id=
aws_secret_access_key=
```

set up default region(~/.aws/config)

```~/.aws/config
[default]
region=ap-northeast-1
```

for proxy user
- set up boto proxy(~/.boto)

```
[Boto]
debug=1
num_retries=1

proxy = PROXY_HOST_NAME
proxy_port = PROXY_PORT
```

-  set up env HTTP_PROXY/HTTPS_PROXY

getting deploy scripts

```
https://github.com/uzresk/aws-auto-operations-using-lambda/releases/download/0.0.1/deploy-playbook.zip
```

For example
---

deregister image

- set up deregister-image/vars.yml
    - change BUCKET_NAME

- deploy using playbook

```
ansible-playbook deregister-image/main.yml
```

---

ebs-snapshot

- set up ebs-snapshot/vars.yml (BUCKET_NAME, cloudwatch-events cron expression/description)

```
release_jar:
  name: ebs-snapshot.jar
  url: https://github.com/uzresk/aws-auto-operations-using-lambda/releases/download/0.0.1/ebs-snapshot-0.0.1.jar
s3:
  bucket_name: BUCKET_NAME
lambda_functions:
  - name: createEBSSnapshotFromVolumeId
    description: create snapshot and generation management
    handler: jp.gr.java_conf.uzresk.aws.ope.ebs.EBSSnapshotFunction::createSnapshotFromVolumeId
    timeout: 60
    memory_size: 512
iam:
  role_name: LambdaEBSSnapshotCreateRole
  policy_name: LambdaEBSSnapshotCreatePolicy
cloudwatch_events:
  - rule_name: CreateEBSSnapshotFromVolumeIdRule
    description: vol-xxxxxxxxxxxxxxx Every Day am 1:00(JST)
    schedule_expression: cron(0 16 * * ? *)
    input: input_EBSSnapshot.json
dynamodb:
  table_name: lambda_locks
  read_capacity: 1
  write_capacity: 1
```

- set up cloudwatch events arguments file(json)

```
{"volumeId":"vol-xxxxxxxxxxxxxx","generationCount":"2"}
```

- deploy using playbook

```
ansible-playbook ebs-snapshot/main.yml
```
