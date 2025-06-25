package com.ibm.sdwb.build390;
/***************************************************************************/
/* Java MBFailedJobsListBox class for the Build/390 client                           */
/*  Builds a listbox, populates it and adds the action listeners specified */
/***************************************************************************/
// Changes
// Date     Defect/Feature      Reason
// 07/16/98 Feature xxx	         Support FastTrack
// 04/27/99 errorHandling        change LogException parms & add new error types
// 01/07/2000 ind.build.log      individual build log file changes
// 03/07/2000 reworklog          changes to write the log stuff using listeners
// 04/10/2000 tableinsteadoflist changed to jtable instead of JList.
// 04/10/2000 thread             made the mbfailed job box run in a separate thread and now dynamic updation happens
// 04/21/2000 thread/table       changes to make the JTable to JList / since the colum sizing problems were there
// 06/01/2000 monospacedfont     made changes to set the font in JList as monospaced / fix a bug that displayed the first abended job twice / align the text and the heading labels at the top/ disable the menus when refreshin happens
// 06/05/2000 lockfailbox        made changes to lock the failedjobs list box /after the jobs are complete./added a method to update the status
// 06/06/2000 status msg retain  made changes to retain the status message when joboutput is requested for a specific job
// 06/14/2000 jobinfo            made changes to display jobout for multiple selections
// 06/19/2000 Defect 46 :  deleted(commented) the empty while in DRIVERBUILD/USERBUILD ETC..and made the threadobj.join() to wait on the current threadobj.
// 11/15/2000 List Box to Table  made the ListBox to Table
// 11/30/2000 commented getJMenuBar().getJMenu(1).set.. causes a nullpointer
// 12/18/2000 sdwb1210			add support for failed listings to a file system
// 03/22/2000 defect 301		remotefile += LSTCOPY + partname; (for hfs the display comes out as nullLISTINGS/partname
// 05/15/2000 defect 399        for systerm ,dont expect a job to be selected
/*********************************************************************/
import java.awt.*;
import java.awt.List;
import java.awt.event.*;
import java.util.*;
import java.io.*;
import javax.swing.*;
import javax.swing.border.*;
import javax.swing.table.*;
import javax.swing.JTable;
import javax.swing.table.DefaultTableModel;
import com.ibm.sdwb.build390.logprocess.*;
import com.ibm.sdwb.build390.mainframe.*;


/** <br>The MBFailedJobsListBox displays a list of choices in a listbox and adds the correct action listener.
* Classes using this class must implement a listener for the OK button and another for the Quit button */
public class MBFailedJobsListBox extends MBModalStatusFrame implements Runnable {

    private MBBuild build;
    private JMenuItem   jclBut    = new JMenuItem("Job Output");    // quit button
    private JMenuItem   systermBut    = new JMenuItem("Systerm");    // quit button
    private JPanel centerPanel = new JPanel(new BorderLayout());
    private MBButtonPanel buttonPanel;
    private final int MARGIN = 10;
    private Vector dataVector = null;
    private int rowsdisplayed=0;
    private JInternalFrame pFrame;
    private boolean isDisposed=false;
    private String LSTCOPY;             // sdwb1210 - add support for failed listings to a file system
    private String workpath;            // sdwb1210 - add support for failed listings to a file system
    private DefaultTableModel md;
    private JTable jobsDisplaytable;
    private int jobsSubmitted;
    private int hostPhaseForPanel = -1;
    private String mvsSystermLocationPrefix = "";


    /** Constructor - Builds the frame and listbox and populates the listbox.
    * @param data Vector containing the tokenized data to be placed into the listbox. The @ character
    * is used as the token delimeter.
    */
    public MBFailedJobsListBox(MBBuild tempBuild, int tempHostPhase, Vector dataVector, JInternalFrame pFrame,final LogEventProcessor lep) {
        super("Failed Jobs for phase "+tempHostPhase, pFrame, lep);
        setResizable(true);
        this.dataVector=dataVector;
        this.pFrame=pFrame;
        build = tempBuild;
        mvsSystermLocationPrefix = build.getDriverInformation().getRelease().getMvsHighLevelQualifier()+"."+build.getDriverInformation().getRelease().getMvsName()+"."+build.getDriverInformation().getName();
        hostPhaseForPanel = tempHostPhase;
        initialize();
    }

    private void initialize() {
        md = new DefaultTableModel() {
            public boolean isCellEditable(int row,int col) {
                return(false);
            }
        };

        jobsDisplaytable = new JTable(md);
        jobsDisplaytable.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);
        jobsDisplaytable.setBackground(MBGuiConstants.ColorFieldBackground);
        jobsDisplaytable.setSelectionMode(ListSelectionModel.SINGLE_INTERVAL_SELECTION);            
        JPanel labbox = new JPanel();

        JScrollPane scrollpane  = new JScrollPane(jobsDisplaytable);
        centerPanel.add("Center", scrollpane);

        setForeground(MBGuiConstants.ColorRegularText);
        setBackground(MBGuiConstants.ColorGeneralBackground);

        // if a fast track userbuild, disable the systerm button and change the title
        if (build.getClass().getName().equals("com.ibm.sdwb.build390.MBUBuild")) {
            if (((MBUBuild) build).getFastTrack()) {
                systermBut.setEnabled(false);
                setTitle("Job List");
            }
        }
        JMenu jobInfoMenu = new JMenu("Job Info");
        getJMenuBar().add(jobInfoMenu);
        jobInfoMenu.add(jclBut);
        jobInfoMenu.add(systermBut);
        Vector actionButtons = new Vector();
        getContentPane().add("Center", centerPanel);
        addButtonPanel(null, actionButtons);
        // show it
        jclBut.addActionListener(new MBCancelableActionListener(thisFrame) {
                                     public void doAction(ActionEvent evt) {
                                         try {
                                             //$FAILEDJOBTABLE
                                             int[] selected = jobsDisplaytable.getSelectedRows();
                                             if (selected.length>0) {
                                                 // sdwb1210 - add support for failed listings to a file system
                                                 for (int i=0;i<selected.length;i++) {
                                                     String   jobString =(String) jobsDisplaytable.getValueAt(selected[i], jobsDisplaytable.convertColumnIndexToModel(0));
                                                     String   partname =(String) jobsDisplaytable.getValueAt(selected[i], jobsDisplaytable.convertColumnIndexToModel(2));
                                                     //System.out.println("Jobstring ="+i+"="+jobString);
                                                     if (jobString != null) {
                                                         jobString = (new StringTokenizer(jobString.trim())).nextToken();
                                                         String oldstattext = getStatus().getStatus();
                                                         String localName = null;
                                                         String fileClass = null;
                                                         String fileMod = null;
                                                         // if lstcopy is set, ftp file from MVS
                                                         //System.out.println("LSTCOPY="+LSTCOPY);
                                                         String lstgen = build.getOptions().getListGen();
                                                         if (lstgen == null) lstgen = "NO";
                                                         if (LSTCOPY != null & (lstgen.equals("FAIL") | lstgen.equals("ALL"))) {
                                                             // find current element in data vector and extract class and mod of the failing part
                                                             for (Iterator jobIterator = dataVector.iterator(); jobIterator.hasNext();) {
                                                                 MBJobInfo oneJob = (MBJobInfo) jobIterator.next();
                                                                 if (oneJob.getJobName().equals(jobString)) {
                                                                     fileClass = oneJob.getfileClass();
                                                                     fileMod   = oneJob.getfileMod();
                                                                 }
                                                             }
                                                             String remotefile = "";
                                                             // seq ds - LSTCOPY is set in the BLDORDER and returned to the client in phase results
                                                             if (LSTCOPY.endsWith("+")) {
                                                                 remotefile = LSTCOPY.substring(0,LSTCOPY.length()-1) + "." + fileClass + "." +fileMod; 
                                                                 // hfs path
                                                             } else if (LSTCOPY.endsWith("/")) {
                                                                 if (!LSTCOPY.startsWith("/")) remotefile = workpath;
                                                                 remotefile += LSTCOPY + partname;
                                                                 // pdse ds
                                                             } else {
                                                                 remotefile = LSTCOPY + "(" + fileMod + ")";
                                                             }
                                                             MBFtp lf = new MBFtp(build.getSetup().getMainframeInfo(),lep);
                                                             localName = new String(build.getBuildPath()+fileMod+"."+fileClass);
                                                             if (!lf.get(remotefile, localName, true)) {
                                                                 throw new FtpError("Could not download "+remotefile+" to "+localName);
                                                             }
                                                             MBEdit edit = new MBEdit(localName,lep);
                                                         } else {
                                                             Set jobsToGet = new HashSet();
                                                             jobsToGet.add(jobString);
                                                             com.ibm.sdwb.build390.process.ProcessWrapperForSingleStep wrapper = new com.ibm.sdwb.build390.process.ProcessWrapperForSingleStep(thisFrame);
                                                             com.ibm.sdwb.build390.process.steps.HeldJobOutputRetrieval jobOutputRetrieval = new com.ibm.sdwb.build390.process.steps.HeldJobOutputRetrieval(jobsToGet, build.getSetup().getMainframeInfo(), build.getBuildPath(), wrapper);
                                                             wrapper.setStep(jobOutputRetrieval);
                                                             jobOutputRetrieval.setShowFilesAfterRun(true, false);
                                                             wrapper.externalRun();
                                                         }
                                                         getStatus().updateStatus(oldstattext,false);
                                                     }
                                                 }
                                             } else {
                                                 problemBox("Selection Error", "You must select a job first");
                                             }
                                         } catch (MBBuildException mbe) {
                                             lep.LogException(mbe);
                                             return;
                                         }
                                     }
                                 });
        systermBut.addActionListener(new MBCancelableActionListener(thisFrame) {
                                         public void doAction(ActionEvent evt) {
                                             try {
                                                 com.ibm.sdwb.build390.process.ProcessWrapperForSingleStep wrapper = new com.ibm.sdwb.build390.process.ProcessWrapperForSingleStep(thisFrame);
                                                 com.ibm.sdwb.build390.process.steps.SystermRetrieval retrieveSysterm = new com.ibm.sdwb.build390.process.steps.SystermRetrieval(build.get_buildid(), Integer.toString(hostPhaseForPanel),build.getMainframeInfo(),build.getBuildPath(), wrapper);
                                                 retrieveSysterm.setMVSSystermLocationPrefix(mvsSystermLocationPrefix);
                                                 retrieveSysterm.setDisplayFileAfterFetch(true);
                                                 wrapper.setStep(retrieveSysterm);
                                                 wrapper.externalRun();
                                             } catch (MBBuildException mbe) {
                                                 lep.LogException(mbe);
                                                 return;
                                             }
                                         }
                                     });

    }


    public  void run() {
        updateJobs(dataVector);
        setVisible(true);
    }

    public void setLSTCOPYAndWorkPathSetting(String lstcopy,String Workpath) {
        if (lstcopy!=null) {
            LSTCOPY=lstcopy.trim();
        }
        if (Workpath!=null) {
            workpath = Workpath;
        }
    }





    public void setTotalJobs(int jobsSubmitted) {
        this.jobsSubmitted = jobsSubmitted;
    }

    public synchronized void updateJobs(final Vector dataVector) {

        this.dataVector=dataVector;//PTM4061

        //  try {     /*PTM3358 */
        SwingUtilities.invokeLater(new Runnable() { /* PTM3358 */

                                       public void run() {
                                           MBCancelableActionListener abc = new MBCancelableActionListener(thisFrame) {
                                               /* the method to override for whatever action you want to perform in response
                                               to a click.
                                               */
                                               public void doAction(ActionEvent e) {
                                                   getStatus().updateStatus("Refreshing !!!!... Please Wait....",false);
                                                   getJMenuBar().getMenu(0).setEnabled(false);
                                                   Vector colHeading = new Vector();
                                                   colHeading.addElement("Job No");
                                                   colHeading.addElement("Job Return Code Information");
                                                   colHeading.addElement("Job File Name");
                                                   colHeading.addElement("Job File Version");
                                                   Vector tempdataVector = new Vector();
                                                   for (Iterator jobIterator = dataVector.iterator(); jobIterator.hasNext();) {
                                                       MBJobInfo oneJob = (MBJobInfo) jobIterator.next();
                                                       if (oneJob.isComplete() & !oneJob.isSuccessful()) {
                                                           Vector oneRow = new Vector();
                                                           oneRow.add(oneJob.getJobName());
                                                           oneRow.add(oneJob.getJobStatus());
                                                           oneRow.add(oneJob.getfileName());
                                                           oneRow.add(oneJob.fileVersion);
                                                           tempdataVector.add(oneRow);
                                                       }
                                                   }

                                                   md.setDataVector(tempdataVector,colHeading);
                                                   getStatus().updateStatus("Total jobs failed = " + tempdataVector.size() + " of " + jobsSubmitted ,false);
                                                   getJMenuBar().getMenu(0).setEnabled(true);
                                                   initColumnSizes();
                                                   repaint();
                                               }
                                           };

                                           abc.actionPerformed(new ActionEvent(this,ActionEvent.ACTION_PERFORMED,""));

                                       }
                                   });

        // } catch (InterruptedException ie) { /* PTM3358 */
        //  } catch (java.lang.reflect.InvocationTargetException ivte) {
        //  }


    }

    public Dimension getPreferredSize() {
        Dimension oldPref  = new Dimension(575,300) ;
        return(oldPref);
    }

    public boolean isDisposed() {
        return(isDisposed);
    }

    public void dispose() {
        isDisposed=true;
        super.dispose();

    }


    private void initColumnSizes() {
        TableColumn column = null;
        Component comp = null;
        int headerWidth = 0;
        int cellWidth = 0;

        for (int i = 0; i < 4; i++) {
            column = jobsDisplaytable.getColumn(jobsDisplaytable.getColumnName(i));

            switch (i) {
            case 0:
                headerWidth=145;
                break;
            case 1:
                headerWidth=215;
                break;
            case 2:
                headerWidth=110;
                break;
            case 3:
                headerWidth=110;
                break;
            default:
                headerWidth=75;
                break;
            }
            column.setWidth(headerWidth);
        }
    } 


}



