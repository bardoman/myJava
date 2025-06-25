package com.ibm.sdwb.build390.process;

import com.ibm.sdwb.build390.process.steps.ProcessStep;
import com.ibm.sdwb.build390.logprocess.LogEventProcessor;
import com.ibm.sdwb.build390.userinterface.UserCommunicationInterface;
import com.ibm.sdwb.build390.*;
import java.util.*;

public class ListPDSMembers extends AbstractProcess {
	static final long serialVersionUID = 1111111111111111L;

	private MBBuild build = null;
	private com.ibm.sdwb.build390.process.steps.ListPDSMembers listPDSMembersStep = null;
	
	public ListPDSMembers(MBBuild tempBuild, String PDSToList,  UserCommunicationInterface userComm){
		super("List PDS Members",1, userComm); 
		build = tempBuild;
		listPDSMembersStep = new com.ibm.sdwb.build390.process.steps.ListPDSMembers(build, PDSToList, this);
	}

	public List getPDSMemberList(){
		return listPDSMembersStep.getPDSMemberList();
	}

	protected ProcessStep getProcessStep(int stepToGet, int stepIteration) {
		switch (stepToGet) {
			case 0:
				return listPDSMembersStep;
		}
		return null;
	}
}
