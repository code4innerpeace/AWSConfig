/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.vb.aws.services.exceptions;

/**
 *
 * @author Vijay Bheemineni
 */
public class NotScheduledNotificationException extends RuntimeException {
    
    private static final long serialVersionUID = 1L;

    public NotScheduledNotificationException(String message) {
        super(message);
    }
}