package com.ibm.sdwb.build390.userinterface.event;

public class SelectionUpdateEvent extends UserInterfaceEvent{
    private static final String UPDATETYPE="SELECTIONUPDATE";
    private Object value = null;

    public SelectionUpdateEvent(Object source, Object tempVal){
        super(source,UPDATETYPE);
        value = tempVal;
    }

    public Object getValue(){
        return value;
    }
}
