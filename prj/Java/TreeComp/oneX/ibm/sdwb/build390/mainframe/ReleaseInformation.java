package com.ibm.sdwb.build390.mainframe;

import java.util.*;

public class ReleaseInformation implements java.io.Serializable {
    static final long serialVersionUID = -1305131284583074067L;

    private String libraryName = null;
    private String mvsName = null;
    private String mvsHighLevelQualifier = null;
    private Set drivers = null;

    public ReleaseInformation(String tempLibName, String tempMvs, String tempHLQ) {
        libraryName=tempLibName;
        mvsName=tempMvs;
        mvsHighLevelQualifier=tempHLQ;
        drivers = new HashSet();
    }

    public String getLibraryName() {
        return libraryName;
    }

    public String getMvsName() {
        return mvsName;
    }

    public String getMvsHighLevelQualifier() {
        return mvsHighLevelQualifier;
    }

    public Set getDrivers() {
        if (drivers==null) {
            return new HashSet();
        }
        return new HashSet(drivers);// do NOT return the actual drivers object.  We have to handle how things are added to it.
    }

    public DriverInformation getDriverByName(String driverName) {
        for (Iterator driverIterator = drivers.iterator(); driverIterator.hasNext();) {
            DriverInformation oneDriver = (DriverInformation) driverIterator.next();
            if (oneDriver.getName().equals(driverName)) {
                return oneDriver;
            }
        }
        return null;
    }

    public void addDriver(DriverInformation newDriver) {
        newDriver.setReleaseInfomation(this);
        String name = newDriver.getName();
        boolean driverExists = false;
        for (Iterator iter=drivers.iterator(); iter.hasNext();) {//see if this driver exists in list
            DriverInformation tempDriver = (DriverInformation) iter.next();
            if (tempDriver.getName().equals(name)) {
                if (!isDriverInfosEquivalent(tempDriver, newDriver)) {
                    removeDriver(tempDriver);
                    drivers.add(newDriver);
                }
                return;
            }
        }
        drivers.add(newDriver);
    }

    private boolean isDriverInfosEquivalent(DriverInformation info1, DriverInformation info2) {
        // this is because the driver could have been deleted and recreated with a delta size change 
        if (!(info1.isFullDriver()==info2.isFullDriver())) {
            return false;
        }
        // this is because the driver could have been updated with a new base chain
        if (info1.getExplicitBaseChain()!=null) {
            if (info2.getExplicitBaseChain()==null) {
                return false;
            } else if (!info1.getExplicitBaseChain().equals(info2.getExplicitBaseChain())) {
                return false;
            }
        } else if (info2.getExplicitBaseChain()!=null) {
            return false;
        }
        // driver could have been deleted and recreated with a new base, or merged changing from a delta to a base driver
        if (info1.getBaseDriver()!=null) {
            if (info2.getBaseDriver()==null) {
                return false;
            } else if (!info1.getBaseDriver().getName().equals(info2.getBaseDriver().getName())) {

                return false;
            }
        } else if (info2.getBaseDriver()!=null) {
            return false;
        }
        // we won't test buildtype status, since buildtypes aren't shown in this report, so mismatches here are not important
        return true;
    }
    //end TST2489

    public void removeDriver(DriverInformation deadDriver) {
        drivers.remove(deadDriver);
    }

    public boolean equals(Object anotherRelease) {
        if (anotherRelease==null) {
            return false;
        } else if (!(anotherRelease instanceof ReleaseInformation)) {
            return false;
        } else {
            ReleaseInformation castRelease = (ReleaseInformation) anotherRelease;

            if ((libraryName!=null && castRelease.getLibraryName()!=null) 
                && (mvsHighLevelQualifier!=null && castRelease.getMvsHighLevelQualifier()!=null) &&
                (mvsName!=null && castRelease.getMvsName()!=null)) {
                boolean isExists = (libraryName.equals(castRelease.getLibraryName()) 
                                    & mvsHighLevelQualifier.equals(castRelease.getMvsHighLevelQualifier())
                                    & mvsName.equals(castRelease.getMvsName()));
                Set castReleaseDrivers = castRelease.getDrivers();
                if (drivers!=null && !castReleaseDrivers.isEmpty()) {
                    isExists = isExists & drivers.equals(castReleaseDrivers);
                }
                return isExists;
            } else {
                return false;
            }
        }
    }

    public String toString() {
        StringBuffer stringOutput = new StringBuffer();
        stringOutput.append("\nMVS Release Information:\n");
        stringOutput.append("   Library Name:"+libraryName+"\n");
        stringOutput.append("   MVS Hlq:"+mvsHighLevelQualifier+"\n");
        stringOutput.append("   MVS Release:"+mvsName+"\n");
        stringOutput.append("   Drivers:\n");
        for (Iterator driverIterator = drivers.iterator(); driverIterator.hasNext();) {
            DriverInformation one = (DriverInformation) driverIterator.next();
            stringOutput.append("      "+one.toString() + "  " + one.getRelease().hashCode()+"\n");
        }
        stringOutput.append("\n");
        return stringOutput.toString();
    }
}
