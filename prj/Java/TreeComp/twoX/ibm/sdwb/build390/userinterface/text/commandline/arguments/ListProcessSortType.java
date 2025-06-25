package com.ibm.sdwb.build390.userinterface.text.commandline.arguments;

import java.util.*;

public class ListProcessSortType extends EnumeratedCommandLineArgument {

    private static final String keyword = "SORT";

    private static final String explaination = "\n<PROCESSID/(TIME)>" +
                                               "\nPROCESSID - Sorts by process ID." +
                                               "\nTIME - Sorts by time process was run.";

    private static final Set possibleSettings = new HashSet();

    static{
        possibleSettings.add("PROCESSID");
        possibleSettings.add("TIME");
    }

    public ListProcessSortType() {
        super(keyword, explaination, possibleSettings);
        setDefaultValue("TIME");
        changeValueToUpperCase();
    }
}
