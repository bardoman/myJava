package com.ibm.sdwb.build390.userinterface.text.commandline.arguments;

import java.util.*;
import com.ibm.sdwb.build390.utilities.BinarySettingUtilities;


/**
 * This is the base class for all commandline parameters that can only take on specific settings.
 */
public abstract class BinaryCommandLineArgument extends EnumeratedCommandLineArgument {

    protected BinaryCommandLineArgument(String tempCommandLineName, String tempExplaination, boolean tempDefault) {
        super(tempCommandLineName,tempExplaination, BinarySettingUtilities.getAllValidSettings());
        if(tempDefault) {
            setDefaultValue(BinarySettingUtilities.getPreferredTrueSetting());
        }
        else {
            setDefaultValue(BinarySettingUtilities.getPreferredFalseSetting());//TST3233
        }
    }

    public boolean validateValue(String tempValue) {
        if(tempValue!=null) {
            tempValue=tempValue.toLowerCase();
        }
        return super.validateValue(tempValue);
    }

    public boolean getBooleanValue() {
        return BinarySettingUtilities.isTrueSetting(getValue());
    }
}
