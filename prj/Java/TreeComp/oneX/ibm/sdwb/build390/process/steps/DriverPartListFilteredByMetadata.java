package com.ibm.sdwb.build390.process.steps;

import java.io.*;
import java.util.*;

import com.ibm.sdwb.build390.*;
import com.ibm.sdwb.build390.MBMainframeInfo;
import com.ibm.sdwb.build390.info.FileInfo;
import com.ibm.sdwb.build390.library.LibraryInfo;
import com.ibm.sdwb.build390.logprocess.LogEventProcessor;
import com.ibm.sdwb.build390.mainframe.DriverInformation;
import com.ibm.sdwb.build390.mainframe.ReleaseInformation;
import com.ibm.sdwb.build390.metadata.filter.MetadataCriteriaGenerator;
import com.ibm.sdwb.build390.metadata.utilities.*;
import com.ibm.sdwb.build390.process.*;

//******************************************************************
//07/10/2003 #DEF.TST1298: Metadata filter returns parts erroneously
//******************************************************************

public class DriverPartListFilteredByMetadata extends ProcessStep {
    static final long serialVersionUID = 1111111111111111L;

    private MBSocket mainframeConnection = null;
    private Collection filterCriteria = null;
    private String jobName = null;
    private String dataSetName = null;
    private List results = null;
    private String filterId;
    private String deleteMetadataHostPDSMember="";
    private static final String JOBEXECUTING    = "EXECUTING.";
    private static final String JOBWAITING      = "EXECUTION.";
    private static final String JOBNOT          = "JOB NOT";
    private static final String RETURNCODEWAS   = "HIGHEST RETURN CODE WAS ";
    private static final String JOBSPURGE       = "jobspurge";
    private Set<String> output = new HashSet<String>();
    private File localSavePath=null;
    private LibraryInfo libInfo = null;
    private MBMainframeInfo mainframeInfo=null;
    private ReleaseInformation relInfo = null;
    private DriverInformation driverInfo=null;

    public DriverPartListFilteredByMetadata(Collection tempCriteria,java.io.File tempLocalSavePath,MBMainframeInfo tempMainInfo,LibraryInfo tempLib, ReleaseInformation tempRelease, DriverInformation tempDriver, com.ibm.sdwb.build390.process.AbstractProcess tempProc){
        super(tempProc,"List Driver parts filtered by metadata");
        setVisibleToUser(false);
        setUndoBeforeRerun(false);
        localSavePath=tempLocalSavePath;
        filterCriteria = tempCriteria;
        mainframeInfo = tempMainInfo;
        libInfo=tempLib;
        relInfo=tempRelease;
        driverInfo=tempDriver;

    }

    public List getResults(){
        return results;
    }

    public void setMetadataFilterOrderFile(String hostMember){
        this.deleteMetadataHostPDSMember = hostMember;
    }

    public void setMetadataFilterId(String filterId){
        this.filterId = filterId;
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

         String clroutMETAEDFT = "";

        if (localSavePath==null) {
            clroutMETAEDFT = MBGlobals.Build390_path+"misc";
        }  else{
            clroutMETAEDFT = localSavePath.getAbsolutePath();
        }

        String cmd = "METAEDFT  "+libInfo.getDescriptiveStringForMVS()+", CMVCREL=\'"+ relInfo.getLibraryName()+"\', DRIVER="+ driverInfo.getName();


        cmd +=  MetadataCriteriaGenerator.generateCriteriaAsString(filterCriteria); /* the starting comma is appended in generateCriteriaAsString method */

        clroutMETAEDFT = clroutMETAEDFT.endsWith(File.separator) ? clroutMETAEDFT : (clroutMETAEDFT+File.separator);

        clroutMETAEDFT =  clroutMETAEDFT + "METAEDFT-"+relInfo.getLibraryName()+"-"+driverInfo.getName();

        localSavePath = new File(clroutMETAEDFT);

        if (filterId!=null) {
            cmd += ", FILTERID=" + filterId;
            clroutMETAEDFT +=  "-" +filterId;
        }

        cmd += ", PATHNAME=YES";

        // Submit the command
        mainframeConnection = new MBSocket(cmd, clroutMETAEDFT, "Returning part list by metadata edit filter", getStatusHandler(), mainframeInfo, getLEP());
        mainframeConnection.unset_clrout();
        mainframeConnection.run();
        try {
            parseFile(clroutMETAEDFT+MBConstants.PRINTFILEEXTENTION);
            boolean jobsExecuting = true;
            String clroutJobStatus  = localSavePath.getAbsolutePath() +  "-JOBSTATUS";
            while (!stopped & jobsExecuting) {
                jobsExecuting = false;

                mainframeConnection = new MBSocket(jobName, clroutJobStatus, "Checking metadata edit filter job ", getStatusHandler(), mainframeInfo, getLEP());
                mainframeConnection.setJobstatus();
                mainframeConnection.run();
                MBJobStatusFileParser parsedJobStatus = new MBJobStatusFileParser(new File(clroutJobStatus+MBConstants.CLEARFILEEXTENTION));
                Enumeration jobsReturned = parsedJobStatus.getJobs();
                while (!stopped & jobsReturned.hasMoreElements()) {
                    String currentJob = (String) jobsReturned.nextElement();
                    String currentStatus = parsedJobStatus.getJobStatus(currentJob).trim();
                    if (currentStatus.endsWith(JOBEXECUTING)) {
                        jobsExecuting = true;
                    }
                    if (currentStatus.endsWith(JOBWAITING)) {
                        jobsExecuting = true;
                    }
                    if (currentStatus.indexOf("HIGHEST RETURN CODE WAS UNKNOWN") != -1) {
                        jobsExecuting = true;
                    }
                }
                if (jobsExecuting) {
                    try {
                        getStatusHandler().updateStatus("Waiting for jobs to finish", false);
                        Thread.sleep(8*1000); 
                    } catch (InterruptedException ie) {
                        throw new GeneralError("Interrupted while waiting for jobs to complete", ie);
                    }
                }
            }

            MBJobStatusFileParser parsedJobStatus = new MBJobStatusFileParser(new File(clroutJobStatus+MBConstants.CLEARFILEEXTENTION));
            String stat = parsedJobStatus.getJobStatus(jobName); //HIGHEST RETURN CODE WAS 4.
            int jobReturnCode = -1;
            int idx = stat.indexOf(RETURNCODEWAS);
            int idx1 = stat.indexOf(".");
            if (idx > -1 & idx1 > -1) {
                jobReturnCode = Integer.parseInt(stat.substring(idx+24,idx1));
            }
            //Begin #HandRetCode99:  Need to handle return code 99
            if (jobReturnCode == 99) {

                MBMsgBox failedQuestion =   new MBMsgBox("Message", "No parts meet the user specified criteria.",null);

                MBMsgBox purgeQuestion =   new MBMsgBox("Message", "Do You Wish to purge the jobs?",null, true);

                if (purgeQuestion.isAnswerYes()) {
                    String clroutJOBPurge  = localSavePath.getAbsolutePath() + "-"  + JOBSPURGE;
                    mainframeConnection = new MBSocket(jobName, clroutJOBPurge,  "Meta data criteria search complete : Purging job output", getStatusHandler(), mainframeInfo, getLEP());
                    mainframeConnection.setDelsysout();
                    mainframeConnection.unset_clrout();
                    mainframeConnection.run();
                }
                getStatusHandler().updateStatus("Meta data criteria search complete.", false);
            }
            //End #HandRetCode99:  

            else if (jobReturnCode > 0) {
                getStatusHandler().updateStatus("Error occurred during metadata edit filter", false);
                MBMsgBox quitQuestion = new MBMsgBox("Error", "Received RC="+jobReturnCode+", x\'"+intToHex(jobReturnCode)+"\' from job "+jobName+" when running metadata edit filter report.Do you wish to view the job output?",null, true);
                if (quitQuestion.isAnswerYes()) {
                    Set jobSet = new HashSet();
                    jobSet.add(jobName);
                    ProcessWrapperForSingleStep wrapper = new ProcessWrapperForSingleStep(mainProcess);
                    HeldJobOutputRetrieval jobOut = new HeldJobOutputRetrieval(jobSet, mainframeInfo, localSavePath.getAbsolutePath(), mainProcess);
                    wrapper.setStep(jobOut);
                    jobOut.setShowFilesAfterRun(true, false);
                    wrapper.externalRun();
                    getStatusHandler().updateStatus("Error occurred during metadata edit filter", false);
                }
            } else if (jobReturnCode == 0) {
                getStatusHandler().updateStatus("Metadata edit filter successful", false);
                // Ken 9/21/99  job purge stuff
                String clroutJobSuccess = localSavePath + JOBSPURGE;
                mainframeConnection = new MBSocket(jobName, clroutJobSuccess,  "Metadata edit filter successful : Purging job output", getStatusHandler(), mainframeInfo, getLEP());
                mainframeConnection.setDelsysout();
                mainframeConnection.unset_clrout();
                mainframeConnection.run();
                // 9/24/99, pjs, reshow this msg, it gets wiped after jobspurge completes
                getStatusHandler().updateStatus("Metadata edit filter successful", false);

                // get the part list 
                String filterReport = clroutMETAEDFT+".rpt";

                MBFtp myFtp = new MBFtp(mainframeInfo,getLEP());

                if (!myFtp.get(dataSetName, filterReport, true)) {
                    throw new FtpError("Could not download metadata edit filter file "+dataSetName + " to " + filterReport);
                }

                getStatusHandler().updateStatus("Deleting " + dataSetName, false);
                myFtp = new MBFtp(mainframeInfo,getLEP());
                myFtp.delete(dataSetName);

                if (filterId!=null) {
                    myFtp = new MBFtp(mainframeInfo,getLEP());
                    getStatusHandler().updateStatus("Deleting " + deleteMetadataHostPDSMember, false);
                    myFtp.delete(deleteMetadataHostPDSMember);
                }

                getStatusHandler().updateStatus("Metadata edit filter successful, parsing results.", false);
                results = MetadataEditFieldsParser.parseFilterReport(filterReport);// parse the report to get part list vector
                for(Iterator iter = results.iterator();iter.hasNext();){
                    ((FileInfo)iter.next()).setProject(relInfo.getLibraryName());
                }
                getStatusHandler().updateStatus("Metadata edit filter successful, execution complete.", false);
                output.add(filterReport);
            }
        } catch (IOException ioe) {
            throw new GeneralError("An error occurred parsing the metadata edit filter print file.", ioe, new File(clroutMETAEDFT+MBConstants.PRINTFILEEXTENTION));
        }
    }

    private void parseFile(String fileName) throws IOException{
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


    public Set<String> getOutputReport(){
        return output;
    }

    private String intToHex(int bint) {
        String hx = new String();
        try {
            hx = Integer.toHexString(bint);
            // pad
            while (hx.length() < 4) {
                hx = "0"+hx;
            }
            // truncate
            if (hx.length() > 4) {
                hx = hx.substring(hx.length()-4);
            }
        } catch (Exception e) {
            hx = "0";
        }
        return hx;
    }


}                                                                
