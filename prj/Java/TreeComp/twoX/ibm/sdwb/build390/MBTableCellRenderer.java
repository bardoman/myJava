package com.ibm.sdwb.build390;
/*********************************************************************/
/* MBTableCellRenderer class for the Build/390 client                  */
/*********************************************************************/
// changes
//Date    Defect/Feature        Reason
//05/03/2000  Feature=EKM001   part retrieve Enhancements - adding 2 to the column since the we changed to Two tables( each table starts with colum 00, and we want the first column in second table which is 2)
/*******************************************************************************/
import java.awt.Component;
import javax.swing.*;
import javax.swing.table.*;
import javax.swing.event.*;
import java.util.*;

public class MBTableCellRenderer extends DefaultTableCellRenderer {
    private static Hashtable usedComps = new Hashtable();

    public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
        Component tempComp = table.getDefaultRenderer(table.getColumnClass(column)).getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
        if ((!table.getModel().isCellEditable(row, column+2) & (table.getColumnClass(column).getName().indexOf("Boolean") > -1))) {//& (column > 2)) {
            if (!((Boolean) table.getValueAt(row,column)).booleanValue()) {
                tempComp = (Component) usedComps.get(Integer.toString(row)+","+Integer.toString(column));
                if (tempComp == null) {
                    tempComp = new JPanel();
                    usedComps.put(Integer.toString(row)+","+Integer.toString(column), tempComp);
                }
            }
            return tempComp;
        }
        return tempComp;
    }
}
