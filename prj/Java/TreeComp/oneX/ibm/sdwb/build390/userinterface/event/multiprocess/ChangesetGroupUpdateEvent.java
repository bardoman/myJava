package com.ibm.sdwb.build390.userinterface.event.multiprocess;

import com.ibm.sdwb.build390.userinterface.event.*;
import com.ibm.sdwb.build390.info.ChangesetGroupInfo;

public class ChangesetGroupUpdateEvent extends UserInterfaceEvent{
    public static String UPDATETYPE = "CHANGESETGROUPDUPDATE";

    public ChangesetGroupUpdateEvent(ChangesetGroupInfo source){
        super(source, UPDATETYPE);
    }

    public ChangesetGroupInfo getChangesetGroupInfo(){
        return (ChangesetGroupInfo) getSource();
    }
}

