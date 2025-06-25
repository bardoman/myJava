package com.ibm.sdwb.build390.process;

import java.io.File;
import java.util.*;

import com.ibm.sdwb.build390.*;
import com.ibm.sdwb.build390.library.LibraryInfo;
import com.ibm.sdwb.build390.logprocess.LogEventProcessor;
import com.ibm.sdwb.build390.mainframe.DriverInformation;
import com.ibm.sdwb.build390.mainframe.ReleaseInformation;
import com.ibm.sdwb.build390.info.FileInfo;
import com.ibm.sdwb.build390.process.steps.ProcessStep;
import com.ibm.sdwb.build390.userinterface.UserCommunicationInterface;

public class CheckMetadataValidity extends AbstractProcess {
    static final long serialVersionUID = 1111111111111111L;

    private com.ibm.sdwb.build390.process.steps.CheckMetadataValidity checkMetadataStep = null;

    public CheckMetadataValidity(FileInfo   tempSourcePartName,ReleaseInformation tempRel,DriverInformation tempDrv, MBMainframeInfo tempMain, LibraryInfo tempLib, File tempLocalMetadataFile, UserCommunicationInterface userComm){
        super("Check metadata validity",1, userComm); 
        checkMetadataStep = new com.ibm.sdwb.build390.process.steps.CheckMetadataValidity(tempSourcePartName, tempRel,tempDrv, tempMain,tempLib,tempLocalMetadataFile, this);
    }

    /*public void setTrackCheck(boolean tempTrackCheck){
        checkMetadataStep.setTrackCheck(tempTrackCheck);
    }*/


    public void setOutputHeaderLocation(String outputHeaderLocation) {
        checkMetadataStep.setOutputHeaderLocation(outputHeaderLocation);
    }

    protected ProcessStep getProcessStep(int stepToGet, int stepIteration) {
        switch (stepToGet) {
        case 0:
            return checkMetadataStep;
        }
        return null;
    }

    public Set getFailedParts(){
        return checkMetadataStep.getFailedParts();
    }


    public String getOutputMetaCheckFileName(){
        if (checkMetadataStep!=null) {
            String fileName = checkMetadataStep.getOutputFile().getAbsolutePath();
            return fileName.substring(0,fileName.indexOf(MBConstants.CLEARFILEEXTENTION));
        }
        return null;
    }

}
