package com.ibm.sdwb.build390.process.steps;

import com.ibm.sdwb.build390.*;
import com.ibm.sdwb.build390.library.*;
import com.ibm.sdwb.build390.mainframe.DriverInformation;
import com.ibm.sdwb.build390.mainframe.parser.DriverReportParser;
import com.ibm.sdwb.build390.logprocess.LogEventProcessor;
import java.util.*;
import java.io.*;

//**************************************************
//04/17/2003 Def.INT1167: Bad path on driver report
//06/05/2003 Def.TST1217: driver reports are stored under build dir
//09/03/2003 #DEF.TST1471:  DRIVER argument for DRVRRPT malfuctions
//05/03/2006 INT2230   Time to clean up (driver report of parts)
//                     replaces MBLogDrvrReport. 
//**************************************************

public class DriverReport extends MainframeCommunication {

    static final long serialVersionUID = 1111111111111111L;

    private boolean forceNewReport = false;
    private boolean checkForHLQAndDriver = false;
    private boolean checkForControlledFlag = false;
    private boolean checkForLockFlag = false;
    private boolean checkForMergeOnlyFlag = false;

    private boolean justGetReport = false;
    private boolean showPrintFile = false;
    private boolean showOutFile = false;
    private boolean holdNextUsermodNumber = false;
    private String summaryType = "ONLY";
    private boolean moduleMacrosOnly = false;
    private boolean includePathname = false;
    private boolean includeLibraryParts = false;
    private boolean checkBaseNotThinDelta = false;

    private MBMainframeInfo mainInfo = null;
    private LibraryInfo libInfo = null;
    private DriverInformation driverInfo = null;
    private DriverReportParser dr = null;
    private File saveLocation = null;
    private StringBuffer outputFileName = new StringBuffer();
    private String outputHeader = null;

    public DriverReport(DriverInformation tempDriver, MBMainframeInfo tempMain, LibraryInfo tempLib, File tempSave, com.ibm.sdwb.build390.process.AbstractProcess tempProc) {
        super(null,"Get Driver Report", tempProc);
        saveLocation = tempSave;
        libInfo = tempLib;
        mainInfo = tempMain;
        setDriverInformation(tempDriver);
        setVisibleToUser(true);
        setUndoBeforeRerun(false);
    }

    public void setDriverInformation(DriverInformation tempDriverInfo) {
        if (driverInfo!=tempDriverInfo) {
            driverInfo = tempDriverInfo;
            dr = null;
            if (driverInfo!=null) {
                outputHeader = saveLocation.getAbsolutePath()+File.separator+"DriverReport-"+mainInfo.getMainframeAddress()+"-"+mainInfo.getMainframePort()+"-"+libInfo.getProcessServerName()+"-"+libInfo.getProcessServerAddress()+"-"+driverInfo.getRelease().getLibraryName()+"-"+driverInfo.getName();
            }
        }
    }

    public void setForceNewReport(boolean tempForceNewReport) {
        forceNewReport = tempForceNewReport; 
    }

    public void setCheckForHLQAndDriver(boolean tempCheckHlqAndDriver) {
        checkForHLQAndDriver = tempCheckHlqAndDriver;
    }

    public void setCheckForControlledFlag(boolean tempCheckControlled) {
        checkForControlledFlag = tempCheckControlled;
    }

    public void setCheckForLockFlag(boolean tempCheckLockFlag) {
        checkForLockFlag = tempCheckLockFlag;
    }

    public void setCheckForMergeOnlyFlag(boolean tempCheckMergeOnlyFlag) {
        checkForMergeOnlyFlag =tempCheckMergeOnlyFlag;
    }

    public void setCheckBaseNotThinDelta(boolean tempCheckBaseNotThinDelta) {
        checkBaseNotThinDelta= tempCheckBaseNotThinDelta;
    }

    public void setHoldNextUsermod(boolean tempHold) {
        holdNextUsermodNumber = tempHold;
    }

    public boolean isDriverLockedByBuild(String buildid) {
        return dr.getBuildid().equals(buildid);
    }

    public void setIncludePathname(boolean tempInclude) {
        includePathname = tempInclude;
        outputFileName.append("-PATHNAME");
    }

    public void setIncludeOnlyLibraryParts(boolean tempLibParts) {
        includeLibraryParts = tempLibParts;
    }

    public void setModMac(boolean tempModuleMacrosOnly) {
        moduleMacrosOnly = tempModuleMacrosOnly;
        outputFileName.append("-MODMAC");
    }

    public void setJustGetReport(boolean tempGet) {
        justGetReport = tempGet;
        forceNewReport = forceNewReport | justGetReport;
    }

    public void setSummaryType(String tempSummary) {
        summaryType = tempSummary;
    }

    /**
     * This is the method that should be implemented to actually
     * run the process.	Use executionArgument if you need to 
     * access the argument from the execute method.
     * 
     * @return Object indicating output of the step.
     */
    public void execute() throws com.ibm.sdwb.build390.MBBuildException{
        getLEP().LogSecondaryInfo(getFullName(),"Entry");

        setOutputHeaderLocation(outputHeader+ outputFileName.toString());
        if (forceNewReport | !getOutputFile().exists()) {
            String driverReportCommand = "DRVRRPT ";

            driverReportCommand +=libInfo.getDescriptiveStringForMVS()+", CMVCREL=\'"+driverInfo.getRelease().getLibraryName() +  "\', DRIVER=\'"+driverInfo.getName()+"\'";

            if (summaryType !=null) {
                driverReportCommand += ", SUMMARY="+summaryType.toUpperCase();
            }

            if (holdNextUsermodNumber) {
                driverReportCommand += ", HLDUSRMD=YES";
            }
            if (includePathname) {
                driverReportCommand += ", PATHNAME=YES";
            }

            if (moduleMacrosOnly) {
                driverReportCommand += ", MODMAC";
            }

            if (includeLibraryParts) {
                driverReportCommand +=", LIBPART=\'ONLY\'";
            }

            if (checkBaseNotThinDelta) {
                driverReportCommand +=", USMDVR=YES";
            }

            createMainframeCall(driverReportCommand, "Requesting a driver report for "+driverInfo.getName(), mainInfo);
            runMainframeCall();
        }

        if (!justGetReport) {
            dr = new DriverReportParser(getOutputFile());
            dr.parseReport();
            dr.setBuildtypesForDriver(driverInfo);

            if (checkForHLQAndDriver) {
                if (dr.getHlq() == null  |  dr.getRel() == null) {
                    throw new GeneralError("Unable to get MVS HLQ and MVS REL from driver report");
                }
            }

            if (checkForControlledFlag) {
                if (dr.getControl()) {
                    throw new GeneralError("The Driver "+ dr.getDriver()+" is CONTROLLED , Contact Build390 System Administrator");
                }
            }

            if (checkForLockFlag) {
                if (checkForMergeOnlyFlag) {
                    if (dr.getLock() && !dr.getMergeOnly()) { /* popup an error when LOCK=ON and MERGONLY=OFF */
                        throw new GeneralError("Driver is locked.\nLOCK flag is ON.\nMERGONLY is OFF."+ "\nRun Reports - Driver Reports - Builds for more information."); /*TST1808*/
                    }
                } else if (dr.getLock()) {
                    throw new GeneralError("Driver is locked.\nLOCK flag is ON."+ "\nRun Reports - Driver Reports - Builds for more information."); /*TST1808*/
                }

            }

        }
    }

    public DriverReportParser getParser() {
        return dr;
    }
}
