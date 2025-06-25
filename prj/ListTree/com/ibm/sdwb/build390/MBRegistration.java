package com.ibm.sdwb.build390;
/*********************************************************************/
/* MBRegistration class for the Build/390 client                     */
/*  Manages client registration                                      */
/*********************************************************************/
// Changes
// Date     Defect/Feature      Reason
// 09/30/99 pjs - Fix help link
//12/03/2002 SDWB-2019 Enhance the help system
/*********************************************************************/
import java.awt.*;
import java.awt.List;
import java.awt.event.*;
import java.awt.event.KeyEvent;
import java.io.*;
import java.util.*;
import javax.swing.*;
import javax.swing.event.*;
import javax.swing.border.*;
import com.ibm.sdwb.build390.help.*;
import com.ibm.sdwb.build390.userinterface.graphic.MainInterface;

/** Create the driver build page */
public class MBRegistration extends MBModalFrame implements Serializable {

    private  boolean results = false;
    private  String regFile = new String();
    private  JLabel Label01       = new JLabel("You must register before running the Build/390 client");
    private  JLabel Label02       = new JLabel("Name:");
    private  JLabel Label03       = new JLabel("Lotus Notes Address:");
    private  JLabel Label04       = new JLabel("Your Project:");
    private  JTextField tfName    = new JTextField(20);
    private  JTextField tfAddrs   = new JTextField(20);
    private  JTextField tfProject = new JTextField(20);
    private  JButton btHelp       = new JButton("Help");
    private  JButton btOk         = new JButton("Ok");
    private  ButtonGroup Group1 = new ButtonGroup();

    private  MBButtonPanel tempButt;
    protected GridBagLayout gridBag = new GridBagLayout();
    protected JPanel centerPanel  = new JPanel(gridBag);
    protected JPanel aPanel  = new JPanel(gridBag);

    private String fName  = new String();
    private String fAddrs = new String();
    private String fProject = new String();
    private String reginfo = new String();
    /**
    * constructor - Create a MBRegistration
    * @param MBGUI gui
    */
    public MBRegistration(String filename) {
        super("Registration", null, null);
        regFile = filename;
        initializeDialog();
    }

    public void initializeDialog() {
        setForeground(MBGuiConstants.ColorRegularText);
        setBackground(MBGuiConstants.ColorGeneralBackground);

        btHelp.setForeground(MBGuiConstants.ColorHelpButton);
        btHelp.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                try {
                    //MBUtilities.ShowHelp("Defining_Releases");
                    MBUtilities.ShowHelp("HDRCLIENT",HelpTopicID.REGISTRATION_HELP);
                } finally {
                }
            }} );

        btOk.setForeground(MBGuiConstants.ColorActionButton);
        btOk.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                new Thread(new Runnable() {
                    public void run() {
                        try {
                            // check entrys
                            String errorData = "";
                            fName  = "";
                            fAddrs = "";
                            fProject = "";
                            // check name
                            if (tfName.getText().trim() != null) {
                                fName = tfName.getText().trim();
                                if (fName.length()<1) {
                                    errorData+= "You must specify your name.\n";
                                }
                            }
                            // check Address
                            if (tfAddrs.getText().trim() != null) {
                                fAddrs = tfAddrs.getText().trim();
                                if (fAddrs.length()<1) {
                                    errorData+= "You must specify your Lotus Notes Address.\n";
                                } else {
                                    // valid addresses look like this 'Kent McCaulley/Boulder/IBM'
                                    StringTokenizer st = new StringTokenizer(fAddrs, "/");
                                    if (st.countTokens()<3) {
                                        errorData+= "Valid Lotus Notes Addresses are in the format \'name/location/IBM\'.\n";
                                    }
                                }
                            }
                            // check Project
                            if (tfProject.getText().trim() != null) {
                                fProject = tfProject.getText().trim();
                                if (fProject.length()<1) {
                                    errorData+= "You must specify the project you are working on.\n";
                                }
                            }

                            if (!errorData.equals("")) {
                                new MBMsgBox("Error", "ERROR:MBRegistration:" + errorData);
                            } else {
                                results = true;
                                save();
                                dispose();
                            }
                        } finally {
                        }
                    }
                }).start();
            }} );


        Vector actionButtons = new Vector();
        actionButtons.addElement(btOk);
        tempButt = new MBButtonPanel(btHelp,null,actionButtons);
        Label01.setForeground(MBGuiConstants.ColorGroupHeading);

        GridBagLayout gridBag = (GridBagLayout) centerPanel.getLayout();
        GridBagLayout gridBaga = (GridBagLayout) aPanel.getLayout();

        GridBagConstraints c = new GridBagConstraints();
        c.weighty = 1;
        c.weightx = 0;
        c.gridx = 1;
        c.gridy = 1;
        c.anchor = GridBagConstraints.WEST;
        c.insets = new Insets(2,5,2,5);
        gridBag.setConstraints(Label01, c);
        centerPanel.add(Label01);

        c.gridwidth = 1;
        c.gridx = 1;
        c.gridy = 1;
        gridBaga.setConstraints(Label02, c);
        aPanel.add(Label02);
        c.gridy = 2;
        gridBaga.setConstraints(Label03, c);
        aPanel.add(Label03);
        c.gridy = 3;
        gridBaga.setConstraints(Label04, c);
        aPanel.add(Label04);

        c.fill = GridBagConstraints.HORIZONTAL;
        c.gridwidth = GridBagConstraints.REMAINDER;
        c.gridx = 2;
        c.gridy = 1;
        gridBaga.setConstraints(tfName, c);
        aPanel.add(tfName);
        c.gridy = 2;
        gridBaga.setConstraints(tfAddrs, c);
        aPanel.add(tfAddrs);
        c.gridy = 3;
        gridBaga.setConstraints(tfProject, c);
        aPanel.add(tfProject);

        c.gridx = 1;
        c.gridy = 2;
        gridBag.setConstraints(aPanel, c);
        centerPanel.add(aPanel);

        getContentPane().add("Center", centerPanel);
        getContentPane().add("South", tempButt);

        setVisible(true);
    }

    public void postVisibleInitialization(){
        tfName.requestFocus();
    }

    public boolean ok() {
        return(results);
    }

    public String getRegInfo() {
        return(reginfo);
    }

    public void save() {
        try {
            ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(regFile));
            reginfo = fName+":"+fAddrs+":"+fProject;
            oos.writeObject(reginfo);
            oos.close();
        }catch (IOException ioe) {
            System.out.println("error saving " + regFile+"   ");
            ioe.printStackTrace(System.err);
        }
    }
}
