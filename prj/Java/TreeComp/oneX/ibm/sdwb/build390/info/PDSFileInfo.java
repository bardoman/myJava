package com.ibm.sdwb.build390.info;

import java.io.*;

//Create Class for TST3337

public class PDSFileInfo extends FileInfo implements Serializable {

    static final long serialVersionUID = 1111111111111111L;
    private String infoClass = null;

    public PDSFileInfo(String infoClass, String tempName) {
        super(null,tempName);

        this.infoClass = infoClass;
    }

    public String getInfoClass() {
        return infoClass;
    }

    public void setInfoClass(String infoClass) {
        this.infoClass = infoClass;
    }

    public String getSourcePartKey() {
        return "SRCPART='"+getInfoClass()+"."+getName()+"'";
    }
}
