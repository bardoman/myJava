package com.ibm.sdwb.build390.userinterface.graphic.panels.metadata;
import java.util.*;
import javax.swing.table.*;
import com.ibm.sdwb.build390.*;
import com.ibm.sdwb.build390.info.*;

class BatchMetadataTableModel extends AbstractTableModel implements MetadataTableModelWrapperInterface {

    private Collection data = new Vector();
    private Vector columnHeadings = new Vector();

    private static final Vector mvsColumnHeadings  = new Vector();


    static {
        mvsColumnHeadings.addElement("Part Name");
        mvsColumnHeadings.addElement("Part Class");
        mvsColumnHeadings.addElement("Library PathName");
        mvsColumnHeadings.addElement("DISTNAME");
    }

    BatchMetadataTableModel() {
        super();
        this.columnHeadings = mvsColumnHeadings;
    }

    BatchMetadataTableModel(Vector columnHeadings) {
        super();
        this.columnHeadings = columnHeadings;
    }


    public int getRowCount() {
        return(data.size());
    }


    public int getColumnCount() {
        return(columnHeadings.size());
    }


    public String getColumnName(int column) {
        return((String)columnHeadings.elementAt(column));
    }




    public boolean isCellEditable(int row, int column) {
        if (column==3) {
            return true;
        }

        return false;
    }

    public Object getValueAt(int row, int column) {

        Object[] objInfo  = ((java.util.Collection)data).toArray();

        FileInfo partInfo  = (FileInfo)objInfo[row];

        String[] mainframeNames = partInfo.getMainframeFilename().split("\\.");


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
            String distName = (String)partInfo.getMetadata().get(BatchMetadataEditorFrame.DISTNAME + 1);
            return distName  == null ? ""  : distName;
        default:
            break;
        }
        return null;

    }


    public void  setValueAt(Object value,int row, int column) {
         Object[] objInfo  = ((java.util.Collection)data).toArray();

        FileInfo partInfo = (FileInfo)objInfo[row];
        switch (column) {
        case 3:
            partInfo.getMetadata().put(BatchMetadataEditorFrame.DISTNAME + 1,(String)value);
            break;
        default:
            break;
        }
        return;
    }


    public Class getColumnClass(int columnIndex) {
        switch (columnIndex) {
        case 0:
            return String.class;
        case 1:
            return String.class;
        case 2:
            return String.class;
        case 3:
            return String.class;
        default:
            break;

        }
        return String.class;
    }

    public Object getValueAt(int row) {
        Object[] objInfo  = ((java.util.Collection)data).toArray();

        return(FileInfo)objInfo[row];
    }

    public AbstractTableModel getModel() {
        return this;
    }

    public void updateDisplay(Collection data) {
        this.data = data;
        fireTableDataChanged();
    }

}
