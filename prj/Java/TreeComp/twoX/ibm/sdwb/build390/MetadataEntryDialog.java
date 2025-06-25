package com.ibm.sdwb.build390;
/*********************************************************************/
/* Java MetadataEntryDialog class for the Build/390 client                          */
/*  enables the user to enter Metadata statements      */
/*********************************************************************/
// 04/17/2001 Creation
//
import java.awt.*;
import java.awt.event.*;
import java.util.*;
import javax.swing.*;
import java.text.ParseException;
import javax.swing.event.*;
import javax.swing.text.*;
import com.ibm.sdwb.build390.help.*;
/**
 * <br>The MetadataEntryDialog class enables the user to enter Metadata statements
 */
public class MetadataEntryDialog extends MBModalFrame {
    public static boolean IS_FILTER=true;
    public static boolean IS_NOT_FILTER=false;

    protected String userEntry = null;
    private String initialString = null;
//
    protected JButton okBt    = new JButton("OK");
    protected JButton helpBt  = new JButton("Help");
    protected JButton cancelBt= new JButton("Cancel");
//
    private MBButtonPanel buttonPanel;
    protected int maxLength = -1;
//
    private String keywordSelections[]=null;
//
    private static final String COMPARITOR_EQUAL="EQUAL";
    private static final String COMPARITOR_NOT_EQUAL="NOT EQUAL";
    private static final String COMPARITOR_LESS_THAN="LESS THAN";
    private static final String COMPARITOR_MORE_THAN="GREATER THAN";

    private String comparitorSelections[]=
    {
        COMPARITOR_EQUAL, 
        COMPARITOR_NOT_EQUAL,
        COMPARITOR_LESS_THAN,
        COMPARITOR_MORE_THAN
    };

    private String comparitorValues[]=
    {
        MetadataType.COMPARITOR_EQUAL,
        MetadataType.COMPARITOR_NOT_EQUAL,
        MetadataType.COMPARITOR_LESS_THAN,
        MetadataType.COMPARITOR_MORE_THAN
    };
//
    private static final String ASSIGNMENT_EQUAL="EQUAL";
    private String assignmentSelections[]={ASSIGNMENT_EQUAL};
    private String assignmentValues[]={MetadataType.ASSIGNMENT_EQUAL};
//
    private static final String BOOLEAN_TRUE="TRUE";
    private static final String BOOLEAN_FALSE="FALSE";

    private String booleanSelections[]={BOOLEAN_TRUE,BOOLEAN_FALSE};
    private String booleanValues[]={MetadataType.BOOLEAN_TRUE,MetadataType.BOOLEAN_FALSE};
//

    private static final String LOGICAL_AND="AND";
    private static final String LOGICAL_OR="OR";
    private String logicSelections[]={LOGICAL_AND,LOGICAL_OR};
    private String logicValues[]={MetadataType.LOGICAL_AND,MetadataType.LOGICAL_OR};
//
    private String displayKeyword="";
    private String displayComparitor="";
    private String displayLogicOper="";
    private String displayValue="";
    private String displayBoolean=MetadataType.BOOLEAN_TRUE;//"ON"
    private String displayText="";
    private String selectedType="";
//
    protected JPanel contentPane;
    protected JPanel valueEntryPanel = new JPanel();
    protected JPanel btPanel=new JPanel();
//
    protected JComboBox keywordSelector;
    protected JComboBox comparitorSelector;
    protected JComboBox booleanSelector = new JComboBox(booleanSelections);
    protected JComboBox logicSelector = new JComboBox(logicSelections);
//
    protected JLabel Msg = new JLabel("Enter or select element values to compose a Metadata statement");
    protected JLabel keySelLabel = new JLabel("Keyword");
    protected JLabel compSelLabel;
    protected JLabel valueSelLabel = new JLabel("Value");
    protected JLabel logicSelLabel = new JLabel("Logic Operator");
    protected JLabel metaStatLabel = new JLabel("Metadata Statement");
//
    protected JTextField metaStatement = new JTextField();
    protected JTextField valueEntryField = new JTextField();
//
    private CardLayout valueEntryLayout = new CardLayout();

    private MetadataType[] metadataTypes;
    private boolean isFilter=false;

    private boolean manualEntryMode=false;

    private JCheckBox manualEntryChkBox=new  JCheckBox("Manual Entry");

    private boolean chkBoxSelfEvent=false;
    /** Constructor - Builds the frame and populates it with the entry field and buttons.
    * It also adds the action listeners.
    */
    public MetadataEntryDialog(JInternalFrame parentFrame, MetadataType[] metadataTypes, boolean isFilter) {
        super("Metadata Entry", parentFrame, null);
        this.metadataTypes=metadataTypes;
        this.isFilter=isFilter;
        initialize();
    }

    public MetadataEntryDialog(JInternalFrame parentFrame, int maxLen, MetadataType[] metadataTypes, boolean isFilter) {
        super("Metadata Entry", parentFrame, null);

        this.metadataTypes=metadataTypes;
        this.isFilter=isFilter;

        maxLength = maxLen;
        initialize();
    }

    public MetadataEntryDialog(String tempInit, JInternalFrame parentFrame, MetadataType[] metadataTypes, boolean isFilter) {
        super("Metadata Entry", parentFrame, null);

        this.metadataTypes=metadataTypes;
        this.isFilter=isFilter;

        initialString = tempInit;
        initialize();
    }

    public MetadataEntryDialog(String tempInit, JInternalFrame parentFrame, int maxLen, MetadataType[] metadataTypes, boolean isFilter) {
        super("Metadata Entry", parentFrame, null);

        this.metadataTypes=metadataTypes;
        this.isFilter=isFilter;

        maxLength = maxLen;
        initialString = tempInit;
        initialize();
    }

    private void initialize() {
        keywordSelections=new String[metadataTypes.length];

        for (int i=0;i!=metadataTypes.length;i++) {//copy array out of metadataTypes
            keywordSelections[i]= metadataTypes[i].getKeyword();
        }

        keywordSelector = new JComboBox(keywordSelections);

        keywordSelector.setEditable(false);

        setForeground(MBGuiConstants.ColorRegularText);
        setBackground(MBGuiConstants.ColorGeneralBackground);

        contentPane = (JPanel) this.getContentPane();
        contentPane.setLayout(new GridBagLayout());

        keywordSelector.addActionListener(new java.awt.event.ActionListener() {
                                              public void actionPerformed(ActionEvent e) {
                                                  keywordSelector_actionPerformed(e);
                                              }
                                          });

        if (isFilter) {
            comparitorSelector=new JComboBox(comparitorSelections);

            compSelLabel = new JLabel("Comparitor");
        } else {
            comparitorSelector=new JComboBox(assignmentSelections);

            compSelLabel = new JLabel("Assignment");
        }

        comparitorSelector.addActionListener(new java.awt.event.ActionListener() {
                                                 public void actionPerformed(ActionEvent e) {
                                                     comparitorSelector_actionPerformed(e);
                                                 }
                                             });

        if (isFilter) {
            logicSelector.addActionListener(new java.awt.event.ActionListener() {
                                                public void actionPerformed(ActionEvent e) {
                                                    logicSelector_actionPerformed(e);
                                                }
                                            });
        }

        valueEntryField.addActionListener(new java.awt.event.ActionListener() {
                                              public void actionPerformed(ActionEvent e) {
                                                  valueEntryField_actionPerformed(e);
                                              }
                                          });

        valueEntryPanel.setLayout(valueEntryLayout);

        valueEntryPanel.add(booleanSelector,"booleanSelector");

        booleanSelector.addActionListener(new java.awt.event.ActionListener() {
                                              public void actionPerformed(ActionEvent e) {
                                                  boolean_actionPerformed(e);
                                              }
                                          });

        valueEntryField.setHorizontalAlignment(JTextField.LEFT);
        valueEntryField.setText("           ");

        valueEntryPanel.add(valueEntryField, "valueEntryField");

        Msg.setForeground(Color.red);

        contentPane.add(Msg,
                        new GridBagConstraints(0, 0, 4, 1, 0.0, 0.0,
                                               GridBagConstraints.CENTER,
                                               GridBagConstraints.NONE,
                                               new Insets(5, 5, 5, 5), 0, 0));

        contentPane.add(keySelLabel,
                        new GridBagConstraints(0, 1, 1, 1, 0.0, 0.0,
                                               GridBagConstraints.CENTER,
                                               GridBagConstraints.NONE,
                                               new Insets(5, 5, 5, 5), 0, 0));

        contentPane.add(compSelLabel,
                        new GridBagConstraints(1, 1, 1, 1, 0.0, 0.0,
                                               GridBagConstraints.CENTER,
                                               GridBagConstraints.NONE,
                                               new Insets(5, 5, 5, 5), 0, 0));

        contentPane.add(valueSelLabel,
                        new GridBagConstraints(2, 1, 1, 1, 0.0, 0.0,
                                               GridBagConstraints.CENTER,
                                               GridBagConstraints.NONE,
                                               new Insets(5, 5, 5, 5), 0, 0));
        if (isFilter) {
            contentPane.add(logicSelLabel,
                            new GridBagConstraints(3, 1, 1, 1, 0.0, 0.0,
                                                   GridBagConstraints.CENTER,
                                                   GridBagConstraints.NONE,
                                                   new Insets(5, 5, 5, 5), 0, 0));
        }

        contentPane.add(keywordSelector,
                        new GridBagConstraints(0, 2, 1, 1, 0.0, 0.0,
                                               GridBagConstraints.CENTER,
                                               GridBagConstraints.NONE,
                                               new Insets(5, 5, 5, 5), 0, 0));
        contentPane.add(comparitorSelector,
                        new GridBagConstraints(1, 2, 1, 1, 0.0, 0.0,
                                               GridBagConstraints.CENTER,
                                               GridBagConstraints.NONE,
                                               new Insets(5, 5, 5, 5), 0, 0));
        contentPane.add(valueEntryPanel,
                        new GridBagConstraints(2, 2, 1, 1, 0.0, 0.0,
                                               GridBagConstraints.CENTER,
                                               GridBagConstraints.NONE,
                                               new Insets(5, 5, 5, 5), 0, 0));
        if (isFilter) {
            contentPane.add(logicSelector,
                            new GridBagConstraints(3, 2, 1, 1, 0.0, 0.0,
                                                   GridBagConstraints.CENTER,
                                                   GridBagConstraints.NONE,
                                                   new Insets(5, 5, 5, 5), 0, 0));
        }


        contentPane.add(manualEntryChkBox,
                        new GridBagConstraints(0, 3, 1, 1, 0.0, 0.0,
                                               GridBagConstraints.CENTER,
                                               GridBagConstraints.HORIZONTAL,
                                               new Insets(5, 5, 5, 5), 20, 0));


        manualEntryChkBox.setSelected(false);

        manualEntryChkBox.addItemListener(new java.awt.event.ItemListener() {
                                              public void itemStateChanged(ItemEvent e) {
                                                  manualEntryChkBox_ItemEvent(e);
                                              }
                                          });


        metaStatLabel.setHorizontalAlignment(JLabel.CENTER);

        contentPane.add(metaStatLabel,
                        new GridBagConstraints(0, 4, 4, 1, 0.0, 0.0,
                                               GridBagConstraints.CENTER,
                                               GridBagConstraints.HORIZONTAL,
                                               new Insets(5, 5, 5, 5), 20, 0));

        metaStatement.setBackground(Color.white);


        metaStatement.setEditable(false);

        contentPane.add(metaStatement,
                        new    GridBagConstraints(0, 5, 4, 1, 0.0, 0.0,
                                                  GridBagConstraints.CENTER,
                                                  GridBagConstraints.HORIZONTAL,
                                                  new Insets(5, 5, 5, 5),20, 0));
//
        btPanel.setLayout(new BoxLayout(btPanel, BoxLayout.X_AXIS));

        btPanel.add(Box.createHorizontalGlue());

        btPanel.add(okBt);
        btPanel.add(Box.createHorizontalGlue());

        btPanel.add(helpBt);
        btPanel.add(Box.createHorizontalGlue());

        btPanel.add(cancelBt);
        btPanel.add(Box.createHorizontalGlue());
//
        contentPane.add(btPanel,
                        new GridBagConstraints(0, 6, 4, 1, 0.0, 0.0,
                                               GridBagConstraints.CENTER,
                                               GridBagConstraints.HORIZONTAL,
                                               new Insets(5, 5, 5, 5),0, 0));

        okBt.setForeground(MBGuiConstants.ColorActionButton);
        helpBt.setForeground(MBGuiConstants.ColorHelpButton);
        cancelBt.setForeground(MBGuiConstants.ColorCancelButton);

        // OK button
        okBt.addActionListener(new ActionListener () {
                                   public void actionPerformed(ActionEvent evt) {
                                       doOk();
                                   }
                               });
        // Help button
        helpBt.addActionListener(new ActionListener () {
                                     public void actionPerformed(ActionEvent evt) {
                                         MBUtilities.ShowHelp("HDRDEFINE",HelpTopicID.PROCESS_REFERENCE_METADATA);
                                     }
                                 });

        // Quit button
        cancelBt.addActionListener(new ActionListener () {
                                       public void actionPerformed(ActionEvent evt) {
                                           dispose();
                                       }
                                   });

        if (initialString!=null) {
            metaStatement.setText(initialString);

            parse(initialString);//try to parse
        } else {
            keywordSelector.setSelectedIndex(0);

            comparitorSelector.setSelectedIndex(0);

            selectedType= metadataTypes[keywordSelector.getSelectedIndex()].getType();

            if (selectedType.equals(MetadataType.BOOLEAN_TYPE)) {
                valueEntryLayout.show(valueEntryPanel,"booleanSelector");
                booleanSelector.setSelectedIndex(0);
                displayBoolean=MetadataType.BOOLEAN_TRUE;
            } else {
                valueEntryLayout.show(valueEntryPanel,"valueEntryField");
                valueEntryField.setText("");
            }

            if (isFilter) {
                logicSelector.setSelectedIndex(0);
            }
            refreshMetadataStatement();
        }

        valueEntryField.getDocument().addDocumentListener(new MetadataValueDocumentListener());
        valueEntryField.getDocument().putProperty("name", "Text Field");


        setResizable(true); // FixMinSize

        setVisible(true);
    }

    public void postVisibleInitialization() {
        metaStatement.requestFocus();
    }

    class WarnMsg  extends Thread {
        private String msg;

        WarnMsg(String msg) {
            this.msg=msg;
        }

        public void run() {
            {    
                new MBMsgBox("Warning",msg,thisFrame);
                metaStatement.requestFocus();

            }
        } 
    }

    boolean hasLowerCase(String s) {
        boolean hasLow=false;
        char charAry[]=s.toCharArray();

        for (int i=0;i!=charAry.length;i++) {
            if (Character.isLowerCase(charAry[i])) {
                hasLow=true;
                break;
            }
        }

        return hasLow;
    }

    public void doOk () {
        if (manualEntryMode) {
            if (metaStatement.getText().length() < 0) {                            // check for length > 0
                new WarnMsg("No Metadata Statement is defined").start();
                return;
            }
        } else {
            if (displayKeyword.length()==0) {
                new WarnMsg("No Keyword has been selected").start();
                return;
            }
            if (displayComparitor.length()==0) {
                new WarnMsg("No Comparitor has been selected").start();
                return;
            }

            // valueEntryField.postActionEvent();//insert this if users want auto update from valueEntryField

            if (displayValue.length()==0) {
                new WarnMsg("No Value has been selected").start();
                return;
            }
            if (isFilter) {
                if (displayLogicOper.length()==0) {
                    new WarnMsg("No Logic has been selected").start();
                    return;
                }
            }
        }

        if (metaStatement.getText().length() > 0) {                              // check for length > 0
            userEntry = new String(metaStatement.getText().trim());            // get the text from the pw field
            if (!verifyEntry()) {
                return;
            }
            // clean up and get out
            dispose();
        }
    }

    private boolean verifyEntry() {

        selectedType= metadataTypes[keywordSelector.getSelectedIndex()].getType();
        if (manualEntryMode) {
            //where manual entry  checking happens.
            String output = "";
            output = validate(userEntry,output);
            if (output.trim().length() > 0) {
                userEntry = output;
            }
            return(output.trim().length() > 0); 
        } else if (selectedType.equals(MetadataType.NUMERICAL_TYPE)) {
            if (displayValue.length()>7) {
                new WarnMsg("Numerical values may only contain 7 digits").start();
                return false;
            }
            if ((displayValue.indexOf("-")!=-1)| (displayValue.indexOf("+")!=-1)) {
                new WarnMsg("Numerical values must be unsigned").start();
                return false;
            }
            try {
                Integer.parseInt(displayValue);
            } catch (NumberFormatException nfe) {
                new WarnMsg("Numerical value is invalid").start();
                return false;
            }
        } else if ((selectedType.equals(MetadataType.SINGLE_ENTRY_TYPE))|(selectedType.equals(MetadataType.MULT_ENTRY_TYPE))) {

            if (displayValue.indexOf(" ")!=-1) {//if any space in displayValue
                if (!(displayValue.startsWith("'")&displayValue.endsWith("'"))) {//must inclose in single quotes
                    new WarnMsg("Text values that contain spaces must be enclosed in single quotes").start();
                    return false;
                }
            }
            if (displayValue.indexOf(",")!=-1) {//if any commas in displayValue
                if (!(displayValue.startsWith("'")&displayValue.endsWith("'"))) {//must inclose in single quotes
                    new WarnMsg("Text values that contain commas must be enclosed in single quotes").start();
                    return false;
                }
            }
            if (hasLowerCase(displayValue)) {
                if (!(displayValue.startsWith("'")&displayValue.endsWith("'"))) {
                    new WarnMsg("Text values with lower case characters must be enclosed in single quotes").start();
                    return false;
                }
            }
            if (displayValue.indexOf("'")!=-1) {//if displayValue contains '
                if (!(displayValue.startsWith("'")&displayValue.endsWith("'")&(displayValue.length()>1))) {
                    new WarnMsg("Text values containing single quotes must begin and end with them").start();
                    return false;
                }
            }
            if (displayValue.length()>50) {//if displayValue contains '
                new WarnMsg("Text values may only contain 50 characters").start();
                return false;
            }
        }
        return true;
    }



    private String validate(String input,String output) {
        int index = -1;
        if (input.indexOf(",") > -1) {
            index = input.indexOf(",");
        } else if (input.indexOf("|") > -1) {
            index = input.indexOf("|");
        } else if (index < 0 && input !=null && input.trim().length() >0) {
            new WarnMsg("The entry " + input + " doesnot end with a , (or) |.\n\n" + "The valid entry should be in format <keyword> <comparitor> <value><, or |>.\nEach entry should end with a , (or) |.\n\nValid examples:\nALEVEL EQ TEST,\nAPARM NE TEST| ASSEM EQ PLX,").start();
            return "";
        }
        String temp = validateSingle(input.substring(0,index+1));
        if (index > -1 && temp.trim().length() > 0) {
            output += temp;
            String nextStr = input.substring(index + 1);
            if (nextStr !=null && nextStr.trim().length() > 0) {
                output =   validate(nextStr,output);
            }
            return output;
        } else {
            new WarnMsg("The input entry " + input + " doesnot end with a , (or) |.\n" + "The valid entry should be in format <keyword> <comparitor> <value><, or |>.\nEach entry should end with a , (or) |.\n\nValid examples:\nALEVEL EQ TEST,\nAPARM NE TEST| ASSEM EQ PLX,").start();
            return "";
        }
    }

    private String validateSingle(String input) {
        String output ="";
        StringTokenizer strk  = new StringTokenizer(input," ");
        if (strk.countTokens()  >= 3 && strk.countTokens() <=4) {
            int i=-1;
            while (strk.hasMoreTokens()) {
                i++;
                String tok = strk.nextToken().replaceAll(" ","");
                if (i==1) {
                    if (Arrays.binarySearch(comparitorValues,tok) < 0 ) {
                        new WarnMsg("The input entry " + input + " contains an invalid comparitor " + tok + ".\n\nValid comparitors are : " + Arrays.asList(comparitorValues).toString()  + ".\n\n" + "The valid entry should be in format <keyword> <comparitor> <value><, or |>.\nEach entry should end with a , (or) |.\n\nValid examples:\nALEVEL EQ TEST,\nAPARM NE TEST| ASSEM EQ PLX,").start();
                    }
                }
                if (tok.matches("[|]|[,]")) {
                    output += tok;
                } else {
                    output += " " + tok; 
                }
            }
            return output;
        } else {
            new WarnMsg("The input entry " + input + " contains " + strk.countTokens() + " words.\n\n" + "The valid entry should be in format <keyword> <comparitor> <value><, or |>.\nEach entry should end with a , (or) |.\n\nValid examples:\nALEVEL EQ TEST,\nAPARM NE TEST| ASSEM EQ PLX,").start();
            return "";
        }
    }

    public Dimension getPreferredSize() {
        Dimension oldPref = super.getPreferredSize();
        oldPref.width = 450;
        oldPref.height= 275;
        return oldPref;
    }

    public String getText() {
        return userEntry;
    }

    void refreshMetadataStatement() {
        metaStatement.setText(displayKeyword.trim()+" "+displayComparitor.trim()+" "+displayValue.trim()+displayLogicOper.trim());
    }

    void keywordSelector_actionPerformed(ActionEvent e) {
        displayKeyword=(String)keywordSelector.getSelectedItem();

        selectedType= metadataTypes[keywordSelector.getSelectedIndex()].getType();

        if (selectedType.equals(MetadataType.BOOLEAN_TYPE)) {
            valueEntryLayout.show(valueEntryPanel,"booleanSelector");
            displayValue=displayBoolean;
        } else {
            valueEntryLayout.show(valueEntryPanel,"valueEntryField");
            displayValue=displayText;
        }
        refreshMetadataStatement();
    }

    void comparitorSelector_actionPerformed(ActionEvent e) {
        String s=(String)comparitorSelector.getSelectedItem();

        if (isFilter) {
            if (s.equals(COMPARITOR_EQUAL))displayComparitor=MetadataType.COMPARITOR_EQUAL;
            if (s.equals(COMPARITOR_NOT_EQUAL))displayComparitor=MetadataType.COMPARITOR_NOT_EQUAL;
            if (s.equals(COMPARITOR_LESS_THAN))displayComparitor=MetadataType.COMPARITOR_LESS_THAN;
            if (s.equals(COMPARITOR_MORE_THAN))displayComparitor=MetadataType.COMPARITOR_MORE_THAN;
        } else {
            if (s.equals(ASSIGNMENT_EQUAL))displayComparitor=MetadataType.ASSIGNMENT_EQUAL;
        }
        refreshMetadataStatement();
    }

    void logicSelector_actionPerformed(ActionEvent e) {
        String s=(String)logicSelector.getSelectedItem();
        if (s.equals(LOGICAL_AND))displayLogicOper=MetadataType.LOGICAL_AND;//",";
        if (s.equals(LOGICAL_OR))displayLogicOper=MetadataType.LOGICAL_OR;//"|";

        refreshMetadataStatement();
    }

    void valueEntryField_actionPerformed(ActionEvent e) {
        displayText=valueEntryField.getText().trim();

        valueEntryField.setText(displayText);

        displayValue=displayText;

        refreshMetadataStatement();
    }


    class MetadataValueDocumentListener implements DocumentListener {

        public void insertUpdate(DocumentEvent e) {
            update(e);
        }
        public void removeUpdate(DocumentEvent e) {
            update(e);
        }
        public void changedUpdate(DocumentEvent e) {
            //Plain text components don't fire these events.
        }

        public void update(DocumentEvent e) {
            try {
                Document doc = (Document)e.getDocument();
                int changeLength = e.getLength();
                String currentValue = doc.getText(doc.getStartPosition().getOffset(),doc.getEndPosition().getOffset());
                displayValue=currentValue.trim();
                refreshMetadataStatement();
                valueEntryField.setCaretPosition(valueEntryField.getDocument().getLength());
            } catch (BadLocationException ble) {
                ble.printStackTrace();
            }
        }
    }


    void boolean_actionPerformed(ActionEvent e) {
        String s=(String)booleanSelector.getSelectedItem();

        if (s.equals(BOOLEAN_TRUE))displayBoolean=MetadataType.BOOLEAN_TRUE;//"ON"
        if (s.equals(BOOLEAN_FALSE))displayBoolean=MetadataType.BOOLEAN_FALSE;//"OFF"

        displayValue=displayBoolean;

        refreshMetadataStatement();
    }

    void manualEntryChkBox_ItemEvent(ItemEvent e) {
        if (e.getStateChange() == ItemEvent.SELECTED) {
            metaStatement.setEditable(true);
            keywordSelector.setEnabled(false);   
            comparitorSelector.setEnabled(false);   
            booleanSelector.setEnabled(false); 
            logicSelector.setEnabled(false); 
            valueEntryField.setEnabled(false); 
            manualEntryMode=true;
        } else
            if (e.getStateChange() == ItemEvent.DESELECTED) {

            /*TST1915
            if (chkBoxSelfEvent) {// if this listener caused the event
                   chkBoxSelfEvent=false;
                   return;
               }
              */

            //TST2214
            if (chkBoxSelfEvent) {// if this listener caused the event
                chkBoxSelfEvent=false;
                return;
            }


            SwingUtilities.invokeLater(new Runnable() {
                                           public void run() {
                                               int i=parse(metaStatement.getText());

                                               if (i==-1) {//if line not parsable

                                                   new Thread(new Runnable() {
                                                                  public void run() {
                                                                      MBMsgBox msgBox = new MBMsgBox ("Warning","Unable to parse line. "+ "Leaving manual mode may overwrite portions of the current Metadata statement. "+
                                                                                                      "Are you sure you want to do this?", thisFrame,true);


                                                                      if (!msgBox.isAnswerYes()) { //if answer is NO
                                                                          chkBoxSelfEvent=true;
                                                                          manualEntryChkBox.setSelected(true);
                                                                          metaStatement.postActionEvent();
                                                                          return;
                                                                      } else {
                                                                          chkBoxSelfEvent=true;

                                                                          manualEntryChkBox.setSelected(false);

                                                                          metaStatement.setEditable(false);
                                                                          keywordSelector.setEnabled(true);   
                                                                          comparitorSelector.setEnabled(true);           
                                                                          booleanSelector.setEnabled(true);  
                                                                          logicSelector.setEnabled(true); 
                                                                          valueEntryField.setEnabled(true); 

                                                                          refreshMetadataStatement();

                                                                          manualEntryMode=false;

                                                                      }
                                                                  }
                                                              }).start();
                                               } else {
                                                   chkBoxSelfEvent=true;

                                                   manualEntryChkBox.setSelected(false);

                                                   metaStatement.setEditable(false);
                                                   keywordSelector.setEnabled(true);   
                                                   comparitorSelector.setEnabled(true);           
                                                   booleanSelector.setEnabled(true);  
                                                   logicSelector.setEnabled(true); 
                                                   valueEntryField.setEnabled(true); 

                                                   refreshMetadataStatement();


                                                   manualEntryMode=false;
                                               }


                                           }
                                       });
        }
    }

    int parse(String init) {
        int keyIndex;
        StringTokenizer tok=new StringTokenizer(init," ");
        String token=null;
        try {
            if (isFilter) {
                if (tok.countTokens()!=3) {
                    throw(new ParseException("Parser Error",0));
                }
            }

            token=tok.nextToken(); //get keyword

            keyIndex=Arrays.binarySearch(keywordSelections,token);

            if (keyIndex<0) {//if not in list
                throw(new ParseException("Parser Error",1));
            } else {
                displayKeyword=token;
                keywordSelector.setSelectedItem(displayKeyword); // set keyword
            }

            token=tok.nextToken();//get comparator


            boolean inList=false;

            if (isFilter) {
                for (int n=0;n!=comparitorValues.length;n++) {
                    if (comparitorValues[n].equals(token)) {//if in list
                        inList=true;
                        break;
                    }
                }
                if (!inList) { /*if the comparitor is invalid, grab the one that out there and use it */
                    comparitorSelector_actionPerformed(new ActionEvent(this,ActionEvent.ACTION_PERFORMED,"COMPARITOR_SELECTOR"));
                    inList=true;
                }

            } else {
                if (assignmentValues[0].equals(token)) {//if in list
                    inList=true;
                }
            }


            if (!inList) {//if not in list
                throw(new ParseException("Parser Error",2));
            } else {
                if (isFilter) {
                    if (token.equals(MetadataType.COMPARITOR_EQUAL))comparitorSelector.setSelectedItem(COMPARITOR_EQUAL);
                    if (token.equals(MetadataType.COMPARITOR_NOT_EQUAL))comparitorSelector.setSelectedItem(COMPARITOR_NOT_EQUAL);
                    if (token.equals(MetadataType.COMPARITOR_LESS_THAN))comparitorSelector.setSelectedItem(COMPARITOR_LESS_THAN);
                    if (token.equals(MetadataType.COMPARITOR_MORE_THAN))comparitorSelector.setSelectedItem(COMPARITOR_MORE_THAN);
                } else {
                    if (token.equals(MetadataType.ASSIGNMENT_EQUAL))comparitorSelector.setSelectedItem(ASSIGNMENT_EQUAL);
                }
            }

            if (!tok.hasMoreTokens()) { /*TST1915*/
                token="";
            } else {
                token = tok.nextToken();
            }

            while (tok.hasMoreTokens()) { //append the rest
                token+=" "+tok.nextToken();
            }

            if (isFilter) {
                if (token.endsWith(MetadataType.LOGICAL_AND)) {//","
                    displayLogicOper=LOGICAL_AND;//"AND";
                } else
                    if (token.endsWith(MetadataType.LOGICAL_OR)) {//"|"
                    displayLogicOper=LOGICAL_OR;//"OR";
                } else {
                    throw(new ParseException("Parser Error",3));
                }  

                logicSelector.setSelectedItem(displayLogicOper);// set the logic

                token=token.substring(0,token.length()-1); // clip the logic off the end
            }
            selectedType= metadataTypes[keyIndex].getType();

            if (selectedType.equals(MetadataType.BOOLEAN_TYPE)) {
                if (token.equals(MetadataType.BOOLEAN_TRUE)) {
                    displayBoolean=BOOLEAN_TRUE;//"ON"
                } else
                    if (token.equals(MetadataType.BOOLEAN_FALSE)) {
                    displayBoolean=BOOLEAN_FALSE;//"OFF"
                } else {
                    throw(new ParseException("Parser Error",4));
                }

                displayValue=displayBoolean;

                valueEntryLayout.show(valueEntryPanel,"booleanSelector");

                booleanSelector.setSelectedItem(displayBoolean); // set boolean 
            } else {
                if (selectedType.equals(MetadataType.NUMERICAL_TYPE)) {
                    if (!isValidNum(token)) {
                        throw(new ParseException("Parser Error",5));
                    }
                }

                displayValue=token;

                displayText=displayValue;

                valueEntryField.setText(displayValue);

                valueEntryLayout.show(valueEntryPanel,"valueEntryField"); // set text value
            }
        } catch (ParseException pe) {
            //  System.out.println("parser error offset="+pe.getErrorOffset());//***BE

            manualEntryMode=true;

            manualEntryChkBox.setSelected(true);

            metaStatement.setText(init);

            return -1;
        }

        refreshMetadataStatement();

        return 0;
    }

    private boolean isValidNum(String num) {
        if (num.length()>7) {
            return false;
        }
        if ((num.indexOf("-")!=-1)| (num.indexOf("+")!=-1)) {
            return false;
        }
        try {
            Integer.parseInt(num);
        } catch (NumberFormatException nfe) {
            return false;
        }
        return true;
    }
}









