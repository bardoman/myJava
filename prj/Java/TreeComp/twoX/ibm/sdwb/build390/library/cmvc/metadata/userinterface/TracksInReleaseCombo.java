package com.ibm.sdwb.build390.library.cmvc.metadata.userinterface;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.lang.reflect.InvocationTargetException;
import java.util.*;

import javax.swing.*;

import com.ibm.sdwb.build390.*;
import com.ibm.sdwb.build390.help.*;
import com.ibm.sdwb.build390.library.*;
import com.ibm.sdwb.build390.library.cmvc.*;
import com.ibm.sdwb.build390.logprocess.*;
import com.ibm.sdwb.build390.mainframe.*;
import com.ibm.sdwb.build390.userinterface.UserCommunicationInterface;
import com.ibm.sdwb.build390.userinterface.graphic.utilities.GeneralUtilities;
import com.ibm.sdwb.build390.userinterface.graphic.widgets.*;
import com.ibm.sdwb.build390.userinterface.RememberedSettingsHandler;
import com.ibm.sdwb.build390.user.Setup;
import com.ibm.sdwb.build390.utilities.SynchronizedFileAccess;

/** Create the driver build page */
public class TracksInReleaseCombo extends RefreshableCombo {

    private ReleaseInformation releaseSelected = null;
    private static final String TRACKS_SELECTION = "TRACKS_SELECTION";
    private RememberedSettingsHandler defaults = null;
    private Setup setup = null;
    private SynchronizedFileAccess buildablesFile = null;

    public TracksInReleaseCombo(final MBMainframeInfo tempMain,LibraryInfo tempLib, LogEventProcessor tempLep) {
        super(tempLep);
        setup = new Setup(tempLib, tempMain, null, true);
        defaults = RememberedSettingsHandler.getInstance();
        setButtonEnabled(false);
        setActionListener(new RefreshTrackList());
        init();
    }

    private void init() {
        setComboEditable(false); 
        populateSelectionBoxes();
        setVisible(true);
    }

    public void setRelease(ReleaseInformation info) {
        releaseSelected = info;
        if (releaseSelected!= null) {
            buildablesFile = SynchronizedFileAccess.getSychronizedFile(new File(MBClient.getCacheDirectory(), "BuildablesMetadata-"+setup.getLibraryInfo().getProcessServerAddress()+"-"+setup.getLibraryInfo().getProcessServerName()+"-"+releaseSelected.getLibraryName()));
            setButtonEnabled(true);
            java.util.List defaultList = defaults.getPerReleaseSettingList(setup,releaseSelected.getLibraryName(), TRACKS_SELECTION);
            choices.setHistory(defaultList);
        } else {
            buildablesFile = null;
            setButtonEnabled(false);
            choices.setHistory(new ArrayList());// blank out the history
        }
        if (buildablesFile!=null && !buildablesFile.exists()) {
           refreshComboConcurrently();
        }
        populateSelectionBoxes();
    }

    private void populateSelectionBoxes() {
        java.util.List trackList = new ArrayList();
        try {
            if (buildablesFile!=null) {
                if (buildablesFile.exists()) {
                    BufferedReader selectionReaders = buildablesFile.getBufferedReader();
                    String line = new String();
                    while (line != null) {
                        line = selectionReaders.readLine();
                        if (line !=null) {
                            StringTokenizer toke = new StringTokenizer(line, "|");
                            String type = toke.nextToken();
                            String state = toke.nextToken();
                            String name = toke.nextToken();
                            if (type.equals(CMVCLibraryInfo.TRACK)) {
                                trackList.add(name);
                            }
                        }
                    }
                    selectionReaders.close();
                }
            }
        } catch (IOException ioe) {
            throw new RuntimeException("Error reading library output.", ioe);
        }
        setData(trackList);
    }


    private class RefreshTrackList extends CancelableAction {

        RefreshTrackList() {
            super("Refresh");
        }

        public void doAction(ActionEvent evt) {
            boolean clearStatus = true;
            try {
                if (releaseSelected!=null) {
                    Set trackTypes = new HashSet();
                    trackTypes.add("integrate");
                    trackTypes.add("fix");
                    getStatusHandler().updateStatus("Getting tracks from library for " + releaseSelected.getLibraryName(),false);
                    String buildableOutput = ((CMVCLibraryInfo)setup.getLibraryInfo()).getBuildableObjects(releaseSelected.getLibraryName(),null,trackTypes);
                    BufferedWriter outputWriter = buildablesFile.getBufferedWriter(false);
                    outputWriter.write(buildableOutput);
                    outputWriter.close();
                    populateSelectionBoxes();
                }
            } catch (Exception mbe) {
                throw new RuntimeException(mbe);
            } finally {
                if (parentWindow!=null && clearStatus) {
                    parentWindow.getStatusHandler().clearStatus();
                }
            }
        } 

    }



    public String getSelectedTrack() {
        if (getElementSelected()!=null & releaseSelected!=null) {
            return getElementSelected();
        }
        return null;
    }

    public void handleFrameClosing() {
        if (releaseSelected!= null) {
            defaults.addPerReleaseSettingList(setup,releaseSelected.getLibraryName(), TRACKS_SELECTION ,getElementSelected());
        }
    }
}
