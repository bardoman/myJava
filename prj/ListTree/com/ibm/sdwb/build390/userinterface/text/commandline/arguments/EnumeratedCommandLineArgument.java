package com.ibm.sdwb.build390.userinterface.text.commandline.arguments;

import java.util.*;


/**
 * This is the base class for all commandline parameters that can only take on specific settings.
 */
public abstract class EnumeratedCommandLineArgument extends CommandLineArgument {

    private Set possibleValues = null;
    private String defaultValue = null;
    private boolean validValuePassed = true;
    private boolean usingDefaults = false;

    protected EnumeratedCommandLineArgument(String tempCommandLineName, String tempExplaination, Set tempPossibleValues) {
        super(tempCommandLineName,tempExplaination);
        possibleValues = tempPossibleValues;
    }



    protected void setDefaultValue(String tempDefault) {
        defaultValue = tempDefault;
    }

    public String getDefaultValue() {
        return defaultValue;
    }

    public boolean validateValue(String tempValue) {
        validValuePassed = possibleValues.contains(tempValue);
        return validValuePassed;
    }


    public boolean inputAvailable() {
        if (getValue()==null || usingDefaults) {
            setValue(defaultValue);
            usingDefaults = true;
            return false; 
        }
        return true;
    }



    protected void addPossibleValue(String tempValue) {
        possibleValues.add(tempValue);
    }

    protected void addPossibleValues(Collection valueCollection) {
        possibleValues.addAll(valueCollection);
    }
    public boolean isSatisfied() {
        if (usingDefaults) {
            return false;
        }
        return validateValue(getValue());
    }
    public String getReasonNotSatisfied() {
        if (!validValuePassed && (getValue()!=null)) {
            return "=>"+ String.format("%-20s%-22s%n%2s%-42s",getCommandLineName(),"was an invalid value.", 
                                       "","[valid values are "+possibleValues.toString().toUpperCase()+"].");
        } else {
            return super.getReasonNotSatisfied();
        }
    }

    public String toString() {
        String parentToString = super.toString();
        return parentToString.substring(0, parentToString.length()-1)+", "+possibleValues.toString()+ ", "+ defaultValue+")";
    }
}
