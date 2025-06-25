package com.ibm.sdwb.build390.library;

import java.rmi.*;

public interface ProcessServerInterface extends Remote {

    public String getServerVersion() throws java.rmi.RemoteException;

}
