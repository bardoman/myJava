
package com.ibm.sdwb.build390;
/*******************************************************************************/
/* This class performs Find Next function to enable the user to see the progress of the archive process
/*******************************************************************************/
// changes
//Date    Defect/Feature        Reason
//05/10/2000 Archiverprocess Birth of The Class
//Dec12/00   jdk1.3          changes
/*******************************************************************************/
import java.awt.*;
import java.awt.List;
import java.awt.event.*;
import java.io.*;
import java.util.*;
import javax.swing.*;
import javax.swing.event.*;
import javax.swing.border.*;
import com.ibm.sdwb.build390.logprocess.*;
import java.text.DateFormat;
import javax.swing.table.*;


class MBLogFileArchiverPage extends MBModalFrame {
    private final static int ONE_SECOND = 1000;
    private ArchiveTask task=null;
    private javax.swing.Timer timer=null;
    private JProgressBar progressBar;
    private JPanel centerPanel;
    private JLabel label  = new JLabel();
    private JButton btNo = new JButton("Cancel Copy");
    private MBButtonPanel btnPanel = null;


    MBLogFileArchiverPage(LogEventProcessor lep) {
        super("Archive Status ...Copying",null, lep);
        initialize();
    }
    private void initialize()  {
        centerPanel = new JPanel(new BorderLayout());
        setForeground(MBGuiConstants.ColorRegularText);
        setBackground(MBGuiConstants.ColorGeneralBackground);
        btNo.setForeground(MBGuiConstants.ColorActionButton);

        /*Border blackline = BorderFactory.createLineBorder(MBGuiConstants.ColorRegularText);
        TitledBorder tld = new TitledBorder(blackline,"Copy Status");
        tld.setTitleColor(MBGuiConstants.ColorGroupHeading);
        */
        centerPanel.setBorder(BorderFactory.createTitledBorder(LineBorder.createGrayLineBorder() ,"Copy Status ...",TitledBorder.DEFAULT_JUSTIFICATION,TitledBorder.DEFAULT_POSITION,null,MBGuiConstants.ColorGroupHeading));


        label = new JLabel("copying...");
        task = new ArchiveTask(lep);
        timer = new javax.swing.Timer(ONE_SECOND, new TimerListener());
        progressBar = new JProgressBar(0,task.getLengthOfTask());
        progressBar.setValue(0);
        progressBar.setStringPainted(true);
        task.start();
        timer.start();

        Vector  actionButtons  = new Vector();
        actionButtons.addElement(btNo);
        btNo.addActionListener(new ActionListener(){
                                   public void actionPerformed(java.awt.event.ActionEvent A) {
                                       task.Forcedstop();
                                       task.stopRunning();
                                       timer.stop();
                                       dispose();

                                   }
                               });
        btnPanel = new MBButtonPanel(null, null, actionButtons);

        centerPanel.add(label,BorderLayout.NORTH);
        centerPanel.add(progressBar,BorderLayout.SOUTH);

        getContentPane().add("North",centerPanel);
        getContentPane().add("South",btnPanel);
        setVisible(true);


    }

    private class TimerListener implements ActionListener {
        public void actionPerformed(ActionEvent evt) {
            progressBar.setValue(task.getCurrent());
            label.setText(task.getMessage());
            if (task.done()) {
                btNo.setEnabled(false);
                task.Forcedstop();
                Toolkit.getDefaultToolkit().beep();
                timer.stop();
                progressBar.setValue(task.getCurrent());
                label.setText("Task completion Successful....");
                if (!task.running) {
                    new Thread(new Runnable(){
                                   public void run() {
                                       new MBMsgBox("Archived","The old build390.log have been Archived into "+task.getArchPath()+ ", and the Existing build390.log has been deleted  from " +(new File(MBGlobals.Build390_path+MBConstants.LOGFILEPATH)).getPath());
                                       dispose();
                                   }
                               }).start();
                }

            }
        }
    }


}


class ArchiveTask extends Thread {

    LogEventProcessor lep=null;
    private int lengthOfTask;
    private int current = 0;
    private String statMessage=new String();
    private String ArchFilePath=new String();

    ArchiveTask(LogEventProcessor lep){
        this.lep=lep;
        lengthOfTask = (int)((new File(MBGlobals.Build390_path+MBConstants.LOGFILEPATH)).length());
    }

    boolean running = true;

    public void run()  {

        while (running) {

            File ArchFile = null;
            BufferedWriter LogArchive = null;
            BufferedReader LogReader  = null;
            try {
                // check size of log file
                File lfth = new File(MBGlobals.Build390_path+MBConstants.LOGFILEPATH);

                //building of a archive file as build390arc-dd-mmm-yy-hh-mm-ss.log
                GregorianCalendar  currCal = new GregorianCalendar ();
                String tempString = new String((DateFormat.getDateInstance().format(new Date())).toString());
                tempString +="-" + Integer.toString(currCal.get(Calendar.HOUR));
                tempString +="-"+ Integer.toString(currCal.get(Calendar.MINUTE));
                tempString +="-"+ Integer.toString(currCal.get(Calendar.SECOND));
                ArchFile = new File(MBGlobals.Build390_path+MBConstants.ARCHIVEFILEPATH+MBConstants.ARCHIVELOG+tempString+MBConstants.LOGFILEEXTENTION);
                ArchFilePath = ArchFile.getPath();
                File ArchTemp = new File(MBGlobals.Build390_path+MBConstants.ARCHIVEFILEPATH);

                //create directory if directory doesnot exist.
                ArchTemp.mkdir();

                LogArchive = new BufferedWriter(new FileWriter(ArchFile));
                LogReader  = new BufferedReader(new FileReader(lfth));
                String currentLine = null;
                while ((currentLine = LogReader.readLine()) != null) {
                    currentLine = currentLine.trim();
                    LogArchive.write(currentLine+MBConstants.NEWLINE);
                    current+=(currentLine+MBConstants.NEWLINE).length();
                    if (current > lengthOfTask) {
                        current = lengthOfTask;
                    }
                    statMessage = "Completed " + current +
                                  " out of " + lengthOfTask;
                }
                //close the opened files or else lfth.delete() wont be successful
                LogReader.close();
                LogArchive.close();


                if (lfth.delete()) {
                    lep.LogPrimaryInfo("INFORMATION","Deleted " + lfth.getPath()  + "Build390 log file and Archived it into " +ArchFile.getPath(),false);
                }



                stopRunning();
            } catch (SecurityException se) {
                System.out.println("Security denied to create LOGARCHIVE - Build390arc..log");
            } catch (IOException e) {
                try {
                    LogReader.close();
                    //LogArchive.close();
                    ArchFile.delete();
                    new MBMsgBox("Archive Failed","Recieved the following I/O error while trying to archive your log file. "+e.toString());
                } catch (IOException ioe) {
                }
            }
            stopRunning();
        }
    }

    void stopRunning(){
        running = false;
    }


    boolean done() {
        if (current >= lengthOfTask) {
            return true;
        } else {
            if (!running) {
                current=lengthOfTask;
                return true;
            } else {
                return false;
            }
        }
    }

    String getMessage() {
        return statMessage;
    }

    int getLengthOfTask() {
        return lengthOfTask;
    }

    int getCurrent() {
        return current;
    }

    void Forcedstop() {
        current = lengthOfTask;
    }
    String getArchPath(){
        return(ArchFilePath);
    }

}



