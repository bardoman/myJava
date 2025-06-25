package com.ibm.sdwb.build390;
/*********************************************************************/
/* Java ComboSelectionDialog class for the Build/390 client                          */
/*  Asks the user for a String and returns it to the caller        */
/*********************************************************************/
// 05/17/99 FixMinSize      Set minimum window size
//
import java.awt.*;
import java.awt.event.*;
import java.util.*;
import javax.swing.*;

/** <br>The ComboSelectionDialog class displays an entry field for the user to enter the password into.
* it then sets the passowrd in MBClient. */
public class ComboSelectionDialog extends MBModalFrame{

   private int userEntry = -1;
   private JComboBox combo1 = new JComboBox();
   private JLabel userLabel;
   private JButton   MBC_Lbu_ok_      = new JButton("OK");
   private JButton   MBC_Lbu_quit_    = new JButton("Cancel");
   private ActionListener qh;
   private ActionListener selh ;
   private MBButtonPanel buttonPanel;

    /** Constructor - Builds the frame and populates it with the entry field and buttons.
    * It also adds the action listeners.
    */
    public ComboSelectionDialog(String question, int bottomNumber, int topNumber, JInternalFrame parentFrame) {
        super("User prompt", parentFrame, null);
        userLabel = new JLabel(question);
		for (int i = bottomNumber; i <= topNumber; i++) {
			combo1.addItem(Integer.toString(i));
		}
        GridBagLayout gridBag = new GridBagLayout();
        GridBagConstraints c = new GridBagConstraints();
        c.gridx = 1;
        c.gridy = 1;
        getContentPane().setLayout(gridBag);

        c.insets = new Insets(5,5,5,5);
        c.gridwidth = GridBagConstraints.REMAINDER;
        c.weightx = 1;
        c.fill = GridBagConstraints.HORIZONTAL;
        gridBag.setConstraints(userLabel, c);
        getContentPane().add(userLabel);
        c.gridy = 2;
        c.insets = new Insets(5,5,10,5);
        c.gridwidth = GridBagConstraints.REMAINDER;
        c.weightx = 1;
        c.fill = GridBagConstraints.HORIZONTAL;
        gridBag.setConstraints(combo1, c);
        getContentPane().add(combo1);
        MBC_Lbu_ok_.setForeground(MBGuiConstants.ColorActionButton);
        MBC_Lbu_quit_.setForeground(MBGuiConstants.ColorCancelButton);
        Vector actionButtons = new Vector();
        actionButtons.addElement(MBC_Lbu_ok_);
        buttonPanel = new MBButtonPanel(null, MBC_Lbu_quit_, actionButtons);
        c.gridy = 3;
        c.insets = new Insets(0,0,0,0);
        gridBag.setConstraints(buttonPanel, c);
        getContentPane().add(buttonPanel);
        setResizable(false); // FixMinSize

        // OK button
        MBC_Lbu_ok_.addActionListener(selh = new ActionListener () {
            public void actionPerformed(ActionEvent evt) {
                doOk();
            }
        });
        // Quit button
        MBC_Lbu_quit_.addActionListener(qh = new ActionListener () {
            public void actionPerformed(ActionEvent evt) {
                dispose();
            }
        });

        setVisible(true);
    }

    public void postVisibleInitialization(){
        combo1.requestFocus();
    }

    public void doOk (){
         userEntry = Integer.parseInt((String) combo1.getSelectedItem());          // get the text from the pw field
		 dispose();
    }

    public int getNumber(){
        return userEntry;
    }
}
