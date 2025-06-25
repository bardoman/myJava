package com.ibm.sdwb.build390.process;

import com.ibm.sdwb.build390.process.steps.ProcessStep;
import com.ibm.sdwb.build390.logprocess.LogEventProcessor;
import com.ibm.sdwb.build390.*;
import com.ibm.sdwb.build390.userinterface.UserCommunicationInterface;
import java.util.*;
import java.io.File;

public class PartDataRetrieval extends AbstractProcess {
	static final long serialVersionUID = 1111111111111111L;

	private com.ibm.sdwb.build390.process.steps.PartDataRetrieval partDataRetrievalStep = null;
	
	public PartDataRetrieval(MBBuild tempBuild, Set tempPartInfoAndTypeSet, UserCommunicationInterface userComm){
		super("Part Data Retrieval",1, userComm); 
		partDataRetrievalStep = new com.ibm.sdwb.build390.process.steps.PartDataRetrieval(tempBuild,tempPartInfoAndTypeSet, this);
	}

	public void setLocalSavePath(File tempLocalSavePath){
		partDataRetrievalStep.setLocalSavePath(tempLocalSavePath);
	}

	public void setSendToAddress(String tempAddress){
		partDataRetrievalStep.setSendToAddress(tempAddress);
	}

	public void setHFSSavePath(String tempPath){
		partDataRetrievalStep.setHFSSavePath(tempPath);
	}

	public void setPDSSavePath(String tempPath){
		partDataRetrievalStep.setPDSSavePath(tempPath);
	}

	public void setBuildLevel(String tempBuildLevel){
		partDataRetrievalStep.setBuildLevel(tempBuildLevel);
	}

	public Set getLocalOutputFiles(){
		return partDataRetrievalStep.getLocalOutputFiles();
	}

	protected ProcessStep getProcessStep(int stepToGet, int stepIteration) {
		switch (stepToGet) {
			case 0:
				return partDataRetrievalStep;
		}
		return null;
	}
}
