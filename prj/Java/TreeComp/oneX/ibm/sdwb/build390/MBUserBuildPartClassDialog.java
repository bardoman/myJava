package com.ibm.sdwb.build390;
/*********************************************************************/
/*                                                                */
/*  Creates a dialogbox for partclass and manages the userBuild Page                        */
/*********************************************************************/
// Changes
// Date     Defect/Feature      Reason
// 05/19/2000                   This is a new dialog box which pops up when the user selects localfiles for metadata,
//                              The user selects the part name and then press option metadataedit.
//                              So this dialog box is poped up asking the user to enter the partclass
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
public class MBUserBuildPartClassDialog extends MBModalFrame implements Serializable {
    private MBBuild     build   = null;
    private Hashtable   cmdHash = new Hashtable();
    private JLabel Label01      = new JLabel("Indicate the Part class for the  part Selected");
    private JButton btHelp      = new JButton("Help");
    private JButton btOk        = new JButton("Ok");
    private JLabel localPartLabel    = new JLabel("Part Name");
    private JLabel partClassLabel   = new JLabel(" Part Class  ");
    private DefaultListModel partNameListModel  = new DefaultListModel();
    private JTextField partName  = new JTextField("",20);
    private JTextField partClassName = new JTextField("", 8);
    private JCheckBox  classCheckBox  = new JCheckBox("Use this class name for all parts selected");
    private String tempPartName = null;
    private boolean isValidate = true;
    private boolean sameClassForAllParts = false;
    private String mclass = new String("");

    private MBButtonPanel tempButt;
    private GridBagLayout gridBag = new GridBagLayout();
    private JPanel centerPanel  = new JPanel(gridBag);
    private JPanel rpanel = new JPanel();
    private JPanel bpanel; // = new JPanel();
    private JPanel classPanel  = new JPanel(gridBag);
    private JPanel checkPanel  = new JPanel();

    /**
    * constructor - Create a MBUserBuildPartTypeDialog
    */
    public MBUserBuildPartClassDialog(MBBuild bld, String partName1, MBInternalFrame pFrame) throws com.ibm.sdwb.build390.MBBuildException{
        super("Part Class", pFrame, null);
        tempPartName= partName1;
        build = bld;
        initializeDialog(build);
    }


    public void dispose() {
        new Thread(new Runnable() {
                       public void run() {
                           if(!mclass.equals(""));
                           MBMsgBox   question=  new MBMsgBox("EXIT","Do you want to Exit", null, true);
                           if(question.isAnswerYes()) {
                               isValidate = false;
                               JustDisposeFrame();
                           }


                       }
                   }).start();
    }
    private void JustDisposeFrame() {
        super.dispose();
    }

    public boolean isSameClassForAllParts() {
        return sameClassForAllParts;
    }

    public boolean getValidate() {
        return isValidate;
    }

    public String getmclass() {
        return mclass;
    }

    //Begin TST2132
    public String getPartName() {
        return partName.getText().trim();
    }
    //End TST2132

    public void initializeDialog(MBBuild tempBuildParm) throws com.ibm.sdwb.build390.MBBuildException {
        build   = tempBuildParm;
        partName.setText(tempPartName);


        GridBagLayout gridBag = new GridBagLayout();
        bpanel = new JPanel(gridBag);
        setForeground(MBGuiConstants.ColorRegularText);
        setBackground(MBGuiConstants.ColorGeneralBackground);


        // help button
        btHelp.setForeground(MBGuiConstants.ColorHelpButton);
        btHelp.addActionListener(new ActionListener() {
                                     public void actionPerformed(ActionEvent evt) {
                                         try {
                                             //TST2180
                                             MBUtilities.ShowHelp("HDRUB",HelpTopicID.LOCALPARTSUSERBUILDPAGE_HELP);
                                         }
                                         finally {
                                         }
                                     }
                                 } );

        classCheckBox.addItemListener(new ItemListener() {
                                          public void itemStateChanged(ItemEvent e) {
                                              sameClassForAllParts=classCheckBox.isSelected();
                                          }
                                      });

        // OK button
        btOk.setForeground(MBGuiConstants.ColorActionButton);
        btOk.addActionListener(new ActionListener() {
                                   public void actionPerformed(ActionEvent evt) {
                                       if(partName.getText() != null) {
                                           if(partName.getText().length() > 0) {
                                               if(partClassName.getText()!= null) {

                                                   if(partClassName.getText().length() > 0) {
                                                       partClassName.getText().toUpperCase();
                                                       if(partClassName.getText().length() > 8);
                                                       mclass = partClassName.getText().trim().toUpperCase();
                                                       if(!mclass.equals("")) {
                                                           cmdHash.put("LOCAL", partName);
                                                           cmdHash.put("CLASS", mclass);
                                                           JustDisposeFrame();

                                                       }
                                                   }
                                               }
                                           }
                                       }
                                   }
                               });

        // build dialog
        Vector actionButtons = new Vector();
        actionButtons.addElement(btOk);
        tempButt = new MBButtonPanel(btHelp,null,actionButtons);
        Label01.setForeground(MBGuiConstants.ColorGroupHeading);
        GridBagConstraints c = new GridBagConstraints();
        c.weighty = 1;
        c.weightx = 0;
        c.gridx = 1;
        c.gridy = 1;
        c.anchor = GridBagConstraints.WEST;
        c.insets = new Insets(5,5,5,5);
        gridBag.setConstraints(Label01, c);
        bpanel.add(Label01);
        c.gridwidth = 1;
        c.fill = GridBagConstraints.NONE;

        c.gridy = 1;
        gridBag.setConstraints(localPartLabel, c);
        rpanel.add(localPartLabel);
        gridBag.setConstraints(partName, c);
        rpanel.add(partName);
        c.gridy = 2;
        gridBag.setConstraints(rpanel, c);
        bpanel.add(rpanel);

        c.gridy = 1;
        gridBag.setConstraints(partClassLabel, c);
        classPanel.add(partClassLabel);
        gridBag.setConstraints(partClassName, c);
        classPanel.add(partClassName);
        c.gridy = 3;
        gridBag.setConstraints(classPanel, c);
        bpanel.add(classPanel);


        //c.gridy = 1;
        c.gridy=4;
        gridBag.setConstraints(classCheckBox, c);
        //checkPanel.add(classCheckBox);
        bpanel.add(classCheckBox);
        //bpanel.add(checkPanel);


        centerPanel.add(bpanel);
        getContentPane().add("Center", centerPanel);
        getContentPane().add("South", tempButt);

        setVisible(true);
    }

    public void postVisibleInitialization() {
        partClassName.requestFocus();
    }
}

