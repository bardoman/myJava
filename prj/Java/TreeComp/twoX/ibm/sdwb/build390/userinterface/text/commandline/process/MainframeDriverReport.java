package com.ibm.sdwb.build390.userinterface.text.commandline.process;

import java.util.*;
import java.io.*;
import com.ibm.sdwb.build390.*;
import com.ibm.sdwb.build390.userinterface.text.commandline.*;
import com.ibm.sdwb.build390.userinterface.text.commandline.arguments.*;
import com.ibm.sdwb.build390.mainframe.*;
import com.ibm.sdwb.build390.logprocess.*;
import com.ibm.sdwb.build390.utilities.*;

//*************************************************************
//09/03/2003 #DEF.TST1471:   DRIVER argument for DRVRRPT malfuctions
//*************************************************************

public class MainframeDriverReport extends CommandLineProcess {

    public static final String PROCESSNAME = "DRVRRPT";

    private LibraryRelease libRelease = new LibraryRelease();
    private MainframeDriver driver = new MainframeDriver();
    private IncludePathname includePathname = new IncludePathname();
    private MainframeDriverReportSummaryType summary = new MainframeDriverReportSummaryType();

    public MainframeDriverReport(LogEventProcessor tempLep, com.ibm.sdwb.build390.MBStatus tempStatus) {
        super(PROCESSNAME, tempLep, tempStatus);
    }

    public String getHelpDescription() {
        return getProcessTypeHandled()+ " command queries the Build/390 for a report on  driver.";
    }

    public String getHelpExamples() {
        return getProcessTypeHandled()+" LIBRELEASE=<librelease> DRIVER=<driver>";
    }

    protected void setArgumentStructure(RequiredAndOptionalArguments argumentStructure) {
        //Start #DEF.TST1471:
        BooleanAnd basicAnd = new BooleanAnd();
        argumentStructure.setRequiredPart(basicAnd);
        basicAnd.addBooleanInterface(libRelease);
        basicAnd.addBooleanInterface(driver);
        //End #DEF.TST1471:

        argumentStructure.addOption(includePathname);
        argumentStructure.addOption(summary);
    }

    public void runProcess() throws com.ibm.sdwb.build390.MBBuildException{
        MBBuild tempBuild = new MBBuild(getLEP());
        getStatusHandler().updateStatus("Checking library release value "+ libRelease.getValue() + "...",false);


        ReleaseInformation releaseInfo = getReleaseInformation(libRelease.getValue(),tempBuild.getSetup(), true);
        DriverInformation driverInfo   = getDriverInformation(driver.getValue(),releaseInfo,tempBuild.getSetup());

        tempBuild.setReleaseInformation(releaseInfo);
        tempBuild.setDriverInformation(driverInfo);


        com.ibm.sdwb.build390.process.DriverReport driverReport = new com.ibm.sdwb.build390.process.DriverReport(tempBuild.getDriverInformation(),tempBuild.getSetup().getMainframeInfo(), tempBuild.getSetup().getLibraryInfo(), new java.io.File(MBGlobals.Build390_path+"misc"+java.io.File.separator), this);
        if (includePathname.isSatisfied()) {
            driverReport.setIncludePathname(includePathname.getBooleanValue());
        }

        driverReport.setSummaryType(null);

        if (summary.isSatisfied() && summary.getValue()!=null) {
            driverReport.setSummaryType(summary.getValue());
        }
        driverReport.setShowFilesAfterRun(true, false);

        driverReport.setJustGetReport(true);

        setCancelableProcess(driverReport);

        driverReport.externalRun();
    }
}
