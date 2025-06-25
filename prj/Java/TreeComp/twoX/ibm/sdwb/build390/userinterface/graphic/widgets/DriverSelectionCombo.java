package com.ibm.sdwb.build390.userinterface.graphic.widgets;

import java.awt.event.*;
import java.io.*;
import java.util.*;

import javax.swing.*;

import com.ibm.sdwb.build390.*;
import com.ibm.sdwb.build390.MBMainframeInfo;
import com.ibm.sdwb.build390.library.LibraryInfo;
import com.ibm.sdwb.build390.logprocess.*;
import com.ibm.sdwb.build390.mainframe.*;
import com.ibm.sdwb.build390.process.MVSReleaseAndDriversList;
import com.ibm.sdwb.build390.process.ProcessWrapperForSingleStep;
import com.ibm.sdwb.build390.process.steps.CheckConnectionToLibrary;
import com.ibm.sdwb.build390.userinterface.RememberedSettingsHandler;
import com.ibm.sdwb.build390.user.Setup;

public class DriverSelectionCombo extends RefreshableCombo{

    private MBMainframeInfo mainInfo = null;
    private LibraryInfo libInfo = null;
    private ReleaseInformation releaseSelected = null;
    private RememberedSettingsHandler defaults = null;
    private Setup setup = null;


    public DriverSelectionCombo(MBMainframeInfo tempMain, LibraryInfo tempLib, LogEventProcessor lep) {
        super(lep);
        setMainframeAndLibrary(tempMain,tempLib);
        defaults = RememberedSettingsHandler.getInstance();
        setButtonEnabled(false);
        init();
    }

    public void setMainframeAndLibrary(MBMainframeInfo tempMain, LibraryInfo tempLib) {
        mainInfo = tempMain;
        libInfo = tempLib;
        setup = new Setup(tempLib, tempMain, null, true);
        setActionListener(new RefreshDriverList(new MVSReleaseAndDriversList(mainInfo, libInfo, null, this)));
    }

    private void init() {
        fillComboBox();
        setVisible(true);
        setComboEditable(true);  //TST3143
    }

    private void fillComboBox() {
        if (releaseSelected!=null) {
            if (releaseSelected.getDrivers()!=null) {
                List drivers = new ArrayList();
                for (Iterator driverIterator = releaseSelected.getDrivers().iterator(); driverIterator.hasNext();) {
                    DriverInformation oneDriver = (DriverInformation) driverIterator.next();
                    drivers.add(oneDriver.getName());
                }
                setData(drivers);
            }
        }else {
            setData(new ArrayList());
        }
    }

    public void setRelease(ReleaseInformation release) {
        releaseSelected = release;
        if (releaseSelected!= null) {
            setButtonEnabled(true);
            List defaultList = defaults.getDriverRememberedSettingsList(setup,releaseSelected.getLibraryName());
            choices.setHistory(defaultList);
        }  else {
            setButtonEnabled(false);
            choices.setHistory(new ArrayList());// blank out the history
        }
        fillComboBox();
        selectItem(getElementSelected()); //an event wasn't getting fired. so explicitly select that item again.
    }


    public void handleFrameClosing() {
        if (releaseSelected!= null) {
            defaults.addDriverRememberedSetting(setup,releaseSelected.getLibraryName() ,getElementSelected ());
        }
    }

    public DriverInformation getSelectedDriver() {
        if (releaseSelected == null) {
            return null;
        }
        return releaseSelected.getDriverByName(getElementSelected());
    }

    class RefreshDriverList extends CancelableProcessAction {

        RefreshDriverList(MVSReleaseAndDriversList tempList) {
            super("Refresh", tempList);
        }

        public void postAction() {
            fillComboBox();
        }
    }

}
