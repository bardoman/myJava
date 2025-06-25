package com.ibm.sdwb.build390.userinterface.graphic.panels;
/*********************************************************************/
/* MBNewShadowDialog class for the Build/390 client                  */
/*  Creates and manages the Driver Build Page                        */
/*********************************************************************/
// Changes
// Date     Defect/Feature      Reason
// 11/18/98                     This is a new dialog
// 04/05/2000 pdsmember
// 04/05/2000 pdsmembercarried  made changes so that whatever the user enters as the pdsname textfield in mbuserbuildpage gets carried over to this dialog
// 02/01/2002 #DEF.INT0792:      Must allow user to restart after reselect of parts 
//12/03/2002 SDWB-2019 Enhance the help system
/*********************************************************************/
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.List;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.io.Serializable;
import java.util.Hashtable;
import java.util.Vector;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;

import com.ibm.sdwb.build390.*;
import com.ibm.sdwb.build390.help.HelpTopicID;
import com.ibm.sdwb.build390.userinterface.graphic.MainInterface;
import com.ibm.sdwb.build390.userinterface.graphic.widgets.CancelableAction;
import com.ibm.sdwb.build390.userinterface.text.utilities.CommandLineSettings;

/** Create the driver build page */
public class PDSUserBuildPartsSelectorFrame extends MBModalFrame implements Serializable {

    private JTextArea    Label01      = new JTextArea("Indicate the fully qualified PDS name and the class of parts in the PDS",1,40);
    private JButton btHelp      = new JButton("Help");
    private JButton btOk        = new JButton("Ok");
    private JLabel PartLabel    = new JLabel("PDS Name");
    private JLabel ClassLabel   = new JLabel(" Part Class  ");
    private JTextField pdsname  = new JTextField("", 30);
    private JTextField pdsPartClass = new JTextField("", 8);


    private String userPDSEntry = "";
    private String userPDSPartTypeEntry = "";
    //Begin TST3412
    private boolean isFastTrack = false;
    boolean isFakeLib = MBClient.getCommandLineSettings().getMode().isFakeLibrary();
    //End TST3412

    /**
    * constructor - Create a MBUserBuildPartTypeDialog
    */
    public PDSUserBuildPartsSelectorFrame(String tempPDSName, String tempPDSPartType, MBInternalFrame parentFrame,boolean isFastTrack) throws com.ibm.sdwb.build390.MBBuildException{
        super("Part Location", parentFrame, null);
        this.userPDSEntry= tempPDSName;
        this.userPDSPartTypeEntry= tempPDSPartType;
        this.isFastTrack = isFastTrack;
        initializeDialog();
    }

    public void initializeDialog() throws com.ibm.sdwb.build390.MBBuildException {
        setVisible(false);
        if(userPDSEntry!=null && userPDSEntry.trim().length() > 0) {
            pdsname.setText(userPDSEntry);
            userPDSEntry = "";
        }
        if(userPDSPartTypeEntry!=null && userPDSPartTypeEntry.trim().length() > 0) {
            pdsPartClass.setText(userPDSPartTypeEntry);
            userPDSPartTypeEntry = "";
        }

        GridBagLayout gridBag = new GridBagLayout();
        JPanel centerPanel  = new JPanel(gridBag);
        setForeground(MBGuiConstants.ColorRegularText);
        setBackground(MBGuiConstants.ColorGeneralBackground);

        // help button
        btHelp.setForeground(MBGuiConstants.ColorHelpButton);
        btHelp.addActionListener(new ActionListener() {
                                     public void actionPerformed(ActionEvent evt) {
                                         try {
                                             //Begin TST3412
                                             if(isFakeLib) {

                                                 MBUtilities.ShowHelp(HelpTopicID.FAKELIB_EXAMPLE);

                                             }
                                             else
                                                 if(isFastTrack) {
                                                 MBUtilities.ShowHelp(HelpTopicID.PDSPARTS_FASTTRACK_HELP);
                                             }
                                             else {
                                                 MBUtilities.ShowHelp(HelpTopicID.PDSUSERBUILDPAGE_HELP);
                                             }
                                             //End TST3412
                                         }
                                         finally {
                                         }
                                     }
                                 } );

        // OK button
        btOk.setForeground(MBGuiConstants.ColorActionButton);
        btOk.addActionListener(new ActionListener() {
                                   public void actionPerformed(ActionEvent evt) {
                                       StringBuilder strbError  =  new StringBuilder();
                                       userPDSEntry = pdsname.getText().trim();
                                       if(userPDSEntry.length() < 1) {
                                           strbError.append("You must specify a PDS name.\n");
                                       }

                                       userPDSPartTypeEntry  = pdsPartClass.getText().trim();
                                       if(userPDSPartTypeEntry.length() < 1) {
                                           strbError.append("You must specify a ");
                                           if(CommandLineSettings.getInstance().getMode().isFakeLibrary()) {
                                               strbError.append("part type. (eg: ASM, C, ORD, MAC, PLX...).");
                                           }
                                           else {
                                               strbError.append("part class. (eg: MODULE, ORDER...).");
                                           }
                                       }

                                       if(strbError.length() >0) {
                                           problemBox("Error", strbError.toString(),false);
                                       }
                                       else {
                                           userPDSEntry         = userPDSEntry.toUpperCase();
                                           userPDSPartTypeEntry = userPDSPartTypeEntry.toUpperCase();
                                           dispose();
                                       }
                                   }
                               });


        // build dialog
        Vector actionButtons = new Vector();
        actionButtons.addElement(btOk);
        MBButtonPanel bottomButtonPanel = new MBButtonPanel(btHelp,null,actionButtons);
        Label01.setFont(new Font("Dialog",Font.BOLD,12));

        //Maybe the mode should be singleton. Why should a gui class want stuff from CommandLineSetting class ??
        if(CommandLineSettings.getInstance().getMode().isFakeLibrary()) {
            ClassLabel.setText(" Part Type    ");
            Label01.setRows(3);
            Label01.setText("Indicate the fully qualified PDS name and the type of parts in the PDS. This Part Type consists of the file extension for the part as it would appear in a local directory (ie: ASM, C, ORD, MAC, PLX...).");
        }

        Label01.setLineWrap(true);
        Label01.setWrapStyleWord(true);
        Label01.setBackground(MBGuiConstants.ColorGeneralBackground);
        Label01.setForeground(MBGuiConstants.ColorGroupHeading);

        GridBagConstraints c = new GridBagConstraints();
        c.fill = GridBagConstraints.NONE;

        c.weightx = 0.0;  
        c.gridwidth = GridBagConstraints.REMAINDER;
        c.insets = new Insets(5,5,5,5);
        gridBag.setConstraints(Label01, c);
        centerPanel.add(Label01);

        c.anchor= GridBagConstraints.WEST;
        c.gridwidth = GridBagConstraints.RELATIVE;
        gridBag.setConstraints(PartLabel, c);
        centerPanel.add(PartLabel);
        c.gridwidth = GridBagConstraints.REMAINDER;
        gridBag.setConstraints(pdsname, c);
        centerPanel.add(pdsname);

        c.anchor= GridBagConstraints.WEST;
        c.gridwidth = GridBagConstraints.RELATIVE;
        gridBag.setConstraints(ClassLabel, c);
        centerPanel.add(ClassLabel);

        c.weightx = 0.0;  
        c.fill = GridBagConstraints.NONE;
        c.gridwidth = GridBagConstraints.REMAINDER;
        gridBag.setConstraints(pdsPartClass, c);
        centerPanel.add(pdsPartClass);

        getContentPane().add("Center", centerPanel);
        getContentPane().add("South",  bottomButtonPanel);
        pack();
    }

    public String getPDSName() {
        return userPDSEntry;
    }

    public String getPDSPartType() {
        return userPDSPartTypeEntry;
    }


    public void postVisibleInitialization() {
        pdsname.requestFocus();
    }


}
