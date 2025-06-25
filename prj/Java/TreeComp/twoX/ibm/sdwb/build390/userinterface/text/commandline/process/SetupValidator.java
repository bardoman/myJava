package com.ibm.sdwb.build390.userinterface.text.commandline.process;
import java.util.Iterator;
import java.util.Set;

import com.ibm.sdwb.build390.GeneralError;
import com.ibm.sdwb.build390.MBBuildException;
import com.ibm.sdwb.build390.MBClient;
import com.ibm.sdwb.build390.MBStatus;
import com.ibm.sdwb.build390.SyntaxError;
import com.ibm.sdwb.build390.logprocess.LogEventProcessor;
import com.ibm.sdwb.build390.user.SetupManager;
import com.ibm.sdwb.build390.userinterface.text.commandline.RequiredAndOptionalArguments;
import com.ibm.sdwb.build390.userinterface.text.commandline.arguments.*;
import com.ibm.sdwb.build390.utilities.BooleanAnd;
import com.ibm.sdwb.build390.utilities.BooleanInterface;
import com.ibm.sdwb.build390.utilities.BooleanOperation;
import com.ibm.sdwb.build390.utilities.BooleanOr;


public class SetupValidator  extends CommandLineProcess {

    private static Library library = new Library();
    private static LibraryName libraryName = new LibraryName();
    private static LibraryAddress  libraryAddress = new LibraryAddress();
    private static LibraryUser libraryUser = new LibraryUser();
    private static LibraryPasswordAuthentication libraryPasswordAuthentication = new LibraryPasswordAuthentication();
    private static ProcessServerPort processServerPort = new ProcessServerPort();
    private static CMVCPort cmvcPort = new CMVCPort();
    private static MainframeName mainframeAddress = new MainframeName();
    private static MainframePort mainframePort = new MainframePort();
    private static MainframeUserid mainframeUserid = new MainframeUserid();
    private static MainframeAccountInformation  mainframeAccountInformation = new MainframeAccountInformation();
    private static Editor  editor = new Editor();
    private static DefaultEditor  defaultEditor = new DefaultEditor();
    private static SelectLibraryName  selectLibraryName = new SelectLibraryName();
    private static SelectMainframeName  selectMainframeName = new SelectMainframeName();

    public SetupValidator(LogEventProcessor lep,MBStatus status) {
        super("SETUPVALIDATOR",lep,status);
    }

    protected void setArgumentStructure(RequiredAndOptionalArguments argumentStructure) {
        BooleanAnd keywordsAnd = new BooleanAnd();

        argumentStructure.setRequiredPart(keywordsAnd);

        BooleanAnd libraryAnd = new BooleanAnd();
        libraryAnd.addBooleanInterface(library);
        libraryAnd.addBooleanInterface(libraryName);
        if (!MBClient.getCommandLineSettings().getMode().isFakeLibrary()) {
            libraryAnd.addBooleanInterface(libraryAddress);
            libraryAnd.addBooleanInterface(processServerPort);
            libraryAnd.addBooleanInterface(libraryUser);
            libraryAnd.addBooleanInterface(libraryPasswordAuthentication);
            libraryAnd.addBooleanInterface(cmvcPort);
        }

        BooleanOr libraryOr = new BooleanOr();
        libraryOr.addBooleanInterface(libraryAnd);
        libraryOr.addBooleanInterface(selectLibraryName);

        BooleanAnd mainframeAnd = new BooleanAnd();
        mainframeAnd.addBooleanInterface(mainframeAddress);
        mainframeAnd.addBooleanInterface(mainframePort);
        mainframeAnd.addBooleanInterface(mainframeUserid);
        mainframeAnd.addBooleanInterface(mainframeAccountInformation);

        BooleanOr mainframeOr = new BooleanOr();
        mainframeOr.addBooleanInterface(mainframeAnd);
        mainframeOr.addBooleanInterface(selectMainframeName);

        keywordsAnd.addBooleanInterface(libraryOr);
        keywordsAnd.addBooleanInterface(mainframeOr);

    }

    private boolean inputAvailable() {
        if (getArgumentStructure().getRequiredPart() instanceof BooleanOperation) {
            Set oset = ((BooleanOperation)getArgumentStructure().getRequiredPart()).getOperandSet();
            boolean available =true;
            for (Iterator iter =oset.iterator();iter.hasNext();) {
                BooleanInterface binterface = (BooleanInterface)iter.next();
                available = available && binterface.inputAvailable();
            }
            return available;
        } else {
            return getArgumentStructure().getRequiredPart().inputAvailable();
        }

    }

    private boolean isValid() throws MBBuildException  {
        if (!getArgumentStructure().isSatisfied()) {
            throw new SyntaxError("The following problems were found \nwith the setup arguments:\n"+getArgumentStructure().getReasonNotSatisfied() +"\nPlease refer client users guide on valid setup keywords.");   
        }
        return true;

    }

    public void runProcess()throws com.ibm.sdwb.build390.MBBuildException {
        if (inputAvailable()) {
            if (isValid()) {
                SetupManager.getSetupManager().getModificationManager().setValuesFromMap(MBClient.getCommandLineSettings().getSettings());
                getLEP().LogPrimaryInfo("INFORMATION","=======================================\n",false);
                getLEP().LogPrimaryInfo("INFORMATION","Build390 is using the following setup :\n"+SetupManager.getSetupManager().toString(),false);
                getLEP().LogPrimaryInfo("INFORMATION","=======================================\n",false);
            }
        }
        return;
    }
}


