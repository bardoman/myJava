package com.ibm.sdwb.build390.process.steps.mainframe;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;

import com.ibm.sdwb.build390.GeneralError;
import com.ibm.sdwb.build390.MBConstants;
import com.ibm.sdwb.build390.library.LibraryInfo;
import com.ibm.sdwb.build390.process.steps.MainframeCommunication;
import com.ibm.sdwb.build390.retain.FmidFactory;
import com.ibm.sdwb.build390.user.Setup;

public class ListFMIDsForLibraryRelease extends MainframeCommunication {
    static final long serialVersionUID = 1111111111111111L;

    private Setup setup = null;
    private String libraryRelease = null;
    private String driver = null;
    private String retainRelease = null;
    private String retainCompID = null;
    private File outputDirectory = null;
    private boolean forceNewReport=true;

    public ListFMIDsForLibraryRelease(String tempRelease, Setup tempSetup, File tempOutputDirectory, com.ibm.sdwb.build390.process.AbstractProcess tempProc) {
        super(tempOutputDirectory.getAbsolutePath()+File.separator + "GETFID-"+tempRelease,"List FMIDs for Library release", tempProc);
        setVisibleToUser(true);
        setUndoBeforeRerun(false);
        libraryRelease = tempRelease;
        setup = tempSetup;
        outputDirectory = tempOutputDirectory;
    }

    public void setDriver(String temp) {
        driver = temp;
    }

    public void setRetainReleaseAndCompID(String tempRelease, String tempCompID) {
        retainRelease = tempRelease;
        retainCompID = tempCompID;
    }

    public void setForceNewReport(boolean tempForceNewReport) {
        forceNewReport = tempForceNewReport; 
    }


    /**
     * Sends back a map of key=PROXYFMID/value=REALFMID.
     * When a PROXY_FMID doesnot exists then it contains the
     * value of REAL_FMID.
     * ie. key=REALFMID/REALFMID.
     * 
     * It is parsed from the GETFID report.
     * 
     * 1.FID1=HBLD130 - this only have a real fmid.
     * 
     * 2.FID2=HBLDPROXY=HBLD130 - in this case HBLDPRXY is a proxyfmid.
     * and HBLD130 is a real fmid.
     * 
     * @return 
     */
    public Map getFMIDMap() {
        Map tempFMIDMap = null;
        LibraryInfo libInfo = setup.getLibraryInfo();
        if (retainRelease!=null) {
            tempFMIDMap =  FmidFactory.getInstance(getLEP()).get(retainRelease,retainCompID,libInfo);
        } else {
            tempFMIDMap =  FmidFactory.getInstance(getLEP()).get(libraryRelease,libInfo);
        }

        return tempFMIDMap;
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
        LibraryInfo libInfo = setup.getLibraryInfo();

        if (!forceNewReport) {
            if (getFMIDMap()!=null && !getFMIDMap().isEmpty()) {
                getStatusHandler().updateStatus("FMID from the cache is " + getFMIDMap().keySet().toString(),false);
                return;
            }
        }

        String getFMIDCommand = "GETFID CMVCREL="+libraryRelease+", "+libInfo.getDescriptiveStringForMVS();
        if (driver != null) {
            getFMIDCommand += ", DRIVER="+driver;
        }
        if (retainRelease!=null) {
            getFMIDCommand+=", RETREL="+retainRelease+", COMPID="+retainCompID;
            setOutputHeaderLocation(outputDirectory.getAbsolutePath()+"GETFID-"+libraryRelease+"-"+retainRelease+"-"+retainCompID);
        }
        createMainframeCall(getFMIDCommand, "Getting FMID information for library release "+ libraryRelease, setup.getMainframeInfo());
        runMainframeCall();

        Map tempFMIDMap = parse(getOutputFile());//parse file and getfmid 

        if (!tempFMIDMap.isEmpty()) {
            if (retainRelease!=null) {
                FmidFactory.getInstance(getLEP()).put(retainRelease,retainCompID,libInfo,tempFMIDMap);
            } else {
                FmidFactory.getInstance(getLEP()).put(libraryRelease,libInfo,tempFMIDMap);

            }
        }

    }


    private java.util.Map parse(File fileLocation) throws GeneralError{ 
        try {
            Map tempFMIDMap  = new HashMap();
            BufferedReader inputReader = new BufferedReader(new FileReader(fileLocation));
            String currentLine=null;
            while (((currentLine = inputReader.readLine())!=null)) {
                StringTokenizer equalTokenizer = new StringTokenizer(currentLine, "=");
                if (equalTokenizer.countTokens()>1) {
                    equalTokenizer.nextToken(); //ignore FID
                    String realFMID = null;
                    String proxyFMID = null;
                    if (equalTokenizer.countTokens() ==2) {                 /*count=2 means we have a proxyfmid */
                            proxyFMID =equalTokenizer.nextToken().trim();
                            realFMID  = equalTokenizer.nextToken().trim(); /* grab the realfmid */
                    } else { /*if its less than 2, then we have only the real fmid */
                        realFMID  = equalTokenizer.nextToken().trim();
                        proxyFMID  = realFMID; //no proxy fmid.

                    }
                    tempFMIDMap.put(proxyFMID, realFMID);//get type
                }
            }
            return tempFMIDMap;
        } catch (IOException ioe) {
            throw new GeneralError("Error reading "+fileLocation+MBConstants.CLEARFILEEXTENTION, ioe);
        }
    }
}
