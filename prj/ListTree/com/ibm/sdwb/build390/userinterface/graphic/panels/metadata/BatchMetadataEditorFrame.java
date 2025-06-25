package com.ibm.sdwb.build390.userinterface.graphic.panels.metadata;

/*********************************************************************/
/* MetadataModelPartsChooserPage  class for the Build/390 client     */
/* Allows multiple parts to be selected to modelled after            */
/* When a bunch of parts are modelled after a single part, there     */
/* are two options, either we can save all the metadata of that      */
/* model after part(ie. 8 level of metadata) or just the metadata    */
/* saved in library.                                                 */
/*********************************************************************/
//01/04/2005 first update for 2005)
//02/11/2005 SDWB2397 Metadata Model After function (INTF2)
//03/18/2005 TST2142C XMETACHK update.
/*********************************************************************/

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.*;

import javax.swing.BorderFactory;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JMenu;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JTabbedPane;
import javax.swing.SwingUtilities;
import javax.swing.border.LineBorder;
import javax.swing.border.TitledBorder;

import com.ibm.sdwb.build390.*;
import com.ibm.sdwb.build390.filter.criteria.FilterCriteria;
import com.ibm.sdwb.build390.help.HelpTopicID;
import com.ibm.sdwb.build390.info.FileInfo;
import com.ibm.sdwb.build390.info.InfoForMainframePartReportRetrieval;
import com.ibm.sdwb.build390.metadata.info.GeneratedMetadataInfo;
import com.ibm.sdwb.build390.metadata.utilities.MetadataSettings;
import com.ibm.sdwb.build390.metadata.utilities.MetadataValueGenerator;
import com.ibm.sdwb.build390.process.MetadataReport;


/** Create the Metadata chooser page */
public class BatchMetadataEditorFrame extends MBInternalFrame implements  Observer {

    private JButton bHelp = new JButton("Help");
    private JTabbedPane tab = new JTabbedPane(JTabbedPane.TOP,JTabbedPane.WRAP_TAB_LAYOUT);

    private static final String objectKey ="METADATAMODELPARTCHOOSERPAGE";
    private static final String METADATAFILTERKEYWORD = "METADATAFILTERKEYWORD";

    private JMenu actionsMenu=new JMenu("Actions");

    private JButton bSave = null;
    private JButton bEdit = null;
    private JButton bCheck = null;


    private JRadioButton copyAllMenu = new JRadioButton("Copy All Metadata Settings");
    private JRadioButton copyOnlyAlreadySetMenu=new JRadioButton("Copy Only MDE Set Metadata");
    private ButtonGroup group = new ButtonGroup();

    private JMenu viewMenu=new JMenu("View");


    private EditMetadataAction      editMetadataAction;
    private SaveMetadataAction      saveMetadataAction;
    private ViewModelPartMetadataAction viewModelPartMetadataAction;
    private GetMetadataValuesAction getMetadataValuesAction;
    private CheckMetadataAction checkMetadataAction;

    private Map allUICommunicators;
    private FileInfo modelPartInfo;
    private MBBuild build;
    private MetadataValueGenerator metadataValues=null;

    public static String DISTNAME = "DNM";


    /**
    * constructor - Create a MetadataModelPartChooser
    * @param MBBuild tempBuild
    */
    public BatchMetadataEditorFrame(MBBuild build,FileInfo partInfo, Map allUICommunicators) {
        super("Choose parts to prime metadata against model part " + partInfo.getName()  , true, null);
        this.allUICommunicators=allUICommunicators;
        this.modelPartInfo = partInfo;
        this.build = build;

        initializeActions();

        actionsMenu.add(editMetadataAction);
        actionsMenu.add(saveMetadataAction);
        viewMenu.add(viewModelPartMetadataAction);



        getJMenuBar().add(actionsMenu);
        getJMenuBar().add(viewMenu);

        group.add(copyAllMenu);
        group.add(copyOnlyAlreadySetMenu);
        // doesnot default to any thing.force the user to select one.  group.setSelected(copyAllMenu.getModel(), true);
        group.setSelected(copyAllMenu.getModel(), false);
        group.setSelected(copyOnlyAlreadySetMenu.getModel(), false);

        JPanel centerPanel = new JPanel(new BorderLayout());
        getContentPane().add("Center", centerPanel);

        bEdit = new JButton(editMetadataAction);
        bSave = new JButton(saveMetadataAction);
        bCheck = new JButton(checkMetadataAction);

        JPanel subCenter = new JPanel();
        GridBagLayout gridBag = new GridBagLayout();
        GridBagConstraints c = new GridBagConstraints();
        subCenter.setLayout(gridBag);

        c.anchor    = GridBagConstraints.WEST;
        c.weightx = 1.0;
        c.gridheight = 2;
        gridBag.setConstraints(copyAllMenu,c);
        subCenter.add(copyAllMenu);

        c.gridwidth = GridBagConstraints.REMAINDER;
        c.anchor    = GridBagConstraints.WEST;
        c.weightx = 1.0;
        c.gridheight = 1;
        gridBag.setConstraints(copyOnlyAlreadySetMenu,c);
        subCenter.add(copyOnlyAlreadySetMenu);
        subCenter.setBorder(BorderFactory.createTitledBorder(LineBorder.createGrayLineBorder() ,"Metadata Save Options:",TitledBorder.DEFAULT_JUSTIFICATION,TitledBorder.DEFAULT_POSITION,null,MBGuiConstants.ColorGroupHeading));
        Vector actionButtons = new Vector();
        actionButtons.addElement(bEdit);
        actionButtons.addElement(bCheck);
        actionButtons.addElement(bSave);
        addButtonPanel(bHelp, actionButtons);

        initializeTabUI();



        tab.setBorder(BorderFactory.createTitledBorder(LineBorder.createGrayLineBorder() ,"Parts:",TitledBorder.DEFAULT_JUSTIFICATION,TitledBorder.DEFAULT_POSITION,null,MBGuiConstants.ColorGroupHeading));
        centerPanel.add("Center", tab);
        centerPanel.add("South", subCenter);

        bHelp.addActionListener(new MBCancelableActionListener(thisFrame) {
                                    public void doAction(ActionEvent evt) {
                                        MBUtilities.ShowHelp("HDREDITOR",HelpTopicID.BATCHMETADATAEDITORPANEL_HELP);
                                    }
                                } );

        for (Iterator iter=allUICommunicators.keySet().iterator();iter.hasNext();) {
            String key = (String)iter.next();
            int index = tab.indexOfTab(key);
            ((MetadataTabbedTablePanel)tab.getComponentAt(index)).getUICommunicator().updateDisplay((Collection)allUICommunicators.get(key));
        }
        setVisible(true);
    }


    private void initializeActions() {
        editMetadataAction = new EditMetadataAction();
        saveMetadataAction = new SaveMetadataAction();
        viewModelPartMetadataAction = new ViewModelPartMetadataAction();
        getMetadataValuesAction = new GetMetadataValuesAction();
        checkMetadataAction = new CheckMetadataAction();


    }

    private void initializeTabUI() {
        for (Iterator iter=allUICommunicators.keySet().iterator();iter.hasNext();) {
            String key = (String)iter.next();
            UITableModelCommunicator partsDisplay = new UITableModelCommunicator();
            tab.add(key,new MetadataTabbedTablePanel(key,new BatchMetadataTableModel(),partsDisplay,this));
        }

    }



    // bring up editors for all the parts that were selected
    private void startTheEditors() {
        Collection selectedValues = ((MetadataTabbedTablePanel)tab.getSelectedComponent()).getSelectedValues();
        for (Iterator iter =  selectedValues.iterator();iter.hasNext();) {
            //INT1980 
            showSingleEditor((FileInfo)iter.next());
        }

    }



    private void showSingleEditor(final FileInfo  tempInfo) { /*INT19801 */
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
                                          (tempInfo.getDirectory()+tempInfo.getName());
                        throw new GeneralError("There was an error retrieving a metadata report for " + partname);
                    }

                } catch (MBBuildException mbe) {
                    lep.LogException(mbe);
                }


            }
        };

        listener.doAction(new ActionEvent(this,ActionEvent.ACTION_PERFORMED,"MDE Action"));

    }




    class EditMetadataAction extends com.ibm.sdwb.build390.userinterface.graphic.widgets.CancelableAction {

        EditMetadataAction() {
            super("Edit");
            setEnabled(false);
        }

        public void doAction(ActionEvent e) {
            startTheEditors();
        }

        public void stop() {
        }
    }


    class GetMetadataValuesAction extends com.ibm.sdwb.build390.userinterface.graphic.widgets.CancelableAction {

        GetMetadataValuesAction() {
            super("Get Metadata Values For  Part " + modelPartInfo.getName());
            setEnabled(true);
        }

        /* the method to override for whatever action you want to perform in response
        to a click.
        */
        public void doAction(ActionEvent e) {
            InfoForMainframePartReportRetrieval info;

            if (modelPartInfo.getMainframeFilename() !=null && modelPartInfo.getMainframeFilename().indexOf(".")  > 0) {
                info =  new InfoForMainframePartReportRetrieval(modelPartInfo.getMainframeFilename().substring(modelPartInfo.getMainframeFilename().indexOf(".")+1),
                                                                modelPartInfo.getMainframeFilename().substring(0,modelPartInfo.getMainframeFilename().indexOf(".")));

                getStatus().updateStatus("Getting metadata values for part " + modelPartInfo.getMainframeFilename(),false);
            } else {
                info = new InfoForMainframePartReportRetrieval(null,null);
                getStatus().updateStatus("Getting metadata values for part " + modelPartInfo.getDirectory() + modelPartInfo.getName(),false);
            }
            info.setDirectory(modelPartInfo.getDirectory());
            info.setName(modelPartInfo.getName());

            try {
                Set request = new HashSet();


                info.setReportType("ALL");
                request.add(info);
                MetadataReport getMetadata = new  MetadataReport(build,new File(build.getBuildPath()), request, thisFrame);
                getMetadata.setBuildLevel("1");
                getMetadata.externalRun();
                Set returnedFiles = getMetadata.getLocalOutputFiles();
                if (!returnedFiles.isEmpty()) {
                    String valueReportFilename = (String) returnedFiles.iterator().next();
                    if (getMetadata.getReturnCode() > 4) {
                        throw new HostError("There was an error retrieving a metadata report.", valueReportFilename.substring(0,valueReportFilename.indexOf(MBConstants.CLEARFILEEXTENTION)), getMetadata.getReturnCode());
                    }

                    metadataValues = new MetadataValueGenerator(valueReportFilename, modelPartInfo , build.getLibraryInfo().getMetadataOperationsHandler(),thisFrame);
                } else {
                    String partname = info.getPartClass()!=null ? (info.getPartClass() + "."+ info.getPartName()) :
                                      (modelPartInfo.getDirectory()+modelPartInfo.getName());
                    throw new GeneralError("There was an error retrieving a metadata report for " + partname);
                }


            } catch (MBBuildException mbe) {
                lep.LogException(mbe);
            }
        }

        public void stop() {
        }
    }

    class ViewModelPartMetadataAction extends com.ibm.sdwb.build390.userinterface.graphic.widgets.CancelableAction {

        ViewModelPartMetadataAction() {
            super("Model Part " + modelPartInfo.getName() +" Metadata");
            setEnabled(true);
        }

        /* the method to override for whatever action you want to perform in response
        to a click.
        */
        public void doAction(ActionEvent e) {
            //screwy kishore why did you make this thing (metadataValues) as a global variable. ah. research it later.
            if (metadataValues==null) {
                getMetadataValuesAction.doAction(new ActionEvent(this,ActionEvent.ACTION_PERFORMED,"Get Metadata"));
            }
            new MBEdit(metadataValues.getGeneratedMetadataInfo().getMetadataReportName(),false,lep);
        }

        public void stop() {
        }
    }

    class CheckMetadataAction extends  com.ibm.sdwb.build390.userinterface.graphic.widgets.CancelableAction {

        CheckMetadataAction() {
            super("Check");
            setEnabled(false);
        }

        /* the method to override for whatever action you want to perform in response
        to a click.
        */
        public void doAction(ActionEvent e) {
            //write a file.
            try {

                if (!copyAllMenu.isSelected() && !copyOnlyAlreadySetMenu.isSelected()) {
                    new MBMsgBox("Metadata option selection:","Please select a metadata save option.");
                    return;
                }


                getMetadataValuesAction.doAction(new ActionEvent(thisFrame,ActionEvent.ACTION_PERFORMED,GetMetadataValuesAction.NAME));
                MetaCheckHelper checker = new MetaCheckHelper(getSelectedPartsInTabs());
                checker.doMetaCheck();

                getStatus().updateStatus("Metadata has been validated", false);
                if (checker.getFailedParts().size() > 0) {
                    getStatus().updateStatus("Metadata validation failed.", false);
                } else {
                    getStatus().updateStatus("Metadata validation successful.", false);
                }



            } catch (MBBuildException mbe) {
                lep.LogException(mbe);
            }

        }

        public void stop() {
        }

    }





    class SaveMetadataAction extends com.ibm.sdwb.build390.userinterface.graphic.widgets.CancelableAction {

        SaveMetadataAction() {
            super("Save");
            setEnabled(false);
        }

        public void doAction(ActionEvent e) {
            Set   metadataBuilder =null;
            try {
                if (!copyAllMenu.isSelected() && !copyOnlyAlreadySetMenu.isSelected()) {
                    new MBMsgBox("Metadata option selection:","Please select a metadata save option.");
                    return;
                }


                getMetadataValuesAction.doAction(new ActionEvent(thisFrame,ActionEvent.ACTION_PERFORMED,GetMetadataValuesAction.NAME));

                metadataBuilder = getSelectedPartsInTabs();

                MetaCheckHelper metacheck = new MetaCheckHelper(metadataBuilder);
                metacheck.doMetaCheck();
                int metaCheckSize = metacheck.getFailedParts().size();
                int allPartsSize  = metadataBuilder.size();

                if (metaCheckSize > 0 ) {
                    MBMsgBox msg=null;
                    if (metaCheckSize < allPartsSize) {
                        msg =  new MBMsgBox("Warning!","Some parts failed in metacheck validation.\n"+
                                            "Which parts should be saved in library ?\n",thisFrame,true,true);

                        if (msg.isAnswerSuccessful()) {
                            getStatus().updateStatus("Batch update for successfully validated parts", false);
                            metadataBuilder = metacheck.removeFailedParts(metadataBuilder);
                        }
                    } else {
                        msg =  new MBMsgBox("Warning!","All parts failed in metacheck validation.\n"+
                                            "Should metadata for parts be saved in library ?\n",thisFrame,true);

                    }

                    if (msg.isAnswerNone() || !msg.isAnswerYes()) {
                        getStatus().updateStatus("Batch update aborted.",false);
                        return;
                    }
                }

                if(copyAllMenu.isSelected()){
                    new MBMsgBox("Information:","The \"Copy All Metadata Settings\" option saves everything in the library, thus the various levels of metadata (for example, OP=UPD, profiles, and others) are collapsed under the library metadata.");
                }

                if (metadataBuilder.isEmpty()) {
                    throw new GeneralError("No parts found for batch update.\n");
                }


                /*do a remote call and perform bulk updates.
                 the DISTNAME has to be changed for everypart, on the remote end, before we save to db2.
                 if the DISTNAME is empty, then an empty DISTNAME would get stored in library
                 */
                for (Iterator iter =metadataBuilder.iterator();iter.hasNext();) {
                    FileInfo info = (FileInfo)iter.next();
                    Map  distNameMap  = new HashMap();
                    for (Iterator distNameIterator = info.getMetadata().entrySet().iterator();distNameIterator.hasNext();) {
                        Map.Entry entry = (Map.Entry)distNameIterator.next();
                        if (((String)entry.getKey()).startsWith(DISTNAME)) {
                            distNameMap.put(entry.getKey(), entry.getValue());
                        }
                    }
                    info.setMetadata(metacheck.getMetadataHash());
                    info.getMetadata().putAll(distNameMap);
                }

                getStatus().updateStatus("Batch update in progress in library(using model part = " + modelPartInfo.getMainframeFilename()+")",false);
                build.getLibraryInfo().getMetadataOperationsHandler().storeMetadataValuesFromPassedInfos(metadataBuilder);
                getStatus().updateStatus("Batch update successful in library(using model part = " + modelPartInfo.getMainframeFilename()+")",false);
            } catch (MBBuildException mbe) {
                getStatus().updateStatus("Batch update failed in library(using model part = " +  modelPartInfo.getMainframeFilename() +")",false);
                lep.LogException(mbe);
            }

        }


        public void stop() {
        }

    }


// FixMinSize
    public Dimension getMinimumSize() {
        Dimension oldPref = new Dimension(200, 220);
        return oldPref;
    }



    public void update(Observable o,  Object arg) {
        boolean enabled=true;
        for (int i=0;i<tab.getTabCount();i++) {
            MetadataTabbedTablePanel uiPanel = (MetadataTabbedTablePanel)tab.getComponentAt(i);
            enabled  = enabled || (uiPanel.getSelectedValues() !=null ? (uiPanel.getSelectedValues().size() > 0) : false);
        }
        editMetadataAction.setEnabled(enabled);
        saveMetadataAction.setEnabled(enabled);
        checkMetadataAction.setEnabled(enabled);
    }

    private Set getSelectedPartsInTabs() {
        final  Set allParts = new HashSet();
        for (int i=0;i<tab.getTabCount();i++) {
            MetadataTabbedTablePanel uiPanel = (MetadataTabbedTablePanel)tab.getComponentAt(i);
            boolean enabled =  (uiPanel.getSelectedValues() !=null ? (uiPanel.getSelectedValues().size() > 0) : false);
            if (enabled) {
                allParts.addAll(uiPanel.getSelectedValues());
            }
        }
        return allParts;
    }




    /** This is the list that we need to ignore, when we save to library. 
     * Dont add DISTNAME to this one. Since the user has an option on the 
     * MetadataModelDialog to null it out or enter new one
     */
    class IgnoreMetadataCriteria implements FilterCriteria {

        private  java.util.List ignoreList = new ArrayList();

        IgnoreMetadataCriteria() {
            ignoreList.add("ALIAS");
            ignoreList.add("CHILDREN");
            ignoreList.add("DALIAS");
            ignoreList.add("DELETE");
            ignoreList.add("DUPART");
            ignoreList.add("ENTRY");
            ignoreList.add("INACTIVE");
            ignoreList.add("LMOD");
            ignoreList.add("MCSDATA");
            ignoreList.add("NEW");
            ignoreList.add("PARENT");
            ignoreList.add("SHPALIAS");
            ignoreList.add("USRLST");
            ignoreList.add("VERSION");
            ignoreList.add("SRCPART"); /*this is not a metadata keyword, but appears in the notmetadataHash */
        }

        public boolean passes(Object o) {
            String passedString = (String)o;
            return ignoreList.contains(passedString);

        }
    } 

    private class MetaCheckHelper {
        private Set failedParts = null;
        private Set inputParts=null;
        private String outputFileName =null;
        private Hashtable metadataHash = null;

        MetaCheckHelper(Set inputParts) {
            this.inputParts= inputParts;
        }


        Set getFailedParts() {
            return failedParts;
        }

        Set removeFailedParts(Set input) {
            for (Iterator failedPartsIterator=getFailedParts().iterator();failedPartsIterator.hasNext();) {
                Object singleFailedPart = failedPartsIterator.next();
                if (input.contains(singleFailedPart)) {
                    input.remove(singleFailedPart);
                }
            }
            return input;
        }


        String getOutputMetaCheckFileName() {
            return outputFileName;
        }

        Hashtable getMetadataHash() {
            return metadataHash;
        }


        void doMetaCheck() throws MBBuildException {
            com.ibm.sdwb.build390.process.CheckMetadataValidity metadataChecker =null;
            try {
                if (metadataValues!=null) {
                    metadataHash = MetadataSettings.dumpMetadataToHash(copyAllMenu.isSelected(),metadataValues.getGeneratedMetadataInfo());

                    if (metadataHash.isEmpty()) {
                        throw new GeneralError("No metadata values found for batch update.\n");
                    }

                    if (copyAllMenu.isSelected()) { /*TST2113 only in case of "CopyAllMetadata Settings options */
                        metadataHash = MetadataSettings.pruneEntries(metadataHash, new IgnoreMetadataCriteria());
                    }


                    boolean isScodeSet = build.getConfigInfo(MBConstants.REQUIREDMETADATASECTION,"scode")!= null;//PTM4499

                    metadataHash = MetadataSettings.buildSettings(metadataValues.getGeneratedMetadataInfo().getFormatInfo(),metadataHash, isScodeSet); //PTM4499

                    File metaCheckFile = new File(build.getBuildPath() + "metacheck"+(new Random()).nextInt()+".ftp");

                    BufferedWriter metaCheckWriter =  new BufferedWriter(new FileWriter(metaCheckFile));

                    for (Iterator iter = inputParts.iterator();iter.hasNext();) {
                        FileInfo info = (FileInfo)iter.next();
                        writeHeader(metaCheckWriter,info);
                        MetadataSettings.writeToFile(metaCheckWriter, getMetadataToCheck(isScodeSet));
                    }
                    metaCheckWriter.close();
                    metadataChecker = new com.ibm.sdwb.build390.process.CheckMetadataValidity(null, build.getReleaseInformation(),build.getDriverInformation(), build.getMainframeInfo(), build.getLibraryInfo(), metaCheckFile, thisFrame);                
                    metadataChecker.setOutputHeaderLocation(build.getBuildPath() + "METACHECK-BATCH");  
                    metadataChecker.externalRun();
                }
            } catch (IOException ioe) {
                throw new GeneralError("An  error occurred while trying to write metacheck file");
            } catch (HostError hE) {
                MBMsgBox displayErrorQuestion = new MBMsgBox("Errors found","The metadata was invalid.  Do you wish to see the results?", parentFrame, true);
                if (displayErrorQuestion.isAnswerYes()) {
                    if (hE.getMessage().equals(com.ibm.sdwb.build390.process.steps.CheckMetadataValidity.INVALIDFIELDS)) {
                        problemBox("Value warning", "Some metadata values you selected were not valid.");
                    }

                    new MBEdit(hE.getOutputFile().getAbsolutePath(),lep);
                }

            } finally {
                if (metadataChecker!=null) {
                    failedParts = metadataChecker.getFailedParts();
                    outputFileName = metadataChecker.getOutputMetaCheckFileName();
                }
            }


        }


        private Hashtable getMetadataToCheck(boolean isScodeSet) {
            //always make the dumpMetadataHash to true. TST3294
            Hashtable tempMetadataHash = MetadataSettings.dumpMetadataToHash(true,metadataValues.getGeneratedMetadataInfo());
            if (copyAllMenu.isSelected()) { /*TST2113 only in case of "CopyAllMetadata Settings options */
                metadataHash = MetadataSettings.pruneEntries(metadataHash, new IgnoreMetadataCriteria());
            }

            tempMetadataHash = MetadataSettings.buildSettings(metadataValues.getGeneratedMetadataInfo().getFormatInfo(),tempMetadataHash, isScodeSet); //PTM4499

            return tempMetadataHash;

        }




        private void writeHeader(BufferedWriter writer,FileInfo info) throws IOException {
            final String SRCPART = "SRCPART=";
            final String DIR = "DIR=";
            final String PATH = "PATH=";


            String mvsPartClass = null;
            String mvsPartName  = null;

            if (info.getMainframeFilename().indexOf(".") <0) {
                info.setMainframeFilename(""); //wipe of the mainframename since if the fileinfo was generated by partlistquery, it will have stuff like C<randomno>
            }

            if (info.getMainframeFilename()!=null && info.getMainframeFilename().trim().length() > 0) {
                mvsPartName  = info.getMainframeFilename().substring(info.getMainframeFilename().indexOf("."));
                mvsPartClass = info.getMainframeFilename().substring(0,info.getMainframeFilename().indexOf(".")+1);
            }

            String firstLine  = SRCPART;
            String secondLine  = DIR;
            String thirdLine  = PATH;

            if (mvsPartClass!=null & mvsPartName!=null) {
                firstLine += "\'" + mvsPartClass + "." + mvsPartName +"\'";
                writer.write(firstLine,0,firstLine.length());
                writer.newLine();
            } else {
                firstLine += "\'\'";
                secondLine+=  (info.getDirectory()!=null ?  "\'" + info.getDirectory().trim() +"\'" : "\'\'");
                thirdLine +=  "\'"+info.getName().trim() + "\'";
                writer.write(firstLine,0,firstLine.length());
                writer.newLine();
                writer.write(secondLine,0,secondLine.length());
                writer.newLine();
                writer.write(thirdLine,0,thirdLine.length());
                writer.newLine();
            } 


        }

    }

    public Dimension getPreferredSize() {
        return new Dimension(490,405);

    } 
}



