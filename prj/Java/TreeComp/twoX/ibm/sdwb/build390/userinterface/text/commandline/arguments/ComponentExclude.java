package com.ibm.sdwb.build390.userinterface.text.commandline.arguments;

import java.util.*;

public class ComponentExclude extends BinaryCommandLineArgument {

    private static final String keyword = "EXCLUDECOMPONENT";
    private static final String explaination = "\n<yes/(no)>"+
                                               "\nyes - Indication that components in the list should be excluded\ninstead of included";

    public ComponentExclude(){
        super(keyword,explaination,false);
        setDefaultValue("NO");
    }
}
