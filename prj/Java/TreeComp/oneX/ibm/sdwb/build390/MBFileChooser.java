package com.ibm.sdwb.build390;

import javax.swing.*;
import java.awt.*;
import java.io.File;

public class MBFileChooser extends JFileChooser {
    boolean wasCanceled = true;

    public void approveSelection() {
        wasCanceled=false;
        super.approveSelection();
    }

    public File getSelectedFile() {
        if (wasCanceled) {
            return null;
        }else {
            return super.getSelectedFile();
        }
    }

    public int showDialog(Component parent, String text) {
        wasCanceled = true;
        return super.showDialog(parent, text);
    }
}


