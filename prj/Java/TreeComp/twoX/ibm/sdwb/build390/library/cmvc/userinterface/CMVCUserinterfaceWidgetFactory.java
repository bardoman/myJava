package com.ibm.sdwb.build390.library.cmvc.userinterface;

import com.ibm.sdwb.build390.MBMainframeInfo;
import com.ibm.sdwb.build390.library.cmvc.*;
import com.ibm.sdwb.build390.library.cmvc.metadata.userinterface.CMVCTrackSourceSelection;
import com.ibm.sdwb.build390.library.userinterface.*;



/**
 * Overridden by libraries that will implement user interface
 */
public class CMVCUserinterfaceWidgetFactory implements java.io.Serializable, UserinterfaceWidgetFactory{

    static final long serialVersionUID = 447703854576358013L;
	private CMVCLibraryInfo libInfo = null;

	public CMVCUserinterfaceWidgetFactory(CMVCLibraryInfo tempInfo){
		libInfo = tempInfo;
	}

	public SourceSelection getSourceSelectionPanel(MBMainframeInfo mainInfo){
		return new CMVCSourceSelection(libInfo, mainInfo);
	}

	public MultipleSourceSelection getUsermodSourceSelectionPanel(MBMainframeInfo mainInfo){
		return new CMVCUsermodSourceSelection(libInfo, mainInfo);
	}

	public LibraryInfoEditor getLibraryInfoEditor(javax.swing.JInternalFrame parentFrame){
 		return new CMVCLibraryInfoEditor(parentFrame, libInfo);
	}


	public SourceSelection getMetadataLibrarySourcePanel(com.ibm.sdwb.build390.MBMainframeInfo mainInfo) {
            return new com.ibm.sdwb.build390.library.cmvc.metadata.userinterface.CMVCTrackSourceSelection(libInfo,mainInfo);
        }
}
