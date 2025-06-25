package com.ibm.sdwb.build390.process.steps;

import com.ibm.sdwb.build390.*;
import com.ibm.sdwb.build390.logprocess.LogEventProcessor;
import java.io.File;

public class CheckPartlistAgainstShadow extends MainframeCommunication {
    static final long serialVersionUID = 1111111111111111L;
    private MBBuild build = null;
    private File partlistFileForShadowCheck = null;
    private String shadowCheckType = new String();
    private String fastTrackArguments = null;
    public static final String SHADOWCHECKFILE = "SHADOWCHECK";

    public CheckPartlistAgainstShadow(MBBuild tempBuild, File tempFilename, String tempCheckType,  com.ibm.sdwb.build390.process.AbstractProcess tempProc) {
        super(tempBuild.getBuildPath() + SHADOWCHECKFILE + "-" + tempCheckType,"Check Partlist Against Shadow", tempProc);
        setUndoBeforeRerun(false);
        build = tempBuild;
        partlistFileForShadowCheck = tempFilename;
        shadowCheckType = tempCheckType;
    }

    public String getOutputFileOfShadowCheck() {
        return getOutputFile().getAbsolutePath();
    }

    public void setFastrackArguments(String tempArgs) {
        fastTrackArguments = tempArgs;
    }

    /**
     * This is the method that should be implemented to actually
     * run the process.	Use executionArgument if you need to 
     * access the argument from the execute method.	
     * 
     * @return Object A string representing the path to the output files, minus extentions.
     */
    public void execute() throws com.ibm.sdwb.build390.MBBuildException{
        getLEP().LogSecondaryInfo(getFullName(),"Entry");
        String shadowCheckCommand = null;
        if(fastTrackArguments !=null) {
            shadowCheckCommand = "FASTRACK OP=CHK, "+ build.getLibraryInfo().getDescriptiveStringForMVS()+
                                 ", CMVCREL=\'"+build.getReleaseInformation().getLibraryName()+"\', DRIVER="+build.getDriverInformation().getName()+", BUILDID="+build.get_buildid() + ", "+fastTrackArguments;
        }
        else {
            boolean initializeShadowCheckCommand = true;
            if(partlistFileForShadowCheck != null) {
                if(partlistFileForShadowCheck.exists()) {
                    getStatusHandler().updateStatus("Uploading "+shadowCheckType+" partlist for check", false);
                    String MVSOrderFile = new String(build.getReleaseInformation().getMvsHighLevelQualifier()+"."+build.getReleaseInformation().getMvsName()+ "."+build.getDriverInformation().getName()+".ORDERS("+build.get_buildid()+")");
                    MBFtp partlistFtpClient = new MBFtp(build.getSetup().getMainframeInfo(),getLEP());
                    if(!partlistFtpClient.put(partlistFileForShadowCheck, MVSOrderFile)) {
                        throw new FtpError("Could not upload "+partlistFileForShadowCheck+" to "+MVSOrderFile);
                    }
                }
                else {
                    initializeShadowCheckCommand = false;
                }
            }

            if(initializeShadowCheckCommand) {

                shadowCheckCommand = "SHADPART OP=CHK, DRIVER="+build.getDriverInformation().getName()+", "+build.getLibraryInfo().getDescriptiveStringForMVS()+
                                     ", CMVCREL=\'"+build.getReleaseInformation().getLibraryName()+"\', BUILDID="+build.get_buildid();

                //Begin INT3097C
                boolean isFakeLib = MBClient.getCommandLineSettings().getMode().isFakeLibrary();

                boolean isPDSBuild = false;

                if(build instanceof MBUBuild) {

                    isPDSBuild = ((MBUBuild) build).getSourceType()==MBUBuild.PDS_SOURCE_TYPE;
                }

                if(isFakeLib & isPDSBuild) {
                    shadowCheckCommand+= ", NOLIB=YES";
                }
                //End INT3097C
            }
        }
        if(shadowCheckCommand!=null) {
            createMainframeCall(shadowCheckCommand, "Checking partlist against shadow", build.getSetup().getMainframeInfo());
            setPathToVerbFile(build.getReleaseInformation().getMvsHighLevelQualifier()+"."+build.getReleaseInformation().getMvsName()+"."+build.getDriverInformation().getName()+".ORDERS");
            runMainframeCall();
        }
    }
}
