package com.ibm.sdwb.build390;
/*********************************************************************/
/* Java LimitedEntryTextfield class for the Build/390 client                          */
/*  limits the number of characters a user can enter into a text field.        */
/*********************************************************************/
//
//
import java.awt.*;
import java.awt.event.*;
import java.util.*;
import javax.swing.*;
import javax.swing.text.*;

/** <br>The LimitedEntryTextfield class displays an entry field for the user to enter the password into.
* it then sets the passowrd in MBClient. */
public class LimitedEntryTextfield extends JTextField{

    /** Constructor - Builds the frame and populates it with the entry field and buttons.
    * It also adds the action listeners.
    */
    public LimitedEntryTextfield(int size) {
        super(size);
    }

    public LimitedEntryTextfield(String text, int size) {
        super(text, size);
    }

    public Dimension getMaximumSize(){
        return getPreferredSize();
    }

	public Dimension getPreferredSize() {
		Dimension prefSize = super.getPreferredSize();
		prefSize.width = (prefSize.width * 7) / 5;
        return prefSize;
	}

    protected Document createDefaultModel() {
        return new LimitedDocument();
    }

    public class LimitedDocument extends PlainDocument {

        public void insertString(int offs, String str, AttributeSet a) throws BadLocationException {
            if (str == null) {
               return;
            }
            if (getLength()+str.length() <= getColumns()){
                super.insertString(offs, str, a);
            }else {
                Toolkit.getDefaultToolkit().beep();
            }
        }
    }
}
