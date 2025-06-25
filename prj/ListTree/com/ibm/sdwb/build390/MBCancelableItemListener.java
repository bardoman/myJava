package com.ibm.sdwb.build390;

/* this class overrides ItemListener, so that the window you are working
in is disabled, and the cancel button enabled, everytime you perform an action,
until that action is completed.
*/


import java.awt.event.*;

abstract class MBCancelableItemListener implements ItemListener, MBStop{
/* the window the ItemListener is keyed from */
    MBAnimationStatusWindow parentWindow = null;
/* a self pointer */
    MBCancelableItemListener thisListener = null;
/* a variable accessable from your doAction method to tell if you have been stopped */
    protected boolean stopped = false;

/* takes the window it's being associated with as an argument, so it can modify things
based on it's state of execution */
    MBCancelableItemListener(MBAnimationStatusWindow temp){
        parentWindow = temp;
        thisListener = this;
    }

/*  the main method of the ItemListener interface
    we don't want to override it in the future, rather override doAction,
    so this is set to final.  The first thing it does is set itself as the running item,
    so the MBStop method associated with this object will be run in the event of  a cancel.
    This also disables the window so no more input (except cancel & help) is possible until
    completion of the action.   Since AWT is single threaded, this also spawns a new thread
    to handle the processing of the action.
*/


    public final void itemStateChanged(final ItemEvent e) {
        final boolean setRunning = !parentWindow.getCancelButtonStatus();
        if (setRunning){
            parentWindow.setRunningItem(thisListener);
        }
        new Thread(new Runnable() {
            public void run() {
                try {
                    handleStateChange(e);
// the finally clause ensures we have a working window, even in the event of an error.
                }finally {
                    if (setRunning){
                        parentWindow.clearRunningItem();
                    }
                    postChange();
                }
// reset stopped to false, so next time through it will run, and stop will work.
                stopped = false;
            }
        }).start();
    }

/* the method to override for whatever action you want to perform in response
to a click.
*/
    public abstract void handleStateChange(ItemEvent e);

    public void postChange(){
    }

/* a basic stop method, can be overridden, or doAction can just check the state of stopped
periodically
*/  public void stop(){
        stopped = true;
    }
}
