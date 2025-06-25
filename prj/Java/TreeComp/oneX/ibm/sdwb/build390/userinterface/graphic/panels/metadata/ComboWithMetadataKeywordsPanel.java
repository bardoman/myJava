
package com.ibm.sdwb.build390.userinterface.graphic.panels.metadata;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.util.*;
import javax.swing.border.*;

import javax.swing.*;
import javax.swing.table.*;
import com.ibm.sdwb.build390.*;
import com.ibm.sdwb.build390.logprocess.*;
import com.ibm.sdwb.build390.userinterface.graphic.widgets.*;

public class  ComboWithMetadataKeywordsPanel extends JPanel {


    private JComboBox  searchText     = new JComboBox();
    private GridBagLayout gridBag1;
    private GridBagConstraints c1;
    private ComboBoxWithHistory findHistoryCombo;
    private String key=null;


    public ComboWithMetadataKeywordsPanel(String key)
    {
        this.key = key;
        gridBag1 = new GridBagLayout();
        c1 = new GridBagConstraints();
        setLayout(gridBag1);
        initializeUI(key);
    }

    //Begin TST2277
    public ComboWithMetadataKeywordsPanel(String key, ComboBoxWithHistory findHistoryCombo)
    {
        this.key = key;

        this.findHistoryCombo = findHistoryCombo;

        gridBag1 = new GridBagLayout();
        c1 = new GridBagConstraints();
        setLayout(gridBag1);
        initializeUI(key);
    }
    //End TST2277


    public String  getKey()
    {
        return key;
    }

    private void initializeUI(String key)
    {
        c1.gridx = 1;
        c1.gridy = 1;
        c1.insets = new Insets(2, 2, 2, 2);
        c1.weightx = 1.0D;
        gridBag1.setConstraints(searchText, c1);
        add(searchText);

        c1.gridx = 2;
        c1.insets = new Insets(2, 2, 2, 2);
        c1.fill = 1;
        c1.weightx = 1.0D;
        c1.gridwidth = -1;
        JLabel equalLabel = new JLabel("=");
        gridBag1.setConstraints(equalLabel, c1);
        add(equalLabel);

        //Begin TST2277
        if (findHistoryCombo==null) {
            findHistoryCombo = new ComboBoxWithHistory(key);
        }
        //End TST2277

        findHistoryCombo.setEnabled(true);
        findHistoryCombo.setEditable(true);
        c1.gridx = 3;
        c1.insets = new Insets(2, 2, 2, 2);
        c1.weightx = 1.0D;
        c1.gridwidth = 0;
        gridBag1.setConstraints(findHistoryCombo, c1);
        add(findHistoryCombo);
    }



    public void setInput(String[] input)
    {
        searchText.setModel(new DefaultComboBoxModel(input));
    }


    public String  getMetadataKeyword()
    {
        return(String)searchText.getSelectedItem();

    }

    public String getFindEntry()
    {
        return(String)findHistoryCombo.getSelectedItem();

    }

    public static void main(String[] args)
    {
        JFrame frame = new JFrame();
        frame.getContentPane().add(new ComboWithMetadataKeywordsPanel("test"));
        frame.pack();
        frame.show();

    }


}


