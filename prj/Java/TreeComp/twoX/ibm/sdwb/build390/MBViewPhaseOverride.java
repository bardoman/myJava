package com.ibm.sdwb.build390;
/*********************************************************************/
/* MBViewPhaseOverride class for the Build/390 client                */
/*  Parse phase override settings from Driver Build report           */
/*********************************************************************/
// 12/17/98                     Update option terminology
// 12/20/2000 pjs 				show all overrides in one window - rewrote class
/*********************************************************************/
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import javax.swing.*;
import java.util.*;

/**
* View Phase Override Settings
* phase override setting string format: buildType:phaseNum:override
* if it is match to buildType and there is phase override settings,
* enable View button.  This is view only dialog.
*/
public class MBViewPhaseOverride {

    private static final int AUTOBLD_INDEX = 0;
    private static final int FORCE_INDEX   = 1;
    private static final int LISTGEN_INDEX = 2;
    private static final int RUNSCAN_INDEX = 3;
    private static final int BUILDCC_INDEX = 4;

    /**
    * Create the Options Dialog
    * @param Frame parent
    * @param boolean modal
    */
	public MBViewPhaseOverride(String buildType, java.util.List phases)  {

		// sample overides in BLDORDER
		//      BFG1=XXFN4=G,
		//      BFG2=XXYY8=G
	    // in this order -> autobld, force, listgen, runscan, buildcc
        
		// but each phases overrides into this array so they can be shown in the correct order
		java.util.List messages = new ArrayList();
		// Loop on phases, pull setting for each phase and build a msg
		for (Iterator phaseIterator = phases.iterator(); phaseIterator.hasNext();) {
			com.ibm.sdwb.build390.mainframe.PhaseInformation onePhase = (com.ibm.sdwb.build390.mainframe.PhaseInformation) phaseIterator.next();
		 	String phaseNum  = Integer.toString(onePhase.getPhaseNumber());
			String phaseSet  = onePhase.getPhaseOverrides();
			String title     = "Overrides for Phase: " + phaseNum;
			if (phaseSet !=null) {
				char[] overRide = phaseSet.toCharArray();      
				// Get AUTOBLD and FORCE setting
				// AUTOBLD = Y => build all dependent parts
				// AUTOBLD = N => only build parts in the build list
				// AUTOBLD = M => build all dependent parts
				// FORCE   = A => build all parts in the driver
				// FORCE   = Y => build parts in the part list
				// FORCE   = N => build parts that are not built
				// y,y "Unconditionally build all parts in list and all dependent parts";
				// y,n "Build all unbuilt parts in list and all dependent parts";
				// m,x "Build all unbuilt parts in list and parts with explicit dependencies";
				// n,x "Build only unbuilt parts";
				// y,a "Unconditionally build everything in the driver and partlist";         
				String depPart = null;
				switch (overRide[AUTOBLD_INDEX]) {
					case 'Y': // autobuild = yes            
						switch (overRide[FORCE_INDEX]) {
							case 'Y':
								depPart = "Unconditionally build all parts in list and all dependent parts";
							break;
							case 'N':
								depPart = "Build all unbuilt parts in list and all dependent parts";
							break;
							case 'A':
								depPart = "Unconditionally build everything in the driver and partlist";
							break;
						}
					break;
					case 'N': // autobuild = no
						depPart = "Build only unbuilt parts";
					break;
					case 'M': // autobuild = manual
						depPart = "Build all unbuilt parts in list and parts with explicit dependencies";
					break;
				}
				if (depPart != null) 
					depPart = "Dependent part processing = "+depPart;

				// Get LISTGEN setting
				// "Do not save any listings"; // listgen=no
				// "Save failed listings";     // listgen=fail
				// "Save good listings";	   // listgen=yes
				// "Save all listings";		   // listgen=all
				String listgen = null;
				switch(overRide[LISTGEN_INDEX]) {
					case 'Y':
						listgen = "Save good listings";
					break;
					case 'N':
						listgen = "Do not save any listings";
					break;
					case 'F':
						listgen = "Save failed listings";
					break;
					case 'A':
						listgen = "Save all listings";
					break;
				}
				if (listgen != null)
					listgen = "Listings = "+listgen;

				// Get RUNSCAN setting
				String runscan = null;
				switch(overRide[RUNSCAN_INDEX]) {
					case 'Y':
						runscan = "YES";
					break;
					case 'N':
						runscan = "NO";
					break;
				}
				if (runscan != null) 
					runscan = "Run scanners and checkers = "+runscan;

				// Get BUILDCC setting
				char cterm = overRide[BUILDCC_INDEX];
				String term = null;
				term = "Termination criteria = "+cterm;

				// build msg text for this phase
				String text = new String();
				if (depPart != null) text += " -"+depPart+"\n";
				if (listgen != null) text += " -"+listgen+"\n";
				if (runscan != null) text += " -"+runscan+"\n";
				if (term != null) 	 text += " -"+term+"\n";
				if (text.length() > 1) {
					messages.add(title+"\n"+text+"\n\n");
				} else {
					messages.add(title+" None\n\n");
				}
			}else {
				messages.add(title+" None\n\n");
			}
		} 

		// Show msgs to user in phase order
		if (!messages.isEmpty()) {
			String omsg = new String();
			for (Iterator messageIterator = messages.iterator(); messageIterator.hasNext();) {
				omsg += (String) messageIterator.next();
			}
			MBMsgBox mb = new MBMsgBox("Overrides for buildtype "+buildType, omsg);
		}
	}
}
