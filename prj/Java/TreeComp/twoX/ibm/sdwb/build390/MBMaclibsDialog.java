package com.ibm.sdwb.build390;
/*********************************************************************/
// This java class provides a dialog to be used to allow the user to
// manage a list of maclib data set names
// Changes
// Date     Defect/Feature      Reason
// 12/03/99 pjs			Update msgs to make leon happy.
// 12/07/99 			force check for blanks in maclib ds's
// 12/09/99             add bookmark for help
//12/03/2002 SDWB-2019 Enhance the help system
/*********************************************************************/

import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.util.*;
import javax.swing.*;
import com.ibm.sdwb.build390.help.*;

public class MBMaclibsDialog extends MBModalStatusFrame {

    private JInternalFrame pframe;
    private JPanel centerPanel = new JPanel();
    private JButton Listds;
    private Box vBox;
    private Vector data;
    private EditableJList dataList;
    private final JLabel helpLabel = new JLabel("To add an entry, right click in the list area.", SwingConstants.CENTER);
    private final JLabel helpLabel1= new JLabel("Select an entry, then right click on it to edit, remove or move it.", SwingConstants.CENTER);
    private StringBuffer editedSetting;
    JLabel HeadLabel;
    JButton bHelp;
    JButton bOK;

    public MBMaclibsDialog(JInternalFrame pFrame, StringBuffer currentSetting) {
        super("Edit Maclib Setting", pFrame, null);
        editedSetting = currentSetting;
        pframe=pFrame;
        // center panel
        MBInsetPanel centerPanel = new MBInsetPanel(new BorderLayout(), 5, 5, 5, 5);
        setForeground(MBGuiConstants.ColorRegularText);
        setBackground(MBGuiConstants.ColorGeneralBackground);
        // panel5
        JPanel panel5 = new JPanel(new BorderLayout());
        centerPanel.add(panel5);
        // head label
        HeadLabel = new JLabel("Maclibs");
        HeadLabel.setFont(new Font("Dialog", Font.BOLD, 12));
        HeadLabel.setForeground(MBGuiConstants.ColorGroupHeading);
        centerPanel.add("North", HeadLabel);
        // box to contain scrollable jlist
        vBox = Box.createVerticalBox();
        // Create a JList that displays the dsn's
        data = new Vector();
        if (editedSetting != null) {
            if (editedSetting.length()>0) {
                // values are seperated by a comma, parse them out
                StringTokenizer mtoke = new StringTokenizer(editedSetting.toString(), ",");
                while (mtoke.hasMoreTokens()) {
                    String mval = mtoke.nextToken();
                    data.addElement(mval);
                }
            }
        }
        dataList = new EditableJList(data, false);  // force check for blanks in maclib ds's
        // put it in a scrol pain
        JScrollPane scrollPane = new JScrollPane(dataList);
        // Set default width
        dataList.setPrototypeCellValue("'DDDDDDDD.DDDDDDDD.DDDDDDDD.DDDDDDDD'");
        // add to box
        vBox.add(scrollPane);
        // add instruction labels
        helpLabel.setForeground(MBGuiConstants.ColorGroupHeading);
        helpLabel1.setForeground(MBGuiConstants.ColorGroupHeading);
        vBox.add(helpLabel);
        vBox.add(helpLabel1);
        // add all to diaply
        panel5.add("Center", vBox);
        // add buttons
        bHelp = new JButton("Help");
        bHelp.setForeground(MBGuiConstants.ColorHelpButton);
        bOK = new JButton("OK");
        bOK.setForeground(MBGuiConstants.ColorActionButton);
        Vector actionButtons = new Vector();
        actionButtons.addElement(bOK);
        addButtonPanel(bHelp,actionButtons);
        // set title
        setTitle("Edit Maclib Setting");
        getContentPane().add("Center", centerPanel);
        // Help button action
        bHelp.setForeground(MBGuiConstants.ColorHelpButton);
        bHelp.addActionListener(new MBCancelableActionListener(thisFrame) {
                                    public void doAction(ActionEvent evt) {
                                        MBUtilities.ShowHelp("SPTUM",HelpTopicID.MACLIBSDIALOG_HELP);
                                    }} );
        // OK button action
        bOK.addActionListener(new MBCancelableActionListener(thisFrame) {
                                  public void doAction(ActionEvent avt) {
                                      saveData();
                                      //save();
                                      dispose();
                                  }
                              });
        setVisible(true);
        SymInternalFrame lSymInternalFrame = new SymInternalFrame();
        this.addInternalFrameListener(lSymInternalFrame);
    }

    // update the original value passed in with the current edited value
    public void saveData() {
        editedSetting.setLength(0);
        for (int i = 0; i < dataList.getModel().getSize(); i++) {
            editedSetting.append(((String)dataList.getModel().getElementAt(i)).trim());
            if (i<dataList.getModel().getSize()-1) {
                editedSetting.append(",");
            }
        }
    }

    public void dispose() {
        dispose(false);
    }

    // set minimum size
    public Dimension getMinimumSize() {
        //System.out.println(getSize());
        Dimension oldPref = new Dimension(330, 300);
        return oldPref;
    }

    class SymInternalFrame implements javax.swing.event.InternalFrameListener {
        public void internalFrameOpened(javax.swing.event.InternalFrameEvent event)
        {
        }

        public void internalFrameDeiconified(javax.swing.event.InternalFrameEvent event)
        {
            Object object = event.getSource();
            if (object == MBMaclibsDialog.this)
                MBMaclibsDialog_internalFrameDeiconified(event);
        }

        public void internalFrameIconified(javax.swing.event.InternalFrameEvent event)
        {
        }

        public void internalFrameDeactivated(javax.swing.event.InternalFrameEvent event)
        {
        }

        public void internalFrameClosed(javax.swing.event.InternalFrameEvent event)
        {
        }

        public void internalFrameActivated(javax.swing.event.InternalFrameEvent event)
        {
        }

        public void internalFrameClosing(javax.swing.event.InternalFrameEvent event)
        {
        }
    }

    void MBMaclibsDialog_internalFrameDeiconified(javax.swing.event.InternalFrameEvent event)
    {
        // to do: code goes here.
    }
}
