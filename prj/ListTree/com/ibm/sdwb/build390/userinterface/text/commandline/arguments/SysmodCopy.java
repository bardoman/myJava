package com.ibm.sdwb.build390.userinterface.text.commandline.arguments;

import java.util.*;

public class SysmodCopy extends BinaryCommandLineArgument {

    private static final String keyword = "SYSMODCOPY";
    private static final String explaination = "\n<yes/(no)>"+
                                               "\nyes - Indications that sysmods are to be copied.";

    public SysmodCopy() {
        super(keyword, explaination, false);
        addAlternativeName("SYSMODS");
    }
}
