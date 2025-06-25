package com.ibm.sdwb.build390.userinterface.graphic.widgets;

/* this class overrides Action, so that the window you are working
in is disabled, and the cancel button enabled, everytime you perform an action,
until that action is completed.
*/

import com.ibm.sdwb.build390.MBStop;
import com.ibm.sdwb.build390.MBAnimationStatusWindow;
import com.ibm.sdwb.build390.process.AbstractProcess;

public class CancelableProcess implements MBStop, Runnable{
    protected MBAnimationStatusWindow parentWindow = null;
	protected AbstractProcess theProcess = null;
	boolean setThisAsRunningItem = false;

/* takes the window it's being associated with as an argument, so it can modify things
based on it's state of execution */
    public CancelableProcess(AbstractProcess tempProcess, MBAnimationStatusWindow temp){
        parentWindow = temp;
		theProcess = tempProcess; 
    }

    public void runTheProcess() {
        setThisAsRunningItem = !parentWindow.getCancelButtonStatus();
        if (setThisAsRunningItem){
            parentWindow.setRunningItem(this);
        }
        new Thread(this).start();
    }

	public void run(){
		try {
			theProcess.run();
// the finally clause ensures we have a working window, even in the event of an error.
		}finally {
			if (setThisAsRunningItem){
				parentWindow.clearRunningItem();
			}
			postAction();
		}
	}

    public void postAction(){
    }

	public void stop() throws com.ibm.sdwb.build390.MBBuildException{
		theProcess.haltProcess();
	}
}
