package com.ibm.sdwb.build390.userinterface.graphic.panels;

//#DEF:TST1483 . 09/09/2003 if no local entry exists add one.(usermod cleanup doesnt local dirs)

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.io.FilenameFilter;
import java.text.DateFormat;
import java.util.Date;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Vector;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;

import com.ibm.sdwb.build390.*;
import com.ibm.sdwb.build390.filter.criteria.FilterCriteria;
import com.ibm.sdwb.build390.help.HelpTopicID;
import com.ibm.sdwb.build390.info.ChangeRequestPartitionedInfo;
import com.ibm.sdwb.build390.info.UsermodGeneralInfo;
import com.ibm.sdwb.build390.logprocess.LogEventGUIListener;
import com.ibm.sdwb.build390.logprocess.LogEventProcessor;
import com.ibm.sdwb.build390.process.management.CleanableEntity;
import com.ibm.sdwb.build390.user.Mode;
import com.ibm.sdwb.build390.user.Setup;
import com.ibm.sdwb.build390.user.SetupManager;
import com.ibm.sdwb.build390.userinterface.graphic.panels.build.DriverBuildPanel;
import com.ibm.sdwb.build390.userinterface.graphic.panels.build.InternalFrameBuildPanelHolder;
import com.ibm.sdwb.build390.userinterface.graphic.panels.build.UserBuildPanel;
import com.ibm.sdwb.build390.userinterface.graphic.widgets.CancelableAction;
import com.ibm.sdwb.build390.userinterface.graphic.widgets.HelpAction;
import com.ibm.sdwb.build390.userinterface.graphic.widgets.SortableTableModel;
import com.ibm.sdwb.build390.userinterface.graphic.widgets.TableSelectionCancelableAction;
import com.ibm.sdwb.build390.utilities.SerializedBuildsLister;
import com.ibm.sdwb.build390.utilities.process.FormattedDateAdapter;


//*************************************************************
//09/16/2003 #DEF.TST1569: Need to ftp zips to gsa instead of kentm
//09/19/2003 #DEF:TST1483/TST1579.if no local entry exists add one(hack for now).
//12/15/2003 #DEF:TST1735 fix repackage in PTF
//01/30/2004 #DEF:TST1735 fix repackage in PTF - use runRepackagePTF.
//08/09/2007 #DEF:TST3333 ClassCastException when doing CREATE on success ++USERMOD
//*************************************************************

public class ProcessManagementFrame extends MBInternalFrame {

    private File build390HomeDirectory = null;
    private static final Map classnameToTypeMapping = new java.util.HashMap();
    private static final String buildIdColumnName = "BuildID";
    private Map buildIdToDirectoryMapping = new java.util.HashMap();

    private LoadAction load;
    private CreateAction create;
//    private CopyToRMIAction copy;

    private ZipAction zip; 
//    private RepackPTFAction repack;
    private CleanAction clean;
    private UndoAction undo;

    private ViewRefreshAction viewRefresh;
    private ViewBuildInfoAction viewBuildInfo;
    private ViewBuildLogAction viewBuildLog;
    private ViewDriverStatusAction viewDriverStatus;

    private JButton loadButton;
    private JButton cleanButton;
    private JButton undoButton;

    private DefaultTableModel loadModel = null;
    static {
        classnameToTypeMapping.put("com.ibm.sdwb.build390.info.MultiAparInfo","MultiApar"); 
        classnameToTypeMapping.put("com.ibm.sdwb.build390.MBABuild","Apar"); 
        classnameToTypeMapping.put("com.ibm.sdwb.build390.MBBuild","Driver"); 
        classnameToTypeMapping.put("com.ibm.sdwb.build390.MBUBuild","User"); 
        classnameToTypeMapping.put("com.ibm.sdwb.build390.MBPBuild","PTF"); 
        classnameToTypeMapping.put("com.ibm.sdwb.build390.info.UsermodGeneralInfo","UserMod"); 
    }

    /** @param parent The parent frame
    * @param modal modal flag */
    public ProcessManagementFrame(File tempBuild390HomeDirectory) {
        super("Manage Processes", false, null);
        build390HomeDirectory = tempBuild390HomeDirectory;
        SetupManager.getSetupManager().createSetupInstance();//this is used to load the help instance. just a hack for now.
        setUpInterface();
    }

    private void setUpInterface() {
        loadModel = new DefaultTableModel() {

            public String getColumnName(int column) {
                switch (column) {
                case 0:
                    return("Process");
                case 1:
                    return(buildIdColumnName);
                case 2:
                    return("Release");
                case 3:
                    return("Driver");
                case 4:
                    return("Date");
                case 5:
                    return("Description");
                }
                return "";
            }



            public Class getColumnClass(int columnIndex) {
                switch (columnIndex) {
                case 0:
                    return java.lang.String.class;

                case 1:
                    return java.lang.String.class;

                case 2:
                    return java.lang.String.class;

                case 3:
                    return java.lang.String.class;

                case 4:
                    return FormattedDateAdapter.class;

                case 5:
                    return java.lang.String.class;
                default:
                    return java.lang.String.class;
                }
            }

            public boolean isCellEditable(int column, int row) {
                return false;
            }
        };
        SortableTableModel sorter =  new SortableTableModel(loadModel);
        JTable loadTable = new JTable(sorter);
        sorter.setTableHeader(loadTable.getTableHeader()); 
        loadTable.getTableHeader().setToolTipText("Click to specify sorting; Control-Click to specify secondary sorting");
        loadTable.addMouseListener(new TableMouseListener(loadTable));
        sorter.setColumnComparator(Date.class,SortableTableModel.COMPARABLE_COMAPRATOR);
        JScrollPane tableScroller = new JScrollPane(loadTable);
        Dimension dm = tableScroller.getPreferredSize();
        dm.height = dm.height/2;
        dm.width  = dm.width/2;
        tableScroller.setPreferredSize(dm);
        tableScroller.setAlignmentY(TOP_ALIGNMENT);
        JPanel tablePanel = new JPanel();
        tablePanel.setLayout(new BoxLayout(tablePanel, BoxLayout.PAGE_AXIS));
        tablePanel.add(Box.createRigidArea(new Dimension(0,5)));
        tablePanel.add(tableScroller);
        getContentPane().add("Center",tablePanel);

        loadTable.setFont(new Font("Courier", Font.PLAIN, 14));
        loadTable.setBackground(MBGuiConstants.ColorFieldBackground);
        loadTable.setColumnSelectionAllowed(false);
        loadTable.setCellSelectionEnabled(false);
        loadTable.setRowSelectionAllowed(true);
        loadTable.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);

        //refreshBuildList(loadTable);
        loadTable.setAutoResizeMode(JTable.AUTO_RESIZE_SUBSEQUENT_COLUMNS);

        loadTable.putClientProperty("terminateEditOnFocusLost", Boolean.TRUE);

        load = new LoadAction(loadTable,buildIdColumnName);
        create = new CreateAction(loadTable, buildIdColumnName);
//        copy = new CopyToRMIAction(loadTable, buildIdColumnName);
        zip = new ZipAction(loadTable, buildIdColumnName);
//        repack = new RepackPTFAction(loadTable, buildIdColumnName);
        clean = new CleanAction(loadTable, buildIdColumnName);
        undo = new UndoAction(loadTable, buildIdColumnName);
        HelpAction help = new HelpAction("HDRRESTART", HelpTopicID.MANAGEPROCESSPAGE_HELP);

        viewRefresh = new ViewRefreshAction(loadTable, buildIdColumnName);
        viewBuildInfo = new ViewBuildInfoAction(loadTable, buildIdColumnName);
        viewBuildLog = new ViewBuildLogAction(loadTable, buildIdColumnName);
        viewDriverStatus = new ViewDriverStatusAction(loadTable, buildIdColumnName);


        JMenu fileMenu = getJMenuBar().getMenu(0);
        JMenu viewMenu = new JMenu("View");
        JMenu actionMenu = new JMenu("Action");
        getJMenuBar().add(viewMenu);
        getJMenuBar().add(actionMenu);

        fileMenu.insert(load,0);
        fileMenu.insert(create,1);
//        fileMenu.insert(copy,2);

        viewMenu.add(viewRefresh);
        viewMenu.add(viewBuildInfo);
        viewMenu.add(viewBuildLog);
        viewMenu.add(viewDriverStatus);

        actionMenu.add(clean);
//Disable undo        actionMenu.add(undo);
//        actionMenu.add(repack);
        actionMenu.add(zip);

        loadButton = new JButton(load);
        cleanButton = new JButton(clean);
        undoButton = new JButton(undo);

        Vector actionButtons = new Vector();
        actionButtons.add(loadButton);
        actionButtons.add(cleanButton);
//Disable undo        actionButtons.add(undoButton);

        java.awt.event.MouseListener ml = new java.awt.event.MouseAdapter() {
            public void mouseReleased(java.awt.event.MouseEvent e) {
                if (e.isPopupTrigger()) {
                    JPopupMenu menu = new JPopupMenu("Menu");
                    menu.add(load);
                    menu.add(create);
                    menu.add(viewRefresh);
                    menu.add(zip);
                    menu.add(clean);
                    menu.show(e.getComponent(),e.getX(),e.getY());
                }
            }

        };
        loadTable.addMouseListener(ml);

        addButtonPanel(new JButton(help), actionButtons);
        MBInsetPanel tempPanel = new MBInsetPanel(new BorderLayout(), 5, 5, 5, 5);

        getContentPane().add("North", tempPanel);


        setVisible(true);

        viewRefresh.actionPerformed(new ActionEvent(this, 0, ""));

    }


    MBBuild getBuildForBuildId(String buildId) throws com.ibm.sdwb.build390.MBBuildException, java.io.IOException{
        File buildFileLocation = (File) buildIdToDirectoryMapping.get(buildId);
        return(new MBBuildLoader()).loadBuild(buildFileLocation);
    }

    private class RefreshTable implements com.ibm.sdwb.build390.process.management.Haltable { /*INT1940 */

        private JTable theTable;
        private boolean stopped = false;
        private FilterCriteria criteria;
        RefreshTable(JTable theTable) {
            this.theTable = theTable;
        }

        private void refresh() {
            stopped = false;
            Vector columnHeadings = new Vector();
            columnHeadings.addElement("Process");
            columnHeadings.addElement(buildIdColumnName);
            columnHeadings.addElement("Release");
            columnHeadings.addElement("Driver");
            columnHeadings.addElement("Date");
            columnHeadings.addElement("Description");

            java.text.SimpleDateFormat formatter = new java.text.SimpleDateFormat("MM/dd/yy hh:mm aaa"); //INT1941
            Vector dataVector = new Vector();

            getStatus().updateStatus("Listing build files tree.", false);
            java.util.List buildFilesList = SerializedBuildsLister.getInstance(build390HomeDirectory).listBUILDFilesTree(build390HomeDirectory.getAbsolutePath(),null);
            com.ibm.sdwb.build390.utilities.MultipleConcurrentException allExceptions = new com.ibm.sdwb.build390.utilities.MultipleConcurrentException("Exceptions encountered during loading builds:!"); /*TST1828 */

            for (Iterator iter = buildFilesList.iterator(); iter.hasNext();) {
                File file = (File)iter.next();
                try {
                    MBBuild loadedBuild  = MBBuildLoader.loadBuild(file);
                    getStatus().updateStatus("Loading  build - " + file.getAbsolutePath() , false);
                    if (loadedBuild!=null && SerializedBuildsLister.BUILD_DISPLAY_CRITERIA.passes(loadedBuild)) {

                        buildIdToDirectoryMapping.put(loadedBuild.get_buildid(), file);
                        // check type
                        Vector oneRow = new Vector();
                        // load the list box
                        oneRow.addElement((String) classnameToTypeMapping.get(loadedBuild.getClass().getName()));
                        oneRow.addElement(loadedBuild.get_buildid());
                        if (loadedBuild.getReleaseInformation()!=null) {
                            oneRow.addElement(loadedBuild.getReleaseInformation().getLibraryName());
                        } else {
                            oneRow.addElement("");
                        }
                        if (loadedBuild.getDriverInformation()!=null) {
                            oneRow.addElement(loadedBuild.getDriverInformation().getName());
                        } else {
                            oneRow.addElement("");
                        }
                        FormattedDateAdapter dateAdapter = new FormattedDateAdapter(loadedBuild.get_date());
                        dateAdapter.setDateFormatter(formatter);
                        oneRow.addElement(dateAdapter);
                        oneRow.addElement(loadedBuild.get_descr());
                        dataVector.addElement(oneRow);
                    }
                } catch (com.ibm.sdwb.build390.MBBuildException mbe) {
                    allExceptions.addException(mbe);
                }

            }

            loadModel.setDataVector(dataVector, columnHeadings);
            loadModel.fireTableDataChanged();
            getStatus().updateStatus("Refresh complete.",false);

            if (!allExceptions.getExceptionSet().isEmpty()) {
                lep.LogException(allExceptions);
            }

        }

        public boolean isHaltable() {
            return true;
        }

        public void haltProcess() throws com.ibm.sdwb.build390.MBBuildException {
            stopped = true;

        }


    }

    class LoadAction extends TableSelectionCancelableAction {
        String buildIdColumnName = null;

        LoadAction(JTable tempTable, String tempBuildIDColumnName) {
            super("Load", tempTable);
            buildIdColumnName = tempBuildIDColumnName;
        }

        public void doAction(ActionEvent e) {

            Setup setup = SetupManager.getSetupManager().createSetupInstance();
            List theBuildids = getBuildsToHandle(buildIdColumnName);
            Iterator buildIterator = theBuildids.iterator();
            StringBuffer serviceBuildsBuffer = new StringBuffer();
            while (buildIterator.hasNext()) {
                try {
                    InternalFrameBuildPanelHolder frameToRestart = null;
                    MBBuild build = getBuildForBuildId((String) buildIterator.next());
                    if (build instanceof MBUBuild) {
                        frameToRestart = UserBuildPanel.getUserBuildFrame((MBUBuild) build, true);
                    } else if (build instanceof com.ibm.sdwb.build390.info.UsermodGeneralInfo) {
                        new MBMsgBox("Error","Build "+build.get_buildid() +" cannot be restarted.  Usermod restart is not supported.");
                    } else if (build instanceof MBBuild) {
                        frameToRestart = DriverBuildPanel.getDriverBuildFrame(build, true);
                    } else {
                        serviceBuildsBuffer.append("Buildid="+build.get_buildid());
                        serviceBuildsBuffer.append(MBConstants.NEWLINE);
                    }

                    if (build.getProcessForThisBuild()!=null) {
                        if (build.getProcessForThisBuild().hasCompletedSuccessfully() & !build.getProcessForThisBuild().isRestartableAfterCompletion()) {
                            new MBMsgBox("Information","Build "+build.get_buildid() +" has been run to completion, it can not be restarted, only viewed.");
                            if (frameToRestart!=null) {
                                frameToRestart.getBuildPanel().setAllowEditing(false);
                            }
                        }
                    }
                    if (build.getProcessForThisBuild()!=null & frameToRestart != null) {
                        frameToRestart.restart();
                    }

                } catch (java.io.IOException ioe) {
                    lep.LogException("Error loading build", ioe);
                } catch (com.ibm.sdwb.build390.MBBuildException mbe) {
                    lep.LogException(mbe);
                }

            }
            if (serviceBuildsBuffer.length() > 0) {
                new MBMsgBox("Warning : ","Restart is not supported for the following builds:" + MBConstants.NEWLINE +
                             serviceBuildsBuffer.toString());
            }

        }
    }

    class CreateAction extends TableSelectionCancelableAction {
        String buildIdColumnName = null;

        CreateAction(JTable tempTable, String tempBuildIDColumnName) {
            super("Create", tempTable);
            buildIdColumnName = tempBuildIDColumnName;
        }

        public void doAction(ActionEvent e) {
            List theBuildids = getBuildsToHandle(buildIdColumnName);
            Iterator buildIterator = theBuildids.iterator();
            while (buildIterator.hasNext()) {
                try {
                    MBBuild oldBuild = getBuildForBuildId((String) buildIterator.next());
                    MBBuild newBuild =null;
                    if (oldBuild instanceof com.ibm.sdwb.build390.MBUBuild) {
                        newBuild = new com.ibm.sdwb.build390.MBUBuild(lep);
                    } else if (oldBuild instanceof com.ibm.sdwb.build390.info.UsermodGeneralInfo) {
                        newBuild = new com.ibm.sdwb.build390.info.UsermodGeneralInfo(lep);
                    } else {
                        //TST3068
                        newBuild = new MBBuild(oldBuild.getBuildIDStart(),MBConstants.DRIVERBUILDDIRECTORY,lep );
                    }
                    newBuild.copyBuildSettings(oldBuild);
                    com.ibm.sdwb.build390.MBJavaFile jf = new com.ibm.sdwb.build390.MBJavaFile(lep);
                    jf.mcopy(oldBuild.getBuildPath(), ".cmp",  newBuild.getBuildPath());
                    jf.mcopy(oldBuild.getBuildPath(), ".dir",  newBuild.getBuildPath());
                    jf.mcopy(oldBuild.getBuildPath(), ".list", newBuild.getBuildPath());
                    newBuild.set_descr("Clone of " + oldBuild.get_descr());
                    if (newBuild instanceof com.ibm.sdwb.build390.info.UsermodGeneralInfo) {//TST3333
                        new MBMsgBox("Error","Copying of Usermod is not supported.");
                    } else if (newBuild instanceof com.ibm.sdwb.build390.MBUBuild) {
                        MBUBuild userBuild = (MBUBuild) newBuild;
                        UserBuildPanel.getUserBuildFrame(userBuild, true);
                    } else if (newBuild instanceof com.ibm.sdwb.build390.MBBuild) {
                        DriverBuildPanel.getDriverBuildFrame(newBuild, true);
                    }
                } catch (java.io.IOException ioe) {
                    lep.LogException("Error loading build", ioe);
                } catch (com.ibm.sdwb.build390.MBBuildException mbe) {
                    lep.LogException(mbe);
                }

            }
        }
    }
/*
    class CopyToRMIAction extends TableSelectionCancelableAction {
        String buildIdColumnName = null;

        CopyToRMIAction(JTable tempTable, String tempBuildIDColumnName) {
            super("Copy to RMI", tempTable, thisFrame);
            buildIdColumnName = tempBuildIDColumnName;
        }

        public void doAction(ActionEvent e) {
            List theBuildids = getBuildsToHandle(buildIdColumnName);
            Iterator buildIterator = theBuildids.iterator();
            StringBuffer dbuildNonServiceBuffer = new StringBuffer();
            while (buildIterator.hasNext()) {
                try {
                    MBBuild oneBuild = getBuildForBuildId((String) buildIterator.next());
                    if (oneBuild instanceof com.ibm.sdwb.build390.MBABuild) {
                        com.ibm.sdwb.build390.MBABuild build = (com.ibm.sdwb.build390.MBABuild)oneBuild;
                        if ((new  File(build.getBuildPath()+ build.getAparName()+".ser")).exists()) {
                            getStatus().updateStatus("Copying "+ build.getAparName() +" to " +build.getSetup().getLibraryInfo().getLibraryAddress(), false);
                            build.getSetup().GetLibrary(lep).sendAparToServer(build);
                            getStatus().updateStatus("Copy of "+ build.getAparName() +" to " + build.getAparName()+"."+build.getReleaseInformation().getLibraryName()+" ("+build.get_buildid()+") Successful", false);
                        } else {
                            getStatus().updateStatus("  File " +(new  File(build.getBuildPath()+ build.getAparName()+".ser")).getAbsolutePath() +
                                                     "  not found - copy unsuccessful", false);
                        }
                    }

                    if (oneBuild instanceof com.ibm.sdwb.build390.info.PTFMultiSetInfo) {
                        for (Iterator ptfBuildIterator = ((com.ibm.sdwb.build390.info.PTFMultiSetInfo)oneBuild).getPTFSets().iterator(); ptfBuildIterator.hasNext();) {
                            MBPBuild oneInfo = (MBPBuild) ptfBuildIterator.next();
                            getStatus().updateStatus("Copying Ptf set " +  oneInfo.get_buildid() + " object to " + oneInfo.getSetup().getLibraryInfo().getLibraryAddress()  , false);
                            oneInfo.getSetup().GetLibrary(lep).sendPTFToServer(oneInfo);
                            getStatus().updateStatus("Copy of Ptf set " +  oneInfo.get_buildid() + " object to " + oneInfo.getSetup().getLibraryInfo().getLibraryAddress()+" Successful", false);

                        }

                    }

                    if (oneBuild instanceof com.ibm.sdwb.build390.MBDBuild) {
                        getStatus().updateStatus("Verifying if " + oneBuild.getReleaseInformation().getLibraryName() + " is in service ",false);
                        if (oneBuild.getSetup().GetLibrary(lep).isInService(oneBuild)) {//if release is in service 
                            getStatus().updateStatus("Copying "+ oneBuild.get_buildid() +" to " +oneBuild.getSetup().getLibraryInfo().getLibraryAddress(), false);
                            com.ibm.sdwb.build390.MBABuild aparBuild = new com.ibm.sdwb.build390.MBABuild(oneBuild.dumpFields(),lep);
                            aparBuild.setDefect(aparBuild.getDriverInformation().getName());
                            aparBuild.setAparName(aparBuild.getDriverInformation().getName());
                            oneBuild.getSetup().GetLibrary(lep).sendAparToServer(aparBuild);//tranfer build obj
                            getStatus().updateStatus("Copy of " + oneBuild.get_buildid() + " Successful", false);
                        } else {
                            dbuildNonServiceBuffer.append("Buildid="+oneBuild.get_buildid());
                            dbuildNonServiceBuffer.append(", Release="+oneBuild.getReleaseInformation().getLibraryName());
                            dbuildNonServiceBuffer.append(MBConstants.NEWLINE);
                        }

                    }

                } catch (java.io.IOException ioe) {
                    lep.LogException("Error loading build", ioe);
                } catch (com.ibm.sdwb.build390.MBBuildException mbe) {
                    lep.LogException(mbe);
                }

            }
            if (dbuildNonServiceBuffer.length() > 0) {
                new MBMsgBox("Warning : Non Service Releases", "The following buildids did not get copied over to the rmi server, since they aren't service releases" + MBConstants.NEWLINE + 
                             dbuildNonServiceBuffer.toString());
            }
        }
    }
*/
    class ZipAction extends TableSelectionCancelableAction {
        String buildIdColumnName = null;
        com.ibm.sdwb.build390.MBFtp zftp = null;

        ZipAction(JTable tempTable, String tempBuildIDColumnName) {
            super("Zip", tempTable);
            buildIdColumnName = tempBuildIDColumnName;
        }

        public void doAction(ActionEvent e) {
            stopped=false;
            List theBuildids = getBuildsToHandle(buildIdColumnName);
            Iterator buildIterator = theBuildids.iterator();
            while (buildIterator.hasNext()&!stopped) {
                try {
                    MBBuild oneBuild = getBuildForBuildId((String) buildIterator.next());

                    //get comments from user
                    com.ibm.sdwb.build390.MBZipCommentsDialog cm = new com.ibm.sdwb.build390.MBZipCommentsDialog(oneBuild.get_buildid(), thisFrame);
                    if (stopped) {
                        return;
                    }

                    if (cm.getOKStatus()) {

                        getStatus().updateStatus("Zipping the build directory contents", false);
                        com.ibm.sdwb.build390.MBUtilities.zipBuildDirectory(oneBuild.getBuildPath(), oneBuild.get_buildid());
                        String ftpserver = com.ibm.sdwb.build390.MBUtilities.getStorageServer();
                        if (ftpserver != null) {
                            //#DEF.TST1569:
                            zftp = new com.ibm.sdwb.build390.MBFtp(ftpserver, "b390util", "team4dog",lep);
                            String path = new String(oneBuild.get_buildid()+".zip");
                            String src = new String(oneBuild.getBuildPath()+java.io.File.separator+oneBuild.get_buildid()+".zip");
                            getStatus().updateStatus("Sending the zipped information", false);

                            if (!zftp.zput(src, path, false)) {
                                throw new com.ibm.sdwb.build390.FtpError("Could not upload "+src+" to "+path);
                            }
                            if (stopped) {
                                return;
                            }

                            getStatus().updateStatus("Notifying the development team", false);
                            // send mail to development
                            com.ibm.sdwb.build390.MBUtilities.SendZipMail(oneBuild.get_buildid(), cm.getNotesID(), cm.getComments(), path);
                            getStatus().updateStatus("", false);
                        }

                    }

                } catch (java.io.IOException ioe) {
                    if (!stopped) {
                        lep.LogException("Error loading build", ioe);
                    }
                } catch (com.ibm.sdwb.build390.MBBuildException mbe) {
                    if (!stopped) {
                        lep.LogException(mbe);
                    }
                }

            }
        }

        public void stop() {
            try {
                super.stop();
                if (zftp!=null) {
                    zftp.stop();
                }
            } catch (com.ibm.sdwb.build390.MBBuildException mbe) {
                // swallow stop exceptions
            }
        }

    }

/*
    class RepackPTFAction extends TableSelectionCancelableAction {
        String buildIdColumnName = null;

        RepackPTFAction(JTable tempTable, String tempBuildIDColumnName) {
            super("Repackage", tempTable, thisFrame);
            buildIdColumnName = tempBuildIDColumnName;
        }

        public void doAction(ActionEvent e) {
            List theBuildids = getBuildsToHandle(buildIdColumnName);
            Iterator buildIterator = theBuildids.iterator();
            while (buildIterator.hasNext()) {
                try {
                    MBBuild oneBuild = getBuildForBuildId((String) buildIterator.next());
                    if (oneBuild instanceof  com.ibm.sdwb.build390.info.PTFMultiSetInfo) {
                     final   PTFMultiSetInfo wholeSet = (PTFMultiSetInfo)oneBuild;
                        // I think this is redundant, but don't have time to confirm.

                        final com.ibm.sdwb.build390.MultiplePTFFrame displayFrame = new com.ibm.sdwb.build390.MultiplePTFFrame();
                        MBCancelableActionListener initPage = new MBCancelableActionListener(displayFrame) {
                            public void doAction(ActionEvent evt) {
                                try {
                                    displayFrame.getStatus().updateStatus("Preparing to read build objects.", false);
                                    for (Iterator ptfBuildIterator = wholeSet.getPTFSets().iterator(); ptfBuildIterator.hasNext();) {
                                        MBPBuild oneInfo = (MBPBuild) ptfBuildIterator.next();
                                        displayFrame.getStatus().updateStatus("Copying Ptf set " +  oneInfo.get_buildid() + " object to " + oneInfo.getSetup().getLibraryInfo().getLibraryAddress()  , false);
                                        oneInfo.getSetup().GetLibrary(lep).sendPTFToServer(oneInfo);
                                        displayFrame.getStatus().updateStatus("Copy of Ptf set " +  oneInfo.get_buildid() + " object to " + oneInfo.getSetup().getLibraryInfo().getLibraryAddress()+" Successful", false);
                                        displayFrame.addPTFSet(oneInfo);
                                    }
                                    displayFrame.getStatus().updateStatus("Build objects copy complete.", false);
                                    wholeSet.setRebuild(true);
                                    displayFrame.setBuild(wholeSet);

                                    try {
                                        displayFrame.runRepackagePTF();
                                        //displayFrame.getStatus().updateStatus("Repackage complete", false);
                                    } finally {
                                        displayFrame.clearRunningItem();
                                    }
                                } catch (MBBuildException mbe) {
                                    status.updateStatus("PTF Setup failed",false);
                                    lep.LogException(mbe);
                                }
                            }
                        };
                        initPage.actionPerformed(new ActionEvent(this, 0, ""));

                    }
                } catch (java.io.IOException ioe) {
                    lep.LogException("Error loading build", ioe);
                } catch (com.ibm.sdwb.build390.MBBuildException mbe) {
                    lep.LogException(mbe);
                }

            }
        }
    }
*/

    class CleanAction extends TableSelectionCancelableAction {
        String buildIdColumnName = null;
        com.ibm.sdwb.build390.process.CleanProcessArtifacts cleanupStuff =null;

        CleanAction(JTable tempTable, String tempBuildIDColumnName) {
            super("Clean", tempTable);
            buildIdColumnName = tempBuildIDColumnName;
        }

        public void doAction(ActionEvent e) {
            try {                 /** an empty runnable to makesure the previous refresh(updating of table model had completed its operation)*/
                SwingUtilities.invokeAndWait(new Runnable() {
                                                 public void run() {
                                                 }
                                             });
            } catch (InterruptedException iep) {

            } catch (java.lang.reflect.InvocationTargetException ivt) {
            }
            CleanupOptionsFrame cleanOptions = new CleanupOptionsFrame(thisFrame);
            Set driverUnlocks = new HashSet();
            Set jobsToPurge = new HashSet();
            Set mvsBuildDeletes = new HashSet();
            Set localDeletes = new HashSet();
            StringBuffer nothingToCleanInMVS = new StringBuffer();

            List theBuildids = getBuildsToHandle(buildIdColumnName);
            Iterator buildIterator = theBuildids.iterator();
            while (buildIterator.hasNext() && !cleanOptions.isClosed() & !stopped) {
                try {
                    MBBuild oneBuild = getBuildForBuildId((String) buildIterator.next());
                    if (!cleanOptions.applyToAll()) {
                        cleanOptions.setBuildIdToDisplayInTitle(oneBuild.get_buildid());
                        cleanOptions.setVisible(true);
                    }
                    if (oneBuild.getProcessForThisBuild() == null) {
                        if (cleanOptions.isLocalDataSelected()) {
                            CleanableEntity localCleanableEntity = new CleanableEntity();
                            localCleanableEntity.addLocalFileOrDirectory(new File(oneBuild.getBuildPath()));
                            localDeletes.add(localCleanableEntity);
                        }
                    } else {
                        //#DEF:TST1483/TST1579.if no local entry exists add one(hack for now).
                        if (!oneBuild.getProcessForThisBuild().getCleanableEntity().getAllLocalFiles().contains(new File(oneBuild.getBuildPath()))) {
                            oneBuild.getProcessForThisBuild().getCleanableEntity().addLocalFileOrDirectory(new File(oneBuild.getBuildPath()));
                        }

                        if (cleanOptions.isLocalDataSelected()) {
                            localDeletes.add(oneBuild.getProcessForThisBuild().getCleanableEntity());
                        }
                        if (cleanOptions.isMVSJobOutputSelected()) {
                            jobsToPurge.add(oneBuild.getProcessForThisBuild().getCleanableEntity());
                        }
                        if (cleanOptions.isUnlockSelected()) {
                            driverUnlocks.add(oneBuild.getProcessForThisBuild().getCleanableEntity());
                        }
                        //TST1483 put is if as the last one. 
                        if (cleanOptions.isHostDataSetsSelected()) {

                            if (oneBuild.getDriverInformation()==null) { /*TST1921 */

                                com.ibm.sdwb.build390.process.MVSReleaseAndDriversList getReleaseList = new com.ibm.sdwb.build390.process.MVSReleaseAndDriversList(oneBuild.getMainframeInfo(), oneBuild.getLibraryInfo(), null,thisFrame);
                                getReleaseList.externalRun();
                            }
                            if (oneBuild.getDriverInformation()!=null) {
                                mvsBuildDeletes.add(oneBuild.getProcessForThisBuild().getCleanableEntity());
                            } else {
                                nothingToCleanInMVS.append(oneBuild.get_buildid() +"\n");
                            }
                        }
                    }
                } catch (java.io.IOException ioe) {
                    lep.LogException("Error loading build", ioe);
                } catch (com.ibm.sdwb.build390.MBBuildException mbe) {
                    lep.LogException(mbe);
                }

            }
            boolean hasBeenCancelled  = cleanOptions.isClosed();
            cleanOptions.dispose();

            if (!hasBeenCancelled&!stopped) {


                cleanupStuff = new com.ibm.sdwb.build390.process.CleanProcessArtifacts(driverUnlocks, jobsToPurge, mvsBuildDeletes, localDeletes, parentWindow);
                cleanupStuff.run();
                viewRefresh.actionPerformed(new ActionEvent(e.getSource(), 0, ""));
                if (nothingToCleanInMVS.toString().trim().length() > 0) {
                    problemBox("Warning!","The following builds do not have entities on the host.\nThe following drivers do not exist.\n" + nothingToCleanInMVS.toString());
                }
            }

        }


        public void stop() {
            try {
                super.stop();
                if (cleanupStuff!=null) {
                    cleanupStuff.haltProcess();
                }
            } catch (com.ibm.sdwb.build390.MBBuildException mbe) {
                // swallow stop exceptions
            }
        }

    }

    class UndoAction extends TableSelectionCancelableAction {
        String buildIdColumnName = null;

        UndoAction(JTable tempTable, String tempBuildIDColumnName) {
            super("Undo", tempTable);
            buildIdColumnName = tempBuildIDColumnName;
        }

        public void doAction(ActionEvent e) {
            List theBuildids = getBuildsToHandle(buildIdColumnName);
            Iterator buildIterator = theBuildids.iterator();
            while (buildIterator.hasNext()) {
                try {
                    MBBuild oneBuild = getBuildForBuildId((String) buildIterator.next());
                    oneBuild.getProcessForThisBuild().setUserCommunicationInterface(thisFrame);
                    oneBuild.getProcessForThisBuild().undoProcess();
                } catch (java.io.IOException ioe) {
                    lep.LogException("Error loading build", ioe);
                } catch (com.ibm.sdwb.build390.MBBuildException mbe) {
                    lep.LogException(mbe);
                }

            }
        }
    }

    class ViewRefreshAction extends CancelableAction {
        String buildIdColumnName = null;
        JTable theTable = null;
        com.ibm.sdwb.build390.process.management.Haltable stopObject;
        RefreshTable refreshTable;

        ViewRefreshAction(JTable tempTable, String tempBuildIDColumnName) {
            super("Refresh");
            buildIdColumnName = tempBuildIDColumnName;
            theTable = tempTable;
            refreshTable = new RefreshTable(theTable);
        }

        public void doAction(ActionEvent e) {
            stopObject = refreshTable;
            refreshTable.refresh();
            //initColumnSizes();
        }

        public void postAction() {
            load.setEnabled(false);
//            copy.setEnabled(false);
            zip.setEnabled(false);
//            repack.setEnabled(false);
            create.setEnabled(false);
//            repack.setEnabled(false);
            clean.setEnabled(false);
            undo.setEnabled (false);

            viewBuildInfo.setEnabled(false);
            viewBuildLog.setEnabled(false);
            viewDriverStatus.setEnabled(false);

            loadButton.setEnabled(false);
            cleanButton.setEnabled(false);
            undoButton.setEnabled(false);
        }
        public void stop() throws com.ibm.sdwb.build390.MBBuildException{
            if (stopObject !=null) {
                stopObject.haltProcess();
            }
        }

    }

    class TableMouseListener extends MouseAdapter {
        JTable theTable;
        TableMouseListener(JTable tempTable) {
            this.theTable = tempTable;
        }
        public void mousePressed(MouseEvent e) {
            if (theTable.getSelectedRowCount() > 0) {

                loadButton.setEnabled(true);
                cleanButton.setEnabled(true);
                undoButton.setEnabled(true);

                load.setEnabled(true);
//                copy.setEnabled(true);
                zip.setEnabled(true);
//                repack.setEnabled(true);
                create.setEnabled(true);
                clean.setEnabled(true);
                undo.setEnabled(true);

                viewBuildInfo.setEnabled(true);
                viewBuildLog.setEnabled(true);
                viewDriverStatus.setEnabled(true);
            }


        }
        public void mouseClicked(MouseEvent e) {
        }
    }
    class ViewBuildInfoAction extends TableSelectionCancelableAction {
        String buildIdColumnName = null;

        ViewBuildInfoAction(JTable tempTable, String tempBuildIDColumnName) {
            super("Build info", tempTable);
            buildIdColumnName = tempBuildIDColumnName;
        }


        public void doAction(ActionEvent e) {
            List theBuildids = getBuildsToHandle(buildIdColumnName);
            Iterator buildIterator = theBuildids.iterator();
            while (buildIterator.hasNext()) {
                try {
                    final MBBuild oneBuild = getBuildForBuildId((String) buildIterator.next());
                    oneBuild.viewBuild(thisFrame);
                } catch (java.io.IOException ioe) {
                    lep.LogException("Error loading build", ioe);
                } catch (com.ibm.sdwb.build390.MBBuildException mbe) {
                    lep.LogException(mbe);
                }
            }
        }
    }


    class ViewBuildLogAction extends TableSelectionCancelableAction {
        String buildIdColumnName = null;

        ViewBuildLogAction(JTable tempTable, String tempBuildIDColumnName) {
            super("Build log", tempTable);
            buildIdColumnName = tempBuildIDColumnName;
        }

        public void doAction(ActionEvent e) {
            List theBuildids = getBuildsToHandle(buildIdColumnName);
            Iterator buildIterator = theBuildids.iterator();
            while (buildIterator.hasNext()) {
                try {
                    MBBuild oneBuild = getBuildForBuildId((String) buildIterator.next());
                    //this will probably go away, when we move all the post build display screens like what we show for usermod (setAllowEdit(false)).
                    if (oneBuild!=null && oneBuild instanceof com.ibm.sdwb.build390.info.UsermodGeneralInfo) {
                        com.ibm.sdwb.build390.info.UsermodGeneralInfo umBuild = (com.ibm.sdwb.build390.info.UsermodGeneralInfo)oneBuild;
                        boolean atleastOneDisplayed  = false;
                        for (Iterator changesetGroupInfoIterator = umBuild.getChangesetGroups().iterator(); changesetGroupInfoIterator.hasNext();) {
                            ChangeRequestPartitionedInfo oneSet = (ChangeRequestPartitionedInfo)  changesetGroupInfoIterator.next();
                            File buildLogFile = new File(oneSet.getBuildPathAsFile(),"Build.log");
                            if (buildLogFile.exists()) {
                                new MBEdit(buildLogFile.getAbsolutePath(),getLEP());
                                atleastOneDisplayed = true;
                            }
                        }

                        File buildLogFile = new File(umBuild.getBuildPathAsFile(),"Build.log");
                        if (buildLogFile.exists()) {
                            new MBEdit(buildLogFile.getAbsolutePath(),getLEP());
                            atleastOneDisplayed = true;
                        }
                        if (!atleastOneDisplayed) {
                            problemBox("Information:"+oneBuild.get_buildid(),"build.log empty ");
                        }
                    } else {
                        new com.ibm.sdwb.build390.MBEdit(oneBuild.getBuildPath()+"Build.log",lep);
                    }
                } catch (java.io.IOException ioe) {
                    lep.LogException("Error loading build", ioe);
                } catch (com.ibm.sdwb.build390.MBBuildException mbe) {
                    lep.LogException(mbe);
                }
            }
        }
    }

    class ViewDriverStatusAction extends TableSelectionCancelableAction {
        String buildIdColumnName = null;
        com.ibm.sdwb.build390.MBStop itemToStop = null;
        boolean stopped = false;

        ViewDriverStatusAction(JTable tempTable, String tempBuildIDColumnName) {
            super("Driver Status", tempTable);
            buildIdColumnName = tempBuildIDColumnName;
        }

        public void doAction(ActionEvent e) {
            stopped=false;
            List theBuildids = getBuildsToHandle(buildIdColumnName);
            Iterator buildIterator = theBuildids.iterator();
            StringBuffer skipped = new StringBuffer();
            while (buildIterator.hasNext()) {
                try {
                    MBBuild build = getBuildForBuildId((String) buildIterator.next());
                    String cmd = "DRVRRPT CMVCREL=\'"+build.getReleaseInformation().getLibraryName()+"\', DRIVER=\'"+build.getDriverInformation().getName()+"\', "+build.getLibraryInfo().getDescriptiveStringForMVS();
                    String clrout = build.getBuildPath()+"drvrstat";
                    com.ibm.sdwb.build390.MBSocket mySock = new com.ibm.sdwb.build390.MBSocket(cmd, clrout, "Getting driver status", getStatus(), build.getSetup().getMainframeInfo(), lep);
                    itemToStop = mySock;
                    mySock.run();
                    if (stopped) {
                        continue;
                    }
                    com.ibm.sdwb.build390.MBEdit outEdit = new com.ibm.sdwb.build390.MBEdit(clrout+MBConstants.CLEARFILEEXTENTION,lep);
                } catch (java.io.IOException ioe) {
                    lep.LogException("Error loading build", ioe);
                } catch (com.ibm.sdwb.build390.MBBuildException mbe) {
                    lep.LogException(mbe);
                }
            }

            if (skipped.toString().length() > 0) {
                skipped.delete(skipped.length()-2,skipped.length());
                new MBMsgBox("Warning","The driver status option is disabled for MULTIPTF builds. " + MBConstants.NEWLINE + "The following buildids were skipped." + MBConstants.NEWLINE + skipped.toString());
            }
        }

        public void stop() throws com.ibm.sdwb.build390.MBBuildException{
            stopped = true;
            if (itemToStop != null) {
                itemToStop.stop();
            }
        }
    }


    public  Dimension getMinimumSize() {   /*INT1760  */
        Dimension sdim = new Dimension(5,5);
        return sdim;
    }

    public Dimension  getPreferredSize() {
        return new  Dimension(500,325);
    }

}


