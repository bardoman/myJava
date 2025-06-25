package com.ibm.sdwb.build390.library.userinterface;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.util.Vector;

import javax.swing.JButton;
import javax.swing.JInternalFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SpringLayout;

import com.ibm.sdwb.build390.MBEditPanel;
import com.ibm.sdwb.build390.MBGlobals;
import com.ibm.sdwb.build390.MBGuiConstants;
import com.ibm.sdwb.build390.MBModalStatusFrame;
import com.ibm.sdwb.build390.MBMsgBox;
import com.ibm.sdwb.build390.MBUtilities;
import com.ibm.sdwb.build390.help.HelpTopicID;
import com.ibm.sdwb.build390.library.LibraryInfo;
import com.ibm.sdwb.build390.user.SetupManager;

public abstract class LibraryInfoEditor extends MBModalStatusFrame {

    private JTextField tfProcessServerName = new JTextField();
    private JButton   MBC_Lbu_ok_      = new JButton(new doOk());
    private JButton btHelp = new JButton("Help");
    private boolean done = false;
    private LibraryInfo libInfo = null;
    JPanel mainPanel = new JPanel(new SpringLayout());
    protected JLabel nameLabel = new JLabel("Name");

    public LibraryInfoEditor(JInternalFrame tempParentFrame, LibraryInfo tempLib) {
        super("Library Setup Information", tempParentFrame, null);

        libInfo = tempLib;
        getContentPane().setLayout(new BorderLayout());
        getContentPane().add(BorderLayout.CENTER, mainPanel);

        nameLabel.setLabelFor(tfProcessServerName);

        mainPanel.add(nameLabel);
        mainPanel.add(tfProcessServerName);
        com.ibm.sdwb.build390.userinterface.graphic.utilities.GeneralUtilities.makeCompactGrid(mainPanel, -1, 2, 5, 5, 3, 3);


        tfProcessServerName.setText(nonNull( libInfo.getProcessServerName()));

        MBC_Lbu_ok_.setForeground(MBGuiConstants.ColorActionButton);
        Vector actionButtons = new Vector();
        actionButtons.addElement(MBC_Lbu_ok_);
        addButtonPanel(btHelp,actionButtons);


        btHelp.addActionListener(new ActionListener() {
                                     public void actionPerformed(ActionEvent evt) {

                                         if (SetupManager.getSetupManager().hasSetup()) {
                                             MBUtilities.ShowHelp("HDRSUYLSC",HelpTopicID.NEWFAMILYDIALOG_HELP);

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
    }

    protected void insertComponentAtTheEnd(Component newComponent) {
        int count = mainPanel.getComponentCount();
        mainPanel.add(newComponent);
        com.ibm.sdwb.build390.userinterface.graphic.utilities.GeneralUtilities.makeCompactGrid(mainPanel, -1, 2, 5, 5, 3, 3);
    }

    protected String doErrorChecking() {
        String famName = tfProcessServerName.getText();
        String errorMessage = new String();
        // verify valid family address
        if (famName !=null && famName.trim().length() < 1) {
            errorMessage +="You must enter a library name.\n";
        } else if (famName.matches(".*\\s.*")) {
            errorMessage+= "You must specify a library name with no spaces.\n";
        }
        return errorMessage;
    }

    public Dimension getPreferredSize() {
        Dimension old = super.getPreferredSize();
        if (old.width < 250) {
            old.width = 250;
        }
        return old;
    }

    public void postVisibleInitialization() {
        tfProcessServerName.requestFocus();
    }

    private String nonNull(String temp) {
        if (temp!=null) {
            return temp;
        }
        return new String();
    }


    private class doOk extends com.ibm.sdwb.build390.userinterface.graphic.widgets.CancelableAction {

        doOk() {
            super("Ok");
        }
        /* the method to override for whatever action you want to perform in response
        to a click.
        */
        public void doAction(ActionEvent e) {
            String errorMessage = doErrorChecking();
            if (errorMessage.trim().length() > 0) {
                new MBMsgBox("Error", errorMessage, thisFrame);
            } else {
                libInfo.setProcessServerName(tfProcessServerName.getText());
                dispose();
            }
        }
    }
}
