package com.ibm.sdwb.build390.userinterface.graphic.panels.setup;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.util.Vector;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.JInternalFrame;

import com.ibm.sdwb.build390.MBButtonPanel;
import com.ibm.sdwb.build390.MBEditPanel;
import com.ibm.sdwb.build390.MBGlobals;
import com.ibm.sdwb.build390.MBGuiConstants;
import com.ibm.sdwb.build390.MBMainframeInfo;
import com.ibm.sdwb.build390.MBModalFrame;
import com.ibm.sdwb.build390.MBMsgBox;
import com.ibm.sdwb.build390.MBUtilities;
import com.ibm.sdwb.build390.help.HelpTopicID;
import com.ibm.sdwb.build390.user.SetupManager;

/*********************************************************************/
/* Java NewMainframeDialog class for the Build/390 client          */
/*  Asks the user for a password and returns it to the caller        */
// 06/09/99                     fix label
//Thulasi:11/9/00: Attached a help button on the frame to display the help 
// information in a browser.
//12/03/2002 SDWB-2019 Enhance the help system
/*********************************************************************/


/** <br>The NewMainframeDialog class displays an entry field for the user to enter the password into.
* it then sets the passowrd in MBClient. */
public class NewMainframeDialog extends MBModalFrame {

    private JTextField tfHostName = new JTextField();
    private JTextField tfPort = new JTextField();
    private JTextField tfUserID = new JTextField();
    private JTextField tfAccountInfo = new JTextField();
    private JLabel nameLabel = new JLabel("Hostname");
    private JLabel addressLabel = new JLabel("Port");
    private JLabel UIDLabel = new JLabel("TSO User ID");
    private JLabel accountLabel = new JLabel("Account Info");
    private JButton   okButton      = new JButton("OK");
    private JButton btHelp = new JButton("Help");
    private MBButtonPanel buttonPanel;
    private MBModalFrame thisFrame;
    private MBMainframeInfo mainInfo = null;

    /** Constructor - Builds the frame and populates it with the entry field and buttons.
    * It also adds the action listeners.
    */
    public NewMainframeDialog(JInternalFrame tempParentFrame) {
        this(tempParentFrame,  new MBMainframeInfo());
    }


    public NewMainframeDialog(JInternalFrame tempParentFrame, MBMainframeInfo tempMain) {
        super("Build/390 Server Setup Information", tempParentFrame, null);
        thisFrame = this;
        mainInfo = tempMain;
        btHelp.setForeground(MBGuiConstants.ColorHelpButton);
        getContentPane().setLayout(new BorderLayout());
        GridBagLayout gridBag = new GridBagLayout();
        GridBagConstraints c = new GridBagConstraints();
        JPanel labelPanel = new JPanel(gridBag);
        JPanel centerPanel = new JPanel(gridBag);

        c.gridx = 1;
        c.gridy = 1;
        c.weighty = 1;
        c.insets = new Insets(5,10,5,0);
        c.anchor = GridBagConstraints.WEST;
        gridBag.setConstraints(nameLabel, c);
        labelPanel.add(nameLabel);
        c.gridy = 2;
        gridBag.setConstraints(addressLabel, c);
        labelPanel.add(addressLabel);
        c.gridy = 3;
        gridBag.setConstraints(UIDLabel, c);
        labelPanel.add(UIDLabel);
        c.gridy = 4;
        gridBag.setConstraints(accountLabel, c);
        labelPanel.add(accountLabel);
        c.gridx = 2;
        c.gridy = 1;
        c.weightx = 1;
        c.insets = new Insets(5,0,5,10);
        c.fill = GridBagConstraints.HORIZONTAL;
        tfHostName.setText(nonNull(mainInfo.getMainframeAddress()));
        tfHostName.addActionListener(new ActionListener() {
                                         public void actionPerformed(ActionEvent evt) {
                                             doOk();
                                         }
                                     });
        gridBag.setConstraints(tfHostName, c);
        centerPanel.add(tfHostName);
        tfPort.setText(nonNull(mainInfo.getMainframePort()));
        tfPort.addActionListener(new ActionListener() {
                                     public void actionPerformed(ActionEvent evt) {
                                         doOk();
                                     }
                                 });
        c.gridy = 2;
        gridBag.setConstraints(tfPort, c);
        centerPanel.add(tfPort);
        tfUserID.setText(nonNull(mainInfo.getMainframeUsername()));
        tfUserID.addActionListener(new ActionListener() {
                                       public void actionPerformed(ActionEvent evt) {
                                           doOk();
                                       }
                                   });
        c.gridy = 3;
        gridBag.setConstraints(tfUserID, c);
        centerPanel.add(tfUserID);
        tfAccountInfo.setText(nonNull(mainInfo.getMainframeAccountInfo()));
        tfAccountInfo.addActionListener(new ActionListener() {
                                            public void actionPerformed(ActionEvent evt) {
                                                doOk();
                                            }
                                        });
        c.gridy = 4;
        gridBag.setConstraints(tfAccountInfo, c);
        centerPanel.add(tfAccountInfo);

        okButton.setForeground(MBGuiConstants.ColorActionButton);
        Vector actionButtons = new Vector();
        actionButtons.addElement(okButton);
        buttonPanel = new MBButtonPanel(btHelp, null, actionButtons);
        getContentPane().add("West", labelPanel);
        getContentPane().add("Center", centerPanel);
        getContentPane().add("South", buttonPanel);

        // OK button
        okButton.addActionListener(new ActionListener () {
                                          public void actionPerformed(ActionEvent evt) {
                                              doOk();
                                          }
                                      });

        //Thulasi: 11/9/00: Attaching an Action Listener for the help button.
        btHelp.addActionListener(new ActionListener() {
                                     public void actionPerformed(ActionEvent evt) {

                                         if (SetupManager.getSetupManager().hasSetup()) {
                                             MBUtilities.ShowHelp("HDRSUYBSC",HelpTopicID.NEWMAINFRAMEDIALOG_HELP);

                                         } else {
                                             // otherwise show help for setup
                                             String hlpfilenm = new String(MBGlobals.Build390_path+"misc" + java.io.File.separator + "setup.txt");
                                             File hlpfile = new File(hlpfilenm);
                                             if (hlpfile.exists()) {
                                                 MBEditPanel editPanel = new MBEditPanel(hlpfilenm,lep); // don't use MBEdit here because setup has not been done
                                             }
                                         }

                                     }
                                 });

        setVisible(true);
    }

    public void doOk () {
        new Thread(new Runnable() {
                       public void run() {

                           String errorString = new String();
                           boolean hasAllParts = false;

                           String hostName = tfHostName.getText();
                           // verify valid family address
                           if (hostName.trim().length() > 0) {
                               if (hostName.length() == hostName.trim().length()) {
                                   int idx = hostName.indexOf(".");
                                   if (idx > -1) {
                                       int idx1 = hostName.indexOf(".", idx+1);
                                       if (idx1 > -1) {
                                           idx = hostName.indexOf(".", idx1+1);
                                           if (idx > -1) {
                                               hasAllParts = true;
                                           }
                                       }
                                   }
                               } else {
                                   errorString += "The hostname must not have spaces\n";
                               }
                           } else {
                               errorString += "You must enter a hostname\n";
                           }
                           if (!hasAllParts) {
                               errorString += "The hostname must have 4 parts\n";
                           }
                           String hostPort = tfPort.getText();
                           if (hostPort.trim().length() > 0) {
                               if (hostPort.length() == hostPort.trim().length()) {
                                   try {
                                       Integer.parseInt(hostPort);
                                   } catch (NumberFormatException nfe) {
                                       errorString += "The port must be numerical\n";
                                   }
                               } else {
                                   errorString += "The port must not contain spaces\n";
                               }
                           } else {
                               errorString += "You must enter a port\n";
                           }
                           String userID = tfUserID.getText();
                           if (userID.trim().length() > 0) {
                               if (userID.trim().length() != userID.length()) {
                                   errorString += "The user id must not contain spaces\n";
                               }
                           } else {
                               errorString += "You must enter a user id\n";
                           }
                           String accountInfo = tfAccountInfo.getText();
                           if (accountInfo.trim().length() > 0) {
                               if (accountInfo.trim().length() != accountInfo.length()) {
                                   errorString += "The account info must not begin or end with spaces\n";
                               }
                           } else {
                               errorString += "You must enter account info\n";
                           }
                           if (errorString.length() > 0) {
                               new MBMsgBox("Error", errorString, thisFrame);
                           } else {
                               mainInfo.setMainframeAddress(tfHostName.getText());
                               mainInfo.setMainframePort(tfPort.getText());
                               mainInfo.setMainframeUsername(tfUserID.getText());
                               mainInfo.setMainframeAccountInfo(tfAccountInfo.getText());
                               dispose();
                           }
                       }
                   }).start();
    }

    public void postVisibleInitialization() {
        tfHostName.requestFocus();
    }

    public MBMainframeInfo getMainframeInfo() {
        return mainInfo;
    }

    public Dimension getPreferredSize() {
        Dimension old = super.getPreferredSize();
        if (old.width < 250) {
            old.width = 250;
        }
        return old;
    }

    private String nonNull(String temp) {
        if (temp!=null) {
            return temp;
        }
        return new String();
    }
}
