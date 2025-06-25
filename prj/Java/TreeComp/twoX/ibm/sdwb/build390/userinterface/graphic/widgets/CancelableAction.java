package com.ibm.sdwb.build390.userinterface.graphic.widgets;

/* this class overrides Action, so that the window you are working
in is disabled, and the cancel button enabled, everytime you perform an action,
until that action is completed.
*/

import javax.swing.AbstractAction;
import java.awt.event.ActionEvent;
import com.ibm.sdwb.build390.MBStop;
import com.ibm.sdwb.build390.MBAnimationStatusWindow;

public abstract class CancelableAction extends AbstractAction implements MBStop, Runnable {
    protected MBAnimationStatusWindow parentWindow = null;
    ActionEvent currentEvent = null;
    boolean setThisAsRunningItem = false;
    protected boolean stopped = false;

/* takes the window it's being associated with as an argument, so it can modify things
based on it's state of execution */
    public CancelableAction(String name){
        super(name);
    }

/*  the main method of the ActionListener interface
    we don't want to override it in the future, rather override doAction,
    so this is set to final.  The first thing it does is set itself as the running item,
    so the MBStop method associated with this object will be run in the event of  a cancel.
    This also disables the window so no more input (except cancel & help) is possible until
    completion of the action.   Since AWT is single threaded, this also spawns a new thread
    to handle the processing of the action.
*/
    public synchronized final void actionPerformed(final ActionEvent e) {
        currentEvent = e;
		parentWindow = com.ibm.sdwb.build390.userinterface.graphic.utilities.GeneralUtilities.getParentAnimationStatus((java.awt.Component) currentEvent.getSource());
		setThisAsRunningItem = !currentEvent.getActionCommand().toUpperCase().equals("HELP") | !parentWindow.getCancelButtonStatus();
		if (setThisAsRunningItem) {
			parentWindow.setRunningItem(this);
		}
		new Thread(this).start();
    }

    public void run(){
        try {
            doAction(currentEvent);
// the finally clause ensures we have a working window, even in the event of an error.
        } finally {
            if (setThisAsRunningItem) {
                parentWindow.clearRunningItem();
            }
            postAction();
            currentEvent = null;
			stopped = false;
        }
    }

/* the method to override for whatever action you want to perform in response
to a click.
*/
    public abstract void doAction(ActionEvent e);

    public void postAction(){
    }

/* a basic stop method, can be overridden, or doAction can just check the state of stopped
periodically
*/  public void stop()  throws com.ibm.sdwb.build390.MBBuildException{
        stopped = true;
    }

}
