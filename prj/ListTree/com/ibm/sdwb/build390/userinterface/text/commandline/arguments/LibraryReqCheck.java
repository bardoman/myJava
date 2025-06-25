package com.ibm.sdwb.build390.userinterface.text.commandline.arguments;

import java.util.*;

public class LibraryReqCheck extends BinaryCommandLineArgument {

    private static final String keyword = "LIBRARYREQCHECK";
    private static final String explaination = "\n<yes/(no)>" +
                                               "\nyes -  Perform an usermod dryrun where the track/level pre/co reqs are verified.";


    public LibraryReqCheck() {
        super(keyword,explaination,false);
        setDefaultValue("NO");
        changeValueToUpperCase();
    }
}
