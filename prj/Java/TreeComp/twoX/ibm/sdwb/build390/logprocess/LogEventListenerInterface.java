package com.ibm.sdwb.build390.logprocess;
import java.io.IOException;


/*****************************************************************************************************/
/* Java LogListener Interface for Build/390 client                                                                */
/* This is the LogEventListener Interface that listerner class implemets for the build/390 LogProcessing                                        */
/****************************************************************************************************/
/* The LogEventListenerInterface class provides two abstract method which are implemented by the listener classes
/* - has abstract method HandleLogEvent which is overrided in the appropriate listener to do stuff
/* eg.in case of File Listener - its override to write to file
/* - isInteresting() is defaulted to true. But is overrided in the Appropriate listeners to determine 
/*  isInteresting or not
/****************************************************************************************************/
/* Changes
/* Date     Defect/Feature      Reason
/* 03/07/2000                  Birth of the class
/****************************************************************************************************/

interface LogEventListenerInterface {

    void handleLogEvent(LogEvent l) throws IOException;

    boolean isInterestingEvent(LogEvent l);
}
