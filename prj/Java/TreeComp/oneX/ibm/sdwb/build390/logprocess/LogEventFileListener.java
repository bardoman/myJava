package com.ibm.sdwb.build390.logprocess;

import com.ibm.sdwb.build390.*;
import java.io.*;
import java.awt.event.*;
import com.ibm.sdwb.build390.userinterface.graphic.MainInterface;


/*****************************************************************************************************/
/* Java FileLogListener for Build/390 client                                                                */
/* This is the LogEventFileListener class for the build/390 LogProcessing                                        */
/****************************************************************************************************/
/* The LogEventFileListener class Listens the LogEvents invoked and does the log it into the File
/* - has abstract method HandleLogEvent which is overrided to write stuff into a file
/* - isInteresting() is true if its LogEvent Level  = LOGEXCEPTION_EVT_LVL or LOGPRMY_INFO_EVT_LVL or
/*                              or if the MBClients Static Method isLogAllMessagesEnables()
/* if found interesting the toLoggableString - is logged into a file in a individual thread
/****************************************************************************************************/



public class LogEventFileListener extends LogEventListener {

    static final long serialVersionUID = 1111111111111111L;
    private transient  File logFile;
    private transient BufferedWriter logWriter=null;
    private String logStr=null;

    public LogEventFileListener(final File logFile) {
        this.logFile = logFile;
    }
    public LogEventFileListener(final String logStr) {
        this.logStr=logStr;
    }


    public synchronized  void handleLogEvent(LogEvent l) {
        // need to see the threading options
        // the writing into a file would be handled by a individual thread
        String logData = l.toLoggableString();
        try {
            if (isInterestingEvent(l)) {
                if (l.getEventLevel()==LogEvent.LOGEXCEPTION_EVT_LEVEL) {
                    logData += MBConstants.NEWLINE + ((LogExceptionEvent)l).getStackTrace();
                }
                logData +=MBConstants.NEWLINE;
                if ((logStr.trim()==null)|(logStr.trim().length()==0)) {
                    //should never happen with logStr = null...just for precaution.
                    // System.out.println("errorr.....logging not done for "+logData);
                } else {
                    try {
// Ken 6/9/00 this should make sure the directory path to the file exists, so when you try to write to it, you won't get a file not
// found exception.
                        File logFile = new File(logStr);
                        if (!logFile.exists()) {
                            String parentDirectory=logFile.getParent();
                            if (parentDirectory!=null ) {
                                File logDirectory = new File(parentDirectory);
                                logDirectory.mkdirs();
                            }
                        }
                        logWriter = new BufferedWriter(new FileWriter(logFile.getAbsolutePath(),true));
                        logWriter.write(logData,0,logData.length());
                        logWriter.close();
                    } catch (IOException e) {
                        //a bug found during running trackreview.perl
                        e.printStackTrace();
                        if (logWriter!=null) {
                            logWriter.close();
                        }
                    }
                }
            }
        } catch (IOException ioe) {
            ioe.printStackTrace(System.err);
        }

    }

    public  boolean isInterestingEvent(LogEvent l) {
        return((l.getEventLevel()<LogEvent.LOGSECYINFO_EVT_LEVEL)||MBClient.getDetailDebug());
    }

}






