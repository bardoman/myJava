package com.ibm.sdwb.build390.userinterface.graphic.widgets;

/* this class overrides Action, so that the window you are working
in is disabled, and the cancel button enabled, everytime you perform an action,
until that action is completed.
*/

import javax.swing.AbstractAction;
import java.awt.event.ActionEvent;
import com.ibm.sdwb.build390.MBUtilities;



public class HelpAction extends AbstractAction{
	private String location = null;
	private String oldLocation = null;

/* takes the window it's being associated with as an argument, so it can modify things
based on it's state of execution */
    public HelpAction(String tempOldLocation, String tempLocation){
		super("Help");
		location = tempLocation;
		oldLocation = tempOldLocation;
    }

/*  the main method of the ActionListener interface
    we don't want to override it in the future, rather override doAction,
    so this is set to final.  The first thing it does is set itself as the running item,
    so the MBStop method associated with this object will be run in the event of  a cancel.
    This also disables the window so no more input (except cancel & help) is possible until
    completion of the action.   Since AWT is single threaded, this also spawns a new thread
    to handle the processing of the action.
*/
    public void actionPerformed(ActionEvent e) {
		MBUtilities.ShowHelp(oldLocation,location);
    }
}
