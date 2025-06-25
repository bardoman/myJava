package com.ibm.sdwb.build390.userinterface.graphic.panels.multiprocesspanels;
import java.util.Vector;

import javax.swing.Box;
import javax.swing.JLabel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;

import com.ibm.sdwb.build390.info.ChangeRequestMultipleInfo;
import com.ibm.sdwb.build390.logprocess.LogEventProcessor;
import com.ibm.sdwb.build390.process.UsermodGeneral;
import com.ibm.sdwb.build390.userinterface.event.UserInterfaceEvent;
import com.ibm.sdwb.build390.userinterface.event.multiprocess.ChangeRequestMultipleUpdateEvent;
import com.ibm.sdwb.build390.userinterface.event.multiprocess.ChangesetGroupUpdateEvent;
import com.ibm.sdwb.build390.userinterface.graphic.widgets.AnimationStatusPanel;

public class OneChangesetMultipleBuildPanel extends AnimationStatusPanel {


    private JTextArea messageList = null;

    public OneChangesetMultipleBuildPanel(LogEventProcessor tempLep) {
        super(tempLep);
        Box mainPane = createMainPanel();
        add("Center", mainPane);
        setVisible(true);
    }

    private Box createMainPanel() {
        Box displayBox = Box.createVerticalBox();
        displayBox.add(new JLabel("User message:"));
        messageList = new JTextArea("",2,10);
        messageList.setLineWrap(true);
        messageList.setWrapStyleWord(true);
        JScrollPane scroll = new JScrollPane(messageList,JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        displayBox.add(scroll);
        return displayBox;
    }

    public void handleUIEvent(UserInterfaceEvent event) {
        if (event instanceof ChangeRequestMultipleUpdateEvent) {
            handleChangesetMultipleUpdate((ChangeRequestMultipleUpdateEvent)event);
        }

    }

    private void handleChangesetMultipleUpdate(ChangeRequestMultipleUpdateEvent event) {
        ChangeRequestMultipleInfo info = event.getChangeRequestMultipleInfo();
        if (info.getProcessForThisBuild()!=null) {
            if (info.getProcessForThisBuild() instanceof UsermodGeneral) {
                if (((UsermodGeneral)info.getProcessForThisBuild()).getChangesetBuildErrorMessage()!=null) {
                    messageList.setText(((UsermodGeneral)info.getProcessForThisBuild()).getChangesetBuildErrorMessage());
                }
            }
        }
    }


}


