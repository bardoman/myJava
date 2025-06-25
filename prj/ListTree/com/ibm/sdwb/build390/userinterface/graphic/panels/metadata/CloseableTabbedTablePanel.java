package com.ibm.sdwb.build390.userinterface.graphic.panels.metadata;
/*********************************************************************/
/* OneTabWithTable  class for the Build/390 client                   */
/* Creates a single tab with a table.                                */
/* A callback handler is registered by  clients to get updates on    */
/* selectedValues or selections.                                     */
/*********************************************************************/
//02/11/2005 SDWB2363 Redesign Part chooser interface
/*********************************************************************/
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.util.*;
import javax.swing.border.*;

import javax.swing.*;
import javax.swing.table.*;
import com.ibm.sdwb.build390.*;
import com.ibm.sdwb.build390.logprocess.*;
import com.ibm.sdwb.build390.userinterface.graphic.widgets.*;

public class  CloseableTabbedTablePanel extends JPanel implements CloseableTabHandler,
javax.swing.event.ListSelectionListener {


    private MetadataTableModelWrapperInterface modelWrapper   = null;
    private SortableTableModel sorter            = null;
    private JTable metadataInfoTable             = null;
    private Collection selectedValues            = null;
    private Observer observer =null;

    private boolean isCloseTab = false;  /*default dont close tab */



    public CloseableTabbedTablePanel(MetadataTableModelWrapperInterface modelWrapper) {
        this.modelWrapper = modelWrapper;
        createSortableModel(modelWrapper.getModel());
        createTable();
        JScrollPane tableScroller = new JScrollPane(metadataInfoTable);
        Dimension dm = tableScroller.getPreferredSize();
        dm.height = dm.height/3;
        tableScroller.setPreferredSize(dm);
        tableScroller.setAlignmentY(TOP_ALIGNMENT);
        setLayout(new BoxLayout(this, BoxLayout.PAGE_AXIS));
        add(Box.createRigidArea(new Dimension(0,5)));
        add(tableScroller);
    }


    protected void createSortableModel(TableModel tableModel){
        this.sorter =  new SortableTableModel(tableModel);

    }

    protected void createTable(){
        this.metadataInfoTable = new JTable(getSorter());
        sorter.setTableHeader(metadataInfoTable.getTableHeader()); 
        metadataInfoTable.getTableHeader().setToolTipText("Click to specify sorting; Control-Click to specify secondary sorting");
        metadataInfoTable.setBackground(MBGuiConstants.ColorFieldBackground);
        metadataInfoTable.setColumnSelectionAllowed(false);
        metadataInfoTable.setCellSelectionEnabled(false);
        metadataInfoTable.setRowSelectionAllowed(true);
        metadataInfoTable.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
        metadataInfoTable.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);
        metadataInfoTable.getSelectionModel().addListSelectionListener(this);
        metadataInfoTable.putClientProperty("terminateEditOnFocusLost", Boolean.TRUE);
    }

    protected void setObserver(Observer observer){
        this.observer = observer;
    }

    public JTable getTable(){
        return metadataInfoTable;
    }

    public SortableTableModel getSorter(){
        return sorter;
    }                 



    public void update(Collection data){
        modelWrapper.updateDisplay(data);
        getSorter().fireTableDataChanged();

        if (!getSorter().isSorting()) {
            getSorter().setSortingStatus(0,SortableTableModel.ASCENDING);
        }
        repaint();

    }

    public Collection getSelectedValues(){
        return selectedValues;
    }


    public void valueChanged(javax.swing.event.ListSelectionEvent e){
        //(selectedValues == null ? (new ArrayList()) : selectedValues);
        selectedValues = new ArrayList();

        int[] selectedRows = getTable().getSelectedRows();

        if (getTable().getCellEditor()!=null) {
            getTable().getCellEditor().stopCellEditing();
        }

        if (!e.getValueIsAdjusting()) {
            for (int i=0;i<selectedRows.length;i++) {
                /*since the table model resides inside the sorter, we need to use the modelToIndex method to 
                figure out the exact row
                */
                int row = getSorter().modelIndex(selectedRows[i]);

                selectedValues.add(modelWrapper.getValueAt(row));

            }
            if (observer!=null) {
                observer.update(null,null);
            }
        }
    }

    public void setCloseable(boolean isCloseTab){
        this.isCloseTab = isCloseTab;

    }


    public boolean isCloseable(){
        return isCloseTab;

    }


}


