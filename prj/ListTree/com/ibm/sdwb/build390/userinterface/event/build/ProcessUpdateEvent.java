package com.ibm.sdwb.build390.userinterface.event.build;

import com.ibm.sdwb.build390.userinterface.event.*;

public class ProcessUpdateEvent extends UserInterfaceEvent {
    public static String UPDATETYPE = "PROCESSUPDATETYPEUPDATE";

    private int status = -1;
    private static final int DEFAULT = -1;
    private static final int START_FROM_BEGINNING = DEFAULT +1;
    private static final int PROCESS_FINISHED     = START_FROM_BEGINNING +1;

    public ProcessUpdateEvent(Object source) {
        super(source, UPDATETYPE);
    }

    public void setStartFromBeginning() {
        status = START_FROM_BEGINNING;
    }

    public void setProcessFinished() {
        status = PROCESS_FINISHED;
    }

    public boolean isStartFromBeginning() {
        return(status == START_FROM_BEGINNING);
    }

    public boolean isProcessFinished() {
        return(status == PROCESS_FINISHED);
    }
}

