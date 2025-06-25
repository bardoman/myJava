package com.ibm.sdwb.build390.userinterface.text.commandline.arguments;

import java.util.*;

public class ModelRoot extends IndexedArgument {

    private static final String keyword = "MODELROOT";
    private static final String explaination ="Specifies the root path of the model in the source library.\n"+
                                              "Use this when specifying a model part\nthat exists in the development library.\n"+
                                              "To input a blank value use \" \"\n"+
                                              "Modeling is only allowed in FASTTRACK mode\n";


    public ModelRoot() {
        super(keyword,explaination);
        setAllowIndexOnly(true);
        setStopOnBrokenIndex(false);
        setAllowOrderedIndexEntry(false);
    }

    public  String getValue() {
        String str = super.getValue();

        if(str != null) {
            str = str.replace('\"', ' ').trim();
        }
        return str;
    }
}
