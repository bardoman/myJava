package com.ibm.sdwb.build390.process.steps.library;

import com.ibm.sdwb.build390.*;
import com.ibm.sdwb.build390.process.steps.*;
import com.ibm.sdwb.build390.logprocess.LogEventProcessor;
import com.ibm.sdwb.build390.library.*;
import java.util.*;

public class RenameLevelInLibrary extends ProcessStep {
    static final long serialVersionUID = 1111111111111111L;

    private ChangesetGroup sourceInfo = null;
	private String newName = null;
	private String oldName = null;
    private boolean renameDone = false;

    public RenameLevelInLibrary(ChangesetGroup tempInfo, String tempNewName, com.ibm.sdwb.build390.process.AbstractProcess tempProc) {
        super(tempProc,"Rename object in library");
        setVisibleToUser(false);
        setUndoBeforeRerun(true);
        sourceInfo = tempInfo;
        newName = tempNewName;
		oldName = sourceInfo.getName();
    }

    public void undoProcess() throws com.ibm.sdwb.build390.MBBuildException{
        if (renameDone) {
            getStatusHandler().updateStatus("Rename  " + newName + " to  " + oldName + " in library ",false);
			sourceInfo.rename(oldName);
            renameDone = false;
        }
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
        getStatusHandler().updateStatus("Rename  " + oldName + " to  " + newName + " in library ",false);
        sourceInfo.rename(newName);
        renameDone = true;
    }
}
