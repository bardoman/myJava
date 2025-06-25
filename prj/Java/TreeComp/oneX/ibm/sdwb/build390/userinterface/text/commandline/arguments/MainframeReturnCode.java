package com.ibm.sdwb.build390.userinterface.text.commandline.arguments;

import java.util.*;

public class MainframeReturnCode extends EnumeratedCommandLineArgument {

    private static final String keyword = "BUILDJOBRETURNCODE";
    private static final String explaination = "\n<(4)/8>" +
                                               "\nIndication that the build should be terminated if\nany job results in the return code specified\n(or) higher";
    private static final Set possibleValues = new HashSet();

    static{
        possibleValues.add("4");
        possibleValues.add("8");
    }

    public MainframeReturnCode(){
        super(keyword,explaination,possibleValues);
        addAlternativeName("BUILDCC");
        setDefaultValue("4");
    }

    public int getValueInteger(){
        return Integer.parseInt(getValue());
    }
}
