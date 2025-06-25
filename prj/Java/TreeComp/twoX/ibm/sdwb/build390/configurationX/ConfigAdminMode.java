
package com.ibm.sdwb.build390.configuration;

import com.ibm.sdwb.build390.user.Mode;
import com.ibm.sdwb.build390.userinterface.graphic.actions.ActionBuilder;
import com.ibm.sdwb.build390.userinterface.graphic.actions.ActionConfigurer;
import com.ibm.sdwb.build390.userinterface.graphic.actions.UserInterfaceActionBarSupport;
import com.ibm.sdwb.build390.userinterface.text.utilities.CommandLineSettings;

public class ConfigAdminMode extends Mode implements UserInterfaceActionBarSupport {


    public int getId() {
        return CONFIG;
    }


    public ActionBuilder getMainActionBar(ActionConfigurer configurer) {
        return new com.ibm.sdwb.build390.userinterface.graphic.DevelopmentActionsBuilder(configurer);

    }

    public ActionBuilder getManagePageActionBar(ActionConfigurer configurer) {
        return new com.ibm.sdwb.build390.userinterface.graphic.panels.managereleases.ConfigurationAdminActionsBuilder(configurer);

    }

    public ActionBuilder getSetupActionBar(ActionConfigurer configurer){
        return new com.ibm.sdwb.build390.userinterface.graphic.panels.setup.DevelopmentSetupActionsBuilder(configurer);
    }

    public String toString(){
        return CommandLineSettings.CONFIGADMIN;
    }
}

