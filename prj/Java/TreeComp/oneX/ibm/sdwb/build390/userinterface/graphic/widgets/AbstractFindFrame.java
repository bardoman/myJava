
package com.ibm.sdwb.build390.userinterface.graphic.widgets;
/*******************************************************************************/
/* FindFrame class for Build390 java client                                    */  
/* This class  is used in searching of some matching criteria                  */
/*******************************************************************************/
//02/11/2005 SDWB2398  Replace metadata in cmvc only(phase 1)
/*******************************************************************************/

import java.awt.*;
import java.awt.event.*;
import java.util.*;

import javax.swing.*;
import javax.swing.border.*;

import com.ibm.sdwb.build390.*;
import com.ibm.sdwb.build390.logprocess.*;
import com.ibm.sdwb.build390.help.*;
import com.ibm.sdwb.build390.library.cmvc.metadata.userinterface.*;

public abstract class AbstractFindFrame extends MBModalStatusFrame {

    private JButton  btFindNext                      = new JButton("Find Next");
    private JButton  btHelp                          = new JButton("HELP");

    private JLabel      searchLabel                  = new JLabel   ("Find  for     :");
    /* private JCheckBox   listAllOccurrences           = new JCheckBox("List all occurrences"); */
    private JCheckBox   regularExpression            = new JCheckBox("Regular expression (Unix)");

    private JComponent findPanel = null;
    private UserSelection selection ;


    public  AbstractFindFrame(String title,MBInternalFrame parentFrame,LogEventProcessor lep) throws MBBuildException {
        super(title,parentFrame,lep);
        selection = new UserSelection();
    }


    public  final void setFinderUI(JComponent findPanel){
        this.findPanel = findPanel;
    }

    public JComponent getFinderUI(){
        return findPanel;
    }

    /*override in subclasses to point to the right link */
    public void helpAction() {
        MBUtilities.ShowHelp("",HelpTopicID.TABLE_OF_CONTENTS);

    }

    public String getSyntaxHelp(){
        return("EXAMPLES\n" +
               "SOTYPE EQ PLX - search for SOTYPE  equals PLX \n"+ 
               "      Note that PLX is treated as string literal if regex is unchecked.\n"+ 
               "           If Regular Expressions(Unix) is checked, then PLX is treated as a regex.\n"+ 
               "           The following example strings match if PLX is a regex.\n"+
               "           PLX, PLXD, APLX, ABCKAPLXD, APALXOPLX.\n"+
               "SOTYPE EQ  P.* - search for SOTYPE equals P followed by anything \n"+ 
               "      Note that the dot matches any character, and the star allows the dot to be\n"+ 
               "           repeated any number of times, including zero.\n"+
               "CPARM EQ 'BUF(SIZE)'. with  regex unchecked.\n"+
               "      Note that 'BUF(SIZE)' is treated as a string literal\n"+
               "           if Regular Expressions(Unix) is unchecked.\n"+
               "CPARM EQ P.*. with regex  unchecked.\n"+
               "      An exact match for string literals P.* is performed.\n"+
               "Note \n"+
               "Greedy Quantifiers\n"+
               "      X?  	X, once or not at all.\n"+
               "      X* 	X, zero or more times.\n"+
               "      X+ 	X, one or more times.\n"+
               "An auto coversion of  .* to  *  happens, when a match is queried in host.\n"+
               "So SOTYPE EQ P.* (with regex checked)  is interpreted as \n"+
               "   P.* in cmvc as match for character P followed by  any character\n"+
               "       any number of times.\n"+
               "   P.* in host undergoes an auto converstion to P* \n"+
               "       match for character P followed by  any character\n"+
               "       any number of times.\n"+
               "In case of regex just having * would result in an  invalid condition.\n"+
               "      and a  host error. eg: SOTYPE EQ *.\n"+
               "In case of regex just having X* would result in invalid search hits in cmvc.\n"+
               "      eg: SOTYPE P*.\n"+
               "      The following example strings match for pattern P* \n"+
               "      PLX PL AP AL ABXD APX AXP AXOP BBBB \n"+
               "Limitation! \n"+
               "Wild card [AlphaNumeric].* is the only supported option for filtering  requests (Host & Cmvc) \n"+
               "Example(for filtering option (Host & Cmvc) PL.*, AO.*, A1.* \n\n" +
               "Refer Client Users Guide for more details.\n"+
               "      For info about regex http://www.regular-expressions.info/");
    }

    public abstract void doFindAction();

    public void display() throws MBBuildException {

        JPanel centerPanel = new JPanel();
        getContentPane().add("Center", centerPanel);
        centerPanel.setBorder(BorderFactory.createTitledBorder(LineBorder.createGrayLineBorder() ,"Find",TitledBorder.DEFAULT_JUSTIFICATION,TitledBorder.DEFAULT_POSITION,null,MBGuiConstants.ColorGroupHeading));
        btHelp.setForeground(MBGuiConstants.ColorHelpButton);

        /*       listAllOccurrences.setSelected(true);
               listAllOccurrences.setEnabled(false);
               */

        regularExpression.setSelected(false);

        btHelp.addActionListener(new ActionListener() {
                                     public void actionPerformed(ActionEvent evt) {
                                         helpAction();
                                     }
                                 } );

        btFindNext.setForeground(MBGuiConstants.ColorActionButton);


        btFindNext.addActionListener(new ActionListener() {
                                         public void actionPerformed(java.awt.event.ActionEvent A) {
                                             doFindAction();
                                         }
                                     });

        Vector  actionButtons  = new Vector();
        actionButtons.addElement(btFindNext);
        addButtonPanel(btHelp, actionButtons);


        GridBagLayout gridBag = new GridBagLayout();
        centerPanel.setLayout(gridBag);
        GridBagConstraints c = new GridBagConstraints();
        c.gridx = 0;
        c.gridy = 0;
        c.insets = new Insets(1,1,1,1);
        c.weightx = 1;
        c.gridwidth = GridBagConstraints.RELATIVE;
        c.fill = GridBagConstraints.HORIZONTAL;
        gridBag.setConstraints(searchLabel, c);
        centerPanel.add(searchLabel);


        c.gridx = 1;
        c.gridy = 0;
        c.insets = new Insets(1,1,1,1);
        c.gridwidth = GridBagConstraints.REMAINDER;
        c.weightx = 1;
        c.fill = GridBagConstraints.HORIZONTAL;
        gridBag.setConstraints(findPanel, c);
        centerPanel.add(findPanel);


        JPanel optionsPanel = new JPanel();
        optionsPanel.setBorder(BorderFactory.createTitledBorder(LineBorder.createGrayLineBorder() ,"Options:",TitledBorder.DEFAULT_JUSTIFICATION,TitledBorder.DEFAULT_POSITION,null,MBGuiConstants.ColorGroupHeading));
        GridBagLayout gridBagO = new GridBagLayout();
        optionsPanel.setLayout(gridBagO);
        GridBagConstraints co = new GridBagConstraints();

        co.gridx = 0;
        co.gridy = 0;
        co.insets = new Insets(1,1,1,1);
        co.weightx = 1;
        co.fill = GridBagConstraints.HORIZONTAL;
        co.gridwidth=GridBagConstraints.REMAINDER;
        gridBagO.setConstraints(regularExpression, co);
        optionsPanel.add(regularExpression);

        /*  co.gridx = 0;
          co.gridy = 1;
          co.insets = new Insets(1,1,1,1);
          co.weightx = 1;
          co.fill = GridBagConstraints.HORIZONTAL;
          co.gridwidth=GridBagConstraints.REMAINDER;
          gridBagO.setConstraints(listAllOccurrences, co);
          optionsPanel.add(listAllOccurrences);
          */


        c.gridx = 0;
        c.gridy = 1;
        c.insets = new Insets(1,1,1,1);
        c.fill = GridBagConstraints.HORIZONTAL;
        c.gridwidth = GridBagConstraints.REMAINDER;
        c.weightx = 1;
        gridBag.setConstraints(optionsPanel, c);
        centerPanel.add(optionsPanel);
        setVisible(true);

    }

    public void attachMenuItemAt(int menuAt,int menuItemAt, Action action,boolean appendSeparator){
        getJMenuBar().getMenu(menuAt).insert(action,menuItemAt);
        if (appendSeparator) {
            getJMenuBar().getMenu(menuAt).insertSeparator(menuItemAt +1);;
        }

    }

    public boolean listAllOccurrences(){
        /*    return listAllOccurrences.isSelected(); */
        return true;
    }

    public boolean isUseRegularExpressions(){
        return regularExpression.isSelected();
    }

    public UserSelection getSelection(){
        return selection;

    }


}

