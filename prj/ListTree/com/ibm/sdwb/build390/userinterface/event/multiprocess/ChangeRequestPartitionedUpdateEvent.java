package com.ibm.sdwb.build390.userinterface.event.multiprocess;

import com.ibm.sdwb.build390.userinterface.event.*;
import com.ibm.sdwb.build390.info.ChangeRequestPartitionedInfo;

public class ChangeRequestPartitionedUpdateEvent extends UserInterfaceEvent{
    public static String UPDATETYPE = "CHANGEREQUESTPARTIONEDUPDATE";

    public ChangeRequestPartitionedUpdateEvent(ChangeRequestPartitionedInfo source){
        super(source, UPDATETYPE);
    }

    public ChangeRequestPartitionedInfo getChangeRequestPartionedInfo(){
        return (ChangeRequestPartitionedInfo) getSource();
    }
}

