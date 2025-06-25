package com.ibm.sdwb.build390.userinterface.text.commandline.arguments;

import java.util.*;

public class Replace extends BinaryCommandLineArgument {

    private static final String keyword = "REPLACE";
    private static final String explaination ="\n<yes/(no)>"+
                                              "\nyes - Indicates that the current client.ser setup file will be replaced.\n";

    public Replace() {
        super(keyword,explaination, false);
    }
}
