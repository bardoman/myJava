package com.ibm.sdwb.build390.userinterface.graphic.panels;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.*;

import javax.swing.*;
import javax.swing.border.*;

import com.ibm.sdwb.build390.*;
import com.ibm.sdwb.build390.help.HelpTopicID;
import com.ibm.sdwb.build390.userinterface.graphic.widgets.HelpAction;

//********************************************************************
//06/30/2003 #DEF.TST1239:CLEAN ignores SKIP
//********************************************************************

public class CleanupOptionsFrame extends MBModalFrame
{

    private JCheckBox cbAll = new JCheckBox("Delete all data associated with selected builds");
    private JCheckBox cbAllLocal = new JCheckBox("All local data");
    private JCheckBox cbDatasets = new JCheckBox("Host data sets");
    private JCheckBox cbJoboutput = new JCheckBox("Host job output");
    private JCheckBox cbUnlock = new JCheckBox("Unlock driver");
    private JCheckBox cbApplyToAll = new JCheckBox("Apply to all remaining cleanups");
    private boolean closeSuccessfully = false;

    public CleanupOptionsFrame(java.awt.Component parentComponent) {
        super("Cleanup options", parentComponent, null);
        initializeDisplay();
    }

    private void initializeDisplay(){
        getContentPane().setLayout(new java.awt.BorderLayout());
        getContentPane().add("North", cbAll);


        JPanel localDataBox = new JPanel();
        GridBagLayout gridbag = new GridBagLayout();
        GridBagConstraints c = new GridBagConstraints();
        localDataBox.setLayout(gridbag);

        c.gridwidth = GridBagConstraints.REMAINDER;
        c.anchor    = GridBagConstraints.WEST;
        c.weightx = 1.0;
        c.gridx=0;
        c.gridy =0;
        gridbag.setConstraints(cbAllLocal,c);
        localDataBox.add(cbAllLocal);
        localDataBox.setBorder(BorderFactory.createTitledBorder(LineBorder.createGrayLineBorder() ,"Local data options:",TitledBorder.DEFAULT_JUSTIFICATION,TitledBorder.DEFAULT_POSITION,null,MBGuiConstants.ColorGroupHeading));


        JPanel remoteDataBox = new JPanel();
        GridBagLayout gridbag1 = new GridBagLayout();
        GridBagConstraints c1 = new GridBagConstraints();
        remoteDataBox.setLayout(gridbag1);
        remoteDataBox.setBorder(BorderFactory.createTitledBorder(LineBorder.createGrayLineBorder() ,"Remote data options:",TitledBorder.DEFAULT_JUSTIFICATION,TitledBorder.DEFAULT_POSITION,null,MBGuiConstants.ColorGroupHeading));

        c1.gridwidth = GridBagConstraints.REMAINDER;
        c1.weightx = 1.0;
        c1.anchor    = GridBagConstraints.WEST;
        c1.gridx=0;
        c1.gridy =0;
        gridbag1.setConstraints(cbDatasets,c1);
        remoteDataBox.add(cbDatasets);

        c1.gridwidth = GridBagConstraints.REMAINDER;
        c1.weightx = 1.0;
        c1.anchor    = GridBagConstraints.WEST;
        c1.gridx=0;
        c1.gridy =1;
        gridbag1.setConstraints(cbJoboutput,c1);
        remoteDataBox.add(cbJoboutput);

        c1.gridwidth = GridBagConstraints.REMAINDER;
        c1.weightx = 1.0;
        c1.anchor    = GridBagConstraints.WEST;
        c1.gridx=0;
        c1.gridy =2;
        gridbag1.setConstraints(cbUnlock,c1);
        remoteDataBox.add(cbUnlock);

        JPanel bothPanels = new JPanel(new BorderLayout());
        bothPanels.add(BorderLayout.NORTH,localDataBox);
        bothPanels.add(BorderLayout.SOUTH,remoteDataBox);
        getContentPane().add("Center", bothPanels);

        JButton okButton = new JButton("Ok");
        okButton.addActionListener(new ActionListener(){
                                       public void actionPerformed(ActionEvent e) {
                                           doOk();
                                       }
                                   });
        JButton skipButton = new JButton("Skip");
        skipButton.addActionListener(new ActionListener(){
                                         public void actionPerformed(ActionEvent e) {
                                             //Begin #DEF.TST1239:
                                             cbAll.setSelected(false);
                                             cbAllLocal.setSelected(false);
                                             cbDatasets.setSelected(false);
                                             cbJoboutput.setSelected(false);
                                             cbUnlock.setSelected(false);
                                             //End #DEF.TST1239:
                                             setVisible(false);
                                         }
                                     });
        JPanel bottomPanel = new JPanel(new BorderLayout());
        bottomPanel.add(BorderLayout.NORTH,cbApplyToAll);
        Box buttonBox = Box.createHorizontalBox();
        buttonBox.add(Box.createHorizontalGlue());
        buttonBox.add(okButton);
        buttonBox.add(Box.createHorizontalGlue());
        buttonBox.add(new JButton(new HelpAction("",HelpTopicID.CLEANUP_OPTIONS_HELP)));
        buttonBox.add(Box.createHorizontalGlue());
        buttonBox.add(skipButton);
        buttonBox.add(Box.createHorizontalGlue());
        bottomPanel.add(BorderLayout.SOUTH, buttonBox);
        getContentPane().add("South", bottomPanel);

        cbAll.addItemListener(new ItemListener() {
                                  public void itemStateChanged(ItemEvent ie) {
                                      if(cbAll.isSelected())
                                      {
                                          cbAllLocal.setSelected(true);
                                          cbDatasets.setSelected(true);
                                          cbJoboutput.setSelected(true);
                                          cbUnlock.setSelected(true);

                                      }
                                  }});

    }

    public void setVisible(boolean visible){
        if(visible)
        {
            closeSuccessfully = false;
        }
        super.setVisible(visible);
    }

    private void doOk(){
        closeSuccessfully = true;
        setVisible(false);
    }

    public boolean isLocalDataSelected(){
        return cbAllLocal.isSelected();
    }

    public boolean isHostDataSetsSelected(){
        return cbDatasets.isSelected();
    }

    public boolean isMVSJobOutputSelected(){
        return cbJoboutput.isSelected();
    }

    public boolean isUnlockSelected(){
        return cbUnlock.isSelected();
    }

    public boolean applyToAll(){
        return cbApplyToAll.isSelected();
    }

    void setBuildIdToDisplayInTitle(String buildidTitle){
        setTitle("Cleanup options for "+buildidTitle);
    }

    public void internalFrameClosed(javax.swing.event.InternalFrameEvent e) {
//		setVisible(false);
    }
}
