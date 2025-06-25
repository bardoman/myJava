package com.ibm.sdwb.build390;
/*********************************************************************/
/* Java MetadataListEntryDialog class for the Build/390 client                          */
/*  Helps the user enter multiple metadata statements into a list and returns it to the caller */
/*********************************************************************/
//
import java.awt.*;
import java.awt.event.*;
import java.util.*;
import javax.swing.*;

/** <br> The MetadataListEntryDialog class for the Build/390 client 
* Helps the user enter multiple metadata statements into a list and returns it to the caller
*/
public class MetadataListEntryDialog 
extends MBModalFrame
{
	protected Vector userEntry = null;
	private Vector initialValues = null;
	protected EditableMetadataList tf1;
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
	MetadataType[] metadataTypes;
	/** Constructor - Builds the frame and populates it with the entry field and buttons.
	* It also adds the action listeners.
	*/
	public MetadataListEntryDialog(Vector tempInit,
								   JInternalFrame parentFrame,
								   int maxLen, boolean showCheck, 
								   MetadataType[] metadataTypes)
	{
		super("Metadata List Entry", parentFrame, null);
		maxLength = maxLen;
		checkBoxAdded = showCheck;
		initialValues = tempInit;
		this.metadataTypes=metadataTypes;
		initialize();
	}

	private void initialize()
	{
		userLabel = new JLabel("Part restriction criteria based on UNIMODC fields");
		JLabel helpLabel = new JLabel("To edit the list of entries, right click in the list area");
		if(maxLength > 0)
		{
			tf1 = new EditableMetadataList(initialValues,metadataTypes , maxLength, MetadataEntryDialog.IS_FILTER);
		}
		else
		{
			tf1 = new EditableMetadataList(initialValues, metadataTypes, MetadataEntryDialog.IS_FILTER);
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
		Vector actionButtons = new Vector();
		actionButtons.addElement(MBC_Lbu_ok_);
		buttonPanel = new MBButtonPanel(null, MBC_Lbu_quit_, actionButtons);
		Box tempBox = Box.createVerticalBox();
		if(checkBoxAdded)
		{
			tempBox.add(caseBox);
		}
		tempBox.add(buttonPanel);
		getContentPane().add("South", tempBox);

		// OK button
		MBC_Lbu_ok_.addActionListener(selh = new ActionListener ()
									  {
										  public void actionPerformed(ActionEvent evt)
										  {
											  doOk();
										  }
									  });
		// Quit button
		MBC_Lbu_quit_.addActionListener(qh = new ActionListener ()
										{
											public void actionPerformed(ActionEvent evt)
											{
												dispose();
											}
										});

		setVisible(true);
	}

	public void postVisibleInitialization()
	{
		tf1.requestFocus();
	}

	public void doOk ()
	{
		userEntry = new Vector();
		for(int i = 0; i < tf1.getModel().getSize(); i++)
		{
			if(checkBoxAdded & caseBox.isSelected())
			{
				userEntry.addElement(tf1.getModel().getElementAt(i).toString().toUpperCase());
			}
			else
			{
				userEntry.addElement(tf1.getModel().getElementAt(i));
			}
		}
	   
		if(userEntry.isEmpty())
		{
		 	new Thread(new Runnable()
					   {
						   public void run()
						   {
							   new MBMsgBox("Warning","No Metadata has been defined");
						   }
					   }).start();

			userEntry=null;
			return;
		}

		if(((String)userEntry.lastElement()).endsWith("|"))
		{
			new Thread(new Runnable()
					   {
						   public void run()
						   {
							   new MBMsgBox("Warning","The last Metadata entry must end with a ','");
						   }
					   }).start();

			userEntry=null;
			return;
		}

		if(userEntry.size() > 0 | allowBlankEntry)
		{
			// clean up and get out
			dispose();
		}
		else
		{
			userEntry = null;
		}
	}

	public Vector getEntries()
	{
		return userEntry;
	}
}
