package com.ibm.sdwb.build390.configuration.server.db2;

import com.ibm.sdwb.build390.LibraryError;
import java.rmi.*;
import java.util.*;


public interface ConfigurationRemoteServiceProvider extends Remote {


	public Map getConfiguration(String project, String section, String keyword) throws RemoteException;

    public void setContiguration(String project, Map settingMap, com.ibm.sdwb.build390.user.authorization.AuthorizationCheck authCheck) throws RemoteException;


}
