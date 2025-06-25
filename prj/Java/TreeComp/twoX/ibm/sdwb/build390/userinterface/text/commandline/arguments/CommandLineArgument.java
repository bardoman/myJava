package com.ibm.sdwb.build390.userinterface.text.commandline.arguments;

import java.util.*;

import com.ibm.sdwb.build390.utilities.BooleanInterface;


/**
 * This is the base class for all commandline parameters.
 * To make a parameter object, you pretty much just call the 
 * constructor with the right arguments.  That's about all you need.
 */
public abstract class CommandLineArgument implements com.ibm.sdwb.build390.utilities.BooleanInterface, java.lang.Cloneable {
    private final String commandLineName; // this is the name you enter for the parameter on the commandline, IE librelease
    private final String commandExplaination; // this is the explaination of the point of this command.
    private String value = null;
    private Set alternativeCommandNames = null;  // this is in case, for compatibility with old code, we need to have several names for the same parameter
    private boolean upperCaseValue = false;

    protected CommandLineArgument(String tempCommandLineName, String tempExplaination) {
        commandLineName = tempCommandLineName;
        commandExplaination = tempExplaination;
        alternativeCommandNames = new HashSet();
    }

    public final String getCommandLineName() {
        return commandLineName;
    }

    public String getNameOfBoolean() {
        return  commandLineName;
    }

    public final String getDescriptionOfBoolean() {
        return commandExplaination;
    }

    protected final void addAlternativeName(String newAlternative) {
        alternativeCommandNames.add(newAlternative);
    }

    public  String getValue() {
        if (value!=null) {
            if (upperCaseValue) {
                return value.toUpperCase();
            }
        }

        return value;

    }

    protected final void changeValueToUpperCase() {
        upperCaseValue=true;
    }

    protected boolean isChangeValueToUpperCase(){
        return upperCaseValue;
    }

    public final CommandLineArgument copy() {
        try {
            return(CommandLineArgument) clone();
        } catch (CloneNotSupportedException cnse) {
            System.out.println("Clone not supported for class " + getClass().getName());
            cnse.printStackTrace();
        }
        return null;
    }


    public final void setValue(String tempValue) {
        if (validateValue(tempValue)) {
            value= tempValue;
        } else {
            if (tempValue!=null) value = tempValue;   /* TST1642 */

        }
    }


    public final void setValueFromMap(Map valueSource) {
        String newValue = (String) valueSource.get(commandLineName);
        for (Iterator alternateIterator = alternativeCommandNames.iterator(); alternateIterator.hasNext() & newValue==null;) {
            newValue = (String) valueSource.get(alternateIterator.next());
        }
        setValue(newValue);
    }

    public  boolean isSatisfied() {
        return value != null;
    }

    public String getReasonNotSatisfied() {
        return "=>"+ String.format("%-20s%-22s",commandLineName,"was   not  specified.");
    }


    public boolean inputAvailable() {
        return(getValue()!=null);
    }
    public String toString() {
        return "("+commandLineName+", "+value+", " + commandExplaination+").";
    }

    protected boolean validateValue(String testArgument) {
        return true;
    }
}
