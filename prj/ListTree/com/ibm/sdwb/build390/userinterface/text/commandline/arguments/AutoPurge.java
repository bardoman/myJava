package com.ibm.sdwb.build390.userinterface.text.commandline.arguments;

import java.util.*;

public class AutoPurge extends BinaryCommandLineArgument {

    private static final String keyword = "AUTOPURGE";
    private static final String explaination = "\n<yes/(no)>" +
                                               "\nyes - This will automatically purge successful jobs as they complete";

    public AutoPurge(){
        super(keyword,explaination,false);
        setDefaultValue("NO");
        changeValueToUpperCase();
    }
}
