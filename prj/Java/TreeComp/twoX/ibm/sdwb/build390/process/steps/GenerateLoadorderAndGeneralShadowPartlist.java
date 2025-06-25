package com.ibm.sdwb.build390.process.steps;

import java.io.*;

import com.ibm.sdwb.build390.*;
import com.ibm.sdwb.build390.logprocess.LogEventProcessor;
import com.ibm.sdwb.build390.mainframe.CreateVerbFiles;
import com.ibm.sdwb.build390.userinterface.event.build.ProcessUpdateEvent;

public class GenerateLoadorderAndGeneralShadowPartlist extends ProcessStep {
    static final long serialVersionUID = 1111111111111111L;
    private MBBuild build = null;
    private CreateVerbFiles verbWriter = null;
    private File loadOrderFile = null;
    private File shadowCheckFile = null;
    private transient Writer loadOrderWriter = null;
    private transient Writer partlistCheckWriter = null;

    public GenerateLoadorderAndGeneralShadowPartlist(MBBuild tempBuild, File tempLoadOrder, File tempShadowCheck, com.ibm.sdwb.build390.process.AbstractProcess tempProc) {
        super(tempProc,"Shadow Check File Generation");
        build = tempBuild;
        loadOrderFile = tempLoadOrder;
        shadowCheckFile = tempShadowCheck;
        setUndoBeforeRerun(false);
    }

    public boolean isLoadorderUpdated() {
        return verbWriter.isLoadOrderUpdated();
    }

    public boolean isPartlistEmpty() {
        return verbWriter.isPartlistEmpty();
    }

    public void setShadowCheckWriter(Writer tempShadCheckWriter) {
        partlistCheckWriter = tempShadCheckWriter;
    }

    /**
     * This is the method that should be implemented to actually
     * run the process.	Use executionArgument if you need to 
     * access the argument from the execute method.
     * 
     * @return Object indicating output of the step.
     */
    public void execute() throws com.ibm.sdwb.build390.MBBuildException{
        getLEP().LogSecondaryInfo(getFullName(),"Entry");
        getStatusHandler().updateStatus("Writing shadow check files", false);
        try {
            verbWriter = new CreateVerbFiles(getLEP());
            if (loadOrderFile!=null) {
                loadOrderWriter = new BufferedWriter(new FileWriter(loadOrderFile));
            }
            if (shadowCheckFile!=null) {
                partlistCheckWriter = new BufferedWriter(new FileWriter(shadowCheckFile));
            }
            verbWriter.makeLoadOrderAndPartlistShadowCheckVerbFile(build, loadOrderWriter, partlistCheckWriter);
            if (loadOrderFile!=null) {
                loadOrderWriter.close();
            }
            if (shadowCheckFile!=null) {
                partlistCheckWriter.close();
            }
            if (verbWriter.isPartlistEmpty()) {
                ProcessUpdateEvent processUpdateevent = new ProcessUpdateEvent(this);
                processUpdateevent.setStartFromBeginning();
                getProcess().handleUIEvent(processUpdateevent);
                throw new GeneralError("Partlist was empty");
            }
        } catch (java.io.IOException ioe) {
            throw new com.ibm.sdwb.build390.GeneralError("Writing the shadow check files", ioe);
        }
    }
}
