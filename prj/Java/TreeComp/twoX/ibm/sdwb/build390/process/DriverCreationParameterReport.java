package com.ibm.sdwb.build390.process;

import com.ibm.sdwb.build390.process.steps.ProcessStep;
import com.ibm.sdwb.build390.logprocess.LogEventProcessor;
import com.ibm.sdwb.build390.userinterface.UserCommunicationInterface;
import com.ibm.sdwb.build390.*;
import java.util.*;
import java.io.*;

public class DriverCreationParameterReport extends AbstractProcess {
    static final long serialVersionUID = 1111111111111111L;

    private MBBuild build = null;
    private com.ibm.sdwb.build390.process.steps.DriverCreationParameterReport driverCreationReportStep = null;
    private String MVSHighLevelQualifier = null;
    private String MVSReleaseName = null;
    private String driver = null;

    public DriverCreationParameterReport(MBBuild tempBuild, String tempMVSHLQ, String tempMVSRelease, String tempDriver, UserCommunicationInterface userComm){
        super("Driver Creation Parameter Report",1, userComm); 
        build = tempBuild;
        MVSHighLevelQualifier = tempMVSHLQ;
        MVSReleaseName = tempMVSRelease;
        driver = tempDriver;
        driverCreationReportStep = new com.ibm.sdwb.build390.process.steps.DriverCreationParameterReport(build, MVSHighLevelQualifier, MVSReleaseName, driver, this);
    }

    public void setBuildForList(MBBuild newBuild){
        build = newBuild;
    }
    public void setAlternativeSaveLocation(File tempLocation){
        driverCreationReportStep.setAlternativeSaveLocation(tempLocation);
    }
    public boolean isCheckSuccessful(){
        return 	driverCreationReportStep.isCheckSuccessful();
    }

    public Map getSettingMap(){
        return driverCreationReportStep.getSettingMap();
    }

    protected ProcessStep getProcessStep(int stepToGet, int stepIteration) {
        switch (stepToGet) {
        case 0:
            return driverCreationReportStep;
        }
        return null;
    }

    public File  getOutputResultFile(){
        return driverCreationReportStep.getPrintFile();
    }
}


