package com.ibm.sdwb.build390;
/*******************************************************************************/
/* This class performs Find Next function to enable the user to see the next matches
/*******************************************************************************/
// changes
//Date    Defect/Feature        Reason
//05/10/2000 EKM001 ParSearcher Birth of The Class
/*******************************************************************************/
import java.util.Iterator;
import java.util.NoSuchElementException;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JViewport;
import javax.swing.JTable;
import javax.swing.border.LineBorder;
import javax.swing.border.TitledBorder;
import javax.swing.BorderFactory;
import javax.swing.border.Border;

import java.awt.GridBagLayout;
import java.awt.GridBagConstraints;

import java.util.Vector;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import com.ibm.sdwb.build390.logprocess.LogEventProcessor;

public class MBLogRetrieveFindNextMatch extends MBModalFrame {
    private JButton  btFindNext      =new JButton(" Find Next");
    private JButton  btNo            =new JButton(" Cancel Find Next ");
    private JLabel searchAgainLabel  = new JLabel();

    private MBButtonPanel  btnPanel;
    private JPanel   centerPanel  =new JPanel();
    private JTable headerTable=null;
    
    private Iterator iteratorkeys=null;
    private String title=null;
    private MBStatus status=null;
    private JViewport rha=null;
    private JViewport rhb=null;
    private JTable logInfoTable=null;


    public  MBLogRetrieveFindNextMatch(MBInternalFrame pFrame,LogEventProcessor lep,JTable headerTable,JTable logInfoTable,Iterator iteratorkeys,String title,JViewport rha,JViewport rhb,MBStatus status) throws com.ibm.sdwb.build390.MBBuildException {
        super("Find Next", pFrame, lep);
        this.headerTable=headerTable;
        this.logInfoTable=logInfoTable;
        this.iteratorkeys=iteratorkeys;
        this.title=title;
        this.status = status;
        this.rha=rha;
        this.rhb=rhb;
        initializeDialog();
    }
    public void initializeDialog() throws com.ibm.sdwb.build390.MBBuildException {
        setForeground(MBGuiConstants.ColorRegularText);
        setBackground(MBGuiConstants.ColorGeneralBackground);
        btFindNext.setForeground(MBGuiConstants.ColorActionButton);
        btNo.setForeground(MBGuiConstants.ColorActionButton);

        Border blackline = BorderFactory.createLineBorder(MBGuiConstants.ColorRegularText);
        TitledBorder tld = new TitledBorder(blackline,"Find Again");
        tld.setTitleColor(MBGuiConstants.ColorGroupHeading);
        centerPanel.setBorder(tld);



        GridBagLayout gridBag = new GridBagLayout();
        centerPanel.setLayout(gridBag);
        GridBagConstraints c = new GridBagConstraints();
        c.gridx = 1;
        c.gridy = 1;
        c.weightx = 1;
        c.fill = GridBagConstraints.HORIZONTAL;
        c.gridwidth = GridBagConstraints.REMAINDER;
        searchAgainLabel.setForeground(MBGuiConstants.ColorTableHeading);
        searchAgainLabel.setText("Find Next: " + title);
        gridBag.setConstraints(searchAgainLabel, c);
        centerPanel.add(searchAgainLabel);

        Vector  actionButtons  = new Vector();
        actionButtons.addElement(btFindNext);
        actionButtons.addElement(btNo);
        btnPanel = new MBButtonPanel(null, null, actionButtons);


        btFindNext.addActionListener(new ActionListener() {
                                         public void actionPerformed(java.awt.event.ActionEvent A) {
                                             try {
                                                 int j = ((Integer)iteratorkeys.next()).intValue();
                                                 headerTable.setRowSelectionAllowed(true);
                                                 headerTable.setRowSelectionInterval(j,j);
                                                 status.updateStatus("",false);

                                                 

                                                 if (logInfoTable!=null) {
                                                     java.awt.Rectangle rect = logInfoTable.getCellRect(j,0,true);
                                                     logInfoTable.scrollRectToVisible(rect);
                                                     logInfoTable.invalidate();
                                                 }

                                                 java.awt.Rectangle rect1 = headerTable.getCellRect(j,0,true);
                                                 headerTable.scrollRectToVisible(rect1);
                                                 headerTable.invalidate();


                                             } catch (NoSuchElementException nsme) {
                                                 status.updateStatus("No more matches",false);
                                                 btFindNext.setEnabled(false);
                                             }

                                         }
                                     });

        btNo.addActionListener(new ActionListener() {
                                   public void actionPerformed(java.awt.event.ActionEvent a){
                                       dispose();
                                   }});
        getContentPane().add("Center",centerPanel);
        getContentPane().add("South",btnPanel);
        setVisible(true);

    }
    public void postVisibleInitialization(){
        btFindNext.requestFocus();
    }

}


