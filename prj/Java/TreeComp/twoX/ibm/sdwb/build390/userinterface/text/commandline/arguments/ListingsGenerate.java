package com.ibm.sdwb.build390.userinterface.text.commandline.arguments;

import java.util.*;

public class ListingsGenerate extends BinaryCommandLineArgument {

    private static final String keyword = "LISTGEN";
    private static final String explaination = "\n<yes/(no)>" +
                                               "\nyes - Indication that listings should be generated";

    public ListingsGenerate(){
        super(keyword,explaination,false);
        setDefaultValue("NO");
        changeValueToUpperCase();
    }
}
