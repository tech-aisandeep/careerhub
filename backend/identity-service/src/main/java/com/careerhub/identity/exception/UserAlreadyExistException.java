package com.careerhub.identity.exception;

import com.careerhub.identity.entities.User;

public class UserAlreadyExistException extends RuntimeException{
    public UserAlreadyExistException(String email){
        super("User already exist this email" + email);
    }
}
