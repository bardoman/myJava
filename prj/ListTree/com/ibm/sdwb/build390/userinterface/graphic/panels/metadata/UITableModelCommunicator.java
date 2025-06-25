package com.ibm.sdwb.build390.userinterface.graphic.panels.metadata;
/*********************************************************************/
/* UITableModelCommunicator            class for the Build/390 client*/
/* Populates data, and has an instance of the UI object.             */
/* Updates the populates data on the UI.                             */
/*********************************************************************/
//02/11/2005 SDWB2397 Metadata Model After function (INTF2)
/*********************************************************************/
import java.util.*;

class UITableModelCommunicator {


    private Collection data = null;
    private boolean changed =false;
    private CloseableTabbedTablePanel panel =null;

    UITableModelCommunicator(){
        this.data = new Vector();
    }

    private Collection preUpdateDisplay(Collection input){
        return input;

    }

    protected void setUI(CloseableTabbedTablePanel panel){
        this.panel = panel;
    }


    public  void setData(Collection input){
        this.data = input;
    }

    public Collection getData(){
        return data;
    }   


    public void updateDisplay(Collection value) {
        setData(value);
        panel.update(preUpdateDisplay(getData()));
    }



}

