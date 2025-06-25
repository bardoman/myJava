package com.ibm.sdwb.build390.userinterface.graphic.widgets;

import java.awt.*;
import javax.swing.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.event.*;
import com.ibm.sdwb.build390.*;
import com.ibm.sdwb.build390.library.LibraryInfo;
import com.ibm.sdwb.build390.logprocess.*;
import com.ibm.sdwb.build390.help.*;
import com.ibm.sdwb.build390.mainframe.*;
import com.ibm.sdwb.build390.userinterface.event.*;
import com.ibm.sdwb.build390.userinterface.graphic.MainInterface;
import com.ibm.sdwb.build390.userinterface.graphic.widgets.*;
import com.ibm.sdwb.build390.userinterface.event.*;
import com.ibm.sdwb.build390.userinterface.event.build.*;

public class BuildSettingsPanel extends JPanel implements RequiredActionsCompletedInterface, UserInterfaceEventListener {

    private BuildtypeSelectionCombo buildtypeCombo = null;
    protected JCheckBox cbSyncDriver = null;
    protected MBMainframeInfo mainInfo = null;
    protected LibraryInfo libInfo = null;
    private LogEventProcessor lep = null;
    private UserInterfaceListenerManager changeManager = new UserInterfaceListenerManager();
    private Box layoutBox = Box.createVerticalBox();
    private BuildSettingsListener listener;

    public BuildSettingsPanel(LibraryInfo tempLib, MBMainframeInfo tempMain, LogEventProcessor tempLep) {
        super(new BorderLayout());
        libInfo = tempLib;
        mainInfo = tempMain;
        lep = tempLep;
        add(BorderLayout.CENTER, layoutBox);
        layoutDialog();
    }

    private void layoutDialog() {

        buildtypeCombo = new BuildtypeSelectionCombo(mainInfo, libInfo, lep);

        Box buildtypeComboBox = Box.createHorizontalBox();
        buildtypeComboBox.add(new JLabel("BuildType"));
        buildtypeComboBox.add(buildtypeCombo);

        listener = new BuildSettingsListener();
        buildtypeCombo.addListActionListener(listener);
        layoutBox.add(Box.createVerticalGlue());
        layoutBox.add(buildtypeComboBox);
        layoutBox.add(Box.createVerticalGlue());
        Box syncCheckBox = Box.createHorizontalBox();
        cbSyncDriver = new JCheckBox("Synchronize full delta driver with base");
        cbSyncDriver.setSelected(false);
        syncCheckBox.add(cbSyncDriver);
        syncCheckBox.add(Box.createHorizontalGlue());
        layoutBox.add(syncCheckBox);
        layoutBox.add(Box.createVerticalGlue());
        setVisible(true);
    }


    public void addLowerSettingSection(JComponent newComponent) {
        layoutBox.add(Box.createVerticalGlue());
        layoutBox.add(newComponent);
        layoutBox.add(Box.createVerticalGlue());
    }

    public void setDriverInformation(DriverInformation tempInfo) {
        buildtypeCombo.setDriverInformation(tempInfo);
        if (tempInfo!=null) {
            cbSyncDriver.setEnabled(tempInfo.isFullDriver());//TST3522
        } else {
            cbSyncDriver.setEnabled(false);
        }
    }

    public String getBuildType() {
        return buildtypeCombo.getElementSelected();
    }

    public boolean isSynchronizeFullDeltaDriverWithBaseSelected() {
        return cbSyncDriver.isSelected();
    }

    public void setBuildType(String type) {
        buildtypeCombo.selectItem(type);
    }

    /**
     * Make sure the buildtype has been filled in.
     * As long as the buildtype is filled we can keep going.
     * 
     * @return 
     */
    public boolean isRequiredActionCompleted() {
        String tempBuild = buildtypeCombo.getElementSelected();
        if (tempBuild != null) {
            if (tempBuild.trim().length() > 0) {
                return true;
            }
        }
        return false;
    }

    public void addUserInterfaceEventListener(UserInterfaceEventListener listener) {
        changeManager.addUserInterfaceEventListener(listener);
    }

    private void fireEvent() {
        com.ibm.sdwb.build390.userinterface.event.build.BuildtypeUpdateEvent newEvent = new com.ibm.sdwb.build390.userinterface.event.build.BuildtypeUpdateEvent(this);
        newEvent.setBuildtype(buildtypeCombo.getElementSelected());
        changeManager.fireEvent(newEvent);
    }

    public void handleUIEvent(UserInterfaceEvent e) {
        if (e instanceof DriverUpdateEvent) {
            DriverUpdateEvent event = (DriverUpdateEvent) e;
            if (buildtypeCombo.getDriverInformation()==event.getDriverInformation()) {
                return;
            }
            if (buildtypeCombo.getDriverInformation()!=null) {
                if (buildtypeCombo.getDriverInformation().equals(event.getDriverInformation())) {
                    return;
                }
            }
            setDriverInformation(event.getDriverInformation());
        }
    }


    private class BuildSettingsListener implements java.awt.event.ActionListener {

        public void actionPerformed(java.awt.event.ActionEvent e) {
            fireEvent();
        }
    }
}
