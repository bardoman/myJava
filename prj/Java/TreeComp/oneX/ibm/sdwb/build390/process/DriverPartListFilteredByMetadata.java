package com.ibm.sdwb.build390.process;

import java.util.*;

import com.ibm.sdwb.build390.*;
import com.ibm.sdwb.build390.library.LibraryInfo;
import com.ibm.sdwb.build390.logprocess.LogEventProcessor;
import com.ibm.sdwb.build390.mainframe.DriverInformation;
import com.ibm.sdwb.build390.mainframe.ReleaseInformation;
import com.ibm.sdwb.build390.process.steps.ProcessStep;
import com.ibm.sdwb.build390.userinterface.UserCommunicationInterface;

public class DriverPartListFilteredByMetadata extends AbstractProcess {
    static final long serialVersionUID = 1111111111111111L;

    private com.ibm.sdwb.build390.process.steps.DriverPartListFilteredByMetadata listPartsStep = null;


    public DriverPartListFilteredByMetadata(Collection tempCriteria,java.io.File tempLocalSavePath,MBMainframeInfo tempMainInfo,LibraryInfo tempLib, ReleaseInformation tempRelease, DriverInformation tempDriver, UserCommunicationInterface userComm){
        super("Driver Partlist filtered by metadata",1, userComm); 
        listPartsStep = new com.ibm.sdwb.build390.process.steps.DriverPartListFilteredByMetadata(tempCriteria, tempLocalSavePath,tempMainInfo,tempLib, tempRelease, tempDriver,this);
    }

    public List getResults(){
        return  listPartsStep.getResults();
    }

    public void setMetadataFilterOrderFile(String uploadedHostFile){
        listPartsStep.setMetadataFilterOrderFile(uploadedHostFile);
    } 

    public void setMetadataFilterId(String filterId){
        listPartsStep.setMetadataFilterId(filterId);
    }


    protected ProcessStep getProcessStep(int stepToGet, int stepIteration) {
        switch (stepToGet) {
        case 0:
            return listPartsStep;
        }
        return null;
    }
}
