package com.ibm.sdwb.build390.userinterface.graphic.panels.metadata;
/*********************************************************************/
/* MetadataEditPartsChooserPage  class for the Build/390 client      */
/*                (Old wine in new bottle)                           */   
/*  Creates the initial part chooser list.                           */
/*  Creates the built parts,unbuilt parts,new parts and all parts tab*/
/*********************************************************************/
/*Defects fixed in MBMetadataChooserPage are,
//09/30/99    pjs -    Fix help link
//01/07/2000           indivdual build log file changes
//03/07/2000           changes to implement the log changes using listeners
//04/19/2001 #MetaOperEnhance: Enhance metadata operations/add filter
//03/13/2002 INT0835   minor gui changes to rename the 'Get' with 'List.
//                     Enable Edit button only when there is some data in the table.
//03/17/2002 INT0835   add one more column to display if the part has 'Editable Metadata or not'
//12/03/2002 SDWB-2019 Enhance the help system
//02/18/2003 SDWB2058: Need easier way to update metadata for parts w/long cmvc names 
//04/07/2002 TST1154:  SDWB REQ 2058-----need sortable columns
//09/03/2004 INT1980   Edit button should be enabled, when a row is selected.
/*********************************************************************/
//02/11/2005 SDWB2363  Redesign part chooser interface.
//02/25/2005 SDWB2393  Relocate Part metadata editor.
//03/22/2005 TST2143   MDE index out of bounds.
/*********************************************************************/

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.sql.Driver;
import java.util.*;
import java.util.regex.PatternSyntaxException;

import javax.swing.*;
import javax.swing.border.LineBorder;
import javax.swing.border.TitledBorder;

import com.ibm.sdwb.build390.*;
import com.ibm.sdwb.build390.filter.DefaultFilter;
import com.ibm.sdwb.build390.filter.EliminateDuplicatesFilter;
import com.ibm.sdwb.build390.filter.Filter;
import com.ibm.sdwb.build390.filter.MultiCriteriaFilter;
import com.ibm.sdwb.build390.filter.RegularExpressionFilter;
import com.ibm.sdwb.build390.filter.ReplaceableFilter;
import com.ibm.sdwb.build390.filter.criteria.EliminateDuplicatesCriteria;
import com.ibm.sdwb.build390.filter.criteria.MultiFilterCriteria;
import com.ibm.sdwb.build390.help.HelpTopicID;
import com.ibm.sdwb.build390.info.FileInfo;
import com.ibm.sdwb.build390.info.InfoForMainframePartReportRetrieval;
import com.ibm.sdwb.build390.library.LibraryInfo;
import com.ibm.sdwb.build390.library.cmvc.metadata.server.MetadataQueryUtilities;
import com.ibm.sdwb.build390.library.userinterface.SourceSelection;
import com.ibm.sdwb.build390.logprocess.LogEventProcessor;
import com.ibm.sdwb.build390.metadata.filter.EliminateDuplicatePartsCriteria;
import com.ibm.sdwb.build390.metadata.filter.FindLibraryPartNameCriteria;
import com.ibm.sdwb.build390.metadata.filter.LibraryMetadataFilterCriteria;
import com.ibm.sdwb.build390.metadata.filter.LibraryMetadataReplaceHandler;
import com.ibm.sdwb.build390.metadata.filter.MetadataCriteriaGenerator;
import com.ibm.sdwb.build390.metadata.filter.NewlyCreatedPartsFilterCriteria;
import com.ibm.sdwb.build390.metadata.filter.RebuildNeededPartsFilterCriteria;
import com.ibm.sdwb.build390.metadata.info.GeneratedMetadataInfo;
import com.ibm.sdwb.build390.metadata.utilities.MetadataKeywordsMapper;
import com.ibm.sdwb.build390.metadata.utilities.MetadataValueGenerator;
import com.ibm.sdwb.build390.process.ProcessWrapperForSingleStep;
import com.ibm.sdwb.build390.process.library.metadata.FindMetadataInHostAndLibrary;
import com.ibm.sdwb.build390.process.library.metadata.FindMetadataInLibrary;
import com.ibm.sdwb.build390.process.steps.library.metadata.ReplaceMetadataInLibrary;
import com.ibm.sdwb.build390.user.SetupManager;
import com.ibm.sdwb.build390.userinterface.event.SelectionUpdateEvent;
import com.ibm.sdwb.build390.userinterface.event.UserInterfaceEvent;
import com.ibm.sdwb.build390.userinterface.event.UserInterfaceEventListener;
import com.ibm.sdwb.build390.userinterface.event.build.DriverUpdateEvent;
import com.ibm.sdwb.build390.userinterface.event.build.ReleaseUpdateEvent;
import com.ibm.sdwb.build390.userinterface.graphic.widgets.CancelableAction;
import com.ibm.sdwb.build390.userinterface.graphic.widgets.ComboBoxWithHistory;
import com.ibm.sdwb.build390.userinterface.graphic.widgets.HelpAction;
import com.ibm.sdwb.build390.userinterface.graphic.widgets.MainframeReleaseAndDriverSelectionPanel;
import com.ibm.sdwb.build390.userinterface.graphic.widgets.SimpleFrameWithSearchOptions;
import com.ibm.sdwb.build390.userinterface.graphic.widgets.UserSelection;



/** Create the Metadata chooser page */
public class MetadataEditorFrame  extends MBInternalFrame implements  Observer {


    private  MBBuild  build;
    private MetadataKeywordsMapper keywordsMap = null; 


    private JTabbedPane tab = new JTabbedPane(JTabbedPane.TOP,JTabbedPane.WRAP_TAB_LAYOUT);

    private JButton bEdit;
    private final JMenu replaceMenu=new JMenu("Replace Functions");
    private final JMenuItem partsInLibraryMenuItem = new JMenuItem("List Parts From Library");
    private final JMenuItem modelPartsMenuItem = new JMenuItem("Model After A Part (Use Part As Model)");
    private final JCheckBox warnMeIfPartsNotInLibrary = new JCheckBox("Warn me if part is not in library");
    private JButton driverParts = null;
    private JButton libraryParts = null;

    private ModelPartsAction        modelPartsAction;
    private EditMetadataAction      editMetadataAction;
    private ListPartsInDriverAction listPartsInDriverAction; 
    private ListPartsInLibraryAction listPartsInLibraryAction; 
    private CloseAllFilterTabsAction closeAllFilterTabsAction; 
    private MetadataFieldsAction  metadataFieldsAction;
    private ClearTabDataAction clearTabDataAction;
    private FilterByLibraryMetadataAction filterByLibraryMetadataAction;
    private FilterByLibraryNameAction filterByLibraryNameAction;
    private FilterByHostAndLibraryMetadataAction filterByHostAndLibraryMetadataAction;


    private UITableModelCommunicator  driverPartsCompositor  = null;
    private UITableModelCommunicator  libraryPartsCompositor   = null;
    private UITableModelCommunicator  allPartsCompositor     = null;
    private UITableModelCommunicator  newOrUnbuiltCompositor = null;


    private ComboBoxWithHistoryFactory comboHistoryMap  = new ComboBoxWithHistoryFactory();

    private static final String METADATAFILTERKEYWORD = "MAINMETADATAFILTERKEYWORD";
    private static final String MAINMETADATACHOOSERPAGE     ="MAINMETADATACHOOSERPAGE";


    private LibraryInfo libInfo;
    private MBMainframeInfo mainInfo;

    private MainframeReleaseAndDriverSelectionPanel mainframeSelectionPanel = null;
    private SourceSelection sourceSelectionPanel = null;



    /**
    * constructor - Create a MainMetadataChooserPage
    */
    public MetadataEditorFrame() throws MBBuildException  {
        super("Metadata Editor" , true, null);
        build = new MBBuild("M",MBConstants.METADATABUILDDIRECTORY,lep);
        build.setSetup(SetupManager.getSetupManager().createSetupInstance());
        libInfo = build.getLibraryInfo();
        this.mainInfo=build.getMainframeInfo();
        lep.addEventListener(build.getLogListener()); 
        setTitle(getTitle() + "("+build.get_buildid()+")");
        layoutDialog();
    }


    private void layoutDialog() throws MBBuildException {

        JPanel centerPanel = new JPanel(new BorderLayout());

        getContentPane().add("Center", centerPanel);     

        initializeActions();


        JButton bHelp = new JButton("Help");
        bHelp.addActionListener(new MBCancelableActionListener(thisFrame) {
                                    public void doAction(ActionEvent evt) {
                                        MBUtilities.ShowHelp("HDREDITOR",HelpTopicID.MAINMETADATAPARTEDITPANEL_HELP);
                                    }
                                });


        Vector actionButtons = new Vector();
        actionButtons.addElement(bEdit);
        addButtonPanel(bHelp, actionButtons);    


        initializeTabUI();


        modelPartsMenuItem.setEnabled(false);

        JPanel topPanel = new JPanel();
        GridBagLayout gridBag =   new GridBagLayout();
        GridBagConstraints c = new GridBagConstraints();
        topPanel.setLayout(gridBag);
        topPanel.setBorder(BorderFactory.createTitledBorder(LineBorder.createGrayLineBorder() ,"Library Address:" + libInfo.getProcessServerName()+"@"+libInfo.getProcessServerAddress(),TitledBorder.DEFAULT_JUSTIFICATION,TitledBorder.DEFAULT_POSITION,null,MBGuiConstants.ColorGroupHeading));      

        sourceSelectionPanel  = libInfo.getUserinterfaceFactory().getMetadataLibrarySourcePanel(mainInfo);

        sourceSelectionPanel.setBorder(BorderFactory.createTitledBorder(LineBorder.createGrayLineBorder() ,"Source selection",TitledBorder.DEFAULT_JUSTIFICATION,TitledBorder.DEFAULT_POSITION,null,MBGuiConstants.ColorGroupHeading));

        c.gridx =1;
        c.gridy=1;
        c.insets = new Insets(1,1,1,1);
        c.weightx = 1;
        c.fill = GridBagConstraints.HORIZONTAL;
        c.anchor = GridBagConstraints.NORTH;
        c.gridwidth = GridBagConstraints.BOTH;
        gridBag.setConstraints(sourceSelectionPanel, c);
        topPanel.add(sourceSelectionPanel);

        mainframeSelectionPanel  = new MainframeReleaseAndDriverSelectionPanel(libInfo, mainInfo, this);
        mainframeSelectionPanel.addUserInterfaceEventListener(new DriverSelectionChangeListener());
        mainframeSelectionPanel.setBorder(BorderFactory.createTitledBorder(LineBorder.createGrayLineBorder() ,"Build destination ",TitledBorder.DEFAULT_JUSTIFICATION,TitledBorder.DEFAULT_POSITION,null,MBGuiConstants.ColorGroupHeading)); 


        c.gridx =1;
        c.gridy =2;
        c.weightx = 1;
        c.fill = GridBagConstraints.HORIZONTAL;
        c.anchor = GridBagConstraints.NORTH;
        c.gridwidth = GridBagConstraints.BOTH;
        gridBag.setConstraints(mainframeSelectionPanel, c);
        topPanel.add(mainframeSelectionPanel);


        driverParts = new JButton(listPartsInDriverAction);
        driverParts.setText("Driver Parts");

        libraryParts = new JButton(listPartsInLibraryAction);
        libraryParts.setText("Library Parts");


        JPanel  buttonPanel = new JPanel();
        buttonPanel.setLayout(new SpringLayout());

        // spacers
        buttonPanel.add(Box.createGlue());
        buttonPanel.add(Box.createGlue());

        buttonPanel.add(driverParts);
        buttonPanel.add(libraryParts);

        // spacers
        buttonPanel.add(Box.createGlue());
        buttonPanel.add(Box.createGlue());


        com.ibm.sdwb.build390.userinterface.graphic.utilities.GeneralUtilities.makeCompactGrid(buttonPanel, -1, 2, 1, 1, 2, 2);





        c.gridx =1;
        c.gridy =3;
        c.fill = GridBagConstraints.HORIZONTAL;
        c.anchor = GridBagConstraints.NORTH;

        c.gridwidth = GridBagConstraints.BOTH;
        gridBag.setConstraints(buttonPanel, c);
        topPanel.add(buttonPanel);

        centerPanel.add(BorderLayout.NORTH, topPanel);

        JPanel tabPanel = new JPanel(new BorderLayout());
        tabPanel.add(BorderLayout.CENTER,tab);
        tabPanel.add(BorderLayout.SOUTH,warnMeIfPartsNotInLibrary);
        warnMeIfPartsNotInLibrary.setSelected(true);


        centerPanel.add(BorderLayout.CENTER, tabPanel);

        warnMeIfPartsNotInLibrary.setEnabled(false);


        setVisible(true);

        sourceSelectionPanel.addUserInterfaceEventListener(new ReleaseSelectionChangeListener());
        sourceSelectionPanel.fireProjectUpdated();
    }


    private MetadataKeywordsMapper getMetadataKeywordsMapper() {
        if (keywordsMap==null) {
            metadataFieldsAction.doAction(new ActionEvent(thisFrame,ActionEvent.ACTION_PERFORMED,"MetadataFieldsAction"));
        }
        return keywordsMap;
    }



    private void initializeActions()  throws MBBuildException {

        final String KEY_EDIT_METADATA            = "EDIT_METADATA";
        final String KEY_LIST_PARTS_DRIVER        = "LIST_PARTS_DRIVER";
        final String KEY_LIST_PARTS_LIBRARY         = "LIST_PARTS_LIBRARY";
        final String KEY_FIND_METADATA_IN_LIBRARY    = "FIND_METADATA_IN_LIBRARY";
        final String KEY_FIND_METADATA_IN_HOST    = "FIND_METADATA_IN_HOST";
        final String KEY_FIND_LIBRARYPART_IN_HOST    = "FIND_LIBRARYPART_IN_HOST";
        final String KEY_REPLACE_METADATA_IN_LIBRARY = "REPLACE_METADATA_IN_LIBRARY";
        final String KEY_FIND_HOST_AND_LIBRARY_METADATA_IN_LIBRARY    = "FIND_HOST_AND_LIBRARY_METADATA_IN_LIBRARY";
        final String KEY_REPLACE_HOST_AND_LIBRARY_METADATA_IN_LIBRARY = "REPLACE_HOST_AND_LIBRARY_METADATA_IN_LIBRARY";
        final String KEY_MODEL_PART               = "MODEL_PART";

        modelPartsAction    = new ModelPartsAction();
        editMetadataAction      = new EditMetadataAction();



        bEdit =  new JButton(editMetadataAction);

        comboHistoryMap.create(MAINMETADATACHOOSERPAGE,true);

        closeAllFilterTabsAction =  new CloseAllFilterTabsAction();

        listPartsInDriverAction = new ListPartsInDriverAction();
        listPartsInLibraryAction  = new ListPartsInLibraryAction();
        //TST2838
        //ClearFilterHistoryHostAction clearFilterHistoryAction  = new ClearFilterHistoryHostAction(Arrays.asList(new String[]{MAINMETADATACHOOSERPAGE}));
        filterByLibraryNameAction                                     = new FilterByLibraryNameAction("FLD"+MAINMETADATACHOOSERPAGE,this);//SDWB2394-I F->Filter L-Library D->Display Filter library parts in display
        FilterByHostMetadataAction    filterByHostMetadataAction      = new FilterByHostMetadataAction(MAINMETADATACHOOSERPAGE,this);
        filterByLibraryMetadataAction                                 = new FilterByLibraryMetadataAction("FH"+MAINMETADATACHOOSERPAGE,this);
        ReplaceLibraryMetadataAction  replaceLibraryMetadataAction   = new ReplaceLibraryMetadataAction("RC"+MAINMETADATACHOOSERPAGE,this);
        filterByHostAndLibraryMetadataAction  = new FilterByHostAndLibraryMetadataAction("FHC"+MAINMETADATACHOOSERPAGE,this);
        ReplaceHostAndLibraryMetadataAction  replaceHostAndLibraryMetadataAction   = new ReplaceHostAndLibraryMetadataAction("RHC"+MAINMETADATACHOOSERPAGE,this);
        RunFilterHistoryAction runFilterHistoryAction  = new RunFilterHistoryAction(MAINMETADATACHOOSERPAGE,this);


        metadataFieldsAction = new MetadataFieldsAction();
        clearTabDataAction = new ClearTabDataAction();


        /*list parts in driver CTRL-D */
        thisFrame.getInputMap().put(KeyStroke.getKeyStroke(KeyEvent.VK_D,InputEvent.CTRL_MASK),
                                    KEY_LIST_PARTS_DRIVER);
        thisFrame.getActionMap().put(KEY_LIST_PARTS_DRIVER, listPartsInDriverAction);

        /*list parts in library CTRL-K */
        getInputMap().put(KeyStroke.getKeyStroke(KeyEvent.VK_K,InputEvent.CTRL_MASK),
                          KEY_LIST_PARTS_LIBRARY);
        getActionMap().put(KEY_LIST_PARTS_LIBRARY, listPartsInLibraryAction);

        /*model part CTRL-M  */
        getInputMap().put(KeyStroke.getKeyStroke(KeyEvent.VK_M,InputEvent.CTRL_MASK),
                          KEY_MODEL_PART);
        getActionMap().put(KEY_MODEL_PART, modelPartsAction);

        /*edit single part mde  CTRL-E  */
        getInputMap().put(KeyStroke.getKeyStroke(KeyEvent.VK_E,InputEvent.CTRL_MASK),
                          KEY_EDIT_METADATA);
        getActionMap().put(KEY_EDIT_METADATA, modelPartsAction);

        /*find in library only CTRL-F  */
        getInputMap().put(KeyStroke.getKeyStroke(KeyEvent.VK_F,InputEvent.CTRL_MASK),
                          KEY_FIND_METADATA_IN_LIBRARY);
        getActionMap().put(KEY_FIND_METADATA_IN_LIBRARY, filterByLibraryMetadataAction);

        /*find in host  only CTRL-H  */
        getInputMap().put(KeyStroke.getKeyStroke(KeyEvent.VK_H,InputEvent.CTRL_MASK),
                          KEY_FIND_METADATA_IN_HOST);
        getActionMap().put(KEY_FIND_METADATA_IN_HOST, filterByHostMetadataAction);

        /*find in library and host  only CTRL-G  */
        getInputMap().put(KeyStroke.getKeyStroke(KeyEvent.VK_G,InputEvent.CTRL_MASK),
                          KEY_FIND_HOST_AND_LIBRARY_METADATA_IN_LIBRARY);
        getActionMap().put(KEY_FIND_HOST_AND_LIBRARY_METADATA_IN_LIBRARY, filterByHostAndLibraryMetadataAction);

        /*find in cmvcname in a particular tab CTRL-P  */
        getInputMap().put(KeyStroke.getKeyStroke(KeyEvent.VK_P,InputEvent.CTRL_MASK),
                          KEY_FIND_LIBRARYPART_IN_HOST);
        getActionMap().put(KEY_FIND_LIBRARYPART_IN_HOST, filterByLibraryNameAction);


        /*replace in library CTRL-R  */
        getInputMap().put(KeyStroke.getKeyStroke(KeyEvent.VK_R,InputEvent.CTRL_MASK),
                          KEY_REPLACE_METADATA_IN_LIBRARY);
        getActionMap().put(KEY_REPLACE_METADATA_IN_LIBRARY, replaceLibraryMetadataAction);

        /*replace in host and library CTRL-T  */
        getInputMap().put(KeyStroke.getKeyStroke(KeyEvent.VK_T,InputEvent.CTRL_MASK),
                          KEY_REPLACE_HOST_AND_LIBRARY_METADATA_IN_LIBRARY);
        getActionMap().put(KEY_REPLACE_HOST_AND_LIBRARY_METADATA_IN_LIBRARY, replaceHostAndLibraryMetadataAction);


        //Begin TST2242
        JMenu actionsMenu=new JMenu("Actions");
        JMenu viewMenu=new JMenu("View");
        JMenu filterMenu=new JMenu("Filter Functions");
        //End TST2242


        actionsMenu.add(editMetadataAction);
        actionsMenu.add(modelPartsMenuItem);
        actionsMenu.addSeparator();
        actionsMenu.add(replaceMenu);

        viewMenu.add(listPartsInDriverAction);
        viewMenu.add(partsInLibraryMenuItem);
        viewMenu.addSeparator();


        filterMenu.add(filterByHostMetadataAction);
        filterMenu.addSeparator();
        filterMenu.add(filterByLibraryMetadataAction);
        filterMenu.add(filterByHostAndLibraryMetadataAction);
        filterMenu.add(filterByLibraryNameAction);//SDWB2394-I

        replaceMenu.add(replaceLibraryMetadataAction);
        replaceMenu.add(replaceHostAndLibraryMetadataAction);


        replaceMenu.setEnabled(false); /*disable this when the editor starts, and enable it when tabs have data */

        viewMenu.add(filterMenu);


        getJMenuBar().add(actionsMenu);
        getJMenuBar().add(viewMenu);

        getJMenuBar().getMenu(0).insertSeparator(0);
        getJMenuBar().getMenu(0).insert(closeAllFilterTabsAction,0);

        partsInLibraryMenuItem.setAction(listPartsInLibraryAction);
        modelPartsMenuItem.setAction(modelPartsAction);
    }

    private void initializeTabUI() {
        driverPartsCompositor  = new UITableModelCommunicator();
        libraryPartsCompositor   = new UITableModelCommunicator();
        allPartsCompositor     = new UITableModelCommunicator();
        newOrUnbuiltCompositor = new UITableModelCommunicator();

        MetadataTabbedTablePanel display1 =  new MetadataTabbedTablePanel("Parts in driver",new MetadataEditorTableModel(),driverPartsCompositor,this);
        tab.add(display1.getTitle(),display1);
        MetadataTabbedTablePanel display2 =    new MetadataTabbedTablePanel("Parts in Library",new MetadataEditorTableModel(),libraryPartsCompositor,this);
        tab.add(display2.getTitle(), display2);
        MetadataTabbedTablePanel display3 = new MetadataTabbedTablePanel("Parts Not in driver",new MetadataEditorTableModel(),newOrUnbuiltCompositor,this);
        tab.add(display3.getTitle(),display3);
        MetadataTabbedTablePanel display4 = new MetadataTabbedTablePanel("All parts",new MetadataEditorTableModel(),allPartsCompositor,this);
        tab.add(display4.getTitle(),display4);


        tab.addChangeListener(new javax.swing.event.ChangeListener() {
                                  public void stateChanged(javax.swing.event.ChangeEvent e) {

                                      //Begin TST2242
                                      JMenuItem item = replaceMenu.getItem(1);

                                      if (tab.getSelectedIndex()== 2) {
                                          item.setEnabled(false);
                                      } else {
                                          item.setEnabled(true);
                                      }
                                      //End TST2242

                                      update(null,null);

                                      setEnableSearchActions(!((MetadataTabbedTablePanel)tab.getSelectedComponent()).getUICommunicator().getData().isEmpty());
                                      focusSelectedTab();

                                  }
                              });

        tab.setSelectedIndex(3);
        tab.setBorder(BorderFactory.createTitledBorder(LineBorder.createGrayLineBorder() ,"Tabbed Parts Chooser:" ,TitledBorder.DEFAULT_JUSTIFICATION,TitledBorder.DEFAULT_POSITION,null,MBGuiConstants.ColorGroupHeading));
    }


    private void setEnableSearchActions(boolean isEnabled) {
        getJMenuBar().getMenu(1).getItem(3).setEnabled(isEnabled);
        filterByLibraryNameAction.setEnabled(isEnabled);
        filterByLibraryMetadataAction.setEnabled(isEnabled);
        filterByHostAndLibraryMetadataAction.setEnabled(isEnabled);
    }

    //TO-DO :  nuke this method when we move to JDK1.5.0
    private void focusSelectedTab() {
        for (int tabIndex = 0; tabIndex < tab.getTabCount(); tabIndex++) { //reset all the guys to black.
            tab.setForegroundAt(tabIndex,Color.BLACK);
        }

        thisFrame.requestFocusInWindow();
        tab.setForegroundAt(tab.getSelectedIndex(),Color.GREEN.darker().darker());
        tab.setSelectedIndex(tab.getSelectedIndex());
        if (tab.isRequestFocusEnabled()) {
            tab.requestFocusInWindow();
            tab.repaint(tab.getUI().getTabBounds(tab, tab.getSelectedIndex()));
        }
    }



    public void update(Observable o,  Object arg) {

        MetadataTabbedTablePanel uiPanel = (MetadataTabbedTablePanel)tab.getSelectedComponent();
        boolean enabled = (uiPanel.getSelectedValues() !=null ? (uiPanel.getSelectedValues().size() > 0) : false);
        editMetadataAction.setEnabled(enabled);
        enabled = (uiPanel.getSelectedValues() !=null ? ((uiPanel.getSelectedValues().size() == 1)) : false);
        modelPartsAction.setEnabled(enabled);
        modelPartsMenuItem.setEnabled(enabled);
        warnMeIfPartsNotInLibrary.setEnabled(enabled);
        if (enabled) {
            modelPartsMenuItem.setText("Model After A Part (Use " +((FileInfo) uiPanel.getSelectedValues().iterator().next()).getName()  +" As Model)");

        }


    }


    // bring up editors for all the parts that were selected
    private void startTheEditors() throws MBBuildException {
        Collection selectedValues = ((MetadataTabbedTablePanel)tab.getSelectedComponent()).getSelectedValues();
        if (sourceSelectionPanel.getSourceInfo()!=null) {
            partsInLibraryMenuItem.setText("List Parts From Library - <"+sourceSelectionPanel.getSourceInfo().getName()+">");
            listPartsInLibraryAction.setEnabled(true);
            partsInLibraryMenuItem.setEnabled(true);
            build.setSource(sourceSelectionPanel.getSourceInfo());
        }

        if (warnMeIfPartsNotInLibrary.isSelected() && (sourceSelectionPanel.getSourceInfo().getName() !=null)) {
            com.ibm.sdwb.build390.process.ProcessWrapperForSingleStep processWrapper = new com.ibm.sdwb.build390.process.ProcessWrapperForSingleStep(thisFrame);
            com.ibm.sdwb.build390.process.steps.LibraryPartlistGeneration partInLibraryStep = new com.ibm.sdwb.build390.process.steps.LibraryPartlistGeneration(build,processWrapper);
            processWrapper.setStep(partInLibraryStep);
            processWrapper.externalRun();
            Set partInfoSet = build.getPartInfoSet();  /* we probably should stick this in a hashMap with key as library_source */

            java.util.List  partInfoList =new Vector(partInfoSet);
            Collections.sort(partInfoList,FileInfo.BASIC_FILENAME_COMPARATOR);

            StringBuffer partNames = new StringBuffer();
            for (Iterator piter = selectedValues.iterator();piter.hasNext();) {
                FileInfo tempInfo = (FileInfo)piter.next();   
                int index = Collections.binarySearch(partInfoList, tempInfo, FileInfo.BASIC_FILENAME_COMPARATOR);
                if (index < 0) { /* the data isn't there */
                    partNames.append(tempInfo.getDirectory() + tempInfo.getName());
                    partNames.append(" ");
                }
            }

            String track = sourceSelectionPanel.getSourceInfo().getName();

            if (partNames.length() > 0) {
                new MBMsgBox("Information:","The part " + partNames + " is  not in library " + track +".You have to add part " + partNames + " to the specified library source " + track +" and build the corresponding library source, containing  "  + track + ", to see the metadata  changes in host for part " +  partNames + ".");
                lep.LogPrimaryInfo("Warning!","The part " + partNames + " is  not in library " + track +".You have to add part " + partNames + " to the specified library source " + track +" and build the corresponding library source containing "  + track + ", to see the metadata  changes in host for part " +  partNames + ".",false);

            }
        }

        for (Iterator iter =  selectedValues.iterator();iter.hasNext();) {
            //INT1980 
            FileInfo tempInfo = (FileInfo)iter.next();
            tempInfo.setProject(sourceSelectionPanel.getProjectChosen().getLibraryName());
            showSingleEditor(tempInfo);
        }

    }

    private void showSingleEditor(final FileInfo tempInfo) { /*INT19801 */
        MBCancelableActionListener listener = new MBCancelableActionListener(thisFrame) {
            /* the method to override for whatever action you want to perform in response
            to a click.
            */
            public void doAction(ActionEvent e) {

                try {

                    Set request = new HashSet();

                    InfoForMainframePartReportRetrieval info;

                    if (tempInfo.getMainframeFilename() !=null && tempInfo.getMainframeFilename().indexOf(".")  > 0) {
                        info =  new InfoForMainframePartReportRetrieval(tempInfo.getMainframeFilename().substring(tempInfo.getMainframeFilename().indexOf(".")+1),
                                                                        tempInfo.getMainframeFilename().substring(0,tempInfo.getMainframeFilename().indexOf(".")));
                    } else {
                        info = new InfoForMainframePartReportRetrieval(null,null);
                    }


                    info.setReportType("ALL");
                    info.setDirectory(tempInfo.getDirectory());
                    info.setName(tempInfo.getName());

                    request.add(info);

                    //we don't need to setRelease/Driver. Its done when the event changes happen, but the DriverUpdateEvent occurs prior to ReleaseUpdate. 
                    //need to debug it later.
                    build.setReleaseInformation(sourceSelectionPanel.getProjectChosen());
                    build.setDriverInformation(mainframeSelectionPanel.getDriverSelected());

                    com.ibm.sdwb.build390.process.MetadataReport getMetadata = new  com.ibm.sdwb.build390.process.MetadataReport(build,new File(build.getBuildPath()), request, thisFrame);
                    getMetadata.setBuildLevel("1");
                    getMetadata.externalRun();
                    Set returnedFiles = getMetadata.getLocalOutputFiles();
                    if (!returnedFiles.isEmpty()) {
                        String valueReportFilename = (String) returnedFiles.iterator().next();
                        if (getMetadata.getReturnCode() > 4) {
                            throw new HostError("There was an error retrieving a metadata report.", valueReportFilename.substring(0,valueReportFilename.indexOf(MBConstants.CLEARFILEEXTENTION)), getMetadata.getReturnCode());
                        }

                        Set  partsSet = new HashSet();
                        partsSet.add(tempInfo);
                        build.setPartInfo(partsSet);

                        MetadataValueGenerator metadataValues = new MetadataValueGenerator(valueReportFilename, tempInfo , build.getLibraryInfo().getMetadataOperationsHandler(),thisFrame);
                        GeneratedMetadataInfo generatedMetadataInfo = metadataValues.getGeneratedMetadataInfo();
                        generatedMetadataInfo.setReleaseAndDriverInformation(build.getReleaseInformation(),build.getDriverInformation());
                        SingleSourceMetadataEditorFrame editPanel = new SingleSourceMetadataEditorFrame(generatedMetadataInfo,build.getSetup().getMainframeInfo(), build.getSetup().getLibraryInfo(),lep);
                        editPanel.setReportSaveLocation(build.getBuildPathAsFile());
                        editPanel.revalidate();
                        editPanel.repaint();
                    } else {
                        String partname = info.getPartClass()!=null ? (info.getPartClass() + "."+ info.getPartName()) :
                                          (info.getDirectory()+info.getName());
                        throw new GeneralError("There was an error retrieving a metadata report for " + partname);
                    }

                } catch (MBBuildException mbe) {
                    lep.LogException(mbe);
                }


            }
        };

        listener.doAction(new ActionEvent(this,ActionEvent.ACTION_PERFORMED,"MDE Action"));

    }

    //we'll make this non-threaded */
    class CloseAllFilterTabsAction extends AbstractAction {

        CloseAllFilterTabsAction() {
            super("Close All Filter Tabs ");
        }

        public void actionPerformed(ActionEvent e) {
            int removeCount =0;
            for (int i=0;i<tab.getTabCount();i++) {
                if (((CloseableTabHandler)tab.getComponentAt(i)).isCloseable()) {
                    tab.removeTabAt(i);
                    i=0; /*reset i, since the tabcount has altered, after a tab has been removed */
                }
            }

        }       

    }


    /*key CTRL-D */
    class ListPartsInDriverAction extends com.ibm.sdwb.build390.userinterface.graphic.widgets.CancelableAction {

        private com.ibm.sdwb.build390.process.management.Haltable stopObject =null;

        ListPartsInDriverAction() {
            super("List Parts From Driver " );
            putValue(ACCELERATOR_KEY,KeyStroke.getKeyStroke(KeyEvent.VK_D,InputEvent.CTRL_MASK));
            setEnabled(false);
            
        }

        public void doAction(ActionEvent e) {
            try {

                if (build.getReleaseInformation()==null) {
                    return; 
                }

                com.ibm.sdwb.build390.mainframe.ReleaseInformation relInfo = sourceSelectionPanel.getProjectChosen();
                build.setReleaseInformation(relInfo);
                build.setDriverInformation(mainframeSelectionPanel.getDriverSelected());
                com.ibm.sdwb.build390.process.ProcessWrapperForSingleStep wrapper = new com.ibm.sdwb.build390.process.ProcessWrapperForSingleStep(parentWindow); 
                com.ibm.sdwb.build390.process.steps.DriverReport step = new com.ibm.sdwb.build390.process.steps.DriverReport(build.getDriverInformation(),build.getMainframeInfo(),build.getLibraryInfo(), new java.io.File(build.getBuildPath()),wrapper);
                step.setForceNewReport(true);
                step.setIncludePathname(true);
                step.setSummaryType("LOCAL");
                step.setIncludeOnlyLibraryParts(true);
                wrapper.setStep(step);
                stopObject = wrapper;
                wrapper.externalRun();

                int sizeOfPartsInLibrary = libraryPartsCompositor.getData().size();
                int sizeOfPartsInDriver = 0;

                if (step.getParser()!=null && step.getParser().getPartsInfo().size() > 0) {
                    driverPartsCompositor.updateDisplay(step.getParser().getPartsInfo());
                    sizeOfPartsInDriver = driverPartsCompositor.getData().size();

                    if (sizeOfPartsInLibrary > 0) {
                        new TabDataAggregator(libraryPartsCompositor.getData(),driverPartsCompositor.getData());
                    } else {
                        allPartsCompositor.updateDisplay(step.getParser().getPartsInfo());
                    }

                    getStatus().updateStatus("Refresh complete.",false);
                } else if (sizeOfPartsInLibrary > 0) {
                    allPartsCompositor.updateDisplay(libraryPartsCompositor.getData());
                }

                tab.setSelectedComponent(tab.getComponentAt(0));
                repaint();
            } catch (MBBuildException mbe) {
                lep.LogException(mbe);
            }

        }

        public void postAction() {
            setEnableSearchActions(!((MetadataTabbedTablePanel)tab.getSelectedComponent()).getUICommunicator().getData().isEmpty());
            focusSelectedTab();
        }

        public void stop() {
        }
    }

    /*key CTRL-K */
    class ListPartsInLibraryAction extends com.ibm.sdwb.build390.userinterface.graphic.widgets.CancelableAction {

        ListPartsInLibraryAction() {
            super("List Parts From Library ");
            putValue(ACCELERATOR_KEY,KeyStroke.getKeyStroke(KeyEvent.VK_K,InputEvent.CTRL_MASK));
            setEnabled(false);
        }


        public void doAction(ActionEvent e) {

            if (sourceSelectionPanel.getSourceInfo()!=null) {
                partsInLibraryMenuItem.setText("List Parts From Library - <"+sourceSelectionPanel.getSourceInfo().getName()+">");
                listPartsInLibraryAction.setEnabled(true);
                partsInLibraryMenuItem.setEnabled(true);
                build.setSource(sourceSelectionPanel.getSourceInfo());
            }

            com.ibm.sdwb.build390.process.ProcessWrapperForSingleStep processWrapper = new com.ibm.sdwb.build390.process.ProcessWrapperForSingleStep(thisFrame);
            com.ibm.sdwb.build390.process.steps.LibraryPartlistGeneration partInLibraryStep = new com.ibm.sdwb.build390.process.steps.LibraryPartlistGeneration(build,processWrapper);
            processWrapper.setStep(partInLibraryStep);
            processWrapper.run();

            Set partInfoSet = build.getPartInfoSet();

            int sizeOfPartsInLibrary = partInfoSet.size();
            int sizeOfPartsInDriver = driverPartsCompositor.getData().size();

            if (sizeOfPartsInDriver > 0) {
                new TabDataAggregator(partInfoSet,driverPartsCompositor.getData());
            } else {
                allPartsCompositor.updateDisplay(partInfoSet);
            }

            libraryPartsCompositor.updateDisplay(partInfoSet);

            tab.setSelectedComponent(tab.getComponentAt(1));

            getStatus().updateStatus("Refresh complete.",false);
            repaint();
        }

        public void postAction() {
            setEnableSearchActions(!((MetadataTabbedTablePanel)tab.getSelectedComponent()).getUICommunicator().getData().isEmpty());
            focusSelectedTab();
        }

        public void stop() {
        }
    }


    /*key CTRL-E */
    class EditMetadataAction extends com.ibm.sdwb.build390.userinterface.graphic.widgets.CancelableAction {

        EditMetadataAction() {
            super("Edit");
            putValue(ACCELERATOR_KEY,KeyStroke.getKeyStroke(KeyEvent.VK_E,InputEvent.CTRL_MASK));
            setEnabled(false);
        }

        public void doAction(ActionEvent e) {
            try {
                startTheEditors();
            } catch (MBBuildException mbe) {
                getLEP().LogException(mbe);
            }
        }

        public void stop() {
        }
    }

    //SDWB2396
    class RunFilterHistoryAction  extends com.ibm.sdwb.build390.userinterface.graphic.widgets.CancelableAction {

        private Observer observer;
        private String key;
        private ShowMetadataCriteriaDialog dialogAction;


        RunFilterHistoryAction(String key,Observer observer) {
            super("Filter");
            this.key = key;
            this.dialogAction = new ShowMetadataCriteriaDialog(key);
            this.observer = observer;
        }

        public void doAction(ActionEvent e) {
            try {

                dialogAction.doAction(new ActionEvent(this,ActionEvent.ACTION_PERFORMED,ShowMetadataCriteriaDialog.NAME));
                if (dialogAction.getCurrentCriteria()==null) {
                    return;
                }
                final       Vector userCriteria = dialogAction.getFindEntries();

                if (userCriteria != null) {

                    com.ibm.sdwb.build390.mainframe.ReleaseInformation relInfo =  sourceSelectionPanel.getProjectChosen();

                    build.setReleaseInformation(relInfo);
                    build.setDriverInformation(mainframeSelectionPanel.getDriverSelected());

                    final com.ibm.sdwb.build390.process.DriverPartListFilteredByMetadata driverPartlist = new com.ibm.sdwb.build390.process.DriverPartListFilteredByMetadata(userCriteria,new File(build.getBuildPath()), build.getMainframeInfo(), build.getLibraryInfo(),
                                                                                                                                                                             build.getReleaseInformation(), build.getDriverInformation(),thisFrame);


                    driverPartlist.externalRun();
                    if (driverPartlist.getResults()!=null ) {
                        SwingUtilities.invokeLater(new Runnable() {
                                                       public void run() {
                                                           UITableModelCommunicator filterCompositor = new UITableModelCommunicator();
                                                           MetadataTabbedTablePanel filterDisplay = new MetadataTabbedTablePanel("Find (Host) " + userCriteria, new MetadataEditorTableModel(),filterCompositor,observer);
                                                           filterDisplay.setCloseable(true);
                                                           tab.add(filterDisplay.getTitle(),filterDisplay);
                                                           filterCompositor.updateDisplay(driverPartlist.getResults());
                                                           tab.setSelectedComponent(filterDisplay);
                                                           focusSelectedTab();
                                                       }
                                                   });

                    }
                }
            } catch (MBBuildException mbe) {
                lep.LogException(mbe);
            }


        }

        public void postAction() {
            focusSelectedTab();
        }

        public void stop() {
        }

    }

    //SDWB2396
    class FilterByHostMetadataAction extends com.ibm.sdwb.build390.userinterface.graphic.widgets.CancelableAction {

        private Observer observer;
        private String saveKey;
        private ShowMetadataCriteriaDialog dialogAction;


        FilterByHostMetadataAction(String saveKey, Observer observer) {
            super("New Filtering Requests(Host) ");
            putValue(ACCELERATOR_KEY,KeyStroke.getKeyStroke(KeyEvent.VK_H,InputEvent.CTRL_MASK));
            this.observer=observer;
            this.saveKey = saveKey;
            this.dialogAction = new ShowMetadataCriteriaDialog(saveKey);
        }

        public void doAction(ActionEvent e) {
            try {

                if (build.getReleaseInformation()==null) {
                    problemBox("Find (Host)","Please refresh release before attempting to run filter query.");
                    return;
                }


                if (build.getDriverInformation()==null) {
                    problemBox("Find (Host)","Please select a driver before attempting to run filter query.");
                    return;
                }


                FindMetadata searchCriteria = new FindMetadata("Find (Host)",saveKey,true);


                UserSelection selection  = searchCriteria.getSelection();
                if (selection == null) {
                    return;
                }

                if (!selection.getFindEntries().isEmpty()) {

                    final     Vector userCriteria =  new Vector(selection.getFindEntries());


                    if (userCriteria != null) {

                        com.ibm.sdwb.build390.mainframe.ReleaseInformation relInfo =  sourceSelectionPanel.getProjectChosen();

                        build.setReleaseInformation(relInfo);
                        build.setDriverInformation(mainframeSelectionPanel.getDriverSelected());
                        final com.ibm.sdwb.build390.process.DriverPartListFilteredByMetadata driverPartlist = new com.ibm.sdwb.build390.process.DriverPartListFilteredByMetadata(userCriteria,new File(build.getBuildPath()), build.getMainframeInfo(), build.getLibraryInfo(),
                                                                                                                                                                                 build.getReleaseInformation(), build.getDriverInformation(),thisFrame);
                        driverPartlist.externalRun();


                        if (driverPartlist.getResults()!=null ) {
                            SwingUtilities.invokeLater(new Runnable() {
                                                           public void run() {
                                                               UITableModelCommunicator filterCompositor = new UITableModelCommunicator();
                                                               MetadataTabbedTablePanel filterDisplay = new MetadataTabbedTablePanel("Find (Host) " + new ExpressionsVector(userCriteria), new MetadataEditorTableModel(),filterCompositor,observer);
                                                               filterDisplay.setCloseable(true);
                                                               tab.add(filterDisplay.getTitle(),filterDisplay);
                                                               filterCompositor.updateDisplay(driverPartlist.getResults());
                                                               tab.setSelectedComponent(filterDisplay);
                                                               focusSelectedTab();
                                                           }
                                                       });

                            repaint(); 
                        }
                    }
                }
            } catch (MBBuildException mbe) {
                lep.LogException(mbe);
            }


        }

        public void stop() {
        }

        public void postAction() {
            focusSelectedTab();
        }
    }

    /* hot-key CTRL-F */
    class FilterByLibraryMetadataAction extends com.ibm.sdwb.build390.userinterface.graphic.widgets.CancelableAction {

        private Observer observer;
        private String key;
        private ShowMetadataCriteriaDialog dialogAction;


        FilterByLibraryMetadataAction(String key,Observer observer) {
            super("Filter List By Library Data");
            putValue(ACCELERATOR_KEY,KeyStroke.getKeyStroke(KeyEvent.VK_F,InputEvent.CTRL_MASK));
            this.key = key;
            this.observer=observer;
            dialogAction = new ShowMetadataCriteriaDialog(key);
        }

        public void doAction(ActionEvent e) {
            Set metadataBuilder = null;

            try {
                String keyWords[]=getMetadataKeywordsMapper().getVirtualKeywordsArray();
                FindMetadata searchCriteria = new FindMetadata("Find (Library)",key);
                final UserSelection selection  = searchCriteria.getSelection();
                if (selection == null) {
                    return;
                }

                if (!selection.getFindEntries().isEmpty()) {

                    Collection selectionValues = ((MetadataTabbedTablePanel)tab.getSelectedComponent()).getUICommunicator().getData();
                    lep.LogSecondaryInfo("DEBUG","selectionValues="+selectionValues);


                    comboHistoryMap.save(key,new Vector(selection.getFindEntries()));  
                    if (!selectionValues.isEmpty()) {


                        MetadataCriteriaGenerator criteriaGenerator = new MetadataCriteriaGenerator(getMetadataKeywordsMapper(),selection.getOptions().useRegularExpression());

                        MultiFilterCriteria criteria = criteriaGenerator.generateCriteria(selection.getFindEntries());


                        if (criteriaGenerator.foundNonMetadataKeywords().length() > 0 && selection.getFindEntries().size()==1) {
                            throw new GeneralError ("The following keywords\n" + criteriaGenerator.foundNonMetadataKeywords() + "\n are  non-metadata fields. No match  exists in library.");
                        } else if (criteriaGenerator.foundNonMetadataKeywords().length() >0) {
                            problemBox("Warning!","The following keywords\n" + criteriaGenerator.foundNonMetadataKeywords() + "\n are  non-metadata fields. No match  exists in library.");
                        }

                        try {
                            metadataBuilder = new HashSet(selectionValues);


                            FindMetadataInLibrary finder = new FindMetadataInLibrary(metadataBuilder,build.getLibraryInfo(),build.getReleaseInformation(),thisFrame);
                            final Filter findFilter = new RegularExpressionFilter(criteria);
                            finder.setFilter(findFilter);
                            finder.run();

                            if (findFilter.matched()==null || findFilter.matched().isEmpty()) {
                                problemBox("Find (Library)","No matches  found.\nPlease enter a different criteria.");
                                return;
                            }

                            SwingUtilities.invokeLater(new Runnable() {
                                                           public void run() {
                                                               UITableModelCommunicator filterCompositor = new UITableModelCommunicator();
                                                               MetadataTabbedTablePanel filterDisplay = new MetadataTabbedTablePanel("Find(Lib) " + new ExpressionsVector(selection.getFindEntries()), new MetadataEditorTableModel(),filterCompositor,observer);
                                                               filterDisplay.setCloseable(true);
                                                               tab.add(filterDisplay.getTitle(),filterDisplay);
                                                               filterCompositor.updateDisplay(findFilter.matched());
                                                               tab.setSelectedComponent(filterDisplay);
                                                               focusSelectedTab();
                                                           }
                                                       });

                        } catch (PatternSyntaxException pse) {
                            problemBox("Find (Library) : Regex error!","There is a problem with the regular expression!\n" +
                                       "The pattern in question is: "+pse.getPattern() + "\n" +
                                       "The description is: "+pse.getDescription() +"\n" +
                                       "The message is: "+pse.getMessage()+"\n"+
                                       "The index is: "+pse.getIndex());
                        }
                    } else {
                        problemBox("Find (Library)","The tab in focus contains no parts. Please choose a tab that contains parts.\n");
                    }

                }

            } catch (MBBuildException mbe) {
                lep.LogException(mbe);
            }


        }

        public void postAction() {
            focusSelectedTab();
        }

        public void stop() {
        }
    }


    /* hot-key CTRL - R */
    class ReplaceLibraryMetadataAction extends com.ibm.sdwb.build390.userinterface.graphic.widgets.CancelableAction {

        private Observer observer;
        private String key;


        ReplaceLibraryMetadataAction(String key, Observer observer) {
            super("Replace Metadata (Library) ");
            putValue(ACCELERATOR_KEY,KeyStroke.getKeyStroke(KeyEvent.VK_R,InputEvent.CTRL_MASK));
            this.observer=observer;
            this.key=key;
        }

        public void doAction(ActionEvent e) {

            try {

                ReplaceMetadata searchCriteria = new ReplaceMetadata("Replace (Library)",key);

                final UserSelection selection  = searchCriteria.getSelection();
                if (selection == null) {
                    return;
                }

                if (selection.getFindString() != null && selection.getReplaceString() !=null) {

                    Collection selectionValues = ((MetadataTabbedTablePanel)tab.getSelectedComponent()).getUICommunicator().getData();
                    if (!selectionValues.isEmpty()) {
                        final ExpressionsVector statusVector = new ExpressionsVector(selection.getFindEntries());
                        String[] find = selection.getFindString().trim().split("=");

                        String findRealKeyword = getMetadataKeywordsMapper().getRealKeyword(find[0]); 

                        LibraryMetadataFilterCriteria criteria =  new LibraryMetadataFilterCriteria(findRealKeyword,find[1]);  // this is not needed here. but just to catch patternsyntax exception 

                        if (!selection.getOptions().useRegularExpression()) criteria.usePatternAsStringLiteral();

                        getStatus().updateStatus("Replace (Library) on criteria " + criteria.toString(),false);
                        try {
                            criteria.compile(); // not needed here, but just to catch patern syntax exp. 
                            Set metadataBuilder = new HashSet(selectionValues);                            

                            getStatus().updateStatus("Replace (Library) ["+ statusVector.toString() +"]  metadata in library. ",false);

                            FindMetadataInLibrary finder = new FindMetadataInLibrary(metadataBuilder,build.getLibraryInfo(),build.getReleaseInformation(),thisFrame);
                            //finder.setFilter(filter);
                            final Filter findFilter = new RegularExpressionFilter(criteria);
                            finder.setFilter(findFilter);
                            finder.run();                            

                            if (findFilter.matched()==null || findFilter.matched().isEmpty()) {
                                problemBox("Replace (Library)","No matches found.\nPlease enter a different criteria.");
                                return;
                            }


                            ReplaceableFilter replaceFilter = new ReplaceableFilter(new RegularExpressionFilter(criteria),new LibraryMetadataReplaceHandler(selection.getReplaceString()));
                            replaceFilter.filter(findFilter.matched()); 


                            ProcessWrapperForSingleStep wrapper = new ProcessWrapperForSingleStep(thisFrame);
                            Set tempParts = new HashSet(replaceFilter.matched());
                            ReplaceMetadataInLibrary replace = new ReplaceMetadataInLibrary(tempParts,build.getLibraryInfo(),build.getReleaseInformation(), wrapper);
                            wrapper.setStep(replace);
                            wrapper.run();

                            final Set replacedValues = replace.getResults();

                            getStatus().updateStatus("Replace (Library) ["+ statusVector.toString() +"] metadata in library successful.",false);

                            if (replacedValues.isEmpty()) {
                                problemBox("Replace (Library)","No matches found.\nPlease enter a different criteria.");
                                return;
                            }

                            problemBox("Replace (Library)","Metadata replaced successfully for criteria \n" + 
                                       statusVector.toString() + "\nreplaced value="+selection.getReplaceString() +"\nPress OK to view the the list of parts names whose metadata where replaced.");

                            SwingUtilities.invokeLater(new Runnable() {

                                                           public void run() {

                                                               UITableModelCommunicator filterCompositor = new UITableModelCommunicator();
                                                               MetadataTabbedTablePanel filterDisplay = new MetadataTabbedTablePanel("Replaced(Lib) " + statusVector.toString(), new MetadataEditorTableModel(),filterCompositor,observer);
                                                               filterDisplay.setCloseable(true);
                                                               tab.add(filterDisplay.getTitle(),filterDisplay);
                                                               filterCompositor.updateDisplay(replacedValues);
                                                               tab.setSelectedComponent(filterDisplay);
                                                               focusSelectedTab();
                                                           }
                                                       });

                        } catch (PatternSyntaxException pse) {
                            problemBox("Replace (Library) : Regex error!","There is a problem with the regular expression!\n" +
                                       "The pattern in question is: "+pse.getPattern() + "\n" +
                                       "The description is: "+pse.getDescription() +"\n" +
                                       "The message is: "+pse.getMessage()+"\n"+
                                       "The index is: "+pse.getIndex());
                        }
                    } else {
                        problemBox("Replace (Library)","The tab in focus contains no parts. Please choose a tab that contains parts.\n");
                    }



                }
            } catch (MBBuildException mbe) {
                getStatus().updateStatus("Replace (Library) metadata failed.",false);
                lep.LogException(mbe);
            }


        }

        public void postAction() {
            focusSelectedTab();
        }


        public void stop() {
        }
    }



    /* hot-key CTRL- G */
    class FilterByHostAndLibraryMetadataAction extends com.ibm.sdwb.build390.userinterface.graphic.widgets.CancelableAction {

        private Observer observer;
        private String key;
        private ShowMetadataCriteriaDialog dialogAction; 


        FilterByHostAndLibraryMetadataAction(String key, Observer observer) {
            super("Filter List By Host & Library Data");
            putValue(ACCELERATOR_KEY,KeyStroke.getKeyStroke(KeyEvent.VK_G,InputEvent.CTRL_MASK));
            dialogAction = new ShowMetadataCriteriaDialog(key);
            this.observer=observer;
            this.key=key;
        }

        public void doAction(ActionEvent e) {
            Set metadataBuilder = null;

            try {
                //begin  TST2211

                FindMetadata searchCriteria = new FindMetadata("Find (Host & Library)",key);
                //end TST2211

                final UserSelection selection  = searchCriteria.getSelection();
                if (selection == null) {
                    return;
                }

                if (!selection.getFindEntries().isEmpty()) {

                    Collection selectionValues = ((MetadataTabbedTablePanel)tab.getSelectedComponent()).getUICommunicator().getData();
                    lep.LogSecondaryInfo("DEBUG","selectionValues="+selectionValues);
                    if (!selectionValues.isEmpty()) {
                        final ExpressionsVector statusVector = new ExpressionsVector(selection.getFindEntries());
                        MetadataCriteriaGenerator criteriaGenerator = new MetadataCriteriaGenerator(getMetadataKeywordsMapper(),selection.getOptions().useRegularExpression());
                        MultiFilterCriteria criteria = criteriaGenerator.generateCriteria(selection.getFindEntries());

                        if (criteriaGenerator.foundNonMetadataKeywords().length() > 0) {
                            problemBox("Warning!","The following keywords\n" + criteriaGenerator.foundNonMetadataKeywords() + "\n are  non-metadata fields.No match  exists in library.");

                        }

                        try {

                            metadataBuilder = new HashSet(selectionValues);
                            FindMetadataInHostAndLibrary findMetadata = new FindMetadataInHostAndLibrary(build,metadataBuilder,thisFrame);
                            findMetadata.setFilter(new MultiCriteriaFilter(criteria));
                            findMetadata.setCriteriaEntries(selection.getFindEntries());
                            findMetadata.run();

                            final  java.util.List displayVector = findMetadata.getHostAndLibraryMatch();

                            if (displayVector!=null) {
                                if (displayVector.isEmpty()) {
                                    problemBox("Find (Host & Library)","No matches found.\nPlease enter a different criteria.");
                                    getStatus().updateStatus("Find (Host & Library) - No matches found.",false);
                                    return;
                                }

                                SwingUtilities.invokeLater(new Runnable() {

                                                               public void run() {
                                                                   UITableModelCommunicator filterCompositor = new UITableModelCommunicator();
                                                                   MetadataTabbedTablePanel filterDisplay = new MetadataTabbedTablePanel("Find(Host & Lib) " + statusVector.toString(), new MetadataEditorTableModel(),filterCompositor,observer);
                                                                   filterDisplay.setCloseable(true);
                                                                   tab.add(filterDisplay.getTitle(),filterDisplay);
                                                                   filterCompositor.updateDisplay(displayVector);
                                                                   tab.setSelectedComponent(filterDisplay);
                                                                   focusSelectedTab();
                                                               }
                                                           });

                            }

                        } catch (PatternSyntaxException pse) {
                            problemBox("Find (Host & Library) : Regex error!","There is a problem with the regular expression!\n" +
                                       "The pattern in question is: "+pse.getPattern() + "\n" +
                                       "The description is: "+pse.getDescription() +"\n" +
                                       "The message is: "+pse.getMessage()+"\n"+
                                       "The index is: "+pse.getIndex());
                        }
                    } else {
                        problemBox("Find (Host & Library)","The tab in focus contains no parts. Please choose a tab that contains parts.\n");
                    }

                }
            } catch (MBBuildException mbe) {
                lep.LogException(mbe);
            }


        }

        public void postAction() {
            focusSelectedTab();
        }


        public void stop() {
        }
    }



    /* hot-key CTRL - T */
    class ReplaceHostAndLibraryMetadataAction extends com.ibm.sdwb.build390.userinterface.graphic.widgets.CancelableAction {

        private Observer observer;
        private String key;


        ReplaceHostAndLibraryMetadataAction(String key, Observer observer) {
            super("Replace Metadata (Host & Library) ");
            putValue(ACCELERATOR_KEY,KeyStroke.getKeyStroke(KeyEvent.VK_T,InputEvent.CTRL_MASK));
            this.observer=observer;
            this.key = key;
        }

        public void doAction(ActionEvent e) {

            try {

                ReplaceMetadata searchCriteria = new ReplaceMetadata("Replace(Host & Library)",key);

                final UserSelection selection  = searchCriteria.getSelection();
                if (selection == null) {
                    return;
                }

                if (selection.getFindString() != null && selection.getReplaceString() !=null) {

                    Collection selectionValues = ((MetadataTabbedTablePanel)tab.getSelectedComponent()).getUICommunicator().getData();
                    if (!selectionValues.isEmpty()) {
                        final ExpressionsVector statusVector = new ExpressionsVector(selection.getFindEntries());
                        String[] find = selection.getFindString().trim().split("=");

                        String findRealKeyword = getMetadataKeywordsMapper().getRealKeyword(find[0]); 

                        LibraryMetadataFilterCriteria criteria =  new LibraryMetadataFilterCriteria(findRealKeyword,find[1]);  // this is not needed here. but just to catch patternsyntax exception 

                        if (!selection.getOptions().useRegularExpression()) criteria.usePatternAsStringLiteral();

                        getStatus().updateStatus("Replace (Host & Library)  on criteria " + criteria.toString(),false);
                        try {
                            criteria.compile(); //not needed here, but just to catch patern syntax exp. 

                            Set metadataBuilder = new HashSet(selectionValues);

                            getStatus().updateStatus("Replace (Host & Library) ["+ statusVector.toString() +"]  metadata in library. ",false);

                            FindMetadataInHostAndLibrary  findMetadataProcess = new FindMetadataInHostAndLibrary(build,metadataBuilder,thisFrame);
                            findMetadataProcess.setCriteriaEntries(selection.getFindEntries());
                            findMetadataProcess.setFilter(new RegularExpressionFilter(criteria));
                            findMetadataProcess.run();

                            metadataBuilder = findMetadataProcess.getPopulatedParts(); // this pretty much has the version etc. populated in step0 FindMetadataProcess 

                            if (findMetadataProcess.getHostAndLibraryMatch()!=null) {
                                if (findMetadataProcess.getHostAndLibraryMatch().isEmpty()) {
                                    problemBox("Replace (Host & Library) : Find !","No matches found.\nPlease enter a different criteria.");
                                    return;
                                }


                                ReplaceableFilter replaceFilter = new ReplaceableFilter(new RegularExpressionFilter(criteria),new LibraryMetadataReplaceHandler(selection.getReplaceString()));


                                replaceFilter.filter(findMetadataProcess.getUpdatesNeededMetadata()); 
                                Set infos = new HashSet(replaceFilter.matched());


                                boolean isUpdateMetadataFound = findMetadataProcess.getUpdatesNeededMetadata()!=null ? !findMetadataProcess.getUpdatesNeededMetadata().isEmpty() : false;

                                if (isUpdateMetadataFound) {
                                    getStatus().updateStatus("Replace (Host & Library) ["+statusVector.toString() +"], update of  metadata in progress.",false);
                                    ProcessWrapperForSingleStep wrapper = new ProcessWrapperForSingleStep(thisFrame);
                                    final  ReplaceMetadataInLibrary replace = new ReplaceMetadataInLibrary(new HashSet(replaceFilter.matched()),build.getLibraryInfo(),build.getReleaseInformation(), wrapper);
                                    wrapper.setStep(replace);
                                    wrapper.run();
                                    getStatus().updateStatus("Replace (Host & Library) ["+statusVector.toString() +"], update of  metadata in library successful.",false);


                                    problemBox("Replace (Host & Library) ","Metadata replaced successfully for criteria \n" + 
                                               statusVector.toString() + "\nwith replaced value="+selection.getReplaceString() +"\nPress OK to view the the list of parts names whose metadata where replaced.");
                                    SwingUtilities.invokeLater(new Runnable() {

                                                                   public void run() {
                                                                       UITableModelCommunicator filterCompositor = new UITableModelCommunicator();
                                                                       MetadataTabbedTablePanel filterDisplay = new MetadataTabbedTablePanel("Replaced(Host & Lib) " + statusVector.toString(), new MetadataEditorTableModel(),filterCompositor,observer);
                                                                       filterDisplay.setCloseable(true);
                                                                       tab.add(filterDisplay.getTitle(),filterDisplay);
                                                                       filterCompositor.updateDisplay(replace.getResults());
                                                                       tab.setSelectedComponent(filterDisplay);
                                                                       focusSelectedTab();

                                                                   }
                                                               });

                                }
                            }
                        } catch (PatternSyntaxException pse) {
                            problemBox("Replace (Host & Library) : Regex error!","There is a problem with the regular expression!\n" +
                                       "The pattern in question is: "+pse.getPattern() + "\n" +
                                       "The description is: "+pse.getDescription() +"\n" +
                                       "The message is: "+pse.getMessage()+"\n"+
                                       "The index is: "+pse.getIndex());
                        }
                    } else {
                        problemBox("Replace (Host & Library)","The tab in focus contains no parts. Please choose a tab that contains parts.\n");
                    }

                }
            } catch (MBBuildException mbe) {
                getStatus().updateStatus("Replace (Host & Library) metadata failed.",false);
                lep.LogException(mbe);
            }

        }


        public void postAction() {
            focusSelectedTab();
        }

        public void stop() {
        }
    }


    /*hot-key CTRL - M */
    class ModelPartsAction extends com.ibm.sdwb.build390.userinterface.graphic.widgets.CancelableAction {

        ModelPartsAction() {
            super("Model After A Part (Use Part As Model)" );
            putValue(ACCELERATOR_KEY,KeyStroke.getKeyStroke(KeyEvent.VK_M,InputEvent.CTRL_MASK));
            setEnabled(false);
        }

        public void doAction(ActionEvent e) {
            MetadataTabbedTablePanel uiPanel = (MetadataTabbedTablePanel)tab.getSelectedComponent();
            Iterator iter = uiPanel.getSelectedValues().iterator();
            FileInfo info = (FileInfo)iter.next();

            Map allCompositors = new HashMap();
            for (int i=0;i<tab.getTabCount();i++) {
                Collection tempData = ((MetadataTabbedTablePanel)tab.getComponentAt(i)).getUICommunicator().getData();
                if (tempData!=null && !tempData.isEmpty()) {
                    allCompositors.put(tab.getTitleAt(i),tempData);
                }
            }


            com.ibm.sdwb.build390.mainframe.ReleaseInformation relInfo =  sourceSelectionPanel.getProjectChosen();

            build.setReleaseInformation(relInfo);
            build.setDriverInformation(mainframeSelectionPanel.getDriverSelected());
            new BatchMetadataEditorFrame(build,info,allCompositors);

            repaint();


        }

        public void stop() {
        }
    }



    //Begin SDWB2394-I
    /*hot-key CTRL - P */
    class FilterByLibraryNameAction extends com.ibm.sdwb.build390.userinterface.graphic.widgets.CancelableAction {
        private Observer observer;
        private String key = "";

        FilterByLibraryNameAction(String key,Observer observer) {
            super("Filter List By Library Pathname");
            putValue(ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_P,InputEvent.CTRL_MASK));
            this.observer =observer;
            this.key= key;
        }

        public void doAction(ActionEvent e) {
            try {
                LibraryFindDialog  searchCriteria = new LibraryFindDialog(key);

                final  UserSelection selection  = searchCriteria.getSelection();
                boolean hasData =  selection!=null ? selection.getFindString()!=null  : false;
                if (!hasData) {
                    return;
                }

                Collection values = ((MetadataTabbedTablePanel) tab.getSelectedComponent()).getUICommunicator().getData();

                if (values.size() > 0) {



                    FindLibraryPartNameCriteria criteria = new FindLibraryPartNameCriteria(selection.getFindString().trim());

                    if (!selection.getOptions().useRegularExpression()) {
                        criteria.usePatternAsStringLiteral();
                    }
                    criteria.compile();
                    final RegularExpressionFilter filter = new RegularExpressionFilter(criteria);
                    filter.filter(values);

                    if (filter.matched().isEmpty()) {
                        problemBox("Find By Library Pathname","No matches found.\nPlease enter a different criteria.");
                        return;
                    }

                    SwingUtilities.invokeLater(new Runnable() {

                                                   public void run() {
                                                       UITableModelCommunicator defaultCompositor = new UITableModelCommunicator();

                                                       MetadataTabbedTablePanel filterListByLibraryPartNameDisplay = new MetadataTabbedTablePanel("Find(Library Pathname) [" + selection.getFindString().trim()+"] ", new MetadataEditorTableModel(),defaultCompositor,observer);

                                                       filterListByLibraryPartNameDisplay.setCloseable(true);
                                                       tab.add(filterListByLibraryPartNameDisplay.getTitle(),filterListByLibraryPartNameDisplay);
                                                       defaultCompositor.updateDisplay(filter.matched());
                                                       tab.setSelectedComponent(filterListByLibraryPartNameDisplay);

                                                       focusSelectedTab();
                                                   }
                                               });

                } else {
                    problemBox("Find By Library Pathname","The tab in focus contains no parts. Please choose a tab that contains parts.\n");
                }
            } catch (PatternSyntaxException pse) {
                problemBox("Regex error!","There is a problem with the regular expression!\n" +
                           "The pattern in question is: "+pse.getPattern() + "\n" +
                           "The description is: "+pse.getDescription() +"\n" +
                           "The message is: "+pse.getMessage()+"\n"+
                           "The index is: "+pse.getIndex());

            } catch (MBBuildException mbe) {
                lep.LogException(mbe);
            }
        }

        public void postAction() {
            focusSelectedTab();
        }


        public void stop() {
        }

        class LibraryFindDialog {


            private JComboBox  combo = null;
            private SimpleFrameWithSearchOptions search;
            private UserSelection selections;

            LibraryFindDialog(String key) throws MBBuildException {
                initialize(key);
            }

            private void initialize(String key) throws MBBuildException {

                FindAction action  = new FindAction();
                Vector actions = new Vector();
                actions.add(new JButton(action));

                search = new SimpleFrameWithSearchOptions(thisFrame, getLEP());
                search.setPanelAndActions(getPanel(key), actions,new HelpAction("",HelpTopicID.FILTER_PARTLIST_BY_LIBRARY_NAME_HELP));            
                search.setTitle("Find(Library pathname)");
                search.setVisible(true);

            }


            private JPanel getPanel(String key) {

                JLabel      searchLabel                  = new JLabel   ("Find  for  library pathname :");
                JPanel centerPanel = new JPanel();
                comboHistoryMap.create(key,true);
                combo = comboHistoryMap.getCombo(key);

                GridBagLayout gridBag = new GridBagLayout();
                centerPanel.setLayout(gridBag);
                GridBagConstraints c = new GridBagConstraints();
                c.gridx = 0;
                c.gridy = 0;
                c.insets = new Insets(1,1,1,1);
                c.weightx = 1;
                c.gridwidth = GridBagConstraints.RELATIVE;
                c.fill = GridBagConstraints.HORIZONTAL;
                gridBag.setConstraints(searchLabel, c);
                centerPanel.add(searchLabel);


                c.gridx = 1;
                c.gridy = 0;
                c.insets = new Insets(1,1,1,1);
                c.gridwidth = GridBagConstraints.REMAINDER;
                c.weightx = 1;
                c.fill = GridBagConstraints.HORIZONTAL;
                gridBag.setConstraints(combo, c);
                centerPanel.add(combo);

                return centerPanel;

            }

            class FindAction extends CancelableAction {

                FindAction() {
                    super("Find");
                }
                /* the method to override for whatever action you want to perform in response
                to a click.
                */
                public void doAction(ActionEvent e) {


                    if (combo.getSelectedItem()!=null) {
                        getSelection().addFindEntry((String)combo.getSelectedItem());
                    } else {
                        problemBox("Find error!","Please enter a text to find.\n\n" + getSyntaxHelp()); 
                        return;
                    } 

                    getSelection().setOptions(search.getOptions());
                    search.dispose();
                }

                private  String getSyntaxHelp() {
                    return("EXAMPLES\n" +
                           "CLRACCT.PLX - search for CLRACCT.PLX \n"+ 
                           "      Note that CLRACCT.PLX is treated as string literal if regex is unchecked.\n"+ 
                           "           If Regular Expressions(Unix) is checked, then CLRACCT.PLX is treated as a regex.\n"+ 
                           "           The following example strings match if CLRACCT.PLX is a regex.\n"+
                           "           CLRACCT.PLX, CLRACCT.PLXD, CLRACCT.APLX, CLRACCT.ABCKAPLXD, CLRACCT.APALXOPLX etc...\n"+
                           "CLRAC.*    - search for CLRAC followed by anything \n"+ 
                           "      Note that the dot matches any character, and the star allows the dot to be\n"+ 
                           "           repeated any number of times, including zero.\n"+
                           "CLRAC.*. with regex  unchecked.\n"+
                           "      An exact match for string literals P.* is performed.\n"+
                           "Note \n"+
                           "Greedy Quantifiers\n"+
                           "      X?  	X, once or not at all.\n"+
                           "      X* 	X, zero or more times.\n"+
                           "      X+ 	X, one or more times.\n"+
                           "In case of regex just having * would result in an  invalid condition.\n"+
                           "      and regex parser error.\n"+
                           "In case of regex just having X* would result in invalid search hits.\n"+
                           "      eg: P*\n"+
                           "      The following example strings match for pattern P* \n"+
                           "      PLX PL AP AL ABXD APX AXP AXOP BBBB \n"+                 
                           "Refer Client Users Guide for more details.\n"+
                           "      For info about regex http://www.regular-expressions.info/");
                }


            }

            protected UserSelection getSelection() {
                if (selections==null) {
                    selections = new UserSelection();
                }
                return selections;

            }


        }

    }
    //End SDWB2394-I

    //SDWB2396
    class ShowMetadataCriteriaDialog extends com.ibm.sdwb.build390.userinterface.graphic.widgets.CancelableAction {

        private MetadataListEntryDialog searchCriteria;
        private String saveKey;
        private Vector currentCriteria =null;


        ShowMetadataCriteriaDialog(String saveKey) {
            super("Filter");
            this.saveKey = saveKey;
        }

        public void doAction(ActionEvent e) {

            if (build.getReleaseInformation()==null) {
                problemBox("Filter Error!","Please refresh release before attempting to run filter query");
                return;
            }

            currentCriteria = null;

            searchCriteria = new MetadataListEntryDialog(getFindEntries(), thisFrame, 67, false, getMetadataKeywordsMapper().getMetadataTypes());
            currentCriteria = searchCriteria.getEntries();
            if (searchCriteria.getEntries()!=null) {
                Vector currentUserCriteria = clearNullEntries(searchCriteria.getEntries());
                comboHistoryMap.save(saveKey, currentUserCriteria);  
            }
        }

        public Vector getCurrentCriteria() {
            return currentCriteria;
        }

        private Vector clearNullEntries(Vector userEntry) {
            /*some times the criteria displayed contains a Space or null, remove any stray spaces.*/
            for (int i=0;i<userEntry.size();i++) {
                Object obj = userEntry.get(i);
                if (obj instanceof String) {
                    String str = (String)obj;
                    boolean isRemoveEntry = (str==null ? true : (str.trim().length() > 0 ? false : true));
                    if (isRemoveEntry) {
                        userEntry.removeElement(str);
                    } else {
                        userEntry.set(i,pruneBrackets(str));
                    }
                }
            }

            return userEntry;
        }


        private String pruneBrackets(String input) {
            StringTokenizer tok = new StringTokenizer(input,"|,",true);

            String cleanedString = "";
            while (tok.hasMoreTokens()) {
                String temp = tok.nextToken();
                temp = temp.replaceAll("^[ \\t]+|[ \\t]+$",""); /* trim starting and ending space and tab char */
                String[] splitStr = temp.split("\\s+");         /* split on one or more spaces */
                if (splitStr.length>=3) {
                    String key =  splitStr[0];
                    String  val = splitStr[2];
                    if (val.indexOf(",") >0) {
                        val = val.substring(0,val.indexOf(",")+1);
                    } else if (val.indexOf("|") >0) {
                        val = val.substring(0,val.indexOf("|")+1);
                    }
                    cleanedString += key + " " + splitStr[1]+ " " + val;

                } else {
                    if (temp.matches("[|]|[,]")) {            /*  if it is comma ,  | (or) then add it to cleanedString */ 
                        cleanedString +=temp;
                    }
                }

            }
            return cleanedString;

        }


        public Vector getFindEntries() {

            JComboBox filterHistoryCombo = comboHistoryMap.getCombo(saveKey);

            Object selectedObject = filterHistoryCombo.getItemAt(filterHistoryCombo.getSelectedIndex() < 0 ? 0:filterHistoryCombo.getSelectedIndex());

            Vector initVect = null;
            if (selectedObject ==null) {
                initVect = new Vector();
            } else {
                if (selectedObject instanceof Vector) {
                    initVect = splitCriteria((Vector)((Vector)selectedObject).clone());
                } else if (selectedObject instanceof String) {

                    initVect = splitCriteria(new String((String)selectedObject));//TST2263
                }
            }
            return clearNullEntries(initVect);
        }

        public void stop() {
        }
    }

    private Vector splitCriteria(Vector vector) {
        Vector temp = new Vector();
        for (Iterator iter = vector.iterator();iter.hasNext();) {
            String str = (String)iter.next();
            temp.addAll(splitCriteria(str));
            iter.remove();
        }
        vector.addAll(temp);
        return vector;
    }

    //Begin TST2263
    private Vector splitCriteria(String str) {
        Vector vect = new Vector();
        StringTokenizer strk = new StringTokenizer(str,",|",true);

        String tmp = "";
        while (strk.hasMoreTokens()) {
            tmp =(String)strk.nextToken();
            if (tmp.matches(",|\\|")) {
                String temp = (String)vect.get(vect.size()-1);
                temp += tmp;
                vect.remove(vect.size() -1);
                vect.add(temp);
            } else {
                vect.add(tmp.trim());
            }

        }

        return vect;
    }


    class ComboBoxWithHistoryFactory {

        private final Map comboWithHistoryMap  = new HashMap();

        //***Begin SDWB2396
        private static final String METADATAUSERCRITERIAHISTORY ="METADATAUSERCRITERIAHISTORY";
        //***End SDWB2396


        JComboBox create(String saveKey,boolean editableFlag) {
            JComboBox filterHistoryCombo = getCombo(saveKey);
            if (filterHistoryCombo==null) {
                filterHistoryCombo = new ComboBoxWithHistory(saveKey);
                filterHistoryCombo.setEditable(editableFlag);
                comboWithHistoryMap.put(saveKey,filterHistoryCombo);

            }


            //***End SDWB2396
            return filterHistoryCombo;
        }


        JComboBox  getCombo(String key) {
            return((JComboBox)comboWithHistoryMap.get(key));
        }

        private java.util.Vector  getCriteria(String key) {
            java.util.List  userCriteriaHistory = (java.util.List) getGenericStatic(key);

            if (userCriteriaHistory == null) {
                userCriteriaHistory = new Vector();
            }

            if (userCriteriaHistory instanceof Vector) {
                return(Vector)userCriteriaHistory;
            }

            return new Vector(userCriteriaHistory);

        }

        void save(final String key,final Vector userCriteria) {
            //***Begin SDWB2396
            // 
            try {
                SwingUtilities.invokeAndWait(new Runnable() {
                                                 public void run() {
                                                     java.util.Vector  userCriteriaHistory = getCriteria(key);
                                                     if (userCriteriaHistory==null) {
                                                         userCriteriaHistory = new Vector();
                                                     }
                                                     if (userCriteria !=null) {
                                                         if (!userCriteria.isEmpty()) {
                                                             ExpressionsVector customVector = new ExpressionsVector();
                                                             customVector.addAll(userCriteria);
                                                             ((ComboBoxWithHistory)getCombo(key)).addHistoryItem(customVector);
                                                             MBBasicInternalFrame.putGenericStatic(key, userCriteriaHistory);
                                                             getCombo(key).setSelectedItem(customVector);
                                                         }
                                                     }
                                                 }
                                             });

            } catch (InterruptedException ivce) {
            } catch (InvocationTargetException ivte) {
            }
            //***End SDWB2396

        }

    }



    //we'll make this non-threaded */
    class ClearTabDataAction extends AbstractAction {

        ClearTabDataAction() {
            super("Clear Tab Data");
        }

        public void actionPerformed(ActionEvent e) {
            if (thisFrame!=null) {
                closeAllFilterTabsAction.actionPerformed(new ActionEvent(thisFrame,ActionEvent.ACTION_PERFORMED, "CloseAllFilterTabsAction"));
                for (int i=0;i<tab.getTabCount();i++) {
                    MetadataTabbedTablePanel display  = (MetadataTabbedTablePanel)tab.getComponentAt(i);
                    if (display.getUICommunicator().getData().size() >0) {
                        display.getUICommunicator().updateDisplay(new Vector());
                    }
                }
            }


            setEnableSearchActions(false);

            editMetadataAction.setEnabled(false);
            bEdit.setEnabled(false);
        }


    }


    class MetadataFieldsAction extends com.ibm.sdwb.build390.userinterface.graphic.widgets.CancelableAction {

        MetadataFieldsAction() {
            super("Metadata Fields");
        }

        public void doAction(ActionEvent e) {
            try {
                if (sourceSelectionPanel.getProjectChosen()!=null) {
                    com.ibm.sdwb.build390.process.MetadataReport metadataFieldReport = new com.ibm.sdwb.build390.process.MetadataReport(build,new File(build.getBuildPath()),null, thisFrame);
                    metadataFieldReport.setJustGetFields(true);
                    metadataFieldReport.externalRun();
                    if (metadataFieldReport.getMetadataTypes()!=null) {
                        if (keywordsMap==null) {
                            keywordsMap = new MetadataKeywordsMapper(); 
                        }
                        keywordsMap.setMetadataTypes(metadataFieldReport.getMetadataTypes());
                    }

                }
            } catch (MBBuildException mbe) {
                lep.LogException(mbe);
            }
        }       

        public void stop() {
        }
    }

    private class UnbuiltPartsFilter {

        private Filter filter;
        private Collection builtParts;
        private Collection libraryParts;
        private Collection notInLibraryParts;
        private Collection newlyCreatedParts;
        private Collection rebuildNeededParts;
        private LogEventProcessor lep;

        private UnbuiltPartsFilter(LogEventProcessor lep) {
            this.lep = lep;
            filter = new DefaultFilter(new RebuildNeededPartsFilterCriteria());
        }

        private void setFilter(Filter filter) {
            this.filter = filter;
        }

        private void setBuiltParts(Collection builtParts) {
            this.builtParts = builtParts;

        }

        private void setLibraryParts(Collection libraryParts) {
            this.libraryParts = libraryParts;
        }


        private java.util.List getBuiltParts() {
            if (builtParts instanceof java.util.List) {
                return(java.util.List)builtParts;
            }
            return Arrays.asList(builtParts.toArray());
        }

        private java.util.List getLibraryParts() {
            if (libraryParts instanceof java.util.List) {
                return(java.util.List)libraryParts;
            }
            return Arrays.asList(libraryParts.toArray());
        }

        /*dependent parts in driver */
        private Collection  getNotInLibraryParts() {
            return notInLibraryParts;
        }

        /*parts in library only. they dont exist in the driver*/
        private Collection getLibraryOnlyParts() {
            return newlyCreatedParts;
        }

        /*version mismatch parts or parts with B flag off */
        private Collection getRebuildNeededParts() {
            return rebuildNeededParts;
        }


        private void  doFilter() {
            /** let  D = driver parts, T= tracks and  
             * M = matched parts, U = unmathced parts
             * D intersection T = M (check for inactive/version mismatch), U(dependent parts, i.e not in library)
             * 
             */

            lep.LogPrimaryInfo("INFORMATION",dumpParts("Library Parts",getLibraryParts()),false);
            lep.LogPrimaryInfo("INFORMATION",dumpParts("Built   Parts",getBuiltParts()),false);

            ((EliminateDuplicatesCriteria)filter.getFilterCriteria()).setSearchList(getLibraryParts());
            filter.filter(getBuiltParts());
            rebuildNeededParts  = filter.matched();
            lep.LogPrimaryInfo("INFORMATION",dumpParts("Rebuild Parts", rebuildNeededParts),false);
            notInLibraryParts = filter.unmatched();
            lep.LogPrimaryInfo("INFORMATION",dumpParts("NotInLibParts", rebuildNeededParts),false);

            /** T intersection D = M (ignore it), U(new parts in library)
             */ 
            filter = new DefaultFilter(new NewlyCreatedPartsFilterCriteria());
            ((EliminateDuplicatesCriteria)filter.getFilterCriteria()).setSearchList(getBuiltParts());
            filter.filter(getLibraryParts());
            newlyCreatedParts = filter.unmatched();

            lep.LogPrimaryInfo("INFORMATION",dumpParts("Newly   Parts", newlyCreatedParts),false);
            lep.LogPrimaryInfo("INFORMATION",dumpParts("Unbuilt Parts", getUnbuiltParts()),false);
        }


        private Collection getUnbuiltParts() {
            java.util.List unbuiltParts = new ArrayList();
            unbuiltParts.addAll(getRebuildNeededParts());
            unbuiltParts.addAll(getLibraryOnlyParts());
            return unbuiltParts;

        }



        private  String dumpParts(String title,Collection parts) {
            StringBuffer toStrb = new StringBuffer();
            toStrb.append("<==== Dump parts " + title  +" starts ====>\n");

            for (Iterator iter=parts.iterator();iter.hasNext();) {
                toStrb.append(dumpPart(title,iter.next()));
            }
            toStrb.append("<==== Dump parts " + title  +" ends   ====>\n");
            return toStrb.toString();
        }


        private String dumpPart(String title,Object part) {
            com.ibm.sdwb.build390.info.FileInfo info = (com.ibm.sdwb.build390.info.FileInfo)part;
            return(title + ":MOD.CLASS="+info.getMainframeFilename()+", LIB="+ info.getDirectory() +info.getName() +", VER="+info.getVersion() +", I="+info.getTypeOfChange()+"\n");     

        }

    }


    private class TabDataAggregator {
        private EliminateDuplicatesCriteria criteria;
        private Filter dupsFilter;

        private TabDataAggregator(Collection librarySourceData, Collection mainframeDestinationData) {
            criteria = new EliminateDuplicatePartsCriteria();
            dupsFilter = new EliminateDuplicatesFilter(criteria);

            int sizeOfPartsInLibrary = librarySourceData.size();
            int sizeOfPartsInDriver  = mainframeDestinationData.size();

            Collection first  = null;
            Collection second = null;

            if (sizeOfPartsInLibrary > sizeOfPartsInDriver) {
                first  = mainframeDestinationData;
                second = librarySourceData;
            } else {
                first  = librarySourceData;
                second = mainframeDestinationData;
            }   

            java.util.LinkedList matchedOut =  new LinkedList(second);

            criteria.setSearchList(matchedOut);

            getStatus().updateStatus("Filtering duplicate parts. please wait..",false);
            ((EliminateDuplicatesFilter)dupsFilter).mergeSearchList();
            dupsFilter.filter(first);
            getStatus().updateStatus("Filtering duplicate parts. complete.",false);
            Collection out = dupsFilter.matched();
            getStatus().updateStatus("Filtering unbuilt parts. please wait..",false);
            UnbuiltPartsFilter unbuiltHelper = new UnbuiltPartsFilter(lep);

            first =  mainframeDestinationData;
            second = librarySourceData;

            unbuiltHelper.setBuiltParts(first);
            unbuiltHelper.setLibraryParts(second);
            unbuiltHelper.doFilter();
            getStatus().updateStatus("Filtering unbuilt parts. complete.",false);
            //do check and get unbuilt parts.
            newOrUnbuiltCompositor.updateDisplay(unbuiltHelper.getLibraryOnlyParts());
            allPartsCompositor.updateDisplay(out);

            getStatus().updateStatus("Refresh complete.",false);
            repaint();

        }
    }


    class ReleaseSelectionChangeListener implements UserInterfaceEventListener {

        public void handleUIEvent(final UserInterfaceEvent tempEvent) {
            if (tempEvent instanceof ReleaseUpdateEvent) {
                ReleaseUpdateEvent event = (ReleaseUpdateEvent) tempEvent;
                mainframeSelectionPanel.setReleaseInfo(event.getReleaseInformation());
                build.setReleaseInformation(event.getReleaseInformation());
                keywordsMap = null;                

                if (sourceSelectionPanel.getSourceInfo()!=null) {
                    listPartsInLibraryAction.setEnabled(sourceSelectionPanel.getSourceInfo().getName()!=null);
                    libraryParts.setEnabled(sourceSelectionPanel.getSourceInfo().getName()!=null);
                } else {
                    listPartsInLibraryAction.setEnabled(false);
                    libraryParts.setEnabled(false);

                }

                listPartsInDriverAction.setEnabled(build.getDriverInformation()!=null);
                driverParts.setEnabled(build.getDriverInformation()!=null);
                if (thisFrame!=null) {
                    clearTabDataAction.actionPerformed(new ActionEvent(thisFrame,ActionEvent.ACTION_PERFORMED,"ClearTabOnReleaseChange"));
                }

            }

            if (tempEvent instanceof SelectionUpdateEvent) {
                if (sourceSelectionPanel.getSourceInfo()!=null) {
                    listPartsInLibraryAction.setEnabled(sourceSelectionPanel.getSourceInfo().getName()!=null);
                    libraryParts.setEnabled(sourceSelectionPanel.getSourceInfo().getName()!=null);
                } else {
                    listPartsInLibraryAction.setEnabled(false);
                    libraryParts.setEnabled(false);

                }

                listPartsInDriverAction.setEnabled(build.getDriverInformation()!=null);
                driverParts.setEnabled(build.getDriverInformation()!=null);

                if (thisFrame!=null) {
                    libraryPartsCompositor.updateDisplay(new Vector());
                    new TabDataAggregator(libraryPartsCompositor.getData(),driverPartsCompositor.getData());
                }
            }

        }
    }

    class DriverSelectionChangeListener implements UserInterfaceEventListener {

        public void handleUIEvent(UserInterfaceEvent tempEvent) {

            if (tempEvent instanceof DriverUpdateEvent) {
                DriverUpdateEvent event = (DriverUpdateEvent) tempEvent;
                build.setDriverInformation(event.getDriverInformation());
                driverParts.setEnabled(build.getDriverInformation()!=null);
                listPartsInDriverAction.setEnabled(build.getDriverInformation()!=null);

                if (build.getDriverInformation()!=null) {
                    driverPartsCompositor.updateDisplay(new Vector());
                    new TabDataAggregator(libraryPartsCompositor.getData(),driverPartsCompositor.getData());
                }

            }
        }
    }



    class FindMetadata {


        private MetadataComboHistoryPanel panel = null;
        private ShowMetadataCriteriaDialog dialogAction;
        private SimpleFrameWithSearchOptions search;
        private UserSelection selections;
        private boolean dontVerifyKeywords = false;
        private String helpAnchor ="";

        FindMetadata(String dialogTitle,String key) throws MBBuildException {
            this(dialogTitle,key,false);
        }


        FindMetadata(String dialogTitle,String key,boolean verifyKeyword) throws MBBuildException {
            this.dontVerifyKeywords= verifyKeyword;
            initialize(key,dialogTitle);
        }


        private void initialize(String key,String dialogTitle) throws MBBuildException {
            if (key.equals(MAINMETADATACHOOSERPAGE)) {
                helpAnchor = HelpTopicID.FILTER_METADATA_HOST_HELP;
            } else if (key.equals("FH"+ MAINMETADATACHOOSERPAGE)) {
                helpAnchor = HelpTopicID.FILTER_METADATA_CMVC_HELP;
            } else if (key.equals("FHC"+MAINMETADATACHOOSERPAGE)) {
                helpAnchor = HelpTopicID.FILTER_METADATA_HOST_CMVC_HELP;
            }

            FindAction action  = new FindAction();
            Vector actions = new Vector();
            actions.add(new JButton(action));

            search = new SimpleFrameWithSearchOptions(thisFrame, getLEP());
            search.setPanelAndActions(getPanel(key), actions,new HelpAction("",helpAnchor));            
            //TST2838
            //search.attachMenuItemAt(0,0,new ClearFilterHistoryHostAction(Arrays.asList(new String[]{key})),true);
            search.setTitle(dialogTitle);
            search.setVisible(true);

        }


        private JPanel getPanel(String key) {

            JLabel      searchLabel                  = new JLabel   ("Find  for     :");
            JPanel centerPanel = new JPanel();
            comboHistoryMap.create(key,true);
            dialogAction = new ShowMetadataCriteriaDialog(key);
            panel = new MetadataComboHistoryPanel(null,comboHistoryMap.getCombo(key),dialogAction);

            GridBagLayout gridBag = new GridBagLayout();
            centerPanel.setLayout(gridBag);
            GridBagConstraints c = new GridBagConstraints();
            c.gridx = 0;
            c.gridy = 0;
            c.insets = new Insets(1,1,1,1);
            c.weightx = 1;
            c.gridwidth = GridBagConstraints.RELATIVE;
            c.fill = GridBagConstraints.HORIZONTAL;
            gridBag.setConstraints(searchLabel, c);
            centerPanel.add(searchLabel);


            c.gridx = 1;
            c.gridy = 0;
            c.insets = new Insets(1,1,1,1);
            c.gridwidth = GridBagConstraints.REMAINDER;
            c.weightx = 1;
            c.fill = GridBagConstraints.HORIZONTAL;
            gridBag.setConstraints(panel, c);
            centerPanel.add(panel);

            return centerPanel;

        }

        class FindAction extends CancelableAction {

            FindAction() {
                super("Find");
            }
            /* the method to override for whatever action you want to perform in response
            to a click.
            */
            public void doAction(ActionEvent e) {
                Vector findEntries =  dialogAction.getFindEntries();

                if (!findEntries.isEmpty()) {

                    //begin TST2211
                    for (Iterator iterator = findEntries.iterator();iterator.hasNext();) {
                        String entry = (String)iterator.next();

                        boolean isKeywordValid = dontVerifyKeywords ?    true : isValidKeyword(entry);

                        if (!isKeywordValid) {
                            problemBox("Find error!", "Invalid MetadataKeyword.\n\n"+ getSyntaxHelp()); 
                            return;
                        } else {
                            if (!isValidFormat(entry)) {

                                problemBox("Find error!", "Incorrect entry format.\n"+
                                           "Please enter using the following format=>\n"+
                                           "<MetadataKeyword> <\"EQ\"|\"GT\"|\"LT\"|\"NE\"> <MetadataValue><\",\" | \"|\">\n\n" + getSyntaxHelp()); 
                                return;

                            }
                        }
                    }
                    //end TST2211

                    getSelection().setFindEntries(findEntries);
                } else {
                    problemBox("Find error!","Please enter a text to find.\n\n" + getSyntaxHelp()); 
                    return;
                } 

                getSelection().setOptions(search.getOptions());
                search.dispose();
            }




            private  boolean isValidKeyword(String entry) {
                String[] ary = entry.trim().split("\\s+"); /* split on one or more space */

                if (ary.length >=4) {
                    return false;
                }


                if (ary[0]==null) {
                    return false;
                } else {
                    String keyword = ary[0].trim();

                    if (keyword.startsWith("[")) {
                        keyword = keyword.substring(1);
                    }


                    java.util.List keyWords = Arrays.asList(getMetadataKeywordsMapper().getVirtualKeywordsArray());
                    if (!keyWords.contains(keyword)) {
                        return false;
                    } else {
                        return true;
                    }
                }

            }

            //begin TST2211
            private   boolean isValidFormat(String entry) {

                String[] ary = entry.trim().split("\\s+"); /* split on one or more space */
                for (int i=0;i<ary.length;i++) {
                }

                boolean formatState = false;

                if (ary[1]!=null) {
                    if (ary[1].equals("EQ")|ary[1].equals("GT")|ary[1].equals("LT")|ary[1].equals("NE")) {
                        formatState = true;
                    }
                } else {
                    return false;
                }

                if (ary[2]==null) {
                    return false;
                } else {
                    if (!(ary[2].endsWith(",")|ary[2].endsWith("|")|ary[2].endsWith("]"))) {
                        if (ary.length >3) {
                            if (ary[3]!=null) {
                                if (ary[3].endsWith("]")) {
                                    ary[3] = ary[3].substring(0,ary[3].length()-1);
                                }

                                if (ary[3].trim().equals(",")|ary[3].trim().equals("|")) {
                                    formatState = true;
                                }
                            }
                        } else {
                            formatState = false;
                        }
                    } else {
                        if (ary[2].length()==1) {
                            formatState = false;
                        }
                        if (ary.length >3) {
                            if (ary[3]!=null) {
                                formatState = false;
                            }
                        }


                    }
                }
                return formatState;
            }
            //end TST2211
            // 

            private  String getSyntaxHelp() {
                return("EXAMPLES\n" +
                       "SOTYPE EQ PLX - search for SOTYPE  equals PLX \n"+ 
                       "      Note that PLX is treated as string literal if regex is unchecked.\n"+ 
                       "           If Regular Expressions(Unix) is checked, then PLX is treated as a regex.\n"+ 
                       "           The following example strings match if PLX is a regex.\n"+
                       "           PLX, PLXD, APLX, ABCKAPLXD, APALXOPLX.\n"+
                       "SOTYPE EQ  P.* - search for SOTYPE equals P followed by anything \n"+ 
                       "      Note that the dot matches any character, and the star allows the dot to be\n"+ 
                       "           repeated any number of times, including zero.\n"+
                       "CPARM EQ 'BUF(SIZE)'. with  regex unchecked.\n"+
                       "      Note that 'BUF(SIZE)' is treated as a string literal\n"+
                       "           if Regular Expressions(Unix) is unchecked.\n"+
                       "CPARM EQ P.*. with regex  unchecked.\n"+
                       "      An exact match for string literals P.* is performed.\n"+
                       "Note \n"+
                       "Greedy Quantifiers\n"+
                       "      X?  	X, once or not at all.\n"+
                       "      X* 	X, zero or more times.\n"+
                       "      X+ 	X, one or more times.\n"+
                       "An auto coversion of  .* to  *  happens, when a match is queried in host.\n"+
                       "So SOTYPE EQ P.* (with regex checked)  is interpreted as \n"+
                       "   P.* in library as match for character P followed by  any character\n"+
                       "       any number of times.\n"+
                       "   P.* in host undergoes an auto converstion to P* \n"+
                       "       match for character P followed by  any character\n"+
                       "       any number of times.\n"+
                       "In case of regex just having * would result in an  invalid condition.\n"+
                       "      and a  host error. eg: SOTYPE EQ *.\n"+
                       "In case of regex just having X* would result in invalid search hits in library.\n"+
                       "      eg: SOTYPE P*.\n"+
                       "      The following example strings match for pattern P* \n"+
                       "      PLX PL AP AL ABXD APX AXP AXOP BBBB \n"+
                       "Limitation! \n"+
                       "Wild card [AlphaNumeric].* is the only supported option for filtering  requests (Host & Library) \n"+
                       "Example(for filtering option (Host & Library) PL.*, AO.*, A1.* \n\n" +
                       "Refer Client Users Guide for more details.\n"+
                       "      For info about regex http://www.regular-expressions.info/");
            }


        }

        protected UserSelection getSelection() {
            if (selections==null) {
                selections = new UserSelection();
            }
            return selections;

        }


    }


    class ReplaceMetadata {

        private ComboWithMetadataKeywordsPanel panel;
        private ComboBoxWithHistory replaceText;
        private UserSelection selections;
        private SimpleFrameWithSearchOptions search;
        private String helpAnchor ="";

        ReplaceMetadata(String diaglogTitle, String key) throws MBBuildException {
            initialize(key,diaglogTitle);
        }

        private void initialize(String key,String diaglogTitle) throws MBBuildException {                       
            ReplaceAction action  = new ReplaceAction();
            Vector actions = new Vector();
            actions.add(new JButton(action));

            if (key.equals("RC"+ MAINMETADATACHOOSERPAGE)) {
                helpAnchor = HelpTopicID.REPLACE_METADATA_CMVC_HELP;
            } else if (key.equals("RHC"+MAINMETADATACHOOSERPAGE)) {
                helpAnchor = HelpTopicID.REPLACE_METADATA_HOST_CMVC_HELP;
            }

            search = new SimpleFrameWithSearchOptions(thisFrame, getLEP());
            search.setPanelAndActions(getPanel(key), actions,new HelpAction("",helpAnchor));            
            //TST2838
            //search.attachMenuItemAt(0,0,new ClearFilterHistoryHostAction(Arrays.asList(new String[]{key})),true);
            search.setTitle(diaglogTitle);
            search.setVisible(true);
        }


        private JPanel getPanel(String key) {

            ComboBoxWithHistory keyWordsCombo = (ComboBoxWithHistory)comboHistoryMap.create(key,true);
            panel = new ComboWithMetadataKeywordsPanel(key, keyWordsCombo);
            panel.setInput(getMetadataKeywordsMapper().getVirtualKeywordsArray());

            String replaceBoxKey = "REPLACE_" + key ;
            replaceText = (ComboBoxWithHistory)comboHistoryMap.create(replaceBoxKey,true);//TST2277
            replaceText.setEnabled(true);
            replaceText.setEditable(true);


            JLabel   searchLabel     = new JLabel("Find    for  :");
            JLabel   replaceLabel    = new JLabel("Replace with :");
            JPanel centerPanel = new JPanel();

            GridBagLayout gridBag = new GridBagLayout();
            centerPanel.setLayout(gridBag);
            GridBagConstraints c = new GridBagConstraints();
            c.gridx = 0;
            c.gridy = 0;
            c.insets = new Insets(1,1,1,1);
            c.weightx = 1;
            c.gridwidth = GridBagConstraints.RELATIVE;
            c.fill = GridBagConstraints.HORIZONTAL;
            gridBag.setConstraints(searchLabel, c);
            centerPanel.add(searchLabel);


            c.gridx = 1;
            c.gridy = 0;
            c.insets = new Insets(1,1,1,1);
            c.gridwidth = GridBagConstraints.REMAINDER;
            c.weightx = 1;
            c.fill = GridBagConstraints.HORIZONTAL;
            gridBag.setConstraints(panel, c);
            centerPanel.add(panel);



            c.gridx = 0;
            c.gridy = 1;
            c.insets = new Insets(1,1,1,1);
            c.weightx = 1;
            c.gridwidth = GridBagConstraints.RELATIVE;
            c.fill = GridBagConstraints.HORIZONTAL;
            gridBag.setConstraints(replaceLabel, c);
            centerPanel.add(replaceLabel);

            c.gridx = 1;
            c.gridy = 1;
            c.insets = new Insets(1,1,1,1);
            c.gridwidth = GridBagConstraints.REMAINDER;
            c.weightx = 1;
            c.fill = GridBagConstraints.HORIZONTAL;
            gridBag.setConstraints(replaceText, c);
            centerPanel.add(replaceText);
            return centerPanel;

        }

        class ReplaceAction extends CancelableAction {

            ReplaceAction() {
                super("Replace");
            }
            /* the method to override for whatever action you want to perform in response
            to a click.
            */
            public void doAction(ActionEvent e) {

                boolean isDataExists = panel.getFindEntry()!=null && getReplaceWithText()!=null ? panel.getFindEntry().length() > 0 && getReplaceWithText().length() > 0   : false;

                if (isDataExists) {
                    getSelection().addFindEntry(panel.getMetadataKeyword() +"="+panel.getFindEntry());
                    getSelection().addReplaceEntry(getReplaceWithText());
                } else {
                    problemBox("Find error!","Please enter a text to find.\n\n" + getSyntaxHelp()); 
                    return;
                } 


                getSelection().setOptions(search.getOptions());
                search.dispose();

            }

            private  String getReplaceWithText() {
                String replaceWith = "";
                replaceWith = replaceText.getSelectedItem()!=null ? ((String)replaceText.getSelectedItem()).trim()   : "";
                return replaceWith;

            }

            private  String getSyntaxHelp() {
                return("EXAMPLES\n" +
                       "SOTYPE EQ PLX - search for SOTYPE  equals PLX \n"+ 
                       "      Note that PLX is treated as string literal if regex is unchecked.\n"+ 
                       "           If Regular Expressions(Unix) is checked, then PLX is treated as a regex.\n"+ 
                       "           The following example strings match if PLX is a regex.\n"+
                       "           PLX, PLXD, APLX, ABCKAPLXD, APALXOPLX.\n"+
                       "SOTYPE EQ  P.* - search for SOTYPE equals P followed by anything \n"+ 
                       "      Note that the dot matches any character, and the star allows the dot to be\n"+ 
                       "           repeated any number of times, including zero.\n"+
                       "CPARM EQ 'BUF(SIZE)'. with  regex unchecked.\n"+
                       "      Note that 'BUF(SIZE)' is treated as a string literal\n"+
                       "           if Regular Expressions(Unix) is unchecked.\n"+
                       "CPARM EQ P.*. with regex  unchecked.\n"+
                       "      An exact match for string literals P.* is performed.\n"+
                       "Note \n"+
                       "Greedy Quantifiers\n"+
                       "      X?  	X, once or not at all.\n"+
                       "      X* 	X, zero or more times.\n"+
                       "      X+ 	X, one or more times.\n"+
                       "An auto coversion of  .* to  *  happens, when a match is queried in host.\n"+
                       "So SOTYPE EQ P.* (with regex checked)  is interpreted as \n"+
                       "   P.* in library as match for character P followed by  any character\n"+
                       "       any number of times.\n"+
                       "   P.* in host undergoes an auto converstion to P* \n"+
                       "       match for character P followed by  any character\n"+
                       "       any number of times.\n"+
                       "In case of regex just having * would result in an  invalid condition.\n"+
                       "      and a  host error. eg: SOTYPE EQ *.\n"+
                       "In case of regex just having X* would result in invalid search hits in library.\n"+
                       "      eg: SOTYPE P*.\n"+
                       "      The following example strings match for pattern P* \n"+
                       "      PLX PL AP AL ABXD APX AXP AXOP BBBB \n"+
                       "Limitation! \n"+
                       "Wild card [AlphaNumeric].* is the only supported option for filtering  requests (Host & Library) \n"+
                       "Example(for filtering option (Host & Library) PL.*, AO.*, A1.* \n\n" +
                       "Refer Client Users Guide for more details.\n"+
                       "      For info about regex http://www.regular-expressions.info/");
            }


        }

        protected UserSelection getSelection() {
            if (selections==null) {
                selections = new UserSelection();
            }
            return selections;

        }

    }




    // FixMinSize
    public Dimension getMinimumSize() {
        Dimension oldPref = new Dimension(300, 320);
        return oldPref;
    }

    public Dimension getPreferredSize() {
        return new Dimension(490,507);
    }



}

