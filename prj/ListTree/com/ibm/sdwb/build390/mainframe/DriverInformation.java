package com.ibm.sdwb.build390.mainframe;

import java.util.ArrayList;
import java.util.Formatter;
import java.util.Iterator;
import java.util.List;

public class DriverInformation implements java.io.Serializable {
    static final long serialVersionUID = 3298666545846822555L;

    private ReleaseInformation release = null;
    private String name = null;
    private DriverInformation baseDriver = null;
    private boolean fullDriver = false;
    private List explictDriverBaseChain = null;
    private List buildTypes = null;

    public DriverInformation(String tempName) {
        name = tempName;
    }

    public void setFull(boolean newFull) {
        fullDriver = newFull;
    }

    public void setReleaseInfomation(ReleaseInformation tempRel) {
        release = tempRel;
    }

    public void setBaseDriver(DriverInformation tempBase) {
        baseDriver = tempBase;
        explictDriverBaseChain = null;
    }

    public void setExplictBaseChain(List baseChain) {
        explictDriverBaseChain = baseChain;
        baseDriver = null;
    }

    public void setBuildTypes(List tempTypes) {
        buildTypes = tempTypes;
    }

    public ReleaseInformation getRelease() {
        return release;
    }

    public String getName() {
        return name;
    }

    public DriverInformation getBaseDriver() {
        return baseDriver;
    }

    public List getExplicitBaseChain() {
        return explictDriverBaseChain;
    }

    public boolean isFullDriver() {
        return fullDriver;
    }

    public List getBuildTypes() {
        return buildTypes;
    }

    public boolean equals(Object testDriver) {
        if (testDriver==null) {
            return false;
        } else if (!(testDriver instanceof DriverInformation)) {
            return false;
        }
        //Begin PTM4391

        /*
        else
        {
            DriverInformation castDriver = (DriverInformation) testDriver;
            return name.equals(castDriver.getName()) & release.equals(castDriver.getRelease());
        } */
        else {
            DriverInformation castDriver = (DriverInformation) testDriver;
            if (release==null) {
                if (castDriver.getRelease()!=null) {
                    return false;
                }
            } else {
                if (castDriver.getRelease()==null) {
                    return false;
                } else {
                    if (!release.equals(castDriver.getRelease())) {
                        return false;
                    }
                }
            }
            if (name==null) {
                if (castDriver.getName()!=null) {
                    return false;
                }
            } else {
                if (castDriver.getName()==null) {
                    return false;
                } else {
                    if (!name.equals(castDriver.getName())) {
                        return false;
                    }
                }
            }
        }
        return true;
        //End PTM4391

    }

    public String toString() {
        StringBuilder strb = new StringBuilder();
        Formatter  formatter = new Formatter(strb);
        formatter.format("%n%s%n","Driver Information");
        formatter.format("%10s%s:%s%n","","Name",name);
        String tempRel = null;
        if (release!=null) {
            tempRel =  release.getLibraryName();
        }
        formatter.format("%10s%s:%s%n","","Libray Release",tempRel);
        formatter.format("%10s%s:%s%n","","Full",fullDriver);

        if (baseDriver!=null) {
            if (baseDriver.getRelease()!=null) {
                formatter.format("%10s%s:%s%n","","Base",baseDriver.getRelease().getLibraryName()+"."+baseDriver.getName());
            }
        }

        if (explictDriverBaseChain!=null) {
            formatter.format("%10s%s:%n","","Explicit base chain");
            for (Iterator baseIterator = explictDriverBaseChain.iterator();baseIterator.hasNext();) {
                DriverInformation currentBase = (DriverInformation) baseIterator.next();
                if (currentBase.getRelease()!=null) {
                    formatter.format("%13s%s%n","",currentBase.getRelease().getLibraryName()+"."+currentBase.getName());
                }
            }
        }

        String buildTypesString = "";
        if (buildTypes!=null) {
            buildTypesString = buildTypes.toString();
        }
        formatter.format("%10s%s:%s%n","","Buildtypes",buildTypesString);
        return strb.toString();
    }
}
