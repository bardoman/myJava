package com.ibm.sdwb.build390.library.cmvc.userinterface;

import java.awt.*;
import java.awt.event.*;
import java.util.*;
import javax.swing.*;
import java.io.*;
import com.ibm.sdwb.build390.help.*;
import com.ibm.sdwb.build390.*;
import com.ibm.sdwb.build390.library.cmvc.*;
import com.ibm.sdwb.build390.library.userinterface.DefaultLibraryInfoEditor;

public class CMVCLibraryInfoEditor extends DefaultLibraryInfoEditor {

	private JTextField tfCmvcPort = new JTextField();
	private JTextField tfCmvcUsername = new JTextField();
	private JCheckBox cbPasswordAuthentication = new JCheckBox("Use password authentication");
	private CMVCLibraryInfo libInfo = null;

	public CMVCLibraryInfoEditor(JInternalFrame tempParentFrame, CMVCLibraryInfo tempLib) {
		super(tempParentFrame, tempLib);

		libInfo = tempLib;
		insertComponentBeforePortLabel(new JLabel("CMVC Port"));
		insertComponentBeforePortLabel(tfCmvcPort);
		insertComponentBeforePortLabel(new JLabel("CMVC Username"));
		insertComponentBeforePortLabel(tfCmvcUsername);
		insertComponentBeforePortLabel(new JLabel()); // to make it line up
		insertComponentBeforePortLabel(cbPasswordAuthentication);

		if (libInfo.getCMVCPort() > 0) {
			tfCmvcPort.setText(Integer.toString(libInfo.getCMVCPort()));
		}
		tfCmvcUsername.setText(nonNull(libInfo.getUsername()));
		cbPasswordAuthentication.setSelected(libInfo.isUsingPasswordAuthentication());
		setVisible(true);
	}

	protected String doErrorChecking() {
		String errorMessage = super.doErrorChecking();
		String cmvcPort = tfCmvcPort.getText();
		String cmvcUsername = tfCmvcUsername.getText();
		try {
			Integer.parseInt(cmvcPort);
		}catch (NumberFormatException nfe){
			errorMessage += "You must enter a number for the CMVC port.\n";
		}
		if(cmvcUsername.trim().length() < 1) {
			errorMessage +="You must enter a username.\n";
		}

		if(errorMessage.trim().length() == 0) {
			libInfo.setCMVCPort(Integer.parseInt(cmvcPort));
			libInfo.setUsername(cmvcUsername);
			libInfo.setUsingPasswordAuthentication(cbPasswordAuthentication.isSelected());
		}
		return errorMessage;
	}

	public Dimension getPreferredSize() {
		Dimension old = super.getPreferredSize();
		if(old.width < 250) {
			old.width = 250;
		}
		return old;
	}

	private String nonNull(String temp){
		if (temp!=null) {
			return temp;
		}
		return new String();
	}
}
