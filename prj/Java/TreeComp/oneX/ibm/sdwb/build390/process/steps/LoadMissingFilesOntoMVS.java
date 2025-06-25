package com.ibm.sdwb.build390.process.steps;

import com.ibm.sdwb.build390.*;
import com.ibm.sdwb.build390.logprocess.LogEventProcessor;
import java.util.*;
import java.io.*;

public class LoadMissingFilesOntoMVS extends ProcessStep {
    static final long serialVersionUID = 1111111111111111L;
    private MBBuild build = null;
    private String shadowCheckResultFile = null;
    private String cmvcAuthorizationType = null;
    private MBChkFileParser shadowCheckResultParser = null;
    private boolean forgetCurrentlyUploadedFiles = true;
    private static final String DELETEDFILES = new String("deletedFiles.ser");
    private static final String DELETEDFILESTEXT = new String("deletedFiles.txt");

    public LoadMissingFilesOntoMVS(MBBuild tempBuild, String tempShadowCheckFile, String tempCmvcAuthorizationType, com.ibm.sdwb.build390.process.AbstractProcess tempProc) {
        super(tempProc,"Load Missing Files Onto MVS");
        shadowCheckResultFile = tempShadowCheckFile ;
        cmvcAuthorizationType = tempCmvcAuthorizationType;
        build = tempBuild;
        setUndoBeforeRerun(false);
    }

    public MBChkFileParser getShadowCheckResultParser() {
        return shadowCheckResultParser;
    }

    public boolean isOnlyRestartableFromBeginning() {
        return false;
    }

    public void restartFromMiddle() {
        forgetCurrentlyUploadedFiles = false;
    }

    /**
     * This is the method that should be implemented to actually
     * run the process.	Use executionArgument if you need to 
     * access the argument from the execute method.
     * 
     * @return Object indicating output of the step.
     */
    public void execute() throws com.ibm.sdwb.build390.MBBuildException{
        getLEP().LogSecondaryInfo(getFullName(),"Entry");
        File shadowCheckFile = new File(shadowCheckResultFile);
        if(!build.get_buildtype().equals("MIGRATE") & shadowCheckFile.exists()) {
            shadowCheckResultParser =  new MBChkFileParser(shadowCheckFile, build.getMapOfPartNameToPartInfo(), getLEP());
            List deletedFiles = shadowCheckResultParser.getDeletedFiles();
            List migratedData = shadowCheckResultParser.getRecalledDatasets();
            if(migratedData == null) {
                migratedData = new ArrayList();
            }
            if(deletedFiles != null) {
                try {
                    getStatusHandler().updateStatus("Saving list of deleted files", false);
                    ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(build.getBuildPath() + DELETEDFILES));
                    oos.writeObject(deletedFiles);
                    oos.close();
                    FileWriter delTextOut = new FileWriter(build.getBuildPath() +DELETEDFILESTEXT);
                    delTextOut.write(deletedFiles.toString());
                    delTextOut.close();
                }
                catch(IOException ioe) {
                    getLEP().LogException("There was an error saving the list of deleted files", ioe);
                }
            }
            if(!shadowCheckResultParser.getTableEntriesContaining("*WARN*").isEmpty() & build.getOptions().isHaltOnShadowCheckWarnings()) {
                throw new HostError("Warnings were found when checking the status of parts to be put in the driver.", shadowCheckResultFile.substring(0,shadowCheckResultFile.lastIndexOf(".")));
            }
            if(shadowCheckResultParser.getMissingFiles() != null) {
                getStatusHandler().updateStatus("Uploading parts", false);
                String MVSFilenamePattern = build.getReleaseInformation().getMvsHighLevelQualifier()+"."+build.getReleaseInformation().getMvsName()+"."+build.get_buildid()+"."+MBConstants.FILENAMEPLACEHOLDER;
                if(build instanceof com.ibm.sdwb.build390.MBUBuild) {
                    if(((com.ibm.sdwb.build390.MBUBuild) build).getFastTrack()) {
                        MVSFilenamePattern = build.getSetup().getMainframeInfo().getMainframeUsername()+"."+build.get_buildid()+"."+MBConstants.FILENAMEPLACEHOLDER;
                    }
                }
                MBUpdateFiles updater = new MBUpdateFiles(getStatusHandler(),getLEP());//TST3169

                currentRunning= updater;//TST3169

                updater.updateMVSFiles(build, shadowCheckResultParser.getMissingFiles(), MVSFilenamePattern, cmvcAuthorizationType);

                currentRunning = null;//TST3169
            }
        }
    }
}
