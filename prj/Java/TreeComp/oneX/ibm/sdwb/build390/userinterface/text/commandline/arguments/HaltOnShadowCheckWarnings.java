package com.ibm.sdwb.build390.userinterface.text.commandline.arguments;

import java.util.*;

public class HaltOnShadowCheckWarnings extends BinaryCommandLineArgument {

    private static final String keyword = "HALTONSHADOWWARNINGS";
    private static final String explaination = "\n<yes/(no)>" +
                                               "\nyes - Terminate the build if warnings are issued\nwhen checking the shadow" +
                                               "\nno  - Do not terminate the build if warnings are issued\nwhen checking the shadow\n";


    public HaltOnShadowCheckWarnings(){
        super(keyword, explaination, false);
        setDefaultValue("NO");
    }
}
