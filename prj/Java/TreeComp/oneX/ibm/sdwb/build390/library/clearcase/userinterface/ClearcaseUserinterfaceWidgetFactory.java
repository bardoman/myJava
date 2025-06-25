package com.ibm.sdwb.build390.library.clearcase.userinterface;

import com.ibm.sdwb.build390.library.userinterface.*;
import com.ibm.sdwb.build390.library.clearcase.*;
import com.ibm.sdwb.build390.MBMainframeInfo;



/**
 * Overridden by libraries that will implement user interface
 */
public class ClearcaseUserinterfaceWidgetFactory implements java.io.Serializable, UserinterfaceWidgetFactory {

    static final long serialVersionUID = 447703854576358013L;
    private ClearcaseLibraryInfo libInfo = null;

    public ClearcaseUserinterfaceWidgetFactory(ClearcaseLibraryInfo tempInfo){
        libInfo = tempInfo;
    }

    public SourceSelection getSourceSelectionPanel(MBMainframeInfo mainInfo){
        return new ClearcaseSourceSelection(libInfo, mainInfo);
    }

    public MultipleSourceSelection getUsermodSourceSelectionPanel(MBMainframeInfo mainInfo){
        return null;
    }

    public LibraryInfoEditor getLibraryInfoEditor(javax.swing.JInternalFrame parentFrame){
        return new ClearcaseLibraryInfoEditor(parentFrame, libInfo);
    }

    public SourceSelection getMetadataLibrarySourcePanel(com.ibm.sdwb.build390.MBMainframeInfo mainInfo) {
        /* for now send this. */
        return new ClearcaseSourceSelection(libInfo,mainInfo);
    }
}
