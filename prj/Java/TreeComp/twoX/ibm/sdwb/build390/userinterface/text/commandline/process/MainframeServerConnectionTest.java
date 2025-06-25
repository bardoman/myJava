package com.ibm.sdwb.build390.userinterface.text.commandline.process;

import com.ibm.sdwb.build390.logprocess.LogEventProcessor;
import com.ibm.sdwb.build390.user.SetupManager;
import com.ibm.sdwb.build390.userinterface.text.commandline.RequiredAndOptionalArguments;
import com.ibm.sdwb.build390.userinterface.text.commandline.arguments.NoArguments;

public class MainframeServerConnectionTest extends CommandLineProcess
{

    public static final String PROCESSNAME = "NUTSY";

    public MainframeServerConnectionTest(LogEventProcessor tempLep, com.ibm.sdwb.build390.MBStatus tempStatus)
    {
        super(PROCESSNAME, tempLep, tempStatus);
    }
    public String getHelpDescription()
    {
        return getProcessTypeHandled()+ " command tests for a connection to the Build/390 server.";
    }

    public String getHelpExamples()
    {
        return getProcessTypeHandled();
    }
    protected void setArgumentStructure(RequiredAndOptionalArguments argumentStructure)
    {
        argumentStructure.setRequiredPart(new NoArguments());
    }

    public void runProcess() throws com.ibm.sdwb.build390.MBBuildException{
        com.ibm.sdwb.build390.process.ProcessWrapperForSingleStep stepWrapper = new com.ibm.sdwb.build390.process.ProcessWrapperForSingleStep(this);
        com.ibm.sdwb.build390.process.steps.MVSServerStatus serverStatusRequest = new com.ibm.sdwb.build390.process.steps.MVSServerStatus(SetupManager.getSetupManager().createSetupInstance().getMainframeInfo(), stepWrapper);
        serverStatusRequest.setShowFilesAfterRun(false,true);
        stepWrapper.setStep(serverStatusRequest);

        setCancelableProcess(stepWrapper);

        stepWrapper.externalRun();
    }
}
