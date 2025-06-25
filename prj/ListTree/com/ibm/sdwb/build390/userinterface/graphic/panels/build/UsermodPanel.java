package com.ibm.sdwb.build390.userinterface.graphic.panels.build;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.io.File;
import java.util.HashSet;
import java.util.Iterator;

import javax.swing.JMenu;
import javax.swing.JMenuItem;

import com.ibm.sdwb.build390.MBBuildException;
import com.ibm.sdwb.build390.MBEdit;
import com.ibm.sdwb.build390.MBMsgBox;
import com.ibm.sdwb.build390.MBUtilities;
import com.ibm.sdwb.build390.help.HelpTopicID;
import com.ibm.sdwb.build390.info.*;
import com.ibm.sdwb.build390.library.ChangeRequest;
import com.ibm.sdwb.build390.library.SourceInfoCollection;
import com.ibm.sdwb.build390.library.userinterface.MultipleSourceSelection;
import com.ibm.sdwb.build390.library.userinterface.SourceSelection;
import com.ibm.sdwb.build390.mainframe.parser.DriverReportParser;
import com.ibm.sdwb.build390.process.AbstractProcess;
import com.ibm.sdwb.build390.process.DriverBuildProcess;
import com.ibm.sdwb.build390.process.UsermodGeneral;
import com.ibm.sdwb.build390.process.steps.DriverReport;
import com.ibm.sdwb.build390.userinterface.event.multiprocess.*;
import com.ibm.sdwb.build390.userinterface.graphic.MainInterface;
import com.ibm.sdwb.build390.userinterface.graphic.panels.HoldDataPanel;
import com.ibm.sdwb.build390.userinterface.graphic.panels.multiprocesspanels.MultipleProcessMonitoringFrame;
import com.ibm.sdwb.build390.userinterface.graphic.panels.multiprocesspanels.usermod.LogicDialog;
import com.ibm.sdwb.build390.userinterface.graphic.widgets.BuildFrameInterface;
import com.ibm.sdwb.build390.userinterface.graphic.widgets.BuildSettingsPanel;
import com.ibm.sdwb.build390.userinterface.graphic.widgets.CancelableAction;
import com.ibm.sdwb.build390.userinterface.graphic.widgets.UsermodBuildSettingsPanel;

public class UsermodPanel extends BuildPanel {

    private MultipleProcessMonitoringFrame multiProcessFrame = null;
    private MultipleSourceSelection sourceSelectionPanel = null;
    private UsermodBuildSettingsPanel buildSettingsPanel = null;
    private UsermodGeneralInfo umBuild = null;
    private static final String OPTIONKEY = "USERMODBUILDOPTIONS";


    private UsermodPanel(UsermodGeneralInfo tempBuild, BuildFrameInterface temp, boolean initializeFieldsFromInfo)throws MBBuildException{
        super(tempBuild, temp);
        umBuild = (UsermodGeneralInfo)getBuild();
        multiProcessFrame = (MultipleProcessMonitoringFrame) temp;
        layoutPanel(initializeFieldsFromInfo);
    }

    public static MultipleProcessMonitoringFrame getUsermodFrame(UsermodGeneralInfo tempBuild, boolean initializeFieldsFromInfo) throws MBBuildException{
        MultipleProcessMonitoringFrame frame = new MultipleProcessMonitoringFrame("Usermod");
        UsermodPanel panel = new UsermodPanel(tempBuild, frame, initializeFieldsFromInfo);
        frame.setBuildPanel(panel);
        frame.getHelpButton().addActionListener(MBUtilities.getHelpListener("",HelpTopicID.USERMODBUILDPAGE_HELP));
        frame.setVisible(true);
        return frame;
    }

    protected void layoutPanel(boolean initializeFieldsFromInfo) throws com.ibm.sdwb.build390.MBBuildException{
        super.layoutPanel(initializeFieldsFromInfo);
        JMenu optionMenu = multiProcessFrame.getJMenuBar().getMenu(1);
        optionMenu.add(new JMenuItem(new LogicAction()));
        optionMenu.add(new JMenuItem(new HoldDataAction()));
        multiProcessFrame.getUserInfoPanel().setLayout(new java.awt.BorderLayout());
        multiProcessFrame.getUserInfoPanel().add(BorderLayout.CENTER, this);
        multiProcessFrame.setTreeRootName("Usermods");
        multiProcessFrame.getTopBottomScrollPane().setResizeWeight(1);
        if (initializeFieldsFromInfo) {
            for (Iterator changesetGroupInfoIterator = umBuild.getChangesetGroups().iterator(); changesetGroupInfoIterator.hasNext();) {
                ChangeRequestPartitionedInfo oneSet = (ChangeRequestPartitionedInfo)  changesetGroupInfoIterator.next();
                ChangeRequestPartitionedUpdateEvent oneEvent = new ChangeRequestPartitionedUpdateEvent(oneSet);
                multiProcessFrame.handleUIEvent(oneEvent);
                if (oneSet.getSingleProjectBuildSet()!=null) {
                    for (Iterator infoIterator = oneSet.getSingleProjectBuildSet().iterator(); infoIterator.hasNext();) {
                        ChangesetGroupInfo aGroup = (ChangesetGroupInfo)infoIterator.next();
                        ChangesetGroupUpdateEvent newEvent = new ChangesetGroupUpdateEvent(aGroup);
                        multiProcessFrame.handleChangesetGroupUpdateEvent(newEvent);
                    }
                }
            }
            if (umBuild.getProcessForThisBuild()!=null) {
                if (((UsermodGeneral)umBuild.getProcessForThisBuild()).getChangesetBuildErrorMessage()!=null) {
                    ChangeRequestMultipleUpdateEvent warningUpdateEvent = new ChangeRequestMultipleUpdateEvent(umBuild);
                    multiProcessFrame.handleUIEvent(warningUpdateEvent);
                }
            }

            buildSettingsPanel.setDryRunSelected(umBuild.isDryRun());
            buildSettingsPanel.setBuildOneUsermodSet(umBuild.isBundled());
            buildSettingsPanel.setSendToSetting(umBuild.getMainframeUserAddressToSendOutputTo());
            buildSettingsPanel.setPDSSetting(umBuild.getMainframeDatasetToStoreOutputIn());
        }
    }

    protected SourceSelection getSourceSelector() {
        if (sourceSelectionPanel ==null) {
            sourceSelectionPanel = umBuild.getSetup().getLibraryInfo().getUserinterfaceFactory().getUsermodSourceSelectionPanel(umBuild.getSetup().getMainframeInfo());
        }
        return sourceSelectionPanel;
    }

    protected BuildSettingsPanel getSettingsPanel() {
        buildSettingsPanel = new UsermodBuildSettingsPanel(umBuild.getSetup().getLibraryInfo(), umBuild.getSetup().getMainframeInfo(),getLEP());
        return buildSettingsPanel;
    }

    public void setBuildArgs() throws MBBuildException{ //TST3564
        super.setBuildArgs();
        umBuild.setDryRun(buildSettingsPanel.isDryRunSet());
        umBuild.setBundled(buildSettingsPanel.isBuildOneUsermodSet());
        umBuild.setMainframeUserAddressToSendOutputTo(buildSettingsPanel.getSendToSetting());
        umBuild.setMainframeDatasetToStoreOutputIn(buildSettingsPanel.getPDSSetting());
        umBuild.setChangeRequestSet(new HashSet(((SourceInfoCollection)sourceSelectionPanel.getSourceInfo()).getChangeRequestCollection()));
    }

    public void setAllowEditing(boolean allowEdits) {
        super.setAllowEditing(allowEdits);
        buildSettingsPanel.setDryRunEnabled(true);

        boolean makePageEditable = (umBuild.getProcessForThisBuild()!=null && umBuild.getProcessForThisBuild().getStepsThatHaveRun().isEmpty());
        boolean isChangesetBuildError = (umBuild.getProcessForThisBuild()!=null && (((UsermodGeneral)umBuild.getProcessForThisBuild()).getChangesetBuildErrorMessage()!=null));
        if ((buildSettingsPanel.isDryRunSet() || isChangesetBuildError) && allowEdits) {
            sourceSelectionPanel.setEnabled(true);
            multiProcessFrame.setBuildButtonEnabled(true);
            umBuild.setProcessForThisBuild(null); //reset it, so we can rerun it(only in case of dryrun).
        } else {
            multiProcessFrame.setBuildButtonEnabled(false); //in other cases we don't allow restarts for usermod.
        }

       if(allowEdits && makePageEditable){
            umBuild.setProcessForThisBuild(null); //reset it, so we can rerun it(only in case of dryrun).
            multiProcessFrame.getTopBottomScrollPane().setDividerLocation(0.8);
            setEditablePage(makePageEditable);
        }
    }

    protected void connectTabsTogether() {
        sourceSelectionPanel.addUserInterfaceEventListener(buildSettingsPanel);
        super.connectTabsTogether();
    }

    protected AbstractProcess getBuildProcess() {
       UsermodGeneral buildProcess =  new UsermodGeneral(umBuild, (com.ibm.sdwb.build390.userinterface.UserCommunicationInterface) multiProcessFrame); 
        umBuild.setProcessForThisBuild(buildProcess);

        return buildProcess;
    }

    protected String getOptionStorageKey() {
        return OPTIONKEY;
    }

    protected void handleBuildAction() throws MBBuildException  {
        //++USERMOD should not be allowed on thin delta NOTHINUM=YES
        
        //Begin TST3192 (moved to UsermodGeneral preExecution
        /*
        if (umBuild!=null) {
            com.ibm.sdwb.build390.process.ProcessWrapperForSingleStep driverReportWrapper = new com.ibm.sdwb.build390.process.ProcessWrapperForSingleStep(multiProcessFrame.getInternalFrame());
            com.ibm.sdwb.build390.process.steps.DriverReport driverReport = new com.ibm.sdwb.build390.process.steps.DriverReport(umBuild.getDriverInformation(),umBuild.getSetup().getMainframeInfo(), umBuild.getSetup().getLibraryInfo(), umBuild.getBuildPathAsFile(), driverReportWrapper);  
            driverReport.setAlwaysRun(true);
            driverReport.setSummaryType("ONLY");
            driverReport.setForceNewReport(true);
            driverReport.setCheckBaseNotThinDelta(true);
            driverReport.setCheckForLockFlag(true); // check for LOCK=ON 
            driverReport.setCheckForMergeOnlyFlag(true); // check for MERGONLY=OFF 
            driverReportWrapper.setStep(driverReport);
            driverReportWrapper.externalRun();
        }
       */
       //End TST3192
    }



    protected void handleBuildLogAction() {
        if (umBuild!=null && umBuild.getProcessForThisBuild()!=null) {
            boolean atleastOneDisplayed  = false;
            for (Iterator changesetGroupInfoIterator = umBuild.getChangesetGroups().iterator(); changesetGroupInfoIterator.hasNext();) {
                ChangeRequestPartitionedInfo oneSet = (ChangeRequestPartitionedInfo)  changesetGroupInfoIterator.next();
                File buildLogFile = new File(oneSet.getBuildPathAsFile(),"Build.log");
                if (buildLogFile.exists()) {
                    new MBEdit(buildLogFile.getAbsolutePath(),getLEP());
                    atleastOneDisplayed = true;
                }
            }
            if (!atleastOneDisplayed) {
                getBuildFrame().getInternalFrame().problemBox("Information:","build.log empty");
            }
        } else {
            getBuildFrame().getInternalFrame().problemBox("Information:","build.log empty");
        }
    }

    public void checkToEnableViewBuildLog() {
        boolean atleastOneExists  = false;
        if (umBuild!=null && umBuild.getProcessForThisBuild()!=null) {
            for (Iterator changesetGroupInfoIterator = umBuild.getChangesetGroups().iterator(); changesetGroupInfoIterator.hasNext();) {
                ChangeRequestPartitionedInfo oneSet = (ChangeRequestPartitionedInfo)  changesetGroupInfoIterator.next();
                File buildLogFile = new File(oneSet.getBuildPathAsFile(),"Build.log");
                if (buildLogFile.exists()) {
                    atleastOneExists = true;
                }
            }
        }
        JMenuItem buildLogMenuItem = (JMenuItem)multiProcessFrame.getJMenuBar().getMenu(2).getItem(2);
        buildLogMenuItem.setEnabled(atleastOneExists);
    }

    private class HoldDataAction extends CancelableAction {
        public HoldDataAction() {
            super("Hold data");
        }

        public void doAction(ActionEvent e) {
            // open the option dialog
            try {
                umBuild.setReleaseInformation(sourceSelectionPanel.getProjectChosen());
                ChangeRequest requestSelected = sourceSelectionPanel.getSelectedChangeRequest();
                if (requestSelected != null) {
                    new HoldDataPanel(umBuild, requestSelected.getName(),getLEP());
                } else {
                    new MBMsgBox("Error", "You must choose a track", multiProcessFrame);
                }

            } catch (MBBuildException mbe) {
                getLEP().LogException(mbe);
            }

        }
    }


    private class LogicAction extends CancelableAction {

        public LogicAction() {
            super("Logic");
        }

        public void doAction(ActionEvent e) {
            // open the option dialog
            try {
                umBuild.setReleaseInformation(sourceSelectionPanel.getProjectChosen());
                ChangeRequest requestSelected = sourceSelectionPanel.getSelectedChangeRequest();
                if (requestSelected != null) {
                    LogicDialog tempLogic = new LogicDialog(multiProcessFrame, umBuild.getIfReqList(requestSelected.getName()));
                    umBuild.setIfReqList(requestSelected.getName(), tempLogic.getLogic());
                } else {
                    new MBMsgBox("Error", "You must choose a track", multiProcessFrame);
                }
            } catch (MBBuildException mbe) {
                getLEP().LogException(mbe);
            }
        }
    }
}
