package com.ibm.sdwb.build390.process;

import java.io.File;
import java.util.*;

import com.ibm.sdwb.build390.*;
import com.ibm.sdwb.build390.info.InfoForMainframePartReportRetrieval;
import com.ibm.sdwb.build390.logprocess.LogEventProcessor;
import com.ibm.sdwb.build390.process.steps.*;
import com.ibm.sdwb.build390.userinterface.UserCommunicationInterface;

public class DependencyReport extends AbstractProcess {
    static final long serialVersionUID = 1111111111111111L;

    private String buildLevel;
    private String HFSSavePath;
    private String PDSSavePath;
    private Set localOutFilesSet;
    private Set savedHostFilesSet;
    private File localSavePath = null;
    private MBBuild build; //remove this dependency, and send in only what this process needs.to-do later.
    private Set partInfoAndTypeSet;
    private transient ConcurrentSteps dependencyConcurrentSteps = null;

    public DependencyReport(MBBuild tempBuild, java.io.File tempLocalSavePath, Set tempPartInfoAndTypeSet, UserCommunicationInterface userComm) {
        super("Dependency Retrieval",1, userComm); 
        partInfoAndTypeSet = tempPartInfoAndTypeSet;
        build = tempBuild;
        localSavePath = tempLocalSavePath;
        localOutFilesSet = new HashSet();
        savedHostFilesSet = new HashSet();
    }

    public void setBuildLevel(String tempBuildLevel) {
        buildLevel = tempBuildLevel;
    }
    public void setHFSSavePath(String tempPath) {
        HFSSavePath = tempPath;
    }

    public void setPDSSavePath(String tempPath) {
        PDSSavePath = tempPath;
    }


    public Set getLocalOutputFiles() {
        return localOutFilesSet;
    }

    public Set getHostSavedLocation() {
        return savedHostFilesSet;
    }

    private ConcurrentSteps createDependencyReportStepsToRun() throws com.ibm.sdwb.build390.MBBuildException {
        dependencyConcurrentSteps = new ConcurrentSteps("Create Dependency", this);    
        for (Iterator partIterator = partInfoAndTypeSet.iterator(); partIterator.hasNext(); ) {
            final InfoForMainframePartReportRetrieval partInfo = (InfoForMainframePartReportRetrieval) partIterator.next();
            com.ibm.sdwb.build390.process.steps.DependencyReport dependencyReportStep = new com.ibm.sdwb.build390.process.steps.DependencyReport(build, localSavePath, partInfo,this);
            dependencyReportStep.setBuildLevel(buildLevel);
            dependencyReportStep.setHFSSavePath(HFSSavePath);
            dependencyReportStep.setPDSSavePath(PDSSavePath);
            dependencyConcurrentSteps.addStepToRun(dependencyReportStep);
        }
        return dependencyConcurrentSteps;
    }

    protected ProcessStep getProcessStep(int stepToGet, int stepIteration) throws  com.ibm.sdwb.build390.MBBuildException {
        switch (stepToGet) {
        case 0:            
            return createDependencyReportStepsToRun();
        }
        return null;
    }

    protected void postStep(int stepRun, int stepIteration) throws com.ibm.sdwb.build390.MBBuildException{
        switch (stepRun) {
        case 0:
            for (Iterator iter  = dependencyConcurrentSteps.getStepSetToRun().iterator();iter.hasNext();) {
                com.ibm.sdwb.build390.process.steps.DependencyReport depStep = (com.ibm.sdwb.build390.process.steps.DependencyReport)iter.next();
                localOutFilesSet.add(depStep.getLocalOutputLocation());
                savedHostFilesSet.add(depStep.getHostSavedLocation());         
            }
        }
    }
}
