package com.ibm.sdwb.build390.userinterface.graphic.panels.metadata;
/**********************************************************************/
/* SingleMetadataDisplay  class for the Build/390 client              */
/* Creates a single tab with a table.                                 */
/* This class knows what compositor to use(iethe data to use)         */
/**********************************************************************/
//02/11/2005 SDWB2363 Redesign Part chooser interface
/**********************************************************************/

import java.awt.Dimension;
import java.util.*;


public class MetadataTabbedTablePanel  extends CloseableTabbedTablePanel {

    private UITableModelCommunicator communicator;
    private String title;

    public MetadataTabbedTablePanel(String title,MetadataTableModelWrapperInterface model, UITableModelCommunicator communicator,Observer observer){
        super(model);
        this.communicator = communicator;
        this.title = title;
        setObserver(observer);
        communicator.setUI(this);
    }


    public MetadataTabbedTablePanel(String title,MetadataTableModelWrapperInterface model, UITableModelCommunicator communicator){
         this(title,model,communicator,null);
    }

    public String getTitle(){
        return title;

    }

    public void setUICommunicator(UITableModelCommunicator compositor){
        this.communicator = communicator;
    }

    public UITableModelCommunicator getUICommunicator(){
        return communicator;
    }

       
}


