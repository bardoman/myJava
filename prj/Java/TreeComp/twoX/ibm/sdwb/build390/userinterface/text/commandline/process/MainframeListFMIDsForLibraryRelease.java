package com.ibm.sdwb.build390.userinterface.text.commandline.process;

import java.io.File;

import com.ibm.sdwb.build390.MBGlobals;
import com.ibm.sdwb.build390.logprocess.LogEventProcessor;
import com.ibm.sdwb.build390.mainframe.ReleaseInformation;
import com.ibm.sdwb.build390.user.Setup;
import com.ibm.sdwb.build390.user.SetupManager;
import com.ibm.sdwb.build390.userinterface.text.commandline.RequiredAndOptionalArguments;
import com.ibm.sdwb.build390.utilities.BooleanAnd;
import com.ibm.sdwb.build390.userinterface.text.commandline.arguments.*;

public class MainframeListFMIDsForLibraryRelease extends CommandLineProcess {

    public static final String PROCESSNAME = "GETFMID_FOR_CMVCRELEASE";

    private LibraryRelease libRelease = new LibraryRelease();
    private MainframeDriver driver = new MainframeDriver();

    public MainframeListFMIDsForLibraryRelease(LogEventProcessor tempLep, com.ibm.sdwb.build390.MBStatus tempStatus) {
        super(PROCESSNAME, tempLep, tempStatus);
    }

    public String getHelpDescription() {
        return getProcessTypeHandled()+ " command returns the fmid for the library release.";
    }

    public String getHelpExamples() {
        return getProcessTypeHandled()+" LIBRELEASE=<cmvcrelease> ";
    }

    protected void setArgumentStructure(RequiredAndOptionalArguments argumentStructure) {
        BooleanAnd baseAnd = new BooleanAnd();
        baseAnd.addBooleanInterface(libRelease);
        argumentStructure.setRequiredPart(baseAnd);

        argumentStructure.addOption(driver);

    }

    public void runProcess() throws com.ibm.sdwb.build390.MBBuildException{
        File outDirectory = new File(MBGlobals.Build390_path+"misc");
        com.ibm.sdwb.build390.process.ProcessWrapperForSingleStep stepWrapper = new com.ibm.sdwb.build390.process.ProcessWrapperForSingleStep(this);
        Setup setup  = SetupManager.getSetupManager().createSetupInstance();

        ReleaseInformation releaseInfo = getReleaseInformation(libRelease.getValue(),setup, true);

        com.ibm.sdwb.build390.process.steps.mainframe.ListFMIDsForLibraryRelease listFMIDs = new com.ibm.sdwb.build390.process.steps.mainframe.ListFMIDsForLibraryRelease(libRelease.getValue(), setup,outDirectory, stepWrapper);
        stepWrapper.setStep(listFMIDs);
        listFMIDs.setShowFilesAfterRun(true, true);

        if(driver.isSatisfied()) {
            listFMIDs.setDriver(driver.getValue());
        }

        stepWrapper.externalRun();
    }
}
