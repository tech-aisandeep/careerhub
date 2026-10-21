package com.careerhub.identity.exception;

public class InvalidCredentialsException extends RuntimeException{
    public InvalidCredentialsException(){
        super("Invalid Email or Password");
    }
}
