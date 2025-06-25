package com.ibm.sdwb.build390;

/* The class to disable windows in response to actions, and
stop things in a logical, consistent way.  This is used in MBInternalFrame, and
MBModalStatusFrame. it provides the code to implement several methods from
MBAnimationStatusWindow
*/
// 04/27/99 errorHandling       change LogException parms & add new error types
// 03/07/2000 reworklog         rework log stuff using listeners

import javax.swing.JButton;
import java.util.*;
import java.awt.*;
import com.ibm.sdwb.build390.logprocess.*;
import com.ibm.sdwb.build390.userinterface.graphic.utilities.*;

public class MBStopHandler {
/* a list of MBStop objects currently executing in a given window. */
    private Vector runningItems = new Vector();
/* the object to disable & enable a window, this should be the only method it is used in */
    private ContainerEnablerDisabler guiAccessController = null;
/* a pointer to the cancel button of the parent window    */
    private JButton cancelButton = null;
/* the parent window this object is associated with */
    private MBAnimationStatusWindow parentFrame = null;
/* the window's animation object */
    private MBAnimation runningAnimation = null;
    private LogEventProcessor lep =null;
/* the window, cancel button, and animation must be passed in */
    public MBStopHandler(MBAnimationStatusWindow tempFrame, JButton tempButton, MBAnimation tempAnimation,LogEventProcessor lep) {
        guiAccessController = new ContainerEnablerDisabler((Container) tempFrame);
        cancelButton = tempButton;
        parentFrame = tempFrame;
        this.lep=lep;
        runningAnimation = tempAnimation;
    }

    public void setComponentsToLeaveAlone(Set tempComp) {
        guiAccessController.setUntouchableComponents(tempComp);
    }

    public Set getComponentsToLeaveAlone(){
        return guiAccessController.getUntouchableComponents();
    }

    /** add an MBStop object to runningItems, and if this is the first, disable the parent window,
    enable the cancel button, set the wait cursor, and start the animation
    */
    public synchronized void setRunningItem(MBStop tempRunningItem) {
        runningItems.addElement(tempRunningItem);
        if (runningItems.size() == 1) {
            guiAccessController.disableContainer();
            cancelButton.setEnabled(true);
            parentFrame.setWaitCursor();
            runningAnimation.start(); // start animation
        }
    }

    /*  happens when an action completes without the cancel button being pressed.
        resets the panel without clearing the status
    */
    public synchronized void clearRunningItem() {
        resetPanel(false);
    }

    /*  happens when an action is canceled by the cancel button.  stops all non null
        MBStop objects in the vector, then resets the panel, clearing the status
    */
    public synchronized void stopRunningItem() {
        for (int i = runningItems.size() - 1; i > -1; i--) {
            if (runningItems.elementAt(i) instanceof MBStop) {
                MBStop tempStop = (MBStop) runningItems.elementAt(i);
                try {
                    tempStop.stop();
                } catch (MBBuildException mbe) {
                    //MBUtilities.LogException( mbe);
                    lep.LogException( mbe);
                }
            }
            runningItems.removeElementAt(i);
        }
        resetPanel(true);
    }


    /** Disable the Stop button, reset the running items vector, stop the animation,
    * clear the wait cursor, conditionally clear the status, and finally enable the window
    * @param clearStatus boolean that indicates to clear the status when true.
    */
    private void resetPanel(boolean clearStatus) {
        cancelButton.setEnabled(false);
        runningItems = new Vector();
        try {
            runningAnimation.stop();  // stop progress animation
        } catch (MBBuildException mbe) {
            //MBUtilities.LogException(mbe);
            lep.LogException(mbe);
        }
        parentFrame.clearWaitCursor();
        if (clearStatus) {
            parentFrame.getStatusHandler().clearStatus();
        }
        guiAccessController.enableContainer();
    }
}
