package com.ibm.sdwb.build390;
/*********************************************************************/
/* MBScheduleEditJobInfo class for the Build/390 client            */
/*  Creates and manages the General scheduler update page            */
/*********************************************************************/
// Changes
// Date     Defect/Feature      Reason
// 05/17/99 FixMinSize          Set min size
// 09/14/99	FixEventFormat		Only allow 8 char mainframe identifier for event name
// 09/30/99 pjs - Fix help link
// 03/07/2000 reworklog         changes to implement the log stuff using listeners
// 02/02/2001 #ItemNotSel       no item selected in JList
// 02/02/2001 #GreyRemove:      add listener to enable the remove menu item
//12/03/2002 SDWB-2019 Enhance the help system
/*********************************************************************/
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.util.Hashtable;
import java.util.Vector;

import javax.swing.*;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import com.ibm.sdwb.build390.help.HelpTopicID;
import com.ibm.sdwb.build390.logprocess.LogEventProcessor;
import com.ibm.sdwb.build390.user.Setup;

/** Create the driver build page */
public class MBScheduleEditJobInfo extends MBModalStatusFrame
{

    private JButton btHelp = new JButton("Help");
    private JButton btOk = new JButton("Ok");
    private JLabel groupLabel = new JLabel("Group");
    private JLabel eventLabel = new JLabel("Event");
    private JLabel intervalLabel = new JLabel("Interval");
    private JLabel startLabel = new JLabel("Start");
    private JLabel stopLabel = new JLabel("Stop");
    private JLabel rundaysLabel = new JLabel("Days To Run");
    private JCheckBox ccCheckingCB = new JCheckBox("Return code checking ");
    private JCheckBox stepCheckingCB = new JCheckBox("Step RC checking ");
    private JCheckBox[] rundaysCB = new JCheckBox[7];
    private JLabel dataSetLabel = new JLabel("DSN");
    private JLabel contactLabel = new JLabel("Contact List");
    private MainframeIdentifierTextfield tfGroup = new MainframeIdentifierTextfield();
    private MainframeIdentifierTextfield tfEvent = new MainframeIdentifierTextfield();
    private timeField tfInterval = new timeField();
    private timeField tfStart = new timeField();
    private timeField tfStop = new timeField();
    private JList listRundays;
    private NumericLimitedTextfield tfCC = new NumericLimitedTextfield(4);
    private JTextField tfStepName = new JTextField();
    private JTextField tfDSN = new JTextField();
    private JList listContacts;
    private DefaultListModel contactsModel = new DefaultListModel();
    private GridBagLayout gridBag = new GridBagLayout();
    private JPanel centerPanel = new JPanel(gridBag);
    private MBSchedulerInfo jobInfo;
    private MBSchedulerInfo returnJobInfo=null;
    private JMenuItem addContact = new JMenuItem("Add");
    private JMenuItem removeContact = new JMenuItem("Remove");
    private Setup setup = null;
    private String requestType = new String();

    /**
    * constructor - Create a MBScheduleEditJobInfo
    * @param MBGUI gui
    */
    public MBScheduleEditJobInfo(MBSchedulerInfo oldJob, Setup tempSetup, String tempRequestType, JInternalFrame pFrame,LogEventProcessor lep) throws com.ibm.sdwb.build390.MBBuildException{
        super("Schedule Edit", pFrame, lep);
        if (oldJob != null)
        {
            jobInfo = oldJob;
            setTitle("Replace Job");
        }
        else
        {
            jobInfo = new MBSchedulerInfo();
            setTitle("Create Job");
        }
        requestType = tempRequestType;
        setup = tempSetup;
        createUI();
    }

    public void createUI()
    {
        JMenu contactMenu = new JMenu("Contacts");
        getJMenuBar().add(contactMenu);
        contactMenu.add(addContact);
        removeContact.setEnabled(false);//#GreyRemove: set remove default disable
        contactMenu.add(removeContact);

        btHelp.setForeground(MBGuiConstants.ColorHelpButton);
        btHelp.addActionListener(new MBCancelableActionListener(thisFrame)
                                 {
                                     public void doAction(ActionEvent evt)
                                     {
                                         //MBUtilities.ShowHelp("Scheduler_add");
                                         MBUtilities.ShowHelp("SPTSCHADD",HelpTopicID.SCHEDULEEDITJOBINFO_HELP);
                                     }
                                 } );

        btOk.setForeground(MBGuiConstants.ColorActionButton);
        btOk.addActionListener(new MBCancelableActionListener(thisFrame)
                               {
                                   public void doAction(ActionEvent evt)
                                   {
                                       try
                                       {
                                           validateEntry();
                                           saveSettings();
                                           sendRequest();
                                           dispose();
                                       }
                                       catch (MBBuildException mbe)
                                       {
                                           //MBUtilities.LogException(mbe);
                                           lep.LogException(mbe);
                                       }
                                   }
                               } );
        ccCheckingCB.addActionListener(new ActionListener()
                                       {
                                           public void actionPerformed(ActionEvent e)
                                           {
                                               if (ccCheckingCB.isSelected())
                                               {
                                                   stepCheckingCB.setSelected(false);
                                               }
                                           }
                                       });
        stepCheckingCB.addActionListener(new ActionListener()
                                         {
                                             public void actionPerformed(ActionEvent e)
                                             {
                                                 if (stepCheckingCB.isSelected())
                                                 {
                                                     ccCheckingCB.setSelected(false);
                                                 }
                                             }
                                         });
        rundaysCB[0] = new JCheckBox("Monday");
        rundaysCB[1] = new JCheckBox("Tuesday");
        rundaysCB[2] = new JCheckBox("Wednesday");
        rundaysCB[3] = new JCheckBox("Thursday");
        rundaysCB[4] = new JCheckBox("Friday");
        rundaysCB[5] = new JCheckBox("Saturday");
        rundaysCB[6] = new JCheckBox("Sunday");
        listRundays = new JList(rundaysCB);
        listRundays.setCellRenderer(new GeneralListCellRenderer());
        for (int i = 0; i < jobInfo.contactList.size(); i++)
        {
            contactsModel.addElement(jobInfo.contactList.elementAt(i));
        }
        listContacts = new JList(contactsModel);
        tfGroup.setBackground(MBGuiConstants.ColorFieldBackground);
        tfEvent.setBackground(MBGuiConstants.ColorFieldBackground);
        GridBagConstraints c = new GridBagConstraints();
        c.weighty = 0;
        c.gridx = 1;
        c.gridy = 1;
        c.anchor = GridBagConstraints.WEST;
        c.insets = new Insets(2,5,2,5);
        gridBag.setConstraints(groupLabel, c);
        centerPanel.add(groupLabel);
        c.gridy = 2;
        gridBag.setConstraints(eventLabel, c);
        centerPanel.add(eventLabel);
        c.gridy = 3;
        gridBag.setConstraints(intervalLabel, c);
        centerPanel.add(intervalLabel);
        c.gridy = 4;
        gridBag.setConstraints(startLabel, c);
        centerPanel.add(startLabel);
        c.gridy = 5;
        gridBag.setConstraints(stopLabel, c);
        centerPanel.add(stopLabel);
        c.gridy = 6;
        gridBag.setConstraints(rundaysLabel, c);
        centerPanel.add(rundaysLabel);
        c.gridy = 11;
        gridBag.setConstraints(ccCheckingCB, c);
        centerPanel.add(ccCheckingCB);
        c.gridy = 12;
        gridBag.setConstraints(stepCheckingCB, c);
        centerPanel.add(stepCheckingCB);
        c.gridy = 13;
        gridBag.setConstraints(dataSetLabel, c);
        centerPanel.add(dataSetLabel);
        c.gridy = 14;
        gridBag.setConstraints(contactLabel, c);
        centerPanel.add(contactLabel);
        c.gridy = 1;
        c.gridx = 2;
        c.fill = GridBagConstraints.HORIZONTAL;
        c.weightx = 1;
        c.gridwidth = GridBagConstraints.REMAINDER;
        gridBag.setConstraints(tfGroup, c);
        centerPanel.add(tfGroup);
        c.gridy = 2;
        gridBag.setConstraints(tfEvent, c);
        centerPanel.add(tfEvent);
        c.gridy = 3;
        gridBag.setConstraints(tfInterval, c);
        centerPanel.add(tfInterval);
        c.gridy = 4;
        gridBag.setConstraints(tfStart, c);
        centerPanel.add(tfStart);
        c.gridy = 5;
        gridBag.setConstraints(tfStop, c);
        centerPanel.add(tfStop);
        c.gridy = 6;
        c.gridheight = 1;
        c.weighty = 1;
        c.fill = GridBagConstraints.BOTH;
        JScrollPane rundaysSP = new JScrollPane(listRundays);
        gridBag.setConstraints(rundaysSP, c);
        centerPanel.add(rundaysSP);
        c.gridheight = 1;
        c.weighty = 0;
        c.fill = GridBagConstraints.HORIZONTAL;
        c.gridy = 11;
        gridBag.setConstraints(tfCC, c);
        centerPanel.add(tfCC);
        c.gridy = 12;
        gridBag.setConstraints(tfStepName, c);
        centerPanel.add(tfStepName);
        c.gridy = 13;
        gridBag.setConstraints(tfDSN, c);
        centerPanel.add(tfDSN);
        c.gridy = 14;
        c.gridheight = 1;
        c.weighty = 1;
        c.fill = GridBagConstraints.BOTH;
        JScrollPane contactsSP = new JScrollPane(listContacts);
        gridBag.setConstraints(contactsSP, c);
        centerPanel.add(contactsSP);
        getContentPane().add("Center", centerPanel);
        Vector actionButtons = new Vector();
        actionButtons.addElement(btOk);
        addButtonPanel(btHelp, actionButtons);
        tfEvent.setText(jobInfo.eventName);
        tfGroup.setText(jobInfo.groupName);
        if (requestType.equals(MBScheduleRequest.REPLACE))
        {
            tfEvent.setEditable(false);
            tfGroup.setEditable(false);
            tfEvent.setEnabled(false);
            tfGroup.setEnabled(false);
        }
        else if (requestType.equals(MBScheduleRequest.ADD))
        {
            tfEvent.setText("");
            tfGroup.setText("");
        }
        tfInterval.setTime(jobInfo.interval);
        tfStart.setTime(jobInfo.start);
        tfStop.setTime(jobInfo.stop);
        for (int i = 0; i < rundaysCB.length; i++)
        {
            rundaysCB[i].setSelected(jobInfo.daysToRun[i]);
        }
        tfCC.setText(jobInfo.conditionCode);
        ccCheckingCB.setSelected(tfCC.getText().trim().length() > 0);
        tfStepName.setText(jobInfo.stepName);
        tfDSN.setText(jobInfo.dataSetName);
        addContact.addActionListener(new ActionListener()
                                     {
                                         public void actionPerformed(ActionEvent e)
                                         {
                                             new Thread(new Runnable()
                                                        {
                                                            public void run()
                                                            {
                                                                TextEntryDialog newContact = new TextEntryDialog("Enter contact (node.userid):", thisFrame);
                                                                String contactName = newContact.getText();
                                                                contactsModel.addElement(contactName);
                                                                listContacts.setModel(contactsModel);
                                                                repaint();
                                                            }
                                                        }).start();
                                         }
                                     });
        removeContact.addActionListener(new ActionListener()
                                        {
                                            public void actionPerformed(ActionEvent e)
                                            {
                                                int i=listContacts.getSelectedIndex();
                                                if (i==-1)return;// #ItemNotSel  no item selected in JList
                                                contactsModel.removeElementAt(i);
                                                listContacts.setModel(contactsModel);
                                                repaint();
                                            }
                                        });
// #GreyRemove:   add listener to enable the remove menu item
        listContacts.addListSelectionListener(new ListSelectionListener()
                                              {
                                                  public void valueChanged(ListSelectionEvent e)
                                                  {
                                                      removeContact.setEnabled(true);
                                                  }
                                              });

        setVisible(true);

    }

    // FixMinSize
    public Dimension getMinimumSize()
    {
        Dimension oldPref = new Dimension(320, 365);
        return oldPref;
    }

    private void validateEntry() throws SyntaxError{
        String errorString = new String();
        if (tfEvent.getText().trim().length() <1)
        {
            errorString += "a valid event name\n";
        }
/*
        if (tfGroup.getText().trim().length() <1) {
            errorString += "a valid group name\n";
        }
*/
        if (tfInterval.getTime().trim().length() <5)
        {
            errorString += "a valid 4 digit interval time\n";
        }
        if (tfStart.getTime().trim().length() <5)
        {
            errorString += "a valid 4 digit start time\n";
        }
        if (tfStop.getTime().trim().length() <5)
        {
            errorString += "a valid 4 digit stop name\n";
        }
        boolean daySelected = false;
        for (int i = 0; i < rundaysCB.length; i++)
        {
            daySelected = daySelected | rundaysCB[i].isSelected();
        }
        if (!daySelected)
        {
            errorString += "at least one day\n";
        }
        if (ccCheckingCB.isSelected())
        {
            if (tfCC.getText().trim().length() < 1)
            {
                errorString += "a condition code, or unselect return code checking\n";
            }
            if (listContacts.getModel().getSize() < 1)
            {
                errorString += "contacts, or unselect return code checking\n";
            }
        }
        if (stepCheckingCB.isSelected())
        {
            if (tfCC.getText().trim().length() < 1)
            {
                errorString += "a condition code, or unselect step name checking\n";
            }
            if (tfStepName.getText().trim().length() < 1)
            {
                errorString += "a step name, or unselect step name checking\n";
            }
            if (listContacts.getModel().getSize() < 1)
            {
                errorString += "contacts, or unselect step name checking\n";
            }
        }
        if (tfDSN.getText().trim().length() <1)
        {
            errorString += "a valid DSN \n";
        }
        if (errorString.trim().length() > 0)
        {
            errorString = "You must provide \n" + errorString + "before this can be submitted.";
            throw new SyntaxError(errorString);
        }
    }

    private void saveSettings()
    {
        returnJobInfo = new MBSchedulerInfo();
        returnJobInfo.eventName = tfEvent.getText();
        returnJobInfo.groupName = tfGroup.getText();
        returnJobInfo.interval = tfInterval.getTime();
        returnJobInfo.start = tfStart.getTime();
        returnJobInfo.stop = tfStop.getTime();
        for (int i = 0; i < jobInfo.daysToRun.length; i++)
        {
            returnJobInfo.daysToRun[i] = rundaysCB[i].isSelected();
        }
        returnJobInfo.conditionCode = tfCC.getText();
        returnJobInfo.doConditionCheck = ccCheckingCB.isSelected();
        returnJobInfo.stepName = tfStepName.getText();
        returnJobInfo.dataSetName = tfDSN.getText();
        ListModel contactModel = listContacts.getModel();
        returnJobInfo.contactList = new Vector();
        for (int i = 0; i < contactModel.getSize(); i++)
        {
            returnJobInfo.contactList.addElement(contactModel.getElementAt(i));
        }
    }

    private void sendRequest() throws com.ibm.sdwb.build390.MBBuildException{
        Hashtable argHash = new Hashtable();
        if (returnJobInfo.groupName.trim().length() > 0)
        {
            argHash.put("GROUP", returnJobInfo.groupName);
        }
        argHash.put("EVENT", returnJobInfo.eventName);
        argHash.put("INTERVAL", returnJobInfo.interval);
        argHash.put("START", returnJobInfo.start);
        argHash.put("STOP", returnJobInfo.stop);
        if (stepCheckingCB.isSelected() | ccCheckingCB.isSelected())
        {
            argHash.put("CC", returnJobInfo.conditionCode);
        }
        if (stepCheckingCB.isSelected())
        {
            argHash.put("STEP", returnJobInfo.stepName);
        }
        if (returnJobInfo.contactList.size() > 0)
        {
            argHash.put("CONTACTS", returnJobInfo.contactList);
        }
        argHash.put("DSN", returnJobInfo.dataSetName);
        Vector daysToRun = new Vector();
        if (returnJobInfo.daysToRun[0])
        {
            daysToRun.addElement("MONDAY");
        }
        if (returnJobInfo.daysToRun[1])
        {
            daysToRun.addElement("TUESDAY");
        }
        if (returnJobInfo.daysToRun[2])
        {
            daysToRun.addElement("WEDNESDAY");
        }
        if (returnJobInfo.daysToRun[3])
        {
            daysToRun.addElement("THURSDAY");
        }
        if (returnJobInfo.daysToRun[4])
        {
            daysToRun.addElement("FRIDAY");
        }
        if (returnJobInfo.daysToRun[5])
        {
            daysToRun.addElement("SATURDAY");
        }
        if (returnJobInfo.daysToRun[6])
        {
            daysToRun.addElement("SUNDAY");
        }
        argHash.put("RUNDAYS", daysToRun);
        MBScheduleRequest newSchedule = new MBScheduleRequest(setup, requestType, getStatus(), argHash,lep);
        newSchedule.doRequest();
    }

    public MBSchedulerInfo getJob()
    {
        return returnJobInfo;
    }
}

class timeField extends Box
{
    NumericLimitedTextfield hoursTF = new NumericLimitedTextfield("00",2);
    NumericLimitedTextfield minutesTF = new NumericLimitedTextfield("00",2);

    timeField()
    {
        super(BoxLayout.X_AXIS);
        add(Box.createHorizontalGlue());
        add(hoursTF);
        add(new JLabel(":"));
        add(minutesTF);
        add(Box.createHorizontalGlue());
    }

    timeField(String stringMinutes)
    {
        super(BoxLayout.X_AXIS);
        add(Box.createHorizontalGlue());
        add(hoursTF);
        add(new JLabel(":"));
        add(minutesTF);
        add(Box.createHorizontalGlue());
        setTime(stringMinutes);
    }

    public void setTime(String stringMinutes)
    {
        if (stringMinutes != null)
        {
            if (stringMinutes.trim().length() > 0)
            {
                int minutes = Integer.parseInt(stringMinutes);
                int hours = minutes / 60;
                int mins = minutes % 60;
                String stringHours = Integer.toString(hours);
                String stringMins = Integer.toString(mins);
                if (stringHours.length() <2)
                {
                    stringHours = "0"+stringHours;
                }
                if (stringMins.length() <2)
                {
                    stringMins = "0"+stringMins;
                }
                hoursTF.setText(stringHours);
                minutesTF.setText(stringMins);
            }
        }
    }

    public String getTime()
    {
        return hoursTF.getText()+":"+minutesTF.getText();
    }
}

