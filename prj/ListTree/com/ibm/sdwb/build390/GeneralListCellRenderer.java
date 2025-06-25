package com.ibm.sdwb.build390;
/*********************************************************************/
/* GeneralListCellRenderer class for the Build/390 client                  */
/*********************************************************************/
import java.awt.Component;
import javax.swing.*;
import javax.swing.event.*;
import java.util.Vector;

public class GeneralListCellRenderer extends DefaultListCellRenderer{
    private Component lastFocus = null;

    public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected, boolean hasFocus) {
        if (value instanceof JCheckBox) {
            JCheckBox tempBox = (JCheckBox) value;
            if (isSelected & hasFocus) {
                if (tempBox != lastFocus) {
                    tempBox.setSelected(!tempBox.isSelected());
                    lastFocus = tempBox;
                }
            }
            return tempBox;
        }else if (value instanceof Component) {
            return (Component) value;
        }else {
            return super.getListCellRendererComponent(list, value, index, isSelected, hasFocus);
        }
    }
}
