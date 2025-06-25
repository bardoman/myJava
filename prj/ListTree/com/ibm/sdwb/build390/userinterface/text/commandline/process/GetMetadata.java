package com.ibm.sdwb.build390.userinterface.text.commandline.process;

import java.io.*;
import java.sql.Driver;
import java.util.*;

import com.ibm.sdwb.build390.*;
import com.ibm.sdwb.build390.logprocess.*;
import com.ibm.sdwb.build390.mainframe.*;
import com.ibm.sdwb.build390.user.*;
import com.ibm.sdwb.build390.userinterface.text.commandline.*;
import com.ibm.sdwb.build390.userinterface.text.commandline.arguments.*;
import com.ibm.sdwb.build390.utilities.*;
import com.ibm.sdwb.build390.info.*;
import com.ibm.sdwb.build390.metadata.utilities.*;
import  com.ibm.sdwb.build390.metadata.info.*;

public class GetMetadata extends CommandLineProcess {

    public static final String PROCESSNAME = "GETMETADATA";

    private LibraryRelease libraryRelease = new LibraryRelease();
    private MainframeDriver driver = new MainframeDriver();
    private MetaFile metaFile = new MetaFile();

    public GetMetadata(LogEventProcessor tempLep, com.ibm.sdwb.build390.MBStatus tempStatus) {
        super(PROCESSNAME, tempLep, tempStatus);
    }

    public String getHelpDescription() {
        return "Get the Metadata values for given part within the Release and Driver specified";

    }

    public String getHelpExamples() {
        return getProcessTypeHandled()+" LIBRELEASE=<librelease> DRIVER=<driver> LIBPARTNAME=<partname>";
    }

    protected void setArgumentStructure(RequiredAndOptionalArguments argumentStructure) {
        BooleanAnd baseAnd = new BooleanAnd();
        baseAnd.addBooleanInterface(libraryRelease);
        baseAnd.addBooleanInterface(driver);
        baseAnd.addBooleanInterface(metaFile);
        argumentStructure.setRequiredPart(baseAnd);

    }

    public void runProcess() throws com.ibm.sdwb.build390.MBBuildException{
        MBBuild build = new MBBuild(getLEP());
        Setup setup = build.getSetup();

        ReleaseInformation releaseInfo = getReleaseInformation(libraryRelease.getValue(),setup, true);
        DriverInformation driverInfo = getDriverInformation(driver.getValue(),releaseInfo,setup);

        build.setReleaseInformation(releaseInfo);
        build.setDriverInformation(driverInfo);

        /*
        com.ibm.sdwb.build390.process.GetBuildTypeList buildTypeRetrieval = new com.ibm.sdwb.build390.process.GetBuildTypeList(tempBuild.getDriverInformation(), tempBuild.getMainframeInfo(), tempBuild.getLibraryInfo(), tempBuild.getBuildPathAsFile(), this);

        setCancelableProcess(buildTypeRetrieval);

        buildTypeRetrieval.externalRun();
        System.out.println("\nBuildtypes:");
        com.ibm.sdwb.build390.userinterface.text.utilities.OutputFormattingMethods.formatList(buildTypeRetrieval.getListOfBuildTypes(), System.out);
        */

        //**************************
        //com.ibm.sdwb.build390.mainframe.ReleaseInformation relInfo = sourceSelectionPanel.getProjectChosen();

        // build.setReleaseInformation(relInfo);
        // build.setDriverInformation(mainframeSelectionPanel.getDriverSelected());

        com.ibm.sdwb.build390.process.ProcessWrapperForSingleStep wrapper = new com.ibm.sdwb.build390.process.ProcessWrapperForSingleStep(this); 
        com.ibm.sdwb.build390.process.steps.DriverReport step = new com.ibm.sdwb.build390.process.steps.DriverReport(build.getDriverInformation(),build.getMainframeInfo(),build.getLibraryInfo(),
                                                                                                                     new java.io.File(build.getBuildPath()),wrapper);

        step.setForceNewReport(true);
        step.setIncludePathname(true);
        step.setSummaryType("LOCAL");
        step.setIncludeOnlyLibraryParts(true);
        wrapper.setStep(step);
        //stopObject = wrapper;
        wrapper.externalRun();



        if(step.getParser()!=null && step.getParser().getPartsInfo().size() > 0) {

            Vector vect = step.getParser().getPartsInfo();

            for(int i=0;i!=vect.size();i++) {
                FileInfo tempInfo = (FileInfo) vect.get(i);

                if(tempInfo.getName().equals("CLRALIAS.ASM")) {

                    try {

                        Set request = new HashSet();

                        InfoForMainframePartReportRetrieval info;

                        if(tempInfo.getMainframeFilename() !=null && tempInfo.getMainframeFilename().indexOf(".")  > 0) {
                            info =  new InfoForMainframePartReportRetrieval(tempInfo.getMainframeFilename().substring(tempInfo.getMainframeFilename().indexOf(".")+1),
                                                                            tempInfo.getMainframeFilename().substring(0,tempInfo.getMainframeFilename().indexOf(".")));
                        }
                        else {
                            info = new InfoForMainframePartReportRetrieval(null,null);
                        }


                        info.setReportType("ALL");
                        info.setDirectory(tempInfo.getDirectory());
                        info.setName(tempInfo.getName());

                        request.add(info);

                        //we don't need to setRelease/Driver. Its done when the event changes happen, but the DriverUpdateEvent occurs prior to ReleaseUpdate. 
                        //need to debug it later.
                        //build.setReleaseInformation(sourceSelectionPanel.getProjectChosen());
                        //build.setDriverInformation(mainframeSelectionPanel.getDriverSelected());

                        com.ibm.sdwb.build390.process.MetadataReport getMetadata = new  com.ibm.sdwb.build390.process.MetadataReport(build,new File(build.getBuildPath()), request, this);
                        getMetadata.setBuildLevel("1");
                        getMetadata.externalRun();
                        Set returnedFiles = getMetadata.getLocalOutputFiles();
                        if(!returnedFiles.isEmpty()) {
                            String valueReportFilename = (String) returnedFiles.iterator().next();
                            if(getMetadata.getReturnCode() > 4) {
                                throw new HostError("There was an error retrieving a metadata report.", valueReportFilename.substring(0,valueReportFilename.indexOf(MBConstants.CLEARFILEEXTENTION)), getMetadata.getReturnCode());
                            }

                            Set  partsSet = new HashSet();
                            partsSet.add(tempInfo);
                            build.setPartInfo(partsSet);

                            MetadataValueGenerator metadataValues = new MetadataValueGenerator(valueReportFilename, tempInfo , build.getLibraryInfo().getMetadataOperationsHandler(),this);
                            GeneratedMetadataInfo generatedMetadataInfo = metadataValues.getGeneratedMetadataInfo();
                            generatedMetadataInfo.setReleaseAndDriverInformation(build.getReleaseInformation(),build.getDriverInformation());

                            int n=0;
                            //SingleSourceMetadataEditorFrame editPanel = new SingleSourceMetadataEditorFrame(generatedMetadataInfo,build.getSetup().getMainframeInfo(), build.getSetup().getLibraryInfo(),lep);
                            //editPanel.setReportSaveLocation(build.getBuildPathAsFile());
                            //editPanel.revalidate();
                            //editPanel.repaint();
                        }
                        else {
                            String partname = info.getPartClass()!=null ? (info.getPartClass() + "."+ info.getPartName()) :
                                              (info.getDirectory()+info.getName());
                            throw new GeneralError("There was an error retrieving a metadata report for " + partname);
                        }

                    }
                    catch(MBBuildException mbe) {
                        getLEP().LogException(mbe);
                    }
                }
            }



        }
        //*************************
    }
}
