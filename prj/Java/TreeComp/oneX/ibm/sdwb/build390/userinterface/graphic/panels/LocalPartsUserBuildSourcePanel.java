package com.ibm.sdwb.build390.userinterface.graphic.panels;
/*********************************************************************/
/* MBLocalPartsUserBuildPage class for the Build/390 client          */
/*  Creates and manages the Local Parts UserBuild                    */
/*********************************************************************/
//05/30/2003 #Feat.INT1178:  Enhance /test parm for improved tracking
//08/19/2003 #DEF.TST1380: MDE changes not stored in unimodc for user build
//09/11/2003 #DEF:TST1498  Driver up to date msg.
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
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Map;
import java.util.Set;

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
import com.ibm.sdwb.build390.info.FileInfo;
import com.ibm.sdwb.build390.info.InfoForMainframePartReportRetrieval;
import com.ibm.sdwb.build390.library.LocalSourceInfo;
import com.ibm.sdwb.build390.library.SourceInfo;
import com.ibm.sdwb.build390.logprocess.LogEventProcessor;
import com.ibm.sdwb.build390.mainframe.ReleaseInformation;
import com.ibm.sdwb.build390.mainframe.parser.DriverReportParser;
import com.ibm.sdwb.build390.metadata.info.GeneratedMetadataInfo;
import com.ibm.sdwb.build390.metadata.utilities.MetadataValueGenerator;
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
import com.ibm.sdwb.build390.userinterface.graphic.widgets.ReleaseSelectionCombo;
import com.ibm.sdwb.build390.userinterface.graphic.widgets.RequiredActionsCompletedInterface;

/** Create the LocalPartsUserBuildSourcePanel */
public class LocalPartsUserBuildSourcePanel extends UserBuildSource {
    private static final String PROTOTYPE_CELL_WIDTH = "ABCDEFGHIJKLMNOPQRSTUVWXYZAb";

    public static final String USERBUILD_ROOTPATH  = "USERBUILD_ROOTPATH";

    private JButton btBrowseRoot = new JButton("Browse Directory");
    private JButton btPartListDelete    = new JButton("Delete    ");
    private JButton btPartListDeleteAll = new JButton("Delete All");
    private JButton btPartListEdit      = new JButton("Edit      ");
    private JButton btPartListAdd       = new JButton("Add       ");
    private DefaultListModel listModel = new DefaultListModel();
    private JList partsList = new JList(listModel);
    private JTextField tfRootPath = new JTextField(20);
    private JLabel releaseLabel = new JLabel("Release");
    private JLabel partListLabel = new JLabel("Part List");
    private JLabel sourceLocationLabel = new JLabel("Source Directory");
    private ReleaseSelectionCombo releaseSelector;
    private LogEventProcessor lep;
    private MBUBuild build;
    private UserBuildPanel parent;
    private String workingDirectory = new String();
    private MBFileChooser openFileDialog = new MBFileChooser();
    private Action metadataEditAction;
    private JButton modelAfterExistingPart = new JButton("Model After Existing Part");
    private Map metadataMap = new HashMap();
    private Hashtable modelAfterHash = new Hashtable();
    private GridBagLayout gridBag = new GridBagLayout();

    private LocalSourceInfo localSourceInfo = null;
    private JButton metadataEdit;
    private boolean restartBuildAgain = false;

    /**
 * constructor - Create a LocalPartsUserBuildSourcePanel
 * @param MBGUI gui
 */
    public LocalPartsUserBuildSourcePanel(UserBuildPanel parent, MBUBuild build, LogEventProcessor lep) {
        this.parent = parent;

        this.lep = lep;

        this.build = build;

        setWidgets();

        LocalPartsUserBuildSourcePanelListener listener = new LocalPartsUserBuildSourcePanelListener(this);
        listModel.addListDataListener(listener);
        releaseSelector.getComboBox().addActionListener(listener);
        partsList.setModel(listModel);

        openFileDialog = new MBFileChooser();
        openFileDialog.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        openFileDialog.setFileFilter(new javax.swing.filechooser.FileFilter() {
                                         public boolean accept(File f) {
                                             return true;
                                         }
                                         public String getDescription() {
                                             return "No Files";
                                         }
                                     });


        SymText lSymText = new SymText();
        tfRootPath.addFocusListener(lSymText);
        tfRootPath.setEditable(true); //PTM4653 //turning it back on. refer TST3197 for remarks.
        tfRootPath.addKeyListener(new LocalPathKeyListener());

        partsList.setBackground(MBGuiConstants.ColorFieldBackground);

        partsList.setPrototypeCellValue(PROTOTYPE_CELL_WIDTH);

        setListeners();

        handleRootDefaults();
        setVisible(true);
    }

    protected void setListeners() {
        // Model part after an existing part, fasttrack only
        modelAfterExistingPart.addActionListener(new LocalPartsModelAction(parent.getInternalFrame()));

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
                                                       btPartListEdit.setEnabled(false);
                                                       btPartListDelete.setEnabled(false);
                                                       btPartListDeleteAll.setEnabled(false);
                                                   }

                                                   metadataEditAction.setEnabled(false);
                                                   modelAfterExistingPart.setEnabled(false);

                                                   parent.checkToEnableViewBuildLog();

                                                   partsList.setPrototypeCellValue(PROTOTYPE_CELL_WIDTH);
                                                   repaint();
                                                   //1.4 bug. we dont need this method in 1.5. keep a tab on it. and remove it in 1.5.
                                                   fireListDataChangedManually();
                                               }
                                           } );

        // Delete part names from list box
        btPartListDeleteAll.addActionListener(new MBCancelableActionListener(parent.getInternalFrame()) {
                                                  public void doAction(ActionEvent evt) {
                                                      listModel.removeAllElements();
                                                      partsList.setModel(listModel);
                                                      // disable menu selections // defect_367
                                                      //btPartListEdit.setEnabled(false);
                                                  }

                                                  public void postAction() {
                                                      parent.checkToEnableViewBuildLog();
                                                      btPartListEdit.setEnabled(false);
                                                      btPartListDelete.setEnabled(false);
                                                      btPartListDeleteAll.setEnabled(false);
                                                      //btLocalPartsModel.setEnabled(false);

                                                      metadataEditAction.setEnabled(false);
                                                      modelAfterExistingPart.setEnabled(false);
                                                      partsList.setPrototypeCellValue(PROTOTYPE_CELL_WIDTH);
                                                      repaint();
                                                      //1.4 bug. we dont need this method in 1.5. keep a tab on it. and remove it in 1.5.
                                                      fireListDataChangedManually();
                                                  }
                                              } );


        // Edot part names from list box
        btPartListEdit.addActionListener(new MBCancelableActionListener(parent.getInternalFrame()) {
                                             public void doAction(ActionEvent evt) {
                                                 Object[] idxs = partsList.getSelectedValues();
                                                 if (idxs.length > 0) {
                                                     for (int i = 0; i < idxs.length; i++) {
                                                         String fullFilename = tfRootPath.getText();
                                                         if (!fullFilename.endsWith(java.io.File.separator)) {
                                                             fullFilename+=java.io.File.separator;
                                                         }
                                                         fullFilename += idxs[i].toString();
                                                         File testFile = new File(fullFilename);
                                                         if (testFile.canWrite()) {
                                                             new MBEdit(fullFilename, true,lep);
                                                         } else {
                                                             parent.getInternalFrame().problemBox("Edit Error", "I cannot get write access to " + fullFilename+ ".\nPlease check your permissions.");
                                                         }
                                                     }
                                                 }
                                             }
                                             public void postAction() {
                                                 parent.checkToEnableViewBuildLog();
                                             }
                                         });


        // add parts to list box, either local or from pds
        btPartListAdd.addActionListener(new PartListAddAction(parent.getInternalFrame()));

        // Browse button, set the relative directory or the PDS file name
        btBrowseRoot.addActionListener(new MBCancelableActionListener(parent.getInternalFrame()) {
                                           public void doAction(ActionEvent evt) {
                                               // Change_FileDlg
                                               String currDir = tfRootPath.getText();
                                               if (currDir.trim().length() > 0) {
                                                   openFileDialog.setCurrentDirectory(new File(currDir));
                                               }
                                               openFileDialog.setDialogTitle("Select the local parts Source Directory");

                                               openFileDialog.showDialog(parent.getInternalFrame(), "Select");
                                               if (openFileDialog.getSelectedFile() != null) {
                                                   String ed = new String(openFileDialog.getSelectedFile().getAbsolutePath());
                                                   if (!ed.endsWith("null")) {
                                                       // no ending slash
                                                       if (ed.endsWith(java.io.File.separator)) {
                                                           ed = ed.substring(0, ed.length()-1);
                                                       }
                                                       workingDirectory = ed;
                                                       tfRootPath.setText(ed);
                                                       if (tfRootPath.getText() != null) {
                                                           if (getProjectChosen()!=null) {
                                                               com.ibm.sdwb.build390.userinterface.RememberedSettingsHandler.getInstance().addPerReleaseSetting(build.getSetup(), getProjectChosen().getLibraryName(), USERBUILD_ROOTPATH, tfRootPath.getText());
                                                           }
                                                       }
                                                       listModel.clear();
                                                       btPartListAdd.setEnabled(true);
                                                       repaint();
                                                   }
                                               }
                                           }
                                           public void postAction() {
                                               if (listModel.size()  > 0 ) {
                                                   btPartListAdd.setEnabled(true);
                                                   btPartListDeleteAll.setEnabled(true);
                                               }
                                               tfRootPath.setToolTipText(tfRootPath.getText());
                                               parent.checkToEnableViewBuildLog();
                                           }
                                       } );

        // Disable delete menu item Defect_144
        partsList.addListSelectionListener(new ListSelectionListener() {
                                               public void valueChanged(ListSelectionEvent ie) {
                                                   // if anything in part list box is selected, enable delete, else disable delete
                                                   // also if fasttrack enable model after
                                                   int[]  idxs  = partsList.getSelectedIndices();
                                                   btPartListEdit.setEnabled(false);
                                                   btPartListDelete.setEnabled(false);
                                                   //btLocalPartsModel.setEnabled(false);
                                                   if (idxs.length > 0) {

                                                       //#Def.INT0792:  
                                                       btPartListDelete.setEnabled(true);
                                                       btPartListEdit.setEnabled(true);

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
                                                             btPartListAdd.setEnabled(false);
                                                             if (tfRootPath.getText().length()>0) {
                                                                 btPartListAdd.setEnabled(true);
                                                             }
                                                         }
                                                         public void removeUpdate(DocumentEvent e) {
                                                             btPartListAdd.setEnabled(false);
                                                             if (tfRootPath.getText().length()>0) {
                                                                 btPartListAdd.setEnabled(true);
                                                             }
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

        //

        c.gridx = 2;
        c.gridy = 0;
        c.weighty = 1;
        c.insets = new Insets(2,5,2,5);
        c.fill = GridBagConstraints.HORIZONTAL;
        c.gridwidth = GridBagConstraints.REMAINDER;
        metadataEditAction = new AddMetaDataEditAction();
        metadataEdit = new JButton(metadataEditAction);
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
        c.weightx= 1;
        c.fill = GridBagConstraints.HORIZONTAL;
        c.gridwidth = GridBagConstraints.REMAINDER;

        releaseSelector = new ReleaseSelectionCombo(build.getMainframeInfo(), build.getLibraryInfo(),lep); 
        gridBag.setConstraints(releaseSelector, c);
        add(releaseSelector);
//
        c.gridx = 0;
        c.gridy = 2;
        c.weighty = 1;
        c.insets = new Insets(2,5,2,5);
        c.anchor = GridBagConstraints.WEST;

        c.gridwidth = 1;
        gridBag.setConstraints(sourceLocationLabel,c);
        add(sourceLocationLabel);
//
        c.gridx=1;
        c.gridy=2;
        c.insets = new Insets(2,5,2,5);
        c.anchor = GridBagConstraints.WEST;
        c.gridwidth = 1;
        gridBag.setConstraints(tfRootPath,c);
        add(tfRootPath);
//
        c.gridx = 0;
        c.gridy=3;
        c.gridwidth = 1;
        gridBag.setConstraints(partListLabel, c);
        add(partListLabel);
//
        c.gridx=1;
        c.gridy=3;
        c.gridwidth = 1;
        JScrollPane listScroller = new JScrollPane(partsList);
        listScroller.setPreferredSize(partsList.getPreferredScrollableViewportSize());
        gridBag.setConstraints(listScroller, c);
        add(listScroller);
//
        c.gridx = 2;
        c.gridy=2;
        c.weightx = 1;
        c.fill = GridBagConstraints.HORIZONTAL;
        c.gridwidth = GridBagConstraints.REMAINDER;
        gridBag.setConstraints(btBrowseRoot,c);
        add(btBrowseRoot);
//
        MBInsetPanel pB = new MBInsetPanel(new GridLayout(4,1),2,0,2,0);
        pB.add(btPartListAdd);
        pB.add(btPartListEdit);
        pB.add(btPartListDelete);
        pB.add(btPartListDeleteAll);
        c.gridx=2;
        c.gridy=3;
        gridBag.setConstraints(pB,c);
        add(pB);
    }

    /** Determine if a file name or member is unique in the file list box
    * @param String memfile is the member or file to check */
    boolean isMemberUnique(String memfile) {
/*
        // if unique, return true
        if (!listModel.isEmpty()) {
            Object[] cmems = listModel.toArray();
            for (int idx1=0; idx1<cmems.length; idx1++) {
                if (memfile.equals(cmems[idx1].toString())) {
                    return false;
                }
            }
        }
*/      
        return true;
    }



    class PartListAddAction extends MBCancelableActionListener {
        PartListAddAction(MBAnimationStatusWindow temp) {
            super(temp);
        }

        public void doAction(ActionEvent evt) {
            // if a root path or pds has been specified

            // metadata Editor


            if (tfRootPath.getText().length()> 0) {
                //Defect 64 : fix for choosing Add again by changing the working directory textfield didnot pick up the new working directory
                String pathentered = tfRootPath.getText().trim();
                if (pathentered.endsWith(java.io.File.separator)) {
                    pathentered = pathentered.substring(0, pathentered.length()-1);
                } else {
                    if ((new File(pathentered)).getPath().endsWith(":")) {
                        pathentered +=  File.separator;
                    }
                }

                workingDirectory = pathentered;
                if ((new File(workingDirectory)).exists()) {
                    MBFileSelector openFileDialog1=null;
                    if (workingDirectory.length()> 0) {
                        openFileDialog1 = new MBFileSelector(new File(workingDirectory), true, parent.getInternalFrame());
                    }
                    File[] selectedFiles = openFileDialog1.getSelectedFiles();
                    if (selectedFiles != null) {
                        //06/15/2000 added this when nothing is selected , and cancel is pressed it shouldnt go beyond this
                        if (selectedFiles.length>0) {
                            if (selectedFiles[0].getAbsolutePath().startsWith(tfRootPath.getText())) {
                                String tempWork = selectedFiles[0].getAbsolutePath();
                                workingDirectory = tempWork.substring(0, tempWork.lastIndexOf(File.separator));
                            }

                            if (selectedFiles!= null) {
                                String ed = new String(selectedFiles[0].getAbsolutePath());
                                if (ed.startsWith(tfRootPath.getText())) {
                                    // FileCase
                                    // Windows allows only one file in a directory with a given name regardless of case
                                    // but lies about the filename in the file dialog. It changes the case, so we need to
                                    // get a list of files in the directory selected that match the file selected
                                    // when both are converted to upper case.
                                    // Put matching file into the list box and continue.
                                    if (System.getProperty("os.name").indexOf("Window") > -1) {
                                        for (int i = 0 ; i < selectedFiles.length; i++) {
                                            String tempName = selectedFiles[i].getAbsolutePath();
                                            if (isMemberUnique(tempName.substring(tfRootPath.getText().length()+1))) {
                                                listModel.addElement(tempName.substring(tfRootPath.getText().length()+1));
                                            }
                                        }

                                    } else {
                                        for (int i = 0 ; i < selectedFiles.length; i++) {
                                            String tempName = selectedFiles[i].getAbsolutePath();
                                            if (isMemberUnique(tempName.substring(tfRootPath.getText().length()+1))) {
                                                listModel.addElement(tempName.substring(tfRootPath.getText().length()+1));
                                            }
                                        }
                                    }

                                    partsList.setModel(listModel);
                                    if (listModel.getSize() >= 1) {
                                        btPartListDeleteAll.setEnabled(true);
                                    }

                                    repaint();
                                } else {

                                    parent.getInternalFrame().problemBox("Error", "You can only select parts that are within (or within a desendant of) the root library path.");
                                }
                            }

                            if (listModel.getSize() >= 1) {
                                btPartListDeleteAll.setEnabled(true);
                            }
                        }
                    }
                } else {
                    //if a invalid path name is entered we must display to the user
                    parent.getInternalFrame().problemBox("Error", "You must specify a valid Local Path.\nLocalPath : "+ (new File(workingDirectory)).getPath() + "  doesnt exist.");
                }
                // }
            } else {
                parent.getInternalFrame().problemBox("Error", "You must specify the root path before adding parts");
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

    //We don't need this when we move to 1.5
    private void fireListDataChangedManually() {
        if (listModel!=null && listModel.getListDataListeners()!=null) {
            for (int i=0;i<listModel.getListDataListeners().length; i++) {
                if (listModel.getListDataListeners()[i] instanceof LocalPartsUserBuildSourcePanelListener) {
                    ((LocalPartsUserBuildSourcePanelListener)listModel.getListDataListeners()[i]).stateChanged(null);
                }
            }
        }

    }


    private class AddMetaDataEditAction extends CancelableAction {
        AddMetaDataEditAction() {
            super("Metadata Edit");
            setEnabled(false);
        }

        public void doAction(ActionEvent evt) {
            try {
                boolean loopit = true;
                Object[]  partClassArrayObject = partsList.getSelectedValues();
                String partClass=null;

                for (int i = 0; i < partClassArrayObject.length & !stopped; i++) {
                    String partName = null;
                    int dotIndex = ((String)partClassArrayObject[i]).lastIndexOf(".");
                    if (dotIndex>-1) {
                        partName = ((String)partClassArrayObject[i]).substring(0, dotIndex);
                    } else {
                        partName = (String)partClassArrayObject[i];
                    }
                    int slashIndex = 0;
                    if ((slashIndex = partName.lastIndexOf(java.io.File.separator))>-1) {
                        partName = partName.substring(slashIndex+1, partName.length());
                    }

                    if (partName!=null && partName.trim().length() > 8) {
                        partName = partName.substring(0,8);//TST2179
                    }

                    String tempPartClass=null;

                    if (partClass == null) {
                        MBUserBuildPartClassDialog pc = new MBUserBuildPartClassDialog(build, partName, parent.getInternalFrame());
                        if (pc.getValidate()) {
                            if (pc.isSameClassForAllParts()) {
                                partClass = pc.getmclass();
                                tempPartClass = partClass;
                            } else {
                                tempPartClass = pc.getmclass();
                            }
                        } else {
                            stopped = true;
                        }
                    } else {
                        tempPartClass = partClass;
                    }

                    if (!stopped) {
                        InfoForMainframePartReportRetrieval retrieveInfo = new InfoForMainframePartReportRetrieval(partName.toUpperCase(), tempPartClass);
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


                        com.ibm.sdwb.build390.process.MetadataReport getMetadata = new com.ibm.sdwb.build390.process.MetadataReport(build,null, requestSet, parent.getInternalFrame());


                        getMetadata.externalRun();
                        Set retrievedFiles = getMetadata.getLocalOutputFiles();
                        if (!retrievedFiles.isEmpty()) {

                            String valueReportFilename = (String) retrievedFiles.iterator().next();
                            if (getMetadata.getReturnCode() > 4) {
                                throw new HostError("There was an error retrieving a metadata report.", valueReportFilename.substring(0,valueReportFilename.indexOf(MBConstants.CLEARFILEEXTENTION)), getMetadata.getReturnCode());
                            }


                            Map currentSetValues = null;


                            String tempDirPath = tfRootPath.getText().trim()+File.separator+(String)partClassArrayObject[i];
                            String rootPath = tfRootPath.getText().trim();
                            File tempFile = new File(tempDirPath);
                            String basicName = tempFile.getName();       

                            String pathString =  tempDirPath.substring((rootPath+File.pathSeparator).length(), tempDirPath.length()-basicName.length()).replace(File.separatorChar, '/').trim();
                            if (pathString.equals("/")) {
                                pathString = "";
                            }
                            String key = pathString + basicName;


                            if (metadataMap.containsKey(key)) {
                                currentSetValues = (Map)metadataMap.get(key);
                            }

                            MetadataValueGenerator metadataValues = new MetadataValueGenerator(valueReportFilename,(com.ibm.sdwb.build390.userinterface.UserCommunicationInterface)parent.getInternalFrame());
                            metadataValues.setLibraryMetadata(currentSetValues);
                            metadataValues.setFileInfo(new FileInfo(pathString,basicName));

                            GeneratedMetadataInfo generatedMetadataInfo = metadataValues.getGeneratedMetadataInfo();
                            generatedMetadataInfo.setReleaseAndDriverInformation(build.getReleaseInformation(),build.getDriverInformation());
                            generatedMetadataInfo.setDontSaveMetadataInLibrary(true);
                            SingleSourceMetadataEditorFrame editPanel = new SingleSourceMetadataEditorFrame(generatedMetadataInfo,build.getSetup().getMainframeInfo(), build.getSetup().getLibraryInfo(),lep);
                            editPanel.setReportSaveLocation(build.getBuildPathAsFile());
                            editPanel.waitForClose();

                            Map partMetadata = generatedMetadataInfo.getFileInfo().getMetadata();

                            if (partMetadata != null && !partMetadata.isEmpty()) {
                                metadataMap.put(key,partMetadata);
                            }
                        }
                    }
                }
            } catch (MBBuildException mbe) {
                lep.LogException(mbe);
            }
        }
    }

    private void handleRootDefaults() {
        if (getProjectChosen()!=null) {
            String rootPath =(String) com.ibm.sdwb.build390.userinterface.RememberedSettingsHandler.getInstance().getPerReleaseSetting(build.getSetup(), getProjectChosen().getLibraryName(),USERBUILD_ROOTPATH);
            if (rootPath != null) {
                tfRootPath.setText(rootPath);
                btPartListAdd.setEnabled(true);
                return;
            }
        }
        btPartListAdd.setEnabled(false);
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
                    if (restartBuildAgain) {
                        ProcessUpdateEvent processUpdateEvent = new ProcessUpdateEvent(this);
                        processUpdateEvent.setStartFromBeginning();
                        fireEvent(processUpdateEvent);
                    }
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

        // ugly hack, for now set the stuff here

        // files - get list from list box and save in file
        Object[] tempOrig    = listModel.toArray();
        String[] tempDest    = new String[tempOrig.length+1];
        String[] tempVersion = new String[tempOrig.length+1];
        String[] tempModel_mod_part   = new String[tempOrig.length+1];
        String[] tempModel_class_path = new String[tempOrig.length+1];
        String[] tempModelType = new String[tempOrig.length+1];
        tempVersion[0] = " ";
        if (tempOrig.length > 0) {
            if (!tfRootPath.getText().endsWith(java.io.File.separator)) {
                tempDest[0] = tfRootPath.getText() + java.io.File.separator;
            } else {
                tempDest[0] = tfRootPath.getText();
            }

            StringBuffer localFilesNotFound = new StringBuffer();

            for (int i = 1; i < tempOrig.length+1; i++) {
                tempDest[i] = tempDest[0] + (String) tempOrig[i-1]; // root path + file
                // if fasttrack and a part has a model defined, set it
                File tempLocalFile = new File(tempDest[i]);
                if (!tempLocalFile.exists()) {
                    localFilesNotFound.append(tempLocalFile.getAbsolutePath() + "\n");
                }

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

            if (localFilesNotFound.length() >0) {
                throw new RuntimeException("The following local path doesnt exists.\n" +
                                           localFilesNotFound.toString() +
                                           "Please specify a valid local path.\n");
            }

        }
        // set local part names
        build.setLocalParts(tempDest);
        // set models
        build.setPartModels(tempModel_mod_part, tempModel_class_path);
        // set model types
        build.setPartModelTypes(tempModelType);
        // set class of parts in pds
        build.setPDSMemberClass(null);
        // set version numbers
        build.setPDSMemberVersions(null);
        File   buildpath_ = new File(build.getBuildPath());                                // path to build data

        // update the title on the dialog
        String title = new String(parent.getInternalFrame().getTitle());
        int idx = title.indexOf("(");
        if (idx > -1) {
            parent.getInternalFrame().setTitle(title.substring(0,idx)+" ("+build.get_buildid()+")");
        } else {
            parent.getInternalFrame().setTitle(title+" ("+build.get_buildid()+")");
        }

        return localSourceInfo;
    }

    public void setSourceInfo(SourceInfo temp) {
        LocalSourceInfo info = (LocalSourceInfo) temp;
        MBUBuild build = info.getBuild();
        releaseSelector.select(build.getReleaseInformation().getLibraryName());
        String[] cl;
        cl = build.getLocalParts();
        if (cl != null) {
            if (cl.length > 0) {
                for (int i = 1; i < cl.length; i++) {
                    listModel.addElement(cl[i].substring(cl[0].length()));
                    if (build.getFastTrack()) {
                        if (build.getPartModels_mod_part() !=null && (build.getPartModels_mod_part().length > 1) &&  build.getPartModels_mod_part()[i]!=null) {
                            modelAfterHash.put(build.getLocalParts()[i]+".MODEL_MOD_PART", build.getPartModels_mod_part()[i]);
                            modelAfterHash.put(build.getLocalParts()[i]+".TYPE", build.getPartModelTypes()[i]);
                        }

                        if (build.getPartModels_class_path() !=null && (build.getPartModels_class_path().length > 1) && build.getPartModels_class_path()[i]!=null) {
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

    public void handleUIEvent(UserInterfaceEvent tempEvent) {
        if ((tempEvent instanceof ProcessUpdateEvent) &&
            ((ProcessUpdateEvent)tempEvent).isProcessFinished()) {
            if (listModel.size()  > 0 ) {
                btPartListAdd.setEnabled(true);
                btPartListDeleteAll.setEnabled(true);
                partsList.setEnabled(true);
                tfRootPath.setEnabled(true);
                btBrowseRoot.setEnabled(true);
                restartBuildAgain = true;
            }
        }

    }

    private class LocalPartsUserBuildSourcePanelListener implements java.awt.event.ActionListener, javax.swing.event.ChangeListener, javax.swing.event.ListDataListener {
        private RequiredActionsCompletedInterface required = null;

        public LocalPartsUserBuildSourcePanelListener(RequiredActionsCompletedInterface temp) {
            required = temp;
        }


        public void actionPerformed(java.awt.event.ActionEvent e) {
            doEventStuff();
            if (e.getSource()==releaseSelector.getComboBox()) {
                handleRootDefaults();
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



