package com.ibm.sdwb.build390;
/*********************************************************************/
/* MBScheduleGeneralUpdate class for the Build/390 client            */
/*  Creates and manages the General scheduler update page            */
/*********************************************************************/
// Changes
// Date     Defect/Feature      Reason
// 09/30/99 pjs - Fix help link
//12/03/2002 SDWB-2019 Enhance the help system
/*********************************************************************/
import java.awt.*;
import java.awt.event.*;
import java.awt.event.KeyEvent;
import java.io.*;
import java.util.*;
import javax.swing.*;
import com.ibm.sdwb.build390.help.*;
import com.ibm.sdwb.build390.userinterface.graphic.MainInterface;

/** Create the driver build page */
public class MBScheduleGeneralUpdate extends MBModalStatusFrame {

    private JButton btHelp = new JButton("Help");
   	private JButton btOk = new JButton("Ok");
    private JLabel groupLabel = new JLabel("Group");
   	private JLabel eventLabel = new JLabel("Event");
   	private JTextField tfGroup = new JTextField();
   	private JTextField tfEvent = new JTextField();
   	protected GridBagLayout gridBag = new GridBagLayout();
   	protected JPanel centerPanel = new JPanel(gridBag);
   	protected int maxY = -1;

    /**
    * constructor - Create a MBScheduleGeneralUpdate
    * @param MBGUI gui
    */
    public MBScheduleGeneralUpdate(String dialogTitle, JInternalFrame pFrame) throws com.ibm.sdwb.build390.MBBuildException{
        super(dialogTitle, pFrame, null);
        createUI();
    }

    public void createUI() {
   		btHelp.setForeground(MBGuiConstants.ColorHelpButton);
        btHelp.addActionListener(new MBCancelableActionListener(thisFrame) {
            public void doAction(ActionEvent evt) {
                MBUtilities.ShowHelp("SPTSCHADD",HelpTopicID.SCHEDULEGENERALUPDATE_HELP);
        }} );

 		btOk.setForeground(MBGuiConstants.ColorActionButton);
        btOk.addActionListener(new MBCancelableActionListener(thisFrame) {
            public void doAction(ActionEvent evt) {
		}} );

		tfGroup.setBackground(MBGuiConstants.ColorFieldBackground);
		tfEvent.setBackground(MBGuiConstants.ColorFieldBackground);
   		GridBagConstraints c = new GridBagConstraints();
   		c.weighty = 1;
   		c.gridx = 1;
   		c.gridy = 1;
		c.anchor = GridBagConstraints.WEST;
		c.insets = new Insets(2,5,2,5);
		gridBag.setConstraints(groupLabel, c);
		centerPanel.add(groupLabel);
		c.gridy = 2;
		gridBag.setConstraints(eventLabel, c);
		centerPanel.add(eventLabel);
		c.gridy = 1;
		c.gridx = 2;
		c.fill = GridBagConstraints.HORIZONTAL;
		c.weightx = 1;
		c.gridwidth = GridBagConstraints.REMAINDER;
		gridBag.setConstraints(tfGroup, c);
		centerPanel.add(tfGroup);
		c.gridy = 2;
		maxY=2;
		gridBag.setConstraints(tfEvent, c);
		centerPanel.add(tfEvent);
		getContentPane().add("Center", centerPanel);
		Vector actionButtons = new Vector();
		actionButtons.addElement(btOk);
        addButtonPanel(btHelp, actionButtons);
		setVisible(true);

    }
}
