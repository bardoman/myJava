package com.ibm.sdwb.build390;
/*********************************************************************/
/* Java MultipleTextEntryDialog class for the Build/390 client                          */
/*  Asks the user for a String and returns it to the caller        */
/*********************************************************************/
// 05/17/99 FixMinSize      Set minimum window size
//
import java.awt.*;
import java.awt.event.*;
import java.util.*;
import javax.swing.*;

/** <br>The MultipleTextEntryDialog class displays an entry field for the user to enter the password into.
* it then sets the passowrd in MBClient. */
public class MultipleTextEntryDialog extends MBModalFrame{

    protected java.util.List userEntry = null;
    private java.util.List initialValues = null;
    protected EditableJList tf1;
    protected JLabel userLabel;
    protected JButton   MBC_Lbu_ok_      = new JButton("OK");
    protected JButton   MBC_Lbu_quit_    = new JButton("Cancel");
    private ActionListener qh;
    private ActionListener selh ;
    private MBButtonPanel buttonPanel;
	private JCheckBox caseBox = new JCheckBox("Convert to uppercase", true);
    protected int maxLength = -1;
    protected boolean allowBlankEntry = true;
	private boolean checkBoxAdded = false;

    /** Constructor - Builds the frame and populates it with the entry field and buttons.
    * It also adds the action listeners.
    */
    public MultipleTextEntryDialog(String question, java.util.List tempInit, Component parentFrame, int maxLen, boolean showCheck) {
        super("User prompt", parentFrame, null);
        maxLength = maxLen;
		checkBoxAdded = showCheck;
        initialValues = tempInit;
        initialize(question);

    }

    private void initialize(String question) {
        userLabel = new JLabel(question);
        JLabel helpLabel = new JLabel("To edit the list of entries, right click in the list area");
        if (maxLength > 0) {
            tf1 = new EditableJList(initialValues, maxLength);
        } else {
            tf1 = new EditableJList(initialValues);
        }
        setForeground(MBGuiConstants.ColorRegularText);
        setBackground(MBGuiConstants.ColorGeneralBackground);
        getContentPane().setLayout(new BorderLayout());

        tf1.setBackground(MBGuiConstants.ColorFieldBackground);
        JPanel txts = new JPanel();
        GridBagLayout gridBag = new GridBagLayout();
        txts.setLayout(gridBag);
        GridBagConstraints c = new GridBagConstraints();
        c.gridy = 1;
        c.gridx = 1;
        c.insets = new Insets(0,0,0,0);
        c.anchor = GridBagConstraints.WEST;
        gridBag.setConstraints(userLabel, c);
        txts.add(userLabel);
        c.gridy = 2;
        gridBag.setConstraints(helpLabel, c);
        txts.add(helpLabel);
        //getContentPane().add("North", userLabel);
        getContentPane().add("North", txts);
        getContentPane().add("Center", new JScrollPane(tf1));
        MBC_Lbu_ok_.setForeground(MBGuiConstants.ColorActionButton);
        MBC_Lbu_quit_.setForeground(MBGuiConstants.ColorCancelButton);
        java.util.List actionButtons = new java.util.ArrayList();
        actionButtons.add(MBC_Lbu_ok_);
        buttonPanel = new MBButtonPanel(null, MBC_Lbu_quit_, actionButtons);
		Box tempBox = Box.createVerticalBox();
		if (checkBoxAdded) {
			tempBox.add(caseBox);
		}
		tempBox.add(buttonPanel);
        getContentPane().add("South", tempBox);
//        setResizable(false); // FixMinSize

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
        tf1.requestFocus();
    }

    public void doOk (){
        userEntry = new java.util.ArrayList();
        for (int i = 0; i < tf1.getModel().getSize(); i++){
			if (checkBoxAdded & caseBox.isSelected()) {
				userEntry.add(tf1.getModel().getElementAt(i).toString().toUpperCase());
			}else {
				userEntry.add(tf1.getModel().getElementAt(i).toString());
			}
        }
        if (!userEntry.isEmpty()  | allowBlankEntry) {
            // clean up and get out
            dispose();
        } else {
            userEntry = null;
        }
    }
/*
    public Dimension getPreferredSize(){
        Dimension oldPref = super.getPreferredSize();
        oldPref.width = 200;;
        return oldPref;
    }
*/
    public java.util.List getEntries(){
        return userEntry;
    }
}
