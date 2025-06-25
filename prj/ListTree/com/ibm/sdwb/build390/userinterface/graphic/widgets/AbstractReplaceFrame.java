package com.ibm.sdwb.build390.userinterface.graphic.widgets;
/*******************************************************************************/
/* ReplaceFrame class for Build390 java client                                 */  
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

public abstract class AbstractReplaceFrame extends MBModalStatusFrame {

    private JButton  btReplace                  = new JButton("Replace");
    private JButton  btHelp                     = new JButton("HELP");
    private JLabel   searchLabel             = new JLabel("Find    for  :");

    private JLabel      replaceLabel            = new JLabel("Replace with :");

    private JComponent replacer =null;
    private JComponent finder = null;

    /* private JCheckBox   replaceAllOccurrences     = new JCheckBox("Replace all occurrences"); */
    private JCheckBox   regularExpression      = new JCheckBox("Regular expression (Unix)");

    private UserSelection selection =null;


    public  AbstractReplaceFrame(String title,MBInternalFrame pFrame,LogEventProcessor lep) throws MBBuildException {
        super(title, pFrame, lep);
        selection = new UserSelection();
    }

    public void  setFinderUI(JComponent finder) {
        this.finder = finder;
    }

    public void setReplacerUI(JComponent replacer){
        this.replacer = replacer;
    }


    public JComponent getFinderUI(){
        return finder;
    }

    public JComponent getReplacerUI(){
        return replacer;
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
               "Example(for filtering option (Host & Cmvc) PL.*, AO.*, A1.* \n"+
               "Refer Client Users Guide for more details.\n"+
               "      For info about regex http://www.regular-expressions.info/");
    }


    public abstract void doReplaceAction();

    public void display() throws MBBuildException {

        JPanel centerPanel = new JPanel();
        getContentPane().add("Center", centerPanel);
        centerPanel.setBorder(BorderFactory.createTitledBorder(LineBorder.createGrayLineBorder() ,"Find",TitledBorder.DEFAULT_JUSTIFICATION,TitledBorder.DEFAULT_POSITION,null,MBGuiConstants.ColorGroupHeading));
        btHelp.setForeground(MBGuiConstants.ColorHelpButton);

        /*    replaceAllOccurrences.setSelected(true);
              replaceAllOccurrences.setEnabled(false);
      
              regularExpression.setSelected(true);
              */


        btHelp.addActionListener(new ActionListener() {
                                     public void actionPerformed(ActionEvent evt) {
                                         helpAction();
                                     }
                                 } );

        btReplace.setForeground(MBGuiConstants.ColorActionButton);
        btReplace.addActionListener(new ActionListener() {
                                        public void actionPerformed(java.awt.event.ActionEvent A) {
                                            doReplaceAction();

                                        }
                                    });

        Vector  actionButtons  = new Vector();
        actionButtons.addElement(btReplace);
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
        gridBag.setConstraints(getFinderUI(), c);
        centerPanel.add(getFinderUI());



        c.gridx = 0;
        c.gridy = 1;
        c.insets = new Insets(1,1,1,1);
        c.weightx = 1;
        c.gridwidth = GridBagConstraints.RELATIVE;
        c.fill = GridBagConstraints.HORIZONTAL;
        gridBag.setConstraints(replaceLabel, c);
        centerPanel.add(replaceLabel);

        c.gridx = 1;
        c.gridy = 1;
        c.insets = new Insets(1,1,1,1);
        c.gridwidth = GridBagConstraints.REMAINDER;
        c.weightx = 1;
        c.fill = GridBagConstraints.HORIZONTAL;
        gridBag.setConstraints(getReplacerUI(), c);
        centerPanel.add(getReplacerUI());


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

        /* co.gridx = 0;
         co.gridy = 1;
         co.insets = new Insets(1,1,1,1);
         co.weightx = 1;
         co.fill = GridBagConstraints.HORIZONTAL;
         co.gridwidth=GridBagConstraints.REMAINDER;
         gridBagO.setConstraints(replaceAllOccurrences, co);
         optionsPanel.add(replaceAllOccurrences);
         */

        c.gridx = 0;
        c.gridy = 2;
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
            getJMenuBar().getMenu(menuAt).insertSeparator(menuItemAt+1);
        }

    }


    public boolean isReplaceAllOccurrences(){
        /*return replaceAllOccurrences.isSelected(); */
        return true;
    }

    public boolean isUseRegularExpressions() {
        return regularExpression.isSelected();

    }

    public UserSelection getSelection(){
        return selection;

    }



}





