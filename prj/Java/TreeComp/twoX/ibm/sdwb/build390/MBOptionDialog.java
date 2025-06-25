package com.ibm.sdwb.build390;
/*********************************************************************/
/* MBOptionDialog class for the Build/390 client                     */
/*  Creates and manages the Build Options                            */
/*********************************************************************/
// Changes
// Date     Defect/Feature      Reason
// 01/07/2000 ind.build.log      changes for logging in build details into a individual build log file for each build
// 01/17/2000 scroll pane        adding scroll pane to build option window in the driver build dialog
// 01/24/2000 scroll pane-userbuild adding scrollpane to build option window in userbuild dialog also
// 03/07/2000 reworklog          reworklog stuff using listeners
// 03/29/2000 223fixMove		move fixes from 223 to 23
// 06/13/2000 Ken				change from using radiobuttons to combo box
// Thulasi:11/15/00:Feature		Provided an option to save listings to a data set. Added a checkbox and a text field to the
//								options section with respect to that if fasttrack is selected.
// 12/14/2000 sdwb1210 			add support for failed listings to a file system, add drop down for save listings
// 12/20/2000 pjs 				show all overrides in one window - rewrote class
//03/26/2001 #Def348:           Allow revist of option dialog to edit fields
//05/22/2001 #TST0438:          Selecting OK with incomplete data dumps the user out of this dialog
//01/02/2002 #Feature.SDWB-1300 add "Generate Debug Files" 
//03/19/2002 #Def.INT0767:     EXTRACHK option not supported
//12/03/2002 SDWB-2019 Enhance the help system
//12/23/2002 #Def.SDWB1986: Can't use "Save Listings To A Dataset" on a fastrack build
//05/27/2003 #DEF.INT1192: Error when displaying OPTIONS on APAR driver build
//05/27/2003 #DEF.INT1121: Extract path obsolete
//03/02/2004 PTM3367             Auto purge successful jobs, as it completes.
/*********************************************************************/

import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.util.*;
import javax.swing.*;
import com.ibm.sdwb.build390.logprocess.*;
import com.ibm.sdwb.build390.help.*;
import com.ibm.sdwb.build390.mainframe.parser.DriverReportParser;
import com.ibm.sdwb.build390.userinterface.graphic.widgets.*;
import com.ibm.sdwb.build390.utilities.BinarySettingUtilities;
import com.ibm.sdwb.build390.info.*;


// Delay spin button for Terminate Criteria due to dependency on symantec
// for now, only temporary, use a Choice.  1/20/98, chris
//import symantec.itools.awt.util.spinner.NumericSpinner;
public class MBOptionDialog extends MBModalStatusFrame implements MBSaveableFrame {

    private JCheckBox cbCompileOption;
    private JComboBox termLevel;
    private JComboBox buildSettingCombo;
    private JComboBox ListingOptionsCombo;
    private JCheckBox cbSkipDCheck;
    private JCheckBox cbRunScan;
    private JCheckBox cbDryRun;
    private JCheckBox cbPurgeOutput;
    private JCheckBox cbTransObjects;
    private JCheckBox cbMaclibObjects; // pjs_maclibs
    private JButton bEditMacs;         // pjs_maclibs
    private JButton bAddMacs;          // pjs_maclibs
    private JButton bDelMacs;          // pjs_maclibs
    private JComboBox MaclibList;      // pjs_maclibs
    private JComboBox transObjectType;
    private JCheckBox cbMetaData;
    private JCheckBox cbHaltOnShadCheckWarnings;
    private JCheckBox cbAutoPurgeSuccessfulJobs;

    private JMenuItem bUndo;
    private JButton bOK;
    private JMenuItem bPhaseOverrides;
    private JPanel panel1;
    private MBButtonPanel buttonPanel;

    private JTextField partSourcePath = null;
    private JTextField transObjects = null;
    private JRadioButton buildGroupCheckbox;
    private JRadioButton sourceGroupCheckbox;
    private Listener cbListener = new Listener(); // check box listener
    private static Vector englishObjectTypes = new Vector();
    private static Vector MVSObjectTypes = new Vector();
    private static Vector fasttrackEnglishObjectTypes = new Vector();
    private static Vector fasttrackMVSObjectTypes = new Vector();

    private String DBMACLIBS_RELEASE = "MACLIBS_"; // pjs_maclibs
    public StringBuffer mdata = new StringBuffer(); // pjs_maclibs

    private static final String buildAll = "Unconditionally build all parts in list and all dependent parts";
    private static final String autobuildYes = "Build all unbuilt parts in list and all dependent parts";
    private static final String buildManual = "Build all unbuilt parts in list and parts with explicit dependencies";
    private static final String autobuildNo = "Build only unbuilt parts";
    private static final String superBuildAll = "Unconditionally build everything in the driver and partlist";

    private static final String SaveNoListings = "Do not save any listings"; // listgen=no
    private static final String SaveFailedListings = "Save failed listings"; // listgen=fail
    private static final String SaveGoodListings = "Save good listings";     // listgen=yes
    private static final String SaveAllListings = "Save all listings";       // listgen=all

    static {
        englishObjectTypes.addElement("Objects");
        englishObjectTypes.addElement("Prelinks");
        englishObjectTypes.addElement("Link Edits");
        englishObjectTypes.addElement("SYSMODs");
        MVSObjectTypes.addElement("OBJ");
        MVSObjectTypes.addElement("PLINK");
        MVSObjectTypes.addElement("LKED");
        MVSObjectTypes.addElement("SMOD");
        fasttrackEnglishObjectTypes.addElement("Objects");
        fasttrackMVSObjectTypes.addElement("OBJ");
    }

    private JCheckBox cbGenDebugFiles;
    private JCheckBox cbXmitDebugFiles;
    private JCheckBox cbSaveDebugFiles;
    private JTextField DebugXmitLocText;
    private JRadioButton extChkYesButton;
    private JRadioButton extChkInactButton;
    private JCheckBox extChkBox;

    private String buildtype = null;
    private LogEventProcessor lep;
    private DriverReportParser dr = null;
    private BuildOptions options = null;
    private boolean fastTrack = false;
    private boolean libraryBuild  = true;
    private boolean usermodBuild = false;


    /**
    * Create the Options Dialog
    * @param Frame parent
    * @param boolean modal
    */
    public MBOptionDialog(BuildOptions tempOptions, Component pFrame, DriverReportParser tempDr, LogEventProcessor lep) throws com.ibm.sdwb.build390.MBBuildException{
        super("Build Options", pFrame, lep);
        dr = tempDr;
        this.lep = lep;
        options = tempOptions;
    }

    public void setLibraryBuild(boolean libBuild){
        libraryBuild = libBuild;
        if (libBuild) {
            fastTrack = false;
        }
    }

    public void setFastTrack(boolean tempFast){
        fastTrack = tempFast;
        if (fastTrack) {
            libraryBuild = false;
        }
    }

    public void setUsermodBuild(boolean tempUM){
        usermodBuild = tempUM;
    }

    public void setBuildtype(String temp){
        buildtype = temp;
    }

    public void initializePanel()throws com.ibm.sdwb.build390.MBBuildException{

        GridBagLayout gridBag = new GridBagLayout();
        GridBagConstraints c = new GridBagConstraints();
        c.gridx = 1;
        c.gridy = 1;
        c.weightx = 1;
        c.weighty = 1;
        c.fill = GridBagConstraints.BOTH;
        JPanel centerPanel = new JPanel(gridBag);
        setForeground(MBGuiConstants.ColorRegularText);
        setBackground(MBGuiConstants.ColorGeneralBackground);

        MBInsetPanel panel2 = new MBInsetPanel(new BorderLayout(), 5, 5, 5, 5);
        gridBag.setConstraints(panel2, c);
        centerPanel.add(panel2);

        JLabel label3 = new JLabel("");
        label3.setFont(new Font("Dialog", Font.BOLD, 12));
        label3.setForeground(MBGuiConstants.ColorGroupHeading);
        panel2.add("North", label3);

        Box westFiller = Box.createVerticalBox();
        westFiller.add(Box.createHorizontalStrut(label3.getPreferredSize().width / 3));

        panel2.add("West", westFiller);

        MBInsetPanel panel4 = new MBInsetPanel(new BorderLayout(), 5, 5, 5, 5);
        c.gridy = 2;
        gridBag.setConstraints(panel4, c);
        centerPanel.add(panel4);

        if (!fastTrack) {
            JLabel label5 = new JLabel("Dependent part processing options");
            label5.setFont(new Font("Dialog", Font.BOLD, 12));
            label5.setForeground(MBGuiConstants.ColorGroupHeading);
            panel4.add("North", label5);
            westFiller = Box.createVerticalBox();
            westFiller.add(Box.createHorizontalStrut(label3.getPreferredSize().width / 3));
            panel4.add("West", westFiller);

            buildSettingCombo = new JComboBox();

            buildSettingCombo.addItem(superBuildAll);
            buildSettingCombo.addItem(buildAll);
            buildSettingCombo.addItem(autobuildYes);
            buildSettingCombo.addItem(buildManual);
            buildSettingCombo.addItem(autobuildNo);

            panel4.add("Center", buildSettingCombo);
        }

        MBInsetPanel panel5 = new MBInsetPanel(new BorderLayout(), 5, 5, 5, 5);
        c.gridy = 3;
        c.fill = GridBagConstraints.HORIZONTAL;
        gridBag.setConstraints(panel5, c);
        centerPanel.add(panel5);

        JLabel label6 = new JLabel("Options");
        label6.setFont(new Font("Dialog", Font.BOLD, 12));
        label6.setForeground(MBGuiConstants.ColorGroupHeading);
        panel5.add("North", label6);
        westFiller = Box.createVerticalBox();
        westFiller.add(Box.createHorizontalStrut(label3.getPreferredSize().width / 3));
        panel5.add("West", westFiller);

        Box p5Box = Box.createVerticalBox();
        Box p5Temp = Box.createHorizontalBox();
        p5Temp = Box.createHorizontalBox();
        JLabel label4 = new JLabel("Termination criteria");
        label4.setFont(new Font("Dialog", Font.BOLD, 12));
        p5Temp.add(label4);

        termLevel = new JComboBox();
        termLevel.addItem("4");
        termLevel.addItem("8");
        try {
            termLevel.setSelectedItem("8");
        } catch (IllegalArgumentException e) {
        }
        JPanel p5BoxSub = new JPanel();
        p5BoxSub.add(termLevel);
        p5Temp.add(p5BoxSub);
        p5Temp.add(Box.createHorizontalGlue());
        termLevel.setBackground(MBGuiConstants.ColorFieldBackground);
        p5Box.add(p5Temp);
        panel5.add("Center", p5Box);

        if (!fastTrack) {
            ListingOptionsCombo = new JComboBox();
            ListingOptionsCombo.addItem(SaveNoListings);
            ListingOptionsCombo.addItem(SaveFailedListings);
            ListingOptionsCombo.addItem(SaveGoodListings);
            ListingOptionsCombo.addItem(SaveAllListings);
            p5Temp = Box.createHorizontalBox();
            p5Temp.add(ListingOptionsCombo);
            p5Temp.add(Box.createHorizontalGlue());
            p5Box.add(p5Temp);

            p5Temp = Box.createHorizontalBox();
            cbSkipDCheck = new JCheckBox("Skip the Driver Check Phase");
            p5Temp.add(cbSkipDCheck);
            p5Temp.add(Box.createHorizontalGlue());
            p5Box.add(p5Temp);

            //Begin #Def.INT0767: 
            if (!fastTrack) {
                GridBagLayout gbl2 = new GridBagLayout();
                GridBagConstraints gblC2 = new GridBagConstraints();
                gblC2.weightx = 1;
                gblC2.weighty = 0;
                gblC2.anchor =GridBagConstraints.WEST;
                gblC2.fill = GridBagConstraints.HORIZONTAL;

                JPanel extChkRadPanel = new JPanel(gbl2);

                extChkBox = new JCheckBox("Perform extended check");
                gblC2.gridx = 0;
                gblC2.gridy = 0;
                gbl2.setConstraints(extChkBox, gblC2);
                extChkRadPanel.add(extChkBox);
                extChkBox.setEnabled(false);

                extChkYesButton = new JRadioButton("Fail driver check if there are parts in the driver that are not in the part list");
                gblC2.gridx = 0;
                gblC2.gridy = 1;
                gblC2.insets = new Insets(0, 20, 0, 0);
                gbl2.setConstraints(extChkYesButton, gblC2);
                extChkRadPanel.add(extChkYesButton);
                extChkYesButton.setEnabled(false);
                extChkYesButton.setSelected(true);

                extChkInactButton = new JRadioButton("Inactivate parts in the driver that are not in the part list");
                gblC2.gridx = 0;
                gblC2.gridy = 2;
                gblC2.insets = new Insets(0, 20, 0, 0);
                gbl2.setConstraints(extChkInactButton, gblC2);
                extChkRadPanel.add(extChkInactButton);
                extChkInactButton.setEnabled(false);

                ButtonGroup extChkGroup = new ButtonGroup();
                extChkGroup.add(extChkYesButton);
                extChkGroup.add(extChkInactButton);

                p5Box.add(extChkRadPanel);

                cbSkipDCheck.addItemListener
                (new ItemListener() {
                     public void itemStateChanged(ItemEvent ie) {
                         if (cbSkipDCheck.isSelected()) {
                             extChkBox.setEnabled(false);
                             extChkYesButton.setEnabled(false);
                             extChkInactButton.setEnabled(false);
                         } else {
                             extChkBox.setEnabled(true); 

                             if (extChkBox.isSelected()) {
                                 extChkYesButton.setEnabled(true);
                                 extChkInactButton.setEnabled(true);
                             }
                         }
                         repaint();
                     }
                 });

                extChkBox.addItemListener
                (new ItemListener() {
                     public void itemStateChanged(ItemEvent ie) {
                         if (extChkBox.isEnabled()==true) {
                             if (extChkBox.isSelected()) {
                                 extChkYesButton.setEnabled(true);
                                 extChkInactButton.setEnabled(true);
                             } else {
                                 extChkYesButton.setEnabled(false);
                                 extChkInactButton.setEnabled(false);
                             }
                         } else {
                             extChkYesButton.setEnabled(false);
                             extChkInactButton.setEnabled(false);
                         }

                         repaint();
                     }
                 }); 
            }

            p5Temp = Box.createHorizontalBox();
            cbRunScan = new JCheckBox("Run scanners and checkers against the build output");
            p5Temp.add(cbRunScan);
            p5Temp.add(Box.createHorizontalGlue());
            p5Box.add(p5Temp);

            p5Temp = Box.createHorizontalBox();
            cbDryRun = new JCheckBox("Perform a Dry run");
            p5Temp.add(cbDryRun);
            p5Temp.add(Box.createHorizontalGlue());
            p5Box.add(p5Temp);

            p5Temp = Box.createHorizontalBox();
            cbHaltOnShadCheckWarnings = new JCheckBox("Terminate build on Lodorder warnings");
            p5Temp.add(cbHaltOnShadCheckWarnings);
            p5Temp.add(Box.createHorizontalGlue());
            p5Box.add(p5Temp);
            cbPurgeOutput = new JCheckBox("Purge job output when a phase completes successfully");
        } else {
            p5Temp = Box.createHorizontalBox(); // FixOptionFlow
            cbMetaData = new JCheckBox("Meta Data is embedded in source files");
            p5Temp.add(cbMetaData);
            p5Temp.add(Box.createHorizontalGlue());
            p5Box.add(p5Temp);
            cbPurgeOutput = new JCheckBox("Purge job output when build completes successfully");
        }

        cbAutoPurgeSuccessfulJobs = new JCheckBox("Auto purge successful jobs as soon as they  complete");
        p5Temp = Box.createHorizontalBox();
        p5Temp.add(cbAutoPurgeSuccessfulJobs);
        p5Temp.add(Box.createHorizontalGlue());
        p5Box.add(p5Temp);


        p5Temp = Box.createHorizontalBox();
        p5Temp.add(cbPurgeOutput);
        p5Temp.add(Box.createHorizontalGlue());
        p5Box.add(p5Temp);


        if (!libraryBuild) {
            GridBagLayout macgbl = new GridBagLayout();
            GridBagConstraints macgblC = new GridBagConstraints();
            macgblC.weightx = 0;
            macgblC.weighty = 0;
            macgblC.gridx = 0;
            macgblC.gridy = 0;
            macgblC.anchor = GridBagConstraints.WEST;
            Box macBox = Box.createHorizontalBox();
            cbMaclibObjects = new JCheckBox("Include Maclibs");
            macgbl.setConstraints(cbMaclibObjects, macgblC);
            cbMaclibObjects.setSelected(false);
            macBox.add(cbMaclibObjects);

            bAddMacs = new JButton("Add");
            macgblC.gridx = 2;
            macgbl.setConstraints(bAddMacs, macgblC);
            macBox.add(bAddMacs);
            bEditMacs = new JButton("Edit");
            macgblC.gridx = 3;
            macgbl.setConstraints(bEditMacs, macgblC);
            macBox.add(bEditMacs);
            bDelMacs = new JButton("Remove");
            macgblC.gridx = 4;
            macgbl.setConstraints(bDelMacs, macgblC);
            macBox.add(bDelMacs);
            macBox.add(Box.createHorizontalGlue());
            p5Box.add(macBox);
            Box maclistBox = Box.createHorizontalBox();
            MaclibList = new JComboBox();
            macgblC.gridx = 5;
            JScrollPane macscr = new JScrollPane(MaclibList, JScrollPane.VERTICAL_SCROLLBAR_NEVER, JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS);
            maclistBox.add(macscr);
            Dimension sdim = new Dimension(0,0);
            sdim.height = macscr.getPreferredSize().height;
            macscr.setMinimumSize(sdim);
            p5Box.add(maclistBox);
        }

        // Add transmit option
        GridBagLayout gbl = new GridBagLayout();
        GridBagConstraints gblC = new GridBagConstraints();
        gblC.weightx = 0;
        gblC.weighty = 0;
        gblC.gridx = 0;
        gblC.gridy = 0;
        gblC.anchor =GridBagConstraints.WEST;
        gblC.fill = GridBagConstraints.HORIZONTAL;
        JPanel transBox = new JPanel(gbl);
        cbTransObjects = new JCheckBox("Transmit");
        if (!fastTrack) {
            transObjectType = new JComboBox(englishObjectTypes);
        } else {
            transObjectType = new JComboBox(fasttrackEnglishObjectTypes);
        }
        transObjects = new JTextField();
        JLabel transLabel = new JLabel("to (node.userid):");
        transBox.add(cbTransObjects);
        gbl.setConstraints(cbTransObjects, gblC);
        gblC.gridx = 2;
        transBox.add(transObjectType);
        gbl.setConstraints(transObjectType, gblC);
        gblC.gridx = 3;
        transBox.add(transLabel);
        gbl.setConstraints(transLabel, gblC);
        gblC.gridx = 5;
        gblC.weightx = 1;
        gblC.gridwidth = GridBagConstraints.REMAINDER;
        transBox.add(transObjects);
        gbl.setConstraints(transObjects, gblC);
        p5Box.add(transBox);

        if (!fastTrack) {
            GridBagLayout gbl1 = new GridBagLayout();
            GridBagConstraints gblC1 = new GridBagConstraints();
            gblC1.weightx = 0;
            gblC1.weighty = 0;
            gblC1.anchor =GridBagConstraints.WEST;
            gblC1.fill = GridBagConstraints.HORIZONTAL;

            JPanel GenDebugFilesBox = new JPanel(gbl1);

            cbGenDebugFiles = new JCheckBox("Generate debug files");
            gblC1.gridx = 0;
            gblC1.gridy = 0;
            GenDebugFilesBox.add(cbGenDebugFiles);
            gbl1.setConstraints(cbGenDebugFiles, gblC1);

            cbXmitDebugFiles = new JCheckBox("Send debug files");
            gblC1.gridx = 0;
            gblC1.gridy = 1;
            gblC1.insets = new Insets(0, 20, 0, 0);
            GenDebugFilesBox.add(cbXmitDebugFiles);
            gbl1.setConstraints(cbXmitDebugFiles, gblC1);
            cbXmitDebugFiles.setEnabled(false);

            cbSaveDebugFiles = new JCheckBox("Save debug work files");
            gblC1.gridx = 0;
            gblC1.gridy = 2;
            gblC1.insets = new Insets(0, 20, 0, 0);
            GenDebugFilesBox.add(cbSaveDebugFiles);
            gbl1.setConstraints(cbSaveDebugFiles, gblC1);
            cbSaveDebugFiles.setEnabled(false);

            final JLabel DebugXmitLocLabel = new JLabel("to (node.userid)or(MVS Volume):");
            gblC1.insets = new Insets(0, 0, 0, 0);
            gblC1.gridx = 2;
            gblC1.gridy = 1;
            GenDebugFilesBox.add(DebugXmitLocLabel);
            gbl1.setConstraints(DebugXmitLocLabel, gblC1);
            DebugXmitLocLabel.setEnabled(false);

            DebugXmitLocText = new JTextField();
            gblC1.gridx = 3;
            gblC1.gridy = 1;
            gblC1.weightx = 1;
            gblC1.gridwidth = GridBagConstraints.REMAINDER;
            GenDebugFilesBox.add(DebugXmitLocText);
            gbl1.setConstraints(DebugXmitLocText, gblC1);
            DebugXmitLocText.setEnabled(false);

            p5Box.add(GenDebugFilesBox);

            cbGenDebugFiles.addItemListener
            (new ItemListener() {
                 public void itemStateChanged(ItemEvent ie) {
                     if (cbGenDebugFiles.isSelected()) {
                         cbXmitDebugFiles.setEnabled(true);
                         cbSaveDebugFiles.setEnabled(true);
                         DebugXmitLocLabel.setEnabled(true);
                     } else {
                         cbXmitDebugFiles.setEnabled(false);
                         cbXmitDebugFiles.setSelected(false);
                         cbSaveDebugFiles.setEnabled(false);
                         cbSaveDebugFiles.setSelected(false);
                         DebugXmitLocLabel.setEnabled(false);
                         DebugXmitLocText.setEnabled(false);
                         DebugXmitLocText.setText("");
                     }
                     repaint();
                 }
             });

            cbXmitDebugFiles.addItemListener
            (new ItemListener() {
                 public void itemStateChanged(ItemEvent ie) {
                     if (cbXmitDebugFiles.isSelected()) {
                         DebugXmitLocLabel.setEnabled(true);
                         DebugXmitLocText.setEnabled(true); 
                     } else {
                         DebugXmitLocLabel.setEnabled(false);
                         DebugXmitLocText.setEnabled(false);
                         DebugXmitLocText.setText("");
                     }
                     repaint();
                 }
             });
        }

        MBInsetPanel panel6 = new MBInsetPanel(new BorderLayout(), 5, 5, 5, 5);
        c.gridy = 4;
        gridBag.setConstraints(panel6, c);
        centerPanel.add(panel6);

        JLabel label7 = new JLabel("Part Source");
        label7.setFont(new Font("Dialog", Font.BOLD, 12));
        label7.setForeground(MBGuiConstants.ColorGroupHeading);

        JButton bHelp = null;

        if (usermodBuild) {
            // help for usermods
            bHelp = new JButton(new HelpAction("HDRUSERMOD",HelpTopicID.USERMODBUILDPAGE_HELP));
        } else if (libraryBuild) {
            // help for driverbuilds
            bHelp = new JButton(new HelpAction("SPTDPPO",HelpTopicID.OPTIONDIALOG0_HELP));
        } else {
            // help for userbuilds 
            bHelp = new JButton(new HelpAction("SPTUBOPTS",HelpTopicID.OPTIONDIALOG1_HELP));
        }

        bUndo = new JMenuItem("Undo");
        bUndo.setEnabled(false);

        bOK = new JButton("OK");
        bOK.setForeground(MBGuiConstants.ColorActionButton);

        bPhaseOverrides = new JMenuItem("Phase Overrides");
        bPhaseOverrides.setEnabled(false);

        JMenu editMenu = new JMenu("Edit");
        getJMenuBar().add(editMenu);
        editMenu.add(bUndo);
        JMenu viewMenu = new JMenu("View");
        getJMenuBar().add(viewMenu);
        viewMenu.add(bPhaseOverrides);
        Vector actionButtons = new Vector();
        actionButtons.addElement(bOK);
        addButtonPanel(bHelp,actionButtons);

        setTitle("Build Options");
        if (!fastTrack) {
            JScrollPane sp = new JScrollPane(centerPanel,ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED,ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);
            getContentPane().add("Center", sp);
        } else {
            getContentPane().add("Center", centerPanel);
        }

        // Handle checkbox events
        if (!fastTrack) {
            cbRunScan.addItemListener(cbListener);
            cbDryRun.addItemListener(cbListener);
            cbSkipDCheck.addItemListener(cbListener);
        }
        cbPurgeOutput.addItemListener(cbListener);

        // Enable Undo button if the following one of radio button is selected
        // if spin button changed

        if (!fastTrack) {
            loadAndSetOptions();
        } else { // set autobuild = no
            BuildOptionsLocal localOptions = (BuildOptionsLocal) options;
            if (options.getAutoBuild()==null) {
                options.setAutoBuild("NO");
                cbPurgeOutput.setSelected(true);
                try {
                    termLevel.setSelectedItem("8");
                } catch (IllegalArgumentException e) {
                }
            } else {
                // Find out the current terminate criteria spin botton
                try {
                    termLevel.setSelectedItem(Integer.toString(options.getBuildCC()));
                } catch (IllegalArgumentException e) {
                }
                cbPurgeOutput.setSelected(options.isPurgeJobs());
                cbAutoPurgeSuccessfulJobs.setSelected(options.isAutoPurgeSuccessfulJobs());

                cbMetaData.setSelected(localOptions.isUsingEmbeddedMetadata());
                if (options.getXmitTo() != null) {
                    if (options.getXmitTo().trim().length() > 0) {
                        cbTransObjects.setSelected(true);
                        transObjects.setText(options.getXmitTo());
                    } else {
                        cbTransObjects.setSelected(false);
                    }
                } else {
                    cbTransObjects.setSelected(false);
                }
            }
            // init the maclibs field to any previously entered for this release
            cbMaclibObjects.setSelected(false);
            String[] userMacs  = localOptions.getUserMacs();
            if (userMacs !=null) {
                for (int i = 0; i < userMacs.length; i++) {
                    MaclibList.addItem(userMacs[i]);
                }
            }

            if (MaclibList.getModel().getSize() > 0) {
                cbMaclibObjects.setSelected(true);
            }
        }

        // spin control for terminate criteria
        if (!fastTrack) {
            buildSettingCombo.addItemListener(new ItemListener() {
                                                  public void itemStateChanged(ItemEvent ie) {
                                                      bUndo.setEnabled(true);
                                                  }
                                              });
        }

        cbTransObjects.addItemListener(new ItemListener() {
                                           public void itemStateChanged(ItemEvent ie) {
                                               bUndo.setEnabled(true);
                                               if (cbTransObjects.isSelected()) {
                                                   transObjects.setEnabled(true);
                                                   transObjects.requestFocus();
                                               } else {
                                                   transObjects.setEnabled(   false);
                                                   transObjects.setText("");
                                               }
                                           }
                                       });

        if (buildtype !=null) {
            bPhaseOverrides.addActionListener(new MBCancelableActionListener(thisFrame) {
                                                  public void doAction(ActionEvent avt) {
                                                      new MBViewPhaseOverride(buildtype,  dr.getPhaseInforamtion(buildtype));
                                                  }
                                              });
        }

        if (!libraryBuild) {
            bAddMacs.addActionListener(new MBCancelableActionListener(thisFrame) {
                                           public void doAction(ActionEvent avt) {
                                               mdata.setLength(0);
                                               MBMaclibsDialog md = new MBMaclibsDialog(thisFrame, mdata); // pass current contens of field
                                               // *maclnulladd donot allow null maclib to be added in the listbox
                                               if (mdata.toString().trim().length()!=0) {
                                                   MaclibList.insertItemAt(mdata.toString().toUpperCase(), 0);
                                                   MaclibList.setSelectedIndex(0);

                                               }
                                           }
                                       });
        }

        if (!libraryBuild) {
            bEditMacs.addActionListener(new MBCancelableActionListener(thisFrame) {
                                            public void doAction(ActionEvent avt) {
                                                int sel = MaclibList.getSelectedIndex();
                                                if (sel > -1) {
                                                    mdata.setLength(0);
                                                    String cdata = (String)MaclibList.getSelectedItem();
                                                    if (cdata != null) mdata.append(cdata);
                                                    MBMaclibsDialog md = new MBMaclibsDialog(thisFrame, mdata); // pass current contens of field
                                                    MaclibList.removeItemAt(sel);
                                                    MaclibList.insertItemAt(mdata.toString().toUpperCase(), 0);
                                                    MaclibList.setSelectedIndex(0);
                                                }
                                            }
                                        });
        }

        // pjs_maclibs handle maclib settings
        if (!libraryBuild) {
            bDelMacs.addActionListener(new MBCancelableActionListener(thisFrame) {
                                           public void doAction(ActionEvent avt) {
                                               int sel = MaclibList.getSelectedIndex();
                                               if (sel > -1) {
                                                   MaclibList.removeItemAt(sel);
                                               }
                                           }
                                       });
        }

        bUndo.addActionListener(new MBCancelableActionListener(thisFrame) {
                                    public void doAction(ActionEvent avt) {
                                        try {
                                            loadAndSetOptions();
                                        } catch (MBBuildException mbe) {
                                            lep.LogException( mbe);
                                        }
                                        bUndo.setEnabled(false);
                                    }
                                });


        bOK.addActionListener(new MBCancelableActionListener(thisFrame) {
                                  public void doAction(ActionEvent avt) {
                                      if (save()) {
                                          dispose();
                                      } else {
                                          return;
                                      }
                                  }
                              });
        setVisible(true);
    }

    /**
    * Load Build Options with default setting if the options.ser file does not
    * exist or deserialize the options.ser
    */
    private  void loadAndSetOptions() throws com.ibm.sdwb.build390.MBBuildException{
        String methodName = new String("MBOptionDialog:loadOptions");
        lep.LogSecondaryInfo("Setup to Driver report", methodName);
        if (!options.isBeenSet()) {
            dr.setBuildOptions(options, buildtype);
            options.setOptions(dr);
        }
        if (!fastTrack) {
            if (options.getListGen().equals("YES")) {
                ListingOptionsCombo.setSelectedItem(SaveGoodListings);
            } else if (options.getListGen().equals("NO")) {
                ListingOptionsCombo.setSelectedItem(SaveNoListings);
            } else if (options.getListGen().equals("FAIL")) {
                ListingOptionsCombo.setSelectedItem(SaveFailedListings);
            } else if (options.getListGen().equals("ALL")) {
                ListingOptionsCombo.setSelectedItem(SaveAllListings);
            }
        }
        if (options.getXmitTo() != null) {
            if (options.getXmitTo().trim().length() > 0) {
                cbTransObjects.setSelected(true);
                transObjects.setText(options.getXmitTo());
            } else {
                cbTransObjects.setSelected(false);
            }
        } else {
            cbTransObjects.setSelected(false);
        }

        if (options.getXmitType() != null) {
            if (options.getXmitType().trim().length() > 0) {
                int xmitIndex = MVSObjectTypes.indexOf(options.getXmitType());
                if (!fastTrack) {
                    transObjectType.setSelectedItem(englishObjectTypes.elementAt(xmitIndex));
                } else {
                    transObjectType.setSelectedItem(fasttrackEnglishObjectTypes.elementAt(xmitIndex));
                }
            }
        }

        // Find out the current terminate criteria spin botton
        try {
            termLevel.setSelectedItem(Integer.toString(options.getBuildCC()));
        } catch (IllegalArgumentException e) {
        }

        // Find out the current selected radio button
        // if buildGroupCheckbox = rbBuildManual, then enable cbForce
        if (!fastTrack) {
            if (options.getAutoBuild().equals("YES")) {
                if (options.getForce().equals("YES")) {
                    buildSettingCombo.setSelectedItem(buildAll);
                } else if (options.getForce().equals("ALL")) {
                    buildSettingCombo.setSelectedItem(superBuildAll);
                } else {
                    buildSettingCombo.setSelectedItem(autobuildYes);
                }
            } else if (options.getAutoBuild().equals("MANUAL")) {
                buildSettingCombo.setSelectedItem(buildManual);
            } else if (options.getAutoBuild().equals("NO")) {
                buildSettingCombo.setSelectedItem(autobuildNo);
            }

            cbSkipDCheck.setSelected(options.isSkippingDriverCheck());
            cbRunScan.setSelected(options.isRunScanners());
            cbDryRun.setSelected(options.isDryRun());
            cbHaltOnShadCheckWarnings.setSelected(options.isHaltOnShadowCheckWarnings());
        }

        cbPurgeOutput.setSelected(options.isPurgeJobs());
        cbAutoPurgeSuccessfulJobs.setSelected(options.isAutoPurgeSuccessfulJobs());

        // Enable text field if rbCopyParts radio button is selected
        if (buildtype!=null) {
            for (Iterator phaseIterator = dr.getPhaseInforamtion(buildtype).iterator(); phaseIterator.hasNext();) {
                com.ibm.sdwb.build390.mainframe.PhaseInformation onePhase = (com.ibm.sdwb.build390.mainframe.PhaseInformation) phaseIterator.next();
                if (onePhase.getPhaseOverrides()!=null) {
                    bPhaseOverrides.setEnabled(true);
                }
            }
        }

        // init the maclibs field to any previously entered for this release
        if (!libraryBuild) {
            BuildOptionsLocal localOptions = (BuildOptionsLocal) options;
            String[] userMacs  = localOptions.getUserMacs();
            cbMaclibObjects.setSelected(false);
            if (userMacs != null) {
                String smacs = new String();
                for (int i = 1; i < userMacs.length; i++) {
                    smacs += userMacs[i];
                    if (i < userMacs.length-1) {
                        smacs+=",";
                    }
                }
                MaclibList.setSelectedItem(smacs);
                if (MaclibList.getModel().getSize() > 0) {
                    cbMaclibObjects.setSelected(true);
                }
            }
        }

        if (!fastTrack) {
            if (options.isGeneratingDebugFiles()) {
                cbGenDebugFiles.setSelected(true);

                if (options.isSaveDebugFiles()) {
                    cbSaveDebugFiles.setSelected(true);
                }
                if (options.isXmitDebugFiles()) {
                    cbXmitDebugFiles.setSelected(true);
                    if (options.getXmitDebugFileLocation()!=null) {
                        DebugXmitLocText.setText(options.getXmitDebugFileLocation());
                    }
                }
            }

            extChkBox.setSelected(options.getExtraDriverCheck()!=null);

            if (options.getExtraDriverCheck()!=null) {
                if (options.getExtraDriverCheck().equals("YES")) {
                    extChkYesButton.setSelected(true);
                } else {
                    extChkInactButton.setSelected(true);
                }
            }
        }
    }


    /**
     * Save the changes to the options object. 
     * If there was a problem saving, then return false
     * 
     * @return true if save successful, 
     *         false otherwise
     */
    public boolean save() {

        if (cbTransObjects.isSelected() & transObjects.getText().length()<3) {
            MBMsgBox mb = new MBMsgBox("Error", "You indicated that objects are to be transmitted but did not specify node.userid");
            return false;
        }

        if (!fastTrack) {
            if (cbGenDebugFiles.isSelected()) {
                options.setGenerateDebugFiles(true);

                if (cbSaveDebugFiles.isSelected()) {
                    options.setSaveDebugFiles(true);
                } else {
                    options.setSaveDebugFiles(false);
                }

                if (cbXmitDebugFiles.isSelected()) {
                    options.setXmitDebugFiles(true);

                    if (DebugXmitLocText.getText().length() == 0) {
                        MBMsgBox mb = new MBMsgBox("Error", "You indicated that Debug files are to be transmitted but did not specify destination");

                        return false;
                    }
                    options.setXmitDebugFileLocation(DebugXmitLocText.getText());
                } else {
                    options.setXmitDebugFiles(false);
                }
            } else {
                options.setGenerateDebugFiles(false);
            }

            if (extChkBox.isSelected()) {
                if (extChkYesButton.isSelected()) {
                    options.setExtraDriverCheck("YES");
                } else {
                    options.setExtraDriverCheck("INACTIVE");
                }
            }
        }
        options.setXmitTo(makeEmptyStringsNull(transObjects.getText()));
        if (!fastTrack) {
            options.setXmitType(makeEmptyStringsNull((String)MVSObjectTypes.elementAt(transObjectType.getSelectedIndex())));
        } else {
            options.setXmitType(makeEmptyStringsNull((String)fasttrackMVSObjectTypes.elementAt(transObjectType.getSelectedIndex())));
        }
        options.setBuildCC(Integer.parseInt((String) termLevel.getSelectedItem()));
        options.setPurgeJobsAfterCompletion(cbPurgeOutput.isSelected());
        options.setAutoPurgeSuccessfulJobs(cbAutoPurgeSuccessfulJobs.isSelected());

        if (!fastTrack) {
            if (ListingOptionsCombo.getSelectedItem().equals(SaveGoodListings)) {
                options.setListGen("YES");
            } else if (ListingOptionsCombo.getSelectedItem().equals(SaveNoListings)) {
                options.setListGen("NO");
            } else if (ListingOptionsCombo.getSelectedItem().equals(SaveFailedListings)) {
                options.setListGen("FAIL");
            } else if (ListingOptionsCombo.getSelectedItem().equals(SaveAllListings)) {
                options.setListGen("ALL");
            }
            options.setRunScanners(cbRunScan.isSelected());
            options.setSkipDriverCheck(cbSkipDCheck.isSelected());
            options.setDryRun(cbDryRun.isSelected());
            if (buildSettingCombo.getSelectedItem().equals(superBuildAll)) {
                options.setForce("ALL");
                options.setAutoBuild("YES");
            }
            if (buildSettingCombo.getSelectedItem().equals(buildAll)) {
                options.setForce("YES");
                options.setAutoBuild("YES");
            }
            if (buildSettingCombo.getSelectedItem().equals(autobuildYes)) {
                options.setAutoBuild("YES");
                options.setForce("NO");
            }
            if (buildSettingCombo.getSelectedItem().equals(buildManual)) {
                options.setAutoBuild("MANUAL");
                options.setForce("NO");
            }
            if (buildSettingCombo.getSelectedItem().equals(autobuildNo)) {
                options.setAutoBuild("NO");
                options.setForce("NO");
            }
            options.setHaltOnShadowCheckWarnings(cbHaltOnShadCheckWarnings.isSelected());
        } else {
            ((BuildOptionsLocal)options).setEmbeddedMetadata(cbMetaData.isSelected());
        }

        if (!libraryBuild) {
            BuildOptionsLocal localOptions = (BuildOptionsLocal) options;
            String cmacs = (String) MaclibList.getSelectedItem();
            if (cmacs==null) {
                cbMaclibObjects.setSelected(false);
            }
            if (cbMaclibObjects.isSelected()) {
                String [] amacs = new String[MaclibList.getItemCount()+1];
                for (int idx=0; idx<MaclibList.getItemCount(); idx++) {
                    amacs[idx+1] = (String) MaclibList.getItemAt(idx);
                }
                localOptions.setUserMacs(amacs);
            } else {
                localOptions.setUserMacs(null);
            }

        }
        options.setOptionsBeenSet(true); 
        return true;
    }

    public boolean saveNeeded() {
        boolean needed = false;
        if (options.getXmitTo() != null) {
            needed = needed | !options.getXmitTo().equals(transObjects.getText());
        } else {
            needed = transObjects.getText().trim().length() >0;
        }
        if (options.getXmitType() != null) {
            if (!fastTrack) {
                needed = needed | !options.getXmitType().equals((String)MVSObjectTypes.elementAt(transObjectType.getSelectedIndex()));
            } else {
                needed = needed | !options.getXmitType().equals((String)fasttrackMVSObjectTypes.elementAt(transObjectType.getSelectedIndex()));
            }
        } else {
            needed = true;
        }
        needed = needed | (options.getBuildCC()!= Integer.parseInt((String) termLevel.getSelectedItem()));
        needed = needed | options.isPurgeJobs() != cbPurgeOutput.isSelected();
        if (!fastTrack) {
            if (options.getListGen() != null) {
                if (ListingOptionsCombo.getSelectedItem().equals(SaveGoodListings)) {
                    needed = needed | !options.getListGen().equals("YES");
                } else if (ListingOptionsCombo.getSelectedItem().equals(SaveNoListings)) {
                    needed = needed | !options.getListGen().equals("NO");
                } else if (ListingOptionsCombo.getSelectedItem().equals(SaveFailedListings)) {
                    needed = needed | !options.getListGen().equals("FAIL");
                } else if (ListingOptionsCombo.getSelectedItem().equals(SaveAllListings)) {
                    needed = needed | !options.getListGen().equals("ALL");
                }
            } else {
                needed = true;
            }
            needed = needed | options.isRunScanners() != cbRunScan.isSelected();
            needed = needed | options.isSkippingDriverCheck()!=cbSkipDCheck.isSelected();
            needed = needed | options.isHaltOnShadowCheckWarnings()!=cbHaltOnShadCheckWarnings.isSelected();
            needed = needed | options.isDryRun() != cbDryRun.isSelected();

            if (options.getForce() != null & options.getAutoBuild() != null) {
                if (buildSettingCombo.getSelectedItem().equals(buildAll)) {
                    needed = needed | !options.getForce().equals("YES");
                    needed = needed | !options.getAutoBuild().equals("MANUAL");
                }
                if (buildSettingCombo.getSelectedItem().equals(autobuildYes)) {
                    needed = needed | !options.getAutoBuild().equals("YES");
                    needed = needed | !options.getForce().equals("NO");
                }
                if (buildSettingCombo.getSelectedItem().equals(buildManual)) {
                    needed = needed | !options.getAutoBuild().equals("MANUAL");
                    needed = needed | !options.getForce().equals("NO");
                }
                if (buildSettingCombo.getSelectedItem().equals(autobuildNo)) {
                    needed = needed | !options.getAutoBuild().equals("NO");
                    needed = needed | !options.getForce().equals("NO");
                }
            } else {
                needed = true;
            }

        } else {
            needed = needed | ((BuildOptionsLocal) options).isUsingEmbeddedMetadata() != cbMetaData.isSelected();
        }
        return needed;
    }

    public void dispose() {
        dispose(false);
// Ken, 6/17/99 don't check this one.        dispose(true);
    }


    // set minimum size
    public Dimension getPreferredSize() {
        Dimension oldPref ;
        if (!fastTrack) {
            oldPref = new Dimension(425,super.getPreferredSize().height - 140) ;
        } else {
            oldPref = new Dimension(425, super.getPreferredSize().height);
        }
        return oldPref;
    }

    private String makeEmptyStringsNull(String temp){
        if (temp!=null) {
            if (temp.trim().length() > 0) {
                return temp;
            }
        }
        return null;
    }


    /**
    * Inner class listener for checkbox and checkbox group
    */
    class Listener implements ItemListener {
        public void itemStateChanged(ItemEvent event) {
            bUndo.setEnabled(true);
        }
    }
}
