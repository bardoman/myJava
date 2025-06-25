package com.ibm.sdwb.build390.process.steps;

import com.ibm.sdwb.build390.*;
import com.ibm.sdwb.build390.logprocess.LogEventProcessor;
import com.ibm.sdwb.build390.library.*;
import java.util.*;

public class ManipulateLevelsInCMVC extends ProcessStep {
    static final long serialVersionUID = 1111111111111111L;

    private Changeset track = null;
    private Set levelsToWorkWith = null;
    private boolean doAdd = false;
    private boolean doRemove = false;

    public ManipulateLevelsInCMVC(Set tempLevels, Changeset tempTrack, com.ibm.sdwb.build390.process.AbstractProcess tempProc) {
        super(tempProc,"Manipulate Levels In CMVC");
        setVisibleToUser(false);
        setUndoBeforeRerun(false);
        track = tempTrack;
        levelsToWorkWith = tempLevels;
    }

    public void setAddMode(){
        doAdd = true;
        doRemove = false;
    }

    public void setRemoveMode(){
        doRemove = true;
        doAdd = false;
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
        for (Iterator levelIterator = levelsToWorkWith.iterator(); levelIterator.hasNext();) {
            ChangesetGroup currentLevel = (ChangesetGroup) levelIterator.next();
            if (doRemove) {
                getStatusHandler().updateStatus("Removing track " + track.getName() + " from level " +currentLevel.getName()+ " in release " + currentLevel.getProject(),false);
                currentLevel.removeChangesetFromGroup(track);
                getStatusHandler().updateStatus("Removed track " + track.getName()  + " from level " +currentLevel.getName()+ " in release " + currentLevel.getProject(),false);
            } else if (doAdd) {
                getStatusHandler().updateStatus("Adding track " + track.getName() + " to level "   +currentLevel.getName()+ " in release " + currentLevel.getProject(),false);
                currentLevel.addChangesetToGroup(track);
                getStatusHandler().updateStatus("Added track "  + track.getName() + " to level "   +currentLevel.getName()+ " in release " + currentLevel.getProject(),false);
            } else {
                throw new GeneralError("Remove or Add not selected");
            }
        }
    }

    public void undoProcess() throws com.ibm.sdwb.build390.MBBuildException{
        for (Iterator levelIterator = levelsToWorkWith.iterator(); levelIterator.hasNext();) {
            ChangesetGroup currentLevel = (ChangesetGroup) levelIterator.next();
            if (doAdd) {
                getStatusHandler().updateStatus("Removing track " + track.getName() + " from level " +currentLevel.getName()+ " in release " + currentLevel.getProject(),false);
                currentLevel.removeChangesetFromGroup(track);
                getStatusHandler().updateStatus("Removed track " + track.getName()  + " from level " +currentLevel.getName()+ " in release " + currentLevel.getProject(),false);
            } else if (doRemove) {
                getStatusHandler().updateStatus("Adding track " + track.getName() + " to level "   +currentLevel.getName()+ " in release " + currentLevel.getProject(),false);
                currentLevel.addChangesetToGroup(track);
                getStatusHandler().updateStatus("Added track "  + track.getName() + " to level "   +currentLevel.getName()+ " in release " + currentLevel.getProject(),false);
            }
        }
    }
}

