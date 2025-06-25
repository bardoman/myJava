package com.ibm.sdwb.build390.userinterface.graphic.panels.multiprocesspanels.usermod;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.List;
import java.util.Vector;

import javax.swing.*;
import javax.swing.event.ListSelectionListener;//3455A
import javax.swing.event.ListSelectionEvent;//3455A

import com.ibm.sdwb.build390.MBBuildException;
import com.ibm.sdwb.build390.MBCancelableActionListener;
import com.ibm.sdwb.build390.MBGuiConstants;
import com.ibm.sdwb.build390.MBInsetPanel;
import com.ibm.sdwb.build390.MBInternalFrame;
import com.ibm.sdwb.build390.MBModalStatusFrame;
import com.ibm.sdwb.build390.MBMsgBox;
import com.ibm.sdwb.build390.MBUtilities;
import com.ibm.sdwb.build390.help.HelpTopicID;


public class LogicDialog extends MBModalStatusFrame {

    private JMenuItem bAdd;
    private JMenuItem bDelete;
    private DefaultListModel lModelReqLogic = new DefaultListModel();
    private JList lReqLogic = new JList(lModelReqLogic);
    private JLabel listLabel;
    private JTextField tfIfReq;
    private JTextField tfAddReq;
    private JLabel lblCondition;
    private JLabel ifLabel;
    private JButton bSubmit;
    private JButton bHelp;
    private MBInsetPanel mainPanel = new MBInsetPanel(new BorderLayout(), 10,10,10,10);
    private java.util.List<String> returnVect = new ArrayList<String>();
    private static final String IFKEYWORD = "IF";


    /** Constructor */
    public LogicDialog(MBInternalFrame pFrame, java.util.List<String> reqList) throws MBBuildException {
        super("Usermod Logic", pFrame, null);

        getContentPane().add("Center", mainPanel);
        lReqLogic.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
        bAdd = new JMenuItem("Add");
        bDelete = new JMenuItem("Delete");
        JMenu reqsMenu = new JMenu("Edit Reqs");
        getJMenuBar().add(reqsMenu);
        reqsMenu.add(bAdd);
        reqsMenu.add(bDelete);
        bDelete.setEnabled(false);

        JScrollPane tempScroll = new JScrollPane(lReqLogic);
        mainPanel.add("Center", tempScroll);
        if (reqList != null) {
            for (Iterator reqIterator = reqList.iterator() ; reqIterator.hasNext();) {
                lModelReqLogic.addElement(IFKEYWORD + " "+(String) reqIterator.next());
            }
        }

        returnVect=reqList;//3455A
        //bDelete.setEnabled(!lModelReqLogic.isEmpty());//3455A

        listLabel = new JLabel("REQ Logic");
        mainPanel.add("North", listLabel);
        Box mainBox = Box.createVerticalBox();
        mainPanel.add("South", mainBox);
        Box subBox = Box.createHorizontalBox();
        mainBox.add(subBox);
        ifLabel = new JLabel(IFKEYWORD);
        subBox.add(ifLabel);
        tfIfReq = new JTextField();
        subBox.add(tfIfReq);
        subBox = Box.createHorizontalBox();
        mainBox.add(subBox);
        lblCondition = new JLabel("Req condition:");
        subBox.add(lblCondition);
        tfAddReq = new JTextField();
        subBox.add(tfAddReq);
        setTitle("Usermod Logic");
        //}}
        //manual input
        bSubmit = new JButton("Ok");
        bHelp = new JButton("Help");
        setForeground(MBGuiConstants.ColorRegularText);
        setBackground(MBGuiConstants.ColorGeneralBackground);
        lReqLogic.setBackground(MBGuiConstants.ColorFieldBackground);
        tfIfReq.setBackground(MBGuiConstants.ColorFieldBackground);
        tfAddReq.setBackground(MBGuiConstants.ColorFieldBackground);
        bSubmit.setForeground(MBGuiConstants.ColorActionButton);
        bHelp.setForeground(MBGuiConstants.ColorHelpButton);
        Vector actionButtons = new Vector();
        actionButtons.addElement(bSubmit);
        bHelp.setForeground(MBGuiConstants.ColorHelpButton);
        addButtonPanel(bHelp, actionButtons);
        
        ListSelectionModel listSelectionModel = lReqLogic.getSelectionModel();//3455A
        listSelectionModel.addListSelectionListener(new ReqLogicListSelectionHandler());//3455A

        bAdd.addActionListener(new MBCancelableActionListener(thisFrame) {
                                   public void doAction(ActionEvent evt) {
                                       if (tfIfReq.getText().trim().length() > 0 & tfAddReq.getText().trim().length() > 0) {
                                           String reqString = IFKEYWORD+" " + tfIfReq.getText().trim()+"."+tfAddReq.getText().trim();
                                           lModelReqLogic.addElement(reqString);
                                           tfIfReq.setText("");
                                           tfAddReq.setText("");
                                       } else {//3455A
                                    		new MBMsgBox("Add Requisite Error!","Please enter the IFREQ condition.");
                                       }//3455A
                                       repaint();
                                       //TST3455<Start>
                                       Enumeration reqEnum = lModelReqLogic.elements();
                                       List<String> temp = new ArrayList<String>();
                                       while (reqEnum.hasMoreElements()) {
                                           String currIf = (String) reqEnum.nextElement();
                                           currIf = currIf.substring(IFKEYWORD.length()).trim();
                                           temp.add(currIf);
                                       }
                                       returnVect = temp;
                                       //TST3455<End>
                                   }
                                   public void postAction() {
                                       bDelete.setEnabled(!lModelReqLogic.isEmpty() && lReqLogic.getSelectedIndices().length>0);//3455A
                                   }
                               } );

        bDelete.addActionListener(new MBCancelableActionListener(thisFrame) {
                                      public void doAction(ActionEvent evt) {
                                          SwingUtilities.invokeLater(new Runnable () {
                                                                         public void run() {
                                                                             int[] idxs = lReqLogic.getSelectedIndices();
                                                                             if (idxs.length > 0) {
                                                                                 for (int x=idxs.length; x>0; x--) {
                                                                                     lModelReqLogic.remove(idxs[x-1]);
                                                                                     returnVect.remove(idxs[x-1]);//TST3455A
                                                                                 }

                                                                                 repaint();
                                                                             } else {
                                                                                 new MBMsgBox("Error:","Select the Approriate REQ Logic, you want to delete..");
                                                                             }
                                                                         }
                                                                     });
                                      }

                                      public void postAction() {
                                          bDelete.setEnabled(!lModelReqLogic.isEmpty()&& lReqLogic.getSelectedIndices().length>0);
                                      }

                                  } );

        bHelp.addActionListener(new MBCancelableActionListener(thisFrame) {
                                    public void doAction(ActionEvent evt) {
                                        //MBUtilities.ShowHelp("Usermod");
                                        MBUtilities.ShowHelp("HDRUSERMOD",HelpTopicID.USERMODLOGICDIALOG_HELP);
                                    }
                                } );

        bSubmit.addActionListener(new MBCancelableActionListener(thisFrame) {
                                      public void doAction(ActionEvent evt) {
                                          dispose();
                                      }
                                  } );
        setVisible(true);
    }

    public java.util.List<String> getLogic() {
        return returnVect;
    }
    
    //Begin 3455A
    private class ReqLogicListSelectionHandler implements ListSelectionListener {
            public void valueChanged(ListSelectionEvent e) { 
            	ListSelectionModel lsm = (ListSelectionModel)e.getSource();
            	if(!lsm.isSelectionEmpty()){
            		bDelete.setEnabled(true);
            	}
            }
    }
    //End 3455A
}
