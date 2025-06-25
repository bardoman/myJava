package com.ibm.sdwb.build390;
/*********************************************************************/
/* GeneralTableCellEditor class for the Build/390 client                  */
/*********************************************************************/
import java.awt.Component;
import javax.swing.*;
import javax.swing.event.*;
import java.util.*;

public class GeneralCellEditor extends DefaultCellEditor{

    private JTable currentTable = null;
    private int currentRow = -1;
    private int currentCol = -1;

    public GeneralCellEditor(JCheckBox x) {
        super(x);
    }

    public GeneralCellEditor(JComboBox x) {
        super(x);
    }

    public GeneralCellEditor(final JTextField x) {
        super(x);
        x.addCaretListener(new CaretListener() {
            public void caretUpdate(CaretEvent e) {
                if (currentTable != null) {
                    currentTable.setValueAt(x.getText(), currentRow, currentCol);
                }
            }
        });
    }

    public Component getTableCellEditorComponent(JTable Table, Object value, boolean isSelected, int row, int col) {
        Component tempEdit =  super.getTableCellEditorComponent(Table, value, isSelected, row, col);
        currentTable = Table;
        currentRow = row;
        currentCol = col;
/* 09/15/99 update for jdk1.2.2
        if (tempEdit instanceof JCheckBox) {
            ((JCheckBox) tempEdit).doClick();
            Table.setValueAt(new Boolean(((JCheckBox) tempEdit).isSelected()), row, col);
        }
*/        
        return tempEdit;
    }

    public void cancelCellEditing() {
        currentTable = null;
        currentRow = -1;
        currentCol = -1;
        super.cancelCellEditing();
    }

    public boolean stopCellEditing() {
        currentTable = null;
        currentRow = -1;
        currentCol = -1;
        return super.stopCellEditing();
    }
}
