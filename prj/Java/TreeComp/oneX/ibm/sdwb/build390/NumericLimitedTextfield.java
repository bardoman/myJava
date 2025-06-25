package com.ibm.sdwb.build390;
/*********************************************************************/
/* Java NumericLimitedTextfield class for the Build/390 client                          */
/*  limits the number of characters a user can enter into a text field.        */
/*********************************************************************/
//
//
import java.awt.*;
import java.awt.event.*;
import java.util.*;
import javax.swing.*;
import javax.swing.text.*;

/** <br>The NumericLimitedTextfield class displays an entry field for the user to enter the password into.
* it then sets the passowrd in MBClient. */
public class NumericLimitedTextfield extends LimitedEntryTextfield{

    /** Constructor - Builds the frame and populates it with the entry field and buttons.
    * It also adds the action listeners.
    */
    public NumericLimitedTextfield(int size) {
        super(size);
    }

    public NumericLimitedTextfield(String text, int size) {
        super(text, size);
    }

    protected Document createDefaultModel() {
        return new NumericDocument();
    }

    public class NumericDocument extends LimitedDocument {

        public void insertString(int offs, String str, AttributeSet a) throws BadLocationException {
            if (str == null) {
               return;
            }
            if (str.length() <= 0) {
               return;
            }
            if (!Character.isDigit(str.charAt(0))) {
                Toolkit.getDefaultToolkit().beep();
                return;
            }
            super.insertString(offs, str, a);
        }
    }
}
