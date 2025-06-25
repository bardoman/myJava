package com.ibm.sdwb.build390;
/*********************************************************************/
/* Java MessageBox for Build/390 client                              */
/* This class defines the MsgBox                                     */
/*********************************************************************/
/* Updates:                                                          */
// 01/14/2000 build.log.1M changes to display the msgbox at a particular location
// 04/17/2000 okactionlistener changes to add a userdefined button + ok button
/*********************************************************************/
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import javax.swing.*;
import java.util.Hashtable;
import java.util.Vector;
import com.ibm.sdwb.build390.userinterface.graphic.MainInterface;

/** <br>The MBMsgBox class provides a method to create modal message boxes.
* <br> There are two constructors.
* The first one creates a msg box that displays information and contains an
* OK button that causes feedback to the requestor when pressed.
* The second one creates a msg box that displays information and contains
* Yes and No buttons.
*/
public class MBMsgBox {

    private boolean pressedYes = false;
    private MBMsgPanel msgPanel = null;
    private String    inp;

    /** Constructor for a modal Message box with an OK button.
    * This Message box is destroyed when the OK button is pressed.
    * @param client Client object requesting the message box.
    * @param title String containing the title of the message box.
    * @param text String containing the text of the message.
    */
    public MBMsgBox(String title, String text) {
        if (MainInterface.getInterfaceSingleton() != null) {
            msgPanel = new MBMsgPanel(title, text);
        } else System.out.println(title+":"+text);
    }


    /** Constructor for a modal Message box with an OK button.
    * This Message box is destroyed when the OK button is pressed.
    * @param client Client object requesting the message box.
    * @param title String containing the title of the message box.
    * @param text String containing the text of the message.
    */
    public MBMsgBox(String title, String text, Component parentFrame) {
        if (MainInterface.getInterfaceSingleton() != null) {
            msgPanel = new MBMsgPanel(title, text, parentFrame);
        } else System.out.println(title+":"+text);
    }

    /** Constructor for a modal Message box with YES and NO buttons.
    * This Message box is destroyed when either the YES or NO button is pressed.
    * <br>If not in GUI mode, the data is written to stdout and it waits for command line response.
    * @param client Client object requesting the message box.
    * @param title String containing the title of the message box.
    * @param text String containing the text of the message.
    * @param boolean field indicating that a yes/no msg box is to be created.
    */
    public MBMsgBox(String title, String text, Component parentFrame, boolean yesno) {
        if (MainInterface.getInterfaceSingleton() != null) {
            msgPanel = new MBMsgPanel(title, text, parentFrame, yesno);
        } else System.out.println(title+":"+text);
    }        

    /** Constructor for a modal Message box with YES and NO buttons.
    * This Message box is destroyed when either the YES or NO button is pressed.
    * <br>If not in GUI mode, the data is written to stdout and it waits for command line response.
    * @param client Client object requesting the message box.
    * @param title String containing the title of the message box.
    * @param text String containing the text of the message.
    * @param boolean field indicating that a yes/no msg box is to be created.
    */
    public MBMsgBox(String title, String text, Component parentFrame, boolean yesno, boolean allsome) {
        if (MainInterface.getInterfaceSingleton() != null) {
            msgPanel = new MBMsgPanel(title, text, parentFrame, yesno, allsome);
        } else System.out.println(title+":"+text);
    }        

    /** Constructor for a modal Message box with user defined buttons.
    * This Message box is destroyed when either the YES or NO button is pressed.
    * <br>If not in GUI mode, the data is written to stdout and it waits for command line response.
    * @param client Client object requesting the message box.
    * @param title String containing the title of the message box.
    * @param text String containing the text of the message.
    * @param buttonName String containing the text of the user defined button.
    * @param al - actionListener for the user defined button.
    * @param boolean field indicating that a yes/no msg box is to be created or if false creates a ok button
    */
    public MBMsgBox(String title, String text, Component parentFrame, boolean yesno,String buttonName, ActionListener al) {
        if (MainInterface.getInterfaceSingleton() != null) {
            msgPanel = new MBMsgPanel(title, text, parentFrame,yesno, buttonName, al);
        } else System.out.println(title+":"+text);
    }



    /** Constructor for a modal Message box with user defined buttons.
    * This Message box is destroyed when either the YES or NO button is pressed.
    * <br>If not in GUI mode, the data is written to stdout and it waits for command line response.
    * @param client Client object requesting the message box.
    * @param title String containing the title of the message box.
    * @param text String containing the text of the message.
    * @param boolean field indicating that a yes/no msg box is to be created.
    * @param Hashtable field indicating the position the textbox needs to be displayed.
    */
    //01/14/2000 build.log.1M to display the msgbox at a particular loc, the location parameters are passed in a Hastable        
    public MBMsgBox(String title, String text, Component parentFrame, boolean yesno,Hashtable locationHash) {
        if (MainInterface.getInterfaceSingleton() != null) {
            msgPanel = new MBMsgPanel(title, text, parentFrame, yesno,locationHash);
        } else System.out.println(title+":"+text);
    }

    public boolean isAnswerYes() {
        if (msgPanel != null) {
            return msgPanel.isAnswerYes();
        } else {
            return false;
        }
    }

    public boolean isAnswerSuccessful() {
        if (msgPanel != null) {
            return msgPanel.isAnswerSuccessful();
        } else {
            return false;
        }
    }

    public boolean isAnswerNone() {
        if (msgPanel != null) {
            return msgPanel.isAnswerNone();
        } else {
            return false;
        }
    }

    public boolean isAnswerAll() {
        if (msgPanel != null) {
            return msgPanel.isAnswerAll();
        } else {
            return false;
        }
    }
}
