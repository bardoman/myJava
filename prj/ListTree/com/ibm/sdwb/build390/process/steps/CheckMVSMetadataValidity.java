package com.ibm.sdwb.build390.process.steps;

import com.ibm.sdwb.build390.*;
import com.ibm.sdwb.build390.logprocess.LogEventProcessor;
import com.ibm.sdwb.build390.library.LibraryInfo;
import com.ibm.sdwb.build390.metadata.filter.MetadataCriteriaGenerator;
import java.util.*;
import java.io.*;

public class CheckMVSMetadataValidity extends MainframeCommunication {
    static final long serialVersionUID = 1111111111111111L;

    private MBBuild build = null;
    private Vector partSelectionCriteria = null;
    private MBInternalFrame parentFrame = null;
    private String jobName = null;
    private String dataSetName = null;
    private final String JOBEXECUTING    = "EXECUTING.";
    private final String JOBWAITING      = "EXECUTION.";
    private final String JOBNOT          = "JOB NOT";
    private final String RETURNCODEWAS   = "HIGHEST RETURN CODE WAS ";
    private final String JOBSPURGE          = new String("jobspurge");

    public CheckMVSMetadataValidity(MBBuild tempBuild, Vector tempPartSelectionCriteria, com.ibm.sdwb.build390.process.AbstractProcess tempProc) {
        super(MBGlobals.Build390_path+"misc"+File.separator+"METAVALIDATION-"+tempBuild.getReleaseInformation().getLibraryName()+"-"+tempBuild.getDriverInformation().getName(), "Check metadata validity", tempProc);
        setVisibleToUser(true);
        setUndoBeforeRerun(false);
        build = tempBuild;
        partSelectionCriteria = tempPartSelectionCriteria;
    }

    public void setParentFrame(MBInternalFrame tempFrame) {
        parentFrame = tempFrame;
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
        // Build the path to the output files
        LibraryInfo libInfo = build.getSetup().getLibraryInfo();
        String cmd = "XMETAVR  "+libInfo.getDescriptiveStringForMVS()+", CMVCREL=\'"+build.getReleaseInformation().getLibraryName()+"\', DRIVER="+build.getDriverInformation().getName();

        if (partSelectionCriteria!=null && !partSelectionCriteria.isEmpty()) {
            cmd +=  MetadataCriteriaGenerator.generateCriteriaAsString(partSelectionCriteria); /* the starting comma is appended in generateCriteriaAsString method */
        }

        createMainframeCall(cmd, "Validating metadata of selected parts", build.getSetup().getMainframeInfo());
        unset_clrout();
        runMainframeCall();
        try {
            parseFile(getPrintFile());
            boolean jobsExecuting = true;
            String clrout_ = MBGlobals.Build390_path+"misc"+File.separator+"METAVALIDATIONJOBSTATUS-"+build.getReleaseInformation().getLibraryName()+"-"+build.getDriverInformation().getName();
            while (!stopped & jobsExecuting) {
                jobsExecuting = false;

                MBSocket mainframeConnection = new MBSocket(jobName, clrout_, "Checking validation job status", getStatusHandler(), build.getSetup().getMainframeInfo(),getLEP());
                mainframeConnection.setJobstatus();
                mainframeConnection.run();
                MBJobStatusFileParser parsedJobStatus = new MBJobStatusFileParser(new File(clrout_+MBConstants.CLEARFILEEXTENTION));
                Enumeration jobsReturned = parsedJobStatus.getJobs();
                while (!stopped & jobsReturned.hasMoreElements()) {
                    String currentJob = (String) jobsReturned.nextElement();
                    String currentStatus = parsedJobStatus.getJobStatus(currentJob).trim();
                    if (currentStatus.endsWith(JOBEXECUTING) | currentStatus.endsWith(JOBWAITING)) {
                        jobsExecuting = true;
                    }
                }
                if (jobsExecuting) {
                    try {
                        getStatusHandler().updateStatus("Waiting for jobs to finish", false);
                        Thread.sleep(20*1000);
                    } catch (InterruptedException ie) {
                        throw new GeneralError("Interrupted while waiting for jobs to complete", ie);
                    }
                }
            }
            MBJobStatusFileParser parsedJobStatus = new MBJobStatusFileParser(new File(clrout_+MBConstants.CLEARFILEEXTENTION));
            String stat = parsedJobStatus.getJobStatus(jobName); //HIGHEST RETURN CODE WAS 4.
            int jobReturnCode = -1;
            int idx = stat.indexOf(RETURNCODEWAS);
            int idx1 = stat.indexOf(".");
            if (idx > -1 & idx1 > -1) {
                jobReturnCode = Integer.parseInt(stat.substring(idx+24,idx1));
            }
            if (jobReturnCode == -1) {
                throw new GeneralError("An error occurred parsing the metadata validation print file.", null, new File(clrout_+MBConstants.PRINTFILEEXTENTION));
            } else if (jobReturnCode == 36) {
                String validationReportPrefix  = MBGlobals.Build390_path+"misc"+File.separator+"METAVALIDATIONREPORT-"+build.getReleaseInformation().getLibraryName()+"-"+build.getDriverInformation().getName();
                String validationReport  = validationReportPrefix + MBConstants.PRINTFILEEXTENTION;
                MBFtp myFtp = new MBFtp(build.getSetup().getMainframeInfo(),getLEP());
                if (!myFtp.get(dataSetName, validationReport, true)) {
                    throw new FtpError("Could not download validation file "+dataSetName + " to " + validationReport);
                }

                if (parentFrame!=null) {
                    MBMsgBox failedQuestion =   new MBMsgBox("Error", "Meta data validation failed ,Do you wish to view the Results?",null, true);
                    if (failedQuestion.isAnswerYes()) {
                        new MBEdit(validationReport,getLEP());
                    } else {
                        new MBMsgBox("Information","The Failed Meta data report file is located in " + validationReport);
                    }

                    MBMsgBox purgeQuestion =   new MBMsgBox("Error", "Do You Wish to purge the jobs?",null, true);
                    if (purgeQuestion.isAnswerYes()) {
                        clrout_ = build.getBuildPath() + JOBSPURGE;
                        MBSocket mainframeConnection = new MBSocket(jobName, clrout_,  "Meta data validation failed : Purging job output", getStatusHandler(), build.getSetup().getMainframeInfo(), getLEP());
                        mainframeConnection.setDelsysout();
                        mainframeConnection.unset_clrout();
                        mainframeConnection.run();
                    }
                } else {
                    throw new HostError("Metadata validation failed.", validationReportPrefix);
                }
                getStatusHandler().updateStatus("Metadata validation failed", false);
            } else
                if (jobReturnCode == 99) {
                getStatusHandler().updateStatus("Metadata validation complete.", false);

                MBMsgBox failedQuestion =   new MBMsgBox("Message", "Metadata validation complete, No parts meet the user specified criteria.",null);

                if (parentFrame!=null) {

                    MBMsgBox purgeQuestion =   new MBMsgBox("Message", "Do You Wish to purge the jobs?",null, true);

                    if (purgeQuestion.isAnswerYes()) {
                        clrout_ = build.getBuildPath() + JOBSPURGE;
                        MBSocket mainframeConnection = new MBSocket(jobName, clrout_,  "Metadata validation complete : Purging job output", getStatusHandler(), build.getSetup().getMainframeInfo(), getLEP());
                        mainframeConnection.setDelsysout();
                        mainframeConnection.unset_clrout();
                        mainframeConnection.run();
                    }
                }
                getStatusHandler().updateStatus("Metadata validation complete.", false);
            } else if (jobReturnCode > 0) {
                getStatusHandler().updateStatus("Error occurred during metadata validation", false);

                MBMsgBox quitQuestion=null;

                if (parentFrame!=null) {
                    quitQuestion = new MBMsgBox("Error", "Received RC="+jobReturnCode+", x\'"+intToHex(jobReturnCode)+"\' from job "+jobName+" when running metadata validation report.Do you wish to view the job output?",null, true);

                    if (quitQuestion.isAnswerYes()) {
                        try {
                            Set jobsToGet = new HashSet();
                            jobsToGet.add(jobName);
                            com.ibm.sdwb.build390.process.ProcessWrapperForSingleStep wrapper = new com.ibm.sdwb.build390.process.ProcessWrapperForSingleStep(mainProcess);
                            com.ibm.sdwb.build390.process.steps.HeldJobOutputRetrieval jobOutputRetrieval = new com.ibm.sdwb.build390.process.steps.HeldJobOutputRetrieval(jobsToGet, build.getSetup().getMainframeInfo(), build.getBuildPath(), wrapper);
                            wrapper.setStep(jobOutputRetrieval);
                            jobOutputRetrieval.setShowFilesAfterRun(true, false);
                            wrapper.externalRun();
                        } catch (MBBuildException mbe) {
                            getLEP().LogException(mbe);
                        }
                    }
                    getStatusHandler().updateStatus("Error occurred during metadata validation", false);
                }
            } else if (jobReturnCode == 0) {
                getStatusHandler().updateStatus("Metadata validation successful", false);
                clrout_ = build.getBuildPath() + JOBSPURGE;
                MBSocket mainframeConnection = new MBSocket(jobName, clrout_,  "Meta data validation successful : Purging job output", getStatusHandler(), build.getSetup().getMainframeInfo(), getLEP());
                mainframeConnection.setDelsysout();
                mainframeConnection.unset_clrout();
                mainframeConnection.run();
                getStatusHandler().updateStatus("Metadata validation successful", false);
            }
        } catch (IOException ioe) {
            throw new GeneralError("An error occurred parsing the metadata validation print file.", ioe, getPrintFile());
        }
    }

    private void parseFile(File fileName)throws IOException{
        BufferedReader fileReader = new BufferedReader(new FileReader(fileName));
        String currLine = null;
        while ((currLine = fileReader.readLine())!=null) {
            if (currLine.startsWith("0")) {
                currLine = currLine.substring(1).trim();
                if (currLine.startsWith("JOB1")) {
                    StringTokenizer tempToke = new StringTokenizer(currLine, "=");
                    tempToke.nextToken();
                    jobName = tempToke.nextToken();
                } else if (currLine.startsWith("resultDSN")) {
                    StringTokenizer tempToke = new StringTokenizer(currLine, "=");
                    tempToke.nextToken();
                    dataSetName = tempToke.nextToken();
                }
            }
        }
    }

    public String intToHex(int bint) {
        String hx = new String();
        try {
            hx = Integer.toHexString(bint);
            while (hx.length() < 4) {
                hx = "0"+hx;
            }
            if (hx.length() > 4) {
                hx = hx.substring(hx.length()-4);
            }
        } catch (Exception e) {
            hx = "0";
        }
        return hx;
    }
}
