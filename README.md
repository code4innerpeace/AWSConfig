# AWS Config Rules

## History :- Why this project was implemented.

I am fascinated and passionate about cloud services and technology in general. I wanted to improve my minimal programming language skills by solving real world problems. This is
one the project evolved from the simple question I had "How to make our environment more secure and optimize our AWS expenditure."

Being part of AWS Security and Infrastructure team, I was looking out for some solution which solves below issues.

1. Security Policies we defined are implemented or not.
2. Identify resources which have low utilization and shut them down to save costs.
3. If our applications and AWS resources are fault tolerant.
4. Analyzing performance of resources and applications.

Fault tolerance by default identifies AWS resources which are not following above standards but it's limited to specific set of rules defined by AWS. I wanted to extend this functionality further, so I had chosen AWS Config service as solution to solve above issues.

Config rule can be triggered in two ways.

    1. When configuration changes.
    2. Or periodically(1hr, 3h, 6hr, 12hrs, 24hrs)

I had chosen second method "Period" as solution for our issue. I wanted to make sure our security policies defined are validated each hour.

Also generally a config rule checks if each resource is "COMPLIANT" and "NONCOMPLIANT", but in this project I had filtered out all "COMPLIANT" resources in my utility classes.
I was more interested in "NONCOMPLIANT" resources.

## Following rules have been implemented as of today. I will be extending rules as and I get time. Please email me if anyone wants any specific rule to be implemented.

1. EBSVolumesAvailable :- This AWS Config rule lists all EBS Volumes which are available(Not attached to any resource). We can save cost by creating S3 Snapshot of these volumes
 and deleting these volumes. When we need the volume, we can just recreate the volume from S3 snapshot.
2. EIPsUnused :- This AWS Config rule lists all unused EIPs. Amazon charges for unused EIPs. We can save little amount of costs, by releasing back unused EIPs.
3. MFANotEnabledUsers :- This AWS Config rule lists all users who have not enabled MFA on their AWS accounts. MFA improves security of AWS accounts.
4. NonCompliantSecurityGroups :- This AWS Config rule list all security groups which allows traffic from 0.0.0.0/0 from ports other than 80,443. Improves the security of AWS resources.
5. NonEncryptedEBSVolumes :- This AWS Config rule list all EBS volumes which are not encrypted. Improves security of data at rest.

## Steps to create AWS Lambda and AWS Config rule.

1. Create S3 bucket and Upload the code.
2. Create a new Lambda Role. Make sure you attach below policies to the role.

Note :- Attached policies are generous and just listed for initial setup. Once you make sure Lambda is working as expected, fine tune the rules so that the role has access to only required resources. Follow Amazon Best Practices, only give access to resources which are absolutely required.

Managed Policies: 

AmazonEC2FullAccess
IAMFullAccess
AmazonAPIGatewayInvokeFullAccess
AWSConfigRole
AmazonAPIGatewayAdministrator
AmazonCognitoPowerUser
AWSSupportAccess
CloudWatchLogsFullAccess
AmazonS3FullAccess

Trusted Entities:

The identity provider(s) lambda.amazonaws.com

3. Create Lambda Function.

Lambda Function --> Blank Function --> Next

Name : LambdaFunctionName
Runtime : Java 8
Code Entry Type : Upload a file from Amazon S3
S3 link URL : Jar file url which had been uploaded to S3 bucket.
Handler :- For example :- com.vb.aws.services.mt.config.NonEncryptedEBSVolumes::handle
Role :- Choose an existing role.
Existing Role :- Select the role created in Step 2.

4) Create AWS Config Rule.

1) If creating Config rule for first time, it will start wizard. Please access default values.
2) Create new Config Rule.

Rules --> Add Rule --> Custom Rule -->

Name: <ConfigRuleName>
AWS Lambda function ARN: Lambda Function ARN created earlier. For example arn:aws:lambda:us-east-1:<ACCOUNT-NUMBER>:function:NonEncryptedEBSVolumes
Trigger: Periodic(Select 1,3,6,12,24 hrs)

## Debugging ##
1. As soon as you create rule or click on "Re-evaluate" button, you should increase in AWS Lambda Invocation count by 1 on AWS Lambda Monitoring console.
2. Next check AWS Lambda CloudWatch logs.
3. If there are no exceptions in Cloudwatch logs, you should see Config Rule updated with resources which are non-compliant.


## Acknowledgements ##

I would like to thank below for providing valuable support for turning this idea into real world project.

Pearson Management Team :- Ian, Ryan, Craig.
Jeff :- Thanks for giving suggestions on test cases.
Anshul :- Thanks for listening to weird ideas and helping me to turn them into real world projects.
Ilya :- Thanks for supporting me from Amazon side and forwarding my errors or weird ideas to product team.
Ravi Ravva :- Thanks for creating initial AWS Config rules. I had got basic idea of how to write Config rules based on your code.


## References ##

http://docs.aws.amazon.com/config/latest/developerguide/WhatIsConfig.html
http://docs.aws.amazon.com/lambda/latest/dg/welcome.html
https://github.com/awslabs/aws-config-rules

