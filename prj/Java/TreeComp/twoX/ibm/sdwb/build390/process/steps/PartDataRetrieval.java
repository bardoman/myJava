package com.ibm.sdwb.build390.process.steps;

import com.ibm.sdwb.build390.*;
import com.ibm.sdwb.build390.logprocess.LogEventProcessor;
import com.ibm.sdwb.build390.library.LibraryInfo;
import com.ibm.sdwb.build390.info.InfoForMainframePartReportRetrieval;
import java.util.*;
import java.io.*;
import com.ibm.sdwb.build390.process.AbstractProcess;

//********************************************************************
//11/07/2003 #DEF.TST1682:  LOGRETRIEVE - Host error should be RC=5
//********************************************************************

public class PartDataRetrieval extends ProcessStep {
    static final long serialVersionUID = 1111111111111111L;

    private transient MBSocket mainframeCommunication = null;
    private MBBuild build = null;
    private String sendToAddress = null;
    private File localSavePath = null;
    private Set localSavedFiles = null;
    private String HFSSavePath = null;
    private String PDSSavePath = null;
    private String buildLevel = null;
    private Set partInfoSet = null;

    private String clrout = null;//TST2352

    private MBBuildException buildException=null;

    public PartDataRetrieval(MBBuild tempBuild, Set tempPartInfoMap, AbstractProcess tempProc) {
        super(tempProc,"Part Data Retrieval");
        setVisibleToUser(true);
        setUndoBeforeRerun(true);
        build = tempBuild;
        partInfoSet = tempPartInfoMap;
    }

    public void setLocalSavePath(File tempLocalSavePath) {
        localSavePath = tempLocalSavePath;
    }

    public void setSendToAddress(String tempAddress) {
        sendToAddress = tempAddress;
    }

    public void setHFSSavePath(String tempPath) {
        HFSSavePath = tempPath;
    }

    public void setPDSSavePath(String tempPath) {
        PDSSavePath = tempPath;
    }

    public void setBuildLevel(String tempBuildLevel) {
        buildLevel = tempBuildLevel;
    }

    public Set getLocalOutputFiles() {
        return localSavedFiles;
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

        final File commandOutputPath;
        if(localSavePath==null) {
            commandOutputPath = new File(MBGlobals.Build390_path+"logfiles"+File.separator);
        }
        else {
            commandOutputPath = localSavePath;
        }

        localSavedFiles=new HashSet();
        final Vector threadsRunning = new Vector();
        final LibraryInfo libInfo = build.getLibraryInfo();
        ThreadGroup logRetrieveTG = new ThreadGroup("Log Retrieve ThreadGroup");
        for(Iterator partIterator = partInfoSet.iterator(); partIterator.hasNext(); ) {
            final InfoForMainframePartReportRetrieval partInfo = (InfoForMainframePartReportRetrieval) partIterator.next();
            final String logType = partInfo.getReportType();
            Thread tempThread = new Thread(logRetrieveTG, new Runnable() {
                                               public void run() {
                                                   String clrout_ = commandOutputPath.getAbsolutePath()+File.separator + partInfo.getPartName()+"."+partInfo.getPartClass()+"-"+logType;//TST2352
                                                   try {
                                                       String cmd_ = "XLOGRTRV "+libInfo.getDescriptiveStringForMVS()+", CMVCREL='"+build.getReleaseInformation().getLibraryName()+"', DRIVER="+build.getDriverInformation().getName()+", MOD="+partInfo.getPartName()+", TYPE="+logType+", CLASS="+partInfo.getPartClass()+", REUSE='YES'";
                                                       if(buildLevel != null) {
                                                           cmd_ += ", BLDLVL="+buildLevel;
                                                       }
                                                       if(sendToAddress != null) {
                                                           cmd_ +=", SENDTO='"+sendToAddress+"'";
                                                       }
                                                       if(PDSSavePath != null) {
                                                           cmd_ +=", FQDSN='"+PDSSavePath+"'";
                                                       }
                                                       if(HFSSavePath != null) {
                                                           cmd_ +=", FQDSN='" +HFSSavePath+"'";
                                                       }

                                                       //Begin INT2395
                                                       if(logType.toUpperCase().startsWith("C")) {
                                                           String temp = logType.substring(1);

                                                           try {
                                                               int i = Integer.valueOf(temp).intValue();

                                                               if(partInfo.isBinary()==true) {
                                                                   cmd_ +=", BINARY=YES'";
                                                               }
                                                           }
                                                           catch(NumberFormatException nfe){}
                                                       }
                                                       //End INT2395

                                                       MBSocket mySock = new MBSocket(cmd_, clrout_, "Requesting part data" , getStatusHandler(), build.getSetup().getMainframeInfo(), getLEP());
                                                       mySock.run();

                                                       String dataSetName = null;
                                                       String localName = null;
                                                       BufferedReader din = new BufferedReader(new FileReader(clrout_+MBConstants.CLEARFILEEXTENTION));
                                                       String lineRead;
                                                       boolean binary = false;
                                                       String binKeyword = new String("BINARY");
                                                       while(((lineRead=din.readLine())!= null) & !stopped) {
                                                           lineRead = lineRead.trim();
                                                           if(lineRead.startsWith("DSN:")) {
                                                               StringTokenizer parseLine = new StringTokenizer(lineRead);
                                                               while(parseLine.hasMoreTokens()) {
                                                                   dataSetName = parseLine.nextToken();
                                                               }
                                                           }
                                                           int binIndex = 0;
                                                           if((binIndex = lineRead.toUpperCase().indexOf(binKeyword)) > -1) {
                                                               int startIndex = binIndex + binKeyword.length()+1;
                                                               String setting = lineRead.toUpperCase().substring(startIndex, startIndex + 2);
                                                               binary = setting.equals("ON");
                                                           }
                                                       }
                                                       if(partInfo.isBinary()) {
                                                           binary = true;
                                                       }

                                                       if(dataSetName != null & sendToAddress == null & localSavePath !=null) {
                                                           getStatusHandler().updateStatus("Transfering file  "+ dataSetName + " to " + localSavePath , false);
                                                           String dataSetNameDisplayed=dataSetName.substring(dataSetName.indexOf(".")+1, dataSetName.length());
                                                           StringBuffer strb = new StringBuffer(build.getDriverInformation().getName() + "."+dataSetNameDisplayed);
                                                           localName = localSavePath+File.separator +strb.toString();
                                                           MBFtp ftpObject = new MBFtp(build.getSetup().getMainframeInfo(),getLEP());
                                                           if(!ftpObject.get(dataSetName, localName, !binary)) {
                                                               throw new FtpError("Could not download file "+ dataSetName + " to file " + localName);
                                                           }
                                                           else {
                                                               /** our old ftpObject closed it conn. once it did the "get" thing. so instantiate a new obj **/
                                                               getStatusHandler().updateStatus("Deleting " + dataSetName,false);
                                                               MBFtp deleteDataSet = new MBFtp(build.getSetup().getMainframeInfo(),getLEP());
                                                               deleteDataSet.delete(dataSetName); /** PTM2666 delete the dataset after is successful **/
                                                           }
                                                           localSavedFiles.add(localName);
                                                       }
                                                   }
                                                   catch(MBBuildException mbe) {
                                                       //Begin #DEF.TST1682:
                                                       buildException = mbe;

                                                       clrout =clrout_;//TST2352

                                                       //getLEP().LogException(mbe);
                                                       //End #DEF.TST1682:
                                                   }
                                                   catch(IOException ioe) {
                                                       getLEP().LogException("There was an error while reading the server output file "+clrout_+MBConstants.CLEARFILEEXTENTION, ioe);
                                                   }
                                                   finally {
                                                       synchronized (threadsRunning) {
                                                           threadsRunning.remove(Thread.currentThread().getName());
                                                           threadsRunning.notify();
                                                       }
                                                   }
                                               }
                                           }, "LogRetrieve " + partInfo.getPartName() + " " + logType + " " + partInfo.getPartClass());
            threadsRunning.addElement(tempThread.getName());
            tempThread.start();
        }
        synchronized(threadsRunning) {
            boolean waitToEnd = threadsRunning.size() > 0;
            while(waitToEnd) {
                try {
                    threadsRunning.wait();
                }
                catch(InterruptedException ie) {
                }
                waitToEnd = threadsRunning.size() > 0;
            }
            //Begin #DEF.TST1682:
            if(buildException !=null) {
                //Begin TST2352
                if(buildException instanceof HostError) {
                    throw new HostError("Host Error Retrieving Part Data" ,clrout);
                }
                else {
                    throw new GeneralError(buildException.getMessage(),buildException);
                }
                //End TST2352
            }
            //End #DEF.TST1682:
        }
    }
}
