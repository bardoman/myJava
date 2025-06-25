package com.ibm.sdwb.build390.userinterface.text.commandline.arguments;

import java.util.*;

public class ExtendedCheck extends EnumeratedCommandLineArgument {

    private static final String keyword = "EXTENDEDCHECK";
    private static final String explaination = "\n<FAIL/INACTIVE/(NO)>" +
                                               "\nFAIL     - Perform extended check and  fail if there are parts\nin the driver that are not in the part list."+
                                               "\nINACTIVE - Perform extended check and Inactivate parts in the \ndriver that are not in the part list."+
                                               "\nNO       - Don't perform extended check.\n";
    private static final Set possibleValues = new HashSet();

    static{
        possibleValues.add("FAIL");
        possibleValues.add("INACTIVE");
        possibleValues.add("NO");
    }


    public ExtendedCheck(){
        super(keyword, explaination, possibleValues);
        setDefaultValue("NO");
        changeValueToUpperCase();
    }
}
