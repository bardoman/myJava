
package com.ibm.sdwb.build390.help;

public class HelpException extends com.ibm.sdwb.build390.MBBuildException {

    public HelpException(String errorMessage) {
        super("Help Error", errorMessage, com.ibm.sdwb.build390.MBConstants.GENERALERROR);
    }

    public HelpException(String errorMessage, Exception e) {
        super("Help Error", errorMessage, e, com.ibm.sdwb.build390.MBConstants.GENERALERROR);
    }
}
