package com.ibm.sdwb.build390.library.cmvc.server;

import java.rmi.*;
import java.util.*;

import com.ibm.sdwb.build390.metadata.MetadataOperationsInterface;

public interface CMVCLibraryServerInterface extends Remote {

    public Set getPartlist(com.ibm.sdwb.build390.library.cmvc.CMVCSourceInfo sourceInfo) throws RemoteException;

 /*   public com.ibm.sdwb.build390.metadata.cmvc.server.CMVCMetadataRemoteOperationsInterface getMetadataRemoteOperationsHandler() throws RemoteException; */

}
