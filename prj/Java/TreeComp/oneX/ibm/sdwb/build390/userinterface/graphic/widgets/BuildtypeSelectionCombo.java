package com.ibm.sdwb.build390.userinterface.graphic.widgets;

import java.io.*;
import java.util.*;
import javax.swing.*;
import java.awt.event.*;
import com.ibm.sdwb.build390.MBMainframeInfo;
import com.ibm.sdwb.build390.logprocess.*;
import com.ibm.sdwb.build390.process.DriverReport;
import com.ibm.sdwb.build390.mainframe.*;
import com.ibm.sdwb.build390.library.LibraryInfo;
import com.ibm.sdwb.build390.userinterface.graphic.widgets.CancelableProcessAction;
import com.ibm.sdwb.build390.userinterface.RememberedSettingsHandler;
import com.ibm.sdwb.build390.user.Setup;

public class BuildtypeSelectionCombo extends RefreshableCombo{

    private MBMainframeInfo mainInfo = null;
    private LibraryInfo libInfo = null;
    private DriverInformation driverInfo = null;
    private RememberedSettingsHandler defaults = null;
    private Setup setup = null;

    BuildtypeSelectionCombo(MBMainframeInfo tempMain, LibraryInfo tempLib, LogEventProcessor lep) {
        super(lep);
        setComboEditable(true);
        mainInfo = tempMain;
        libInfo = tempLib;
        setup = new Setup(tempLib, tempMain, null, true);
        defaults = RememberedSettingsHandler.getInstance();
        setButtonEnabled(false);
        setVisible(true);
    }

    public void setDriverInformation(DriverInformation tempInfo) {
        if (driverInfo==tempInfo) {
            return; // don't do anything if we've already handled this
        }
        driverInfo = tempInfo;
        if (driverInfo != null) {
            List defaultList = defaults.getBuildtypeRememberedSettingsList(setup, driverInfo.getRelease().getLibraryName(), driverInfo.getName());
            getComboBox().setHistory(defaultList);
            setButtonEnabled(true);
            setActionListener(new RefreshBuildtypeList(new DriverReport(driverInfo,  mainInfo, libInfo, null, this)));
            fillComboBox();
        }else {
            setButtonEnabled(false);
            getComboBox().setHistory(new ArrayList());
            getComboBox().setItems(new ArrayList());
            fillComboBox();
        }
    }

    public DriverInformation getDriverInformation(){
        return driverInfo;
    }


    private void fillComboBox() {
        List buildTypes = null;
        if (driverInfo != null) {
            buildTypes =driverInfo.getBuildTypes();
        }
        if (buildTypes==null) {
            buildTypes = new ArrayList();
        }
        setData(buildTypes);
    }

    public void handleFrameClosing() {
        if (driverInfo!=null) {
            if (!(new String("NONE")).equalsIgnoreCase(getElementSelected())) {// don't save the "there's nothing here" buildtype
                defaults.addBuildtypeRememberedSetting(setup, driverInfo.getRelease().getLibraryName(), driverInfo.getName(),getElementSelected ());
            }
        }
    }


    class RefreshBuildtypeList extends CancelableProcessAction {

        RefreshBuildtypeList(DriverReport reportGetter) {
            super("Refresh", reportGetter);
        }

        public void postAction() {
            fillComboBox();
        }
    }
}
