package com.ibm.sdwb.build390.userinterface.graphic.widgets;
import java.awt.*;
import java.awt.List;
import java.awt.event.*;
import java.util.*;
import javax.swing.*;
import com.ibm.sdwb.build390.*;
import com.ibm.sdwb.build390.logprocess.*;

public class JListDialogBox extends MBModalFrame {

    private JList     lbox;                                     // new listbox
    private JButton   MBC_Lbu_ok_      = new JButton("OK");      // ok button
    private JButton   MBC_Lbu_quit_    = new JButton("Cancel");    // quit button
    private JButton   select_all_      = new JButton("Select All"); // select all button
    private JPanel    pl               = new JPanel();           // panel for buttons
    private String[] elementsSelected   = null;           // String array to hold the elements selected in the list box.
    private String elementSelected   = null;           // String to hold the element selected in the list box.
    private MBButtonPanel buttonPanel;
    private boolean quitRequested = false;


    public JListDialogBox(String title, JInternalFrame pFrame,LogEventProcessor lep) {
        super(title, pFrame, lep);
        initializeBox(title);
    }



    public JListDialogBox(String title, java.util.List data, JInternalFrame pFrame,LogEventProcessor lep) {
        super(title, pFrame, lep);
        initializeBox(title);
        setData(data);
    }

    public void setData(java.util.List dataList){
        lbox.setListData(new Vector((Collection) dataList));
    }

    public void setAllowMultipeSelection(boolean multi){
        if (multi) {
            select_all_.setVisible(true);
            lbox.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
        }else {
            select_all_.setVisible(false);
            lbox.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        }
    }

    public void setSelectedItem(Object toSelect){
        lbox.setSelectedValue(toSelect, true);
    }

    private void initializeBox(String title) {
        // create frame and populate it
        setVisible(false);
        lbox = new JList();
        setBounds(50,50,250,220);
        getContentPane().setLayout(new BorderLayout());
        setForeground(MBGuiConstants.ColorRegularText);
        setBackground(MBGuiConstants.ColorGeneralBackground);
        GridBagLayout gridBag = new GridBagLayout();
        GridBagConstraints c = new GridBagConstraints();
        JPanel tempPanel = new JPanel(gridBag);
        JScrollPane listScroller = new JScrollPane(lbox);
        c.insets = new Insets(5,5,5,5);
        c.weightx = 1;
        c.weighty = 1;
        c.fill = GridBagConstraints.BOTH;
        gridBag.setConstraints(listScroller, c);
        tempPanel.add(listScroller);
        getContentPane().add(tempPanel, "Center");
        MBC_Lbu_quit_.setForeground(MBGuiConstants.ColorCancelButton);
        MBC_Lbu_ok_.setForeground(MBGuiConstants.ColorActionButton);
        Vector actionButtons = new Vector();
        select_all_.setForeground(java.awt.Color.blue);
        actionButtons.addElement(select_all_);
        actionButtons.addElement(MBC_Lbu_ok_);
        buttonPanel = new MBButtonPanel(null, MBC_Lbu_quit_, actionButtons);
        getContentPane().add(buttonPanel, "South");

        // add the action listener for the OK button
        MBC_Lbu_ok_.addActionListener(new SelectHandler());
        // add the action listener for the Quit button
        MBC_Lbu_quit_.addActionListener(new QHandler());

        if (lbox.getSelectionMode()==ListSelectionModel.MULTIPLE_INTERVAL_SELECTION) {
            select_all_.addActionListener(new ActionListener() {
                                              public void actionPerformed(ActionEvent evt) {
                                                  int cnt = lbox.getModel().getSize();
                                                  int[] inds = new int[cnt];
                                                  for (int lp=0; lp<cnt; lp++) {
                                                      inds[lp] = lp;
                                                  }
                                                  lbox.setSelectedIndices(inds);
                                              }} );
        }
    }

    /** getElementsSelected - returns the element selected from the list box
    * @return String elementSelected */
    public String[] getElementsSelected() {
        if (quitRequested) {
            return null;
        }
        if (lbox.getModel().getSize() > 1) {
            return elementsSelected;
        } else if (lbox.getModel().getSize() == 1) {
            ((MBInternalFrame) parentFrame).getStatus().updateStatus("Query resulted in 1 match, field updated", false);
            lbox.setSelectedIndex(0);
            String[] singleValue = new String[1];
            singleValue[0] = (String) lbox.getSelectedValues()[0];
            return singleValue;
        } else {
            return null;
        }
    }

    /** getElementSelected - returns the element selected from the list box
    * @return String elementSelected */
    public String getElementSelected() {
        if (quitRequested) {
            return null;
        }
        if (lbox.getModel().getSize() > 1) {
            return elementSelected;
        } else if (lbox.getModel().getSize() == 1) {
            ((MBInternalFrame) parentFrame).getStatus().updateStatus("Query resulted in 1 match, field updated", false);
            lbox.setSelectedIndex(0);
            return(String) lbox.getSelectedValue();
        } else {
            return null;
        }
    }

    /** Handle the Quit button for the list box */
    public class QHandler implements ActionListener {
        public void actionPerformed(ActionEvent evt) {
            lep.LogSecondaryInfo("Debug", "JListDialogBox:QHandler:Entry");
            quitRequested = true;
            dispose();
        }
    }

    /** Handle a item selection for the list box */
    public class SelectHandler implements ActionListener {
        public void actionPerformed(ActionEvent evt) {
            new Thread(new Runnable() {
                           public void run() {
                               //MBUtilities.Logit(MBConstants.DEBUG_DEV, "JListDialogBox:SelectHandler:Entry", "");
                               lep.LogSecondaryInfo("Debug", "JListDialogBox:SelectHandler:Entry");
                               // Release list request from DRIVER BUILD dialog
                               if (lbox.getSelectionMode()==ListSelectionModel.MULTIPLE_INTERVAL_SELECTION) {
                                   elementsSelected = new String[lbox.getSelectedValues().length];
                                   for (int i = 0; i < lbox.getSelectedValues().length; i++) {
                                       elementsSelected[i] = (String) lbox.getSelectedValues()[i];
                                   }
                                   //                elementsSelected = (String[])lbox.getSelectedValues();
                                   if (elementsSelected.length > 0) {
                                       dispose();
                                   } else {
                                       problemBox("Selection Error", "You must select something from the list");
                                       return;
                                   }
                               } else {
                                   if (lbox.getSelectedIndex() > -1) {
                                       elementSelected = (String) lbox.getSelectedValue();
                                       // Get BuildType selected
                                       dispose();
                                   } else {
                                       problemBox("Selection Error", "You must select something from the list");
                                   }
                               }
                           }
                       }).start();
        }
    }
}
