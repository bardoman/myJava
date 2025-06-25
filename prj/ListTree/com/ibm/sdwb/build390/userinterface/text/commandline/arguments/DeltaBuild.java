package com.ibm.sdwb.build390.userinterface.text.commandline.arguments;

import java.util.*;

public class DeltaBuild extends BinaryCommandLineArgument {

    private static final String keyword = "DELTABUILD";
    private static final String explaination = "\n<yes/(no)>" +
                                               "\nyes - Indication that a delta build is to be done";

    public DeltaBuild(){
        super(keyword,explaination,false);
        setDefaultValue("NO");
    }
}
