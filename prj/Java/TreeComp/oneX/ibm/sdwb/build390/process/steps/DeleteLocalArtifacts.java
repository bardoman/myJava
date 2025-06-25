package com.ibm.sdwb.build390.process.steps;

import com.ibm.sdwb.build390.*;
import com.ibm.sdwb.build390.process.management.CleanableEntity;
import java.util.*;
import java.io.File;

public class DeleteLocalArtifacts extends ProcessStep {
    static final long serialVersionUID = 1111111111111111L;

    private Set cleanablesToHandle = null;

    public DeleteLocalArtifacts(Set tempCleanables, com.ibm.sdwb.build390.process.AbstractProcess tempProc) {
        super(tempProc,"Delete local artifacts");
        setVisibleToUser(true);
        setUndoBeforeRerun(false);
        cleanablesToHandle = tempCleanables;
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
        for (Iterator cleanableIterator = cleanablesToHandle.iterator(); cleanableIterator.hasNext();) {
            CleanableEntity oneCleanable = (CleanableEntity) cleanableIterator.next();
            Set hasToBeRemovedFromCleanableEntity =  new HashSet();
            try {
                getStatusHandler().updateStatus("Deleting local files " ,false);
                for (Iterator fileAndDirectoryIterator = oneCleanable.getAllLocalFiles().iterator(); fileAndDirectoryIterator.hasNext();) {
                    File fileOrDirectory = (File) fileAndDirectoryIterator.next();
                    com.ibm.sdwb.build390.utilities.FileSystem.deleteDirectoryTree(fileOrDirectory);  // this will also handle files
                    /** oneCleanable.removeLocalFile(fileOrDirectory);  we are trying to delete stuff cyclically. */
                    hasToBeRemovedFromCleanableEntity.add(fileOrDirectory);
                } 
            } catch (Exception e) {
                throw (MBBuildException)e;
            } finally {
                for (Iterator deleteIterator = hasToBeRemovedFromCleanableEntity.iterator(); deleteIterator.hasNext();) {
                    oneCleanable.removeLocalFile((File)deleteIterator.next());
                }
            }
        }
    }
}
