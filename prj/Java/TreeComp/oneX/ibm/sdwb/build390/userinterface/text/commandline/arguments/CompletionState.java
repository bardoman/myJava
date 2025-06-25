package com.ibm.sdwb.build390.userinterface.text.commandline.arguments;


import java.util.*;

public class CompletionState extends EnumeratedCommandLineArgument {

    private static final String keyword = "COMPLETED";

    private static final String explaination = "\n<TRUE/FALSE/(ALL)>" +
                                               "\nTRUE - Returns a list of completed processes." +
                                               "\nFALSE - Returns a list of incompleted processes." +
                                               "\nALL - Returns a list of all available processes."; 

    private static final Set possibleSettings = new HashSet();

    static{
        possibleSettings.add("TRUE");
        possibleSettings.add("FALSE");
        possibleSettings.add("ALL");
    }

    public CompletionState() {
        super(keyword, explaination, possibleSettings);
        setDefaultValue("ALL");
        changeValueToUpperCase();
    }
}
