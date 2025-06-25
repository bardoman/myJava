package com.ibm.sdwb.build390.userinterface.text.commandline.arguments;

import java.util.*;

public class DeleteAdditionalSettings extends BinaryCommandLineArgument {

    private static final String keyword = "DELETEADDSETS";
    private static final String explaination = "\n<yes/(no)>" +
                                               "\n Delete additional settings for Restart";

    public DeleteAdditionalSettings() {
        super(keyword,explaination,false);
        setDefaultValue("NO");
    }
}
