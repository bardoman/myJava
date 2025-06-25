package com.ibm.sdwb.build390.userinterface.text.commandline.arguments;

import java.util.*;

public class MainframeDriverDeltaOrBase extends EnumeratedCommandLineArgument {

    private static final String keyword = "DRIVERTYPE";
    private static final String explaination = "\n<full/delta>" +
                                               "\nSpecify whether the driver will be a base, or be part of a chain"+
                                               "\nFULL  - a base driver (It will be populated from another driver)"+
                                               "\nDELTA - a driver that is based on another driver";
    private static final Set possibleValues = new HashSet();

    static{
        possibleValues.add("FULL");
        possibleValues.add("DELTA");
    }


    public MainframeDriverDeltaOrBase() {
        super(keyword, explaination, possibleValues);
        addAlternativeName("CDRVRTYP");
        changeValueToUpperCase();
    }
}
