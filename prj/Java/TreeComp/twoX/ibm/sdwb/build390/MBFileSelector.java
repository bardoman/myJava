package com.ibm.sdwb.build390;
/***************************************************************************/
/* Java MBFileSelector class for the Build/390 client                           */
/*  Builds a FileSelector, populates it and adds the action listeners specified */
// 04/28/99 Defect85    Fixed problems with parent directories, and sub directories.
//08/15/02 Feature.SDWB1777: Need directory tree selection for parts in User build
/***************************************************************************/
import java.awt.*;
import java.awt.event.*;
import java.util.*;
import javax.swing.*;
import javax.swing.event.*;
import java.io.*;

/** <br>The MBFileSelector displays a list of choices in a FileSelector and adds the correct action listener.
* Classes using this class must implement a listener for the OK button and another for the Quit button */
public class MBFileSelector extends MBModalFrame {

    private JList     lbox;                                     // new FileSelector
    private JButton   MBC_Lbu_ok_      = new JButton("OK");      // ok button
    private JButton   MBC_Lbu_quit_    = new JButton("Cancel");    // quit button
    private JButton   select_all_      = new JButton("Select All"); // select all button

    //Feature.SDWB1777:
    private JButton   selectTree      = new JButton("Select Tree"); // select tree

    private JPanel    pl               = new JPanel();           // panel for buttons
    private boolean multi=true;                                    // global flag indicating that multi select is enabled
    private File[] elementsSelected   = null;           // String array to hold the elements selected in the list box.
    private File elementSelected   = null;           // String to hold the element selected in the list box.
    private MBButtonPanel buttonPanel;
    private File currentLocation = null;
    private int firstSelection = -1;
    private int lastSelection = -1;
    private MBFileSelector thisFrame = null;
    private boolean inActionListener = false;
    private static final String parentDirectoryIndicator = ".. (up to parent directory)";

    /** Constructor - Builds the frame and FileSelector and populates the FileSelector.
    * @param title String containing the title for the FileSelector
    * @param data Vector containing the tokenized data to be placed into the FileSelector
    * @param xselh ActionListener to be used for the ok button
    * @param xqh ActionListener to be used for the quit button
    * @param multi If true a multiple selection FileSelector is to be created, otherwise a single selection FileSelector is created.
    */

    public MBFileSelector(File startLocation, boolean tempMulti, JInternalFrame pFrame) {
        super("File Selector", pFrame, null);

        // create frame and populate it
        setVisible(false);
        thisFrame = this;
        multi=tempMulti;
        lbox = new JList();
        currentLocation = startLocation;
        setTitle("Path " + currentLocation.getAbsolutePath());
        lbox.setBackground(MBGuiConstants.ColorFieldBackground);
        setBounds(50,50,250,220);
        getContentPane().setLayout(new BorderLayout());
        setForeground(MBGuiConstants.ColorRegularText);
        setBackground(MBGuiConstants.ColorGeneralBackground);
        GridBagLayout gridBag = new GridBagLayout();
        GridBagConstraints c = new GridBagConstraints();
        JPanel tempPanel = new JPanel(gridBag);
        JScrollPane listScroller = new JScrollPane(lbox);
        c.insets = new Insets(5,5,5,5);
        c.weightx = 1;
        c.weighty = 1;
        c.fill = GridBagConstraints.BOTH;
        gridBag.setConstraints(listScroller, c);
        tempPanel.add(listScroller);
        getContentPane().add(tempPanel, "Center");
        MBC_Lbu_quit_.setForeground(MBGuiConstants.ColorCancelButton);
        MBC_Lbu_ok_.setForeground(MBGuiConstants.ColorActionButton);
        Vector actionButtons = new Vector();
        if(multi) {
            select_all_.setForeground(java.awt.Color.blue);
            actionButtons.addElement(select_all_);

            //Feature.SDWB1777:
            selectTree.setForeground(java.awt.Color.blue);
            actionButtons.addElement(selectTree);
        }
        actionButtons.addElement(MBC_Lbu_ok_);
        buttonPanel = new MBButtonPanel(null, MBC_Lbu_quit_, actionButtons);
        getContentPane().add(buttonPanel, "South");

        // add the action listener for the OK button
        MBC_Lbu_ok_.addActionListener(new ActionListener() {
                                          public void actionPerformed(ActionEvent evt) {
                                              new Thread(new Runnable() {
                                                             public void run() {
                                                                 if(lbox.getSelectionMode()==ListSelectionModel.MULTIPLE_INTERVAL_SELECTION) {
                                                                     elementsSelected = new File[lbox.getSelectedValues().length];
                                                                     for(int i = 0; i < lbox.getSelectedValues().length; i++) {
                                                                         elementsSelected[i] = new File(currentLocation, (String) lbox.getSelectedValues()[i]);
                                                                     }
                                                                     if(elementsSelected.length > 0) {
                                                                         dispose();
                                                                     }
                                                                     else {
                                                                         problemBox("Error", "You must select something from the list");
                                                                         return;
                                                                     }
                                                                 }
                                                                 else {
                                                                     if(lbox.getSelectedIndex() > -1) {
                                                                         elementSelected = new File(currentLocation, (String) lbox.getSelectedValue());
                                                                         // Get BuildType selected
                                                                         dispose();
                                                                     }
                                                                     else {
                                                                         problemBox("Error", "You must select something from the list");
                                                                         return;
                                                                     }
                                                                 }
                                                             }
                                                         }).start();
                                          }
                                      });
        // add the action listener for the Quit button
        MBC_Lbu_quit_.addActionListener(new ActionListener() {
                                            public void actionPerformed(ActionEvent evt) {
                                                dispose();
                                            }
                                        });
        // add the action listener for Item selected

        // Handle Options-Setup //Defect_239 rewrite for jlist
        if(multi) {
            //Start Feature.SDWB1777:
            selectTree.addActionListener
            (new ActionListener() {
                 public void actionPerformed(ActionEvent evt) {
                     new Thread
                     (new Runnable() {
                          public void run() {
                              selectTree();
                          }
                      }
                     ).start();
                 }
             });
            //End Feature.SDWB1777:

            select_all_.addActionListener(new ActionListener() {
                                              public void actionPerformed(ActionEvent evt) {
                                                  int cnt = lbox.getModel().getSize();
                                                  int[] inds = new int[cnt];
                                                  int numberUsed = 0;
                                                  for(int lp=0; lp<cnt; lp++) {
                                                      String tempFile = (String) lbox.getModel().getElementAt(lp);
                                                      if(!tempFile.equals(parentDirectoryIndicator) & !tempFile.endsWith(File.separator)) {
                                                          inds[numberUsed] = lp;
                                                          numberUsed ++;
                                                      }
                                                  }
                                                  int[] usedIndices = new int[numberUsed];
                                                  System.arraycopy(inds, 0, usedIndices, 0, numberUsed);
                                                  lbox.setSelectedIndices(usedIndices);
                                              }
                                          } );
        }
        loadDirectoryListing();
        lbox.addListSelectionListener(new ListSelectionListener() {
                                          public void valueChanged(ListSelectionEvent e) {
                                              if(!inActionListener) {
                                                  inActionListener = true;
                                                  final File possibleFile;
                                                  final int usedIndex;
                                                  int first = e.getFirstIndex();
                                                  int last = e.getLastIndex();
                                                  String firstPossibleChoice = new String();
                                                  String secondPossibleChoice = new String();
// Ken, 4/27/99 Split into 2
                                                  if(first  < lbox.getModel().getSize()) {
                                                      firstPossibleChoice = (String) lbox.getModel().getElementAt(first);
                                                      if(first != firstSelection & lbox.isSelectedIndex(first)) {
                                                          firstSelection = first;
                                                      }
                                                      else {
                                                          firstPossibleChoice = "  ";
                                                      }
                                                  }
                                                  if(last < lbox.getModel().getSize()) {
                                                      secondPossibleChoice = (String) lbox.getModel().getElementAt(last);
                                                      if(!lbox.isSelectedIndex(last)) {
                                                          secondPossibleChoice = " ";
                                                      }
                                                  }

                                                  if(firstPossibleChoice.equals(parentDirectoryIndicator) | secondPossibleChoice.equals(parentDirectoryIndicator) ) {
// do workaround for java bug, if you getParent file E:\test\me\  you get E:\test\me.   This fixes that.
                                                      if(currentLocation.toString().endsWith(File.separator)) {
                                                          currentLocation = new File(currentLocation.toString().substring(0, (currentLocation.toString().length()-1)));
                                                      }
                                                      possibleFile = new File(currentLocation.getParent());
                                                      usedIndex = 0;
                                                  }
                                                  else if(firstPossibleChoice.endsWith(File.separator)) {
                                                      possibleFile = new File(currentLocation, firstPossibleChoice);
                                                      usedIndex = first;
                                                  }
                                                  else if(secondPossibleChoice.endsWith(File.separator)) {
                                                      possibleFile = new File(currentLocation, secondPossibleChoice);
                                                      usedIndex = last;
                                                  }
                                                  else {
                                                      possibleFile = null;
                                                      usedIndex = -1;
                                                  }
                                                  if(possibleFile != null & !currentLocation.equals(possibleFile)) {

                                                	  SwingUtilities.invokeLater(new Thread(new Runnable() {
                                                                     public void run() {
/* Ken 6/17/99  don't ask, just change the location
                                MBMsgBox viewQuestion = new MBMsgBox("", "Do you wish to change to directory \n"+possibleFile, thisFrame, true);
                                if (viewQuestion.isAnswerYes()) {
                                    currentLocation = possibleFile;
                                    loadDirectoryListing();

                                }else {
                                    lbox.removeSelectionInterval(usedIndex,usedIndex);
                                }
*/
                                                                         currentLocation = possibleFile;
                                                                         loadDirectoryListing();
                                                                         inActionListener = false;
                                                                     }
                                                                 }));

                                                  }
                                                  else {
                                                      inActionListener = false;
                                                  }
                                              }
                                          }
                                      });
        setVisible(true);
    }

    private void loadDirectoryListing() {
        try {
            lbox.setEnabled(false);
            MBC_Lbu_ok_.setEnabled(false);
            select_all_.setEnabled(false);
            setWaitCursor();

            setTitle("Path " + currentLocation.getAbsolutePath());
            AlphabetizedVector directories = new AlphabetizedVector();
            AlphabetizedVector files = new AlphabetizedVector();
            if(currentLocation.getPath().endsWith(":")) {
                currentLocation = new File(currentLocation.getPath() + File.separator);
            }
            String[] fileList = currentLocation.list();
            for(int i = 0; i < fileList.length; i++) {
                File currentFile = new File(currentLocation, fileList[i]);
                if(currentFile.isDirectory()) {
                    directories.addElement(fileList[i]);
                }
                else {
                    files.addElement(fileList[i]);
                }
            }
            Vector totalVector = new Vector();
            if(currentLocation.getParent() != null) {
                totalVector.addElement(parentDirectoryIndicator);
            }
            for(int i = 0; i < directories.size(); i++) {
                totalVector.addElement(((String) directories.elementAt(i))+File.separator);
            }
            for(int i = 0; i < files.size(); i++) {
                totalVector.addElement(files.elementAt(i));
            }
            lbox.setListData(totalVector);
            if(multi) {
                lbox.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
            }
            else {
                lbox.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
            }
            firstSelection = -1;
            lastSelection = -1;
        }
        finally {
            lbox.setEnabled(true);
            MBC_Lbu_ok_.setEnabled(true);
            select_all_.setEnabled(true);
            clearWaitCursor();
        }
    }

    /** getElementsSelected - returns the element selected from the list box
    * @return String elementSelected */
    public File[] getSelectedFiles() {
        return elementsSelected;
    }

    /** getElementSelected - returns the element selected from the list box
    * @return String elementSelected */
    public File getSelectedFile() {
        return elementSelected;
    }

    //Start Feature.SDWB1777:
    public void selectTree() {
        String msg=
        "This operation will select the entire directory tree begining at the root path \"" +
        currentLocation + "\" and including all files and subdirectories.\n"+
        "Do you want to proceed?";

        MBMsgBox msgBox = new MBMsgBox("Tree Select", msg, buttonPanel, true); 

        if(msgBox.isAnswerYes()) {
            Vector fileVect = new Vector();

            fileVect = getFileVect(fileVect, currentLocation.getPath());

            elementsSelected = new File[fileVect.size()];

            elementsSelected = (File[]) fileVect.toArray(elementsSelected);

            dispose();
        }
        else {
            return;
        }
    }

    Vector getFileVect(Vector vect, String arg) {
        File pathName = new File(arg);

        String[] fileNames = pathName.list();

        for(int i=0; i<fileNames.length; i++) {
            File tmpFile = new File(pathName.getPath(),fileNames[i]);

            if(tmpFile.isFile()) {
                vect.add(tmpFile);
            }

            if(tmpFile.isDirectory()) {
                getFileVect(vect, tmpFile.getPath());
            }
        }
        return vect;
    }
    //End Feature.SDWB1777:
}
