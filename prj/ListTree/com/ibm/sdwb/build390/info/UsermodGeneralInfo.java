package com.ibm.sdwb.build390.info;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.swing.JInternalFrame;

import com.ibm.sdwb.build390.MBBuildException;
import com.ibm.sdwb.build390.logprocess.LogEventProcessor;
import com.ibm.sdwb.build390.process.AbstractProcess;
import com.ibm.sdwb.build390.process.AbstractProcess.RepeatedProcessStep;
import com.ibm.sdwb.build390.process.UsermodGeneral;
import com.ibm.sdwb.build390.userinterface.event.multiprocess.ChangeRequestMultipleUpdateEvent;
import com.ibm.sdwb.build390.userinterface.graphic.panels.build.UsermodPanel;
import com.ibm.sdwb.build390.userinterface.graphic.panels.multiprocesspanels.MultipleProcessMonitoringFrame;


public class UsermodGeneralInfo extends ChangeRequestMultipleInfo {

    static final long serialVersionUID = 327313906448589986L;
    private Map<String,List<String>> ifReqMap = null;

    public UsermodGeneralInfo(LogEventProcessor lep) {
        super("M", com.ibm.sdwb.build390.MBConstants.USERMODBUILDDIRECTORY, lep);
        ifReqMap = new java.util.HashMap<String,List<String>>();
    }

    public List<String> getIfReqList(String trackName) {
        return ifReqMap.get(trackName);
    }

    public void setIfReqList(String trackName, List reqList) {
        ifReqMap.put(trackName, reqList);
    }


    public void viewBuild(JInternalFrame tempFrame) throws MBBuildException {
        MultipleProcessMonitoringFrame multiProcessFrame = UsermodPanel.getUsermodFrame(this,true);
        multiProcessFrame.getBuildPanel().setAllowEditing(false);
        multiProcessFrame.setVisible(true);
    }


}
