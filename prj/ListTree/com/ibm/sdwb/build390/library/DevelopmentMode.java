package com.ibm.sdwb.build390.library;

import com.ibm.sdwb.build390.user.Mode;
import com.ibm.sdwb.build390.userinterface.graphic.actions.ActionBuilder;
import com.ibm.sdwb.build390.userinterface.graphic.actions.ActionConfigurer;
import com.ibm.sdwb.build390.userinterface.graphic.actions.UserInterfaceActionBarSupport;
import com.ibm.sdwb.build390.userinterface.text.commandline.process.*;
import com.ibm.sdwb.build390.userinterface.text.utilities.*;

import java.util.HashSet;
import java.util.Set;

public class DevelopmentMode extends Mode implements UserInterfaceActionBarSupport,CommandLineSupport {


    public ActionBuilder getMainActionBar(ActionConfigurer configurer) {
        return new com.ibm.sdwb.build390.userinterface.graphic.DevelopmentActionsBuilder(configurer);

    }

    public ActionBuilder getManagePageActionBar(ActionConfigurer configurer) {
        return new com.ibm.sdwb.build390.userinterface.graphic.panels.managereleases.DevelopmentActionsBuilder(configurer);

    }

    public ActionBuilder getSetupActionBar(ActionConfigurer configurer) {
        return new com.ibm.sdwb.build390.userinterface.graphic.panels.setup.DevelopmentSetupActionsBuilder(configurer);
    }

    public Set getSupportedCommands() {
        Set commandsSupported = new HashSet();
        commandsSupported.add(SetupCreate.PROCESSNAME);
        commandsSupported.add(MainframeShadowDelete.PROCESSNAME);
        commandsSupported.add(MainframeDriverDelete.PROCESSNAME);
        commandsSupported.add(MainframeDriverList.PROCESSNAME);
        commandsSupported.add(MainframeDriverParameterCheck.PROCESSNAME);
        commandsSupported.add(MainframeDriverPartlistFilteredByMetadata.PROCESSNAME);
        commandsSupported.add(MainframeDriverReport.PROCESSNAME);
        commandsSupported.add(MainframeMetadataFieldList.PROCESSNAME);
        commandsSupported.add(MainframeReleaseList.PROCESSNAME);
        commandsSupported.add(MainframeServerConnectionTest.PROCESSNAME);
        commandsSupported.add(MainframeSmodReport.PROCESSNAME);
        commandsSupported.add(CleanupBuilds.PROCESSNAME);
        commandsSupported.add(MainframeShadowReport.PROCESSNAME);
        commandsSupported.add(MainframeShadowCreate.PROCESSNAME);
        commandsSupported.add(MainframeDriverCreate.PROCESSNAME);
        commandsSupported.add(LibraryDrivenBuild.PROCESSNAME);
        commandsSupported.add(GetBuildTypeList.PROCESSNAME);
        commandsSupported.add(LogRetrieve.PROCESSNAME);
        commandsSupported.add(UserModBuild.PROCESSNAME);
        commandsSupported.add(UserSourceDrivenBuild.PROCESSNAME);
        commandsSupported.add(ListProcesses.PROCESSNAME);
        commandsSupported.add(ProcessInfo.PROCESSNAME);
        commandsSupported.add(RestartProcess.PROCESSNAME);
        commandsSupported.add(MainframeMetadataValidation.PROCESSNAME);
        commandsSupported.add(MainframeListFMIDsForLibraryRelease.PROCESSNAME);
        if (com.ibm.sdwb.build390.MBClient.getCommandLineSettings().isSwitchSet(CommandLineSettings.KUNGFOOMONKEY)) {
            commandsSupported.add(MainframeUNCHECKEDSocketConnection.PROCESSNAME);
        }

        return commandsSupported;
    }

    public String toString() {
        return "development";
    }
}

