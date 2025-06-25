package com.ibm.sdwb.build390.library.cmvc.metadata.userinterface;

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.EventObject;

import javax.swing.Box;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.SpringLayout;

import com.ibm.sdwb.build390.MBMainframeInfo;
import com.ibm.sdwb.build390.library.SourceInfo;
import com.ibm.sdwb.build390.library.cmvc.CMVCLibraryInfo;
import com.ibm.sdwb.build390.library.cmvc.CMVCTrackSourceInfo;
import com.ibm.sdwb.build390.library.userinterface.SourceSelection;
import com.ibm.sdwb.build390.userinterface.event.SelectionUpdateEvent;
import com.ibm.sdwb.build390.userinterface.event.UserInterfaceEvent;
import com.ibm.sdwb.build390.userinterface.event.UserInterfaceEventListener;
import com.ibm.sdwb.build390.userinterface.event.build.ReleaseUpdateEvent;
import com.ibm.sdwb.build390.userinterface.graphic.widgets.CancelableAction;
import com.ibm.sdwb.build390.userinterface.graphic.widgets.ReleaseSelectionCombo;
import com.ibm.sdwb.build390.userinterface.graphic.widgets.RequiredActionsCompletedInterface;


public class CMVCTrackSourceSelection extends SourceSelection {


    private ReleaseSelectionCombo releaseCombo = null;
    private TracksInReleaseCombo trackCombo = null;


    private CMVCLibraryInfo libInfo = null;
    private MBMainframeInfo mainInfo = null;

    public CMVCTrackSourceSelection(com.ibm.sdwb.build390.library.LibraryInfo tempLib, MBMainframeInfo tempMain) {
        libInfo = (CMVCLibraryInfo) tempLib;
        mainInfo = tempMain;      

        layoutDialog();
    }



    private void layoutDialog() {
        setLayout(new SpringLayout());

        releaseCombo = new ReleaseSelectionCombo(mainInfo, libInfo, libInfo.getLEP());
        trackCombo = new TracksInReleaseCombo(mainInfo,libInfo, libInfo.getLEP());
        // spacers
        add(Box.createGlue());
        add(Box.createGlue());

        add(new JLabel("Library Release"));
        add(releaseCombo);

        CMVCTrackSourceSelectionListener listener = new CMVCTrackSourceSelectionListener(this);
        releaseCombo.addListActionListener(listener);

        // spacers
        add(Box.createGlue());
        add(Box.createGlue());

        add(new JLabel("Unassigned Tracks"));
        add(trackCombo);
        trackCombo.addListActionListener(listener);
        // spacers
        add(Box.createGlue());
        add(Box.createGlue());

        com.ibm.sdwb.build390.userinterface.graphic.utilities.GeneralUtilities.makeCompactGrid(this, -1, 2, 1, 1, 2, 2);




        final Component comp = this;
      releaseCombo.addListItemListener(new ItemListener () {
                                             public void itemStateChanged(ItemEvent e) {
                                                 switch (e.getStateChange()) {
                                                 case ItemEvent.SELECTED :
                                                     (new CancelableAction("Release Refresh") {
                                                          public void doAction(ActionEvent e) {
                                                              trackCombo.setRelease(getProjectChosen());
                                                          }

                                                          public void postAction(){
                                                              fireProjectUpdated();
                                                          }

                                                      }).actionPerformed(new ActionEvent(comp,ActionEvent.ACTION_PERFORMED,"initial"));
                                                     break;
                                                 }

                                             }
                                         });

        addUserInterfaceEventListener(new UserInterfaceEventListener() {
                                          public void handleUIEvent(UserInterfaceEvent tempEvent) {
                                              if (tempEvent instanceof ReleaseUpdateEvent) {
                                                  ReleaseUpdateEvent event = (ReleaseUpdateEvent) tempEvent;
                                                  trackCombo.setRelease(event.getReleaseInformation());
                                              }

                                          }
                                      });


        setVisible(true);

    }


    public SourceInfo getSourceInfo() {
        String selectedString = trackCombo.getSelectedTrack();
        String libraryRelease = null;
        if(releaseCombo.getSelectedRelease()!=null){
            libraryRelease = releaseCombo.getSelectedRelease().getLibraryName();
        }
        CMVCTrackSourceInfo trackInfo = new CMVCTrackSourceInfo(libInfo, libraryRelease, selectedString,  null);
        return trackInfo;
    }

    public void setSourceInfo(SourceInfo info) {
        trackCombo.select(info.getName());
    }

    public com.ibm.sdwb.build390.mainframe.ReleaseInformation getProjectChosen() {
        return releaseCombo.getSelectedRelease();
    }

    public boolean isRequiredActionCompleted() {
        /* check the release has been chosen and a level or track has been chosen */
        if (releaseCombo.getElementSelected()!=null) {
            if (trackCombo.getSelectedTrack()!=null) {
                return true;
            }
        }
        return false;
    }

    private class CMVCTrackSourceSelectionListener implements java.awt.event.ActionListener, javax.swing.event.ChangeListener {
        private RequiredActionsCompletedInterface required = null;

        public CMVCTrackSourceSelectionListener(RequiredActionsCompletedInterface temp) {
            required = temp;
        }


        public void actionPerformed(java.awt.event.ActionEvent e) {
            doEventStuff(e);
        }

        public void stateChanged(javax.swing.event.ChangeEvent e) {
            doEventStuff(e);
        }

        private void doEventStuff(EventObject e) {
            UserInterfaceEvent newEvent = new UserInterfaceEvent(required);
            fireEvent(newEvent);
            if (e.getSource() == releaseCombo.getComboBox()) {
                // it's a release update event
                ReleaseUpdateEvent rue = new ReleaseUpdateEvent(e.getSource());
                rue.setReleaseInformation(releaseCombo.getSelectedRelease());
                fireEvent(rue);
            }

            if (e.getSource() == trackCombo.getComboBox()) {
                SelectionUpdateEvent sce = new SelectionUpdateEvent(e.getSource(),trackCombo.getSelectedTrack());
                fireEvent(sce);
            }
        }
    }
}
