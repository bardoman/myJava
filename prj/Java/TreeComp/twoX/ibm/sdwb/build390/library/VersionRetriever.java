package com.ibm.sdwb.build390.library;

import com.ibm.sdwb.build390.LibraryError;


public class VersionRetriever  {


    private VersionInfo versionInfo = null;
    private boolean hasVerified = false;

    public VersionRetriever() {
        if (versionInfo==null) {
            versionInfo = new VersionInfo();
        }
    }

    public void retrieveLocalVersion() {
        versionInfo.setLocalVersion(com.ibm.sdwb.build390.MBConstants.getProgramVersion());

    }

    public void retrieveRemoteVersion(ProcessServerInterface remoteInterface) throws java.rmi.RemoteException{
        versionInfo.setRemoteVersion(remoteInterface.getServerVersion());
    }

    public boolean hasVerified(){
        return hasVerified;
    }

    public void setVerified(boolean hasVerified){
        this.hasVerified = hasVerified;
    }

    public VersionInfo getVersionInfo(){
        return versionInfo;
    }

}

