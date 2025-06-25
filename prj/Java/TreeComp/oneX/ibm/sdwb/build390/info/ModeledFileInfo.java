package com.ibm.sdwb.build390.info;

import java.io.*;

public class ModeledFileInfo extends FileInfo{

    static final long serialVersionUID = 1111111111111111L;
	private String partToModelAfterName = null;
	private String partToModelAfterDirectory = null;

	public ModeledFileInfo(String tempDir, String tempName, String tempModelAfterDirectory, String tempModelAfterName) {
		super(tempDir,tempName);
		partToModelAfterDirectory = tempModelAfterDirectory;
		partToModelAfterName = tempModelAfterName;
    }

	public void setModelAfterInforamtion(String tempModelDirectory, String tempModelName){
		partToModelAfterName = tempModelName;
		partToModelAfterDirectory = tempModelDirectory;
	}

	public String getModelAfterName(){
		return partToModelAfterName;
	}

	public String getModelAfterDirectory(){
		return partToModelAfterDirectory;
	}

    public boolean equals(Object testObject) {
        if (!(testObject instanceof ModeledFileInfo)) {
            return false;
        }
		if (!super.equals(testObject)) {
			return false;
		}
        ModeledFileInfo testInfo = (ModeledFileInfo) testObject;
        if (partToModelAfterName != null) {
            if (!partToModelAfterName.equals(testInfo.partToModelAfterName)){
                return false;
            }
        }else if (testInfo.partToModelAfterName != null) {
            return false;
        }
        if (partToModelAfterDirectory != null) {
            if (!partToModelAfterDirectory.equals(testInfo.partToModelAfterDirectory)){
                return false;
            }
        }else if (testInfo.partToModelAfterDirectory != null) {
            return false;
        }
		return true;
    }

    public String toString() {
        return super.toString() + " append "+partToModelAfterDirectory+","+partToModelAfterName;
    }
}
