package com.ibm.sdwb.build390;
/************************************************************************/
/* JAVA Animation class for progress convas area                        */
/************************************************************************/
// Changes
// Date     Defect/Feature      Reason
// 04/01/99 MoveImages          move images directory
// 03/07/2000 reworklog         changes to implement the log stuff using listeners
/*********************************************************************/

import java.awt.event.*;
import java.awt.*;
import java.io.*;
import javax.swing.*;
import com.ibm.sdwb.build390.logprocess.*;
import com.ibm.sdwb.build390.userinterface.graphic.MainInterface;

/**
* Progress animation uses a canvas to display images while a command is executing
*/
public class MBAnimation extends JLabel implements ActionListener {

    static final int BOX_SIZE = 25;
    private Timer runner;
    private static ImageIcon piepics[] = null; //new ImageIcon[7];
    private boolean stopped = false;
    private int frame =0;

    private static String imgsrc[] = {"w1.gif", "w2.gif", "w3.gif", "w4.gif", "w5.gif", "w6.gif", "w7.gif"};
    private transient LogEventProcessor lep=null;

    /**
    * Create a MBAnimation
    */
    public MBAnimation(LogEventProcessor lep) {
        this.lep=lep;
        init();
    }

    /**
    * Load images for animation
    */
    public void init() {
        String methodName = new String("MBAnimation:init");
        // Get the animation images, set the first image as the current image
        //MBUtilities.Logit(MBConstants.DEBUG_DEV, methodName, "");
        lep.LogSecondaryInfo("Debug", methodName);
        // MoveImages
        String filePath = new String(MBGlobals.Build390_path+"images"+File.separator);
        synchronized(MainInterface.getInterfaceSingleton()) {
            if (piepics == null) {
                Image temppics[] = new Image[imgsrc.length];
                MediaTracker tracker = new MediaTracker(this);

                for (int i=0; i < imgsrc.length; i++) {
                    temppics[i] = Toolkit.getDefaultToolkit().getImage(filePath + imgsrc[i]);
                    tracker.addImage(temppics[i], i);
                    // only for debug
                    File file = new File(filePath + imgsrc[i]);
                    if (file.exists())
                        //MBUtilities.Logit(MBConstants.DEBUG_DEV, methodName + file.getPath(), "Image file full path");
                        lep.LogSecondaryInfo("Image file full path", methodName + file.getPath());
                    else
                        //MBUtilities.Logit(MBConstants.DEBUG_DEV, methodName, "Can't locate the image file");
                        lep.LogSecondaryInfo("Can't locate the image file", methodName );
                } // for

                try {
                    tracker.waitForAll();
                    if (tracker.isErrorAny())
                        //MBUtilities.Logit(MBConstants.DEBUG_DEV, methodName, "Error loading images");
                        lep.LogSecondaryInfo("Error loading images", methodName);
                } catch (InterruptedException e) {
                }
                tracker = null;

                piepics = new ImageIcon[7];
                for (int i = 0; i < temppics.length; i++) {
                    piepics[i] = new ImageIcon(temppics[i].getScaledInstance(25,25,Image.SCALE_SMOOTH));
                    temppics[i]=null;
                }
                temppics = null;
            }
        }
        setIcon(piepics[frame]);
        runner = new Timer(100, this);
        setVisible(true);
    }

    /**
    * Start an animation thread
    */
    public synchronized void start() {
        runner.start();
    }

    /**
    * Stop the animation thread
    */
    public synchronized void stop() throws com.ibm.sdwb.build390.MBBuildException {
        runner.stop();
        //MBUtilities.Logit(MBConstants.DEBUG_DEV, "MBAnimation:stop", "Debug");
        lep.LogSecondaryInfo("Debug", "MBAnimation:stop");
    }

    /**
    * Display 4 images forever until the command is completed and change images
    * every 100 miliseconds
    */
    public void actionPerformed(ActionEvent e) {
        // Infinite loop
        frame = (frame +1)%piepics.length;
        setIcon(piepics[frame]);
    }
	
private void readObject(java.io.ObjectInputStream in) throws IOException, ClassNotFoundException{
		in.defaultReadObject();
		lep=new  LogEventProcessor();
/*Ken 7/5/00 we should do this, but we'll add & test later.
		lep.addEventListener(MBClient.getGlobalLogDBListener());
		lep.addEventListener(MBClient.getGlobalLogFileListener());
*/		
	}
}

