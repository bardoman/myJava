package com.ibm.sdwb.build390;
/*********************************************************************/
/* Java NonBlankTextEntryDialog class for the Build/390 client                          */
/*  Asks the user for a String and returns it to the caller        */
/*********************************************************************/
//
import java.awt.*;
import java.awt.event.*;
import java.util.*;
import javax.swing.*;

/** <br>The NonBlankTextEntryDialog class displays an entry field for the user to enter the password into.
* it then sets the passowrd in MBClient. */
public class NonBlankTextEntryDialog extends TextEntryDialog{

    /** Constructor - Builds the frame and populates it with the entry field and buttons.
    * It also adds the action listeners.
    */
    public NonBlankTextEntryDialog(String question, JInternalFrame parentFrame) {
        super(question, parentFrame);
	}
		
    public NonBlankTextEntryDialog(String question, JInternalFrame parentFrame, int maxLen) {
        super(question, parentFrame, maxLen);
	}
		
	public NonBlankTextEntryDialog(String question, String tempInit, JInternalFrame parentFrame) {
        super(question, tempInit, parentFrame);

	}
	
	public NonBlankTextEntryDialog(String question, String tempInit, JInternalFrame parentFrame, int maxLen) {
        super(question, tempInit, parentFrame, maxLen);
	}
	

    public void doOk (){
         if (tf1.getText().trim().length() > 0) {                          // check for length > 0
			 userEntry = new String(tf1.getText());          // get the text from the pw field
            // clean up and get out
            dispose();
         } else {
			 problemBox("Invalid entry", "You cannot enter a blank value, use *NONE* instead");
		 }
    }
}
