package com.ibm.sdwb.build390;
/*********************************************************************/
/* MBLogTableModel class for the Build/390 client                  */
/*  Creates and manages the Driver Build Page                        */
/*********************************************************************/
import java.io.*;
import java.util.*;

import javax.swing.*;
import javax.swing.table.*;

/** Hold the table for the SmodLogRetrieve page */
public class MBSmodTableModel extends DefaultTableModel{

    private boolean[][] editableArray;

    public void setEditableArray() {
        editableArray = new boolean[getRowCount()][getColumnCount()];
        for (int row = 0; row < getRowCount(); row++) {
            editableArray[row][0]=false;
            editableArray[row][1]=false;
            editableArray[row][2]=true;
            setValueAt(new Boolean(false), row, 2);
            for (int col = 3; col < getColumnCount(); col++) {
                editableArray[row][col]=((Boolean)getValueAt(row, col)).booleanValue();
                setValueAt(new Boolean (false), row, col);
            }
        }
    }


    public Class getColumnClass(int columnIndex){
        if (columnIndex < 2) {
            return String.class;
        }else {
            return Boolean.class;
        }
    }

    public boolean isCellEditable(int rowIndex, int columnIndex) {
        return editableArray[rowIndex][columnIndex];
    }

    public void setAllTrue() {
        for (int col = 0; col < getColumnCount(); col++) {
            for (int row = 0; row < getRowCount(); row++) {
                if (editableArray[row][col]){
                    setValueAt(new Boolean (true), row, col);
                }
            }
        }
    }

    public void setAllFalse() {
        for (int col = 0; col < getColumnCount(); col++) {
            for (int row = 0; row < getRowCount(); row++) {
                if (editableArray[row][col]){
                    setValueAt(new Boolean (false), row, col);
                }
            }
        }
    }


}

