package com.ibm.sdwb.build390.userinterface.text.commandline.arguments;

import java.util.*;

public class DirectoryExclude extends BinaryCommandLineArgument {

    private static final String keyword = "EXCLUDEDIRECTORY";
    private static final String explaination = "\n<yes/(no)>" +
                                               "\nyes - Indication that directories in the list should be excluded\ninstead of included\n";

    public DirectoryExclude(){
        super(keyword,explaination,false);
        setDefaultValue("NO");
    }
}
