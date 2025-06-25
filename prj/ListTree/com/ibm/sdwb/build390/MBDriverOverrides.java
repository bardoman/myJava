package com.ibm.sdwb.build390;
/*********************************************************************/
/* MBDriverOverrides class for the Build/390 client                  */
/*  Creates and manages the Driver Build Page                        */
/*********************************************************************/
// Changes
// Date     Defect/Feature      Reason
// 02/15/99 num_parts           add support to create a driver via driversize
// 03/01/99 DrvrCreate          add support for new driver create rules
// 05/26/99                     Remove extra setVisible(true)
// 08/16/99 FixMaxExtents       Change max from 99 to 123
// 09/30/99 pjs - Fix help link
//04/04/2002 #DEF.PTM1738:          New Driver Overrides window accepts incorrect data
//12/03/2002 SDWB-2019 Enhance the help system
/*********************************************************************/
import java.awt.BorderLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.List;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.io.Serializable;
import java.util.Hashtable;
import java.util.Vector;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

import com.ibm.sdwb.build390.help.HelpTopicID;
import com.ibm.sdwb.build390.mainframe.MainframeStorageParameters;
import com.ibm.sdwb.build390.mainframe.ReleaseAndDriverParameters;
import com.ibm.sdwb.build390.user.Setup;
import com.ibm.sdwb.build390.userinterface.graphic.MainInterface;

/** Create the driver build page */
public class MBDriverOverrides extends MBModalStatusFrame implements Serializable {

    private MBInternalFrame ParentFrame;
    private MBBuild     build = null;
    private String      typeOfBuildToMake = null;
    private String      cmd = new String();
    private String      DriverType = new String();
    private boolean     new_shadow = true;
    private boolean     based   = false;
    private boolean     delta   = false;
    private boolean     stopped = false;
    private boolean     initialized = false;
    private ReleaseAndDriverParameters parameters = null;
    private Hashtable parmHash = new Hashtable();
    private Vector      sizes   = new Vector();
    private Setup     setup;


    private JLabel Label10 = new JLabel(" Storage Information");
    private JLabel Label11 = new JLabel("  DASD volume serial");
    private JLabel Label12 = new JLabel("        OR");
    private JLabel Label13 = new JLabel("  SMS Storage class");
    private JLabel Label14 = new JLabel("  SMS Management class");
    private JLabel Label15 = new JLabel("Unibank primary space");
    private JLabel Label16 = new JLabel("Bulk primary space");
    private JLabel Label17 = new JLabel("Bulk secondary space");
    private JLabel Label18 = new JLabel("Bulk max size");
    private JLabel Label19 = new JLabel("Bulk max extents");

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

    private int SaveUbkSpace = 0;
    private int SaveBulkPSpace = 0;

    private JButton btHelp = new JButton("Help");
    private JButton btOk = new JButton("Ok");
    protected GridBagLayout gridBag = new GridBagLayout();
    protected JPanel centerPanel = new JPanel(gridBag);
    //}}

    /**
    * constructor - Create a MBNewShadowDialog
    * @param MBGUI gui
    */
    public MBDriverOverrides(Setup tempSetup, ReleaseAndDriverParameters tempParms, boolean tempDelta, Hashtable tempParmHash, MBInternalFrame pFrame) throws com.ibm.sdwb.build390.MBBuildException{
        super("New Driver Overrides", pFrame, null);
        parameters = tempParms;
        ParentFrame = pFrame;
        setup = tempSetup;
        delta = tempDelta;
        parmHash = tempParmHash;
        initializeDialog();
    }

    private void initializeDialog() throws com.ibm.sdwb.build390.MBBuildException {
        getContentPane().setLayout(new BorderLayout());
        setForeground(MBGuiConstants.ColorRegularText);
        setBackground(MBGuiConstants.ColorGeneralBackground);

        try {
            SaveUbkSpace = new Integer(parameters.getDriverUnibankDatasetPrimarySpaceInCylinders()).intValue();
        } catch (NumberFormatException n) {
        }
        try {
            SaveBulkPSpace = new Integer(parameters.getDriverBulkDatasetPrimarySpaceInCylinders()).intValue();
        } catch (NumberFormatException n) {
        }

        btHelp.setForeground(MBGuiConstants.ColorHelpButton);
        btHelp.addActionListener(new MBCancelableActionListener(thisFrame) {
                                     public void doAction(ActionEvent evt) {
                                         MBUtilities.ShowHelp("HDRCAD",HelpTopicID.DRIVEROVERRIDES_HELP);
                                     }
                                 } );
        btOk.setForeground(MBGuiConstants.ColorActionButton);

        btOk.addActionListener(new MBCancelableActionListener(thisFrame) {
                                   public void doAction(ActionEvent evt) {
                                       // save info
                                       saveEntrys();
                                       // check entrys
                                       String errorData = "";

                                       String fVolid = tfVolid.getText().trim().toUpperCase();
                                       String fStgClass = tfStgClass.getText().trim().toUpperCase();
                                       String fMgtClass = tfMgtClass.getText().trim().toUpperCase();
                                       if (fVolid.length() < 1 & fStgClass.length() < 1 & fMgtClass.length() < 1) {
                                           errorData+= "You must specify either a DASD volume serial or the SMS storage class (and/or) management class.\n";
                                       }

                                       if (!(new MainframeStorageParameters(fVolid, fStgClass,fMgtClass)).combinationAllowed()) {
                                           errorData+= "You must specify a valid storage parameter combination as shown below.\n" + 
                                           MainframeStorageParameters.TABLE_TEXT;
                                       }



                                       // Check ubk space for range 1-999, if copying a base to base check for ubk space >= bases ubk space
                                       String fUbkSpace = tfUbkSpace.getText().trim();
                                       int ifUbkSpace = 0;
                                       try {
                                           if (fUbkSpace.length() > 0) ifUbkSpace = (new Integer(fUbkSpace)).intValue();
                                       } catch (NumberFormatException n) {
                                       }
                                       if (!delta) {
                                           if (ifUbkSpace < SaveUbkSpace) {
                                               errorData+= "You must specify the Unibank primary space greater than or equal to that of the base driver which is "+ SaveUbkSpace +".\n";
                                           }
                                       } else if (ifUbkSpace < 1 | ifUbkSpace > 999)
                                           errorData+= "You must specify the Unibank primary space in cylinders from 1 to 999.\n";

                                       // Check bulkp space for range 1-999, if copying a base to base check for bulkp space >= bases bulkp space
                                       String fBulkPrimarySpace = tfBulkPrimarySpace.getText().trim();
                                       int ifBulkPrimarySpace = 0;
                                       try {
                                           if (fBulkPrimarySpace.length() > 0) ifBulkPrimarySpace = (new Integer(fBulkPrimarySpace)).intValue();
                                       } catch (NumberFormatException n) {
                                       }
                                       if (!delta) {
                                           if (ifBulkPrimarySpace < SaveBulkPSpace) {
                                               errorData+= "You must specify the Bulk primary space greater than or equal to that of the base driver which is "+ SaveBulkPSpace +".\n";
                                           }
                                       } else if (ifBulkPrimarySpace < 1 | ifBulkPrimarySpace > 9999) /* PTM2710 */
                                           errorData+= "You must specify the Bulk primary space in cylinders from 1 to 9999.\n";

                                       String fBulkSecondaySpace = tfBulkSecondaySpace.getText().trim();
                                       int ifBulkSecondaySpace = 0;
                                       try {
                                           if (fBulkSecondaySpace.length() > 0) ifBulkSecondaySpace = (new Integer(fBulkSecondaySpace)).intValue();
                                       } catch (NumberFormatException n) {
                                       }
                                       if (ifBulkSecondaySpace < 1 | ifBulkSecondaySpace > 999)
                                           errorData+= "You must specify the Bulk secondary space in cylinders from 1 to 999.\n";

                                       String fBulkMaxSize = tfBulkMaxSize.getText().trim();
                                       int ifBulkMaxSize = 0;
                                       try {
                                           if (fBulkMaxSize.length() > 0) ifBulkMaxSize = (new Integer(fBulkMaxSize)).intValue();
                                       } catch (NumberFormatException n) {
                                       }
                                       if (ifBulkMaxSize < 1 | ifBulkMaxSize > 9999)    /*PTM2710 */
                                           errorData+= "You must specify the Bulk max size in cylinders from 1 to 9999.\n";

                                       String fBulkMaxExtents = tfBulkMaxExtents.getText().trim();
                                       int ifBulkMaxExtents = 0;
                                       try {
                                           if (fBulkMaxExtents.length() > 0) ifBulkMaxExtents = (new Integer(fBulkMaxExtents)).intValue();
                                       } catch (NumberFormatException n) {
                                       }

                                       // FixMaxExtents
                                       if (ifBulkMaxExtents < 1 | ifBulkMaxExtents > 123) {
                                           errorData+= "You must specify the Bulk max extents from 1 to 123.\n";
                                       }

                                       //#DEF.PTM1738:  
                                       if ((ifBulkPrimarySpace + ifBulkSecondaySpace) > ifBulkMaxSize ) {
                                           errorData+= "BulkPrime + BulkSecond must be less than or equal to BulkMax.\n";
                                       }

                                       if (!errorData.equals("")) {
                                           new MBMsgBox("Error", errorData);
                                       } else {
                                           parameters.setDriverBulkDatasetPrimarySpaceInCylinders(fBulkPrimarySpace);
                                           parameters.setDriverBulkDatasetSecondarySpaceInCylinders(fBulkSecondaySpace);
                                           parameters.setDriverUnibankDatasetPrimarySpaceInCylinders(fUbkSpace);
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

                                           initialized = true;
                                           dispose();
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

        // entry fields
        c.gridy = 1;
        c.gridx = 2;
        c.weightx = 1;
        c.fill = GridBagConstraints.NONE;

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
        getContentPane().add("Center", centerPanel);

        Label10.setForeground(MBGuiConstants.ColorGroupHeading);
        Label12.setForeground(MBGuiConstants.ColorGroupHeading);

        // init the fields
        initFields();
        setVisible(true);
    }

    public void saveEntrys() {
        parmHash.put("tfBulkPrimarySpace", tfBulkPrimarySpace.getText());
        parmHash.put("tfBulkSecondaySpace", tfBulkSecondaySpace.getText());
        parmHash.put("tfUbkSpace", tfUbkSpace.getText());
        parmHash.put("tfBulkMaxSize", tfBulkMaxSize.getText());
        parmHash.put("tfBulkMaxExtents", tfBulkMaxExtents.getText());
        parmHash.put("tfVolid", tfVolid.getText());
        parmHash.put("tfStgClass", tfStgClass.getText());
        parmHash.put("tfMgtClass", tfMgtClass.getText());
    }

    public void postVisibleInitialization() {
        tfVolid.requestFocus();
    }

    public void initFields() throws com.ibm.sdwb.build390.MBBuildException
    {
        // set fields
        tfBulkPrimarySpace.setText((String)parmHash.get("tfBulkPrimarySpace"));
        tfBulkSecondaySpace.setText((String)parmHash.get("tfBulkSecondaySpace"));
        tfUbkSpace.setText((String)parmHash.get("tfUbkSpace"));
        tfBulkMaxSize.setText((String)parmHash.get("tfBulkMaxSize"));
        tfBulkMaxExtents.setText((String)parmHash.get("tfBulkMaxExtents"));
        tfVolid.setText((String)parmHash.get("tfVolid"));
        tfStgClass.setText((String)parmHash.get("tfStgClass"));
        tfMgtClass.setText((String)parmHash.get("tfMgtClass"));
    }

    public boolean wasInitialized() {
        return initialized;
    }
}
