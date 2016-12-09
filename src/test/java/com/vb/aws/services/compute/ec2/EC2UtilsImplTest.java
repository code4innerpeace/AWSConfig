/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.vb.aws.services.compute.ec2;

import com.amazonaws.AmazonClientException;
import com.amazonaws.services.ec2.AmazonEC2;
import com.amazonaws.services.ec2.AmazonEC2Client;
import com.amazonaws.services.ec2.model.Address;
import com.amazonaws.services.ec2.model.DescribeVolumesResult;
import com.amazonaws.services.ec2.model.Instance;
import com.amazonaws.services.ec2.model.IpPermission;
import com.amazonaws.services.ec2.model.SecurityGroup;
import com.amazonaws.services.ec2.model.Volume;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.junit.Test;
import static org.junit.Assert.*;
import org.junit.Before;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 *
 * @author Vijay Bheemineni
 */
public class EC2UtilsImplTest {
    
    private EC2UtilsImpl ec2UtilsImpl;
    private List<SecurityGroup> sgs;
    private List<Volume> allEBSVolumes = new ArrayList<>();
    private List<Instance> allInstances = new ArrayList<>();
    private List<Volume> allEBSRootVolumes = new ArrayList<>();
    private Volume volume1,volume2,volume3;
    private List<Address> allEIPs = new ArrayList<>();
    private Address address1,address2;
    
    public EC2UtilsImplTest() {
    }

    @Before
    public void setup() {
        
        ec2UtilsImpl = mock(EC2UtilsImpl.class);
        
        // Security Groups.
        sgs = new ArrayList<SecurityGroup>();
        
        //Defining Security Group 1. This security group is COMPLIANT.
        
        SecurityGroup sg1 = new SecurityGroup();
        IpPermission ip1 = new IpPermission();
        ip1.setFromPort(80);
        ip1.setToPort(80);
        List<String> ipRanges = Arrays.asList("0.0.0.0/0", "148.181.0.3");
        ip1.setIpRanges(ipRanges);
        
        IpPermission ip2 = new IpPermission();
        ip2.setFromPort(443);
        ip2.setToPort(443);
        List<String> ip2Ranges = Arrays.asList("0.0.0.0/0", "148.181.0.3");
        ip1.setIpRanges(ip2Ranges);
        sg1.setIpPermissions(Arrays.asList(ip1,ip2));
        sg1.setGroupName("VAS_COMPLIANT");
        
        //Defining Security Group 1. This security group is NON-COMPLIANT.
        
        SecurityGroup sg2 = new SecurityGroup();
        IpPermission ip3 = new IpPermission();
        ip3.setFromPort(22);
        ip3.setToPort(22);
        List<String> ipRanges3 = Arrays.asList("0.0.0.0/0");
        ip3.setIpRanges(ipRanges3);
        sg2.setIpPermissions(Arrays.asList(ip3));
        sg2.setGroupName("VAS_NONCOMPLIANT");
        
        // Add sg1,sg2 to security groups list.
        sgs.addAll(Arrays.asList(sg1,sg2));
        
        // Volumes
        volume1 = new Volume();
        volume1.setEncrypted(Boolean.TRUE);
        volume1.setVolumeId("volume1");
        
        volume2 = new Volume();
        volume2.setEncrypted(Boolean.FALSE);
        volume2.setVolumeId("volume2");
        
        volume3 = new Volume();
        volume3.setEncrypted(Boolean.TRUE);
        volume3.setVolumeId("volume3");
        
        allEBSVolumes.add(volume1);
        allEBSVolumes.add(volume2);
        allEBSVolumes.add(volume3);
        
        // Instances
        Instance instance1 = new Instance();
        Instance instance2 = new Instance();
        
        allInstances.addAll(Arrays.asList(instance1,instance2));
        
        allEBSRootVolumes = Arrays.asList(allEBSVolumes.get(0));
        
        // EIPs
        Address address1 = new Address();
        Address address2 = new Address();
        allEIPs.addAll(Arrays.asList(address1,address2));
        
        
    }
    
    @Test
    public void testGetAllSecurityGroups() {
        
        when(ec2UtilsImpl.getAllSecurityGroups()).thenReturn(sgs);
        List<SecurityGroup> output = ec2UtilsImpl.getAllSecurityGroups();
        assertEquals(2, output.size());
    }
    
    @Test(expected = AmazonClientException.class)
    public void testGetAllSecurityGroupsThrowsAmazonClientException() {
        when(ec2UtilsImpl.getAllSecurityGroups()).thenThrow(AmazonClientException.class);
        ec2UtilsImpl.getAllSecurityGroups();
    }
    
    @Test
    public void testGetNonComplaintSecurityGroups() {
       
        EC2UtilsImpl ec2UtilsImpl = new EC2UtilsImpl();
        List<SecurityGroup> nonCompliantSecurityGroups = ec2UtilsImpl.getNonComplaintSecurityGroups(sgs);
        assertEquals(1, nonCompliantSecurityGroups.size());
        assertEquals("VAS_NONCOMPLIANT", nonCompliantSecurityGroups.get(0).getGroupName());
    }
    
    @Test
    public void testGetAllEBSVolumes() {
        Volume volume1 = new Volume();
        Volume volume2 = new Volume();
        DescribeVolumesResult describeVolumesResult = new DescribeVolumesResult();
        describeVolumesResult.setVolumes(Arrays.asList(volume1,volume2));
        AmazonEC2 amazonEc2 = mock(AmazonEC2Client.class);
        when(amazonEc2.describeVolumes()).thenReturn(describeVolumesResult);
        assertEquals(2,describeVolumesResult.getVolumes().size());
    }
    
    @Test(expected = AmazonClientException.class)
    public void testGetAllEBSVolumesThrowsAmazonClientException() {
        when(ec2UtilsImpl.getAllEBSVolumes()).thenThrow(AmazonClientException.class);
        ec2UtilsImpl.getAllEBSVolumes();
    }
    
    
    @Test
    public void testGetAllEncryptedEBSVolumes() {
        
        EC2UtilsImpl ec2UtilsImpl = new EC2UtilsImpl();
        List<Volume> allEncryptedEBSVolumes = ec2UtilsImpl.getAllEncryptedEBSVolumes(allEBSVolumes);
        assertEquals(2,allEncryptedEBSVolumes.size());
        
    }
    
    @Test
    public void testGetAllNonEncryptedEBSVolumes() {
        
        EC2UtilsImpl ec2UtilsImpl = new EC2UtilsImpl();
        List<Volume> allEncryptedEBSVolumes = ec2UtilsImpl.getAllNonEncryptedEBSVolumes(allEBSVolumes);
        assertEquals(1,allEncryptedEBSVolumes.size());
        
    }
    
    @Test
    public void testGetAllEBSNonRootVolumes() {
        
        EC2UtilsImpl ec2UtilsImpl = new EC2UtilsImpl();
        System.out.println("INFO : All EBS Volumes Size : " + allEBSVolumes.size());
        System.out.println("INFO : All EBS Root Volumes Size : " + allEBSRootVolumes.size());
        List<Volume> allEBSNonRootVolumes = ec2UtilsImpl.getAllEBSNonRootVolumes(allEBSVolumes, allEBSRootVolumes);
        assertEquals(2,allEBSNonRootVolumes.size());
        
        
    }
    
    @Test
    public void testGetAllEBSRootVolumes() {
        
        EC2UtilsImpl ec2UtilsImpl = mock(EC2UtilsImpl.class);
        List<Volume> allEBSRootVolumes = Arrays.asList(allEBSVolumes.get(0));
        when(ec2UtilsImpl.getAllEBSRootVolumes()).thenReturn(allEBSRootVolumes);
        assertEquals(1,allEBSRootVolumes.size());
        
        
    }
    
    @Test
    public void testGetAllInstances() {
        EC2UtilsImpl ec2UtilsImpl = mock(EC2UtilsImpl.class);
        when(ec2UtilsImpl.getAllInstances()).thenReturn(allInstances);
        assertEquals(2,allInstances.size());
    }
    
    @Test(expected = AmazonClientException.class)
    public void testGetAllInstancesThrowsAmazonClientException() {
        when(ec2UtilsImpl.getAllInstances()).thenThrow(AmazonClientException.class);
        ec2UtilsImpl.getAllInstances();
    }
    
    @Test
    public void testGetAllEIPs() {
        
        EC2UtilsImpl ec2UtilsImpl = mock(EC2UtilsImpl.class);
        when(ec2UtilsImpl.getAllEIPs()).thenReturn(allEIPs);
        assertEquals(2, allEIPs.size());
    }
    
    @Test(expected = AmazonClientException.class)
    public void testGetAllEIPsThrowsAmazonClientException() {
        when(ec2UtilsImpl.getAllEIPs()).thenThrow(AmazonClientException.class);
        ec2UtilsImpl.getAllEIPs();
    }
    
    @Test
    public void testGetAllUnusedEIPs() {
        
        EC2UtilsImpl ec2UtilsImpl = new EC2UtilsImpl();
        List<Address> allUnusedEIPs = ec2UtilsImpl.getAllUnusedEIPs(allEIPs);
        assertEquals(2, allUnusedEIPs.size());
    }

   
}
