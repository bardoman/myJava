package com.ibm.sdwb.build390.process.steps.mainframe;

import com.ibm.sdwb.build390.*;
import com.ibm.sdwb.build390.logprocess.LogEventProcessor;
import com.ibm.sdwb.build390.mainframe.*;
import com.ibm.sdwb.build390.process.steps.*;
import java.util.*;
import java.io.*;

public class RetrievePassticket extends MainframeCommunication {
	static final long serialVersionUID = 1111111111111111L;

	private MBMainframeInfo mainInfo = null;
	private String passticket = null;

	public RetrievePassticket(String outputPath, MBMainframeInfo tempMainInfo, com.ibm.sdwb.build390.process.AbstractProcess tempProc) {
		super(outputPath+"PASTK","Passticket request", tempProc);
		setVisibleToUser(false);
		mainInfo = tempMainInfo;
	}

	public String getPassticket(){
		return passticket;
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
		String passticketRequestCommand = "PASTK";
		for (passticket = null; passticket == null;) {
			createMainframeCall(passticketRequestCommand, "Passticket request" , mainInfo);
			runMainframeCall();
			passticket = parseOutputForPassticket();
		}

	}

	private String parseOutputForPassticket() throws GeneralError{
		String tempTicket = null;
		try {
			BufferedReader ResultFileReader = new BufferedReader(new FileReader(getOutputFile()));
			String currentLine = new String();
			if ((currentLine = ResultFileReader.readLine()) != null) {
				if (currentLine.trim().length() == 8) {
					ResultFileReader.close();
					tempTicket = currentLine;
				}
			} else {
				ResultFileReader.close();
				throw new GeneralError("Failed to obtain a pass ticket from the Build/390 server.");
			}
		} catch (IOException ioe) {
			throw new GeneralError("Failed to obtain a pass ticket from the Build/390 server.", ioe);
		}
		// racf is giving us invalid passwords here, so check it for validity
		// Passwords must conatin at least 1 numeric
		// Thulasi: 10/12/00: Passwords can start with a numeric or alpha.
		for (int x=0; x<8; x++) {
			if (Character.isDigit(tempTicket.charAt(x))){
				return tempTicket;
			}
		}
		return null;
	}
}
