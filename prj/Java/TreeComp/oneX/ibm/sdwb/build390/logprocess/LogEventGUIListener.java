
package com.ibm.sdwb.build390.logprocess;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.event.ActionListener;
import java.io.File;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JInternalFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;

import com.ibm.sdwb.build390.MBClient;
import com.ibm.sdwb.build390.MBConstants;
import com.ibm.sdwb.build390.MBEdit;
import com.ibm.sdwb.build390.MBGlobals;
import com.ibm.sdwb.build390.MBMsgBox;
import com.ibm.sdwb.build390.StopError;




/*****************************************************************************************************/
/* Java GUIListener for Build/390 client                                                                */
/* This is the LogEventGUIListener class for the build/390 LogProcessing                                        */
/****************************************************************************************************/
/* The LogEventGUIListener class Listens to the LogEvents invoked and decides whether to show up on the screen or no
/* - has abstract method HandleLogEvent which is overrided to display the stuff on the screen
/* - isInteresting() is true if visual debug is set on + on a logprimary info event level or
/* - if its a LogException. - the same applies handling events for the config process
/****************************************************************************************************/
/*09/15/2003 TST1558 - show hosterrors, when multipleconcurrentexception occurs. **/

public class LogEventGUIListener extends LogEventListener {

    static final long serialVersionUID = 1111111111111111L;
    transient JComponent basicComponent=null;
    transient JInternalFrame internalFrame = null;



    public LogEventGUIListener(JComponent component) {
        basicComponent = component;
    }

    public synchronized void handleLogEvent(final LogEvent l) {
        Thread handler =  new handleDisplay(l);
        if (l.getEventLevel()==LogEvent.LOGPRMYINFO_EVT_LEVEL) {
            handler.run();  // if this is debug, wait while we run it
        } else {
            handler.start();  // if exception, display it and don't wait.
        }

    }

    // if debug is set or
    // if its a logExceptionEvent or
    //
    public  boolean isInterestingEvent(LogEvent l) {
        if (((MBClient.getVisualDebug())&&(l.getEventLevel()==LogEvent.LOGPRMYINFO_EVT_LEVEL)&&l.getEventDisplay())||
            (l.getEventLevel()==LogEvent.LOGEXCEPTION_EVT_LEVEL)) {
            return true;
        }
        return false;
    }



    private JPanel createPanel(String PassedEventType,String PassedEventInfo, final JDialog msgDialog) {
        JPanel msgPanel = new JPanel(new BorderLayout());
        JLabel  titleLabel = new JLabel(PassedEventType);
        titleLabel.setForeground(Color.red);
        JTextArea  statusArea = new JTextArea(5,40);
        statusArea.setText(PassedEventInfo);

        statusArea.setLineWrap(true);
        statusArea.setWrapStyleWord(true);
        statusArea.setBackground(Color.black);
        statusArea.setForeground(Color.green);

        final JButton btOK = new JButton("OK");
        JPanel  btnpanel  = new JPanel();
        FlowLayout flowlayout = new FlowLayout();
        btnpanel.setLayout(flowlayout);
        btnpanel.add(btOK);


        msgPanel.add(titleLabel,BorderLayout.NORTH);

        //the getLineCount did nt work.. so use the linelength as cut off.
        if (statusArea.getText().length() <= 1000) {
            msgPanel.add(statusArea, BorderLayout.CENTER);
        } else {
            statusArea.setRows(25);
            JScrollPane scrollPanel   = new JScrollPane(statusArea);
            msgPanel.add(scrollPanel, BorderLayout.CENTER);

        }

        msgPanel.add(btnpanel,BorderLayout.SOUTH);

        btOK.setToolTipText("Press to Continue");
        btOK.addActionListener(new ActionListener() {
                                   public void actionPerformed(java.awt.event.ActionEvent A) {
                                       if (A.getSource()==btOK) {
                                           msgDialog.dispose();
                                       }
                                   }
                               });

        return msgPanel;

    }

    private class handleDisplay extends Thread {

        private LogEvent l = null;

        private final  ActionListener al = new ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent A) {
                String logFilepath = MBGlobals.Build390_path+MBConstants.LOGFILEPATH;
                if ((new File(logFilepath)).exists()) {
                    new MBEdit(logFilepath,MBClient.lep);
                } else {
                    new MBMsgBox("Information:","Build.log empty");
                }

            }
        };

        handleDisplay(LogEvent tempL) {
            l = tempL;
        }

        public void run() {
            String logData= l.toLoggableString();
            if ((isInterestingEvent(l))&!(l.getExtraInformation() instanceof StopError)) {
                if (l.getEventLevel()==LogEvent.LOGEXCEPTION_EVT_LEVEL) {
                    /** TST1558 **/
                    ExceptionEventComposer exceptionFormatter = new ExceptionEventComposer(l);
                    if (l.getExtraInformation() instanceof com.ibm.sdwb.build390.utilities.MultipleConcurrentException) {
                        exceptionFormatter.handleMultipleConcurrentException();
                    } else {
                        exceptionFormatter.handleSingleException(l.getExtraInformation());
                    }

                    if (exceptionFormatter.hasHostErrors()) {
                        showHostFiles(exceptionFormatter.getHostErrorFilesMap());
                    } else {
                        new MBMsgBox(l.getEventType()+" : "+ getFrame().getTitle(),l.getEventInfo(),getFrame(),false,"View Build Log", al);
                    }

                } else {
                    new MBMsgBox(l.getEventType()+" : " + getFrame().getTitle(),l.getEventInfo(),getFrame());
                }

            }
        }

        private void showHostFiles(Map  hostFilesMap) {
            final  ActionListener al = new ActionListener() {
                public void actionPerformed(java.awt.event.ActionEvent A) {
                    String logFilepath = MBGlobals.Build390_path+MBConstants.LOGFILEPATH;
                    if ((new File(logFilepath)).exists()) {
                        new MBEdit(logFilepath,MBClient.lep);
                    } else {
                        new MBMsgBox("Information:","Build.log empty");
                    }

                }
            };
            for (Iterator iter=hostFilesMap.entrySet().iterator();iter.hasNext();) {
                Set filesSet = (Set)(((Map.Entry)iter.next()).getValue());
                String msg = getHostUserMessage(filesSet);
                MBMsgBox viewQuestion = new MBMsgBox(l.getEventType()+" : "+ getFrame().getTitle(), msg + "\nDo you wish to see the results?", getFrame(), true,"View Build Log",al);
                if (viewQuestion.isAnswerYes()) {
                    for (Iterator filesIterator = filesSet.iterator();filesIterator.hasNext();) {
                        Object singleObj = filesIterator.next();
                        if (singleObj instanceof File) {
                            new MBEdit(((File)singleObj).getAbsolutePath(),MBClient.lep);
                        }
                    }
                }
            }
        }

        private String getHostUserMessage(Set tempFilesSet) {
            for (Iterator filesIterator = tempFilesSet.iterator();filesIterator.hasNext();) {
                Object singleObj = filesIterator.next();
                if (singleObj instanceof String) {
                    return(String)singleObj;
                }
            }
            return new String();
        }



        private JInternalFrame getFrame() {
            if (internalFrame == null) {
                internalFrame = com.ibm.sdwb.build390.userinterface.graphic.utilities.GeneralUtilities.getParentInternalFrame(basicComponent);
            }
            return internalFrame;
        }
    }

}







