package com.ibm.sdwb.build390.library.clearcase.userinterface;

import javax.swing.*;
import com.ibm.rational.clearcase.*;
import com.ibm.sdwb.build390.library.clearcase.*;
import com.ibm.sdwb.build390.library.userinterface.DefaultLibraryInfoEditor;

public class ClearcaseLibraryInfoEditor extends DefaultLibraryInfoEditor {

	private ClearcaseLibraryInfo libInfo = null;
	private JTextField tfProjectVOB = new JTextField();

	public ClearcaseLibraryInfoEditor(JInternalFrame tempParentFrame, ClearcaseLibraryInfo tempLib) {
		super(tempParentFrame, tempLib);
		libInfo = tempLib;
		additionalLayout();
		setVisible(true);
	}

	private void additionalLayout(){
        nameLabel.setText("Region");
        addressLabel.setText("Registry Server Address");
		insertComponentBeforePortLabel(new JLabel("Project VOB"));
		insertComponentBeforePortLabel(tfProjectVOB);
		if (libInfo.getProjectVob()!=null) {
			tfProjectVOB.setText(libInfo.getProjectVob());
		}
	}

	protected String doErrorChecking(){
		String errorMessage = super.doErrorChecking();
		String pvob = tfProjectVOB.getText();
		if (!pvob.startsWith("\\")) {
			pvob = "\\"+pvob;
		}
		try {
			ClearToolAPI.isValidProjectVOB(pvob);
		}catch (CTAPIException cte){
			errorMessage +="The PVOB you entered was not found.\n";
		}
		if(errorMessage.trim().length() == 0) {
			libInfo.setProjectVob(pvob);
		}
		return errorMessage;
	}

	public java.awt.Dimension getPreferredSize() {
		java.awt.Dimension old = super.getPreferredSize();
		if(old.width < 250) {
			old.width = 250;
		}
		return old;
	}

}
