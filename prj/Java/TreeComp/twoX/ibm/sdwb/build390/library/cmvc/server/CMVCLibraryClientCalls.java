package com.ibm.sdwb.build390.library.cmvc.server;

import com.ibm.sdwb.build390.ExternalProgramHandler;
import com.ibm.sdwb.build390.logprocess.LogEventProcessor;
import com.ibm.sdwb.build390.GeneralError;

public class CMVCLibraryClientCalls {

	private LogEventProcessor lep = new LogEventProcessor();

	public void acceptTestRecord(String defect, String release, String environment, String tester) throws GeneralError{
		String[] cmd = new String[10];
		cmd[0]="Test";
		cmd[1]="-accept";
		cmd[2]="-defect";
		cmd[3]=defect;
		cmd[4]="-release";
		cmd[5]=release;
		cmd[6]="-environment";
		cmd[7]=environment;
		cmd[8]="-tester";
		cmd[9]=tester;
		ExternalProgramHandler testRunner = new ExternalProgramHandler(lep);
		testRunner.runCommand(cmd);
	}

	public void abstainTestRecord(String defect, String release, String environment, String tester) throws GeneralError{
		String[] cmd = new String[10];
		cmd[0]="Test";
		cmd[1]="-abstain";
		cmd[2]="-defect";
		cmd[3]=defect;
		cmd[4]="-release";
		cmd[5]=release;
		cmd[6]="-environment";
		cmd[7]=environment;
		cmd[8]="-tester";
		cmd[9]=tester;
		ExternalProgramHandler testRunner = new ExternalProgramHandler(lep);
		testRunner.runCommand(cmd);
	}

	public void rejectTestRecord(String defect, String release, String environment, String tester) throws GeneralError{
		String[] cmd = new String[10];
		cmd[0]="Test";
		cmd[1]="-reject";
		cmd[2]="-defect";
		cmd[3]=defect;
		cmd[4]="-release";
		cmd[5]=release;
		cmd[6]="-environment";
		cmd[7]=environment;
		cmd[8]="-tester";
		cmd[9]=tester;
		ExternalProgramHandler testRunner = new ExternalProgramHandler(lep);
		testRunner.runCommand(cmd);
	}

	public boolean checkForAuthority(String actionType, String component, String cmvcUser) throws GeneralError{
		String[] cmd = new String[7];
		cmd[0]="Access";
		cmd[1]="-check";
		cmd[2]=actionType;
		cmd[3]="-login";
		cmd[4]=cmvcUser;
		cmd[5]="-component";
		cmd[6]=component;
		ExternalProgramHandler testRunner = new ExternalProgramHandler(lep);
		String results = testRunner.runCommand(cmd);
		return results.trim().startsWith("yes");
	}

	public void createTestRecord(String defect, String release, String environment, String tester) throws GeneralError{
		String[] cmd = new String[10];
		cmd[0]="Test";
		cmd[1]="-create";
		cmd[2]="-defect";
		cmd[3]=defect;
		cmd[4]="-release";
		cmd[5]=release;
		cmd[6]="-environment";
		cmd[7]=environment;
		cmd[8]="-tester";
		cmd[9]=tester;
		ExternalProgramHandler testRunner = new ExternalProgramHandler(lep);
		testRunner.runCommand(cmd);
	}	

	public void createEnvironment(String environment, String tester, String release) throws GeneralError{
		String[] cmd = new String[7];
		cmd[0]="Environment";
		cmd[1]="-create";
		cmd[2]=environment;
		cmd[3]="-tester";
		cmd[4]=tester;
		cmd[5]="-release";
		cmd[6]=release;
		ExternalProgramHandler testRunner = new ExternalProgramHandler(lep);
		testRunner.runCommand(cmd);
	}	

	public void readyLevel(String level, String release) throws GeneralError{
		String[] cmd = new String[5];
		cmd[0]="Level";
		cmd[1]="-ready";
		cmd[2]=level;
		cmd[3]="-release";
		cmd[4]=release;
		ExternalProgramHandler testRunner = new ExternalProgramHandler(lep);
		testRunner.runCommand(cmd);
	}	

	public void certifyLevel(String level, String release) throws GeneralError{
		String[] cmd = new String[5];
		cmd[0]="Level";
		cmd[1]="-certify";
		cmd[2]=level;
		cmd[3]="-release";
		cmd[4]=release;
		ExternalProgramHandler testRunner = new ExternalProgramHandler(lep);
		testRunner.runCommand(cmd);
	}	
}
