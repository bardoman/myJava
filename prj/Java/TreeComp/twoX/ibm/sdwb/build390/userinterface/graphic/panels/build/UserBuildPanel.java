package com.ibm.sdwb.build390.userinterface.graphic.panels.build;

import com.ibm.sdwb.build390.*;
import com.ibm.sdwb.build390.help.HelpTopicID;
import com.ibm.sdwb.build390.library.userinterface.*;
import com.ibm.sdwb.build390.process.*;
import com.ibm.sdwb.build390.userinterface.graphic.panels.*;
import com.ibm.sdwb.build390.userinterface.graphic.widgets.BuildFrameInterface;

public class UserBuildPanel extends BuildPanel {

    private static final String OPTIONKEY = "USERBUILDOPTIONS";
    private static final String FASTTRACKOPTIONKEY = "FASTTRACKOPTIONS";
    protected UserBuildSource userBuildSourcePanel = null;
    protected com.ibm.sdwb.build390.process.UserBuildProcess buildProcess = null;

    private UserBuildPanel(MBUBuild ubuild, BuildFrameInterface temp, boolean initializeFieldsFromInfo)throws com.ibm.sdwb.build390.MBBuildException{
        super(ubuild, temp);
        buildProcess = (UserBuildProcess) getBuild().getProcessForThisBuild();
        setSettingsTabVisible(!ubuild.getFastTrack());
        layoutPanel(initializeFieldsFromInfo);
    }

    public static InternalFrameBuildPanelHolder getUserBuildFrame(MBUBuild build, boolean initializeFieldsFromInfo) throws MBBuildException{
        String title = null;
        String helpAnchor = "";
        if (build.getFastTrack()) {
            title = "Fast Track";
        } else {
            title = "User Build";
        }
        if (build.getSourceType()==MBUBuild.LOCAL_SOURCE_TYPE) {
            title = "Local Parts " + title;
            if (build.getFastTrack()) {
                helpAnchor = HelpTopicID.LOCALPARTS_FASTTRACK_HELP;
            } else {
                helpAnchor = HelpTopicID.LOCALPARTSUSERBUILDPAGE_HELP;
            }
        } else {
            title = "PDS " + title;
            if (build.getFastTrack()) {
                helpAnchor = HelpTopicID.PDSPARTS_FASTTRACK_HELP;
            } else {
                helpAnchor = HelpTopicID.PDSUSERBUILDPAGE_HELP;
            }
        }
        InternalFrameBuildPanelHolder frame = new InternalFrameBuildPanelHolder(title, build.getLEP());
        UserBuildPanel panel = new UserBuildPanel(build, frame, initializeFieldsFromInfo);
        frame.setBuildPanel(panel);
        frame.getHelpButton().addActionListener(MBUtilities.getHelpListener("",helpAnchor));
        frame.setVisible(true);
        return frame;
    }

    protected SourceSelection getSourceSelector() {
        MBUBuild build = (MBUBuild) getBuild();
        if (build.getSourceType()==MBUBuild.LOCAL_SOURCE_TYPE) {
            userBuildSourcePanel =  new LocalPartsUserBuildSourcePanel(this,(MBUBuild) getBuild(), getLEP());
        } else {
            userBuildSourcePanel = new PDSUserBuildSourcePanel(this,(MBUBuild) getBuild(), getLEP());
        }
        return userBuildSourcePanel;
    }


    protected String getOptionStorageKey() {
        if (((MBUBuild)getBuild()).getFastTrack()) {
            return FASTTRACKOPTIONKEY;
        }
        return OPTIONKEY;
    }

    protected com.ibm.sdwb.build390.process.AbstractProcess getBuildProcess() {
        if (!((MBUBuild) getBuild()).getFastTrack()) {
            buildProcess =  new UserBuildProcess((MBUBuild) getBuild(), this);
        } else {
            buildProcess = new com.ibm.sdwb.build390.process.FastTrackBuildProcess((MBUBuild) getBuild(), this);
        }
        if (userBuildSourcePanel!=null) {
            buildProcess.setMetadata(userBuildSourcePanel.getMetadataMap());
        } else {
            System.out.println("Mysterious userbuild nuller strikes again. Film at 11");
        }
        buildProcess.addProcessActionListener(new com.ibm.sdwb.build390.test.TestNotifyListener());
        return buildProcess;
    }

    protected void handleBuildAction() throws MBBuildException{
        buildProcess.setParentInternalFrame(getBuildFrame().getInternalFrame());
    }

    protected void handleBuildLogAction() {
        getBuildFrame().getInternalFrame().problemBox("Information:","build.log empty");
    }

    public com.ibm.sdwb.build390.MBInternalFrame getInternalFrame() {
        return getBuildFrame().getInternalFrame();
    }
}
