package com.ibm.sdwb.build390.userinterface.graphic.widgets;

/* this class overrides Action, so that the window you are working
in is disabled, and the cancel button enabled, everytime you perform an action,
until that action is completed.
*/

import java.awt.event.ActionEvent;
import com.ibm.sdwb.build390.process.AbstractProcess;
import com.ibm.sdwb.build390.MBStop;
import com.ibm.sdwb.build390.MBAnimationStatusWindow;

public class CancelableProcessAction extends CancelableAction {
    protected AbstractProcess theProcess = null;

    public CancelableProcessAction(String name, AbstractProcess tempProcess){
        super(name);
        theProcess = tempProcess;
    }

	public void setProcess(AbstractProcess tempProcess){
		theProcess = tempProcess;
	}

    public void doAction(ActionEvent e){
        theProcess.run();
    }

    public void postAction(){
    }

    public void stop() throws com.ibm.sdwb.build390.MBBuildException{
        theProcess.haltProcess();
        //TST1474. this is a hack to reset stopped=false in AbstractProcess. 
        theProcess.prepareRestart(0,0,theProcess.getUserCommunicationInterface());
    }
}
