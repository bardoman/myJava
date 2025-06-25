package com.ibm.sdwb.build390.logprocess;

import java.util.*;
import java.io.IOException;
import java.io.*;
import com.ibm.sdwb.build390.*;
import com.ibm.sdwb.build390.userinterface.graphic.MainInterface;


/****************************************************************************************************/
/* Java Logger for Build/390 client                                                                 */
/* This is the main class for the build/390 LogProcessing                                           */
/****************************************************************************************************/
/* The main log processing class. Would be instantiated in MBClient and would be subesequently passed
/* into the the main pages like driverbuildpage,userbuildpage,etc - incase of GUI mode
/* - in case of Command line processing - The LogEventProcessor object is passed through the method in
/* MBClient - serverCmd - and put in the Hashtable presetValues to the Verb classess to be traced.
/*Type of Logging  - LOGEXCEPTION - logs exception
/*                   LOGPRMYINFO  -
/*                   LOGSECYINFO  -
/* If LogException method is invoked on LogEventProcessor
/* (i)  First the Exception or Throwable object are decrypted to user/messages using ExceptionMessageDecrypter
/* (ii) LogEvents are generated with the EventLevel - denotes Logexception or Pryinfo or secyinfo =
/*                (LogEvent.LOGEXCEPTION_EVT_LVL,LogEvent.LOGPRMY_EVT_LVL,LOGSECY_EVT_LVL)
/*                                       EventType  - denotes title - to be displayed on MBMsgBox
/*                                       EventInfo  - The message to be displayed or logged
/* (iii) The listeners that are added to the Vector are casted and the HandleLogEvent method is invoked to handle it
/*
/*
/* IF  A CLASS doesnt log  stuff. - trace back where the LogEventProcessor was instantiated .
/*                                - check if the LogEventListeners are added.
/*                                - if LogFileEventListener is not added logging to file will not be there
/*                                - if LogDBListener is not added interesting exception would be logged to DB
/****************************************************************************************************/
/* Changes
/* Date     Defect/Feature      Reason
/* 03/07/2000                  Birth of the class
/* 05/02/2000  eventDisplay     Additon of EventDisplay to control just traceit or traceit + display in logprimaryinfo
/* 06/19/2000  shit element     make the LogEventFileListener as element 0
/****************************************************************************************************/

public class LogEventProcessor implements Cloneable {
    //adds all the listeners into the vector
    private List listeners = new ArrayList();
    static final long serialVersionUID = 1111111111111111L;

    public LogEventProcessor(){
        listeners = new Vector();
        addEventListener(MBClient.getGlobalLogFileListener());
        addEventListener(MBClient.getGlobalLogCommandLineListener());
    }


/*adds logevent listener
to ensure  if a listener is already added in the vector - it wouldnt be added - again.
  */  public synchronized void addEventListener(LogEventListener lel) {
        if ((!listeners.contains(lel))&(lel!=null)) {
            //shift the element LogEventFileListener to element 0
            if (lel instanceof LogEventFileListener) {
                listeners.add(0,lel);
            } else {
                listeners.add(lel);
            }
        }
    }

    public synchronized void removeEventListener(LogEventListener lel) {
        listeners.remove(lel);
    }

    public synchronized void removeAllListeners(){
        listeners = new ArrayList();
    }

    public List getEventListenerList(){
        return new ArrayList(listeners);
    }


// for  multi threading, we'll do that on a per listener method, rather than here.
// sound good?

    //EventLevel = 1
    public void LogException(MBBuildException mbe) {
        ExceptionMessageDecrypter emd = new ExceptionMessageDecrypter(mbe);
        LogEvent lel = new LogExceptionEvent(emd.getMessageFromDecrypter(),emd.getUserMessageFromDecrypter(),mbe,emd.getStackTraceFromDecrypter());
        for (int i = 0; i < listeners.size(); i++) {
            try {
                ((LogEventListener)listeners.get(i)).handleLogEvent(lel);
            } catch (IOException ioe) {
            }
        }

    }



    public void LogException(String data, Throwable ex) {
        ExceptionMessageDecrypter emd = new ExceptionMessageDecrypter(ex);
        LogEvent lel = new LogExceptionEvent(emd.getMessageFromDecrypter(),data+emd.getUserMessageFromDecrypter(),ex,emd.getStackTraceFromDecrypter());
        for (int i = 0; i < listeners.size(); i++) {
            try {
                ((LogEventListener)listeners.get(i)).handleLogEvent(lel);
            } catch (IOException ioe) {
            }
        }
    }

    //EventLevel = 2
    public void LogPrimaryInfo(String title,String data,boolean isDisplay)  {
        LogEvent  lel = new LogEvent(title,data,LogEvent.LOGPRMYINFO_EVT_LEVEL,isDisplay);
        for (int i = 0; i < listeners.size(); i++) {
            try {
                ((LogEventListener)listeners.get(i)).handleLogEvent(lel);
            } catch (IOException ioe) {
            }
        }

    }

    //EventLevel = 3
    public void LogSecondaryInfo(String title, String data)  {
        LogEvent  lel = new LogEvent(title,data,LogEvent.LOGSECYINFO_EVT_LEVEL);
        for (int i = 0; i < listeners.size(); i++) {
            try {
                ((LogEventListener)listeners.get(i)).handleLogEvent(lel);
            } catch (IOException ioe) {
            }
        }

    }

    public Object clone(){
        LogEventProcessor clonedLEP = new LogEventProcessor();
        for (Iterator iter = this.listeners.iterator();iter.hasNext();) {
            clonedLEP.addEventListener((LogEventListener)iter.next());
        }
        return clonedLEP;
    }
}

