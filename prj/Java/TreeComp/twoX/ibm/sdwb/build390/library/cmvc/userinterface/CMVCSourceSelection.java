package com.ibm.sdwb.build390.library.cmvc.userinterface;

import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.util.*;

import javax.swing.*;
import javax.swing.border.*;
import javax.swing.event.*;

import com.ibm.sdwb.build390.*;
import com.ibm.sdwb.build390.library.SourceInfo;
import com.ibm.sdwb.build390.library.cmvc.*;
import com.ibm.sdwb.build390.library.userinterface.SourceSelection;
import com.ibm.sdwb.build390.logprocess.*;
import com.ibm.sdwb.build390.mainframe.DriverInformation;
import com.ibm.sdwb.build390.user.Setup;
import com.ibm.sdwb.build390.userinterface.RememberedSettingsHandler;
import com.ibm.sdwb.build390.userinterface.UserCommunicationInterface;
import com.ibm.sdwb.build390.userinterface.event.*;
import com.ibm.sdwb.build390.userinterface.event.build.DriverUpdateEvent;
import com.ibm.sdwb.build390.userinterface.event.build.ReleaseUpdateEvent;
import com.ibm.sdwb.build390.userinterface.graphic.MainInterface;
import com.ibm.sdwb.build390.userinterface.graphic.panels.*;
import com.ibm.sdwb.build390.userinterface.graphic.widgets.*;
import com.ibm.sdwb.build390.utilities.SynchronizedFileAccess;


public class CMVCSourceSelection extends SourceSelection implements Comparator, AncestorListener {

    private ReleaseSelectionCombo releaseCombo = null;
    private static final String TRACKTITLE = "Tracks";
    private JTabbedPane levelPane = new JTabbedPane();
    private JComboBox comboTracks = new JComboBox();
    private JCheckBox deltaBuildCheckbox = new JCheckBox("Delta build");
    private RefreshButtonActionListener refreshList = null;
    private CMVCLibraryInfo libInfo = null;
    private MBMainframeInfo mainInfo = null;
    private CMVCRestrictionPanel restrictionPanel = null;
    private RememberedSettingsHandler defaults = null;
    private Setup setup = null;
    private static final String RESTRICTIONKEY = "RESTRICTIONS";
    private SynchronizedFileAccess buildablesFile = null;
    private boolean initializeFieldsFromInfo = false;

    public CMVCSourceSelection(com.ibm.sdwb.build390.library.LibraryInfo tempLib, MBMainframeInfo tempMain) {
        libInfo = (CMVCLibraryInfo) tempLib;
        mainInfo = tempMain;
        setup = new Setup(libInfo, mainInfo, null, true);
        defaults = RememberedSettingsHandler.getInstance();
        addAncestorListener(this);
        setLayout(new BorderLayout());

        Box horizontalTopBox = Box.createHorizontalBox();

        Box verticalCenterBox = Box.createVerticalBox();

        CMVCSourceSelectionListener listener = new CMVCSourceSelectionListener(this);
        releaseCombo = new ReleaseSelectionCombo(mainInfo, libInfo, libInfo.getLEP());

        levelPane.addTab("Complete", createLevelComboBox(listener));
        levelPane.addTab("Commit", createLevelComboBox(listener));
        levelPane.addTab("Certify", createLevelComboBox(listener));
        levelPane.addTab("Ready", createLevelComboBox(listener));
        levelPane.addTab("Build", createLevelComboBox(listener));
        levelPane.addTab("Integrate", createLevelComboBox(listener));
        levelPane.addTab("All", createLevelComboBox(listener));
        levelPane.addTab(TRACKTITLE, comboTracks);
        levelPane.setTabLayoutPolicy(JTabbedPane.SCROLL_TAB_LAYOUT);
        levelPane. setPreferredSize(new Dimension(300,40));
        levelPane.setSelectedIndex(6);
        verticalCenterBox.add(Box.createVerticalGlue());
        verticalCenterBox.add(levelPane);
        verticalCenterBox.add(Box.createVerticalStrut(7));
        verticalCenterBox.add(new JButton(new RefreshButtonActionListener()));
        verticalCenterBox.add(Box.createVerticalGlue());
        Box deltaBuildBox = Box.createHorizontalBox();
        deltaBuildBox.add(deltaBuildCheckbox);
        deltaBuildBox.add(Box.createHorizontalGlue());
        verticalCenterBox.add(deltaBuildBox);
        verticalCenterBox.add(Box.createVerticalGlue());

        comboTracks.addActionListener(listener);
        levelPane.addChangeListener(listener);
        handleReleaseSelection();
        releaseCombo.getComboBox().addActionListener(new ReleaseSelectionListener());
        horizontalTopBox.add(new JLabel("Library Release"));
        horizontalTopBox.add(releaseCombo);
        restrictionPanel = new CMVCRestrictionPanel(libInfo,mainInfo);
        handleRememberedSettings();

        add(BorderLayout.NORTH, horizontalTopBox);
        add(BorderLayout.CENTER, verticalCenterBox);
        add(BorderLayout.SOUTH, restrictionPanel);
    }


    public SourceInfo getSourceInfo() {
        String tabLabel = levelPane.getTitleAt(levelPane.getSelectedIndex());
        String selectedString = (String) ((JComboBox) levelPane.getSelectedComponent()).getSelectedItem();
        CMVCSourceInfo sourceInfo = null;
        if (tabLabel.equalsIgnoreCase(TRACKTITLE)) {
            sourceInfo = new CMVCTrackSourceInfo(libInfo, releaseCombo.getSelectedRelease().getLibraryName(), selectedString,  restrictionPanel.getComponentAndPathRestrictions());
        } else {
            sourceInfo = new CMVCLevelSourceInfo(libInfo, releaseCombo.getSelectedRelease().getLibraryName(),  selectedString,  restrictionPanel.getComponentAndPathRestrictions());
        }
        sourceInfo.setIncludingCommittedBase(!deltaBuildCheckbox.isSelected());
        return sourceInfo;
    }

    private void handleRememberedSettings() {
        if (releaseCombo.getSelectedRelease()!=null) {
            SourceInfo tempSourceInfo = (SourceInfo) defaults.getPerReleaseSetting(setup, releaseCombo.getSelectedRelease().getLibraryName(), SOURCEKEY);
            if (tempSourceInfo!=null) {
                setSourceInfo(tempSourceInfo);
            }
        }
    }

    public void setSourceInfo(SourceInfo tempInfo) {
        CMVCSourceInfo info = (CMVCSourceInfo) tempInfo;
        releaseCombo.select(tempInfo.getProject());
        deltaBuildCheckbox.setSelected(!info.isIncludingCommittedBase());
        initializeFieldsFromInfo = true;
        try {
            if (info instanceof CMVCTrackSourceInfo) {
                levelPane.setSelectedIndex(levelPane.indexOfTab(TRACKTITLE));
                ((JComboBox) levelPane.getSelectedComponent()).setEditable(true);
                ((JComboBox) levelPane.getSelectedComponent()).setSelectedItem(info.getName());
                ((JComboBox) levelPane.getSelectedComponent()).setEditable(false);
            } else if (info instanceof CMVCLevelSourceInfo) {
                boolean found = false;
                for (int tabIndex = 0; tabIndex < levelPane.getTabCount() & !found;tabIndex++ ) {
                    if (levelPane.getTitleAt(tabIndex).equalsIgnoreCase(info.getState())) {
                        levelPane.setSelectedIndex(tabIndex);
                        JComboBox theBox =(JComboBox) levelPane.getComponentAt(tabIndex);
                        theBox.setSelectedItem(info.getName());
                        if (!info.getName().equals(theBox.getSelectedItem())) {// this means the item wasn't in there, so it wasn't selected
                            theBox.setEditable(true);
                            theBox.setSelectedItem(info.getName());
                            theBox.setEditable(false);
                        }
                    }
                }
            }

        } catch (MBBuildException mbe) {
            throw new RuntimeException("Unable to determine the state of the level", mbe);
        }

       
        restrictionPanel.setRestriction(info.getRestrictions());
    }


    public com.ibm.sdwb.build390.mainframe.ReleaseInformation getProjectChosen() {
        if (releaseCombo.getSelectedRelease()!=null) {
            return releaseCombo.getSelectedRelease();
        }
        return null;
    }

    private JComboBox createLevelComboBox(ActionListener listener) {
        final JComboBox levelBox = new JComboBox();
        if (listener!=null) {
            levelBox.addActionListener(listener);
        }
        return levelBox;
    }

    private void populateSelectionBoxes() {
        comboTracks.removeAllItems();
        for (int i = 0; i < levelPane.getTabCount(); i++) {
            JComboBox tempCombo = (JComboBox) levelPane.getComponentAt(i);
            if (tempCombo.getItemCount() > 0) {
                tempCombo.removeAllItems();
            }
        }
        Map levelMap = new TreeMap();//TST3079
        Map trackMap = new TreeMap();//TST3079

        try {
            if (buildablesFile!=null) {
                if (buildablesFile.exists()) {
                    BufferedReader selectionReaders = buildablesFile.getBufferedReader();
                    String line = new String();
                    while (line != null) {
                        line = selectionReaders.readLine();
                        if (line !=null) {
                            StringTokenizer toke = new StringTokenizer(line, "|");
                            String type = toke.nextToken();
                            String state = toke.nextToken();
                            String name = toke.nextToken();
                            if (type.equals(CMVCLibraryInfo.LEVEL)) {
                                levelMap.put(name, state);
                            } else if (type.equals(CMVCLibraryInfo.TRACK)) {
                                trackMap.put(name, state);
                            }
                        }
                    }
                }

            }
        } catch (IOException ioe) {
            throw new RuntimeException("Error reading command output " + buildablesFile.getAbsolutePath(), ioe);
        }
        for (Iterator levelIterator = levelMap.keySet().iterator();levelIterator.hasNext(); ) {
            String name = (String ) levelIterator.next();
            String state = (String) levelMap.get(name);
            for (int i = 0; i < levelPane.getTabCount()-2; i++) {
                if (state.equalsIgnoreCase(levelPane.getTitleAt(i).toUpperCase())) {
                    JComboBox tempCombo = (JComboBox) levelPane.getComponentAt(i);
                    tempCombo.addItem(name);
                    i = levelPane.getTabCount();
                }
            }
            JComboBox tempCombo = (JComboBox) levelPane.getComponentAt(levelPane.getTabCount()-2);
            tempCombo.addItem(name);
        }
        for (int i = 0; i < levelPane.getTabCount(); i++) {
            JComboBox tempCombo = (JComboBox) levelPane.getComponentAt(i);
            if (tempCombo.getItemCount() > 1) {
                tempCombo.setSelectedIndex(1);
            }
        }
        for (Iterator trackIterator = trackMap.keySet().iterator();trackIterator.hasNext(); ) {
            String name = (String ) trackIterator.next();
            comboTracks.addItem(name);
        }

    }

    public int compare(Object o1, Object o2) {
        String s1 = (String) o1;
        String s2 = (String) o2;

        return s1.toUpperCase().compareTo(s2.toUpperCase());
    }

    public boolean isRequiredActionCompleted() {
        /* check the release has been chosen and a level or track has been chosen */
        if (releaseCombo.getElementSelected()!=null) {
            if (levelPane.getSelectedComponent()!=null) {
                String selectedString = (String) ((JComboBox) levelPane.getSelectedComponent()).getSelectedItem();
                if (selectedString != null) {
                    if (selectedString.trim().length() > 0) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    private class CMVCSourceSelectionListener implements java.awt.event.ActionListener, javax.swing.event.ChangeListener {
        private RequiredActionsCompletedInterface required = null;

        public CMVCSourceSelectionListener(RequiredActionsCompletedInterface temp) {
            required = temp;
        }

        public void actionPerformed(java.awt.event.ActionEvent e) {
            doEventStuff(e);
        }

        public void stateChanged(javax.swing.event.ChangeEvent e) {
            doEventStuff(e);
        }

        private void doEventStuff(EventObject e) {
            UserInterfaceEvent newEvent = new UserInterfaceEvent(required);
            fireEvent(newEvent);
        }
    }

    private class RefreshButtonActionListener extends CancelableAction {

        RefreshButtonActionListener() {
            super("Refresh");
        }

        public void doAction(ActionEvent evt) {
            boolean clearStatus = true;
            try {
                if (releaseCombo.getSelectedRelease()!=null) {
                    Set levelTypes = new HashSet();
                    for (int i = 0; i < levelPane.getTabCount()-1; i++) {
                        levelTypes.add(levelPane.getTitleAt(i).toLowerCase());
                    }
                    Set trackTypes = new HashSet();
                    trackTypes.add("integrate");
                    trackTypes.add("fix");
                    trackTypes.add("review");

                    String buildableOutput = libInfo.getBuildableObjects(releaseCombo.getSelectedRelease().getLibraryName(),levelTypes,trackTypes);
                    BufferedWriter outputWriter = buildablesFile.getBufferedWriter(false);
                    outputWriter.write(buildableOutput);
                    outputWriter.close();
                    populateSelectionBoxes();
                }
            } catch (Exception mbe) {
                throw new RuntimeException(mbe);
            } finally {
                if (clearStatus) parentWindow.getStatusHandler().clearStatus();
            }
        }
    }

    public void handleFrameClosing() {
        if (releaseCombo.getSelectedRelease()!=null) {
            /**The restriction is stored inside the SourceInfo object **/
            defaults.addPerReleaseSetting(setup, releaseCombo.getSelectedRelease().getLibraryName(), SOURCEKEY, getSourceInfo());
        }
    }

    private class ReleaseSelectionListener implements ActionListener {
        public void actionPerformed(ActionEvent e) {
            handleReleaseSelection();
        }
    }

    private void handleReleaseSelection() {
        if (getProjectChosen()!=null) {
            buildablesFile = SynchronizedFileAccess.getSychronizedFile(new File(MBClient.getCacheDirectory(), "Buildables-"+libInfo.getProcessServerAddress()+"-"+libInfo.getProcessServerName()+"-"+getProjectChosen().getLibraryName()));
        } else {
            buildablesFile = null;
        }
        populateSelectionBoxes();
        // it's a release update event
        ReleaseUpdateEvent rue = new ReleaseUpdateEvent(releaseCombo.getComboBox());
        rue.setReleaseInformation(releaseCombo.getSelectedRelease());
        fireEvent(rue);
    }

    public void handleUIEvent(UserInterfaceEvent tempEvent) {
        if (tempEvent instanceof DriverUpdateEvent) { //during a load  we shouldn't update it.
            DriverUpdateEvent event = (DriverUpdateEvent)tempEvent;
            if (event.getDriverInformation()!=null && !initializeFieldsFromInfo) {
                DriverInformation driverInfo = event.getDriverInformation();
                deltaBuildCheckbox.setSelected((driverInfo.getBaseDriver()!=null | driverInfo.getExplicitBaseChain()!=null) & !driverInfo.isFullDriver());
            }

            /**allows one time only. if the loaded build had processes, its going to popup step list panel.
            if there are no steps, the panel is enabled. user can choose a different driver. so we reset initializeFieldsFromInfo to false.
            **/
            initializeFieldsFromInfo = false; 
        }

    }

    private class FrameListener extends InternalFrameAdapter {
        private boolean hasRun = false;
        /**
         * Invoked when an internal frame is in the process of being closed.
         * The close operation can be overridden at this point.
         */
        public void internalFrameClosed(InternalFrameEvent e) {
            if (!hasRun) {
                handleFrameClosing();
                hasRun = true;
            }
        }
    }

    public void ancestorAdded(AncestorEvent ae) {
        com.ibm.sdwb.build390.userinterface.graphic.utilities.GeneralUtilities.getParentInternalFrame((java.awt.Component) getParent()).addInternalFrameListener(new FrameListener());

    }

    public void ancestorMoved(AncestorEvent ae) {
    }

    public void ancestorRemoved(AncestorEvent ae) {
    }
}
