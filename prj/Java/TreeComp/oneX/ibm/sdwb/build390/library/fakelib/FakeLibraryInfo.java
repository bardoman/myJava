package com.ibm.sdwb.build390.library.fakelib;
import java.io.File;
import java.io.Serializable;
import java.util.Formatter;
import java.util.Set;

import com.ibm.sdwb.build390.LibraryError;
import com.ibm.sdwb.build390.MBBuildException;
import com.ibm.sdwb.build390.MBClient;
import com.ibm.sdwb.build390.MBMainframeInfo;
import com.ibm.sdwb.build390.MBStatus;
import com.ibm.sdwb.build390.configuration.ConfigurationAccess;
import com.ibm.sdwb.build390.help.HelpLoaderInterface;
import com.ibm.sdwb.build390.library.*;
import com.ibm.sdwb.build390.library.fakelib.FakeLibraryHelpLoader;
import com.ibm.sdwb.build390.library.fakelib.userinterface.FakeLibraryUserinterfaceWidgetFactory;
import com.ibm.sdwb.build390.logprocess.LogEventProcessor;


public  class FakeLibraryInfo implements Serializable, Cloneable, LibraryInfo {

    static final long serialVersionUID = 1111111111111111L;

    private String processServerAddress = "NOLIB.BUILD390.IBM.COM";
    private int    processServerPort = 390;
    private String processServerName = null;


    private FakeLibraryUserinterfaceWidgetFactory widgetFactory = null;
    private transient HelpLoaderInterface helpLoader;
    private transient LogEventProcessor lep = null;
    private transient MBStatus status = null;

    /*  Constructor which initializes all the member variable settings
    */
    public FakeLibraryInfo() {
        widgetFactory = new FakeLibraryUserinterfaceWidgetFactory(this);
    }

    public FakeLibraryInfo(LibraryInfo oldInfo) {
        processServerAddress = oldInfo.getProcessServerAddress();
        processServerPort = oldInfo.getProcessServerPort();
        processServerName = oldInfo.getProcessServerName();
        widgetFactory = new FakeLibraryUserinterfaceWidgetFactory(this);
    }

    public void setLogEventProcessor(LogEventProcessor tempLep) {
        lep = tempLep;
    }

    public void setStatusHandler(MBStatus tempStatus) {
        status = tempStatus;
    }

    public String getProcessServerName() {
        return processServerName;
    }

    public String getProcessServerAddress() {
        return processServerAddress;
    }

    public int getProcessServerPort() {
        return processServerPort;
    }

    public VersionInfo getProcessServerVersion(boolean forceRefresh) {
        return null;
    }

    public void setProcessServerName(String newName) {
        processServerName = newName;
    }

    public void setProcessServerAddress(String newAddress) {
        processServerAddress = newAddress;
    }

    public void setProcessServerPort(int newPort) {
        processServerPort = newPort;
    }

    public String getDescriptiveString() {
        return processServerName;
    }


    public String getDescriptiveStringForMVS() {
        return "FAMILY=\'"+processServerName+"\', FAMADR=\'"+getAddressStringForMVS()+"\'";
    }

    public String getAddressStringForMVS() {
        if (processServerAddress!=null) {
            return processServerAddress +"@"+Integer.toString(getProcessServerPort());
        }
        return "";
    }


    public com.ibm.sdwb.build390.configuration.server.db2.ConfigurationRemoteServiceProvider getConfigurationServer() throws LibraryError{
        return null;
    }

    public boolean isFakeInfo() {
        return true;
    }

    public boolean isUsingPasswordAuthentication() {
        return false;
    }

    public String getAuthenticationKey() {
        return null;
    }

    public Set getProjectsInServiceMode() {
        throw new RuntimeException("Get projects not supported");
    }

    public Set getProjectsNotInServiceMode() {
        throw new RuntimeException("Get projects not supported");
    }

    public ChangesetGroup getChangesetGroup(String groupName, String projectName) {
        throw new RuntimeException("getChangesetGroup not supported");
    }

    public com.ibm.sdwb.build390.user.authorization.AuthorizationCheck getAuthorizationChecker() {
        return null;
    }

    public com.ibm.sdwb.build390.library.userinterface.UserinterfaceWidgetFactory getUserinterfaceFactory() {
        return widgetFactory;
    }

    public HelpLoaderInterface getHelpLoaderInterface() {
        try {
            if (helpLoader==null) {
                helpLoader = new FakeLibraryHelpLoader();
            }
        } catch (com.ibm.sdwb.build390.help.HelpException hpe) {
            getLEP().LogException(hpe);
        }
        return helpLoader;
    }

    public ConfigurationAccess getConfigurationAccess(String project, boolean cached) {
        return null;
    }


    public com.ibm.sdwb.build390.metadata.MetadataOperationsInterface getMetadataOperationsHandler() {
        return null;
    }

    public boolean isLibraryConnectionValid() {
        return true;
    }

    public boolean isValidLibraryProject(String project) throws MBBuildException{
        return true;
    }

    public void getBuildableObjects(String release, Set allowableLevelStates, Set allowableTrackStates, File outputFile) throws MBBuildException{
    }

    public void doCopy(com.ibm.sdwb.build390.info.FileInfo fileInfo, MBMainframeInfo mainInfo, String destinationName) throws LibraryError{
    }


    public java.util.Map getPrereqsAndCoreqs(Set changesetsToCheck, boolean includeStates, Set stateList) {
        throw new UnsupportedOperationException();
    }


    public MBStatus getStatusHandler() {
        if (status == null) {
            status = new MBStatus(null);
        }
        return status;
    }

    public LogEventProcessor getLEP() {
        if (lep == null) {
            lep = new LogEventProcessor();
            lep.addEventListener(MBClient.getGlobalLogFileListener());
            lep.addEventListener(MBClient.getGlobalLogGUIListener());
        }
        return lep;
    }

    public void handleUIEvent(com.ibm.sdwb.build390.userinterface.event.UserInterfaceEvent event) {
    }

    public String toString() {
        return getDescriptiveString();
    }

    public LibraryInfo cloneLibraryInfo() {
        try {
            return(LibraryInfo)clone();
        } catch (CloneNotSupportedException cnse) {
            throw new RuntimeException("Problem making clone of "+getClass().getName(), cnse);
        }
    }

    //The super.clone() doesn't work, since the userinterfaceFactory retains an old reference to the libraryOInfobject. 
    protected Object clone() throws CloneNotSupportedException {        
        FakeLibraryInfo info = new FakeLibraryInfo();
        info.setProcessServerName(getProcessServerName());
        info.setProcessServerAddress(getProcessServerAddress());
        info.setProcessServerPort(getProcessServerPort());
        return info;
    }

    /** show displays the fields of this object */
    public String toCompleteString() {
        StringBuilder buf = new StringBuilder();
        Formatter formatter = new Formatter(buf);
        formatter.format("%-29s=%s%n","LIBRARY","NOLIB");
        formatter.format("%-29s=%s%n","LIBRARYNAME",processServerName);
        formatter.format("%-29s=%s%n","LIBRARYADDRESS",processServerAddress);
        formatter.format("%-29s=%s%n","PROCESSSERVERPORT",processServerPort);
        return buf.toString();
    }
}
