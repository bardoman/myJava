package com.ibm.sdwb.build390.userinterface.text.commandline.arguments;

import java.util.*;

public class MainframeSynchronizeDeltaDriverWithBase extends BinaryCommandLineArgument {

    private static final String keyword = "SYNCDRIVER";
    private static final String explaination = "\n<yes/(no)>" +
                                               "\nyes - Synchronize a full delta driver with it's base driver";

    public MainframeSynchronizeDeltaDriverWithBase(){
        super(keyword, explaination, false);
    }
}
