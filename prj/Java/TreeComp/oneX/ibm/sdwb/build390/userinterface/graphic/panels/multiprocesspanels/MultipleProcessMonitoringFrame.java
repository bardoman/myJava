package com.ibm.sdwb.build390.userinterface.graphic.panels.multiprocesspanels;

import java.awt.CardLayout;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.math.BigDecimal;
import java.util.*;

import javax.swing.*;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeSelectionModel;

import com.ibm.sdwb.build390.*;
import com.ibm.sdwb.build390.info.*;
import com.ibm.sdwb.build390.userinterface.event.*;
import com.ibm.sdwb.build390.userinterface.event.multiprocess.*;
import com.ibm.sdwb.build390.userinterface.graphic.panels.build.BuildPanel;
import com.ibm.sdwb.build390.userinterface.graphic.widgets.*;

public class MultipleProcessMonitoringFrame extends MBInternalFrame implements TreeSelectionListener, BuildFrameInterface {

    private Map infoToTreeNodeMap = new HashMap();
    private Map infoToPanelMap = new HashMap();
    private Map nodeToPanelMap = new HashMap();
    private List processList = new ArrayList();
    private JPanel blankPanel = new JPanel();
    private JPanel overallInfoPanel = new JPanel();
    private DefaultMutableTreeNode top = null;
    protected DefaultTreeModel treeModel = null;
    private JTree processTree = new JTree();
    private com.ibm.sdwb.build390.userinterface.graphic.utilities.JTreeUtilities treeUtils = new com.ibm.sdwb.build390.userinterface.graphic.utilities.JTreeUtilities(processTree);
    private JSplitPane leftRightSplit = null;
    private JSplitPane topBottomSplit = null;
    private JButton executeButton = new JButton();
    private BuildPanel buildPanel =null;
    private final String HORIZONTAL_DIVIDER_LOCATION_SAVE_KEY = "HORIZONTAL_DIVIDER_LOCATION_SAVE_KEY";
    private final String VERTICAL_DIVIDER_LOCATION_SAVE_KEY = "VERTICAL_DIVIDER_LOCATION_SAVE_KEY";
    private JButton btHelp = new JButton("Help");
    private final String BLANKKEY = "BLANKPANEL";

    public MultipleProcessMonitoringFrame(String title) {
        super(title, true, null);

        JScrollPane processTreeScrollPane = new JScrollPane(processTree);
        processTreeScrollPane.setMinimumSize(new java.awt.Dimension(0,0));
        leftRightSplit = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT,processTreeScrollPane , blankPanel);
        topBottomSplit = new JSplitPane(JSplitPane.VERTICAL_SPLIT, new JScrollPane(overallInfoPanel), leftRightSplit);
        getContentPane().add(java.awt.BorderLayout.CENTER, topBottomSplit);
        leftRightSplit.setResizeWeight(0.5);
        setDividerDefaults();
        //leftRightSplit.setContinuousLayout(true);
        topBottomSplit.setOneTouchExpandable(true);
        processTree.getSelectionModel().setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);
        top = new DefaultMutableTreeNode("Processes");
        treeModel = new DefaultTreeModel(top);
        processTree.setModel(treeModel);
        processTree.setRootVisible(true);
        processTree.setShowsRootHandles(true);
        processTree.addTreeSelectionListener(this);
        Set componentsToAvoid = new HashSet();
        componentsToAvoid.add(processTree);
        setNondisablableComponentClasses(componentsToAvoid);
        Vector actionButtons = new Vector();
        actionButtons.addElement(executeButton);
        addButtonPanel(btHelp, actionButtons);
        executeButton.setEnabled(false);
        setDefaultCloseOperation(JInternalFrame.DO_NOTHING_ON_CLOSE);
    }

    public void setDividerDefaults() {
        //these are user preferences. probably should inside the defaults in separate hash.
        Object  tempWidth = MBBasicInternalFrame.getGenericStatic(HORIZONTAL_DIVIDER_LOCATION_SAVE_KEY);
        if(tempWidth!=null){
        leftRightSplit.setDividerLocation(((Integer)tempWidth).intValue() + leftRightSplit.getInsets().left);
        } else{
            leftRightSplit.setDividerLocation(120 + leftRightSplit.getInsets().left);
        }

        tempWidth = MBBasicInternalFrame.getGenericStatic(VERTICAL_DIVIDER_LOCATION_SAVE_KEY);
        if(tempWidth!=null){
            topBottomSplit.setDividerLocation(((Integer)tempWidth).intValue() + topBottomSplit.getInsets().top);
        } else{
            topBottomSplit.setDividerLocation(340+ topBottomSplit.getInsets().top);
        }
    }

    public JSplitPane getTopBottomScrollPane() {
        return topBottomSplit;
    }

    public BuildPanel getBuildPanel() {
        return buildPanel;
    }

    public void setBuildPanel(BuildPanel tempBuildPanel) {
        buildPanel = tempBuildPanel;
    }

    public JPanel getUserInfoPanel() {
        return overallInfoPanel;
    }

    public void setTreeRootName(String label) {
        top.setUserObject(label);
    }

    public void setCancelableButtonVisibility(boolean vis) {
        btnCancel.setVisible(vis);
    }

    public void setBuildButtonEnabled(boolean enabled) {
        executeButton.setEnabled(enabled);
    }

    public JButton getHelpButton() {
        return btHelp;
    }

    public MBInternalFrame getInternalFrame() {
        return this;
    }

    public void setBuildAction(Action action) {
        executeButton.setAction(new ExecuteProcessesAction(action));
    }

    public void setHelpLocation(String helpLocation1, String helpLocation2) {
        javax.swing.event.ChangeListener listeners[] = btHelp.getChangeListeners();
        for (int changeIndex = 0; changeIndex < listeners.length; changeIndex++) {
            btHelp.removeChangeListener(listeners[changeIndex]);
        }
        btHelp.addActionListener(MBUtilities.getHelpListener(helpLocation1,helpLocation2));
    }

    public void handleUIEvent(UserInterfaceEvent event) {
        if (event instanceof ChangeRequestPartitionedUpdateEvent) {
            handleChangeRequestPartionedUpdateEvent((ChangeRequestPartitionedUpdateEvent) event);
        } else if (event instanceof ChangeRequestMultipleUpdateEvent) {
            handleChangeRequestMultipleUpdateEvent((ChangeRequestMultipleUpdateEvent) event);
        }

    }


    private void handleChangeRequestPartionedUpdateEvent(ChangeRequestPartitionedUpdateEvent event) {
        ChangeRequestPartitionedInfo info = event.getChangeRequestPartionedInfo();
        updateChangeRequestPartitionedUI(event);
        OnePartitionedBuildPanel panel = (OnePartitionedBuildPanel)infoToPanelMap.get(info); 
        panel.handleUIEvent(event);
    }

    private void handleChangeRequestMultipleUpdateEvent(ChangeRequestMultipleUpdateEvent event) {
        ChangeRequestMultipleInfo info = event.getChangeRequestMultipleInfo();
        DefaultMutableTreeNode infoNode = (DefaultMutableTreeNode) infoToTreeNodeMap.get(info);
        if (infoNode == null) {
            infoNode = new DefaultMutableTreeNode("Warnings:");
            infoToTreeNodeMap.put(info, infoNode);
            treeUtils.addTreeNodeToTreeNodeSorted(top, infoNode);
            treeUtils.updateNodeStructure();
        }
        OneChangesetMultipleBuildPanel panel = (OneChangesetMultipleBuildPanel)infoToPanelMap.get(info); 
        if (panel == null) {
            panel = new OneChangesetMultipleBuildPanel(getLEP());
            infoToPanelMap.put(info, panel);
            nodeToPanelMap.put(infoNode, panel);
        }
        panel.handleUIEvent(event);
    }


    private void updateChangeRequestPartitionedUI(ChangeRequestPartitionedUpdateEvent event) {
        ChangeRequestPartitionedInfo info = event.getChangeRequestPartionedInfo();
        DefaultMutableTreeNode infoNode = (DefaultMutableTreeNode) infoToTreeNodeMap.get(info);
        if (infoNode == null) {
            infoNode = new DefaultMutableTreeNode(info.get_buildid());
            infoToTreeNodeMap.put(info, infoNode);
            treeUtils.addTreeNodeToTreeNodeSorted(top, infoNode);
            treeUtils.updateNodeStructure();
        }

        OnePartitionedBuildPanel panel = (OnePartitionedBuildPanel)infoToPanelMap.get(info); 
        if (panel == null) {
            panel = new OnePartitionedBuildPanel(this, getLEP());
            infoToPanelMap.put(info, panel);
            nodeToPanelMap.put(infoNode, panel);
        }
    }

    public void handleChangesetGroupUpdateEvent(ChangesetGroupUpdateEvent event) {
        ChangesetGroupInfo info = event.getChangesetGroupInfo();
        updateChangeSetGroupUI(event);
        OneChangesetGroupBuildPanel panel = (OneChangesetGroupBuildPanel)infoToPanelMap.get(info); 
        panel.handleUIEvent(event);
    }

    public void updateChangeSetGroupUI(ChangesetGroupUpdateEvent event) {
        ChangesetGroupInfo info = event.getChangesetGroupInfo();
        DefaultMutableTreeNode infoNode = (DefaultMutableTreeNode) infoToTreeNodeMap.get(info);
        if (infoNode == null) {
            infoNode = new DefaultMutableTreeNode(info.get_buildid());
            infoToTreeNodeMap.put(info, infoNode);
            DefaultMutableTreeNode parentNode = (DefaultMutableTreeNode) infoToTreeNodeMap.get(info.getParentInfo());
            treeUtils.addTreeNodeToTreeNodeSorted(parentNode, infoNode);
            treeUtils.updateNodeStructure();
        }
        OneChangesetGroupBuildPanel panel = (OneChangesetGroupBuildPanel)infoToPanelMap.get(info); 
        if (panel == null) {
            panel = new OneChangesetGroupBuildPanel(getLEP());
            infoToPanelMap.put(info, panel);
            nodeToPanelMap.put(infoNode, panel);
        }
    }

    public Dimension getMinimumSize() {
        return new Dimension(200, 200);
    }

    public Dimension getPreferredSize() {
        return new Dimension(490, 580);
    }

    public void dispose() {
        MBBasicInternalFrame.putGenericStatic(HORIZONTAL_DIVIDER_LOCATION_SAVE_KEY, new Integer(leftRightSplit.getDividerLocation()));
        MBBasicInternalFrame.putGenericStatic(VERTICAL_DIVIDER_LOCATION_SAVE_KEY, new Integer(topBottomSplit.getDividerLocation()));
        dispose(false);
    }   

    public void internalFrameClosing(javax.swing.event.InternalFrameEvent e) {
        dispose();
    }


    public void valueChanged(TreeSelectionEvent tse) {
        DefaultMutableTreeNode selectedNode = (DefaultMutableTreeNode) processTree.getLastSelectedPathComponent();
        JPanel showPanel = new JPanel();
        if (selectedNode != null) {
            showPanel = (JPanel) nodeToPanelMap.get(selectedNode);
        }
        leftRightSplit.setRightComponent(showPanel);
        leftRightSplit.invalidate();
    }

    private class ExecuteProcessesAction extends AbstractAction {
        private Action theAction = null;

        ExecuteProcessesAction(Action tempListener) {
            super("Build");
            theAction = tempListener;
        }

        public void actionPerformed(ActionEvent e) {
            topBottomSplit.setDividerLocation((double)0.300);
//            topBottomSplit.invalidate();
            theAction.actionPerformed(e);
        }
    }
}
