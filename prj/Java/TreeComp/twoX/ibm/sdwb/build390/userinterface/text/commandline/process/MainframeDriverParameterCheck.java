package com.ibm.sdwb.build390.userinterface.text.commandline.process;

import com.ibm.sdwb.build390.MBBuild;
import com.ibm.sdwb.build390.MBEdit;
import com.ibm.sdwb.build390.MBGlobals;
import com.ibm.sdwb.build390.logprocess.LogEventProcessor;
import com.ibm.sdwb.build390.mainframe.DriverInformation;
import com.ibm.sdwb.build390.mainframe.ReleaseInformation;
import com.ibm.sdwb.build390.userinterface.text.commandline.RequiredAndOptionalArguments;
import com.ibm.sdwb.build390.userinterface.text.commandline.arguments.MainframeDriver;
import com.ibm.sdwb.build390.userinterface.text.commandline.arguments.MainframeHighLevelQualifier;
import com.ibm.sdwb.build390.userinterface.text.commandline.arguments.MainframeRelease;
import com.ibm.sdwb.build390.utilities.BooleanAnd;

public class MainframeDriverParameterCheck extends CommandLineProcess {

    public static final String PROCESSNAME = "CHECKDRIVER";

    private MainframeRelease mainframeRelease = new MainframeRelease();
    private MainframeDriver driver = new MainframeDriver();
    private MainframeHighLevelQualifier highLevelQualifier = new MainframeHighLevelQualifier();

    public MainframeDriverParameterCheck(LogEventProcessor tempLep, com.ibm.sdwb.build390.MBStatus tempStatus) {
        super(PROCESSNAME, tempLep, tempStatus);
    }
    public String getHelpDescription() {
        return getProcessTypeHandled()+ " command queries a driver on the host system\n"+
        "for information about it's settings..";
    }

    public String getHelpExamples() {
        return getProcessTypeHandled()+" MVSHLQ=<hlq> DRIVER=<driver> MVSRELEASE=<mvsrelease>";
    }
    protected void setArgumentStructure(RequiredAndOptionalArguments argumentStructure) {
        BooleanAnd baseAnd = new BooleanAnd();
        baseAnd.addBooleanInterface(mainframeRelease);
        baseAnd.addBooleanInterface(driver);
        baseAnd.addBooleanInterface(highLevelQualifier);
        argumentStructure.setRequiredPart(baseAnd);
    }

    public void runProcess() throws com.ibm.sdwb.build390.MBBuildException{
        MBBuild tempBuild = new MBBuild(getLEP());

        ReleaseInformation releaseInfo = getReleaseInformation(mainframeRelease.getValue(),tempBuild.getSetup(), false);
        DriverInformation driverInfo   = getDriverInformation(driver.getValue(),releaseInfo,tempBuild.getSetup());
        tempBuild.setReleaseInformation(releaseInfo);
        tempBuild.setDriverInformation(driverInfo);
        com.ibm.sdwb.build390.process.DriverCreationParameterReport driverParmReport = new com.ibm.sdwb.build390.process.DriverCreationParameterReport(tempBuild,highLevelQualifier.getValue(), mainframeRelease.getValue(), driver.getValue(), this);
        driverParmReport.setAlternativeSaveLocation(new java.io.File(MBGlobals.Build390_path+"misc"+java.io.File.separator));

        setCancelableProcess(driverParmReport);

        driverParmReport.externalRun();
        MBEdit edit = new MBEdit(driverParmReport.getOutputResultFile().getAbsolutePath(), getLEP());

    }
}
