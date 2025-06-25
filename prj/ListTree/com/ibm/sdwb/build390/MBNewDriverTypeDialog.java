package com.ibm.sdwb.build390;
/*********************************************************************/
/* MBNewShadowDialog class for the Build/390 client                  */
/*  Creates and manages the Driver Build Page                        */
/*********************************************************************/
// Changes
// Date     Defect/Feature      Reason
// 11/13/98 Add_sysmod_support  Add CopySysmod checkbox
// 04/27/99 errorHandling       change LogException parms & add new error types
// 05/24/99                     Fix help button anchor
// 09/30/99 pjs - Fix help link
// 11/02/99 chris - put a flag for APAR delta driver creation
// 03/07/2000 reworklog         changes to implement the log stuff using listeners
// 05/12/2000 pjs - 			add debug to bps calls
// 11/07/00 Thulasi 			Added key listeners for the text fields not to take more than 8 characters.
//Thulasi:11/14/00: feature 111 - Made the Driver Type Dialog frame not to get disposed  if wrong value is entered for the
//                                base driver.
//Feature 111 :                   The driver window doesnt get disposed.
//03/22/2001 #Defect341:  Changed textfield reference to tfBaseApar so manual entry would work
//12/03/2002 SDWB-2019 Enhance the help system
/*********************************************************************/
import java.awt.BorderLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.List;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.io.Serializable;
import java.util.Hashtable;
import java.util.Vector;

import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.tree.DefaultMutableTreeNode;

import com.ibm.sdwb.build390.help.HelpTopicID;
import com.ibm.sdwb.build390.library.LibraryInfo;
import com.ibm.sdwb.build390.userinterface.graphic.MainInterface;


/** Create the driver build page */
public class MBNewDriverTypeDialog extends MBModalStatusFrame implements Serializable {

    private MBBuild     build = null;
    private Hashtable   cmdHash = new Hashtable();
//    private MBServiceClient msc;
    private MBInternalFrame iFrame = null;
    //Thulasi:11/14/00: Added a private variable of type DefaultMutableTreeNode
    private DefaultMutableTreeNode top = new DefaultMutableTreeNode("");

    private JLabel Label01       = new JLabel("Indicate the type of driver to be created");
    private JTextField tfBase    = new JTextField(10);
    private JTextField tfCopyFrom= new JTextField(10);
    private JTextField tfBaseApar= new JTextField(10);
    private JTextField tfDefect  = new JTextField(10); // good size?
    private JLabel Label02       = new JLabel("for Defect");
    private JButton btHelp       = new JButton("Help");
    private JButton btOk         = new JButton("Ok");
    private ButtonGroup Group1 = new ButtonGroup();
    private JCheckBox newbase    = new JCheckBox("Create a base driver");
    private JCheckBox basedon    = new JCheckBox("Create a delta driver based on");
    private JCheckBox aparDelta    = new JCheckBox("Create an APAR delta driver based on");
    private JCheckBox CopyFrom   = new JCheckBox("Copy from");
    private JCheckBox Sysmod     = new JCheckBox("Copy Sysmods");

    private MBButtonPanel tempButt;
    protected GridBagLayout gridBag = new GridBagLayout();
    protected JPanel centerPanel  = new JPanel(gridBag);
    protected JPanel tpanel = new JPanel();
    protected JPanel apanel = new JPanel(); //apar delta
    protected JPanel bpanel = new JPanel(); //Add_sysmod_support
    protected JPanel cpanel = new JPanel(); //Add_sysmod_support
    protected JPanel dpanel = new JPanel(); //Add_sysmod_support

    /**
    * constructor - Create a MBNewDriverTypeDialog
    * @param MBGUI gui
    */
    //Thulasi:11/14/00: Passed a variable of type DefaultMutableTreeNode as an additional parameter
    //for MBNewDriverTypeDialog constructor.
    public MBNewDriverTypeDialog(MBBuild bld, Hashtable inHash, MBInternalFrame pFrame, DefaultMutableTreeNode top ) throws com.ibm.sdwb.build390.MBBuildException{
        super("New Driver Type", pFrame, null);
        iFrame = pFrame;
        cmdHash = inHash;
        build = bld;
        this.top = top;
        //Thulasi: adding key listeners for the text fields
        int startPosArray[] = {0};
        int searchPosArray[] = {0};
        char[] dataType = {'A', 'N'};

        tfCopyFrom.addKeyListener(new MBKeyAdapter(getStatus(),startPosArray, searchPosArray, dataType, 8));
        tfBase.addKeyListener(new MBKeyAdapter(getStatus(),startPosArray, searchPosArray, dataType, 17));
        tfBaseApar.addKeyListener(new MBKeyAdapter(getStatus(),startPosArray, searchPosArray, dataType, 8));
        tfDefect.addKeyListener(new MBKeyAdapter(getStatus(),startPosArray, searchPosArray, dataType, 8));

        initializeDialog(build);
    }

    public void initializeDialog(MBBuild tempBuildParm) throws com.ibm.sdwb.build390.MBBuildException {
        build   = tempBuildParm;
        lep.addEventListener(build.getLogListener());
        getContentPane().setLayout(new BorderLayout());
        setForeground(MBGuiConstants.ColorRegularText);
        setBackground(MBGuiConstants.ColorGeneralBackground);

        // set enable state of the sysmod checkbox and uncheck it if copyfrom is not selected
        //Add_sysmod_support
        CopyFrom.addActionListener(new ActionListener() {
                                       public void actionPerformed(ActionEvent evt) {
                                           Sysmod.setEnabled(CopyFrom.isSelected());
                                           if (!CopyFrom.isSelected()) Sysmod.setSelected(false);
                                           tfCopyFrom.setEnabled(CopyFrom.isSelected());
                                       }
                                   });

        newbase.addActionListener(new ActionListener() {
                                      public void actionPerformed(ActionEvent evt) {
                                          CopyFrom.setEnabled(newbase.isSelected());
                                          tfBase.setEnabled(basedon.isSelected());
                                          tfCopyFrom.setEnabled(CopyFrom.isSelected());
                                          tfBaseApar.setEnabled(aparDelta.isSelected());
                                          tfDefect.setEnabled(aparDelta.isSelected());
                                      }
                                  });

        basedon.addActionListener(new ActionListener() {
                                      public void actionPerformed(ActionEvent evt) {
                                          CopyFrom.setEnabled(newbase.isSelected());
                                          tfBase.setEnabled(basedon.isSelected());
                                          tfCopyFrom.setEnabled(CopyFrom.isSelected());
                                          tfBaseApar.setEnabled(aparDelta.isSelected());
                                          tfDefect.setEnabled(aparDelta.isSelected());
                                      }
                                  });

        aparDelta.addActionListener(new ActionListener() {
                                        public void actionPerformed(ActionEvent evt) {
                                            CopyFrom.setEnabled(newbase.isSelected());
                                            tfBase.setEnabled(basedon.isSelected());
                                            tfCopyFrom.setEnabled(CopyFrom.isSelected());
                                            tfBaseApar.setEnabled(aparDelta.isSelected());
                                            tfDefect.setEnabled(aparDelta.isSelected());
                                        }
                                    });

        btHelp.setForeground(MBGuiConstants.ColorHelpButton);
        btHelp.addActionListener(new ActionListener() {
                                     public void actionPerformed(ActionEvent evt) {
                                         try {
                                             //MBUtilities.ShowHelp("Defining_Drivers");
                                             MBUtilities.ShowHelp("HDRCAD",HelpTopicID.NEWDRIVERTYPEDIALOG_HELP);
                                         } finally {
                                         }
                                     }
                                 } );

        btOk.setForeground(MBGuiConstants.ColorActionButton);
        btOk.addActionListener(new ActionListener() {
                                   public void actionPerformed(ActionEvent evt) {
                                       new Thread(new Runnable() {
                                                      public void run() {
                                                          try {
                                                              // check entrys
                                                              String errorData = "";
                                                              boolean bnewbase = newbase.isSelected();
                                                              // 2/2/99, Chris, service support
                                                              boolean bapardelta = aparDelta.isSelected();
                                                              boolean copyfrom = CopyFrom.isSelected();
                                                              String fBase  = new String();
                                                              String defect = new String();
                                                              // new base driver
                                                              if (bnewbase) {
                                                                  if (copyfrom) {
                                                                      fBase = tfCopyFrom.getText().trim().toUpperCase();
                                                                      if (fBase.length() < 1)
                                                                          errorData+= "You must specify the source to copy from when the \'Copy From\' button is checked.\n";
                                                                  }
                                                              } else if (bapardelta) {
                                                                  // apar delta driver
                                                                  fBase = tfBaseApar.getText().trim().toUpperCase();
                                                                  if (fBase.length() < 1)
                                                                      errorData+= "You must specify a base driver.\n";
                                                                  // get defect number from user
                                                                  defect = tfDefect.getText().trim();//#Defect341:
                                                                  if (defect.length() < 1)
                                                                      errorData+= "You must specify a defect number.\n";
                                                              }
                                                              // new delta driver
                                                              else {
                                                                  fBase = tfBase.getText().trim().toUpperCase();
                                                                  if (fBase.length() < 1)
                                                                      errorData+= "You must specify a base driver.\n";
                                                              }

                                                              if (!errorData.equals("")) {
                                                                  new MBMsgBox("Error", "ERROR:MBNewDriverTypeDialog:" + errorData);

                                                              } else {
                                                                  if (bnewbase) {
                                                                      cmdHash.put("TYPE", "FULL");  // indicates that a base is to br created
                                                                      //Add_sysmod_support
                                                                      if (Sysmod.isSelected()) {
                                                                          cmdHash.put("SYSMODS", "YES");
                                                                      } else {
                                                                          cmdHash.put("SYSMODS", "NO");
                                                                      }
                                                                  } else if (bapardelta) { // apar delta
                                                                      LibraryInfo lib = build.getLibraryInfo();
                                                                      MBStatus status = (MBStatus)iFrame.getStatus();

                                                                      String release = build.getReleaseInformation().getLibraryName();
                                                                      String aparName = null;
/*
                                                                      String family = lib.getLibraryName();
                                                                      status.updateStatus("Lookup Service390 RMI server", false);

                                                                      // get apar name for defect from BPS database
                                                                      status.updateStatus("Getting APAR name for "+defect+".", false);
                                                                      try {
                                                                          lep.LogPrimaryInfo("Debug:","BPS: " + "getAparName(" + family + ", " + release + ", " + defect + ")", true);
                                                                          aparName = msc.getAparName(family, release, defect);
                                                                          lep.LogPrimaryInfo("Debug:","BPS: " + "AparName = " + aparName, true);
                                                                      } catch (Exception e) {
                                                                          throw new ServiceError("Creating APAR Delta driver"+e);
                                                                      }

                                                                      if (aparName.length() < 1 ) {
                                                                          throw new ServiceError("No APAR found for this defect, " +defect);
                                                                      }
*/                                    

                                                                      // set the delta driver name in New Release/Driver Dialog
                                                                      // user can change other param
                                                                      cmdHash.put("TYPE", "DELTA");
                                                                      cmdHash.put("CNEWDRVR", aparName);
                                                                      // 11/02/99, chris, flag to indicate apar delta driver type
                                                                      cmdHash.put("DRVRTYPE", "APAR");
                                                                      cmdHash.put("DEFECT", defect);
                                                                  } // new delta
                                                                  else cmdHash.put("TYPE", "DELTA");
                                                                  cmdHash.put("BASE", fBase);  // indicates a copy is to be done instead of a create

                                                                  //Thulasi:11/14/00: Provided the logic to display an error message if the base
                                                                  //driver entered is wrong. If everything is ok, the driver type dialog frame
                                                                  // will be disposed.
                                                                  /*if (!fBase.trim().equals("") & !(fBase.indexOf(".")>0)) {
                                                                      DefaultMutableTreeNode temp = findNodeInTree(fBase);
                                                                      String Estring = new String("");
                                                                      if (temp == null) {
                                                                          Estring = "The base you selected \'"+fBase+"\' does not exist";
                                                                          if (!Estring.equals("")) {
                                                                              new MBMsgBox("Error", Estring);
                                                                          }
                                                                      } else {
                                                                          dispose();
                                                                      }

                                                                  } */

                                                                  dispose();

                                                              }
//                                                          } catch (MBBuildException mbe) {
                                                              //MBUtilities.LogException(mbe);
//                                                              lep.LogException(mbe);
                                                          } finally {
                                                          }


                                                      }
                                                  }).start();
                                   }
                               } );


        Vector actionButtons = new Vector();
        actionButtons.addElement(btOk);
        //tempButt = new MBButtonPanel(btHelp,null,actionButtons);
        Label01.setForeground(MBGuiConstants.ColorGroupHeading);


        GridBagConstraints c = new GridBagConstraints();
        c.weighty = 1;
        c.weightx = 0;
        c.gridx = 1;
        c.gridy = 1;
        c.gridwidth = GridBagConstraints.REMAINDER;
        c.anchor = GridBagConstraints.WEST;
        c.insets = new Insets(2,5,2,0);
        gridBag.setConstraints(Label01, c);
        centerPanel.add(Label01);

        c.gridwidth = 1;
        c.gridy = 2;
        bpanel.add(newbase);
        gridBag.setConstraints(bpanel, c);
        centerPanel.add(bpanel);

        //Add_sysmod_support
        c.gridy = 3;
        cpanel.add(new JPanel());
        cpanel.add(CopyFrom);
        cpanel.add(tfCopyFrom);
        gridBag.setConstraints(cpanel, c);
        centerPanel.add(cpanel);

        //Add_sysmod_support
        c.gridy = 4;
        dpanel.add(new JPanel());
        dpanel.add(new JPanel());
        dpanel.add(Sysmod);
        gridBag.setConstraints(dpanel, c);
        centerPanel.add(dpanel);

        c.gridy = 5;
        tpanel.add(basedon);
        tpanel.add(tfBase);
        gridBag.setConstraints(tpanel, c);
        centerPanel.add(tpanel);

        gridBag.setConstraints(Label02, c);
        c.gridy = 6;
        apanel.add(aparDelta);
        apanel.add(tfBaseApar);
        apanel.add(Label02);
        apanel.add(tfDefect);
        gridBag.setConstraints(apanel, c);
        centerPanel.add(apanel);

        // init the fields
        String baseSetting =  initFields();
        Group1.add(newbase);
        Group1.add(basedon);
        Group1.add(aparDelta);

        if (baseSetting!=null) {
            basedon.setSelected(true);
        } else {
            newbase.setSelected(true);
        }
/*
        try {
            String releaseProcessQuery = "SELECT relProcess FROM ReleaseView WHERE name='"+build.getReleaseInformation().getLibraryName()+"'";
            Vector releaseProcessResult = build.getLibraryInfo().getLibraryOpServer().executeQuery(releaseProcessQuery, build.getSetup().getLibraryInfo().getLibraryUsername(),  System.getProperty("user.name"));
            if (releaseProcessResult.size() > 0) {
                Vector oneProcess = (Vector) releaseProcessResult.elementAt(0);
                if (oneProcess.size() > 0) {
                    String releaseProcess = (String) oneProcess.elementAt(0);
                    if (releaseProcess != null) {
                        if (releaseProcess.trim().toUpperCase().equals("SERVICE")) {
                            aparDelta.setSelected(true);
                        }
                    }
                }
            }
        }catch (java.rmi.RemoteException re){
            throw new ServiceError("Getting release process for release " + build.getReleaseInformation().getLibraryName());
        }
*/

        addButtonPanel(btHelp, actionButtons);
        getContentPane().add("North", centerPanel);


        setVisible(true);
    }

    public void postVisibleInitialization() {
        CopyFrom.setEnabled(newbase.isSelected());
        tfBase.setEnabled(basedon.isSelected());
        tfCopyFrom.setEnabled(CopyFrom.isSelected());
        tfBaseApar.setEnabled(aparDelta.isSelected());
        tfDefect.setEnabled(aparDelta.isSelected());
    }

    public String initFields() {
        String tmp = (String)cmdHash.remove("IBASE");
        // try to deserialize the hash ser file
        try {
            if (tmp!=null) {
                tfBase.setText(tmp);
                tfCopyFrom.setText(tmp);
                tfBaseApar.setText(tmp);
            }
            Sysmod.setEnabled(false); //Add_sysmod_support
        }
        // if that fails get the defaults from the host
        catch (Exception e) {
            e.printStackTrace();
        }
        return tmp;
    }
}
