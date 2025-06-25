package com.ibm.sdwb.build390.userinterface.graphic.panels;
/*********************************************************************/
/* OptionPanel class for the Build/390 client                     */
/*  Creates and manages the Build Options                            */
/*********************************************************************/
/*********************************************************************/

import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.util.*;

import javax.swing.*;
import javax.swing.event.*;

import com.ibm.sdwb.build390.*;
import com.ibm.sdwb.build390.help.*;
import com.ibm.sdwb.build390.info.*;
import com.ibm.sdwb.build390.logprocess.*;
import com.ibm.sdwb.build390.process.ProcessWrapperForSingleStep;
import com.ibm.sdwb.build390.process.steps.DriverReport;
import com.ibm.sdwb.build390.user.Setup;
import com.ibm.sdwb.build390.userinterface.UserCommunicationInterface;
import com.ibm.sdwb.build390.userinterface.event.*;
import com.ibm.sdwb.build390.userinterface.event.build.*;
import com.ibm.sdwb.build390.userinterface.graphic.utilities.*;
import com.ibm.sdwb.build390.userinterface.graphic.widgets.*;
import com.ibm.sdwb.build390.userinterface.graphic.widgets.CancelableProcess;
import com.ibm.sdwb.build390.utilities.BinarySettingUtilities;



public class OptionPanel extends JScrollPane implements RequiredActionsCompletedInterface, UserInterfaceEventListener, AncestorListener {

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

    //private JMenuItem bUndo;
    //private JMenuItem bPhaseOverrides;
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
    public static final String OPTIONKEY = "OPTIONKEY";

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
    private OptionPanel thisPanel = null;

    private String buildtype = null;
    private UserCommunicationInterface userComm = null;
    private DriverReport driverReportStep = null;
    private BuildOptions options = null;
    private boolean libraryBuild  = true;
    private com.ibm.sdwb.build390.mainframe.ReleaseInformation relInfo = null;
    private com.ibm.sdwb.build390.mainframe.DriverInformation driverInfo = null;
    private String optionStorageKey = null;
    private UserInterfaceListenerManager changeManager = new UserInterfaceListenerManager();
    private boolean requirePageVisit = false;
    private Setup setup = null;
    private boolean justDisplay = false;

    private JButton bUndo = null;
    private JButton bPhaseOverrides = null;

    /**
    * Create the Options panel
    * @param Frame parent
    * @param boolean modal
    */
    //public OptionPanel(MBInternalFrame parent, BuildOptions tempOptions, DriverReportParser tempDr, LogEventProcessor lep) 
    public OptionPanel(BuildOptions tempOptions, Setup tempSetup, String tempOptionStorageKey, UserCommunicationInterface tempComm) {
        options = tempOptions.getCopy();
        addAncestorListener(this);
        thisPanel = this;
        userComm = tempComm;
        setup = tempSetup;
        libraryBuild = isLibraryBuild();
        optionStorageKey = tempOptionStorageKey;
        layoutPanel();
        refreshOptionDisplay();
    }

    public void setOptions(BuildOptions newOptions) {
        options = newOptions.getCopy();
        libraryBuild = isLibraryBuild();
        refreshOptionDisplay();
    }

    private boolean isLibraryBuild() {
        if (options instanceof BuildOptionsLocal) {
            return false;
        } else {
            return true;
        }
    }

    public BuildOptions getOptions() {
        return options;
    }

    public void setJustDisplay(boolean tempDisplay) {
        justDisplay = tempDisplay;
    }

    private boolean isFastTrack() {
        if (options instanceof BuildOptionsLocal) {
            return((BuildOptionsLocal)options).isFastTrack();
        }
        return false;
    }

    public void refreshOptionDisplay() {
        String methodName = new String("MBOptionDialog:loadOptions");
        userComm.getLEP().LogSecondaryInfo("Setup to Driver report", methodName);
        if (!isFastTrack()) {
            if (!options.isBeenSet()& driverReportStep!=null & !justDisplay) {
                if (driverReportStep.getParser()!=null) {
                    driverReportStep.getParser().setBuildOptions(options, buildtype);
                    options.setOptions(driverReportStep.getParser());
                }
            }
            if (options.getListGen().equals("YES")) {
                ListingOptionsCombo.setSelectedItem(SaveGoodListings);
            } else if (options.getListGen().equals("NO")) {
                ListingOptionsCombo.setSelectedItem(SaveNoListings);
            } else if (options.getListGen().equals("FAIL")) {
                ListingOptionsCombo.setSelectedItem(SaveFailedListings);
            } else if (options.getListGen().equals("ALL")) {
                ListingOptionsCombo.setSelectedItem(SaveAllListings);
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
                    if (!isFastTrack()) {
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

            cbPurgeOutput.setSelected(options.isPurgeJobs());
            cbAutoPurgeSuccessfulJobs.setSelected(options.isAutoPurgeSuccessfulJobs());

            // Enable text field if rbCopyParts radio button is selected
            if (buildtype!=null & driverReportStep!=null) {
                if (driverReportStep.getParser()!=null) {
                    for (Iterator phaseIterator = driverReportStep.getParser().getPhaseInforamtion(buildtype).iterator(); phaseIterator.hasNext();) {
                        com.ibm.sdwb.build390.mainframe.PhaseInformation onePhase = (com.ibm.sdwb.build390.mainframe.PhaseInformation) phaseIterator.next();
                        if (onePhase.getPhaseOverrides()!=null) {
                            bPhaseOverrides.setEnabled(true);
                        }
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
        } else {
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
    }

    public void setDriverReportStep(DriverReport drp) {
        driverReportStep = drp;
    }

    public DriverReport getDriverReportStep() {
        return driverReportStep;
    }

    private void layoutPanel() {
        GridBagLayout gridBag = new GridBagLayout();
        GridBagConstraints c = new GridBagConstraints(); 
        final JPanel centerPanel = new JPanel(gridBag);
        getViewport().setView(centerPanel);

//

        c.gridx = 1;
        c.gridy = 0;
        c.weighty = 1;
        c.insets = new Insets(2,5,2,5);
        c.anchor = GridBagConstraints.WEST;
        c.gridwidth = 1;


        bUndo = new JButton(new CancelableAction("Undo") {
                                public void doAction(ActionEvent avt) {
                                    setOptions(options);
                                    bUndo.setEnabled(false);
                                }
                            });
        gridBag.setConstraints(bUndo,c);
        centerPanel.add(bUndo);
        //
        c.gridx = 2;
        c.gridy = 0;
        c.weighty = 1;
        c.insets = new Insets(2,5,2,5);
        c.fill = GridBagConstraints.HORIZONTAL;
        c.gridwidth = GridBagConstraints.REMAINDER;



        bPhaseOverrides = new JButton(new CancelableAction("View Phase Overrides") {
                                          public void doAction(ActionEvent avt) {
                                              if (buildtype!=null) {
                                                  new MBViewPhaseOverride(buildtype,  driverReportStep.getParser().getPhaseInforamtion(buildtype));
                                              } else {
                                                  new MBMsgBox("View Phase Overrides", "Please select a buildtype.");
                                              }
                                          }
                                      });
        gridBag.setConstraints(bPhaseOverrides,c);
        centerPanel.add(bPhaseOverrides);
        //
        c.gridx = 1;
        c.gridy = 1;
        c.weightx = 1;
        c.weighty = 1;
        c.fill = GridBagConstraints.BOTH;
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

        if (!isFastTrack()) {
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

        if (!isFastTrack()) {
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
            if (!isFastTrack()) {
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
                extChkBox.setEnabled(cbSkipDCheck.isEnabled());

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

            bAddMacs = new JButton(new CancelableAction("Add") {
                                       public void doAction(ActionEvent avt) {
                                           mdata.setLength(0);

                                           //***BE
                                           MBMaclibsDialog md = new MBMaclibsDialog(GeneralUtilities.getParentInternalFrame(centerPanel), mdata); // pass current contens of field
                                           // *maclnulladd donot allow null maclib to be added in the listbox
                                           if (mdata.toString().trim().length()!=0) {
                                               StringTokenizer tok = new StringTokenizer(mdata.toString().trim(), ",");
                                               int i=0;
                                               while (tok.hasMoreTokens()) {
                                                   MaclibList.insertItemAt((String) tok.nextElement(),i++);
                                               }

                                               MaclibList.setSelectedIndex(0);
                                           }
                                       }
                                   });

            macgblC.gridx = 2;
            macgbl.setConstraints(bAddMacs, macgblC);
            macBox.add(bAddMacs);
            bEditMacs = new JButton(new CancelableAction("Edit") {
                                        public void doAction(ActionEvent avt) {
                                            int sel = MaclibList.getSelectedIndex();
                                            if (sel > -1) {
                                                mdata.setLength(0);
                                                String cdata = (String)MaclibList.getSelectedItem();
                                                if (cdata != null) mdata.append(cdata);
                                                MBMaclibsDialog md = new MBMaclibsDialog(GeneralUtilities.getParentInternalFrame(centerPanel), mdata); // pass current contens of field
                                                MaclibList.removeItemAt(sel);
                                                MaclibList.insertItemAt(mdata.toString().toUpperCase(), 0);
                                                MaclibList.setSelectedIndex(0);
                                            }
                                        }
                                    });

            macgblC.gridx = 3;
            macgbl.setConstraints(bEditMacs, macgblC);
            macBox.add(bEditMacs);
            bDelMacs = new JButton(new CancelableAction("Remove") {
                                       public void doAction(ActionEvent avt) {
                                           int sel = MaclibList.getSelectedIndex();
                                           if (sel > -1) {
                                               MaclibList.removeItemAt(sel);
                                           }
                                       }
                                   });


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
        if (!isFastTrack()) {
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

        if (!isFastTrack()) {
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


        /*
        bUndo = new JMenuItem("Undo");
        bUndo.setEnabled(false);


        bPhaseOverrides = new JMenuItem("Phase Overrides");
        bPhaseOverrides.setEnabled(false);

        JMenu editMenu = new JMenu("Edit");
        getJMenuBar().add(editMenu);
        editMenu.add(bUndo);
        JMenu viewMenu = new JMenu("View");
        getJMenuBar().add(viewMenu);
        viewMenu.add(bPhaseOverrides);
        Vector actionButtons = new Vector();
        */


        // Handle checkbox events
        if (!isFastTrack()) {
            cbRunScan.addItemListener(cbListener);
            cbDryRun.addItemListener(cbListener);
            cbSkipDCheck.addItemListener(cbListener);
        }
        cbPurgeOutput.addItemListener(cbListener);
        cbAutoPurgeSuccessfulJobs.addItemListener(cbListener);

        // Enable Undo button if the following one of radio button is selected
        // if spin button changed

        // spin control for terminate criteria
        if (!isFastTrack()) {
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
        setVisible(true);
    }

    public void setEnabled(boolean enabled) {
        super.setEnabled(enabled);
        setCompEnabled(cbCompileOption, enabled);
        setCompEnabled(termLevel, enabled);
        setCompEnabled(buildSettingCombo, enabled);
        setCompEnabled(ListingOptionsCombo, enabled);
        setCompEnabled(cbSkipDCheck, enabled);
        setCompEnabled(cbRunScan, enabled);
        setCompEnabled(cbDryRun, enabled);
        setCompEnabled(cbPurgeOutput, enabled);
        setCompEnabled(cbTransObjects, enabled);
        setCompEnabled(cbMaclibObjects, enabled);
        setCompEnabled(bEditMacs, enabled);
        setCompEnabled(bAddMacs, enabled);
        setCompEnabled(bDelMacs, enabled);
        setCompEnabled(MaclibList, enabled);
        setCompEnabled(transObjectType, enabled);
        setCompEnabled(cbMetaData, enabled);
        setCompEnabled(cbHaltOnShadCheckWarnings, enabled);
        setCompEnabled(cbAutoPurgeSuccessfulJobs, enabled);
        setCompEnabled(partSourcePath, enabled);
        setCompEnabled(transObjects, enabled);
        setCompEnabled(buildGroupCheckbox, enabled);
        setCompEnabled(sourceGroupCheckbox, enabled);
        setCompEnabled(cbGenDebugFiles, enabled);
        setCompEnabled(extChkBox, enabled);
        setCompEnabled(bUndo, enabled);
        setCompEnabled(bPhaseOverrides, enabled);
    }

    private void setCompEnabled(Component comp, boolean enabled) {
        if (comp!=null) {
            comp.setEnabled(enabled);
            if((comp instanceof JToggleButton) && enabled && ((JToggleButton)comp).isSelected()){
                ((JCheckBox)comp).setSelected(false); //just to trigger an item event.
                ((JCheckBox)comp).setSelected(true);  //we change state to trigger an item event.
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


        if ((cbTransObjects!=null && cbTransObjects.isSelected()) & (transObjects!=null && transObjects.getText().length()<3)) {
            MBMsgBox mb = new MBMsgBox("Error", "You indicated that objects are to be transmitted but did not specify node.userid");
            return false;
        }

        if (!isFastTrack()) {
            if (cbGenDebugFiles!=null && cbGenDebugFiles.isSelected()) {
                options.setGenerateDebugFiles(true);

                if (cbSaveDebugFiles!=null && cbSaveDebugFiles.isSelected()) {
                    options.setSaveDebugFiles(true);
                } else {
                    options.setSaveDebugFiles(false);
                }

                if (cbXmitDebugFiles!=null && cbXmitDebugFiles.isSelected()) {
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

            if (extChkBox !=null && extChkBox.isSelected()) {
                if (extChkYesButton.isSelected()) {
                    options.setExtraDriverCheck("YES");
                } else {
                    options.setExtraDriverCheck("INACTIVE");
                }
            } else {
                options.setExtraDriverCheck(null);
            }
        }

        if (transObjects!=null) {
            options.setXmitTo(makeEmptyStringsNull(transObjects.getText()));
        }

        if (transObjectType!=null) {
            if (!isFastTrack()) {
                options.setXmitType(makeEmptyStringsNull((String)MVSObjectTypes.elementAt(transObjectType.getSelectedIndex())));
            } else {
                options.setXmitType(makeEmptyStringsNull((String)fasttrackMVSObjectTypes.elementAt(transObjectType.getSelectedIndex())));
            }
        }

        if (termLevel!=null) {
            options.setBuildCC(Integer.parseInt((String) termLevel.getSelectedItem()));
        }

        if (cbPurgeOutput!=null) {
            options.setPurgeJobsAfterCompletion(cbPurgeOutput.isSelected());
        }

        if (cbAutoPurgeSuccessfulJobs!=null) {
            options.setAutoPurgeSuccessfulJobs(cbAutoPurgeSuccessfulJobs.isSelected());
        }

        if (!isFastTrack()) {
            if (ListingOptionsCombo!=null) {
                if (ListingOptionsCombo.getSelectedItem().equals(SaveGoodListings)) {
                    options.setListGen("YES");
                } else if (ListingOptionsCombo.getSelectedItem().equals(SaveNoListings)) {
                    options.setListGen("NO");
                } else if (ListingOptionsCombo.getSelectedItem().equals(SaveFailedListings)) {
                    options.setListGen("FAIL");
                } else if (ListingOptionsCombo.getSelectedItem().equals(SaveAllListings)) {
                    options.setListGen("ALL");
                }
            }

            if (cbRunScan!=null) {
                options.setRunScanners(cbRunScan.isSelected());
            }
            if (cbSkipDCheck!=null) {
                options.setSkipDriverCheck(cbSkipDCheck.isSelected());
            }

            if (cbDryRun!=null) {
                options.setDryRun(cbDryRun.isSelected());
            }

            if (buildSettingCombo!=null) {
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

            }
            if (cbHaltOnShadCheckWarnings!=null) {
                options.setHaltOnShadowCheckWarnings(cbHaltOnShadCheckWarnings.isSelected());
            }
        } else {
            if (cbMetaData!=null) {
                ((BuildOptionsLocal)options).setEmbeddedMetadata(cbMetaData.isSelected());
            }
        }

        if (!libraryBuild) {
            BuildOptionsLocal localOptions = (BuildOptionsLocal) options;
            if (MaclibList!=null) {
                String cmacs = (String) MaclibList.getSelectedItem();
                if (cmacs==null) {
                    cbMaclibObjects.setSelected(false);
                }
            }
            if (cbMaclibObjects !=null && cbMaclibObjects.isSelected()) {
                String [] amacs = new String[MaclibList.getItemCount()];

                for (int idx=0; idx<MaclibList.getItemCount(); idx++) {
                    amacs[idx] = (String) MaclibList.getItemAt(idx);
                }
                localOptions.setUserMacs(amacs);
            } else {
                localOptions.setUserMacs(null);
            }

        }

        if (driverReportStep!=null && driverInfo!=null) {
            options.setOptionsBeenSet(true); 
        }
        fireEvent();
        return true;
    }

    public boolean isRequiredActionCompleted() {
        return options.isBeenSet() & !requirePageVisit;
    }

    public boolean saveNeeded() {
        boolean needed = false;
        if (options.getXmitTo() != null) {
            needed = needed | !options.getXmitTo().equals(transObjects.getText());
        } else {
            needed = transObjects.getText().trim().length() >0;
        }
        if (options.getXmitType() != null) {
            if (!isFastTrack()) {
                needed = needed | !options.getXmitType().equals((String)MVSObjectTypes.elementAt(transObjectType.getSelectedIndex()));
            } else {
                needed = needed | !options.getXmitType().equals((String)fasttrackMVSObjectTypes.elementAt(transObjectType.getSelectedIndex()));
            }
        } else {
            needed = true;
        }
        needed = needed | (options.getBuildCC()!= Integer.parseInt((String) termLevel.getSelectedItem()));
        needed = needed | options.isPurgeJobs() != cbPurgeOutput.isSelected();
        if (!isFastTrack()) {
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


    // set minimum size
    public Dimension getPreferredSize() {
        Dimension oldPref ;
        if (!isFastTrack()) {
            oldPref = new Dimension(425,super.getPreferredSize().height - 140) ;
        } else {
            oldPref = new Dimension(425, super.getPreferredSize().height);
        }
        return oldPref;
    }

    private String makeEmptyStringsNull(String temp) {
        if (temp!=null) {
            if (temp.trim().length() > 0) {
                return temp;
            }
        }
        return null;
    }

    public void addUserInterfaceEventListener(UserInterfaceEventListener listener) {
        changeManager.addUserInterfaceEventListener(listener);
    }

    public void visitingPage() {
        requirePageVisit = false;
        if (driverReportStep.getParser()==null&driverInfo!=null) {
            DriverReportFetcher processRunner = new DriverReportFetcher();
            processRunner.runTheProcess();
        } else {
            save();
            fireEvent();
        }

    }


    private void fireEvent() {
        UserInterfaceEvent newEvent = new UserInterfaceEvent(this);
        changeManager.fireEvent(newEvent);
    }

    public void handleUIEvent(UserInterfaceEvent e) {
        if (e instanceof ReleaseUpdateEvent) {
            ReleaseUpdateEvent event = (ReleaseUpdateEvent) e;
            if (relInfo==event.getReleaseInformation()) {
                return;
            }
            if (relInfo!=null) {
                if (relInfo.equals(event.getReleaseInformation())) {
                    return;
                }
            }
            if (relInfo!=null) {
                // if we're here, then we must have loaded other settings and we're about to overwrite those
                requirePageVisit=true;
            }
            relInfo = event.getReleaseInformation();
            if (relInfo!=null) {
                BuildOptions loadedOptions = (BuildOptions) com.ibm.sdwb.build390.userinterface.RememberedSettingsHandler.getInstance().getPerReleaseSetting(setup, relInfo.getLibraryName(),optionStorageKey);
                if (loadedOptions!=null) {
                    setOptions(loadedOptions);
                }
            }
            fireEvent();
        } else if (e instanceof BuildtypeUpdateEvent) {
            BuildtypeUpdateEvent event = (BuildtypeUpdateEvent) e;
            buildtype = event.getBuildtype();
        } else if (e instanceof DriverUpdateEvent) {
            DriverUpdateEvent event = (DriverUpdateEvent) e;
            if (driverInfo==event.getDriverInformation()) {
                return;
            }
            if (driverInfo!=null) {
                if (driverInfo.equals(event.getDriverInformation())) {
                    return;
                }
            }
            driverInfo = event.getDriverInformation();
            driverReportStep.setDriverInformation(driverInfo);
        }
    }

    private void saveOptionSettings() {
        if (relInfo!=null) {
            save();
            com.ibm.sdwb.build390.userinterface.RememberedSettingsHandler.getInstance().addPerReleaseSetting(setup, relInfo.getLibraryName(), optionStorageKey, options);
        }
    }



    private class Listener implements ItemListener {
        public void itemStateChanged(ItemEvent event) {
            bUndo.setEnabled(true);
        }
    }

    private class DriverReportFetcher extends CancelableProcess {
        private DriverReportFetcher() {
            super(driverReportStep.getProcess(), com.ibm.sdwb.build390.userinterface.graphic.utilities.GeneralUtilities.getParentAnimationStatus(thisPanel));
        }

        public void postAction() {
            if (!options.isBeenSet() && (driverReportStep!=null) && 
                driverReportStep.getParser()!=null && !justDisplay) {// this check should prevent overwrites of loaded options
                setOptions(options);
                save();
                fireEvent();
            }
        }
    }


    private class FrameListener extends InternalFrameAdapter {
        private boolean hasRun = false;
        /**
         * Invoked when an internal frame is in the process of being closed.
         * The close operation can be overridden at this point.
         */
        public void internalFrameClosed(InternalFrameEvent e) {
            if (!hasRun) {
                saveOptionSettings();
                hasRun = true;
            }
        }
    }

    public void ancestorAdded(AncestorEvent ae) {
        com.ibm.sdwb.build390.userinterface.graphic.utilities.GeneralUtilities.getParentInternalFrame((java.awt.Component) getParent()).addInternalFrameListener(new FrameListener());
    }

    public void ancestorMoved(AncestorEvent ae) {
    }

    public void ancestorRemoved(AncestorEvent ae) {
    }
}
