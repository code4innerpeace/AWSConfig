/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.vb.aws.services.si.iam;

import com.amazonaws.AmazonClientException;
import com.amazonaws.services.identitymanagement.model.User;
import java.util.List;

/**
 *
 * @author Vijay Bheemineni
 */
public interface IamUtils {
    
    
    public List<User> getAllIamUsers() throws AmazonClientException;
    public List<User> getAllMFANotEnabledUsers(List<User> allIamUsers);
    public Boolean isMFAEnabled(User user) throws AmazonClientException;
    
}
