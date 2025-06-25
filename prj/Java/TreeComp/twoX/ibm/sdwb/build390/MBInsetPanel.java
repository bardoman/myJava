package com.ibm.sdwb.build390;

import javax.swing.*;
import java.awt.*;

public class MBInsetPanel extends JPanel {

    private Insets panelInsets = null;

    public MBInsetPanel(LayoutManager layout, int top, int left, int bottom, int right) {
        super(layout);
        setInsets(top,left,bottom,right);
    }

    public MBInsetPanel(int top, int left, int bottom, int right) {
        setInsets(top,left,bottom,right);
    }

    public void setInsets(int top, int left, int bottom, int right) {
        panelInsets = new Insets(top, left,bottom, right) ;
    }

    public Insets getInsets() {
        return panelInsets;
    }
}
