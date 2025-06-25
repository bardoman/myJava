// Decompiled by Jad v1.5.8g. Copyright 2001 Pavel Kouznetsov.
// Jad home page: http://www.kpdus.com/jad.html
// Decompiler options: packimports(3) 

package com.ibm.sdwb.build390.help;

import java.awt.*;
import java.awt.event.*;
import java.net.*;
import java.util.*;

import javax.swing.*;
import javax.swing.border.*;

import oracle.bali.ewt.print.PrinterUtils;
import oracle.bali.ewt.util.WindowUtils;
import oracle.help.common.Topic;
import oracle.help.common.TopicTreeNode;
import oracle.help.common.View;
import oracle.help.common.navigator.tocNavigator.MergingTopicTreeNode;
import oracle.help.common.navigator.tocNavigator.TOCUtils;
import oracle.help.common.util.java.StaticLocaleContext;
import oracle.help.java.tree.TopicTreeComponent;
import oracle.help.java.tree.TopicTreeItem;
import oracle.help.java.tree.TopicTreeListener;
import oracle.help.java.tree.TopicTreePane;
import oracle.help.java.util.MenuUtils;
import oracle.help.navigator.Navigator;
import oracle.help.navigator.tocNavigator.TOCItemFactory;
import oracle.help.navigator.tocNavigator.TOCNavigator;

public class B390TOCNavigator extends Navigator {

    private static String TYPE_TOCNAVIGATOR = "com.ibm.sdwb.build390.help.B390TOCNavigator";
    private static String GENERIC_RESOURCE_BUNDLE = "oracle.help.resource.Generic";
    private static String DEFAULT_LABEL_ID = "navigator.tocNavigator.default_label";
    private TopicTreePane treePane;
    private MergingTopicTreeNode rootTopicTreeNode;

    public B390TOCNavigator() {
        //The oracle.help.navigator.tocNavigator.TOCNavigator doesn't initialize its instance.
        super();
    }

    public void initNavigator(View aview[]) {
        setLayout(new BorderLayout());
        setBorder(new EmptyBorder(3, 1, 1, 1));
        rootTopicTreeNode = new MergingTopicTreeNode(aview);
        treePane = new TopicTreePane(new TopicTreeComponent(rootTopicTreeNode, TOCItemFactory.getInstance()));
        add(treePane);
        treePane.addTopicTreeListener(new TreePaneListener());
    }

    public String getDefaultLabel() {
        ResourceBundle resourcebundle = ResourceBundle.getBundle(GENERIC_RESOURCE_BUNDLE, StaticLocaleContext.getLocale());
        String s = resourcebundle.getString(DEFAULT_LABEL_ID);
        return s;
    }

    public Topic getCurrentSelection() {
        TopicTreeItem topictreeitem = treePane.getSelectedItem();
        return topictreeitem != null ? topictreeitem.getTopic() : null;
    }

    public TopicTreeNode getCurrentSelectionNode() {
        TopicTreeItem topictreeitem = treePane.getSelectedItem();
        return topictreeitem != null ? topictreeitem.getTopicTreeNode() : null;
    }

    public void addView(View view) {
        if (view != null && view.getType().equals(TYPE_TOCNAVIGATOR))
            rootTopicTreeNode.addView(view);
    }

    public void removeView(View view) {
        if (view != null && view.getType().equals(TYPE_TOCNAVIGATOR))
            rootTopicTreeNode.removeView(view);
    }

    public void printNavigator() {
        try {
            PrinterUtils.print(treePane.getTopicTreeComponent(), null, false, WindowUtils.parentFrame(this), "OHJ Table of Contents", null, null, null, 1.0D, 1.0D, 1.0D, 1.0D);
        } catch (Exception exception) {
            System.err.println("Error printing Navigator");
        }
    }

    public boolean selectMatchingTopic(URL url) {
        TopicTreeNode topictreenode = getCurrentSelectionNode();
        java.util.List list = rootTopicTreeNode.getContributingViews();
        TopicTreeNode topictreenode1 = TOCUtils.findClosestMatchingNode(topictreenode, url, list);
        return treePane.selectTopicTreeNode(topictreenode1);
    }

    public void setVisible(boolean flag) {
        super.setVisible(flag);
        treePane.getTopicTreeComponent().setVisible(flag);
    }


    private class TreePaneListener     implements TopicTreeListener {
        private TreePaneListener() {
        }
        private class DisplayInNewWindowAction extends AbstractAction {
           private TopicTreeItem item;
           public DisplayInNewWindowAction(TopicTreeItem topictreeitem) {
                item = topictreeitem;
            }
            public void actionPerformed(ActionEvent actionevent) {
                fireTopicActivated(item.getTopic(), 2);
            }

        }

        public void topicActivated(TopicTreeItem topictreeitem, boolean flag) {
            byte byte0 = ((byte)(flag ? 2 : 1));
            fireTopicActivated(topictreeitem.getTopic(), byte0);
        }

        public void topicSelected(TopicTreeItem topictreeitem) {
            Topic topic = topictreeitem != null ? topictreeitem.getTopic() : null;
            fireTopicSelected(topic);
        }

        public void popupDisplayed(TopicTreeItem topictreeitem, JPopupMenu jpopupmenu) {
            Topic topic = topictreeitem.getTopic();
            if (topic != null && topic.hasTarget()) {
                String s = MenuUtils.getDefaultMenuLabel("menu.Display_New");
                JMenuItem jmenuitem = new JMenuItem(s);
                jmenuitem.setAction(new DisplayInNewWindowAction(topictreeitem));
                MenuUtils.setMenuAttributes(jmenuitem, s, true);
                jmenuitem.setActionCommand(s);
                jpopupmenu.add(jmenuitem);
            }
        }

    }

}
