package com.ibm.sdwb.build390.userinterface.text.commandline.arguments;

import java.util.*;

public class DataRetrievalBinaryColumn extends BinaryCommandLineArgument {

    private static final String keyword = "BINCOL";
    private static final String explaination = "\n<yes/(no)>"+
                                               "\nIdentifes the binary type for an instance of LOGTYPE=<CollectorName>.\n"+
                                               "The numerical index tag of this parameter must match that of the\nLOGTYPE parameter it modifies.";

    public DataRetrievalBinaryColumn() {
        super(keyword,explaination,false);

        setDefaultValue("NO");
    }
}

