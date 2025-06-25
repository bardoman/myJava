package com.ibm.sdwb.build390.userinterface.text.commandline.arguments;

import java.util.*;

public class AutomaticDependencyChecking extends EnumeratedCommandLineArgument {

    private static final String keyword = "AUTOBUILD";
    private static final String explaination = "\n<(yes)/manual/no>" +
                                               "\nyes    - Build all unbuilt parts and all dependent parts"+
                                               "\nno     - Build only unbuilt parts"+
                                               "\nmanual - Build all unbuilt parts and parts with explicit dependencies";
    private static final Set possibleSettings = new HashSet();

    static{
        possibleSettings.add("YES");
        possibleSettings.add("NO");
        possibleSettings.add("MANUAL");
    }

    public AutomaticDependencyChecking(){
        super(keyword, explaination, possibleSettings);
        addAlternativeName("AUTOBLD");
        setDefaultValue("YES");
        changeValueToUpperCase();
    }
}
