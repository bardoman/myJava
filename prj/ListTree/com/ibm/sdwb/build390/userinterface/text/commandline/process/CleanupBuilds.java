
package com.ibm.sdwb.build390.userinterface.text.commandline.process;

import java.util.*;
import java.io.*;
import com.ibm.sdwb.build390.*;
import com.ibm.sdwb.build390.userinterface.text.commandline.*;
import com.ibm.sdwb.build390.userinterface.text.commandline.arguments.*;
import com.ibm.sdwb.build390.process.management.*;
import com.ibm.sdwb.build390.logprocess.*;
import com.ibm.sdwb.build390.utilities.*;

public class CleanupBuilds extends CommandLineProcess {

    public static final String PROCESSNAME = "CLEANUP";

    private UnlockDriverSwitch unlockDriver = new UnlockDriverSwitch();               
    private DeleteAllHostDataSetsSwitch deleteAllHostDataSets = new DeleteAllHostDataSetsSwitch();      
    private DeleteAllJobOutputSwitch deleteAllJobs = new DeleteAllJobOutputSwitch();              
    private DeleteAllLocalFilesSwitch deleteAllLocalFiles = new DeleteAllLocalFilesSwitch();        
    private DeleteALLSwitch  deleteALLData = new DeleteALLSwitch();
    private MultipleAssociatedCommandLineArgument buildIDs = new MultipleAssociatedCommandLineArgument();
    private File build390HomeDirectory = new File(MBGlobals.Build390_path);
    private Set buildSet = null;

    public CleanupBuilds(LogEventProcessor tempLep, com.ibm.sdwb.build390.MBStatus tempStatus) {
        super(PROCESSNAME, tempLep, tempStatus);
        this.buildSet = buildSet;
    }

    public String getHelpDescription() {
        return "The CLEANUP command deletes local and remote files that were\n"+
        "created as the result of a build and unlocks the driver associated\n"+
        "with a build.";
    }

    public String getHelpExamples() {
        return "1."+getProcessTypeHandled()+" BUILD1=<buildid1> BUILD2=<buildid2> /unlock \n"+ "2."+getProcessTypeHandled()+" BUILD1=<buildid1> BUILD2=<buildid2> /unlock /hostds /jobs /local\n" +
        "3."+getProcessTypeHandled()+" BUILD1=<buildid1> BUILD2=<buildid2> /deleteall \n";
    }

    protected void setArgumentStructure(RequiredAndOptionalArguments argumentStructure) {
        BooleanAnd required = new BooleanAnd();
        buildIDs.addCommandLineArgument(new Build());
        required.addBooleanInterface(buildIDs);
        BooleanExclusiveOr optionsOr = new BooleanExclusiveOr();
        optionsOr.addBooleanInterface(deleteALLData);
        BooleanOr singleOptionsOr = new BooleanOr();
        singleOptionsOr.addBooleanInterface(unlockDriver);
        singleOptionsOr.addBooleanInterface(deleteAllHostDataSets);
        singleOptionsOr.addBooleanInterface(deleteAllJobs);
        singleOptionsOr.addBooleanInterface(deleteAllLocalFiles);
        optionsOr.addBooleanInterface(singleOptionsOr);
        required.addBooleanInterface(optionsOr);
        argumentStructure.setRequiredPart(required);

    }

    public void runProcess() throws com.ibm.sdwb.build390.MBBuildException {
        Set driverUnlocks = new HashSet();
        Set jobsToPurge = new HashSet();
        Set mvsBuildDeletes = new HashSet();
        Set localDeletes = new HashSet();
        Iterator buildIterator = buildIDs.getIndexToArgumentsMap().keySet().iterator();
        while (buildIterator.hasNext()) {
            try {
                String indexString = (String) buildIterator.next();
                int index = Integer.parseInt(indexString);
                String singleBuildID = ((CommandLineArgument) ((Set) buildIDs.getIndexToArgumentsMap().get(indexString)).iterator().next()).getValue();
                getStatusHandler().updateStatus("Running cleanup for buildid "+singleBuildID,false);
                MBBuild oneBuild = getBuildForBuildId(singleBuildID);

                //#DEF.TST1844:

                if (oneBuild.getProcessForThisBuild() == null) {
                    if (deleteAllLocalFiles.getValue()!=null || deleteALLData.getValue()!=null) {
                        CleanableEntity localCleanableEntity = new CleanableEntity();
                        localCleanableEntity.addLocalFileOrDirectory(new File(oneBuild.getBuildPath()));
                        localDeletes.add(localCleanableEntity);
                    }
                } else {

                    oneBuild.getProcessForThisBuild().getCleanableEntity().addMVSBuildID(oneBuild.get_buildid(), oneBuild.getDriverInformation());

                    //#DEF:TST1483/TST1579.if no local entry exists add one(hack for now).
                    if (!oneBuild.getProcessForThisBuild().getCleanableEntity().getAllLocalFiles().contains(new File(oneBuild.getBuildPath()))) {
                        oneBuild.getProcessForThisBuild().getCleanableEntity().addLocalFileOrDirectory(new File(oneBuild.getBuildPath()));
                    }

                    if (deleteAllLocalFiles.getValue()!=null || deleteALLData.getValue()!=null) {
                        localDeletes.add(oneBuild.getProcessForThisBuild().getCleanableEntity());
                    }
                    if (deleteAllJobs.getValue()!=null || deleteALLData.getValue()!=null) {
                        jobsToPurge.add(oneBuild.getProcessForThisBuild().getCleanableEntity());
                    }
                    if (unlockDriver.getValue()!=null || deleteALLData.getValue()!=null) {
                        driverUnlocks.add(oneBuild.getProcessForThisBuild().getCleanableEntity());
                    }
                    //TST1483 put is if as the last one. 
                    if (deleteAllHostDataSets.getValue()!=null || deleteALLData.getValue()!=null) {
                        mvsBuildDeletes.add(oneBuild.getProcessForThisBuild().getCleanableEntity());
                    }
                }
            } catch (java.io.IOException ioe) {
                throw new com.ibm.sdwb.build390.GeneralError("An error occurred during cleanup",ioe);
            }
        }

        com.ibm.sdwb.build390.process.CleanProcessArtifacts cleanupStuff = new com.ibm.sdwb.build390.process.CleanProcessArtifacts(driverUnlocks, jobsToPurge, mvsBuildDeletes, localDeletes, this);

        setCancelableProcess(cleanupStuff);

        cleanupStuff.externalRun();


    }
    private MBBuild getBuildForBuildId(String buildId) throws com.ibm.sdwb.build390.MBBuildException, java.io.IOException{
        File locatedBuildFile = SerializedBuildsLister.getInstance(build390HomeDirectory).locateBuildId(buildId);
        try {
            if (locatedBuildFile!=null) {
                if (locatedBuildFile.exists()) {
                    return MBBuildLoader.loadBuild(locatedBuildFile);
                }
            }
        } catch (MBBuildException mbe) {
            getLEP().LogException(mbe);
        }
        throw new SyntaxError("Specified build " + buildId + " not found.");
    }
}
