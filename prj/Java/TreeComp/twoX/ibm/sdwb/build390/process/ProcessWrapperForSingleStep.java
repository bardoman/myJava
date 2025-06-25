package com.ibm.sdwb.build390.process;

import com.ibm.sdwb.build390.logprocess.LogEventProcessor;
import com.ibm.sdwb.build390.*;
import java.util.*;
import com.ibm.sdwb.build390.process.steps.ProcessStep;
import com.ibm.sdwb.build390.userinterface.UserCommunicationInterface;

// this class is to be used if you need to run a single step operation from the GUI and it doesn't have it's own process

public class ProcessWrapperForSingleStep extends AbstractProcess {
	static final long serialVersionUID = 1111111111111111L;

	private ProcessStep theStep = null;
	
	public ProcessWrapperForSingleStep(UserCommunicationInterface tempUserComm){
		super("Process Wrapper",1, tempUserComm); 
	}

	public void setStep(ProcessStep tempStep) {
		theStep = tempStep;
                setName(getName() + " (" +  tempStep.getName()+ ") ");
	}

	protected ProcessStep getProcessStep(int stepToGet, int stepIteration) {
		switch (stepToGet) {
			case 0:
				return theStep;
		}
		return null;
	}
}
