package com.ibm.sdwb.build390.userinterface.graphic.utilities;

import java.util.*;
import java.awt.*;
import javax.swing.*;
import javax.swing.tree.*;

public class JTreeUtilities {

    private JTree theTree = null;

    public JTreeUtilities(JTree tempTree) {
        theTree = tempTree;
    }

    public void updateNodeStructure() {
        Map expandedMap = new HashMap();
        mapNodeExpansion(getRoot(), expandedMap);
        ((DefaultTreeModel)theTree.getModel()).nodeStructureChanged(getRoot());
        restoreNodeExpansion(getRoot(), expandedMap);
        theTree.repaint();
    }

    public void expandTree() {
        for (int i = 0; i < theTree.getRowCount(); i++) {
            theTree.expandRow(i);
        }
    }

    public void expandPath(TreePath thePath) {
        if (thePath.getPathCount()>1) {
            expandPath(thePath.getParentPath());
        }
        theTree.expandRow(theTree.getRowForPath(thePath));
        theTree.repaint();
    }

    private DefaultMutableTreeNode getRoot(){
        return (DefaultMutableTreeNode)theTree.getModel().getRoot();
    }

    private void mapNodeExpansion(DefaultMutableTreeNode topNode, Map tempMap) {
        tempMap.put(topNode, new Boolean(theTree.isExpanded(new TreePath(topNode.getPath()))));
        for (Enumeration nodeEnum = topNode.children(); nodeEnum.hasMoreElements();) {
            mapNodeExpansion((DefaultMutableTreeNode) nodeEnum.nextElement(),tempMap);
        }
    }

    private void restoreNodeExpansion(DefaultMutableTreeNode topNode, Map tempMap) {
        boolean expanded = ((Boolean) tempMap.get(topNode)).booleanValue();
        if (expanded) {
            expandPath(new TreePath(topNode.getPath()));
        }
        for (Enumeration nodeEnum = topNode.children(); nodeEnum.hasMoreElements();) {
            restoreNodeExpansion((DefaultMutableTreeNode) nodeEnum.nextElement(),tempMap);
        }
    }

    public void addTreeNodeToTreeNodeSorted(DefaultMutableTreeNode parent, DefaultMutableTreeNode child){
        int returnIndex = -1;
        for (int i = 0; i < parent.getChildCount() & returnIndex< 0; i++) {
            DefaultMutableTreeNode oneNode = (DefaultMutableTreeNode) parent.getChildAt(i);
            if (oneNode.getUserObject().toString().compareToIgnoreCase(child.getUserObject().toString()) > 0){
                returnIndex = i;
            }
        }
        if (returnIndex < 0) {
            parent.add(child);
        }else {
            parent.insert(child, returnIndex);
        }

        TreePath tempPath = new TreePath(child.getPath());
        expandPath(tempPath);
    }

}
