package com.ibm.sdwb.build390.library.clearcase.userinterface;

import javax.swing.*;
import java.io.File;
import java.awt.BorderLayout;
import com.ibm.sdwb.build390.userinterface.graphic.widgets.*;

class ViewMountPointChooser extends JPanel {

    private ComboBoxWithHistory mountPointCombo = null;
    private MountPointSelectionAction mountAction = null;
    private static final String VIEW_MOUNT_POINT = "VIEW_MOUNT_POINT";

    ViewMountPointChooser() {
        mountPointCombo = new ComboBoxWithHistory(VIEW_MOUNT_POINT);
        mountAction = new MountPointSelectionAction(mountPointCombo);
        layoutPanel();
    }

    private void layoutPanel() {
        setLayout(new BorderLayout());
        add(new JLabel("View Mount Point"), BorderLayout.WEST);
        add(mountPointCombo, BorderLayout.CENTER);
        add(new JButton(mountAction), BorderLayout.EAST);

    }

    File getSelectedMountPoint() {
        Object selectedObject = mountPointCombo.getValidSelectedItem();
        if (selectedObject instanceof File) {
            return (File) selectedObject;
        }else if (selectedObject != null) {
            return new File(selectedObject.toString());
        }
        
        return null;
    }

    public void setSelectedMountPoint(File file) {
        mountPointCombo.setSelectedItem(file);
    }


    class MountPointSelectionAction extends AbstractAction {

        private ComboBoxWithHistory combo = null;
        private File chosenFile = null;


        MountPointSelectionAction(ComboBoxWithHistory tempComp) {
            super("Browse");
            combo = tempComp;
        }

        public void actionPerformed(java.awt.event.ActionEvent e) {
            JFileChooser chooser = new JFileChooser();
            chooser.setMultiSelectionEnabled(false);
            chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
            int returnVal = chooser.showOpenDialog(combo);
            if(returnVal == JFileChooser.APPROVE_OPTION) {
                File selectedFile = new File(chooser.getSelectedFile().toURI());// needs to be done, the file returned from the call is a non serializeable sublcass of file
                combo.addItem(selectedFile);
                combo.setSelectedItem(selectedFile);
            }
        }
    }

}
