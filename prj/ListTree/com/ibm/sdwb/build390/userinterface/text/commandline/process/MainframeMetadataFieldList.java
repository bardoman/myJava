package com.ibm.sdwb.build390.userinterface.text.commandline.process;

import java.util.*;
import com.ibm.sdwb.build390.*;
import com.ibm.sdwb.build390.userinterface.text.commandline.*;
import com.ibm.sdwb.build390.userinterface.text.commandline.arguments.*;
import com.ibm.sdwb.build390.mainframe.*;
import com.ibm.sdwb.build390.logprocess.*;
import com.ibm.sdwb.build390.utilities.*;

//*********************************************************************
//11/11/2003 #DEF.TST1696: USS CMD LINE "METAFLDS" doesnot work
//*********************************************************************

public class MainframeMetadataFieldList extends CommandLineProcess {
    //#DEF.TST1696:
    private LibraryRelease libRelease = new LibraryRelease();

    public static final String PROCESSNAME = "METAFLDS";

    public MainframeMetadataFieldList(LogEventProcessor tempLep, com.ibm.sdwb.build390.MBStatus tempStatus) {
        super(PROCESSNAME, tempLep, tempStatus);
    }

    protected void setArgumentStructure(RequiredAndOptionalArguments argumentStructure) {
        //#DEF.TST1696:
        argumentStructure.setRequiredPart(libRelease);
    }

    public String getHelpDescription() {
        return getProcessTypeHandled()+ " command queries the Build/390 for a report on valid metadata fields.";
    }

    public String getHelpExamples() {
        return getProcessTypeHandled() + " LIBRELEASE=<librelease>";
    }

    public void runProcess() throws com.ibm.sdwb.build390.MBBuildException{
        MBBuild build = new MBBuild(getLEP());
        
        ReleaseInformation releaseInfo = getReleaseInformation(libRelease.getValue(),build.getSetup(), true);
        build.setReleaseInformation(releaseInfo);
        com.ibm.sdwb.build390.process.MetadataReport getMetadata = new com.ibm.sdwb.build390.process.MetadataReport(build,null, null, this);
        getMetadata.setJustGetFields(true);

        setCancelableProcess(getMetadata);

        getMetadata.externalRun();

        getStatusHandler().updateStatus("Please the see the following output files:",false);
        for (Iterator localOutputIterator=getMetadata.getLocalOutputFiles().iterator();localOutputIterator.hasNext();) {

            getStatusHandler().updateStatus((String) localOutputIterator.next(), false);
        }
    }

    private void displayMetadataTypes(MetadataType[] types) {
        System.out.println("KEYWORD           DESCRIPTION          TYPE");
        for (int index = 0; index < types.length; index++) {
            System.out.println(types[index].getKeyword()+"     "+types[index].getDescription()+"       "+types[index].getType());
        }
    }
}
