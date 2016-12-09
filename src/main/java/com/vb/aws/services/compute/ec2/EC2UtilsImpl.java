/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.vb.aws.services.compute.ec2;

import com.amazonaws.AmazonClientException;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.ec2.AmazonEC2;
import com.amazonaws.services.ec2.AmazonEC2Client;
import com.amazonaws.services.ec2.model.DescribeInstancesRequest;
import com.amazonaws.services.ec2.model.DescribeInstancesResult;
import com.amazonaws.services.ec2.model.DescribeSecurityGroupsResult;
import com.amazonaws.services.ec2.model.DescribeVolumesResult;
import com.amazonaws.services.ec2.model.DeviceType;
import com.amazonaws.services.ec2.model.Instance;
import com.amazonaws.services.ec2.model.InstanceBlockDeviceMapping;
import com.amazonaws.services.ec2.model.IpPermission;
import com.amazonaws.services.ec2.model.Reservation;
import com.amazonaws.services.ec2.model.SecurityGroup;
import com.amazonaws.services.ec2.model.Volume;
import com.amazonaws.services.elasticloadbalancing.AmazonElasticLoadBalancing;
import com.amazonaws.services.elasticloadbalancing.AmazonElasticLoadBalancingClient;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 *
 * @author Vijay Bheemineni
 */
public class EC2UtilsImpl implements EC2Utils {
    
    private AmazonEC2 amazonEc2;
    private AmazonElasticLoadBalancing amazonElasticLoadBalancing;

    // Variables for Non Compliant Security Groups.(All Security Groups allowing traffic other than
    // 80, 443 from 0.0.0.0 are non compliant.
    private final String SOURCE_IP_ADDRESS = "0.0.0.0/0";
    private final Integer HTTP_PORT = 80;
    private final Integer HTTPS_PORT = 443;
    
    /**
     * Default constructor.
     */
    public EC2UtilsImpl() {
        this.amazonEc2 = new AmazonEC2Client();
        this.amazonElasticLoadBalancing = new AmazonElasticLoadBalancingClient();
    }
    
    /**
     * This method fetches all security groups in an aws account.
     * @return List<SecurityGroup> returns all security groups.
     * @throws AmazonClientException 
     */
    public List<SecurityGroup> getAllSecurityGroups() throws AmazonClientException {
        
        List<SecurityGroup> allSecurityGroups;
        
        try {
            
           DescribeSecurityGroupsResult describeSecurityGroupsResult = this.amazonEc2.describeSecurityGroups();
           allSecurityGroups = describeSecurityGroupsResult.getSecurityGroups();
           
        } catch(AmazonClientException e) {
           System.out.println("ERROR : fetching all security groups in the account.");
           throw e;
        }
        
        List<String> allSecurityGroupsNames = allSecurityGroups.stream().map(e -> e.getGroupName()).collect(Collectors.toList());
        System.out.println("INFO : Security Groups Names : " + allSecurityGroupsNames);
        
        return allSecurityGroups;
    }
    
    /**
     * This method returns all non-compliant security groups.
     * Security groups which allow traffic from 0.0.0.0/0 and ports 80,443,ALL are non-compliant.
     * @param allSecurityGroups
     * @return List<SecurityGroup> returns all non compliant security groups. 
     */
    public List<SecurityGroup> getNonComplaintSecurityGroups(List<SecurityGroup> allSecurityGroups) {
        
         List<SecurityGroup> nonCompliantSecurityGroups = new ArrayList<SecurityGroup>();
         
         // Check if number of security groups is 0 or null. 
         if ( allSecurityGroups.size() != 0 || allSecurityGroups != null ) {
            
             for(SecurityGroup sg: allSecurityGroups) {
                 
                 
                 Boolean compliantSG = true;
                 List<IpPermission> igressIpPermissions = sg.getIpPermissions();
                 
                 // Check if igress permissions size greater than 0, if 0 security group is compliant.
                 if(igressIpPermissions.size() > 0 ) {
                     
                     for(IpPermission ipPermission: igressIpPermissions) {
                         
                        List<String> ipRanges = ipPermission.getIpRanges();
                        Integer fromPort = ipPermission.getFromPort();
                        Integer toPort = ipPermission.getFromPort();
                        
                        // Check if ip ranges greater than 0 and ipRanges contains 0.0.0.0/0, else security group is compliant.
                        if( ipRanges.size() > 0 && ipRanges.contains(SOURCE_IP_ADDRESS)) {
                            
                            if ( fromPort == null || toPort == null) {
                                //ALL non compliant.
                                compliantSG = false;
                                nonCompliantSecurityGroups.add(sg);
                                
                                //Need to use Set's instead of ArrayList. Once SecurityGroup is confirmed NON-COMPLIANT no need to check other rules.
                                break;
                                
                             } else if (! (toPort.equals(HTTP_PORT) || toPort.equals(HTTPS_PORT) || fromPort.equals(HTTP_PORT) || fromPort.equals(HTTPS_PORT))) {
                                // All ports other than 80, 443 are non-compliant.
                                compliantSG = false;
                                nonCompliantSecurityGroups.add(sg);
                                //Need to use Set's instead of ArrayList. Once SecurityGroup is confirmed NON-COMPLIANT no need to check other rules.
                                break;
                            } 
                            
                        }
                    
                     }
                 }
                 
                if (compliantSG) {
                    System.out.println("INFO : SG GROUP : " + sg.getGroupName() + " : " + sg.getGroupId() + " is COMPLIANT."); 
                } else {
                    System.out.println("INFO : SG GROUP : " + sg.getGroupName() + " : " + sg.getGroupId() + " is NON COMPLIANT.");
                }
             }
         } 
        
        System.out.println("INFO : Number of NON COMPLIANT SECURITY GROUPS : " + nonCompliantSecurityGroups.size());
        List<String> allNonCompliantSecurityGroupsNames = nonCompliantSecurityGroups.stream().map(e -> e.getGroupName()).collect(Collectors.toList());
        System.out.println("INFO : NON COMPLIANT SECURITY GROUPS : " + allNonCompliantSecurityGroupsNames);
        
        return nonCompliantSecurityGroups;
    }
    
    /**
     * This method returns all the volumes.
     * @return returns list of EBS volumes.
     */
    public List<Volume> getAllEBSVolumes() throws AmazonClientException {
        
        List<Volume> allVolumes;
        try {
            DescribeVolumesResult describeVolumesResult = this.amazonEc2.describeVolumes();
            allVolumes = describeVolumesResult.getVolumes();
            
        } catch(AmazonClientException e) {
            System.out.println("ERROR : fetching volumes.");
            e.printStackTrace();
            throw e;
        }
        
        List<String> allVolumesIds = allVolumes.stream().map(e -> e.getVolumeId()).collect(Collectors.toList());
        System.out.println("INFO : All Volumes Ids : " + allVolumesIds);
        return allVolumes;
    }
    
     /**
     * This method returns all encrypted EBS volumes.
     * @param allVolumes
     * @return returns list of all encrypted EBS volumes.
     */
    
    public List<Volume> getAllEncryptedEBSVolumes(List<Volume> allEBSVolumes) {
        
        List<Volume> allEncryptedEBSVolumes = new ArrayList<Volume>();
        
        for(Volume volume : allEBSVolumes) {
            if( volume.isEncrypted()) {
                allEncryptedEBSVolumes.add(volume);
            }
        }
        
        List<String> allEncryptedVolumesIds = allEncryptedEBSVolumes.stream().map(e -> e.getVolumeId()).collect(Collectors.toList());
        System.out.println("INFO : All Encrypted EBS volumes : " + allEncryptedVolumesIds);
        return allEncryptedEBSVolumes;
    }
    
    /**
     * This method returns all non encrypted volumes.
     * @return returns list of all non encrypted volumes.
     */
    public List<Volume> getAllNonEncryptedEBSVolumes(List<Volume> allEBSVolumes) {
        
        
        List<Volume> allEncryptedEBSVolumes = getAllEncryptedEBSVolumes(allEBSVolumes);
        
        //Non encrypted volumes.
        allEBSVolumes.removeAll(allEncryptedEBSVolumes);
        List<Volume> allNonEncryptedEBSVolumes = allEBSVolumes;
        
        List<String> allNonEncryptedEBSVolumesIds = allNonEncryptedEBSVolumes.stream().map(e -> e.getVolumeId()).collect(Collectors.toList());
        System.out.println("INFO : All Non Encrypted EBS volumes : " + allNonEncryptedEBSVolumesIds);
        return allNonEncryptedEBSVolumes;
        
    }
    
    /**
     * This method returns all EC2 instances.
     * @return
     * @throws AmazonClientException 
     */
    public List<Instance> getAllInstances() throws AmazonClientException {
        
        String marker = null;
        List<Instance> allInstances = new ArrayList<>();
        
        try {
            
            while ( true ) {
                DescribeInstancesRequest describeInstancesRequest = new DescribeInstancesRequest();
                describeInstancesRequest.setNextToken(marker);
                DescribeInstancesResult describeInstancesResult = this.amazonEc2.describeInstances(describeInstancesRequest);
                List<Reservation> reservations = describeInstancesResult.getReservations();
                for(Reservation reservation: reservations) {
                    allInstances.addAll(reservation.getInstances());
                }

                marker = describeInstancesResult.getNextToken();
                if ( marker == null) {
                    break;
                }
            }
            
        } catch(AmazonClientException e) {
            System.out.println("ERROR : Fetching all EC2 instances.");
            e.printStackTrace();
            throw e;
        }
        
        System.out.println("INFO : Number of EC2 instances : " + allInstances.size());
        List<String> instancesId = allInstances.stream().map( e -> e.getInstanceId()).collect(Collectors.toList());
        System.out.println("INFO : EC2 Instances : " + instancesId);
        return allInstances;
    }
    
    /**
     * This method returns all EBS non root volumes.
     * @return 
     */
    public List<Volume> getAllEBSNonRootVolumes(List<Volume> allEBSVolumes, List<Volume> allEBSRootVolumes) {
       
       System.out.println("INFO : Main Code : allEBSVolumes size : " + allEBSVolumes.size());
       System.out.println("INFO : Main Code : allEBSVolumes  : " + allEBSVolumes);
       System.out.println("INFO : Main Code : allEBSRootVolumes size : " + allEBSRootVolumes.size());
       System.out.println("INFO : Main Code : allEBSRootVolumes : " + allEBSRootVolumes);
       allEBSVolumes.removeAll(allEBSRootVolumes);
       
       System.out.println("INFO : Number of Non Root EBS volumes Size : " + allEBSVolumes.size());
       System.out.println("INFO : Number of Non Root EBS volumes : " + allEBSVolumes);
       List<String> allEBSNonRootVolumeIds = allEBSVolumes.stream().map(e -> e.getVolumeId()).collect(Collectors.toList());
       System.out.println("INFO : EBS Non Root Volumes IDs : " + allEBSNonRootVolumeIds);
       
       return allEBSVolumes;
    }
    
    /**
     * This method returns all EBS root volumes.
     * @return 
     */
    public List<Volume> getAllEBSRootVolumes() {
        
        List<Instance> allInstances =  getAllInstances();
        List<Volume> allEBSRootVolumes = new ArrayList<>();
        
        for(Instance instance: allInstances) {
            
            //We need volumes of type only EBS.
            if ( instance.getRootDeviceType().equalsIgnoreCase(DeviceType.Ebs.toString())) {
                String rootDeviceName = instance.getRootDeviceName();
                List<InstanceBlockDeviceMapping> instanceBlockDeviceMappings = instance.getBlockDeviceMappings();
                for(InstanceBlockDeviceMapping instanceBlockDeviceMapping: instanceBlockDeviceMappings) {
                    if(instanceBlockDeviceMapping.getDeviceName().equalsIgnoreCase(rootDeviceName)) {
                        String volumeId = instanceBlockDeviceMapping.getEbs().getVolumeId();
                        Volume volume = new Volume().withVolumeId(volumeId);
                        allEBSRootVolumes.add(volume);
                    }
                }
            }
        }
        
        System.out.println("INFO: NUMBER OF EBS ROOT VOLUMES : " + allEBSRootVolumes.size());
        List<String> volumeIds = allEBSRootVolumes.stream().map(e -> e.getVolumeId()).collect(Collectors.toList());
        System.out.println("INFO: EBS ROOT VOLUMES : " + volumeIds);
        
        return allEBSRootVolumes;
    }
    
}
