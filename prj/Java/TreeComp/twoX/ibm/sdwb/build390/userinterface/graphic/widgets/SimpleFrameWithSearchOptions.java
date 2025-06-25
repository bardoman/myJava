
package com.ibm.sdwb.build390.userinterface.graphic.widgets;
/*******************************************************************************/
/* SimpleFrameWithSearchOptions class for Build390 java client                 */  
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

public  class SimpleFrameWithSearchOptions extends MBModalStatusFrame {

    private JComponent contentPane = null;
    private Action helpAction =null;
    private JPanel optionsPanel;


    public  SimpleFrameWithSearchOptions(MBInternalFrame parentFrame,LogEventProcessor lep) throws MBBuildException {
        super("default simple frame ",parentFrame,lep);

        if (optionsPanel==null) {
            optionsPanel = createOptionsPanel();
        }
    }


    /** A setter to set the content panel, and the actions 
     */ 
    public  final void setPanelAndActions(JComponent contentPane,Vector actions,Action helpAction) {
        this.contentPane = contentPane;
        this.helpAction= helpAction;
        layoutDialog(actions);
    }

    /** We dont wanna expose this outside, since the calling class is going to feed in this pane.
    */
    protected JComponent getContentPanel(){
        return contentPane;
    }

    /** Any sub class can plug in its own Options panel 
     */
    protected JPanel createOptionsPanel(){
        return new DefaultOptionsPanel();
    }

    public UserSelectionOptions getOptions(){
        return((UserSelectedOptionsListener)optionsPanel).getOptions();
    }

    private void layoutDialog(Vector actions) {

        JPanel centerPanel = new JPanel();
        getContentPane().add("Center", centerPanel);
        centerPanel.setBorder(BorderFactory.createTitledBorder(LineBorder.createGrayLineBorder() ,"Find",TitledBorder.DEFAULT_JUSTIFICATION,TitledBorder.DEFAULT_POSITION,null,MBGuiConstants.ColorGroupHeading));

        JButton btHelp = null;

        /**if help action is not null, add it */
        btHelp =  helpAction !=null  ? btHelp = new JButton(helpAction) : null;

        addButtonPanel(btHelp, actions);


        GridBagLayout gridBag = new GridBagLayout();
        centerPanel.setLayout(gridBag);
        GridBagConstraints c = new GridBagConstraints();
        c.gridx = 0;
        c.gridy = 0;
        c.insets = new Insets(1,1,1,1);
        c.gridwidth = GridBagConstraints.REMAINDER;
        c.weightx = 1;
        c.fill = GridBagConstraints.HORIZONTAL;
        gridBag.setConstraints(getContentPanel(), c);
        centerPanel.add(getContentPanel());


        c.gridx = 0;
        c.gridy = 1;
        c.insets = new Insets(1,1,1,1);
        c.fill = GridBagConstraints.HORIZONTAL;
        c.gridwidth = GridBagConstraints.REMAINDER;
        c.weightx = 1;
        gridBag.setConstraints(optionsPanel, c);
        centerPanel.add(optionsPanel);

    }


    private class DefaultOptionsPanel extends JPanel  implements UserSelectedOptionsListener {

        /* private JCheckBox   listAllOccurrences           = new JCheckBox("List all occurrences"); */
        private JCheckBox   regularExpression            = new JCheckBox("Regular expression (Unix)");
        private UserSelectionOptions options;

        private DefaultOptionsPanel(){
            options = new UserSelectionOptions();
            layoutDialog();
        }

        private void layoutDialog(){
            GridBagLayout gridBag = new GridBagLayout();
            setLayout(gridBag);

            setBorder(BorderFactory.createTitledBorder(LineBorder.createGrayLineBorder() ,"Options:",TitledBorder.DEFAULT_JUSTIFICATION,TitledBorder.DEFAULT_POSITION,null,MBGuiConstants.ColorGroupHeading));
            GridBagConstraints c = new GridBagConstraints();

            regularExpression.setSelected(false);
            c.gridx = 0;
            c.gridy = 0;
            c.insets = new Insets(1,1,1,1);
            c.weightx = 1;
            c.fill = GridBagConstraints.HORIZONTAL;
            c.gridwidth=GridBagConstraints.REMAINDER;
            gridBag.setConstraints(regularExpression, c);
            add(regularExpression);

            /*  c.gridx = 0;
              c.gridy = 1;
              c.insets = new Insets(1,1,1,1);
              c.weightx = 1;
              c.fill = GridBagConstraints.HORIZONTAL;
              c.gridwidth=GridBagConstraints.REMAINDER;
              gridBag.setConstraints(listAllOccurrences, c);
              add(listAllOccurrences);
              */
        }

        public UserSelectionOptions getOptions(){
            options.setRegularExpression(regularExpression.isSelected());
            /*    options.setListAllOccurrences(listAllOccurrences()); */
            options.setListAllOccurrences(true); 
            return options;

        }

    }


    public void attachMenuItemAt(int menuAt,int menuItemAt, Action action,boolean appendSeparator){
        getJMenuBar().getMenu(menuAt).insert(action,menuItemAt);
        if (appendSeparator) {
            getJMenuBar().getMenu(menuAt).insertSeparator(menuItemAt +1);;
        }

    }



}

