package com.ibm.sdwb.build390;
/***************************************************************************/
/* Java BasicPanel class for the Build/390 client               */
/*  Builds a listbox, populates it and adds the action listeners specified */
/***************************************************************************/
// Changes
// Date     Defect/Feature      Reason
//12/03/2002 SDWB-2019 Enhance the help system
/*********************************************************************/
import java.awt.Rectangle;
import java.util.*;
import javax.swing.JPanel;
import javax.swing.JInternalFrame;
import java.awt.Cursor;
import java.awt.Container;
import java.awt.Component;
import javax.swing.*;
import javax.swing.event.*;
import com.ibm.sdwb.build390.logprocess.*;
import com.ibm.sdwb.build390.help.*;

/** <br>The BasicPanel overrides setVisible in a predictable & consistant way
*/
public class BasicPanel extends JPanel implements AncestorListener{

    private int waitCursor = 0;
    private boolean clearedCursor = false;
    private boolean disposeCalled = false;
    private Object cursorLock = new Object();
    private Vector listenerVector = new Vector();
    protected BasicPanel thisFrame;
    protected transient LogEventProcessor lep=null;
    private LogEventGUIListener savedGUILogger=null;
    private LogEventGUIListener LogGUIObject=null;
    public  String title = null;
	private Vector menuVector = null;
	protected JInternalFrame enclosingFrame = null;
    
	/** Constructor - Builds the frame and listbox and populates the listbox.
    */
    public BasicPanel(LogEventProcessor tempLep) {
		if (tempLep == null) {
			lep=new  LogEventProcessor();
		}else {
			lep = tempLep;
		}
        thisFrame = this;
        addAncestorListener(this);
		menuVector = new Vector();
        super.setVisible(false);
    }

    public void getHelp() {
        MBUtilities.ShowHelp("ToC",HelpTopicID.TABLE_OF_CONTENTS);
    }

    public void setWaitCursor() {
        synchronized(cursorLock) {
            waitCursor++;
            Cursor cursorToUse = new Cursor(Cursor.WAIT_CURSOR);
            setCursor(cursorToUse);
            recursiveCursorSet(this, cursorToUse);
        }
    }

    public void clearWaitCursor() {
        synchronized(cursorLock) {
            waitCursor--;
            if (waitCursor < 0) {
                waitCursor = 0;
            }
            if (waitCursor == 0) {
                Cursor cursorToUse = new Cursor(Cursor.DEFAULT_CURSOR);
                setCursor(cursorToUse);
                recursiveCursorSet(this, cursorToUse);
            }
        }
    }

	public Vector getMenuVector(){
		return menuVector;
	}

	public JInternalFrame getEnclosingFrame(){
		return enclosingFrame;
	}

    private void recursiveCursorSet(Container tempContainer, Cursor newCursor) {
        Component[] components = tempContainer.getComponents();
        for (int i = 0; i < components.length; i++) {
            components[i].setCursor(newCursor);
            try {
                if (Class.forName("javax.swing.AbstractButton").isInstance(components[i])) {
                    if (((AbstractButton) components[i]).getText().toUpperCase().equals("CANCEL")) {
                        components[i].setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
                    }
                    if (((AbstractButton) components[i]).getText().toUpperCase().equals("HELP")) {
                        components[i].setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
                    }
                }
                if (Class.forName("java.awt.Container").isInstance(components[i])) {
                    recursiveCursorSet((Container)components[i], newCursor);
                }
            } catch (ClassNotFoundException cnfe) {
                lep.LogException("Classes were missing, check your classpath", cnfe);
            }
        }
    }

    public void problemBox(final String title, final String data){
        new Thread(new Runnable() {
                       public void run() {
                           new MBMsgBox(title,data, enclosingFrame);
                       }
				   }).start();
    }


    private class FrameListener extends InternalFrameAdapter {
        public void internalFrameClosed(InternalFrameEvent e) {
            lep.removeEventListener(LogGUIObject);
            if (savedGUILogger!=null) {
                lep.addEventListener(savedGUILogger);
            }
        }

        public void internalFrameOpened(InternalFrameEvent e) {
            boolean foundIt = false;
            for (Iterator listenerIterator = lep.getEventListenerList().iterator(); listenerIterator.hasNext() & !foundIt; ) {
                Object listen = listenerIterator.next();
                if (listen instanceof LogEventGUIListener) {
                    savedGUILogger=(LogEventGUIListener)listen;
                    lep.removeEventListener(savedGUILogger);
                    foundIt = true;
                }
            }
            lep.addEventListener(getBasicGUIListener());
        }

    }

    public void ancestorAdded(AncestorEvent ae) {
        com.ibm.sdwb.build390.userinterface.graphic.utilities.GeneralUtilities.getParentInternalFrame((java.awt.Component) getParent()).addInternalFrameListener(new FrameListener());
    }

    public void ancestorMoved(AncestorEvent ae) {
    }

    public void ancestorRemoved(AncestorEvent ae) {
    }

    private LogEventGUIListener getBasicGUIListener() {
        if (LogGUIObject!=null) {
            return LogGUIObject;
        } else {
            LogGUIObject = new LogEventGUIListener(this);
            return LogGUIObject;
        }

    }
}
