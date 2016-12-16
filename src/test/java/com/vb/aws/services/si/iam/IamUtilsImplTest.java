/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.vb.aws.services.si.iam;

import com.amazonaws.AmazonClientException;
import com.amazonaws.services.identitymanagement.model.ListMFADevicesResult;
import com.amazonaws.services.identitymanagement.model.MFADevice;
import com.amazonaws.services.identitymanagement.model.User;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.junit.Test;
import static org.junit.Assert.*;
import org.junit.Before;
import org.mockito.Mockito;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 *
 * @author Vijay Bheemineni
 */
public class IamUtilsImplTest {
    
    private List<User> allIamUsers;
    private IamUtilsImpl iamUtilsImpl;
    private User user1,user2,user3;
    
    @Before
    public void setup() {
        
        iamUtilsImpl = new IamUtilsImpl();
        
        allIamUsers = new ArrayList<>();
        
        User user1 = new User();
        user1.setUserName("VASVijay1");
        
        User user2 = new User();
        user2.setUserName("VASVijay2");
        
        MFADevice mfaDevice1 = new MFADevice();
        mfaDevice1.setUserName(user2.getUserName());
        
        User user3 = new User();
        user3.setUserName("VASVijay3@vastech.com");
        MFADevice mfaDevice2 = new MFADevice();
        mfaDevice2.setUserName(user3.getUserName());
        
        allIamUsers.addAll(Arrays.asList(user1,user2,user3));
    }
    
    @Test
    public void testGetAllIamUsers() {
        
        IamUtilsImpl iamUtilsImpl = mock(IamUtilsImpl.class);
        when(iamUtilsImpl.getAllIamUsers()).thenReturn(allIamUsers);
        List<User> output = iamUtilsImpl.getAllIamUsers();
        assertEquals(3, output.size());
    }
    
    @Test(expected = AmazonClientException.class)
    public void testGetAllIamUsersThrowsAmazonClientException() {
        IamUtilsImpl iamUtilsImpl = mock(IamUtilsImpl.class);
        when(iamUtilsImpl.getAllIamUsers()).thenThrow(AmazonClientException.class);
        iamUtilsImpl.getAllIamUsers();
    }
    
    @Test 
    public void testIsMFAEnabled() {
        
        Boolean mfaDeviceEnabled = false;
        ListMFADevicesResult listMFADevicesResult = mock(ListMFADevicesResult.class);
        List<MFADevice> mfaDevices = new ArrayList<>();
        MFADevice mfaDevice = new MFADevice();
        mfaDevices.add(mfaDevice);
        when(listMFADevicesResult.getMFADevices()).thenReturn(mfaDevices);
        
        if ( listMFADevicesResult.getMFADevices().size() > 0) {
            mfaDeviceEnabled = true;
        }
        
        assertEquals(true, mfaDeviceEnabled);
        
    }
    
    
    @Test(expected = AmazonClientException.class)
    public void testIsMFAEnabledThrowsAmazonClientException() {
        IamUtilsImpl iamUtilsImpl = mock(IamUtilsImpl.class);
        when(iamUtilsImpl.isMFAEnabled(user1)).thenThrow(AmazonClientException.class);
        iamUtilsImpl.isMFAEnabled(user1);
    }
    
   
    
    @Test 
    public void testGetAllMFANotEnabledUsers() {   
        // Not sure how to test this method. 'getAllMFANotEnabledUsers' method calls 'isMFAEnabled',
        // which inturn uses AWS api. So mocking below.
        IamUtilsImpl iamUtilsImpl = mock(IamUtilsImpl.class);
        when(iamUtilsImpl.getAllMFANotEnabledUsers(allIamUsers)).thenReturn(Arrays.asList(user3));
        List<User> allMFANotEnabledUsers = iamUtilsImpl.getAllMFANotEnabledUsers(allIamUsers);
        assertEquals(1,allMFANotEnabledUsers.size());
    }

}
