package com.ibm.sdwb.build390;
/*********************************************************************/
/* Java ExternalProgramHandler interface for the Build390 java client             */
/*********************************************************************/
// Updates:
// 03/07/2000 rework log stuff using listeners
// 03/24/2000 config changes in the cmd[] array during display - at debug time
// 03/27/2000 guilistener - changes to delete the "Query command:" string.
// 05/02/2000 eventDisplay      Additon of boolean switch to LogPrimaryIfno to  control just traceit or traceit + display
/******************************************************************************/

import java.io.*;
import java.util.*;
import java.rmi.*;
import javax.swing.JInternalFrame;
import com.ibm.sdwb.build390.logprocess.*;


public class ExternalProgramHandler {

    Process child = null;
    LogEventProcessor lep=null;

    public ExternalProgramHandler(LogEventProcessor lep){
        this.lep=lep;
    }

    public String runCommand(String[] cmd) throws GeneralError {
        lep.LogSecondaryInfo("Debug:","ExternalProgamHandler : runCommand method");
        asynchReader errReader = null;
        asynchReader inReader = null;
        String outputString = null;
        if (System.getProperty("os.name").indexOf("Window") > -1) {
            for (int i = 0; i < cmd.length; i++) {
                String currentTest = cmd[i];
                if (currentTest.indexOf(" ")> -1) {
                    cmd[i] = "\""+currentTest+"\"";
                }
            }
        }

        String debugtraceBuffer = "Query Command: ";
        for (int i = 0; i < cmd.length; i++) {
            debugtraceBuffer+=cmd[i] + " ";
        }

        lep.LogPrimaryInfo("Debug Config Query:", debugtraceBuffer,true);

        try {
            // issue the cmvc command            
            try {
                child = Runtime.getRuntime().exec(cmd);
            } catch (IOException e) {
                throw new GeneralError("There was a problem calling the external program " + cmd[0], e);
            }

            BufferedReader err = new BufferedReader(new InputStreamReader(child.getErrorStream()));
            errReader = new asynchReader(err);
            BufferedReader in = new BufferedReader(new InputStreamReader(child.getInputStream()));
            inReader = new asynchReader(in);
            errReader.join();
            inReader.join();
            in.close();
            err.close();
            in = null;
            err = null;
            outputString = inReader.getRead();
            if (outputString == null) {
                outputString = new String();
            }

            // wait for child and get the rc
            child.waitFor();
            child = null;
        } catch (InterruptedException ie) {
            throw new GeneralError("An interruption occurred while waiting for the library process to terminate", ie);
        } catch (IOException ioe) {
            throw new GeneralError("An error occurred while closing the stream to the library process", ioe);
        }
        if (errReader.getRead() != null) {
            throw new GeneralError(errReader.getRead());
        }
        return outputString;
    }

    public void killProcess(){
        if (child != null) {
            child.destroy();
        }
    }

    class asynchReader extends Thread {
        BufferedReader source = null;
        String returnString = null;

        asynchReader(BufferedReader tempReader) {
            source = tempReader;
            start();
        }

        public void run() {
            try {
                char[] buff = new char[1024];
                int bytesRead = source.read(buff);
                if (bytesRead >=0) {
                    returnString = new String(buff, 0, bytesRead);
                    while ((bytesRead = source.read(buff)) >=0) {
                        returnString += new String(buff, 0, bytesRead);
                    }
                }
            } catch (IOException e) {
                //MBUtilities.LogException("An error occurred while reading bytes", e);
                lep.LogException("An error occurred while reading bytes", e);
            }
        }

        public String getRead() {
            return returnString;
        }
    }
}
