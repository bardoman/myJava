
package com.ibm.sdwb.build390.process.steps;

import com.ibm.sdwb.build390.*;
import java.io.*;
import java.util.*;

//08/27/2003 #DEF:TST1538 nullpointer in usermod. throw hosterror instead of general error.
//09/15/2003 #DEF:TST1558 fine tune hosterr msg.

public class PartsVerificationForUsermodPackaging extends MainframeCommunication {
    static final long serialVersionUID = 1111111111111111L;

    private static final String ERRORLINE = "*ERROR*";

    private String smodName = null;
    private boolean partsExistForProcessing = false;
    private boolean usermodProcessingWithoutShippablesEnabled = false;
    private File partsVerificationOrderFile = null;
    private MBBuild build =null;
    private boolean uploadFile = true;
    private String oneFMID = null;
    private boolean isPartsExistsForPackaging = false;


    public PartsVerificationForUsermodPackaging(MBBuild build, String oneFMID,File partsVerificationOrderFile, com.ibm.sdwb.build390.process.AbstractProcess tempProc) {
        super(build.getBuildPath()+"SMODBLD-CHECK-"+build.getDriverInformation().getName() + "-"+oneFMID, "PartsVerificationForUsermodPackaging", tempProc);
        this.build = build;
        this.oneFMID = oneFMID;
        this.partsVerificationOrderFile = partsVerificationOrderFile;
        setVisibleToUser(false);
        setUndoBeforeRerun(false);
    }

    public String getFMID() {
        return oneFMID;
    }

    public boolean isPartsExistForProcessing(){
        return partsExistForProcessing;
    }

    public boolean isUsermodProcessingWithoutShippablesEnabled(){
        return usermodProcessingWithoutShippablesEnabled;
    }

    public void setUploadFile(boolean tempUp){
        uploadFile = tempUp;
    }


    /**
     * This is the method that should be implemented to actually
     * run the process.	Use executionArgument if you need to 
     * access the argument from the execute method.
     * 
     * @return Object indicating output of the step.
     */
    public void execute() throws com.ibm.sdwb.build390.MBBuildException {
        getLEP().LogSecondaryInfo(getFullName(),"Entry");
        String dsName = build.getReleaseInformation().getMvsHighLevelQualifier()+"."+build.getReleaseInformation().getMvsName()+"."+build.getDriverInformation().getName()+"."+"ORDERS";
        String MVSOrdersFile = dsName+"("+build.get_buildid()+")";
        if (uploadFile) {
            MBFtp mftp = new MBFtp(build.getSetup().getMainframeInfo(),getLEP());
            if (!mftp.put(partsVerificationOrderFile, MVSOrdersFile)) {
                throw new FtpError("Could not upload "+partsVerificationOrderFile.getAbsolutePath()+" to "+MVSOrdersFile);
            }
        }
        String partsCheckCommand = "SMODBLD CMVCREL=\'"+build.getReleaseInformation().getLibraryName()+"\', DRIVER=\'"+build.getDriverInformation().getName()+"\', BUILDID="+build.get_buildid()+ ", OP=CHECK, TYPE=USERMOD, FUNCTION="+oneFMID+", "+build.getLibraryInfo().getDescriptiveStringForMVS();

        createMainframeCall(partsCheckCommand, "Verifying parts to be built for  "+build.getDriverInformation().getName()+ ", FMID ="+ getFMID(), true, build.getSetup().getMainframeInfo());
        setPathToVerbFile(dsName);
        setSystsprt(); // return SYSTSPRT data as CLRPRINT data
        runMainframeCall();
        parse();
    }

    private void parse() throws com.ibm.sdwb.build390.MBBuildException {

        MBClearReport smodOPCHECKParser = new MBClearReport(getPrintFile().getAbsolutePath());


        if (smodOPCHECKParser.isThereError()) {     //TST1538
            throw new HostError("Built parts verification failed for driver="+build.getDriverInformation().getName() + ", and fmid="+oneFMID, this);
        }

        partsExistForProcessing =  smodOPCHECKParser.isPartsToProcess();
        usermodProcessingWithoutShippablesEnabled = smodOPCHECKParser.isUsermodProcessingWithNoShippablesEnabled();

    }


}   





