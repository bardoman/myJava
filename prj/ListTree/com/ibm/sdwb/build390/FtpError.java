package com.ibm.sdwb.build390;


public class FtpError extends MBBuildException {

    public FtpError(String errorMessage) {
        super("FTP Error", errorMessage, MBConstants.FTPERROR);
    }

    public FtpError(String errorMessage, Exception e) {
        super("FTP Error", errorMessage, e, MBConstants.FTPERROR);
    }
}


