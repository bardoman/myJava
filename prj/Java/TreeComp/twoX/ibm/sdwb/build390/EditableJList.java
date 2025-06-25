package com.ibm.sdwb.build390;
/*********************************************************************/
/* EditableJList class for the Build/390 client                 */
/*  a JList with a popup menu which allows various operations on the list memebers*/
/*	(insert (above, below, at end) remove, edit, add)*/
/*********************************************************************/
// Changes
// Date     Defect/Feature      Reason
// 12/07/99                     force check for blanks in maclib ds's   
/*********************************************************************/
import java.io.*;
import java.util.*;
import javax.swing.*;
import javax.swing.event.*;
import java.awt.event.*;
import java.awt.*;

/** Create the driver build page */
public class EditableJList extends JList{

	private JMenuItem addEndEntry = null;
	private JMenuItem addAboveEntry = null;
	private JMenuItem addBelowEntry = null;
	private JMenuItem removeEntry = null;
	private JMenuItem editEntry = null;
	private JMenuItem moveEntry = null;
	private JPopupMenu popupMenu = null;
	private EditableJList localCopy = null;
	private DefaultListModel localModel = null;
	protected int maxEntryLength = -1;
	private boolean allowBlanks = true;     // force check for blanks in maclib ds's   

	public EditableJList(java.util.List values) {
		initialize(values);
	}

	// force check for blanks in maclib ds's   
	public EditableJList(java.util.List values, boolean iallowBlanks) {
		allowBlanks = iallowBlanks;
		initialize(values);
	}

	public EditableJList(java.util.List values, int maxLength) {
		maxEntryLength = maxLength;
		initialize(values);
	}

	// force check for blanks in maclib ds's   
	public EditableJList(java.util.List values, int maxLength, boolean iallowBlanks) {
        allowBlanks = iallowBlanks;
		maxEntryLength = maxLength;
		initialize(values);
	}

	public void initialize(java.util.List values) {
		if (values == null) {
			values = new ArrayList();
		}
		if (values.size() > 0) {   
			setListData(values);
		} else {
			setModel(new DefaultListModel());
		}
// create the popup menu and set up the listeners
		popupMenu = new JPopupMenu("Entries");
		addMouseListener(new MouseAdapter(){
			public void mouseClicked(MouseEvent e) {
				checkForPopup(e);
			}
			public void mousePressed(MouseEvent e) {
				checkForPopup(e);
			}
			public void mouseReleased(MouseEvent e) {
				checkForPopup(e);
			}
		});
// set up the popup entries
		addEndEntry = new JMenuItem("to the end");
		addAboveEntry = new JMenuItem("above selected");
		removeEntry = new JMenuItem("Remove");
		editEntry = new JMenuItem("Edit");
		moveEntry = new JMenuItem("Move");
		addEndEntry.setEnabled(false);
		JMenu addMenu = new JMenu("Add");
		addMenu.add(addAboveEntry);
		addMenu.add(addEndEntry);
		popupMenu.add(addMenu);
		popupMenu.add(removeEntry);
		popupMenu.add(editEntry);
		popupMenu.add(moveEntry);
// based on whether the list is enabled or not, set the enabled state of the entries.
		popupMenu.addPopupMenuListener(new PopupMenuListener() {
			public void popupMenuWillBecomeVisible(PopupMenuEvent e) {
				boolean isSel = !isSelectionEmpty();
				boolean isEnbl = localCopy.isEnabled();
				removeEntry.setEnabled(isSel & isEnbl);
				addAboveEntry.setEnabled(isSel & isEnbl);
				editEntry.setEnabled(isSel & isEnbl);
				moveEntry.setEnabled(isSel & isEnbl);
				addEndEntry.setEnabled(isEnbl);
			}
			public void popupMenuWillBecomeInvisible(PopupMenuEvent e) {
			}
			public void popupMenuCanceled(PopupMenuEvent e) {
			}
		});
		localCopy = this;
		addEndEntry.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				new Thread(new Runnable() {
					public void run() {
						Component parentWindow = localCopy;
						while (parentWindow != null & !(parentWindow instanceof JInternalFrame)) {
							parentWindow = parentWindow.getParent();
						}
						String newEntry = getNewEntry(null, (JInternalFrame) parentWindow);
						if (newEntry != null) {
							java.util.List newValues = new ArrayList();
							for (int i = 0; i < localCopy.getModel().getSize(); i++) {
								if (localCopy.getModel().getElementAt(i) != null) {
									newValues.add(localCopy.getModel().getElementAt(i));
								}
							}

							newValues.add(newEntry);
							localCopy.setListData((java.util.List)newValues); //to make sure we call EditableJList.setListData instead of JList.setListData
						}
					}
				}).start();
			}
		});
		addAboveEntry.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				new Thread(new Runnable() {
					public void run() {
						Component parentWindow = localCopy;
						while (parentWindow != null & !(parentWindow instanceof JInternalFrame)) {
							parentWindow = parentWindow.getParent();
						}
						String newEntry = getNewEntry(null, (JInternalFrame) parentWindow);
						if (newEntry != null) {
							Vector newValues = new Vector();
							int selectedIndex = localCopy.getSelectedIndex();
							for (int i = 0; i < localCopy.getModel().getSize(); i++) {
								if (localCopy.getModel().getElementAt(i) != null) {
									newValues.addElement(localCopy.getModel().getElementAt(i));
								}
							}
							newValues.insertElementAt(newEntry, selectedIndex);
							localCopy.setListData((java.util.List)newValues); //to make sure we call EditableJList.setListData instead of JList.setListData
						}
					}
				}).start();
			}
		});
		moveEntry.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				new Thread(new Runnable() {
					public void run() {
						Component parentWindow = localCopy;
						while (parentWindow != null & !(parentWindow instanceof JInternalFrame)) {
							parentWindow = parentWindow.getParent();
						}
						Vector newValues = new Vector();
						String selectedVal = (String) localCopy.getSelectedValue();
						for (int i = 0; i < localCopy.getModel().getSize(); i++) {
							if (localCopy.getModel().getElementAt(i) != null & i != getSelectedIndex()) {
								newValues.addElement(localCopy.getModel().getElementAt(i));
							}
						}
						ComboSelectionDialog userInput = new ComboSelectionDialog("Select the new position:",1, newValues.size()+1,  (JInternalFrame) parentWindow);
						if (userInput.getNumber()-1 >= 0) {
							newValues.insertElementAt(selectedVal, userInput.getNumber()-1);
							localCopy.setListData((java.util.List)newValues); //to make sure we call EditableJList.setListData instead of JList.setListData
						}
					}
				}).start();
			}
		});
		removeEntry.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				Vector newValues = new Vector();
				for (int i = 0; i < localCopy.getModel().getSize(); i++) {
					newValues.addElement(localCopy.getModel().getElementAt(i));
				}
				Object[] selectedValues = localCopy.getSelectedValues();
				for (int i = 0; i < selectedValues.length; i++) {
					newValues.removeElement(selectedValues[i]);
				}
                                localCopy.setListData((java.util.List)newValues); //to make sure we call EditableJList.setListData instead of JList.setListData
			}
		});
		editEntry.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				new Thread(new Runnable() {
					public void run() {
						Component parentWindow = localCopy;
						while (parentWindow != null & !(parentWindow instanceof JInternalFrame)) {
							parentWindow = parentWindow.getParent();
						}
						String previousString = (String) localCopy.getSelectedValue();
						String newEntry = getNewEntry(previousString, (JInternalFrame) parentWindow);
						if (newEntry != null) {
							Vector newValues = new Vector();
							int selectedIndex = localCopy.getSelectedIndex();
							for (int i = 0; i < selectedIndex; i++) {
								newValues.addElement(localCopy.getModel().getElementAt(i));
							}
							newValues.addElement(newEntry);
							for (int i = selectedIndex+1; i < localCopy.getModel().getSize(); i++) {
								newValues.addElement(localCopy.getModel().getElementAt(i));
							}
							localCopy.setListData((java.util.List)newValues); //to make sure we call EditableJList.setListData instead of JList.setListData
						}
					}
				}).start();
			}
		});
	}

	public void setListData(final java.util.List listData){

		DefaultListModel tempMod = new DefaultListModel();
		if (listData != null) {
			for (Iterator dataIterator = listData.iterator(); dataIterator.hasNext();) {
				tempMod.addElement(dataIterator.next());
			}
		}
		setModel(tempMod);
	}

	public String getNewEntry(String originalValue, JInternalFrame parentWindow) {
		TextEntryDialog userInput;
		String newtxt = new String();  // force check for blanks in maclib ds's   
		boolean repeat = true;         // force check for blanks in maclib ds's   
		while (repeat) {               // force check for blanks in maclib ds's   
			if (originalValue != null) {
				if (maxEntryLength > 0){
					userInput= new TextEntryDialog("Enter the new value:",originalValue, parentWindow, maxEntryLength);
				} else {
					userInput= new TextEntryDialog("Enter the new value:",originalValue, parentWindow);
				}
			} else {
				if (maxEntryLength > 0){
					userInput= new TextEntryDialog("Enter the new value:", parentWindow, maxEntryLength);
				} else {
					userInput= new TextEntryDialog("Enter the new value:", parentWindow);
				}
			}
            // force check for blanks in maclib ds's   
			repeat = false;
			newtxt = userInput.getText();
			if (newtxt != null) {
				newtxt = newtxt.trim();
			}
			if (!allowBlanks & newtxt!=null) {
				if (newtxt.indexOf(" ")>-1) {
					originalValue = newtxt;
					new MBMsgBox("Entry error", "This data cannot contain blanks.", parentWindow);
					repeat = true;
				}
			}
		}
		return newtxt;
		//return userInput.getText();
	}


	public void setModel(ListModel model) {
		localModel = (DefaultListModel) model;
		super.setModel(model);
	}


	private void checkForPopup(MouseEvent e) {
		if (e.isPopupTrigger() & isEnabled()) {
			popupMenu.show(e.getComponent(), e.getX(), e.getY());
		}
	}
}
