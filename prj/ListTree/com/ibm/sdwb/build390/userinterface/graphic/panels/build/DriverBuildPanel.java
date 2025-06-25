package com.ibm.sdwb.build390.userinterface.graphic.panels.build;

import com.ibm.sdwb.build390.*;
import com.ibm.sdwb.build390.help.HelpTopicID;
import com.ibm.sdwb.build390.process.*;
import com.ibm.sdwb.build390.userinterface.graphic.widgets.BuildFrameInterface;

public class DriverBuildPanel extends BuildPanel {
    private static final String OPTIONKEY = "LIBRARYBUILDOPTIONS";
    private DriverBuildProcess buildProcess = null;

    private DriverBuildPanel(MBBuild tempBuild, BuildFrameInterface temp, boolean initializeFieldsFromInfo)throws MBBuildException{
        super(tempBuild, temp);
        buildProcess = (DriverBuildProcess) getBuild().getProcessForThisBuild();
        layoutPanel(initializeFieldsFromInfo);
    }

    public static InternalFrameBuildPanelHolder getDriverBuildFrame(MBBuild build, boolean initializeFieldsFromInfo) throws MBBuildException{
        InternalFrameBuildPanelHolder frame = new InternalFrameBuildPanelHolder("Driver build", build.getLEP());
        DriverBuildPanel panel = new DriverBuildPanel(build, frame, initializeFieldsFromInfo);
        frame.setBuildPanel(panel);
        frame.getHelpButton().addActionListener(MBUtilities.getHelpListener("",HelpTopicID.DRIVERBUILDPAGE_HELP));
        frame.setVisible(true);
        return frame;
    }

    protected com.ibm.sdwb.build390.library.userinterface.SourceSelection getSourceSelector() {
        return getBuild().getSetup().getLibraryInfo().getUserinterfaceFactory().getSourceSelectionPanel(getBuild().getSetup().getMainframeInfo());
    }

    //we shouldn't set it explicitly, but make the layout manager do the job. We'll have to revisit this fix when we move to jdk5.0
    public java.awt.Dimension getPreferredSize() {
        return new java.awt.Dimension(500, 400);
    }


    protected String getOptionStorageKey() {
        return OPTIONKEY;
    }

    protected AbstractProcess getBuildProcess() {
        buildProcess = new DriverBuildProcess(getBuild(), this);
        return buildProcess;
    }

    protected void handleBuildAction() throws MBBuildException {
        buildProcess.setParentInternalFrame(getBuildFrame().getInternalFrame());
    }

    protected void handleBuildLogAction() {
        getBuildFrame().getInternalFrame().problemBox("Information:","build.log empty");
    }
}
