package com.ibm.sdwb.build390;
/*********************************************************************/
/* DisableableTextBox class for the Build/390 client                 */
/*  allows a user to enable or disable the text entry field*/
/*********************************************************************/
// Changes
// Date     Defect/Feature      Reason
/*********************************************************************/
import java.io.*;
import java.util.*;
import javax.swing.*;
import java.awt.event.*;
import java.awt.*;

/** manage the disableable Textfield */
public class DisableableTextBox extends DisableableEntryBox
{

    private MetadataLimitedTextfield entryField = null;
    private String keyword = null;

    public DisableableTextBox(String boxName, String tempKeyword, boolean isEnabled, int maxLength, boolean tempAllow)
    {
        super(boxName, isEnabled);
        entryField = new MetadataLimitedTextfield(maxLength);
        entryField.allowBlanksAndCommas(tempAllow);
        keyword = tempKeyword;
        initializeComponent(entryField);
    }

    public void setQuoted(boolean tempQuote)
    {
        super.setQuoted(tempQuote);
        entryField.allowBlanksAndCommas(tempQuote);

    }

/* setValue - set the value of the textfield to the string passed in
    @param Object newValue - cast to a string & set textfield
*/  
    public void setValue(Object newValue)
    {
        String oneVal = newValue.toString();
        if(oneVal.startsWith("'"))
        {
            oneVal = oneVal.substring(1);
        }
        if(oneVal.endsWith("'"))
        {
            oneVal = oneVal.substring(0, oneVal.length()-1);
        }

        entryField.setText(oneVal);
    }

/*
    getSettings - return a string containing the field name = textfield value
*/
    public String getSettings()
    {
        String value = entryField.getText();
        if(value != null)
        {
            value = value.trim();
            if(isQuoted())
            {
                value = "'"+value+"'";
            }
            if(!isCaseSensitive())
            {
                value = value.toUpperCase();
            }
        }
        else
        {
            value = new String();
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
    getValue - return a string containing the textfield value
*/
    public String getValue(int i)
    {
        String value = entryField.getText();
        if(value != null)
        {
            value = value.trim();
            if(isQuoted())
            {
                value = "'"+value+"'";
            }
            if(!isCaseSensitive())
            {
                value = value.toUpperCase();
            }
        }
        else
        {
            value = new String();
        }
        return value;
    }
}
