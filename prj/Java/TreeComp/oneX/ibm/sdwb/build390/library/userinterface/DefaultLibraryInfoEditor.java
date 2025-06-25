package com.ibm.sdwb.build390.library.userinterface;

import java.awt.*;
import java.awt.event.*;
import java.util.*;
import javax.swing.*;
import java.io.*;
import com.ibm.sdwb.build390.help.*;
import com.ibm.sdwb.build390.*;
import com.ibm.sdwb.build390.library.LibraryInfo;

public abstract class DefaultLibraryInfoEditor extends LibraryInfoEditor {

    private JTextField tfProcessServerAddress = new JTextField();
    private JTextField tfProcessServerPort = new JTextField();
    private LibraryInfo libInfo = null;
    private Component insertionPointMarker = null;
    private int portLabelIndex = -1;
    protected JLabel addressLabel = new JLabel("Address");

    public DefaultLibraryInfoEditor(JInternalFrame tempParentFrame, LibraryInfo tempLib) {
        super(tempParentFrame, tempLib);

        libInfo = tempLib;

        addressLabel.setLabelFor(tfProcessServerAddress);
        JLabel processServerPortLabel = new JLabel("Process Server Port");
        insertionPointMarker = processServerPortLabel;
        processServerPortLabel.setLabelFor(tfProcessServerPort);

        insertComponentAtTheEnd(addressLabel);
        insertComponentAtTheEnd(tfProcessServerAddress);
        insertComponentAtTheEnd(processServerPortLabel);
        insertComponentAtTheEnd(tfProcessServerPort);


        tfProcessServerAddress.setText(nonNull(libInfo.getProcessServerAddress()));
        if (libInfo.getProcessServerPort() > 0) {
            tfProcessServerPort.setText((Integer.toString(libInfo.getProcessServerPort())));
        }


    }

    protected void insertComponentBeforePortLabel(Component newComponent) {
        Component[] comps = mainPanel.getComponents();
        int portLabelIndex = -1;
        for (int i = 0; i < comps.length; i++) {
            if (insertionPointMarker == comps[i]) {  // we've found the index of the Process Server Port label
                portLabelIndex = i;
                i = comps.length;
            }
        }
        mainPanel.add(newComponent, portLabelIndex);
        com.ibm.sdwb.build390.userinterface.graphic.utilities.GeneralUtilities.makeCompactGrid(mainPanel, -1, 2, 5, 5, 3, 3);
    }

    protected String doErrorChecking() {
        String famAdr = tfProcessServerAddress.getText();
        String famRMIPort = tfProcessServerPort.getText();
        String errorMessage = super.doErrorChecking();

        if (famAdr!=null && famAdr.trim().length() < 1) {
            errorMessage +="You must enter a library address.\n";
        }

        try {
            Integer.parseInt(tfProcessServerPort.getText());
        } catch (NumberFormatException nfe) {
            errorMessage += "You must enter a number for the process server port.\n";
        }

        if (errorMessage.trim().length() == 0) {
            libInfo.setProcessServerAddress(tfProcessServerAddress.getText());
            libInfo.setProcessServerPort(Integer.parseInt(tfProcessServerPort.getText()));
        }
        return errorMessage;
    }

    public Dimension getPreferredSize() {
        Dimension old = super.getPreferredSize();
        if (old.width < 250) {
            old.width = 250;
        }
        return old;
    }

    public void postVisibleInitialization() {
    }

    private String nonNull(String temp) {
        if (temp!=null) {
            return temp;
        }
        return new String();
    }


    private class doOk extends com.ibm.sdwb.build390.userinterface.graphic.widgets.CancelableAction {

        doOk() {
            super("Ok");
        }
        /* the method to override for whatever action you want to perform in response
        to a click.
        */
        public void doAction(ActionEvent e) {
            String errorMessage = doErrorChecking();
            if (errorMessage.trim().length() > 0) {
                new MBMsgBox("Error", errorMessage, thisFrame);
            } else {
                libInfo.setProcessServerAddress(tfProcessServerAddress.getText());
                libInfo.setProcessServerPort(Integer.parseInt(tfProcessServerPort.getText()));
                dispose();
            }
        }
    }
}
