package com.ibm.sdwb.build390.userinterface.text.commandline.process;

import java.io.File;
import java.util.Iterator;

import com.ibm.sdwb.build390.GeneralError;
import com.ibm.sdwb.build390.MBClient;
import com.ibm.sdwb.build390.MBConstants;
import com.ibm.sdwb.build390.MBGlobals;
import com.ibm.sdwb.build390.SyntaxError;
import com.ibm.sdwb.build390.logprocess.LogEventProcessor;
import com.ibm.sdwb.build390.user.Setup;
import com.ibm.sdwb.build390.user.SetupManager;
import com.ibm.sdwb.build390.userinterface.text.commandline.RequiredAndOptionalArguments;
import com.ibm.sdwb.build390.userinterface.text.commandline.arguments.*;
import com.ibm.sdwb.build390.utilities.BooleanAnd;
import com.ibm.sdwb.build390.utilities.BooleanExclusiveOr;
import com.ibm.sdwb.build390.utilities.BooleanInterface;

public class SetupCreate extends CommandLineProcess {

    public static final String PROCESSNAME = "CREATESETUP";

    private Replace replace = new Replace();

    private Editor  editor = new Editor();
    private DefaultEditor  defaultEditor = new DefaultEditor();

    public SetupCreate(LogEventProcessor tempLep, com.ibm.sdwb.build390.MBStatus tempStatus) {
        super(PROCESSNAME, tempLep, tempStatus);
    }
    public String getHelpDescription() {
        return getProcessTypeHandled()+ " command  creates setup  (client.ser file in /misc dir).";
    }

    public String getHelpExamples() {
        return "1.To "+getProcessTypeHandled()+" from a parmfile.\n"+
        getProcessTypeHandled()+" PARMFILE=<parmfile>\n\n"+
        "2.To "+getProcessTypeHandled()+" from a parmfile and replace the old setup file.\n"+
        getProcessTypeHandled()+" PARMFILE=<parmfile> REPLACE=<yes>\n\n"+
        "3.To "+getProcessTypeHandled()+" for library cmvc using the keywords.\n"+
        getProcessTypeHandled()+" LIBRARY=CMVC LIBRARYNAME=<libraryname>\n"+
        "       LIBRARYADDRESS=<libraryaddress> LIBRARYUSER=<libraryuser> \n"+
        "       LIBRARYPASSWORDAUTHENTICATION=<yes/no>\n"+
        "       PROCESSSERVERPORT=<processserverport> CMVCPORT=<cmvcport> \n"+
        "       BUILDSERVERNAME=<mainframeaddress> BUILDSERVERPORT=<mainframeport> \n"+
        "       BUILDSERVERACCTINFO=<mainframeacctinfo> BUILDSERVERUSERID=<mainframeuserid>\n\n"+
        "4.To "+getProcessTypeHandled()+" for no library using the keywords.\n"+
        getProcessTypeHandled()+" LIBRARY=NOLIB LIBRARYNAME=<libraryname>\n"+
        "       BUILDSERVERNAME=<mainframeaddress> BUILDSERVERPORT=<mainframeport> \n"+
        "       BUILDSERVERACCTINFO=<mainframeacctinfo> BUILDSERVERUSERID=<mainframeuserid> \n\n"+
        "Note:\n"+
        "1.There are two ways in creating a setup.\n"+
        "      (a)All the required keywords are made available using a PARMFILE=<parmfile>.\n"+
        "      (b)All the required keywords are made available on the command line.\n\n"+
        "2.REPLACE   <YES/NO> ,if YES the old client.ser would be replaced.\n\n"+
        "3.PARMFILE=<parmfile>, is an optional way of creating setup. When available\n"+
        "       the <parmfile>  should contain all the required arguments.\n" +
        "       Refer B390Client Users Guide on how to create the parmfile.\n\n"; 

    }


    protected void setArgumentStructure(RequiredAndOptionalArguments argumentStructure) {
        SetupValidator validator  = new SetupValidator(getLEP(),getStatusHandler());

        argumentStructure.setRequiredPart(validator.getArgumentStructure().getRequiredPart());
        for (Iterator iter = validator.getArgumentStructure().getOptions().iterator();iter.hasNext();) {
            argumentStructure.addOption((BooleanInterface)iter.next());
        }
        argumentStructure.addOption(replace);
        argumentStructure.addOption(editor);
        argumentStructure.addOption(defaultEditor);

    }

    public void runProcess() throws com.ibm.sdwb.build390.MBBuildException{


        File serfile = new File(MBGlobals.Build390_path + "misc"+ java.io.File.separator + "client.ser");

        if (serfile.exists()) {
            if (!replace.getBooleanValue()) {
                throw new GeneralError("GeneralError  : Setup Client.ser file already exists \n" + 
                                       "                Please delete or rename the existing client.ser file.\n" +
                                       "            or  Please run CREATESETUP command with REPLACE=YES option\n" +
                                       "                to replace the client.ser file.");
            } else {
                getStatusHandler().updateStatus("OLD client.ser file was replaced.",false);
            }
        }


        SetupManager.newInstance();
        SetupManager.getSetupManager().getModificationManager().setValuesFromMap(MBClient.getCommandLineSettings().getSettings());

        SetupManager.getSetupManager().saveSetup();

        getLEP().LogPrimaryInfo("INFORMATION: ",SetupManager.getSetupManager().toString(),false);


        if (!SetupManager.getSetupManager().hasSetup()) {
            throw new SyntaxError("\nNo setup information found.!\n"+
                                  "One or more of the setup keywords are incomplete or not found.!\n"+
                                  "Note:\n"+
                                  "To create setup from the command line, type " + PROCESSNAME + " /help\n"+ 
                                  "Refer Client Users Guide for more details.");
        }
        getStatusHandler().updateStatus("client.ser setup file created successfully.",false);
    }
}
