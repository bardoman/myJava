package com.ibm.sdwb.build390.logprocess;

import java.io.File;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import com.ibm.sdwb.build390.MBClient;
import com.ibm.sdwb.build390.MBEdit;
import com.ibm.sdwb.build390.userinterface.graphic.MainInterface;

/*****************************************************************************************************/
/* Java GUIListener for Build/390 client                                                                */
/* This is the LogEventGUIListener class for the build/390 LogProcessing                                        */
/****************************************************************************************************/
/* The LogEventGUIListener class Listens to the LogEvents invoked and decides whether to show up on the screen or no
/* - has abstract method HandleLogEvent which is overrided to display the stuff on the screen
/* - isInteresting() is true if visual debug is set on + on a logprimary info event level or
/* - if its a LogException. - the same applies handling events for the config process
/****************************************************************************************************/
/*09/15/2003 TST1558 - show hosterrors, when multipleconcurrentexception occurs. **/

public class LogEventCommandLineListener extends LogEventListener {

    static final long serialVersionUID = 1111111111111111L;

    public LogEventCommandLineListener() {
    }

    // if debug is set or
    // if its a logExceptionEvent or
    //
    public  boolean isInterestingEvent(LogEvent l) {
        if (((MBClient.getVisualDebug())&&(l.getEventLevel()==LogEvent.LOGPRMYINFO_EVT_LEVEL)&&l.getEventDisplay())||
            (l.getEventLevel()==LogEvent.LOGEXCEPTION_EVT_LEVEL)) {
            return true;
        }
        return false;
    }



    public synchronized void handleLogEvent(final LogEvent l) {
        boolean isDisplayed = false;
        if ((isInterestingEvent(l)) && MainInterface.getInterfaceSingleton()==null) { //only commandline
            if (l.getEventLevel()==LogEvent.LOGEXCEPTION_EVT_LEVEL) {
                ExceptionEventComposer exceptionFormatter = new ExceptionEventComposer(l);
                if (l.getExtraInformation() instanceof com.ibm.sdwb.build390.utilities.MultipleConcurrentException) {
                    exceptionFormatter.handleMultipleConcurrentException();
                } else {
                    exceptionFormatter.handleSingleException(l.getExtraInformation());
                }
// Don't show question if not in gui mode // CmdLineErrorMsg
                if (exceptionFormatter.hasHostErrors()) {
                    // Show user file names in cmd line mode // CmdLineErrorMsg
                    showHostFiles(exceptionFormatter.getHostErrorFilesMap());
                } else {
                    writeEventToConsole(l); //to handle display of LOGEXCEPTION Event
                    isDisplayed = true;
                }
            } else {
                System.out.println(l.toLoggableString());
                isDisplayed = true;
            }
        }

        if (MBClient.getDetailDebug() && !isDisplayed) { //should be invoked in both cases (cmd line and gui)
            System.out.println(l.toLoggableString());
        }
    }

    private void showHostFiles(Map  hostFilesMap) {
        for (Iterator iter=hostFilesMap.entrySet().iterator();iter.hasNext();) {
            Set filesSet = (Set)(((Map.Entry)iter.next()).getValue());
            String msg = getHostUserMessage(filesSet);
            for (Iterator filesIterator = filesSet.iterator();filesIterator.hasNext();) {
                Object singleObj = filesIterator.next();
                if (singleObj instanceof File) {
                    new MBEdit(((File)singleObj).getAbsolutePath(),MBClient.lep);
                }
            }
        }
    }

    private String getHostUserMessage(Set tempFilesSet) {
        for (Iterator filesIterator = tempFilesSet.iterator();filesIterator.hasNext();) {
            Object singleObj = filesIterator.next();
            if (singleObj instanceof String) {
                return(String)singleObj;
            }
        }
        return new String();
    }

    private void writeEventToConsole(LogEvent l) {
        if (l.getEventInfo().length() >0) {
            System.out.println(l.getEventInfo());
        } else {
            System.out.println(l.toLoggableString());
        }
    }

}



