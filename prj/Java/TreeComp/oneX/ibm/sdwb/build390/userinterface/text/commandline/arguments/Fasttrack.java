package com.ibm.sdwb.build390.userinterface.text.commandline.arguments;

import java.util.*;

public class Fasttrack extends EnumeratedCommandLineArgument {

    private static final String keyword = "FASTTRACK";
    private static final String explaination ="<yes>\n"+
                                              "yes - Perform a stripped down, strictly compile version of a build";

    private static final Set possibleSettings = new HashSet();

    static{
        possibleSettings.add("YES");
        possibleSettings.add("yes");
    }


    public Fasttrack() {
        super(keyword,explaination,possibleSettings);
        changeValueToUpperCase();
    }
}
