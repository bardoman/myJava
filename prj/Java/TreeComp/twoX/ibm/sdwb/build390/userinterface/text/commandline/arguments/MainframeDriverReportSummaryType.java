package com.ibm.sdwb.build390.userinterface.text.commandline.arguments;

import java.util.*;

public class MainframeDriverReportSummaryType extends CommandLineArgument {

    private static final String keyword = "SUMMARY";

    private static final String explaination ="\n<BUILDTYPES/FAILURES/UNBUILT/SHIPPED/BUILDS/DELTA/UNPACKAGED/INACTIVE/(STATUS)>"+
                                              "\nDetermine what information will be included in the report.\n"+
                                              "BUILDTYPES   - a report of build types.\n"+
                                              "FAILURES     - a report of build failures.\n"+
                                              "UNBUILT      - a report of unbuilt parts.\n"+
                                              "SHIPPED      - a report of shipped parts.\n"+
                                              "BUILDS       - a report of builds.\n"+
                                              "DELTA        - a report of delta parts.\n"+
                                              "UNPACKAGED   - a report of unpackaged parts.\n"+
                                              "INACTIVE     - a report of inactive parts.\n"+
                                              "STATUS       - a driver status report.\n" +
                                              "default      - STATUS\n";


    public MainframeDriverReportSummaryType() {
        super(keyword,explaination);
        changeValueToUpperCase();
    }

    public String getValue() {
        String value = super.getValue();
        if (value!=null) {
            if (value.equals("BUILDTYPES")) {
                value = "ONLY";
            }

            if (value.equals("FAILURES")) {
                value = "FAIL";
            }

            if (value.equals("DELTA")) {
                value="LOCAL";
            }

            if (value.equals("STATUS")) {
                value = null;
            }
        }
        return value;
    }
}
