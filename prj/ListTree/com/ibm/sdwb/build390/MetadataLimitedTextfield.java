package com.ibm.sdwb.build390;
/*********************************************************************/
/* Java MetadataLimitedTextfield class for the Build/390 client                          */
/*  limits the number of characters a user can enter into a text field.        */
/*********************************************************************/
//
//
import java.awt.*;
import java.awt.event.*;
import java.util.*;
import javax.swing.*;
import javax.swing.text.*;

/** <br>The MetadataLimitedTextfield class displays an entry field for the user to enter the password into.
* it then sets the passowrd in MBClient. */
public class MetadataLimitedTextfield extends LimitedEntryTextfield{

	private boolean allowBlankAndComma = false;

    /** Constructor - Builds the frame and populates it with the entry field and buttons.
    * It also adds the action listeners.
    */
    public MetadataLimitedTextfield(int size) {
        super(size);
    }

    public MetadataLimitedTextfield(String text, int size) {
        super(text, size);
    }

    protected Document createDefaultModel() {
        return new MetadataDocument();
    }

	public void allowBlanksAndCommas(boolean tempAllow){
		allowBlankAndComma = tempAllow;
	}

    public class MetadataDocument extends LimitedDocument {

        public void insertString(int offs, String str, AttributeSet a) throws BadLocationException {
            if (str == null) {
               return;
            }
            if (str.length() <= 0) {
               return;
            }
			char[] safe = str.toCharArray();
			StringBuffer stringToSave = new StringBuffer();
			for(int i = 0; i < safe.length; i++) {
				boolean validChar = true;
				if(safe[i]=='&' | safe[i]=='\'') {
					Toolkit.getDefaultToolkit().beep();
				}else {
					if(allowBlankAndComma) {
						stringToSave.append(safe[i]);
					}else if(safe[i]==' ' | safe[i]==',') {
						Toolkit.getDefaultToolkit().beep();
					}else {
						stringToSave.append(safe[i]);
					}
				}
			}
            super.insertString(offs, stringToSave.toString(), a);
        }
    }
}
