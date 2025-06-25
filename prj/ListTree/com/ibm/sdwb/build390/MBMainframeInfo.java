package com.ibm.sdwb.build390;
/*********************************************************************/
/* Java MBMainframeInfo class for the Build390 java client                  */
/*********************************************************************/
/* Updates:                                                          */
// 01/18/01 Feature :SORT_SETUP_DLG  Sort the bps,mainframe,family fields in the combo boxes(implement Comparable)
// 01/30/01 Feature :SORT_SETUP_DLG  take the changes out since OE systems are not up to 1.3 java
/*********************************************************************/
import java.io.*;
import java.util.*;
import com.ibm.sdwb.build390.mainframe.*;
import com.ibm.sdwb.build390.library.LibraryInfo;

/** <br>MBMainframeInfo holds Mainframe information
*   <br>This is information tells which mainframe to use, its port,
*   <br>,the username to connect as, and the account info associated
*   <br>with that user.
*/
public class MBMainframeInfo implements Serializable, Cloneable {

    static final long serialVersionUID = 1111111111111111L;

    private String mainframeName = null;                // The internet address of the mainframe
    private String mainframePort = null;                // The port to connect to on the mainframe
    private String mainframeUsername = null;            // The username to connect as
    private String mainframeAccountInfo = null;         // The account info for the username
    private transient String password = null;
    private static Map serverInfoMap = new HashMap();


    public MBMainframeInfo() {
        if (serverInfoMap==null) {
            serverInfoMap = new HashMap();
        }
    }

    /*  Constructor which initializes all the member variable settings
    */
    public MBMainframeInfo(String mainName, String mainPort, String mainUsername, String mainAccountInfo) {
        mainframeName = new String(mainName);
        mainframePort = new String(mainPort);
        mainframeUsername = new String(mainUsername);
        mainframeAccountInfo = new String(mainAccountInfo);
        if (serverInfoMap==null) {
            serverInfoMap = new HashMap();
        }
    }

    // Getters
    /* Return the mainframe name
    */
    public String  getMainframeAddress() {
        return mainframeName;
    }

    /* Return the mainframe port
    */
    public String  getMainframePort() {
        return mainframePort;
    }

    /* Return the mainframe username
    */
    public String  getMainframeUsername() {
        return mainframeUsername;
    }

    /* Return the mainframe password
    */
    public String  getMainframePassword() {
        return password;
    }

    /* Return the mainframe account info
    */
    public String  getMainframeAccountInfo() {
        return mainframeAccountInfo;
    }

    public Set getReleaseSet(LibraryInfo libInfo) {
        Set releaseSet = null;
        synchronized (getThisServerMap()) {
            releaseSet = (Set) getThisServerMap().get(libInfo.getDescriptiveString());
            if (releaseSet==null) {
                releaseSet = new HashSet();
                setReleaseSet(releaseSet,libInfo);
            }
        }
        return releaseSet; 
    }


    public ReleaseInformation getReleaseByLibraryName(String libraryReleaseName, LibraryInfo libInfo) {
        ReleaseInformation  releaseInfo = null;
        Set releaseSet = getReleaseSet(libInfo);
        synchronized(releaseSet) {
            releaseInfo = libraryReleaseNameSearcher(libraryReleaseName, releaseSet);
        }

        return releaseInfo;
    }
/*
    private void refreshReleaseAndDriverList(MBLibraryInfo libInfo){
        com.ibm.sdwb.build390.logprocess.LogEventProcessor lep = new com.ibm.sdwb.build390.logprocess.LogEventProcessor();
        try {
            MBBuild tempBuild = new MBBuild(lep);
            // make sure you seit it here since this might have been a concurrency issue
            tempBuild.setSetup(mySetup);
            tempBuild.getSetup().addMainframeInfo(mainframeName, mainframePort, mainframeUsername, mainframeUsername);
            tempBuild.getSetup().SetMainframeInfo(tempBuild.getSetup().GetMainframeInfoVector().size()-1);
            tempBuild.getSetup().addFamilyInfo(libInfo.getLibraryName(), libInfo.getLibraryAddress(), libInfo.getLibraryCMVCPortAsString(), libInfo.getLibraryUsername(), libInfo.getLibraryRMIPortAsString(), libInfo.isUsingPasswordAuthentication());
            tempBuild.getSetup().SetFamilyInfo(tempBuild.getSetup().GetFamilyInfoVector().size()-1);
            com.ibm.sdwb.build390.process.MVSReleaseAndDriversList getReleaseList = new com.ibm.sdwb.build390.process.MVSReleaseAndDriversList(tempBuild,lep, new MBStatus(null));
            getReleaseList.externalRun();
        }catch (MBBuildException mbe){
            lep.LogException(mbe);
        }
    }
*/
    private ReleaseInformation libraryReleaseNameSearcher(String libraryName, Set releaseSet) {
        for (Iterator releaseIterator = releaseSet.iterator();releaseIterator.hasNext();) {
            ReleaseInformation currentRelease = (ReleaseInformation) releaseIterator.next();
            if (currentRelease.getLibraryName().equals(libraryName)) {
                return currentRelease;
            }
        }
        return null;
    }

    public ReleaseInformation getReleaseByMVSName(String mvsName, LibraryInfo libInfo) {
        ReleaseInformation  releaseInfo = null;
        Set releaseSet = getReleaseSet(libInfo);
        synchronized(releaseSet) {
            releaseInfo = mainframeReleaseNameSearcher(mvsName, releaseSet);
        }
        return releaseInfo;
    }

    private ReleaseInformation mainframeReleaseNameSearcher(String mainframeName, Set releaseSet) {
        for (Iterator releaseIterator = releaseSet.iterator();releaseIterator.hasNext();) {
            ReleaseInformation currentRelease = (ReleaseInformation) releaseIterator.next();
            if (currentRelease.getMvsName().equals(mainframeName)) {
                return currentRelease;
            }
        }
        return null;
    }

    // Setters
    /* Set the mainframe name
    */
    public void setMainframeAddress(String tempName) {
        mainframeName = tempName;
    }

    /* Set the mainframe port
    */
    public void setMainframePort(String tempName) {
        mainframePort = tempName;
    }

    /* Set the mainframe user name
    */
    public void setMainframeUsername(String tempName) {
        mainframeUsername = tempName;
    }

    /* Set the mainframe user name
    */
    public void setMainframePassword(String tempPassword) {
        password = tempPassword;
    }

    /* Set the mainframe account
    */
    public void setMainframeAccountInfo(String tempName) {
        mainframeAccountInfo = tempName;
    }

    public void addRelease(ReleaseInformation tempRelease, LibraryInfo libInfo) {
        synchronized(serverInfoMap) {
            if (getReleaseSet(libInfo)==null) {
                setReleaseSet(new HashSet(), libInfo);
            }
        }
        getReleaseSet(libInfo).add(tempRelease);
    }

    public void removeRelease(ReleaseInformation tempRelease, LibraryInfo libInfo) {
        for (Iterator iter = getReleaseSet(libInfo).iterator(); iter.hasNext();) {
            ReleaseInformation releaseInfo = (ReleaseInformation)iter.next();
            if (tempRelease.equals(releaseInfo)) {
                iter.remove();
                return;
            }
        }
    }


    public void setReleaseSet(Set releaseSet, LibraryInfo libInfo) {
        getThisServerMap().put(libInfo.getDescriptiveString(), releaseSet);
    }

    private Map getThisServerMap() {
        synchronized (serverInfoMap) {
            Map thisServerMap = (Map) serverInfoMap.get(getServerKey());
            if (thisServerMap ==null) {
                thisServerMap = new HashMap();
                serverInfoMap.put(getServerKey(), thisServerMap);
            }
            return thisServerMap;
        }
    }

    private String getServerKey() {
        return mainframeName+"@"+mainframePort;
    }


    public String toString() {
        return getMainframeAddress()+"@"+ getMainframePort() + "  " + getMainframeUsername();
    }

    public String toCompleteString() {
        StringBuilder buf = new StringBuilder();
        Formatter formatter = new Formatter(buf);
        formatter.format("%-29s=%s%n","BUILDSERVERNAME",mainframeName);
        formatter.format("%-29s=%s%n","BUILDSERVERPORT",mainframePort);
        formatter.format("%-29s=%s%n","BUILDSERVERUSERID",mainframeUsername);
        formatter.format("%-29s=%s%n","BUILDSERVERACCTINFO",mainframeAccountInfo);
        formatter.format("%2s%s%1s%s=%n","pw",mainframeUsername,"@",mainframeName);
        return buf.toString();
    }

    public static void saveStaticInfoMap(java.io.ObjectOutputStream outputStream) throws java.io.IOException {
        outputStream.writeObject(serverInfoMap);
    }

    public static void readStaticInfoMap(java.io.ObjectInputStream inputStream) throws java.io.IOException, ClassNotFoundException{
        serverInfoMap = (Map) inputStream.readObject();
    }

    public MBMainframeInfo cloneMainframeInfo(){
        MBMainframeInfo info = (MBMainframeInfo)clone();
        return info;

    }

    protected Object clone(){
        return new MBMainframeInfo(mainframeName, mainframePort, mainframeUsername, mainframeAccountInfo);
    }
}
