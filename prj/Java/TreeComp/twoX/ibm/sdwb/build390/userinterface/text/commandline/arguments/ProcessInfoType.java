package com.ibm.sdwb.build390.userinterface.text.commandline.arguments;

import java.util.*;

public class ProcessInfoType extends EnumeratedCommandLineArgument {

    private static final String keyword = "INFO";
    private static final String explaination = "\n<PHASES/OPTIONS/FULL/(BASIC)>" +
                                               "\nBASIC - Returns basic information about a process." +
                                               "\nPHASES - Returns phase information about a process."+
                                               "\nOPTIONS - Returns options  information about a process."+
                                               "\nFULL - Returns full information about a process."; 

    private static final Set possibleSettings = new HashSet();

    static{
        possibleSettings.add("BASIC");
        possibleSettings.add("PHASES");
        possibleSettings.add("OPTIONS");
        possibleSettings.add("FULL");
    }

    public ProcessInfoType() {
        super(keyword, explaination, possibleSettings);
        setDefaultValue("BASIC");
        changeValueToUpperCase();
    }
}
