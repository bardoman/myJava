package com.ibm.sdwb.build390.userinterface.text.commandline.process;

import com.ibm.sdwb.build390.FtpError;
import com.ibm.sdwb.build390.MBFtp;
import com.ibm.sdwb.build390.MBUtilities;
import com.ibm.sdwb.build390.logprocess.LogEventProcessor;
import com.ibm.sdwb.build390.user.Setup;
import com.ibm.sdwb.build390.user.SetupManager;
import com.ibm.sdwb.build390.userinterface.text.commandline.RequiredAndOptionalArguments;
import com.ibm.sdwb.build390.userinterface.text.commandline.arguments.HoldCode;
import com.ibm.sdwb.build390.userinterface.text.commandline.arguments.LibraryTrack;
import com.ibm.sdwb.build390.userinterface.text.commandline.arguments.MainframeHighLevelQualifier;
import com.ibm.sdwb.build390.userinterface.text.commandline.arguments.MainframeRelease;
import com.ibm.sdwb.build390.userinterface.text.commandline.arguments.OutputFile;
import com.ibm.sdwb.build390.utilities.BooleanAnd;

public class MainframeHoldDataRetrieval extends CommandLineProcess {

    public static final String PROCESSNAME = "GETHOLDDATA";

    private MainframeRelease mainRelease = new MainframeRelease();
    private MainframeHighLevelQualifier highLevelQualifier = new MainframeHighLevelQualifier();
    private LibraryTrack libTrack = new LibraryTrack();
    private OutputFile outputFile = new OutputFile();
    private HoldCode holdCode = new HoldCode();
    private static String HOLDDATACONTAINER = "COMMENTS";

    public MainframeHoldDataRetrieval(LogEventProcessor tempLep, com.ibm.sdwb.build390.MBStatus tempStatus){
        super(PROCESSNAME, tempLep, tempStatus);
    }
    public String getHelpDescription(){
        return getProcessTypeHandled()+ " command retrieves hold data from the host and stores it in a file.";
    }

    public String getHelpExamples(){
        return "1.To perform " +getProcessTypeHandled()+ " for holdcode=A in windows.\n"+
               getProcessTypeHandled()+" MVSRELEASE=<mvsrelease> MVSHLQ=<hlq>\n"+
               "       HOLDCODE=A FILE=C:\build390\\holddata.out\n\n"+
               "2.To perform " +getProcessTypeHandled()+ " for holdcode=A in uss.\n"+
                getProcessTypeHandled()+" MVSRELEASE=<mvsrelease> MVSHLQ=<hlq>\n"+
               "       HOLDCODE=A FILE=/u/MYUSERID/holddata.out\n";
    }




    protected void setArgumentStructure(RequiredAndOptionalArguments argumentStructure){
        BooleanAnd baseAnd = new BooleanAnd();
        baseAnd.addBooleanInterface(highLevelQualifier);
        baseAnd.addBooleanInterface(libTrack);
        baseAnd.addBooleanInterface(mainRelease);
        baseAnd.addBooleanInterface(outputFile);
        baseAnd.addBooleanInterface(holdCode);
        argumentStructure.setRequiredPart(baseAnd);
    }

    public void runProcess() throws com.ibm.sdwb.build390.MBBuildException{
        MBUtilities.validateTrackForSMOD(libTrack.getValue());

        String MVSFilename = new String(highLevelQualifier.getValue()+"."+mainRelease.getValue()+ "."+HOLDDATACONTAINER+"."+holdCode.getValue()+libTrack.getValue());

        Setup setup = SetupManager.getSetupManager().createSetupInstance();

        MBFtp mftp = new MBFtp(setup.getMainframeInfo(),getLEP());
        if (!mftp.get(MVSFilename, outputFile.getValue(), true)) {
            throw new FtpError("Could not download "+outputFile.getValue()+" from "+MVSFilename);
        }
    }
}
