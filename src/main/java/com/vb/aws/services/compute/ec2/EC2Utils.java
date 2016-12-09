/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.vb.aws.services.compute.ec2;

import com.amazonaws.AmazonClientException;
import com.amazonaws.services.ec2.model.Address;
import com.amazonaws.services.ec2.model.Instance;
import com.amazonaws.services.ec2.model.SecurityGroup;
import com.amazonaws.services.ec2.model.Volume;
import java.util.List;
import java.util.Map;

/**
 *
 * @author Vijay Bheemineni
 */
public interface EC2Utils {
    
    // Security Group Names
    public List<SecurityGroup> getAllSecurityGroups() throws AmazonClientException;
    public List<SecurityGroup> getNonComplaintSecurityGroups(List<SecurityGroup> allSecurityGroups);
    
    // Instances
    public List<Instance> getAllInstances() throws AmazonClientException;
    
    // EBS Volumes
    public List<Volume> getAllEBSVolumes() throws AmazonClientException;
    public List<Volume> getAllEncryptedEBSVolumes(List<Volume> allEBSVolumes);
    public List<Volume> getAllNonEncryptedEBSVolumes(List<Volume> allEBSVolumes);
    public List<Volume> getAllEBSNonRootVolumes(List<Volume> allEBSVolumes, List<Volume> allEBSRootVolumes);
    public List<Volume> getAllEBSRootVolumes();
    
    // Cost Optimization
    public List<Volume> getEBSVolumesAvailable(List<Volume> allEBSVolumes);
    public List<Address> getAllEIPs() throws AmazonClientException;
    public List<Address> getAllUnusedEIPs(List<Address> allEIPs);
    

    
}
