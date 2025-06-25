package com.ibm.sdwb.build390.userinterface.text.commandline.arguments;

import java.util.*;

public class DataRetrievalDependencyType extends EnumeratedCommandLineArgument {

    private static final String keyword = "DEPENDTYPE";
    private static final String explaination ="\n<USER/USES/RBLD/(BOTH)>"+
                                              "\nIdentifies the dependency type for an instance of LOGTYPE=DEPENDENCY\n"+
                                              "The numerical index tag of this parameter must match that of the\nLOGTYPE parameter it modifies.";

    private static Set allowedSettings = new HashSet();
    static {
        allowedSettings.add("USER");
        allowedSettings.add("USES");
        allowedSettings.add("BOTH");
        allowedSettings.add("RBLD");
    }

    public DataRetrievalDependencyType() {
        super(keyword,explaination, allowedSettings);

        setDefaultValue("BOTH");
    }
}

