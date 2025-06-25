package com.ibm.sdwb.build390.userinterface.text.commandline.process;

import java.util.*;
import java.io.*;
import com.ibm.sdwb.build390.*;
import com.ibm.sdwb.build390.process.steps.*;
import com.ibm.sdwb.build390.process.*;
import com.ibm.sdwb.build390.userinterface.text.commandline.*;
import com.ibm.sdwb.build390.userinterface.text.commandline.arguments.*;
import com.ibm.sdwb.build390.mainframe.*;
import com.ibm.sdwb.build390.logprocess.*;
import com.ibm.sdwb.build390.utilities.*;


public class MainframeUNCHECKEDSocketConnection extends CommandLineProcess {

    public static final String PROCESSNAME = "KENT";

    private CommandToPass command = new CommandToPass();
    private OutputLocation output = new OutputLocation();
    private Delsysout delSysout = new Delsysout();
    private UnsetCLROUT unsetCLROUT = new UnsetCLROUT();
    private Hold hold = new Hold();
    private Sysout sysout = new Sysout();
    private Scheduler schedule = new Scheduler();
    private JobsCancel jobsCancel = new JobsCancel();
    private Jobstatus jobStatus = new Jobstatus();
    private Systsprt systsprt = new Systsprt();
    private TSO tso = new TSO();
    private com.ibm.sdwb.build390.user.Setup currentSetup = null;



    public MainframeUNCHECKEDSocketConnection(LogEventProcessor tempLep, com.ibm.sdwb.build390.MBStatus tempStatus) {
        super(PROCESSNAME, tempLep, tempStatus);
        currentSetup = com.ibm.sdwb.build390.user.SetupManager.getSetupManager().createSetupInstance();
    }

    public String getHelpDescription() {
        return new String(); // this is an unofficial internal command.  We do NOT want to give any commandline help
    }

    public String getHelpExamples() {
        return new String();
    }

    protected void setArgumentStructure(RequiredAndOptionalArguments argumentStructure) {
        BooleanAnd baseAnd = new BooleanAnd();
        baseAnd.addBooleanInterface(command);
        baseAnd.addBooleanInterface(output);
        argumentStructure.setRequiredPart(baseAnd);

        argumentStructure.addOption(delSysout);
        argumentStructure.addOption(unsetCLROUT);
        argumentStructure.addOption(hold);
        argumentStructure.addOption(sysout);
        argumentStructure.addOption(schedule);
        argumentStructure.addOption(jobsCancel);
        argumentStructure.addOption(jobStatus);
        argumentStructure.addOption(systsprt);
        argumentStructure.addOption(tso);
    }

    public void runProcess() throws com.ibm.sdwb.build390.MBBuildException{
        ProcessWrapperForSingleStep fakeProc = new ProcessWrapperForSingleStep(this);
        PrivateMainCom commLink = new PrivateMainCom(fakeProc);
        fakeProc.setStep(commLink);


        fakeProc.externalRun();
    }

    private class PrivateMainCom extends MainframeCommunication{
        PrivateMainCom(AbstractProcess tempProc){
            super(output.getValue(), "Kent",tempProc );
        }

        public void execute() throws com.ibm.sdwb.build390.MBBuildException{
            createMainframeCall(command.getValue(), "Running custom command", currentSetup.getMainframeInfo());
            if (hold.isSatisfied()) {
                set_hold(hold.getBooleanValue());
            }
            if (delSysout.isSatisfied()) {
                if (delSysout.getBooleanValue()) {
                    setDelsysout();
                }
            }
            if (unsetCLROUT.isSatisfied()) {
                if (unsetCLROUT.getBooleanValue()) {
                    unset_clrout();
                }
            }
            if (sysout.isSatisfied()) {
                if (sysout.getBooleanValue()) {
                    setSysout();
                }
            }
            if (schedule.isSatisfied()) {
                if (schedule.getBooleanValue()) {
                    setScheduler();
                }
            }
            if (jobsCancel.isSatisfied()) {
                if (jobsCancel.getBooleanValue()) {
                    setJobsCancel();
                }
            }
            if (jobStatus.isSatisfied()) {
                if (jobStatus.getBooleanValue()) {
                    setJobstatus();
                }
            }
            if (systsprt.isSatisfied()) {
                if (systsprt.getBooleanValue()) {
                    setSystsprt();
                }
            }
            if (tso.isSatisfied()) {
                if (tso.getBooleanValue()) {
                    setTSO();
                }
            }
            runMainframeCall();
        }
    }

    private class CommandToPass extends CommandLineArgument {

        private static final String keyword = "COMMANDTOPASS";

        public CommandToPass(){
            super(keyword,new String());
        }
    }

    private class OutputLocation extends CommandLineArgument {

        private static final String keyword = "OUTPUTLOCATION";

        public OutputLocation(){
            super(keyword,new String());
        }
    }

    private class Delsysout extends BinaryCommandLineArgument {

        private static final String keyword = "DELSYSOUT";

        public Delsysout(){
            super(keyword,new String(), false);
        }
    }

    private class UnsetCLROUT extends BinaryCommandLineArgument {

        private static final String keyword = "UNSETCLROUT";

        public UnsetCLROUT(){
            super(keyword,new String(), false);
        }
    }

    private class Hold extends BinaryCommandLineArgument {

        private static final String keyword = "HOLDSWITCH";

        public Hold(){
            super(keyword,new String(), false);
        }
    }

    private class Sysout extends BinaryCommandLineArgument {

        private static final String keyword = "SYSOUT";

        public Sysout(){
            super(keyword,new String(), false);
        }
    }

    private class Scheduler extends BinaryCommandLineArgument {

        private static final String keyword = "SCHEDULER";

        public Scheduler(){
            super(keyword,new String(), false);
        }
    }

    private class JobsCancel extends BinaryCommandLineArgument {

        private static final String keyword = "JOBSCANCEL";

        public JobsCancel(){
            super(keyword,new String(), false);
        }
    }

    private class Jobstatus extends BinaryCommandLineArgument {

        private static final String keyword = "JOBSTATUS";

        public Jobstatus(){
            super(keyword,new String(), false);
        }
    }

    private class Systsprt extends BinaryCommandLineArgument {

        private static final String keyword = "SYSTSPRT";

        public Systsprt(){
            super(keyword,new String(), false);
        }
    }

    private class TSO extends BinaryCommandLineArgument {

        private static final String keyword = "TSO";

        public TSO(){
            super(keyword,new String(), false);
        }
    }

}
