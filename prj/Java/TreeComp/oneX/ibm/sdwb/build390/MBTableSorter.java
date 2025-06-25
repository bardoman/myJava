package com.ibm.sdwb.build390;     
/*************************************************************************/
/* Java Table Sorter  class for the Build390 java client (MBCleanupPage) */
/********************************************************************************************************* 
/* A sorter for TableModels. The sorter has a model (conforming to TableModel) and itself implements TableModel. TableSorter does not store or copy 
/* the data in the TableModel, instead it maintains an array of integers which it keeps the same size as the number of rows in its 
/* model. When the model changes it notifies the sorter that something has changed eg. "rowsAdded" so that its internal array of integers 
/* can be reallocated. As requests are made of the sorter (like getValueAt(row, col) it redirects them to its model via the mapping 
/* array. That way the MBTableSorter appears to hold another copy of the table with the rows in a different order. The sorting algorthm used is stable 
/* which means that it does not move around rows when its comparison function returns 0 to denote that they are equivalent. 
/* The sorting algorithm is the normal comparison , where one element is compared with all the 
/* Logic taken out from java tutorials - tables.
/***********************************************************************************************/
/* Updates:  01/20/2000 birth date   
//Defect 62 If in the table in cleanuppage - we select a empty column , this occurs(ie suppose the table has 6 columns and we place the mouse beyond 6 on empty space)
/***********************************************************************************************/
import java.util.*;
import javax.swing.table.TableModel;
import javax.swing.event.TableModelEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.InputEvent;
import java.awt.event.MouseMotionAdapter;
import javax.swing.JTable;
import javax.swing.table.*;

public class MBTableSorter extends DefaultTableModel {
	int             indexes[];
	Vector          sortingColumns = new Vector();
	boolean         ascending = true;
	int compares;

	public MBTableSorter() {
		indexes = new int[0]; // initialization - hold the row[]array for the table
	}

	//* row 1 the indexes of  row1 to be compared with row2 
	//* row 2 the indexes of  row2 to be compared with row1
	//* column the int value of the column selected to be sorted with  
	public int compareRowsByColumn(int row1, int row2, int column) {
		// gets the class of the column  - String,int,boolean,Date etc.
		Class type = getColumnClass(column);

		// Check for nulls.

		Object o1 = super.getValueAt(row1, column);
		Object o2 = super.getValueAt(row2, column); 

		// If both values are null, return 0.
		if(o1 == null && o2 == null) {
			return 0; 
		} else if(o1 == null) {	// Define null less than everything. 
			return -1; 
		} else if(o2 == null) {
			return 1; 
		}

		/*
		 * We copy all returned values from the getValue call in case
		 * an optimised model is reusing one object to return many
		 * values.  The Number subclasses in the JDK are immutable and
		 * so will not be used in this way but other subclasses of
		 * Number might want to do this to save space and avoid
		 * unnecessary heap allocation.
		 */

		if(java.lang.Number.class.isAssignableFrom(type)) {
// Ken 5/30/2001 make this a little simpler
//			if (type.getSuperclass() == java.lang.Number.class) {
			Number n1 = (Number)super.getValueAt(row1, column);
			double d1 = n1.doubleValue();
			Number n2 = (Number)super.getValueAt(row2, column);
			double d2 = n2.doubleValue();

			if(d1 < d2) {
				return -1;
			} else if(d1 > d2) {
				return 1;
			} else {
				return 0;
			}
// Ken 5/30/2001  ditto
		} else if(java.util.Date.class.isAssignableFrom(type)) {
			Date d1 = (Date)super.getValueAt(row1, column);
			long n1 = d1.getTime();
			Date d2 = (Date)super.getValueAt(row2, column);
			long n2 = d2.getTime();

			if(n1 < n2) {
				return -1;
			} else if(n1 > n2) {
				return 1;
			} else {
				return 0;
			}
		} else if(String.class.isAssignableFrom(type)) {
			String s1 = (String)super.getValueAt(row1, column);
			String s2    = (String)super.getValueAt(row2, column);
			int result = s1.compareTo(s2);

			if(result < 0) {
				return -1;
			} else if(result > 0) {
				return 1;
			} else {
				return 0;
			}
		} else if(Boolean.class.isAssignableFrom(type)) {
			Boolean bool1 = (Boolean)super.getValueAt(row1, column);
			boolean b1 = bool1.booleanValue();
			Boolean bool2 = (Boolean)super.getValueAt(row2, column);
			boolean b2 = bool2.booleanValue();

			if(b1 == b2) {
				return 0;
			} else if(b1) {	// Define false < true
				return 1;
			} else {
				return -1;
			}
		} else {
			Object v1 = super.getValueAt(row1, column);
			String s1 = v1.toString();
			Object v2 = super.getValueAt(row2, column);
			String s2 = v2.toString();
			int result = s1.compareTo(s2);

			if(result < 0) {
				return -1;
			} else if(result > 0) {
				return 1;
			} else {
				return 0;
			}
		}
	}

	public int compare(int row1, int row2) {
		compares++;
		for(int level = 0; level < sortingColumns.size(); level++) {
			Integer column = (Integer)sortingColumns.elementAt(level);
			int result = compareRowsByColumn(row1, row2, column.intValue());
			//the result returned is checked to sort out for ascending or descending
			if(result != 0) {
				return ascending ? -result : result;
			}
		}
		return 0;
	}

	public void reallocateIndexes() {
		int rowCount = getRowCount();

		// Set up a new array of indexes with the right number of elements
		// for the new data model.
		indexes = new int[rowCount];

		// Initialise with the identity mapping.
		// initially the rows are ordered 1,2,3,etc...after sorted it might get jumbled out.
		for(int row = 0; row < rowCount; row++) {
			indexes[row] = row;
		}
	}

	public void sort(Object sender) {
		reallocateIndexes();
		compares = 0;
		//the simplest sort is used here.
		n2sort();
	}

	public void n2sort() {
		for(int i = 0; i < getRowCount(); i++) {
			for(int j = i+1; j < getRowCount(); j++) {
				//the result returned is checked out which row should be swapped
				if(compare(indexes[i], indexes[j]) == -1) {
					swap(i, j);
				}
			}
		}
	}


	// row1 and row2 indexes are swapped(values are not swapped).This way we can use the model.getValueAT(...) method to pull
	// in the values of the rows
	public void swap(int i, int j) {
		int tmp = indexes[i];
		indexes[i] = indexes[j];
		indexes[j] = tmp;
	}

	public boolean isCellEditable(int row,int col) {
		return false;
	}

	// The mapping only affects the contents of the data rows.
	// Pass all requests to these rows through the mapping array: "indexes".

	public Object getValueAt(int aRow, int aColumn) {
		return super.getValueAt(indexes[aRow], aColumn);
	}

	public void setValueAt(Object aValue, int aRow, int aColumn) {
		super.setValueAt(aValue, indexes[aRow], aColumn);
		sort(this);
	}

	public void addRow(Object[] rowData) {
		super.addRow(rowData);
		sort(this);
	}

	public void addRow(Vector rowData) {
		super.addRow(rowData);
		sort(this);
	}

	public void insertRow(int row, Object[] rowData) {
		super.insertRow(row, rowData);
		sort(this);
	}

	public void insertRow(int row, Vector rowData) {
		super.insertRow(row, rowData);
		sort(this);
	}

	public void removeRow(int row) {
		super.removeRow(row);
		sort(this);
	}

	public void setDataVector(Object[][] newData, Object[] columnNames) {
		super.setDataVector(newData, columnNames);
		sort(this);
	}

	public void setDataVector(Vector newData, Vector columnNames) {
		super.setDataVector(newData, columnNames);
		sort(this);
	}

	public void setRowCount(int rowCount) {
		super.setRowCount(rowCount);
		sort(this);
	}

	public void sortByColumn(int column) {
		sortByColumn(column, true);
	}

	public void sortByColumn(int column, boolean ascending) {
		this.ascending = ascending;
		sortingColumns.removeAllElements();
		sortingColumns.addElement(new Integer(column));
		sort(this);
	}

	// Add a mouse listener to the Table to trigger a table sort 
	// when a column heading is clicked in the JTable. 
	// And it displays a tooltip text when mouse moves over the respective heading
	public void addMouseListenerToHeaderInTable(JTable table) {
		final MBTableSorter sorter = this; 
		final JTable tableView = table;
		tableView.setColumnSelectionAllowed(false); 
		MouseAdapter listMouseListener = new MouseAdapter() {
			public void mouseClicked(MouseEvent e) {
				TableColumnModel columnModel = tableView.getColumnModel();
				int viewColumn = columnModel.getColumnIndexAtX(e.getX()); 
				int column = tableView.convertColumnIndexToModel(viewColumn); 
				// if single click sort by ascending order and a column selection is there
				if(e.getClickCount() == 1 && column != -1) {
					ascending = true; 
				} else {
					// if more than one  click sort by descending order and a column selection is there
					if(e.getClickCount() >1 && column != -1) {
						ascending = false; 
					}
				}

				sorter.sortByColumn(column, ascending); 
			}
		};
		final JTableHeader th = tableView.getTableHeader(); 
		th.addMouseMotionListener(new MouseMotionAdapter() {
									  public void mouseMoved(MouseEvent e) {
										  TableColumnModel tempcolumnModel = tableView.getColumnModel();
										  int tempviewColumn = tempcolumnModel.getColumnIndexAtX(e.getX()); 
										  int tempcolumn = tableView.convertColumnIndexToModel(tempviewColumn); 
										  //Defect 62 If in the table in cleanuppage - we select a empty column , this occurs(ie suppose the table
										  //has 6 columns and we place the mouse beyond 6 on empty space)
										  if(tempcolumn>-1) {
											  th.setToolTipText("Click to Sort by -  " + getColumnName(tempcolumn).toUpperCase());
										  }
									  }
								  });
		th.addMouseListener(listMouseListener); 


	}

}

