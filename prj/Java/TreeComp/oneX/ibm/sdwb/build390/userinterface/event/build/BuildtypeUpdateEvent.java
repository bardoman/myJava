package com.ibm.sdwb.build390.userinterface.event.build;

import com.ibm.sdwb.build390.userinterface.event.*;
import com.ibm.sdwb.build390.info.ChangeRequestPartitionedInfo;

public class BuildtypeUpdateEvent extends UserInterfaceEvent{
    public static String UPDATETYPE = "BUILDTYPEUPDATE";
    private String buildtype = null;

    public BuildtypeUpdateEvent(Object source){
        super(source, UPDATETYPE);
    }

    public void setBuildtype(String tempType){
        buildtype = tempType;
    }

    public String getBuildtype(){
        return buildtype;
    }
}

