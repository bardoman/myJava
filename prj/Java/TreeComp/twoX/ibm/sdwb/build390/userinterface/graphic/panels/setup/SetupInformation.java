package com.ibm.sdwb.build390.userinterface.graphic.panels.setup;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.io.File;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.*;

import javax.swing.*;

import com.ibm.sdwb.build390.*;
import com.ibm.sdwb.build390.filter.DefaultFilter;
import com.ibm.sdwb.build390.filter.criteria.FilterCriteria;
import com.ibm.sdwb.build390.help.HelpTopicID;
import com.ibm.sdwb.build390.library.LibraryInfo;
import com.ibm.sdwb.build390.library.clearcase.ClearcaseLibraryInfo;
import com.ibm.sdwb.build390.library.cmvc.CMVCLibraryInfo;
import com.ibm.sdwb.build390.library.fakelib.FakeLibraryInfo;
import com.ibm.sdwb.build390.library.userinterface.LibraryInfoEditor;
import com.ibm.sdwb.build390.logprocess.LogEventProcessor;
import com.ibm.sdwb.build390.user.Mode;
import com.ibm.sdwb.build390.user.Setup;
import com.ibm.sdwb.build390.user.SetupManager;
import com.ibm.sdwb.build390.userinterface.graphic.actions.ActionBuilder;
import com.ibm.sdwb.build390.userinterface.graphic.actions.ActionConfigurer;
import com.ibm.sdwb.build390.userinterface.graphic.actions.UserInterfaceActionBarSupport;
import com.ibm.sdwb.build390.userinterface.graphic.widgets.CancelableAction;

/*********************************************************************/
// 05/03/99 defect306           make a local copy of setup when you open this, and if you save, synch the original with it
//12/23/2002 #DEF.PTM2155: warn user that saved setup is only for future builds
//05/09/2003 #DEF.TST1221:  Null Pointer creating new Lib info
//09/09/2003 #DEF.TST1455  fix redundant rmi server compatibility msgs.
/*********************************************************************/

public class SetupInformation extends MBModalStatusFrame implements MBSaveableFrame {

    private boolean stopped=false;
    private boolean mainSetup = true;
    private JComboBox choiceMainframe;
    private JComboBox choiceLibrary;
    //private JComboBox choceServiceServer;

    private Action removeLibraryAction = new RemoveLibraryEntry();
    private Action editLibraryAction = new EditLibraryEntry();
    private Action removeMainframeAction = new RemoveMVSEntry();
    private Action editMainframeAction = new  EditMVSEntry();
    private Action testMVSAction = new TestMVSEntry();
    private JButton btnSave;
    private JButton btnHelp;
    private JTextField tfEditor;
    private JCheckBox rBtnCustomEditor  = new JCheckBox("Use custom editor ");

    private List<Setup> virtualSetupList = null; /* by default this null.Should be used by the classes that want a changed  setup, but not update SetupManager */
    private Set<MBMainframeInfo> notRemovedMainframeSet  =  new HashSet<MBMainframeInfo>(); 
    private Set<LibraryInfo>     notRemovedLibrarySet    =  new HashSet<LibraryInfo>(); 


//this has become too ugly :). We need capability to create Immutable SetupManager which would allow in memory updates
//The isUsingVirtualSetup just works for now.
    public SetupInformation(MBAnimationStatusWindow ipFrame) {
        this(ipFrame,null);
    }

    public SetupInformation(MBAnimationStatusWindow ipFrame,List<Setup> virtualSetupList) {
        super("Setup Dialog", (JInternalFrame) ipFrame, null);
        this.virtualSetupList = virtualSetupList;
        init();
    }

    private boolean isUsingVirtualSetup() {
        return virtualSetupList !=null;
    }



    private void layoutPanel() {
        JPanel centerPanel = new JPanel(new javax.swing.SpringLayout());

        Box editorBox = Box.createHorizontalBox();
        editorBox.add(tfEditor);
        editorBox.add(Box.createHorizontalStrut(5));
        editorBox.add(new JButton(new EditorPathAction()));

        JLabel mvsServerLabel = new JLabel(MBConstants.productName+" Server");
        mvsServerLabel.setLabelFor(choiceMainframe);
        JLabel libraryServerLabel = new JLabel(getLibraryTextPrefix() +" Server");
        libraryServerLabel.setLabelFor(choiceLibrary);

        //JLabel serviceServerLabel = new JLabel("Service server");
        //serviceServerLabel.setLabelFor(choceServiceServer);

        centerPanel.add(mvsServerLabel);
        centerPanel.add(choiceMainframe);
        centerPanel.add(libraryServerLabel);
        centerPanel.add(choiceLibrary);
        //centerPanel.add(serviceServerLabel);
        //centerPanel.add(choceServiceServer);
        centerPanel.add(rBtnCustomEditor);
        centerPanel.add(editorBox);
        com.ibm.sdwb.build390.userinterface.graphic.utilities.GeneralUtilities.makeCompactGrid(centerPanel, -1, 2, 5, 5, 3, 3);
        getContentPane().add(BorderLayout.CENTER, centerPanel);

    }

    private void init() {
        //

        JMenu serverMenu = new JMenu("Server");
        getJMenuBar().add(serverMenu);
        serverMenu.add(new AddMVSEntry());
        serverMenu.add(removeMainframeAction);
        serverMenu.add(editMainframeAction);
        serverMenu.add(testMVSAction);

        JMenu libraryMenu = new JMenu(getLibraryTextPrefix());
        getJMenuBar().add(libraryMenu);
        JMenu libraryAddMenu = new JMenu("Add");
        libraryMenu.add(libraryAddMenu);
        libraryMenu.add(removeLibraryAction);
        libraryMenu.add(editLibraryAction);
        fillActionBar(new SetupConfigurer());

        choiceMainframe = createMainframeCombo();
        choiceMainframe.setEditable(false);
        choiceMainframe.setBackground(MBGuiConstants.ColorFieldBackground);

        choiceLibrary = createLibraryCombo();
        choiceLibrary.setEditable(false);
        choiceLibrary.setBackground(MBGuiConstants.ColorFieldBackground);

        /*choceServiceServer = new JComboBox();
        choceServiceServer.setEditable(false);
        choceServiceServer.setBackground(MBGuiConstants.ColorFieldBackground);

        Vector tempBpsInfo = null;
        tempBpsInfo = mbs_.GetBpsInfoVector();
        if (tempBpsInfo != null) {
            for (int i = 0; i < tempBpsInfo.size(); i++) {
                MBBpsInfo tempBps = (MBBpsInfo) tempBpsInfo.elementAt(i);
                choceServiceServer.addItem(tempBps.getHost()+"@"+tempBps.getPort());
            }
        }*/


        tfEditor = new JTextField(20);
        tfEditor.setPreferredSize(new Dimension(20, tfEditor.getPreferredSize().height));
        tfEditor.setBackground(MBGuiConstants.ColorFieldBackground);

        btnSave = new JButton("Save");
        btnSave.setForeground(MBGuiConstants.ColorActionButton);
        btnHelp = new JButton("Help");
        btnHelp.setForeground(MBGuiConstants.ColorHelpButton);
        Vector actionButtons = new Vector();
        actionButtons.addElement(btnSave);
        btnHelp.setForeground(MBGuiConstants.ColorHelpButton);
        addButtonPanel(btnHelp, actionButtons);

        if (choiceLibrary.getItemCount() > 0) {
            removeLibraryAction.setEnabled(true);
            editLibraryAction.setEnabled(true);
            firePropertyChange(DevelopmentSetupActionsBuilder.SETUP_LIBRARY_SELECTED,0,1);
        } else {
            removeLibraryAction.setEnabled(false);
            editLibraryAction.setEnabled(false);
            firePropertyChange(DevelopmentSetupActionsBuilder.SETUP_LIBRARY_SELECTED,0,-1);
        }

        if (choiceMainframe.getItemCount() > 0) {
            removeMainframeAction.setEnabled(true);
            editMainframeAction.setEnabled(true);
            testMVSAction.setEnabled(true);
        } else {
            removeMainframeAction.setEnabled(false);
            editMainframeAction.setEnabled(false);
            testMVSAction.setEnabled(false);
        }
        btnHelp.addActionListener(new MBCancelableActionListener(thisFrame) {
                                      public void doAction(ActionEvent evt) {
                                          // if help path is set use help facility  // ShowHelp
                                          if (SetupManager.getSetupManager().hasSetup()) {
                                              MBUtilities.ShowHelp("HDRSETUP",HelpTopicID.SETUPDLG_HELP);
                                          } else {
                                              // otherwise show help for setup
                                              String hlpfilenm = new String(MBGlobals.Build390_path+"misc" + java.io.File.separator + "setup.txt");
                                              File hlpfile = new File(hlpfilenm);
                                              if (hlpfile.exists()) {
                                                  MBEditPanel editPanel = new MBEditPanel(hlpfilenm,lep); // don't use MBEdit here because setup has not been done
                                              }
                                          }
                                      }
                                  } );


        choiceLibrary.addItemListener(new ItemListener() {
                                          public void itemStateChanged(ItemEvent evt) {
                                              if (choiceLibrary.getItemCount() > 0) {
                                                  removeLibraryAction.setEnabled(true);
                                                  editLibraryAction.setEnabled(true);
                                                  if (choiceLibrary.getSelectedIndex() > -1 & choiceLibrary.getSelectedIndex() < SetupManager.getSetupManager().getLibraryInfoSet().size()) {
                                                      LibraryInfo tempLib = (LibraryInfo)choiceLibrary.getSelectedItem();
                                                      choiceLibrary.setToolTipText(tempLib.getDescriptiveString());
                                                  } else {
                                                      choiceLibrary.setToolTipText(new String());
                                                  }
                                                  firePropertyChange(DevelopmentSetupActionsBuilder.SETUP_LIBRARY_SELECTED,0,1);
                                              } else {
                                                  removeLibraryAction.setEnabled(false);
                                                  editLibraryAction.setEnabled(false);
                                                  firePropertyChange(DevelopmentSetupActionsBuilder.SETUP_LIBRARY_SELECTED,0,-1);
                                              }
                                          }
                                      } );

        choiceMainframe.addItemListener(new ItemListener() {
                                            public void itemStateChanged(ItemEvent evt) {
                                                if (choiceMainframe.getItemCount() > 0) {
                                                    removeMainframeAction.setEnabled(true);
                                                    editMainframeAction.setEnabled(true);
                                                    testMVSAction.setEnabled(true);
//Ken, 4/28/99 Check we're in bounds
                                                    if (choiceMainframe.getSelectedIndex() > -1 & choiceMainframe.getSelectedIndex() < SetupManager.getSetupManager().getMainframeInfoSet().size()) {
                                                        MBMainframeInfo tempMain = (MBMainframeInfo)choiceMainframe.getSelectedItem();
                                                        choiceMainframe.setToolTipText(tempMain.getMainframeAccountInfo());
                                                    } else {
                                                        choiceMainframe.setToolTipText(new String());
                                                    }
                                                } else {
                                                    removeMainframeAction.setEnabled(false);
                                                    editMainframeAction.setEnabled(false);
                                                    testMVSAction.setEnabled(false);
                                                    choiceMainframe.setToolTipText(new String());
                                                }
                                            }
                                        } );
        btnSave.addActionListener(new MBCancelableActionListener(thisFrame) {
                                      public void doAction(ActionEvent evt) {
                                          if (save()) {
                                              lep.LogPrimaryInfo("INFORMATION: ",SetupManager.getSetupManager().toString(),false);
                                              dispose(false);
                                          }
                                      }
                                  } );

        // init fields
        if (choiceMainframe.getItemCount() > 0) {
            choiceMainframe.setToolTipText(SetupManager.getSetupManager().getCurrentMainframeInfo().getMainframeAccountInfo());
        }
        // EditorSelect
        rBtnCustomEditor.setSelected(!SetupManager.getSetupManager().IsDefaultEditorSelected());
        tfEditor.setText(SetupManager.getSetupManager().getEditorPath());

        setDefaultCloseOperation(JInternalFrame.DO_NOTHING_ON_CLOSE);
        layoutPanel();

        setVisible(true);
    }



    private void fillActionBar(com.ibm.sdwb.build390.userinterface.graphic.actions.ActionConfigurer configurer) {
        ActionBuilder  actionBuilder = ((UserInterfaceActionBarSupport)MBClient.getCommandLineSettings().getMode()).getSetupActionBar(configurer);
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
            getLEP().LogException("An error occurred while trying to instantiate an Action: "+actionClass.getName()+".\nContact development with this stack trace.\n\n", ivte);
        }
        return null;
    }

    private JComboBox createMainframeCombo() {

        Object currentData = null;
        if (!isUsingVirtualSetup() && !SetupManager.getSetupManager().getMainframeInfoSet().isEmpty()) {
            currentData = SetupManager.getSetupManager().getCurrentMainframeInfo();
        }

        List mainframeList = new ArrayList();
        if (isUsingVirtualSetup() && !virtualSetupList.isEmpty()) {
            currentData = ((Setup)((LinkedList)virtualSetupList).getLast()).getMainframeInfo();
            for (Setup tempSetup: virtualSetupList) {
                mainframeList.add(tempSetup.getMainframeInfo());
            }
        }

        return createCombo(choiceMainframe, Arrays.asList((Object[])SetupManager.getSetupManager().getMainframeInfoSet().toArray().clone()),currentData, mainframeList.toArray());
    }

    private JComboBox createLibraryCombo() {

        Object currentData = null;
        if (!isUsingVirtualSetup() && !SetupManager.getSetupManager().getLibraryInfoSet().isEmpty()) {
            currentData = SetupManager.getSetupManager().getCurrentLibraryInfo();
        }

        List libraryList = new ArrayList();
        if (isUsingVirtualSetup() && !virtualSetupList.isEmpty()) {
            currentData = ((Setup)((LinkedList)virtualSetupList).getLast()).getLibraryInfo();
            for (Setup tempSetup :  virtualSetupList) {
                libraryList.add(tempSetup.getLibraryInfo());
            }
        }


        DefaultFilter filter = new DefaultFilter(new DisplayeableInfoCriteria());
        filter.filter(Arrays.asList((Object[])SetupManager.getSetupManager().getLibraryInfoSet().toArray().clone()));
        return  createCombo(choiceLibrary,filter.matched(),currentData, libraryList.toArray());
    }


    private JComboBox createCombo(JComboBox combo,Collection infoCollection,Object currentData,Object[] virtualData) {
        Object[] tempMainArray = getComboDataArray(infoCollection, virtualData);

        if (tempMainArray !=null) {
            combo = new JComboBox(tempMainArray);
            combo.setSelectedItem(currentData);
        } else {
            combo = new JComboBox();
        }

        return combo;

    }
    private Object[] getComboDataArray(Collection infoCollection,Object[] virtualData) {
        Set tempSet = new HashSet();
        Set temp1Set = new HashSet();

        if (virtualData!=null && virtualData.length > 0) {
            tempSet.addAll(Arrays.asList(virtualData));
        }

        if (isUsingVirtualSetup()) {
            for (Iterator  iter = infoCollection.iterator();iter.hasNext();) {
                Object obj = iter.next();
                for (Iterator iter1 = tempSet.iterator();iter1.hasNext();) {
                    Object obj1 = iter1.next();
                    if (obj.toString().equals(obj1.toString())) {
                        break;
                    }
                    temp1Set.add(obj);
                }

            }
        } else {
            temp1Set.addAll(infoCollection);
        }

        tempSet.addAll(temp1Set);


        if (!tempSet.isEmpty()) {
            return(Object[])tempSet.toArray();
        } else {
            return null;
        }

    }

    public void dispose() {
        dispose(true);
    }

    public void dispose(boolean saveQuestion) {
        super.dispose(saveQuestion);
        synchronized(this) {
            notifyAll();
        }
    }


    /* just return a Setup object that the current combo has. Only used in virtualSetupList !=null mode */
    public Setup getSetup() {
        if (isUsingVirtualSetup()) {
            return SetupManager.getSetupManager().createSetupInstance((LibraryInfo)choiceLibrary.getSelectedItem(), (MBMainframeInfo)choiceMainframe.getSelectedItem(),tfEditor.getText(),rBtnCustomEditor.isSelected());
        } else {
            throw new RuntimeException("ERROR! This is only supported in the virtual setup mode.");
        }
    }

    public boolean saveNeeded() {
        boolean needed = false;
        needed = needed | (SetupManager.getSetupManager().IsDefaultEditorSelected() == rBtnCustomEditor.isSelected());
        needed = needed | (choiceLibrary.getItemCount() > 0 ? (SetupManager.getSetupManager().getCurrentLibraryInfo() != choiceLibrary.getSelectedItem()) : false);
        needed = needed | (choiceMainframe.getItemCount() > 0 ? (SetupManager.getSetupManager().getCurrentMainframeInfo() != choiceMainframe.getSelectedItem()) : false);
        needed = needed | (!notRemovedLibrarySet.isEmpty() | !notRemovedMainframeSet.isEmpty());

        if (tfEditor.getText() !=null) {
            if (tfEditor.getText().trim().length() > 0) {
                needed = needed | (!SetupManager.getSetupManager().getEditorPath().equals(tfEditor.getText()));
            }
        }

        return needed;
    }

    public boolean save() {
        String data;
        String errorString = new String();

        SetupManager.getSetupManager().setDefaultEditorSelected(!rBtnCustomEditor.isSelected());

        if (choiceLibrary.getItemCount() < 1) {
            errorString += "You must enter a family.\n";
        } else {
            for (LibraryInfo tempLibraryInfo: notRemovedLibrarySet) {
                if (!isUsingVirtualSetup()) {
                    SetupManager.getSetupManager().removeLibraryInfo(tempLibraryInfo); 
                } else {
                    boolean isRemoved = false;
                    for (Iterator<Setup> iterSetup = virtualSetupList.iterator(); (iterSetup.hasNext() && isRemoved);) {
                        Setup tempSetup  = iterSetup.next();
                        if (tempSetup.getLibraryInfo().equals(tempLibraryInfo)) {
                            iterSetup.remove();
                            isRemoved =true;
                        }
                    }

                }
            }
            if (!isUsingVirtualSetup()) {
                for (int i = 0; i<choiceLibrary.getItemCount(); i++) {
                    SetupManager.getSetupManager().addLibraryInfo((LibraryInfo)choiceLibrary.getItemAt(i));
                }
                SetupManager.getSetupManager().setCurrentLibrary((LibraryInfo)choiceLibrary.getSelectedItem());
            }
        }

        if (choiceMainframe.getItemCount() < 1) {
            errorString += "You must enter a server.\n";
        } else {
            for (MBMainframeInfo tempMainInfo: notRemovedMainframeSet) {
                if (!isUsingVirtualSetup()) {
                    SetupManager.getSetupManager().removeMainframeInfo(tempMainInfo);
                } else {
                    boolean isRemoved = false;
                    for (Iterator<Setup> iterSetup = virtualSetupList.iterator(); (iterSetup.hasNext() && isRemoved);) {
                        Setup tempSetup  = iterSetup.next();
                        if (tempSetup.getMainframeInfo().equals(tempMainInfo)) {
                            iterSetup.remove();
                            isRemoved =true;
                        }
                    }

                }

            }
            if (!isUsingVirtualSetup()) {
                for (int i = 0; i<choiceMainframe.getItemCount(); i++) {
                    SetupManager.getSetupManager().addMainframeInfo((MBMainframeInfo)choiceMainframe.getItemAt(i));
                }
                SetupManager.getSetupManager().setCurrentMainframe((MBMainframeInfo)choiceMainframe.getSelectedItem());
            }
        }

        // verify editor path EditorSelect
        data = tfEditor.getText();

        if (rBtnCustomEditor.isSelected()) {
            if (data.length() > 0) {
                File bf = new File(data);
                if (!bf.exists()) {
                    errorString += "You must enter a valid editor path";
                } else {
                    if (virtualSetupList ==null) {
                        SetupManager.getSetupManager().setEditorPath(data);
                    }
                }
            } else {
                errorString += "You must enter a valid editor path";
            }
        }

        if (errorString.trim().length() > 0) {
            new MBMsgBox("Setup incomplete", errorString);
            return false;
        } else {
            // }

            //Begin TST2340
            StringBuffer  warningMsg = new StringBuffer();
            if (virtualSetupList==null) {
                warningMsg.append("This setup will apply only to subsequent build objects and not those created prior to this save operation.\n");
                warningMsg.append("Some function panels rely on setup information. These may need to be re-opened for proper operation.\n");
            } else {
                warningMsg.append("Note:\n\n");
                warningMsg.append("These setup changes will apply only to this process.\n\n");
                warningMsg.append("To change setup for all processes use the Option, Setup menu selection on the main window.");
                warningMsg.append("The setup changes are not saved.");
            }
            //End TST2340

            new MBMsgBox("Warning", warningMsg.toString());

            if (!isUsingVirtualSetup()) {
                SetupManager.getSetupManager().saveSetup();
            }
        }
        return true;
    }

    public void internalFrameClosing(javax.swing.event.InternalFrameEvent e) {
        dispose(true);
    }

    class AddNOLIBEntry extends CancelableAction {

        AddNOLIBEntry() {
            super("NOLIB");
        }

        public void doAction(ActionEvent e) {
            FakeLibraryInfo fakeLibInfo = new FakeLibraryInfo();
            LibraryInfoEditor  libEditor = fakeLibInfo.getUserinterfaceFactory().getLibraryInfoEditor(thisFrame);
            if (fakeLibInfo.getProcessServerName()!=null) {
                choiceLibrary.addItem(fakeLibInfo);
                choiceLibrary.setSelectedItem(fakeLibInfo);
            }


        }
    }

    class AddCMVCEntry extends CancelableAction {

        AddCMVCEntry() {
            super("CMVC");
        }

        public void doAction(ActionEvent e) {
            CMVCLibraryInfo cmvcInfo = new CMVCLibraryInfo();
            LibraryInfoEditor  libEditor = cmvcInfo.getUserinterfaceFactory().getLibraryInfoEditor(thisFrame);
            if (cmvcInfo.getProcessServerName()!=null) {
                choiceLibrary.addItem(cmvcInfo);
                choiceLibrary.setSelectedItem(cmvcInfo);
            }
        }
    }

    class AddRationalEntry extends CancelableAction {

        AddRationalEntry() {
            super("Rational");
        }

        public void doAction(ActionEvent e) {
            ClearcaseLibraryInfo ratInfo = new ClearcaseLibraryInfo();
            LibraryInfoEditor  libEditor = ratInfo.getUserinterfaceFactory().getLibraryInfoEditor(thisFrame);
            if (ratInfo.getProjectVob()!=null) {
                choiceLibrary.addItem(ratInfo);
                choiceLibrary.setSelectedItem(ratInfo);
            }
        }
    }

    private class RemoveLibraryEntry extends CancelableAction {

        RemoveLibraryEntry() {
            super("Remove");
        }

        public void doAction(ActionEvent e) {
            if (choiceLibrary.getSelectedIndex() > -1) {
                notRemovedLibrarySet.add((LibraryInfo)choiceLibrary.getSelectedItem());
                choiceLibrary.removeItem(choiceLibrary.getSelectedItem());
            }

            if (choiceLibrary.getItemCount() <= 0) {
                removeLibraryAction.setEnabled(false);
                editLibraryAction.setEnabled(false);
                thisFrame.firePropertyChange(DevelopmentSetupActionsBuilder.SETUP_LIBRARY_SELECTED,0,-1);
            } else {
                choiceLibrary.setSelectedIndex(0);
            }

        }
    }

    private class EditLibraryEntry extends CancelableAction {

        EditLibraryEntry() {
            super("Edit");
        }

        public void doAction(ActionEvent e) {
            int selectionIndex = choiceLibrary.getSelectedIndex(); 
            if (selectionIndex > -1) {
                LibraryInfo tempLib = (LibraryInfo) choiceLibrary.getSelectedItem();
                LibraryInfoEditor newFam =tempLib.getUserinterfaceFactory().getLibraryInfoEditor(thisFrame);
                choiceLibrary.removeItem(tempLib);
                choiceLibrary.insertItemAt(tempLib, selectionIndex);
                choiceLibrary.setSelectedIndex(selectionIndex);
            }
        }
    }

    // Editor button EditorSelect
    private class EditorPathAction extends CancelableAction {
        private EditorPathAction() {
            super("Browse");
        }
        public void doAction(ActionEvent evt) {
            String currDir = tfEditor.getText();

            MBFileChooser openFileChooser = new MBFileChooser();

            if (currDir.trim().length() > 0)openFileChooser.setCurrentDirectory(new File(currDir));

            openFileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);

            openFileChooser.setDialogTitle("Browse for editor to be used");

            openFileChooser.showDialog(thisFrame, "Select");

            if (openFileChooser.getSelectedFile() != null) {
                String tmpStr=openFileChooser.getSelectedFile().getAbsolutePath();
                tfEditor.setText(tmpStr);
            }
        }
    }

    class TestLibraryEntry extends CancelableAction {

        TestLibraryEntry() {
            super("Test");
        }

        public void doAction(ActionEvent e) {
            if (choiceLibrary.getSelectedIndex() > -1) {
                LibraryInfo tempLib = (LibraryInfo) choiceLibrary.getSelectedItem();

                // show status
                // Build the query
                getStatus().updateStatus("Checking connection to library " +tempLib.getDescriptiveString(), false);
                try {
                    if (tempLib.isLibraryConnectionValid()) {
                        getStatus().updateStatus("Checking RMI Server connection " + tempLib.getProcessServerAddress() + " @ " + Integer.toString(tempLib.getProcessServerPort()), false);
                        com.ibm.sdwb.build390.library.VersionInfo version = tempLib.getProcessServerVersion(true);
                        if(version!=null){
                        new MBMsgBox("OK", "Attempt to connect to library server  was successful." + MBConstants.NEWLINE 
                                      + MBConstants.NEWLINE + version.compatibleOrNotMessage());
                        }
                    }
                } finally {
                    // reset_status
                    getStatus().updateStatus("", false);
                }
            }
        }
    }

    private class AddMVSEntry extends CancelableAction {

        AddMVSEntry() {
            super("Add");
        }

        public void doAction(ActionEvent e) {
            NewMainframeDialog newMain = new NewMainframeDialog(thisFrame);
            MBMainframeInfo newInfo = newMain.getMainframeInfo();
            if (newInfo.getMainframeAddress() != null) {
                choiceMainframe.addItem(newInfo);
                choiceMainframe.setSelectedItem(newInfo);
                choiceMainframe.setToolTipText(newInfo.getMainframeAccountInfo().toUpperCase());
            }
        }
    }

    private class RemoveMVSEntry extends CancelableAction {

        RemoveMVSEntry() {
            super("Remove");
        }

        public void doAction(ActionEvent e) {
            if (choiceMainframe.getSelectedIndex() > -1) {
                notRemovedMainframeSet.add((MBMainframeInfo)choiceMainframe.getSelectedItem());
                choiceMainframe.removeItem(choiceMainframe.getSelectedItem());

                if (choiceMainframe.getSelectedIndex() > -1) {
                    MBMainframeInfo tempMain = (MBMainframeInfo) choiceMainframe.getSelectedItem();
                    choiceMainframe.setToolTipText(tempMain.getMainframeAccountInfo());
                } else {
                    choiceMainframe.setToolTipText(new String());
                }
            }

            if (choiceMainframe.getItemCount() <= 0) {
                removeMainframeAction.setEnabled(false);
                editMainframeAction.setEnabled(false);
                testMVSAction.setEnabled(false);
                choiceMainframe.setToolTipText(new String());
            } else {
                choiceMainframe.setSelectedIndex(0);
            }
        }
    }

    private class EditMVSEntry extends CancelableAction {

        EditMVSEntry() {
            super("Edit");
        }

        public void doAction(ActionEvent e) {


            int selectionIndex = choiceMainframe.getSelectedIndex(); 

            if (selectionIndex > -1) {
                MBMainframeInfo tempMain = (MBMainframeInfo) choiceMainframe.getSelectedItem();
                NewMainframeDialog newMain = new NewMainframeDialog(thisFrame, tempMain);
                choiceMainframe.repaint();
                choiceMainframe.setSelectedItem(tempMain);
                choiceMainframe.setToolTipText(tempMain.getMainframeAccountInfo().toUpperCase());
            }
        }
    }

    private class TestMVSEntry extends CancelableAction {

        TestMVSEntry() {
            super("Test");
        }

        public void doAction(ActionEvent e) {
            if (choiceMainframe.getSelectedIndex() > -1) {
                MBMainframeInfo tempMain = (MBMainframeInfo) choiceMainframe.getSelectedItem();
                try {
                    getStatus().updateStatus("Checking Build390 server connection "+tempMain.getMainframeAddress() + "@"+tempMain.getMainframePort()  , false);
                    com.ibm.sdwb.build390.process.MVSServerStatus serverStatus = new com.ibm.sdwb.build390.process.MVSServerStatus(tempMain, parentWindow);
                    serverStatus.externalRun();
                } catch (MBBuildException mbe) {
                    lep.LogException(mbe);
                }
            }
        }
    }

    private class SetupConfigurer implements ActionConfigurer {
        public java.awt.Component getFrame() {
            return thisFrame;
        }
        public JMenuBar getMenuBar() {
            return getJMenuBar();
        }
        public MBStatus getStatusHandler() {
            return getStatus();
        }
        public LogEventProcessor getLEP() {
            return getLEP();
        }
        public void handleUIEvent(com.ibm.sdwb.build390.userinterface.event.UserInterfaceEvent event) {
        }
    }

    private class DisplayeableInfoCriteria implements FilterCriteria {
        public boolean passes(Object o) {
            LibraryInfo libInfo = (LibraryInfo)o;

            if (!MBClient.getCommandLineSettings().getMode().isFakeLibrary()) {
                return !libInfo.isFakeInfo();
            }

            return libInfo.isFakeInfo();
        }
    }



    private String getLibraryTextPrefix() {
        return MBClient.getCommandLineSettings().getMode().getCategory();
    }


}


