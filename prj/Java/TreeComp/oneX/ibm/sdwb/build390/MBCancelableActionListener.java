package com.ibm.sdwb.build390;

/* this class overrides ActionListener, so that the window you are working
in is disabled, and the cancel button enabled, everytime you perform an action,
until that action is completed.
*/

// 6/2/99   Ken     handle help processing alongside other commands.

import java.awt.event.*;

public abstract class MBCancelableActionListener implements ActionListener, MBStop{
/* the window the actionlistener is keyed from */
    MBAnimationStatusWindow parentWindow = null;
/* a self pointer */
    MBCancelableActionListener thisListener = null;
/* a variable accessable from your doAction method to tell if you have been stopped */
    protected boolean stopped = false;

/* takes the window it's being associated with as an argument, so it can modify things
based on it's state of execution */
    public MBCancelableActionListener(MBAnimationStatusWindow temp){
        parentWindow = temp;
        thisListener = this;
    }

/*  the main method of the ActionListener interface
    we don't want to override it in the future, rather override doAction,
    so this is set to final.  The first thing it does is set itself as the running item,
    so the MBStop method associated with this object will be run in the event of  a cancel.
    This also disables the window so no more input (except cancel & help) is possible until
    completion of the action.   Since AWT is single threaded, this also spawns a new thread
    to handle the processing of the action.
*/


    public final void actionPerformed(final ActionEvent e) {
        final boolean setRunning = !e.getActionCommand().toUpperCase().equals("HELP") | !parentWindow.getCancelButtonStatus();
        if (setRunning){
            parentWindow.setRunningItem(thisListener);
        }
        new Thread(new Runnable() {
            public void run() {
                try {
                    doAction(e);
// the finally clause ensures we have a working window, even in the event of an error.
                }finally {
                    if (setRunning){
                        parentWindow.clearRunningItem();
                    }
                    postAction();
                }
// reset stopped to false, so next time through it will run, and stop will work.
                stopped = false;
            }
        }).start();
    }

/* the method to override for whatever action you want to perform in response
to a click.
*/
    public abstract void doAction(ActionEvent e);

    public void postAction(){
    }

/* a basic stop method, can be overridden, or doAction can just check the state of stopped
periodically
*/  public void stop(){
        stopped = true;
    }
}
