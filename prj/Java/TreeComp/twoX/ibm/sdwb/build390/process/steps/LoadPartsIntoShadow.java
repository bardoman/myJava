package com.ibm.sdwb.build390.process.steps;

import com.ibm.sdwb.build390.*;
import com.ibm.sdwb.build390.logprocess.LogEventProcessor;
import com.ibm.sdwb.build390.info.FileInfo;
import java.io.File;
import java.util.*;

//*************************************************************************
//09/02/2003 #DEF.TST1462:  Nullpointer except on Userbuild with PDS
//*************************************************************************

public class LoadPartsIntoShadow extends MainframeCommunication {
    static final long serialVersionUID = 1111111111111111L;
    private MBBuild build = null;
    private File partlistOrderFile = null;
    private boolean isLoadorder = false;
    private boolean areTherePartsToProcess = true;
    private String partClass= null;

    private static final String SHADOWLOADFILE = "ShadowLoad";

    public LoadPartsIntoShadow(MBBuild tempBuild, File tempPartlistOrderFile, com.ibm.sdwb.build390.process.AbstractProcess tempProc) {
        super(tempBuild.getBuildPath() + SHADOWLOADFILE,"Load Parts Into Shadow", tempProc);
        build = tempBuild;
        partlistOrderFile = tempPartlistOrderFile;
        setUndoBeforeRerun(false);
    }

    public void setPartClass(String partClass) {
        this.partClass = partClass;
    }

    public void setLoadOrderProcessing(boolean tempIsLoad) {
        isLoadorder = tempIsLoad;
    }

    /**
     * The execution argument is the file that has Shadow Check output.
     * This is the method that should be implemented to actually
     * run the process.	Use executionArgument if you need to 
     * access the argument from the execute method.
     */
    public void execute() throws com.ibm.sdwb.build390.MBBuildException{
        getLEP().LogSecondaryInfo(getFullName(),"Entry");
        getStatusHandler().updateStatus("Verifying results of partlist check against shadow", false);
        if(partlistOrderFile.exists()|build.get_buildtype().equals("MIGRATE")) {
            if(!build.get_buildtype().equals("MIGRATE")) {
                String MVSOrderFile = new String(build.getReleaseInformation().getMvsHighLevelQualifier()+"."+build.getReleaseInformation().getMvsName()+ "."+build.getDriverInformation().getName()+".ORDERS("+build.get_buildid()+")");
                MBFtp partlistFtpClient = new MBFtp(build.getSetup().getMainframeInfo(),mainProcess.getLEP());
                if(!partlistFtpClient.put(partlistOrderFile, MVSOrderFile)) {
                    throw new FtpError("Could not upload "+partlistOrderFile+" to "+MVSOrderFile);
                }
            }
            //Begin INT3097C
            String LoadPartsIntoShadowCommand = "SHADPART OP=LOAD, DRIVER="+build.getDriverInformation().getName()+", "+build.getLibraryInfo().getDescriptiveStringForMVS()+
                                                ", CMVCREL=\'"+build.getReleaseInformation().getLibraryName()+"\', BUILDID="+build.get_buildid();

            boolean isFakeLib = MBClient.getCommandLineSettings().getMode().isFakeLibrary();

            boolean isPDSBuild = false;

            if(build instanceof MBUBuild) {

                isPDSBuild = ((MBUBuild) build).getSourceType()==MBUBuild.PDS_SOURCE_TYPE;
            }

            if(isFakeLib & isPDSBuild) {
                LoadPartsIntoShadowCommand+= ", NOLIB=YES";
            }
            //End INT3097C

            if(isLoadorder) {
                LoadPartsIntoShadowCommand +=", LODORDER";
                setOutputHeaderLocation(build.getBuildPath() + SHADOWLOADFILE +"-LOADORDER");
            }

            // Submit the command
            createMainframeCall(LoadPartsIntoShadowCommand, "Loading files into shadow", build.getSetup().getMainframeInfo());
            setPathToVerbFile(build.getReleaseInformation().getMvsHighLevelQualifier()+"."+build.getReleaseInformation().getMvsName()+"."+build.getDriverInformation().getName()+".ORDERS");
            runMainframeCall();
            getStatusHandler().updateStatus("Verifying load of files", false);

            MBChkFileParser loadedFileChecker;

            //Begin UserBldUpdate0
            if(partClass!=null) {

                loadedFileChecker = new MBChkFileParser(getOutputFile(), build.getMapOfPartNameToPartInfo(), partClass, getLEP());
            }
            else {

                loadedFileChecker = new MBChkFileParser(getOutputFile(), build.getMapOfPartNameToPartInfo(), getLEP());
            }
            //End UserBldUpdate0

            Set errorHash = loadedFileChecker.getTableEntriesContaining("*ERROR*");
            if(!errorHash.isEmpty()) {
                throw new HostError("There were errors loading files into shadow", this);
            }
        }
    }
}

