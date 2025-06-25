package com.ibm.sdwb.build390.userinterface.graphic.panels.build;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.io.File;
import java.util.Formatter;

import javax.swing.AbstractAction;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import javax.swing.JTextField;
import javax.swing.border.LineBorder;
import javax.swing.border.TitledBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import com.ibm.sdwb.build390.MBBuild;
import com.ibm.sdwb.build390.MBBuildException;
import com.ibm.sdwb.build390.MBClient;
import com.ibm.sdwb.build390.MBEdit;
import com.ibm.sdwb.build390.MBGuiConstants;
import com.ibm.sdwb.build390.MBMsgBox;
import com.ibm.sdwb.build390.MBSaveableFrame;
import com.ibm.sdwb.build390.MBStatus;
import com.ibm.sdwb.build390.library.userinterface.SourceSelection;
import com.ibm.sdwb.build390.logprocess.LogEventProcessor;
import com.ibm.sdwb.build390.mainframe.DriverInformation;
import com.ibm.sdwb.build390.mainframe.parser.DriverReportParser;
import com.ibm.sdwb.build390.process.AbstractProcess;
import com.ibm.sdwb.build390.process.DriverBuildProcess;
import com.ibm.sdwb.build390.process.steps.DriverReport;
import com.ibm.sdwb.build390.user.Setup;
import com.ibm.sdwb.build390.userinterface.UserCommunicationInterface;
import com.ibm.sdwb.build390.userinterface.event.UserInterfaceEvent;
import com.ibm.sdwb.build390.userinterface.event.UserInterfaceListenerManager;
import com.ibm.sdwb.build390.userinterface.event.build.DriverUpdateEvent;
import com.ibm.sdwb.build390.userinterface.event.build.ProcessUpdateEvent;
import com.ibm.sdwb.build390.userinterface.graphic.MainInterface;
import com.ibm.sdwb.build390.userinterface.graphic.panels.AdditionalBuildSettings;
import com.ibm.sdwb.build390.userinterface.graphic.panels.OptionPanel;
import com.ibm.sdwb.build390.userinterface.graphic.panels.ProcessStepListPanel;
import com.ibm.sdwb.build390.userinterface.graphic.utilities.ContainerEnablerDisabler;
import com.ibm.sdwb.build390.userinterface.graphic.widgets.BuildFrameInterface;
import com.ibm.sdwb.build390.userinterface.graphic.widgets.BuildSettingsPanel;
import com.ibm.sdwb.build390.userinterface.graphic.widgets.CancelableAction;
import com.ibm.sdwb.build390.userinterface.graphic.widgets.EnhancedTabbedPane;
import com.ibm.sdwb.build390.userinterface.graphic.widgets.MainframeReleaseAndDriverSelectionPanel;
import com.ibm.sdwb.build390.StopError;//TST3564



public abstract class BuildPanel extends JPanel implements MBSaveableFrame, UserCommunicationInterface {
    private final JMenuItem btViewBuildLog = new JMenuItem(new ViewBuildLogAction());
    private JMenuItem btSave = new JMenuItem(new SaveAction());
    private JMenuItem MenuPhases = new JMenuItem(new ViewBuildtypePhasesAction());
    private JMenuItem btAddbuildsettings  =  new JMenuItem(new AddBuildSettingsAction());
    private JTextField descriptionTextField = new JTextField();
    protected  EnhancedTabbedPane mainPane = new EnhancedTabbedPane();
    protected MainframeReleaseAndDriverSelectionPanel mainframeSelectionPanel = null;
    protected SourceSelection sourceSelectionPanel = null;
    protected BuildSettingsPanel buildSettingsPanel = null;
    private DriverReport driverReport = null;
    private MBBuild build = null;
    private BuildFrameInterface buildFrame = null;
    BuildPanel buildPanel = null;
    protected OptionPanel optionPanel = null;
    private boolean settingsTabVisible = true;
    private boolean doingBuild  = false;
    private boolean allowEdits  = true;
    private ContainerEnablerDisabler disableFrameController = null;
    private UserInterfaceListenerManager eventNotificationManager = new UserInterfaceListenerManager(); //it is becoming complicated to use abstract protected methods to reach the Local/PDS Source panel code. so iam using the event strategy.

    public BuildPanel(MBBuild tempBuild, BuildFrameInterface tempFrame) throws com.ibm.sdwb.build390.MBBuildException{
        super(new BorderLayout());
        buildPanel = this;
        buildFrame = tempFrame;
        disableFrameController = new ContainerEnablerDisabler(buildFrame.getInternalFrame());
        disableFrameController.setUntouchableComponents(buildFrame.getInternalFrame().getNondisablableComponentClasses());
        build = tempBuild;
    }

    protected void setSettingsTabVisible(boolean visible) {
        settingsTabVisible =visible;
    }

    protected void layoutPanel(boolean initializeFieldsFromInfo) throws com.ibm.sdwb.build390.MBBuildException{
        mainPane.setTabLayoutPolicy(JTabbedPane.SCROLL_TAB_LAYOUT);
        add(BorderLayout.CENTER, mainPane);
        Box horizontalDescriptionBox = Box.createHorizontalBox();
        horizontalDescriptionBox.add(new JLabel("Description"));
        horizontalDescriptionBox.add(descriptionTextField);
        add(BorderLayout.NORTH, horizontalDescriptionBox);
        setupSourceInformation();
        setupDestinationInformation();
        if (settingsTabVisible) {
            setupSettingsTab();
        }
        setupOptionsTab();
        connectTabsTogether();
        mainPane.clearVisitedTabs();
        mainPane.addChangeListener(new MainPaneListener());
        mainPane.addUserInterfaceEventListener(this);
        buildFrame.setBuildAction(getBuildAction());
        addMenus();
        if (initializeFieldsFromInfo) {
            setFieldsFromBuild();
        }
        buildFrame.setBuildButtonEnabled(mainPane.isAllRequiredActionsSatisfied());

    }

    private void addMenus() {
        JMenu optionMenu = new JMenu("Options");
        buildFrame.getInternalFrame().getJMenuBar().add(optionMenu);
        optionMenu.add(btAddbuildsettings);
        JMenu ViewMenu = new JMenu("View");
        buildFrame.getInternalFrame().getJMenuBar().add(ViewMenu);
        if (settingsTabVisible) {
            ViewMenu.add(MenuPhases);
            ViewMenu.addSeparator();
        }
        ViewMenu.add(btViewBuildLog);
        buildFrame.getInternalFrame().getJMenuBar().getMenu(0).insertSeparator(0);
        buildFrame.getInternalFrame().getJMenuBar().getMenu(0).insert(btSave,0);
    }

    protected void setFieldsFromBuild() {
        descriptionTextField.setText(build.get_descr());
        if (build.getSource()!=null) {
            if (sourceSelectionPanel!=null) {
                sourceSelectionPanel.setSourceInfo(build.getSource());
            }
        }

        mainframeSelectionPanel.setDriverSelected(build.getDriverInformation());
        if (settingsTabVisible) {
            buildSettingsPanel.setDriverInformation(build.getDriverInformation());
            buildSettingsPanel.setBuildType(build.get_buildtype());
        }
        optionPanel.setOptions(build.getOptions());
        checkToEnableViewBuildLog();
    }

    public void setAllowEditing(boolean tempAllowEdits) {
        allowEdits = tempAllowEdits;
        if (build.getProcessForThisBuild()!=null) {// if this has been run, by default don't let anyone touch anything
            setEditablePage(false);
            buildFrame.getInternalFrame().getJMenuBar().getMenu(0).setEnabled(true);
            buildFrame.getInternalFrame().getJMenuBar().getMenu(2).setEnabled(true);
            btViewBuildLog.setEnabled(true);
            MenuPhases.setEnabled(true);
            if (allowEdits & !build.getProcessForThisBuild().hasCompletedSuccessfully()) { // if it's been run but they want edits, then enable specific things only
                buildFrame.getInternalFrame().getJMenuBar().getMenu(1).setEnabled(true);
                btAddbuildsettings.setEnabled(true);
                descriptionTextField.setEnabled(true);
                optionPanel.setEnabled(true);
                buildFrame.setBuildButtonEnabled(true);

                ProcessUpdateEvent  newEvent = new ProcessUpdateEvent(this);
                newEvent.setProcessFinished();
                eventNotificationManager.fireEvent(newEvent);
            }
        }
    }

    protected abstract SourceSelection getSourceSelector();

    protected BuildSettingsPanel getSettingsPanel() {
        if (buildSettingsPanel==null & settingsTabVisible) {
            buildSettingsPanel = new BuildSettingsPanel(build.getSetup().getLibraryInfo(), build.getSetup().getMainframeInfo(),getLEP());
        }
        return buildSettingsPanel;
    }

    protected void setupSourceInformation() {
        sourceSelectionPanel  = getSourceSelector();
        sourceSelectionPanel.setBorder(BorderFactory.createTitledBorder(LineBorder.createGrayLineBorder() ,"Source selection",TitledBorder.DEFAULT_JUSTIFICATION,TitledBorder.DEFAULT_POSITION,null,MBGuiConstants.ColorGroupHeading));
        sourceSelectionPanel.addUserInterfaceEventListener(mainPane);
        sourceSelectionPanel.addUserInterfaceEventListener(this);
        eventNotificationManager.addUserInterfaceEventListener(sourceSelectionPanel); //global events are notified to all panels.
        mainPane.add(sourceSelectionPanel, "Source", 0);
    }

    protected void setupDestinationInformation()throws MBBuildException{
        mainframeSelectionPanel  = new MainframeReleaseAndDriverSelectionPanel(build.getSetup().getLibraryInfo(), build.getSetup().getMainframeInfo(), this);
        mainframeSelectionPanel.setBorder(BorderFactory.createTitledBorder(LineBorder.createGrayLineBorder() ,"Build destination ",TitledBorder.DEFAULT_JUSTIFICATION,TitledBorder.DEFAULT_POSITION,null,MBGuiConstants.ColorGroupHeading)); 
        mainframeSelectionPanel.addUserInterfaceEventListener(mainPane);
        eventNotificationManager.addUserInterfaceEventListener(mainframeSelectionPanel); //global events are notified to all panels.
        mainPane.add(mainframeSelectionPanel, "Destination");
    }

    protected void setupSettingsTab() {
        buildSettingsPanel = getSettingsPanel();
        buildSettingsPanel.addUserInterfaceEventListener(mainPane);
        eventNotificationManager.addUserInterfaceEventListener(buildSettingsPanel); //global events are notified to all panels.
        mainPane.add(buildSettingsPanel, "Settings");
    }

    private void setupOptionsTab() throws MBBuildException{
        optionPanel = new OptionPanel(build.getOptions(),build.getSetup(), getOptionStorageKey(), this);
        com.ibm.sdwb.build390.process.ProcessWrapperForSingleStep driverReportWrapper = new com.ibm.sdwb.build390.process.ProcessWrapperForSingleStep(this);
        driverReport = new com.ibm.sdwb.build390.process.steps.DriverReport(null, build.getSetup().getMainframeInfo(), build.getSetup().getLibraryInfo(), MBClient.getCacheDirectory(), driverReportWrapper);  
        driverReport.setIncludePathname(true);
        driverReport.setSummaryType("ONLY");
        driverReportWrapper.setStep(driverReport);
        optionPanel.setDriverReportStep(driverReport);
        optionPanel.addUserInterfaceEventListener(mainPane);
        mainPane.add(optionPanel, "Options");
    }

    public void setEditablePage(boolean editableFrame) {
        if (editableFrame==false) {
            disableFrameController.disableContainer(true);
        } else {
            disableFrameController.enableContainer();
        }
    }

    public void checkToEnableViewBuildLog() {
        if (build!=null) {
            File log = new File(build.getBuildPathAsFile(), MBBuild.BUILDLOGFILENAME);
            if (log.exists()) {
                btViewBuildLog.setEnabled(true);
            } else {
                btViewBuildLog.setEnabled(false);
            }
        }
    }

    public void setBuildArgs()throws MBBuildException {//TST3564
        if (build.getProcessForThisBuild()==null) {// we've never run it
            if (settingsTabVisible) {
                build.set_buildtype(buildSettingsPanel.getBuildType());
            }
            build.setDriverInformation(mainframeSelectionPanel.getDriverSelected());
            if (sourceSelectionPanel!=null) {
                build.setSource(sourceSelectionPanel.getSourceInfo());
            }
        }
        boolean optsave = optionPanel.save();//TST3564
        //TST3564<Begin>
        if (!optsave) {
        	getStatusHandler().updateStatus("Required optional values are missing - build halted",false);
        	throw new StopError();
        }
        //TST3564<End>
        build.setOptions(optionPanel.getOptions());
        build.set_descr(descriptionTextField.getText());
    }

    public boolean save() throws com.ibm.sdwb.build390.MBBuildException{
        setBuildArgs();
        build.save();
        return true;
    }

    public boolean saveNeeded() {
        boolean needed = false;
        return needed;
    }

    public void handleDisposeTimeActions() {
    }

    public DriverInformation getDriver() {
        return mainframeSelectionPanel.getDriverSelected();
    }

    protected void connectTabsTogether() {
        sourceSelectionPanel.addUserInterfaceEventListener(mainframeSelectionPanel);
        sourceSelectionPanel.addUserInterfaceEventListener(optionPanel);
        mainframeSelectionPanel.addUserInterfaceEventListener(optionPanel);
        mainframeSelectionPanel.addUserInterfaceEventListener(sourceSelectionPanel);
        if (settingsTabVisible) {
            mainframeSelectionPanel.addUserInterfaceEventListener(buildSettingsPanel);
            buildSettingsPanel.addUserInterfaceEventListener(optionPanel);
        }
        sourceSelectionPanel.fireProjectUpdated();
    }

    protected BuildFrameInterface getBuildFrame() {
        return buildFrame;
    }

    protected MBBuild getBuild() {
        return build;
    }

    protected abstract String getOptionStorageKey();

    protected abstract AbstractProcess getBuildProcess();

    protected abstract void handleBuildAction() throws MBBuildException;

    protected abstract void handleBuildLogAction();


    protected CancelableAction getBuildAction() {
        return new BuildAction();
    }


    private class BuildAction extends CancelableAction {

        private com.ibm.sdwb.build390.process.management.Haltable stopObject = null;

        private BuildAction() {
            super("Build");
            setEnabled(false);
        }

        public void doAction(ActionEvent e) {
            doingBuild=true;
            if (build.getProcessForThisBuild()!=null) {
                if (build.getProcessForThisBuild().hasCompletedSuccessfully() & !build.getProcessForThisBuild().isRestartableAfterCompletion()) {
                    return;
                }
            }
            try {
                setBuildArgs();
                if (build.getSource() == null) {
                    buildFrame.getInternalFrame().problemBox("Error","You must specify part selection criteria first ");
                    return;
                } else {
                    buildFrame.getInternalFrame().getJMenuBar().getMenu(0).setEnabled(false);
                    buildFrame.getInternalFrame().getJMenuBar().getMenu(1).setEnabled(false);
                    buildFrame.getInternalFrame().getJMenuBar().getMenu(2).setEnabled(true);
                    btViewBuildLog.setEnabled(true);
                    MenuPhases.setEnabled(false);
                    buildFrame.getInternalFrame().setTitle(buildFrame.getInternalFrame().getTitle().indexOf(build.get_buildid()) > 0 ? buildFrame.getInternalFrame().getTitle() : buildFrame.getInternalFrame().getTitle() + "("+build.get_buildid() + ")");
                    AbstractProcess buildProcess = build.getProcessForThisBuild();
                    if (buildProcess == null) {
                        buildProcess =  getBuildProcess(); 
                    } else if (!buildProcess.getStepsThatHaveRun().isEmpty()) {
                        ProcessStepListPanel phaseList = new ProcessStepListPanel(buildProcess, buildFrame.getInternalFrame(), getLEP());
                        java.util.List phaseStepsList=null; 

                        if (buildProcess.getStepsThatHaveRun().get(0)!=null) {
                            com.ibm.sdwb.build390.process.AbstractProcess.RepeatedProcessStep step = (com.ibm.sdwb.build390.process.AbstractProcess.RepeatedProcessStep)buildProcess.getStepsThatHaveRun().get(0);
                            com.ibm.sdwb.build390.process.steps.DriverReport driverReportStep = (com.ibm.sdwb.build390.process.steps.DriverReport)step.getStep();
                            if (driverReportStep!=null) {
                                DriverReportParser driverRep =  driverReportStep.getParser();
                                if (driverRep!=null) {
                                    phaseStepsList = driverRep.getPhaseInforamtion(build.get_buildtype());
                                }
                            }
                        }

                        phaseList.populateStepList(phaseStepsList);
                        phaseList.setEnablePreviousResultsCheckBox(true);
                        phaseList.setVisible(true);
                        int selectedStep = phaseList.getStepToStartWith();
                        int selectedIteration = phaseList.getIterationToStartWith();
                        if (selectedStep > -1) {
                            buildProcess.prepareRestart(selectedStep,selectedIteration,buildPanel);
                        } else {
                            return; 
                        }
                    }
                    handleBuildAction();
                    stopObject = buildProcess;
                    buildProcess.run();
                }
            } catch (MBBuildException mbe) {
                getLEP().LogException(mbe);
            }
        }


        public void postAction() {
            doingBuild = false;
            buildFrame.getInternalFrame().getJMenuBar().getMenu(0).setEnabled(true);
            buildFrame.getInternalFrame().getJMenuBar().getMenu(1).setEnabled(true);
            MenuPhases.setEnabled(true);


            if (build.getProcessForThisBuild()!=null) {

                if (build.getProcessForThisBuild().hasCompletedSuccessfully()) {
                    buildFrame.setBuildButtonEnabled(build.getProcessForThisBuild().isRestartableAfterCompletion());
                    if (build.getProcessForThisBuild() instanceof DriverBuildProcess) {
                        if (((DriverBuildProcess)build.getProcessForThisBuild()).isPartsInDriverUpToDate()) {
                            getStatusHandler().updateStatus("Driver " + build.getDriverInformation().getName() + " is up to date.",false);
                        }
                    }
                } else {
                    getStatusHandler().updateStatus("-build failed",true);
                }
                setAllowEditing(!build.getProcessForThisBuild().hasCompletedSuccessfully());
            }
        }

        public void stop() {
            try {
                if (stopObject !=null) {
                    stopObject.haltProcess();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private class AddBuildSettingsAction extends CancelableAction {
        private AddBuildSettingsAction() {
            super("Additional build settings");
        }

        public void doAction(ActionEvent e) {
            try {
                AdditionalBuildSettings  abs  =  new AdditionalBuildSettings(build, buildFrame.getInternalFrame());
            } catch (MBBuildException mbe) {
                getLEP().LogException(mbe);
            }
        }
    }

    private class ViewBuildLogAction extends AbstractAction {
        private ViewBuildLogAction() {
            super("View Build Log");
        }

        public void actionPerformed(ActionEvent e) {
            new Thread(new Runnable() {
                           public void run() {
                               String currbuildpath = build.getBuildPath()+"Build.log";
                               if ((build!=null)&&(new File(currbuildpath)).exists()) {
                                   new MBEdit(build.getBuildPath()+"Build.log",getLEP());
                               } else {
                                   handleBuildLogAction();
                               }
                           }
                       }).start();

        }
    }

    private class ViewBuildtypePhasesAction extends CancelableAction {
        private ViewBuildtypePhasesAction() {
            super("View Build Type details");
        }

        public void doAction(ActionEvent e) {
            try {
                DriverInformation drvInfo = mainframeSelectionPanel.getDriverSelected();
                if (drvInfo!=null) {
                    File saveLocation = MBClient.getCacheDirectory();
                    com.ibm.sdwb.build390.process.ProcessWrapperForSingleStep driverReportWrapper = new com.ibm.sdwb.build390.process.ProcessWrapperForSingleStep(buildFrame.getInternalFrame());
                    driverReport = new com.ibm.sdwb.build390.process.steps.DriverReport(drvInfo,build.getSetup().getMainframeInfo(), build.getSetup().getLibraryInfo(), saveLocation, driverReportWrapper);  
                    driverReport.setAlwaysRun(true);
                    driverReport.setForceNewReport(false);
                    driverReport.setIncludePathname(true);
                    driverReport.setSummaryType("ONLY");
                    driverReportWrapper.setStep(driverReport);
                    driverReportWrapper.externalRun(); 
                } else {
                    new MBMsgBox("View Build Type Details", "Please select a driver.");
                }
            } catch (MBBuildException mbbe) {
                getLEP().LogException(mbbe);
            }
            if (buildSettingsPanel.getBuildType()!=null) {
                java.util.List phaseList = driverReport.getParser().getPhaseInforamtion(buildSettingsPanel.getBuildType().toUpperCase());
                StringBuilder phases = new StringBuilder();
                Formatter formatter = new Formatter(phases);
                for (int x=0; x<phaseList.size(); x++) {
                    com.ibm.sdwb.build390.mainframe.PhaseInformation onePhase = (com.ibm.sdwb.build390.mainframe.PhaseInformation)phaseList.get(x);
                    formatter.format("%-8s %-10s %-10s","Phase"+x, onePhase.getClassName(), onePhase.getName());
                    if (onePhase.getPhaseNumberToHaltOnIfErrorsFound()!=onePhase.getPhaseNumber()) {
                        formatter.format("%-17s: %s","Check after",onePhase.getPhaseNumberToHaltOnIfErrorsFound());
                    }
                    formatter.format("%n");
                }
                new MBMsgBox("Phase definitions for Build Type "+buildSettingsPanel.getBuildType().toUpperCase(), phases.toString());
                checkToEnableViewBuildLog();
            } else {
                new MBMsgBox("View Build Type Details", "Please select a buildtype.");
            }
        }
    }

    private class SaveAction extends CancelableAction {
        private SaveAction() {
            super("Save");
        }

        public void doAction(ActionEvent e) {
            try {
                save();
            } catch (MBBuildException mbe) {
                getLEP().LogException(mbe);
            }
        }
    }

    private class MainPaneListener implements ChangeListener {

        public void stateChanged(ChangeEvent e) {
            Component selectedComp = mainPane.getSelectedComponent();
            if (selectedComp == optionPanel) {
                optionPanel.visitingPage();
            }
        }
    }

    public LogEventProcessor getLEP() {
        return buildFrame.getLEP();
    }

    public MBStatus getStatusHandler() {
        return buildFrame.getStatusHandler();
    }

    public void handleUIEvent(UserInterfaceEvent event) {
        if (event instanceof com.ibm.sdwb.build390.userinterface.event.build.BuildtypeUpdateEvent) {
            String buildtype = ((com.ibm.sdwb.build390.userinterface.event.build.BuildtypeUpdateEvent) event).getBuildtype();
            if (settingsTabVisible) {
                buildSettingsPanel.setDriverInformation(build.getDriverInformation());
                buildSettingsPanel.setBuildType(buildtype);
            }
        } else if (event.getUpdateType().equals(EnhancedTabbedPane.ENHANCEDTABBEDPANEUPDATE)) {
            if (!doingBuild) {
                if (build.getProcessForThisBuild()!=null && build.getProcessForThisBuild().hasCompletedSuccessfully()) {
                    /** We should have ReBuildEvent or a class which would allow us to add BuildEnableRules to it, since we have more conditions coming in.
                    *for instance, do a rebuild when 
                    *1.driver is locked, user wants to build again (after unlocking the driver).
                    *2.when process is complete, but change parameters and start a build again.
                    *3.When the partlist is empty, the user wants to choose a different track to build.
                    *4.driver is locked, user wants to build again, after choosing a different driver
                    **/ 
                    buildFrame.setBuildButtonEnabled(allowEdits && build.getProcessForThisBuild().isRestartableAfterCompletion());
                } else {
                    buildFrame.setBuildButtonEnabled(allowEdits && mainPane.isAllRequiredActionsSatisfied());
                }
            }
        } else if (event instanceof ProcessUpdateEvent) {
            if (((ProcessUpdateEvent)event).isStartFromBeginning() && build.getProcessForThisBuild()!=null) {
                build.setProcessForThisBuild(null);
            }
        }

    }
}
