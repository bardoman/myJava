package com.ibm.sdwb.build390.userinterface.text.commandline.process;

import com.ibm.sdwb.build390.logprocess.LogEventProcessor;
import com.ibm.sdwb.build390.mainframe.ReleaseInformation;
import com.ibm.sdwb.build390.user.Setup;
import com.ibm.sdwb.build390.user.SetupManager;
import com.ibm.sdwb.build390.userinterface.text.commandline.RequiredAndOptionalArguments;
import com.ibm.sdwb.build390.userinterface.text.commandline.arguments.LibraryRelease;

public class MainframeShadowReport extends CommandLineProcess {

    public static final String PROCESSNAME = "SHADOWRPT";

    private LibraryRelease libRelease = new LibraryRelease();

    public MainframeShadowReport(LogEventProcessor tempLep, com.ibm.sdwb.build390.MBStatus tempStatus) {
        super(PROCESSNAME, tempLep, tempStatus);
    }
    public String getHelpDescription() {
        return getProcessTypeHandled()+ " command queries the Build/390 server for a report on a shadow release.";
    }

    public String getHelpExamples() {
        return getProcessTypeHandled()+" LIBRELEASE=<librelease>";
    }
    protected void setArgumentStructure(RequiredAndOptionalArguments argumentStructure) {
        argumentStructure.setRequiredPart(libRelease);
    }

    public void runProcess() throws com.ibm.sdwb.build390.MBBuildException{
        Setup setup = SetupManager.getSetupManager().createSetupInstance();
        ReleaseInformation releaseInfo = getReleaseInformation(libRelease.getValue(),setup, true);
        com.ibm.sdwb.build390.process.ProcessWrapperForSingleStep stepWrapper = new com.ibm.sdwb.build390.process.ProcessWrapperForSingleStep(this);
        com.ibm.sdwb.build390.process.steps.ShadowReport shadowReport = new com.ibm.sdwb.build390.process.steps.ShadowReport(setup, libRelease.getValue(),stepWrapper);
        shadowReport.setShowFilesAfterRun(true, false);
        stepWrapper.setStep(shadowReport);

        setCancelableProcess(stepWrapper);

        stepWrapper.externalRun();

    }
}
