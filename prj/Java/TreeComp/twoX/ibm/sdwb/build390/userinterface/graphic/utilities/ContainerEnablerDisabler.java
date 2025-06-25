package com.ibm.sdwb.build390.userinterface.graphic.utilities;

import java.util.*;
import java.awt.*;
import javax.swing.*;
import com.ibm.sdwb.build390.*;
import com.ibm.sdwb.build390.logprocess.*;
import com.ibm.sdwb.build390.userinterface.graphic.MainInterface;

public class ContainerEnablerDisabler {

    private Set componentsToEnable = null;
    private Set menusToEnable = null;
    private Container activeContainer = null;
    private boolean enabled = true;
    private LogEventProcessor lep=null;
    private Set untouchableComponents = null;

    public ContainerEnablerDisabler(Container tempContainer) {
        activeContainer = tempContainer;
        componentsToEnable = new HashSet();
        menusToEnable = new HashSet();
        lep = new LogEventProcessor();
        lep.addEventListener(MBClient.getGlobalLogFileListener());
        if (MainInterface.getInterfaceSingleton()!=null) {
            lep.addEventListener(MBClient.getGlobalLogGUIListener());
        }
    }

    // query enabled status
    public boolean isEnabled() {
        return enabled;
    }

    public void setUntouchableComponents(Set tempTouch) {
        untouchableComponents = tempTouch;
    }

    public Set getUntouchableComponents() {
        return untouchableComponents;
    }

    /** The enable method .
    */
    public void enableContainer() {
        synchronized (this) {
            enabled = true;
            if (activeContainer instanceof JInternalFrame) {
                JInternalFrame temp = ((JInternalFrame) activeContainer);
                temp.setClosable(true);
            }
            for (Iterator menuIterator = menusToEnable.iterator(); menuIterator.hasNext();) {
                Component temp = (Component) menuIterator.next();
                temp.setEnabled(true);
            }
            for (Iterator componentIterator=componentsToEnable.iterator(); componentIterator.hasNext();) {
                Component temp = (Component) componentIterator.next();
                temp.setEnabled(true);
            }
        }
    }

    public void disableContainer() {
        disableContainer(false);
    }

    /** The disable method .
    */
    public void disableContainer(boolean allowClose) {
        synchronized(this) {
            enabled = false;
            componentsToEnable = new HashSet();
            if (activeContainer instanceof JInternalFrame) {
                JInternalFrame temp = ((JInternalFrame) activeContainer);
                for (int i = 0; i < temp.getJMenuBar().getMenuCount(); i++) {
                    JMenu tempMenu = temp.getJMenuBar().getMenu(i);
                    if (tempMenu.isEnabled()) {
                        tempMenu.setEnabled(false);
                        menusToEnable.add(tempMenu);
                    }
                }
                temp.setClosable(allowClose);
                disableContainer(temp.getContentPane());
            } else {
                disableContainer(activeContainer);
            }
        }
    }

    private void disableContainer(Container tempContainer) {
        Component[] components = tempContainer.getComponents();
        for (int i = 0; i < components.length; i++) {
            boolean skipComponent = false;
            if (untouchableComponents != null) {
                if (untouchableComponents.contains(components[i])) {
                    skipComponent = true;
                }
            }
            if (!skipComponent) {
                if (components[i] instanceof javax.swing.JButton) {
                    if (!MBBasicInternalFrame.CANCEL_STRING.equalsIgnoreCase(((JButton)components[i]).getText()) &
                        !MBBasicInternalFrame.HELP_STRING.equalsIgnoreCase(((JButton)components[i]).getText())) {
                        if (components[i].isEnabled()) {
                            componentsToEnable.add(components[i]);
                            components[i].setEnabled(false);
                        }

                    }
                } else if (components[i] instanceof javax.swing.JToggleButton) {
                    if (components[i].isEnabled()) {
                        componentsToEnable.add(components[i]);
                        components[i].setEnabled(false);
                    }
                } else if (components[i] instanceof javax.swing.JComboBox) {
                    if (components[i].isEnabled()) {
                        componentsToEnable.add(components[i]);
                        components[i].setEnabled(false);
                    }
                } else if (components[i] instanceof javax.swing.JTree) {
                    if (components[i].isEnabled()) {
                        componentsToEnable.add(components[i]);
                        components[i].setEnabled(false);
                    }
                } else if (components[i] instanceof javax.swing.JTable) {
                    if (components[i].isEnabled()) {
                        componentsToEnable.add(components[i]);
                        components[i].setEnabled(false);
                    }
                } else if (components[i] instanceof javax.swing.JList) {
                    if (components[i].isEnabled()) {
                        componentsToEnable.add(components[i]);
                        components[i].setEnabled(false);
                    }
                } else if (components[i] instanceof javax.swing.text.JTextComponent) {
                    if (components[i].isEnabled() & ((javax.swing.text.JTextComponent) components[i]).isEditable()) {
                        componentsToEnable.add(components[i]);
                        components[i].setEnabled(false);
                    }
                } else if (components[i] instanceof java.awt.Container) {
                    disableContainer((Container)components[i]);
                }
            }
        }
    }
}
