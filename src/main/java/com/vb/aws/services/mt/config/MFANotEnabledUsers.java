/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.vb.aws.services.mt.config;

import com.amazonaws.regions.Regions;
import com.amazonaws.services.config.AmazonConfig;
import com.amazonaws.services.config.AmazonConfigClient;
import com.amazonaws.services.config.model.ComplianceType;
import com.amazonaws.services.config.model.Evaluation;
import com.amazonaws.services.config.model.InvalidParameterValueException;
import com.amazonaws.services.config.model.InvalidResultTokenException;
import com.amazonaws.services.config.model.MessageType;
import com.amazonaws.services.config.model.NoSuchConfigRuleException;
import com.amazonaws.services.config.model.PutEvaluationsRequest;
import com.amazonaws.services.config.model.PutEvaluationsResult;
import com.amazonaws.services.identitymanagement.model.User;
import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.events.ConfigEvent;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.vb.aws.services.exceptions.NotScheduledNotificationException;
import com.vb.aws.services.si.iam.IamUtilsImpl;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 *
 * @author Vijay Bheemineni
 */
public class MFANotEnabledUsers {
    
    private static final String MESSAGE_TYPE_PROPERTY = "messageType";
    private static final String COMPLIANCE_RESOURCE_TYPE = "AWS::IAM::User";
    private static final Integer MAX_NUMBER_OF_EVALUATIONS = 100;
    private static final String AWS_REGION_PROPERTY = "AWS_DEFAULT_REGION";
    private static final String AWS_DEFAULT_REGION = "us-east-1";
    
    public void handle(ConfigEvent event, Context context) throws IOException {
         
        Regions region = Regions.fromName(System.getenv(AWS_REGION_PROPERTY));
        
        // AWS Config client.
        AmazonConfig awsConfig = new AmazonConfigClient().withRegion(region);
        
        IamUtilsImpl iamUtilsImpl = new IamUtilsImpl(region);
        
        doHandle(event, context, awsConfig, iamUtilsImpl);
        
     }
     
     // Handle the event.
    public void doHandle(ConfigEvent event,Context context, AmazonConfig awsConfig, IamUtilsImpl iamUtilsImpl) throws IOException {
        
        // Fetch invoking event.
        JsonNode invokingEvent = new ObjectMapper().readTree(event.getInvokingEvent());
        
        // Check if invoking event is of type ScheduleNotification or not.
        checkForInCompatibleTypes(invokingEvent);
        
        // Fetch all Iam Users.
        List<User> allIamUsers = iamUtilsImpl.getAllIamUsers();
        
        // Fetch all MFA Not enabled Users.
        List<User> allMFANotEnabledUsers = iamUtilsImpl.getAllMFANotEnabledUsers(allIamUsers);
        
        List<Evaluation> evaluations = createEvaluations(allMFANotEnabledUsers);
        
        doPutEvaluations(awsConfig,event,evaluations);

        
        
    }
    
    // Check if message is of type ScheduledNotification, if not throw NotScheduledNotificationException exception.
    private void checkForInCompatibleTypes(JsonNode invokingEvent) {
        
        String messageType = invokingEvent.get(MESSAGE_TYPE_PROPERTY).asText();
        if (!MessageType.ScheduledNotification.toString().equals(messageType)) {
            System.out.println("ERROR : Message Types of kind : " + messageType + " are not evaluated by this config rule.");
            throw new NotScheduledNotificationException("Message Types of kind : " + messageType + " are not evaluated by this config rule.");
        }
    }
    
    /**
     * 
     * @param allMFANotEnabledUsers
     * @return List<Evaluation> returns list of Evaluation objects.
     */
    private List<Evaluation> createEvaluations(List<User> allMFANotEnabledUsers) {
        
        List<Evaluation> evaluations = new ArrayList<>();
        
        if ( allMFANotEnabledUsers == null || allMFANotEnabledUsers.size() > 0 ) {
            
            for(User user: allMFANotEnabledUsers) {
                
                String userName = user.getUserName();
                Evaluation evaluation = new Evaluation();
                evaluation.setComplianceResourceId(userName);
                evaluation.setComplianceResourceType(COMPLIANCE_RESOURCE_TYPE);
                evaluation.setComplianceType(ComplianceType.NON_COMPLIANT);
                evaluation.setOrderingTimestamp(new Date());
                evaluations.add(evaluation);
            }
        }
        
        System.out.println("INFO : Number of evaluations : " + evaluations.size());
        return evaluations;
        
    }
    
    /**
     * This method puts the evaluations on config rule.
     * @param awsConfig
     * @param event
     * @param evaluations 
     */
    private void doPutEvaluations(AmazonConfig awsConfig,ConfigEvent event,List<Evaluation> evaluations ) throws InvalidParameterValueException,InvalidResultTokenException,NoSuchConfigRuleException {
        
        //Currently number of evaluations we can put is 100 at a time. So we need to put evaluations in batches.
        
        for ( int i = 0; i < evaluations.size(); i+=100) {
            List<Evaluation> subListEvaluations = evaluations.subList(i, Math.min(evaluations.size(), i+100));
            System.out.println("INFO : ITERATION : " + String.valueOf(i + 1) + ". Number of evaluations put in this iteration : " + subListEvaluations.size());
            
            PutEvaluationsRequest putEvaluationsRequest = new PutEvaluationsRequest();
            putEvaluationsRequest.setEvaluations(subListEvaluations);
            putEvaluationsRequest.setResultToken(event.getResultToken());
            
            try {
                PutEvaluationsResult  putEvaluationsResult = awsConfig.putEvaluations(putEvaluationsRequest);
                System.out.println("INFO : Number of failed evaluations : " + putEvaluationsResult.getFailedEvaluations().size());
                System.out.println("INFO : Failed evaluations : " + putEvaluationsResult.getFailedEvaluations());
            } catch(InvalidParameterValueException e) {
                System.out.println("ERROR : InvalidParameterValueException caught while putting evaluations.");
                e.printStackTrace();
                throw e;
            } catch(InvalidResultTokenException e) {
                System.out.println("ERROR : InvalidResultTokenException caught while putting evaluations.");
                e.printStackTrace();
                throw e;
            } catch(NoSuchConfigRuleException e) {
                System.out.println("ERROR : NoSuchConfigRuleException caught while putting evaluations.");
                e.printStackTrace();
                throw e;
            }
        }
    }
    
}
