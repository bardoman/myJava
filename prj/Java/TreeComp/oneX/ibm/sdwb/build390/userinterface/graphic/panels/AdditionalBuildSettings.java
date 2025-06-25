package com.ibm.sdwb.build390.userinterface.graphic.panels;
/*******************************************************************************/
/* This class manages the driver build verb additional settings*/
/*******************************************************************************/
// changes
//Date    Defect/Feature        Reason
/*******************************************************************************/
import java.awt.*;
import java.awt.List;
import java.awt.event.*;
import java.awt.event.KeyEvent;
import java.io.*;
import java.util.*;
import java.util.Map.Entry;

import javax.swing.*;
import javax.swing.border.*;
import javax.swing.event.*;

import com.ibm.sdwb.build390.*;
import com.ibm.sdwb.build390.help.*;
import com.ibm.sdwb.build390.userinterface.graphic.MainInterface;

public class AdditionalBuildSettings extends MBModalStatusFrame {

	//#SDWB-1300:        Add  Additional build settings dialog
	private MBBuild  build  = null;
	private JButton  btHelp   =   new JButton("Help");
	private JButton  btOk   =   new JButton(" OK");
	private BuildSettingEditableJList settingList = null;

	public  AdditionalBuildSettings(MBBuild  bld,  MBInternalFrame pFrame) throws com.ibm.sdwb.build390.MBBuildException {
		super("Additional Build Settings", pFrame, null);
		build = bld;
		initializeDialog();
	}

	private void initializeDialog()  throws com.ibm.sdwb.build390.MBBuildException{
		setForeground(MBGuiConstants.ColorRegularText);
		setBackground(MBGuiConstants.ColorGeneralBackground);
		btHelp.setForeground(MBGuiConstants.ColorHelpButton);
		Map<String,String> buildSettingMap = build.getBuildSettings();
		Vector buildSettings = new Vector();
                for(Map.Entry<String,String> entry:buildSettingMap.entrySet()){
			buildSettings.add(entry.getKey()+"="+entry.getValue());
                }
		settingList = new BuildSettingEditableJList(buildSettings);
		getContentPane().add("Center", settingList);

		btHelp.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent evt) {
				MBUtilities.ShowHelp("SPTABSET",HelpTopicID.DRIVERADDBUILDSETTINGS_HELP);
			}
		} );
		btOk.setForeground(MBGuiConstants.ColorActionButton);
		btOk.addActionListener(new ActionListener() {
			public void actionPerformed(java.awt.event.ActionEvent A) {
                                build.getBuildSettings().clear();
				build.getBuildSettings().putAll(settingList.getSettings());
				dispose();
			}
		});
		Vector  actionButtons  = new Vector();
		actionButtons.addElement(btOk);
		addButtonPanel(btHelp, actionButtons);
		setVisible(true);
	}


	public Dimension getMinimumSize(){
		Dimension newMin = super.getMinimumSize();
		return new Dimension((int) newMin.getWidth(),((int) newMin.getHeight()*2));
	}

	public Dimension getPreferredSize(){
		Dimension newMin = super.getPreferredSize();
		return new Dimension((int) newMin.getWidth(),((int) newMin.getHeight()*2));
	}


	class BuildSettingEditableJList extends EditableJList{
		
		BuildSettingEditableJList(Vector values){
			super(values);
		}

		public String getNewEntry(String originalValue, JInternalFrame parentWindow) {
			AddOneBuildSetting userInput = new AddOneBuildSetting(originalValue, thisFrame);
			String newtxt = new String();  // force check for blanks in maclib ds's   
			// force check for blanks in maclib ds's   
			newtxt = userInput.getSetting();
			if (newtxt != null) {
				newtxt = newtxt.trim();
			}
			return newtxt;
		}

		public java.util.Map getSettings(){
			java.util.Map returnMap = new HashMap();
			for(int i = 0; i < getModel().getSize(); i++) {
				String oneSetting = (String) getModel().getElementAt(i);
				int splitIndex = oneSetting.indexOf("=");
				returnMap.put(oneSetting.substring(0, splitIndex), oneSetting.substring(splitIndex+1));
			}
			return returnMap;
		}
	}


	class AddOneBuildSetting extends MBModalStatusFrame {
		private JButton  btOk   =   new JButton(" OK");

		private JTextField  keywordText = new JTextField("",10);
		private JTextField  valueText = new JTextField("",10);
		private JLabel keywordLabel = new JLabel("KEYWORD");
		private JLabel valueLabel = new JLabel("VALUE");
		private JLabel warning = new JLabel("WARNING:");

		private JLabel label = new JLabel(" VALUE will be upper cased if no quotes");//" Saves in Host as it is else converted to Uppercase");

		private GridBagLayout  gridbag   =  new GridBagLayout();
		private JPanel   centerPanel  =  new JPanel(gridbag);
		private JPanel   panel2  = new JPanel();
		private JPanel   wpanel  = new JPanel();

		private JPanel   bpanel  = new JPanel();
		private JPanel   epanel  = new JPanel();
		private String setting = null;


		//#SDWB-1300:        Add  Additional build settings dialog
		public  AddOneBuildSetting(String tempSetting, JInternalFrame pFrame)  {
			super(" Add build setting", pFrame, null);
			initializeDialog(tempSetting);
		}


		public void initializeDialog(String tempSetting) {
			GridBagLayout gridbag  =  new GridBagLayout();
			bpanel = new JPanel(gridbag);
			setForeground(MBGuiConstants.ColorRegularText);
			setBackground(MBGuiConstants.ColorGeneralBackground);

			btOk.setForeground(MBGuiConstants.ColorActionButton);
			btOk.addActionListener(new ActionListener() {
				public void actionPerformed(java.awt.event.ActionEvent A) {
					if(keywordText.getText().trim() != null) {
						if(keywordText.getText().trim().length() > 0) {
							setting = keywordText.getText().trim().toUpperCase()+"="+ valueText.getText().trim();
						}
					}
					dispose();
				}
			});

			keywordText.addKeyListener(new keyAdapter1(getStatus(),8));
			valueText.addKeyListener(new keyAdapter1(getStatus(),50));
			Vector  actionButtons  = new Vector();
			actionButtons.addElement(btOk);
			addButtonPanel(null, actionButtons);


			GridBagLayout gridBag = new GridBagLayout();
			GridBagConstraints c = new GridBagConstraints();
			c.gridx = 0;
			c.gridy = 0;
			c.weightx = 1;
			c.weighty = 1;
			panel2 = new MBInsetPanel(gridBag,8,8,8,8);
			panel2.setBackground(MBGuiConstants.ColorGeneralBackground);
			getContentPane().add("Center", panel2);

			c.insets = new Insets(1,1,1,1);

			c.gridx = 1;
			c.gridy = 1;
			c.fill = GridBagConstraints.HORIZONTAL;
			gridBag.setConstraints(keywordLabel, c);
			panel2.add(keywordLabel);


			keywordText.setBackground(MBGuiConstants.ColorFieldBackground);
			c.gridx = 2;
			c.gridy = 1;
			gridBag.setConstraints(keywordText, c);
			panel2.add(keywordText);
			c.gridx = 1;
			c.gridy = 2;
			gridBag.setConstraints(label,c);
			panel2.add(label);
			label.setForeground(MBGuiConstants.ColorGroupHeading);
			c.gridx = 1;
			c.gridy = 3;
			c.fill = GridBagConstraints.HORIZONTAL;
			gridBag.setConstraints(valueLabel, c);
			panel2.add(valueLabel);

			valueText.setBackground(MBGuiConstants.ColorFieldBackground);
			c.gridx = 2;
			c.gridy = 3;
			c.gridwidth = GridBagConstraints.REMAINDER;
			gridBag.setConstraints(valueText, c);
			panel2.add(valueText);
			if(tempSetting!=null) {
				int splitPoint = tempSetting.indexOf("=");
				keywordText.setText(tempSetting.substring(0,splitPoint));
				valueText.setText(tempSetting.substring(splitPoint+1, tempSetting.length()));
			}
			setVisible(true);

		}

		String getSetting(){
			return setting;
		}


		class  keyAdapter1 extends KeyAdapter {
			MBStatus stat=null;

			int allowableKeyBoardInputLength;
			boolean transveron = false;
			keyAdapter1(MBStatus stat, int allowableKeyBoardInputLength) {
				this.stat=stat;
				this.allowableKeyBoardInputLength=allowableKeyBoardInputLength;
			}
			public void KeyTyped(java.awt.event.KeyEvent event) {
			}
			public void KeyPressed(java.awt.event.KeyEvent event) {
			}




			public void keyReleased(java.awt.event.KeyEvent event) {
				//if (!event.isAltDown() && !event.isShiftDown() && !event.isControlDown() && event.getKeyCode() != 16) {
				boolean CharacterCheckOK=true;
				//JTextField keywordText = new JTextField();
				JTextField field = (JTextField)event.getSource();
				String fiestr = field.getText().toString();
				if((field != null)&fiestr.length()>0) {
					int noCol = field.getText().trim().length();
					//CharacterCheckOK= ValidateEachCharacter(field);

					if(field == keywordText) {
						field.setText(fiestr.toUpperCase());
					}
					//	if (CharacterCheckOK) {
					if(!field.getText().trim().equals("")) {
						if(noCol > allowableKeyBoardInputLength) {
							field.setText(fiestr.substring(0,allowableKeyBoardInputLength));

							stat.updateStatus("length cannot exceed " +allowableKeyBoardInputLength+ " characters" ,false);
							//field.setText(fiestr.toUpperCase());
						} else {

							int pos = field.getCaretPosition();
							switch(event.getKeyCode()) {
								case KeyEvent.VK_HOME:
									field.setCaretPosition(0);
									break;
									// case event.VK_LEFT:
									//	field.setCaretPosition((pos > 0 ? pos : 0));
									//	break;
								case KeyEvent.VK_RIGHT:
									//field.setCaretPosition((((pos > 0)&(pos<=8)) ? pos : 0));
									field.setCaretPosition(pos > 0 ? pos : 0);
									break;
								case KeyEvent.VK_END:
									field.setCaretPosition(field.getText().length());
									break;
								case KeyEvent.VK_INSERT:
									field.setCaretPosition(pos);
									break;
									// case event.VK_DELETE:
									//	field.setCaretPosition((pos > 0 ? pos : 0));
									//	break;
								default:
									//field.setText(fiestr.toUpperCase());
									field.setCaretPosition(pos);
									break;
							}
							stat.clearStatus();

						}


					}
					//}
				}
			}

		}
	}

}
