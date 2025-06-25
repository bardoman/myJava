package com.ibm.sdwb.build390.userinterface.text.commandline.arguments;

import java.util.*;

public class LocalFilePath extends IndexedArgument {

    private static final String keyword = "LOCALPART";
    private static final String explaination ="\nspecifies the relative path to a part to be included in the build.\n"+
                                              "If the relative path includes directory information,\n"+
                                              "it must start with a subdirectory of the ROOTDIRECTORY";

    public LocalFilePath() {
        super(keyword,explaination);
        setAllowIndexOnly(true);
        setStopOnBrokenIndex(true);
        setAllowOrderedIndexEntry(true);
    }
}
