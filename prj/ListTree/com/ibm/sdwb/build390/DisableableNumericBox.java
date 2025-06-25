package com.ibm.sdwb.build390;
/*********************************************************************/
/* DisableableNumericBox class for the Build/390 client                 */
/*  allows a user to enable or disable the numeric entry field*/
/*********************************************************************/
// Changes
// Date     Defect/Feature      Reason
/*********************************************************************/
import java.io.*;
import java.util.*;
import javax.swing.*;
import java.awt.event.*;
import java.awt.*;

/** manage a disableable NumericLimitedTextfield */
public class DisableableNumericBox extends DisableableEntryBox
{

    private NumericLimitedTextfield entryField = null;
    private String keyword = null;

    public DisableableNumericBox(String boxName, String tempKeyword, boolean isEnabled, int maxLength)
    {
        super(boxName, isEnabled);
        entryField = new NumericLimitedTextfield(maxLength);
        keyword = tempKeyword;
        initializeComponent(entryField);
    }

/*
    set the value of the textfield to whatever the string passed in is.
*/
    public void setValue(Object newValue)
    {
        entryField.setText(newValue.toString());
    }

/*
    getSettings - return a string containing the field name = numeric string
*/
    public String getSettings()
    {
        String value = entryField.getText();
        if(value != null)
        {
            value = value.trim();
        }
        else
        {
            value = " ";
        }
        return keyword + "="+value+MBConstants.NEWLINE;
    }

/*
    getKeyword - return a string containing the field name 
*/
    public String getKeyword(int i)
    {
        return keyword;
    }

/*
    getValue - return a string containing the numeric string
*/
    public String getValue(int i)
    {
        String value = entryField.getText();
        if(value != null)
        {
            value = value.trim();
        }
        else
        {
            value = " ";
        }
        return value;
    }
}
