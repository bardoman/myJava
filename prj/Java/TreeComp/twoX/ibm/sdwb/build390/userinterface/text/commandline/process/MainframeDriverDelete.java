package com.ibm.sdwb.build390.userinterface.text.commandline.process;

import com.ibm.sdwb.build390.GeneralError;
import com.ibm.sdwb.build390.MBClient;
import com.ibm.sdwb.build390.MBGlobals;
import com.ibm.sdwb.build390.SyntaxError;
import com.ibm.sdwb.build390.logprocess.LogEventProcessor;
import com.ibm.sdwb.build390.mainframe.DriverInformation;
import com.ibm.sdwb.build390.mainframe.ReleaseInformation;
import com.ibm.sdwb.build390.process.ProcessWrapperForSingleStep;
import com.ibm.sdwb.build390.user.Setup;
import com.ibm.sdwb.build390.user.SetupManager;
import com.ibm.sdwb.build390.userinterface.text.commandline.RequiredAndOptionalArguments;
import com.ibm.sdwb.build390.userinterface.text.commandline.arguments.MainframeDriver;
import com.ibm.sdwb.build390.userinterface.text.commandline.arguments.MainframeHighLevelQualifier;
import com.ibm.sdwb.build390.userinterface.text.commandline.arguments.MainframeRelease;
import com.ibm.sdwb.build390.utilities.BooleanAnd;    
//import com.ibm.sdwb.build390.process.steps.*;
//import com.ibm.sdwb.build390.process.*;
import com.ibm.sdwb.build390.*;


public class MainframeDriverDelete extends CommandLineProcess {

    public static final String PROCESSNAME = "DELETEDRIVER";

    private MainframeHighLevelQualifier highLevelQualifier = new MainframeHighLevelQualifier();
    private MainframeRelease shadow = new MainframeRelease();
    private MainframeDriver driver = new MainframeDriver();

    public MainframeDriverDelete(LogEventProcessor tempLep, com.ibm.sdwb.build390.MBStatus tempStatus) {
        super(PROCESSNAME, tempLep, tempStatus);
    }

    public String getHelpDescription() {
        return "The " + getProcessTypeHandled() + " command deletes drivers from the host system.";
    }

    public String getHelpExamples() {
        return "1.To delete a driver\n"+
        getProcessTypeHandled()+" MVSRELEASE=<mvsrelease> DRIVER=<driver>\n"+
        "        MVSHLQ=<high level qualifier>\n"+
        "Note:To delete a release(mvs shadow), please use DELETERELEASE command.\n";
    }

    protected void setArgumentStructure(RequiredAndOptionalArguments argumentStructure) {
        BooleanAnd baseAnd = new BooleanAnd();
        baseAnd.addBooleanInterface(shadow);
        baseAnd.addBooleanInterface(driver);
        argumentStructure.setRequiredPart(baseAnd);
        //#DEF.TST1841:
        argumentStructure.addOption(highLevelQualifier);
    }

    public void runProcess() throws com.ibm.sdwb.build390.MBBuildException{
        Setup setup = SetupManager.getSetupManager().createSetupInstance();

        ReleaseInformation releaseInfo = getReleaseInformation(shadow.getValue(),setup, false);

        if(highLevelQualifier.isSatisfied()) {
            String libraryRelease = setup.getMainframeInfo().getReleaseByMVSName(shadow.getValue(), setup.getLibraryInfo()).getLibraryName();
            releaseInfo = new ReleaseInformation(libraryRelease, shadow.getValue(),highLevelQualifier.getValue());
        }
        //End #DEF.TST1841:






        DriverInformation driverInfo = new DriverInformation(driver.getValue());
        driverInfo.setReleaseInfomation(releaseInfo);

        //
        ProcessWrapperForSingleStep driverReportWrapper = new ProcessWrapperForSingleStep(this);

        com.ibm.sdwb.build390.process.steps.DriverReport driverReportStep = new com.ibm.sdwb.build390.process.steps.DriverReport(driverInfo, setup.getMainframeInfo(), setup.getLibraryInfo(), new java.io.File(MBGlobals.Build390_path+"misc"+java.io.File.separator), driverReportWrapper);
        driverReportStep.setAlwaysRun(true);
        driverReportStep.setCheckForHLQAndDriver(true);
        driverReportStep.setForceNewReport(true);
        driverReportStep.setSummaryType("ONLY");
        driverReportWrapper.setStep(driverReportStep);
        // setCancelButtonStatus(false);
        driverReportWrapper.externalRun();  

        MBBuild tempBuild = new MBBuild(driverReportStep.getParser().getBuildid(),getLEP()); 
        tempBuild.setLocked(driverReportStep.getParser().getBuildid());
        driverReportStep.getParser().doDriverLockCheck(tempBuild);
        // 


        ProcessWrapperForSingleStep wrapper = new ProcessWrapperForSingleStep(this);
        com.ibm.sdwb.build390.process.steps.DeleteMVSDriver driverDelete = new com.ibm.sdwb.build390.process.steps.DeleteMVSDriver(setup.getMainframeInfo(), MBGlobals.Build390_path+"misc"+java.io.File.separator, driverInfo, wrapper );
        wrapper.setStep(driverDelete);
        driverDelete.setShowFilesAfterRun(true,true);

        setCancelableProcess(wrapper);

        wrapper.externalRun();

        //Begin #DEF.TST1726:
        com.ibm.sdwb.build390.process.MVSReleaseAndDriversList releaseAndDriversList = new com.ibm.sdwb.build390.process.MVSReleaseAndDriversList(setup.getMainframeInfo(), setup.getLibraryInfo(), MBClient.getCacheDirectory() ,this);

        setCancelableProcess(releaseAndDriversList);

        releaseAndDriversList.externalRun();
        //End #DEF.TST1726:
    }
}
