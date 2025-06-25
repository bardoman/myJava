package com.ibm.sdwb.build390;
/*
    A basic extension of the MBInternalFrame class
 */
//12/03/2002 SDWB-2019 Enhance the help system

import java.awt.Dimension;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Hashtable;
import java.util.Vector;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableColumn;

import com.ibm.sdwb.build390.help.HelpTopicID;
import com.ibm.sdwb.build390.user.Setup;

/** <br>The MBJobSchedulerPage class creates and manages the JobScheduler dialog for the MBClient.*/
public class MBJobSchedulerPage extends MBInternalFrame {

    private Vector columnHeadings = new Vector();
    private DefaultTableModel JobSchedulerModel;
    private  JTable JobSchedulerTable;
    private Vector schedulerInfoVector = new Vector();
    private JButton btnHelp;
    private JMenuItem btnAdd;
    private JMenuItem btnRemove;
    private JMenuItem btnReplace;
    private JMenuItem btnDeactivate;
    private JMenuItem btnReactivate;
    private JButton btnRefresh;
    private JLabel txt1;
    private Setup setup = null;
    private String columnConstant = "JOBSCHEDULERCOL";

    /** @param parent The parent frame
    * @param modal modal flag */
    public MBJobSchedulerPage(Setup tempSetup) {
        super("Scheduler "+tempSetup.getMainframeInfo().getMainframeAddress()+"@"+tempSetup.getMainframeInfo().getMainframePort(), false, null);
        setup = tempSetup;

        setForeground(MBGuiConstants.ColorRegularText);
        setBackground(MBGuiConstants.ColorGeneralBackground);
        JobSchedulerModel = new DefaultTableModel() {
            public Class getColumnClass(int  columnIndex) {
                if (columnIndex == 2) {
                    try {
                        return Class.forName("java.lang.Boolean");
                    } catch (ClassNotFoundException cnfe) {
                        return super.getColumnClass(columnIndex);
                    }
                } else {
                    return super.getColumnClass(columnIndex);
                }
            }

            public boolean isCellEditable(int row, int col) {
                return false;
            }
        };
        JobSchedulerTable = new JTable(JobSchedulerModel);
        JScrollPane tableScroller = new JScrollPane(JobSchedulerTable);
        Dimension dm = tableScroller.getPreferredSize();
        dm.height = dm.height/2;
        tableScroller.setPreferredSize(dm);
        getContentPane().add("Center",tableScroller);
        JobSchedulerTable.setFont(new Font("Courier", Font.PLAIN, 14));
        JobSchedulerTable.setBackground(MBGuiConstants.ColorFieldBackground);
        columnHeadings.addElement("Group");
        columnHeadings.addElement("Event");
        columnHeadings.addElement("Active");
//		columnHeadings.addElement("Interval");
//		columnHeadings.addElement("Start");
//		columnHeadings.addElement("Stop");
//		columnHeadings.addElement("CC");
        columnHeadings.addElement("Data Set");
        JobSchedulerModel.setColumnIdentifiers(columnHeadings);
        JobSchedulerTable.setColumnSelectionAllowed(false);
        JobSchedulerTable.setRowSelectionAllowed(true);
        JobSchedulerTable.setCellSelectionEnabled(false);
        JobSchedulerTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        btnHelp = new JButton("Help");
        btnHelp.setForeground(MBGuiConstants.ColorHelpButton);
        btnRefresh = new JButton("Refresh");
        btnRefresh.setForeground(MBGuiConstants.ColorActionButton);
        btnAdd = new JMenuItem("Add");
        btnAdd.setEnabled(true);
        btnRemove = new JMenuItem("Remove");
        btnRemove.setEnabled(false);
        btnReplace = new JMenuItem("Edit");
        btnReplace.setEnabled(false);
        btnReactivate = new JMenuItem("Reactivate");
        btnReactivate.setEnabled(false);
        btnDeactivate = new JMenuItem("Deactivate");
        btnDeactivate.setEnabled(false);
        JMenu jobMenu = new JMenu("Jobs");
        getJMenuBar().add(jobMenu);
        jobMenu.add(btnAdd);
        jobMenu.add(btnRemove);
        jobMenu.add(btnReplace);
        jobMenu.add(btnReactivate);
        jobMenu.add(btnDeactivate);

        Vector actionButtons = new Vector();
        actionButtons.addElement(btnRefresh);
        addButtonPanel(btnHelp, actionButtons);

        // Help button
        btnHelp.setForeground(MBGuiConstants.ColorHelpButton);
        btnHelp.addActionListener(new ActionListener() {
                                      public void actionPerformed(ActionEvent evt) {
                                          //MBUtilities.ShowHelp("JobSchedule");
                                          MBUtilities.ShowHelp("HDRSCHEDUL",HelpTopicID.JOBSCHEDULERPAGE_HELP);
                                      }
                                  } );

        // Enable/disable buttons based on build selection status
        JobSchedulerTable.getSelectionModel().addListSelectionListener(new ListSelectionListener() {
                                                                           public void valueChanged(ListSelectionEvent ie) {
                                                                               boolean stb = JobSchedulerTable.getSelectedRowCount() > 0;
//                btnAdd.setEnabled(stb);
                                                                               int idx = JobSchedulerTable.getSelectedRow();
                                                                               if (idx > -1) {
                                                                                   btnRemove.setEnabled(stb);
                                                                                   btnReplace.setEnabled(stb);
// Ken, 5/20/99     Sometimes this isn't true, and thats screwy, so skip it.
                                                                                   if (idx < JobSchedulerTable.getRowCount()) {
                                                                                       boolean isActive =((Boolean) JobSchedulerTable.getValueAt(idx, JobSchedulerTable.convertColumnIndexToView(2))).booleanValue();
                                                                                       btnReactivate.setEnabled(stb & !isActive);
                                                                                       btnDeactivate.setEnabled(stb & isActive);
                                                                                   }
                                                                               }
                                                                           }
                                                                       });

        // listener for the JobScheduler/restart button
        btnRefresh.addActionListener(new MBCancelableActionListener(thisFrame) {
                                         public void doAction(ActionEvent evt) {
                                             try {
                                                 refreshScheduleInfo();
                                             } catch (MBBuildException mbe) {
                                                 lep.LogException(mbe);
                                             }
                                         }
                                     });

        // listener for the JobScheduler/restart button
        btnReplace.addActionListener(new MBCancelableActionListener(thisFrame) {
                                         public void doAction(ActionEvent evt) {
                                             try {
                                                 // find out if anything is selected
                                                 int idx = JobSchedulerTable.getSelectedRow();
                                                 if (idx > -1) {
                                                     MBScheduleEditJobInfo tempJobWindow = new MBScheduleEditJobInfo((MBSchedulerInfo) schedulerInfoVector.elementAt(idx), setup, MBScheduleRequest.REPLACE, thisFrame,lep);
                                                     MBSchedulerInfo newJob = tempJobWindow.getJob();
                                                     if (newJob != null) {
                                                         refreshScheduleInfo();
                                                     }
                                                 }
                                             } catch (MBBuildException mbe) {
                                                 lep.LogException(mbe);
                                             }
                                         }
                                     });

        // listener for the JobScheduler/restart button
        btnAdd.addActionListener(new MBCancelableActionListener(thisFrame) {
                                     public void doAction(ActionEvent evt) {
                                         try {
                                             // find out if anything is selected
                                             MBScheduleEditJobInfo tempJobWindow = new MBScheduleEditJobInfo(null, setup, MBScheduleRequest.ADD, thisFrame,lep);
                                             MBSchedulerInfo newJob = tempJobWindow.getJob();
                                             if (newJob != null) {
                                                 refreshScheduleInfo();
                                             }
                                         } catch (MBBuildException mbe) {
                                             lep.LogException(mbe);
                                         }
                                     }
                                 });

        // listener for the JobScheduler/create button
        btnRemove.addActionListener(new MBCancelableActionListener(thisFrame) {
                                        public void doAction(ActionEvent evt) {
                                            try {
                                                // find out if anything is selected
                                                int idx = JobSchedulerTable.getSelectedRow();
                                                if (idx > -1) {
                                                    String groupName  = (String) JobSchedulerTable.getValueAt(idx, JobSchedulerTable.convertColumnIndexToView(0));
                                                    String eventName  = (String) JobSchedulerTable.getValueAt(idx, JobSchedulerTable.convertColumnIndexToView(1));
                                                    Hashtable argHash = new Hashtable();
                                                    argHash.put("GROUP", groupName);
                                                    argHash.put("EVENT", eventName);
                                                    MBScheduleRequest newSchedule = new MBScheduleRequest(setup, MBScheduleRequest.REMOVE, getStatus(), argHash,lep);
                                                    newSchedule.doRequest();
                                                    refreshScheduleInfo();
                                                }
                                            } catch (MBBuildException mbe) {
                                                lep.LogException(mbe);
                                            }
                                        }
                                    });

        // listener for the JobScheduler/create button
        btnReactivate.addActionListener(new MBCancelableActionListener(thisFrame) {
                                            public void doAction(ActionEvent evt) {
                                                try {
                                                    // find out if anything is selected
                                                    int idx = JobSchedulerTable.getSelectedRow();
                                                    if (idx > -1) {
                                                        String groupName  = (String) JobSchedulerTable.getValueAt(idx, JobSchedulerTable.convertColumnIndexToView(0));
                                                        String eventName  = (String) JobSchedulerTable.getValueAt(idx, JobSchedulerTable.convertColumnIndexToView(1));
                                                        Hashtable argHash = new Hashtable();
                                                        argHash.put("GROUP", groupName);
                                                        argHash.put("EVENT", eventName);
                                                        MBScheduleRequest newSchedule = new MBScheduleRequest(setup, MBScheduleRequest.REACTIVATE, getStatus(), argHash,lep);
                                                        newSchedule.doRequest();
                                                        refreshScheduleInfo();
                                                    }
                                                } catch (MBBuildException mbe) {
                                                    lep.LogException(mbe);
                                                }
                                            }
                                        });

        // listener for the JobScheduler/create button
        btnDeactivate.addActionListener(new MBCancelableActionListener(thisFrame) {
                                            public void doAction(ActionEvent evt) {
                                                try {
                                                    // find out if anything is selected
                                                    int idx = JobSchedulerTable.getSelectedRow();
                                                    if (idx > -1) {
                                                        String groupName  = (String) JobSchedulerTable.getValueAt(idx, JobSchedulerTable.convertColumnIndexToView(0));
                                                        String eventName  = (String) JobSchedulerTable.getValueAt(idx, JobSchedulerTable.convertColumnIndexToView(1));
                                                        Hashtable argHash = new Hashtable();
                                                        argHash.put("GROUP", groupName);
                                                        argHash.put("EVENT", eventName);
                                                        MBScheduleRequest newSchedule = new MBScheduleRequest(setup, MBScheduleRequest.DEACTIVATE, getStatus(), argHash,lep);
                                                        newSchedule.doRequest();
                                                        refreshScheduleInfo();
                                                    }
                                                } catch (MBBuildException mbe) {
                                                    lep.LogException(mbe);
                                                }
                                            }
                                        });
        setVisible(true);
        JobSchedulerTable.sizeColumnsToFit(-1);
    }


    /** Save the screen position of the JobScheduler dialg */
    private void refreshScheduleInfo() throws com.ibm.sdwb.build390.MBBuildException{
        for (int i = 0; i < JobSchedulerTable.getColumnCount(); i++) {
            TableColumn tempCol = JobSchedulerTable.getColumn(JobSchedulerTable.getColumnName(i));
            String tempKey = columnConstant+Integer.toString(i);
            Dimension tempWidth = new Dimension(tempCol.getWidth(), 0);
            putSizeInfo(tempKey, tempWidth);
        }
        JobSchedulerTable.clearSelection();
        MBScheduleRequest newSchedule = new MBScheduleRequest(setup, MBScheduleRequest.QUERY, getStatus(), new Hashtable(),lep);
        newSchedule.doRequest();
        schedulerInfoVector = newSchedule.getSchedule();
        Vector tableFormattedJobs = new Vector();
        for (int i = 0; i < schedulerInfoVector.size(); i++) {
            MBSchedulerInfo tempInfo = (MBSchedulerInfo) schedulerInfoVector.elementAt(i);
            Vector oneRow = new Vector();
            oneRow.addElement(tempInfo.groupName);
            oneRow.addElement(tempInfo.eventName);
            oneRow.addElement(new Boolean(tempInfo.active));
//		    oneRow.addElement(formatHHMM(tempInfo.interval));
//		    oneRow.addElement(formatHHMM(tempInfo.start));
//		    oneRow.addElement(formatHHMM(tempInfo.stop));
//		    oneRow.addElement(tempInfo.conditionCode);
            oneRow.addElement(tempInfo.dataSetName);
            tableFormattedJobs.addElement(oneRow);
        }
        JobSchedulerModel.setDataVector(tableFormattedJobs, columnHeadings);
        for (int i = 0; i < JobSchedulerTable.getColumnCount(); i++) {
            TableColumn tempCol = JobSchedulerTable.getColumn(JobSchedulerTable.getColumnName(i));
            String tempKey = columnConstant+Integer.toString(i);
            Dimension tempWidth = getSizeInfo(tempKey);
            if (tempWidth != null) {
                tempCol.setWidth(tempWidth.width);
            }
        }
        repaint();
    }


    public void dispose() {
        if (JobSchedulerTable != null) {
            for (int i = 0; i < JobSchedulerTable.getColumnCount(); i++) {
                TableColumn tempCol = JobSchedulerTable.getColumn(JobSchedulerTable.getColumnName(i));
                String tempKey = columnConstant+Integer.toString(i);
                Dimension tempWidth = new Dimension(tempCol.getWidth(), 0);
                putSizeInfo(tempKey, tempWidth);
            }
        }
        super.dispose();
    }


    private String formatHHMM(String stringMinutes) {
        try {
            int minutes = Integer.parseInt(stringMinutes);
            int hours = minutes / 60;
            int mins = minutes % 60;
            String stringHours = Integer.toString(hours);
            String stringMins = Integer.toString(mins);
            if (stringHours.length() <2) {
                stringHours = "0"+stringHours;
            }
            if (stringMins.length() <2) {
                stringMins = "0"+stringMins;
            }
            return stringHours+":"+stringMins;
        } catch (NumberFormatException nfe) {
            return stringMinutes;
        }
    }
}
