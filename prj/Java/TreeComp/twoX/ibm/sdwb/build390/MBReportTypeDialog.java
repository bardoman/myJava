package com.ibm.sdwb.build390;
/*********************************************************************/
/* MBNewShadowDialog class for the Build/390 client                  */
/*  Creates and manages the Driver Build Page                        */
//12/03/2002 SDWB-2019 Enhance the help system
/*********************************************************************/
import java.awt.*;
import java.awt.List;
import java.awt.event.*;
import java.awt.event.KeyEvent;
import java.io.*;
import java.util.*;
import javax.swing.*;
import javax.swing.event.*;
import javax.swing.border.*;
import com.ibm.sdwb.build390.help.*;
import com.ibm.sdwb.build390.userinterface.graphic.MainInterface;

/** Create the driver build page */
public class MBReportTypeDialog extends MBModalFrame implements Serializable {

    private MBBuild     build = null;
    private Hashtable   cmdHash = new Hashtable();

    //{{DECLARE_CONTROLS
    private JLabel btLabel       = new JLabel("Select the type of driver report desired");
    private JButton btHelp       = new JButton("Help");
   	private JButton btOk         = new JButton("Ok");
//   	private JButton btCancel     = new JButton("Cancel");
	private ButtonGroup Group1   = new ButtonGroup();
	private JRadioButton rbBuildTypes  = new JRadioButton("Build Types",false);
	private JRadioButton rbFailures    = new JRadioButton("Failures", false);
	private JRadioButton rbStatus      = new JRadioButton("Full Driver Status", false);
	private JRadioButton rbUnbuilt     = new JRadioButton("Unbuilt Parts", false);
	private JRadioButton rbLocal       = new JRadioButton("Delta Parts Status", false);
	private JRadioButton rbApar        = new JRadioButton("APAR", false);
	private JRadioButton rbUsermod     = new JRadioButton("USERMOD", false);

   	private MBButtonPanel tempButt;
   	protected GridBagLayout gridBag = new GridBagLayout();
   	protected JPanel centerPanel  = new JPanel(gridBag);
   	protected JPanel tpanel = new JPanel();
   	protected JPanel bpanel = new JPanel();
    //}}

    /**
    * constructor - Create a MBReportTypeDialog
    * @param MBGUI gui
    */
    public MBReportTypeDialog(MBBuild bld, Hashtable inHash, MBInternalFrame pFrame) throws com.ibm.sdwb.build390.MBBuildException {
        super("Report Type", pFrame, null);
        cmdHash = inHash;
        build = bld;
        initializeDialog(build);
    }

    public void initializeDialog(MBBuild tempBuildParm) throws com.ibm.sdwb.build390.MBBuildException {
        build   = tempBuildParm;
   		getContentPane().setLayout(new BorderLayout());
		setForeground(MBGuiConstants.ColorRegularText);
		setBackground(MBGuiConstants.ColorGeneralBackground);

   		btHelp.setForeground(MBGuiConstants.ColorHelpButton);
        btHelp.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                try {
                    //MBUtilities.ShowHelp("Getting_reports_about");
                    MBUtilities.ShowHelp("HDRGRARAD",HelpTopicID.REPORTTYPEDIALOG_HELP);
                }finally {           }
        }} );
 		btOk.setForeground(MBGuiConstants.ColorActionButton);
        btOk.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                try {
                    String rtype = new String();
                    if (rbStatus.isSelected())rtype = "";
                    else if (rbBuildTypes.isSelected())rtype = "ONLY";
                    else if (rbFailures.isSelected())rtype = "FAIL";
                    else if (rbUnbuilt.isSelected())rtype = "UNBUILT";
                    else if (rbLocal.isSelected())rtype = "LOCAL";
                    else if (rbApar.isSelected())rtype = "APAR";
                    else if (rbUsermod.isSelected())rtype = "USERMOD";
                    else return;

                    if (rtype != null) {
                        cmdHash.put("REPORTTYPE", rtype);
                    }
                    dispose();

                }finally {  }
		}} );


		Vector actionButtons = new Vector();
		actionButtons.addElement(btOk);
		tempButt = new MBButtonPanel(btHelp,null ,actionButtons);
 		btLabel.setForeground(MBGuiConstants.ColorGroupHeading);

  		GridBagConstraints c = new GridBagConstraints();
   		c.weighty = 1;
   		c.weightx = 0;
   		c.gridx = 1;
   		c.gridy = 1;
		c.anchor = GridBagConstraints.WEST;
		c.insets = new Insets(2,5,2,0);
  		gridBag.setConstraints(btLabel, c);
   		centerPanel.add(btLabel);

   		c.gridy = 2;
   		gridBag.setConstraints(rbBuildTypes, c);
   		Group1.add(rbBuildTypes);
   		centerPanel.add(rbBuildTypes);
   		c.gridy = 3;
   		gridBag.setConstraints(rbFailures, c);
   		Group1.add(rbFailures);
   		centerPanel.add(rbFailures);
   		c.gridy = 4;
   		gridBag.setConstraints(rbStatus, c);
   		Group1.add(rbStatus);
   		centerPanel.add(rbStatus);
   		c.gridy = 5;
   		gridBag.setConstraints(rbUnbuilt, c);
   		Group1.add(rbUnbuilt);
   		centerPanel.add(rbUnbuilt);
   		c.gridy = 6;
   		gridBag.setConstraints(rbLocal, c);
   		Group1.add(rbLocal);
   		centerPanel.add(rbLocal);
   		c.gridy = 7;
   		gridBag.setConstraints(rbApar, c);
   		Group1.add(rbApar);
   		centerPanel.add(rbApar);
   		c.gridy = 8;
   		gridBag.setConstraints(rbUsermod, c);
   		Group1.add(rbUsermod);
   		centerPanel.add(rbUsermod);

        getContentPane().add("Center", centerPanel);
		getContentPane().add("South", tempButt);

		setVisible(true);
	}
}
