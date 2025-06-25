package com.ibm.sdwb.build390.userinterface.text.commandline.process;

import com.ibm.sdwb.build390.GeneralError;
import com.ibm.sdwb.build390.MBBuildException;
import com.ibm.sdwb.build390.MBClient;
import com.ibm.sdwb.build390.MBGlobals;
import com.ibm.sdwb.build390.logprocess.LogEventProcessor;
import com.ibm.sdwb.build390.mainframe.ReleaseInformation;
import com.ibm.sdwb.build390.user.Setup;
import com.ibm.sdwb.build390.user.SetupManager;
import com.ibm.sdwb.build390.userinterface.text.commandline.RequiredAndOptionalArguments;
import com.ibm.sdwb.build390.userinterface.text.commandline.arguments.MainframeHighLevelQualifier;
import com.ibm.sdwb.build390.userinterface.text.commandline.arguments.MainframeRelease;
import com.ibm.sdwb.build390.utilities.BooleanAnd;

public class MainframeShadowDelete extends CommandLineProcess {

    public static final String PROCESSNAME = "DELETERELEASE";

    private MainframeHighLevelQualifier highLevelQualifier = new MainframeHighLevelQualifier();
    private MainframeRelease shadow = new MainframeRelease();

    public MainframeShadowDelete(LogEventProcessor tempLep, com.ibm.sdwb.build390.MBStatus tempStatus) {
        super(PROCESSNAME, tempLep, tempStatus);
    } 
    public String getHelpDescription() {
        return getProcessTypeHandled()+ " command  deletes a mvs shadow.";
    }

    public String getHelpExamples() {
        return getProcessTypeHandled()+" MVSRELEASE=<mvsrelease> MVSHLQ=<hlq>";
    }

    protected void setArgumentStructure(RequiredAndOptionalArguments argumentStructure) {
        BooleanAnd baseAnd = new BooleanAnd();
        baseAnd.addBooleanInterface(shadow);
        baseAnd.addBooleanInterface(highLevelQualifier);
        argumentStructure.setRequiredPart(baseAnd);
    }

    public void runProcess() throws MBBuildException{
        Setup setup = SetupManager.getSetupManager().createSetupInstance();

        ReleaseInformation releaseInfo = getReleaseInformation(shadow.getValue(),setup, false);
        if(highLevelQualifier.isSatisfied()){
            releaseInfo = new ReleaseInformation(releaseInfo.getLibraryName(),releaseInfo.getMvsName(),highLevelQualifier.getValue());
        }

        com.ibm.sdwb.build390.process.DeleteMVSRelease releaseDelete = new com.ibm.sdwb.build390.process.DeleteMVSRelease(setup, releaseInfo, this);

        setCancelableProcess(releaseDelete);

        releaseDelete.externalRun();

        setup.getMainframeInfo().removeRelease(releaseInfo,setup.getLibraryInfo());
    }

}
