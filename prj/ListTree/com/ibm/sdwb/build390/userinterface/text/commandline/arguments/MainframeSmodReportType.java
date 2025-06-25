package com.ibm.sdwb.build390.userinterface.text.commandline.arguments;

import java.util.*;

public class MainframeSmodReportType extends EnumeratedCommandLineArgument {

    private static final String keyword = "TYPE";
    private static final String explaination ="\n<USERMOD>"+
                                              "\nType of SMOD report to generate.\n"+
                                              "\nUSERMOD - for an USERMOD report";
    private static final Set acceptableSetting = new HashSet();
    static{
        //   acceptableSetting.add("APAR");
        acceptableSetting.add("USERMOD");
    }

    public MainframeSmodReportType() {
        super(keyword,explaination, acceptableSetting);
        changeValueToUpperCase();
    }
}
