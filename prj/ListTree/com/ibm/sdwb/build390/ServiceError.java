package com.ibm.sdwb.build390;


public class ServiceError extends MBBuildException {

    public ServiceError(String errorMessage) {
        super("Service Error", errorMessage, MBConstants.SERVICEERROR);
    }

    public ServiceError(String errorMessage, Exception e) {
        super("Service Error", errorMessage, e, MBConstants.SERVICEERROR);
    }
}


