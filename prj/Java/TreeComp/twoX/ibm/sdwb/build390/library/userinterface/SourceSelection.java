package com.ibm.sdwb.build390.library.userinterface;

import java.util.*;

import com.ibm.sdwb.build390.library.*;
import com.ibm.sdwb.build390.userinterface.event.*;
import com.ibm.sdwb.build390.userinterface.graphic.widgets.RequiredActionsCompletedInterface;

/**
 * Extended by panels that will return library specific 
 * information on where to get build source from, such as 
 * CMVC track info
 */
public abstract class SourceSelection extends javax.swing.JPanel implements com.ibm.sdwb.build390.userinterface.graphic.widgets.RequiredActionsCompletedInterface, UserInterfaceEventListener {
      private UserInterfaceListenerManager listenerManager = new UserInterfaceListenerManager();

      protected static final String SOURCEKEY = "SOURCEINFO";

    /**
     * Get the appropriate SourceInfo object for the specific panel 
     * that is being used.
     * 
     * @return 
     */
    public abstract SourceInfo getSourceInfo();

    /**
    * Set the appropriate SourceInfo object for the specific panel
    * that is being used.
    * 
    * @return 
    */
    public abstract void setSourceInfo(SourceInfo info);

    public abstract com.ibm.sdwb.build390.mainframe.ReleaseInformation getProjectChosen();

    public void fireProjectUpdated(){
        com.ibm.sdwb.build390.userinterface.event.build.ReleaseUpdateEvent rue = new com.ibm.sdwb.build390.userinterface.event.build.ReleaseUpdateEvent(this);
        rue.setReleaseInformation(getProjectChosen());
        fireEvent(rue);
    }

    public void fireEvent(UserInterfaceEvent event){
        listenerManager.fireEvent(event);
    }

    public void addUserInterfaceEventListener(UserInterfaceEventListener listener) {
        listenerManager.addUserInterfaceEventListener(listener);

    }


    public void handleUIEvent(UserInterfaceEvent tempEvent) {
        //rather than making this abstract, and making all subclasses with empty handleUIEvent method,
        //we go by creating an empty method in SourceSelection class and sub classes overrides it if wants to. 
    }


}
