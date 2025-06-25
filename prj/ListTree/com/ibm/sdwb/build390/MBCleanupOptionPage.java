
package com.ibm.sdwb.build390;
/*******************************************************************************/
/* This class performs Cleanup OptionDisplay
/*******************************************************************************/
// changes
//Date    Defect/Feature        Reason
//05/31/2000 11 cleanupoption Birth of The Class
/*******************************************************************************/
import java.awt.*;
import java.awt.List;
import java.awt.event.*;
import java.awt.event.KeyEvent;
import java.io.*;
import java.util.*;
import javax.swing.*;
import javax.swing.event.*;
import javax.swing.border.*;
import com.ibm.sdwb.build390.logprocess.*;
import javax.swing.table.*;

public class MBCleanupOptionPage extends MBModalFrame {
    private JButton   btOK        =new JButton("Yes");
    private JButton   btNO        =new JButton("No");
    private JCheckBox optcheckbox =new JCheckBox("Process all selected builds, don't ask me again");
    private JLabel    label       =new JLabel();
    private JLabel    label1      =new JLabel();

    private MBButtonPanel  btnPanel;
    private JPanel   centerPanel   =new JPanel();
    boolean DontAskMeAgain=false;
    boolean AnswerYes=false;
    private final String CLEANUPOPTION="CLEANUPOPTION";
    private String buildid=null;


    public  MBCleanupOptionPage(MBInternalFrame pFrame,LogEventProcessor lep ,final String buildid) throws com.ibm.sdwb.build390.MBBuildException {
        super("Cleanup Confirm Option", pFrame, lep);
        this.buildid=buildid;
        initializeDialog();
    }
    public void initializeDialog() throws com.ibm.sdwb.build390.MBBuildException {
        setForeground(MBGuiConstants.ColorRegularText);
        setBackground(MBGuiConstants.ColorGeneralBackground);
        btOK.setForeground(MBGuiConstants.ColorActionButton);
        btNO.setForeground(MBGuiConstants.ColorActionButton);


        Border blackline = BorderFactory.createLineBorder(MBGuiConstants.ColorRegularText);
        TitledBorder tld = new TitledBorder(blackline,"Cleanup Option : ");
        tld.setTitleColor(MBGuiConstants.ColorGroupHeading);
        centerPanel.setBorder(tld);

        String labelstr =    "Do you want to cleanup build : ";
        label.setText(labelstr + buildid);
        //label1.setForeground(MBGuiConstants.ColorTableHeading);
        //label1.setText(buildid);

        GridBagLayout gridBag = new GridBagLayout();
        centerPanel.setLayout(gridBag);
        GridBagConstraints c = new GridBagConstraints();
        c.gridx = 1;
        c.gridy = 1;
        c.insets = new Insets(1,1,1,1);
        c.weightx = 1;
        c.fill = GridBagConstraints.HORIZONTAL;
        gridBag.setConstraints(label, c);
        centerPanel.add(label);

        c.gridx = 2;
        c.gridy = 1;
        c.insets = new Insets(1,1,1,1);
        c.weightx = 1;
        c.fill = GridBagConstraints.HORIZONTAL;
        gridBag.setConstraints(label1, c);
        centerPanel.add(label1);

        c.gridx = 1;
        c.gridy = 2;
        c.insets = new Insets(1,1,1,1);
        c.gridwidth = GridBagConstraints.REMAINDER;
        c.weightx = 1;
        c.fill = GridBagConstraints.HORIZONTAL;
        gridBag.setConstraints(optcheckbox, c);
        centerPanel.add(optcheckbox);


        /*if (getGeneric(CLEANUPOPTION) != null) {
            if (((Boolean)getGeneric(CLEANUPOPTION)).booleanValue()) {
                optcheckbox.setSelected(true);
            } else {
                optcheckbox.setSelected(false);
            }
        } else {
			optcheckbox.setSelected(false);
		}*/
		optcheckbox.setSelected(false);


        Vector  actionButtons  = new Vector();
        actionButtons.addElement(btOK);
        actionButtons.addElement(btNO);
        btnPanel = new MBButtonPanel(null, null, actionButtons);


        btOK.addActionListener(new ActionListener() {
                                   public void actionPerformed(java.awt.event.ActionEvent A) {
                                       if (optcheckbox.isSelected()) {
                                           DontAskMeAgain=true;
                                       }
                                       AnswerYes=true;
                                       dispose();
                                   }
                               });
        btNO.addActionListener(new ActionListener() {
                                   public void actionPerformed(java.awt.event.ActionEvent A) {
                                       AnswerYes=false;
                                       dispose();
                                   }
                               });


        getContentPane().add("Center",centerPanel);
        getContentPane().add("South",btnPanel);
        setVisible(true);

    }
    public void postVisibleInitialization(){
        btOK.requestFocus();
    }


    protected boolean isDontAskMeAgain(){
        return(DontAskMeAgain);
    }

    protected boolean isAnswerYes(){
        return(AnswerYes);
    }

     // set minimum size
    public Dimension getPreferredSize() {
        Dimension oldPref = new Dimension(320,super.getPreferredSize().height) ;
        return oldPref;
    }

    public void dispose(){
        if (optcheckbox.isSelected()) {
            putGeneric(CLEANUPOPTION,new Boolean(true));
        } else {
            putGeneric(CLEANUPOPTION,new Boolean(false));
        }
        super.dispose();
    }


}


