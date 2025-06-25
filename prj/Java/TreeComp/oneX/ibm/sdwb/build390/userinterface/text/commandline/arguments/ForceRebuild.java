package com.ibm.sdwb.build390.userinterface.text.commandline.arguments;

import java.util.*;

public class ForceRebuild extends EnumeratedCommandLineArgument {

    private static final String keyword = "FORCE";
    private static final String explaination = "\n<yes/all/(no)>" +
                                               "\n(This setting is only used when AUTOBUILD=YES)"+
                                               "\nyes - Unconditionally build all dependant parts"+
                                               "\nall - Unconditionally build all driver & partlist parts"+
                                               "\nno  - Conditionally build dependent parts\n";
    private static final Set possibleValues = new HashSet();

    static{
        possibleValues.add("YES");
        //possibleValues.add("ON");
        possibleValues.add("ALL");
        //possibleValues.add("OFF");
        possibleValues.add("NO");
    }


    public ForceRebuild(){
        super(keyword, explaination, possibleValues);
        setDefaultValue("NO");
        changeValueToUpperCase(); //TST3329A
    }
}
