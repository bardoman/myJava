package com.ibm.sdwb.build390;
/*********************************************************************/
/* Java Status hanlder for Build/390 client                          */
/* This class defines Status handling.                               */
/* Status handling is done in this class seperate from the gui so    */
/* that status is handled correctly wether in gui mode or not.       */
/*********************************************************************/
/* Updates:                                                          */
/*********************************************************************/
import javax.swing.*;

/** <br>The MBStatus class provides methods to manage status information. */
public class MBStatus {

    private JTextField statusBar = null;                           // text field
    private String Title;                        // title info
    private String statusText = null;

    /** Constructor for MBStatus. 
    * @param client Client object requesting status change.
    */
    public MBStatus(JTextField statBar) {
        if (statBar != null) {
            statBar.setEditable(false);
        }
        statusText = new String();
        statusBar = statBar;
    }

    /** Constructor for MBStatus. 
    * If in gui mode, updates the status field, otherwise prints the status info.
    * @param client Client object requesting status change.
    * @param title String containing the title of the status box.
    * @param text String containing the text of the message.
    */
/*    
    public MBStatus(MBInternalFrame intFrame, String title, String text) {
        if (intFrame != null) {
            internalFrame = intFrame;
            Title = title;
            internalFrame.setStatusBarText(title+":"+text);
        }else System.out.println(title+":"+text);
    }
*/
    /** Clears the status area.
    * @param client Client that created the status box
    */
    public void clearStatus() {
        if (statusBar != null){
            statusBar.setText("");
        }else {
            statusText = "";
        }
    }

    /** Updates the text in the status area.
    * @param client Client that is requesting the status change.
    * @param text String containing the new text for the status box.
    * @param appendtxt boolean indicating that the text is to be appended
    * instead of replacing tha current text.
    */
    public void updateStatus(String text, boolean appendtxt) {
        if (statusBar != null) {
            if (appendtxt) {
                String ot = statusBar.getText()+text;
                statusBar.setText(ot);
            }
            //else MBClient.mbgui.setStatustxt(Title+":"+text);
            else
                statusBar.setText(text);
        }else{
            if (appendtxt) {
                statusText +=text;
            }else {
                statusText = text;
            }
            System.out.println(statusText);
        }
    }
    
    /** Updates the text in the status area.
    * @param client Client that is requesting the status change.
    * @param text String containing the new text for the status box.
    * @param appendtxt boolean indicating that the text is to be appended
    * instead of replacing tha current text.
    */
    public String getStatus() {
        if (statusBar != null) {
            return statusBar.getText();
        }else {
            return statusText;
        }
    }
}
