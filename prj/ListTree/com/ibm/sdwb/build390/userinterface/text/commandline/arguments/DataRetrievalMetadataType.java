package com.ibm.sdwb.build390.userinterface.text.commandline.arguments;

import java.util.*;

public class DataRetrievalMetadataType extends EnumeratedCommandLineArgument {

    private static final String keyword = "METADATA";
    private static final String explaination ="\n<SHORT/ALL/(LONG)>"+
                                              "\nIdentifes the Metadata Type for an instance of\n"+
                                              "LOGTYPE=METADATA Values.\n"+
                                              "The numerical index tag of this parameter must match that of the\nLOGTYPE parameter it modifies.";

    private static final Set allowedSettings = new HashSet();
    static{
        allowedSettings.add("LONG");
        allowedSettings.add("SHORT");
        allowedSettings.add("ALL");
    }


    public DataRetrievalMetadataType() {
        super(keyword,explaination, allowedSettings);
        setDefaultValue("LONG");
        changeValueToUpperCase(); //TST3312
    }
}
