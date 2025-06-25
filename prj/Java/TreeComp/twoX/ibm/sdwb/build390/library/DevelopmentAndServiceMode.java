package com.ibm.sdwb.build390.library;

import com.ibm.sdwb.build390.userinterface.graphic.actions.ActionBuilder;
import com.ibm.sdwb.build390.userinterface.graphic.actions.ActionConfigurer;
import java.util.Set;

public class DevelopmentAndServiceMode extends DevelopmentMode  {


    public ActionBuilder getMainActionBar(ActionConfigurer configurer) {
        return new com.ibm.sdwb.build390.userinterface.graphic.DevelopmentAndServiceActionsBuilder(configurer);

    }

    public ActionBuilder getManagePageActionBar(ActionConfigurer configurer) {
        return new com.ibm.sdwb.build390.userinterface.graphic.panels.managereleases.DevelopmentAndServiceActionsBuilder(configurer);

    }

    public ActionBuilder getSetupActionBar(ActionConfigurer configurer) {
        return new com.ibm.sdwb.build390.userinterface.graphic.panels.setup.DevelopmentAndServiceSetupActionsBuilder(configurer);
    }

    public Set getSupportedCommands() {
        Set commandsSupported = super.getSupportedCommands();
        return commandsSupported;
    }

    public String toString() {
        return "development and service";
    }
}

