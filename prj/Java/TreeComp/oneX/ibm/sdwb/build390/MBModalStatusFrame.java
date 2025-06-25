package com.ibm.sdwb.build390;
/*********************************************************************/
/* MBModalStatusFrame class for the Build/390 client                  */
/*  Creates and manages the Driver Build Page                        */
/*********************************************************************/
// 01/11/99 Defect_210          disable cancel
// 05/17/99 UIFixes             fix cancel button color
// 05/20/99 #341                Ken, add wasCancelPressed method and support so we know whether to throw exceptions
// 03/07/2000 reworklog         changes to implement the logstuff using listeners
/*********************************************************************/
import java.awt.*;
import java.awt.event.*;
import java.util.Vector;
import javax.swing.*;

/** Just a basic internal frame */
public class MBModalStatusFrame extends MBModalFrame implements MBAnimationStatusWindow {

    private JButton btnCancel = new JButton("Cancel");
    private JTextField statusBar = new JTextField("Status");
    private MBAnimation runningAnimation;
    private MBStopHandler stopHandler = null;
    protected MBModalStatusFrame thisFrame;
    protected MBStatus status = null;
    private boolean cancelPressed = false;

    /**
    * constructor - Create a MBModalStatusFrame
    * @param String title
    * @param boolean iconifiable
    */
    public MBModalStatusFrame(String title, Component pFrame, com.ibm.sdwb.build390.logprocess.LogEventProcessor tempLep){
        super(title, pFrame, tempLep);
        thisFrame = this;
        runningAnimation = new MBAnimation(lep);
        status = new MBStatus(statusBar);
        stopHandler = new MBStopHandler(this, btnCancel, runningAnimation,lep);
        getContentPane().setLayout(new BorderLayout());
        setForeground(MBGuiConstants.ColorRegularText);
        setBackground(MBGuiConstants.ColorGeneralBackground);
        btnCancel.setForeground(MBGuiConstants.ColorCancelButton);
        btnCancel.addActionListener(new ActionListener() {
                                        public void actionPerformed(ActionEvent evt) {
                                            cancelPressed = true;
                                            btnCancel.setEnabled(false);             // disabel the stop button
                                            new Thread(new Runnable() {
                                                           public void run() {
                                                               stopHandler.stopRunningItem();
                                                           }
                                                       }).start();
                                        }
                                    });
    }

    public MBStatus getStatus() {
        return status;
    }

    public void handleUIEvent(com.ibm.sdwb.build390.userinterface.event.UserInterfaceEvent event){
    }

    public void addButtonPanel(JButton help, Vector actionButtons){
        JPanel buttonPanel = new JPanel();
        buttonPanel.setLayout(new BoxLayout(buttonPanel,BoxLayout.X_AXIS));
        Box bottomPanel = new  Box(BoxLayout.Y_AXIS);
        buttonPanel.add(Box.createHorizontalGlue());
        for (int i = 0; i < actionButtons.size(); i++) {
            buttonPanel.add((JButton) actionButtons.elementAt(i));
            buttonPanel.add(Box.createHorizontalGlue());
        }
        if (help != null) {
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
        getContentPane().add("South", bottomPanel);
    }

    /** Enable the Stop button.
    */
    public void setRunningItem(MBStop tempRunningItem) {
        cancelPressed = false;
        stopHandler.setRunningItem(tempRunningItem);
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

	public MBStatus getStatusHandler(){
		return status;
	}

	public com.ibm.sdwb.build390.logprocess.LogEventProcessor getLEP(){
		return getLogEventProcessor();
	}
}
