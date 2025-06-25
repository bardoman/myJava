package com.ibm.sdwb.build390.userinterface.event.build;

import com.ibm.sdwb.build390.userinterface.event.*;
import com.ibm.sdwb.build390.info.ChangeRequestPartitionedInfo;

public class DriverUpdateEvent extends UserInterfaceEvent{
    public static String UPDATETYPE = "DRIVERUPDATE";
    private com.ibm.sdwb.build390.mainframe.DriverInformation driverInfo = null;

    public DriverUpdateEvent(Object source){
        super(source, UPDATETYPE);
    }

    public void setDriverInformation(com.ibm.sdwb.build390.mainframe.DriverInformation tempDriver){
        driverInfo = tempDriver;
    }

    public com.ibm.sdwb.build390.mainframe.DriverInformation getDriverInformation(){
        return driverInfo;
    }
}

