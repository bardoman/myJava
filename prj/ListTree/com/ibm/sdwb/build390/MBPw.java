package com.ibm.sdwb.build390;
/*********************************************************************/
/* Java MBPw class for the Build/390 client                          */
/*  Asks the user for a password and returns it to the caller        */
/*********************************************************************/
// 05/17/99 FixMinSize      Set minimum window size
//
import java.awt.*;
import java.awt.event.*;
import java.util.*;
import javax.swing.*;

/** <br>The MBPw class displays an entry field for the user to enter the password into.
* it then sets the passowrd in MBClient. */
public class MBPw extends MBModalFrame{

   private String password = null;
   private  static boolean isAlive = false;
   private JPasswordField tf1 = new JPasswordField();
   private JLabel userLabel;
   private JButton   MBC_Lbu_ok_      = new JButton("OK");
   private JButton   MBC_Lbu_quit_    = new JButton("Cancel");
   private ActionListener qh;
   private ActionListener selh ;
   private MBButtonPanel buttonPanel;
   private static Object pwLock = new Object();
   private boolean userCancel = false;

    /** Constructor - Builds the frame and populates it with the entry field and buttons.
    * It also adds the action listeners.
    */
    public MBPw(String userAtHostKey) {
        super("Enter Password", null, null);
        userLabel = new JLabel("Enter password for "+ userAtHostKey);
        setForeground(MBGuiConstants.ColorRegularText);
        setBackground(MBGuiConstants.ColorGeneralBackground);
        GridBagLayout gridBag = new GridBagLayout();
        GridBagConstraints c = new GridBagConstraints();
        c.gridx = 1;
        c.gridy = 1;
        getContentPane().setLayout(gridBag);

        tf1.setEchoChar('*');
        tf1.setBackground(MBGuiConstants.ColorFieldBackground);
        tf1.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                doOk();
            }
        });
        c.insets = new Insets(5,5,5,5);
        c.gridwidth = GridBagConstraints.REMAINDER;
        c.weightx = 1;
        c.fill = GridBagConstraints.HORIZONTAL;
        gridBag.setConstraints(userLabel, c);
        getContentPane().add(userLabel);
        c.gridy = 2;
        c.insets = new Insets(5,5,10,5);
        c.gridwidth = GridBagConstraints.REMAINDER;
        c.weightx = 1;
        c.fill = GridBagConstraints.HORIZONTAL;
        gridBag.setConstraints(tf1, c);
        getContentPane().add(tf1);
        MBC_Lbu_ok_.setForeground(MBGuiConstants.ColorActionButton);
        MBC_Lbu_quit_.setForeground(MBGuiConstants.ColorCancelButton);
        Vector actionButtons = new Vector();
        actionButtons.addElement(MBC_Lbu_ok_);
        buttonPanel = new MBButtonPanel(null, MBC_Lbu_quit_, actionButtons);
        c.gridy = 3;
        c.insets = new Insets(0,0,0,0);
        gridBag.setConstraints(buttonPanel, c);
        getContentPane().add(buttonPanel);
        setResizable(false); // FixMinSize

        // OK button
        MBC_Lbu_ok_.addActionListener(selh = new ActionListener () {
            public void actionPerformed(ActionEvent evt) {
                doOk();
            }
        });
        // Quit button
        MBC_Lbu_quit_.addActionListener(qh = new ActionListener () {
            public void actionPerformed(ActionEvent evt) {
                dispose();
            }
        });

        setVisible(true);
    }

    public void postVisibleInitialization(){
        tf1.requestFocus();
    }


    public Dimension getPreferredSize() {
        Dimension old = super.getPreferredSize();
        //old.width = old.width * 2;
        return old;
    }

    public void doOk (){
         password = new String(tf1.getPassword());          // get the text from the pw field
         if (password != null & password.length() > 0) {                          // check for length > 0
            // clean up and get out
            dispose();
            userCancel = false;
         }
    }

    public void dispose() {
    	userCancel = true;
    	super.dispose();
    }

    public String getPassword(){
        return password;
    }

    public boolean wasUserCancelled() {
    	return userCancel;
    }
}
