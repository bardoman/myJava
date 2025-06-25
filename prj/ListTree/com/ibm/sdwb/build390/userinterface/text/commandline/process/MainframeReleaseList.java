package com.ibm.sdwb.build390.userinterface.text.commandline.process;

import java.util.*;
import java.io.*;
import com.ibm.sdwb.build390.*;
import com.ibm.sdwb.build390.userinterface.text.commandline.*;
import com.ibm.sdwb.build390.userinterface.text.commandline.arguments.*;
import com.ibm.sdwb.build390.utilities.*;
import com.ibm.sdwb.build390.mainframe.*;
import com.ibm.sdwb.build390.logprocess.*;

public class MainframeReleaseList extends CommandLineProcess {

    public static final String PROCESSNAME = "RELEASELIST";

    private MBBuild build = null;

    public MainframeReleaseList(LogEventProcessor tempLep, com.ibm.sdwb.build390.MBStatus tempStatus) {
        super(PROCESSNAME, tempLep, tempStatus);
    }
    public String getHelpDescription() {
        return getProcessTypeHandled()+ " command queries the Build/390 server for a list of releases.";
    }

    public String getHelpExamples() {
        return getProcessTypeHandled();
    }
    protected void setArgumentStructure(RequiredAndOptionalArguments argumentStructure) {
        argumentStructure.setRequiredPart(new NoArguments());
        // nothing here, no args
    }

    public void runProcess() throws com.ibm.sdwb.build390.MBBuildException{
        build = new MBBuild(getLEP());
        com.ibm.sdwb.build390.process.MVSReleaseAndDriversList releaseList = new com.ibm.sdwb.build390.process.MVSReleaseAndDriversList(build.getMainframeInfo(), build.getLibraryInfo(), null, this);

        setCancelableProcess(releaseList);

        releaseList.externalRun();
        try {
            displayList(releaseList);
        }
        catch(java.io.IOException ioe) {
            throw new com.ibm.sdwb.build390.GeneralError("There was an error displaying the release list.", ioe);
        }
    }


    private void displayList(com.ibm.sdwb.build390.process.MVSReleaseAndDriversList releaseListSource)throws java.io.IOException{ /*TST1671 */
        Set releaseSet = build.getSetup().getMainframeInfo().getReleaseSet(build.getSetup().getLibraryInfo());
        StringBuilder strb = new StringBuilder();
        Formatter formatter = new Formatter(strb);
        if (releaseSet !=null) {
            formatter.format("%-29s%n","=============================");
            formatter.format("** %s%n",build.getSetup().getMainframeInfo().getMainframeAddress());
            formatter.format("** %s%n",build.getSetup().getLibraryInfo().getAddressStringForMVS());
            formatter.format("%s:  %-15s:%n","MVS Release","LIBRARY release");
            formatter.format("%-29s%n","=============================");
            for (Iterator releaseIterator = releaseSet.iterator(); releaseIterator.hasNext();) {
                ReleaseInformation oneRelease = (ReleaseInformation) releaseIterator.next();
                // should be made pretty later, when ISPF supports it.
                // formatter.format("%-8s      %s%n",oneRelease.getMvsName(),oneRelease.getLibraryName());
                formatter.format("%s%n",com.ibm.sdwb.build390.utilities.ParsingFunctions.AppendSpaceAtEndOfString(oneRelease.getMvsName(),8) + "      " + oneRelease.getLibraryName());
            }
            formatter.format("%-29s%n","=============================");
            System.out.print(strb.toString());
            getStatusHandler().updateStatus("The output is saved in "+releaseListSource.getOutputResultsFile().getAbsolutePath(),false);
        } else {
            System.out.println("No releases found");
        }

    }

    
}
