package com.ibm.sdwb.build390.userinterface.graphic.panels.multiprocesspanels;

import java.awt.BorderLayout;
import java.util.*;

import javax.swing.*;
import javax.swing.border.LineBorder;
import javax.swing.border.TitledBorder;
import javax.swing.event.*;
import javax.swing.table.*;

import com.ibm.sdwb.build390.*;
import com.ibm.sdwb.build390.info.*;
import com.ibm.sdwb.build390.library.*; 
import com.ibm.sdwb.build390.logprocess.*;
import com.ibm.sdwb.build390.process.AbstractProcess;
import com.ibm.sdwb.build390.userinterface.event.*;
import com.ibm.sdwb.build390.userinterface.event.multiprocess.*;
import com.ibm.sdwb.build390.userinterface.graphic.widgets.*;

public class OnePartitionedBuildPanel extends AnimationStatusPanel {


    private JList requestList = null;
    private JList prereqList = null;
    private MultipleProcessMonitoringFrame parentFrame = null;

    public OnePartitionedBuildPanel(MultipleProcessMonitoringFrame tempFrame, LogEventProcessor tempLep) {
        super(tempLep);
        parentFrame = tempFrame;
        Box mainPane = createMainPanel();
        add("Center", mainPane);
        setVisible(true);
    }

    private Box createMainPanel() {
        Box displayBox = Box.createVerticalBox();
        displayBox.add(new JLabel("Change requests:"));
        requestList = new JList();
        displayBox.add(new JScrollPane(requestList));
        displayBox.add(Box.createVerticalGlue());
        displayBox.add(new JLabel("Prerequisite processes:"));
        prereqList = new JList();
        displayBox.add(new JScrollPane(prereqList));
        return displayBox;
    }

    public void handleUIEvent(UserInterfaceEvent event) {
        if  (event instanceof ChangesetGroupUpdateEvent) {
            parentFrame.handleChangesetGroupUpdateEvent((ChangesetGroupUpdateEvent) event);
        }else if(event instanceof ChangeRequestPartitionedUpdateEvent) {
            ChangeRequestPartitionedInfo info = ((ChangeRequestPartitionedUpdateEvent) event).getChangeRequestPartionedInfo();
            if (info.getProcessForThisBuild()!=null) {
                info.getProcessForThisBuild().setUserCommunicationInterface(this);
            }
            synchPanelToBuild(((ChangeRequestPartitionedUpdateEvent) event).getChangeRequestPartionedInfo());
            fillLoadedInfo(info);
        } 
    }

    private void synchPanelToBuild(ChangeRequestPartitionedInfo info) {
        Vector requestVector = new Vector();
        for (Iterator requestIterator = info.getChangeRequestsInGroup().iterator(); requestIterator.hasNext();) {
            ChangeRequest change = (ChangeRequest) requestIterator.next();
            requestVector.add(change.getName());
        }
        Vector prereqVector = new Vector();
        for (Iterator prereqIterator = info.getPrereqChangeRequestGroups().iterator(); prereqIterator.hasNext();) {
            prereqVector.add(prereqIterator.next());
        }
        requestList.setListData(requestVector);
        prereqList.setListData(prereqVector);
        repaint();
    }

    private void fillLoadedInfo(ChangeRequestPartitionedInfo info) {
        if (info.getProcessForThisBuild()!=null) {
            if (!info.getProcessForThisBuild().isActiveNow()) {
                if (info.getProcessForThisBuild().hasCompletedSuccessfully()) {
                    getStatusHandler().updateStatus("Completed successfully.",false);
                }else {
                    if (info.getProcessForThisBuild().getLastStepRun()!=null) {
                        getStatusHandler().updateStatus("Failed, last step that ran => "+info.getProcessForThisBuild().getLastStepRun().getName(),false);
                    } else {
                        getStatusHandler().updateStatus("Failed, process did not complete.",false);
                    }
                }
            }
        } else {
            getStatusHandler().updateStatus("Process not created.",false);
        }
    }

}
