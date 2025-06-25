package com.ibm.sdwb.build390.library.userinterface;

import com.ibm.sdwb.build390.library.LibraryInfo;


/**
 * Overridden by libraries that will implement user interface
 */
public interface UserinterfaceWidgetFactory {


	/**
	 * Get the appropriate source selection panel
	 * 
	 * @return 
	 */
	public SourceSelection getSourceSelectionPanel(com.ibm.sdwb.build390.MBMainframeInfo mainInfo);



	/**
	 * Get the appropriate source selection panel
	 * 
	 * @return 
	 */
	public MultipleSourceSelection getUsermodSourceSelectionPanel(com.ibm.sdwb.build390.MBMainframeInfo mainInfo);



	/**
	 * Get the editor appropriate to the library info type
	 * you are dealing with
	 * 
	 * @return 
	 */
	public LibraryInfoEditor getLibraryInfoEditor(javax.swing.JInternalFrame parentFrame);


        /**
	 * Get the  appropriate library metadata source panel to the library info type
	 * you are dealing with
	 * 
	 * @return 
	 */
	public SourceSelection getMetadataLibrarySourcePanel(com.ibm.sdwb.build390.MBMainframeInfo mainInfo);
}
