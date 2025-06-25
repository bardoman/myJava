package com.ibm.sdwb.build390.userinterface.text.commandline.arguments;

import com.ibm.sdwb.build390.*;

public class RestartPhase extends CommandLineArgument {
    private static final String keyword = "RESTARTPHASE";
    private static final String explaination = "\nSpecifies the index value of a phase to restart.\nA list of the phases is located in the PROCESSINFO INFO=PHASES report."; 

    public RestartPhase() {
        super(keyword,explaination);
    }

    public int getIntValue() throws MBBuildException
    {
        try {
            int i = Integer.valueOf(getValue()).intValue();

            if (i<=0) {
                throw new GeneralError("Error:Minimum restart phase value is 1");
            }
            return i;
        } catch (NumberFormatException nfe) {
            throw new GeneralError("Error:Number format exception converting restart phase value", nfe);
        }
    }
}
