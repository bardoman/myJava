package com.ibm.sdwb.build390.userinterface.graphic.actions;

public interface UserInterfaceActionBarSupport {

    public ActionBuilder getMainActionBar(ActionConfigurer configurer);

    public ActionBuilder getManagePageActionBar(ActionConfigurer configurer);

    public ActionBuilder getSetupActionBar(ActionConfigurer configurer);
  
}
