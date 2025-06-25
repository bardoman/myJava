package com.ibm.sdwb.build390;
/***************************************************************************/
/* Java MBModalFrame class for the Build/390 client                           */
/*  Builds a listbox, populates it and adds the action listeners specified */
//  04/14/99    Bugs        fix assorted and sundry hangs
// 04/27/99 errorHandling       change LogException parms & add new error types
// 04/27/99 Defect_296      make global modal frames work
// 05/02/99 globalModal     fix problems with menu enabling on global modal dialogs.
// 05/05/99 globalModal     fix more hangs with global metadata.
// 03/07/2000 reworklog     rework of logging process using listeners
/***************************************************************************/
import java.awt.Rectangle;
import java.awt.Component;
import javax.swing.*;
import javax.swing.event.*;
import java.util.Vector;
import com.ibm.sdwb.build390.logprocess.*;
import com.ibm.sdwb.build390.userinterface.graphic.MainInterface;

/** <br>The MBModalFrame displays a list of choices in a listbox and adds the correct action listener.
* Classes using this class must implement a listener for the OK button and another for the Quit button */
public class MBModalFrame extends MBBasicInternalFrame {

    boolean parentFrameInitialized = false;
    JInternalFrame[] frameArray = null;
    private Object modalLock = new Object();
    private Vector[] oldListeners = null;
    private boolean[] menuStatus = null;
    private int currentCloseOp = 0;


    /** Constructor - Builds the frame and listbox and populates the listbox.
    */

    public MBModalFrame(String title,Component pFrame, LogEventProcessor tempLep) {
        super(title, pFrame, true, false, tempLep);
    }

    public void setVisible(boolean visible) {
        if (visible) {
            synchronized (modalLock) {
                parentFrameInitialized = true;
                super.setVisible(visible);
                synchronized(MainInterface.getInterfaceSingleton()) {
                    if (parentFrame instanceof JInternalFrame) {
                        JInternalFrame parentAlias = (JInternalFrame) parentFrame;
                        currentCloseOp = parentAlias.getDefaultCloseOperation();
                        parentAlias.setDefaultCloseOperation(currentCloseOp);
                        parentAlias.addInternalFrameListener(this);
                    } else {
// Ken, 4/27/99 Added code to disable menu
                        if (menuStatus == null) {
                            JMenuBar menuBar = MainInterface.getInterfaceSingleton().getframe().getJMenuBar();
                            menuStatus = new boolean[menuBar.getMenuCount()];
                            for (int i = 0; i < menuStatus.length; i++) {
                                JMenu tempMenu = menuBar.getMenu(i);
                                menuStatus[i]=tempMenu.isEnabled();
                                tempMenu.setPopupMenuVisible(false);
                                tempMenu.setEnabled(false);
                            }
                        }
                        frameArray = ((JDesktopPane) MainInterface.getInterfaceSingleton().getframe().getLayeredPane()).getAllFrames();
                        oldListeners = new Vector[frameArray.length];
                        for (int i = 0; i < frameArray.length; i++) {
                            if (frameArray[i] instanceof MBBasicInternalFrame) {
                                oldListeners[i] =((MBBasicInternalFrame) frameArray[i]).getInternalFrameListenersVector();
                                ((MBBasicInternalFrame) frameArray[i]).clearInternalFrameListeners();
                                frameArray[i].addInternalFrameListener(this);
                            }
                        }
                    }
                }
                postVisibleInitialization();
                try {
                    modalLock.wait();
                } catch (InterruptedException ie) {
                    //MBUtilities.LogException("An interruption occurred while waiting on the modal lock", ie);
                    lep.LogException("An interruption occurred while waiting on the modal lock", ie);
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
        setVisible(false);
        super.dispose();
    }

    public void postVisibleInitialization(){
    }


    /** getout - disposes of the frame and contents and then removes the action listeners */
    private void getout() {
        synchronized(MainInterface.getInterfaceSingleton()) {
            if (parentFrame instanceof JInternalFrame) {
                JInternalFrame parentAlias = (JInternalFrame) parentFrame;
                parentAlias.removeInternalFrameListener(this);
                parentAlias.setDefaultCloseOperation(currentCloseOp);
                try {
                    parentAlias.setSelected(true);
                } catch (java.beans.PropertyVetoException pve) {
                    //MBUtilities.LogException("An error occurred while attempting to select the parent frame", pve);
                    lep.LogException("An error occurred while attempting to select the parent frame", pve);
                }
            } else {
                if (frameArray != null) {
                    for (int i = 0; i < frameArray.length; i++) {
                        if (frameArray[i] instanceof MBBasicInternalFrame) {
                            ((MBBasicInternalFrame) frameArray[i]).setInternalFrameListeners(oldListeners[i]);
                        } else {
//                            frameArray[i].removeInternalFrameListener(this);
                        }
                    }
                }
// Ken, 4/27/99  add code to reenable menu
                JMenuBar menuBar = MainInterface.getInterfaceSingleton().getframe().getJMenuBar();
                for (int i = 0; i < menuStatus.length; i++) {
                    menuBar.getMenu(i).setEnabled(menuStatus[i]);
                }
            }
        }
        parentFrameInitialized = false;
        synchronized (modalLock) {
            modalLock.notify();
        }
    }

    public void internalFrameActivated(InternalFrameEvent e) {
        if (e.getSource() != this) {
            try {
                setSelected(true);
            } catch (java.beans.PropertyVetoException pve) {
                //MBUtilities.LogException("An error occurred while attempting to select this frame", pve);
                lep.LogException("An error occurred while attempting to select this frame", pve);
            }
        }
        super.internalFrameActivated(e);
    }
}
