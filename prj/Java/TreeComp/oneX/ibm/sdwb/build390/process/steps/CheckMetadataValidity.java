package com.ibm.sdwb.build390.process.steps;

import java.io.*;
import java.util.*;

import com.ibm.sdwb.build390.*;
import com.ibm.sdwb.build390.library.*;
import com.ibm.sdwb.build390.logprocess.LogEventProcessor;
import com.ibm.sdwb.build390.mainframe.DriverInformation;
import com.ibm.sdwb.build390.mainframe.ReleaseInformation;
import com.ibm.sdwb.build390.info.FileInfo;
import com.ibm.sdwb.build390.metadata.utilities.MetaCheckParser;

public class CheckMetadataValidity extends MainframeCommunication {
    static final long serialVersionUID = 1111111111111111L;

    private File localMetadataFile = null;
    private FileInfo fileInfo = null;
    public static final String INVALIDFIELDS = "Invalid values were found in the metadata.";
    private Set failedParts = new HashSet();
    private MBMainframeInfo mainframeInfo;
    private LibraryInfo libInfo;
    private ReleaseInformation releaseInfo;
    private DriverInformation driverInfo;

    public CheckMetadataValidity(FileInfo tempSourcePartName, ReleaseInformation tempRel, DriverInformation tempDrv,MBMainframeInfo tempMain, LibraryInfo tempLib, File tempLocalMetadataFile, com.ibm.sdwb.build390.process.AbstractProcess tempProc) {
        super(MBGlobals.Build390_path+"misc"+File.separator+"METACHECK-"+tempSourcePartName, "Check metadata validity", tempProc);
        setVisibleToUser(true);
        setUndoBeforeRerun(false);
        localMetadataFile = tempLocalMetadataFile;
        fileInfo = tempSourcePartName;
        this.mainframeInfo=tempMain;
        this.libInfo= tempLib;
        this.releaseInfo=tempRel;
        this.driverInfo= tempDrv;
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

        // should be set externally.
        //  setOutputHeaderLocation(build.getBuildPath() + "METACHECK-" + (sourcePartName.indexOf("*") >=0 ? "BATCH" : sourcePartName));  

        String uploadHostDatasetName= mainframeInfo.getMainframeUsername()+"."+ releaseInfo.getMvsName()+"."+ driverInfo.getName()  + ".METACHK."+createMetadataCheckFileHostName();

        String sourcePartName = getPartName(fileInfo);
        sourcePartName = sourcePartName.replace('/','-');

        String cmd_ = "XMETACHK METADSN='"+uploadHostDatasetName+"', "+ getSourcePartKeyword(fileInfo) +", "+libInfo.getDescriptiveStringForMVS()+", CMVCREL='"+releaseInfo.getLibraryName()+"', DRIVER="+driverInfo.getName();
        /*   if (trackCheck) {
               cmd_+=", REQFLD=IGNORE";
           }
   */
        //Begin TST3337
        boolean isFakeLib = MBClient.getCommandLineSettings().getMode().isFakeLibrary();

        if(isFakeLib) {
            cmd_+= ", NOLIB=YES";
        }
        //End TST3337




        getStatusHandler().updateStatus("Upload saved temporary metadata file to " + uploadHostDatasetName,false);
        MBFtp ftpObject = new MBFtp(mainframeInfo,getLEP());
        if(!ftpObject.put(localMetadataFile, uploadHostDatasetName)) {
            throw new FtpError("Could not upload file "+ localMetadataFile.getAbsolutePath() + " to file " + uploadHostDatasetName);
        }
        createMainframeCall(cmd_,"Validating metadata", true, mainframeInfo);
        runMainframeCall();
        if(getReturnCode() > 4) {
            throw new HostError("An error occurred while attempting to validate the metadata", this);
        }
        else if(getReturnCode() == 4) {
            //parse the report.
            //and figure out the partnames that have errors.
            MetaCheckParser parser = new MetaCheckParser(getOutputFile().getAbsolutePath());
            parser.parse();
            failedParts = parser.getFailedParts();
            throw new HostError(INVALIDFIELDS, this);
        }
    }

    private String createMetadataCheckFileHostName() {
        String codedName = new String();
        GregorianCalendar  timeCodeSource = new GregorianCalendar ();
        codedName = Integer.toString(timeCodeSource.get(Calendar.MILLISECOND)/100);
        codedName = Integer.toString(timeCodeSource.get(Calendar.SECOND)) + codedName;
        if(codedName.length() < 3) {
            codedName = "0" + codedName;
        }
        codedName = Integer.toString(timeCodeSource.get(Calendar.MINUTE)) + codedName;
        if(codedName.length() < 5) {
            codedName = "0" + codedName;
        }
        codedName = Integer.toString(timeCodeSource.get(Calendar.HOUR_OF_DAY))+codedName;
        if(codedName.length() < 7) {
            codedName = "0" + codedName;
        }
        codedName = "T" +  codedName;
        return codedName;
    }

    private String getPartName(FileInfo fileInfo) {
        if(fileInfo==null) {
            return "*";
        }
        boolean isLibraryNameExists = fileInfo.getName() !=null ?
                                      (fileInfo.getName().trim().length() > 0 ? true : false)  : false;

        String sourcePartName = isLibraryNameExists ? (fileInfo.getDirectory()!=null ?
                                                       fileInfo.getDirectory().trim() : "") +
                                fileInfo.getName() : fileInfo.getMainframeFilename();

        return sourcePartName;
    }



    //Begin TST3337
    private String  getSourcePartKeyword(FileInfo fileInfo) {
        if(fileInfo==null) {
            return "SRCPART='*'";
        }

        return fileInfo.getSourcePartKey();
    }

    /*
    private String getSourcePartKeyword(FileInfo fileInfo){
        if (fileInfo==null) {
            return "SRCPART='*'";
        }


        boolean isLibraryNameExists = fileInfo.getName() !=null ?
                                      (fileInfo.getName().trim().length() > 0 ? true : false)  : false;


        String sourceKeyword = (isLibraryNameExists ? "SRCPART='', ":"");

        sourceKeyword += (isLibraryNameExists ? (fileInfo.getDirectory()!=null ?
                                                "DIR='"+fileInfo.getDirectory().trim() +"', " : "DIR='', ") +
                         "PATH='"+ fileInfo.getName() +"'" : "SRCPART='"+ fileInfo.getMainframeFilename()+"'");

        return sourceKeyword;
    }
    */

    //End TST3337

    public Set getFailedParts() {
        return failedParts;
    }

}
