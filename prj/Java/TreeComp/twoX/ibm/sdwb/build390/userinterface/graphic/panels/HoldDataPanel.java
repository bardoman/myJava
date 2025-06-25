package com.ibm.sdwb.build390.userinterface.graphic.panels;
import java.awt.*;
import java.awt.event.*;
import java.awt.event.KeyEvent;
import java.io.*;
import com.ibm.sdwb.build390.*;
import java.util.*;
import javax.swing.*;
import com.ibm.sdwb.build390.logprocess.*;
import com.ibm.sdwb.build390.help.*;

/** HoldDataDialog */
public class HoldDataPanel extends MBInternalFrame {

//    private MBReleaseCombo releaseCombo;
//    private MBTrackCombo trackCombo;
    private JTextField releaseText = null;
    private JTextField trackText = null;
    private JButton btHelp = new JButton("Help");
    private JLabel Label1 = new JLabel("Library Release");
    private JLabel Label2 = new JLabel("Tracks");
    private JLabel Label41 = new JLabel("Hold Code");
    private JLabel dataLabel = new JLabel("Hold Data:");
    private JTextArea holdDataArea = new JTextArea();
    private JComboBox holdCodes = new JComboBox();
    private JButton bSubmit = new JButton("Submit");
    private MBBuild build = null;
    private String track = null;
    protected GridBagLayout gridBag = new GridBagLayout();
    protected JPanel centerPanel = new JPanel(gridBag);

    /**
    * constructor - Create a MBHoldDataDialog
    * @param MBGUI gui
    */
    public HoldDataPanel(MBBuild tempBuild, String tempTrack,LogEventProcessor lep) throws com.ibm.sdwb.build390.MBBuildException{
        super("Hold Data Dialog", true, lep);
        track = tempTrack;
        build = tempBuild;
        releaseText = new JTextField(build.getReleaseInformation().getLibraryName());
        trackText = new JTextField(track);
        initializeDialog();
    }

    public void initializeDialog() throws com.ibm.sdwb.build390.MBBuildException {
        lep.LogSecondaryInfo("Debug", "MBHoldDataDialog:initializeDialog:Entry");

        btHelp.setForeground(MBGuiConstants.ColorHelpButton);
        btHelp.addActionListener(new MBCancelableActionListener(thisFrame) {
                                     public void doAction(ActionEvent evt) {
                                         //MBUtilities.ShowHelp("Hold_data");
                                         MBUtilities.ShowHelp("SPTHLDDTA",HelpTopicID.APARHOLDDATADIALOG_HELP);
                                     }} );

        GridBagConstraints c = new GridBagConstraints();
        c.weighty = 1;
        c.gridx = 1;
        c.gridy = 1;
        c.anchor = GridBagConstraints.WEST;
        c.insets = new Insets(2,5,2,5);
        gridBag.setConstraints(Label1, c);
        centerPanel.add(Label1);
        c.gridy = 2;
        gridBag.setConstraints(Label2, c);
        centerPanel.add(Label2);
        c.gridy = 3;
        gridBag.setConstraints(Label41, c);
        centerPanel.add(Label41);
        c.gridy = 1;
        c.gridx = 2;
        c.fill = GridBagConstraints.HORIZONTAL;
        c.weightx = 1;
        c.gridwidth = GridBagConstraints.REMAINDER;
        gridBag.setConstraints(releaseText, c);
        centerPanel.add(releaseText);
        c.gridy = 2;
        gridBag.setConstraints(trackText, c);
        centerPanel.add(trackText);
        c.gridy = 3;
        gridBag.setConstraints(holdCodes, c);
        centerPanel.add(holdCodes);
        JPanel mainPanel = new JPanel(new BorderLayout());
        c.gridx = 0;
        c.gridy = 4;
        c.gridwidth = GridBagConstraints.REMAINDER;
        c.gridheight = GridBagConstraints.REMAINDER;
        c.weighty = 10;
        c.fill = GridBagConstraints.BOTH;
        gridBag.setConstraints(mainPanel, c);
        centerPanel.add(mainPanel);
        JScrollPane tempScroll = new JScrollPane(holdDataArea);
        mainPanel.add("North", dataLabel);
        mainPanel.add("Center", tempScroll);
        getContentPane().add("North", centerPanel);
        getContentPane().add("Center", mainPanel);
        holdCodes.addItem("A - Documentation");
        holdCodes.addItem("B - Enhancement");
        holdCodes.addItem("C - Dependency");
        holdCodes.addItem("D - Action");
        holdCodes.addItem("E - Comment");
        holdCodes.addItem("F - Engineering change or Special hardware conditions");
        holdCodes.addItem("G - UCLIN");
        holdCodes.addItem("H - Full Sysgen required");
        holdCodes.addItem("I - I/O gen required");
        holdCodes.addItem("K - ++DELETE statement");
        holdCodes.addItem("L - ++RENAME statement");
        holdCodes.addItem("M - ++MOVE   statement");
        holdCodes.addItem("N - MVSCP - Requires that the MVS configuration be run.");
        holdCodes.addItem("P - ++element - Part is to be deleted using ++element DELETE.");
        holdCodes.addItem("Q - Changes to an automated procedure");
        holdCodes.addItem("R - Changes to NLS message skeletons (requires post-apply utility)");
        holdCodes.addItem("U - 1-7 Prefix characters for PREFIX keyword on ++MACUPD");
        holdCodes.addItem("V - Specified part is included with the ASSEM keyword on ++MACUPD");
        holdCodes.addItem("X - Provides notification to MVS/XA users of extended recovery");
        Vector actionButtons = new Vector();
        actionButtons.addElement(bSubmit);
        addButtonPanel(btHelp, actionButtons);
        holdCodes.addActionListener(new MBCancelableActionListener(thisFrame) {
                                        public void doAction(ActionEvent evt) {
                                            holdDataArea.setText(getHoldData(((String) holdCodes.getSelectedItem()).substring(0,1), track));
                                        }
                                    });
        bSubmit.addActionListener(new MBCancelableActionListener(thisFrame) {
                                      public void doAction(ActionEvent evt) {
                                          File holdDataFile = new File(build.getBuildPath()+track+".hold");
                                          try {
                                              BufferedWriter holdOut = new BufferedWriter(new FileWriter(holdDataFile));
                                              BufferedReader source = new BufferedReader(new StringReader(holdDataArea.getText()));
                                              String line = null;
                                              while ((line = source.readLine()) != null) {
                                                  holdOut.write(line+MBConstants.NEWLINE);
                                              }
                                              source.close();
                                              holdOut.close();
                                              source = null;
                                              holdOut = null;
											  com.ibm.sdwb.build390.process.ProcessWrapperForSingleStep processWrapper = new com.ibm.sdwb.build390.process.ProcessWrapperForSingleStep(thisFrame);
											  com.ibm.sdwb.build390.process.steps.ManualHoldDataOperations updateHold = new com.ibm.sdwb.build390.process.steps.ManualHoldDataOperations(build, track, ((String) holdCodes.getSelectedItem()).substring(0,1), holdDataFile, processWrapper);
											  updateHold.doSet();
											  processWrapper.setStep(updateHold);
											  processWrapper.externalRun();
                                          } catch (IOException e) {
                                              lep.LogException("There was an error writing the "+holdDataFile +" file", e);
                                          } catch (MBBuildException e) {
                                              lep.LogException(e);
                                          }
                                          dispose();
                                      }} );
        setVisible(true);

        new MBCancelableActionListener(thisFrame) {
            public void doAction(ActionEvent evt) {
                holdDataArea.setText(getHoldData(((String) holdCodes.getSelectedItem()).substring(0,1), track));
            }
        }.actionPerformed(new ActionEvent(this, 0, ""));

        // restore default settings #defaults
        RestoreDefaults();
    }

private String getHoldData(String holdCode, String track) {
    File holdDataFile = new File(build.getBuildPath()+holdCode+track+".old");
        try {
            try {
				com.ibm.sdwb.build390.process.ProcessWrapperForSingleStep processWrapper = new com.ibm.sdwb.build390.process.ProcessWrapperForSingleStep(this);
				com.ibm.sdwb.build390.process.steps.ManualHoldDataOperations updateHold = new com.ibm.sdwb.build390.process.steps.ManualHoldDataOperations(build, track, holdCode, holdDataFile, processWrapper);
				updateHold.doGet();
				processWrapper.setStep(updateHold);
				processWrapper.externalRun();
            } catch (FtpError fe) {
                return "No Hold Data found";
            } catch (MBBuildException mbe) {
                lep.LogException(mbe);
                return "No Hold Data found";
            }
            BufferedReader holdSource = new BufferedReader(new FileReader(holdDataFile));
            StringBuffer fileContents = new StringBuffer();
            char[] buf = new char[2048];
            int bytesRead = 0;
            while ((bytesRead = holdSource.read(buf)) >= 0) {
                fileContents.append(buf, 0, bytesRead);
            }
            return fileContents.toString();
        } catch (IOException e) {
            lep.LogException("There was an error reading the hold file " + holdDataFile, e);

        }
        return null;
    }


    // restore default field settings if setup has not changed since save #defaults
    private void RestoreDefaults() {
        //MBUtilities.Logit(MBConstants.DEBUG_DEV, "MBHoldDataDialog:RestoreDefaults:Entry", "");
        //01/07/2000   ind.build.log
        lep.LogSecondaryInfo("Debug", "MBHoldDataDialog:RestoreDefaults:Entry");
    }
}
