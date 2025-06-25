package com.ibm.sdwb.build390.library.clearcase.userinterface;

import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;
import java.awt.event.*;
import java.util.*;
import java.io.*;
import com.ibm.rational.clearcase.*;
import com.ibm.sdwb.build390.*;
import com.ibm.sdwb.build390.library.SourceInfo;
import com.ibm.sdwb.build390.logprocess.*;
import com.ibm.sdwb.build390.library.clearcase.*;
import com.ibm.sdwb.build390.library.userinterface.SourceSelection;
import com.ibm.sdwb.build390.userinterface.graphic.widgets.*;
import com.ibm.sdwb.build390.userinterface.graphic.panels.*;
import com.ibm.sdwb.build390.userinterface.graphic.MainInterface;
import com.ibm.sdwb.build390.userinterface.UserCommunicationInterface;
import com.ibm.sdwb.build390.userinterface.event.*;
import com.ibm.sdwb.build390.userinterface.event.build.*;
import javax.swing.event.*;



public class ClearcaseSourceSelection extends SourceSelection implements java.awt.event.ActionListener,  RequiredActionsCompletedInterface {

    private JTextField releaseField = null;
    private ViewMountPointChooser viewMountCombo = null;
    private ViewChooser viewCombo = null;
    private JCheckBox cbDeltaBuild = new JCheckBox("Delta Build");
    private ClearcaseLibraryInfo libInfo = null;
    private MBMainframeInfo mainInfo = null;
    private File buildablesFile = null;
    private String lastView = null;

    public ClearcaseSourceSelection(com.ibm.sdwb.build390.library.LibraryInfo tempLib, MBMainframeInfo tempMain) {
        libInfo = (ClearcaseLibraryInfo) tempLib;
        mainInfo = tempMain;

        Box centerBox = Box.createVerticalBox();
        add(centerBox);

        Box horizontalProjectBox = Box.createHorizontalBox();
        releaseField = new JTextField();
        releaseField.setEditable(false);
        horizontalProjectBox.add(new JLabel("Project"));
        horizontalProjectBox.add(releaseField);
        ViewListener viewListener = new ViewListener(this);

        viewMountCombo = new ViewMountPointChooser();

        viewCombo = new ViewChooser();
        viewCombo.addActionListener(this);
        viewCombo.addActionListener(viewListener);

        centerBox.add(viewMountCombo);
        centerBox.add(Box.createGlue());
        centerBox.add(horizontalProjectBox);
        centerBox.add(Box.createGlue());
        centerBox.add(viewCombo);
    }

    public SourceInfo getSourceInfo() {
        return new ClearcaseViewSourceInfo(libInfo, viewCombo.getSelectedView(), getProjectChosen().getLibraryName(), viewMountCombo.getSelectedMountPoint());
    }

    public void setSourceInfo(SourceInfo info) { //***BE
        viewCombo.setSelectedView(info.getName());
        releaseField.setText(info.getProject());
        viewMountCombo.setSelectedMountPoint(((ClearcaseViewSourceInfo)info).getMountPoint());
    }

    public com.ibm.sdwb.build390.mainframe.ReleaseInformation getProjectChosen() {
        com.ibm.sdwb.build390.mainframe.ReleaseInformation relInfo = mainInfo.getReleaseByLibraryName(releaseField.getText(),libInfo );
        if (relInfo !=null) {
            return relInfo;
        }
        return null;
    }

    public void actionPerformed(java.awt.event.ActionEvent e) {
        try {
            String viewChosen = viewCombo.getSelectedView();
            if (viewChosen != null) {
                if (!viewChosen.equals(lastView)) {
                    ProjectInfo project = ClearToolAPI.getProjectInfoForView(viewCombo.getSelectedView());
                     if (!project.getName().equals(releaseField.getText())) {
                         releaseField.setText(project.getName());
                         ReleaseUpdateEvent rue = new ReleaseUpdateEvent(releaseField);
                         rue.setReleaseInformation(mainInfo.getReleaseByLibraryName(project.getName(), libInfo));
                         fireEvent(rue);
                    }
                    lastView = viewChosen;
                }
            }
        } catch (CTAPIException cte) {
            throw new RuntimeException(cte);
        }
        UserInterfaceEvent newEvent = new UserInterfaceEvent(this);
        fireEvent(newEvent);
    }

    public boolean isRequiredActionCompleted() {
        /* Check that the view, project, and mount point have been filled in*/
        if (viewCombo.getSelectedView()!=null) {
            if (viewCombo.getSelectedView().trim().length()>0) {
                if (getProjectChosen()!=null) {
                    if (viewMountCombo.getSelectedMountPoint()!=null) {
                        return true;
                    }
                }
            }
        }                 
        return false;
    }

    private class ViewListener implements java.awt.event.ActionListener {
        private RequiredActionsCompletedInterface required = null;

        public ViewListener(RequiredActionsCompletedInterface temp) {
            required = temp;
        }

        public void actionPerformed(ActionEvent e) {
            UserInterfaceEvent newEvent = new UserInterfaceEvent(required);
            fireEvent(newEvent);
        }
    }

}
