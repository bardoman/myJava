package com.ibm.sdwb.build390;
/*********************************************************************/
/* DisableableCheckBox class for the Build/390 client                 */
/*  allows a user to enable or disable the check box*/
/*********************************************************************/
// Changes
// Date     Defect/Feature      Reason
/*********************************************************************/
import java.io.*;
import java.util.*;
import javax.swing.*;
import java.awt.event.*;
import java.awt.*;

/** manage the disableable Checkbox */
public class DisableableCheckBox extends DisableableEntryBox{

	private JCheckBox entryCheck = null;
	private String keyword = null;

    public DisableableCheckBox(String boxName, String tempKeyword, boolean isEnabled, boolean isSelected) {
		super(boxName, isEnabled);
		entryCheck = new JCheckBox("", isSelected);
		keyword = tempKeyword;
		initializeComponent(entryCheck);
	}

/* setValue - takes an Object that has to be a string and sets the checkbox to true if the value
	passed in is "on"
	@param Object newValue - cast to a string and set checkbox to true ifthe value is "ON"
*/	
	public void setValue(Object newValue){
		entryCheck.setSelected(((String) newValue).toUpperCase().equals("ON"));
	}

/*
	getSettings - return a string containing the field name = ON or OFF
*/
	public String getSettings(){
		String value;
		if (entryCheck.isSelected()) {
			value = "ON";
		} else {
			value = "OFF";
		}
		return keyword + "="+value+MBConstants.NEWLINE;
	}

/*
	getKeyword - return a string containing the field name 
*/
	public String getKeyword(int i){
		return keyword;
	}

/*
	getValue - return a string containing ON or OFF
*/
	public String getValue(int i){
		String value;
		if (entryCheck.isSelected()) {
			value = "ON";
		} else {
			value = "OFF";
		}
		return value;
	}
}
