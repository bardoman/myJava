package com.ibm.sdwb.build390.userinterface.graphic.panels.metadata;
/***************************************************************************/
/* Java MBMetadataEditPanel class for the Build/390 client                */
/*  takes a list of metadata fields and sets up editors with the appropriate defaults */
/***************************************************************************/
// Changes
// Date         Defect/Feature      Reason
// 08/25/99     FixSaveName	        Save parts to mod.class.pro
// 08/25/99	fixSaveLoc			fix the default diretories files are saved to
// 08/25/99	fixMetaCheck		fix the way the unselected values are uploaded for check
// 09/30/99     pjs - Fix help link
// 01/07/2000   individual build log file changes
// 03/07/2000   reworklog stuff

//05/16/2000    UBUILD_METADATA   Added a constructor with the boolean variable UserBuildMetadata for Metadataedit
//05/24/2000    UBUILD_METADATA   just added a button with the label check/save.
//		                  which does the validation and then save the metadata in a hash table.

//06/13/2000    HELP_ACT_LSTNR  provided help for both the  userBuild metadata editor, and the driver build metadataEditor
//02/23/2001    #FixNull: needed to test for null modalLock
//12/03/2002    SDWB-2019 Enhance the help system
//11/02/02      Feat.SDWB1776:      BLD390 should use disttype and scode values from CMVC.
//05/16/2003    #DEF.CleanUpSDWB1776: 
//05/16/2003    TST1379. Error file is not displayed. The save thing is encapsulated in AbstractActions
//09/03/2004    INT1981  MDE Repaint problem.
/***************************************************************************/
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.Vector;

import javax.swing.*;
import javax.swing.border.LineBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import com.ibm.sdwb.build390.*;
import com.ibm.sdwb.build390.help.HelpTopicID;
import com.ibm.sdwb.build390.info.FileInfo;
import com.ibm.sdwb.build390.library.LibraryInfo;
import com.ibm.sdwb.build390.library.SourceInfo;
import com.ibm.sdwb.build390.library.cmvc.metadata.server.MetadataQueryUtilities;
import com.ibm.sdwb.build390.library.cmvc.metadata.server.VersionPopulator;
import com.ibm.sdwb.build390.logprocess.LogEventProcessor;
import com.ibm.sdwb.build390.mainframe.DriverInformation;
import com.ibm.sdwb.build390.mainframe.ReleaseInformation;
import com.ibm.sdwb.build390.metadata.MetadataOperationsInterface;
import com.ibm.sdwb.build390.metadata.info.GeneratedMetadataInfo;
import com.ibm.sdwb.build390.metadata.info.MetadataFormatInfo;
import com.ibm.sdwb.build390.metadata.utilities.MetadataSettings;
import com.ibm.sdwb.build390.metadata.utilities.MetadataValueGenerator;
import com.ibm.sdwb.build390.userinterface.graphic.widgets.CancelableAction;

public class SingleSourceMetadataEditorFrame extends MBInternalFrame implements MBSaveableFrame {

    private String sourcePartName = null;

    private Map keywordComponentHash = new HashMap();
    private Map originalMetaHash = new HashMap();
    private Map originalNumberOfEntries = new HashMap();
    private String savedVersion = null;
    private String checkVersion = null;
    private Vector allEntryBoxes = new Vector();
    private Vector embeddedEntryBoxes = new Vector();
    private String embeddedFieldList = new String();
    private String embeddedRelatedConflictList = new String();
    private Vector neverEditableFields = new Vector();
    private static final Vector relatedFields = new Vector();

    private Object modalLock = new Object();
    private boolean readyToWait = false;

    private JMenuItem saveAsFile = new JMenuItem("Save as");
    private JCheckBoxMenuItem editEmbedded = new JCheckBoxMenuItem("Edit embedded fields");
    JButton checkButton = new JButton("Check");

    private static final String DISTNAME ="DISTNAME";
    private static final String SCODE = "scode";

/* put a list of related fields in a vector
these should be multiple entry fields that must all have the same number
of entries in them. */
    static {
        relatedFields.addElement("PARTTYPE");
        relatedFields.addElement("DISTSRC");
        relatedFields.addElement("DISTLIB");
        relatedFields.addElement("SYSLIB");
        relatedFields.addElement("DISPLN");
        relatedFields.addElement(DISTNAME);
        relatedFields.addElement("FCNS");
    }

    private boolean isScodeSet = false;//PTM4499

    private GeneratedMetadataInfo generatedMetadataInfo;
    private MBMainframeInfo mainframeInfo;
    private LibraryInfo libraryInfo;
    private File reportSavePath;

    //Begin TST3566
    private boolean noSave=false;
    private static String NO_SAVE_MESSAGE="Metadata for this part cannot be saved because the part does not exist in the library";                                 
    private static String LIBRARY_VERSION_PROBLEM="Library version problem";                                 
    //End TST3566

    public SingleSourceMetadataEditorFrame(GeneratedMetadataInfo tempGeneratedMetadataInfo, MBMainframeInfo tempMainframeInfo, LibraryInfo tempLibraryInfo,LogEventProcessor lep) {
        super("Metadata Edit", true, null);
        this.generatedMetadataInfo  =tempGeneratedMetadataInfo;
        this.mainframeInfo = tempMainframeInfo;
        this.libraryInfo = tempLibraryInfo;
        synchronized(modalLock) {
            readyToWait = true;
            try {
                initialize();
            }
            catch(MBBuildException mbe) {
                lep.LogException(mbe);
            }
        }
    }

    public void setReportSaveLocation(File tempReportSavePath) {
        this.reportSavePath = tempReportSavePath;
        if(!reportSavePath.getAbsolutePath().endsWith(java.io.File.separator)) {
            this.reportSavePath = new File(reportSavePath,java.io.File.separator);
        }
    }



    /*		
    initializeValues - create the different panel types and populate them.
*/
    private void initialize()throws LibraryError{

        sourcePartName = generatedMetadataInfo.getFileInfo().getName() !=null ? (generatedMetadataInfo.getFileInfo().getDirectory()!=null ?
                                                                                 generatedMetadataInfo.getFileInfo().getDirectory().trim() : "") +
                         generatedMetadataInfo.getFileInfo().getName() : generatedMetadataInfo.getFileInfo().getMainframeFilename();
        setTitle("Metadata Edit " +sourcePartName);
        layoutPanel();
        initializeMenuAndActions();
        setVisible(true);
// fix the minimum size if the window has never been opened before.
        Dimension tempDim = getSizeInfo("");
        if(tempDim == null) {
            revalidate();
            setSize(350, 500);
        }

// check for certain special metadata conditions and inform the user of them.
        if(embeddedFieldList.trim().length() > 0) {
            problemBox("Embedded Metadata", "Because the following metadata fields are embedded in the part, "+
                       "you will not be able to override them:\n\n"+embeddedFieldList);
        }
        if(embeddedRelatedConflictList.trim().length() > 0) {
            problemBox("Embedded & Related Metadata Conflict", "The following fields are in the previous\nversion of this part but are also " +
                       "embedded.  By default, they will\nnot be included in the new version.\n\n"+embeddedRelatedConflictList);
        }

        if(!generatedMetadataInfo.dontSaveMetadataInLibrary()) {
            doVersionCheck();
        }

        savedVersion = createSettingString(false);
        checkVersion = createSettingString(false);

    }

    private void layoutPanel() throws LibraryError {
        Map values = generatedMetadataInfo.getGeneralMetadataMap();
        MetadataFormatInfo formatInfo = generatedMetadataInfo.getFormatInfo();

        JPanel switchPanel = null;
        JPanel accumPanel = null;
        JPanel singleEntryPanel = null;
        JPanel multipleEntryPanel = null;
        JPanel relatedPanel = null;

        JButton helpButton = new JButton("Help");

        GridBagLayout panelLayout = new GridBagLayout();
        GridBagConstraints c = new GridBagConstraints();
        c.gridx = 1;
        c.gridy = 1;
        c.anchor = GridBagConstraints.CENTER;
        c.fill = GridBagConstraints.BOTH;
        c.gridheight = 1;
        c.gridwidth = 1;
        c.insets = new Insets(2,2,2,2);
        c.ipadx = 2;
        c.ipady = 2;
        c.weightx = 1;
        c.weighty = 1;
        final JPanel editPanel = new JPanel(panelLayout);

        if(libraryInfo.getConfigurationAccess(generatedMetadataInfo.getReleaseInformation().getLibraryName(),false)!=null) {
            isScodeSet = libraryInfo.getConfigurationAccess(generatedMetadataInfo.getReleaseInformation().getLibraryName(),false).getProjectConfigurationSetting(MBConstants.REQUIREDMETADATASECTION,SCODE)!= null;//PTM4499
        }

// create and add the panels
        switchPanel = createSwitchPanel(formatInfo.getSwitchFieldList(), formatInfo.getFieldInfo());
        c.gridy = 1;
        editPanel.add(switchPanel);
        panelLayout.setConstraints(switchPanel, c);
        accumPanel = createAccumulatorPanel(formatInfo.getAccumFieldList(),  formatInfo.getFieldInfo());
        c.gridy = 2;
        editPanel.add(accumPanel);
        panelLayout.setConstraints(accumPanel, c);
        singleEntryPanel = createSingleEntryPanel(formatInfo.getSingleEntryFieldList(), formatInfo.getFieldInfo());
        c.gridy = 3;
        editPanel.add(singleEntryPanel);
        panelLayout.setConstraints(singleEntryPanel, c);
        multipleEntryPanel = createMultipleEntryPanel(formatInfo.getMultipleFields(), formatInfo.getFieldInfo());
        c.gridy = 4;
        editPanel.add(multipleEntryPanel);
        panelLayout.setConstraints(multipleEntryPanel, c);
        relatedPanel = createRelatedPanel(relatedFields, formatInfo.getFieldInfo());
        c.gridy = 5;
        JLabel relLabel = new JLabel("Related Fields (equal number of entries, *NONE* placeholder, blank out to null value)");
        relLabel.setForeground(MBGuiConstants.ColorGroupHeading);
        editPanel.add(relLabel);
        panelLayout.setConstraints(relLabel, c);
        c.gridy = 6;
        editPanel.add(relatedPanel);
        panelLayout.setConstraints(relatedPanel, c);

        Map formatHash = formatInfo.getFieldInfo();
        Map cmvcFields =   generatedMetadataInfo.getLibraryMetadataMap();
        Map embeddedFields =  generatedMetadataInfo.getEmbeddedMetadataMap();
        for(Iterator keywordEnum = keywordComponentHash.keySet().iterator(); keywordEnum.hasNext();) {
            String nextKey = (String) keywordEnum.next();
            DisableableEntryBox currentBox = (DisableableEntryBox) keywordComponentHash.get(nextKey);
            currentBox.setValue(values.get(nextKey));
            if(currentBox.isCaseSensitive()) {
                originalMetaHash.put(nextKey, currentBox.getSettings());
            }
            else {
                originalMetaHash.put(nextKey, currentBox.getSettings().toUpperCase());
            }
            if(currentBox instanceof DisableableMultipleEntryBox) {
                int numberOfEntries = ((DisableableMultipleEntryBox)currentBox).getList().getModel().getSize();
                originalNumberOfEntries.put(nextKey, new Integer(numberOfEntries));
            }
            if(neverEditableFields.contains(nextKey)) {
                currentBox.setSelected(false);
                currentBox.setEnabled(false);
                currentBox.setEditable(false);
            }
            else {
                currentBox.setSelected(cmvcFields.containsKey(nextKey));
                currentBox.setEditable(cmvcFields.containsKey(nextKey));
                if(embeddedFields.containsKey(nextKey)) {
                    embeddedEntryBoxes.addElement(currentBox);
                    embeddedFieldList += nextKey + "    ";
                    currentBox.setEnabled(editEmbedded.isSelected());
                    if(cmvcFields.containsKey(nextKey)) {
                        embeddedRelatedConflictList += nextKey + "    ";
                    }
                }
                else if(cmvcFields.containsKey(nextKey)) {
                    currentBox.setValue(cmvcFields.get(nextKey));
                }
            }
        }



        final JScrollPane tempPane = new JScrollPane(editPanel);
        tempPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        tempPane.getViewport().setScrollMode(JViewport.BLIT_SCROLL_MODE);

        tempPane.getViewport().addChangeListener(new ChangeListener() {
                                                     public void stateChanged(ChangeEvent e) {
                                                         JViewport viewport = (JViewport)e.getSource();
                                                         Dimension newSize = viewport.getViewSize();
                                                         newSize.width = viewport.getExtentSize().width;
                                                         ((JComponent)viewport.getView()).setPreferredSize(newSize); /*INT1981 */
                                                     }
                                                 });



        helpButton.addActionListener(new ActionListener() {
                                         public void actionPerformed(ActionEvent e) {
                                             MBUtilities.ShowHelp("",HelpTopicID.SINGLEMETADATAEDITORPANEL_HELP);
                                         }
                                     });


        getContentPane().setLayout(new BorderLayout());
        getContentPane().add("Center", tempPane);


        Vector actionButtons = new Vector();
        actionButtons.addElement(checkButton);

        addButtonPanel(helpButton, actionButtons);
        setDefaultCloseOperation(JInternalFrame.DO_NOTHING_ON_CLOSE);
    }

    // set up the menu for the editor
    private void initializeMenuAndActions() {
        JMenuBar menuBar = getJMenuBar();
        JMenu viewMenu = new JMenu("View");
        menuBar.add(viewMenu);
        JMenu fileMenu = menuBar.getMenu(0);


        JMenuItem listOpUpdateFields = new JMenuItem("OP=UPD");
        JMenuItem listBuildOrderFields = new JMenuItem("Build Order");
        JMenuItem listProfileFields = new JMenuItem("Profile");
        JMenuItem listRMPFields = new JMenuItem("Related Metadata Part");
        JMenuItem listEmbeddedFields = new JMenuItem("Embedded");
        JMenuItem listCMVCFields = new JMenuItem("Library");
        JMenuItem saveFile = new JMenuItem("Save");
        if(!generatedMetadataInfo.dontSaveMetadataInLibrary()) {
            saveAsFile.setEnabled(true);
            fileMenu.insert(saveFile,0);
            fileMenu.insert(saveAsFile,1);
            fileMenu.insertSeparator(2);
            saveAsFile.setAction(new SaveAsAction());
        }
        else {
            checkButton.setText("Check/Save");
        }

        JMenu fieldMenu = new JMenu("Fields set by ");
        viewMenu.add(fieldMenu);
        fieldMenu.add(listOpUpdateFields);
        fieldMenu.add(listBuildOrderFields);
        fieldMenu.add(listProfileFields);
        fieldMenu.add(listRMPFields);
        fieldMenu.add(listEmbeddedFields);
        fieldMenu.add(listCMVCFields);

        JMenu optionsMenu = new JMenu("Options");
        menuBar.add(optionsMenu);
        optionsMenu.add(editEmbedded);
        listOpUpdateFields.setAction(new PopupMetadataSettingsAction("OP=UPD field settings", generatedMetadataInfo.getOpUpdateMetadataMap()));
        listBuildOrderFields.setAction(new PopupMetadataSettingsAction("Build Order field settings", generatedMetadataInfo.getBuildorderMetadataMap()));
        listProfileFields.setAction(new PopupMetadataSettingsAction("Profile field settings", generatedMetadataInfo.getProfileMetadataMap()));
        listRMPFields.setAction(new PopupMetadataSettingsAction("RMP field settings", generatedMetadataInfo.getMVSRelatedPartMetadataMap()));
        listEmbeddedFields.setAction(new PopupMetadataSettingsAction("Embedded field settings", generatedMetadataInfo.getEmbeddedMetadataMap()));
        listCMVCFields.setAction(new PopupMetadataSettingsAction("Library field settings", generatedMetadataInfo.getLibraryMetadataMap()));


        SaveAsAction saveAction = new SaveAsAction();
        saveAction.setSkipModelPartSave();
        saveFile.setAction(saveAction);
        saveFile.setText("Save");

        checkButton.setAction(new CheckAction());


        editEmbedded.addActionListener(new ActionListener() {
                                           public void actionPerformed(ActionEvent e) {
                                               if(editEmbedded.isSelected()) {
                                                   problemBox("Warning", "Any embedded values you override here will NOT be used unless \n" +
                                                              "you remove them from the part.");
                                               }
                                               for(int i = 0; i < embeddedEntryBoxes.size(); i++) {
                                                   DisableableEntryBox currentBox = (DisableableEntryBox) embeddedEntryBoxes.elementAt(i);
                                                   currentBox.setEnabled(editEmbedded.isSelected());
                                                   currentBox.setEditable(false);
                                                   currentBox.setSelected(false);
                                               }
                                           }
                                       });
    }

    private void doVersionCheck() {

        Set inputSet = new HashSet();
        inputSet.add(generatedMetadataInfo.getFileInfo());
        Set compareSet = new HashSet();
        compareSet.add(generatedMetadataInfo.getBuiltFileInfo());


        Map resultsMap = libraryInfo.getMetadataOperationsHandler().comparisonFacilitatorForPassedInfos(inputSet, compareSet);

        for(Iterator iter =  resultsMap.entrySet().iterator();iter.hasNext();) {
            Map.Entry entry = (Map.Entry)iter.next();
            FileInfo info = (FileInfo)entry.getKey();
            String message = (String)entry.getValue();
            problemBox(LIBRARY_VERSION_PROBLEM, message);

            if(message.indexOf("not found in the library")!=-1) {//TST3566

                noSave = true;
            }

        }


        String anyMetadataVersionWarnings = generatedMetadataInfo.anyWarningsInMetadataVersionComparison();
        if(anyMetadataVersionWarnings!=null && anyMetadataVersionWarnings.trim().length() > 0) {
            problemBox("Library version conflict", anyMetadataVersionWarnings);
        }


    }


// convienience method to create the related metadata panel
    private JPanel createRelatedPanel(Vector fieldNames, Map fieldInfo) {
        return createEntryTypePanel("(Right click to add or remove)", 0, fieldNames, fieldInfo);
    }

// convienience method to create the switch metadata panel
    private JPanel createSwitchPanel(Vector fieldNames, Map fieldInfo) {
        return createEntryTypePanel("Switches", 3, fieldNames, fieldInfo);
    }

// convienience method to create the accumulator metadata panel
    private JPanel createAccumulatorPanel(Vector fieldNames, Map fieldInfo) {
        return createEntryTypePanel("Accumulators", 3, fieldNames, fieldInfo);
    }

// convienience method to create the single entry metadata panel
    private JPanel createSingleEntryPanel(Vector fieldNames, Map fieldInfo) {
        return createEntryTypePanel("Text fields", 2, fieldNames, fieldInfo);
    }

// convienience method to create the multiple entry metadata panel
    private JPanel createMultipleEntryPanel(Vector fieldNames, Map fieldInfo) {
        return createEntryTypePanel("Multiple text fields (Right click to add or remove)", 2, fieldNames, fieldInfo);
    }

// convienience method to create entry panels with debug turned off.
    private JPanel createEntryTypePanel(String panelName, int numCols, Vector fieldNames, Map fieldInfo) {
        return createEntryTypePanel(panelName, numCols, fieldNames, fieldInfo, false);
    }

// create the specified type entry panel.
    private JPanel createEntryTypePanel(String panelName, int numCols, Vector fieldNames, Map fieldInfo, boolean d) {
        FlowedGridLayout tempLayout = new FlowedGridLayout(d);
        JPanel returnPanel = new JPanel(tempLayout);
        //entryBorder = new TitledBorder(new EtchedBorder(), panelName);
        returnPanel.setBorder(BorderFactory.createTitledBorder(LineBorder.createGrayLineBorder(),panelName));
        for(int i = 0; i < fieldNames.size(); i++) {
            String currentName = (String) fieldNames.elementAt(i);
            if(numCols == 0 | !relatedFields.contains(currentName)) {
                MetadataFormatInfo.universalFormatInfo formatInfo = (MetadataFormatInfo.universalFormatInfo) fieldInfo.get(currentName);
                DisableableEntryBox compEntry = createEntryBox(formatInfo);
                keywordComponentHash.put(currentName, compEntry);
                returnPanel.add(compEntry);
                tempLayout.add(compEntry);
            }
        }
        return returnPanel;
    }


// create the appropriate type entry box & set certain default values of it.
    private DisableableEntryBox createEntryBox(MetadataFormatInfo.universalFormatInfo tempInfo) {
        DisableableEntryBox returnBox = null;
        final Dimension listSize = new Dimension(120,55);

        if(tempInfo instanceof MetadataFormatInfo.accumulatorEntry) {
            returnBox = new DisableableNumericBox(tempInfo.getRealName(), tempInfo.getKeyword(), false, 8);
            JComponent comp = returnBox.getEntryComponent();
            // comp.setPreferredSize(textSize);
        }
        else if(tempInfo instanceof MetadataFormatInfo.multipleEntry) {
            MetadataFormatInfo.multipleEntry temp = (MetadataFormatInfo.multipleEntry) tempInfo;
            returnBox = new DisableableMultipleEntryBox(tempInfo.getRealName(), tempInfo.getKeyword(), temp.getLength(), false, new Vector());
            JComponent comp = returnBox.getEntryComponent();
            comp.setPreferredSize(listSize);
        }
        else if(tempInfo instanceof MetadataFormatInfo.singleEntry) {
            MetadataFormatInfo.singleEntry temp = (MetadataFormatInfo.singleEntry) tempInfo;
            returnBox = new DisableableTextBox(tempInfo.getRealName(), tempInfo.getKeyword(), false, temp.getLength(), temp.isQuoted());
            JComponent comp = returnBox.getEntryComponent();
            //comp.setPreferredSize(textSize);
            //comp.setMinimumSize(textSize); 
            //comp.setMaximumSize(textSize);

        }
        else if(tempInfo instanceof MetadataFormatInfo.switchEntry) {
            returnBox = new DisableableCheckBox(tempInfo.getRealName(), tempInfo.getKeyword(), false, false);
        }
        returnBox.setEditable(tempInfo.isReadOnly());
        returnBox.setCaseSensitive(tempInfo.isCaseSensitive());
        returnBox.setQuoted(tempInfo.isQuoted());
        if(tempInfo.isReadOnly()) {
            returnBox.setSelected(false);
            returnBox.setEnabled(false);
        }
        return returnBox;
    }

// output the current metadata settings in the editor to the specified file
// the boolean switch determines whether all values are written, or only ones the user has
// specified to be included.
    private void writeRMPFile(File RMPFileName, boolean writeAll) throws IOException{
        BufferedWriter rmpWriter = new BufferedWriter(new FileWriter(RMPFileName));
        rmpWriter.write(createSettingString(writeAll));
        rmpWriter.close();
    }

// output the current metadata settings in the editor to the specified file
// the boolean switch determines whether all values are written, or only ones the user has
// specified to be included.
    private String createSettingString(boolean writeAll) {
        String settingString = "SRCPART="+sourcePartName+MBConstants.NEWLINE;
        for(Iterator allEntriesEnum = keywordComponentHash.keySet().iterator(); allEntriesEnum.hasNext();) {
            String currKey = (String) allEntriesEnum.next();

            //#DEF.CleanUpSDWB1776:   Don't add SCODE to setting string
            if(currKey.equals(SCODE.toUpperCase())) {
                if(isScodeSet) {//PTM4499
                    continue;
                }
            }

            DisableableEntryBox tempEntry = (DisableableEntryBox) keywordComponentHash.get(currKey);

            if(tempEntry.isSelected()) {
                settingString += tempEntry.getSettings();
            }
            else if(writeAll) {
// ken 8/25/99	if dumping everything, get the original unselected values.				
                settingString += (String) originalMetaHash.get(currKey);
            }
        }
        return settingString;
    }

// output the current metadata settings in the editor to the specified file
// the boolean switch determines whether all values are written, or only ones the user has
// specified to be included.
    private Map createSettingHash() {
        Map settingHash = new HashMap();
        for(Iterator allEntriesEnum = keywordComponentHash.keySet().iterator(); allEntriesEnum.hasNext();) {
            String currKey = (String) allEntriesEnum.next();

            //#DEF.CleanUpSDWB1776:   Don't add SCODE to setting string
            if(currKey.equals(SCODE.toUpperCase())) {
                if(isScodeSet) {//PTM4499
                    continue;
                }
            }

            DisableableEntryBox tempEntry = (DisableableEntryBox) keywordComponentHash.get(currKey);
            if(tempEntry.isSelected()) {
                if(tempEntry.getNumberOfSettings() == 0) {
                    settingHash.put(tempEntry.getKeyword(0), "");
                }
                else {
                    for(int i = 0; i < tempEntry.getNumberOfSettings(); i++) {
                        String settingString = tempEntry.getValue(i);
                        settingHash.put(tempEntry.getKeyword(i), settingString);
                    }
                }
            }
        }
        return settingHash;
    }




//make a string from the fields that are passed in
    private String createFieldHashDump(Map sourceHash) {
        String fieldString = new String();
        final String keywordPlaceHolder = "            ";
        for(Iterator sysFieldEnum = sourceHash.keySet().iterator(); sysFieldEnum.hasNext();) {
            String currKey = (String) sysFieldEnum.next();
            if(!currKey.equals(MBConstants.METADATAVERSIONKEYWORD) && 
               !currKey.equals(VersionPopulator.VERSIONSID_KEY)) {
                fieldString += currKey + keywordPlaceHolder.substring(currKey.length()-2)+ "= ";
                if(sourceHash.get(currKey) instanceof String) {
                    String currVal = (String) sourceHash.get(currKey);
                    fieldString += currVal+"\n";
                }
                else if(sourceHash.get(currKey) instanceof Vector) {
                    Vector currVals = (Vector) sourceHash.get(currKey);
                    if(currVals.size() > 0) {
                        fieldString += (String) currVals.elementAt(0)+"\n";
                        for(int i = 1; i < currVals.size(); i++) {
                            fieldString += keywordPlaceHolder + (String) currVals.elementAt(i)+"\n";
                        }
                    }
                }
            }
        }
        return fieldString;
    }

    private class PopupMetadataSettingsAction extends CancelableAction {
        private String metadataFieldSetting = "";
        private Map metadataFieldMap;

        private PopupMetadataSettingsAction(String tempMetadataFieldSetting, Map tempMetadataFieldMap) {
            super(tempMetadataFieldSetting);
            this.metadataFieldSetting = tempMetadataFieldSetting;
            this.metadataFieldMap = tempMetadataFieldMap;
        }

        public void doAction(ActionEvent e) {
            problemBox(metadataFieldSetting, createFieldHashDump(metadataFieldMap));

        }
    };




    class SaveAsAction extends com.ibm.sdwb.build390.userinterface.graphic.widgets.CancelableAction {

        boolean skipModelPartSave = false;
        FileInfo info = null;
        SaveAsAction() {
            super("Save As");
        }

        void setSkipModelPartSave() {
            this.skipModelPartSave = true;
        }

        public void doAction(ActionEvent e) {
            if(noSave==true) {//TST3566
                problemBox(LIBRARY_VERSION_PROBLEM, NO_SAVE_MESSAGE);
                return;
            }
            try {
                if(skipModelPartSave) {
                    info = generatedMetadataInfo.getFileInfo();
                }
                else {
                    info  = makeNewFileInfo();
                }
                doMetadataCheck(generatedMetadataInfo.dontSaveMetadataInLibrary());
                if(info!=null) {
                    doSave(info);
                }
            }
            catch(HostError hE) {
                handleError(true,info,hE);
            }
            catch(MBBuildException mbe) {
                lep.LogException (mbe);

            }

        }

        private FileInfo makeNewFileInfo() {
            TextEntryDialog newFileName;

            String builtLibraryPartName ="";

            if(generatedMetadataInfo.getBuiltFileInfo().getDirectory()!=null & generatedMetadataInfo.getBuiltFileInfo().getName()!=null) {
                builtLibraryPartName = generatedMetadataInfo.getBuiltFileInfo().getDirectory() +generatedMetadataInfo.getBuiltFileInfo().getName();
            }
            else if(generatedMetadataInfo.getBuiltFileInfo().getName()!=null) {
                builtLibraryPartName = generatedMetadataInfo.getBuiltFileInfo().getName();
            }

            if(builtLibraryPartName != null) {
                newFileName = new TextEntryDialog("Enter the full library name of the file you want this metadata associated with:",builtLibraryPartName, thisFrame);
            }
            else {
                newFileName = new TextEntryDialog("Enter the full library name of the file you want this metadata associated with:",thisFrame);
            }

            if(newFileName.getText()!=null) {
                if(newFileName.getText().trim().length() > 0) {
                    FileInfo info = MetadataQueryUtilities.makeInfoFromName(newFileName.getText().trim());
                    info.setProject(generatedMetadataInfo.getFileInfo().getProject());
                    Set infosSet = new HashSet();
                    infosSet.add(info);
                    libraryInfo.getMetadataOperationsHandler().populateMetadataMapFieldOfPassedInfos(infosSet); 
                    info = (FileInfo)infosSet.iterator().next();
                    if(info.getVersion()!= null) {
                        return info;
                    }
                    else {
                        problemBox("Part not found", "No part named " + newFileName.getText().trim() + " could be found in the library.  Please check the spelling.");
                    }
                }
            }
            return null;
        }

        public void stop() {

        }
    };

    // check the metadata for validity
    private void doMetadataCheck(boolean forceCheck) throws com.ibm.sdwb.build390.MBBuildException{
        if(!generatedMetadataInfo.dontSaveMetadataInLibrary()) {
            saveAsFile.setEnabled(true);
        }
        Map tempHash = null;
        String errorString = new String();
        getStatus().updateStatus("Preparing to validate metadata",false);
        Map currentValueHash = createSettingHash();
        String tempSettingDump = createSettingString(false);
        if(!tempSettingDump.equals(checkVersion) | forceCheck) {
            try {
                File  tempFile = new File(MBGlobals.Build390_path+"misc"+File.separator+"temp");
                getStatus().updateStatus("Storing metadata to temporary file " + tempFile.getAbsolutePath(),false);
                writeRMPFile(tempFile, true);

                com.ibm.sdwb.build390.process.CheckMetadataValidity metadataChecker = new com.ibm.sdwb.build390.process.CheckMetadataValidity(generatedMetadataInfo.getFileInfo(), generatedMetadataInfo.getReleaseInformation(), generatedMetadataInfo.getDriverInformation(), mainframeInfo, libraryInfo, tempFile, thisFrame);                
                metadataChecker.setOutputHeaderLocation(reportSavePath +  "METACHECK-MDE-"+sourcePartName);  
                metadataChecker.externalRun();
                getStatus().updateStatus("Metadata has been validated", false);
                checkVersion = tempSettingDump;


            }
            catch(IOException ioe) {
                lep.LogException("An error occurred writing the temporary RMP file " + MBGlobals.Build390_path+"misc"+File.separator+"temp",  ioe);

            }
        }
    }


    private void handleError(boolean saveOnError,FileInfo info,HostError hE) {
        MBMsgBox displayErrorQuestion = new MBMsgBox("Errors found","The metadata was invalid.  Do you wish to see the results?", parentFrame, true);
        if(displayErrorQuestion.isAnswerYes()) {
            if(hE.getMessage().indexOf(com.ibm.sdwb.build390.process.steps.CheckMetadataValidity.INVALIDFIELDS) >=0) {
                problemBox("Value warning", "Some metadata values you selected were not valid.");
            }
            new MBEdit(hE.getOutputFile().getAbsolutePath(),lep);
        }
        else {
            // we need this empty else i think. when the displayErrorQuestion.isAnswerYes() is false, it intermittently quits.
        }
        try {
            if(saveOnError) {
                MBMsgBox saveQuestion= new MBMsgBox("Errors found","The metadata was invalid.  Do you wish to save anyway? \nNote:\n In case of userbuild, the metadata saved is valid only for this build only. The updates aren't reflected in  the library.", parentFrame, true);
                if(saveQuestion.isAnswerYes()) {
                    doSave(info);

                }
            }
        }
        catch(MBBuildException mbe) {
            lep.LogException(mbe);
        }
    }



    private void doSave(FileInfo info) throws MBBuildException {

        if(noSave==true) {//TST3566
            problemBox(LIBRARY_VERSION_PROBLEM, NO_SAVE_MESSAGE);
            return;
        }

        Map  currentMetadataHash = createSettingHash();
        info.setMetadata(currentMetadataHash);
        if(generatedMetadataInfo.dontSaveMetadataInLibrary()) {
            getStatus().updateStatus("Saved metadata for this build. ", false);
        }
        else {
            if(info!=null) {
                getStatus().updateStatus("Updating part metadata for part " + generatedMetadataInfo.getFileInfo().getName()  + " in release " + generatedMetadataInfo.getReleaseInformation().getLibraryName() ,false);
                Set infosSet = new HashSet();
                infosSet.add(info);
                libraryInfo.getMetadataOperationsHandler().storeMetadataValuesFromPassedInfos(infosSet);
                getStatus().updateStatus("Saved in library.", false);
            }
            else {
                getStatus().updateStatus("Saved failed.", false);
            }
        }
        savedVersion = createSettingString(false);
    }

    class CheckAction extends com.ibm.sdwb.build390.userinterface.graphic.widgets.CancelableAction {

        CheckAction() {
            super("Check");
            if(generatedMetadataInfo.dontSaveMetadataInLibrary()) {
                putValue(NAME, "Check/Save");
            }
            else {
                putValue(NAME, "Check");
            }
        }

        public void doAction(ActionEvent e) {
            FileInfo  info = null;

            if(generatedMetadataInfo.dontSaveMetadataInLibrary()) {
                if(info == null) {
                    info = generatedMetadataInfo.getFileInfo();
                }
            }

            try {
                doMetadataCheck(true);
                if(generatedMetadataInfo.dontSaveMetadataInLibrary()) {
                    doSave(info);
                }
            }
            catch(HostError hE) {
                handleError(generatedMetadataInfo.dontSaveMetadataInLibrary(),info,hE);
            }
            catch(MBBuildException mbe) {
                lep.LogException (mbe);

            }
        }


        public void stop() {
        }
    }




    public boolean saveNeeded() {
        if(savedVersion==null) {
            return false;
        }
        else {
            return !savedVersion.equals(createSettingString(false));
        }
    }

    public boolean save() throws com.ibm.sdwb.build390.MBBuildException{

        Map  currentMetadataHash = createSettingHash();
        FileInfo info = generatedMetadataInfo.getFileInfo();
        if(generatedMetadataInfo.dontSaveMetadataInLibrary()) {
            info.setMetadata(currentMetadataHash);
            getStatus().updateStatus("Saved metadata for this build. ", false);
        }
        else {
            getStatus().updateStatus("Updating part metadata for part " + generatedMetadataInfo.getFileInfo().getName()  + " in release " + generatedMetadataInfo.getReleaseInformation().getLibraryName() ,false);
            Set infosSet = new HashSet();
            infosSet.add(info);
            libraryInfo.getMetadataOperationsHandler().storeMetadataValuesFromPassedInfos(infosSet);
            getStatus().updateStatus("Saved in library.", false);
        }
        savedVersion = createSettingString(false);        return true;
    }



    public Dimension getMinimumSize() {
        Dimension oldPref = new Dimension(110, 80);
        return oldPref;
    }




    public void waitForClose() {
        while(!readyToWait) {
            try {
                Thread.currentThread().sleep(250);
            }
            catch(InterruptedException ie) {
            }
        }
        synchronized(modalLock) {
            if(isVisible()) {
                try {
                    modalLock.wait();
                }
                catch(InterruptedException ie) {
                }
            }
        }
    }


    public void dispose() {
        synchronized(modalLock) {
            modalLock.notify();
        }
        super.dispose(true);
    }

    public void setVisible(boolean visible) {
        boolean wasVisible = isVisible();
        super.setVisible(visible);
        if(wasVisible & !isVisible() & (modalLock!=null)) {//#FixNull: needed to test for null modalLock
            synchronized(modalLock) {
                modalLock.notify();
            }          
        }
    }



    public void internalFrameClosing(javax.swing.event.InternalFrameEvent e) {
        dispose();
    }

}



