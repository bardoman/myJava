package com.ibm.sdwb.build390;
import com.ibm.sdwb.build390.logprocess.*;
import com.ibm.sdwb.build390.userinterface.graphic.MainInterface;
/* This class replaces the default thread group and handles Errors intelligently
*/


class ErrorThreadGroup extends ThreadGroup {
    private LogEventProcessor lep = new LogEventProcessor();
    private boolean guiListenerAdded = false;


    public ErrorThreadGroup(String name) {
        super(name);
        attachLogListeners();
    }

    public ErrorThreadGroup(ThreadGroup parent, String name) {
        super(parent,name);
        attachLogListeners();
    }

    private void attachLogListeners() {
        lep.addEventListener(MBClient.getGlobalLogFileListener());
    }

    private void attachGUIListener() {
        if (MainInterface.getInterfaceSingleton()!=null & !guiListenerAdded) {
            guiListenerAdded=true;
            lep.addEventListener(MBClient.getGlobalLogGUIListener());
        }
    }

    public void uncaughtException(Thread t, Throwable e) {
        try {
            attachGUIListener();
            if (e instanceof MBBuildException) {
                lep.LogException((MBBuildException) e);
            } else {
                lep.LogException(e.getMessage()+"\nFor more details, check your " + MBGlobals.Build390_path+MBConstants.LOGFILEPATH + " log file.",e);
                if(MBClient.getCommandLineSettings()!=null && MBClient.getCommandLineSettings().isCommandLine()){ /* to avoid runtime exception to cause a hang in command line */
                    MBClient.exitApplication(MBConstants.GENERALERROR);
                }
            }
        } catch (Throwable anotherException) {
            System.err.println("Error handling uncaught exception.");
            anotherException.printStackTrace();
            System.err.println("Original exception");
            e.printStackTrace();
        }
//        super.uncaughtException(t,e);
    }
}
