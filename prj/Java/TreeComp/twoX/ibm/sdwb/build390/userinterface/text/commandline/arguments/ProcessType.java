package com.ibm.sdwb.build390.userinterface.text.commandline.arguments;

import java.util.*;

public class ProcessType extends EnumeratedCommandLineArgument {

    private static final String keyword = "PROCESSTYPE";
    private static final String explaination = "\n<DRIVERBUILD/USERBUILD/USERMOD/(ALL)>" +
                                               "\nDRIVERBUILD - Returns a list of available driver processes." +
                                               "\nUSERBUILD - Returns a list of available user processes." +
                                               "\nUSERMOD - Returns a list of available usermod processes." +
                                               "\nALL - Returns a list of all available processes."; 

    private static final Set possibleSettings = new HashSet();

    static{
        possibleSettings.add("DRIVERBUILD");
        possibleSettings.add("USERBUILD");
        possibleSettings.add("USERMOD");
        possibleSettings.add("ALL");
    }

    public ProcessType() {
        super(keyword, explaination, possibleSettings);
        setDefaultValue("ALL");
        changeValueToUpperCase();
    }
}
