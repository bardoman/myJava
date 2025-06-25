package com.ibm.sdwb.build390.userinterface.graphic.panels;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.ListIterator;
import java.util.Map;
import java.util.Set;

import javax.swing.*;

import com.ibm.sdwb.build390.*;
import com.ibm.sdwb.build390.help.*;
import com.ibm.sdwb.build390.logprocess.LogEventProcessor;
import com.ibm.sdwb.build390.mainframe.PhaseInformation;
import com.ibm.sdwb.build390.process.AbstractProcess;
import com.ibm.sdwb.build390.process.DriverBuildProcess;
import com.ibm.sdwb.build390.process.ProcessWrapperForSingleStep;
import com.ibm.sdwb.build390.process.steps.BuildPhaseOnMVS;
import com.ibm.sdwb.build390.process.steps.FullProcess;
import com.ibm.sdwb.build390.process.steps.ProcessStep;
import com.ibm.sdwb.build390.process.steps.PurgeJobOutput;
import com.ibm.sdwb.build390.userinterface.graphic.widgets.CancelableAction;
import com.ibm.sdwb.build390.userinterface.graphic.widgets.HelpAction;

/*03/22/2004   TST1787     BPXn processing allows restart in post failure phase */
/*06/16/2004   INT1897     BPXn processing allows restart in post failure phase */
/*07/15/2004   TST1911     Restart of build on final phase results in nullpointer*/
//06/08/2004   PTM3611     Forced to restart on a successful phase


public class ProcessStepListPanel extends MBModalStatusFrame {
    private AbstractProcess theProcess = null;
    private int stepToStartWith = -1;
    private int iterationToStartWith = -1;
    private boolean checkResultsOfPreviousPhase = false;
    private Map stepNameToNumberMap = null;
    private boolean enablePreviousResults=false;
    private static final String MVSPHASE = "MVS Phase ";

    public ProcessStepListPanel(AbstractProcess tempProcess, MBInternalFrame pFrame,LogEventProcessor tempLep) throws com.ibm.sdwb.build390.MBBuildException {
        super("Process phase list", pFrame, tempLep);
        theProcess = tempProcess;
        stepNameToNumberMap = new java.util.HashMap();

/*
        btnRestart.addActionListener(new MBCancelableActionListener(thisFrame) {
                                         public void doAction(ActionEvent evt) {
                                             String selectedStep = (String) lstSteps.getSelectedValue();
                                             if (selectedStep != null) {
                                                 boolean isDisposeIt=true;
                                                 if (selectedStep.startsWith(HOSTPHASE)) {
                                                     String pnum = new String(selectedStep.substring(HOSTPHASE.length()));
                                                     pnum = pnum.trim();
                                                     pnum = pnum.substring(0, pnum.indexOf(" "));
                                                     if (pnum.equals("0")) {
                                                         pnum = "1";
                                                     }
                                                     if (cbCheckJobs.isSelected()) {
                                                         String MVSStepResultFile = new String(buildobj.get_hlq()+"."
                                                                                                +buildobj.get_MVSRelease()+"."+buildobj.get_driver()+"."+buildid_+".P"+pnum);
                                                         String phaseResultFile = new String(buildobj.getBuildPath()+"phase"+pnum+".result");
                                                         boolean ErrorFound = false;
                                                         boolean WarningFound = false;
                                                         int totalSteps=0;
                                                         File tprf = new File(phaseResultFile);
                                                         tprf.delete();
                                                         try {
                                                             mftp = new MBFtp(buildobj.getSetup(),lep);
                                                             lep.LogSecondaryInfo("Debug", "ProcessStepListPanel:Downloading "+phaseResultFile+" from "+MVSStepResultFile);
                                                             if (mftp.get(MVSStepResultFile, phaseResultFile, true)) {
                                                                 phaseParse = new MBStepResultFileParser(new File(phaseResultFile),lep);
                                                                 String numberOfSteps = null;
                                                                 String phaseLine = phaseParse.getFileSetting("PHASE:");
                                                                 StringTokenizer tokenStep = new StringTokenizer(phaseLine);
                                                                 while (tokenStep.hasMoreTokens()) {
                                                                     numberOfSteps = tokenStep.nextToken();
                                                                 }
                                                                 if (numberOfSteps != null) {
                                                                     totalSteps = Integer.parseInt(numberOfSteps);
                                                                 }
                                                                 if (phaseParse.getErrorInfo()) {
                                                                     ErrorFound = true;
                                                                 }
                                                                 if (buildobj.get_buildcc() == 4) {
                                                                     if (phaseParse.getWarningInfo()) {
                                                                         WarningFound = true;
                                                                     }
                                                                 }
                                                             }
                                                         } catch (noPasswordException npwe) {
                                                         } catch (MBBuildException be) {
                                                             lep.LogException(be);
                                                         }
                                                         File prf = new File(phaseResultFile);

                                                         try {

                                                             if (!prf.exists()) {
                                                                 restartStep=null;
                                                                 throw new GeneralError("No phase result file found, Restart terminated", new Exception());
                                                             } else if (ErrorFound) {
                                                                 restartStep=null;
                                                                 throw new GeneralError("Errors were found in the previous execution of the selected phase, Restart terminated", new Exception());
                                                             } else if (WarningFound) {
                                                                 restartStep=null;
                                                                 throw new GeneralError("Warnings were found in the previous execution of the selected phase, Restart terminated", new Exception());
                                                             } else {
                                                                 Vector failedJobs = new Vector();
                                                                 Hashtable tempHash = new Hashtable();
                                                                 tempHash.put("BUILD", buildobj);
                                                                 tempHash.put("PHASE" ,pnum);
                                                                 tempHash.put("QUERYTYPE", "FAILEDJOBS");
                                                                 tempHash.put("STATUS", getStatus());
                                                                 tempHash.put("LOGPROCESSOR",lep);


                                                                 MBC_JOBOUTPUT getJobOutput = new MBC_JOBOUTPUT();
                                                                 failedJobs = (Vector) getJobOutput.ServerCmd(tempHash);

                                                                 if (failedJobs.size() > 0) {
                                                                     MBMsgBox question = new MBMsgBox("Host Job Errors", "There were errors.  Should host job results be deleted?", parentFrame, true);
                                                                     if (question.isAnswerYes()) {

                                                                         Hashtable jobsSubmitted = phaseParse.getJobInfo();
                                                                         String jobString = new String();
                                                                         if (jobsSubmitted!=null) {
                                                                             MBJobInfo tempJob;
                                                                             Enumeration jobs = jobsSubmitted.keys();
                                                                             while (!stopped & jobs.hasMoreElements()) {
                                                                                 tempJob = (MBJobInfo) jobsSubmitted.get(jobs.nextElement());
                                                                                 jobString += tempJob.jobName +" ";
                                                                             }
                                                                         }

                                                                         clrout_ = buildobj.getBuildPath() + JOBSPURGE;
                                                                         mySock = new MBSocket(jobString, clrout_,  "Step " + buildobj.get_hostBuildStep() + ": Purging job output", status, buildobj.getSetup(), lep);
                                                                         mySock.setDelsysout();
                                                                         mySock.unset_clrout();
                                                                         mySock.run();
                                                                     }
                                                                     restartStep=null;
                                                                     throw new GeneralError("At least 1 job submitted during the previous execution of the selected phase failed, Restart terminated", new Exception());
                                                                 } else {
                                                                     int nextNum = Integer.parseInt(pnum)+1;
                                                                     if (buildobj.get_buildtype().toUpperCase().equals("NONE")) {
                                                                         MBClient.SetArgValue("RESTART_PHASE", (String) buildSteps.lastElement(), true);
                                                                         restartStep = (String)buildSteps.lastElement();
                                                                         dr = new MBDrReport(buildobj, getStatus(),lep);
                                                                         dr.initializeReport(true);
                                                                         Vector buildTypes = dr.getBuildTypes();
                                                                         if (buildTypes.size() == 1) {
                                                                             buildobj.set_buildtype(((String) buildTypes.firstElement()).trim());
                                                                         } else {
                                                                             MBListBox lb = new MBListBox(MBConstants.productName+" - Buildtype List",buildTypes, false, thisFrame,lep);
                                                                             String buildtypeselected =lb.getElementSelected();
                                                                             if (buildtypeselected == null) {
                                                                                 isDisposeIt=false;
                                                                                 throw new GeneralError("Build was cancelled on user request");

                                                                             }
                                                                             buildobj.set_buildtype((lb.getElementSelected()).trim());
                                                                         }
                                                                         if (parentFrame != null) {
                                                                             ((MBBuildTypeWindow)parentFrame).settfBuildtype(buildobj.get_buildtype());
                                                                         }
                                                                     } else if (nextNum > totalSteps) {
                                                                         MBClient.SetArgValue("RESTART_PHASE", Integer.toString(7), true);
                                                                         restartStep = Integer.toString(7);
                                                                         buildobj.set_hostBuildStep(nextNum);
                                                                     } else {
                                                                         MBClient.SetArgValue("RESTART_PHASE", (String) buildSteps.lastElement(), true);
                                                                         restartStep = (String)buildSteps.lastElement();
                                                                         buildobj.set_hostBuildStep(nextNum);
                                                                     }
                                                                 }
                                                             }
                                                         } catch (MBBuildException mbbe) {
                                                             lep.LogException(mbbe);
                                                             stop();
                                                         }
                                                     } else {
                                                         MBClient.SetArgValue("RESTART_PHASE", (String) buildSteps.lastElement(), true);
                                                         restartStep = (String) buildSteps.lastElement();
                                                         buildobj.set_hostBuildStep(Integer.parseInt(pnum));
                                                         if (buildobj.get_buildtype().toUpperCase().equals("NONE")) {
                                                             try {
                                                                 dr = new MBDrReport(buildobj, getStatus(),lep);
                                                                 dr.initializeReport(true);
                                                                 Vector buildTypes = dr.getBuildTypes();
                                                                 if (buildTypes.size() == 1) {
                                                                     String tempType = ((String) buildTypes.firstElement()).trim();
                                                                     if (!tempType.equals("NONE")) {
                                                                         buildobj.set_buildtype(tempType);
                                                                     }
                                                                 } else {
                                                                     MBListBox lb = new MBListBox(MBConstants.productName+" - Buildtype List",buildTypes, false, thisFrame,lep);
                                                                     String buildtypeselected =lb.getElementSelected();
                                                                     if (buildtypeselected == null) {
                                                                         isDisposeIt=false;
                                                                         throw new GeneralError("Build was cancelled on user request");
                                                                     }
                                                                     buildobj.set_buildtype((lb.getElementSelected()).trim());
                                                                 }
                                                                 if (parentFrame != null) {
                                                                     ((MBBuildTypeWindow)parentFrame).settfBuildtype(buildobj.get_buildtype());
                                                                 }
                                                             } catch (MBBuildException mbe) {
                                                                 lep.LogException(mbe);
                                                             }
                                                         }
                                                     }
                                                 } else {
                                                     MBClient.SetArgValue("RESTART_PHASE", selectedStep, true);
                                                     restartStep = selectedStep;
                                                 }
                                                 if (selectedStep.equals(LOADPARTSPHASE)) {
                                                     MBMsgBox question = new MBMsgBox("Upload Restart Options", "To continue uploading all files that have not successfully been uploaded click YES\nTo reupload ALL files, click NO", thisFrame, true);
                                                     if (!question.isAnswerYes()) {
                                                         (new File(buildobj.get_extractpath()+File.separator+MBUpdateFiles.UPLOADLOG)).delete();
                                                         MBUpdateFiles.removeBuild(buildobj);
                                                     }
                                                 } else {
                                                     (new File(buildobj.get_extractpath()+File.separator+MBUpdateFiles.UPLOADLOG)).delete();
                                                     MBUpdateFiles.removeBuild(buildobj);
                                                 }
                                                 if (isDisposeIt & !wasCancelPressed()) {
                                                     dispose();
                                                 }
                                             }
                                         }

                                         public void stop() {
                                             if (dr!=null) {
                                                 try {
                                                     dr.stop();
                                                 } catch (MBBuildException mbe) {
                                                     lep.LogException( mbe);
                                                 }
                                                 dr = null;
                                             }
                                             if (mftp!=null) {
                                                 try {
                                                     mftp.stop();
                                                 } catch (MBBuildException mbe) {
                                                     lep.LogException(mbe);
                                                 }
                                                 mftp = null;
                                             }
                                         }
                                     });

        btnShowResults.addActionListener(new MBCancelableActionListener(thisFrame) {
                                             public void doAction(ActionEvent evt) {
                                             }
                                         } );


        String bldstatus = buildobj.get_status();
        if (bldstatus != null) {
            if (buildobj.getClass().getName().equals("com.ibm.sdwb.build390.MBDBuild")) {
                buildSteps = MBC_DRIVERBUILD.getBuildSteps();
            } else if (buildobj.getClass().getName().equals("com.ibm.sdwb.build390.MBUBuild")) {
                buildSteps = MBC_USERBUILD.getBuildSteps();
            }
            Enumeration phasesEnum =buildSteps.elements();
            String tempStep = new String();
            while (phasesEnum.hasMoreElements()& !bldstatus.equals(tempStep)) {
                tempStep = (String) phasesEnum.nextElement();
                if (!tempStep.equals(PROCESSBUILDPHASE)) {
                    if (buildobj.getClass().getName().equals("com.ibm.sdwb.build390.MBUBuild")) {
                        if (((MBUBuild)buildobj).getFastTrack()) {
                            if (!tempStep.equals(FASTSKIPPEDPHASE1) & !tempStep.equals(FASTSKIPPEDPHASE2) & !tempStep.equals(FASTSKIPPEDPHASE3)) {
                                ((DefaultListModel)lstSteps.getModel()).addElement(tempStep);
                            }
                        } else {
                            ((DefaultListModel)lstSteps.getModel()).addElement(tempStep);
                        }
                    } else {
                        ((DefaultListModel)lstSteps.getModel()).addElement(tempStep);
                    }
                }
            }
            if (!phasesEnum.hasMoreElements()) {
                boolean fastTrackBuild = false;
                if (buildobj instanceof MBUBuild) {
                    if (((MBUBuild) buildobj).getFastTrack()) {
                        fastTrackBuild = true;
                    }
                }
                if (!fastTrackBuild) {
                    MBDrReport drReport = new MBDrReport(buildobj, ((MBInternalFrame)parentFrame).getStatus(),lep);
                    drReport.initializeReport(false);
                    Hashtable phaseNames = drReport.getStepNames(buildobj.get_buildtype());
                    for (int i = 1; i <= buildobj.get_hostBuildStep(); i++) {
                        if (i==1) {
                            String prfn = new String(buildobj.getBuildPath()+"phaseresults0.prt");
                            File prf = new File(prfn);
                            if (prf.exists()) {
                                ((DefaultListModel) lstSteps.getModel()).addElement(HOSTPHASE + "0 - " + "Process Load Order");
                            }
                        }
                        ((DefaultListModel) lstSteps.getModel()).addElement(HOSTPHASE + i + " - " + (String) phaseNames.get(Integer.toString(i)));
                    }
                } else {
                    ((DefaultListModel) lstSteps.getModel()).addElement(HOSTPHASE + "1 - Build");
                }
            }

            lstSteps.addListSelectionListener(new ListSelectionListener() {
                                                   public void valueChanged(ListSelectionEvent ie) {
                                                       boolean stb = false;
                                                       if (ie.getLastIndex() > -1)
                                                           stb = true;
                                                       btnRestart.setEnabled(stb);
                                                       btnShowResults.setEnabled(stb);
                                                       if (stb) {
                                                           if (((String)lstSteps.getSelectedValue()).startsWith(HOSTPHASE){
                                                               cbCheckJobs.setEnabled(true);
                                                               cbCheckJobs.setSelected(false);
                                                           } else {
                                                               cbCheckJobs.setEnabled(false);
                                                               cbCheckJobs.setSelected(false);
                                                           }
                                                       }
                                                   }
                                               });
            setVisible(true);
        }
        else {
            MBClient.SetArgValue("RESTART_PHASE", "none", true);
            restartStep = "none";
            dispose();
        }
*/      
    }
    private JList setUpInterface() {

        setForeground(MBGuiConstants.ColorRegularText);
        setBackground(MBGuiConstants.ColorGeneralBackground);
        JPanel centerPanel = new JPanel();
        centerPanel.setLayout(new BoxLayout(centerPanel, BoxLayout.PAGE_AXIS));


        JLabel label1 = new JLabel("Select the phase to restart:");
        label1.setForeground(MBGuiConstants.ColorGroupHeading);


        final JList lstSteps = new JList(new DefaultListModel());
        lstSteps.setBackground(MBGuiConstants.ColorFieldBackground);

        JScrollPane listScroller = new JScrollPane(lstSteps);
        listScroller.setPreferredSize(new Dimension(300,150));
        listScroller.setAlignmentX(LEFT_ALIGNMENT);

        final JCheckBox cbCheckJobs = new JCheckBox("Check results of previous execution of phase", false);
        cbCheckJobs.setEnabled(false);

        label1.setLabelFor(lstSteps);
        centerPanel.add(label1);
        centerPanel.add(Box.createRigidArea(new Dimension(0,5)));
        centerPanel.add(listScroller);
        centerPanel.setBorder(BorderFactory.createEmptyBorder(10,10,10,10));
        centerPanel.add(listScroller);
        centerPanel.add(cbCheckJobs);
        java.util.Vector actionButtons = new java.util.Vector();

        final OkAction ok = new OkAction(lstSteps, cbCheckJobs);
        ok.setEnabled(false);

        HelpAction help = null;
        if (theProcess instanceof com.ibm.sdwb.build390.process.DriverBuildProcess) {
            help = new HelpAction("HDRRESTART",HelpTopicID.RESTARTING_A_DRIVERBUILD);
        } else if (theProcess instanceof com.ibm.sdwb.build390.process.UserBuildProcess) {
            help = new HelpAction("HDRRESTART",HelpTopicID.RESTARTING_A_USERBUILD);
        } 

        actionButtons.addElement(new JButton(ok));

        lstSteps.addListSelectionListener(new javax.swing.event.ListSelectionListener() {
                                              /**
                                               * Called whenever the value of the selection changes.
                                               * @param e the event that characterizes the change.
                                               */
                                              public void valueChanged(javax.swing.event.ListSelectionEvent e) {
                                                  if (lstSteps.getSelectedIndex() !=-1) {
                                                      ok.setEnabled(true);
                                                      if (((String)lstSteps.getSelectedValue()).startsWith(MVSPHASE)) {
                                                          if (enablePreviousResults) {
                                                              cbCheckJobs.setEnabled(true);
                                                          }
                                                      } else {
                                                          cbCheckJobs.setEnabled(false);
                                                          cbCheckJobs.setSelected(false);
                                                      }

                                                  }

                                              }
                                          });
        JButton helpButton = new JButton(help);
        helpButton.setForeground(MBGuiConstants.ColorHelpButton);
        addButtonPanel(helpButton, actionButtons);
        getContentPane().add("Center", centerPanel);
        return lstSteps;
    }

    public void populateStepList(java.util.List phasesInformationList) {
        JList theList = setUpInterface();
        String startStepPhase = "Beginning";
        ((DefaultListModel) theList.getModel()).addElement(startStepPhase);
        stepNameToNumberMap.put("Beginning", new StepIterationPair(0, 0));
        Iterator stepIterator = theProcess.getStepsThatHaveRun().iterator();
        while (stepIterator.hasNext()) {
            AbstractProcess.RepeatedProcessStep theStep = (AbstractProcess.RepeatedProcessStep) stepIterator.next();
            if (theStep.getStep().isVisibleToUser()) {
                String stepName = theStep.getStep().getName();
                ((DefaultListModel) theList.getModel()).addElement(stepName);
                stepNameToNumberMap.put(stepName, new StepIterationPair(theStep.getStepNumber(), theStep.getRepeptition()));
            }

            if (theStep.getStep() instanceof  FullProcess) { /*TST1787 */
                FullProcess fullprocess = (FullProcess)theStep.getStep();
                if (!fullprocess.hasCompletedSuccessfully()) {
                    if (phasesInformationList!=null) {
                        if (phasesInformationList.size() > theStep.getRepeptition()+2) { /*PTM3611 */
                            PhaseInformation phaseInfo = ( PhaseInformation) phasesInformationList.get(theStep.getRepeptition()+1);
                            /**if (theStep.getRepeptition() >= (phaseInfo.getPhaseNumberToHaltOnIfErrorsFound()-1)) {
                                 break;
                             } 
                             **/
                            break;

                        }
                    }
                }
            }
        }
    }

    public int getStepToStartWith() {
        return stepToStartWith;
    }

    public int getIterationToStartWith() {
        return iterationToStartWith;
    }

    /* public boolean shouldCheckPreviousExecutionResults(){
         return checkResultsOfPreviousPhase;
     }
     */
    public void setEnablePreviousResultsCheckBox(boolean enablePreviousResults) {
        this.enablePreviousResults = enablePreviousResults;
    }

    class StepResultsAction extends CancelableAction {

        StepResultsAction() {
            super("Step Results");
        }

        public void doAction(ActionEvent e) {
/*
            java.util.Vector failedJobs = new java.util.Vector();
            int phaseidx = 0;
            int idx = lstSteps.getSelectedIndex();
            if (idx > -1) {
                String selectedStep = (String) lstSteps.getSelectedValue();
                String buildStepFile = null;
                String buildStepJobResultFile = null;
                if (selectedStep != null) {
                    if (selectedStep.startsWith(HOSTPHASE)) {
                        String pnum = new String(selectedStep.substring(HOSTPHASE.length()));
                        pnum = pnum.trim();
                        pnum = pnum.substring(0, pnum.indexOf(" "));
                        buildStepFile = new String(buildobj.getBuildPath()+"phaseresults"+pnum+".prt");
                        File tf = new File(buildStepFile);
                        if (!tf.exists()) {
                            buildStepFile = new String(buildobj.getBuildPath()+"phase"+pnum+".result");
                        }
                        buildStepJobResultFile = new String(buildobj.getBuildPath()+"jobstatus"+pnum+".out");
                        Hashtable tempHash = new Hashtable();
                        tempHash.put("BUILD", buildobj);
                        tempHash.put("PHASE", pnum);
                        tempHash.put("QUERYTYPE", "FAILEDJOBS");
                        tempHash.put("STATUS", getStatus());
                        tempHash.put("LOGPROCESSOR",lep);
                        try {
                            MBC_JOBOUTPUT getJobOutput = new MBC_JOBOUTPUT();
                            failedJobs = (Vector) getJobOutput.ServerCmd(tempHash);
                        } catch (MBBuildException mbbe) {
                            lep.LogException(mbbe);
                        }
                    } else {
                        if (buildobj.getClass().getName().equals("com.ibm.sdwb.build390.MBDBuild")) {
                            buildStepFile = new String(buildobj.getBuildPath()+MBC_DRIVERBUILD.getBuildStepFile(idx));
                        } else if (buildobj.getClass().getName().equals("com.ibm.sdwb.build390.MBUBuild")) {
                            buildStepFile = new String(buildobj.getBuildPath()+MBC_USERBUILD.getBuildStepFile(idx));
                        }

                    }
                    if (buildStepFile != null) {
                        MBEdit edit = new MBEdit(buildStepFile,lep);
                    }
                }
            }
*/          
        }

        public void stop() {
        }
    }

    class OkAction extends CancelableAction {
        private JList stepListDisplay = null;
        private JCheckBox checkPreviousExecutionResultsElement = null;

        OkAction(JList tempList, JCheckBox tempCheck) {
            super("Ok");
            checkPreviousExecutionResultsElement = tempCheck;
            stepListDisplay = tempList;
        }

        public void doAction(ActionEvent e) {
            String selectedStepName = (String) stepListDisplay.getSelectedValue();
            StepIterationPair selectedPair = (StepIterationPair) stepNameToNumberMap.get(selectedStepName);
            stepToStartWith = selectedPair.getStep();
            iterationToStartWith = selectedPair.getIteration();
            checkResultsOfPreviousPhase =  checkPreviousExecutionResultsElement.isSelected();
            boolean results = false;
            if (checkResultsOfPreviousPhase) {
                ProcessStep tempStepO =null;
                for (ListIterator runStepIterator = theProcess.getStepsThatHaveRun().listIterator(); runStepIterator.hasNext();) {
                    AbstractProcess.RepeatedProcessStep tempStep = ( AbstractProcess.RepeatedProcessStep) runStepIterator.next();
                    int repetition = tempStep.getRepeptition();
                    int stepNumber = tempStep.getStepNumber();
                    if (stepToStartWith==stepNumber &&
                        repetition == iterationToStartWith) {
                        tempStepO = tempStep.getStep();
                        break;
                    }

                }
                if (checkPreviousPhaseStep(tempStepO))
                    thisFrame.dispose();
            } else {
                thisFrame.dispose();
            }

        }

        public void stop() {
        }
    }

    private boolean checkPreviousPhaseStep( ProcessStep  step) {
        BuildPhaseOnMVS currentStep = null;
        for (ListIterator runStepIterator = (( FullProcess)step).getProcessRun().getStepsThatHaveRun().listIterator(); runStepIterator.hasNext();) {
            AbstractProcess.RepeatedProcessStep tempStep = ( AbstractProcess.RepeatedProcessStep) runStepIterator.next();
            if (tempStep.getStep() instanceof  BuildPhaseOnMVS) {
                currentStep = ( BuildPhaseOnMVS)tempStep.getStep();
                break;
            }
        }


        if (currentStep==null) {
            return true;
        }

        MBPhaseResultFileParser phaseResultsParser = currentStep.getResultParser();

        if (phaseResultsParser==null) /**TST1911 */
            return true;

        try {
            if (phaseResultsParser.getErrorInfo()) {
                new MBMsgBox("Phase error!","Errors were found in the previous execution of the selected phase, Restart terminated");
                return false;
            } else if (phaseResultsParser.getWarningInfo()) {
                new MBMsgBox("Warning!","Warnings were found in the previous execution of the selected phase, Restart terminated");
                return false;
            } else {
                Set allHeldJobs = currentStep.getJobsCreatedByDriverBuildCall();
                Set failedJobs = new HashSet();
                for (Iterator jobIterator = allHeldJobs.iterator(); jobIterator.hasNext();) {
                    MBJobInfo jobInfo = (MBJobInfo) jobIterator.next();
                    if (jobInfo.isComplete()&!jobInfo.isSuccessful()) {
                        failedJobs.add(jobInfo);
                    }
                }

                if (failedJobs.size() > 0) {
                    MBMsgBox question = new MBMsgBox("Host Job Errors", "There were errors.  Should host job results be deleted?", parentFrame, true);
                    if (question.isAnswerYes()) {

                        ProcessWrapperForSingleStep wrapper = new  ProcessWrapperForSingleStep(this);
                        PurgeJobOutput jobPurgeStep = new  PurgeJobOutput(theProcess.getCleanableEntity(), MBGlobals.Build390_path +"misc", wrapper);
                        wrapper.setStep(jobPurgeStep);
                        wrapper.externalRun();
                        //jobPurgeStep.externalExecute();
                    }
                    new MBMsgBox("Jobs failed.","At least 1 job submitted during the previous execution of the selected phase failed, Restart terminated");
                    return false;
                } else {
                    iterationToStartWith++;
                    if (iterationToStartWith >= phaseResultsParser.getTotalNumberOfPhases()) { /*INT1897 */
                        iterationToStartWith--;
                    }
                    return true;
                }
            }
        } catch (MBBuildException mbbe) {
            lep.LogException(mbbe);
        }

        return false;

    }

    class StepIterationPair {
        private int step = -1;
        private int iteration = -1;

        StepIterationPair(int tempStep, int tempIteration) {
            step = tempStep;
            iteration = tempIteration;
        }

        public int getStep() {
            return step;
        }

        public int getIteration() {
            return iteration;
        }
    }
}
