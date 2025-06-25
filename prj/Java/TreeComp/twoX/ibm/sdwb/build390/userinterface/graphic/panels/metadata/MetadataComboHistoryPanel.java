package com.ibm.sdwb.build390.userinterface.graphic.panels.metadata;
import java.awt.*;
import java.util.Vector;

import javax.swing.*;
import javax.swing.border.LineBorder;

import com.ibm.sdwb.build390.MBGuiConstants;

public class MetadataComboHistoryPanel extends JPanel {

    private GridBagLayout gridBag1;
    private GridBagConstraints c1;
    private JButton btRunFilterHistory;

    public MetadataComboHistoryPanel(String name,JComboBox combo, Action action) {
        gridBag1 = new GridBagLayout();
        c1 = new GridBagConstraints();
        setLayout(gridBag1);
        initializeActions(action);
        initializeUI(name,combo);
    }

    private void initializeUI(String name, JComboBox combo) {
        int gridXIndex =1;
        if (name!=null) {
            JLabel filterHistoryLabel = new JLabel(name);
            c1.gridx = gridXIndex;
            c1.gridy = 1;
            c1.insets = new Insets(2, 2, 2, 2);
            c1.weightx = 1.0D;
            gridBag1.setConstraints(filterHistoryLabel, c1);
            add(filterHistoryLabel);
            gridXIndex++;
            setBorder(BorderFactory.createTitledBorder(LineBorder.createGrayLineBorder(),name , 0, 0, null, MBGuiConstants.ColorGroupHeading));
        }
        c1.gridx = gridXIndex;
        c1.insets = new Insets(2, 2, 2, 2);
        c1.fill = 1;
        c1.weightx = 1.0D;
        c1.gridwidth = -1;
        gridBag1.setConstraints(combo, c1);
        add(combo);
        gridXIndex++;

        c1.gridx = gridXIndex;
        c1.insets = new Insets(2, 2, 2, 2);
        c1.weightx = 1.0D;
        c1.gridwidth = 0;
        gridBag1.setConstraints(btRunFilterHistory, c1);
        add(btRunFilterHistory);
        //name = ""Filter History (Host) "
    }


    private void initializeActions(Action action) {
        btRunFilterHistory = new JButton(action);
    }

    public Action getAction(){
        return btRunFilterHistory.getAction();
    }


}
