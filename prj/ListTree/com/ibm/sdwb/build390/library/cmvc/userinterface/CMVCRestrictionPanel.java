package com.ibm.sdwb.build390.library.cmvc.userinterface;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.ArrayList;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JPanel;
import javax.swing.border.LineBorder;
import javax.swing.border.TitledBorder;

import com.ibm.sdwb.build390.MBFileChooser;
import com.ibm.sdwb.build390.MBGlobals;
import com.ibm.sdwb.build390.MBGuiConstants;
import com.ibm.sdwb.build390.MBMainframeInfo;
import com.ibm.sdwb.build390.MBMsgBox;
import com.ibm.sdwb.build390.MultipleTextEntryDialog;
import com.ibm.sdwb.build390.library.SourceInfo;
import com.ibm.sdwb.build390.library.cmvc.CMVCLibraryInfo;
import com.ibm.sdwb.build390.library.cmvc.ComponentAndPathRestrictions;
import com.ibm.sdwb.build390.library.userinterface.SourceSelection;
import com.ibm.sdwb.build390.userinterface.UserCommunicationInterface;
import com.ibm.sdwb.build390.userinterface.event.UserInterfaceListenerManager;
import com.ibm.sdwb.build390.userinterface.graphic.MainInterface;


public class CMVCRestrictionPanel extends JPanel {

    private final static String EXCLUDECONSTANT = "excluded";
    private final static String INCLUDECONSTANT = "included";

    private JButton btComponentsEdit = new JButton("View List");
    private JButton btPathsEdit = new JButton("View List");
    private JCheckBox cbComponents = new JCheckBox("Components to be ");
    private JCheckBox cbPaths = new JCheckBox("Paths to be ");
    private JComboBox comboComponentChoice = new JComboBox();
    private JComboBox comboPathChoice = new JComboBox();
    private CMVCLibraryInfo libInfo = null;
    private MBMainframeInfo mainInfo = null;
    private java.util.List directoryList = null;
    private java.util.List componentList = null;

    public CMVCRestrictionPanel(com.ibm.sdwb.build390.library.LibraryInfo tempLib, MBMainframeInfo tempMain) {
        libInfo = (CMVCLibraryInfo) tempLib;
        mainInfo = tempMain;
        comboComponentChoice.addItem(EXCLUDECONSTANT);
        comboPathChoice.addItem(EXCLUDECONSTANT);
        comboComponentChoice.addItem(INCLUDECONSTANT);
        comboPathChoice.addItem(INCLUDECONSTANT);
        setLayout(new java.awt.BorderLayout());

        btPathsEdit.setEnabled(false);
        btPathsEdit.addActionListener(new ActionListener() {
                                          public void actionPerformed(ActionEvent evt) {
                                              new Thread(new Runnable() {
                                                             public void run() {
                                                                 java.util.List tempDirectoryList=null;
                                                                 if (directoryList == null) {
                                                                     tempDirectoryList= new ArrayList();
                                                                 } else {
                                                                     tempDirectoryList=new ArrayList(directoryList);
                                                                 }
                                                                 MBMsgBox optionBox = new MBMsgBox("Option","Do you want to select a file for source?", getParentFrame(),true);
                                                                 if (optionBox.isAnswerYes()) {
                                                                     File file = getListPrimingFile("Select *.dir File for Restriction", "dir");
                                                                     if (file != null) {
                                                                         try {
                                                                             BufferedReader in = new BufferedReader(new FileReader(file));
                                                                             String inStr;
                                                                             do {
                                                                                 inStr = in.readLine();
                                                                                 if (inStr != null) {
                                                                                     inStr=inStr.trim();
                                                                                     if (inStr.length() > 0) {
                                                                                         if (!tempDirectoryList.contains(inStr+"/")) {
                                                                                             tempDirectoryList.add(inStr);
                                                                                         }
                                                                                     }
                                                                                 }
                                                                             }
                                                                             while (inStr != null);
                                                                         } catch (Exception e) {
                                                                             e.printStackTrace();
                                                                         }
                                                                     }
                                                                 }
                                                                 MultipleTextEntryDialog pathEdit = new MultipleTextEntryDialog("Directories to restrict by:", tempDirectoryList, getParentFrame(), 0, false);
                                                                 if (pathEdit.getEntries()!=null) {
                                                                     directoryList = pathEdit.getEntries();
                                                                 }
                                                                 if (tempDirectoryList!=null) {
                                                                     for (int i = 0; i != tempDirectoryList.size(); i++) {
                                                                         String entry = ((String) tempDirectoryList.get(i)).trim();
                                                                         if (!entry.endsWith("/")) {
                                                                             entry += "/";
                                                                             tempDirectoryList.set(i,entry);
                                                                         }
                                                                     }
                                                                 }
                                                             }
                                                         }).start();
                                          }
                                      });

        btComponentsEdit.setEnabled(false);
        btComponentsEdit.addActionListener(new ActionListener() {
                                               public void actionPerformed(ActionEvent evt) {
                                                   new Thread(new Runnable() {
                                                                  public void run() {
                                                                      java.util.List tempComponentList=null;
                                                                      if (componentList == null) {
                                                                          tempComponentList= new ArrayList();
                                                                      } else {
                                                                          tempComponentList=new ArrayList(componentList);
                                                                      }

                                                                      MBMsgBox optionBox = new MBMsgBox("Option","Do you want to select a file for source?", getParentFrame(),true);
                                                                      if (optionBox.isAnswerYes()) {
                                                                          File file=getListPrimingFile("Select *.cmp File for Restriction", "cmp");
                                                                          if (file != null) {
                                                                              try {
                                                                                  BufferedReader in = new BufferedReader(new FileReader(file));
                                                                                  String inStr;
                                                                                  do {
                                                                                      inStr = in.readLine();
                                                                                      if (inStr != null) {
                                                                                          inStr=inStr.trim();
                                                                                          if (inStr.length() > 0) {
                                                                                              tempComponentList.add(inStr);
                                                                                          }
                                                                                      }
                                                                                  }
                                                                                  while (inStr != null);
                                                                              } catch (Exception e) {
                                                                                  e.printStackTrace();
                                                                              }
                                                                          }
                                                                      }

                                                                      MultipleTextEntryDialog compEdit = new MultipleTextEntryDialog("Components to restrict by:", tempComponentList, getParentFrame(), 0, false);
                                                                      if (compEdit.getEntries()!=null) {
                                                                          componentList = compEdit.getEntries();
                                                                      }
                                                                  }
                                                              }).start();
                                               }
                                           });

        cbComponents.addItemListener(new ItemListener() {
                                         public void itemStateChanged(final ItemEvent ie) {
                                             new Thread(new Runnable() {
                                                            public void run() {
                                                                switch (ie.getStateChange()) {
                                                                case java.awt.event.ItemEvent.SELECTED:
                                                                    btComponentsEdit.setEnabled(true);
                                                                    break;
                                                                case java.awt.event.ItemEvent.DESELECTED:
                                                                    btComponentsEdit.setEnabled(false);
                                                                    break;
                                                                default :
                                                                }
                                                            }
                                                        }).start();
                                         }
                                     });
        cbPaths.addItemListener(new ItemListener() {
                                    public void itemStateChanged(final ItemEvent ie) {
                                        new Thread(new Runnable() {
                                                       public void run() {
                                                           switch (ie.getStateChange()) {
                                                           case java.awt.event.ItemEvent.SELECTED:
                                                               btPathsEdit.setEnabled(true);
                                                               break;
                                                           case java.awt.event.ItemEvent.DESELECTED:
                                                               btPathsEdit.setEnabled(false);
                                                               break;
                                                           default :
                                                           }
                                                       }
                                                   }).start();
                                    }
                                });

        GridBagLayout gridBag = new GridBagLayout();
        GridBagConstraints c = new GridBagConstraints();

        JPanel restrictionBox = new JPanel(gridBag);
        restrictionBox.setBorder(BorderFactory.createTitledBorder(LineBorder.createGrayLineBorder() ,"Restrictions ",TitledBorder.DEFAULT_JUSTIFICATION,TitledBorder.DEFAULT_POSITION,null,MBGuiConstants.ColorGroupHeading));

        c.fill = GridBagConstraints.BOTH;
        c.anchor = GridBagConstraints.WEST;
        c.weightx = 1.0;  
        c.gridwidth=1;  
        c.insets = new Insets(2,5,2,5);
        gridBag.setConstraints(cbComponents, c);
        restrictionBox.add(cbComponents);

        c.fill = GridBagConstraints.HORIZONTAL;
        c.gridwidth = GridBagConstraints.RELATIVE;
        gridBag.setConstraints(comboComponentChoice, c);
        restrictionBox.add(comboComponentChoice);

        c.fill = GridBagConstraints.HORIZONTAL;
        c.gridwidth = GridBagConstraints.REMAINDER;
        gridBag.setConstraints(btComponentsEdit, c);
        restrictionBox.add(btComponentsEdit);

        c.weightx = 1.0;  
        c.gridwidth=1;  
        c.fill = GridBagConstraints.HORIZONTAL;
        gridBag.setConstraints(cbPaths, c);
        restrictionBox.add(cbPaths);

        c.fill = GridBagConstraints.HORIZONTAL;
        c.gridwidth = GridBagConstraints.RELATIVE;
        c.anchor = GridBagConstraints.WEST;
        gridBag.setConstraints(comboPathChoice, c);
        restrictionBox.add(comboPathChoice);

        c.weightx = 1.0;  
        c.fill = GridBagConstraints.HORIZONTAL;
        c.gridwidth = GridBagConstraints.REMAINDER;
        gridBag.setConstraints(btPathsEdit, c);
        restrictionBox.add(btPathsEdit);

        add(restrictionBox);
    }

    public void setRestriction(ComponentAndPathRestrictions restrictions) {
        if (restrictions!=null) {
            cbComponents.setSelected(restrictions.getComponentList()!=null);
            cbPaths.setSelected(restrictions.getPathList()!=null);
            selectIncludeOrExclude(comboComponentChoice, restrictions.isComponentsIncluded());
            selectIncludeOrExclude(comboPathChoice, restrictions.isPathsIncluded());
            directoryList=restrictions.getPathList();
            componentList=restrictions.getComponentList();
        }
    }

    private void selectIncludeOrExclude(JComboBox combo, boolean includeSelected) {
        if (includeSelected) {
            combo.setSelectedItem(INCLUDECONSTANT);
        } else {
            combo.setSelectedItem(EXCLUDECONSTANT);
        }
    }

    public ComponentAndPathRestrictions getComponentAndPathRestrictions() {
        ComponentAndPathRestrictions restrictions = new ComponentAndPathRestrictions();
        if (cbComponents.isSelected()) {
            restrictions.setIncludeComponents(INCLUDECONSTANT.equals(comboComponentChoice.getSelectedItem()));
            restrictions.setComponentList(componentList);
        }
        if (cbPaths.isSelected()) {
            restrictions.setIncludePaths(INCLUDECONSTANT.equals(comboPathChoice.getSelectedItem()));
            restrictions.setPathList(directoryList);
        }
        return restrictions;
    }


    private java.awt.Component getParentFrame() {
        return getParent();
    }

    private File getListPrimingFile(String title, String suffix) {

        MBFileChooser openFileChooser1 = new MBFileChooser();
        String dir = MBGlobals.Build390_path;
        openFileChooser1.setCurrentDirectory(new File(dir));
        openFileChooser1.setFileFilter(new SuffixFileFilter(suffix));
        openFileChooser1.setFileSelectionMode(JFileChooser.FILES_ONLY);
        openFileChooser1.setDialogTitle(title);
        openFileChooser1.showDialog(getParentFrame(), "Select");
        return openFileChooser1.getSelectedFile();
    }

    private class SuffixFileFilter extends javax.swing.filechooser.FileFilter {

        private String suffixToLookFor = null;

        SuffixFileFilter(String tempSuffix) {
            suffixToLookFor = tempSuffix;
        }

        public  boolean accept(File f) {
            boolean accept =f.isDirectory();
            if (!accept) {
                String suffix =getSuffix(f);
                if (suffix != null) {
                    accept =suffix.equals(suffix);
                }
            }
            return accept;
        }

        public  String getDescription() {
            return "*."+suffixToLookFor+" files";
        }

        private String getSuffix(File f) {
            String s = f.getPath(), suffix = null;

            int i= s.lastIndexOf('.');

            if (i > 0 && i < s.length() -1) {
                suffix = s.substring(i+1).toLowerCase();
            }
            return suffix;
        }
    }
}
