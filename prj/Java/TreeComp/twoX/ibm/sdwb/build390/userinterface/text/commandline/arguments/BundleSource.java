
package com.ibm.sdwb.build390.userinterface.text.commandline.arguments;

import java.util.*;

public class BundleSource extends BinaryCommandLineArgument {

    private static final String keyword = "BUNDLED";
    private static final String explaination = "\n<yes/(no)>" +
                                               "\nyes - Process all library source tracks in a level into one usermod.";

    public BundleSource() {
        super(keyword,explaination,false);
        setDefaultValue("NO");
        changeValueToUpperCase();
    }
}
