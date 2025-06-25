package com.ibm.sdwb.build390.process.steps;

import java.io.File;

import com.ibm.sdwb.build390.MBGlobals;
import com.ibm.sdwb.build390.library.LibraryInfo;
import com.ibm.sdwb.build390.user.Setup;
import com.ibm.sdwb.build390.*;

public class ShadowReport extends MainframeCommunication {
    static final long serialVersionUID = 1111111111111111L;

    private Setup setup = null;
    private String cmvcRelease = null;

    public ShadowReport(Setup tempSetup, String tempCmvcRelease, com.ibm.sdwb.build390.process.AbstractProcess tempProc) {
        super(MBGlobals.Build390_path+"misc"+File.separator+"SHADRPT-"+tempCmvcRelease,"Shadow report", tempProc);
        setVisibleToUser(true);
        setUndoBeforeRerun(false);
        setup = tempSetup;
        cmvcRelease = tempCmvcRelease;
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
        LibraryInfo libInfo = setup.getLibraryInfo();

        //Begin INT3097C
        boolean isFakeLib = MBClient.getCommandLineSettings().getMode().isFakeLibrary();

        String shadowReportCommand = "SHADPART OP=REPORT, "+libInfo.getDescriptiveStringForMVS()+", CMVCREL=\'"+cmvcRelease+"\'";

        if(isFakeLib) {
            shadowReportCommand+= ", NOLIB=YES";
        }
        //End INT3097C

        createMainframeCall(shadowReportCommand, "Getting Shadow Report", setup.getMainframeInfo());
        runMainframeCall();
    }
}

