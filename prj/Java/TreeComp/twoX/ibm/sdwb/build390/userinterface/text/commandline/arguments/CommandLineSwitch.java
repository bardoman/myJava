
package com.ibm.sdwb.build390.userinterface.text.commandline.arguments;

import java.util.*;

/*base class for switch */
public abstract class CommandLineSwitch extends CommandLineArgument {

    protected CommandLineSwitch(String switchName, String explaination){
        super(switchName,explaination);
    }   

    public final void setSwitchesFromSet(Set switchesSource){
        if (switchesSource.contains(getCommandLineName())) {
            super.setValue(getCommandLineName());
        }
    }

}
