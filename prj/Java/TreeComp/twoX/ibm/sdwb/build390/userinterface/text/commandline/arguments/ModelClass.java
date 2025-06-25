package com.ibm.sdwb.build390.userinterface.text.commandline.arguments;

import java.util.*;

public class ModelClass extends IndexedArgument {

    private static final String keyword = "MODELCLASS";
    private static final String explaination ="Specifies the class of the model.\nUse this when specifying a model\n"+
                                              "part that exists in the host release.\n"+
                                              "Modeling is only allowed in FASTTRACK mode\n";


    public ModelClass() {
        super(keyword,explaination);
        setAllowIndexOnly(true);
        setStopOnBrokenIndex(false);
        setAllowOrderedIndexEntry(false);
    }
}
