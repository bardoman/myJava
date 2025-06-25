package com.ibm.sdwb.build390;
/* A basic extension of the java.awt.Dialog class */
// Changes
// Date     Defect/Feature      Reason
// 04/01/99 MoveImages          move images directory
// 04/27/99
// 05/05/99 UI stuff            fix some colors
// 10/20/2004 PTM3735           Getter methods to access PROGRAMVERSION/BUILDDATE.
/*********************************************************************/

import java.awt.*;
import java.io.*;
import java.awt.image.*;
import javax.swing.*;
import javax.swing.border.*;

public class MBAboutDialog extends MBBasicInternalFrame {
    private static ImageIcon doggyPic;

    private static boolean isShowing = false;
    private static MBAboutDialog showingVersion = null;
    private JCheckBox dontshow;

    public MBAboutDialog() {
        super("About "+MBConstants.productName,null, true, false, null);
        if (!isShowing) {
            isShowing = true;
            showingVersion = this;
            // MoveImages
            JPanel mainPanel = new JPanel(new BorderLayout());

            doggyPic = new ImageIcon(MBGlobals.Build390_path+"images"+File.separator+MBConstants.MASCOTFILE);

            setVisible(false);
            setForeground(MBGuiConstants.ColorRegularText);
            setBackground(MBGuiConstants.ColorGeneralBackground);

            GridLayout g = new GridLayout();
            MBInsetPanel p1 = new MBInsetPanel(g,5,5,5,5);
            TitledBorder tld1 = new TitledBorder("Version : ");
            tld1.setTitleColor(MBGuiConstants.ColorGroupHeading);
            p1.setBorder(tld1);

            g.setColumns(0);
            g.setRows(1);
            JLabel dummy = new JLabel("  ");
            p1.add(dummy);

            g.setRows(2);
            label1 = new JLabel(" "+MBConstants.productName+": "+MBConstants.getProgramVersion());
            p1.add(label1);

            label2 = new JLabel(" Build Date: "+MBConstants.getBuildDate());
            g.setRows(3);
            p1.add(label2);


            mainPanel.add(p1,BorderLayout.NORTH);

            // add the dontshow checkbox and check it if the ser file exists

            GridLayout g1 = new GridLayout();
            MBInsetPanel p2 = new MBInsetPanel(g1,5,5,5,5);

            TitledBorder tld = new TitledBorder("Copyright : ");
            tld.setTitleColor(MBGuiConstants.ColorGroupHeading);
            p2.setBorder(tld);
            Insets is = new Insets(5,5,5,5);

            mainPanel.add(p2,BorderLayout.SOUTH);

            g1.setColumns(0);
            g1.setRows(1);
            JLabel dummy1 = new JLabel("  ");
            p2.add(dummy1);

            JLabel label3 = new JLabel(MBConstants.COPYRIGHT1);
            g1.setRows(2);
            p2.add(label3,BorderLayout.NORTH);


            g1.setRows(3);
            JLabel label4 = new JLabel(MBConstants.COPYRIGHT2);         
            p2.add(label4,BorderLayout.CENTER);

            g1.setRows(4);
            JLabel label5 = new JLabel(MBConstants.COPYRIGHT3);         
            p2.add(label5,BorderLayout.SOUTH);


            g1.setRows(5);
            JLabel label6 = new JLabel(MBConstants.COPYRIGHT4);         
            p2.add(label6,BorderLayout.NORTH);

            g1.setRows(6);
            JLabel label7 = new JLabel(MBConstants.COPYRIGHT5);         
            p2.add(label7,BorderLayout.SOUTH);

            dontshow = new JCheckBox("Don't show this dialog at startup");
            dontshow.setBackground(MBGuiConstants.ColorGeneralBackground);
            g1.setRows(7);
            p2.add(dontshow);


            File tf = new File(MBConstants.dontshowfile);
            if (tf.exists()) {
                dontshow.setSelected(true);
            }

            getContentPane().add("West", mainPanel);
            JLabel imageLabel = new JLabel(doggyPic, JLabel.CENTER);
            doggyPic.setImageObserver(imageLabel);
            getContentPane().add("Center", imageLabel);
            okButton = new JButton("OK");
            JPanel tempPanel = new JPanel();
            tempPanel.setBackground(MBGuiConstants.ColorGeneralBackground);
            tempPanel.add(okButton);
            getContentPane().add("South", tempPanel);

            SymAction lSymAction = new SymAction();
            okButton.addActionListener(lSymAction);

            setVisible(true);
            okButton.requestFocus();
        } else {
            dispose();
        }
    }

    public void dispose() {
        if (this == showingVersion) {
            isShowing = false;
            showingVersion = null;
        }
        super.dispose();
    }

    JLabel label1;
    JLabel label2;
    JButton okButton;

    class SymAction implements java.awt.event.ActionListener {
        public void actionPerformed(java.awt.event.ActionEvent event) {
            Object object = event.getSource();
            if (object == okButton)
                okButton_Clicked(event);
        }
    }

    void okButton_Clicked(java.awt.event.ActionEvent event) {
        // Clicked from okButton Hide the Dialog
        // if the checkbox is checked, create the ser file, else delete the ser file
        if (dontshow.isSelected()) {
            try {
                ObjectOutputStream oos = new ObjectOutputStream(
                                                               new FileOutputStream(MBConstants.dontshowfile));
                oos.writeObject(dontshow);
                oos.close();
            } catch (IOException ioe) {
                System.out.println("error saving " + MBConstants.dontshowfile+"   ");
                ioe.printStackTrace(System.err);
            }
        } else {
            File tf = new File(MBConstants.dontshowfile);
            tf.delete();
        }
        dispose();
    }
}
