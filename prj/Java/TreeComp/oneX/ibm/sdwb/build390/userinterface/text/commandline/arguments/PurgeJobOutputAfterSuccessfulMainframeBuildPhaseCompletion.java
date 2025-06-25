package com.ibm.sdwb.build390.userinterface.text.commandline.arguments;

import java.util.*;

public class PurgeJobOutputAfterSuccessfulMainframeBuildPhaseCompletion extends BinaryCommandLineArgument {

    private static final String keyword = "PURGEJOBS";
    private static final String explaination = "\n<(yes)/no>" +
                                               "\nyes - Indication that mainframe job output should be purged after\neach successful mainframe build phase\n";

    public PurgeJobOutputAfterSuccessfulMainframeBuildPhaseCompletion() {
        super(keyword,explaination,false);
        setDefaultValue("YES");
    }
}
