package com.ibm.sdwb.build390.userinterface.graphic.widgets;

import java.awt.*;
import javax.swing.*;
import javax.swing.event.*;
import javax.swing.*;
import com.ibm.sdwb.build390.*;
import com.ibm.sdwb.build390.library.LibraryInfo;
import com.ibm.sdwb.build390.logprocess.*;
import com.ibm.sdwb.build390.help.*;
import com.ibm.sdwb.build390.mainframe.*;
import com.ibm.sdwb.build390.userinterface.graphic.MainInterface;
import com.ibm.sdwb.build390.userinterface.UserCommunicationInterface;
import com.ibm.sdwb.build390.userinterface.graphic.widgets.*;
import com.ibm.sdwb.build390.userinterface.event.build.*;
import com.ibm.sdwb.build390.userinterface.event.*;

public class MainframeReleaseAndDriverSelectionPanel extends JPanel implements RequiredActionsCompletedInterface, UserInterfaceEventListener {

    private DriverSelectionCombo driverCombo = null;
    private JTextField shadowReleaseTextfield = null;
    private ReleaseInformation relInfo = null;

    private MBMainframeInfo mainInfo = null;
    private LibraryInfo libInfo = null;
    private UserCommunicationInterface userComm = null;
    private UserInterfaceListenerManager changeManager = new UserInterfaceListenerManager();

    public MainframeReleaseAndDriverSelectionPanel(LibraryInfo tempLib, MBMainframeInfo tempMain, UserCommunicationInterface tempComm) {
        userComm = tempComm;
        libInfo = tempLib;
        mainInfo = tempMain;
        layoutDialog();
    }
    public void setReleaseInfo(ReleaseInformation tempInfo) {
        if (relInfo == tempInfo) {
            return;
        }
        if(tempInfo != null) {
            if (tempInfo.equals(relInfo)){
                return;
            }
        }
        relInfo = tempInfo;
        if(relInfo !=null ) {
            shadowReleaseTextfield.setText(relInfo.getMvsHighLevelQualifier()+"."+relInfo.getMvsName());
        }else {
            shadowReleaseTextfield.setText(new String());
        }
        driverCombo.setRelease(relInfo);
    }

    private void layoutDialog() {
        setLayout(new SpringLayout());

        driverCombo = new DriverSelectionCombo(mainInfo, libInfo,userComm.getLEP());
        shadowReleaseTextfield = new JTextField();
        shadowReleaseTextfield.setEditable(false);
        shadowReleaseTextfield.setEnabled(false);
        // spacers
        add(Box.createGlue());
        add(Box.createGlue());

        add(new JLabel("S/390 Shadow"));
        add(shadowReleaseTextfield);

        // spacers
        add(Box.createGlue());
        add(Box.createGlue());

        add(new JLabel("S/390 Driver"));
        add(driverCombo);

        ReleaseAndDriverListener listener = new ReleaseAndDriverListener(this);
        driverCombo.getComboBox().addActionListener(listener);
        // spacers
        add(Box.createGlue());
        add(Box.createGlue());

        com.ibm.sdwb.build390.userinterface.graphic.utilities.GeneralUtilities.makeCompactGrid(this, -1, 2, 5, 5, 3, 3);

        setVisible(true);
    }

    public DriverInformation getDriverSelected() {
        return driverCombo.getSelectedDriver();
    }

    public  void setDriverSelected(DriverInformation info) {
        if (info!=null) {
            driverCombo.select(info.getName());
        }
    }

    public void addDriverActionListener(java.awt.event.ActionListener listener){
        driverCombo.getComboBox().addActionListener(listener);
    }

    public boolean isRequiredActionCompleted() {
        /* just make sure we have a driver */
        boolean done =driverCombo.getSelectedDriver()!=null;
        return done;
    }

    public void addUserInterfaceEventListener(UserInterfaceEventListener listener) {
        changeManager.addUserInterfaceEventListener(listener);
    }

    private class ReleaseAndDriverListener implements java.awt.event.ActionListener {
        private RequiredActionsCompletedInterface required = null;

        public ReleaseAndDriverListener(RequiredActionsCompletedInterface temp) {
            required = temp;
        }


        public void actionPerformed(java.awt.event.ActionEvent e) {
            DriverUpdateEvent newEvent = new DriverUpdateEvent(required);
            newEvent.setDriverInformation(getDriverSelected());
            changeManager.fireEvent(newEvent);
        }
    }

    public void handleUIEvent(UserInterfaceEvent e){
        if (e instanceof ReleaseUpdateEvent) {
            ReleaseUpdateEvent event = (ReleaseUpdateEvent) e;
            if (relInfo==event.getReleaseInformation()) {
                return;
            }
            if (relInfo!=null) {
                if (relInfo.equals(event.getReleaseInformation())) {
                    return;
                }
            }
            setReleaseInfo(event.getReleaseInformation());
        }
    }
}
