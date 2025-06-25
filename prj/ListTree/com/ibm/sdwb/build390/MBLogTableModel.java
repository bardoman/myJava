package com.ibm.sdwb.build390;
/*********************************************************************/
/* MBLogTableModel class for the Build/390 client                  */
/*  Creates and manages the Driver Build Page                        */
// 05/01/99 feature249          allow retrieval of dependency reports
//04/26/2000 PRT_RT_INTO_PDS added two methods to enable the facility to choose any column from the part Retrieve dialogbox
/*********************************************************************/
import java.io.*;
import java.util.*;

import javax.swing.*;
import javax.swing.table.*;

/** Create the Release/Driver mgt page */
public class MBLogTableModel extends AbstractTableModel {

    private boolean[][] editableArray;
    private Object[][] data = new Object[0][0];
    private Vector columnHeadings = new Vector();


    public void setEditableArray() {
        editableArray = new boolean[getRowCount()][getColumnCount()];
        for (int row = 0; row < getRowCount(); row++) {
            editableArray[row][0] = false;
            editableArray[row][1] = false;
            editableArray[row][2] = true;
            editableArray[row][3] = true;
            for (int col = 4; col < getColumnCount(); col++) {
                editableArray[row][col]=((Boolean)getValueAt(row, col)).booleanValue();
            }
        }
    }

    public String getColumnName(int columnIndex){
        return (String)columnHeadings.get(columnIndex);

    }

    public int getColumnCount(){
        return columnHeadings.size();
    }

    public int getRowCount(){
        return data.length;
    }

    public void  updateData(Vector dataVector,Vector columnHeadings){
        this.columnHeadings = columnHeadings;
        data = new Object[dataVector.size()][getColumnCount()];
        int i=-1;
        for (Iterator iter=dataVector.iterator();iter.hasNext();) {
            Vector singleRowData = (Vector)iter.next();
            int j=-1;
            i++;
            for (Iterator rowIter=singleRowData.iterator();rowIter.hasNext();) {
                j++;
                data[i][j]=rowIter.next();
            }
        }
        fireTableDataChanged();
    }

    public Object getValueAt(int row, int col) {
        return data[row][col];
    }


    /*
     * Don't need to implement this method unless your table's
     * data can change.
     */
    public void setValueAt(Object value, int row, int col) {
        data[row][col] = value;
        fireTableCellUpdated(row, col);
    }


    public Class getColumnClass(int columnIndex){
        if (columnIndex < 2) {
            return String.class;
        } else {
            return Boolean.class;
        }
    }

    public boolean isCellEditable(int rowIndex, int columnIndex) {
        return editableArray[rowIndex][columnIndex];
    }

    public void setAllTrue() {
        for (int col = 0; col < getColumnCount(); col++) {
            for (int row = 0; row < getRowCount(); row++) {
                if (editableArray[row][col])
                    setValueAt(new Boolean (true), row, col);
            }
        }
    }

    public void setAllFalse() {
        for (int col = 0; col < getColumnCount(); col++) {
            for (int row = 0; row < getRowCount(); row++) {
                if (editableArray[row][col])
                    setValueAt(new Boolean (false), row, col);
            }
        }
    } 

//04/26/2000 PRT_RT_INTO_PDS added two methods to enable the facility to select and deselect
//	 any column from the part Retrieve dialogbox.
    public    void   setanyColumntrue(String columnName){
        int anyColumnnumber = findColumn(columnName);
        for ( int row = 0; row < getRowCount(); row++) {
            if (editableArray[row][anyColumnnumber])
                setValueAt(new Boolean(true), row, anyColumnnumber);


        }
    }   
    public  void   setanyColumnFalse(String columnName){
        int anyColumnnumber = findColumn(columnName);
        for ( int row = 0; row < getRowCount(); row++) {
            if (editableArray[row][anyColumnnumber])
                setValueAt(new Boolean(false), row, anyColumnnumber);


        }
    }


}

