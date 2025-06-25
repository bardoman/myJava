
package com.ibm.sdwb.build390.userinterface.text.commandline.arguments;

import java.util.*;


public class LogicalReqType extends IndexedArgument {


    public static final String  IFREQ =  "IFREQ";
    public static final String  COREQ =  "COREQ";
    public static final String  PREREQ=  "PREREQ";

    private static final Set possibleSettings = new HashSet();

    static{
        possibleSettings.add(IFREQ);
    }

    private static final String keyword = "LOGICALREQTYPE";
    private static final String explaination = "REQTYPES are logical condition types";

    public LogicalReqType() {
        super(keyword, explaination);
        setAllowIndexOnly(true);
        setStopOnBrokenIndex(false);
        setAllowOrderedIndexEntry(true);
        changeValueToUpperCase();
        setArgument(new  LogicalReqEnumType());

    }

    private class LogicalReqEnumType extends EnumeratedCommandLineArgument {


        public LogicalReqEnumType() {
            super(keyword, explaination, possibleSettings);
            changeValueToUpperCase();
        }
    }


}
