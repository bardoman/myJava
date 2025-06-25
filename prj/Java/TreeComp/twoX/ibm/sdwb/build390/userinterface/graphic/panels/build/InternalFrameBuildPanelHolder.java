package com.ibm.sdwb.build390.userinterface.graphic.panels.build;

import com.ibm.sdwb.build390.*;
import com.ibm.sdwb.build390.userinterface.graphic.utilities.*;
import com.ibm.sdwb.build390.userinterface.graphic.widgets.*;
import com.ibm.sdwb.build390.logprocess.*;
import javax.swing.*;
import java.util.*;

//08/09/2007 make it resizeable

public class InternalFrameBuildPanelHolder extends MBInternalFrame implements MBSaveableFrame, BuildFrameInterface {
    private MBInternalFrame thisFrame = null;

    private JButton btBuild = new JButton("Build");
    private JButton btHelp = new JButton("Help");
    private BuildPanel buildPanel = null;

    public InternalFrameBuildPanelHolder(String title, LogEventProcessor tempLep) throws com.ibm.sdwb.build390.MBBuildException{

        super(title, true, tempLep);

        layoutPanel();
    }

    private void layoutPanel() {
        thisFrame = this;
        setForeground(MBGuiConstants.ColorRegularText);
        setBackground(MBGuiConstants.ColorGeneralBackground);
        btBuild.setForeground(MBGuiConstants.ColorActionButton);
        btHelp.setForeground(MBGuiConstants.ColorHelpButton);
        Vector actionButtons = new Vector();
        actionButtons.addElement(btBuild);
        addButtonPanel(btHelp, actionButtons);
        setDefaultCloseOperation(JInternalFrame.DO_NOTHING_ON_CLOSE);
    }

    public void setBuildPanel(BuildPanel newPanel) {
        buildPanel=newPanel;
        getContentPane().add(java.awt.BorderLayout.CENTER, buildPanel);
        setVisible(true);
    }

    public BuildPanel getBuildPanel() {
        return buildPanel;
    }

    public void restart()throws MBBuildException {
        btBuild.doClick();
    }

    public void setBuildButtonEnabled(boolean enabled) {
        btBuild.setEnabled(enabled);
    }

    public void setBuildAction(javax.swing.Action action) {
        btBuild.setAction(action);
    }

    public JButton getHelpButton() {
        return btHelp;
    }

    public void dispose() {
        dispose(true);
    }

    public void handleUIEvent(com.ibm.sdwb.build390.userinterface.event.UserInterfaceEvent event) {
        buildPanel.handleUIEvent(event);
    }

    public boolean save()throws MBBuildException{
        return buildPanel.save();
    }

    public boolean saveNeeded() {
        return buildPanel.saveNeeded();
    }

    public void dispose(boolean showSaveDialog) {
        super.dispose(showSaveDialog);
        buildPanel.handleDisposeTimeActions();
    }

    public void internalFrameClosing(javax.swing.event.InternalFrameEvent e) {
        dispose();
    }

    public MBInternalFrame getInternalFrame() {
        return this;
    }
   
    public java.awt.Dimension getMinimumSize() {
        java.awt.Dimension oldPref = new java.awt.Dimension(540, 540);
        return oldPref;
    }
}
