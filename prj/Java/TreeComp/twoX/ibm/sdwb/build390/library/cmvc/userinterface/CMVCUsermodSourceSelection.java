package com.ibm.sdwb.build390.library.cmvc.userinterface;

import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.util.*;

import javax.swing.*;
import javax.swing.border.*;
import javax.swing.event.*;
import javax.swing.tree.*;

import com.ibm.sdwb.build390.*;
import com.ibm.sdwb.build390.library.*;
import com.ibm.sdwb.build390.library.cmvc.*;
import com.ibm.sdwb.build390.library.userinterface.MultipleSourceSelection;
import com.ibm.sdwb.build390.logprocess.*;
import com.ibm.sdwb.build390.user.Setup;
import com.ibm.sdwb.build390.userinterface.RememberedSettingsHandler;
import com.ibm.sdwb.build390.userinterface.UserCommunicationInterface;
import com.ibm.sdwb.build390.userinterface.event.*;
import com.ibm.sdwb.build390.userinterface.event.build.*;
import com.ibm.sdwb.build390.userinterface.graphic.MainInterface;
import com.ibm.sdwb.build390.userinterface.graphic.panels.*;
import com.ibm.sdwb.build390.userinterface.graphic.utilities.GeneralUtilities;
import com.ibm.sdwb.build390.userinterface.graphic.widgets.*;

public class CMVCUsermodSourceSelection extends MultipleSourceSelection implements Comparator, AncestorListener {

    private ReleaseSelectionCombo releaseCombo = null;
    private CMVCLibraryInfo libInfo = null;
    private MBMainframeInfo mainInfo = null;
    private java.util.Map allAvailableLevelsMap = null;
    private java.util.List allUnassignedTracksList = null;
    private CMVCRestrictionPanel restrictionPanel = null;
    private DefaultMutableTreeNode usermodContentsRoot = new DefaultMutableTreeNode("Usermod contents");
    private DefaultMutableTreeNode levelsNode = new DefaultMutableTreeNode("Levels");
    private DefaultMutableTreeNode tracksNode = new DefaultMutableTreeNode("Unassigned tracks");
    private DefaultTreeModel treeModel = new DefaultTreeModel(usermodContentsRoot);
    private JTree usermodContentsTree = new JTree(treeModel);
    private com.ibm.sdwb.build390.userinterface.graphic.utilities.JTreeUtilities treeUtils = new com.ibm.sdwb.build390.userinterface.graphic.utilities.JTreeUtilities(usermodContentsTree);
    private RememberedSettingsHandler defaults = null;
    private Setup setup = null;
    private String userStagingLevelSetting = null;
    private boolean userStagingLevelEditable = true;
    private JButton addStagingLevelButton = null;
    private JButton addTrackButton = null;
    private JButton removeButton = null;
    private static final String RESTRICTIONKEY = "RESTRICTIONS";
    public static final String STAGINGLEVEL = "STAGING_LEVEL";
    public static final String STAGINGLEVELEDITABLE="STAGING_LEVEL_EDITABLE";
    public static final String USERMODCONFIGSECTIONKEY = "USERMOD";

    public CMVCUsermodSourceSelection(com.ibm.sdwb.build390.library.LibraryInfo tempLib, MBMainframeInfo tempMain) {
        libInfo = (CMVCLibraryInfo) tempLib;
        mainInfo = tempMain;
        setup = new Setup(libInfo, mainInfo, null, true);
        defaults = RememberedSettingsHandler.getInstance();
        addAncestorListener(this);
        setLayout(new BorderLayout());

        Box horizontalTopBox = Box.createHorizontalBox();
        releaseCombo = new ReleaseSelectionCombo(mainInfo, libInfo, libInfo.getLEP());

        horizontalTopBox.add(new JLabel("Library Release"));
        horizontalTopBox.add(releaseCombo);

        usermodContentsRoot.add(levelsNode);
        usermodContentsRoot.add(tracksNode);

        Box verticalCenterBox = Box.createVerticalBox();
        Box treeBox = Box.createHorizontalBox();
        JScrollPane treeScroll = new JScrollPane(usermodContentsTree);
        treeScroll.setPreferredSize(new java.awt.Dimension(200, 150));
        treeBox.add(treeScroll);
        Box treeButtonBox = Box.createVerticalBox();
        addStagingLevelButton = new JButton(new AddStagingLevelButtonActionListener());
        addTrackButton = new JButton(new AddTrackButtonActionListener());
        removeButton = new JButton(new RemoveButtonActionListener());
        treeButtonBox.add(Box.createVerticalGlue());
        treeButtonBox.add(addStagingLevelButton);
        treeButtonBox.add(Box.createVerticalGlue());
        treeButtonBox.add(addTrackButton);
        treeButtonBox.add(Box.createVerticalGlue());
        treeButtonBox.add(removeButton);
        treeButtonBox.add(Box.createVerticalGlue());
        treeBox.add(treeButtonBox);
        verticalCenterBox.add(treeBox);
        verticalCenterBox.add(Box.createVerticalGlue());

        restrictionPanel = new CMVCRestrictionPanel(libInfo,mainInfo);

        handleRememberedSettings();
        add(BorderLayout.NORTH, horizontalTopBox);
        add(BorderLayout.CENTER, verticalCenterBox);
        add(BorderLayout.SOUTH, restrictionPanel);
        treeUtils.expandTree();
        releaseCombo.addListItemListener(new ReleaseListItemListener());
    }



    public void setEnabled(boolean enabled) {
        super.setEnabled(enabled);
        usermodContentsTree.setEnabled(enabled);
        addStagingLevelButton.setEnabled(enabled);
        addTrackButton.setEnabled(enabled);
        removeButton.setEnabled(enabled);
    }

    private void handleRememberedSettings() {
        if (releaseCombo.getSelectedRelease()!=null) {
            ComponentAndPathRestrictions restrictions = (ComponentAndPathRestrictions) defaults.getPerReleaseSetting(setup, releaseCombo.getSelectedRelease().getLibraryName(), RESTRICTIONKEY);
            restrictionPanel.setRestriction(restrictions);
        }
    }

    private void handleConfigurationSettings() {
        try {
            com.ibm.sdwb.build390.configuration.ConfigurationAccess configAccess = libInfo.getConfigurationAccess(releaseCombo.getSelectedRelease().getLibraryName(), false);
            userStagingLevelSetting = configAccess.getProjectConfigurationSetting(USERMODCONFIGSECTIONKEY,STAGINGLEVEL);
            String userStagingLevelEditableSetting = null;
            if (userStagingLevelSetting!=null) {
                userStagingLevelEditableSetting = configAccess.getProjectConfigurationSetting(USERMODCONFIGSECTIONKEY,STAGINGLEVELEDITABLE);
            }
            if (userStagingLevelEditableSetting!=null) {
                userStagingLevelEditable = userStagingLevelEditableSetting.equalsIgnoreCase("true");
            } else {
                userStagingLevelEditable=true;
            }
            if (userStagingLevelSetting!=null) {
                handleStagingLevelAdd(userStagingLevelSetting);
            }
        } catch (MBBuildException le) {
            throw new RuntimeException(le);
        }
    }

    public SourceInfo getSourceInfo() {
        Set sourceInfoSet = new HashSet();
        Set projectSet = new HashSet();
        projectSet.add(releaseCombo.getSelectedRelease().getLibraryName());
        // first add all the unassigned tracks
        for (Enumeration unassignedTrackNodesEnum = tracksNode.children(); unassignedTrackNodesEnum.hasMoreElements();) {
            DefaultMutableTreeNode oneTrack = (DefaultMutableTreeNode) unassignedTrackNodesEnum.nextElement();
            CMVCChangeRequestInfo changeRequestInfo = new CMVCChangeRequestInfo(libInfo, oneTrack.getUserObject().toString(),  restrictionPanel.getComponentAndPathRestrictions());
            changeRequestInfo.setInterestedProjects(projectSet);
            sourceInfoSet.add(changeRequestInfo);
        }
        // now handle the levels.  First go through levels
        for (Enumeration levelNodeEnum = levelsNode.children(); levelNodeEnum.hasMoreElements();) {
            DefaultMutableTreeNode oneLevel = (DefaultMutableTreeNode) levelNodeEnum.nextElement();
            CMVCLevelSourceInfo levelSource = new CMVCLevelSourceInfo(libInfo, releaseCombo.getSelectedRelease().getLibraryName(), oneLevel.getUserObject().toString(), restrictionPanel.getComponentAndPathRestrictions());
            // now find all the tracks to build of that level
            for (Enumeration tracksOfLevelEnum = oneLevel.children(); tracksOfLevelEnum.hasMoreElements();) {
                DefaultMutableTreeNode oneTrack = (DefaultMutableTreeNode) tracksOfLevelEnum.nextElement();
                CMVCChangeRequestInfo changeRequestInfo = new CMVCChangeRequestInfo(libInfo, oneTrack.getUserObject().toString(),  restrictionPanel.getComponentAndPathRestrictions());
                changeRequestInfo.setInterestedProjects(projectSet);
                // now we need to make sure we track what level this was selected from, so we'll prime source infos now
                Set infoSet = changeRequestInfo.getIndividualSourceInfos();
                for (Iterator changesetIterator = infoSet.iterator(); changesetIterator.hasNext();) {
                    Changeset oneChangeset = (Changeset) changesetIterator.next();
                    if (oneChangeset.getProject().equals(releaseCombo.getSelectedRelease().getLibraryName())) {    // in case we enable multiple releases, make sure we only pull this from the release we got it from
                        oneChangeset.setChangesetGroupContainingChangeset(levelSource);
                    }
                }
                sourceInfoSet.add(changeRequestInfo);
            }
        }
        SourceInfoCollection sourceCollection = new SourceInfoCollection();
        sourceCollection.setChangeRequestCollection(sourceInfoSet);
        return sourceCollection;
    }

    private Set getSetOfSelectedTracks() {
        Set tracksSelected = new HashSet();
        for (Enumeration unassignedTrackNodesEnum = tracksNode.children(); unassignedTrackNodesEnum.hasMoreElements();) {
            DefaultMutableTreeNode oneTrack = (DefaultMutableTreeNode) unassignedTrackNodesEnum.nextElement();
            tracksSelected.add(oneTrack.getUserObject().toString());
        }
        return tracksSelected;
    }

    private Set getSetOfSelectedLevels() {
        Set levelsSelected = new HashSet();
        for (Enumeration levelNodeEnum = levelsNode.children(); levelNodeEnum.hasMoreElements();) {
            DefaultMutableTreeNode oneLevel = (DefaultMutableTreeNode) levelNodeEnum.nextElement();
            levelsSelected.add(oneLevel.getUserObject().toString());
        }
        return levelsSelected;
    }

    public void setSourceInfo(final SourceInfo source) {
        SourceInfoCollection sourceCollection = (SourceInfoCollection)source;

        DefaultMutableTreeNode thisLevel = null;
        Set projectSet = null;
        String tempProject =null;

        for (Iterator iter=sourceCollection.getChangeRequestCollection().iterator(); (iter.hasNext() && tempProject==null);) {
            CMVCChangeRequestInfo changeRequestInfo = (CMVCChangeRequestInfo)iter.next();
            projectSet = changeRequestInfo.getInterestedProjects();
            tempProject = (String)projectSet.toArray()[0];
        }
        releaseCombo.select(tempProject);

        for (Iterator iter=sourceCollection.getChangeRequestCollection().iterator(); iter.hasNext();) {
            CMVCChangeRequestInfo changeRequestInfo = (CMVCChangeRequestInfo)iter.next();
            projectSet = changeRequestInfo.getInterestedProjects();

            DefaultMutableTreeNode thisTrack = new DefaultMutableTreeNode(changeRequestInfo.getName());
            if (changeRequestInfo.getIndividualSourceInfos()!=null) {
                Set infoSet = changeRequestInfo.getIndividualSourceInfos();
                for (Iterator changesetIterator = infoSet.iterator(); changesetIterator.hasNext();) {
                    Changeset oneChangeset = (Changeset) changesetIterator.next();
                    if (oneChangeset.getProject().equals(tempProject)) {    // in case we enable multiple releases, make sure we only pull this from the release we got it from
                        if (thisLevel==null && oneChangeset.getChangesetGroupContainingChangeset()!=null) {
                            thisLevel = new DefaultMutableTreeNode(oneChangeset.getChangesetGroupContainingChangeset().getName());
                        }
                        if (oneChangeset.getChangesetGroupContainingChangeset()!=null) {
                            treeUtils.addTreeNodeToTreeNodeSorted(thisLevel, thisTrack);
                        } else {
                            treeUtils.addTreeNodeToTreeNodeSorted(tracksNode, thisTrack);
                        }
                    }
                }
            } else {
                treeUtils.addTreeNodeToTreeNodeSorted(tracksNode, thisTrack);
            }
        }

        if (thisLevel!=null) {
            treeUtils.addTreeNodeToTreeNodeSorted(levelsNode, thisLevel);
        }
        treeUtils.updateNodeStructure();

        SwingUtilities.invokeLater(new Runnable() {
                                       public void run() {
                                           //  doEventStuff();
                                           //  releaseCombo.select(tempProject);
                                       }
                                   });
    }

    public ChangeRequest getSelectedChangeRequest() {
        if (!usermodContentsTree.isSelectionEmpty()) {
            DefaultMutableTreeNode selectedNode = (DefaultMutableTreeNode) usermodContentsTree.getSelectionPath().getLastPathComponent();
            Set projectSet = new HashSet();
            projectSet.add(releaseCombo.getSelectedRelease().getLibraryName());
            CMVCChangeRequestInfo changeRequestInfo = new CMVCChangeRequestInfo(libInfo, selectedNode.getUserObject().toString(),  restrictionPanel.getComponentAndPathRestrictions());
            changeRequestInfo.setInterestedProjects(projectSet);
            return changeRequestInfo;
        }
        return null;
    }

    public com.ibm.sdwb.build390.mainframe.ReleaseInformation getProjectChosen() {
        return releaseCombo.getSelectedRelease();
    }

    private Set getUserSelection(String queryTitle, String filterString, String levelToSelect) throws com.ibm.sdwb.build390.MBBuildException{
        Set levelTypes = new HashSet();
        levelTypes.add("integrate");
        levelTypes.add("working");
        levelTypes.add("build");
        Set trackTypes = new HashSet();
        trackTypes.add("integrate");
        trackTypes.add("review");
        try {
//uncomment to reenamble track / level caching            if (allAvailableLevelsMap==null | allUnassignedTracksList==null) {
            if (true) {
                allAvailableLevelsMap = new HashMap();
                allUnassignedTracksList = new ArrayList();
                String buildableOutput = libInfo.getBuildableObjects(releaseCombo.getSelectedRelease().getLibraryName(),levelTypes,trackTypes);
                BufferedReader selectionReaders = new BufferedReader(new StringReader(buildableOutput));
                String line = new String();
                while (line != null) {
                    line = selectionReaders.readLine();
                    if (line !=null) {
                        StringTokenizer toke = new StringTokenizer(line, "|");
                        String type = toke.nextToken();
                        String stateOrLevel = toke.nextToken();
                        String name = toke.nextToken();
                        if (type.equals(CMVCLibraryInfo.LEVELMEMBER)) {
                            java.util.List levelMemberList = (java.util.List) allAvailableLevelsMap.get(stateOrLevel);
                            if (levelMemberList==null) {
                                levelMemberList = new ArrayList();
                                allAvailableLevelsMap.put(stateOrLevel, levelMemberList);
                            }
                            levelMemberList.add(name);
                        } else if (type.equals(CMVCLibraryInfo.TRACK)) {
                            allUnassignedTracksList.add(name);
                        } else if (type.equals(CMVCLibraryInfo.LEVEL) && (levelToSelect!=null) && (name.equals(levelToSelect))) {
                            allAvailableLevelsMap.put(name, new ArrayList());
                        }
                    }
                }
            }

            java.util.List choiceList = null;
            if (filterString.equals(CMVCLibraryInfo.LEVEL)) {
                choiceList = new ArrayList();
                for (Iterator levelIterator = allAvailableLevelsMap.keySet().iterator(); levelIterator.hasNext();) {
                    choiceList.add(levelIterator.next());
                }
                //choiceList.removeAll(getSetOfSelectedLevels()); //TST3559
            } else if (filterString.equals(CMVCLibraryInfo.TRACK)) {
                choiceList = allUnassignedTracksList;
                choiceList.removeAll(getSetOfSelectedTracks());
            }

            if (levelToSelect== null) {
                if (!choiceList.isEmpty()) {
                    MBAnimationStatusWindow theWindow = com.ibm.sdwb.build390.userinterface.graphic.utilities.GeneralUtilities.getParentAnimationStatus(this);
                    JListDialogBox userSelectionBox = new JListDialogBox(queryTitle, choiceList,  (JInternalFrame) theWindow, theWindow.getLEP());
                    userSelectionBox.setAllowMultipeSelection(true);
                    userSelectionBox.setVisible(true);
                    Set selection = null;
                    if (userSelectionBox.getElementsSelected()!=null) {
                        selection = new HashSet(Arrays.asList(userSelectionBox.getElementsSelected()));
                    }
                    return selection;
                } else {
                    String stateString = null;
                    if (filterString.equals(CMVCLibraryInfo.LEVEL)) {
                        stateString = "integrate, working, or build";
                    } else if (filterString.equals(CMVCLibraryInfo.TRACK)) {
                        stateString = "review or integrate";
                    }
                    new MBMsgBox("Selection error", "No " + stateString + " " + filterString + "s were found.");
                }

            } else {
                java.util.List levelMemberList = (java.util.List) allAvailableLevelsMap.get(levelToSelect);
                String errorString = null;
                if (levelMemberList == null) {
                    errorString = levelToSelect+" is defined in your site's configuration settings as the staging level for your site, but does not exist.  It cannot be added.";
                } else if (levelMemberList.isEmpty()) {
                    errorString = levelToSelect+" is defined in your site's configuration settings as the staging level for your site, but has no tracks at present.  It cannot be added.";
                }
                if (!userStagingLevelEditable) {
                    if (errorString!=null) {
                        errorString += " Because the configuration setting "+STAGINGLEVELEDITABLE + " is set as not true, you cannot manipulate the build list. Please modify the level in CMVC or have your admistrator remove the " + STAGINGLEVELEDITABLE + " setting in your configuration.";
                    }
                }
                if (errorString!=null) {
                    new MBMsgBox("Staging level error", errorString);
                    return null;
                }
                Set selection = new HashSet();
                selection.add(levelToSelect);
                return selection;
            }
        } catch (IOException ioe) {
            throw new RuntimeException("Error reading library output.", ioe);
        }
        return null;
    }


    public int compare(Object o1, Object o2) {
        String s1 = (String) o1;
        String s2 = (String) o2;

        return s1.toUpperCase().compareTo(s2.toUpperCase());
    }

    public boolean isRequiredActionCompleted() {
        if (tracksNode.getChildCount() > 0) {
            return true;
        }
        for (Enumeration levelNodeEnum = levelsNode.children(); levelNodeEnum.hasMoreElements();) {
            DefaultMutableTreeNode oneLevel = (DefaultMutableTreeNode) levelNodeEnum.nextElement();
            if (oneLevel.getChildCount()>0) {
                return true;
            }
        }
        return false;
    }

    private void doEventStuff() {
        UserInterfaceEvent newEvent = new UserInterfaceEvent(this);
        fireEvent(newEvent);
    }

    private void handleStagingLevelAdd(String levelName) throws MBBuildException{

        if (releaseCombo.getSelectedRelease()!=null) {
            Set userSelectionSet = getUserSelection("Staging level candidates", CMVCLibraryInfo.LEVEL, levelName);
            if (userSelectionSet!=null) {
                for (Iterator userSelectIterator = userSelectionSet.iterator(); userSelectIterator.hasNext();) {
                    //boolean nodeAlreadyAdded = false; //TST3559
                    String userSelection = (String) userSelectIterator.next();
                    for (Enumeration childrenEnum = levelsNode.children(); childrenEnum.hasMoreElements();) {
                        DefaultMutableTreeNode oneChild = (DefaultMutableTreeNode) childrenEnum.nextElement();
                        if (userSelection.equals(oneChild.getUserObject().toString())) {
                        	levelsNode.remove(oneChild);
                            //nodeAlreadyAdded = true; //TST3559
                        }
                    }
                    //if (!nodeAlreadyAdded) {
                        DefaultMutableTreeNode thisLevel = new DefaultMutableTreeNode(userSelection);
                        java.util.List levelMemberList = (java.util.List) allAvailableLevelsMap.get(userSelection);
                        for (Iterator levelMemberIterator = levelMemberList.iterator(); levelMemberIterator.hasNext();) {
                            DefaultMutableTreeNode levelMemberNode = new DefaultMutableTreeNode(levelMemberIterator.next());
                            treeUtils.addTreeNodeToTreeNodeSorted(thisLevel, levelMemberNode);
                        }
                        treeUtils.addTreeNodeToTreeNodeSorted(levelsNode, thisLevel);
                    //}
                }
                treeUtils.updateNodeStructure();
                treeUtils.expandTree(); //TST3559
            }
        }
    }

    private class AddStagingLevelButtonActionListener extends CancelableAction {

        AddStagingLevelButtonActionListener() {
            super("Add Staging level");
        }

        public void doAction(ActionEvent evt) {
            try {
                handleStagingLevelAdd(null);
            } catch (Exception mbe) {
                throw new RuntimeException(mbe);
            } finally {
                parentWindow.getStatusHandler().clearStatus();
            }
        }

        public void postAction() {
            doEventStuff();
        }
    }

    private class AddTrackButtonActionListener extends CancelableAction {

        AddTrackButtonActionListener() {
            super("Add track");
        }

        public void doAction(ActionEvent evt) {
            try {
                if (releaseCombo.getSelectedRelease()!=null) {
                    Set userSelectionSet = getUserSelection("Track candidates", CMVCLibraryInfo.TRACK, null);
                    if (userSelectionSet!=null) {
                        for (Iterator userSelectIterator = userSelectionSet.iterator(); userSelectIterator.hasNext();) {
                            String userSelection = (String) userSelectIterator.next();
                            boolean nodeAlreadyAdded = false;
                            for (Enumeration childrenEnum = tracksNode.children(); childrenEnum.hasMoreElements();) {
                                DefaultMutableTreeNode oneChild = (DefaultMutableTreeNode) childrenEnum.nextElement();
                                if (userSelection.equals(oneChild.getUserObject().toString())) {
                                    nodeAlreadyAdded = true;
                                }
                            }
                            if (!nodeAlreadyAdded) {
                                DefaultMutableTreeNode thisTrack = new DefaultMutableTreeNode(userSelection);
                                treeUtils.addTreeNodeToTreeNodeSorted(tracksNode, thisTrack);
                            }
                        }
                    }
                    treeUtils.updateNodeStructure();
                }
            } catch (Exception mbe) {
                throw new RuntimeException(mbe);
            } finally {
                parentWindow.getStatusHandler().clearStatus();
            }
        }

        public void postAction() {
            doEventStuff();
        }
    }

    private class RemoveButtonActionListener extends CancelableAction {

        RemoveButtonActionListener() {
            super("Remove");
        }

        public void doAction(ActionEvent evt) {
            try {
                for (int index=0; index < usermodContentsTree.getSelectionCount();index++) {
                    DefaultMutableTreeNode selectedNode = (DefaultMutableTreeNode) usermodContentsTree.getSelectionPaths()[index].getLastPathComponent();
                    if (!selectedNode.equals(levelsNode) &&
                        !selectedNode.equals(tracksNode)) {
                        ((DefaultMutableTreeNode) selectedNode.getParent()).remove(selectedNode);
                    }
                }
                treeUtils.updateNodeStructure();
            } catch (Exception mbe) {
                throw new RuntimeException(mbe);
            } finally {
                parentWindow.getStatusHandler().clearStatus();
            }
        }

        public void postAction() {
            doEventStuff();
        }
    }

    private class ReleaseListItemListener implements java.awt.event.ItemListener {
        private String lastSelection = null;

        public void itemStateChanged(ItemEvent e) {
            if (releaseCombo.getSelectedRelease()!=null) {
                if (!releaseCombo.getSelectedRelease().getLibraryName().equals(lastSelection)) {
                    lastSelection = releaseCombo.getSelectedRelease().getLibraryName();
                    allAvailableLevelsMap = null;
                    allUnassignedTracksList = null;
                    levelsNode.removeAllChildren();
                    tracksNode.removeAllChildren();
                    addStagingLevelButton.setEnabled(true);
                    addTrackButton.setEnabled(true);
                    removeButton.setEnabled(true);

                    treeModel.nodeStructureChanged(usermodContentsRoot);
                    ReleaseUpdateEvent rue = new ReleaseUpdateEvent(e.getSource());
                    rue.setReleaseInformation(releaseCombo.getSelectedRelease());
                    if (GeneralUtilities.getParentAnimationStatus(releaseCombo)!=null) { //to get around couple of bugs. not an elegant way though.
                        (new HandleConfigurationInfo()).actionPerformed(new ActionEvent(releaseCombo , 0, "get config"));
                    }
                    fireEvent(rue);
                } else {
                    lastSelection = releaseCombo.getSelectedRelease().getLibraryName();
                }
            }
        }
    }

    public void handleFrameClosing() {
        if (releaseCombo.getSelectedRelease()!=null) {
            ComponentAndPathRestrictions restrictions = restrictionPanel.getComponentAndPathRestrictions();
            defaults.addPerReleaseSetting(setup, releaseCombo.getSelectedRelease().getLibraryName(), RESTRICTIONKEY, restrictions);
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

    private class HandleConfigurationInfo extends com.ibm.sdwb.build390.userinterface.graphic.widgets.CancelableAction {

        HandleConfigurationInfo() {
            super("Handle Usermod configuration settings");
            setEnabled(false);
        }

        public void doAction(ActionEvent e) {
            handleConfigurationSettings();
        }

        public void postAction() {
            if (!userStagingLevelEditable) {
                addStagingLevelButton.setEnabled(false);
                addTrackButton.setEnabled(false);
                removeButton.setEnabled(false);
            }
            doEventStuff();
        }

        public void stop() {
        }
    }


    public void ancestorAdded(AncestorEvent ae) {
        com.ibm.sdwb.build390.userinterface.graphic.utilities.GeneralUtilities.getParentInternalFrame((java.awt.Component) getParent()).addInternalFrameListener(new FrameListener());
        releaseCombo.forceEvent();
    }

    public void ancestorMoved(AncestorEvent ae) {
    }

    public void ancestorRemoved(AncestorEvent ae) {
    }
}
