package com.ibm.sdwb.build390.process.steps;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import com.ibm.sdwb.build390.MBJobInfo;
import com.ibm.sdwb.build390.process.management.CleanableEntity;
import com.ibm.sdwb.build390.user.Setup;

public class PurgeJobOutput extends MainframeCommunication {
    static final long serialVersionUID = 1111111111111111L;

    private Set cleanablesToHandle = null;
    private Set subsetOfAllJobsToPurgeHandle = null;

    public PurgeJobOutput(Set tempCleanablesToHandle, String tempOutputPath, com.ibm.sdwb.build390.process.AbstractProcess tempProc) {
        super(tempOutputPath+"jobspurge","Purging job output", tempProc);
        setVisibleToUser(false);
        setUndoBeforeRerun(false);
        setRetryHandler(new ReSubmitPurgeHandler());
        cleanablesToHandle = tempCleanablesToHandle;
    }

    public PurgeJobOutput(CleanableEntity tempCleanable, String tempOutputPath, com.ibm.sdwb.build390.process.AbstractProcess tempProc) {
        super(tempOutputPath+"jobspurge","Purging job output", tempProc);
        setVisibleToUser(false);
        setUndoBeforeRerun(false);
        cleanablesToHandle = new HashSet();
        cleanablesToHandle.add(tempCleanable);
    }

    public  void setSubsetOfAllJobsToPurge(Set tempSubsetOfAllJobsToPurge){
        subsetOfAllJobsToPurgeHandle = tempSubsetOfAllJobsToPurge;
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
        Map setupToCleanableMap = CleanableEntity.divideCleanablesBySetup(cleanablesToHandle);
        for (Iterator setupIterator = setupToCleanableMap.keySet().iterator(); setupIterator.hasNext();) {
            Setup setup = (Setup) setupIterator.next();
            Set cleanableSet = (Set) setupToCleanableMap.get(setup);
            handleOneSetup(setup,cleanableSet);
        }
    }

    private void handleOneSetup(Setup setup, Set cleanables) throws com.ibm.sdwb.build390.MBBuildException{
        String stringOfJobsToPurge = new String();
        for (Iterator cleanableIterator = cleanables.iterator(); cleanableIterator.hasNext();) {
            CleanableEntity oneCleanable = (CleanableEntity) cleanableIterator.next();
            for (Iterator jobIterator = oneCleanable.getAllHeldJobs().iterator(); jobIterator.hasNext();) {
                MBJobInfo oneJob = (MBJobInfo) jobIterator.next();
                if (isJobInJobSubset(oneJob, subsetOfAllJobsToPurgeHandle)) {
                    stringOfJobsToPurge += oneJob.getJobName();
                    if (jobIterator.hasNext()) {
                        stringOfJobsToPurge += " ";
                    }
                }
            }
        }
        if (stringOfJobsToPurge.length() >0) {
            createMainframeCall(stringOfJobsToPurge, "Purging job output", setup.getMainframeInfo());
            setDelsysout();
            unset_clrout();
            runMainframeCall();
        }
        for (Iterator cleanableIterator = cleanables.iterator(); cleanableIterator.hasNext(); ) {
            CleanableEntity oneCleanable = (CleanableEntity) cleanableIterator.next();
            if (subsetOfAllJobsToPurgeHandle!=null) {
                // just remove the ones in the subset
                oneCleanable.removeHeldJobs(subsetOfAllJobsToPurgeHandle);
            } else {
                oneCleanable.clearAllHeldJobs();
            }
        }
    }

    private boolean isJobInJobSubset(Object testJob, Set testSet){
        if (testSet==null) {
            return true;
        }
        return testSet.contains(testJob);
    }

    private class ReSubmitPurgeHandler implements com.ibm.sdwb.build390.mainframe.RetryHandler {

        public int getRetryCount() {
            return 5;
        }

        public int getSleepCountAfterRetry() {
            return 60 * 1000;
        }

        public boolean isRetryAppropriate() {
            return true;
        }
    }

}
