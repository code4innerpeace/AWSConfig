/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.vb.aws.services.compute.ec2;

import com.amazonaws.AmazonClientException;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.ec2.model.SecurityGroup;
import java.util.List;
import java.util.Map;

/**
 *
 * @author Vijay Bheemineni
 */
public interface AWSRegionUtils {
    
    public Map<String,List<SecurityGroup>> getAllSecurityGroups() throws AmazonClientException;
    public List<SecurityGroup> getAllSecurityGroupsByRegion(Regions region) throws AmazonClientException;
    
}
