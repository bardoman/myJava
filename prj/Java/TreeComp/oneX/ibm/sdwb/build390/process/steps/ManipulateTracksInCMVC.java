package com.ibm.sdwb.build390.process.steps;

import com.ibm.sdwb.build390.*;	 
import com.ibm.sdwb.build390.library.*;
import com.ibm.sdwb.build390.logprocess.LogEventProcessor;
import java.util.*;

public class ManipulateTracksInCMVC extends ProcessStep {
    static final long serialVersionUID = 1111111111111111L;

    private ChangesetGroup level = null;
    private Set tracksToWorkWith = null;
    private boolean doAdd = false;
    private boolean doRemove = false;

    public ManipulateTracksInCMVC(Set tempTracks, ChangesetGroup tempLevel, com.ibm.sdwb.build390.process.AbstractProcess tempProc) {
        super(tempProc,"Manipulate changesets in library");
        setVisibleToUser(false);
        setUndoBeforeRerun(false);
        level = tempLevel;
        tracksToWorkWith = tempTracks;
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
        for (Iterator trackIterator = tracksToWorkWith.iterator(); trackIterator.hasNext();) {
            Changeset currentTrack = (Changeset) trackIterator.next();
            if (doRemove) {
                getStatusHandler().updateStatus("Removing track " + currentTrack.getName() + " from level " +level.getName()+ " in release " + level.getProject(),false);
                level.removeChangesetFromGroup(currentTrack);
                getStatusHandler().updateStatus("Removed track " + currentTrack.getName() + " from level " +level.getName()+ " in release " + level.getProject(),false);
            } else if (doAdd) {
                getStatusHandler().updateStatus("Adding track " + currentTrack.getName() + " to level " +level.getName()+ " in release " + level.getProject(),false);
                level.addChangesetToGroup(currentTrack);
                getStatusHandler().updateStatus("Added track " + currentTrack.getName() + " to level " +level.getName()+ " in release " + level.getProject(),false);
            } else {
                throw new GeneralError("Remove or Add not selected");
            }
        }
    }

    public void undoProcess() throws com.ibm.sdwb.build390.MBBuildException{
        for (Iterator trackIterator = tracksToWorkWith.iterator(); trackIterator.hasNext();) {
            Changeset currentTrack = (Changeset) trackIterator.next();
            if (doAdd) {
                getStatusHandler().updateStatus("Removing track " + currentTrack.getName() + " from level " +level.getName()+ " in release " + level.getProject(),false);
                level.removeChangesetFromGroup(currentTrack);
                getStatusHandler().updateStatus("Removed track " + currentTrack.getName() + " from level " +level.getName()+ " in release " + level.getProject(),false);
            } else if (doRemove) {
                getStatusHandler().updateStatus("Adding track " + currentTrack.getName() + " to level " +level.getName()+ " in release " + level.getProject(),false);
                level.addChangesetToGroup(currentTrack);
                getStatusHandler().updateStatus("Added track " + currentTrack.getName() + " to level " +level.getName()+ " in release " + level.getProject(),false);
            }
        }
    }
}
