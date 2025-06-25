package com.ibm.sdwb.build390.configuration;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.*;
import java.io.*;
import com.ibm.sdwb.build390.*;
import com.ibm.sdwb.build390.logprocess.*;
import javax.swing.tree.*;
import com.ibm.sdwb.build390.userinterface.graphic.widgets.JListDialogBox;
import com.ibm.sdwb.build390.library.*;
import com.ibm.sdwb.build390.metadata.MetadataOperationsInterface;
import com.ibm.sdwb.build390.process.*;
import com.ibm.sdwb.build390.process.steps.*;
import com.ibm.sdwb.build390.configuration.server.db2.ConfigurationRemoteServiceProvider;



public class ConfigurationAdministrationPanel extends MBInternalFrame {
    private JTree treeOfSettings;
    private DefaultMutableTreeNode rootNode = new DefaultMutableTreeNode("Configuration Settings");
    private DefaultTreeModel settingTreeModel = new DefaultTreeModel(rootNode);
    private boolean closingNow = false;

    private JButton loadButton = new JButton("Load");
    private JButton saveButton = new JButton("Save");
    private String release = new String();
    private Map settingsHash = new HashMap();
    private ConfigurationAdministrationPanel thisFrame;
    private Map lastSavedHash = new HashMap();
    protected LogEventProcessor lep=null;
    private JMenu m_options  = new JMenu("Options");
    private com.ibm.sdwb.build390.library.LibraryInfo libInfo = null;
    private MBMainframeInfo mainInfo = null;


    public ConfigurationAdministrationPanel(String tempRelease, com.ibm.sdwb.build390.library.LibraryInfo tempLib, MBMainframeInfo tempMain, com.ibm.sdwb.build390.logprocess.LogEventProcessor tempLep) {
        super("Build/390 Configuration Administration Tool", false, tempLep);
        libInfo = tempLib;
        mainInfo = tempMain;
        release = tempRelease;
        thisFrame = this;
        getJMenuBar().add(m_options);
        lep = tempLep;
        lep.LogSecondaryInfo("Debug:","Entry into ConfigAdmin Frame");
        setContentPane(buildContentPanel());
        pack();
        loadButton.setEnabled(true);
        saveButton.setEnabled(true);
        setVisible(true);
    }

    private JPanel buildContentPanel() {

        lep.LogSecondaryInfo("Debug:","ConfigAdminFrame : BuildContentPanel method");
        JPanel newContentPanel = new JPanel(new BorderLayout());

        loadButton.addActionListener(new ReleaseListRefreshAction(new com.ibm.sdwb.build390.process.MVSReleaseAndDriversList(mainInfo, libInfo, null, this)));

        saveButton.addActionListener(new SaveAction(new com.ibm.sdwb.build390.process.MVSReleaseAndDriversList(mainInfo, libInfo, null, this)));

        treeOfSettings = new JTree(settingTreeModel);
        treeOfSettings.setShowsRootHandles(true);
        treeOfSettings.setExpandsSelectedPaths(true);
        // set selection model
        treeOfSettings.getSelectionModel().setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);
        settingsHash = new HashMap();

        MouseListener ml = new MouseAdapter() {
            public void mouseReleased(MouseEvent e) {
                //Defect 204 
                //Please dont use e.isPopupTrigger() - this doesnt work in AIX 
                //instead trap the ButtonPressed -BUTTON3_MASK is mouse Right Click.
                if((e.getModifiers() & InputEvent.BUTTON3_MASK) != 0) {
                    //if (e.isPopupTrigger()) {
                    final int selRow = treeOfSettings.getRowForLocation(e.getX(), e.getY());
                    final TreePath selPath = treeOfSettings.getPathForLocation(e.getX(), e.getY());
                    final String realmStart;
                    final String sectionStart;
                    final String keywordStart;
                    final String valueStart;
                    if(selPath != null) {
                        if(selPath.getPathCount()>1) {
                            realmStart = ((DefaultMutableTreeNode) selPath.getPathComponent(1)).getUserObject().toString();
                        }
                        else {
                            realmStart = new String();
                        }
                        if(selPath.getPathCount()>2) {
                            sectionStart = ((DefaultMutableTreeNode) selPath.getPathComponent(2)).getUserObject().toString();
                        }
                        else {
                            sectionStart = new String();
                        }
                        if(selPath.getPathCount()>3) {
                            String settingString = ((DefaultMutableTreeNode) selPath.getPathComponent(3)).getUserObject().toString();
                            StringTokenizer keyValParser = new StringTokenizer(settingString, "=");
                            if(keyValParser.countTokens()>1) {
                                keywordStart = keyValParser.nextToken().trim();
                                valueStart = keyValParser.nextToken().trim();
                            }
                            else {
                                keywordStart = new String();
                                valueStart = new String();
                            }
                        }
                        else {
                            keywordStart = new String();
                            valueStart = new String();
                        }
                    }
                    else {
                        keywordStart = new String();
                        valueStart = new String();
                        sectionStart = new String();
                        realmStart = new String();
                    }
                    if(selRow != -1) {
                        JPopupMenu menu = new JPopupMenu("Edit");
                        JMenuItem add = new JMenuItem(new AddSettingAction(realmStart, sectionStart, keywordStart, valueStart)); ;
                        JMenuItem remove = new JMenuItem("Remove Branch");
                        if(selPath.getPathCount()<3) {
                            remove.setEnabled(false);
                        }
                        remove.addActionListener(new ActionListener() {
                                                     public void actionPerformed(ActionEvent e) {
                                                         if(selPath.getPathCount()==3) {
                                                             settingsHash.remove( ((DefaultMutableTreeNode)selPath.getLastPathComponent()).getUserObject());
                                                         }
                                                         else if(selPath.getPathCount()==4) {
                                                             Map realmHash =  (HashMap) settingsHash.get( ((DefaultMutableTreeNode) selPath.getPathComponent(1)).getUserObject());
                                                             Map sectionHash = (HashMap) realmHash.get( ((DefaultMutableTreeNode) selPath.getPathComponent(2)).getUserObject());
                                                             String settingString = (String) ((DefaultMutableTreeNode)selPath.getLastPathComponent()).getUserObject();
                                                             sectionHash.remove(settingString.substring(0, settingString.indexOf("=")));
                                                             if(sectionHash.isEmpty()) {
                                                                 realmHash.remove(((DefaultMutableTreeNode) selPath.getPathComponent(2)).getUserObject());
                                                             }
                                                             if(realmHash.isEmpty()) {
                                                                 settingsHash.remove(((DefaultMutableTreeNode) selPath.getPathComponent(1)).getUserObject());
                                                             }
                                                         }
//								 populateMyTree(settingsHash);
                                                         settingTreeModel.removeNodeFromParent((MutableTreeNode)selPath.getLastPathComponent());
                                                     }
                                                 });
                        menu.add(add);
                        menu.add(remove);
                        menu.show(e.getComponent(),e.getX(),e.getY());
                    }
                }
            }
        };
        treeOfSettings.addMouseListener(ml);


        JPanel centerPanel = new JPanel(new BorderLayout());
        centerPanel.add("Center", new JScrollPane(treeOfSettings));

        newContentPanel.add("Center", centerPanel);
        newContentPanel.add("North", new JLabel("Right click on tree to change nodes"));

        Box buttonBox = Box.createHorizontalBox();
        buttonBox.add(loadButton);
        buttonBox.add(saveButton);

        newContentPanel.add("South", buttonBox);
        return newContentPanel;
    }

    private Map buildSettingHashFromTree() {
        lep.LogSecondaryInfo("Debug:","ConfigAdminFrame : BuildSettingHashFromTree method");
        Map treeSettingHash = new HashMap();
        Enumeration realmEnum = rootNode.children();
        while(realmEnum.hasMoreElements()) {
            DefaultMutableTreeNode oneRealm = (DefaultMutableTreeNode) realmEnum.nextElement();
            Map realmHash = new HashMap();
            treeSettingHash.put(oneRealm.getUserObject().toString(), realmHash);

            Enumeration sectionEnum = oneRealm.children();
            while(sectionEnum.hasMoreElements()) {
                DefaultMutableTreeNode oneSection = (DefaultMutableTreeNode) sectionEnum.nextElement();
                Map sectionHash = new HashMap();

                //TST3539
                if(oneSection.getChildCount()==0) {
                    continue;
                }
                //TST3539

                realmHash.put(oneSection.getUserObject().toString(), sectionHash);
                Enumeration sectionSettingEnum = oneSection.children();
                while(sectionSettingEnum.hasMoreElements()) {
                    DefaultMutableTreeNode oneSetting = (DefaultMutableTreeNode) sectionSettingEnum.nextElement();
                    StringTokenizer settingParser = new StringTokenizer(oneSetting.getUserObject().toString(), "=");
                    if(settingParser.countTokens()==2) {
                        String keyword = settingParser.nextToken();
                        String value = settingParser.nextToken();
                        sectionHash.put(keyword, value);
                    }
                }
            }
        }

        return treeSettingHash;
    }

    private Map buildSettingHash(com.ibm.sdwb.build390.library.LibraryInfo libInfo, String release) throws Exception{
        lep.LogSecondaryInfo("Debug:","ConfigAdminFrame : BuildSettingHash method");
        Map tempSettings = libInfo.getConfigurationAccess(release, false).getAllConfigurationSettings();
        return tempSettings;
    }

    class ReleaseListRefreshAction extends com.ibm.sdwb.build390.userinterface.graphic.widgets.CancelableProcessAction {

        ReleaseListRefreshAction(com.ibm.sdwb.build390.process.MVSReleaseAndDriversList refreshProcess) {
            super("Load Release", refreshProcess);
        }

        public void doAction(ActionEvent e) {
            try {

                boolean doLoad = true;
                if(!areMapsEqual(lastSavedHash,buildSettingHashFromTree())) {
                    int response = JOptionPane.showConfirmDialog(thisFrame, "If you load, you will lose the changes you made.", "Load confirmation", JOptionPane.YES_NO_OPTION);
                    if(response != JOptionPane.YES_OPTION) {
                        doLoad=false;
                    }
                }
                if(doLoad) {
                    ProcessWrapperForSingleStep wrapper = new ProcessWrapperForSingleStep(theProcess);
                    CheckConnectionToLibrary connCheck = new CheckConnectionToLibrary(libInfo,wrapper);
                    wrapper.setStep(connCheck);
                    wrapper.externalRun();

                    java.util.List listOfReleases = new ArrayList();
                    Set allReleases = mainInfo.getReleaseSet(libInfo);
                    if(allReleases!=null) {
                        for(Iterator releaseIterator = allReleases.iterator(); releaseIterator.hasNext();) {
                            com.ibm.sdwb.build390.mainframe.ReleaseInformation oneRelease = (com.ibm.sdwb.build390.mainframe.ReleaseInformation) releaseIterator.next();
                            listOfReleases.add(oneRelease.getLibraryName());
                        }
                    }

                    JListDialogBox releaseChooser = new JListDialogBox("Release Chooser",(JInternalFrame) thisFrame, lep);
                    releaseChooser.setData(listOfReleases);
                    releaseChooser.setAllowMultipeSelection(false);
                    if(release.trim().length() > 0) {
                        releaseChooser.setSelectedItem(release);
                    }
                    releaseChooser.setVisible(true);
                    if(releaseChooser.getElementSelected()!=null) {
                        release = releaseChooser.getElementSelected();
                        settingsHash = buildSettingHash(libInfo, release);
                        populateMyTree(settingsHash);
                        lastSavedHash = buildSettingHashFromTree();
                    }
                }
                else {
                    JOptionPane.showMessageDialog(thisFrame, "No updates to the tree, since saved data is same as the tree data.", "Reload data", JOptionPane.WARNING_MESSAGE);
                }

                super.doAction(e);
            }
            catch(MBBuildException mbe) {
                getLEP().LogException(mbe);
            }
            catch(Exception exception1) {
                getLEP().LogException("Problem getting the list of configuration settings.", exception1);
            }
        }
    }



    class AddSettingAction extends com.ibm.sdwb.build390.userinterface.graphic.widgets.CancelableAction {

        private String realmStart = null;
        private String sectionStart = null;
        private String keywordStart = null;
        private String valueStart = null;

        AddSettingAction(String tempRealm, String tempSection, String tempKeyword, String tempValue) {
            super("Add/Change");
            realmStart = tempRealm;
            sectionStart = tempSection;
            keywordStart = tempKeyword;
            valueStart = tempValue;
        }

        public void doAction(ActionEvent e) {
            ConfigurationEditSettingsDialog newSettings = new ConfigurationEditSettingsDialog(thisFrame,realmStart, sectionStart, keywordStart, valueStart,getLEP());
            if(newSettings.getRealm()!=null) {
                Map realmToChange = (Map) settingsHash.get(newSettings.getRealm());
                if(realmToChange == null) {
                    realmToChange = new HashMap();
                    settingsHash.put(newSettings.getRealm(), realmToChange);
                }
                Map hashToChange = (Map) realmToChange.get(newSettings.getSection());
                if(hashToChange == null) {
                    hashToChange = new HashMap();
                    realmToChange.put(newSettings.getSection(), hashToChange);
                }
                hashToChange.put(newSettings.getKeyword(),newSettings.getValue());
//Ken, test code									 TreePopulator.insertNode(treeOfSettings, newSettings.getSection(), newSettings.getKeyword()+"="+newSettings.getValue());
                populateMyTree(settingsHash);
            }
        }
    }


    private void populateMyTree(Map treeHash) {
        lep.LogSecondaryInfo("Debug:","ConfigAdminFrame : PopulateMyTree method");
        TreePath rootPath = treeOfSettings.getPathForRow(0);
        Map nodeStates = createNodeExpansionStateHash(rootPath);

        Map allHash = new HashMap();
        for(Iterator realmIterator = treeHash.keySet().iterator(); realmIterator.hasNext();) {
            String realm = (String) realmIterator.next();
            Map realmHash = (Map) treeHash.get(realm);
            Map sectionHash = new HashMap();
            allHash.put(realm, sectionHash);
            for(Iterator sectionIterator = realmHash.keySet().iterator(); sectionIterator.hasNext(); ) {
                String section = (String) sectionIterator.next();
                Map sectionValues = (Map) realmHash.get(section);
                Map newValues = new HashMap();
                sectionHash.put(section, newValues);
                for(Iterator keywordIter = sectionValues.keySet().iterator(); keywordIter.hasNext();) {
                    String keyword = (String) keywordIter.next();
                    String value = (String) sectionValues.get(keyword);
                    newValues.put(keyword+"="+value, new HashMap());
                }
            }
        }
        TreePopulator.populateTree("Configuration Settings", allHash, treeOfSettings);
        restoreNodeExpansionState(nodeStates);
        if(nodeStates.isEmpty()) {
            // expand all nodes
            for(int x=0; x<treeOfSettings.getRowCount(); x++) {
                treeOfSettings.expandRow(x);
            }

        }
    }

    private Map createNodeExpansionStateHash(TreePath rootPath) {
        lep.LogSecondaryInfo("Debug:","ConfigAdminFrame : createNodeExpansionStateHash");
        Map returnHash = new HashMap();
        if(rootPath != null) {
            Enumeration pathEnum = treeOfSettings.getExpandedDescendants(rootPath);
            if(pathEnum!=null) {
                while(pathEnum.hasMoreElements()) {
                    TreePath tempPath = (TreePath) pathEnum.nextElement();
                    if(!tempPath.equals(rootPath)) {
                        returnHash.put(tempPath, createNodeExpansionStateHash(tempPath));
                    }
                }
            }
        }
        return returnHash;
    }

    private void restoreNodeExpansionState(Map expansionState) {
        lep.LogSecondaryInfo("Debug:","ConfigAdminFrame : restoreNodeExpansionState");
        for(Iterator nodeIterator = expansionState.keySet().iterator(); nodeIterator.hasNext();) {
            TreePath tempPath = createCurrentTreePath((TreePath) nodeIterator.next());
            if(tempPath != null) {
                treeOfSettings.expandPath(tempPath);
                restoreNodeExpansionState((Map)expansionState.get(tempPath));
            }
        }
    }

    private TreePath createCurrentTreePath(TreePath oldPath) {
        DefaultTreeModel tempMod = (DefaultTreeModel) treeOfSettings.getModel();
        DefaultMutableTreeNode tempRoot = (DefaultMutableTreeNode) tempMod.getRoot();
        TreePath newPath = null;
        if(tempRoot.getUserObject().equals(((DefaultMutableTreeNode) oldPath.getPathComponent(0)).getUserObject())) {
            newPath = new TreePath(tempRoot);
        }
        else {
            return null;
        }

        for(int i = 0; i < oldPath.getPathCount()-1; i++) {
            DefaultMutableTreeNode tempOldNode = (DefaultMutableTreeNode) oldPath.getPathComponent(i);
            Enumeration kidEnum = tempRoot.children();
            boolean found = false;
            while(kidEnum.hasMoreElements() & !found) {
                DefaultMutableTreeNode tempKid = (DefaultMutableTreeNode) kidEnum.nextElement();
                if(tempKid.getUserObject().equals(tempOldNode.getUserObject())) {
                    tempRoot = tempKid;
                    found = true;
                    newPath = newPath.pathByAddingChild(tempKid);
                }
            }
            if(!found) {
                return null;
            }
        }
        return newPath;
    }

    private void setConfigSettings(String release, Map settingMap) throws Exception{
        lep.LogSecondaryInfo("Debug:","ConfigAdminFrame : setConfigSettings");
        ConfigurationRemoteServiceProvider updater = libInfo.getConfigurationServer();
        updater.setContiguration(release, settingMap, libInfo.getAuthorizationChecker());
    }

    class SaveAction extends com.ibm.sdwb.build390.userinterface.graphic.widgets.CancelableProcessAction {

        SaveAction(com.ibm.sdwb.build390.process.MVSReleaseAndDriversList refreshProcess) {
            super("Save", refreshProcess);
        }

        public void doAction(ActionEvent e) {
            try {
                ProcessWrapperForSingleStep wrapper = new ProcessWrapperForSingleStep(theProcess);
                CheckConnectionToLibrary connCheck = new CheckConnectionToLibrary(libInfo,wrapper);
                wrapper.setStep(connCheck);
                wrapper.externalRun();

                java.util.List listOfReleases = new ArrayList();
                Set allReleases = mainInfo.getReleaseSet(libInfo);
                if(allReleases!=null) {
                    for(Iterator releaseIterator = allReleases.iterator(); releaseIterator.hasNext();) {
                        com.ibm.sdwb.build390.mainframe.ReleaseInformation oneRelease = (com.ibm.sdwb.build390.mainframe.ReleaseInformation) releaseIterator.next();
                        listOfReleases.add(oneRelease.getLibraryName());
                    }
                }

                JListDialogBox releaseChooser = new JListDialogBox("Release Chooser",(JInternalFrame) thisFrame, lep);
                releaseChooser.setData(listOfReleases);
                releaseChooser.setAllowMultipeSelection(true);
                if(release.trim().length() > 0) {
                    releaseChooser.setSelectedItem(release);
                }
                releaseChooser.setVisible(true);
                Map totalSettingHash = buildSettingHashFromTree();
                Map saveErrorMap = new HashMap();

                String[] releasesSelected = releaseChooser.getElementsSelected();
                if(releasesSelected!=null && releasesSelected.length > 0) {
                    for(int releaseIndex = 0; releaseIndex < releasesSelected.length; releaseIndex++) {
                        String currentRelease = (String) releasesSelected[releaseIndex];
                        try {
                            setConfigSettings(currentRelease, totalSettingHash);
                        }
                        catch(Exception except) {
                            saveErrorMap.put(currentRelease, except);
                        }
                        lastSavedHash = totalSettingHash;
                    }
                    if(!saveErrorMap.isEmpty()) {
                        for(Iterator errorIterator = saveErrorMap.keySet().iterator(); errorIterator.hasNext();) {
                            String rel = (String) errorIterator.next();
                            Exception le = (Exception)saveErrorMap.get(rel);
                            lep.LogException(le.getMessage(),le);
                        }
                    }
                }
                super.doAction(e);
            }
            catch(MBBuildException mbe) {
                getLEP().LogException(mbe);
            }
        }
    }

    private boolean areMapsEqual(Map hash1, Map hash2) {
        if(hash1==null & hash2==null) {
            return true;
        }
        else if(hash1.isEmpty() & hash2.isEmpty()) {
            return true;
        }
        if(hash1==null | hash2==null) {
            return false;
        }
        if(hash1.size()!=hash2.size()) {
            return false;
        }
        Iterator hash1Keys = hash1.keySet().iterator();
        while(hash1Keys.hasNext()) {
            String currKey = (String) hash1Keys.next();
            if(hash1.get(currKey) instanceof Map) {
                if(!areMapsEqual((Map)hash1.get(currKey),(Map) hash2.get(currKey))) {
                    return false;
                }
            }
            else if(!hash1.get(currKey).equals(hash2.get(currKey))) {
                return false;
            }
        }
        return true;
    }
}
