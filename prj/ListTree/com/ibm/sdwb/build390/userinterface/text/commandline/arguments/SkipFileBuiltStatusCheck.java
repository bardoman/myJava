package com.ibm.sdwb.build390.userinterface.text.commandline.arguments;

import java.util.*;

public class SkipFileBuiltStatusCheck extends BinaryCommandLineArgument {

    private static final String keyword = "SKIPDCHECK";
    private static final String explaination = "\n<yes/(no)>" +
                                               "\nyes - Indication that the driver check phase is to be skipped";

    public SkipFileBuiltStatusCheck(){
        super(keyword,explaination,false);
        setDefaultValue("NO");
    }
}
