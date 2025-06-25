package com.ibm.sdwb.build390;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.io.File;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.SortedMap;
import java.util.StringTokenizer;
import java.util.Vector;

import javax.swing.*;
import javax.swing.table.DefaultTableColumnModel;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableColumn;
import javax.swing.table.TableColumnModel;

import com.ibm.sdwb.build390.help.HelpTopicID;
import com.ibm.sdwb.build390.info.InfoForMainframePartReportRetrieval;
import com.ibm.sdwb.build390.process.ProcessWrapperForSingleStep;
import com.ibm.sdwb.build390.process.steps.DriverReport;
import com.ibm.sdwb.build390.userinterface.graphic.widgets.CancelableAction;
/*********************************************************************/
/* MBLogRetrievePage class for the Build/390 client                  */
/*  Creates and manages the Driver Build Page                        */
// 04/27/99 errorHandling      change LogException parms & add new error types
// 05/01/99 feature249         allow retrieval of dependency reports
// 06/17/99 #360               add a browse button for the save path
// 07/12/99 #metatype=all      add metadata report type=all
// 01/05/2000 pjs defect-InactiveParts don't include INACTIVE parts in retrieve dialog
// 01/07/2000 pjs Warn user if depend or md is checked anywhere and sendto is selected
// 01/07/2000  ind.build.log   changes for logging into a individual build log file for each build.
// 03/07/2000 reworklog        changes to implement the log stuff using listeners
// 03/29/2000 kumar. Sending the OBJ, LST etc to the PDS.
// 4/6/00		Ken				a few addendums to the sending stuff.
// 04/19/2000 PRT_RT_INTO_PDS  cheking the check box if there is any value in the text field.
//04/20/2000 PRT_RT_INTO_PDS made the auto check disabled.
//04/26/2000 PRT_RT_INTO_PDS this is the code which enables the user to enter either PDS path or local or HFS path.
//04/26/2000  This is the code to select all the parts from any one either metadata, dependency or SRC.
//04/26/2000  This is the code to Deselect all the parts from any one either metadata, dependency or SRC.
//05/03/2000  Feature=EKM001   part retrieve Enhancements
//05/15/2000  Feature=EKM001   fix nosuchelementexception when no tokens are there to there
//05/16/2000  Feature=EKM001   fix *Alpa seach , wasnt working properly.
//05/17/2000  Feature=EKM001   fix show first hit.(when done inside the list of parts for the second time)
//05/24/2000  Feature=EKM001   make the search stuff in a separate class.
//05/24/2000  Feature=EKM001   made the reloadall parts display into a JMenuItem
//06/06/2000  remove system.out remove system.out.println that printed argumentHash
//06/21/2000 PRT_RT_INTO_PDS  for logretrieve, changed the checking condition, now checking with
//                            the first four characters, like "pds:" or "hfs:" if local it is the same.
//07/05/2000 bGetInfo       fix for the bGetInfo button, to make it enabled if the user enters the panel second
//02/20/2000                for sendto in case of metadata/depency report - point to the directory the report is stored
//12/03/2002 SDWB-2019 Enhance the help system
//02/17/2004 TST1270 Dependency retrieval msg.
/*********************************************************************/

/** Create the Release/Driver mgt page */
public class MBLogRetrievePage extends MBInternalFrame {

    private MBBuild     build;
    private MBLogTableModel logTableModel= new MBLogTableModel();
    private JTable logInfoTable;
    private JTable headerColumnTable;
    //private JScrollBar rowviewscroll = new JScrollBar(JScrollBar.HORIZONTAL,0,5,0,200);
    private JButton bGetInfo = new JButton("Get Part Data");
    private JButton bHelp = new JButton("Help");
    private JCheckBoxMenuItem bViewParts = new JCheckBoxMenuItem("View Parts", false);
    //private JCheckBoxMenuItem bLongMetaType = new JCheckBoxMenuItem("Long Metadata");
    // pjs - #metatype=all
    private JRadioButtonMenuItem bShortMetaType = new JRadioButtonMenuItem("Short Metadata");
    private JRadioButtonMenuItem bLongMetaType = new JRadioButtonMenuItem("Long Metadata");
    private JRadioButtonMenuItem bAllMetaType = new JRadioButtonMenuItem("All Metadata");
    private JRadioButtonMenuItem rbPartsThatUse = new JRadioButtonMenuItem("Parts that use selected");
    private JRadioButtonMenuItem rbPartsThatAreUsed = new JRadioButtonMenuItem("Parts used by selected");
    private JRadioButtonMenuItem rbBoth = new JRadioButtonMenuItem("Both");
    private JRadioButtonMenuItem rbRebuild = new JRadioButtonMenuItem("Rebuild list");
    private JMenu bSelect = new JMenu("Select");
    private JMenuItem   cbReloadDisplay  = new JMenuItem("Reload All Parts - Display");
    private JMenuItem bSelectAll = new JMenuItem("Select All");
    private JMenuItem bSelectMAll = new JMenuItem("Select All(METADATA)");
    private JMenuItem bSelectDAll = new JMenuItem("Select All(DEPENDENCY)");
    private JMenuItem bSelectSRCAll = new JMenuItem("Select All(SRC)");
    private JMenu bdSelect = new JMenu("DeSelect");
    private JMenuItem bDeselectAll = new JMenuItem("Deselect All");
    private JMenuItem bDeselectMAll = new JMenuItem("Deselect All(METADATA)");
    private JMenuItem bDeselectDAll = new JMenuItem("Deselect All(DEPENDENCY)");
    private JMenuItem bDeselectSRCAll = new JMenuItem("Deselect All(SRC)");
    private JCheckBox cbSendTo = new JCheckBox("Send To (Node.UserID):");
    private JMenuItem bFind      = new JMenuItem("Find");
    private JTextField tfSendTo = new JTextField();
    private TableColumnModel cm;
    private TableColumnModel rowHeaderModel;
    //kumar 03/29/2000

    private JLabel labelSavePath = new JLabel("Prefix with DSN: for OS/390 data set save, HFS: for hfs save");
    private JCheckBox cbSavePath = new JCheckBox("Save To:(LocalPath/HFS path/DSN)");
    private JTextField tfSavePath = new JTextField();
    private JButton btnSavePath = new JButton("Browse");
    private Vector columnHeadings = new Vector();
    private static final String SENDTOTEXTCONSTANT = "SENDTOTEXTSETTING";
    private static final String SAVEPATHTEXTCONSTANT = "SAVEPATHTEXTSETTING";

    private static final String SENDTOBOOLEANCONSTANT = "SENDTOBOOLEANSETTING";
    private static final String SAVEPATHBOOLEANCONSTANT = "SAVEPATHBOOLEANSETTING";

    private static final String BINARYCOLLECTORCONSTANT = " is binary collector";
    private String hfsField = null;
    private String dsnField = null;
    private String localField = null;
    private Vector SkimmedPartsVector = new Vector();
    private Vector AllPartsVector = new Vector();
    private static final int LOGRETRIEVEPAGEDRIVERREPORT=0;
    //private Vector RebuildDisplayPartVector = new Vector();
    //private Hashtable FindNextHashtable = new Hashtable();


    /**
    * constructor - Create a MBDriverBuildPage
    * @param MBGUI gui
    */
    public MBLogRetrievePage(MBBuild tempBuild) throws com.ibm.sdwb.build390.MBBuildException{
        // 02/03/2000 pjs add release.driver to title of dialog
        super("Retrieve Data ("+tempBuild.getReleaseInformation().getLibraryName()+"."+tempBuild.getDriverInformation().getName()+")", true, null);

        lep.addEventListener(tempBuild.getLogListener());
        JPanel centerPanel = new JPanel(new BorderLayout());
        getContentPane().add("Center", centerPanel);


        JPanel subCenter = new JPanel(new BorderLayout());
        centerPanel.add("South", subCenter);
        JMenu editMenu = new JMenu("Edit");
        getJMenuBar().add(editMenu);
        editMenu.add(bSelect);
        bSelect.add(bSelectAll);
        bSelect.add(bSelectMAll);
        bSelect.add(bSelectDAll);
        bSelect.add(bSelectSRCAll);
        editMenu.add(bdSelect);
        bdSelect.add(bDeselectAll);
        bdSelect.add(bDeselectMAll);
        bdSelect.add(bDeselectDAll);
        bdSelect.add(bDeselectSRCAll);
        JMenu searchMenu = new JMenu("Search");
        bFind.setEnabled(false);
        searchMenu.add(bFind);
        cbReloadDisplay.setEnabled(false);
        searchMenu.add(cbReloadDisplay);
        getJMenuBar().add(searchMenu);
        final JMenu optionsMenu = new JMenu("Options");
        getJMenuBar().add(optionsMenu);
        optionsMenu.add(bViewParts);
        //optionsMenu.add(bLongMetaType);
        // pjs - #metatype=all
        JMenu metatypeMenu = new JMenu("Metadata Report Type");
        optionsMenu.add(metatypeMenu);
        metatypeMenu.add(bShortMetaType);
        metatypeMenu.add(bLongMetaType);
        metatypeMenu.add(bAllMetaType);
        ButtonGroup metatypeGroup = new ButtonGroup();
        metatypeGroup.add(bShortMetaType);
        metatypeGroup.add(bLongMetaType);
        metatypeGroup.add(bAllMetaType);
        bShortMetaType.setSelected(true);
        JMenu dependencyMenu = new JMenu("Dependency");
        optionsMenu.add(dependencyMenu);
        dependencyMenu.add(rbPartsThatUse);
        dependencyMenu.add(rbPartsThatAreUsed);
        dependencyMenu.add(rbBoth);
        dependencyMenu.add(rbRebuild);
        ButtonGroup dependencyGroup = new ButtonGroup();
        dependencyGroup.add(rbPartsThatUse);
        dependencyGroup.add(rbPartsThatAreUsed);
        dependencyGroup.add(rbBoth);
        dependencyGroup.add(rbRebuild);
        rbBoth.setSelected(true);

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
            cbSavePath.setSelected(false);
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

        //row header model for the first two columns only
        rowHeaderModel = new DefaultTableColumnModel() {
            int i=0;
            public void addColumn(TableColumn tc) {
                if (i<=1) {
                    tc.setMinWidth(85);
                    super.addColumn(tc);
                    i++;
                }
            }
        };

        //column model to follow for the rest of the columns other than partname and partclass
        cm  = new DefaultTableColumnModel() {
            //boolean first=true;
            int i=0;
            public void addColumn(TableColumn tc) {
                if (i<=1) {
                    i++;
                    return;
                }
                tc.setMinWidth(100);
                super.addColumn(tc);

            }
        };

        logInfoTable = new JTable(logTableModel,cm);
        headerColumnTable = new JTable(logTableModel,rowHeaderModel);

        // Ken 6/2/99 fix for having to double click selections in tables.   These lines can be copied to any file that needs this fix.
        try {
            logInfoTable.setDefaultEditor(Class.forName("java.lang.Boolean"), new GeneralCellEditor(new JCheckBox()));
        } catch (ClassNotFoundException cnfe) {
            //MBUtilities.LogException("A class is missing.  Checking a checkbox will require 2 clicks.", cnfe);
            //01/07/2000 ind.build.log
            //build.LogException("A class is missing.  Checking a checkbox will require 2 clicks.", cnfe);
            lep.LogException("A class is missing.  Checking a checkbox will require 2 clicks.", cnfe);
        }
        logInfoTable.setColumnSelectionAllowed(false);
        logInfoTable.setRowSelectionAllowed(false);
        logInfoTable.setCellSelectionEnabled(false);
        logInfoTable.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
        logInfoTable.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);


        headerColumnTable.setColumnSelectionAllowed(false);
        headerColumnTable.setRowSelectionAllowed(false);
        headerColumnTable.setCellSelectionEnabled(false);
        headerColumnTable.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
        headerColumnTable.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);

        final JScrollPane jsp = new JScrollPane();
        jsp.setViewportView(logInfoTable);
        jsp.setRowHeaderView(headerColumnTable);
        centerPanel.add("Center", jsp);

        final JViewport rh = jsp.getRowHeader();
        rh.setPreferredSize(new Dimension(180,100));
        final JTableHeader hd = headerColumnTable.getTableHeader();

        /*
        tried this code to make the row header columns also scrollable
        rowviewscroll.addAdjustmentListener(new AdjustmentListener(){
                                                public void adjustmentValueChanged(AdjustmentEvent e){
                                                    //headerColumnTable.setViewPort()
                                                    //rh.setViewPosition(new Point(e.getValue(),100));
                                                    JViewport rha = jsp.getRowHeader();
                                                    rha.setViewPosition(rha.toViewCoordinates(new Point(e.getValue(),0)));
                                                }});*/


        bGetInfo.setEnabled(false);
        setVisible(true);
        build = tempBuild;
        CancelableAction  initPage = new CancelableAction("initialize") {
            com.ibm.sdwb.build390.process.management.Haltable stopObject = null;

            public void doAction(ActionEvent evt) {
                try {
// 01/05/2000 pjs defect-InactiveParts don't include INACTIVE parts in retrieve dialog
// add true flag to end of MBLogDrvrReport create to indicate 'don't include inactive parts'
                    ProcessWrapperForSingleStep wrapper = new ProcessWrapperForSingleStep(thisFrame); 
                    DriverReport step = new DriverReport(build.getDriverInformation(),build.getMainframeInfo(),build.getLibraryInfo(),
                                                         new java.io.File(build.getBuildPath()),wrapper);
                    step.setForceNewReport(true);
                    step.setSummaryType(null);
                    wrapper.setStep(step);
                    stopObject = wrapper;
                    wrapper.externalRun();
                    //filter the vector for inactive parts.

                    if (step.getParser()!=null) {
                        AllPartsVector = getPartsVector(step.getParser().getPartsVector(), step.getParser().getDataSymbols());
                        columnHeadings.addElement("Part Name");
                        columnHeadings.addElement("Part Class");
//                    columnHeadings.addElement("Local");
                        columnHeadings.addElement("Dependency");
                        columnHeadings.addElement("MetaData");
                        Vector typeVector = step.getParser().getDataTypes();
                        for (int i = 0; i < typeVector.size(); i++) {
                            String heading = (String) typeVector.elementAt(i);
                            columnHeadings.addElement(heading);
                            if (heading.toUpperCase().trim().startsWith("C")) {
                                JCheckBoxMenuItem bCollectorBin = new JCheckBoxMenuItem(heading + BINARYCOLLECTORCONSTANT);
                                optionsMenu.add(bCollectorBin);
                            }
                        }
                        if (AllPartsVector.size()>0) {
                            bFind.setEnabled(true);
                            SkimmedPartsVector.removeAllElements();
                            SkimmedPartsVector=(Vector)AllPartsVector.clone();
                        }


                        logTableModel.updateData(AllPartsVector,columnHeadings);
                        //    logTableModel.setDataVector(AllPartsVector, columnHeadings);
                        logTableModel.setEditableArray();
                        logTableModel.setAllFalse();
                        logInfoTable.createDefaultColumnsFromModel();
                        headerColumnTable.createDefaultColumnsFromModel();


                        for (int i = 0; i < headerColumnTable.getColumnCount(); i++) {
                            TableColumn tempCol = rowHeaderModel.getColumn(i);
                            String tempKey = "LOGRETRIEVEHEADERCOL"+Integer.toString(i);
                            Dimension tempWidth = getSizeInfo(tempKey);
                            if (tempWidth != null) {
                                tempCol.setWidth(tempWidth.width);
                            }
                        }

                        for (int i = 0; i < logInfoTable.getColumnCount(); i++) {
                            TableColumn tempCol = cm.getColumn(i);
                            String tempKey = "LOGRETRIEVEDATACOL"+Integer.toString(i);
                            Dimension tempWidth = getSizeInfo(tempKey);
                            if (tempWidth != null) {
                                tempCol.setWidth(tempWidth.width);
                            }
                        }

                        for (int i = 2; i < columnHeadings.size(); i++) {
                            TableColumn tempCol = logInfoTable.getColumn(columnHeadings.elementAt(i));
                            tempCol.setMinWidth((int) (((double)(new JLabel((String) tempCol.getHeaderValue())).getPreferredSize().width) *1.5));
                        }

                        for (int i = 0; i < logInfoTable.getColumnCount(); i++) {
                            TableColumn tempCol =  cm.getColumn(i);
                            //TableColumn tempCol = cm.getColumn(i-2);
                            tempCol.setCellRenderer(new MBTableCellRenderer());
                        }


                        for (int i = 0; i < headerColumnTable.getColumnCount(); i++) {
                            TableColumn tempCol = rowHeaderModel.getColumn(i);
                            tempCol.setMinWidth((int) (((double)(new JLabel((String) tempCol.getHeaderValue())).getPreferredSize().width) *1.5));
                        }

                        jsp.setCorner(JScrollPane.UPPER_LEFT_CORNER,hd);
                        if ((cbSendTo.isSelected()) | (cbSavePath.isSelected())) {
                            bGetInfo.setEnabled(true);

                        } else {
                            bGetInfo.setEnabled(false);
                        }

                    }
                    revalidate();
                    repaint();
                } catch (MBBuildException mbe) {
                    lep.LogException(mbe);
                }
            }

            private Vector getPartsVector(Vector parts,Vector symbols) {
                Vector partsVector = new Vector();
                for (Iterator iter=parts.iterator();iter.hasNext();) {
                    Vector currentVector = (Vector) iter.next();
                    Vector newVector = new Vector();
                    String flagTypeString = (String) currentVector.elementAt(5);
                    if (flagTypeString.indexOf("I") < 0) { /*ignore iactive parts */
                        newVector.addElement(currentVector.elementAt(2));
                        newVector.addElement(currentVector.elementAt(3));
                        newVector.addElement(new Boolean(true));
                        newVector.addElement(new Boolean(true));
                        String dataTypeString = (String) currentVector.elementAt(6);
                        for (Iterator symbolIter=symbols.iterator();symbolIter.hasNext();) {
                            String currentSymbol = (String) symbolIter.next();
                            if (dataTypeString.indexOf(currentSymbol)>-1) {
                                newVector.addElement(new Boolean(true));
                            } else {
                                newVector.addElement(new Boolean(false));
                            }
                        }
                        partsVector.addElement(newVector);
                    }
                }
                return partsVector;
            }

            public void stop() throws com.ibm.sdwb.build390.MBBuildException{
                if (stopObject !=null) {
                    stopObject.haltProcess();
                }
            }
        };
        initPage.actionPerformed(new ActionEvent(this, 0, ""));
        bGetInfo.addActionListener(new MBCancelableActionListener(thisFrame) {
                                       public void doAction(ActionEvent evt) {
                                           try {
                                               boolean submitRequest = true;
                                               Vector fileNames = new Vector();
                                               String errorMsg = new String();
                                               String hfsPath = null;
                                               String PDSPath = null;
                                               String localPath = null;
                                               String sendToAddress = null;

                                               if (cbSendTo.isSelected()) {
                                                   bViewParts.setSelected(false);
                                                   if (tfSendTo.getText().trim().length() > 0) {
                                                       sendToAddress =tfSendTo.getText().trim();
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
                                                           hfsPath =tpath1.trim();

                                                           MBMsgBox tempBox =  new MBMsgBox("WARNING" ,"HFS directory " + hfsPath + " selected must exist on HOST."+MBConstants.NEWLINE+"Continue?", thisFrame, true);
                                                           submitRequest = tempBox.isAnswerYes();
                                                       } else if (tpath.substring(0,4).toUpperCase().equals("DSN:")) {
                                                           bViewParts.setSelected(false);
                                                           String tpath1  = tpath.substring(tpath.lastIndexOf(":") + 1);
                                                           PDSPath =tpath1.trim().toUpperCase();
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
                                                   String metadataType = "ALL";
                                                   if (bLongMetaType.isSelected()) {
                                                       metadataType = "LONG";
                                                   } else if (bShortMetaType.isSelected()) {
                                                       metadataType = "SHORT";
                                                   }

                                                   String dependencyType = "LONG";
                                                   if (rbPartsThatUse.isSelected()) {
                                                       dependencyType = "USER";
                                                   } else if (rbPartsThatAreUsed.isSelected()) {
                                                       dependencyType = "USES";
                                                   } else if (rbBoth.isSelected()) {
                                                       dependencyType  = "BOTH";
                                                   } else if (rbRebuild.isSelected()) {
                                                       dependencyType = "RBLD";
                                                   }

                                                   Set metadataReportsToGet = new HashSet();
                                                   Set dependencyReportsToGet = new HashSet();
                                                   Set partDataReportsToGet = new HashSet();
                                                   for (   int    i    =    0;    i    <    logInfoTable.getRowCount(   );    i++) {
                                                       for (int i2 = 0; i2 < logInfoTable.getColumnCount(); i2++) {
                                                           if (logInfoTable.getColumnClass(i2).getName().indexOf("Boolean") > -1) {
                                                               if (((Boolean) logInfoTable.getValueAt(i, i2)).booleanValue()) {
                                                                   InfoForMainframePartReportRetrieval retrieveInfo = new InfoForMainframePartReportRetrieval((String) headerColumnTable.getValueAt(i, 0),(String) headerColumnTable.getValueAt(i, 1)); 
                                                                   String typeOfRetrieve = logInfoTable.getColumnName(i2).toUpperCase().trim();
                                                                   if (binaryColumns.contains(typeOfRetrieve)) {
                                                                       retrieveInfo.setBinary(true);
                                                                   }
                                                                   if (typeOfRetrieve.equals("METADATA")) {
                                                                       retrieveInfo.setReportType(metadataType);
                                                                       metadataReportsToGet.add(retrieveInfo);
                                                                   } else if (typeOfRetrieve.equals("DEPENDENCY")) {
                                                                       retrieveInfo.setReportType(dependencyType);
                                                                       dependencyReportsToGet.add(retrieveInfo);
                                                                   } else {
                                                                       retrieveInfo.setReportType(logInfoTable.getColumnName(i2));
                                                                       partDataReportsToGet.add(retrieveInfo);
                                                                   }

                                                               }
                                                           }
                                                       }
                                                   }



                                                   if ((!(dependencyReportsToGet.isEmpty() & metadataReportsToGet.isEmpty())) & sendToAddress!=null) {
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

                                                   if ((!metadataReportsToGet.isEmpty() || !dependencyReportsToGet.isEmpty())) { /** TST1270 */
                                                       if (PDSPath!=null) {
                                                           MBMsgBox tempBox = new MBMsgBox("WARNING" ,"Fully qualified OS/390 data set names must be used." + MBConstants.NEWLINE + 
                                                                                           "In case of dependency and metadata , a qualifier DEPXXXX for dependency (or) METXXXX for metadata\n"+
                                                                                           "is appended to "+PDSPath +  " for sequential datasets;\n"+
                                                                                           "member DEPXXXX and METXXXX is created in "  + PDSPath + " for partitioned datasets.\n" +
                                                                                           "eg:\n1.DSN:BINGO.TEST.REPORT is the user entry.\n"+
                                                                                           "The output will be stored in BINGO.TEST.REPORT.DEPXXXX for sequential\n"+
                                                                                           "and BINGO.TEST.REPORT(DEPXXXX) for partitioned,"+ 
                                                                                           "where XXXX are random numbers.\n\nContinue?", thisFrame, true);
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

                                                   if (!dependencyReportsToGet.isEmpty() && submitRequest) {     /** TST1270 */
                                                       com.ibm.sdwb.build390.process.DependencyReport dependencyReportRetriever = new com.ibm.sdwb.build390.process.DependencyReport(build, new File(localPath), dependencyReportsToGet, thisFrame);
                                                       dependencyReportRetriever.setHFSSavePath(hfsPath);
                                                       dependencyReportRetriever.setPDSSavePath(PDSPath);
                                                       dependencyReportRetriever.externalRun();
                                                       returnedFiles.addAll(dependencyReportRetriever.getLocalOutputFiles());
                                                       storedFiles.addAll(dependencyReportRetriever.getHostSavedLocation());
                                                   }

                                                   if (!storedFiles.isEmpty()) {
                                                       StringBuffer dispBuffer = new StringBuffer();
                                                       for (Iterator iter=storedFiles.iterator();iter.hasNext();) {
                                                           dispBuffer.append((String)iter.next() + "\n");
                                                       }
                                                       if (dispBuffer.length() > 0 && dispBuffer.toString().trim().length() > 0) {
                                                           problemBox("Information", "The reports have been saved in the host as follows:\n\n" + 
                                                                      dispBuffer.toString());
                                                       }
                                                   }

                                                   if (bViewParts.isSelected()) {
                                                       for (Iterator fileIterator = returnedFiles.iterator(); fileIterator.hasNext();) {
                                                           String currentFile = (String) fileIterator.next();
                                                           MBEdit edit = new MBEdit(currentFile,lep);
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

        //04/26/2000  This is the code to select all the parts from any one either metadat, dependency or SRC
        bSelectMAll.addActionListener(new MBCancelableActionListener(thisFrame) {
                                          public void doAction(ActionEvent evt) {
                                              logTableModel.setanyColumntrue("MetaData");
                                          }
                                      });
        bSelectDAll.addActionListener(new MBCancelableActionListener(thisFrame) {
                                          public void doAction(ActionEvent evt) {
                                              logTableModel.setanyColumntrue("Dependency");
                                          }
                                      });
        bSelectSRCAll.addActionListener(new MBCancelableActionListener(thisFrame) {
                                            public void doAction(ActionEvent evt) {
                                                logTableModel.setanyColumntrue("SRC");
                                            }
                                        });


        //04/26/2000  This is the code to Dselect all the parts from any one either metadat, dependency or SRC

        bDeselectAll.addActionListener(new MBCancelableActionListener(thisFrame) {
                                           public void doAction(ActionEvent evt) {
                                               logTableModel.setAllFalse();
                                           }
                                       });

        bDeselectMAll.addActionListener(new MBCancelableActionListener(thisFrame) {
                                            public void doAction(ActionEvent evt) {
                                                logTableModel.setanyColumnFalse("MetaData");
                                            }
                                        });
        bDeselectDAll.addActionListener(new MBCancelableActionListener(thisFrame) {
                                            public void doAction(ActionEvent evt) {
                                                logTableModel.setanyColumnFalse("Dependency");
                                            }
                                        });
        bDeselectSRCAll.addActionListener(new MBCancelableActionListener(thisFrame) {
                                              public void doAction(ActionEvent evt) {
                                                  logTableModel.setanyColumnFalse("SRC");
                                              }
                                          });

        bFind.addActionListener(new MBCancelableActionListener(thisFrame) {
                                    public void doAction(ActionEvent evt) {
                                        try {
                                            MBSearchForPart mp=new MBSearchForPart(SkimmedPartsVector.elements(),thisFrame,getStatus(),lep,false);
                                            boolean isFirstHit=false,isRebuild = false;
                                            Vector RebuildDisplayPartVector = mp.getMatchedPartsVector();
                                            SortedMap FindNextHashMap = mp.getFindNextHashMap();
                                            String SearchTitle = mp.getSearchTitle();
                                            String PartNameToFind = mp.getNameToFind();
                                            String PartClassToFind =  mp.getPartClassToFind();

                                            if (mp.getDisplayOption()=='F') {
                                                isFirstHit=true;
                                            } else {
                                                isRebuild=true;
                                            }



                                            String statstr = new String();
                                            if (RebuildDisplayPartVector.size()>0 |FindNextHashMap.size()>0) {
                                                if (isRebuild) {
                                                    try {
                                                        cbReloadDisplay.setEnabled(true);
                                                        SkimmedPartsVector.removeAllElements();
                                                        SkimmedPartsVector=(Vector)RebuildDisplayPartVector.clone();
                                                        logTableModel.updateData(RebuildDisplayPartVector,columnHeadings);
                                                        logTableModel.setEditableArray();
                                                        logTableModel.setAllFalse();
                                                        revalidate();
                                                        repaint();
                                                    } catch (Exception e) {
                                                        lep.LogException("Error occurred",e);
                                                    }
                                                }

                                                if (isFirstHit) {
                                                    logTableModel.updateData(SkimmedPartsVector,columnHeadings);
                                                    logTableModel.setEditableArray();
                                                    logTableModel.setAllFalse();
                                                    Iterator keys = FindNextHashMap.keySet().iterator();
                                                    int j = ((Integer)keys.next()).intValue();
                                                    headerColumnTable.setRowSelectionAllowed(true);
                                                    headerColumnTable.setRowSelectionInterval(j,j);
                                                    JViewport rha = jsp.getRowHeader();
                                                    JViewport rhb = jsp.getViewport();
                                                    rha.setViewPosition(rha.toViewCoordinates((headerColumnTable.getCellRect(j,2,true)).getLocation()));
                                                    rhb.setViewPosition(rhb.toViewCoordinates((logInfoTable.getCellRect(j,0,true)).getLocation()));
                                                    revalidate();
                                                    repaint();
                                                    if (keys.hasNext()) {
                                                        new MBLogRetrieveFindNextMatch(thisFrame,lep,headerColumnTable,logInfoTable,keys,SearchTitle,rha,rhb,getStatus());
                                                    }

                                                }
                                                getStatus().updateStatus("Search Complete...  Successful ",false);
                                            } else {
                                                getStatus().updateStatus("Search Complete...  No Matches Found !!!!!",false);
                                                if ((PartNameToFind!=null)) {
                                                    if (PartClassToFind!=null) {
                                                        new MBMsgBox("Information:","No Matches Found For Part Name : "+ PartNameToFind + " Part Class : " + PartClassToFind);
                                                    } else {
                                                        new MBMsgBox("Information:","No Matches Found For Part Name : "+ PartNameToFind);
                                                    }
                                                } else {
                                                    if (PartClassToFind!=null) {
                                                        new MBMsgBox("Information:","No Matches Found For Part Class : "+ PartClassToFind);
                                                    }
                                                }
                                            }

                                            revalidate();
                                            repaint();

                                        } catch (MBBuildException e) {
                                            lep.LogException(e);
                                        }
                                    }
                                });

        cbReloadDisplay.addActionListener(new MBCancelableActionListener(thisFrame) {
                                              public void doAction(ActionEvent evt) {
                                                  try {
                                                      SkimmedPartsVector.removeAllElements();
                                                      SkimmedPartsVector = (Vector)AllPartsVector.clone();
                                                      logTableModel.updateData(AllPartsVector,columnHeadings);
                                                      logTableModel.setEditableArray();
                                                      logTableModel.setAllFalse();
                                                      cbReloadDisplay.setEnabled(false);
                                                      revalidate();
                                                      repaint();

                                                  } catch (Exception e) {
                                                      lep.LogException("Error Occurred : ",e);
                                                  }
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
                                        MBUtilities.ShowHelp("HDRRDFAD",HelpTopicID.LOGRETRIEVEPAGE_HELP);
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

// set minimum size
    public Dimension getPreferredSize() {
        Dimension oldPref ;
        oldPref = new Dimension(600,super.getPreferredSize().height - 140) ;
        return oldPref;
    }

    public void dispose() {
        putGeneric(SENDTOTEXTCONSTANT, tfSendTo.getText());
        putGeneric(SAVEPATHTEXTCONSTANT, tfSavePath.getText());
        putGeneric(SENDTOBOOLEANCONSTANT, new Boolean(cbSendTo.isSelected()));
        putGeneric(SAVEPATHBOOLEANCONSTANT, new Boolean(cbSavePath.isSelected()));
        if (logInfoTable != null) {
            for (int i = 0; i < logInfoTable.getColumnCount(); i++) {
                TableColumn tempCol = logInfoTable.getColumn(logInfoTable.getColumnName(i));
                String tempKey = "LOGRETRIEVEDATACOL"+Integer.toString(i);
                Dimension tempWidth = new Dimension(tempCol.getWidth(), 0);
                putSizeInfo(tempKey, tempWidth);
            }
        }
        if (headerColumnTable != null) {
            for (int i = 0; i < headerColumnTable.getColumnCount(); i++) {
                TableColumn tempCol = headerColumnTable.getColumn(headerColumnTable.getColumnName(i));
                String tempKey = "LOGRETRIEVEHEADERCOL"+Integer.toString(i);
                Dimension tempWidth = new Dimension(tempCol.getWidth(), 0);
                putSizeInfo(tempKey, tempWidth);
            }
        }

        super.dispose();
    }
}









