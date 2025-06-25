package com.ibm.sdwb.build390.logprocess;

import java.io.IOException;
import java.io.Serializable;               

/*****************************************************************************************************/
/* Java LogListener for Build/390 client                                                                */
/* This is the LogEventListener class for the build/390 LogProcessing                                        */
/****************************************************************************************************/
/* The LogEventListener class Listens the LogEvents invoked and does the appropriate stuff
/* - has abstract method HandleLogEvent which is overrided in the appropriate listener to do stuff
/* eg.in case of File Listener - its override to write to file
/* - isInteresting() is defaulted to true. But is overrided in the Appropriate listeners to determine 
/*  isInteresting or not
/****************************************************************************************************/
/* Changes
/* Date     Defect/Feature      Reason
/* 03/07/2000                   Birth of the class
/****************************************************************************************************/

abstract class LogEventListener implements LogEventListenerInterface {

    public abstract  void handleLogEvent(LogEvent l) throws IOException;

    public boolean isInterestingEvent(LogEvent l) {
        return true;
    }
}


