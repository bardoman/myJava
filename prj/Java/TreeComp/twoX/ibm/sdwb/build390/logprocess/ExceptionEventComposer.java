package com.ibm.sdwb.build390.logprocess;

import java.io.File;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import com.ibm.sdwb.build390.HostError;
import com.ibm.sdwb.build390.MBBuildException;
import com.ibm.sdwb.build390.MBConstants;


class ExceptionEventComposer {

    private LogEvent l;
    private boolean isHandledException = false;
    private boolean hasHostErrors = false;
    private Map errorsFilesMap = new HashMap();
    private int count =0;

    ExceptionEventComposer(LogEvent tempLel) {
        this.l= tempLel;
    }

    protected boolean isHandledException() {
        return isHandledException;
    }

    protected boolean hasHostErrors() {
        return hasHostErrors;
    }

    /*TST1558 */
    protected void  handleSingleException(Object obj) {
        String  logData= l.toLoggableString();

        if (obj instanceof com.ibm.sdwb.build390.MBBuildException) {
            count++;

            MBBuildException singleException = (com.ibm.sdwb.build390.MBBuildException)obj;
            if (singleException instanceof com.ibm.sdwb.build390.HostError) {
                HostError hE = (HostError) singleException;
                if (hE.getPrintFile() != null) {
                    logData += MBConstants.NEWLINE+"Please refer the associated files " + hE.getPrintFile().getAbsolutePath()+"  and " + hE.getOutputFile().getAbsolutePath() + " for more details.";
                    // if either file exists and is > 0 bytes in length, tell user // checkFileSize
                    boolean pfileexists = false;
                    boolean ofileexists = false;
                    File pfile = hE.getPrintFile();
                    File ofile = hE.getOutputFile();
                    Set hostFilesSet  = new HashSet();

                    hostFilesSet.add(hE.getUserMessage());

                    if (hE.getPrintFile().exists()) {
                        if (hE.getPrintFile().length()>0) {
                            hostFilesSet.add(hE.getPrintFile());
                        }
                    }
                    if (hE.getOutputFile().exists()) {
                        if (hE.getOutputFile().length()>0) {
                            hostFilesSet.add(hE.getOutputFile());
                        }
                    }
                    if (!hostFilesSet.isEmpty()) {
                        hasHostErrors = true;
                        errorsFilesMap.put(new Integer(count), hostFilesSet);
                    }

                }
            }

            isHandledException = true;


        }
    }

    protected Map getHostErrorFilesMap() {
        return errorsFilesMap;
    }

    /*TST1558 */
    protected void handleMultipleConcurrentException() {
        java.util.List hostErrorList  = new java.util.ArrayList();
        java.util.List notHostErrorList  = new java.util.ArrayList();
        if (l.getExtraInformation() instanceof com.ibm.sdwb.build390.utilities.MultipleConcurrentException) {
            for (java.util.Iterator exceptionIterator = ((com.ibm.sdwb.build390.utilities.MultipleConcurrentException)l.getExtraInformation()).getExceptionSet().iterator();exceptionIterator.hasNext();) {
                Object obj = exceptionIterator.next();
                if (obj instanceof com.ibm.sdwb.build390.HostError) {
                    hostErrorList.add(obj);
                } else {
                    notHostErrorList.add(obj);
                }
            }
            for (java.util.Iterator hostErrorIterator = hostErrorList.iterator();hostErrorIterator.hasNext();) {
                handleSingleException(hostErrorIterator.next());
            }
            if (!notHostErrorList.isEmpty()) {
                handleSingleException(new Object()); /* just a dummy object **/
            }
        }

    }
}

    
