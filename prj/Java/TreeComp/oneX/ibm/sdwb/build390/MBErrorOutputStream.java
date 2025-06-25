package com.ibm.sdwb.build390;

import java.io.*;
import com.ibm.sdwb.build390.logprocess.*;
import com.ibm.sdwb.build390.userinterface.graphic.MainInterface;
// 04/27/99 errorHandling       change LogException parms & add new error types
// 03/07/2000 reworklog         changes to write the log stuff using listeners

public class MBErrorOutputStream extends OutputStream {

    private ByteArrayOutputStream errorOutput=null;
    private PrintStream originalErrorStream=null;
    private LogEventProcessor lep=null;

    public MBErrorOutputStream(PrintStream tempOrigErr) {
        lep= new LogEventProcessor();
        lep.addEventListener(MBClient.getGlobalLogFileListener());
        if (MainInterface.getInterfaceSingleton()!=null) {
            lep.addEventListener(MBClient.getGlobalLogGUIListener());
        }
        errorOutput = new ByteArrayOutputStream();
        originalErrorStream = tempOrigErr;
    }

    public void close() throws IOException {
        errorOutput.close();
        originalErrorStream.close();
    }

    public void flush() throws IOException {
        errorOutput.flush();
        originalErrorStream.flush();
    }

    public void write(byte b[]) throws IOException{
        errorOutput.write(b);
        originalErrorStream.write(b);
        updateErrorOutput();
    }

    public void write(byte b[], int off, int len) throws IOException{
        errorOutput.write(b, off, len);
        originalErrorStream.write(b, off, len);
        updateErrorOutput();
    }

    public void write(int b) throws IOException {
        errorOutput.write(b);
        originalErrorStream.write(b);
        updateErrorOutput();
    }

    public void updateErrorOutput()throws IOException{
        if (MainInterface.getInterfaceSingleton()!=null) {
            errorOutput.flush();
            MainInterface.getInterfaceSingleton().updateErrorPanel(errorOutput.toString());
        }
    }

    public String toString() {
        try {
            errorOutput.flush();
            return errorOutput.toString();
        } catch (IOException ioe) {
            lep.LogException("There was an error getting the stdErr output", ioe);
        }
        return "Error converting MBErrorOutputStream to String";
    }
}
