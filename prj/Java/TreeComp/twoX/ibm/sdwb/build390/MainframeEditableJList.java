package com.ibm.sdwb.build390;
/*********************************************************************/
/* MainframeEditableJList class for the Build/390 client                 */
/*  a JList with a popup menu which allows various operations on the list memebers*/
/*	(insert (above, below, at end) remove, edit, add)*/
/*********************************************************************/
// Changes
// Date     Defect/Feature      Reason
/*********************************************************************/
import java.io.*;
import java.util.*;
import javax.swing.*;
import javax.swing.event.*;
import java.awt.event.*;
import java.awt.*;

/** Create the driver build page */
public class MainframeEditableJList extends EditableJList {

    private boolean allowBlankAndComma = false;


    public MainframeEditableJList(Vector values) {
        super(values);
    }

    public MainframeEditableJList(Vector values, int maxLength) {
        super(values, maxLength);
    }

    public void setAllowBlankAndComma(boolean tempAllow){
        allowBlankAndComma = tempAllow;
    }

    public String getNewEntry(String originalValue, JInternalFrame parentWindow) {
        TextEntryDialog userInput;
        if (allowBlankAndComma) {
            if (getModel().getSize() >0) { /*TST2238 allow only one NULL value in the multi entry list */
                for (int i=0;i<getModel().getSize() && allowBlankAndComma;i++) {
                    allowBlankAndComma = ((String)getModel().getElementAt(i)).trim().length() >0;
                }
            }
        }
        if (originalValue != null) {
            if (maxEntryLength > 0) {
                userInput= new TextEntryDialog("Enter the new value:",originalValue, parentWindow, maxEntryLength, true, allowBlankAndComma);
            } else {
                userInput= new TextEntryDialog("Enter the new value:",originalValue, parentWindow, true, allowBlankAndComma);
            }
        } else {
            if (maxEntryLength > 0) {
                userInput= new TextEntryDialog("Enter the new value:", parentWindow, maxEntryLength, true, allowBlankAndComma);
            } else {
                userInput= new TextEntryDialog("Enter the new value:", parentWindow, true, allowBlankAndComma);
            }
        }
        return userInput.getText();
    }


}
