package com.ibm.sdwb.build390;
/*********************************************************************/
/* MBUserBuildModelAfterDialog class for the Build/390 client        */
/*  Handles the definition of a model for a new part in user build   */
/*********************************************************************/
// Changes
// Date     Defect/Feature      Reason
//12/03/2002 SDWB-2019 Enhance the help system
//09/13/2003 #DEF.TST1537: Fix Model help link
/*********************************************************************/
import java.awt.*;
import java.awt.List;
import java.awt.event.*;
import java.awt.event.KeyEvent;
import java.io.*;
import java.util.*;
import javax.swing.*;
import javax.swing.event.*;
import javax.swing.border.*;
import com.ibm.sdwb.build390.help.*;
import com.ibm.sdwb.build390.userinterface.graphic.MainInterface;

/** Create the driver build page */
public class MBUserBuildModelAfterDialog extends MBModalFrame implements Serializable {
    private MBBuild     build   = null;
    private Hashtable   cmdHash = new Hashtable();
    private JButton btHelp      = new JButton("Help");
   	private JButton btOk        = new JButton("Ok");
   	private JLabel Label1       = new JLabel("Class Name");
   	private JLabel Label2       = new JLabel("Module Name");
   	private String Label2a      = new String("Class Name");
   	private String Label2b      = new String("Root Path");
   	private String Label1a      = new String("Module Name");
   	private String Label1b      = new String("Part Name");
	private JTextField class_Path = new JTextField("");
	private JTextField mod_Part   = new JTextField("");
   	private MBButtonPanel tempButt;
   	protected GridBagLayout gridBag = new GridBagLayout();
   	protected JPanel centerPanel  = new JPanel(gridBag);
   	protected JPanel rpanel;
   	protected JPanel bpanel; // = new JPanel();
   	protected JPanel classPanel  = new JPanel(gridBag);
    private JRadioButton libname = new JRadioButton("Indicate the library name of an existing part to model this one after");
	private JRadioButton modname = new JRadioButton("Indicate the mod and class name of an existing part to model this one after");
	private ButtonGroup Group1  = new ButtonGroup();

    /**
    * constructor - Create a MBUserBuildModelAfterDialog
    */
    public MBUserBuildModelAfterDialog(MBBuild bld, Hashtable inHash, MBInternalFrame pFrame) throws com.ibm.sdwb.build390.MBBuildException{
        super("Define Model for ", pFrame, null);
        cmdHash = inHash;
        build = bld;
        initializeDialog(build);
    }

    public void initializeDialog(MBBuild tempBuildParm) throws com.ibm.sdwb.build390.MBBuildException {
        build   = tempBuildParm;
		GridBagLayout gridBag = new GridBagLayout();
		bpanel = new JPanel(gridBag);
		rpanel = new JPanel(gridBag);
		setForeground(MBGuiConstants.ColorRegularText);
		setBackground(MBGuiConstants.ColorGeneralBackground);

        // get name of part that needs a model
        String newpart = (String)cmdHash.get("NEWPART");
        // set the title to show the name
        setTitle("Define Model for "+newpart);
        // get previously set model for this part
        String cmodel_mod_part   = (String)cmdHash.get("MODEL_MOD_PART");
        String cmodel_class_path = (String)cmdHash.get("MODEL_CLASS_PATH");
        // get previously set model type for this part
        String ctype  = (String)cmdHash.get("MODELTYPE");
        boolean inittypemod = false;
        if (ctype!=null) {
            if (ctype.equals("MOD.CLASS")) {
                inittypemod=true;
            }
        }

        // help button
   		btHelp.setForeground(MBGuiConstants.ColorHelpButton);
        btHelp.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                try {
                    //MBUtilities.ShowHelp("Modeling");
                    //#DEF.TST1537:
                    MBUtilities.ShowHelp("SPTMODLING",HelpTopicID.MODELPARTSDIALOG_HELP);
                }finally {           }
        }} );

        // OK button
        // Set the model name for the new part
 		btOk.setForeground(MBGuiConstants.ColorActionButton);
        btOk.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                if (mod_Part.getText() != null & class_Path.getText() != null) {
                    String model_mod_part = mod_Part.getText().trim();
                    String model_class_Path = class_Path.getText().trim();
                    if (modname.isSelected()) {
                        model_mod_part = model_mod_part.toUpperCase();
                        model_class_Path = model_class_Path.toUpperCase();
                    }

                    // if mod_name then both fields must be set, otherwise just the mod_part field must be set but both can be
                    if (model_mod_part.trim().length() > 0) {
                        if (modname.isSelected()) {
                            if (model_class_Path.trim().length() > 0) {
                                cmdHash.put("MODEL_MOD_PART", model_mod_part);
                                cmdHash.put("MODEL_CLASS_PATH", model_class_Path);
                                cmdHash.put("MODELTYPE", "MOD.CLASS");
                            }
                        } else {
                            if (model_class_Path==null) model_class_Path="";
                            cmdHash.put("MODEL_MOD_PART", model_mod_part);
                            cmdHash.put("MODEL_CLASS_PATH", model_class_Path);
                            cmdHash.put("MODELTYPE", "LIBRARY");
                        }
                        dispose();
                    } else {
                        cmdHash.remove("MODEL_MOD_PART");
                        cmdHash.put("MODEL_CLASS_PATH", "");
                        cmdHash.put("MODELTYPE", "");
                        dispose();
                    }
                } 
		}});

        // handle modname button, reset entry field
		modname.addItemListener(new ItemListener() {
			public void itemStateChanged(ItemEvent ie) {
			    mod_Part.setText("");
			    class_Path.setText("");
				if (ie.getStateChange() == java.awt.event.ItemEvent.SELECTED) {
				    mod_Part.setColumns(8);
				    class_Path.setColumns(8);
				    Label1.setText(Label1a);
				    Label2.setText(Label2a);
				}
   	        setSize(getSize());
        }} );

        // handle modname button, reset entry field
		libname.addItemListener(new ItemListener() {
			public void itemStateChanged(ItemEvent ie) {
			    mod_Part.setText("");
			    class_Path.setText("");
				if (ie.getStateChange() == java.awt.event.ItemEvent.SELECTED) {
				    mod_Part.setColumns(30);
				    class_Path.setColumns(30);
				    Label1.setText(Label1b);
				    Label2.setText(Label2b);
                }
   	        setSize(getSize());
		}} );

        // build dialog
		mod_Part.setBackground(MBGuiConstants.ColorFieldBackground);
		class_Path.setBackground(MBGuiConstants.ColorFieldBackground);
        Vector actionButtons = new Vector();
		actionButtons.addElement(btOk);
		tempButt = new MBButtonPanel(btHelp,null,actionButtons);
  		GridBagConstraints c = new GridBagConstraints();
   		c.weighty = 1;
   		c.weightx = 0;
   		c.gridx = 1;
   		c.gridy = 1;
		c.anchor = GridBagConstraints.WEST;
		c.insets = new Insets(5,5,5,5);
		Group1.add(modname);
		Group1.add(libname);
  		gridBag.setConstraints(modname, c);
   		bpanel.add(modname);
   		c.gridy = 2;
  		gridBag.setConstraints(libname, c);
   		bpanel.add(libname);
		c.gridwidth = 1;
		c.fill = GridBagConstraints.NONE;

   		c.gridy = 1;
        gridBag.setConstraints(Label1, c);
   		rpanel.add(Label1);
   		c.gridx = 2;
        gridBag.setConstraints(mod_Part, c);
   		rpanel.add(mod_Part);
   		c.gridx = 1;
   		c.gridy = 2;
        gridBag.setConstraints(Label2, c);
   		rpanel.add(Label2);
   		c.gridx = 2;
        gridBag.setConstraints(class_Path, c);
   		rpanel.add(class_Path);
   		c.gridx = 1;
   		c.gridy = 3;
        gridBag.setConstraints(rpanel, c);
   		bpanel.add(rpanel);

   		centerPanel.add(bpanel);

        getContentPane().add("Center", centerPanel);
        getContentPane().add("South", tempButt);

        if (!inittypemod) {
            libname.setSelected(true);
		    mod_Part.setColumns(30);
		    class_Path.setColumns(30);
		    Label1.setText(Label1b);
		    Label2.setText(Label2b);
        } else {
            modname.setSelected(true);
		    mod_Part.setColumns(8);
		    class_Path.setColumns(8);
		    Label1.setText(Label1a);
		    Label2.setText(Label2a);
        }

        // init the model name field
        if (cmodel_mod_part!=null) {
            if (cmodel_mod_part.length() > 0) {
                mod_Part.setText(cmodel_mod_part);
            }
        }
        if (cmodel_class_path!=null) {
            if (cmodel_class_path.length() > 0) {
                class_Path.setText(cmodel_class_path);
            }
        }

		setVisible(true);
	}

    public void postVisibleInitialization(){
	    mod_Part.requestFocus();
    }

}
