package com.ibm.sdwb.build390;
/*********************************************************************/
/* MBSmodRetrievePage class for the Build/390 client                  */
/*  Creates and manages the SMOD Retrieve Page                        */
// 04/27/99 errorHandling     change LogException parms & add new error types
// 06/17/99	#360		add a browse button for the save path
// 01/07/2000 ind.build.log     changes for logging in build details into a individual build log
// 03/07/2000 reworklog         changes to rewrite the log stuff using listeners
// 06/20/2000 Defect 55         fix - Due to changes in TableCellRenderer for MBLogRetrievePage - scrolling spit out a bunch of errors. and also there were modification to pass HFSPATH/LOCALPATH to LOGRETRIEVE verb.
//12/03/2002 SDWB-2019 Enhance the help system
/*********************************************************************/
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.util.*;

import javax.swing.*;
import javax.swing.table.*;
import com.ibm.sdwb.build390.logprocess.*;
import com.ibm.sdwb.build390.help.*;
import com.ibm.sdwb.build390.info.InfoForMainframePartReportRetrieval;

/** Create the SMOD Retrieval page */
public class MBSmodRetrievePage extends MBInternalFrame {

    private MBBuild     build;
    private MBSmodDrvrReport  dr;
    private MBSmodTableModel logTableModel= new MBSmodTableModel();
    private JTable logInfoTable = new JTable(logTableModel);
    private JButton bGetInfo = new JButton("Get Part Data");
    private JButton bHelp = new JButton("Help");
    private JMenuItem bSelectAll = new JMenuItem("Select All");
    private JMenuItem bDeselectAll = new JMenuItem("Deselect All");
    private JCheckBox cbSendTo = new JCheckBox("Send To (Node.UserID):");
    private JTextField tfSendTo = new JTextField();
    private JCheckBox cbSavePath = new JCheckBox("Save To:(LocalPath/HFS path/DSN)");

    private JLabel labelSavePath = new JLabel("Prefix with DSN: for OS/390 data set save, HFS: for hfs save");
    private JTextField tfSavePath = new JTextField();
    private JButton btnSavePath = new JButton("Browse");
    private JLabel SMODChoiceLabel = new JLabel("SMOD:");
    private JComboBox SMODCombo = new JComboBox();
    private JLabel buildDateLabel = new JLabel("Build Date:");
    private boolean columnsSet = false;
    private String UMName = new String();
    private String UMDate = new String();
    private JComboBox buildDate = new JComboBox();
    private JCheckBoxMenuItem bViewParts = new JCheckBoxMenuItem("View Parts");
    private JCheckBoxMenuItem bLongMetaType = new JCheckBoxMenuItem("Long Metadata");

    private Vector columnHeadings = new Vector();
    private static final String SENDTOTEXTCONSTANT = "SMODSENDTOTEXTSETTING";
    private static final String SAVEPATHTEXTCONSTANT = "SMODSAVEPATHTEXTSETTING";
    private static final String SENDTOBOOLEANCONSTANT = "SMODSENDTOBOOLEANSETTING";
    private static final String SAVEPATHBOOLEANCONSTANT = "SMODSAVEPATHBOOLEANSETTING";
    private static final String BINARYCOLLECTORCONSTANT = " is binary collector";


    /**
    * constructor - Create a MBDriverBuildPage
    * @param MBGUI gui
    */
    public MBSmodRetrievePage(MBBuild tempBuild) {
        super("Retrieve "+tempBuild.get_buildtype()+" Data", true, null);
        lep.addEventListener(tempBuild.getLogListener());
        SMODCombo.setEditable(false);
        buildDate.setEditable(false);
        SMODCombo.setBackground(MBGuiConstants.ColorFieldBackground);
        buildDate.setBackground(MBGuiConstants.ColorFieldBackground);
        Box topBox = Box.createHorizontalBox();
        MBInsetPanel topPanel = new MBInsetPanel(new BorderLayout(), 5,5,5,5);
        topPanel.add(topBox);
        getContentPane().add("North", topPanel);
        topBox.add(SMODChoiceLabel);
        topBox.add(SMODCombo);
        topBox.add(Box.createHorizontalStrut(10));
        topBox.add(buildDateLabel);
        topBox.add(buildDate);
        JPanel centerPanel = new JPanel(new BorderLayout());
        getContentPane().add("Center", centerPanel);

        JScrollPane jsp = new JScrollPane(logInfoTable);
        Dimension dm = jsp.getPreferredSize();
        dm.height = dm.height/2;
        jsp.setPreferredSize(dm);
        centerPanel.add("Center", jsp);

        JPanel subCenter = new JPanel(new BorderLayout());
        centerPanel.add("South", subCenter);

        JMenu editMenu = new JMenu("Edit");
        getJMenuBar().add(editMenu);
        editMenu.add(bSelectAll);
        editMenu.add(bDeselectAll);
        final JMenu optionsMenu = new JMenu("Options");
        getJMenuBar().add(optionsMenu);
        optionsMenu.add(bViewParts);
        optionsMenu.add(bLongMetaType);

        JPanel cBoxPanel = new JPanel(new GridLayout(3,1));
        MBInsetPanel tempPanel = new MBInsetPanel(new BorderLayout(), 5,5,5,5);
        if (getGeneric(SENDTOBOOLEANCONSTANT) != null) {
            cbSendTo.setSelected(((Boolean) getGeneric(SENDTOBOOLEANCONSTANT)).booleanValue());;
        }
        tempPanel.add("West", cbSendTo);
        if (getGeneric(SENDTOTEXTCONSTANT) != null) {
            tfSendTo.setText((String) getGeneric(SENDTOTEXTCONSTANT));
        }
        tempPanel.add("Center", tfSendTo);
        cBoxPanel.add(tempPanel);
        cBoxPanel.add(labelSavePath);
        tempPanel = new MBInsetPanel(new BorderLayout(), 5,5,5,5);
        if (getGeneric(SAVEPATHBOOLEANCONSTANT) != null) {
            cbSavePath.setSelected(((Boolean) getGeneric(SAVEPATHBOOLEANCONSTANT)).booleanValue());
        } else {
            cbSavePath.setSelected(true);
        }
        tempPanel.add("West",cbSavePath);
        if (getGeneric(SAVEPATHTEXTCONSTANT) != null) {
            tfSavePath.setText((String) getGeneric(SAVEPATHTEXTCONSTANT));
        } else {
            tfSavePath.setText(MBGlobals.Build390_path+"logfiles"+File.separator);
        }
        tempPanel.add("Center", tfSavePath);
        tempPanel.add("East", btnSavePath);
        cBoxPanel.add(tempPanel);
        subCenter.add("South", cBoxPanel);
        Vector actionButtons = new Vector();
        actionButtons.addElement(bGetInfo);
        addButtonPanel(bHelp, actionButtons);
        logInfoTable.setColumnSelectionAllowed(false);
        logInfoTable.setRowSelectionAllowed(false);
        logInfoTable.setCellSelectionEnabled(false);
        logInfoTable.setCellSelectionEnabled(false);
        logInfoTable.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
        logInfoTable.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);

        try {
            logInfoTable.setDefaultEditor(Class.forName("java.lang.Boolean"), new GeneralCellEditor(new JCheckBox()));
        } catch (ClassNotFoundException cnfe) {
            lep.LogException("A class is missing.  Checking a checkbox will require 2 clicks.", cnfe);
        }


        setVisible(true);
        build = tempBuild;
        MBCancelableActionListener initPage = new MBCancelableActionListener(thisFrame) {
            public void doAction(ActionEvent evt) {
                try {
                    dr = new MBSmodDrvrReport(build, build.get_buildtype(), getStatus(),lep);
                    dr.initializeReport();
                    Hashtable allGroups = dr.getGroups();
                    Enumeration groupNames = allGroups.keys();
                    while (groupNames.hasMoreElements()) {
                        String currentGroup = (String) groupNames.nextElement();
                        Hashtable groupHash = (Hashtable) allGroups.get(currentGroup);
                        Enumeration ids = groupHash.keys();
                        while (ids.hasMoreElements()) {
                            SMODCombo.addItem((String) ids.nextElement());
                        }
                    }
                    columnHeadings.addElement("Part Name");
                    columnHeadings.addElement("Part Class");
                    columnHeadings.addElement("Metadata");
                    Vector typeVector = dr.getDataTypes();
                    for (int i = 0; i < typeVector.size(); i++) {
                        String heading = (String) typeVector.elementAt(i);
                        columnHeadings.addElement(heading);
                        if (heading.toUpperCase().trim().startsWith("C")) {
                            JCheckBoxMenuItem bCollectorBin = new JCheckBoxMenuItem(heading + BINARYCOLLECTORCONSTANT);
                            optionsMenu.add(bCollectorBin);
                        }
                    }
                    SMODCombo.addItemListener(new ItemListener() {
                                                  public void itemStateChanged(ItemEvent evt) {
                                                      loadDataForUsermod((String) SMODCombo.getSelectedItem());
                                                  }
                                              });
                    buildDate.addItemListener(new ItemListener() {
                                                  public void itemStateChanged(ItemEvent evt) {
                                                      if (buildDate.getSelectedItem() != null) {
                                                          if (!UMName.equals((String) SMODCombo.getSelectedItem()) | !UMDate.equals((String) buildDate.getSelectedItem())) {
                                                              try {
                                                                  loadDataForUsermodDate((String) SMODCombo.getSelectedItem(), (String) buildDate.getSelectedItem());
                                                                  if (!columnsSet) {
                                                                      for (int i = 0; i < columnHeadings.size(); i++) {
                                                                          TableColumn tempCol = logInfoTable.getColumn(columnHeadings.elementAt(i));
                                                                          tempCol.setMinWidth((int) (((double)(new JLabel((String) tempCol.getHeaderValue())).getPreferredSize().width) * 1.5));
                                                                      }
                                                                      for (int i = 2; i < columnHeadings.size(); i++) {
                                                                          TableColumn tempCol = logInfoTable.getColumn(columnHeadings.elementAt(i));
                                                                          tempCol.setCellRenderer(new DefaultTableCellRenderer() {


                                                                                                      Hashtable usedComps = new Hashtable();

                                                                                                      public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
                                                                                                          Component tempComp = table.getDefaultRenderer(table.getColumnClass(column)).getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                                                                                                          if ((!table.getModel().isCellEditable(row, column) & (table.getColumnClass(column).getName().indexOf("Boolean") > -1))) {//& (column > 2)) {
                                                                                                              if (!((Boolean) table.getValueAt(row,column)).booleanValue()) {
                                                                                                                  tempComp = (Component) usedComps.get(Integer.toString(row)+","+Integer.toString(column));
                                                                                                                  if (tempComp == null) {
                                                                                                                      tempComp = new JPanel();
                                                                                                                      usedComps.put(Integer.toString(row)+","+Integer.toString(column), tempComp);
                                                                                                                  }
                                                                                                              }
                                                                                                              return tempComp;
                                                                                                          }
                                                                                                          return tempComp;
                                                                                                      }
                                                                                                  });


                                                                      }
                                                                  }
                                                              } catch (MBBuildException mbe) {
                                                                  lep.LogException(mbe);
                                                              }
                                                          }
                                                      }
                                                  }
                                              });
                    loadDataForUsermod((String) SMODCombo.getSelectedItem());
                    repaint();
                } catch (MBBuildException mbe) {
                    lep.LogException(mbe);
                }
            }
        };
        initPage.actionPerformed(new ActionEvent(this, 0, ""));
        bGetInfo.addActionListener(new MBCancelableActionListener(thisFrame) {
                                       public void doAction(ActionEvent evt) {
                                           try {
                                               boolean submitRequest =true;
                                               String hfsPath = null;
                                               String PDSPath = null;
                                               String localPath = null;
                                               String sendToAddress = null;
                                               String metadataType = null;
                                               if (bLongMetaType.isSelected()) {
                                                   metadataType = "LONG";
                                               } else {
                                                   metadataType = "SHORT";
                                               }
                                               if (!(cbSendTo.isSelected() | cbSavePath.isSelected())) {
                                                   new MBMsgBox("Error", "You must select a directory to save to, or a userid to send to.", thisFrame);
                                                   return;
                                               }
                                               String errorMsg = new String();
                                               if (cbSendTo.isSelected()) {
                                                   if (tfSendTo.getText().trim().length() > 0) {
                                                       sendToAddress = tfSendTo.getText().trim();
                                                   } else {
                                                       errorMsg += "You must enter a valid destination address in the form node.userid.\n";
                                                   }
                                               }
                                               if (cbSavePath.isSelected()) {
                                                   if (tfSavePath.getText().trim().length() > 0) {
                                                       // if not a valid path, error
                                                       String tpath = tfSavePath.getText().trim();

                                                       if (tpath.substring(0,4).toUpperCase().equals("HFS:")) {
                                                           bViewParts.setSelected(false);
                                                           String tpath1  = tpath.substring(tpath.lastIndexOf(":") + 1);
                                                           hfsPath = tpath1.trim();

                                                           MBMsgBox tempBox =  new MBMsgBox("WARNING" ,"HFS directory " + hfsPath +" selected must exist on HOST."+MBConstants.NEWLINE+"Continue?", thisFrame, true);
                                                           submitRequest = tempBox.isAnswerYes();

                                                       } else if (tpath.substring(0,4).toUpperCase().equals("DSN:")) {
                                                           bViewParts.setSelected(false);
                                                           String tpath1  = tpath.substring(tpath.lastIndexOf(":") + 1);
                                                           PDSPath = tpath1.trim().toUpperCase();

                                                       } else {
                                                           localPath = tfSavePath.getText().trim();
                                                       }
                                                   } else {
                                                       errorMsg+="You must enter a file path to save the information to.\n";
                                                   }
                                               } else {
                                                   bViewParts.setSelected(false);
                                               }

                                               if (submitRequest) {
                                                   Set binaryColumns = new HashSet();
                                                   binaryColumns.add("OBJ");
                                                   for (int i = 0; i < optionsMenu.getItemCount(); i++) {
                                                       JMenuItem tempItem = optionsMenu.getItem(i);
                                                       if (tempItem.getText().endsWith(BINARYCOLLECTORCONSTANT)) {
                                                           JCheckBoxMenuItem tempBox = (JCheckBoxMenuItem) tempItem;
                                                           if (tempBox.isSelected()) {
                                                               binaryColumns.add((new StringTokenizer(tempBox.getText())).nextToken());
                                                           }
                                                       }
                                                   }
                                                   if (errorMsg.length() > 0) {
                                                       new MBMsgBox("Error", errorMsg, thisFrame);
                                                       return;
                                                   }
                                                   String usermodName = (String) SMODCombo.getSelectedItem();
                                                   String usermodDate = (String) buildDate.getSelectedItem();
                                                   Hashtable allGroups = dr.getGroups();
                                                   Hashtable valueSource=null;
                                                   boolean smodFound = false;
                                                   Enumeration groupIDs = allGroups.keys();
                                                   while (groupIDs.hasMoreElements()) {
                                                       Hashtable oneGroup = (Hashtable) allGroups.get(groupIDs.nextElement());
                                                       Vector dateVect = (Vector) oneGroup.get(usermodName);
                                                       if (dateVect != null) {
                                                           for (int i = 0; i < dateVect.size(); i++) {
                                                               Hashtable temp = (Hashtable) dateVect.elementAt(i);
                                                               if (usermodDate.equals((String) temp.get("DATE"))) {
                                                                   valueSource = temp;
                                                               }
                                                               smodFound = true;
                                                           }
                                                       }
                                                   }
                                                   if (!smodFound) {
                                                       throw new GeneralError("The date you selected was not found");
                                                   }
                                                   String saveLevel = (String) valueSource.get("SAVELEVEL");
                                                   Set metadataReportsToGet = new HashSet();
                                                   Set partDataReportsToGet = new HashSet();
                                                   for (int i = 0; i < logInfoTable.getRowCount(); i++) {
                                                       for (int i2 = 0; i2 < logInfoTable.getColumnCount(); i2++) {
                                                           if (logInfoTable.getColumnClass(i2).getName().indexOf("Boolean") > -1) {
                                                               if (((Boolean) logInfoTable.getValueAt(i, i2)).booleanValue()) {
                                                                   InfoForMainframePartReportRetrieval retrieveInfo = new InfoForMainframePartReportRetrieval((String) logInfoTable.getValueAt(i, 0), (String)logInfoTable.getValueAt(i, 1)); 
                                                                   String typeOfRetrieve = logInfoTable.getColumnName(i2).toUpperCase();
                                                                   if (binaryColumns.contains(typeOfRetrieve)) {
                                                                       retrieveInfo.setBinary(true);
                                                                   }
                                                                   if (typeOfRetrieve.equals("METADATA")) {
                                                                       retrieveInfo.setReportType(metadataType);
                                                                       metadataReportsToGet.add(retrieveInfo);
                                                                   } else {
                                                                       retrieveInfo.setReportType(logInfoTable.getColumnName(i2));
                                                                       partDataReportsToGet.add(retrieveInfo);
                                                                   }

                                                               }
                                                           }
                                                       }
                                                   }

                                                   if (!metadataReportsToGet.isEmpty() & sendToAddress!=null) {
                                                       new MBMsgBox("Warning", "The Send To option does NOT apply to metadata and dependency reports , \n"+
                                                                    "Your reports will be stored in  " + MBGlobals.Build390_path+"logfiles"+File.separator , thisFrame);
                                                   }

                                                   Set returnedFiles = new HashSet();
                                                   Set storedFiles = new HashSet();
                                                   com.ibm.sdwb.build390.process.PartDataRetrieval partDataRetriever = new com.ibm.sdwb.build390.process.PartDataRetrieval(build,  partDataReportsToGet, thisFrame);
                                                   if (localPath!=null) {
                                                       partDataRetriever.setLocalSavePath(new File(localPath));
                                                   }
                                                   partDataRetriever.setHFSSavePath(hfsPath);
                                                   partDataRetriever.setPDSSavePath(PDSPath);
                                                   partDataRetriever.setSendToAddress(sendToAddress);
                                                   partDataRetriever.externalRun();
                                                   returnedFiles.addAll(partDataRetriever.getLocalOutputFiles());
                                                   if (localPath == null) {
                                                       localPath = MBGlobals.Build390_path+"logfiles"+File.separator;
                                                   }

                                                   if (!metadataReportsToGet.isEmpty()) { /** TST1270 */
                                                       if (PDSPath!=null) {
                                                           MBMsgBox tempBox = new MBMsgBox("WARNING" ,"Fully qualified OS/390 data set names must be used." + MBConstants.NEWLINE + 
                                                                                           "In case of dependency and metadata , a qualifier DEPXXXX for dependency (or) METXXXX for metadata\n"+
                                                                                           "is appended to "+ PDSPath +  " for sequential datasets;\n"+
                                                                                           "member DEPXXXX and METXXXX is created in "  + PDSPath + " for partitioned datasets.\n" +
                                                                                           "eg:\n1.DSN:BINGO.TEST.REPORT is the user entry.\n"+
                                                                                           "The output will be stored in BINGO.TEST.REPORT.DEPXXXX for sequential\n"+
                                                                                           "and BINGO.TEST.REPORT(DEPXXXX) for partitioned,"+ 
                                                                                           "where XXXX are random nos.\n\nContinue?", thisFrame, true);
                                                           submitRequest = tempBox.isAnswerYes();
                                                       }
                                                   }


                                                   if (!metadataReportsToGet.isEmpty() && submitRequest) { /** TST1270 */
                                                       com.ibm.sdwb.build390.process.MetadataReport metadataReportRetriever = new com.ibm.sdwb.build390.process.MetadataReport(build, new File(localPath), metadataReportsToGet, thisFrame);
                                                       metadataReportRetriever.setHFSSavePath(hfsPath);
                                                       metadataReportRetriever.setPDSSavePath(PDSPath);
                                                       metadataReportRetriever.externalRun();
                                                       returnedFiles.addAll(metadataReportRetriever.getLocalOutputFiles());
                                                       storedFiles.addAll(metadataReportRetriever.getHostSavedLocation());
                                                   }

                                                   if (!storedFiles.isEmpty()) {
                                                       StringBuffer dispBuffer = new StringBuffer();
                                                       for (Iterator iter=storedFiles.iterator();iter.hasNext();) {
                                                           dispBuffer.append((String)iter.next() + "\n");
                                                       }
                                                       if (dispBuffer.length() > 0) {
                                                           problemBox("Information", "The reports have been saved in the host as follows:\n\n" + 
                                                                      dispBuffer.toString());
                                                       }
                                                   }

                                                   if (bViewParts.isSelected()) {
                                                       for (Iterator fileIterator = returnedFiles.iterator(); fileIterator.hasNext();) {
                                                           String currentFile = (String) fileIterator.next();
                                                           if (currentFile.endsWith(MBConstants.CLEARFILEEXTENTION) | currentFile.endsWith("SRC")) {
                                                               MBEdit edit = new MBEdit(currentFile,lep);
                                                           }
                                                       }
                                                   }
                                               }
                                           } catch (MBBuildException mbe) {
                                               lep.LogException(mbe);
                                           }
                                       }
                                   });
        bSelectAll.addActionListener(new MBCancelableActionListener(thisFrame) {
                                         public void doAction(ActionEvent evt) {
                                             logTableModel.setAllTrue();
                                         }
                                     });
        bDeselectAll.addActionListener(new MBCancelableActionListener(thisFrame) {
                                           public void doAction(ActionEvent evt) {
                                               logTableModel.setAllFalse();
                                           }
                                       });

        btnSavePath.addActionListener(new MBCancelableActionListener(thisFrame) {
                                          public void doAction(ActionEvent evt) {
                                              MBFileChooser savePathDialog = new MBFileChooser();
                                              savePathDialog.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
                                              savePathDialog.setFileFilter(new javax.swing.filechooser.FileFilter() {
                                                                               public boolean accept(File f) {
                                                                                   return true;
                                                                               }
                                                                               public String getDescription() {
                                                                                   return "No Files";
                                                                               }
                                                                           });
                                              savePathDialog.setDialogTitle("Browse for save path");
                                              File currSavePath = new File(tfSavePath.getText());
                                              if (currSavePath.exists()) {
                                                  savePathDialog.setCurrentDirectory(currSavePath);
                                              }
                                              savePathDialog.showDialog(thisFrame, "Select");
                                              if (savePathDialog.getSelectedFile() != null) {
                                                  String ed = new String(savePathDialog.getSelectedFile().getAbsolutePath());
                                                  if (!ed.endsWith("null")) {
                                                      // no ending slash
                                                      if (ed.endsWith(java.io.File.separator)) {
                                                          ed = ed.substring(0, ed.length()-1);
                                                      }
                                                      tfSavePath.setText(ed);
                                                  }
                                              }
                                          }
                                      } );

        bHelp.addActionListener(new MBCancelableActionListener(thisFrame) {
                                    public void doAction(ActionEvent evt) {
                                        //MBUtilities.ShowHelp("Retrieving_Data_from");
                                        MBUtilities.ShowHelp("HDRRDFAD",HelpTopicID.SMODRETRIEVEPAGE_HELP);
                                    }
                                } );


        cbSendTo.addItemListener(new ItemListener() {
                                     public void itemStateChanged(ItemEvent evt) {
                                         if (cbSendTo.isSelected()) {
                                             cbSavePath.setSelected(false);
                                             bViewParts.setSelected(false);
                                             bViewParts.setEnabled(false);
                                             bGetInfo.setEnabled(true);
                                         } else {
                                             bViewParts.setEnabled(true);
                                             bGetInfo.setEnabled(false);
                                         }
                                     }

                                 });
        cbSavePath.addItemListener(new ItemListener() {
                                       public void itemStateChanged(ItemEvent evt) {

                                           bViewParts.setEnabled(true);
                                           if (cbSavePath.isSelected()) {


                                               cbSendTo.setSelected(false);
                                               bGetInfo.setEnabled(true);
                                           } else {
                                               bGetInfo.setEnabled(false);
                                           }
                                       }
                                   });
    }

    private void loadDataForUsermod(String usermodName) {
        Hashtable allGroups = dr.getGroups();
        Enumeration groupIDs = allGroups.keys();
        boolean datesSet = false;
        if (buildDate.getItemCount() > 0) {
            buildDate.removeAllItems();
        }
        while (groupIDs.hasMoreElements()) {
            Hashtable oneGroup = (Hashtable) allGroups.get(groupIDs.nextElement());
            Vector dateVect = (Vector) oneGroup.get(usermodName);
            if (dateVect != null) {
                for (int i = 0; i < dateVect.size(); i++) {
                    Hashtable temp = (Hashtable) dateVect.elementAt(i);
                    buildDate.addItem((String) temp.get("DATE"));
                    datesSet = true;
                }
            }
        }
        repaint();
    }

    private void loadDataForUsermodDate(String usermodName, String usermodDate) throws com.ibm.sdwb.build390.MBBuildException {
        Hashtable allGroups = dr.getGroups();
        Hashtable valueSource = null;
        Enumeration groupIDs = allGroups.keys();
        boolean dateFound = false;
        while (groupIDs.hasMoreElements()) {
            Hashtable oneGroup = (Hashtable) allGroups.get(groupIDs.nextElement());
            Vector dateVect = (Vector) oneGroup.get(usermodName);
            if (dateVect != null) {
                for (int i = 0; i < dateVect.size(); i++) {
                    Hashtable temp = (Hashtable) dateVect.elementAt(i);
                    if (usermodDate.equals((String) temp.get("DATE"))) {
                        valueSource = temp;
                    }
                    dateFound = true;
                }
            }
        }
        if (!dateFound) {
            throw new GeneralError("The date you selected was not found");
        }

        Vector tempVector = (Vector) valueSource.get("PARTS");
        Vector partsVector = new Vector();
        for (int i = 0; i < tempVector.size(); i++) {
            Vector currentPart = new Vector();
            partsVector.addElement(currentPart);
            Vector tempPart = (Vector) tempVector.elementAt(i);
            for (int i2 = 0; i2 < tempPart.size(); i2++) {
                if (tempPart.elementAt(i2).getClass().getName().indexOf("Boolean") > -1) {
                    Boolean tempBool = (Boolean) tempPart.elementAt(i2);
                    if (tempBool.booleanValue()) {
                        currentPart.addElement(new Boolean(true));
                    } else {
                        currentPart.addElement(new Boolean(false));
                    }
                } else {
                    currentPart.addElement(tempPart.elementAt(i2));
                }
            }
        }
        logTableModel = new MBSmodTableModel();
        logInfoTable.setModel(logTableModel);
        logTableModel.setDataVector(partsVector, columnHeadings);
        logTableModel.setEditableArray();

        for (int i = 0; i < logInfoTable.getColumnCount(); i++) {
            TableColumn tempCol = logInfoTable.getColumn(logInfoTable.getColumnName(i));
            String tempKey = "LOGRETRIEVECOL"+Integer.toString(i);
            Dimension tempWidth = getSizeInfo(tempKey);
            if (tempWidth != null) {
                tempCol.setWidth(tempWidth.width);
            }
        }
    }


    public void dispose() {
        putGeneric(SENDTOTEXTCONSTANT, tfSendTo.getText());
        putGeneric(SAVEPATHTEXTCONSTANT, tfSavePath.getText());
        putGeneric(SENDTOBOOLEANCONSTANT, new Boolean(cbSendTo.isSelected()));
        putGeneric(SAVEPATHBOOLEANCONSTANT, new Boolean(cbSavePath.isSelected()));
        if (logInfoTable != null) {
            for (int i = 0; i < logInfoTable.getColumnCount(); i++) {
                TableColumn tempCol = logInfoTable.getColumn(logInfoTable.getColumnName(i));
                String tempKey = "LOGRETRIEVECOL"+Integer.toString(i);
                Dimension tempWidth = new Dimension(tempCol.getWidth(), 0);
                putSizeInfo(tempKey, tempWidth);
            }
        }
        super.dispose();
    }
}

