package com.ibm.sdwb.build390.process.steps;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.Vector;

import com.ibm.sdwb.build390.*;
import com.ibm.sdwb.build390.info.FileInfo;
import com.ibm.sdwb.build390.user.Setup;
import com.ibm.sdwb.build390.userinterface.graphic.MainInterface;

/* xx/xx/2004  PTM3367 purge jobs completed as soon as it completes */
/* xx/xx/2004  TST1792 PTF freeze and merge never take place */
/* 05/26/2004  PTM3492 TST1792 PTF freeze and merge never take place */

public class CheckJobStatus extends MainframeCommunication {
    static final long serialVersionUID = 1111111111111111L;

    private Setup setup = null;
    private int minimumFailureReturnCode = 1;
    private int phaseFailureReturnCode = 1;
    private Map failureReturnCodesForIndividualJobs = null;
    private Set jobsToHandle = null;
    private int completedJobsCount = 0;
    private int totalJobsCount = 0;
    private int successfulJobsPurged = 0;
    private String listGenSetting = null;
    private String listCopySetting = null;
    private String workPath = null;
    private MBBuild buildForFailureNotification = null;
    private int currentHostPhase = -1;
    private boolean sendNotificationEmail=true;
    private boolean sendPartOwnerNotificationEmail = false;
    private transient Thread jobThread = null;
    private transient MBFailedJobsListBox failedjobbox = null;
    private transient javax.swing.JInternalFrame parentFrame = null;

    private static final String JOBEXECUTING    = "EXECUTING.";
    private static final String JOBWAITING    = "EXECUTION.";
    private static final String JOBNOT          = "JOB NOT";
    private static final String LISTGEN_FAIL = "FAIL";
    private static final String LISTGEN_ALL  = "ALL";
    private static final String NOTIFICATIONSECTION="NOTIFICATION";
    private static final String BUILD_FAILURE_NOTIFICATION="BUILD_FAILURE_NOTIFICATION";
    private static final String NOTIFY_OWNER_OF_PART="NOTIFY_OWNER_OF_PART";

    public CheckJobStatus(Set tempJobsToHandle, int tempHostPhase, Setup tempSetup, String tempOutputPath, String jobSourceIdentifier, com.ibm.sdwb.build390.process.AbstractProcess tempProc) {
        super(tempOutputPath + "JOBSTATUS-"+jobSourceIdentifier, "Check Job Status", tempProc);
        setVisibleToUser(false);
        setUndoBeforeRerun(false);
        setRetryHandler(new RetryJobStatusCheckHandler());
        setup = tempSetup;
        currentHostPhase = tempHostPhase;
        jobsToHandle = tempJobsToHandle;
        totalJobsCount = jobsToHandle.size();
        failureReturnCodesForIndividualJobs = new HashMap();
    }


    /**
     * Specify the lowest return code that counts as a failure.
     * For instance, if you say 4, then 4 and everything
     * higher will count as a failure.  If you say 8, 
     * then 8 and higher fail.
     *  This is from the RC from client build.get_buildcc();
     * @param tempMinimumReturnCode
     */
    public void setMinimumFailureReturnCode(int tempMinimumReturnCode) {
        minimumFailureReturnCode = tempMinimumReturnCode;
    }

    public void setPhaseFailureReturnCode(int tempPhaseFailureReturnCode) {
        phaseFailureReturnCode =  tempPhaseFailureReturnCode;
    }

    public void setParentInternalFrame(javax.swing.JInternalFrame parentFrame) {
        this.parentFrame = parentFrame;
    }

    /**
     * If return codes have been specified for individual jobs
     * (as in job1 fails if it's above 4, but job2 only fails if
     * it's above 8) this is where you set the map of module
     * names to return codes.  DRVRBLD output can have this.
     * 
     * @param failureRCMap
     */
    public void setFailureReturnCodesForIndividualJobs(Map failureRCMap) {
        failureReturnCodesForIndividualJobs = failureRCMap;
    }

    public void setListCopy(String newListCopy) {
        listCopySetting = newListCopy;
    }

    public void setListGen(String newListGen) {
        listGenSetting = newListGen;
    }

    public void setWorkPath(String newWorkPath) {
        workPath = newWorkPath;
    }

    public Set getJobsHandled() {
        return new HashSet(jobsToHandle);
    }

    public Set getFailedJobs() {
        Set failedJobs = new HashSet();
        for (Iterator jobIterator = jobsToHandle.iterator(); jobIterator.hasNext();) {
            MBJobInfo jobInfo = (MBJobInfo) jobIterator.next();
            if (jobInfo.isComplete()&!jobInfo.isSuccessful()) {
                failedJobs.add(jobInfo);
            }
        }
        return new HashSet(failedJobs);
    }

    public Set getSuccessfulJobs() {
        Set successfulJobs = new HashSet();
        for (Iterator jobIterator = jobsToHandle.iterator(); jobIterator.hasNext();) {
            MBJobInfo jobInfo = (MBJobInfo) jobIterator.next();
            if (jobInfo.isComplete()&jobInfo.isSuccessful()) {
                successfulJobs.add(jobInfo);
            }
        }
        return new HashSet(successfulJobs);
    }

    public Set getUnfinishedJobs() {
        Set unfinishedJobs = new HashSet();
        for (Iterator jobIterator = jobsToHandle.iterator(); jobIterator.hasNext();) {
            MBJobInfo jobInfo = (MBJobInfo) jobIterator.next();
            if (!jobInfo.isComplete()) {
                unfinishedJobs.add(jobInfo);
            }
        }
        return new HashSet(unfinishedJobs);
    }

    public void setBuild(MBBuild tempBuild) throws LibraryError{
        buildForFailureNotification = tempBuild;
        if (!(tempBuild  instanceof MBUBuild)) {
            String str = buildForFailureNotification.getConfigInfo(NOTIFICATIONSECTION,BUILD_FAILURE_NOTIFICATION);
            getLEP().LogPrimaryInfo(getFullName() ,"the Notification string ="+str,false);
            if (str!=null) {
                if (str.trim().toUpperCase().equals("FALSE")) {
                    sendNotificationEmail=false;
                    getLEP().LogPrimaryInfo(getFullName() ,"the isConfiguredForNotificationByEmail ="+sendNotificationEmail,false);
                }
            }

            String str1 = buildForFailureNotification.getConfigInfo(NOTIFICATIONSECTION,NOTIFY_OWNER_OF_PART);
            if (str1!=null) {
                if (str1.trim().toUpperCase().equals("TRUE")) {
                    sendPartOwnerNotificationEmail=true;
                }
            }
        }
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
        String allJobsToCheck = new String();
        for (Iterator jobIterator = jobsToHandle.iterator(); jobIterator.hasNext();) {
            MBJobInfo oneJob = (MBJobInfo)jobIterator.next();
            allJobsToCheck += oneJob.getJobName();
            if (jobIterator.hasNext()) {
                allJobsToCheck += " ";
            }
        }
        for (boolean jobsExecuting = true; !stopped & jobsExecuting& (jobsToHandle.size() >0);) {
            jobsExecuting = false;
            createMainframeCall(allJobsToCheck, "Checking job Status", setup.getMainframeInfo());
            setJobstatus();
            runMainframeCall();
            getStatusHandler().updateStatus("Parsing job status file",false);
            MBJobStatusFileParser parsedJobStatus = new MBJobStatusFileParser(getOutputFile());
            for (Iterator jobIterator = jobsToHandle.iterator(); jobIterator.hasNext();) {
                MBJobInfo currentJob = (MBJobInfo) jobIterator.next();
                String currentJobStatus = parsedJobStatus.getJobStatus(currentJob.getJobName()).trim();
                currentJob.setJobStatus(currentJobStatus);
                if (currentJobStatus.endsWith(JOBEXECUTING) | currentJobStatus.endsWith(JOBWAITING)) {
                    jobsExecuting = true;
                } else {
                    getStatusHandler().updateStatus("Checking the Return code for "+currentJob.getJobName(),false);
                    handleCompletedJob(currentJob);
                    currentJob.setComplete();
                }
            }
            Set successfulSet = getSuccessfulJobs();

            if (buildForFailureNotification!=null) { /*TST1792 */ 
                if (buildForFailureNotification.getOptions().isAutoPurgeSuccessfulJobs()
                    && successfulSet.size() > 0) {
                    PurgeJobOutput jobPurgeStep = new PurgeJobOutput(getCleanableEntity(), buildForFailureNotification.getBuildPath(), buildForFailureNotification.getProcessForThisBuild());
                    jobPurgeStep.setSubsetOfAllJobsToPurge(successfulSet);
                    jobPurgeStep.externalExecute();
                    successfulJobsPurged += successfulSet.size();
                    trimJobsSet(successfulSet);
                }
            }

            completedJobsCount = successfulJobsPurged + getCompleteJobsCount(jobsToHandle); /*PTM3492 */

            if (jobsExecuting) {
                try {
                    getStatusHandler().updateStatus(completedJobsCount + " jobs of "+ totalJobsCount +" complete. [successful jobs purged =" + successfulJobsPurged +"]", false); /* PTM3367 */
                    int sleepTimeInSeconds = 20;
                    if (buildForFailureNotification != null) {
                        if (buildForFailureNotification instanceof com.ibm.sdwb.build390.MBUBuild) {
                            if (((MBUBuild) buildForFailureNotification).getFastTrack()) {
                                sleepTimeInSeconds = 4;
                            }
                        }
                    }
                    Thread.sleep(sleepTimeInSeconds*1000);
                } catch (InterruptedException ie) {
                    throw new GeneralError("Job status checking:Interrupted while waiting for jobs to complete", ie);
                }
            } else {
                getStatusHandler().updateStatus("All jobs complete", false);
            }
        }
    }

    private void handleCompletedJob(MBJobInfo jobInfo) {
        if (!jobInfo.isComplete()) {
            boolean jobFailed = false;
            if (jobInfo.getJobStatus().indexOf(JOBNOT) > -1) {
                jobFailed = true;
                jobInfo.setSucceeded(false);
                jobInfo.setJobReturnCode(-2);
            } else {
                String jobReturnCode = jobInfo.getJobStatus().substring(jobInfo.getJobStatus().lastIndexOf(" ")+1, jobInfo.getJobStatus().length()-1);

                /** this code is for system return codes. eg.
                * CLRHSPEC(JOB21166)  HIGHEST RETURN CODE WAS SYSTEM=013.
                */

                String systemReturnCode ="";
                boolean hasAlphaCharacters = false;
                for (int i=0;i<jobReturnCode.toCharArray().length;i++) {
                    if (Character.isDigit(jobReturnCode.toCharArray()[i])) {
                        systemReturnCode += jobReturnCode.toCharArray()[i];
                    } else {
                        hasAlphaCharacters=true;
                    }
                }    

                try {
                    jobReturnCode = hasAlphaCharacters ? systemReturnCode : jobReturnCode;    

                    jobInfo.setJobReturnCode(Integer.parseInt(jobReturnCode));

                    // Let BRC values override our defaults
                    Integer individualJobFailureCondition = null;
                    if (jobInfo.getfileMod()!=null) {
                        individualJobFailureCondition = (Integer) failureReturnCodesForIndividualJobs.get(jobInfo.getfileMod());
                    }
                    if (individualJobFailureCondition !=null) {
                        jobFailed = jobInfo.getJobReturnCode() >= individualJobFailureCondition.intValue();
                    } else {
                        if (phaseFailureReturnCode > -1) {
                            jobFailed = jobInfo.getJobReturnCode() >= phaseFailureReturnCode;
                        } else {
                            jobFailed = jobInfo.getJobReturnCode() >= minimumFailureReturnCode;

                        }
                    }

                    if (!jobFailed && hasAlphaCharacters) {
                        jobFailed = true;
                    }

                    if (jobFailed) {
                        jobInfo.setSucceeded(false);
                        getLEP().LogPrimaryInfo("INFORMATION:","JOBFAILED :"+ jobInfo.toString(),false);
                        //SDWB 1120 send email when a failedjob occurs
                        if (jobInfo.getfileMod()!=null) {
                            sendBuildFailureNotificationEmail(jobInfo);
                        }
                    } else {
                        jobInfo.setSucceeded(true);
                        getLEP().LogPrimaryInfo("INFORMATION:","JOBSUCCES :"+ jobInfo.toString(),false);
                    }
                } catch (NumberFormatException nfe) {
                    jobInfo.setSucceeded(false);
                    getLEP().LogPrimaryInfo("INFORMATION:","JOBFAILED :"+ jobInfo.toString(),false);
                }
            }

            if (jobFailed & MainInterface.getInterfaceSingleton() !=null & buildForFailureNotification!=null) {
                jobInfo.setComplete();
                if (!stopped) {
                    if (failedjobbox==null) {
                        failedjobbox = new MBFailedJobsListBox(buildForFailureNotification, currentHostPhase,  new Vector(getFailedJobs()), parentFrame, getLEP());
                        failedjobbox.setLSTCOPYAndWorkPathSetting(listCopySetting, workPath);
                        failedjobbox.setTotalJobs(jobsToHandle.size());
                        jobThread = new Thread(failedjobbox);
                        jobThread.start();
                    } else {
                        failedjobbox.updateJobs(new Vector(getFailedJobs()));
                    }
                }
            }

            if (jobFailed & MainInterface.getInterfaceSingleton() ==null & buildForFailureNotification!=null) {
                jobInfo.setComplete();
                if (!stopped & jobInfo.isComplete() & !jobInfo.isSuccessful()) {
                    getStatusHandler().updateStatus("Job failed:"+jobInfo.getJobName() + "|"+jobInfo.getJobStatus() + "|"+jobInfo.getfileName() + "|"+ jobInfo.fileVersion,false);
                }
            }
        }
    }

    private void sendBuildFailureNotificationEmail(MBJobInfo tempJob) {
        getLEP().LogPrimaryInfo(getFullName()+":sendBuildFailureNotificationEmail","Entry",false);
        if (buildForFailureNotification != null) {
            if (!(buildForFailureNotification  instanceof MBUBuild)) {
                boolean isEmailExists = false;
                FileInfo tempInfo = buildForFailureNotification.getFileInfo(tempJob.getfilePath().trim(), tempJob.getfileName().trim());
                if (tempInfo!=null) {
                    MBUtilities.InitMailToLists();
                    String dt = new String("Subject: "+ "Build/390 Failure: "+tempJob.getfileMod()+"."+tempJob.getfileClass() + " in  " + buildForFailureNotification.getReleaseInformation().getLibraryName() + "." +buildForFailureNotification.getDriverInformation().getName() +" , " + "BuildId="+buildForFailureNotification.get_buildid() +"\n"
                                           + "\nRelease                     : "+buildForFailureNotification.getReleaseInformation().getLibraryName()  
                                           + "\nDriver                      : " + buildForFailureNotification.getDriverInformation().getName() 
                                           + "\nBuild  ID                   : "+buildForFailureNotification.get_buildid() 
                                           + "\n____________________________________________________________"
                                           + "\n\nPart Failure Information                                            "
                                           + "\n____________________________________________________________"
                                           + "\nPARTNAME                    : "+tempJob.getfileName()
                                           + "\nPARTVERSION                 : "+tempJob.fileVersion
                                           + "\nPARTCLASS                   : "+tempJob.getfileClass()
                                           + "\nPARTMOD                     : "+tempJob.getfileMod()
                                           + "\nJOBNAME                     : "+tempJob.jobName
                                           + "\nFailed with RC              : "+(tempJob.getJobReturnCode() < 0 ? "<NO RC>" : String.valueOf(tempJob.getJobReturnCode()))); 
                    if (listGenSetting!=null) {//if LISTGEN overiden 
                        //if LISTGEN is fail or all then show listings
                        if (listGenSetting.equals(LISTGEN_FAIL)||
                            listGenSetting.equals(LISTGEN_ALL)) {
                            dt+= "\nFailed Job Listing Location : ";

                            if (listCopySetting !=null) {
                                // seq ds - LSTCOPY is set in the BLDORDER and returned to the client in phase results
                                String remotefile = "";
                                listCopySetting=listCopySetting.trim();
                                if (listCopySetting.endsWith("+")) {
                                    remotefile = listCopySetting.substring(0,listCopySetting.length()-1) + "." + tempJob.getfileClass() + "." + tempJob.getfileMod(); 
                                    // hfs path
                                } else if (listCopySetting.endsWith("/")) {
                                    //tempJob.fileName is actually the HFS partname
                                    //the so the listing location in HFS is 
                                    //workPath+directory+LSTCOPY location + partname
                                    if (workPath!=null) {
                                        remotefile +=workPath;
                                    }

                                    remotefile += listCopySetting ;
                                    if (tempInfo.getDirectory() !=null) {
                                        remotefile +=tempInfo.getDirectory();
                                    }
                                    remotefile += tempJob.getfileName();
                                    // pdse ds
                                } else {
                                    remotefile = listCopySetting + "(" + tempJob.getfileMod() + ")";
                                }
                                dt +=remotefile;
                            } else {
                                dt += "No Listing Location Available";
                            }  
                        }
                    }

                    dt+= "\n____________________________________________________________";

                    Vector RecipientEmailIDVector = new Vector();

                    if (sendNotificationEmail) {
                        String lastchangedid = tempInfo.getLastUpdaterEmail();
                        if (lastchangedid!=null) {
                            RecipientEmailIDVector.addElement(lastchangedid.trim());  

                        }
                    }

                    getLEP().LogPrimaryInfo("MBC_JOBOUTPUT:sendBuildFailureNotificationEmail ","dt ="+dt ,false);

                    if (sendPartOwnerNotificationEmail) {
                        String ownerid =tempInfo.getPartOwnerEmail();
                        if (ownerid!=null) {
                            RecipientEmailIDVector.addElement(ownerid.trim());  
                        }
                    }

                    if (RecipientEmailIDVector.size() > 0) {
                        isEmailExists=true ;
                    }

                    if (isEmailExists) {
                        getLEP().LogPrimaryInfo(getFullName(),"Sending Email to RecipientVector ="+RecipientEmailIDVector.toString() ,false);
                        MBUtilities.SendMail(dt, null, RecipientEmailIDVector);
                    }

                    if (!isEmailExists) {
                        if (sendNotificationEmail) {
                            new MBMsgBox("Build Failure Notification","Unable to find the email address in CMVC of the person who last changed the part " + MBConstants.NEWLINE +
                                         tempJob.getfileName() +"."+ tempJob.getfileClass() + MBConstants.NEWLINE +
                                         "Please notify the appropriate person who last changed the part ");
                        }
                    }
                }
            }
        }
    }

    private int getCompleteJobsCount(Set jobs) {
        int completeJobs = 0;
        for (Iterator jobIterator = jobs.iterator(); jobIterator.hasNext();) {
            MBJobInfo jobInfo = (MBJobInfo) jobIterator.next();
            if (jobInfo.isComplete()) {
                completeJobs++;
            }
        }
        return completeJobs;
    }


    private void trimJobsSet(Set successfulJobs) {
        for (Iterator jobIterator = jobsToHandle.iterator(); jobIterator.hasNext();) {
            MBJobInfo jobInfo = (MBJobInfo) jobIterator.next();
            if (successfulJobs.contains(jobInfo)) {
                jobIterator.remove();
            }
        }
    }


    private class RetryJobStatusCheckHandler implements com.ibm.sdwb.build390.mainframe.RetryHandler {

        static final long serialVersionUID = -8884516390456114172L;

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
