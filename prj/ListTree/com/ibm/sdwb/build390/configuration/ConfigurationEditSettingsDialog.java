package com.ibm.sdwb.build390.configuration;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.event.*;
import java.util.*;
import com.ibm.sdwb.build390.*;
import com.ibm.sdwb.build390.logprocess.*;

public class ConfigurationEditSettingsDialog extends MBModalFrame {
	JComboBox realmComboBox = new JComboBox(DictionaryOfConfigOptions.getAllRealms());
    JComboBox sectionComboBox = new JComboBox();
    JComboBox keywordComboBox = new JComboBox();
    JTextField valueTextField = new JTextField(5);
    JLabel realmLabel = new JLabel("Realm:");
    JLabel sectionLabel = new JLabel("Section:");
    JLabel settingLabel = new JLabel("keyword=value");
    JButton accept = new JButton("Ok");
    String realm = null;
    String section = null;
    String keyword = null;
    String value = null;
    private transient LogEventProcessor lep=null;

    public ConfigurationEditSettingsDialog(JInternalFrame owner, String startingRealm, String startingSection, String startingKeyword, String startingValue,LogEventProcessor tempLep) {
        super("Edit Configuration settings", owner, tempLep);
        setTitle("Edit configuration settings");
        lep=tempLep;
        setContentPane(createPanel(startingRealm, startingSection, startingKeyword, startingValue));
        pack();
        setVisible(true);
    }

    private void handleRealmSelection(){
        if (realmComboBox.getSelectedItem()!=null) {
            Vector newSections =  DictionaryOfConfigOptions.getSectionsForRealm((String) realmComboBox.getSelectedItem());
            if (newSections != null) {
                sectionComboBox.setModel(new DefaultComboBoxModel(newSections));
            }
        }
    }

    public void handleSectionSelection(){
        if (realmComboBox.getSelectedItem()!=null) {
            if (sectionComboBox.getSelectedItem()!=null) {
                Vector newKeywords =  DictionaryOfConfigOptions.getKeywordsForSectionInRealm((String) realmComboBox.getSelectedItem(), (String) sectionComboBox.getSelectedItem());
                if (newKeywords != null) {
                    keywordComboBox.setModel(new DefaultComboBoxModel(newKeywords));
                }
            }
        }
    }

    private JPanel createPanel(String startingRealm, String startingSection, String startingKeyword, String startingValue){
        lep.LogSecondaryInfo("Debug:","EditSettingsDialog : CreatePanel method");
        realmComboBox.addActionListener(new ActionListener(){
                                              public void actionPerformed(ActionEvent e) {
                                                  handleRealmSelection();
                                              }
                                          });
        sectionComboBox.addActionListener(new ActionListener(){
                                              public void actionPerformed(ActionEvent e) {
                                                  handleSectionSelection();
                                              }
                                          });
        realmComboBox.setSelectedItem(startingRealm);
        handleRealmSelection();
        sectionComboBox.setSelectedItem(startingSection);
        handleSectionSelection();
        keywordComboBox.setSelectedItem(startingKeyword);
        valueTextField.setText(startingValue);

        JPanel newPanel = new JPanel();
        Box newBox = Box.createVerticalBox();
        Box hBox = Box.createHorizontalBox();
        hBox.createHorizontalGlue();
        hBox.add(realmLabel);
        Box h1Box = Box.createHorizontalBox();
        h1Box.createHorizontalGlue();
        h1Box.add(realmComboBox);
        Box h2Box = Box.createHorizontalBox();

        Box hBox2 = Box.createHorizontalBox();
        hBox2.createHorizontalGlue();
        hBox2.add(sectionLabel);
        Box h1Box2 = Box.createHorizontalBox();
        h1Box2.createHorizontalGlue();
        h1Box2.add(sectionComboBox);
        Box h2Box2 = Box.createHorizontalBox();
        h2Box2.createHorizontalGlue();
        h2Box2.add(settingLabel);



        Box settingBox = Box.createHorizontalBox();
        settingBox.createHorizontalGlue();
        settingBox.add(keywordComboBox);
        settingBox.createHorizontalGlue();
        settingBox.add(new JLabel(" = "));
        settingBox.createHorizontalGlue();
        settingBox.add(valueTextField);

        newBox.add(hBox);
        newBox.add(h1Box);
        newBox.add(h2Box);
        newBox.add(hBox2);
        newBox.add(h1Box2);
        newBox.add(h2Box2);

        newBox.add(settingBox);
        newBox.add(Box.createRigidArea(new Dimension(0,10)));
        newBox.add(accept);

        accept.setEnabled(true);

        accept.addActionListener(new ActionListener(){
                                     public void actionPerformed(ActionEvent e) {
                                         if (((String) sectionComboBox.getSelectedItem()).trim().length()<1 | ((String)keywordComboBox.getSelectedItem()).trim().length() < 1 | valueTextField.getText().trim().length() < 1) {
                                             String message[]={"         You must fill in :   ","SECTION, KEYWORD, and VALUE fields before proceeding. "};
                                             JOptionPane.showInternalMessageDialog( thisFrame,message,"Warning",JOptionPane.WARNING_MESSAGE);
                                             System.out.println("You must fill in the section, keyword, and value fields before proceeding.");
                                         } else {
											 realm = ((String)realmComboBox.getSelectedItem()).trim();
                                             section = ((String)sectionComboBox.getSelectedItem()).trim();
                                             value = valueTextField.getText().trim();
                                             keyword = ((String) keywordComboBox.getSelectedItem()).trim();
                                             dispose();
                                         }
                                     }
                                 }
                                );
        newPanel.add(newBox);
        return newPanel;
    }

    public String getRealm(){
        if (realm != null) {
            return realm.trim().toUpperCase();
        }
        return null;
    }

    public String getSection(){
        if (section != null) {
            return section.trim().toUpperCase();
        }
        return null;
    }

    public String getKeyword(){
        if (keyword!=null) {
            return keyword.trim().toUpperCase();
        }
        return null;
    }

    public String getValue(){
        if (value != null) {
            return value.trim();
        }
        return null;
    }
	
	private void readObject(java.io.ObjectInputStream in) throws java.io.IOException, ClassNotFoundException{
		in.defaultReadObject();
		lep=new  LogEventProcessor();
/*Ken 7/5/00 we should do this, but we'll add & test later.
		lep.addEventListener(MBClient.getGlobalLogFileListener());
*/		
	}
}
