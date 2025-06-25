package com.ibm.sdwb.build390;
/*********************************************************************/
/* Java MessageBox for Build/390 client                              */
/* This class defines the MsgBox                                     */
/*********************************************************************/
/* Updates:
/* 12/15/99  *wordwrap setWrapStyleWord method added to the textarea to enable word wrapping */
/* 01/14/2000 build.log.1M       changes to display the dialog at a particular location
/* 04/17/2000 okactionlistener   make changes in constructor to allow a display of OK and a userdefined button.
/* 09/26/2000 scrollpaneaddition changes to add the TextArea in a Scrollpane
/* 02/07/2000 textarealengthcount if textarea lengthcount greater than 25 then use a scrollpane.
/* 02/12/2000 textarealengthcount used the text area getText().length() as cut off.
/* 02/16/2000 textarealengthcount had to add the textarea in a panel
/*********************************************************************/
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import javax.swing.*;
import java.util.Hashtable;
import java.util.*;
import com.ibm.sdwb.build390.userinterface.graphic.MainInterface;


/** <br>The MBMsgPanel class provides a method to create modal message boxes.
* <br> There are two constructors.
* The first one creates a msg box that displays information and contains an
* OK button that causes feedback to the requestor when pressed.
* The second one creates a msg box that displays information and contains
* Yes and No buttons.
*/
public class MBMsgPanel extends MBModalFrame {

    private JButton    bu_ok_    = new JButton("OK");  // ok button
    private JButton    bu_yes_   = new JButton("YES"); // yes button
    private JButton    bu_no_    = new JButton("NO");  // no button
    private JButton    bu_success_    = new JButton("SUCCESSFUL");  // no button
    private JButton    bu_all_    = new JButton("ALL");  // no button
    private JButton    bu_none_    = new JButton("NONE");  // no button
    private JButton    bu_userdefined  = new JButton();  // no button
    private JButton focusButton = null;
    private MBButtonPanel buttonPanel;
    private boolean pressedYes = false;
    private boolean pressedSuccess = false;
    private boolean pressedAll = false;
    private boolean pressedNone = false;
    private String    inp;
    //01/14/2000 build.log.1M -  to pass in the location parameters.
    private boolean isLocationPassed = false;
    private Hashtable locationHash = new Hashtable();
    /** Constructor for a modal Message box with an OK button.
    * This Message box is destroyed when the OK button is pressed.
    * @param client Client object requesting the message box.
    * @param title String containing the title of the message box.
    * @param text String containing the text of the message.
    */
    public MBMsgPanel(String title, String text) {
        super(title, null, null);
        isLocationPassed=false;
        Vector actionButtons = new Vector();
        actionButtons.addElement(bu_ok_);
        focusButton = bu_ok_;
        buttonPanel = new MBButtonPanel(null, null, actionButtons);
        getContentPane().add(buttonPanel, "South");
        initializeBox(text, null);
    }


    /** Constructor for a modal Message box with an OK button.
    * This Message box is destroyed when the OK button is pressed.
    * @param client Client object requesting the message box.
    * @param title String containing the title of the message box.
    * @param text String containing the text of the message.
    */
    public MBMsgPanel(String title, String text, Component parentFrame) {
        super(title, parentFrame, null);
        isLocationPassed=false;
        Vector actionButtons = new Vector();
        actionButtons.addElement(bu_ok_);
        focusButton = bu_ok_;
        buttonPanel = new MBButtonPanel(null, null, actionButtons);
        getContentPane().add(buttonPanel, "South");
        initializeBox(text, parentFrame);
    }

    /** Constructor for a modal Message box with YES and NO buttons.
    * This Message box is destroyed when either the YES or NO button is pressed.
    * <br>If not in GUI mode, the data is written to stdout and it waits for command line response.
    * @param client Client object requesting the message box.
    * @param title String containing the title of the message box.
    * @param text String containing the text of the message.
    * @param boolean field indicating that a yes/no msg box is to be created.
    */
    public MBMsgPanel(String title, String text, Component parentFrame, boolean yesno) {
        super(title, parentFrame, null);
        isLocationPassed=false;
        Vector actionButtons = new Vector();
        actionButtons.addElement(bu_yes_);
        actionButtons.addElement(bu_no_);
        focusButton = bu_yes_;
        buttonPanel = new MBButtonPanel(null, null, actionButtons);
        getContentPane().add(buttonPanel, "South");
        initializeBox(text, null);
    }

    /** Constructor for a modal Message box with YES and NO buttons.
    * This Message box is destroyed when either the YES or NO button is pressed.
    * <br>If not in GUI mode, the data is written to stdout and it waits for command line response.
    * @param client Client object requesting the message box.
    * @param title String containing the title of the message box.
    * @param text String containing the text of the message.
    * @param boolean field indicating that a yes/no msg box is to be created.
    */
    public MBMsgPanel(String title, String text, Component parentFrame, boolean yesno, boolean someall) {
        super(title, parentFrame, null);
        isLocationPassed=false;
        Vector actionButtons = new Vector();
        actionButtons.addElement(bu_success_);
        actionButtons.addElement(bu_all_);
        actionButtons.addElement(bu_none_);
        focusButton = bu_success_;
        buttonPanel = new MBButtonPanel(null, null, actionButtons);
        getContentPane().add(buttonPanel, "South");
        initializeBox(text, null);
    }

    /** Constructor for a modal Message box with YES, NO, and userdefined buttons.
    * This Message box is destroyed when either the YES or NO button is pressed.
    * <br>If not in GUI mode, the data is written to stdout and it waits for command line response.
    * @param client Client object requesting the message box.
    * @param title String containing the title of the message box.
    * @param text String containing the text of the message.
    * @param boolean field indicating that a yes/no msg box is to be created.
    */
    public MBMsgPanel(String title, String text, Component parentFrame,boolean yesno, String buttonName, ActionListener al) {
        super(title, parentFrame, null);
        isLocationPassed=false;
        Vector actionButtons = new Vector();
        bu_userdefined.setText(buttonName);
        bu_userdefined.addActionListener(al);
        if (yesno) {
            actionButtons.addElement(bu_yes_);
            actionButtons.addElement(bu_no_);
        } else {
            actionButtons.addElement(bu_ok_);
        }
        actionButtons.addElement(bu_userdefined);
        focusButton = bu_userdefined;
        buttonPanel = new MBButtonPanel(null, null, actionButtons);
        getContentPane().add(buttonPanel, "South");
        initializeBox(text, null);
    }
    /** Constructor for a modal Message box with YES, NO, and userdefined buttons.
   * This Message box is destroyed when either the YES or NO button is pressed.
   * <br>If not in GUI mode, the data is written to stdout and it waits for command line response.
   * @param client Client object requesting the message box.
   * @param title String containing the title of the message box.
   * @param text String containing the text of the message.
   * @param boolean field indicating that a yes/no msg box is to be created.
   * @param Hashtable field contains the position the dialog box needs to displayed.
   */

    public MBMsgPanel(String title, String text, Component parentFrame, boolean yesno,Hashtable tempLocationHash) {
        super(title, parentFrame, null);
        isLocationPassed = true;
        locationHash = tempLocationHash;
        Vector actionButtons = new Vector();
        actionButtons.addElement(bu_yes_);
        actionButtons.addElement(bu_no_);
        focusButton = bu_yes_;
        buttonPanel = new MBButtonPanel(null, null, actionButtons);
        getContentPane().add(buttonPanel, "South");
        initializeBox(text, parentFrame);

    }

    private void initializeBox(String text, Component parentFrame) {
        if (parentFrame == null) {
            parentFrame = MainInterface.getInterfaceSingleton().getframe();
        }
        if (parentFrame != null) {
            setForeground(MBGuiConstants.ColorRegularText);
            setBackground(MBGuiConstants.ColorGeneralBackground);
            //added the debug box in a scrollpane.
            JTextArea ta = new JTextArea(text,5,40);
            ta.setLineWrap(true);
            // *wordwrap added method setWrapStyleWord to true
            ta.setWrapStyleWord(true);

            //check if the length is greater than 5 and less than 25 lines then no scroll bar 
            ta.setBackground(MBGuiConstants.ColorFieldBackground);
            ta.setEditable(false);

            //the getLineCount did nt work.. so use the linelength as cut off.
            if (ta.getText().length() <= 1000){
                JPanel p1 = new JPanel();
                p1.add(ta);
                getContentPane().add(p1,"Center");
            } else {
                ta.setRows(25);
                JScrollPane p1   = new JScrollPane(ta);
                getContentPane().add(p1,"Center");

            }


            bu_ok_.setForeground(MBGuiConstants.ColorActionButton);

            // Handle OK button
            bu_ok_.addActionListener(new ActionListener() {
                                         public void actionPerformed(ActionEvent evt) {
                                             dispose();
                                         }} );
            // Handle YES button
            bu_yes_.addActionListener(new ActionListener() {
                                          public void actionPerformed(ActionEvent evt) {
                                              pressedYes = true;
                                              dispose();
                                          }} );
            // Handle NO button
            bu_no_.addActionListener(new ActionListener() {
                                         public void actionPerformed(ActionEvent evt) {
                                             pressedYes = false;
                                             dispose();
                                         }} );

            bu_success_.addActionListener(new ActionListener() {
                                              public void actionPerformed(ActionEvent evt) {
                                                  pressedSuccess = true;
                                                  pressedNone = false;
                                                  pressedAll = false;
                                                  dispose();
                                              }} );

            bu_all_.addActionListener(new ActionListener() {
                                          public void actionPerformed(ActionEvent evt) {
                                              pressedAll = true;
                                              pressedNone = false;
                                              pressedSuccess = false;
                                              dispose();
                                          }} );

            bu_none_.addActionListener(new ActionListener() {
                                           public void actionPerformed(ActionEvent evt) {
                                               pressedAll = false;
                                               pressedNone = true;
                                               pressedSuccess = false;
                                               dispose();
                                           }} );

            // 01/14/2000 build.log.1M added one more method to pass in the location to display a dialog at a particular position
            if (isLocationPassed) {
                // the locationHastable  - is used to setLocation in MBBasicInternalFrame
                setVisible(true,locationHash);
            } else {
                setVisible(true);

            }
        };
    }

    public void postVisibleInitialization() {
        if (focusButton != null) {
            focusButton.requestFocus();
        }
    }

    public boolean isAnswerYes() {
        return pressedYes;
    }

    public boolean isAnswerAll() {
        return pressedAll;
    }

    public boolean isAnswerNone() {
        return pressedNone;
    }

    public boolean isAnswerSuccessful() {
        return pressedSuccess;
    }
}
