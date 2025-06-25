package com.ibm.sdwb.build390.userinterface.text.commandline.arguments;

import java.util.*;

public class MainframeOutputScannersExecute extends BinaryCommandLineArgument {

    private static final String keyword = "RUNSCAN";
    private static final String explaination = "\n<yes/(no)>" +
                                               "\nyes - Indication that scanners (i.e. Oco scanner) should be run";

    public MainframeOutputScannersExecute(){
        super(keyword,explaination,false);
        setDefaultValue("NO");
    }
}
