package com.ibm.sdwb.build390.mainframe;

import java.util.*;

public class PhaseInformation implements java.io.Serializable {
    static final long serialVersionUID = 1111111111111111111L;

    private String name = null;
    private String className = "";
    private int phaseNumber = -1;
    private int phaseToHaltOnIfErrorsFound = -1;  
    private String phaseOverrideString = null;

    public PhaseInformation(String tempName, String tempClassName, int tempNumber) {
        name = tempName;
        className = tempClassName;
        phaseNumber = tempNumber;
        phaseToHaltOnIfErrorsFound = tempNumber;  //default to halting after the current phase phase.
    }

    public void setPhaseToHaltOnIfErrorsFound(int tempPhase) {
        phaseToHaltOnIfErrorsFound = tempPhase;
    }

    public void setPhaseOverrideString(String tempOverride) {
        phaseOverrideString = tempOverride;
    }

    public String getName() {
        return name;
    }

    public String getClassName() {
        return className;
    }

    public String getPhaseOverrides() {
        return phaseOverrideString;
    }

    public int getPhaseNumber() {
        return phaseNumber;
    }

    public int getPhaseNumberToHaltOnIfErrorsFound() {
        return phaseToHaltOnIfErrorsFound;
    }

    public boolean equals(Object test) {
        if (test==null) {
            return false;
        } else if (!(test instanceof PhaseInformation)) {
            return false;
        } else {
            PhaseInformation castTest = (PhaseInformation) test;
            return name.equals(castTest.getName()) & phaseNumber == castTest.phaseNumber & phaseToHaltOnIfErrorsFound == castTest.phaseToHaltOnIfErrorsFound ;
        }
    }

    public String toString() {
        StringBuffer outputString=new StringBuffer();
        outputString.append("Phase Information\n");
        outputString.append("	Name:"+name+"\n");
        outputString.append("	Phase Number:"+phaseNumber+"\n");
        outputString.append("  	Phase to Halt On:"+phaseToHaltOnIfErrorsFound+"\n");
        outputString.append("  	Override String:"+phaseOverrideString+"\n");
        return outputString.toString();
    }
}
