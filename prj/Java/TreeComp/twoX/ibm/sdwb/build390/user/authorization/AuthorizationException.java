package com.ibm.sdwb.build390.user.authorization;

public class AuthorizationException extends Exception {

    public AuthorizationException(String message){
        super(message);
    }

    public AuthorizationException(String message, Throwable cause){
        super(message,cause);
    }

    public AuthorizationException(Throwable cause){
        super(cause);
    }

}
