package com.ibm.sdwb.build390.process;

import com.ibm.sdwb.build390.process.steps.*;
import com.ibm.sdwb.build390.*;
import com.ibm.sdwb.build390.logprocess.LogEventProcessor;
import com.ibm.sdwb.build390.info.*;
import com.ibm.sdwb.build390.userinterface.UserCommunicationInterface;
import java.util.*;
import java.io.*;
import javax.swing.JInternalFrame;

public class FastTrackBuildProcess extends UserBuildProcess {
    static final long serialVersionUID = 1111111111111111L;

    private StringBuffer shadowCheckStringBuffer = null;
    private StringBuffer buildVerbStringBuffer = null;

    private static final String AUTHORIZATIONNAME = "S390UserBuild";
    private static final String PDSFILELOCATION = "PDS";

    public FastTrackBuildProcess(MBUBuild tempBuild, UserCommunicationInterface userComm) {
        super(tempBuild, userComm); 
        tempBuild.set_buildtype(new String ());// buildtype isn't used, but some things complain if its null
        setName("Fast Track Build Process");
        setNumberOfSteps(7);
    }

    public void preExecution() {
        getCleanableEntity().addLocalFileOrDirectory(new File(build.getBuildPath()));
    }

    public boolean isRestartableAfterCompletion(){
        return true;
    }

    /**
     * This method is used to return the steps to run to accomplish
     * a process.   The step to run first, then the next step to run
     * and so on.  If you need to have a step repeated, that should be
     * handled in the step, not here.
     * 
     * @param stepToGet
     *
     * @param stepIteration
     *
     * @return The step to run, null if there are no more steps
     */
    protected ProcessStep getProcessStep(int stepToGet, int stepIteration) {
        switch (stepToGet) {
            case 0:
                driverReportStep = new com.ibm.sdwb.build390.process.steps.DriverReport(build.getDriverInformation(), build.getMainframeInfo(), build.getLibraryInfo(), build.getBuildPathAsFile(), this);
                driverReportStep.setAlwaysRun(true);
                driverReportStep.setCheckForHLQAndDriver(true);
                driverReportStep.setCheckForControlledFlag(true);
                driverReportStep.setForceNewReport(true);
                driverReportStep.setSummaryType("ONLY");
                driverReportStep.setVisibleToUser(false);
                return driverReportStep;
            case 1:
                partlistGenerationStep = new LocalPartlistGeneration(build, this);
                return partlistGenerationStep;
            case 2:
                StringWriter shadowCheckStringWriter = new StringWriter();
                shadowCheckStringBuffer = shadowCheckStringWriter.getBuffer();
                shadowCheckFileGeneration = new GenerateLoadorderAndGeneralShadowPartlist(build,null,null, this);
                shadowCheckFileGeneration.setShadowCheckWriter(shadowCheckStringWriter);
                return shadowCheckFileGeneration;
            case 3:
                if (isPDSBuild()) {
                    return new NoOp(this);
                } else {
                    generalPartlistCheckStep = new CheckPartlistAgainstShadow(build,null, "General", this);
                    String fastrackArgs = shadowCheckStringBuffer.toString();
                    if (partlistGenerationStep.isHostdataStringRequired()) {
                        fastrackArgs +=", HOSTDATA=YES";
                    }
                    generalPartlistCheckStep.setFastrackArguments(fastrackArgs);
                    return generalPartlistCheckStep;
                }
            case 4:
                if (isPDSBuild()) {
                    return new NoOp(this);
                } else {
                    LoadMissingFilesOntoMVS generalUploadStep = new LoadMissingFilesOntoMVS(build,generalPartlistCheckStep.getOutputFileOfShadowCheck(),AUTHORIZATIONNAME, this);
                    generalUploadStep.setName("Upload missing partlist files to MVS");
                    return generalUploadStep;
                }
            case 5:
                StringWriter buildVerbStringWriter = new StringWriter();
                buildVerbStringBuffer = buildVerbStringWriter.getBuffer();
                unbuiltFiles.clear(); /*to avoid duplicates in fastrack during restart the old unbuiltfiles hashset is cleared */
                unbuiltFiles.addAll(build.getPartInfoSet());
                buildVerbCreationStep = new GenerateBuildVerbStep(build, unbuiltFiles, rebuildFiles, null, this);
                buildVerbCreationStep.setBuildVerbWriter(buildVerbStringWriter);
                return buildVerbCreationStep;
            case 6:
                CallDRVRBLD runMVSBuildPhaseProcess = new CallDRVRBLD(build, driverReportStep, 1, false, this);
                FullProcess mvsBuildPhaseStep = new FullProcess(runMVSBuildPhaseProcess, this);
                runMVSBuildPhaseProcess.setFastTrackArguments(prepareFastTrackArguments(buildVerbStringBuffer.toString(), partlistGenerationStep.isHostdataStringRequired()));
                runMVSBuildPhaseProcess.setParentInternalFrame(parentInternalFrame);

                runMVSBuildPhaseProcess.setBuildSource(build.getSource());

                return mvsBuildPhaseStep;
        }
        return null;
    }

    protected void postStep(int stepRun, int stepIteration) throws com.ibm.sdwb.build390.MBBuildException{
        switch (stepRun) {
            case 2:
                if (shadowCheckFileGeneration.isPartlistEmpty()) {
                    throw new com.ibm.sdwb.build390.GeneralError("The partlist was empty");
                }
                break;
        }
    }


    private String prepareFastTrackArguments(String origString, boolean doTheAdd) {
        if (((BuildOptionsLocal) build.getOptions()).getSaveListing() !=null) {
            if (((BuildOptionsLocal) build.getOptions()).getSaveListing().trim().length() >0) {
                origString += ", LISTDSN=" +((BuildOptionsLocal) build.getOptions()).getSaveListing();
            }
        }
        if (((BuildOptionsLocal) build.getOptions()).isUsingEmbeddedMetadata()) {
            origString += ", METADATA=YES";
        }
        if (doTheAdd) {
            return origString + ", HOSTDATA=YES";
        }
        return origString;
    }
}
