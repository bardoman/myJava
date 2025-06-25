package com.ibm.sdwb.build390.userinterface.graphic.panels.metadata;
import java.util.*;
import javax.swing.table.*;

import com.ibm.sdwb.build390.info.*;
import com.ibm.sdwb.build390.*;

class MetadataEditorTableModel extends AbstractTableModel implements MetadataTableModelWrapperInterface {

    private Collection data = new Vector();
    private Vector columnHeadings = new Vector();

    private static final Vector mvsColumnHeadings  = new Vector();

    static {
        mvsColumnHeadings.addElement("Part Name");
        mvsColumnHeadings.addElement("Part Class");
        mvsColumnHeadings.addElement("Library PathName");
        mvsColumnHeadings.addElement("Has  Host MDE Updates");
    }

    MetadataEditorTableModel() {
        super();
        this.columnHeadings = mvsColumnHeadings;
    }

    MetadataEditorTableModel(Vector columnHeadings) {
        super();
        this.columnHeadings = columnHeadings;
    }


    public int getRowCount()    {
        return(data.size());
    }


    public int getColumnCount()    {
        return(columnHeadings.size());
    }


    public String getColumnName(int column)    {
        return((String)columnHeadings.elementAt(column));
    }


    public Class getColumnClass(int column)    {
        return(columnHeadings.elementAt(column).getClass());
    }

    public boolean isCellEditable(int row,int col)    {
        return false;
    }


    public Object getValueAt(int row, int column)    {


        Object[] objInfo  = ((java.util.Collection)data).toArray();

        FileInfo partInfo  = (FileInfo)objInfo[row];

        String[] mainframeNames= partInfo.getMainframeFilename().split("\\.");

        switch (column) {
        case 0:
            String partClass = mainframeNames.length >= 2 ? mainframeNames[1] : "";

            return partClass;
        case 1:
            String partName = mainframeNames.length >= 2 ? mainframeNames[0] : "";

            return partName;
        case 2:
            return partInfo.getDirectory()!=null ? partInfo.getDirectory() + partInfo.getName() : partInfo.getName();
        case 3:
            String METADATA_FLAG ="M";

            if (partInfo.getTypeOfChange().indexOf("M") >= 0) {
                return "Yes";
            } else {
                return "No";
            }

        default:
            break;
        }
        return null;
    }

    public Object getValueAt(int row)    {
        Object[] objInfo  = ((java.util.Collection)data).toArray();

        return  (FileInfo)objInfo[row];
    }

    public AbstractTableModel getModel()    {
        return this;
    }

    public void updateDisplay(Collection data)    {
        this.data = data;
        fireTableDataChanged();
    }

}
