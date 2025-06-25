package com.ibm.sdwb.build390.process.steps;

import java.io.File;

import com.ibm.sdwb.build390.MBGlobals;
import com.ibm.sdwb.build390.MBMainframeInfo;

public class MVSServerStatus extends MainframeCommunication {
    static final long serialVersionUID = 1111111111111111L;

    private MBMainframeInfo mainframeInfo = null;
    private String outputLocation = null;

    public MVSServerStatus(MBMainframeInfo tempMainframeInfo, com.ibm.sdwb.build390.process.AbstractProcess tempProc) {
        super(MBGlobals.Build390_path+"misc"+File.separator+"NUTSY-MVSServerStatus-"+tempMainframeInfo.getMainframeAddress()+"-"+tempMainframeInfo.getMainframePort(),"Checking MVS server status", tempProc);
        setVisibleToUser(true);
        setUndoBeforeRerun(false);
        this.mainframeInfo  = tempMainframeInfo;
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
        createMainframeCall("NUTSY", "Requesting server status", mainframeInfo);
        runMainframeCall();
    }
}
