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

public class OneChangesetGroupBuildPanel extends AnimationStatusPanel {

    private JTextField project = null;
    private JTextField driver = null;
    private JList changesetList = null;
    private JList fmidList = null;


    public OneChangesetGroupBuildPanel(LogEventProcessor tempLep) {
        super(tempLep);
        Box mainPane = createMainPanel();
        add("Center", mainPane);
        setVisible(true);
    }

    private Box createMainPanel() {
        Box displayBox = Box.createVerticalBox();
        Box projectBox = Box.createHorizontalBox();
        projectBox.add(new JLabel("Project:"));
        project = new JTextField();
        project.setEditable(false);
        projectBox.add(project);
        displayBox.add(projectBox);
        displayBox.add(Box.createVerticalGlue());
        Box driverBox = Box.createHorizontalBox();
        driverBox.add(new JLabel("Driver:"));
        driver = new JTextField();
        driver.setEditable(false);
        driverBox.add(driver);
        displayBox.add(driverBox);
        displayBox.add(Box.createVerticalGlue());
        displayBox.add(new JLabel("Changesets:"));
        changesetList = new JList();
        displayBox.add(new JScrollPane(changesetList));
        displayBox.add(Box.createVerticalGlue());
        displayBox.add(new JLabel("FMID-SMOD Name:"));
        fmidList = new JList();
        displayBox.add(new JScrollPane(fmidList));
        return displayBox;
    }

    public void handleUIEvent(UserInterfaceEvent event) {
        if (event instanceof ChangesetGroupUpdateEvent) {
            handleChangesetGroupUpdate((ChangesetGroupUpdateEvent)event);
        }
    }

    private void handleChangesetGroupUpdate(ChangesetGroupUpdateEvent event) {
        ChangesetGroupInfo info = event.getChangesetGroupInfo();
        if (info.getProcessForThisBuild()!=null) {
            info.getProcessForThisBuild().setUserCommunicationInterface(this);
        }
        synchPanelToBuild(info);
        fillLoadedInfo(info);
    }

    private void synchPanelToBuild(ChangesetGroupInfo info) {
        Vector changesetVector = new Vector();
        for (Iterator changesetIterator = info.getSetOfChangesets().iterator(); changesetIterator.hasNext(); ) {
            Changeset oneChangeset = (Changeset) changesetIterator.next();
            project.setText(oneChangeset.getProject());
            changesetVector.add(oneChangeset.getName());
        }
        if (info.getDriverInformation()!=null) {
            driver.setText(info.getDriverInformation().getName());
        }
        changesetList.setListData(changesetVector);
        
        fmidList.setListData(getFMIDListData(info).toArray());
        repaint();
    }

    private java.util.List getFMIDListData(ChangesetGroupInfo info){
        java.util.List tempList = new ArrayList();

        for(Iterator iter = info.getFMIDToSMODMap().keySet().iterator();iter.hasNext();){
            String oneSMOD = (String) iter.next();
            String oneFMID = (String) info.getFMIDToSMODMap().get(oneSMOD);
            tempList.add(oneSMOD+ "-"+ oneFMID);
        }
        if (info.getFMIDToSMODMap().isEmpty()) {
            if (info.getProcessForThisBuild()!= null) {
                Boolean emptyUsermodProcessingEnabled = ((com.ibm.sdwb.build390.process.ChangesetGroup)info.getProcessForThisBuild()).getUsermodNoShippableProcessingEnabled();
                if (emptyUsermodProcessingEnabled!=null) {
                    if (emptyUsermodProcessingEnabled.booleanValue()) {
                        tempList.add("No shippables were");
                        tempList.add("found but processing");
                        tempList.add("will continue due to");
                        tempList.add("EMPTYUM=YES setting.");
                    }else{
                        tempList.add("No shippables were");
                        tempList.add("found.  Processing");
                        tempList.add("will end.");
                    }
                }
            }
        }
        return tempList;
    }

    private void fillLoadedInfo(ChangesetGroupInfo info) {
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
