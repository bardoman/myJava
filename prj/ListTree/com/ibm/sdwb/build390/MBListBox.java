package com.ibm.sdwb.build390;
/***************************************************************************/
/* Java MBListBox class for the Build/390 client                           */
/*  Builds a listbox, populates it and adds the action listeners specified */
// 03/04/99     PTFstuff        allow a list box that only displays information, and returns nothing.
// 03/25/99     Defect_239      select all does not work
// 03/07/2000   reworklog       changes to write the log stuff using listeners
/***************************************************************************/
import java.awt.*;
import java.awt.List;
import java.awt.event.*;
import java.util.*;
import javax.swing.*;
import com.ibm.sdwb.build390.logprocess.*;

/** <br>The MBListBox displays a list of choices in a listbox and adds the correct action listener.
* Classes using this class must implement a listener for the OK button and another for the Quit button */
public class MBListBox extends MBModalFrame {

    private JList     lbox;                                     // new listbox
    private JButton   MBC_Lbu_ok_      = new JButton("OK");      // ok button
    private JButton   MBC_Lbu_quit_    = new JButton("Cancel");    // quit button
    private JButton   select_all_      = new JButton("Select All"); // select all button
    private JPanel    pl               = new JPanel();           // panel for buttons
    private boolean multi_;                                    // global flag indicating that multi select is enabled
    private ActionListener xselh;                             // action listener for the ok button
    private ActionListener xqh;                               // action listener for the quit button
    private String[] elementsSelected   = null;           // String array to hold the elements selected in the list box.
    private String elementSelected   = null;           // String to hold the element selected in the list box.
    private MBButtonPanel buttonPanel;
    private boolean autoRet = false;
    private boolean displayOnly = false;


    /** Constructor - Builds the frame and listbox and populates the listbox.
    * @param title String containing the title for the listbox
    * @param data Vector containing the tokenized data to be placed into the listbox
    * @param xselh ActionListener to be used for the ok button
    * @param xqh ActionListener to be used for the quit button
    * @param multi If true a multiple selection listbox is to be created, otherwise a single selection listbox is created.
    */

    public MBListBox(String title, String data, boolean multi, JInternalFrame pFrame,LogEventProcessor lep) {
        super(title, pFrame, lep);
        Vector dataVector = new Vector();
        StringTokenizer st = new StringTokenizer(data, "@", false);
        while (st.hasMoreTokens()) {
            dataVector.addElement(st.nextToken());
        }
        initializeBox(title, dataVector,multi, true);
    }



    /** Constructor - Builds the frame and listbox and populates the listbox.
    * @param title String containing the title for the listbox
    * @param data StringBuffer containing the tokenized data to be placed into the listbox. The @ character
    * is used as the token delimeter.
    * @param xselh ActionListener to be used for the ok button
    * @param xqh ActionListener to be used for the quit button
    * @param multi If true a multiple selection listbox is to be created, otherwise a single selection listbox is created.
    */
    public MBListBox(String title, Vector data, boolean multi, JInternalFrame pFrame,LogEventProcessor lep) {
        super(title, pFrame, lep);
        initializeBox(title, data, multi, true);
    }


    /** Constructor - Builds the frame and listbox and populates the listbox.
    * @param title String containing the title for the listbox
    * @param data StringBuffer containing the tokenized data to be placed into the listbox. The @ character
    * is used as the token delimeter.
    * @param xselh ActionListener to be used for the ok button
    * @param xqh ActionListener to be used for the quit button
    * @param multi If true a multiple selection listbox is to be created, otherwise a single selection listbox is created.
    */
    public MBListBox(String title, Enumeration data, boolean multi, JInternalFrame pFrame,LogEventProcessor lep) {
        super(title, pFrame, lep);
        Vector dataVector = new Vector();
        while (data.hasMoreElements()) {
            dataVector.addElement(data.nextElement());
        }
        initializeBox(title, dataVector, multi, true);
    }

    /** Constructor - Builds the frame and listbox and populates the listbox.
    * @param title String containing the title for the listbox
    * @param data StringBuffer containing the tokenized data to be placed into the listbox. The @ character
    * is used as the token delimeter.
    * @param xselh ActionListener to be used for the ok button
    * @param xqh ActionListener to be used for the quit button
    * @param multi If true a multiple selection listbox is to be created, otherwise a single selection listbox is created.
    */
    public MBListBox(String title, Enumeration data, boolean multi, boolean autoReturn, JInternalFrame pFrame,LogEventProcessor lep) {
        super(title, pFrame, lep);
        Vector dataVector = new Vector();
        while (data.hasMoreElements()) {
            dataVector.addElement(data.nextElement());
        }
        initializeBox(title, dataVector, multi, autoReturn);
    }

    /** Constructor - Builds the frame and listbox and populates the listbox.
    * @param title String containing the title for the listbox
    * @param data StringBuffer containing the tokenized data to be placed into the listbox. The @ character
    * is used as the token delimeter.
    * @param xselh ActionListener to be used for the ok button
    * @param xqh ActionListener to be used for the quit button
    * @param multi If true a multiple selection listbox is to be created, otherwise a single selection listbox is created.
    */
    public MBListBox(String title, Vector data, JInternalFrame pFrame,LogEventProcessor lep) {
        super(title, pFrame, lep);
        displayOnly=true;
        initializeBox(title, data, false, false);
    }

    private void initializeBox(String title, Vector elements, boolean multi, boolean autoReturn) {
        xselh = new SelectHandler();
        xqh = new QHandler();
        multi_ = multi;
        autoRet = autoReturn;

        // create frame and populate it
        setVisible(false);
        lbox = new JList(elements);
        if (multi) {
            lbox.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
        } else {
            lbox.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        }
        lbox.setBackground(MBGuiConstants.ColorFieldBackground);
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
        if (multi) {
            select_all_.setForeground(java.awt.Color.blue);
            actionButtons.addElement(select_all_);
        }
        if (!displayOnly) {
            actionButtons.addElement(MBC_Lbu_ok_);
        }
        buttonPanel = new MBButtonPanel(null, MBC_Lbu_quit_, actionButtons);
        getContentPane().add(buttonPanel, "South");

        // add the action listener for the OK button
        MBC_Lbu_ok_.addActionListener(xselh);
        // add the action listener for the Quit button
        MBC_Lbu_quit_.addActionListener(xqh);
        // add the action listener for Item selected
/*        lbox.addActionListener(xselh);*/

        // Handle Options-Setup //Defect_239 rewrite for jlist
        if (multi) {
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
        if (lbox.getModel().getSize() > 1 | !autoReturn) {
            setVisible(true);
        } else {
            dispose();
        }
    }

    /** getElementsSelected - returns the element selected from the list box
    * @return String elementSelected */
    public String[] getElementsSelected() {
        if (lbox.getModel().getSize() > 1 | !autoRet) {
            return elementsSelected;
        } else if (lbox.getModel().getSize() == 1) {
            ((MBInternalFrame) parentFrame).getStatus().updateStatus("Query resulted in 1 match, field updated", false);
            lbox.setSelectedIndex(0);
            String[] singleValue = new String[1];
            singleValue[0] = (String) lbox.getSelectedValues()[0];
            return singleValue;
        } else {
            new MBMsgBox("No entries were found", "Empty Query Result");
            return new String[0];
        }
    }

    /** getElementSelected - returns the element selected from the list box
    * @return String elementSelected */
    public String getElementSelected() {
        if (lbox.getModel().getSize() > 1 | !autoRet) {
            return elementSelected;
        } else if (lbox.getModel().getSize() == 1) {
            ((MBInternalFrame) parentFrame).getStatus().updateStatus("Query resulted in 1 match, field updated", false);
            lbox.setSelectedIndex(0);
            return(String) lbox.getSelectedValue();
        } else {
            new MBMsgBox("No entries were found", "Empty Query Result");
            return new String();
        }
    }

    /** Handle the Quit button for the list box */
    public class QHandler implements ActionListener {
        public void actionPerformed(ActionEvent evt) {
            //MBUtilities.Logit(MBConstants.DEBUG_DEV, "MBListBox:QHandler:Entry", "");
            lep.LogSecondaryInfo("Debug", "MBListBox:QHandler:Entry");
            dispose();
        }
    }

    /** Handle a item selection for the list box */
    public class SelectHandler implements ActionListener {
        public void actionPerformed(ActionEvent evt) {
            new Thread(new Runnable() {
                           public void run() {
                               //MBUtilities.Logit(MBConstants.DEBUG_DEV, "MBListBox:SelectHandler:Entry", "");
                               lep.LogSecondaryInfo("Debug", "MBListBox:SelectHandler:Entry");
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
