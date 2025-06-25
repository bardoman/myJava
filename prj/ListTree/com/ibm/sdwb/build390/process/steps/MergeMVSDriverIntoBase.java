package com.ibm.sdwb.build390.process.steps;

import com.ibm.sdwb.build390.*;
import com.ibm.sdwb.build390.logprocess.LogEventProcessor;
import com.ibm.sdwb.build390.mainframe.*;
import java.util.*;
import java.io.*;

public class MergeMVSDriverIntoBase extends MainframeCommunication {
	static final long serialVersionUID = 1111111111111111L;

	private MBMainframeInfo mainInfo = null;
	private DriverInformation driverInfo = null;
    private static Map driverLockMap = null;

	public MergeMVSDriverIntoBase(MBMainframeInfo tempMain, String tempOutputPath, DriverInformation tempInfo, com.ibm.sdwb.build390.process.AbstractProcess tempProc) {
		super(tempOutputPath+"MERGEDRIVER-"+tempInfo.getRelease().getMvsName()+"-"+tempInfo.getName(),"Merge MVS driver into base", tempProc);
		setVisibleToUser(true);
		setUndoBeforeRerun(false);
		mainInfo = tempMain;
		driverInfo = tempInfo;
	}

	/**
	 * This is the method that should be implemented to actually
	 * run the process.	Use executionArgument if you need to 
	 * access the argument from the execute method.
	 * 
	 * @return Object indicating output of the step.
	 */
	public void execute() throws com.ibm.sdwb.build390.MBBuildException{
        synchronized(MBClient.client){
            if (driverLockMap==null) {
                driverLockMap = new HashMap();
            }
        }
        Object driverLock = null;
        String lockKey = driverInfo.getBaseDriver().getRelease().getMvsHighLevelQualifier()+"."+driverInfo.getBaseDriver().getRelease().getMvsName()+"."+driverInfo.getBaseDriver().getName();
        synchronized(driverLockMap){
            // unique string to identify the base driver.  May be a bit excessive, but I'd rather it work
            driverLock = driverLockMap.get(lockKey);
            if (driverLock==null) {
                driverLock = new Object();
                driverLockMap.put(lockKey, driverLock);
            }
        }
		getLEP().LogSecondaryInfo(getFullName(),"Entry");
		String mergeDriverCommand = "DRVRMERG DRIVER="+driverInfo.getName()+", FAMHLQ="+driverInfo.getRelease().getMvsHighLevelQualifier()+", REL="+driverInfo.getRelease().getMvsName();
        createMainframeCall(mergeDriverCommand, "Merging driver" , mainInfo);
        dontAllowHostCallCancel();
        String output = "about "+(new Date()).getTime()+"  to synch " + mergeDriverCommand+"\n\n";
        synchronized(driverLock){   // just need to make sure only one of these runs at a time
            runMainframeCall();
        }
        driverInfo.setBaseDriver(null);
	}
}
