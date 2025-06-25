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

public class ReleaseSelectionCombo extends RefreshableCombo {

    private MBMainframeInfo mainInfo = null;
    private LibraryInfo libInfo = null;
    private RememberedSettingsHandler defaults = null;
    private Setup setup = null;


    public ReleaseSelectionCombo(MBMainframeInfo tempMain, LibraryInfo tempLib, LogEventProcessor lep) {
        super( lep);
        setMainframeAndLibrary(tempMain,tempLib);
        defaults = RememberedSettingsHandler.getInstance();
        init();
    }

    public void setMainframeAndLibrary(MBMainframeInfo tempMain, LibraryInfo tempLib) {
        mainInfo = tempMain;
        libInfo = tempLib;
        setup = new Setup(tempLib, tempMain, null, true);
        setActionListener(new ReleaseListRefreshAction(new MVSReleaseAndDriversList(mainInfo, libInfo, null, this)));
    }

    private void init() {
        fillComboBox();
        List defaultList = defaults.getReleaseRememberedSettingsList(setup);
        getComboBox().setHistory(defaultList);
        setComboEditable(true);  //TST3143
        setVisible(true);
    }

    private void fillComboBox() {
        TreeSet releases = new TreeSet();//TST3025 
        Set allReleases = mainInfo.getReleaseSet(libInfo);
        if (allReleases!=null) {
            for (Iterator releaseIterator = allReleases.iterator(); releaseIterator.hasNext();) {
                ReleaseInformation oneRelease = (ReleaseInformation) releaseIterator.next();
                releases.add(oneRelease.getLibraryName());
            }
            setData(releases);
        } else {
            setData(releases);
        }
    }

    public void handleFrameClosing() {
        defaults.addReleaseRememberedSetting(setup,getElementSelected());
    }

    public ReleaseInformation getSelectedRelease() {
        return mainInfo.getReleaseByLibraryName(getElementSelected(),libInfo );
    }

    private class ReleaseListRefreshAction extends com.ibm.sdwb.build390.userinterface.graphic.widgets.CancelableProcessAction {

        ReleaseListRefreshAction(MVSReleaseAndDriversList refreshProcess) {
            super("Refresh", refreshProcess);
        }

        public void doAction(ActionEvent e) {
            try {
                getStatusHandler().updateStatus("Checking connection to library.",false);
                ProcessWrapperForSingleStep wrapper = new ProcessWrapperForSingleStep(theProcess);
                CheckConnectionToLibrary connCheck = new CheckConnectionToLibrary(libInfo,wrapper);
                wrapper.setStep(connCheck);
                wrapper.externalRun();
                super.doAction(e);
            } catch (MBBuildException mbe) {
                getLEP().LogException(mbe);
            }
        }

        public void postAction() {
            fillComboBox();
            if (((super.theProcess)).getExceptionEncountered()==null) {
                getStatusHandler().updateStatus("Refresh complete.",false);
            } else {
                getStatusHandler().updateStatus("Refresh failed.",false);
            }
        }
    }

}
