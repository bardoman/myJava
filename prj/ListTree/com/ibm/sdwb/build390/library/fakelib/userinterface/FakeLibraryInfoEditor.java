package com.ibm.sdwb.build390.library.fakelib.userinterface;

import javax.swing.JInternalFrame;

import com.ibm.sdwb.build390.library.fakelib.FakeLibraryInfo;
import com.ibm.sdwb.build390.library.userinterface.LibraryInfoEditor;


public class FakeLibraryInfoEditor extends LibraryInfoEditor {

    private FakeLibraryInfo libInfo = null;

    public FakeLibraryInfoEditor(JInternalFrame tempParentFrame, FakeLibraryInfo tempLib) {
        super(tempParentFrame, tempLib);
        setTitle("NOLIB Setup Information");
        libInfo = tempLib;
        setVisible(true);
    }


}
