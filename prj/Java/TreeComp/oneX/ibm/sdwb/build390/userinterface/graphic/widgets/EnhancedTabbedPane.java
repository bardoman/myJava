package com.ibm.sdwb.build390.userinterface.graphic.widgets;

import javax.swing.*;
import java.awt.Color;
import java.awt.Component;
import java.util.*;
import com.ibm.sdwb.build390.userinterface.event.*;

public class EnhancedTabbedPane extends JTabbedPane implements UserInterfaceEventListener {

    private boolean[] visitedTabs = new boolean[0];
    private static Color NOTVISITEDNOTSATISFIED = Color.RED.brighter();
    private static Color VISITEDNOTSATISFIED = Color.RED.darker().darker();
    private static Color NOTVISITEDSATISFIED = Color.GREEN.brighter();
    private static Color VISITEDSATISFIED = Color.GREEN.darker().darker();
    private TabChangeListener myTabChangeListener = null;
    private static boolean requireAllTabsToBeVisited = false;
    private UserInterfaceListenerManager changeManager = new UserInterfaceListenerManager();

    public static String ENHANCEDTABBEDPANEUPDATE = "ENHANCEDTABBEDPANEUPDATE";


    public EnhancedTabbedPane() {
        super();
        myTabChangeListener = new TabChangeListener();
        addChangeListener(myTabChangeListener);
    }

    public EnhancedTabbedPane(int tabPlacement) {
        super(tabPlacement);
        myTabChangeListener = new TabChangeListener();
        addChangeListener(myTabChangeListener);
    }

    public EnhancedTabbedPane(int tabPlacement, int tabLayoutPolicy) {
        super(tabPlacement,tabLayoutPolicy);
        myTabChangeListener = new TabChangeListener();
        addChangeListener(myTabChangeListener);
    }

    public static void setRequireAllTabsToBeVisited(boolean temp){
        requireAllTabsToBeVisited=temp;
    }

    public void add(Component newComponent, Object constraints, int index) {
        super.add(newComponent,constraints, index);
        primeNewComponent(newComponent);
    }

    public void add(Component newComponent, Object constraints) {
        super.add(newComponent,constraints);
        primeNewComponent(newComponent);
    }

    public Component add(Component newComponent, int index) {
        super.add(newComponent,index);
        primeNewComponent(newComponent);
        return newComponent;
    }

    public Component add(Component tempComp) {
        super.add(tempComp);
        primeNewComponent(tempComp);
        return tempComp;
    }

    public javax.swing.event.ChangeListener getTabChangeListener() {
        return myTabChangeListener;
    }

    public void clearVisitedTabs() {
        visitedTabs = new boolean[getTabCount()];
        if (visitedTabs.length > getSelectedIndex()) {
            visitedTabs[getSelectedIndex()] = true;
        }
        refreshTabColors();
    }

    private void primeNewComponent(Component tempComp) {
        if (tempComp instanceof RequiredActionsCompletedInterface) {
            RequiredActionsCompletedInterface newComp = (RequiredActionsCompletedInterface) tempComp;
            boolean tabSelected = getSelectedIndex() == indexOfComponent((Component)newComp);
            visitedTabs = new boolean[getTabCount()]; 
            if (visitedTabs.length > getSelectedIndex()) {
                visitedTabs[getSelectedIndex()] = true;
            }
            handleRequiredActionChange(newComp);
        }
    }

    private void handleRequiredActionChange(RequiredActionsCompletedInterface testComp) {
        int tabIndex = indexOfComponent((Component) testComp);
        if (visitedTabs[tabIndex]) {
            if (testComp.isRequiredActionCompleted()) {
                setForegroundAt(tabIndex, VISITEDSATISFIED);
            } else {
                setForegroundAt(tabIndex, VISITEDNOTSATISFIED);
            }
        } else {
            if (testComp.isRequiredActionCompleted()) {
                Color tabColor = NOTVISITEDSATISFIED;
                if (requireAllTabsToBeVisited) {
                    if (!visitedTabs[tabIndex]) {
                        tabColor = NOTVISITEDNOTSATISFIED;
                    }
                }
                setForegroundAt(tabIndex, tabColor);
            } else {
                setForegroundAt(tabIndex, NOTVISITEDNOTSATISFIED);
            }
        }
        UserInterfaceEvent newEvent = new UserInterfaceEvent(this, ENHANCEDTABBEDPANEUPDATE);
        changeManager.fireEvent(newEvent);
    }

    public boolean isAllRequiredActionsSatisfied(){
        boolean isSatisfied = true;
        for (int index = 0; index < getTabCount(); index++) {
            if (requireAllTabsToBeVisited) {
                isSatisfied = isSatisfied && visitedTabs[index];
            }
            Component eachComp = getComponentAt(index);
            if (eachComp instanceof RequiredActionsCompletedInterface) {
                isSatisfied = isSatisfied && ((RequiredActionsCompletedInterface)eachComp).isRequiredActionCompleted();
            }
        }
        return isSatisfied;
    }

    public void addUserInterfaceEventListener(UserInterfaceEventListener listener) {
        changeManager.addUserInterfaceEventListener(listener);
    }

    public void handleUIEvent(UserInterfaceEvent e) {
        if (e.getSource() instanceof RequiredActionsCompletedInterface) {
            handleRequiredActionChange((RequiredActionsCompletedInterface) e.getSource());
        }
    }

    private void refreshTabColors(){
        for (int tabIndex = 0; tabIndex < visitedTabs.length; tabIndex++) {
            if (getComponentAt(tabIndex) instanceof RequiredActionsCompletedInterface) {
                handleRequiredActionChange( (RequiredActionsCompletedInterface) getComponentAt(tabIndex));
            }
        }
    }

    protected class TabChangeListener implements javax.swing.event.ChangeListener {
        public void stateChanged(javax.swing.event.ChangeEvent e) {
            if (getSelectedIndex() < visitedTabs.length & getSelectedIndex() > 0) {
                visitedTabs[getSelectedIndex()]=true;
            }
            refreshTabColors();
        }
    }

}
