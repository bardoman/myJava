package com.ibm.sdwb.build390.userinterface.text.commandline.arguments;

import java.util.*;

public class Priced extends BinaryCommandLineArgument {

    private static final String keyword = "PRICED";
    private static final String explaination ="A switch to indicate whether or not\nthe version (FMID) is priced.\n"+
                                              "Default is ON which means that the version is priced.";
    private final static Set possibleValues = new HashSet();


    public Priced(){
        super(keyword,explaination,true);
    }
}
