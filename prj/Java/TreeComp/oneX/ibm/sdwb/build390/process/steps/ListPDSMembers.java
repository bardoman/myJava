package com.ibm.sdwb.build390.process.steps;

import com.ibm.sdwb.build390.*;
import com.ibm.sdwb.build390.logprocess.LogEventProcessor;
import com.ibm.sdwb.build390.mainframe.*;
import java.util.*;
import java.io.*;

public class ListPDSMembers extends MainframeCommunication {
	static final long serialVersionUID = 1111111111111111L;

	private MBBuild build = null;
	private String pdsName = null;
	private List pdsMemberList = null;

	public ListPDSMembers(MBBuild tempBuild, String tempPDS, com.ibm.sdwb.build390.process.AbstractProcess tempProc) {
		super(MBGlobals.Build390_path+"misc"+File.separator+"LISTPDS-"+tempPDS,"Get list of PDS Members", tempProc);
		setVisibleToUser(true);
		setUndoBeforeRerun(false);
		build = tempBuild;
		pdsName = tempPDS.toUpperCase();
	}

	public List getPDSMemberList(){
		return pdsMemberList;
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
		String listPDSCommand = "LISTPDS PDS="+pdsName;
		
        createMainframeCall(listPDSCommand, "Getting list of PDS members" , build.getSetup().getMainframeInfo());
        runMainframeCall();
		try {
			BufferedReader pdsListReader = new BufferedReader(new FileReader(getOutputFile()));
			ParseMembers(pdsListReader);
			pdsListReader.close();
		}catch (IOException ioe){
			throw new HostError("Reading report "+getOutputFile().getAbsolutePath(), ioe);
		}

	}

    private void ParseMembers(BufferedReader reportReader) throws IOException {
		pdsMemberList = new ArrayList();
		int pos;
		String currentLine = new String();
		while ((currentLine = reportReader.readLine())!=null) {
			currentLine = currentLine.trim();
			if (currentLine.indexOf("Member list for")< 0){
				if (currentLine.length()>0) {
					pdsMemberList.add(currentLine);
				}
			}
		}
    }
}
