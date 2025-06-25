package com.ibm.sdwb.build390.library.clearcase;

import java.io.*;
import java.util.*;

import com.ibm.rational.clearcase.*;
import com.ibm.sdwb.build390.*;
import com.ibm.sdwb.build390.configuration.ConfigurationAccess;
import com.ibm.sdwb.build390.help.HelpException;
import com.ibm.sdwb.build390.help.HelpLoaderInterface;
import com.ibm.sdwb.build390.library.*;
import com.ibm.sdwb.build390.library.clearcase.userinterface.ClearcaseUserinterfaceWidgetFactory;

/** <br>LibraryInfo holds a single set of information relating to a library.
*   <br>This is information tells what type of library to use, it's location,
*   <br>and the username to connect as.
*/
public class ClearcaseLibraryInfo extends AbstractLibraryInfo implements Serializable, Cloneable {


    static final long serialVersionUID = 1111111111111111L;

    private String projectVob = null;
    private ClearcaseUserinterfaceWidgetFactory widgetFactory = null;

    private transient HelpLoaderInterface helpLoader = null;


    /*  Constructor which initializes all the member variable settings
    */
    public ClearcaseLibraryInfo() {
        setRegistryServerInformation();
        widgetFactory = new ClearcaseUserinterfaceWidgetFactory(this);

    }

    public ClearcaseLibraryInfo(ClearcaseLibraryInfo oldInfo) {
        super(oldInfo);
        widgetFactory = new ClearcaseUserinterfaceWidgetFactory(this);
    }

    private void setRegistryServerInformation() {
        try {
            HostInfo hostInfo =  ClearToolAPI.getHostInfo();
            setProcessServerAddress(hostInfo.getRegistryHost());
            setProcessServerName(hostInfo.getRegistryRegion());
        } catch (CTAPIException cta) {
            throw new RuntimeException(cta);
        }
    }

    public void setProjectVob(String tempVob) {
        projectVob = tempVob;
    }

    public String getProjectVob() {
        return projectVob;
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

    public com.ibm.sdwb.build390.library.userinterface.UserinterfaceWidgetFactory getUserinterfaceFactory() {
        return widgetFactory;
    }

    public ConfigurationAccess getConfigurationAccess(String project, boolean cached) {
        return new RealtimeConfigAccess();  // Ken6.0
    }

    public com.ibm.sdwb.build390.metadata.MetadataOperationsInterface getMetadataOperationsHandler() {
        return null; // Ken6.0
    }

    public com.ibm.sdwb.build390.help.HelpLoaderInterface getHelpLoaderInterface() {
        try {
            if (helpLoader==null) {
                helpLoader = new ClearCaseHelpLoader();
            }
        } catch (HelpException hpe) {
            getLEP().LogException(hpe);
        }
        return helpLoader; 
    }


    public boolean isLibraryConnectionValid() {
        try {
            HostInfo hostInfo =  ClearToolAPI.getHostInfo();
        } catch (CTAPIException cta) {
            getLEP().LogException("Error connecting to library registry server", cta);
            return false;
        }
        return true;
    }

    public String getDescriptiveString() {
        return projectVob;
    }

    public boolean isValidLibraryProject(String project) throws MBBuildException{
        try {
            return ClearToolAPI.isValidProject(project, getProjectVob()); 
        } catch (CTAPIException cte) {
            throw new LibraryError("Could not find project " + project + " in PVOB " + getProjectVob(), cte);
        }
    }

    public void doCopy(com.ibm.sdwb.build390.info.FileInfo fileInfo, MBMainframeInfo mainInfo, String destinationName) throws LibraryError{
        throw new RuntimeException("In present implementation all Clearcase files should be accessed as local files, ClearcaseLibraryInfo.doCopy should not be called");
    }

    public java.util.Map getPrereqsAndCoreqs(Set changesetsToCheck, boolean includeStates, Set stateList) {
        throw new UnsupportedOperationException();
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
        ClearcaseLibraryInfo info = new ClearcaseLibraryInfo();
        info.setProcessServerName(getProcessServerName());
        info.setProcessServerAddress(getProcessServerAddress());
        info.setProcessServerPort(getProcessServerPort());
        info.setProjectVob(getProjectVob());
        return info;
    }

    /** show displays the fields of this object */
    public String toCompleteString() {
        String buf = super.toCompleteString();
        return buf;
    }

    public com.ibm.sdwb.build390.user.authorization.AuthorizationCheck getAuthorizationChecker() {
        return null;
        // Ken6.0 skip this
    }

    private class RealtimeConfigAccess implements Serializable, ConfigurationAccess {
        public java.util.Map getAllConfigurationSettings() {
            return new HashMap();
        }
        public String getProjectConfigurationSetting(String section, String keyword) throws com.ibm.sdwb.build390.LibraryError {
            return null;
        }
    }
}
