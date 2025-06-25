package com.ibm.sdwb.build390.userinterface.text.commandline.arguments;

import java.util.*;

public class EmbeddedMetadata extends BinaryCommandLineArgument {

    private static final String keyword = "EMBEDDEDMETADATA";

    private static final String explaination = "\n<yes/no>" +
                                               "\nyes - To use metadata that is embedded in the files.\n";

    public EmbeddedMetadata(){
        super(keyword,explaination, false);
    }
}
