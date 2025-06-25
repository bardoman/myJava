package com.ibm.sdwb.build390.userinterface.event;

public class UserInterfaceEvent extends java.util.EventObject{
    private String updateType = null;
    private static final String UPDATETYPE="GENERALUPDATE";

    public UserInterfaceEvent(Object source, String tempUpdateType){
        super(source);
        updateType = tempUpdateType;
    }

    public UserInterfaceEvent(Object source){
        super(source);
        updateType = UPDATETYPE;
    }

    public String getUpdateType(){
        return updateType;
    }
}
