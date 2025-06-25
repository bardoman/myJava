package com.ibm.sdwb.build390.library.fakelib.userinterface;

import com.ibm.sdwb.build390.MBMainframeInfo;
import com.ibm.sdwb.build390.library.fakelib.FakeLibraryInfo;
import com.ibm.sdwb.build390.library.userinterface.*;

/**
 * Overridden by libraries that will implement user interface
 */
public class FakeLibraryUserinterfaceWidgetFactory implements java.io.Serializable, UserinterfaceWidgetFactory {

    static final long serialVersionUID = 1111111111111111L;
    private FakeLibraryInfo libInfo = null;

    public FakeLibraryUserinterfaceWidgetFactory(FakeLibraryInfo tempInfo) {
        libInfo = tempInfo;
    }

    public SourceSelection getSourceSelectionPanel(MBMainframeInfo mainInfo) {
        return null;
    }

    public MultipleSourceSelection getUsermodSourceSelectionPanel(MBMainframeInfo mainInfo) {
        return null;
    }

    public LibraryInfoEditor getLibraryInfoEditor(javax.swing.JInternalFrame parentFrame) {
        return new FakeLibraryInfoEditor(parentFrame, libInfo);
    }


    public SourceSelection getMetadataLibrarySourcePanel(com.ibm.sdwb.build390.MBMainframeInfo mainInfo) {
        return null;
    }
}
