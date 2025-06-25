package com.ibm.sdwb.build390;
/*********************************************************************/
/* Java TextEntryDialog class for the Build/390 client                          */
/*  Asks the user for a String and returns it to the caller        */
/*********************************************************************/
// 05/17/99 FixMinSize      Set minimum window size
//
import java.awt.*;
import java.awt.event.*;
import java.util.*;
import javax.swing.*;

/** <br>The TextEntryDialog class displays an entry field for the user to enter the password into.
* it then sets the passowrd in MBClient. */
public class TextEntryDialog extends MBModalFrame {

    protected String userEntry = null;
    private String initialString = null;
    protected JTextField tf1;
    protected JLabel userLabel;
    protected JButton   MBC_Lbu_ok_      = new JButton("OK");
    protected JButton   MBC_Lbu_quit_    = new JButton("Cancel");
    private ActionListener qh;
    private ActionListener selh ;
    private MBButtonPanel buttonPanel;
    protected int maxLength = -1;
    private boolean metadataEntry = false;
    private boolean allowBlankAndComma = false;

    /** Constructor - Builds the frame and populates it with the entry field and buttons.
    * It also adds the action listeners.
    */
    public TextEntryDialog(String question, JInternalFrame parentFrame) {
        super("User prompt", parentFrame, null);
        initialize(question);
    }

    public TextEntryDialog(String question, JInternalFrame parentFrame, int maxLen) {
        super("User prompt", parentFrame, null);
        maxLength = maxLen;
        initialize(question);
    }

    public TextEntryDialog(String question, String tempInit, JInternalFrame parentFrame) {
        super("User prompt", parentFrame, null);
        initialString = tempInit;
        initialize(question);

    }

    public TextEntryDialog(String question, String tempInit, JInternalFrame parentFrame, int maxLen) {
        super("User prompt", parentFrame, null);
        maxLength = maxLen;
        initialString = tempInit;
        initialize(question);

    }

    /** Constructor - Builds the frame and populates it with the entry field and buttons.
    * It also adds the action listeners.
    */
    public TextEntryDialog(String question, JInternalFrame parentFrame, boolean isMetadata, boolean tempAllow) {
        super("User prompt", parentFrame, null);
        metadataEntry = isMetadata;
        allowBlankAndComma = tempAllow;
        initialize(question);
    }

    public TextEntryDialog(String question, JInternalFrame parentFrame, int maxLen, boolean isMetadata, boolean tempAllow) {
        super("User prompt", parentFrame, null);
        maxLength = maxLen;
        metadataEntry = isMetadata;
        allowBlankAndComma = tempAllow;
        initialize(question);
    }

    public TextEntryDialog(String question, String tempInit, JInternalFrame parentFrame, boolean isMetadata, boolean tempAllow) {
        super("User prompt", parentFrame, null);
        initialString = tempInit;
        metadataEntry = isMetadata;
        allowBlankAndComma = tempAllow;
        initialize(question);

    }

    public TextEntryDialog(String question, String tempInit, JInternalFrame parentFrame, int maxLen, boolean isMetadata, boolean tempAllow) {
        super("User prompt", parentFrame, null);
        metadataEntry = isMetadata;
        maxLength = maxLen;
        initialString = tempInit;
        allowBlankAndComma = tempAllow;
        initialize(question);

    }

    private void initialize(String question) {
        userLabel = new JLabel(question);
        if (metadataEntry) {
            if (maxLength>0) {
                tf1 = new MetadataLimitedTextfield(maxLength);
            } else {
                tf1 = new MetadataLimitedTextfield(255);
            }
            ((MetadataLimitedTextfield) tf1).allowBlanksAndCommas(allowBlankAndComma);
        } else {
            if (maxLength > 0) {
                tf1 = new LimitedEntryTextfield(maxLength);
            } else {
                tf1 = new JTextField();
            }
        }
        setForeground(MBGuiConstants.ColorRegularText);
        setBackground(MBGuiConstants.ColorGeneralBackground);
        GridBagLayout gridBag = new GridBagLayout();
        GridBagConstraints c = new GridBagConstraints();
        c.gridx = 1;
        c.gridy = 1;
        if (initialString != null) {
            tf1.setText(initialString);
        }
        getContentPane().setLayout(gridBag);

        tf1.setBackground(MBGuiConstants.ColorFieldBackground);
        tf1.addActionListener(new ActionListener() {
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
        gridBag.setConstraints(tf1, c);
        getContentPane().add(tf1);
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
        tf1.requestFocus();
    }

    public void doOk () {
        if (tf1.getText().length() > 0) {                          // check for length > 0
            userEntry = new String(tf1.getText());          // get the text from the pw field
            // clean up and get out
            dispose();
        }
    }

    public Dimension getMinimumSize() {
        Dimension oldPref = super.getMinimumSize();
        oldPref.width = 200;
        return oldPref;
    }


    public String getText() {
        return userEntry;
    }
}
