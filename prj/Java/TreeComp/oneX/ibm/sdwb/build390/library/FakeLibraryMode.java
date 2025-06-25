package com.ibm.sdwb.build390.library;

import java.util.HashSet;
import java.util.Set;

import com.ibm.sdwb.build390.user.Mode;
import com.ibm.sdwb.build390.userinterface.graphic.actions.ActionBuilder;
import com.ibm.sdwb.build390.userinterface.graphic.actions.ActionConfigurer;
import com.ibm.sdwb.build390.userinterface.graphic.actions.UserInterfaceActionBarSupport;
import com.ibm.sdwb.build390.userinterface.text.commandline.process.*;
import com.ibm.sdwb.build390.userinterface.text.utilities.CommandLineSettings;
import com.ibm.sdwb.build390.userinterface.text.utilities.CommandLineSupport;


public class FakeLibraryMode extends Mode implements  UserInterfaceActionBarSupport,CommandLineSupport{

    public int getId() {
        return NO_LIB;
    }

    public ActionBuilder getMainActionBar(ActionConfigurer configurer) {
        return new com.ibm.sdwb.build390.userinterface.graphic.FakeLibraryActionsBuilder(configurer);

    }

    public ActionBuilder getManagePageActionBar(ActionConfigurer configurer) {
        return new com.ibm.sdwb.build390.userinterface.graphic.panels.managereleases.BaseActionsBuilder(configurer);
    }

    public ActionBuilder getSetupActionBar(ActionConfigurer configurer){
        return new com.ibm.sdwb.build390.userinterface.graphic.panels.setup.FakeLibrarySetupActionsBuilder(configurer);
    }


    public Set getSupportedCommands() {
        Set commandsSupported = new HashSet();
        commandsSupported.add(GetBuildTypeList.PROCESSNAME);
        commandsSupported.add(MainframeDriverParameterCheck.PROCESSNAME);
        commandsSupported.add(CleanupBuilds.PROCESSNAME);
        commandsSupported.add(SetupCreate.PROCESSNAME);
        commandsSupported.add(MainframeShadowCreate.PROCESSNAME);
        commandsSupported.add(MainframeShadowDelete.PROCESSNAME);
        commandsSupported.add(MainframeDriverCreate.PROCESSNAME);
        commandsSupported.add(MainframeDriverDelete.PROCESSNAME);
        commandsSupported.add(MainframeDriverList.PROCESSNAME);
        commandsSupported.add(MainframeDriverPartlistFilteredByMetadata.PROCESSNAME);
        commandsSupported.add(MainframeMetadataFieldList.PROCESSNAME);
        commandsSupported.add(MainframeMetadataValidation.PROCESSNAME);
        commandsSupported.add(ListProcesses.PROCESSNAME);
        commandsSupported.add(LogRetrieve.PROCESSNAME);
        commandsSupported.add(MainframeDriverReport.PROCESSNAME);
        commandsSupported.add(MainframeReleaseList.PROCESSNAME);
        commandsSupported.add(MainframeServerConnectionTest.PROCESSNAME);
        commandsSupported.add(MainframeShadowReport.PROCESSNAME);
        commandsSupported.add(UserSourceDrivenBuild.PROCESSNAME);
        commandsSupported.add(ProcessInfo.PROCESSNAME);
        commandsSupported.add(RestartProcess.PROCESSNAME);
        return commandsSupported;
    }

    public String toString(){
        return CommandLineSettings.NOLIB;
    }
}
