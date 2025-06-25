package com.ibm.sdwb.build390.library;

import java.io.*;
import java.util.*;
import java.rmi.*;
import com.ibm.sdwb.build390.*;
import com.ibm.sdwb.build390.library.userinterface.*;
import com.ibm.sdwb.build390.logprocess.LogEventProcessor;
import com.ibm.sdwb.build390.userinterface.UserCommunicationInterface;

/** <br>LibraryInfo holds a single set of information relating to a library.
*   <br>This is information tells what type of library to use, it's location,
*   <br>and the username to connect as.
*/
public abstract class AbstractLibraryInfo implements Serializable, Cloneable, LibraryInfo {
    static final long serialVersionUID = 1111111111111111L;

    private String processServerAddress = null;
    private int    processServerPort = -1;
    private String processServerName = null;
    private transient LogEventProcessor lep = null;
    private transient MBStatus status = null;
    private transient ProcessServerInterface generalServerHolder = null;
    private transient Remote libraryServerHolder = null;
    private transient Remote configurationServerHolder = null;
    private transient VersionRetriever processServerVersionRetriever = null;

    /*  Constructor which initializes all the member variable settings
    */
    public AbstractLibraryInfo() {
    }

    public AbstractLibraryInfo(AbstractLibraryInfo oldInfo) {
        processServerAddress = oldInfo.processServerAddress;
        processServerPort = oldInfo.processServerPort;
        processServerName = oldInfo.processServerName;
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
        try {
            if (processServerVersionRetriever == null) {
                processServerVersionRetriever = new VersionRetriever();
            }
            if (forceRefresh | !processServerVersionRetriever.hasVerified()) {
                processServerVersionRetriever.retrieveLocalVersion();
                processServerVersionRetriever.retrieveRemoteVersion(getGeneralServer());
                getLEP().LogPrimaryInfo("Build390 RMI Server Compatibility Check.",processServerVersionRetriever.getVersionInfo().compatibleOrNotMessage(),false); 
                processServerVersionRetriever.setVerified(true);
            }
            return processServerVersionRetriever.getVersionInfo();
        } catch (Exception le) {
            getLEP().LogException("Warning! RMI Server compatibility checking :\n" ,le);
        }
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

    public  boolean isFakeInfo() {
        return false; //by default its false.
    }

    public boolean isUsingPasswordAuthentication() {
        return false;
    }

    public String getAuthenticationKey() {
        return null; //by default its null
    }

    public String getDescriptiveString() {
        return processServerName+"@"+processServerAddress;
    }

    public String getDescriptiveStringForMVS() {
        return "FAMILY=\'"+processServerName+"\', FAMADR=\'"+getAddressStringForMVS()+"\'";
    }

    public String getAddressStringForMVS() {
        return processServerAddress;
    }

    protected ProcessServerInterface getGeneralServer() throws LibraryError{
        if (generalServerHolder == null) {
            generalServerHolder = (ProcessServerInterface) getRemoteServer(getProcessServerName().toLowerCase()+ProcessServer.RMINAMINGSEPARATOR+ProcessServer.GENERALSERVER);
        }
        return generalServerHolder;
    }

    protected Remote getLibraryServer() throws LibraryError{
        if (libraryServerHolder == null) {
            libraryServerHolder = getRemoteServer(getProcessServerName().toLowerCase()+ProcessServer.RMINAMINGSEPARATOR+ProcessServer.LIBRARYSERVER);
        }
        return libraryServerHolder;
    }

    public com.ibm.sdwb.build390.configuration.server.db2.ConfigurationRemoteServiceProvider getConfigurationServer() throws LibraryError{
        if (configurationServerHolder == null) {
            configurationServerHolder = getRemoteServer(getProcessServerName().toLowerCase()+ProcessServer.RMINAMINGSEPARATOR+ProcessServer.CONFIGSERVER);
        }
        return(com.ibm.sdwb.build390.configuration.server.db2.ConfigurationRemoteServiceProvider) configurationServerHolder;
    }

    private Remote getRemoteServer(String whichServer) throws LibraryError{
        String processServerURL = "rmi://"+getProcessServerAddress()+":"+getProcessServerPort()+"/"+whichServer;
        try {
            Remote processServerHolder = Naming.lookup(processServerURL);
            return processServerHolder;
        } catch (java.net.MalformedURLException e) {
            throw new LibraryError("The URL " + processServerURL + " is not formatted appropriately."+
                                   "\nAn bad URL error occurred while trying to lookup build390 rmi server \n" + getProcessServerAddress() + "@" + Integer.toString(getProcessServerPort()), e);
        } catch (NotBoundException nbe) {
            throw new LibraryError("The Build390 client and the Build390 RMI server are NOT compatible." +"\n" + "\nThe URL "+ processServerURL + " is not currently bound in the rmi registry(daemon) running in " +
                                   "server " + getProcessServerAddress() + "@" + Integer.toString(getProcessServerPort())+ ".\n"+
                                   "\nPlease contact your administrator for the correct library server information.\n", nbe);
        } catch (UnknownHostException uhe) {
            throw new LibraryError("The host "+getProcessServerAddress()+" was not found."+
                                   "\nThe IP address of a host  could not be determined.", uhe);
        } catch (RemoteException re) {
            throw new LibraryError("A remote error occurred while trying to find the " +
                                   "remote rmi service " + processServerURL , (Exception) re.detail);
        }
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

    /** show displays the fields of this object */
    public String toCompleteString() {
        StringBuilder buf = new StringBuilder();
        Formatter formatter = new Formatter(buf);
        formatter.format("%-29s=%s%n","LIBRARYNAME",processServerName);
        formatter.format("%-29s=%s%n","LIBRARYADDRESS",processServerAddress);
        formatter.format("%-29s=%s%n","PROCESSSERVERPORT",processServerPort);
        return buf.toString();
    }
}
