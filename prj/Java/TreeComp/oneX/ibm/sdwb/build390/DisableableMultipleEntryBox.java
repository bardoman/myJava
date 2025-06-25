package com.ibm.sdwb.build390;
/*********************************************************************/
/* DisableableMultipleEntryBox class for the Build/390 client                 */
/*  allows a user to enable or disable the EditableList entry field*/
/*********************************************************************/
// Changes
// Date     Defect/Feature      Reason
/*********************************************************************/
import java.io.*;
import java.util.*;
import javax.swing.*;
import java.awt.event.*;
import java.awt.*;

/** manage an EditableJList in a user disableable context */
public class DisableableMultipleEntryBox extends DisableableEntryBox {

    private MainframeEditableJList entryList = null;
    private String keyword = null;
    private JPanel internalPanel = null;

    public DisableableMultipleEntryBox(String boxName, String tempKeyword, boolean isEnabled, Vector values) {
        super(boxName, isEnabled);
        if (values == null) {
            values = new Vector();
        }
        entryList = new MainframeEditableJList(values);
        initialize(tempKeyword);
    }

    public DisableableMultipleEntryBox(String boxName, String tempKeyword, int maxLen, boolean isEnabled, Vector values) {
        super(boxName, isEnabled);
        if (values == null) {
            values = new Vector();
        }
        entryList = new MainframeEditableJList(values, maxLen);
        initialize(tempKeyword);
    }

/* set up the layout so the list doesn't expand as the size is changed.  This is necessary for uniformity
between instances of this class
*/
    private void initialize(String tempKeyword) {
        internalPanel = new JPanel(new BorderLayout());
        JScrollPane listScroller = new JScrollPane(entryList);
        internalPanel.add("Center", listScroller);
        listScroller.setPreferredSize(new Dimension(50, 40));

        keyword = tempKeyword;
        initializeComponent(internalPanel);
    }

/*
    setValue - get a vector and set the list contents to that.
*/
    public void setValue(Object newValue) {
        Vector newValues = (Vector) newValue;
        Vector dataToSet = null;
        if (isQuoted()) {
            dataToSet = new Vector();
            for (int i = 0; i < newValues.size(); i++) {
                String oneVal = newValues.elementAt(i).toString();
                if (oneVal.startsWith("'")) {
                    oneVal = oneVal.substring(1);
                }
                if (oneVal.endsWith("'")) {
                    oneVal = oneVal.substring(0, oneVal.length()-1);
                }
                dataToSet.addElement(oneVal);
            }
        } else {
            dataToSet = newValues;
        }
        entryList.setListData((java.util.List)dataToSet);
    }

/*
    getList - get a copy of the List (for assorted and sundry purposes.  Originally added to 
    all counting of items in the list.  
*/  
    public JList getList() {
        return entryList;
    }

    public void setQuoted(boolean tempQuote) {
        super.setQuoted(tempQuote);
        entryList.setAllowBlankAndComma(tempQuote);

    }

    public void setEditable(boolean editable) {
        super.setEditable(editable);
        entryList.setEnabled(editable);
    }

/*
    getSettings - return a string containing the field name + count = value
*/
    public String getSettings() {
        String returnString = new String();
        if (entryList.getModel().getSize() < 1) {
            returnString += keyword +"1="+MBConstants.NEWLINE;
        } else {
            for (int i = 0; i < entryList.getModel().getSize(); i++) {
                String val = (String) entryList.getModel().getElementAt(i);
                if (isQuoted()) {
                    val="'"+val+"'";
                }
                if (!isCaseSensitive()) {
                    val = val.toUpperCase();
                }
                returnString += keyword+(i+1)+"="+val+""+MBConstants.NEWLINE;
            }
        }
        return returnString;
    }

/*
    getNumberOfSettings - return an int with the number of entries
*/
    public int getNumberOfSettings() {
        return entryList.getModel().getSize();
    }

/*
    getKeyword - return a string containing the field name + count 
*/
    public String getKeyword(int i) {
        return keyword+(i+1);
    }

/*
    getValue - return a string containing the value
*/
    public String getValue(int i) {
        String val = (String) entryList.getModel().getElementAt(i);
        if (val != null) {
            if (isQuoted()) {
                val = "'"+val+"'";
            }
            if (!isCaseSensitive()) {
                val = val.toUpperCase();
            }
        }
        return val;
    }
}
