package com.ibm.sdwb.build390.library;

import com.ibm.sdwb.build390.MBConstants;
public class VersionInfo implements java.io.Serializable {

    static final long serialVersionUID = 1111111111111111L;
    
    private String localVersion;
    private String remoteVersion;

    public String getLocalVersion(){
        return localVersion;
    }

    public void setLocalVersion(String localVersion){
        this.localVersion = localVersion;
    }

    public String getRemoteVersion(){
        return remoteVersion;
    }

    public void setRemoteVersion(String remoteVersion){
        this.remoteVersion = remoteVersion;
    }

   

    public String compatibleOrNotMessage() {
        if (localVersion.equals(remoteVersion)) {
            return("The Build390 client " + localVersion +" and " + MBConstants.NEWLINE +
                   "Build390 RMI Server "+remoteVersion + " are compatible.");

        }
        return("WARNING.! " + MBConstants.NEWLINE + "The Build390 client " + localVersion +" and " + MBConstants.NEWLINE +
                   "Build390 RMI Server "+remoteVersion + " are NOT compatible.");
    }


    
}

