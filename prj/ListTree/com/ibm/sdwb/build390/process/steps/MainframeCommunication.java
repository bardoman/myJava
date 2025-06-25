/*********************************************************************/
/* This should be the parent class of any process steps
/* that makes a mainframe call.   It's methods should be
/* used to manipulate mainframe socket calls.
/*********************************************************************/
//09/19/2005 PTM4094 autoretry connection.
/*********************************************************************/

package com.ibm.sdwb.build390.process.steps;

import com.ibm.sdwb.build390.*;
import com.ibm.sdwb.build390.logprocess.LogEventProcessor;
import com.ibm.sdwb.build390.mainframe.RetryHandler;
import java.io.*;

public abstract class MainframeCommunication extends ProcessStep implements com.ibm.sdwb.build390.mainframe.MainframeOutputSourceInterface {
    static final long serialVersionUID = 1111111111111111L;

    private String fileOutputHeader = null;
    private transient MBSocket mainframeConnection = null;
    private File clearPrintFile = null;
    private File clearOutFile = null;
    private boolean showPrintFile = false;
    private boolean showOutFile = false;
    private int returnCode = -1;
    private RetryHandler connectionRetryHandler;

    public MainframeCommunication(String outputHeaderLocation, String stepName, com.ibm.sdwb.build390.process.AbstractProcess tempProc) {
        super(tempProc,stepName);
        setVisibleToUser(false);
        setUndoBeforeRerun(false);
        setOutputHeaderLocation(outputHeaderLocation);
    }

    public void setRetryHandler(RetryHandler connectionRetryHandler) {
        this.connectionRetryHandler = connectionRetryHandler;

    }

    public final File getPrintFile() {
        return clearPrintFile;
    }

    public final File getOutputFile() {
        return clearOutFile;
    }

    public final int getReturnCode() {
        return returnCode;
    }

    public final String getClearOutString() {
        return mainframeConnection.getClearOut();
    }

    public final String getClearPrintString() {
        return mainframeConnection.getClearPrint();
    }

    public final void setShowFilesAfterRun(boolean tempShowOutFile, boolean tempShowPrintFile) {
        showOutFile = tempShowOutFile;
        showPrintFile = tempShowPrintFile;
    }

    public final void setOutputHeaderLocation(String newHeader) {
        fileOutputHeader = newHeader;
        if (fileOutputHeader !=null) {
            clearOutFile = new File(fileOutputHeader+".out");
            clearPrintFile = new File(fileOutputHeader+".prt");
        } else {
            clearOutFile = null;
            clearPrintFile = null;
        }
    }

    protected final String getOutputHeaderLocation() {
        return fileOutputHeader;
    }

    public final void createMainframeCall(String mainframeCommand, String statusMessageForCall, MBMainframeInfo mainInfoForConnection) {
        mainframeConnection = new MBSocket(mainframeCommand, fileOutputHeader, statusMessageForCall, getStatusHandler(), mainInfoForConnection, getLEP());
    }

    public final void createMainframeCall(String mainframeCommand, String statusMessageForCall, boolean dontThrowException, MBMainframeInfo mainInfoForConnection) {
        mainframeConnection = new MBSocket(mainframeCommand, fileOutputHeader, statusMessageForCall, dontThrowException, getStatusHandler(), mainInfoForConnection, getLEP());
    }

    /** Set hold setting.
    * This method sets bit 4 in server switch 1.
    * @param hld boolean value to set hold to */
    public static final void set_hold(boolean hld) {
        /* MBSocket.set_hold(hld); */
        com.ibm.sdwb.build390.mainframe.MainframeOutputTraceOptions.getInstance().setHoldSubServerJobOutput(hld);
    }

    /** Set CLROUT setting.
    * This method sets bit 2 in server switch 0.*/
    public final void unset_clrout() {
        mainframeConnection.unset_clrout();
    }

    /** Set SYSOUT setting.
    * This method sets bit 7 in server switch 0. */
    public final void setSysout() {
        mainframeConnection.setSysout();
    }

    /** Set DELSYSOUT setting.
    * This method sets bit 8 in server switch 0.*/
    public final void setDelsysout() {
        mainframeConnection.setDelsysout();
    }

    /** Set scheduler setting.
    * This method sets bit  in server switch 1.*/
    public final void setScheduler() {
        mainframeConnection.setScheduler();
    }

    /** Set JobsCancel setting.
    * This method sets bit 4 in server switch 0.*/
    public final void setJobsCancel() {
        mainframeConnection.setJobsCancel();
    }

    /** Set JOBSTATUS setting.
    * This method sets bit 3 in server switch 0.*/
    public final void setJobstatus() {
        mainframeConnection.setJobstatus();
    }

    /** Set SYSTSPRT setting.
    * This method sets bit 6 in server switch 2.*/
    public final void setSystsprt() {
        mainframeConnection.setSystsprt();
    }

    /** Set TSO setting.
    * This method sets bit 3 in server switch 2.*/
    public final void setTSO() {
        mainframeConnection.setTSO();
    }

    /** disable cancel of the actual host call.
    */
    public final void dontAllowHostCallCancel() {
        mainframeConnection.dontAllowHostCallCancel();
    }

    /** Set CLRTSRC for the server to find the buildid file you're calling
    * @ param pathToFile */
    public final void setPathToVerbFile(String tempVerbPath) {
        mainframeConnection.setPathToVerbFile(tempVerbPath);
    }

    public final void runMainframeCall() throws com.ibm.sdwb.build390.MBBuildException{
        getOutputFile().delete();
        getPrintFile().delete();

        //Begin INT2387
        LogEventProcessor lep = new LogEventProcessor();

        lep.addEventListener(MBClient.getGlobalLogFileListener());
        //End INT2387

        int retryCount = 1;
        int sleepCount = 0;
        boolean needsToRunAgain = true;

        if (connectionRetryHandler!=null) {
            retryCount = connectionRetryHandler.getRetryCount();
            sleepCount = connectionRetryHandler.getSleepCountAfterRetry();
            needsToRunAgain = connectionRetryHandler.isRetryAppropriate();
        }

        /*by default the connection is run once. If there is an error, the default sleep count is zero, so the thread 
        doesnot sleep 
        */
        for (int exceptionRetryIndex= 1; needsToRunAgain & (exceptionRetryIndex <= retryCount) ; exceptionRetryIndex++ ) {
            try {
                mainframeConnection.run();
                needsToRunAgain = false; /* if the step completes successfully, dont run it again */
            } catch (PasswordError ivpe) {
                /** for now trap the PasswordException.
                 *  The isRetryAppropriate should handle it later on. The problem is Password stuff is tightly coupled with MBSocket class */
                needsToRunAgain = false;
                throw ivpe;
            } catch (com.ibm.sdwb.build390.MBBuildException someError) {
                if (exceptionRetryIndex == retryCount) {
                    throw someError;
                }
                if (sleepCount > 0) {
                    getStatusHandler().updateStatus("Connection Error! Retrying in " + sleepCount  +" ms... , Count = " + exceptionRetryIndex + " of " + retryCount, false);                

                    //INT2387
                    lep.LogPrimaryInfo("Debug:","Connection Error! Retrying in " + sleepCount  +" ms... , Count = " + exceptionRetryIndex + " of " + retryCount,true);

                    try {
                        Thread.sleep(sleepCount);
                    } catch (InterruptedException irpe) {
                    }
                }
            } catch (Exception randomError) {
                if (exceptionRetryIndex == retryCount ) {
                    throw new GeneralError("Error occurred during mainframe socket call.", randomError);
                }
                if (sleepCount > 0) {
                    getStatusHandler().updateStatus("Connection Error! Retrying in " + sleepCount  +" ms... , Count = " + exceptionRetryIndex + " of " + retryCount, false);                

                    //INT2387
                    lep.LogPrimaryInfo("Debug:","Connection Error! Retrying in " + sleepCount  +" ms... , Count = " + exceptionRetryIndex + " of " + retryCount,true);//***BE

                    try {
                        Thread.sleep(sleepCount);
                    } catch (InterruptedException irpe) {
                    }
                }
            }
        }


        returnCode = mainframeConnection.getHostReturnCode();
        mainframeConnection = null;
        if (showOutFile) {
            MBEdit outFileDisplay = new MBEdit(getOutputFile().getAbsolutePath(), getLEP());
        }
        if (showPrintFile) {
            MBEdit printFileDisplay = new MBEdit(getPrintFile().getAbsolutePath(), getLEP());
        }


    }

    public void haltProcess() throws com.ibm.sdwb.build390.MBBuildException{
        if (isHaltable()) {
            if (mainframeConnection!=null) {
                mainframeConnection.stop();
                mainframeConnection = null;
            }
            super.haltProcess();
        }

    }

}
