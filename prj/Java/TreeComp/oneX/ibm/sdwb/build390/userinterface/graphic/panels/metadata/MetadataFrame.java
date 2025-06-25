package com.ibm.sdwb.build390.userinterface.graphic.panels.metadata;

/*********************************************************************/
//07/29/2004 INT1661 birth of the class. (activate undo).
/*********************************************************************/

import javax.swing.*;
import javax.swing.event.*;
import javax.swing.border.*;
import java.awt.*;
import java.awt.event.*;
import java.util.*;
import com.ibm.sdwb.build390.*;
import com.ibm.sdwb.build390.process.library.metadata.*;
import com.ibm.sdwb.build390.process.steps.*;
import com.ibm.sdwb.build390.help.*;

public class MetadataFrame extends MBInternalFrame {

    private JButton btHelp = new JButton("Help");
    private JButton btViewMetadata      = null;
    private JButton btViewModelMetadata = null;
    private JButton btEdit = null;
    private EditAction editAction = null;
    private ViewMetadataAction viewMetadataAction = null;
    private ViewModelMetadataAction viewModelMetadataAction = null;

    JTextField modelcmvcPartNameTextField    = new JTextField(15);
    JLabel     modelcmvcPartNameLabel    = new JLabel("Enter the full library model pathname  ?");



    public MetadataFrame() throws MBBuildException {
        super("Metadata Frame", true, null);
        editAction = new EditAction();
        viewMetadataAction = new ViewMetadataAction();
        viewModelMetadataAction = new ViewModelMetadataAction();
        btEdit = new JButton(editAction);
        Vector actionButtons = new Vector();
        actionButtons.addElement(btEdit);
        addButtonPanel(btHelp, actionButtons);


        btHelp.addActionListener(new ActionListener() {
                                     public void actionPerformed(ActionEvent evt) {
//MBUtilities.ShowHelp("HDRPTF",HelpTopicID.MULTIPLEPTFFRAME_HELP);
                                     }

                                 } );

        getContentPane().add(setupUI(), "Center");
        setVisible(true);
    }



    private JPanel setupUI(){
        JPanel leftPanel = new JPanel();
        GridBagLayout gridBag = new GridBagLayout();
        leftPanel.setLayout(gridBag);


        leftPanel.setBorder(BorderFactory.createTitledBorder(LineBorder.createGrayLineBorder() ,"Metadata user selections ",TitledBorder.DEFAULT_JUSTIFICATION,TitledBorder.DEFAULT_POSITION,null,MBGuiConstants.ColorGroupHeading)); 


        JLabel     cmvcPartNameLabel         = new JLabel("Enter the full library pathname (metadata part) ?");
        JTextField cmvcPartNameTextField     = new JTextField(15);
        JCheckBox  modelcmvcPartNameCheckBox = new JCheckBox("Model metadata from " + modelcmvcPartNameTextField.getText());

        btViewMetadata      = new JButton(viewMetadataAction);
        btViewModelMetadata = new JButton(viewModelMetadataAction);

        GridBagConstraints c = new GridBagConstraints();
        c.gridx = 1;
        c.gridy = 1;
        c.weighty = 0;
        c.insets = new Insets(2,5,2,5);
        c.anchor = GridBagConstraints.WEST;
        gridBag.setConstraints(cmvcPartNameLabel, c);
        leftPanel.add(cmvcPartNameLabel);
        c.gridy = 2;
        gridBag.setConstraints(modelcmvcPartNameLabel, c);
        leftPanel.add(modelcmvcPartNameLabel);
        c.gridy = 3;
        c.gridwidth =2; 
        gridBag.setConstraints(modelcmvcPartNameCheckBox, c);
        leftPanel.add(modelcmvcPartNameCheckBox);

        c.gridx=2;
        c.gridy=1;
        c.gridwidth = 1;
        gridBag.setConstraints(cmvcPartNameTextField, c);
        leftPanel.add(cmvcPartNameTextField);
        c.gridy = 2;
        gridBag.setConstraints(modelcmvcPartNameTextField, c);
        leftPanel.add(modelcmvcPartNameTextField);


        c.gridx=3;
        c.gridy=1;
        c.gridwidth = 1;
        c.fill =GridBagConstraints.REMAINDER;
        gridBag.setConstraints(btViewMetadata, c);
        leftPanel.add(btViewMetadata);
        c.gridy = 2;
        gridBag.setConstraints(btViewModelMetadata, c);
        leftPanel.add(btViewModelMetadata,c);


        modelcmvcPartNameCheckBox.addItemListener(new ItemListener(){
                                                      /**
                                                       * Invoked when an item has been selected or deselected by the user.
                                                       * The code written for this method performs the operations
                                                       * that need to occur when an item is selected (or deselected).
                                                       */
                                                      public void itemStateChanged(ItemEvent e) {
                                                          if (e.getStateChange()==ItemEvent.SELECTED) {
                                                              setEnableModelPartName(true);
                                                          } else {
                                                              setEnableModelPartName(false);
                                                          }
                                                      }
                                                  });
        setEnableModelPartName(false);
        return leftPanel;
    }




    public void dispose(){
        super.dispose();
    }


    /* public Dimension getMinimumSize() {
         return new Dimension(300, 200);
     }
     */

    void setEnableModelPartName(boolean isEnable){
        modelcmvcPartNameTextField.setEnabled(isEnable);
        modelcmvcPartNameLabel.setEnabled(isEnable);
        viewModelMetadataAction.setEnabled(isEnable);


    }



    class EditAction extends com.ibm.sdwb.build390.userinterface.graphic.widgets.CancelableAction {

        EditAction(){
            super("OK");
        }

        public void doAction(ActionEvent e) {

        }

        public void stop(){

        }
    }


    class ViewMetadataAction extends com.ibm.sdwb.build390.userinterface.graphic.widgets.CancelableAction {

        ViewMetadataAction(){
            super("View Metadata");
        }

        public void doAction(ActionEvent e) {

        }

        public void stop(){

        }
    }


    class ViewModelMetadataAction extends com.ibm.sdwb.build390.userinterface.graphic.widgets.CancelableAction {

        ViewModelMetadataAction(){
            super("View model Metadata");
        }

        public void doAction(ActionEvent e) {

        }

        public void stop(){

        }
    }

}


