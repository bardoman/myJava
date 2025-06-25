package com.ibm.sdwb.build390.userinterface.graphic.panels.managereleases;
/*********************************************************************/
/* MBDriverBuildPage class for the Build/390 client                  */
/*  Creates and manages the Driver Build Page                        */
/*********************************************************************/
// Changes
// Date     Defect/Feature      Reason
// 09/01/98 Defect_112 	        When creating a driver, use the same hlq as the release
// 10/19/98 //#x                Add library setting to dialog and update dialog when setup gets changed
// 11/03/98 //#refresh          refresh the tree when a driver gets created or deleted
// 12/04/98 Defect_149          Add shadow report
// 02/10/99 changed_label       Changed the menu labels under retrieve to remove confussion
// 03/30/99 force_query         allow force of query
// 04/01/99 MoveImages          move images directory
// 04/21/99 AddPDTreports       Add pdt report support
// 04/27/99 metadata editor     remove menu selectio
// 05/02/99 defect318           fix multiple windows off of this page and don't confuse info
// 05/17/99 FixMinSize          Set minimum window size
// 05/20/99 #320                various UI interface changes
// 06/17/99 #320       		more of the same
// 06/28/99 defect_415          edit reports
// 07/29/99 fix parse of dirver name
// 08/05/99 RemoteBase          add support for delta drivers whose base is in a differnet release
// 08/26/99 SMODRPT             group SMODRPT as a menu item
// 08/26/99                     disable the metadata validation report menu selection
// 08/26/99                     fix parse of selected driver name when a delta of a driver based in a diff release
// 08/27/99                     enable report type base on driver name
// 09/22/99 pjs - warn the user that setup changes just affect this process.
// 09/29/99 pjs - if a syntax error occurred during MD syntax check, restart
// 09/30/99 pjs - Fix help link
// 10/21/99 ken - change metadata calls to library
// 11/04/99 chris - send Setup and MBABuild objects to RMI server
// 11/08/99 chris - set driver in MBABuild object
// 12/09/99 *apar  - set status display of apar driver successful.
/*********************************************************************/
/* 01/09/00 Ken    	complete rewrite of the page  */
// 02/16/00 Ken		change release itemlistener to actionlistener
// 03/07/2000 reworklog rework the log process using listeners
// 03/29/2000 Ken move fixes from 223 to 23
// 05/02/2000 change the comments "unexpected error" to "error occurred ,check your log file"
// 05/04/2000 pjs - Add ability to view config data
// 05/10/2000 Ken - when doing findNode, only compare up to the first space, so it works with drivers based on diff rels.
// 05/14/2000 Ken - desynchronized method so it doesn't lock up anymore.
// 05/30/2000 defect 17 - passed the lep object in the constructor of MBFamilyClient
// 11/14/00: feature 111 - Made the Driver Type Dialog frame not to get disposed  if wrong value is entered for the base driver.
/*********************************************************************/
// 01/15/2001 #ReleaseCombo Editable:  Set ReleaseCombo Editable to allow right click popup of filter
// 01/16/2001 #View Release:    View release configuration data  should not be grayed out
// 02/07/2001 #pass true in ReleaseCombo - combo doesnt refresh
// 02/08/2001 #editMetadata:  logic needed to be reworked so edit metadata menu items enabled correctly
// 02/09/2001 #BlankSel: must catch non null zero len selection object
// 02/16/2001 #Defect 209: shadow Report should be enabled when a release is clicked
// 02/16/2001 #NullSel: must catch null selection object
// 03/20/2001 #defect168: changed from "Dependency chain"
// 03/22/2001 #Defect277: commented out two calls to prevent debug related hang
// 04/10/2001 #MetaOperEnhance: Enhance metadata operations
// 05/07/2001 #226: ungrey create driver only after release is chosen
// 06/05/2001 #DEF_TST0458: Need to get releases to update internal hash
// 07//11//2001 #Defect.627: Need to determine dependancy properly
// 12/14/2001 #Def:FixMetadataMenu 
/*********************************************************************/
// 02/08/2002 #Def.806: Full delta chain on new drvr not seen until refresh.
// 03/12/2002 #Def.ViewRelConfData: View Release Config Data not enabled
// 03/12/2002 #Def.PTM1975: A typo 'Now' has to be changed to 'No'
// 04/10/2002 #Def.INT0805  L/R Scroll bar on Manage Rel/Drv not working
// 04/22/2002 #Def.INT0879:  Manage releases column size is reset
// 12/03/2002 SDWB-2019 Enhance the help system
// 12/26/2002 INT0849  SERVXFER done on the host lacks setPartListForRelease call to BPS
// 01/03/2002 #Def.PK41052: The Metatdata editor worked for a release that was not turned on
//04/16/2003  #Def.INT1161:  MetadataReport fails on TYPE=FIELDS
//06/06/2003  #Def.INT1027:  size the Driver's Table.
//0920/2003   #DEF.TST1232: Sort driver list on populate
//12/02/2003  #DEF:INT1164: Additional Driver Report Options
//02/17/2004 #DEF INT1764 : Driver creation successful msg.
//02/18/2004 #DEF:INT1754 : Driver delete on a locked driver
//03/25/2004 TST1801        nullpointer/create inspection package
//05/03/2004 TST1864 Duplicate status.
/*********************************************************************/
//11/02/2005 SDWB2363 Redesign partchooser interface.
//08/24/2007 TST3459  Null pointer exception invoking Config admin function
//08/28/2007 TST3459A Null pointer exception invoking Config admin function
/*********************************************************************/


import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.SortedMap;
import java.util.Vector;

import javax.swing.*;
import javax.swing.border.LineBorder;
import javax.swing.border.TitledBorder;
import javax.swing.event.TableModelEvent;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableColumn;
import javax.swing.table.TableModel;

import com.ibm.sdwb.build390.*;
import com.ibm.sdwb.build390.help.HelpTopicID;
import com.ibm.sdwb.build390.library.LibraryInfo;
import com.ibm.sdwb.build390.logprocess.LogEventProcessor;
import com.ibm.sdwb.build390.mainframe.DriverInformation;
import com.ibm.sdwb.build390.mainframe.ReleaseAndDriverParameters;
import com.ibm.sdwb.build390.mainframe.ReleaseInformation;
import com.ibm.sdwb.build390.process.AbstractProcess;
import com.ibm.sdwb.build390.process.CheckMVSMetadataValidity;
import com.ibm.sdwb.build390.process.DeleteMVSDriver;
import com.ibm.sdwb.build390.process.DeleteMVSRelease;
import com.ibm.sdwb.build390.process.DriverReport;
import com.ibm.sdwb.build390.process.GetMVSSiteDefaults;
import com.ibm.sdwb.build390.process.MetadataReport;
import com.ibm.sdwb.build390.process.SMODReport;
import com.ibm.sdwb.build390.process.ShadowReport;
import com.ibm.sdwb.build390.user.Setup;
import com.ibm.sdwb.build390.userinterface.graphic.actions.ActionBuilder;
import com.ibm.sdwb.build390.userinterface.graphic.actions.UserInterfaceActionBarSupport;
import com.ibm.sdwb.build390.userinterface.graphic.widgets.CancelableAction;
import com.ibm.sdwb.build390.userinterface.graphic.widgets.CancelableProcess;
import com.ibm.sdwb.build390.userinterface.graphic.widgets.HelpAction;
import com.ibm.sdwb.build390.userinterface.graphic.widgets.ReleaseSelectionCombo;
import com.ibm.sdwb.build390.userinterface.graphic.widgets.SortableTableModel;
import com.ibm.sdwb.build390.userinterface.text.utilities.CommandLineSettings;


public class ManageReleasesFrame extends MBInternalFrame implements javax.swing.event.TableModelListener {

    private MBBuild build; //need to figure out.


    private JScrollPane jsp;
    private JTable driverInfoTable;
    private DefaultTableModel dataModel;
    private SortableTableModel sorter;

    private static final String BasedString = new String("    (Based on "); // RemoteBase
    private static final String FullString = new String("    (Full)");
    private static final String NOLOCKINDICATOR = "NONE";
    private static final String DEFAULTKEY= "MANAGEPAGERELEASEDEFAULTS";
    private static final String METADATAVALIDATIONREPORTKEYWORD = "METADATAVALIDATIONREPORTKEYWORD";

    private Vector SearchedListOfDriversVector = new Vector();
    private JMenuItem cbReloadDisplay = new JMenuItem("Rebuild All Drivers List");
    private JButton reloadToolBarButton = new JButton();
    private JButton searchToolBarButton = new JButton();
    private final JButton btHelp            = new JButton("Help");
    private FindAction findAction;
    private ReloadDisplayAction reloadDisplay;

    private final JLabel libraryAddressLabel = new JLabel("Address:"); //#x
    private final JLabel libraryAddressNameLabel      = new JLabel(""); //#x
    private final JLabel libraryReleaseLabel             = new JLabel("Release");
    private final JLabel driverTableTitleLabel             = new JLabel("Drivers Table");
    private final JLabel reportTypesLabel            = new JLabel("Report Types");

    private final GridBagLayout gridBag = new GridBagLayout();
    private final JPanel topNorthPanel = new JPanel(gridBag);
    private final JPanel centerPanel  = new MBInsetPanel(new BorderLayout(), 2, 5, 2, 5);

    private final JList tableHeaderListBox = new JList();
    private TitledBorder driverTitledBorder=null;

    private ReleaseSelectionCombo releaseObject;
    private MetadataType metadataTypes[]=null;

    private static Vector columnHeadings = new Vector();
    private static Hashtable familyReleaseDefaults = null;

    static {
        columnHeadings.addElement("Driver Name");
        columnHeadings.addElement("Drivers in base");//#defect168: changed from "Dependency chain"
    }


    private static Map  driverReportHostMap = new HashMap();
    static {
        driverReportHostMap.put(BaseActionsBuilder.ACTION_NAME_BUILDS,BaseActionsBuilder.ACTION_NAME_BUILDS.toUpperCase());
        driverReportHostMap.put(BaseActionsBuilder.ACTION_NAME_DELTA_PARTS,"LOCAL");
        driverReportHostMap.put(BaseActionsBuilder.ACTION_NAME_FAILURE,"FAIL");
        driverReportHostMap.put(BaseActionsBuilder.ACTION_NAME_INACTIVE_PARTS,"INACTIVE");
        driverReportHostMap.put(BaseActionsBuilder.ACTION_NAME_SHIPPED_PARTS,"SHIPPED");
        driverReportHostMap.put(BaseActionsBuilder.ACTION_NAME_UNBUILT_PARTS,"UNBUILT");
        driverReportHostMap.put(BaseActionsBuilder.ACTION_NAME_UNPACKAGED_PARTS,"UNPACKAGED");
        driverReportHostMap.put(BaseActionsBuilder.ACTION_NAME_USERMOD,BaseActionsBuilder.ACTION_NAME_USERMOD.toUpperCase());
    }


    private PopulateReleasesAction populateReleasesAction =null;

    private javax.swing.Timer tableTimer= null;

    private List<Setup> internalSetupList = new LinkedList<Setup>(); /*this enables us to grab the latest pointer */

    private String MANAGERELEASES_SETUP_CHANGED = "MANAGERELEASES_SETUP_CHANGED";


    public ManageReleasesFrame() throws MBBuildException  {
        super("Manage Releases/Drivers", true, null);

        build = new MBBuild(lep);
        internalSetupList.add(build.getSetup());
        lep.addEventListener(build.getLogListener());
        releaseObject = new ReleaseSelectionCombo(build.getMainframeInfo(), build.getLibraryInfo(),lep);

        if (familyReleaseDefaults == null) {
            familyReleaseDefaults = (Hashtable) getGeneric(DEFAULTKEY);
            if (familyReleaseDefaults == null) {
                familyReleaseDefaults=new Hashtable();
            }
        }



        btHelp.setForeground(MBGuiConstants.ColorHelpButton);


        fillActionBar(new ManageReleasesActionConfigurer());

        Vector actionButtons = new Vector();
        addButtonPanel(btHelp, actionButtons);

        GridBagConstraints c0 = new GridBagConstraints();
        c0.gridx = 1;
        c0.gridy = 2;
        c0.weightx=1;
        c0.weighty=0;
        c0.insets = new Insets(15,5,2,5);
        c0.anchor = GridBagConstraints.WEST;

        libraryAddressLabel.setText(getLibraryTextPrefix() + libraryAddressLabel.getText());
        gridBag.setConstraints(libraryAddressLabel, c0);
        topNorthPanel.add(libraryAddressLabel);
        c0.gridx = 2;
        gridBag.setConstraints(libraryAddressNameLabel, c0);
        topNorthPanel.add(libraryAddressNameLabel);

        setLibInfo();

        c0.gridx = 1;
        c0.gridy = 3;

        libraryReleaseLabel.setText(getLibraryTextPrefix() + libraryReleaseLabel.getText());
        gridBag.setConstraints(libraryReleaseLabel, c0);
        topNorthPanel.add(libraryReleaseLabel);

        c0.weightx=2;
        c0.gridx = 2;
        c0.fill = GridBagConstraints.HORIZONTAL;
        gridBag.setConstraints(releaseObject, c0);
        topNorthPanel.add(releaseObject);


        centerPanel.add("Center", createTablePanel());

        getContentPane().add("North", topNorthPanel);
        getContentPane().add("Center", centerPanel);

        makeToolBarActions();

        if (familyReleaseDefaults.get(getLibInfo()) != null) {
            String previouslySelectedRelease = (String ) familyReleaseDefaults.get(getLibInfo());
            if (previouslySelectedRelease!=null) {
                releaseObject.select(previouslySelectedRelease);
            }
        }

        setVisible(true);

        populateReleasesAction.actionPerformed(new ActionEvent(this,ActionEvent.ACTION_PERFORMED,"FetchReleasesFromMainframe"));
        fireMenuEnableEvent(); // enable correct buttons
    }

    private JPanel createTablePanel() {
        dataModel = new DefaultTableModel() {
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        JPanel tableHeaderPanel = new JPanel(new BorderLayout());
        JPanel tablePanel = new JPanel(new BorderLayout());
        driverTitledBorder = BorderFactory.createTitledBorder(LineBorder.createGrayLineBorder() ,"Drivers Table ",TitledBorder.DEFAULT_JUSTIFICATION,TitledBorder.DEFAULT_POSITION,null,MBGuiConstants.ColorGroupHeading);
        tablePanel.setBorder(driverTitledBorder);


        JToolBar toolBar = new JToolBar();
        toolBar.setFloatable(false);
        toolBar.setToolTipText("search ToolBar");
        toolBar.setBorder(BorderFactory.createEtchedBorder());
        toolBar.setBackground(MBGuiConstants.ColorGeneralBackground);
        reloadToolBarButton = new JButton(new ImageIcon("images/reload.gif"));
        reloadToolBarButton.setToolTipText("Rebuild All Drivers List");
        toolBar.add(reloadToolBarButton);
        reloadToolBarButton.setEnabled(false);

        //Find button
        searchToolBarButton = new JButton(new ImageIcon("images/find.gif"));
        searchToolBarButton.setToolTipText("Find Driver");
        toolBar.add(searchToolBarButton);

        tableHeaderListBox.addMouseListener(mouseListener);
        tableHeaderPanel.add(toolBar,BorderLayout.NORTH);
        tableHeaderPanel.add(tableHeaderListBox,BorderLayout.SOUTH);
        tableHeaderListBox.setForeground(MBGuiConstants.ColorTableHeading);


        sorter = new SortableTableModel(dataModel);

        driverInfoTable = new JTable(sorter);
        sorter.setTableHeader(driverInfoTable.getTableHeader()); 
        driverInfoTable.getTableHeader().setToolTipText("Click to specify sorting; Control-Click to specify secondary sorting");
        driverInfoTable.setBorder(BorderFactory.createEtchedBorder());
        driverInfoTable.setBackground(Color.white);
        driverInfoTable.setColumnSelectionAllowed(false);
        driverInfoTable.setRowSelectionAllowed(true);
        driverInfoTable.setSelectionMode(ListSelectionModel.SINGLE_INTERVAL_SELECTION);

        driverInfoTable.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);

        driverInfoTable.addMouseListener(searchTableMouseListener);
        jsp = new JScrollPane(driverInfoTable,ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS, ScrollPaneConstants.HORIZONTAL_SCROLLBAR_ALWAYS);
        tablePanel.add(tableHeaderPanel,BorderLayout.NORTH);
        tablePanel.add(jsp,BorderLayout.CENTER);
        return tablePanel;
    }

    private void fillActionBar(com.ibm.sdwb.build390.userinterface.graphic.actions.ActionConfigurer configurer) {
        ActionBuilder  actionBuilder = ((UserInterfaceActionBarSupport)MBClient.getCommandLineSettings().getMode()).getManagePageActionBar(configurer);
        setJMenuBar(configurer.getMenuBar());
    }

    private void makeToolBarActions() {
        populateReleasesAction = new PopulateReleasesAction();
        releaseObject.addListItemListener(new ChangeReleaseItemListener());

        btHelp.addActionListener(new HelpAction("",HelpTopicID.MANAGEPAGE_HELP));


        findAction  = new FindAction();

        reloadDisplay = new ReloadDisplayAction();
        cbReloadDisplay.addActionListener(reloadDisplay);
        reloadToolBarButton.addActionListener(reloadDisplay);
        searchToolBarButton.addActionListener(findAction);
    }


    protected Action createAction(Class actionClass,Object[] arguments) {
        try {
            Class temp = actionClass.forName(actionClass.getName());
            Constructor[] actionConstructor = temp.getDeclaredConstructors();
            Object[] actionParameters = null; 
            if (arguments!=null && arguments.length > 0) {
                actionParameters = new Object[arguments.length +1];
                System.arraycopy(arguments,0,actionParameters,1,arguments.length);
            } else {
                actionParameters = new Object[1];
            }
            actionParameters[0]=this;

            /*The action constructor has one param: ie its enclosing instance. Eventhough the respective action classes
            like CreateMVSRelease have null constructor, we  still have to pass in the "ManageReleasesFrame's instance" */
            Action action = (Action)actionConstructor[0].newInstance(actionParameters);
            return action;
        } catch (ClassNotFoundException cnfe) {
        } catch (InstantiationException inte) {
        } catch (IllegalAccessException iae) {
        } catch (InvocationTargetException ivte) {
            ivte.printStackTrace();
        }
        return null;
    }


    private void initColumnSizes(JTable table) {
        TableModel model = table.getModel();
        TableColumn column = null;
        Component comp = null;
        int headerWidth = 0;
        int cellWidth = 0;
        int colCnt = table.getColumnCount();
        int rowCnt = table.getRowCount();
        String longValue = "";
        String tmpStr="";

        for (int col = 0; col < colCnt; col++) {
            column = table.getColumnModel().getColumn(col);

            headerWidth = ((String)column.getHeaderValue()).length();

            for (int row=0;row!=rowCnt;row++) {
                tmpStr = (String) model.getValueAt(row,col);
                if (tmpStr.length() > longValue.length()) {
                    longValue = tmpStr;
                }
            }
            comp = table.getDefaultRenderer(model.getColumnClass(col)).
                   getTableCellRendererComponent(table, longValue,false, false, 0, col);

            cellWidth = comp.getPreferredSize().width;

            int colNameWidth = table.getColumnName(col).length() * 12;

            int colDataWidth = cellWidth + headerWidth;

            if (colNameWidth > colDataWidth) {
                column.setPreferredWidth(colNameWidth);
            } else {
                column.setPreferredWidth(colDataWidth);
            }
        }
    } 

    private void handleReleaseSelection() throws LibraryError{
        tableTimer = new javax.swing.Timer(0, new ActionListener() {
                                               public void actionPerformed(ActionEvent evt) {

                                                   populateTable(getReleaseSelected());//#NullSel: must catch null selection object
                                                   fireMenuEnableEvent();  //TST2027:
                                                   tableTimer.stop();
                                               }
                                           });

        tableTimer.setRepeats(false);
        tableTimer.start();
    }

    private void populateTable(ReleaseInformation relInfo) {
        populateTable(relInfo, null);
    }

    private void populateTable(ReleaseInformation relInfo, Vector onlyShowThese) {

        String releaseConstant = getLibraryTextPrefix() + " Release: ";
        String headerstr = null;
        if (relInfo == null) {
            headerstr = "No Release Selected";
        } else {
            headerstr=releaseConstant+relInfo.getLibraryName()+" S/390 Release: "+relInfo.getMvsHighLevelQualifier()+"."+relInfo.getMvsName();
        }
        tableHeaderListBox.setToolTipText(headerstr);
        Vector listVector = new Vector();
        tableHeaderListBox.clearSelection();
        listVector.addElement(headerstr);
        tableHeaderListBox.setListData(listVector);
        Vector tableDataVector = new Vector();
        if (onlyShowThese!=null) {
            tableDataVector = onlyShowThese;
        } else if (relInfo!=null) {
            tableDataVector = createTableVectorsFromReleaseInformation(relInfo);
        }
        dataModel.setDataVector(tableDataVector,columnHeadings);
        if (tableDataVector.size()>0) {
            SearchedListOfDriversVector.removeAllElements();
            SearchedListOfDriversVector=(Vector)tableDataVector.clone();
            searchToolBarButton.setEnabled(true);
        } else {
            searchToolBarButton.setEnabled(false);
        }

        dataModel.fireTableDataChanged();
        initColumnSizes(driverInfoTable);

        //#DEF.TST1232: & TST1941
        if (!sorter.isSorting()) {
            sorter.setSortingStatus(0,SortableTableModel.ASCENDING);
        }

        repaint();
    }

    private Vector createTableVectorsFromReleaseInformation(ReleaseInformation relInfo) {
        Vector tableRowsVector = new Vector();
        for (Iterator driverIterator = relInfo.getDrivers().iterator(); driverIterator.hasNext();) {
            Vector oneRow = new Vector();
            DriverInformation driverInfo = (DriverInformation) driverIterator.next();
            String driverName = driverInfo.getName();
            if (driverInfo.isFullDriver()) {
                driverName +=" (FULL)";
            } else {
                if ((driverInfo.getBaseDriver()!=null | driverInfo.getExplicitBaseChain()!=null) & !driverInfo.isFullDriver()) {
                    driverName += " (DELTA)";
                }

            }
            oneRow.add(driverName);
            oneRow.add(createDriverBaseChain(driverInfo));
            tableRowsVector.add(oneRow);
        }
        return tableRowsVector;
    }

    private String createDriverBaseChain(DriverInformation driverInfo) {
        String driverBaseChain = "";
        if (driverInfo.getExplicitBaseChain()!=null) {
            for (Iterator baseIterator = driverInfo.getExplicitBaseChain().iterator(); baseIterator.hasNext();) {
                DriverInformation oneBase = (DriverInformation) baseIterator.next();
                if (driverBaseChain.length()>0) {
                    driverBaseChain += " - ";
                }
                driverBaseChain += createProperBaseDriverName(driverInfo, oneBase);
            }
        } else if (driverInfo.getBaseDriver()!=null) {
            driverBaseChain =  createProperBaseDriverName(driverInfo, driverInfo.getBaseDriver());
            if (driverInfo.getRelease().equals(driverInfo.getBaseDriver().getRelease())) {
                // so we don't recurse through other releases
                String baseChain = createDriverBaseChain(driverInfo.getBaseDriver());
                if (baseChain.trim().length()>0) {
                    driverBaseChain +=" - "+baseChain;
                }
            }
        }
        return driverBaseChain;
    }

    private String createProperBaseDriverName(DriverInformation mainDriver, DriverInformation baseDriver) {
        String baseName = "";
        if (!isSameRelease(mainDriver, baseDriver)) {//TST2517
            baseName = baseDriver.getRelease().getLibraryName()+".";
        }
        return baseName += baseDriver.getName();
    }

    //Begin TST2517
    private boolean isSameRelease(DriverInformation mainDriver, DriverInformation baseDriver) {

        ReleaseInformation mainRel = mainDriver.getRelease();
        ReleaseInformation baseRel = baseDriver.getRelease();

        return mainRel.getLibraryName().equals(baseRel.getLibraryName()) &
        mainRel.getMvsName().equals(baseRel.getMvsName()) &
        mainRel.getMvsHighLevelQualifier().equals(baseRel.getMvsHighLevelQualifier());
    }
    //End TST2517

    public boolean hasDependantDrivers(DriverInformation driverSelectedFromTable) {
        status.updateStatus("Checking dependency for driver " + driverSelectedFromTable.getName(), false);

        int col=  driverInfoTable.convertColumnIndexToModel(1);

        String testString;

        for (int row=0;row!=driverInfoTable.getRowCount();row ++) {
            testString=(String)driverInfoTable.getValueAt(row,col);

            if (testString.trim().equals(driverSelectedFromTable.getName().trim())) {
                return true;
            }
        }

        return false;
    }

    private boolean checkPossiblyNullValue(Object test) {
        if (test == null) {
            return false;
        }
        if (((String)test).trim().length()<1) {
            return false;
        }
        return true;
    }

    /** Update the Library setup info */ //#x
    private void setLibInfo() {
        lep.LogSecondaryInfo("Debug", "MBManagePage:setLibInfo:Entry");
        libraryAddressNameLabel.setText(build.getSetup().getLibraryInfo().getDescriptiveString());
    }

    /** Update the Library setup info */ //#x
    private String getLibInfo() {
        lep.LogSecondaryInfo("Debug", "MBManagePage:getLibInfo:Entry");
        return build.getSetup().getLibraryInfo().getDescriptiveString();
    }

    private MetadataType[] getMetadataTypes() {
        Hashtable tempHash = new Hashtable();
        try {
            if (getDriverSelected().getName() != null) {

                MBBuild tempBuild = build.getClone();

                tempBuild.setDriverInformation(getDriverSelected());

                tempBuild.setReleaseInformation(getReleaseSelected()); 

                MetadataReport metadataFieldReport = new MetadataReport(tempBuild,null,null, this);

                metadataFieldReport.setJustGetFields(true);

                metadataFieldReport.externalRun();

                return metadataFieldReport.getMetadataTypes();
            }
        } catch (MBBuildException mbe) {
            lep.LogException(mbe);
        }
        return null;
    }


    // 05/04/2000 pjs - Add ability to view config data
    // get the current config hash, and puill all sections, keywords and values from it
    /* public void viewConfig() throws LibraryError {
         String outdata = new String();
         Setup tsetup = build.getSetup();
         LibraryInfo libInfo = tsetup.getLibraryInfo();
         Map configHash = libInfo.getConfigurationAccess(getReleaseSelected().getLibraryName()).getAllConfigurationSettings();
         if (configHash != null) {
             Iterator realmKeys = configHash.keySet().iterator();
             while (realmKeys.hasNext()) {
                 String tempKey = (String) realmKeys.next();
                 outdata = outdata+tempKey+":\n";  // print section name
                 Map sectionHash = (Map) configHash.get(tempKey);
                 Iterator cdataKeys = sectionHash.keySet().iterator();
                 while (cdataKeys.hasNext()) {
                     tempKey = (String) cdataKeys.next();
                     outdata = outdata+"  "+tempKey+"="+(Map)sectionHash.get(tempKey)+"\n";
                 }
             }
         }
         if (outdata.length()<1) {
             outdata = "No configuration data found for release "+getReleaseSelected();
         }
         new MBMsgBox("Configuration Data for "+getReleaseSelected(), outdata);
     }
 
 */

    private void fireSetupChangeEvent(final Setup oldSetup, final Setup newSetup) {
        new SetupChanger(oldSetup,newSetup).doAction(new ActionEvent(thisFrame,ActionEvent.ACTION_PERFORMED,"SetupChanger"));
    }

    private void fireMenuEnableEvent() {
        if (getDriverSelected() != null) {
            firePropertyChange(BaseActionsBuilder.MANAGERELEASES_DRIVER_SELECTED,0,1);
        } else {
            firePropertyChange(BaseActionsBuilder.MANAGERELEASES_DRIVER_SELECTED,0,-1);
        }   

        if (getReleaseSelected()!=null) {
            firePropertyChange(BaseActionsBuilder.MANAGERELEASES_RELEASE_CHANGED,0,1);
        } else {
            firePropertyChange(BaseActionsBuilder.MANAGERELEASES_RELEASE_CHANGED,0,-1);
        } 
    }

    private ReleaseInformation getReleaseSelected() {
        ReleaseInformation releaseInfo = null;
        if (releaseObject.getElementSelected() !=null) {
            releaseInfo =  releaseObject.getSelectedRelease();
        }

        return releaseInfo;
    }

    private DriverInformation getDriverSelected() {
        if (dataModel.getRowCount()>0) {
            int selectedrow =driverInfoTable.getSelectedRow();
            if (selectedrow >= 0) {
                String driverStr = (String)driverInfoTable.getValueAt(driverInfoTable.getSelectedRow(), driverInfoTable.convertColumnIndexToModel(0));
                if (driverStr.indexOf("(") > 0) {
                    driverStr = driverStr.substring(0,driverStr.indexOf("("));
                }
                return(getReleaseSelected()!=null ? getReleaseSelected().getDriverByName(driverStr.trim()) : null);
            } else {
                return null;
            }
        } else {
            return null;
        }

    }


    public void tableChanged(TableModelEvent e) {
        fireMenuEnableEvent();
    }



    class MainframeLogRetrieveAction extends CancelableAction {
        MainframeLogRetrieveAction() {
            super("Driver Parts");
        }
        public void doAction(ActionEvent e) {
            MBBuild tempBuild = build.getClone();
            tempBuild.setDriverInformation(getDriverSelected());
            try {
                new com.ibm.sdwb.build390.MBLogRetrievePage(tempBuild);
            } catch (MBBuildException mbe) {
                getLEP().LogException(mbe);
            }
        }
    };

    class MainframeUsermodLogRetrieveAction extends CancelableAction {
        MainframeUsermodLogRetrieveAction() {
            super("Usermod Parts");
        }
        public void doAction(ActionEvent e) {
            MBBuild tempBuild = build.getClone();
            tempBuild.setDriverInformation(getDriverSelected());
            tempBuild.set_buildtype("USERMOD");
            new com.ibm.sdwb.build390.MBSmodRetrievePage(tempBuild);
        }
    };


    /** Process requests for Driver reports*/
    class DriverReportAction extends CancelableAction {
        private String reportType   = null;
        private JMenuItem menuItem  = null;
        private DriverReportHelper driverReportRunner;

        DriverReportAction(String tempType,JMenuItem tempMenuItem) {
            super(tempType);
            reportType = tempType;
            this.menuItem = tempMenuItem;
        }

        public void doAction(ActionEvent e) {
            DriverReportHelper driverReportRunner =  new DriverReportHelper(reportType);
            driverReportRunner.setIncludeLibraryPathName(menuItem.isSelected());
            driverReportRunner.executeReport();
        }


        class DriverReportHelper {
            private boolean includeLibraryPathName =false;
            private String reportType ="";

            private DriverReportHelper(String reportType) {
                this.reportType =reportType;
            }

            void setIncludeLibraryPathName(boolean includeLibraryPathName) {
                this.includeLibraryPathName = includeLibraryPathName;
            }

            void executeReport() {
                MBBuild tempBuild = build.getClone();
                tempBuild.setDriverInformation(getDriverSelected());

                AbstractProcess processToRun = null;
                if (reportType.equals("USERMOD")) {
                    SMODReport smodReportGetter = new SMODReport(tempBuild, reportType, parentWindow);
                    smodReportGetter.setJustGetReport(true);
                    smodReportGetter.setIncludePathname(includeLibraryPathName);
                    smodReportGetter.setShowFilesAfterRun(true, false);
                    processToRun = smodReportGetter;
                } else {
                    DriverReport driverReportGetter = new DriverReport(tempBuild.getDriverInformation(), tempBuild.getSetup().getMainframeInfo(), tempBuild.getSetup().getLibraryInfo(), new java.io.File(tempBuild.getBuildPath()), parentWindow);
                    driverReportGetter.setJustGetReport(true);
                    driverReportGetter.setIncludePathname(includeLibraryPathName);
                    if (reportType.length()>0 && driverReportHostMap.containsKey(reportType)) {
                        driverReportGetter.setSummaryType((String) driverReportHostMap.get(reportType));
                    } else {
                        driverReportGetter.setSummaryType(null);
                    }
                    driverReportGetter.setShowFilesAfterRun(true, false);
                    processToRun = driverReportGetter;
                }
                CancelableProcess processRunner = new CancelableProcess(processToRun, parentWindow);
                processRunner.run();
            }
        }

    };


    class CreateShadowAction extends CancelableAction {
        CreateShadowAction() {
            super("Release");
        }
        public void doAction(ActionEvent e) {
            try {
                MBBuild tempBuild = build.getClone();
                getStatus().updateStatus("Getting  MVS Site Defaults ",false);
                ReleaseAndDriverParameters releaseParameters = new ReleaseAndDriverParameters();

                String highLevelQual="";

                if (getReleaseSelected() != null) {
                    highLevelQual = getReleaseSelected().getMvsHighLevelQualifier();
                }

                // get site defaults
                GetMVSSiteDefaults siteDefaults = new GetMVSSiteDefaults(build.getSetup(), thisFrame);
                siteDefaults.externalRun();
                com.ibm.sdwb.build390.mainframe.ReleaseAndDriverParameters defaultSettings = siteDefaults.getDefaultSettingsForMVS();

                //begin TST2430
                if (defaultSettings == null) {
                    throw new GeneralError("There has been an error getting default settings for MVS");
                }
                //end TST2430

                NewShadowDialog nsd = new NewShadowDialog(tempBuild, "SHADOW", false, false, null, highLevelQual, null, null, releaseParameters, thisFrame);
                nsd.initializeDefaults(defaultSettings);
                if (nsd.wasInitialized()) {
                    getStatus().updateStatus("Preparing to create release " + nsd.getMVSRelease() ,false);
                    ReleaseInformation  newRelease = new ReleaseInformation(nsd.getLibraryProject(),  nsd.getMVSRelease(), releaseParameters.getHighLevelQualifier());
                    com.ibm.sdwb.build390.process.CreateMVSRelease releaseCreator = new com.ibm.sdwb.build390.process.CreateMVSRelease(tempBuild.getSetup(), tempBuild.getSetup().getLibraryInfo(), newRelease, releaseParameters, parentWindow);
                    setCancelButtonStatus(false);
                    releaseCreator.externalRun();
                    String releaseToSelect =newRelease.getLibraryName();
                    releaseObject.refreshCombo();
                    if (releaseToSelect != null) {
                        releaseObject.select(releaseToSelect);
                    }
                }
            } catch (MBBuildException mbe) {
                lep.LogException(mbe);
            }
        }
    };

    class CreateMVSDriverAction extends CancelableAction {

        CreateMVSDriverAction() {
            super("Driver");
        }

        public void doAction(ActionEvent e) {
            try {
                Hashtable cmdHash = new Hashtable();
                // if a driver is selected in the tree use it as the initial base
                DriverInformation driver = getDriverSelected();
                if (driver!=null) {
                    cmdHash.put("IBASE", driver.getName()); // newdrivertype dialog removes this from the hash table
                }
                // ask user about base
                MBBuild tempBuild = build.getClone();

                MBNewDriverTypeDialog ndtd = new MBNewDriverTypeDialog(tempBuild, cmdHash, thisFrame, null);
                if (!cmdHash.isEmpty()) {
                    if (getReleaseSelected().getMvsName() != null) {
                        String aparName = null;
                        if ( (new String("APAR")).equals(cmdHash.get("DRVRTYPE"))) {
                            aparName = (String) cmdHash.get("CNEWDRVR");
                        }
                        ReleaseAndDriverParameters parameters = new ReleaseAndDriverParameters();
                        getStatus().updateStatus("Getting  MVS Site Defaults ",false);
                        GetMVSSiteDefaults siteDefaults = new GetMVSSiteDefaults(build.getSetup(),thisFrame);
                        siteDefaults.externalRun();
                        com.ibm.sdwb.build390.mainframe.ReleaseAndDriverParameters defaultSettings = siteDefaults.getDefaultSettingsForMVS();

                        //begin TST2430
                        if (defaultSettings == null) {
                            throw new GeneralError("There has been an error getting default settings for MVS");
                        }
                        //end TST2430
                        NewShadowDialog nsd = new NewShadowDialog(tempBuild, "DRIVER", checkPossiblyNullValue(cmdHash.get("BASE")), 
                                                                  (new String("DELTA")).equals(cmdHash.get("TYPE")),aparName,getReleaseSelected().getMvsHighLevelQualifier(),
                                                                  getReleaseSelected().getMvsName(), (String) cmdHash.get("BASE"),parameters, thisFrame);
                        nsd.initializeDefaults(defaultSettings);
                        if (nsd.wasInitialized()) {
                            getStatus().updateStatus("Preparing to create driver " + nsd.getDriver() ,false);
                            DriverInformation newDriver = new DriverInformation(nsd.getDriver());
                            String baseDriverName = nsd.getBaseDriver();
                            String baseReleaseName = getReleaseSelected().getMvsName();
                            DriverInformation baseDriver = null;
                            ReleaseInformation baseRelease = null;
                            if (baseDriverName!=null) {
                                if (baseDriverName.indexOf(".")>0) {
                                    baseReleaseName = baseDriverName.substring(0, baseDriverName.indexOf("."));
                                    baseDriverName = baseDriverName.substring(baseDriverName.indexOf(".")+1);
                                }
                                ReleaseInformation tempSourceRelease = tempBuild.getSetup().getMainframeInfo().getReleaseByMVSName(baseReleaseName,tempBuild.getSetup().getLibraryInfo());
                                baseDriver = tempSourceRelease.getDriverByName(baseDriverName);
                            }
                            baseRelease = tempBuild.getSetup().getMainframeInfo().getReleaseByMVSName(nsd.getMVSRelease(),tempBuild.getSetup().getLibraryInfo());
                            newDriver.setFull(nsd.isFullDelta());
                            if (nsd.isDelta()) {
                                newDriver.setBaseDriver(baseDriver);
                            }
                            newDriver.setReleaseInfomation(baseRelease);
                            com.ibm.sdwb.build390.process.CreateMVSDriver driverCreate = new com.ibm.sdwb.build390.process.CreateMVSDriver(tempBuild,baseDriver, newDriver,nsd.isDelta(), parameters, thisFrame);
                            if (cmdHash.get("SYSMODS")==null) {
                                driverCreate.setIncludeSysMods(false);
                            } else {
                                driverCreate.setIncludeSysMods(cmdHash.get("SYSMODS").equals("YES"));
                            }
                            if (nsd.isOverrideSet()) { /* PTM4535  */
                                driverCreate.setDriverSize(null);
                            } else {
                                driverCreate.setDriverSize(nsd.getDriverSize());
                            }
                            driverCreate.setOverrideDefaultSettings(nsd.isOverrideSet());
                            setCancelButtonStatus(false);
                            driverCreate.externalRun();

                            status.updateStatus("Driver creation successful",false);
                            releaseObject.select(baseRelease.getLibraryName());

                            populateReleasesAction.actionPerformed(new ActionEvent(thisFrame,ActionEvent.ACTION_PERFORMED,"fetch"));
                        }
                    }
                    //}
                }
            } catch (MBBuildException mbe) {
                lep.LogException(mbe);
            }
        }

    };



    class DeleteShadowAction extends CancelableAction {

        DeleteShadowAction() {
            super("Release");
        }
        public void doAction(ActionEvent e) {
            try {
                ReleaseInformation releaseChosen = getReleaseSelected();
                if (tableHeaderListBox.getSelectedIndex()>-1) {
                    // Release delete
                    // Make sure there are no drivers defined for this release
                    if (!releaseChosen.getDrivers().isEmpty())
                        new MBMsgBox("Error", "This Release cannot be deleted because it has dependent drivers.");
                    else {
                        MBMsgBox viewQuestion = new MBMsgBox("Warning", "You are about to delete "+releaseChosen.getLibraryName()+"\nAre you sure you want to proceed?", null, true);
                        if (viewQuestion.isAnswerYes()) {
                            DeleteMVSRelease releaseDelete = new DeleteMVSRelease(build.getSetup(),releaseChosen, thisFrame);
                            setCancelButtonStatus(false);
                            releaseDelete.externalRun();
                            build.getSetup().getMainframeInfo().removeRelease(releaseChosen, build.getSetup().getLibraryInfo());
                            releaseObject.deleteItem(releaseChosen.getLibraryName());
                            populateReleasesAction.doAction(new ActionEvent(thisFrame,ActionEvent.ACTION_PERFORMED,"PopulateTable"));
                        }
                    }
                } else {
                    new MBMsgBox("Information", "Please Select the Release Before Delete Action.");
                }
            } catch (MBBuildException mbe) {
                lep.LogException(mbe);
            }
        }
    };



    class DeleteDriverAction extends CancelableAction {

        DeleteDriverAction() {
            super("Driver");
        }
        public void doAction(ActionEvent e) {
            try {
                DriverInformation deadDriver = getDriverSelected();

                // check for (FULL) at end of stext and strip it
                // ask user if he is sure
                // setup for call to deletedriver
                if (deadDriver!=null) {
                    if (hasDependantDrivers(getDriverSelected())) {
                        new MBMsgBox("Error", "This Driver cannot be deleted because it has dependent drivers.");
                    } else {

                        com.ibm.sdwb.build390.process.ProcessWrapperForSingleStep driverReportWrapper = new com.ibm.sdwb.build390.process.ProcessWrapperForSingleStep(thisFrame);
                        build.setDriverInformation(deadDriver);
                        com.ibm.sdwb.build390.process.steps.DriverReport driverReportStep = new com.ibm.sdwb.build390.process.steps.DriverReport(build.getDriverInformation(), build.getSetup().getMainframeInfo(), build.getSetup().getLibraryInfo(), new java.io.File(build.getBuildPath()), driverReportWrapper);
                        driverReportStep.setAlwaysRun(true);
                        driverReportStep.setCheckForHLQAndDriver(true);
                        driverReportStep.setForceNewReport(true);
                        driverReportStep.setSummaryType("ONLY");
                        driverReportWrapper.setStep(driverReportStep);
                        setCancelButtonStatus(false);
                        driverReportWrapper.externalRun();  

                        MBBuild tempBuild = new MBBuild(driverReportStep.getParser().getBuildid(),getLEP()); //doesn't like a good idea, just to compare a build object is created.
                        tempBuild.setLocked(driverReportStep.getParser().getBuildid());
                        driverReportStep.getParser().doDriverLockCheck(tempBuild);

                        MBMsgBox viewQuestion = new MBMsgBox("Warning", "You are about to delete driver "+deadDriver.getName()+"\nAre you sure you want to proceed?", parentFrame, true);
                        if (viewQuestion.isAnswerYes()) {
                            DeleteMVSDriver releaseDelete = new DeleteMVSDriver(build.getSetup().getMainframeInfo(), build.getBuildPath(),  deadDriver, thisFrame);
                            releaseDelete.externalRun();
                            releaseObject.select(deadDriver.getRelease().getLibraryName());
                            populateReleasesAction.doAction(new ActionEvent(thisFrame,ActionEvent.ACTION_PERFORMED,"PopulateTable"));
                        }
                    }
                } else {
                    new MBMsgBox("Information", "Please Select a  Driver before performaing Delete Action.");
                }
            } catch (MBBuildException mbe) {
                lep.LogException(mbe);
            }
        }
    };

    class CheckAllMVSMetadataValidityAction extends CancelableAction {
        CheckAllMVSMetadataValidityAction() {
            super("Validate ALL Metadata");
        }

        public void doAction(ActionEvent e) {
            String syntaxerrors = new String("SYNTAX");
            while (syntaxerrors.equals("SYNTAX")) {
                Hashtable tempHash = new Hashtable();
                try {
                    if ( getDriverSelected() != null) {
                        MBBuild tempBuild = build.getClone();
                        tempBuild.setDriverInformation(getDriverSelected());
                        CheckMVSMetadataValidity metadataCheck = new CheckMVSMetadataValidity(tempBuild,new Vector(), thisFrame);
                        metadataCheck.externalRun();
                    }
                } catch (MBBuildException mbe) {
                    lep.LogException(mbe);
                }
                syntaxerrors = (String)tempHash.get("ERROR");
                if (syntaxerrors == null) {
                    syntaxerrors = "NONE";
                }
            }
        }
    };


    class FilterByMetadataAction extends CancelableAction {
        FilterByMetadataAction() {
            super("Filter By Metadata");
        }
        public void doAction(ActionEvent e) {
            try {
                if (getDriverSelected() != null) {
                    MBBuild tempBuild = build.getClone();
                    tempBuild.setDriverInformation(getDriverSelected());
                    Vector initVect = (Vector) getGeneric(METADATAVALIDATIONREPORTKEYWORD);

                    if (initVect == null) {
                        initVect = new Vector();
                    }

                    if (metadataTypes==null) {//if list not avail
                        metadataTypes= getMetadataTypes();//get a fresh list
                        if (metadataTypes==null) {
                            return;
                        }
                    }

                    MetadataListEntryDialog searchCriteria =
                    new MetadataListEntryDialog(initVect, thisFrame, 67, false, metadataTypes);

                    Vector userCriteria = new Vector();
                    userCriteria = searchCriteria.getEntries();

                    if (userCriteria != null) {
                        putGeneric(METADATAVALIDATIONREPORTKEYWORD, userCriteria);

                        CheckMVSMetadataValidity metadataCheck = new CheckMVSMetadataValidity(tempBuild, userCriteria, thisFrame);
                        metadataCheck.externalRun();
                    }
                }
            } catch (MBBuildException mbe) {
                lep.LogException(mbe);
            }
        }
    };



    /** Process requests for Shadow reports*/
    class ShadowReportAction extends CancelableAction {

        com.ibm.sdwb.build390.process.management.Haltable stop = null;

        ShadowReportAction() {
            super("Shadow Report");
        }

        /* the method to override for whatever action you want to perform in response
        to a click.                                  		*/
        public void doAction(ActionEvent e) {
            if (getReleaseSelected()!=null) {
                ShadowReport reportGetter = new ShadowReport(build.getSetup(), getReleaseSelected().getLibraryName(), parentWindow); 
                stop = reportGetter;
                reportGetter.setShowFilesAfterRun(true, false);
                reportGetter.run();
            } else {
                problemBox("Shadow Report Error!","Please enter a valid library release.");
                return;
            }
        }

        public void stop() throws com.ibm.sdwb.build390.MBBuildException{
            if (stop !=null) {
                stop.haltProcess();
            }
        }
    };

    class ConfigAdminAction extends CancelableAction {

        ConfigAdminAction(String name) {
            super(name);
        }
        /**
         * Invoked when an action occurs.
         */
        public void doAction(ActionEvent e) {
        	if(getReleaseSelected()!=null){//Begin TST3459A
        		com.ibm.sdwb.build390.configuration.ConfigurationAdministrationPanel configAdminPanel = new com.ibm.sdwb.build390.configuration.ConfigurationAdministrationPanel(releaseObject.getSelectedRelease().getLibraryName(), build.getSetup().getLibraryInfo(),build.getSetup().getMainframeInfo(), getLEP());
        	} else {
        		new MBMsgBox("Config Admin Error!","Please enter a valid library release.");
        	}//End TST3459A
        }
    };

    class ViewConfigurationAction extends CancelableAction {
        ViewConfigurationAction() {
            super("View Configuration");
        }
        public void doAction(ActionEvent evt) {
        	if(getReleaseSelected()!=null){//Begin TST3459
        		try {
                    getStatus().updateStatus("Getting configuration data", false);
                    // 05/04/2000 pjs - Add ability to view config data
                    // get the current config hash, and puill all sections, keywords and values from it
                    String outdata = new String();
                    Setup tsetup = build.getSetup();
                    LibraryInfo libInfo = tsetup.getLibraryInfo();
                    Map configHash = libInfo.getConfigurationAccess(getReleaseSelected().getLibraryName(), false).getAllConfigurationSettings();
                    if (configHash != null) {
                        Iterator realmKeys = configHash.keySet().iterator();
                        while (realmKeys.hasNext()) {
                            String tempKey = (String) realmKeys.next();
                            outdata = outdata+tempKey+":\n";  // print section name
                            Map sectionHash = (Map) configHash.get(tempKey);
                            Iterator cdataKeys = sectionHash.keySet().iterator();
                            while (cdataKeys.hasNext()) {
                                tempKey = (String) cdataKeys.next();
                                outdata = outdata+"  "+tempKey+"="+(Map)sectionHash.get(tempKey)+"\n";
                            }
                        }
                    }
                    if (outdata.length()<1) {
                        outdata = "No configuration data found for release "+getReleaseSelected().getLibraryName();
                    }
                    new MBMsgBox("Configuration Data for "+getReleaseSelected().getLibraryName(), outdata);
                } catch (LibraryError le) {
                    getStatus().updateStatus("Error getting configuration data", false);
                    new MBMsgBox("Error getting configuration data", le.toString());
                }
                getStatus().clearStatus();
        	} else {
        		new MBMsgBox("View Configuration Error!","Please enter a valid library release.");
        	}//End TST3459
        }
    };

    class SetupAction extends CancelableAction {

        private Setup newSetup =null;
        private Setup oldSetup =null;

        SetupAction() {
            super("Setup");
        }

        public void doAction(ActionEvent e) {
            oldSetup = build.getSetup();
            String oldLibrary = oldSetup.getLibraryInfo().getDescriptiveString(); 
            String oldMainframe = oldSetup.getMainframeInfo().getMainframeAddress();
            com.ibm.sdwb.build390.userinterface.graphic.panels.setup.SetupInformation tempSetupDialog = new com.ibm.sdwb.build390.userinterface.graphic.panels.setup.SetupInformation(thisFrame,internalSetupList);
            build.setSetup(tempSetupDialog.getSetup());
            setLibInfo();
            newSetup = build.getSetup();

            String newLibrary = newSetup.getLibraryInfo().getDescriptiveString();
            String newMainframe = newSetup.getMainframeInfo().getMainframeAddress();

            if (!oldLibrary.equals(newLibrary)
                || !oldMainframe.equals(newMainframe)) {
                internalSetupList.add(build.getSetup());
            }

            fireSetupChangeEvent(oldSetup,newSetup);

        }


    };


//this is a very old code. We need to re write it so we use standard regex search, and MBSearchPart should be cleaned up.
    class FindAction extends CancelableAction {
        FindAction( ) {
            super("Find");
        }

        public void doAction(ActionEvent e) {
            try {

                MBSearchForPart mp=new MBSearchForPart(sorter.getSorteDataVector().elements() ,thisFrame,getStatus(),lep,true);
                boolean isFirstHit=false,isRebuild = false;
                Vector RebuildDisplayDriverVector = mp.getMatchedPartsVector();
                SortedMap FindNextHashMap = mp.getFindNextHashMap();
                String SearchTitle = mp.getSearchTitle();
                String DriverNameToFind = mp.getNameToFind();

                //:) too ugly ... kishore - need to clean it up .
                if (mp.getDisplayOption()=='F') {
                    isFirstHit=true;
                } else {
                    isRebuild=true;
                }



                String statstr = new String();
                if (RebuildDisplayDriverVector.size()>0 |FindNextHashMap.size()>0) {
                    if (isRebuild) {
                        cbReloadDisplay.setEnabled(true);
                        reloadToolBarButton.setEnabled(true);
                        populateTable(getReleaseSelected(), RebuildDisplayDriverVector);
                        driverTitledBorder.setTitle("Filtered Drivers Table");
                    }

                    if (isFirstHit) {
                        Iterator keys = FindNextHashMap.keySet().iterator();
                        int j = ((Integer)keys.next()).intValue();
                        driverInfoTable.setRowSelectionAllowed(true);
                        int sortIndex = sorter.modelIndex(j);
                        driverInfoTable.setRowSelectionInterval(j,j);
                        jsp.getViewport().setViewPosition(driverInfoTable.getCellRect(j,0,true).getLocation());
                        repaint();
                        if (keys.hasNext()) {
                            new MBLogRetrieveFindNextMatch(thisFrame,lep,driverInfoTable,null,keys,SearchTitle,null,null,getStatus());
                        }

                    }
                    getStatus().updateStatus("Find Complete...  ",false);
                } else {
                    getStatus().updateStatus("Find Complete...  No Matches Found. ",false);
                    if ((DriverNameToFind!=null)) {
                        new MBMsgBox("Find:","No Matches Found For Driver Name : "+ DriverNameToFind);

                    }
                }

                revalidate();
                repaint();

            } catch (MBBuildException mbe) {
                lep.LogException(mbe);
            }
        }

    };



    class ReloadDisplayAction extends CancelableAction {
        ReloadDisplayAction() {
            super("Reload Display");
        }
        public void doAction(ActionEvent evt) {
            try {
                populateTable(getReleaseSelected());
                driverTitledBorder.setTitle("Drivers Table");
                revalidate();
                repaint();

            } catch (Exception e) {
                lep.LogException("Error Occurred : ",e);
            }
        }
        public void postAction() {
            cbReloadDisplay.setEnabled(false);
            reloadToolBarButton.setEnabled(false);
        }
    };

    class PopulateReleasesAction extends com.ibm.sdwb.build390.userinterface.graphic.widgets.CancelableAction {

        PopulateReleasesAction() {
            super("Retrieve Releases from mainframe");
        }
        /* the method to override for whatever action you want to perform in response
        to a click.
        */
        public void doAction(ActionEvent e) {
            try {

                if (getReleaseSelected()!=null) {
                    handleReleaseSelection();
                } else {
                    releaseObject.initData(null);
                    populateTable(null);
                }
            } catch (LibraryError le) {
                getStatus().updateStatus("Metadata configuration checking failed.",false);
                lep.LogException(le);
            }
        }

        public void stop() throws MBBuildException{
            // if (stopObject !=null) {
            //      stopObject.haltProcess();
            //  }
        }


    };


    /*TST1925 */
    class ChangeReleaseItemListener implements  java.awt.event.ItemListener {
        /**
         * Invoked when an item has been selected or deselected by the user.
         * The code written for this method performs the operations
         * that need to occur when an item is selected (or deselected).
         */
        public void itemStateChanged(java.awt.event.ItemEvent e) {
            if (releaseObject.getSelectedRelease() !=null) {
                final String release = releaseObject.getSelectedRelease().getLibraryName().trim();
                if (release.length()>0) {
                    if (e.getStateChange()==java.awt.event.ItemEvent.SELECTED) {
                        SwingUtilities.invokeLater(new Runnable() {
                                                       public void run() {
                                                           populateReleasesAction.actionPerformed(new ActionEvent(thisFrame,ActionEvent.ACTION_PERFORMED,"fetch"));
                                                       }
                                                   });
                    }
                }
            }
        }
    };



    MouseListener searchTableMouseListener = new MouseAdapter() {
        public void mouseReleased(MouseEvent e) {
            if (e.isPopupTrigger()) {
                JPopupMenu menu = new JPopupMenu("Search");
                JMenuItem FindDriver = new JMenuItem("Find Driver");
                FindDriver.addActionListener(findAction);
                menu.add(FindDriver);
                menu.show(e.getComponent(),e.getX(),e.getY());
            }
            tableHeaderListBox.clearSelection();
            fireMenuEnableEvent();
        }
        public void mouseClicked(MouseEvent e) {
            tableHeaderListBox.clearSelection();
            fireMenuEnableEvent();
        }
    };

    MouseListener mouseListener = new MouseAdapter() {
        public void mouseClicked(MouseEvent e) {
            driverInfoTable.getSelectionModel().clearSelection();
            fireMenuEnableEvent();
            firePropertyChange(BaseActionsBuilder.MANAGERELEASES_RELEASE_SELECTED,0,1);

        }
        public void mouseReleased(MouseEvent e) {
            driverInfoTable.getSelectionModel().clearSelection();
            fireMenuEnableEvent();
            firePropertyChange(BaseActionsBuilder.MANAGERELEASES_RELEASE_SELECTED,0,1);
        }


    };

    private class SetupChanger extends CancelableAction {
        private Setup oldValue;
        private Setup newValue;

        private SetupChanger(Setup oldValue,Setup newValue) {
            super("Change Setup");
            this.oldValue = oldValue;
            this.newValue = newValue;
        }
        public void doAction(ActionEvent e) {
            String oldLibrary = oldValue.getLibraryInfo().getDescriptiveStringForMVS();
            String newLibrary = newValue.getLibraryInfo().getDescriptiveStringForMVS();
            String oldMainframe = oldValue.getMainframeInfo().getMainframeAddress();
            String newMainframe = newValue.getMainframeInfo().getMainframeAddress();
            if (!oldLibrary.equals(newLibrary)
                || !oldMainframe.equals(newMainframe)) {
                releaseObject.setMainframeAndLibrary(build.getSetup().getMainframeInfo(), build.getSetup().getLibraryInfo());
                releaseObject.refreshCombo();
            }
        }
    }


    private class ManageReleasesActionConfigurer implements com.ibm.sdwb.build390.userinterface.graphic.actions.ActionConfigurer {

        public JMenuBar getMenuBar() {
            return getJMenuBar();
        }
        public java.awt.Component getFrame() {
            return thisFrame;
        }


        public MBStatus getStatusHandler() {
            return getStatus();
        }
        public LogEventProcessor getLEP() {
            return getLEP();
        }
        public void handleUIEvent(com.ibm.sdwb.build390.userinterface.event.UserInterfaceEvent event) {
        }

        public String toString() {
            return "ManageReleases Action Configurer : " + ((MBInternalFrame)getFrame()).getTitle();
        }
    }


    // FixMinSize
    public Dimension getMinimumSize() {
        Dimension oldPref = new Dimension(360, 320);
        return oldPref;
    }
    public Dimension getPreferredSize() {
        Dimension prefSize = new Dimension(360,420);
        return prefSize;
    }

    public void dispose() {
        if (isIcon()) {
            try {
                setIcon(false);
            } catch (java.beans.PropertyVetoException pve) {
                lep.LogException("There was an error restoring the window so it can be disposed", pve);
            }
        }
        putGeneric(DEFAULTKEY, familyReleaseDefaults);
        super.dispose();
    }


    private String getLibraryTextPrefix() {
        return MBClient.getCommandLineSettings().getMode().getCategory();
    }



}
