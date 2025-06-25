package com.ibm.sdwb.build390.userinterface.graphic.widgets;
/*********************************************************************/
/* AnimationStatusPanel class for the Build/390 client                  */
/*  Creates and manages the Driver Build Page                        */
/*********************************************************************/
/*********************************************************************/
import java.awt.*;
import java.awt.event.*;
import java.util.Vector;
import javax.swing.*;
import com.ibm.sdwb.build390.*;
import com.ibm.sdwb.build390.logprocess.*;

/** Just a basic internal frame */
public class AnimationStatusPanel extends BasicPanel implements MBAnimationStatusWindow {

    protected JButton btnCancel = null;
    private JTextField statusBar = null;
    private MBAnimation runningAnimation=null;
    protected AnimationStatusPanel thisFrame=null;
    protected MBStatus status = null;
    private MBStopHandler stopHandler = null;
    private boolean cancelPressed = false;
    private MBInsetPanel buttonPanel = null;
    /**
    * constructor - Create a AnimationStatusPanel
    * @param String title
    * @param boolean iconifiable
    */
    public AnimationStatusPanel(com.ibm.sdwb.build390.logprocess.LogEventProcessor tempLep){
        super(tempLep);
        lep = tempLep;
        btnCancel = new JButton("Cancel");
        btnCancel.setForeground(MBGuiConstants.ColorCancelButton);
        statusBar = new JTextField("Status");
        thisFrame = this;
        status = new MBStatus(statusBar);
        setLayout(new BorderLayout());
        setForeground(MBGuiConstants.ColorRegularText);
        setBackground(MBGuiConstants.ColorGeneralBackground);
        runningAnimation = new MBAnimation(lep);
        stopHandler = new MBStopHandler(this, btnCancel, runningAnimation,lep);
        addAnimationAndStatus();
    }

    public MBStatus getStatus() {
        return status;
    }

    public void handleUIEvent(com.ibm.sdwb.build390.userinterface.event.UserInterfaceEvent event){
        throw new RuntimeException("couldn't handle in AnimationStatusPanel received event " + event.toString());
    }

    public void addButtonPanel(JButton help, Vector actionButtons){
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
    }

    private void addAnimationAndStatus(){
        buttonPanel = new MBInsetPanel(5,5,5,5);
        buttonPanel.setLayout(new BoxLayout(buttonPanel,BoxLayout.X_AXIS));
        Box bottomPanel = Box.createVerticalBox();
        bottomPanel.add(buttonPanel);
        bottomPanel.add(Box.createVerticalStrut(buttonPanel.getPreferredSize().height / 5));
        JPanel statLine = new JPanel(new BorderLayout());
        statLine.add("Center",statusBar);
        statLine.add("East", runningAnimation);
        bottomPanel.add(statLine);
        add("South", bottomPanel);
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
		return lep;
	}
}
