package com.ibm.sdwb.build390.userinterface.graphic.widgets;

import javax.swing.*;
import com.ibm.sdwb.build390.MBMainframeInfo; 
import com.ibm.sdwb.build390.library.LibraryInfo;
import com.ibm.sdwb.build390.logprocess.LogEventProcessor;
import com.ibm.sdwb.build390.userinterface.event.build.*;
import com.ibm.sdwb.build390.userinterface.event.*;

public class UsermodBuildSettingsPanel extends BuildSettingsPanel {

    private JCheckBox dryRunCheckbox = new JCheckBox("Library Requisite Check (usermod dryrun)");
    private JCheckBox buildOneUsermodCheckbox = new JCheckBox("Bundle all tracks into one usermod");
    private JTextField sendToTextfield = new JTextField();
    private JTextField PDSTextfield = new JTextField();
    private String lastRelease = null;
    public static final String BUNDLEDPROCESSING = "BUILD_MANY";
    public static final String BUNDLEDPROCESSINGEDITABLE = "BUILD_MANY_ENABLED";

    public UsermodBuildSettingsPanel(LibraryInfo tempLib, MBMainframeInfo tempMain, LogEventProcessor tempLep) {
        super(tempLib, tempMain, tempLep);
        cbSyncDriver.setVisible(false);
        addLowerSettingSection(createLowerSettingsPanel());
    }

    private Box createLowerSettingsPanel() {
        Box checkboxBox = Box.createHorizontalBox();
        checkboxBox.add(Box.createHorizontalGlue());
        checkboxBox.add(dryRunCheckbox);
        checkboxBox.add(Box.createHorizontalGlue());
        checkboxBox.add(buildOneUsermodCheckbox);
        checkboxBox.add(Box.createHorizontalGlue());

        JPanel storageLocationPanel = new JPanel(new java.awt.GridLayout(2,2));
        storageLocationPanel.add(new JLabel("Send built USERMODs to node.userid:"));
        storageLocationPanel.add(sendToTextfield);
        storageLocationPanel.add(new JLabel("Put built USERMODs into PDS:"));
        storageLocationPanel.add(PDSTextfield);

        Box lowerSettingsBox = Box.createVerticalBox();
        lowerSettingsBox.add(checkboxBox);
        lowerSettingsBox.add(Box.createVerticalGlue());
        lowerSettingsBox.add(storageLocationPanel);
        return lowerSettingsBox;
    }

    public boolean isDryRunSet() {
        return dryRunCheckbox.isSelected();
    }

    public void setDryRunEnabled(boolean enabled) {
        dryRunCheckbox.setEnabled(enabled);
    }

    public void setDryRunSelected(boolean selected){
        dryRunCheckbox.setSelected(selected);
    }

    public boolean isBuildOneUsermodSet() {
        return buildOneUsermodCheckbox.isSelected();
    }

    public void setBuildOneUsermodSet(boolean enabled) {
        buildOneUsermodCheckbox.setSelected(enabled);
    }


    public String getSendToSetting() {
        if (sendToTextfield.getText().trim().length()<1) {
            return null;
        }
        return sendToTextfield.getText().trim();
    }

    public void setSendToSetting(String sendToString) {
        if (sendToString!=null) {
            sendToTextfield.setText(sendToString);
        }
    }

    public String getPDSSetting() {
        if (PDSTextfield.getText().trim().length()<1) {
            return null;
        }
        return PDSTextfield.getText().trim();
    }

    public void setPDSSetting(String pdsString) {
        if (pdsString!=null) {
            PDSTextfield.setText(pdsString);
        }
    }

    private void handleProjectUpdate(String newProject) {
        try {
            String bundledSetting = null;
            String bundledSettingEditable = null;
            boolean allowBundledEdit = true;
            buildOneUsermodCheckbox.setSelected(false);
            buildOneUsermodCheckbox.setEnabled(true);
            if (newProject!=null) {
                com.ibm.sdwb.build390.configuration.ConfigurationAccess configAccess = libInfo.getConfigurationAccess(newProject, false);
                bundledSetting = configAccess.getProjectConfigurationSetting(com.ibm.sdwb.build390.library.cmvc.userinterface.CMVCUsermodSourceSelection.USERMODCONFIGSECTIONKEY,BUNDLEDPROCESSING);
                if (bundledSetting!=null) {
                    bundledSettingEditable = configAccess.getProjectConfigurationSetting(com.ibm.sdwb.build390.library.cmvc.userinterface.CMVCUsermodSourceSelection.USERMODCONFIGSECTIONKEY,BUNDLEDPROCESSINGEDITABLE);
                    if (bundledSettingEditable!=null) {
                        allowBundledEdit = bundledSettingEditable.equalsIgnoreCase("true");
                    }
                    buildOneUsermodCheckbox.setSelected(bundledSetting.equalsIgnoreCase("true"));
                    buildOneUsermodCheckbox.setEnabled(allowBundledEdit);
                }
            }
        } catch (com.ibm.sdwb.build390.MBBuildException le) {
            throw new RuntimeException(le);
        }
    }

    public void handleUIEvent(UserInterfaceEvent e) {
        super.handleUIEvent(e);
        if (e instanceof ReleaseUpdateEvent) {
            ReleaseUpdateEvent event = (ReleaseUpdateEvent) e;
            if (event.getReleaseInformation()!=null) {
                if (event.getReleaseInformation().getLibraryName().equals(lastRelease)) {
                    return;
                }
                lastRelease=event.getReleaseInformation().getLibraryName();
            } else {
                lastRelease = null;
            }
            handleProjectUpdate(lastRelease);
        }
    }
}
