package com.ibm.sdwb.build390;
/*
    A basic extension of the java.awt.Dialog class
 */
// 04/27/99 errorHandling       change LogException parms & add new error types
// 06/16/99 usability stuff		move build phases up to menu, change from edit to view
// 01/07/2000 ind.build.log      changes for logging the build details into a individual build log for each build
// 03/07/2000 reworklog          changes to implement the log stuff using listeners
import java.io.*;
import java.awt.*;
import java.awt.event.*;
import java.util.Vector;
import javax.swing.*;
import com.ibm.sdwb.build390.logprocess.*;

public class MBUBuildEdit extends MBBuildEdit {
    private MBUBuild build;
    private MBButtonPanel buttonPanel;
    private JMenuItem btnPhases;
    private JInternalFrame thisFrame;


    public MBUBuildEdit( JInternalFrame pFrame, MBUBuild tempBuild) throws com.ibm.sdwb.build390.MBBuildException
    {
        super("User Build Viewer", pFrame, tempBuild);

        build = tempBuild;
        thisFrame = this;

        setVisible(true);
    }

    protected void initializeDialog(){
        super.initializeDialog();
        int bottom = getBottomRow();
        GridBagConstraints c = new GridBagConstraints();

        c.weighty = 1;
        c.gridx = 1;
        JPanel tempPanel  = new JPanel();
        c.gridy = bottom + 1;
        c.gridwidth = GridBagConstraints.REMAINDER;
        gridBag.setConstraints(tempPanel, c);
        centerPanel.add(tempPanel);
        cbFastTrack = new JCheckBox("FastTrack");
        cbFastTrack.setEnabled(false);
        tempPanel.add(cbFastTrack);
        cbControlled = new JCheckBox("Controlled");
        cbControlled.setEnabled(false);
        tempPanel.add(cbControlled);
        cbDelta = new JCheckBox("Delta Build");
        cbDelta.setEnabled(false);
        tempPanel.add(cbDelta);

        label10 = new JLabel(" Local Parts ");
        c.weighty = 1;
        c.gridx = 1;
        c.gridy = bottom + 6;
        gridBag.setConstraints(label10, c);
        centerPanel.add(label10);
        c.gridwidth = 2;
        c.gridy = bottom + 7;
        c.fill = GridBagConstraints.HORIZONTAL;
        c.weightx = GridBagConstraints.REMAINDER;
        listLocalParts = new JList();
        listLocalParts.setBackground(MBGuiConstants.ColorFieldBackground);
        JScrollPane listScroller = new JScrollPane(listLocalParts);
        gridBag.setConstraints(listScroller, c);
        centerPanel.add(listScroller);

        btnPhases = new JMenuItem("Build Phases");
        c.gridx=1;
        c.gridy= bottom+8;
        c.gridwidth = GridBagConstraints.REMAINDER;
        c.fill = GridBagConstraints.NONE;
        JMenu infoMenu = getJMenuBar().getMenu(getJMenuBar().getMenuCount()-1);
        infoMenu.add(btnPhases);
        btnPhases.addActionListener(new ActionListener() {
                                        public void actionPerformed(ActionEvent evt) {
                                            new Thread(new Runnable() {
                                                           public void run() {
/*
                                                               try {
                                                                   MBPhaseList pl = new MBPhaseList(build, thisFrame,lep);
                                                               } catch (MBBuildException mbe) {
                                                                   lep.LogException(mbe);
                                                               }
*/															   
                                                           }
                                                       }).start();
                                        }
                                    });

    }

    public void displayBuildInfo(MBBuild tempBuild) {
        super.displayBuildInfo(tempBuild);
        MBUBuild build = (MBUBuild) tempBuild;
        if (build != null) {
            cbFastTrack.setSelected(build.getFastTrack());
            String[] tempParts = build.getLocalParts();
            String[] clevels = new String[tempParts.length - 1];
            System.arraycopy(tempParts, 1, clevels, 0, tempParts.length - 1);
            listLocalParts.setListData(clevels);
            cbControlled.setSelected(build.getOptions().isControlled());
            cbDelta.setSelected(!build.getSource().isIncludingCommittedBase());
        }
    }


    protected String buildDirectory() {
        return MBConstants.USERBUILDDIRECTORY;
    }

    //public void setVisible(boolean visible)
    //{
    //	Rectangle bounds = getParent().getBounds();
    //	Rectangle abounds = getBounds();

    //	setLocation(bounds.x + (bounds.width - abounds.width)/ 2,
    //		 bounds.y + (bounds.height - abounds.height)/2);

    //	super.setVisible(visible);
    //}

    //{{DECLARE_CONTROLS
    JLabel label10;
    JLabel label20;
    JList listLocalParts;
    JCheckBox cbFastTrack;

    //}}

    /** The CommandFilter class creates a list of B* files and do not contain a . */
    class CommandFilter implements FilenameFilter {
        public boolean accept(File dir, String name) {
            if (name.startsWith("U")) {
                if (name.indexOf(".") < 0) {
                    return new File(dir, name).exists();
                }
            }
            return false;
        }
    }
}
