
package com.ibm.sdwb.build390.userinterface.text.commandline.arguments;

import java.util.*;

public class COMPIDHeader extends CommandLineArgument {

    private static final String keyword = "HLCOMP";
    private static final String explaination ="The first four numbers of the product's Component ID";


    public COMPIDHeader(){
        super(keyword,explaination);
        changeValueToUpperCase();
    }
}
