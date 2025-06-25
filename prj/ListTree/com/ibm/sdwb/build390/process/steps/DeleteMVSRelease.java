package com.ibm.sdwb.build390.process.steps;

import com.ibm.sdwb.build390.*;
import com.ibm.sdwb.build390.logprocess.LogEventProcessor;
import com.ibm.sdwb.build390.mainframe.*;
import com.ibm.sdwb.build390.library.LibraryInfo;
import java.util.*;
import java.io.*;

public class DeleteMVSRelease extends MainframeCommunication {
    static final long serialVersionUID = 1111111111111111L;

    private MBMainframeInfo mainInfo = null;
    private LibraryInfo libInfo = null;
    private ReleaseInformation deadRelease = null;

    public DeleteMVSRelease(MBMainframeInfo tempMain, LibraryInfo tempLib, ReleaseInformation tempRelease, com.ibm.sdwb.build390.process.AbstractProcess tempProc) {
        super(MBGlobals.Build390_path+"misc"+File.separator+"DELETESHADOW-"+tempRelease.getMvsName(),"Delete MVS Release", tempProc);
        setVisibleToUser(true);
        setUndoBeforeRerun(false);
        mainInfo = tempMain;
        libInfo = tempLib;
        deadRelease = tempRelease;
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
        String deleteShadowCommand = "DELDB SHADDB "+deadRelease.getMvsHighLevelQualifier()+" "+deadRelease.getMvsName();

        createMainframeCall(deleteShadowCommand, "Deleting mvs release " + deadRelease.getMvsName(), mainInfo);
        setTSO();
        setSystsprt();
        dontAllowHostCallCancel();
        runMainframeCall();
        mainInfo.removeRelease(deadRelease, libInfo);

        try {  /*TST1921 */
            ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(MBGlobals.Build390_path+"releasesAndDrivers.ser"));
            MBMainframeInfo.saveStaticInfoMap(oos);
            oos.close();
        } catch (IOException ioe) {
            System.out.println("error saving release and driver info to  " + MBGlobals.Build390_path+"releasesAndDrivers.ser");
            ioe.printStackTrace();
        }







    }
}
