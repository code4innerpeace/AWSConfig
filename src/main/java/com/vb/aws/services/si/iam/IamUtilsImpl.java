/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.vb.aws.services.si.iam;

import com.amazonaws.AmazonClientException;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.identitymanagement.AmazonIdentityManagement;
import com.amazonaws.services.identitymanagement.AmazonIdentityManagementClient;
import com.amazonaws.services.identitymanagement.model.ListMFADevicesRequest;
import com.amazonaws.services.identitymanagement.model.ListMFADevicesResult;
import com.amazonaws.services.identitymanagement.model.ListUsersRequest;
import com.amazonaws.services.identitymanagement.model.ListUsersResult;
import com.amazonaws.services.identitymanagement.model.User;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 *
 * @author Vijay Bheemineni
 */
public class IamUtilsImpl implements IamUtils {
    
    private AmazonIdentityManagement iamClient;
    
    /**
     * Parameterized constructor. Pass the AWS Region as parameter.
     * @param region 
     */
    public IamUtilsImpl(Regions region) {
        this.iamClient = new AmazonIdentityManagementClient().withRegion(region);        
    }
    
    /**
     * Default constructor.
     */
    public IamUtilsImpl() {
        this.iamClient = new AmazonIdentityManagementClient();
    }
    
    /**
     * This method returns all Iam users.
     * @return List<User> returns all Iam users.
     * @throws AmazonClientException
     */
    
    public List<User> getAllIamUsers() throws AmazonClientException {
        
        String marker = null;
        List<User> allIamUsers = new ArrayList<>();
        
        try {
             // Fetch all iam users.
            while(true) {
            
                ListUsersRequest listUsersRequest = new ListUsersRequest();
                listUsersRequest.setMarker(marker);
                ListUsersResult listUsersResult = this.iamClient.listUsers(listUsersRequest);
                allIamUsers.addAll(listUsersResult.getUsers());
            
                // Check the listUsersResult is truncated. This method returns users in batches of 100.
                if ( listUsersResult.isTruncated() ) {
                    marker = listUsersResult.getMarker();			
                } else {
                    break;
                }
            }
        } catch(AmazonClientException e) {
            System.out.println("ERROR : fetching all iam users");
            e.printStackTrace();
            throw e;
        }
        
        
        List<String> allIamUsersName = allIamUsers.stream().map(e -> e.getUserName()).collect(Collectors.toList());
        System.out.println("INFO : Number of Iam users : " + allIamUsers.size());
        System.out.println("INFO : Iam users : " + allIamUsersName);
        
        return allIamUsers;
    }
    
    /**
     * Returns all Iam users for whom MFA is not enabled.
     * @param allIamUsers
     * @return List<User> :- return all Iam users whose MFA is not enabled.
     */
    public List<User> getAllMFANotEnabledUsers(List<User> allIamUsers) {
        
        List<User> allMFANotEnabledUsers = new ArrayList<>();
        if ( allIamUsers != null || allIamUsers.size() > 0 ) {
            for ( User user: allIamUsers) {
                if (! isMFAEnabled(user)) {
                    allMFANotEnabledUsers.add(user);
                }
            }
        }
        
        System.out.println("INFO : Number of MFA Not Enabled Users : " + allMFANotEnabledUsers.size());
        System.out.println("INFO : All MFA Not Enabled Users : " + allMFANotEnabledUsers);
        
        return allMFANotEnabledUsers;
    }
    
    /**
     * Checks if user MFA is enabled or not.
     * @param user
     * @return returns true, if MFA is enabled for the user.
     */
    public Boolean isMFAEnabled(User user) throws AmazonClientException {
        
        Boolean mfaDeviceEnabled = false;
        try {
            if ( user != null ) {
                ListMFADevicesRequest listMFADevicesRequest = new ListMFADevicesRequest(user.getUserName());
                ListMFADevicesResult listMFADevicesResult = this.iamClient.listMFADevices(listMFADevicesRequest);
                if ( listMFADevicesResult.getMFADevices().size() > 0) {
                    mfaDeviceEnabled = true;
                }
            }
            
        }catch(AmazonClientException e) {
            System.out.println("ERROR : Fetching list of MFA Devices.");
            e.printStackTrace();
            throw e;
        }
        //System.out.println("INFO : MFA enabled for the user? " + mfaDeviceEnabled);
        return mfaDeviceEnabled;
        
    }
    
    
}
