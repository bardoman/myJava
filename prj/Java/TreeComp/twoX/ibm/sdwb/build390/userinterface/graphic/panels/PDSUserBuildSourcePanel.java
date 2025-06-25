package com.ibm.sdwb.build390.userinterface.graphic.panels;
/*********************************************************************/
/* PDSUserBuildSourcePanel class for the Build/390 client          */
/*  Creates and manages the PDS UserBuild                    */
/*********************************************************************/
//08/09/2007 TST3409 userbuild hang   
/*********************************************************************/
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.List;
import java.awt.event.ActionEvent;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.KeyEvent;
import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.Vector;

import javax.swing.Action;
import javax.swing.BorderFactory;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.border.LineBorder;
import javax.swing.border.TitledBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import com.ibm.sdwb.build390.*;
import com.ibm.sdwb.build390.info.InfoForMainframePartReportRetrieval;
import com.ibm.sdwb.build390.info.PDSFileInfo;
import com.ibm.sdwb.build390.library.LocalSourceInfo;
import com.ibm.sdwb.build390.library.SourceInfo;
import com.ibm.sdwb.build390.logprocess.LogEventProcessor;
import com.ibm.sdwb.build390.mainframe.ReleaseInformation;
import com.ibm.sdwb.build390.mainframe.parser.DriverReportParser;
import com.ibm.sdwb.build390.metadata.info.GeneratedMetadataInfo;
import com.ibm.sdwb.build390.metadata.utilities.MetadataValueGenerator;
import com.ibm.sdwb.build390.process.ListPDSMembers;
import com.ibm.sdwb.build390.process.UserBuildProcess;  
import com.ibm.sdwb.build390.process.steps.DriverReport;
import com.ibm.sdwb.build390.test.TestNotifyListener;
import com.ibm.sdwb.build390.user.Mode;
import com.ibm.sdwb.build390.userinterface.event.UserInterfaceEvent;
import com.ibm.sdwb.build390.userinterface.event.build.ProcessUpdateEvent;
import com.ibm.sdwb.build390.userinterface.event.build.ReleaseUpdateEvent;
import com.ibm.sdwb.build390.userinterface.graphic.MainInterface;
import com.ibm.sdwb.build390.userinterface.graphic.panels.build.BuildPanel;
import com.ibm.sdwb.build390.userinterface.graphic.panels.build.UserBuildPanel;
import com.ibm.sdwb.build390.userinterface.graphic.panels.metadata.SingleSourceMetadataEditorFrame;
import com.ibm.sdwb.build390.userinterface.graphic.widgets.CancelableAction;
import com.ibm.sdwb.build390.userinterface.graphic.widgets.CancelableProcess;
import com.ibm.sdwb.build390.userinterface.graphic.widgets.JListDialogBox;
import com.ibm.sdwb.build390.userinterface.graphic.widgets.ReleaseSelectionCombo;
import com.ibm.sdwb.build390.userinterface.graphic.widgets.RequiredActionsCompletedInterface;

/** Create the LocalPartsUserBuildSourcePanel */
public class PDSUserBuildSourcePanel extends UserBuildSource {
    private static final String PROTOTYPE_CELL_WIDTH = "ABCDEFGHIJKLMNOPQRSTUVWXYZAb";

    private static final String USERBUILD_PDSPATH = "USERBUILD_PDSPATH";

    private JButton btBrowseRoot = new JButton("Set PDS Name");
    private JButton btPartListDelete    = new JButton("Delete    ");
    private JButton btPartListDeleteAll = new JButton("Delete All");
    private JButton btPartListAdd       = new JButton("Add       ");
    private DefaultListModel listModel = new DefaultListModel();
    private JList partsList = new JList(listModel);
    private JTextField tfRootPath = new JTextField(20);
    private JLabel releaseLabel = new JLabel("Release");
    private JLabel partsListLabel = new JLabel("Part List");
    private JLabel sourceLocationLabel = new JLabel("Select PartLocation");
    private ReleaseSelectionCombo releaseSelector;
    private LogEventProcessor lep;
    private MBUBuild build;
    private UserBuildPanel parent;
    private Action metadataEditAction;
    private JButton modelAfterExistingPart = new JButton("Model After Existing Part");
    private Map metadataMap = new HashMap();
    private Hashtable modelAfterHash = new Hashtable();
    private GridBagLayout gridBag = new GridBagLayout();
    private Vector members = null;
    private String memberClass = new String(" ");

    private boolean restartBuildAgain = false;
    /**
 * constructor - Create a LocalPartsUserBuildSourcePanel
 * @param MBGUI gui
 */
    public PDSUserBuildSourcePanel(UserBuildPanel parent, MBUBuild build, LogEventProcessor lep) {
        this.parent = parent;

        this.lep = lep;

        this.build = build;

        setWidgets();

        PDSUserBuildSourcePanelListener listener = new PDSUserBuildSourcePanelListener(this);
        listModel.addListDataListener(listener);
        partsList.setModel(listModel);
        releaseSelector.getComboBox().addActionListener(listener);

        SymText lSymText = new SymText();
        tfRootPath.addFocusListener(lSymText);
        tfRootPath.addKeyListener(new LocalPathKeyListener());

        partsList.setBackground(MBGuiConstants.ColorFieldBackground);

        partsList.setPrototypeCellValue(PROTOTYPE_CELL_WIDTH);

        setListeners();

        handlePDSDefaults();
        setVisible(true);
    }

    protected void setListeners() {
        // Model part after an existing part, fasttrack only
        modelAfterExistingPart.addActionListener(new LocalPartsModelAction(parent.getInternalFrame()));//UserBldUpdate0

        // Delete part names from list box
        btPartListDelete.addActionListener(new MBCancelableActionListener(parent.getInternalFrame()) {

                                               public void doAction(ActionEvent evt) {
                                                   SwingUtilities.invokeLater(new Runnable() {
                                                                                  public void run() {
                                                                                      Object[] idxValues = partsList.getSelectedValues();
                                                                                      if (idxValues.length > 0) {
                                                                                          for (Object idxValue: idxValues) {
                                                                                              listModel.removeElement(idxValue);
                                                                                          }
                                                                                      }
                                                                                  }
                                                                              });
                                               }

                                               public void postAction() {
                                                   if (listModel.getSize()<=0) {
                                                       btPartListDelete.setEnabled(false);
                                                       btPartListDeleteAll.setEnabled(false);
                                                   }
                                                   modelAfterExistingPart.setEnabled(false);
                                                   metadataEditAction.setEnabled(false);
                                                   parent.checkToEnableViewBuildLog();

                                                   //1.4 bug. we dont need this method in 1.5. keep a tab on it. and remove it in 1.5.
                                                   fireListDataChangedManually();
                                               }
                                           } );

        // Delete part names from list box
        btPartListDeleteAll.addActionListener(new MBCancelableActionListener(parent.getInternalFrame()) {
                                                  public void doAction(ActionEvent evt) {
                                                      listModel.removeAllElements();
                                                      partsList.setModel(listModel);
                                                      repaint();
                                                  }

                                                  public void postAction() {
                                                      parent.checkToEnableViewBuildLog();
                                                      btPartListDelete.setEnabled(false);
                                                      btPartListDeleteAll.setEnabled(false);

                                                      metadataEditAction.setEnabled(false);
                                                      modelAfterExistingPart.setEnabled(false);
                                                      //1.4 bug. we dont need this method in 1.5. keep a tab on it. and remove it in 1.5.
                                                      fireListDataChangedManually();
                                                  }
                                              } );




        // add parts to list box, either local or from pds
        btPartListAdd.addActionListener(new PartListAddAction(parent.getInternalFrame()));

        // Browse button, set the relative directory or the PDS file name
        btBrowseRoot.addActionListener(new MBCancelableActionListener(parent.getInternalFrame()) {
                                           public void doAction(ActionEvent evt) {
                                               // does user want local parts or parts from a pds ?
                                               members = null; // make sure that members list is null
                                               memberClass = " ";
                                               try {
                                                   members = getPDSMembersFromHost();
                                               } catch (MBBuildException e) {
                                                   lep.LogException(e);
                                               }
                                           }
                                           public void postAction() {
                                               if (members!=null) {
                                                   if (members.size() > 0 ) {
                                                       btPartListAdd.setEnabled(true);
                                                       btPartListDeleteAll.setEnabled(true);
                                                       parent.getInternalFrame().problemBox("Information","Members have been successfully fetched from the PDS " + tfRootPath.getText() + MBConstants.NEWLINE + 
                                                                                            "Please Click Add to include the fetched members in the build ",true);
                                                   }
                                               }
                                               parent.checkToEnableViewBuildLog();
                                           }
                                       } );

        // Disable delete menu item Defect_144
        partsList.addListSelectionListener(new ListSelectionListener() {
                                               public void valueChanged(ListSelectionEvent ie) {
                                                   // if anything in part list box is selected, enable delete, else disable delete
                                                   // also if fasttrack enable model after
                                                   int[]  idxs  = partsList.getSelectedIndices();
                                                   btPartListDelete.setEnabled(false);
                                                   //btLocalPartsModel.setEnabled(false);
                                                   if (idxs.length > 0) {

                                                       //#Def.INT0792:  
                                                       btPartListDelete.setEnabled(true);

                                                       metadataEditAction.setEnabled(true);
                                                       // if fast track enable model after
                                                       if (build.getFastTrack()) {
                                                           //#Def.INT0792:  
                                                           partsList.setEnabled(true);
                                                           modelAfterExistingPart.setEnabled(true);
                                                       }


                                                       //1.4 bug. we dont need this method in 1.5. keep a tab on it. and remove it in 1.5.
                                                       fireListDataChangedManually();
                                                   }
                                               }
                                           } );

        //
        modelAfterExistingPart.addItemListener(new ItemListener() {
                                                   public void itemStateChanged(ItemEvent evt) {
                                                       btPartListAdd.setEnabled(false);
                                                       //04/21/2000 USER_B_ADDITEM made the add menu item enabled if the local radio button is selected for local parts
                                                       // if (local.isSelected()) {
                                                       if (tfRootPath.getText().length()>0) {
                                                           //#Def.INT0792:
                                                           btPartListAdd.setEnabled(true);
                                                       }

                                                   }

                                               } );


        tfRootPath.getDocument().addDocumentListener(new DocumentListener() {
                                                         public void insertUpdate(DocumentEvent e) {
                                                             SwingUtilities.invokeLater(new Runnable() {
                                                                                            public void run() {
                                                                                                btPartListAdd.setEnabled(false);
                                                                                                if (tfRootPath.getText().length()>0) {
                                                                                                    btPartListAdd.setEnabled(true);
                                                                                                }
                                                                                            }
                                                                                        });

                                                         }
                                                         public void removeUpdate(DocumentEvent e) {
                                                             SwingUtilities.invokeLater(new Runnable() {
                                                                                            public void run() {
                                                                                                btPartListAdd.setEnabled(false);
                                                                                                if (tfRootPath.getText().length()>0) {
                                                                                                    btPartListAdd.setEnabled(true);
                                                                                                }
                                                                                            }
                                                                                        });

                                                         }
                                                         public void changedUpdate(DocumentEvent e) {
                                                         }
                                                     });
    }

    protected void setWidgets() {
        setLayout(gridBag);

        setBorder(BorderFactory.createTitledBorder(LineBorder.createGrayLineBorder() ,"PartList Selections",TitledBorder.DEFAULT_JUSTIFICATION,TitledBorder.DEFAULT_POSITION,null,MBGuiConstants.ColorGroupHeading));

        GridBagConstraints c = new GridBagConstraints();

//
        c.gridx = 1;
        c.gridy = 0;
        c.weighty = 1;
        c.insets = new Insets(2,5,2,5);
        c.anchor = GridBagConstraints.WEST;
        c.gridwidth = 1;

        gridBag.setConstraints(modelAfterExistingPart,c);

        if (build.getFastTrack()) {
            add(modelAfterExistingPart);
            modelAfterExistingPart.setEnabled(false);
        }


        c.gridx = 2;
        c.gridy = 0;
        c.weighty = 1;
        c.insets = new Insets(2,5,2,5);
        c.fill = GridBagConstraints.HORIZONTAL;
        c.gridwidth = GridBagConstraints.REMAINDER;
        c.anchor = GridBagConstraints.WEST;


        metadataEditAction = new AddMetaDataEditAction();
        JButton metadataEdit = new JButton(metadataEditAction);
        gridBag.setConstraints(metadataEdit,c);

        if (!build.getFastTrack()) {
            add(metadataEdit);
        }

        c.gridx = 0;
        c.gridy = 1;
        c.weighty = 1;
        c.insets = new Insets(2,5,2,5);
        c.anchor = GridBagConstraints.WEST;
        c.gridwidth = 1;
        gridBag.setConstraints(releaseLabel,c);
        add(releaseLabel);

        //
        c.gridx = 1;
        c.gridy = 1;
        c.weightx=2;
        c.fill = GridBagConstraints.HORIZONTAL;

        c.gridwidth = GridBagConstraints.REMAINDER;

        releaseSelector = new ReleaseSelectionCombo(build.getMainframeInfo(), build.getLibraryInfo(),lep); 

        gridBag.setConstraints(releaseSelector, c);
        add(releaseSelector);

        c.gridx = 0;
        c.gridy = 2;
        c.weighty = 1;
        c.insets = new Insets(2,5,2,5);
        c.anchor = GridBagConstraints.WEST;

        c.gridwidth = 1;
        gridBag.setConstraints(sourceLocationLabel,c);
        add(sourceLocationLabel);


        c.gridx = 0;
        c.gridy=3;
        gridBag.setConstraints(partsListLabel, c);
        add(partsListLabel);


        c.gridx=1;
        c.gridy=2;
        c.insets = new Insets(2,5,2,5);
        c.anchor = GridBagConstraints.WEST;
        tfRootPath.setColumns(15);
        gridBag.setConstraints(tfRootPath,c);
        add(tfRootPath);
        c.gridy=3;
        JScrollPane listScroller = new JScrollPane(partsList);
        gridBag.setConstraints(listScroller, c);
        add(listScroller);
        listScroller.setPreferredSize(partsList.getPreferredScrollableViewportSize());


        c.gridx = 2;
        c.gridy=2;
        c.weightx = 1;
        c.fill = GridBagConstraints.HORIZONTAL;
        c.gridwidth = GridBagConstraints.REMAINDER;
        gridBag.setConstraints(btBrowseRoot,c);
        add(btBrowseRoot);



        MBInsetPanel partListBox = new MBInsetPanel(new GridLayout(5,1),1,0,1,0);

        partListBox.add(btPartListAdd);

        partListBox.add(new JLabel("          "));
        partListBox.add(btPartListDelete);

        partListBox.add(new JLabel("          "));
        partListBox.add(btPartListDeleteAll);
        c.gridx=2;
        c.gridy=3;
        gridBag.setConstraints(partListBox,c);
        add(partListBox);

    }


    private Vector getPDSMembersFromHost() throws com.ibm.sdwb.build390.MBBuildException {
        Vector membersTemp = null; // make sure that members list is null
        memberClass = " ";

        PDSUserBuildPartsSelectorFrame pdsSelector = new PDSUserBuildPartsSelectorFrame(tfRootPath.getText().trim(),build.getPDSMemberClass(), parent.getInternalFrame(),build.getFastTrack());//TST3412
        pdsSelector.setVisible(true);
        String userEnteredPartType =  pdsSelector.getPDSPartType();

        final String userEnteredPDSName = pdsSelector.getPDSName();
        if ((userEnteredPDSName != null && userEnteredPDSName.trim().length() >0) && (userEnteredPartType !=null && userEnteredPartType.trim().length() >0)) {
            if (userEnteredPDSName.length() > 0) {
                memberClass = pdsSelector.getPDSPartType();
                tfRootPath.setText(userEnteredPDSName);
                if (tfRootPath.getText() != null) {
                    if (getProjectChosen()!=null) {
                        com.ibm.sdwb.build390.userinterface.RememberedSettingsHandler.getInstance().addPerReleaseSetting(build.getSetup(), getProjectChosen().getLibraryName(), USERBUILD_PDSPATH, tfRootPath.getText());
                    }
                }

                listModel.removeAllElements();
                partsList.setModel(listModel);
                repaint();

                membersTemp = getPDSMemberList(build,userEnteredPDSName);
                build.setPDSMemberClass(memberClass);

                if (membersTemp.isEmpty()) {
                    tfRootPath.setText("");
                    parent.getInternalFrame().problemBox("Information", "PDS " + userEnteredPDSName+ " is empty." + MBConstants.NEWLINE + "Please choose a different PDS.",true);
                } else {
                    /** parse the ssi number from each member name
                    * if the member name contains an SSI number, parse it out and use the member
                    * these members have no SSI, show them in a message
                    */ 
                    Vector noSSImem = new Vector();
                    Enumeration memberEnum = membersTemp.elements();
                    while (memberEnum.hasMoreElements()) {
                        String thismem = (String)memberEnum.nextElement();
                        if (thismem.indexOf(" ")==-1) {
                            noSSImem.addElement(thismem);
                        }
                    }
                    /** show user members that have no SSI
                     * if the member name contains an SSI number, parse it out and use the member
                     */
                    if (!noSSImem.isEmpty()) {
                        Enumeration noSSIs = noSSImem.elements();
                        String msg = "A version number cannot be associated with the following members, ";
                        while (noSSIs.hasMoreElements()) {
                            String nossimem = (String)noSSIs.nextElement();
                            msg = msg+nossimem+" ";
                        }
                        msg=msg+"."+MBConstants.NEWLINE+"Resave these members in ISPF with STATS set to ON.";
                        parent.getInternalFrame().problemBox("Warning", msg,true);
                    }
                }
            }
        }
        return membersTemp;
    }

    private Vector getPDSMemberList(MBBuild build, String pdsName) {
        ListPDSMembers pdsMemberLister = new ListPDSMembers(build,pdsName, parent.getInternalFrame());

        CancelableProcess processWrapper = new CancelableProcess(pdsMemberLister, parent.getInternalFrame());

        processWrapper.run();
        if (pdsMemberLister.getPDSMemberList()!=null) {
            return new Vector(pdsMemberLister.getPDSMemberList());
        } else {
            return new Vector();
        }
    }


    class PartListAddAction extends MBCancelableActionListener {
        PartListAddAction(MBAnimationStatusWindow temp) {
            super(temp);
        }

        public void doAction(ActionEvent evt) {
            try {
                if (tfRootPath.getText().length()> 0) {
                    if (members ==null) {
                        members = getPDSMembersFromHost();
                    }
                    if (members != null) {
                        // if the member name contains an SSI number, parse it out and use the member
                        // parse the ssi number from each member name
                        java.util.List choiceList  = new ArrayList();
                        for (Iterator iter=members.iterator();iter.hasNext();) {
                            String thismem = (String)iter.next();
                            if (thismem.indexOf(" ")>-1) {
                                choiceList.add(thismem.substring(0,thismem.indexOf(" ")));
                            }
                        }
                        if(!choiceList.isEmpty()) {//TST3567
                        	JListDialogBox userSelectionBox = new JListDialogBox(MBConstants.productName+" - PDS Member List", choiceList, parent.getInternalFrame(), lep);
                            userSelectionBox.setAllowMultipeSelection(true);
                            userSelectionBox.setVisible(true);

                            String membersSelected[] = null;
                            membersSelected = userSelectionBox.getElementsSelected();
                            if (membersSelected != null) {
                                for (int idx=0; idx<membersSelected.length; idx++) {
                                    listModel.addElement(membersSelected[idx]);
                                    partsList.setModel(listModel);
                                    repaint();
                                }
                            }

                            if (listModel.getSize() >= 1) {
                                btPartListDeleteAll.setEnabled(true);
                            }
                        } else {
                        	MBMsgBox mb = new MBMsgBox("Warning!","No parts available for selection");//TST3567
                        }
                    }   
                } else {
                    parent.getInternalFrame().problemBox("Error", "You must specify the pdsname before adding parts");
                }
            } catch (MBBuildException e) {
                lep.LogException(e);
            }

        }

        public void postAction() {
            parent.checkToEnableViewBuildLog();
            partsList.setPrototypeCellValue(PROTOTYPE_CELL_WIDTH);
            repaint();
            //1.4 bug. we dont need this method in 1.5. keep a tab on it. and remove it in 1.5.
            fireListDataChangedManually();
        }
    }

    // Model part after an existing part, fasttrack only

    class LocalPartsModelAction extends MBCancelableActionListener {
        LocalPartsModelAction(MBAnimationStatusWindow temp) {
            super(temp);
        }

        public void doAction(ActionEvent evt) {
            //btAddMetaDataEdit.setEnabled(false);
            int[] idxs = partsList.getSelectedIndices();
            Object[] tempOrig = listModel.toArray();
            String[] values = new String[tempOrig.length+1];
            if (idxs.length > 0) {
                for (int x=idxs.length; x>0; x--) {
                    String tpart = (String)tempOrig[idxs[x-1]];
                    Hashtable modelHash = new Hashtable();
                    modelHash.put("NEWPART", tpart);
                    String tm = (String)modelAfterHash.get(tpart+".MODEL_MOD_PART");
                    if (tm != null) {
                        modelHash.put("MODEL_MOD_PART", tm);
                    }
                    String tc = (String)modelAfterHash.get(tpart+".MODEL_CLASS_PATH");
                    if (tc != null) {
                        modelHash.put("MODEL_CLASS_PATH", tc);
                    }
                    String tt = (String)modelAfterHash.get(tpart+".TYPE");
                    if (tt != null) {
                        modelHash.put("MODELTYPE", tt);
                    }
                    try {
                        MBUserBuildModelAfterDialog ptd = new MBUserBuildModelAfterDialog(build, modelHash, parent.getInternalFrame());
                    } catch (MBBuildException mbe) {
                        lep.LogException(mbe);
                    }
                    if (modelHash.containsKey("MODEL_MOD_PART")) {
                        if (!((String)modelHash.get("MODEL_MOD_PART")).trim().equals("")) {
                            modelAfterHash.put(tpart+".MODEL_MOD_PART", (String)modelHash.get("MODEL_MOD_PART"));
                        }
                        modelAfterHash.put(tpart+".MODEL_CLASS_PATH", (String)modelHash.get("MODEL_CLASS_PATH"));
                        modelAfterHash.put(tpart+".TYPE", (String)modelHash.get("MODELTYPE"));
                    } else {
                        modelAfterHash.remove(tpart+".MODEL_MOD_PART");
                        modelAfterHash.remove(tpart+".MODEL_CLASS_PATH");
                        modelAfterHash.remove(tpart+".TYPE");
                    }
                }

            }
        }
        public void postAction() {
            parent.checkToEnableViewBuildLog();
        }
    }


    private class AddMetaDataEditAction extends CancelableAction {
        AddMetaDataEditAction() {
            super("Metadata Edit");
            setEnabled(false);
        }
        public void doAction(ActionEvent evt) {

            Vector partNameVector = new Vector();
            try {
                Object[] selectedValues = partsList.getSelectedValues();
                for (int i = 0; i < selectedValues.length; i++) {
                    String partClass = build.getPDSMemberClass();
                    String partName = (String) selectedValues[i];
                    InfoForMainframePartReportRetrieval retrieveInfo = new InfoForMainframePartReportRetrieval(partName.toUpperCase(), partClass.toUpperCase(),InfoForMainframePartReportRetrieval.IS_PDS);//TST3337
                    retrieveInfo.setReportType("ALL");
                    Set requestSet = new HashSet();
                    requestSet.add(retrieveInfo);

                    if (releaseSelector.getSelectedRelease()==null) {
                        new MBMsgBox("Warning","Please select a release and try again");

                        return;
                    } else {
                        build.setReleaseInformation(releaseSelector.getSelectedRelease()); 
                    }

                    if (parent.getDriver()==null) {
                        new MBMsgBox("Warning","Please select a driver and try again");

                        return;
                    } else {
                        build.setDriverInformation(parent.getDriver());
                    }

                    com.ibm.sdwb.build390.process.MetadataReport getMetadata = new com.ibm.sdwb.build390.process.MetadataReport(build,null, requestSet, parent);
                    getMetadata.externalRun();


                    Set localFileSet = getMetadata.getLocalOutputFiles();

                    if (!localFileSet.isEmpty()) {

                        String valueReportFilename = (String) localFileSet.iterator().next();
                        if (getMetadata.getReturnCode() > 4) {
                            throw new HostError("There was an error retrieving a metadata report.", valueReportFilename.substring(0,valueReportFilename.indexOf(MBConstants.CLEARFILEEXTENTION)), getMetadata.getReturnCode());
                        }

                        Map currentSetValues = null;

                        if (metadataMap.containsKey(partName)) {
                            currentSetValues = (Map)metadataMap.get(partName);
                        }


                        MetadataValueGenerator metadataValues = new MetadataValueGenerator(valueReportFilename,(com.ibm.sdwb.build390.userinterface.UserCommunicationInterface)parent.getInternalFrame());
                        metadataValues.setLibraryMetadata(currentSetValues);
                        metadataValues.setFileInfo(new PDSFileInfo(build.getPDSMemberClass(),partName));//TST3337

                        GeneratedMetadataInfo generatedMetadataInfo = metadataValues.getGeneratedMetadataInfo();
                        generatedMetadataInfo.setReleaseAndDriverInformation(build.getReleaseInformation(),build.getDriverInformation());
                        generatedMetadataInfo.setDontSaveMetadataInLibrary(true);
                        SingleSourceMetadataEditorFrame editPanel = new SingleSourceMetadataEditorFrame(generatedMetadataInfo,build.getSetup().getMainframeInfo(), build.getSetup().getLibraryInfo(),lep);
                        editPanel.setReportSaveLocation(build.getBuildPathAsFile());
                        editPanel.waitForClose();


                        Map partMetadata = generatedMetadataInfo.getFileInfo().getMetadata();

                        if (partMetadata != null && !partMetadata.isEmpty()) {
                            metadataMap.put(partName,partMetadata);
                        }

                    }
                }

            } catch (MBBuildException mbe) {
                lep.LogException(mbe);
            }

        }
    }

    private void handlePDSDefaults() {
        if (getProjectChosen()!=null) {
            String rootPath =(String) com.ibm.sdwb.build390.userinterface.RememberedSettingsHandler.getInstance().getPerReleaseSetting(build.getSetup(), getProjectChosen().getLibraryName(),USERBUILD_PDSPATH);
            if (rootPath != null) {
                tfRootPath.setText(rootPath);
                btPartListAdd.setEnabled(true);
                return;
            }
        }
        btPartListAdd.setEnabled(false);
    }

    public void handleUIEvent(UserInterfaceEvent tempEvent) {
        if ((tempEvent instanceof ProcessUpdateEvent) &&
            ((ProcessUpdateEvent)tempEvent).isProcessFinished()) {
            if (listModel.size()  > 0 ) {
                btPartListAdd.setEnabled(true);
                btPartListDeleteAll.setEnabled(true);
                partsList.setEnabled(true);
                restartBuildAgain = true;
            }
        }

    }

    //We don't need this when we move to 1.5
    private void fireListDataChangedManually() {
        if (listModel!=null && listModel.getListDataListeners()!=null) {
            for (int i=0;i<listModel.getListDataListeners().length; i++) {
                if (listModel.getListDataListeners()[i] instanceof PDSUserBuildSourcePanelListener) {
                    ((PDSUserBuildSourcePanelListener)listModel.getListDataListeners()[i]).stateChanged(null);
                }
            }
        }
    }

    class LocalPathKeyListener extends java.awt.event.KeyAdapter {
        /**
         * Invoked when a key has been released.
         */
        public void keyReleased(java.awt.event.KeyEvent e) {
            if (tfRootPath.getText()!=null) {
                if (tfRootPath.getText().length() > 0) {
                    btPartListAdd.setEnabled(true);
                    tfRootPath.setToolTipText(tfRootPath.getText());
                }
            }
        }
    }

    // when the path field looses focus, remove trailing file seperators
    class SymText implements java.awt.event.FocusListener {
        public void focusGained(java.awt.event.FocusEvent event) {
        }

        public void focusLost(java.awt.event.FocusEvent event) {
            Object object = event.getSource();
            if (object == tfRootPath) {
                if (!tfRootPath.getText().equals(null)) {
                    while (tfRootPath.getText().endsWith(java.io.File.separator)) {
                        tfRootPath.setText(tfRootPath.getText().substring(0, tfRootPath.getText().length()-1));
                    }
                }
            }
        }
    }

    public Map getMetadataMap() {
        return metadataMap;
    }


    public SourceInfo getSourceInfo() {
        HashSet parts = new HashSet(Arrays.asList(build.getLocalParts()));

        LocalSourceInfo localSourceInfo = new LocalSourceInfo(parts, "");

        // files - get list from list box and save in file
        Object[] tempOrig    = listModel.toArray();
        String[] tempDest    = new String[tempOrig.length+1];
        String[] tempVersion = new String[tempOrig.length+1];
        String[] tempModel_mod_part   = new String[tempOrig.length+1];
        String[] tempModel_class_path = new String[tempOrig.length+1];
        String[] tempModelType = new String[tempOrig.length+1];
        tempVersion[0] = " ";
        if (tempOrig.length > 0) {
            tempDest[0] = tfRootPath.getText();
            for (int i = 1; i < tempOrig.length+1; i++) {
                tempDest[i] = (String)tempOrig[i-1]; // member
                if (members != null) {
                    Enumeration memEnum = members.elements();
                    while (memEnum.hasMoreElements()) {
                        String thismem = (String)memEnum.nextElement();
                        if (thismem.indexOf(" ")>-1) {
                            if (thismem.startsWith(tempDest[i]+" ")) {
                                tempVersion[i] = thismem.substring(thismem.indexOf(" ")+1);
                            }
                        }
                    }
                } else {
                    //the code below to listPDS is repeating over and over..should be put a method.. 
                    String tempVer[] = build.getPDSMemberVersions();
                    Vector findmemver=null;
                    findmemver = getPDSMemberList(build,tempDest[0]);
                    tempDest[i] = (String)tempOrig[i-1]; 
                    Enumeration verEnum = findmemver.elements();
                    while (verEnum.hasMoreElements()) {
                        String thismem = (String)verEnum.nextElement();
                        if (thismem.indexOf(" ")>-1) {
                            lep.LogSecondaryInfo("DEBUG:","MBUserBuildPage:setBuildArgs:LIST PDS : tempDest i "+i+"="+tempDest[i]);
                            if (thismem.startsWith(tempDest[i]+" ")) {
                                tempVersion[i] = thismem.substring(thismem.indexOf(" ")+1);
                                lep.LogSecondaryInfo("DEBUG:","MBUserBuildPage:setBuildArgs:LIST PDS : Version for "+tempDest[i]+ " is " + tempVersion[i]);
                            }
                        }
                    }

                }

                // if fasttrack and a part has a model defined, set it
                if (build.getFastTrack()) {
                    String tModel_mod_part = null;
                    String tModel_class_path = null;

                    String tModelType = null;
                    tModel_mod_part   = (String)modelAfterHash.get((String)tempOrig[i-1]+".MODEL_MOD_PART");
                    tModel_class_path = (String)modelAfterHash.get((String)tempOrig[i-1]+".MODEL_CLASS_PATH");

                    if (tModel_mod_part!=null) {
                        tempModel_mod_part[i] = tModel_mod_part;
                    }
                    if (tModel_class_path!=null) {
                        tempModel_class_path[i] = tModel_class_path;
                    }
                    tModelType = (String)modelAfterHash.get((String)tempOrig[i-1]+".TYPE");
                    if (tModelType!=null) {
                        tempModelType[i] = tModelType;
                    }
                }
            }
        }
        // set local part names
        build.setLocalParts(tempDest);
        // set models
        build.setPartModels(tempModel_mod_part, tempModel_class_path);
        // set model types
        build.setPartModelTypes(tempModelType);
        // set class of parts in pds
        build.setPDSMemberClass(memberClass);
        // set version numbers
        build.setPDSMemberVersions(tempVersion);
        File   buildpath_ = new File(build.getBuildPath());                                // path to build data
        // set base path for build files
        //basepath_   = buildpath_.getAbsolutePath();

        // update the title on the dialog
        String title = new String(parent.getInternalFrame().getTitle());
        int idx = title.indexOf("(");
        if (idx > -1) parent.getInternalFrame().setTitle(title.substring(0,idx)+" ("+build.get_buildid()+")");
        else parent.getInternalFrame().setTitle(title+" ("+build.get_buildid()+")");

        return localSourceInfo;
    }

    public void setSourceInfo(SourceInfo temp) {
        LocalSourceInfo info = (LocalSourceInfo) temp;
        MBUBuild build = info.getBuild();
        releaseSelector.select(build.getReleaseInformation().getLibraryName());
        memberClass = build.getPDSMemberClass();
        String[] cl;
        cl = build.getLocalParts();
        if (cl != null) {
            if (cl.length > 0) {
                for (int i = 1; i < cl.length; i++) {
                    listModel.addElement(cl[i]);
                    if (build.getFastTrack()) {
                        if (build.getPartModels_mod_part()[i]!=null) {
                            modelAfterHash.put(build.getLocalParts()[i]+".MODEL_MOD_PART", build.getPartModels_mod_part()[i]);
                            modelAfterHash.put(build.getLocalParts()[i]+".TYPE", build.getPartModelTypes()[i]);
                        }
                        if (build.getPartModels_class_path()[i]!=null) {
                            modelAfterHash.put(build.getLocalParts()[i]+".MODEL_CLASS_PATH", build.getPartModels_class_path()[i]);
                        }
                    }
                }
                // make sure path does not end with a slash
                if (cl[0] != null) {
                    String tmp = new String(cl[0]);
                    while (tmp.endsWith(java.io.File.separator)) {
                        tmp = tmp.substring(0, tmp.length()-1);
                    }
                    tfRootPath.setText(tmp);
                }
            }
        }

    }

    public com.ibm.sdwb.build390.mainframe.ReleaseInformation getProjectChosen() {
        if (releaseSelector.getSelectedRelease()!=null) {
            return releaseSelector.getSelectedRelease();
        }
        return null;
    }

    public boolean isRequiredActionCompleted() {
        return !listModel.isEmpty();
    }

    private class PDSUserBuildSourcePanelListener implements java.awt.event.ActionListener, javax.swing.event.ChangeListener, javax.swing.event.ListDataListener {
        private RequiredActionsCompletedInterface required = null;

        public PDSUserBuildSourcePanelListener(RequiredActionsCompletedInterface temp) {
            required = temp;
        }


        public void actionPerformed(java.awt.event.ActionEvent e) {
            doEventStuff();
            if (e.getSource()==releaseSelector.getComboBox()) {
                handlePDSDefaults();
                ReleaseUpdateEvent rue = new ReleaseUpdateEvent(e.getSource());
                rue.setReleaseInformation(releaseSelector.getSelectedRelease());
                fireEvent(rue);
            }
        }

        public void stateChanged(javax.swing.event.ChangeEvent e) {
            doEventStuff();
        }

        public void contentsChanged(javax.swing.event.ListDataEvent e) {
            doEventStuff();
        }

        public void intervalAdded(javax.swing.event.ListDataEvent e) {
            doEventStuff();
        }

        public void intervalRemoved(javax.swing.event.ListDataEvent e) {
            doEventStuff();
        }

        private void doEventStuff() {
            UserInterfaceEvent newEvent = new UserInterfaceEvent(required);
            fireEvent(newEvent);
            if (restartBuildAgain) {
                ProcessUpdateEvent processUpdateEvent = new ProcessUpdateEvent(this);
                processUpdateEvent.setStartFromBeginning();
                fireEvent(processUpdateEvent);
            }
        }
    }
}
