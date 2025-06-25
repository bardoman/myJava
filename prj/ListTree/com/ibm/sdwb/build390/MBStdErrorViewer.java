package com.ibm.sdwb.build390;
/***************************************************************************/
/* Java MBStdErrorViewer class for the Build/390 client                           */
/*  Opens a panel displaying an updateable string */
// 04/27/99 errorHandling       change LogException parms & add new error types
// 03/07/2000 reworklog         changes to implement the log stuff using listeners
//01/17/2003 DEF.PTM2334:    Std Error view the size of postage stamp.
/***************************************************************************/
import java.util.*;
import java.io.*;
import java.awt.*;
import javax.swing.*;
import javax.swing.event.*;
import javax.swing.text.*;
import com.ibm.sdwb.build390.logprocess.*;

public class MBStdErrorViewer extends MBBasicInternalFrame {

    private JTextArea edit;
    private MBStdErrorViewer thisFrame = null;
    private JCheckBoxMenuItem menuCheck = null;
    private JScrollPane editScroller = null;

    public MBStdErrorViewer(LogEventProcessor lep) {
        super("Standard Error Output", null, true, true, lep);
        setMaximizable(true);
        thisFrame = this;
        edit = new JTextArea();
        edit.setEditable(false);
        edit.setFont(new Font("Monospaced",Font.PLAIN, 12));
        editScroller = new JScrollPane(edit);
        getContentPane().add("Center",editScroller);

        setVisible(true);
    }


    public void dispose(){
        if (isIcon()) {
            try {
                setIcon(false);
            } catch (java.beans.PropertyVetoException pve) {
                //MBUtilities.LogException("There was a problem restoring the StdError viewer", pve);
                lep.LogException("There was a problem restoring the StdError viewer", pve);
            }
        }
        if (menuCheck!=null) {
            if (menuCheck.isSelected()) {
                menuCheck.setSelected(false);
            }
        }
        super.dispose();
    }

    public void setCheckItem(JCheckBoxMenuItem tempCheck) {
        menuCheck = tempCheck;
    }

    public void setText(String text) {
        edit.setText(text);

       
        JScrollBar tempBar = editScroller.getVerticalScrollBar();
        tempBar.setValue(tempBar.getMaximum());
        
        setSize(new Dimension(500,300));//DEF.PTM2334:
    }
/*
    // make it fit in the frame
    public Dimension getPreferredSize() {
        Rectangle prt = MBClient.mbgui.getframe().getBounds();
        Dimension dim = super.getPreferredSize();
        if (dim.height > prt.height-50) dim.height = prt.height-50;
        if (dim.width  > prt.width)  dim.width  = prt.width;
        return dim;
    }
*/
}
