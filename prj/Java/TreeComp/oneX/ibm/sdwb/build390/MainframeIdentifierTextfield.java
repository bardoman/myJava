package com.ibm.sdwb.build390;
/*********************************************************************/
/* Java MainframeIdentifierTextfield class for the Build/390 client                          */
/*  limits the number of characters a user can enter into a text field.        */
/*********************************************************************/
//
//
import java.awt.*;
import java.awt.event.*;
import java.util.*;
import javax.swing.*;
import javax.swing.text.*;

/** <br>The MainframeIdentifierTextfield class displays an entry field for the user to enter the password into.
* it then sets the passowrd in MBClient. */
public class MainframeIdentifierTextfield extends LimitedEntryTextfield{
    private static final int identifierLength = 8;

    /** Constructor - Builds the frame and populates it with the entry field and buttons.
    * It also adds the action listeners.
    */
    public MainframeIdentifierTextfield() {
        super(identifierLength);
    }

    public MainframeIdentifierTextfield(String text) {
        super(text, identifierLength);
    }

    protected Document createDefaultModel() {
        return new MainframeDocument();
    }

    public class MainframeDocument extends LimitedDocument {

        public void insertString(int offs, String str, AttributeSet a) throws BadLocationException {
            if (str == null) {
               return;
            }
            if (str.length() <= 0) {
               return;
            }
            str = str.toUpperCase();
            if (offs == 0) {
                if (!Character.isLetter(str.charAt(0))) {
                    Toolkit.getDefaultToolkit().beep();
                    return;
                }
            }
            if (!Character.isLetterOrDigit(str.charAt(0))) {
                Toolkit.getDefaultToolkit().beep();
                return;
            }
            super.insertString(offs, str, a);
        }
    }
}
