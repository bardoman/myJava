package com.ibm.sdwb.build390.process.steps;

import com.ibm.sdwb.build390.library.*;
import com.ibm.sdwb.build390.logprocess.LogEventProcessor;
import java.util.*;

public class CreateLevelInLibrary extends ProcessStep {
    static final long serialVersionUID = 1111111111111111L;

    private ChangesetGroup sourceInfo = null;

    public CreateLevelInLibrary(ChangesetGroup tempInfo, com.ibm.sdwb.build390.process.AbstractProcess tempProc) {
        super(tempProc,"Create Level in Library");
        setVisibleToUser(false);
        setUndoBeforeRerun(true);
        sourceInfo = tempInfo;
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
        getStatusHandler().updateStatus("Creating "+sourceInfo.getName()+" in library",false);
        sourceInfo.create();
    }

    public void undoProcess() throws com.ibm.sdwb.build390.MBBuildException{
        getStatusHandler().updateStatus("Removing "+sourceInfo.getName()+" in library",false);
        sourceInfo.delete();
    }

    public String getCreatedLevelName(){
        return sourceInfo.getName();
    }
}
