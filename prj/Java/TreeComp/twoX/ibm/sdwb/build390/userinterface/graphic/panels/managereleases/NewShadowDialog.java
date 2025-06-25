package com.ibm.sdwb.build390.userinterface.graphic.panels.managereleases;
/*********************************************************************/
/* NewShadowDialog class for the Build/390 client                  */
/*  Creates and manages the Driver Build Page                        */
/*********************************************************************/
// Changes
// Date     Defect/Feature      Reason
//10/27/2003 #DEF.INT1667:  Cannot create full delta driver
/*********************************************************************/

import java.awt.BorderLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.io.Serializable;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.Vector;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;

import com.ibm.sdwb.build390.*;
import com.ibm.sdwb.build390.help.HelpTopicID;
import com.ibm.sdwb.build390.library.LibraryInfo;
import com.ibm.sdwb.build390.mainframe.MainframeStorageParameters;
import com.ibm.sdwb.build390.mainframe.ReleaseAndDriverParameters;
import com.ibm.sdwb.build390.process.DriverCreationParameterReport;
import com.ibm.sdwb.build390.process.GetMVSSiteDefaults;
import com.ibm.sdwb.build390.user.Setup;

/** Create the driver build page */
public class NewShadowDialog extends MBModalStatusFrame implements Serializable, ItemListener {

    private boolean goodRel = false;
    private MBInternalFrame ParentFrame;
    private MBBuild     build = null;
    private String      basepath_ = new String();              // base path to build files
    private String      typeOfBuildToMake = null;
    private String      cmd = new String();
    private String      clrout_;                               // path to clrout
    private boolean     new_shadow = true;
    private boolean     based   = false;
    private boolean     delta   = false;
    private boolean     fullDelta = false;
    private boolean     stopped = false;
    private boolean     initialized = false;
    private ReleaseAndDriverParameters parameters = null;
    private Hashtable   parmHash= new Hashtable();
    private Vector      sizes   = new Vector();
    private Setup     setup;

    private String libraryProject = null;
    private String mvsRelease = null;
    private String familyName = null;
    private String familyAddress = null;
    private String driver = null;
    private String baseDriver = null;
    private String driverSize = null;
    private boolean overrideDefaults = false;
    private String highLevelQualifier = null;

    private static final String HashFile = new String("shadowdef.ser");
    private static final String DEFAULTS = new String("shadowdef.host");
    public static final String OVERRIDE = "OVERRIDE";

    //{{DECLARE_CONTROLS
    private JLabel Label01 = new JLabel("Information");
    private JLabel Label02 = new JLabel("name");
    private JLabel Label03 = new JLabel("address");
    private JLabel Label04 = new JLabel("Release name");

    private JLabel Label05 = new JLabel("S/390 Information");
    private JLabel Label06 = new JLabel("High level index");
    private JLabel Label07 = new JLabel("S/390 Release name");
    private JLabel Label08 = new JLabel("Additional collectors");
    private JLabel Label09 = new JLabel("Additional process steps");

    private JLabel Label10 = new JLabel(" Storage Information");
    private JLabel Label11 = new JLabel("  DASD volume serial");
    private JLabel Label12 = new JLabel("    AND/OR");
    private JLabel Label13 = new JLabel("  SMS Storage class");
    private JLabel Label14 = new JLabel("  SMS Management class");
    private JLabel Label15 = new JLabel("Unibank primary space");
    private JLabel Label16 = new JLabel("Bulk primary space");
    private JLabel Label17 = new JLabel("Bulk secondary space");
    private JLabel Label18 = new JLabel("Bulk max size");
    private JLabel Label19 = new JLabel("Bulk max extents");

    private JLabel Label20 = new JLabel("S/390 Driver name");

    private JLabel Label21 = new JLabel("S/390 Base Driver name");
    private JLabel Label23 = new JLabel("S/390 Driver size");

    private JTextField tfHlq               = new JTextField(10);
    private JTextField tfMvsRel            = new JTextField(10);
    private JTextField tfFamily            = new JTextField(25);
    private JTextField tfFamAddr           = new JTextField(25);
    private JTextField tfLibRel            = new JTextField(25);
    private JTextField tfVolid             = new JTextField(10);
    private JTextField tfStgClass          = new JTextField(10);
    private JTextField tfMgtClass          = new JTextField(10);
    private JTextField tfUbkSpace          = new JTextField(6);
    private JTextField tfUbkCollectors     = new JTextField(6);
    private JTextField tfUbkSteps          = new JTextField(6);
    private JTextField tfBulkPrimarySpace  = new JTextField(6);
    private JTextField tfBulkSecondaySpace = new JTextField(6);
    private JTextField tfBulkMaxSize       = new JTextField(6);
    private JTextField tfBulkMaxExtents    = new JTextField(6);

    private JTextField tfMvsDriver         = new JTextField(6);
    private String newdrvr                 = new String();
    private JTextField tfMvsDriverBase     = new JTextField(6);
    private JCheckBox  cbMvsDriverDrag     = new JCheckBox("All parts in delta");

    private JComboBox  coMvsDriverSize     = new JComboBox();

    private int SaveUbkSpace = 0;
    private int SaveBulkPSpace = 0;
    private String baseRelease = null;

    //DEF.PTM2074:
    private JButton btoverride = new JButton("Select Allocation Overrides");

    private JButton btHelp = new JButton("Help");
    private JButton btOk = new JButton("Ok");

    protected GridBagLayout gridBag = new GridBagLayout();
    protected JPanel centerPanel = new JPanel(gridBag);

    // DriverAlloc, need to be able to access this from UseSelectedDriverSize
    private Hashtable chash = new Hashtable();

    private JCheckBox overrideCheckBox = new JCheckBox("Override Default Allocations");

    /**
    * constructor - Create a MBNewShadowDialog
    * @param MBGUI gui
    */
    public NewShadowDialog(MBBuild tempBuild, String dtype, boolean tempBased, boolean tempDelta, String aparName, String tempHlq, String tempBaseRelease, String tempBaseDriver,  ReleaseAndDriverParameters inParms, MBInternalFrame pFrame)throws com.ibm.sdwb.build390.MBBuildException{
        super("New Release/Driver Dialog", pFrame, null);
        // lep.addEventListener(returnBuild.getLogListener());
        if (dtype.equals("DRIVER")) {
            new_shadow = false;
        }
        parameters = inParms;
        ParentFrame = pFrame;
        build = tempBuild;
        based = tempBased;
        baseDriver = tempBaseDriver;
        mvsRelease = tempBaseRelease;
        highLevelQualifier = tempHlq;
        setup = build.getSetup();

        delta = tempDelta;
        newdrvr = aparName;

        initializeDialog();
    }

    public String getLibraryProject() {
        return libraryProject;
    }

    public String getMVSRelease() {
        return mvsRelease;
    }

    public String getFamilyName() {
        return familyName;
    }

    public String getFamilyAddress() {
        return familyAddress;
    }

    public String getDriver() {
        return driver;
    }

    public String getBaseDriver() {
        return baseDriver;
    }

    public String getDriverSize() {
        return driverSize;
    }

    public boolean isDelta() {
        return delta;
    }

    public boolean isFullDelta() {
        return fullDelta;
    }

    public boolean isOverrideSet() {
        return overrideDefaults;
    }

    public ReleaseAndDriverParameters getCreationParameters() {
        return parameters;
    }

    private void initializeDialog() throws com.ibm.sdwb.build390.MBBuildException{
        getContentPane().setLayout(new BorderLayout());
        setForeground(MBGuiConstants.ColorRegularText);
        setBackground(MBGuiConstants.ColorGeneralBackground);

        btHelp.setForeground(MBGuiConstants.ColorHelpButton);
        btHelp.addActionListener
        (new MBCancelableActionListener(thisFrame) {
             public void doAction(ActionEvent evt) {
                 if (new_shadow) MBUtilities.ShowHelp("HDRCR",HelpTopicID.NEWSHADOWDIALOG0_HELP);
                 else MBUtilities.ShowHelp("HDRCAD",HelpTopicID.NEWSHADOWDIALOG1_HELP);
             }
         } );
        coMvsDriverSize.setForeground(MBGuiConstants.ColorActionButton);
        coMvsDriverSize.addActionListener
        (new MBCancelableActionListener(thisFrame) {
             public void doAction(ActionEvent evt) {
                 UseSelectedDriverSize();
             }
         });


        // DriverAlloc, DrvrDrag, update defaults when selecting thin or full
        cbMvsDriverDrag.addActionListener
        (new MBCancelableActionListener(thisFrame) {
             public void doAction(ActionEvent evt) {
                 UseSelectedDriverSize();
             }
         });

        btoverride.setForeground(MBGuiConstants.ColorActionButton);
        btoverride.addActionListener
        (new MBCancelableActionListener(thisFrame) {
             public void doAction(ActionEvent evt) {
                 try {
                     MBDriverOverrides dro = new MBDriverOverrides(build.getSetup(), parameters, delta, parmHash, ParentFrame);
                 } catch (MBBuildException e) {
                 }
                 // DrvrCreate
                 tfBulkPrimarySpace.setText((String)parmHash.get("tfBulkPrimarySpace"));
                 tfBulkSecondaySpace.setText((String)parmHash.get("tfBulkSecondaySpace"));
                 tfUbkSpace.setText((String)parmHash.get("tfUbkSpace"));
                 tfBulkMaxSize.setText((String)parmHash.get("tfBulkMaxSize"));
                 tfBulkMaxExtents.setText((String)parmHash.get("tfBulkMaxExtents"));
                 tfVolid.setText((String)parmHash.get("tfVolid"));
                 tfStgClass.setText((String)parmHash.get("tfStgClass"));
                 tfMgtClass.setText((String)parmHash.get("tfMgtClass"));
             }
         });
        btOk.setForeground(MBGuiConstants.ColorActionButton);
        btOk.addActionListener
        (new MBCancelableActionListener(thisFrame) {
             public void doAction(ActionEvent evt) {
                 // save info
                 saveEntrys();

                 // check entrys
                 String errorData = "";
                 String fFamily  = new String();
                 String fFamAddr = new String();
                 String fLibRel  = new String();
                 String fMvsDriver = new String();
                 String fUbkCollectors = new String();
                 String fUbkSteps = new String();
                 String fMvsDriverBase = new String();
                 String fMvsDriverSize = new String();
                 boolean bMvsDriverDrag = false;
                 if (new_shadow) {
                     fFamily = tfFamily.getText().trim();
                     if (fFamily.length() < 1)
                         errorData+= "You must specify a Library name.\n";

                     fLibRel = tfLibRel.getText().trim();
                     if (fLibRel.length() < 1) {
                         errorData+= "You must specify a " + getLibraryTextPrefix()+ " Release name.\n";
                     }
                     if (fLibRel.matches(".*\\s.*")) {
                         errorData+= "You must specify a " + getLibraryTextPrefix() + " Release with no spaces.\n";
                     }

                 }

                 String fHlq = tfHlq.getText().trim().toUpperCase();
                 if (fHlq.length() < 1 | fHlq.length() > 8) {
                     errorData+= "You must specify a high level index from 1 to 8 characters.\n";
                 }

                 String fMvsRel = tfMvsRel.getText().trim().toUpperCase();
                 if (fMvsRel.length() < 1 | fMvsRel.length() > 8) {
                     errorData+= "You must specify a S/390 Release from 1 to 8 characters.\n";
                 }

                 if (fMvsRel.matches(".*\\s.*")) {
                     errorData+= "You must specify a S/390 Release with no spaces.\n";
                 }

                 if (!new_shadow) {

                     fMvsDriver = tfMvsDriver.getText().trim().toUpperCase();
                     if (fMvsDriver.length() < 1 | fMvsDriver.length() > 8) {
                         errorData+= "You must specify a S/390 Driver from 1 to 8 characters.\n";
                     }
                     if (fMvsDriver.matches(".*\\s.*")) {
                         errorData+= "You must specify a S/390 Driver with no spaces.\n";
                     }
                     if (based) {
                         fMvsDriverBase = tfMvsDriverBase.getText().trim().toUpperCase();
                         if (fMvsDriverBase.length() < 1 | (fMvsDriverBase.length() > 8 & fMvsDriverBase.indexOf(".") <0)) {
                             errorData+= "You must specify a S/390 Driver base from 1 to 8 characters.\n";
                         }

                         fMvsDriverSize = ((String)(coMvsDriverSize.getSelectedItem())).trim().toUpperCase();

                         if (fMvsDriverSize.length() < 1 ) {
                             errorData+= "You must specify a S/390 Driver size.\n";
                         }
                         bMvsDriverDrag = cbMvsDriverDrag.isSelected();
                     }

                 }

                 String fVolid = new String();
                 String fStgClass = new String();
                 String fMgtClass = new String();
                 String fUbkSpace = new String();
                 String fBulkPrimarySpace = new String();
                 String fBulkSecondaySpace = new String();
                 String fBulkMaxSize = new String();
                 String fBulkMaxExtents = new String();

                 //if (new_shadow | !based) {
                 fVolid = tfVolid.getText().trim().toUpperCase();
                 fStgClass = tfStgClass.getText().trim().toUpperCase();
                 fMgtClass = tfMgtClass.getText().trim().toUpperCase();
                 if (fVolid.length() < 1 & fStgClass.length() < 1 & fMgtClass.length() < 1) {
                     errorData+= "You must specify either a DASD volume serial or the SMS storage class and management class.\n";
                 }


                 if(!(new MainframeStorageParameters(fVolid, fStgClass,fMgtClass)).combinationAllowed()){
                     errorData+= "You must specify a valid storage parameter combination as shown below.\n" + 
                         MainframeStorageParameters.TABLE_TEXT;
                 }

                 // Check ubk space for range 1-999, if copying a base to base check for ubk space >= bases ubk space
                 fUbkSpace = tfUbkSpace.getText().trim();
                 int ifUbkSpace = 0;
                 try {
                     if (fUbkSpace.length() > 0) ifUbkSpace = (new Integer(fUbkSpace)).intValue();
                 } catch (NumberFormatException n) {
                 }
                 if (!delta | cbMvsDriverDrag.isSelected()) { // full delta or not a delta
                     if (ifUbkSpace < SaveUbkSpace) {
                         errorData+= "You must specify the Unibank primary space greater than or equal to that of the base driver which is "+ SaveUbkSpace +".\n";
                     }
                 } else if (ifUbkSpace < 1 | ifUbkSpace > 999)
                     errorData+= "You must specify the Unibank primary space in cylinders from 1 to 999.\n";

                 if (new_shadow) {
                     fUbkCollectors = tfUbkCollectors.getText().trim();
                     int ifUbkCollectors = 0;
                     try {
                         if (fUbkCollectors.length() > 0) ifUbkCollectors = (new Integer(fUbkCollectors)).intValue();
                     } catch (NumberFormatException n) {
                     }
                     if (ifUbkCollectors < 0 | ifUbkCollectors > 99)
                         errorData+= "You must specify the Additional collectors from 0 to 99.\n";

                     fUbkSteps = tfUbkSteps.getText().trim();
                     int ifUbkSteps = 0;
                     try {
                         if (fUbkSteps.length() > 0) ifUbkSteps = (new Integer(fUbkSteps)).intValue();
                     } catch (NumberFormatException n) {
                     }
                     if (ifUbkSteps < 0 | ifUbkSteps > 99)
                         errorData+= "You must specify the Additional process steps from 0 to 99.\n";
                 }

                 // Check bulkp space for range 1-999, if copying a base to base check for bulkp space >= bases bulkp space
                 fBulkPrimarySpace = tfBulkPrimarySpace.getText().trim();
                 int ifBulkPrimarySpace = 0;
                 try {
                     if (fBulkPrimarySpace.length() > 0) ifBulkPrimarySpace = (new Integer(fBulkPrimarySpace)).intValue();
                 } catch (NumberFormatException n) {
                 }
                 if (!delta) {
                     if (ifBulkPrimarySpace < SaveBulkPSpace) {
                         errorData+= "You must specify the Bulk primary space greater than or equal to that of the base driver which is "+ SaveBulkPSpace +".\n";
                     }
                 } else if (ifBulkPrimarySpace < 1 | ifBulkPrimarySpace > 9999) /*PTM2741 */
                     errorData+= "You must specify the Bulk primary space in cylinders from 1 to 999.\n";

                 fBulkSecondaySpace = tfBulkSecondaySpace.getText().trim();
                 int ifBulkSecondaySpace = 0;
                 try {
                     if (fBulkSecondaySpace.length() > 0) ifBulkSecondaySpace = (new Integer(fBulkSecondaySpace)).intValue();
                 } catch (NumberFormatException n) {
                 }
                 if (ifBulkSecondaySpace < 1 | ifBulkSecondaySpace > 999)
                     errorData+= "You must specify the Bulk secondary space in cylinders from 1 to 999.\n";

                 fBulkMaxSize = tfBulkMaxSize.getText().trim();
                 int ifBulkMaxSize = 0;
                 try {
                     if (fBulkMaxSize.length() > 0) ifBulkMaxSize = (new Integer(fBulkMaxSize)).intValue();
                 } catch (NumberFormatException n) {
                 }

                 if (ifBulkMaxSize < 1 | ifBulkMaxSize > 9999) {
                     errorData+= "You must specify the Bulk max size in cylinders from 1 to 9999.\n";
                 }

                 if (ifBulkMaxSize< ifBulkPrimarySpace) {
                     errorData+= "'Bulk max size' cannot be less than 'Bulk primary space'.\n";
                 }

                 fBulkMaxExtents = tfBulkMaxExtents.getText().trim();
                 int ifBulkMaxExtents = 0;
                 try {
                     if (fBulkMaxExtents.length() > 0) ifBulkMaxExtents = (new Integer(fBulkMaxExtents)).intValue();
                 } catch (NumberFormatException n) {
                 }

                 if (ifBulkMaxExtents < 1 | ifBulkMaxExtents > 123) {
                     errorData+= "You must specify the Bulk max extents from 1 to 123.\n";
                 }

                 if ((ifBulkPrimarySpace + ifBulkSecondaySpace) > ifBulkMaxSize ) {
                     errorData+= "BulkPrime + BulkSecond must be less than or equal to BulkMax.\n";
                 }

                 if (!errorData.equals("")) {
                     new MBMsgBox("Error", errorData);

                 } else {
                     // check for valid lib release
                     if (new_shadow) {
                         getStatus().updateStatus("Checking library release", false);
                         goodRel = false;
                         LibraryInfo lib = setup.getLibraryInfo();
                         try {
                             lib.isValidLibraryProject(fLibRel); 
                             goodRel = true; 
                         } catch (MBBuildException mbe) {
                             goodRel = false;
                             lep.LogException(mbe);
                         }

                         getStatus().clearStatus();

                     }

                     // set command parms
                     if (goodRel | !new_shadow) {
                         parameters.setHighLevelQualifier(fHlq);
                         if (new_shadow) {
                             familyName = fFamily;
                             familyAddress = fFamAddr;
                             libraryProject = fLibRel;
                             mvsRelease = fMvsRel;
                             parameters.setAdditionalCollectors(fUbkCollectors);
                             parameters.setAdditionalProcessSteps(fUbkSteps);
                         }
                         // not a new shadow
                         else {
                             if (!based) {
                                 mvsRelease = fMvsRel;
                                 driver = fMvsDriver;
                             } else {
                                 mvsRelease = fMvsRel;
                                 driver = fMvsDriver;
                             }
                             baseDriver = fMvsDriverBase;
                             driverSize = fMvsDriverSize;
                             fullDelta = bMvsDriverDrag;
                         }
                         if (new_shadow) {
                             parameters.setShadowBulkDatasetPrimarySpaceInCylinders(fBulkPrimarySpace);
                             parameters.setShadowBulkDatasetSecondarySpaceInCylinders(fBulkSecondaySpace);
                             parameters.setShadowUnibankDatasetPrimarySpaceInCylinders(fUbkSpace);
                         } else {
                             parameters.setDriverBulkDatasetPrimarySpaceInCylinders(fBulkPrimarySpace);
                             parameters.setDriverBulkDatasetSecondarySpaceInCylinders(fBulkSecondaySpace);
                             parameters.setDriverUnibankDatasetPrimarySpaceInCylinders(fUbkSpace);
                         }
                         parameters.setBulkDatasetMaximumExtentsInCylinders(fBulkMaxExtents);
                         parameters.setBulkDatasetMaximumSizeInCylinders(fBulkMaxSize);

                         if (fVolid.length() > 0) {
                             parameters.setDASDVolumeIdentifier(fVolid);
                         }
                         if (fStgClass.length() > 0) { /* INT1765 */
                             parameters.setSMSStorageClass(fStgClass);
                         }
                         if (fMgtClass.length() > 0) { /* INT1765 */
                             parameters.setSMSManagementClass(fMgtClass);
                         }
                         overrideDefaults = overrideCheckBox.isSelected();
                         initialized = true;
                         dispose();
                     }
                 }
             }
         } );

        Vector actionButtons = new Vector();
        actionButtons.addElement(btOk);
        addButtonPanel(btHelp, actionButtons);

        GridBagConstraints c = new GridBagConstraints();
        c.weighty = 1;
        c.gridx = 1;
        c.gridy = 1;
        c.anchor = GridBagConstraints.WEST;
        c.insets = new Insets(2,5,2,5);

        if (new_shadow) {
            Label01.setText(getLibraryTextPrefix() + Label01.getText());
            gridBag.setConstraints(Label01, c);
            centerPanel.add(Label01);
            c.gridy++;
            Label02.setText(getLibraryTextPrefix() + Label02.getText());
            gridBag.setConstraints(Label02, c);
            centerPanel.add(Label02);
            c.gridy++;
            Label03.setText(getLibraryTextPrefix() + Label03.getText());
            gridBag.setConstraints(Label03, c);
            centerPanel.add(Label03);
            c.gridy++;
            Label04.setText(getLibraryTextPrefix() + Label04.getText());
            gridBag.setConstraints(Label04, c);
            centerPanel.add(Label04);
        }

        c.gridy++;
        gridBag.setConstraints(Label05, c);
        centerPanel.add(Label05);
        c.gridy++;
        gridBag.setConstraints(Label06, c);
        centerPanel.add(Label06);
        c.gridy++;
        gridBag.setConstraints(Label07, c);
        centerPanel.add(Label07);

        if (!new_shadow) {
            if (based) {
                c.gridy++;
                gridBag.setConstraints(Label21, c);
                centerPanel.add(Label21);
            }
            c.gridy++;
            gridBag.setConstraints(Label20, c);
            centerPanel.add(Label20);
            if (delta) {
                c.gridy++;
                gridBag.setConstraints(Label23, c);
                centerPanel.add(Label23);
                c.gridy++;
                gridBag.setConstraints(cbMvsDriverDrag, c);
                centerPanel.add(cbMvsDriverDrag);
            }
        }

        if (new_shadow) {
            c.gridy++;
            gridBag.setConstraints(Label08, c);
            centerPanel.add(Label08);
            c.gridy++;
            gridBag.setConstraints(Label09, c);
            centerPanel.add(Label09);
        }

        if (new_shadow | !based) { // num_parts
            c.gridy++;
            gridBag.setConstraints(Label10, c);
            centerPanel.add(Label10);
            c.gridy++;
            gridBag.setConstraints(Label11, c);
            centerPanel.add(Label11);

            c.gridy++;
            gridBag.setConstraints(Label12, c);
            centerPanel.add(Label12);

            c.gridy++;
            gridBag.setConstraints(Label13, c);
            centerPanel.add(Label13);
            c.gridy++;
            gridBag.setConstraints(Label14, c);
            centerPanel.add(Label14);

            c.gridy++;
            gridBag.setConstraints(Label15, c);
            centerPanel.add(Label15);
            c.gridy++;
            gridBag.setConstraints(Label16, c);
            centerPanel.add(Label16);
            c.gridy++;
            gridBag.setConstraints(Label17, c);
            centerPanel.add(Label17);
            c.gridy++;
            gridBag.setConstraints(Label18, c);
            centerPanel.add(Label18);
            c.gridy++;
            gridBag.setConstraints(Label19, c);
            centerPanel.add(Label19);
        }

        // entry fields
        c.gridy = 1;
        c.gridx = 2;
        c.weightx = 1;
        c.fill = GridBagConstraints.HORIZONTAL;

        if (new_shadow) {
            c.gridy++;
            gridBag.setConstraints(tfFamily, c);
            centerPanel.add(tfFamily);
            tfFamily.setEditable(false);  // Defect_119
            c.gridy++;
            gridBag.setConstraints(tfFamAddr, c);
            centerPanel.add(tfFamAddr);
            tfFamAddr.setEditable(false); // Defect_119
            c.gridy++;
            gridBag.setConstraints(tfLibRel, c);
            centerPanel.add(tfLibRel);
        }

        c.gridy=c.gridy+2;
        gridBag.setConstraints(tfHlq, c);
        centerPanel.add(tfHlq);
        tfHlq.setEditable(new_shadow);
        c.gridy++;
        gridBag.setConstraints(tfMvsRel, c);
        centerPanel.add(tfMvsRel);
        if (!new_shadow) tfMvsRel.setEditable(false);

        if (!new_shadow) {
            if (based) {
                c.gridy++;
                gridBag.setConstraints(tfMvsDriverBase, c);
                centerPanel.add(tfMvsDriverBase);
                tfMvsDriverBase.setEditable(false);
            }
            c.gridy++;
            gridBag.setConstraints(tfMvsDriver, c);
            centerPanel.add(tfMvsDriver);
            // 2/3/99, Chris, service support
            if (newdrvr != null)
                tfMvsDriver.setEditable(false);
            if (delta) {
                c.gridy++;
                gridBag.setConstraints(coMvsDriverSize, c);
                centerPanel.add(coMvsDriverSize);
                c.gridy++;
            }
        }

        if (new_shadow) {
            c.gridy++;
            gridBag.setConstraints(tfUbkCollectors, c);
            centerPanel.add(tfUbkCollectors);
            c.gridy++;
            gridBag.setConstraints(tfUbkSteps, c);
            centerPanel.add(tfUbkSteps);
        }

        if (new_shadow | !based) { // num_parts
            c.gridy=c.gridy+2;
            gridBag.setConstraints(tfVolid, c);
            centerPanel.add(tfVolid);
            c.gridy=c.gridy+2;
            gridBag.setConstraints(tfStgClass, c);
            centerPanel.add(tfStgClass);
            c.gridy++;
            gridBag.setConstraints(tfMgtClass, c);
            centerPanel.add(tfMgtClass);
            c.gridy++;
            gridBag.setConstraints(tfUbkSpace, c);
            centerPanel.add(tfUbkSpace);
            c.gridy++;
            gridBag.setConstraints(tfBulkPrimarySpace, c);
            centerPanel.add(tfBulkPrimarySpace);
            c.gridy++;
            gridBag.setConstraints(tfBulkSecondaySpace, c);
            centerPanel.add(tfBulkSecondaySpace);
            c.gridy++;
            gridBag.setConstraints(tfBulkMaxSize, c);
            centerPanel.add(tfBulkMaxSize);
            c.gridy++;
            gridBag.setConstraints(tfBulkMaxExtents, c);
            centerPanel.add(tfBulkMaxExtents);
        } else { // num_parts

            //Begin DEF.PTM2074:
            c.gridy++;
            c.gridx--;
            gridBag.setConstraints(overrideCheckBox, c);
            centerPanel.add(overrideCheckBox);

            overrideCheckBox.addItemListener(this);

            btoverride.setEnabled(false);
            //End DEF.PTM2074:

            c.gridy++;
            gridBag.setConstraints(btoverride, c);
            centerPanel.add(btoverride);
        }

        getContentPane().add("Center", centerPanel);

        Label01.setForeground(MBGuiConstants.ColorGroupHeading);
        Label05.setForeground(MBGuiConstants.ColorGroupHeading);
        Label10.setForeground(MBGuiConstants.ColorGroupHeading);
        Label12.setForeground(MBGuiConstants.ColorGroupHeading);

    }

    public void saveEntrys() {
        parmHash.put("tfFamily", tfFamily.getText());
        parmHash.put("tfFamAddr", tfFamAddr.getText());
        parmHash.put("tfLibRel", tfLibRel.getText());
        parmHash.put("tfHlq", tfHlq.getText());
        parmHash.put("tfMvsRel", tfMvsRel.getText());
        parmHash.put("tfMvsDriverBase", tfMvsDriverBase.getText());
        parmHash.put("tfBulkPrimarySpace", tfBulkPrimarySpace.getText());
        parmHash.put("tfBulkSecondaySpace", tfBulkSecondaySpace.getText());
        parmHash.put("tfUbkSpace", tfUbkSpace.getText());
        parmHash.put("tfBulkMaxSize", tfBulkMaxSize.getText());
        parmHash.put("tfBulkMaxExtents", tfBulkMaxExtents.getText());
        parmHash.put("tfVolid", tfVolid.getText());
        parmHash.put("tfStgClass", tfStgClass.getText());
        parmHash.put("tfMgtClass", tfMgtClass.getText());
        parmHash.put("tfUbkCollectors", tfUbkCollectors.getText());
        parmHash.put("tfUbkSteps", tfUbkSteps.getText());

    }

    public void postVisibleInitialization() {
        SwingUtilities.invokeLater(new Runnable() {
                                       public void run() {
                                           if (new_shadow) {
                                               tfLibRel.requestFocus();

                                           } else {
                                               tfMvsDriver.requestFocus();
                                           }
                                       }
                                   });
    }

    protected void initializeDefaults(ReleaseAndDriverParameters defaultSettings) throws com.ibm.sdwb.build390.MBBuildException{
        boolean sizesSet = false;
        

        if (new_shadow) {
            parmHash.put("tfHlq", defaultSettings.getHighLevelQualifier());
            parmHash.put("tfBulkPrimarySpace", defaultSettings.getShadowBulkDatasetPrimarySpaceInCylinders());
            parmHash.put("tfBulkSecondaySpace", defaultSettings.getShadowBulkDatasetSecondarySpaceInCylinders());
            parmHash.put("tfUbkSpace", defaultSettings.getShadowUnibankDatasetPrimarySpaceInCylinders());
        } else {
            parmHash.put("tfHlq", highLevelQualifier);  // if a new driver, use the same hlq as the release
            parmHash.put("tfBulkPrimarySpace", defaultSettings.getDriverBulkDatasetPrimarySpaceInCylinders());
            parmHash.put("tfBulkSecondaySpace", defaultSettings.getDriverBulkDatasetSecondarySpaceInCylinders());
            parmHash.put("tfUbkSpace", defaultSettings.getDriverUnibankDatasetPrimarySpaceInCylinders());
        }
        parmHash.put("tfBulkMaxSize", defaultSettings.getBulkDatasetMaximumSizeInCylinders());
        parmHash.put("tfBulkMaxExtents", defaultSettings.getBulkDatasetMaximumExtentsInCylinders());
        parmHash.put("tfStgClass", defaultSettings.getSMSStorageClass());
        parmHash.put("tfMgtClass", defaultSettings.getSMSManagementClass());
        parmHash.put("tfUbkCollectors", defaultSettings.getAdditionalCollectors());
        parmHash.put("tfUbkSteps",  defaultSettings.getAdditionalProcessSteps());
        parmHash.put("SIZES", defaultSettings.getDriverSizes());
        // get release
        if (mvsRelease != null) {
            if (mvsRelease.indexOf(".")<0)
                parmHash.put("tfMvsRel", mvsRelease);
            else parmHash.put("tfMvsRel", mvsRelease.substring(0,mvsRelease.indexOf(".")));
        }

        if (baseDriver != null) {
            parmHash.put("tfMvsDriverBase", baseDriver);
        }
        // set family stuff to setup info
        parmHash.put("tfFamily", setup.getLibraryInfo().getProcessServerName());
        if (setup.getLibraryInfo().getProcessServerAddress()!=null) {
            parmHash.put("tfFamAddr", setup.getLibraryInfo().getProcessServerAddress());
        } else {
            parmHash.put("tfFamAddr", "");
        }

        // if this is a copy request or a delta, submit op=check to get default space settings
        // DriverAlloc -
        //  New Base Driver - use defaults
        //  New base copied - UBKSP = Bankused
        //                    BULKP = Larger of Bulkcyl or Pricyl
        //                    BULKS = Seccyl
        //  New Full Detla  - UBKSP = Larger of Bankused or Value 1 from size selection
        //                    BULKP = Second value from size selection
        //                    BULKS = Third value from size selection
        //  New Thin Detla  - UBKSP = First value from size selection
        //                    BULKP = Second value from size selection
        //                    BULKS = Third value from size selection
        if (based) {
            String basedRelease = null;
            String basedTempDriver = null;
            if (baseDriver.indexOf(".") <0) {
                basedRelease = mvsRelease;
                basedTempDriver = baseDriver;
            } else {
                basedRelease = baseDriver.substring(0,baseDriver.indexOf("."));
                basedTempDriver = baseDriver.substring(baseDriver.indexOf(".")+1);    
            }

            try {
                DriverCreationParameterReport parameterReportProcess = new DriverCreationParameterReport(build, highLevelQualifier, basedRelease , basedTempDriver, this);
                parameterReportProcess.externalRun();
                // if process failed, update status and get out
                if (parameterReportProcess.isCheckSuccessful()) {
                    chash.putAll(parameterReportProcess.getSettingMap());
                    if (!delta) {
                        String bulkcyl = new String();
                        String pricyl  = new String();
                        if ((String)chash.get("CUBKP") != null)
                            parmHash.put("tfUbkSpace", (String)chash.get("CUBKP"));           // Bankused
                        if ((String)chash.get("BULKCYL") != null)
                            bulkcyl = (String)chash.get("BULKCYL");                           // Bulkcyl
                        if ((String)chash.get("CBLKP") != null)
                            pricyl  = (String)chash.get("CBLKP");                            // Pricyl
                        Integer ix = new Integer(0);
                        if ((ix.decode(bulkcyl)).intValue() > (ix.decode(pricyl)).intValue()) {
                            parmHash.put("tfBulkPrimarySpace", (String)chash.get("BULKCYL"));
                        } else {
                            parmHash.put("tfBulkPrimarySpace", (String)chash.get("CBLKP"));
                        }
                        if ((String)chash.get("CBLKS") != null)
                            parmHash.put("tfBulkSecondaySpace", (String)chash.get("CBLKS")); // Seccyl
                    }
                    // otherwise they will be set in UseSelectedDriverSize

                    if ((String)chash.get("CMAXCYL") != null)
                        parmHash.put("tfBulkMaxSize", (String)chash.get("CMAXCYL"));
                    if ((String)chash.get("CMAXEXT") != null)
                        parmHash.put("tfBulkMaxExtents", (String)chash.get("CMAXEXT"));
                  if ((String)chash.get("CSTGCLS") != null)
                        parmHash.put("tfStgClass", (String)chash.get("CSTGCLS"));
                    else parmHash.put("tfStgClass","");//INT3094
                    if ((String)chash.get("CMGTCLS") != null)
                        parmHash.put("tfMgtClass", (String)chash.get("CMGTCLS"));
                    else parmHash.put("tfMgtClass", "");//INT3094
                    if ((String)chash.get("CVOLID") != null)
                        parmHash.put("tfVolid", (String)chash.get("CVOLID"));
                    // get size info
                    int SIZEKeywordCount = getKeywordCount("SIZE");
                    for (int cnt=1; cnt < SIZEKeywordCount; cnt++) {
                        String name = (String)chash.get("SIZE"+cnt+"_NAME");
                        if (name != null) {
                            sizesSet = true;
                            coMvsDriverSize.addItem(name);
                            parmHash.put("SIZE"+cnt+"_NAME", name);
                            parmHash.put("SIZE"+cnt+"_UBKP", (String)chash.get("SIZE"+cnt+"_UBKP"));
                            parmHash.put("SIZE"+cnt+"_BLKP", (String)chash.get("SIZE"+cnt+"_BLKP"));
                            parmHash.put("SIZE"+cnt+"_BLKS", (String)chash.get("SIZE"+cnt+"_BLKS"));
                        }
                    }
                } else {
                    ParentFrame.getStatus().updateStatus("Check of base driver failed", false);
                    dispose();
                    return;
                }


            } catch (MBBuildException mbe  ) {
                ParentFrame.getStatus().updateStatus("Check of base driver failed", false);
                dispose();
                throw mbe;
            }
        }

        // set fields
        tfFamily.setText((String)parmHash.get("tfFamily"));
        tfFamAddr.setText((String)parmHash.get("tfFamAddr"));
        tfLibRel.setText((String)parmHash.get("tfLibRel"));
        tfHlq.setText((String)parmHash.get("tfHlq"));
        tfMvsRel.setText((String)parmHash.get("tfMvsRel"));
        tfMvsDriverBase.setText((String)parmHash.get("tfMvsDriverBase"));
        tfBulkPrimarySpace.setText((String)parmHash.get("tfBulkPrimarySpace"));
        tfBulkSecondaySpace.setText((String)parmHash.get("tfBulkSecondaySpace"));
        tfUbkSpace.setText((String)parmHash.get("tfUbkSpace"));
        tfBulkMaxSize.setText((String)parmHash.get("tfBulkMaxSize"));
        tfBulkMaxExtents.setText((String)parmHash.get("tfBulkMaxExtents"));
        tfVolid.setText((String)parmHash.get("tfVolid"));
        tfStgClass.setText((String)parmHash.get("tfStgClass"));
        tfMgtClass.setText((String)parmHash.get("tfMgtClass"));
        tfUbkCollectors.setText((String)parmHash.get("tfUbkCollectors"));
        tfUbkSteps.setText((String)parmHash.get("tfUbkSteps"));
        if (newdrvr != null) tfMvsDriver.setText(newdrvr);

        String drag =  new String();
        drag = (String)parmHash.get("cbMvsDriverDrag");
        cbMvsDriverDrag.setSelected(false);
        if (drag != null)
            if (drag.equals("YES"))
                cbMvsDriverDrag.setSelected(true);

            // Get sizes vector if not set from checkdriver
        if (!sizesSet) {
            Set sz = (Set)parmHash.get("SIZES");
            if (sz != null) {
                if (!sz.isEmpty()) {
                    sizes.removeAllElements();
                    Iterator en = sz.iterator();
                    while (en.hasNext()) {
                        String tmp = new String((String)en.next());
                        sizes.addElement(tmp);
                        coMvsDriverSize.addItem(tmp);
                    }
                    coMvsDriverSize.setSelectedIndex(0);
                }
            }
        }

        setVisible(true);

        if (delta) UseSelectedDriverSize();   
        
        try {
            SaveUbkSpace = (new Integer((String)parmHash.get("tfUbkSpace"))).intValue();
        } catch (NumberFormatException n) {
        }
        try {
            SaveBulkPSpace = (new Integer((String)parmHash.get("tfBulkPrimarySpace"))).intValue();
        } catch (NumberFormatException n) {
        }
    }

    private int getKeywordCount(String key) {
        int count=0;
        for (Iterator iter=chash.entrySet().iterator();iter.hasNext();) {
            Map.Entry entry = (Map.Entry)iter.next();
            if (((String)entry.getKey()).startsWith(key)) {
                count++;
            }
        }
        return count;
    }

    public boolean wasInitialized() {
        return initialized;
    }

    public void UseSelectedDriverSize() {
        // only mess with this stuff if this is a delta driver
    	
        if (delta) {
        	
        	      	
        	// DriverAlloc, init to bankused incase no sizes defined
            if (cbMvsDriverDrag.isSelected()) {
                // full delta
                parmHash.put("tfUbkSpace", (String)chash.get("CUBKP"));

                //#DEF.INT1667:
                fullDelta=true;
            }
            // get selected size
            String DriverSize = ((String)(coMvsDriverSize.getSelectedItem())).trim();
            // update setting based on size
            for (int cnt=1; cnt<4; cnt++) {
                if ((String)parmHash.get("SIZE"+cnt+"_NAME") != null) {
                    if (DriverSize.equals((String)parmHash.get("SIZE"+cnt+"_NAME"))) {
                        parmHash.put("tfUbkSpace", (String)parmHash.get("SIZE"+cnt+"_UBKP"));
                        // DriverAlloc
                        if (cbMvsDriverDrag.isSelected()) { // full delta
                            Integer ix = new Integer(0);
                            if ((ix.decode((String)chash.get("CUBKP"))).intValue() < (ix.decode((String)parmHash.get("SIZE"+cnt+"_UBKP"))).intValue()) {
                                parmHash.put("tfUbkSpace", (String)parmHash.get("SIZE"+cnt+"_UBKP"));
                            }
                        } else {
                            parmHash.put("tfUbkSpace", (String)parmHash.get("SIZE"+cnt+"_UBKP"));
                        }
                        parmHash.put("tfBulkPrimarySpace", (String)parmHash.get("SIZE"+cnt+"_BLKP"));
                        parmHash.put("tfBulkSecondaySpace", (String)parmHash.get("SIZE"+cnt+"_BLKS"));
                        tfUbkSpace.setText((String)parmHash.get("SIZE"+cnt+"_UBKP"));
                        tfBulkPrimarySpace.setText((String)parmHash.get("SIZE"+cnt+"_BLKP"));
                        tfBulkSecondaySpace.setText((String)parmHash.get("SIZE"+cnt+"_BLKS"));
                    }
                }
            }
        }
    }

    private void setIfNotNull(Map theMap, Object key, Object value) {
        if (key!=null & value !=null) {
            theMap.put(key,value);
        }
    }

    //Begin DEF.PTM2074:
    public void itemStateChanged(ItemEvent e) {
        Object source = e.getItemSelectable();

        if (source == overrideCheckBox) {
            if (overrideCheckBox.isSelected()) {
                coMvsDriverSize.setEnabled(false);
                btoverride.setEnabled(true);
            } else {
                coMvsDriverSize.setEnabled(true);
                btoverride.setEnabled(false);
            }
        }
    }
    //End DEF.PTM2074:


    private String getLibraryTextPrefix() {
        return MBClient.getCommandLineSettings().getMode().getCategory();
    }
}
