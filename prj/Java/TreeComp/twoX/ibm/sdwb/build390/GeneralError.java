package com.ibm.sdwb.build390;

import java.io.File;

public class GeneralError extends MBBuildException {

    public GeneralError(String errorMessage) {
        super("General Error", errorMessage, MBConstants.GENERALERROR);
    }

    public GeneralError(String message, Exception e) {
        super("General Error", message, e);
    }

    public GeneralError(String errorMessage, File errorFile) {
        super("General Error", errorMessage, MBConstants.GENERALERROR);
        setErrorFile(errorFile);
    }

    public GeneralError(String message, Exception e, File errorFile) {
        super("General Error", message, e);
        setErrorFile(errorFile);
    }
}


