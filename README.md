# AWS Config Rules 

Being part of AWS Security and Infrastructure team, we were looking out for some solution which solves below issues.

1. Security Policies we defined are implemented or not.
2. Identify resources which have low utilization and shut them down to save costs.
3. If our applications and AWS resources are fault tolerant.
4. Analyzing performance of resources and applications.

Fault tolerance by default identifies AWS resources which are not following above standards. But we want to extend this functionality further, so we had chosen AWS Config service as solution to solve above issues.

## Following rules have been implemented.

1. EBSVolumesAvailable :- This AWS Config rule lists all EBS Volumes which are available(Not attached to any resource). We can save cost by creating S3 Snapshot of these volumes and deleting these volumes. When we need the volume, we can just recreate the volume from S3 snapshot.
2. EIPsUnused :- This AWS Config rule lists all unused EIPs. Amazon charges for unused EIPs. We can save little amount of costs, by releasing back unused EIPs.
3. MFANotEnabledUsers :- This AWS Config rule lists all users who have not enabled MFA on their AWS accounts.
4. NonCompliantSecurityGroups :- This AWS Config rule list all security groups which allows traffic from 0.0.0.0/0 from ports other than 80,443.
5. NonEncryptedEBSVolumes :- This AWS Config rule list all EBS volumes which are not encrypted.

## Steps to create AWS Lambda and AWS Config rule.

