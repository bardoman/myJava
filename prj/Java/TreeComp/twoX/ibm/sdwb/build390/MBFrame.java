package com.ibm.sdwb.build390;
/************************************************************************/
/* Java MBFrame class                                                   */
/************************************************************************/
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import javax.swing.*;
// Changes
// 08/11/99 SaveSizesOnExit     save the sizes of the frames when the app is exited


/** <br>Provides a frame that exits when the system exit button is pressed.
* <br> Also serializes the postion of the frame on exit */
public class MBFrame extends JFrame implements WindowListener {

    // main window indicator
    private boolean mw_;

    /** <br>Provides a frame that exits when the system exit button is pressed.
    * @param title A string containing the title for the frame.
    * @param mainwindow A boolean that indicates that this is the applications
    * main window.
    */
    public MBFrame(String title, boolean mainwindow) {
        super(title);
        mw_ = mainwindow;
        setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
        addWindowListener(this);
    }

    /* Window Listener methods */
    public void windowClosed(WindowEvent e) {}

    public void windowDeiconified(WindowEvent e) {}

    public void windowIconified(WindowEvent e) {}

    public void windowActivated(WindowEvent e) {}

    public void windowDeactivated(WindowEvent e) {}

    public void windowOpened(WindowEvent e) {}

    /** If a non-main window, disposes of the window,
    * <br>otherwise, serializes the position of the window and exits the application
    * @param e The WINDOW_CLOSING event.
    */
    public void windowClosing(WindowEvent e) {
        // if this is the main application window, serialize and exit
        if (mw_) closeAll();

        // if not the main window, just dispose of it
        else {
            setVisible(false);
            dispose();
        }
    }

    /** Save the position of the main window and if on the DriverBuild page, delete the directory
    * for the current build if it exists but not .ser file has been created.
    * This way you don't end up with partially populated directories */
    public void closeAll() {
        new Thread(new Runnable() {
            public void run() {
                MBMsgBox quitQuestion = new MBMsgBox("Exit", "Do you wish to exit the program?",null, true);
                if (!quitQuestion.isAnswerYes()) {
                    return;
                }
                // 08/11/99 SaveSizesOnExit
                MBBasicInternalFrame.saveSizes();

                // serialize the point object of this window
                Point pt = new Point(getLocation());
                Dimension size = getSize();
                String fn = new String(MBGlobals.Build390_path+"misc"+File.separator+"mainpos.ser");
                try {
                    ObjectOutputStream oos = new ObjectOutputStream(
                    new FileOutputStream(fn));
                    oos.writeObject(pt);
                    oos.writeObject(size);
                } catch (IOException ioe) {
                    System.out.println("error saving window postion");
                }
                MBClient.exitApplication(MBConstants.EXITSUCCESS);
                dispose();
                // kill me
                //MBClient.exitApplication(MBConstants.EXITSUCCESS);
            }
        }).start();
    }

    /** The AllFilter class creates a list of all files. */
    class AllFilter implements FilenameFilter {
        public boolean accept(File dir, String name) {
            return new File(dir, name).exists();
        }
    }
}
