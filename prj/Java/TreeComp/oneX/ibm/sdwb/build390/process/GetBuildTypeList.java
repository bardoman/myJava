package com.ibm.sdwb.build390.process;

import com.ibm.sdwb.build390.process.steps.ProcessStep;
import com.ibm.sdwb.build390.logprocess.LogEventProcessor;
import com.ibm.sdwb.build390.library.LibraryInfo;
import com.ibm.sdwb.build390.mainframe.DriverInformation;
import com.ibm.sdwb.build390.userinterface.UserCommunicationInterface;
import com.ibm.sdwb.build390.*;
import java.util.*;
import java.io.File;

public class GetBuildTypeList extends AbstractProcess {
	static final long serialVersionUID = 1111111111111111L;

	private com.ibm.sdwb.build390.process.steps.DriverReport buildTypeListStep = null;
	
	public GetBuildTypeList(DriverInformation tempDriver, MBMainframeInfo tempMain, LibraryInfo tempLib, File saveLocation, UserCommunicationInterface userComm){
		super("Get Build Type List",1, userComm); 
		if (saveLocation == null) {
			saveLocation = new File(MBGlobals.Build390_path+"misc");
		}
		buildTypeListStep = new com.ibm.sdwb.build390.process.steps.DriverReport(tempDriver,tempMain, tempLib, saveLocation, this);
	}

	public List getListOfBuildTypes(){
		return 	buildTypeListStep.getParser().getBuildTypes();
	}

	protected ProcessStep getProcessStep(int stepToGet, int stepIteration) {
		switch (stepToGet) {
			case 0:
				return buildTypeListStep;
		}
		return null;
	}
}
