package com.ibm.sdwb.build390.userinterface.graphic.widgets;
/*******************************************************************************/
/* This class filters the model of a JComboBox
/*******************************************************************************/
// changes
//Date    Defect/Feature        Reason
//01/09/2001 Create class B.E.
/*******************************************************************************/
import java.awt.*;
import java.awt.event.*;
import java.util.*;
import javax.swing.*;
import com.ibm.sdwb.build390.*;

/** <br>The FilterFrame class displays an entry field for the user to enter the filter criteria.
* it then filters the JComboModel it serves. */
public class FilterFrame extends MBModalFrame {
	private  static boolean isAlive = false;
	private JTextField filterField=new JTextField("*");

	private JLabel userLabel;
	private JButton   MBC_Lbu_ok_      = new JButton("Filter");
	private JButton   MBC_Lbu_quit_    = new JButton("Cancel");
	private ActionListener qh;
	private ActionListener selh ;
	private MBButtonPanel buttonPanel;
	private static Object pwLock = new Object();
	private boolean userCancel = false;
	private RefreshableCombo comboBox;

	/** Constructor - Builds the frame and populates it with the entry field and buttons.
	* It also adds the action listeners.
	*/
	public FilterFrame(RefreshableCombo comboBox) {
		super("Filter", null, null);
		this.comboBox=comboBox;
		userLabel = new JLabel("Enter text for filter");
		setForeground(MBGuiConstants.ColorRegularText);
		setBackground(MBGuiConstants.ColorGeneralBackground);
		GridBagLayout gridBag = new GridBagLayout();
		GridBagConstraints c = new GridBagConstraints();
		c.gridx = 1;
		c.gridy = 1;
		getContentPane().setLayout(gridBag);

		filterField.setBackground(MBGuiConstants.ColorFieldBackground);

		filterField.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent evt) {
				doOk();
			}
		});
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
		gridBag.setConstraints(filterField, c);
		getContentPane().add(filterField);
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

	public void postVisibleInitialization() {
		filterField.requestFocus();
	}


	public Dimension getPreferredSize() {
		Dimension old = super.getPreferredSize();
		//old.width = old.width * 2;
		return old;
	}

	public void doOk () {
		//do filter stuff here
		synchronized(comboBox) {
//			comboBox.filterModel(filterField.getText());
		}
		// clean up and get out
		dispose();
		userCancel = false;
	}

	public void dispose() {
		userCancel = true;
		super.dispose();
	}

	public boolean wasUserCancelled() {
		return userCancel;
	}
}
