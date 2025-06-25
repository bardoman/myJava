package com.ibm.sdwb.build390.userinterface.text.commandline.arguments;

import java.util.*;

public class Library extends EnumeratedCommandLineArgument {

    private static final String keyword = "LIBRARY";
    private static final String explaination ="The name of the supported library type to use." +
                                              "\nCMVC  - Use CMVC library system." +
                                              "\nNOLIB - Use a fakelib or nolib system.\n(All interactions to library are ignored.)";

    private static final Set possibleSettings = new HashSet();

    public static String CMVC = "CMVC";
    public static String FAKELIB = "NOLIB";


    static{
        possibleSettings.add(CMVC);
        possibleSettings.add(FAKELIB);
    }

    public Library() {
        super(keyword, explaination, possibleSettings);
        setDefaultValue(CMVC);
        changeValueToUpperCase();
    }
}
