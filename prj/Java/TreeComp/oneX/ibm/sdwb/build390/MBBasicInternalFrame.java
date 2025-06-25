package com.ibm.sdwb.build390;
/***************************************************************************/
/* Java MBBasicInternalFrame class for the Build/390 client               */
/*  Builds a listbox, populates it and adds the action listeners specified */
/***************************************************************************/
// Changes
// Date     Defect/Feature      Reason
// 09/02/98 #1      	        resize reload dialog if to small
// 09/10/98 menubar             add menubar to replace buttons
// 02/16/99 jdk 1.2             setDefaultCloseOperation not working.
// 04/14/99 modalFrameBugs      modal frame hangs and bugs
// 04/27/99 errorHandling       change LogException parms & add new error types
// 05/06/99 closeQuestion       fix dispose so it asks a question before closing.
// 05/14/99 FixMinSize          set minimum window size
// 08/11/99 SaveSizesOnExit     save the sizes of the frames when the app is exited
// 09/30/99 pjs - Fix help link
// 01/14/2000 build.log.1M      changes to display a dialog at a particular loc. - used in MBClient to display dialog
// 01/14/2000 build.log.1M      build log > 1m  at the top of the frame ,location positions passed in a hashtable.
// 03/07/2000 reworklog         changes to rewrite the log stuff using listeners
// 03/29/2000 tieerrordialgs    changes to tie error dialogs
// 05/09/2000 fixMinSize 	keep things from dissappearing.
// 06/02/2000 titledisappearing the buildid didnt appear at the title .. in driverbuilds..because we need not override getTitle() method in this class.
//12/03/2002 SDWB-2019 Enhance the help system
//08/09/2007 TST3409 userbuild hang    
/*********************************************************************/
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.util.*;

import javax.swing.*;
import javax.swing.event.*;

import com.ibm.sdwb.build390.help.*;
import com.ibm.sdwb.build390.logprocess.*;
import com.ibm.sdwb.build390.userinterface.graphic.MainInterface;
import com.ibm.sdwb.build390.userinterface.graphic.fascade.CursorControl;

/** <br>The MBBasicInternalFrame overrides setVisible in a predictable & consistant way
*/
public class MBBasicInternalFrame extends JInternalFrame implements InternalFrameListener, CursorControl {

    protected Component parentFrame = null;
    private static Hashtable windowSizes = new Hashtable();
    private boolean initDone = false;
    private int waitCursor = 0;
    private boolean clearedCursor = false;
    private boolean disposeCalled = false;
    private Object cursorLock = new Object();
    private Vector listenerVector = new Vector();
    protected MBBasicInternalFrame thisFrame;
    private static String SIZESSAVEFILE = new String("misc" + java.io.File.separator + "winsizes.ser");
    private int saveHeight = 0;
    private int saveWidth = 0;
    private Dimension sz;  // FixMinSize
    private static Vector theseframes = null;
    //01/14/2000 build.log.1M
    private Hashtable locationHash = new Hashtable();
    private boolean isLocationPassed = false;
    protected transient LogEventProcessor lep=null;
    private LogEventGUIListener savedGUILogger=null;
    private LogEventGUIListener LogGUIObject=null;
    public  String title = null;
    public static final String CANCEL_STRING = "CANCEL";
    public static final String HELP_STRING = "HELP";

    private static Set SkipTheseClasses = new HashSet();
    static {
        SkipTheseClasses.add("com.ibm.sdwb.build390.MBMsgPanel");
        SkipTheseClasses.add("com.ibm.sdwb.build390.userinterface.graphic.panels.managereleases.NewShadowDialog");
        SkipTheseClasses.add("com.ibm.sdwb.build390.MBPw");
        SkipTheseClasses.add("com.ibm.sdwb.build390.TextEntryDialog");
    }
    /** Constructor - Builds the frame and listbox and populates the listbox.
    */

    public MBBasicInternalFrame(String title,Component pFrame, boolean closeable, boolean iconifiable, LogEventProcessor tempLep) {
        super(title, true, closeable, false, iconifiable);
        this.title=title;
        if (tempLep == null) {
            lep=new  LogEventProcessor();
            lep.addEventListener(MBClient.getGlobalLogFileListener());
        } else {
            lep = tempLep;
        }
        parentFrame = pFrame;
        thisFrame = this;

        if (listenerVector==null)
            listenerVector = new Vector();

        theseframes =  new Vector();


        // 08/11/99 SaveSizesOnExit
        theseframes.addElement(thisFrame);


        if (parentFrame == null) {
            MainInterface.getInterfaceSingleton().setWaitCursor();
        } else {
            if (parentFrame instanceof CursorControl) {
                ((CursorControl) parentFrame).setWaitCursor();
            }
        }
//        setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        addInternalFrameListener(this);
        JMenuBar mb = new JMenuBar();
        JMenu fileMenu = new JMenu("File");
        mb.add(fileMenu);
        JMenuItem exitItem = new JMenuItem("Exit");
        exitItem.addActionListener(new ActionListener() {
                                       public void actionPerformed(ActionEvent e) {
                                           dispose();
                                       }
                                   });
        fileMenu.add(exitItem);
        setJMenuBar(mb);
        super.setVisible(false);
    }

    public void getHelp() {
        //MBUtilities.ShowHelp("Table_of_Contents:");
        MBUtilities.ShowHelp("ToC",HelpTopicID.TABLE_OF_CONTENTS);
    }

    public static void saveSizes() {
        synchronized(theseframes) { /*TST1858 */
            // 08/11/99 SaveSizesOnExit
            for (int xx=0; xx<theseframes.size(); xx++) {
                MBBasicInternalFrame thisone = (MBBasicInternalFrame)theseframes.elementAt(xx);
                String frameClass = thisone.getClass().getName();
                if (!SkipTheseClasses.contains(frameClass)) {
                    windowSizes.put(thisone.getClass().getName(), thisone.getSize());
                }
            }
        }

        try {
            ObjectOutputStream oos = new ObjectOutputStream(
                                                           new FileOutputStream(MBGlobals.Build390_path+SIZESSAVEFILE));
            oos.writeObject(windowSizes);
            oos.close();
        } catch (IOException ioe) {
            System.out.println("error saving " + MBGlobals.Build390_path+SIZESSAVEFILE);
        }
    }

    public static void loadSizes() {
        if ((new File(MBGlobals.Build390_path+SIZESSAVEFILE)).exists()) {
            try {
                ObjectInputStream ois = new ObjectInputStream(
                                                             new FileInputStream(MBGlobals.Build390_path+SIZESSAVEFILE));
                windowSizes = (Hashtable)ois.readObject();
            } catch (IOException ioe) {
                System.out.println("error reading " + MBGlobals.Build390_path+SIZESSAVEFILE + " " + ioe);
            } catch (ClassNotFoundException e) {
                System.out.println("error reading " + MBGlobals.Build390_path+SIZESSAVEFILE + " " + e);
            }
        }
    }

    public void setWaitCursor() {
        synchronized(cursorLock) {
            waitCursor++;
            Cursor cursorToUse = new Cursor(Cursor.WAIT_CURSOR);
//            recursiveCursorSet(getContentPane(), cursorToUse);
//            getGlassPane().setCursor(cursorToUse);
//            recursiveCursorSet(getRootPane(), cursorToUse);
//            recursiveCursorSet(getLayeredPane(), cursorToUse);
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

    private void recursiveCursorSet(Container tempContainer, Cursor newCursor) {
        Component[] components = tempContainer.getComponents();
//	    tempContainer.setCursor(newCursor);
        for (int i = 0; i < components.length; i++) {
            components[i].setCursor(newCursor);
            try {
                if (Class.forName("javax.swing.AbstractButton").isInstance(components[i])) {
                    String buttonLabel = ((AbstractButton) components[i]).getText();
                    if (CANCEL_STRING.equalsIgnoreCase(buttonLabel)) {
                        components[i].setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
                    }
                    if (HELP_STRING.equalsIgnoreCase(buttonLabel)) {
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

    public synchronized void addInternalFrameListener(InternalFrameListener l) {
        if (listenerVector==null)
            listenerVector = new Vector();

        listenerVector.addElement(l);
        super.addInternalFrameListener(l);
    }

    public synchronized void removeInternalFrameListener(InternalFrameListener l) {
        if (listenerVector==null)
            listenerVector = new Vector();

        listenerVector.removeElement(l);
        super.removeInternalFrameListener(l);
    }

    public synchronized InternalFrameListener[] getInternalFrameListeners() {
        return super.getInternalFrameListeners();
    } 

    public synchronized Vector getInternalFrameListenersVector() {
        if (listenerVector==null)
            listenerVector = new Vector();

        return listenerVector;
    }


    public synchronized void clearInternalFrameListeners() {
        if (listenerVector==null)
            listenerVector = new Vector();

        for (int i = 0; i < listenerVector.size(); i++) {
            super.removeInternalFrameListener((InternalFrameListener) listenerVector.elementAt(i));
        }
        listenerVector = new Vector();
    }

    public synchronized void setInternalFrameListeners(Vector newListeners) {
        if (listenerVector==null)
            listenerVector = new Vector();

        for (int i = 0; i < listenerVector.size(); i++) {
            super.removeInternalFrameListener((InternalFrameListener) listenerVector.elementAt(i));
        }
        listenerVector = newListeners;
        for (int i = 0; i < listenerVector.size(); i++) {
            super.addInternalFrameListener((InternalFrameListener) listenerVector.elementAt(i));
        }
    }

    public void putSizeInfo(String hashKey, Dimension size) {
        windowSizes.put(getClass().getName()+hashKey, size);
    }

    public Dimension getSizeInfo(String hashKey) {
        return(Dimension) windowSizes.get(getClass().getName()+hashKey);
    }

    public void putGeneric(String hashKey, Object stuff) {
        windowSizes.put(getClass().getName()+hashKey, stuff);
    }

    public Object getGeneric(String hashKey) {
        return windowSizes.get(getClass().getName()+hashKey);
    }

    public static void putGenericStatic(String mapKey, Object stuff) {
        windowSizes.put(mapKey, stuff);
    }

    public static Object getGenericStatic(String mapKey) {
        return windowSizes.get(mapKey);
    }

// Ken, 5.6.99  make sure we want to close something.
    public void setClosed(boolean isClosed) throws java.beans.PropertyVetoException {
        if (disposeCalled) {
            super.setClosed(isClosed);
        } else {
            dispose();
        }
    }

    public void dispose() {
        dispose(false);
    }

    public void dispose(final boolean showDialog) {
//	    final MBBasicInternalFrame thisFrame = this;
        synchronized(theseframes) { /*TST1858 */

            // 08/11/99 SaveSizesOnExit
            theseframes.removeElement(this);
        }
        synchronized(this) {

            if (!disposeCalled) {
                disposeCalled = true;
                if (showDialog) {
                    if (((MBSaveableFrame) thisFrame).saveNeeded()) {
                        new Thread(new Runnable() {
                                       public void run() {
                                           MBMsgBox closeQuestion = new MBMsgBox("Close", "Do you wish to close the window?\nIf you do, any changes you made since your last save will be lost.",thisFrame, true);
                                           if (closeQuestion.isAnswerYes()) {
                                               thisFrame.realDispose();
                                           } else {
                                               disposeCalled = false;
                                           }
                                       }
                                   }).start();
                    } else {
                        realDispose();
                    }
                } else {
                    realDispose();
                }
            }
        }

    }


// ken 5/6/99   handle dispose stuff here.
    private void realDispose() {
        String frameClass = thisFrame.getClass().getName();
        if (!SkipTheseClasses.contains(frameClass)) {
            windowSizes.put(thisFrame.getClass().getName(), getSize());
        }
        if (!clearedCursor) {
            clearedCursor = true;
            if (parentFrame == null) {
                MainInterface.getInterfaceSingleton().clearWaitCursor();
            } else {
                if (parentFrame instanceof CursorControl) {
                    ((CursorControl) parentFrame).clearWaitCursor();
                }
            }
        }
        super.dispose();
    }

    public void problemBox(final String title, final String data) {
        problemBox(title,data, false);
    }

    public void problemBox(final String title, final String data,boolean waitToComplete) {

        Thread msgThread = new Thread(new Runnable() {
                                          public void run() {
                                              new MBMsgBox(title,data, thisFrame);
                                          }
                                      });
        msgThread.start();

        if (waitToComplete) {
            try {
                msgThread.join();
            } catch (InterruptedException irpe) {
            }
        }
    }


    //01/14/2000 build.log.1M - location passed in hashtable
    public void setVisible(boolean visible,Hashtable tempLocationHash) {
        locationHash = tempLocationHash;
        isLocationPassed=true;
        setVisible(true);

    }

    public void setVisible(boolean visible) {
        if (visible) {
            if (!initDone) {
                MainInterface.getInterfaceSingleton().addFrame(this);
                Dimension tempDim = (Dimension)windowSizes.get(getClass().getName());

                if (!SkipTheseClasses.contains(getClass().getName())) {
                    if (tempDim != null) {
// ken 11/15/99 put this check in so windows don't disappear on us						
// ken 5/8/00 add a pack to the else so it actually works.
                        if (tempDim.width > 30 & tempDim.height > 30) {
                            reshape(0,0,tempDim.width, tempDim.height);
                        } else {
                            pack();
                        }
                    } else {
                        pack();
                    }
                } else {
                    pack();
                }
                Rectangle abounds = getBounds();
                Rectangle bounds;

                int y = 0;
                int x = 0;
                int xpos=0;
                int ypos=0;
                if (parentFrame == null) {
                    bounds = MainInterface.getInterfaceSingleton().getframe().getBounds();
                    x = (bounds.width - abounds.width)/ 2;
                } else {
                    bounds = parentFrame.getBounds();
                    y = bounds.y;
                    x = bounds.x + (bounds.width - abounds.width)/ 2;
                }
                if (abounds.height < bounds.height) {
                    y = y + (bounds.height - abounds.height)/2;
                }

                Dimension mainSize = MainInterface.getInterfaceSingleton().getframe().getSize();
                if (y+getSize().height > mainSize.height) {
                    int yDiff = y+getSize().height - (mainSize.height-(MainInterface.getInterfaceSingleton().getframe().getInsets().top+MainInterface.getInterfaceSingleton().getframe().getInsets().bottom));
                    if (yDiff < y) {
                        y = y - yDiff;
                    } else {
                        y = 0;
                    }
                }

                if (x+getSize().width+getInsets().left+getInsets().right > mainSize.width-(MainInterface.getInterfaceSingleton().getframe().getInsets().left+MainInterface.getInterfaceSingleton().getframe().getInsets().right)) {
                    int xDiff = x+getSize().width - (mainSize.width-(MainInterface.getInterfaceSingleton().getframe().getInsets().left+MainInterface.getInterfaceSingleton().getframe().getInsets().right));
                    if (xDiff < x) {
                        x = x - xDiff;
                    } else {
                        x = 0;
                    }
                }

                if (x < 0) {
                    x = 0;
                }

// 01/14/2000 build.log.1M - selects the appropriate SetLocation ,
                /*if XPOS,YPOS are passed in hashtable setLocation(XPOS,YPOS) is selected.
                if only YPOS passed then setLocation(x,YPOS) is selected,if only XPOS passed then setLocation(XPOS,y) is selected
                if none are passed  setLocation(x,y)*/
                String tempPos = new String();
                if ((isLocationPassed)&&(!locationHash.isEmpty())) {
                    if ((tempPos = (String) locationHash.get("XPOS")) != null) {
                        xpos = Integer.parseInt(tempPos);
                    } else {
                        xpos =x;
                    }
                    if ((tempPos = (String) locationHash.get("YPOS")) != null) {
                        ypos = Integer.parseInt(tempPos);
                    } else {
                        ypos = y;
                    }
                    setLocation(xpos,ypos);
                } else {
                    setLocation(x,y);
                }

            }
            Dimension tempSize = getSize();

            synchronized(MainInterface.getInterfaceSingleton()) {
// ken 11/15/99 let's try getting rid of this and see if that lears up some of our errors.
/*
                toFront();
                try {
                    setSelected(true);
                } catch (java.beans.PropertyVetoException pve) {
                    MBUtilities.LogException("There was a problem selecting this frame", pve);
                }
*/
                super.setVisible(visible);
                if (!clearedCursor) {
                    clearedCursor = true;
                    if (parentFrame == null) {
                        MainInterface.getInterfaceSingleton().clearWaitCursor();
                    } else {
                        if (parentFrame instanceof CursorControl) {
                            ((CursorControl) parentFrame).clearWaitCursor();
                        }
                    }
                }
                if (!initDone) {
// ken 11/15/99 put this check in so windows don't disappear on us						
                    if (tempSize.height > 30 & tempSize.width > 30) {
                        setSize(tempSize);
                    }
                }
            }
            initDone = true;
        } else {
            synchronized(MainInterface.getInterfaceSingleton()) {
                super.setVisible(visible);
            }
        }

        sz = getPreferredSize(); // FixMinSize - save default size
        // make sure dialog is at least min size
        Dimension td = getSize();
        if (td.width < getMinimumSize().width) {
            Point pt = getLocation();
            if (getMinimumSize().width > 30 & td.height > 30) {
                reshape(pt.x, pt.y, getMinimumSize().width, td.height);
            }
        }
    }

    public LogEventProcessor getLogEventProcessor() {
        return lep;
    }

    // FixMinSize - Override this in classes when you want something other than the
    // the default minimum size.
    public Dimension getMinimumSize() {
        return sz;
    }
    //creation of GUI Listener
    private void createGUIListener() {
        if (LogGUIObject==null) {
            LogGUIObject = new LogEventGUIListener(this);
        }
    }
    // Global GUI Listener
    private LogEventGUIListener getBasicGlobalGUIListener() {
        if (LogGUIObject!=null) {
            return LogGUIObject;
        } else {
            createGUIListener();
            return LogGUIObject;
        }

    }

    public void internalFrameActivated(InternalFrameEvent e) {
    }

    public void internalFrameClosed(InternalFrameEvent e) {
        lep.removeEventListener(LogGUIObject);
        if (savedGUILogger!=null) {
            lep.addEventListener(savedGUILogger);
        }
        if (!disposeCalled) {
            dispose();
        }
    }

    public void internalFrameClosing(InternalFrameEvent e) {
    }

    public void internalFrameDeactivated(InternalFrameEvent e) {
    }

    public void internalFrameDeiconified(InternalFrameEvent e) {
    }

    public void internalFrameIconified(InternalFrameEvent e) {
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
        createGUIListener();
        lep.addEventListener(getBasicGlobalGUIListener());
    }

    private void readObject(java.io.ObjectInputStream in) throws java.io.IOException, ClassNotFoundException{
        in.defaultReadObject();
        lep=new  LogEventProcessor();
/*Ken 7/5/00 we should do this, but we'll add & test later.
        createGUIListener();
        lep.addEventListener(getBasicGlobalGUIListener());
        lep.addEventListener(MBClient.getGlobalLogFileListener());
*/      
    }
}

