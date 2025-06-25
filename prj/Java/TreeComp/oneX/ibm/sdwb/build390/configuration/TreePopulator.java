package com.ibm.sdwb.build390.configuration;

import javax.swing.*;
import java.util.*;
import javax.swing.tree.*;
import com.ibm.sdwb.build390.AlphabetizedVector;


public class TreePopulator {

/* this method takes the name of the release, a hash containing all the driver relations, and a hash telling the type 
  	of each driver, and fills the tree based on this.
*/	
	public static void populateTree(String topNodeName, Map nodeMap, JTree theTree){
		DefaultMutableTreeNode top = (DefaultMutableTreeNode)theTree.getModel().getRoot();
		top.removeAllChildren();
		top.setUserObject(topNodeName);
		populateNode(top, nodeMap);
		theTree.setRootVisible(true);
		theTree.setShowsRootHandles(true);
		((DefaultTreeModel)theTree.getModel()).reload();
	}

/*
	This recursively goes through and populates a node (given) with nodes it creates from the hashtable
*/
	private static void populateNode(DefaultMutableTreeNode parentNode, Map nodeMap){
		AlphabetizedVector nodeVector = new AlphabetizedVector();
		if (nodeMap == null) {
			nodeMap = new HashMap();
		}
// stick the drivers in an alphabetized vector, so they come out nice and sorted	 
        for (Iterator childNodeIterator = nodeMap.keySet().iterator(); childNodeIterator.hasNext();) {
			String nextName = (String) childNodeIterator.next();
			nodeVector.addElement(nextName);
		}
		for (int i = 0; i < nodeVector.size(); i++) {
			String currentNodeName = (String) nodeVector.elementAt(i);
			Map currentNodeMap = (Map) nodeMap.get(currentNodeName);
			DefaultMutableTreeNode currentNode = new DefaultMutableTreeNode(currentNodeName);
			populateNode(currentNode, currentNodeMap);
			parentNode.add(currentNode);
		}
	}
	
	public static boolean insertNode(JTree currTree, String parentNodeName, String newNodeName){
		if (insertNode((DefaultMutableTreeNode) currTree.getModel().getRoot(),parentNodeName, newNodeName)){
			((DefaultTreeModel)currTree.getModel()).reload();
			return true;
		} else{
			return false;
		}
	}

	private static boolean insertNode(DefaultMutableTreeNode rootNode, String parentNodeName, String newNodeName){
		Enumeration kids = rootNode.children();
		while (kids.hasMoreElements()) {
			DefaultMutableTreeNode currKid = (DefaultMutableTreeNode) kids.nextElement();
			if (currKid.getUserObject().equals(parentNodeName)) {
				DefaultMutableTreeNode newNode = new DefaultMutableTreeNode(newNodeName);
				AlphabetizedVector newKidVector = new AlphabetizedVector();
				Map nodeMap = new HashMap();
				nodeMap.put(newNodeName, newNode);
				newKidVector.addElement(newNodeName);
				Enumeration siblings = currKid.children();
				while (siblings.hasMoreElements()) {
					DefaultMutableTreeNode currNode = (DefaultMutableTreeNode) siblings.nextElement();
					newKidVector.addElement(currNode.getUserObject());
					nodeMap.put(currNode.getUserObject(), currNode);
				}
				currKid.removeAllChildren();
				for (int i = 0; i < newKidVector.size(); i++) {
					currKid.add((DefaultMutableTreeNode) nodeMap.get(newKidVector.elementAt(i)));
				}
				return true;
			}else if (insertNode(currKid, parentNodeName, newNodeName)) {
				return true;
			}
		}
		return false;
	}
}
