package com.ibm.sdwb.build390.userinterface.event.build;

import com.ibm.sdwb.build390.userinterface.event.*;
import com.ibm.sdwb.build390.info.ChangeRequestPartitionedInfo;

public class ReleaseUpdateEvent extends UserInterfaceEvent{
    public static String UPDATETYPE = "RELEASEUPDATE";
    private com.ibm.sdwb.build390.mainframe.ReleaseInformation relInfo = null;

    public ReleaseUpdateEvent(Object source){
        super(source, UPDATETYPE);
    }

    public void setReleaseInformation(com.ibm.sdwb.build390.mainframe.ReleaseInformation tempRel){
        relInfo = tempRel;
    }

    public com.ibm.sdwb.build390.mainframe.ReleaseInformation getReleaseInformation(){
        return relInfo;
    }
}

