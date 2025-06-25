
package com.ibm.sdwb.build390;
/*******************************************************************************/
/* This class  is used in part searching of some matching criteria             */
/*******************************************************************************/
// changes
//Date              Defect/Feature        Reason
//05/10/2000        EKM001 part searcher  Birth of the Class
//06/13/2000          HELP_ACT_LISTNR                provided help for find function in the log retrieve page.
//12/03/2002 SDWB-2019 Enhance the help system
/*******************************************************************************/
import java.awt.GridBagLayout;
import java.awt.Color;
import java.awt.GridBagConstraints;

import java.util.HashMap;
import java.util.Vector;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JTextField;
import javax.swing.ButtonGroup;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.border.Border;
import javax.swing.BorderFactory;
import javax.swing.border.TitledBorder;
import javax.swing.border.LineBorder;
import com.ibm.sdwb.build390.logprocess.*;
import com.ibm.sdwb.build390.help.*;

public class MBLogRetrievePartFinder extends MBModalFrame {
    private JButton  btOk         =new JButton(" OK");
    private JButton  btNo         =new JButton(" Cancel Find");
    private JButton  btHelp       =new JButton("HELP");
    private JTextField  searchPartNameText        = new JTextField("",8);
    private JTextField  searchPartClassText       = new JTextField("",8);
    private JRadioButton   rebuildRadioButton     = new JRadioButton("Rebuild Display ");
    private JRadioButton   firstSearchRadioButton = new JRadioButton("Select First Find Hit ");
    private JLabel searchLabel2 =  new JLabel("PartClass");

    private MBButtonPanel  btnPanel;
    private JPanel   centerPanel  =new JPanel();
    private ButtonGroup bgroup    =null;
    private JPanel radioPanel     =new JPanel();
    private HashMap storedStrsHashMap=null;
    private static final String FINDPARTNAME       = "FINDPARTNAME";
    private static final String FINDPARTCLASS      = "FINDPARTCLASS";
    private static final String FINDDISPLAYOPTIONS = "FINDDISPLAYOPTIONS";


    private JLabel searchLabel1  = new JLabel();
    private static final String FINDDRIVERNAME       = "FINDDRIVERNAME";
    private boolean isDriverSearch = false;
    public  MBLogRetrievePartFinder(MBInternalFrame pFrame,LogEventProcessor lep) throws com.ibm.sdwb.build390.MBBuildException {
        super("Find", pFrame, lep);
        isDriverSearch=false;
        initializeDialog();
    }

    public  MBLogRetrievePartFinder(MBInternalFrame pFrame,LogEventProcessor lep,boolean isDriverSearch) throws com.ibm.sdwb.build390.MBBuildException {
        super("Find", pFrame, lep);
        this.isDriverSearch=isDriverSearch;
        initializeDialog();
    }
    public void initializeDialog() throws com.ibm.sdwb.build390.MBBuildException {
        setForeground(MBGuiConstants.ColorRegularText);
        setBackground(MBGuiConstants.ColorGeneralBackground);
        btHelp.setForeground(MBGuiConstants.ColorHelpButton);
        btOk.setForeground(MBGuiConstants.ColorActionButton);

        Border blackline = BorderFactory.createLineBorder(MBGuiConstants.ColorRegularText);
        TitledBorder tld1 = new TitledBorder(blackline,"Find");
        TitledBorder tld2 = new TitledBorder(blackline,"Display Options");
        tld1.setTitleColor(MBGuiConstants.ColorGroupHeading);
        tld2.setTitleColor(Color.red);
        centerPanel.setBorder(tld1);
        radioPanel.setBorder(tld2);

        GridBagLayout gridBag = new GridBagLayout();
        centerPanel.setLayout(gridBag);
        GridBagConstraints c = new GridBagConstraints();
        c.gridx = 1;
        c.gridy = 1;
        c.insets = new Insets(1,1,1,1);
        c.weightx = 1;
        c.fill = GridBagConstraints.HORIZONTAL;
        gridBag.setConstraints(searchLabel1, c);
        if (isDriverSearch) {
            searchLabel1.setText("Driver Name");
        } else {
            searchLabel1.setText("PartName");
        }
        centerPanel.add(searchLabel1);

        c.gridx = 2;
        c.gridy = 1;
        c.insets = new Insets(1,1,1,1);
        c.gridwidth = GridBagConstraints.REMAINDER;
        c.weightx = 1;
        c.fill = GridBagConstraints.HORIZONTAL;
        gridBag.setConstraints(searchPartNameText, c);
        centerPanel.add(searchPartNameText);

        if (!isDriverSearch) {
            c.gridx = 1;
            c.gridy = 2;
            c.insets = new Insets(1,1,1,1);
            c.weightx = 1;
            c.fill = GridBagConstraints.HORIZONTAL;
            gridBag.setConstraints(searchLabel2, c);
            centerPanel.add(searchLabel2);


            c.gridx = 2;
            c.gridy = 2;
            c.insets = new Insets(1,1,1,1);
            c.gridwidth = GridBagConstraints.REMAINDER;
            c.weightx = 1;
            c.fill = GridBagConstraints.HORIZONTAL;
            gridBag.setConstraints(searchPartClassText, c);
            centerPanel.add(searchPartClassText);
        }

        GridBagLayout gridBagr = new GridBagLayout();
        radioPanel.setLayout(gridBagr);
        GridBagConstraints cr = new GridBagConstraints();
        bgroup = new ButtonGroup();
        bgroup.add(firstSearchRadioButton);
        cr.gridx = 1;
        cr.gridy = 1;
        cr.insets = new Insets(1,1,1,1);
        cr.gridwidth = GridBagConstraints.REMAINDER;
        cr.weightx = 1;
        cr.fill = GridBagConstraints.HORIZONTAL;
        gridBagr.setConstraints(firstSearchRadioButton, cr);
        radioPanel.add(firstSearchRadioButton);

        bgroup.add(rebuildRadioButton);
        cr.gridx = 1;
        cr.gridy = 2;
        cr.insets = new Insets(1,1,1,1);
        cr.gridwidth = GridBagConstraints.REMAINDER;
        cr.weightx = 1;
        cr.fill = GridBagConstraints.HORIZONTAL;
        gridBagr.setConstraints(rebuildRadioButton, cr);
        radioPanel.add(rebuildRadioButton);
        if (!isDriverSearch) {
            if (getGeneric(FINDPARTNAME) != null) {
                searchPartNameText.setText((String) getGeneric(FINDPARTNAME));
            }
            if (getGeneric(FINDPARTCLASS) != null) {
                searchPartClassText.setText((String) getGeneric(FINDPARTCLASS));
            }
        } else {
            if (getGeneric(FINDDRIVERNAME) != null) {
                searchPartNameText.setText((String) getGeneric(FINDDRIVERNAME));
            }


        }

        if (getGeneric(FINDDISPLAYOPTIONS)!=null) {
            if (((Boolean)getGeneric(FINDDISPLAYOPTIONS)).booleanValue()) {
                firstSearchRadioButton.setSelected(true);
            } else {
                rebuildRadioButton.setSelected(true);
            }
        } else {
            firstSearchRadioButton.setSelected(true);
        }



        btOk.addActionListener(new ActionListener() {
                                   public void actionPerformed(java.awt.event.ActionEvent A) {
                                       storedStrsHashMap = new  HashMap();
                                       storedStrsHashMap.clear();
                                       String searchPartNameTextStr= new String();
                                       String searchPartClassTextStr= new String();
                                       if (searchPartNameText.getText().trim() != null) {
                                           searchPartNameTextStr  = searchPartNameText.getText().trim();
                                           if (searchPartNameTextStr.length() > 0) {
                                               if (!isDriverSearch) {
                                                   storedStrsHashMap.put("PARTNAME",searchPartNameTextStr.trim().toUpperCase());
                                               } else {
                                                   storedStrsHashMap.put("DRIVERNAME",searchPartNameTextStr.trim().toUpperCase());
                                               }
                                           }
                                       }
                                       if (!isDriverSearch) {
                                           if (searchPartClassText.getText().trim() != null) {
                                               searchPartClassTextStr  = searchPartClassText.getText().trim();
                                               if (searchPartClassTextStr.length() > 0) {
                                                   storedStrsHashMap.put("PARTCLASS",searchPartClassTextStr.trim().toUpperCase());
                                               }
                                           }
                                       }
                                       if (firstSearchRadioButton.isSelected()) {
                                           storedStrsHashMap.put("FIRSTSEARCH",new Boolean(true));
                                           storedStrsHashMap.put("REBUILD",new Boolean(false));
                                       }
                                       if (rebuildRadioButton.isSelected()) {
                                           storedStrsHashMap.put("REBUILD",new Boolean(true));
                                           storedStrsHashMap.put("FIRSTSEARCH",new Boolean(false));
                                       }
                                       if ((searchPartNameTextStr.length() > 0)|(searchPartClassTextStr.length() > 0)) {
                                           dispose();
                                       } else {
                                           new Thread(new Runnable(){
                                                          public void run() {
                                                              storedStrsHashMap.clear();
                                                              new MBMsgBox("Warning","You Must Enter " + (isDriverSearch ? "Driver Name " : " PartName or PartClass or Both" ) + " to Search !!");
                                                          }
                                                      }).start();
                                       }

                                   }
                               });

        btNo.addActionListener(new ActionListener(){
                                   public void actionPerformed(java.awt.event.ActionEvent A) {
                                       dispose();
                                   }
                               });

//06/13/2000  HELP_ACT_LISTNR  for find function in the log retrieve page.
        btHelp.addActionListener(new ActionListener() {
                                     public void actionPerformed(ActionEvent evt) {
                                         if (!isDriverSearch) {
                                             MBUtilities.ShowHelp("SPTLRFND",HelpTopicID.LOGRETRIEVEPARTFINDER_HELP);
                                         } else {
                                             MBUtilities.ShowHelp("HDRDRVFND",HelpTopicID.LOGRETRIEVEPARTFINDER_HELP);
                                         }


                                     }} );


        Vector  actionButtons  = new Vector();
        actionButtons.addElement(btOk);
        actionButtons.addElement(btNo);
        btnPanel = new MBButtonPanel(btHelp, null, actionButtons);

        getContentPane().add("North", centerPanel);
        getContentPane().add("Center",radioPanel);
        getContentPane().add("South", btnPanel);
        setVisible(true);

    }


    HashMap getFindByName() {
        return storedStrsHashMap;
    }

    public void postVisibleInitialization(){
        searchPartNameText.requestFocus();
    }

    public void dispose(){
        if(!isDriverSearch){
        putGeneric(FINDPARTNAME,  searchPartNameText.getText().trim());
        putGeneric(FINDPARTCLASS, searchPartClassText.getText().trim());
        }
        else{
            putGeneric(FINDDRIVERNAME,  searchPartNameText.getText().trim());

        }
        if (firstSearchRadioButton.isSelected()) {
            putGeneric(FINDDISPLAYOPTIONS,new Boolean(true));
        } else {
            putGeneric(FINDDISPLAYOPTIONS,new Boolean(false));
        }
        super.dispose();
    }

}





