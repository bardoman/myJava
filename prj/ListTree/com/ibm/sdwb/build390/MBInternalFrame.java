package com.ibm.sdwb.build390;
/*********************************************************************/
/* MBInternalFrame class for the Build/390 client                  */
/*  Creates and manages the Driver Build Page                        */
/*********************************************************************/
// 01/11/99 Defect_210          disable cancel
// 04/27/99 Defect_295          Fix window selection
// 05/17/99 UIFixes             fix cancel button color
// 05/20/99 #341                Ken, add wasCancelPressed method and support so we know whether to throw exceptions
// 03/07/2000 reworklog         changes to implement the log stuff using listeners
/*********************************************************************/
import java.awt.*;
import java.awt.event.*;
import java.util.Set;
import java.util.Vector;

import javax.swing.*;

/** Just a basic internal frame */
public class MBInternalFrame extends MBBasicInternalFrame implements MBAnimationStatusWindow {

    protected JButton btnCancel = null;
    private JTextField statusBar = null;
    private MBAnimation runningAnimation=null;
    protected MBInternalFrame thisFrame=null;
    protected MBStatus status = null;
    private MBStopHandler stopHandler = null;
    private boolean cancelPressed = false;
    private JPanel centerPanel = null;


    /**
    * constructor - Create a MBInternalFrame
    * @param String title
    * @param boolean iconifiable
    */
    public MBInternalFrame(String title, boolean iconifiable, com.ibm.sdwb.build390.logprocess.LogEventProcessor tempLep) {
        super(title, null, true, iconifiable, tempLep);
        btnCancel = new JButton("Cancel");
        btnCancel.setForeground(MBGuiConstants.ColorCancelButton);
        statusBar = new JTextField("Status");
        thisFrame = this;
        status = new MBStatus(statusBar);
        getContentPane().setLayout(new BorderLayout());
        setForeground(MBGuiConstants.ColorRegularText);
        setBackground(MBGuiConstants.ColorGeneralBackground);
        btnCancel.addActionListener(new ActionListener() {
                                        public void actionPerformed(ActionEvent evt) {
                                            btnCancel.setEnabled(false);             // disabel the stop button
                                            cancelPressed = true;
                                            new Thread(new Runnable() {
                                                           public void run() {
                                                               stopHandler.stopRunningItem();
                                                           }
                                                       }).start();
                                        }
                                    });
        runningAnimation = new MBAnimation(lep);
        stopHandler = new MBStopHandler(this, btnCancel, runningAnimation,lep);
    }

    public void dispose() {
        super.dispose();
        btnCancel = null;
        statusBar = null;
        runningAnimation=null;
        thisFrame=null;
        status = null;
        System.gc();
    }

    public MBStatus getStatus() {
        return status;
    }

    public void handleUIEvent(com.ibm.sdwb.build390.userinterface.event.UserInterfaceEvent event) {
    }

    public void addButtonPanel(JButton help, Vector actionButtons) {
        MBInsetPanel buttonPanel = new MBInsetPanel(5,5,5,5);
        buttonPanel.setLayout(new BoxLayout(buttonPanel,BoxLayout.X_AXIS));
        Box bottomPanel = new  Box(BoxLayout.Y_AXIS);
        buttonPanel.add(Box.createHorizontalGlue());
        for (int i = 0; i < actionButtons.size(); i++) {
            ((JButton)actionButtons.elementAt(i)).setForeground(MBGuiConstants.ColorActionButton);
            buttonPanel.add((JButton) actionButtons.elementAt(i));
            buttonPanel.add(Box.createHorizontalGlue());
        }
        if (help != null) {
            help.setForeground(MBGuiConstants.ColorHelpButton);
            buttonPanel.add(help);
            buttonPanel.add(Box.createHorizontalGlue());
        }
        buttonPanel.add(btnCancel);
        buttonPanel.add(Box.createHorizontalGlue());
        btnCancel.setEnabled(false);
        bottomPanel.add(buttonPanel);
        bottomPanel.add(Box.createVerticalStrut(buttonPanel.getPreferredSize().height / 5));
        JPanel statLine = new JPanel(new BorderLayout());
        statLine.add("Center",statusBar);
        statLine.add("East", runningAnimation);
        bottomPanel.add(statLine);
        getContentPane().add(BorderLayout.SOUTH, bottomPanel);
    }

    /** Enable the Stop button.
    */
    public void setRunningItem(MBStop tempRunningItem) {
        cancelPressed = false;
        stopHandler.setRunningItem(tempRunningItem);
    }

    public void setNondisablableComponentClasses(java.util.Set tempNon) {
        stopHandler.setComponentsToLeaveAlone(tempNon);
    }

    public Set getNondisablableComponentClasses(){
        return stopHandler.getComponentsToLeaveAlone();
    }

    /** Enable or Disable the Stop button.
    * @param enable boolean that indicates to enable the button when true.
    */
    public void clearRunningItem() {
        stopHandler.clearRunningItem();
    }

    public boolean wasCancelPressed() {
        return cancelPressed;
    }

    public boolean getCancelButtonStatus() {
        return btnCancel.isEnabled();
    }

    public void setCancelButtonStatus(boolean enable) {
        btnCancel.setEnabled(enable);
    }

    public MBStatus getStatusHandler() {
        return status;
    }

    public com.ibm.sdwb.build390.logprocess.LogEventProcessor getLEP() {
        return lep;
    }
}
