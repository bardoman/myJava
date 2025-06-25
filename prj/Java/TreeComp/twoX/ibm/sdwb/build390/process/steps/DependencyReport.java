package com.ibm.sdwb.build390.process.steps;

import com.ibm.sdwb.build390.*;
import com.ibm.sdwb.build390.logprocess.LogEventProcessor;
import com.ibm.sdwb.build390.info.InfoForMainframePartReportRetrieval;
import java.util.*;
import java.io.*;
import com.ibm.sdwb.build390.process.AbstractProcess;
import com.ibm.sdwb.build390.userinterface.text.commandline.arguments.*;
import com.ibm.sdwb.build390.library.Changeset;
import com.ibm.sdwb.build390.library.LibraryInfo;
import com.ibm.sdwb.build390.process.steps.ConcurrentSteps;


public class DependencyReport extends ProcessStep {
    static final long serialVersionUID = 1111111111111111L;

    private transient MBSocket mainframeCommunication = null;
    private MBBuild build = null;
    private File localSavePath = null;
    private String buildLevel = null;
    private String HFSSavePath = null;
    private String dataSetName = null;
    private String hostSavedFileLocation ="";
    private String localOutputLocation ="";
    private InfoForMainframePartReportRetrieval partInfo = null;
    private transient java.util.Random randomSource = new java.util.Random();

    //actually we don't need the full build object. We need ReleaseInfo, DriverInfo, LibraryInfo, MBMainframeInfo. And this class should  extend MainframeCommunication.
    // Something to tweak later. 
    public DependencyReport(MBBuild tempBuild,File tempLocalSavePath, InfoForMainframePartReportRetrieval tempMainframePart,AbstractProcess tempProcess) {
        super(tempProcess,"Dependency Report");
        setVisibleToUser(true);
        setUndoBeforeRerun(true);
        build = tempBuild;
        localSavePath = tempLocalSavePath;
        partInfo = tempMainframePart;
    }

    public void setBuildLevel(String tempBuildLevel) {
        buildLevel = tempBuildLevel;
    }

    public void setHFSSavePath(String tempPath) {
        HFSSavePath = tempPath;
    }

    public void setPDSSavePath(String tempPath) {
        dataSetName = tempPath;
    }

    public String getLocalOutputLocation() {
        return localOutputLocation;
    }

    public String getHostSavedLocation() {
        return hostSavedFileLocation;
    }

    /**
     * This is the method that should be implemented to actually
     * run the process.	Use executionArgument if you need to 
     * access the argument from the execute method.
     * 
     * @return Object indicating output of the step.
     */

    public void execute() throws com.ibm.sdwb.build390.MBBuildException{
        getLEP().LogSecondaryInfo(getFullName(),"Entry");
        final File commandOutputPath;

        if (localSavePath==null) {
            commandOutputPath = new File(MBGlobals.Build390_path+"logfiles"+File.separator);
        } else {
            commandOutputPath = localSavePath;
        }

        final String dependencyType = partInfo.getReportType();
        String clrout_ = commandOutputPath.getAbsolutePath()+File.separator +partInfo.getPartName()+"."+partInfo.getPartClass()+"-DEPENDENCY-"+dependencyType;
        String cmd_ = "XDEPRPT "+build.getLibraryInfo().getDescriptiveStringForMVS()+", CMVCREL='"+build.getReleaseInformation().getLibraryName()+"', DRIVER="+build.getDriverInformation().getName()+", MOD="+partInfo.getPartName()+", CLASS="+partInfo.getPartClass()+", TYPE="+dependencyType;
        if (buildLevel != null) {
            cmd_ += ", BLDLVL="+buildLevel;
        }
        MBSocket mySock = new MBSocket(cmd_, clrout_, "Requesting Dependency Report" , getStatusHandler(), build.getSetup().getMainframeInfo(), getLEP());
        mySock.run();

        String localName = clrout_+MBConstants.CLEARFILEEXTENTION;
        boolean isValidFile = (new File(localName)).exists() && (new File(localName)).length() > 0;

        //Begin TST3016
        String uploadName=null;
        String dispName = null;
        if ((dataSetName != null) &&  localName !=null && isValidFile) {
            String lastQualifier = "DEP" + (new String(Math.abs(randomSource.nextLong()) + "00000")).substring(0, 4);
            uploadName = dataSetName + "." + lastQualifier;
            dispName  = "If partition dataset,           data saved as " + dataSetName +"("+lastQualifier +") (or)\n";
            dispName += "If physical sequential dataset, data saved in " + dataSetName +"."+lastQualifier +"\n";

        }
        if ((HFSSavePath!=null) &&  localName !=null && isValidFile) {
            if (!HFSSavePath.endsWith("/")) {
                HFSSavePath += "/";
            }
            uploadName = HFSSavePath +  "DEP" + (new String(Math.abs(randomSource.nextLong()) + "00000")).substring(0, 4);
            dispName = uploadName;
        }

        if (uploadName!=null) {
            uploadFileToHost(localName,uploadName);
            hostSavedFileLocation = dispName;
        }
        //End TST3016
        localOutputLocation = localName;



    }

    private void uploadFileToHost(String localName, String dataSetName) throws MBBuildException {

        getStatusHandler().updateStatus("Upload file "+ localName + " to host.", false);
        MBFtp ftpObject = new MBFtp(build.getSetup().getMainframeInfo(),getLEP());

        if (!ftpObject.put(new File(localName),dataSetName)) {
            throw new FtpError("Could not upload file "+ localName + " to " + dataSetName);
        }

    }   

}
