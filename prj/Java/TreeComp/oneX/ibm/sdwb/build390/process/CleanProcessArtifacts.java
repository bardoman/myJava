package com.ibm.sdwb.build390.process;

import com.ibm.sdwb.build390.process.steps.*;
import com.ibm.sdwb.build390.logprocess.LogEventProcessor;
import com.ibm.sdwb.build390.*;
import com.ibm.sdwb.build390.userinterface.UserCommunicationInterface;
import com.ibm.sdwb.build390.process.management.CleanableEntity;
import java.util.*;

public class CleanProcessArtifacts extends AbstractProcess {
    static final long serialVersionUID = 1111111111111111L;

    private Set cleanablesThatShouldHaveDriversUnlocked = null;
    private Set cleanablesThatShouldHaveJobsPurged = null;
    private Set cleanablesThatShouldHaveMVSFilesDeleted = null;
    private Set cleanablesThatShouldHaveLocalFilesDeleted = null;


    public CleanProcessArtifacts(Set tempDriverUnlocks, Set tempJobsToPurge, Set tempMVSBuildDeletes, Set tempLocalBuildDeletes, UserCommunicationInterface userComm) {
        super("Clean Process Artifacts", 5, userComm); 
        cleanablesThatShouldHaveDriversUnlocked = tempDriverUnlocks;
        cleanablesThatShouldHaveJobsPurged = tempJobsToPurge;
        cleanablesThatShouldHaveMVSFilesDeleted = tempMVSBuildDeletes;
        cleanablesThatShouldHaveLocalFilesDeleted = tempLocalBuildDeletes;
    }

    protected ProcessStep getProcessStep(int stepToGet, int stepIteration) {
        switch (stepToGet) {
        case 0:
            DriverControl unlockDriversStep = new DriverControl(cleanablesThatShouldHaveDriversUnlocked, MBGlobals.Build390_path+"misc"+java.io.File.separator, DriverControl.UNLOCKDRIVER, this);
            unlockDriversStep.ignoreHostErrors(true);
            return unlockDriversStep;
        case 1:
            PurgeJobOutput purgeJobOutputStep = new PurgeJobOutput(cleanablesThatShouldHaveJobsPurged, MBGlobals.Build390_path+"misc"+java.io.File.separator, this);
            return purgeJobOutputStep;
        case 2:
            DeleteFasttrackMVSFiles manualMVSFilesDeleteStep = new DeleteFasttrackMVSFiles(cleanablesThatShouldHaveMVSFilesDeleted, this);
            return manualMVSFilesDeleteStep;
        case 3:
            DeleteMVSBuildArtifacts deleteMVSBuildArtifactsStep = new DeleteMVSBuildArtifacts(cleanablesThatShouldHaveMVSFilesDeleted, this);
            return deleteMVSBuildArtifactsStep;
        case 4:
            DeleteLocalArtifacts deleteLocalBuildArtifacts = new DeleteLocalArtifacts(cleanablesThatShouldHaveLocalFilesDeleted, this);
            return deleteLocalBuildArtifacts;
        }
        return null;
    }
}
