package com.ibm.sdwb.build390.process.steps;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.StringTokenizer;

import com.ibm.sdwb.build390.HostError;
import com.ibm.sdwb.build390.MBGlobals;
import com.ibm.sdwb.build390.MBMainframeInfo;
import com.ibm.sdwb.build390.library.LibraryInfo;
import com.ibm.sdwb.build390.mainframe.DriverInformation;
import com.ibm.sdwb.build390.mainframe.ReleaseInformation;
import com.ibm.sdwb.build390.process.AbstractProcess;
import com.ibm.sdwb.build390.user.SetupManager;

//******************************************************************************************
/**#DEFTST1570 usermod driver after its merged shouldnt show up as DELTA in manage page. **/
/**#DEFTST1714 cleanup causes nullpointer. when run on a clean B390 installation from commandline. **/
//01/12/2004 #DEF.PTM3264: Stack overflow due to driver base chain recursion
//******************************************************************************************

public class MVSReleaseAndDriversList extends MainframeCommunication {
    static final long serialVersionUID = 1111111111111111L;

    private static final String OUTPUTFILENAME = "ReleaseAndDriverReport";
    private static final String CMVCRELEASELINE = "CMVCREL:";
    private static final String MVSHLQLINE = "MVS HLQ:";
    private static final String MVSRELLINE = "MVS REL:";
    private static final String DRIVERSLINE = "DRIVERS:";
    private static final String FULLDRIVERSTRING = "FULL";

    private LibraryInfo libInfo = null;
    private MBMainframeInfo mainInfo = null;

    public MVSReleaseAndDriversList(MBMainframeInfo tempMain, LibraryInfo tempLib, File saveDirectory, AbstractProcess tempProc) {
        super(saveDirectory.getAbsolutePath()+File.separator+OUTPUTFILENAME+"-"+tempLib.getProcessServerName()+"-"+tempLib.getProcessServerAddress(),"MVS Release and Driver List", tempProc);
        setVisibleToUser(true);
        setUndoBeforeRerun(false);
        libInfo = tempLib;
        mainInfo = tempMain;
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

        String releaseListReportCommand = "RELRPTJ DRIVERS=YES, "+libInfo.getDescriptiveStringForMVS();
        createMainframeCall(releaseListReportCommand, "Requesting release and driver list", mainInfo);
        runMainframeCall();
        try {
            BufferedReader reportReader = new BufferedReader(new FileReader(getOutputFile()));
            Set newReleaseSet = new HashSet();

            parseReleaseAndDriverListReport(reportReader, newReleaseSet);

            reportReader.close();

            updateOldReleaseSetWithNew(newReleaseSet, libInfo);

            ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(MBGlobals.Build390_path+"releasesAndDrivers.ser"));
            MBMainframeInfo.saveStaticInfoMap(oos);
            oos.close();

        }
        catch(IOException ioe) {
            throw new HostError("Reading release and driver report", ioe);
        }
    }

    //Begin PTM4447
    private  void updateOldReleaseSetWithNew(Set newReleaseSet, LibraryInfo libInfo) {
        Set oldReleaseSet =  SetupManager.getSetupManager().getCurrentMainframeInfo().getReleaseSet(libInfo);
        for (Iterator releaseIterator = (new HashSet(oldReleaseSet)).iterator(); releaseIterator.hasNext();) {
            ReleaseInformation oldReleaseInfo = (ReleaseInformation) releaseIterator.next();
            ReleaseInformation newReleaseInfo = getRelease(newReleaseSet, oldReleaseInfo);
            // if the release was just deleted, it won't show up in the new list
            if(newReleaseInfo != null) {
                // There is one danger here as far as drivers go.  If a driver is deleted, and recreated with different parameters, and releaselist never get's run
                for (Iterator driverIterator = (new HashSet(oldReleaseInfo.getDrivers())).iterator(); driverIterator.hasNext();) {
                    DriverInformation oldDriverInfo = (DriverInformation) driverIterator.next();
                    String driverName = oldDriverInfo.getName();
                    DriverInformation newDriverInfo = newReleaseInfo.getDriverByName(driverName);
                    // check to see if it has been deleted (isn't in the new list)
                    if(newDriverInfo == null) {
                        //it was deleted, nuke it
                        oldReleaseInfo.removeDriver(oldDriverInfo);
                    }else if(!isDriverInfosEquivalent(oldDriverInfo, newDriverInfo)) {
                        // if the two infos aren't equivalent, replace the old one with the new one.  They do NOT have to be equal.
                        oldReleaseInfo.removeDriver(oldDriverInfo);
                        oldReleaseInfo.addDriver(newDriverInfo);
                    }
                }

                for (Iterator driverIterator = new HashSet(newReleaseInfo.getDrivers()).iterator(); driverIterator.hasNext();) {
                    DriverInformation newDriverInfo =(DriverInformation)driverIterator.next();

                    String driverName = newDriverInfo.getName();
                    DriverInformation oldDriverInfo = oldReleaseInfo.getDriverByName(driverName);

                    // check to see if it has been added (isn't in the old list)
                    if(oldDriverInfo == null) {
                        //it was added, add to old list
                        oldReleaseInfo.addDriver(newDriverInfo);
                    }
                }
            }
            else {
                // the release was deleted, nuke it from the list
                // test to make sure this nukes deleted releases from the release list
                oldReleaseSet.remove(oldReleaseInfo); 
            }
        }

        ReleaseInformation newReleases[] = new ReleaseInformation[newReleaseSet.size()];

        newReleases = (ReleaseInformation []) newReleaseSet.toArray(newReleases);

        // we can't really test for equality here, so we have to do it manually

        for(int i=0;i!=newReleases.length;i++) {
            ReleaseInformation newReleaseInfo =  newReleases[i];

            ReleaseInformation oldReleaseInfo = getRelease(oldReleaseSet, newReleaseInfo);
            // if the release was just created, it won't show up in the old list
            if(oldReleaseInfo == null) {
                // not found, so add it to the old list
                // test by making sure a newly created release gets added to the list
                oldReleaseSet.add(newReleaseInfo);
            }
        }
    }

    private ReleaseInformation getRelease(Set releaseSet, ReleaseInformation releaseInfo) {
        for(Iterator releaseIterator = releaseSet.iterator();releaseIterator.hasNext();) {

            ReleaseInformation tempInfo = (ReleaseInformation) releaseIterator.next();

            if(releaseInfo.getLibraryName().equals(tempInfo.getLibraryName())  & releaseInfo.getMvsHighLevelQualifier().equals(tempInfo.getMvsHighLevelQualifier()) & releaseInfo.getMvsName().equals(tempInfo.getMvsName())) {
                return tempInfo;
            }
        }
        return null;
    }

    private boolean isDriverInfosEquivalent(DriverInformation info1, DriverInformation info2) {
        // this is because the driver could have been deleted and recreated with a delta size change 
        if(!(info1.isFullDriver()==info2.isFullDriver())) {
            return false;
        }
        // this is because the driver could have been updated with a new base chain
        if(info1.getExplicitBaseChain()!=null) {
            if(info2.getExplicitBaseChain()==null) {
                return false;
            }else if(!info1.getExplicitBaseChain().equals(info2.getExplicitBaseChain())) {
                return false;
            }
        }else if(info2.getExplicitBaseChain()!=null) {
            return false;
        }
        // driver could have been deleted and recreated with a new base, or merged changing from a delta to a base driver
        if(info1.getBaseDriver()!=null) {
            if(info2.getBaseDriver()==null) {
                return false;
            }else if(!info1.getBaseDriver().getName().equals(info2.getBaseDriver().getName())) {
                return false;
            }
        }else if(info2.getBaseDriver()!=null) {
            return false;
        }
        // we won't test buildtype status, since buildtypes aren't shown in this report, so mismatches here are not important
        return true;
    }


    private void parseReleaseAndDriverListReport(BufferedReader reportSource, Set releaseSet) throws IOException{
        String currentStanza = null;
        Map driverInfoMap = new HashMap();
        while((currentStanza=getNextStanza(reportSource))!=null) {
            /*scan for a line with
              CMVCREL: we do that because we parse the same file for all the releases.
              So whatever comes after that would have
              MVS HLQ: CLRTEST
              MVS REL: ODEREL
              DRIVERS:
                    ODEDRV
                    TESTBASE

            */
            BufferedReader stanzaReader = new BufferedReader(new StringReader(currentStanza));
            parseStanza(stanzaReader, driverInfoMap, releaseSet);
        }
    }

    private String getNextStanza(BufferedReader reportSource) throws IOException{
        String returnStanza = null;
        while(true) {
            String currentLine = reportSource.readLine();
            if(currentLine!=null) {
                if((currentLine.trim().length() < 1) & returnStanza!=null) {
                    // a blank line separates the stanzas, so quit
                    return returnStanza;
                }
                if(currentLine.indexOf(CMVCRELEASELINE)>-1) {
                    returnStanza = new String();
                }
                if(returnStanza!=null) {
                    returnStanza += currentLine+"\n";
                }
            }
            else {
                return returnStanza;
            }
        }
    }

    private void parseStanza(BufferedReader stanzaSource, Map driverInfoMap, Set releaseSet) throws IOException{
        // first 3 lines should be cmvc name, mvs name, and mvs hlq
        String cmvcName = stanzaSource.readLine().trim().substring(CMVCRELEASELINE.length()).trim();
        String mvsHlq = stanzaSource.readLine().trim().substring(MVSHLQLINE.length()).trim();
        String mvsName = stanzaSource.readLine().trim().substring(MVSRELLINE.length()).trim();
        ReleaseInformation thisRelease = null;
        for(Iterator releaseIterator = releaseSet.iterator(); releaseIterator.hasNext(); ) {
            ReleaseInformation oneInfo = (ReleaseInformation) releaseIterator.next();
            if(oneInfo.getLibraryName().equals(cmvcName)) {
                thisRelease = oneInfo;
            }
        }
        if(thisRelease == null) {
            thisRelease = new ReleaseInformation(cmvcName, mvsName, mvsHlq);
            releaseSet.add(thisRelease);
        }
        String currentLine = null;
        while((currentLine=stanzaSource.readLine())!=null) {
            currentLine = currentLine.trim();
            if(!currentLine.equals(DRIVERSLINE)) {
                StringTokenizer driverLineTokenizer = new StringTokenizer(currentLine);
                String driverName = driverLineTokenizer.nextToken();
                String baseDriver = null;
                if(driverLineTokenizer.hasMoreTokens()) {
                    baseDriver = driverLineTokenizer.nextToken();
                }
                boolean isFull = false;
                if(driverLineTokenizer.hasMoreTokens()) {
                    isFull = driverLineTokenizer.nextToken().equals(FULLDRIVERSTRING);
                }
                DriverInformation oneDriver = getDriverObject(createUniqueDriverIdentifier(driverName, thisRelease), driverInfoMap, thisRelease);
                oneDriver.setFull(isFull);
                oneDriver.setReleaseInfomation(thisRelease);
                thisRelease.addDriver(oneDriver);
                if(currentLine.endsWith("+")) {
                    oneDriver.setExplictBaseChain(new ArrayList());
                    parseExplicitBaseChain(oneDriver,baseDriver, stanzaSource, driverInfoMap);
                }
                else if(baseDriver!=null) {
                    oneDriver.setBaseDriver(getDriverObject(createUniqueDriverIdentifier(baseDriver,thisRelease), driverInfoMap, thisRelease));
                }
                else {
                    /**#DEFTST1570 this basically removes any old basedriverobject, if the driver got merged into BASE, from a DELTA driver
                    I verified that this change doesnt cause TST1575. :)
                    **/
                    if(baseDriver==null) {
                        oneDriver.setBaseDriver(null);
                    }
                }

            }
        }
    }

    private void parseExplicitBaseChain(DriverInformation driverInfo, String currentBase, BufferedReader chainSource, Map driverInfoMap) throws IOException{
        String chainContinuationMarker = "-";
        driverInfo.getExplicitBaseChain().add(getDriverObject(createUniqueDriverIdentifier(currentBase, driverInfo.getRelease()), driverInfoMap, driverInfo.getRelease()));
        chainSource.mark(500);
        String nextBaseLine = chainSource.readLine();
        if(nextBaseLine !=null) {
            StringTokenizer baseLineTokenizer = new StringTokenizer(nextBaseLine);
            if(chainContinuationMarker.equals(baseLineTokenizer.nextToken())) {
                // then the chain continues
                parseExplicitBaseChain(driverInfo,baseLineTokenizer.nextToken(),chainSource,driverInfoMap);
            }
            else {
                chainSource.reset();
            }
        }
    }

    private String createUniqueDriverIdentifier(String driverName, ReleaseInformation relInfo) {
        if(driverName.indexOf(".")>0) {
            return driverName;
        }
        else {
            return relInfo.getMvsName()+"."+driverName;
        }
    }

    private DriverInformation getDriverObject(String driverIdentifier, Map driverInfoMap, ReleaseInformation relInfo) {
        String driverName = driverIdentifier.substring(driverIdentifier.indexOf(".")+1);

        DriverInformation driverInfo = null;

        //Begin #DEF.PTM3264:
        String baseReleaseName = driverIdentifier.substring(0,driverIdentifier.indexOf(".")-1);

        if(baseReleaseName.equals(relInfo.getMvsName())) {
            if((driverInfo = relInfo.getDriverByName(driverName))!=null) {
                return driverInfo;
            }
        }
        //End #DEF.PTM3264:

        driverInfo = (DriverInformation) driverInfoMap.get(driverIdentifier);
        if(driverInfo == null) {
            driverInfo = new DriverInformation(driverName);
            driverInfoMap.put(driverIdentifier, driverInfo);
        }
        return driverInfo;
    }
}
