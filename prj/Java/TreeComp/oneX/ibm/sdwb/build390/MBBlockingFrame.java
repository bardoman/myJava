package com.ibm.sdwb.build390;
/***************************************************************************/
/* Java MBBlockingFrame class for the Build/390 client                           */
/*  Blocks on setVisible(true), but allows access to the parent window */
// 04/27/99 errorHandling       change LogException parms & add new error types
// 03/07/2000 reworklog         reworklog stuff using listeners
/***************************************************************************/
import java.awt.Rectangle;
import javax.swing.*;
import javax.swing.event.*;

public class MBBlockingFrame extends MBBasicInternalFrame {

    boolean parentFrameInitialized = false;


    /** Constructor - Builds the frame and listbox and populates the listbox.
    */

    public MBBlockingFrame(String title, boolean shrinkable, JInternalFrame pFrame, com.ibm.sdwb.build390.logprocess.LogEventProcessor lep) {
        super(title, pFrame, true, shrinkable, lep);
    }

    public void setVisible(boolean visible) {

        if (visible) {
            synchronized (this) {
                super.setVisible(visible);
                postVisibleInitialization();
                try {
                    wait();
                } catch (InterruptedException ie) {
                    //MBUtilities.LogException("An interruption occurred while blocked", ie);
                    lep.LogException("An interruption occurred while blocked", ie);
                }
            }
        } else {
            super.setVisible(visible);
            if (parentFrameInitialized) {
                getout();
            }
        }
    }


    public void dispose() {
        super.dispose();
        getout();
    }

    public void postVisibleInitialization(){
    }


    /** getout - disposes of the frame and contents and then removes the action listeners */
    private void getout() {
        synchronized (this) {
            notifyAll();
        }
    }
}
