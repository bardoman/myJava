package com.ibm.sdwb.build390.library;

import java.io.*;
import java.util.*;
import java.rmi.*;
import com.ibm.sdwb.build390.*;
import com.ibm.sdwb.build390.info.FileInfo;
import com.ibm.sdwb.build390.library.userinterface.*;
import com.ibm.sdwb.build390.logprocess.LogEventProcessor;
import com.ibm.sdwb.build390.userinterface.UserCommunicationInterface;

/** <br>LibraryInfo holds a single set of information relating to a library.
*   <br>This is information tells what type of library to use, it's location,
*   <br>and the username to connect as.
*/
public interface LibraryInfo extends UserCommunicationInterface {

    public void setLogEventProcessor(LogEventProcessor tempLep);

    public void setStatusHandler(MBStatus tempStatus);

    public String getProcessServerName();

    public String getProcessServerAddress();

    public int getProcessServerPort();

    public VersionInfo getProcessServerVersion(boolean forceRefresh);

    public void setProcessServerName(String newName);

    public void setProcessServerAddress(String newAddress);

    public void setProcessServerPort(int newPort);

    public String getDescriptiveString();

    public String getDescriptiveStringForMVS();

    public String getAddressStringForMVS();

    public boolean isFakeInfo();

    public boolean isUsingPasswordAuthentication();

    public String getAuthenticationKey();

    public UserinterfaceWidgetFactory getUserinterfaceFactory();

    public com.ibm.sdwb.build390.configuration.ConfigurationAccess getConfigurationAccess(String project, boolean cacheValues);

    public boolean isLibraryConnectionValid();

    public boolean isValidLibraryProject(String project) throws MBBuildException;

    public Set getProjectsInServiceMode() throws MBBuildException;

    public Set getProjectsNotInServiceMode() throws MBBuildException;

    public ChangesetGroup getChangesetGroup(String groupName, String projectName);

    public void doCopy(FileInfo filename, MBMainframeInfo destinationInfo, String destinationName) throws LibraryError;

    public Map getPrereqsAndCoreqs(Set changesetsToCheck, boolean includeStates, Set stateList);

    public com.ibm.sdwb.build390.metadata.MetadataOperationsInterface getMetadataOperationsHandler();

    public com.ibm.sdwb.build390.configuration.server.db2.ConfigurationRemoteServiceProvider getConfigurationServer() throws LibraryError;

    public MBStatus getStatusHandler();

    public LogEventProcessor getLEP();

    public com.ibm.sdwb.build390.help.HelpLoaderInterface getHelpLoaderInterface();

    public com.ibm.sdwb.build390.user.authorization.AuthorizationCheck getAuthorizationChecker();

    public LibraryInfo cloneLibraryInfo();

    public String toCompleteString();

}
