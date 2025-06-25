package com.ibm.sdwb.build390.user;
import java.io.Serializable;

import com.ibm.sdwb.build390.MBMainframeInfo;
import com.ibm.sdwb.build390.library.LibraryInfo;



public class Setup implements Serializable,Cloneable {


    static final long serialVersionUID = 1111111111111111L;

    private LibraryInfo libraryInfo;
    private MBMainframeInfo mainframeInfo;
    private String identifyingString = null;

    private boolean useDefaultEditor = true;                    // EditorSelect
    private String editor;


    public Setup(LibraryInfo tempLibraryInfo, MBMainframeInfo tempMainframeInfo,String tempEditor, boolean tempUseDefaultEditor) {
        // this is mildly dangerous.  At some point we need to put in cloning (and not the .clone method, that's unreliable) like adding a cloneLibraryInfo method to LibraryInfo that had to be implemented
        // by all subclasses
        libraryInfo = tempLibraryInfo;
        mainframeInfo = tempMainframeInfo;
        useDefaultEditor = tempUseDefaultEditor;
        editor = tempEditor;
    }

    public void setLibraryInfo(LibraryInfo tempLib){
        this.libraryInfo = tempLib;
    }

    public LibraryInfo getLibraryInfo() {
        return libraryInfo;
    }

    public void setMainframeInfo(MBMainframeInfo tempMain){
        this.mainframeInfo = tempMain;
    }

    public MBMainframeInfo getMainframeInfo() {
        return mainframeInfo;
    }

    public boolean useDefaultEditor() {
        return useDefaultEditor;
    }

    public String getEditorPath() {
        return editor;
    }

    public String getIdentifyingStringForLibraryMainframePair(){
        if (identifyingString == null) {
            identifyingString = libraryInfo.getProcessServerName()+libraryInfo.getProcessServerAddress()+"|"+mainframeInfo.getMainframeAddress()+"|"+mainframeInfo.getMainframePort();
        }
        return identifyingString;
    }

    public Setup getClone() {
        try {
            return(Setup) clone();
        } catch (CloneNotSupportedException cnse) {
            throw new RuntimeException("Problem making clone of "+getClass().getName(), cnse);
        }
    }

    protected Object clone() throws CloneNotSupportedException{
        return new Setup(getLibraryInfo(), getMainframeInfo(), getEditorPath() , useDefaultEditor());
    }


}
