package com.ibm.sdwb.build390.process;

import com.ibm.sdwb.build390.process.steps.ProcessStep;
import com.ibm.sdwb.build390.logprocess.LogEventProcessor;
import com.ibm.sdwb.build390.userinterface.UserCommunicationInterface;
import com.ibm.sdwb.build390.*;
import java.util.*;

public class MetadataReport extends AbstractProcess {
	static final long serialVersionUID = 1111111111111111L;

	private com.ibm.sdwb.build390.process.steps.MetadataReport metadataReportStep = null;
	
	public MetadataReport(MBBuild tempBuild, java.io.File tempLocalSavePath, Set tempPartInfoAndTypeSet, UserCommunicationInterface userComm){
		super("Metadata Retrieval",1, userComm); 
		metadataReportStep = new com.ibm.sdwb.build390.process.steps.MetadataReport(tempBuild, tempLocalSavePath,tempPartInfoAndTypeSet, this);
	}

	public void setBuildLevel(String tempLevel){
		metadataReportStep.setBuildLevel(tempLevel);
	}

        public void setHFSSavePath(String tempPath){
                metadataReportStep.setHFSSavePath(tempPath);
        }

        public void setPDSSavePath(String tempPath){
                metadataReportStep.setPDSSavePath(tempPath);
        }

	public void setJustGetFields(boolean temp){
		metadataReportStep.setJustGetFields(temp);
	}

	public MetadataType[] getMetadataTypes(){
		return metadataReportStep.getMetadataTypes();
	}

	public Set getLocalOutputFiles(){
		return metadataReportStep.getLocalOutputFiles();
	}

        public Set getHostSavedLocation(){
		return metadataReportStep.getHostSavedLocation();
	}

	protected ProcessStep getProcessStep(int stepToGet, int stepIteration) {
		switch (stepToGet) {
			case 0:
				return metadataReportStep;
		}
		return null;
	}

        public int getReturnCode(){
            if(metadataReportStep!=null){
                return metadataReportStep.getReturnCode();
            }
            return -1;
        }
}
