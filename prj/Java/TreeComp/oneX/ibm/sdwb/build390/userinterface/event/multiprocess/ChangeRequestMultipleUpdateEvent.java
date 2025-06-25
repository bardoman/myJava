
package com.ibm.sdwb.build390.userinterface.event.multiprocess;

import com.ibm.sdwb.build390.userinterface.event.*;
import com.ibm.sdwb.build390.info.ChangeRequestMultipleInfo;

public class ChangeRequestMultipleUpdateEvent extends UserInterfaceEvent{
    public static String UPDATETYPE = "CHANGEREQUESTMULTIPLEUPDATE";

    public ChangeRequestMultipleUpdateEvent(ChangeRequestMultipleInfo source){
        super(source, UPDATETYPE);
    }

    public ChangeRequestMultipleInfo getChangeRequestMultipleInfo(){
        return (ChangeRequestMultipleInfo) getSource();
    }
}

