package com.ibm.sdwb.build390;
/*********************************************************************/
/* DisableableEntryBox class for the Build/390 client                 */
/*  allows a user to enable or disable the entry field*/
/*********************************************************************/
// Changes
// Date     Defect/Feature      Reason
/*********************************************************************/
import java.io.*;
import java.util.*;
import javax.swing.*;
import java.awt.event.*;
import java.awt.*;

/** A parent class for various type of user entry fields the user can specify to be enabled
or disabled */
abstract public class DisableableEntryBox extends JPanel
{

    private JCheckBox selectEnable = null;
    private JComponent localComp = null;
    private boolean caseSensitive = true;
    private boolean quote = false;
    protected boolean stopped = false;
    protected JComponent thisComp = null;

/*
    constructor - take the name of the box to set the checkbox label, 
                  the JComponent to control, and whether the user can enable component or not.
*/    
    public DisableableEntryBox(String boxName, JComponent tempComp, boolean isEnabled)
    {
        thisComp = this;
        setLayout(new BorderLayout());
        selectEnable = new JCheckBox(boxName, isEnabled);
        add("West", selectEnable);
        initializeComponent(tempComp);
    }

/*
    constructor - take the name of the box to set the checkbox label, 
                  and whether the user can enable component or not.
*/    
    public DisableableEntryBox(String boxName, boolean isEnabled)
    {
        setLayout(new BorderLayout());
        selectEnable = new JCheckBox(boxName, isEnabled);
        add("West", selectEnable);
    }

/*
    initializeComponent - grab a pointer to the component.  Put it into the layout, and 
    set up the action listener to enable or disable the component based on the state of 
    the checkbox.	
*/
    public void initializeComponent(JComponent tempComp)
    {
        localComp = tempComp;
        add("East", localComp);
        selectEnable.addActionListener(new ActionListener()
                                       {
                                           public void actionPerformed(ActionEvent e)
                                           {
                                               setEditable(selectEnable.isSelected());
                                           }
                                       });
    }

/*
    isSelected - return whether the user enabled the component or not.
*/
    public boolean isSelected()
    {
        return selectEnable.isSelected() ;
    }

/*
    setSelected - programically set the check box to enable or disable the user component 
*/
    public void setSelected(boolean sel)
    {
        selectEnable.setSelected(sel);
    }

/*
    getBoxAndLabel - get the Checkbox that enables or disables the user component.   This
    access is allowed to increase the flexiblity of layout of the components.
*/
    public JComponent getBoxAndLabel()
    {
        return selectEnable;
    }

/*
    getEntryComponent - get the user component.   This access is allowed to increase the 
    flexiblity of layout of the components.
*/
    public JComponent getEntryComponent()
    {
        return localComp;
    }

/*
    setEditable - set whether the user componenet can be accessed or not.
*/
    public void setEditable(boolean editable)
    {
        localComp.setEnabled(editable);
    }

/*
    setCaseSensitive - set whether the user componenet is case sensitive.
*/
    public void setCaseSensitive(boolean tempCase)
    {
        caseSensitive = tempCase;
    }

    public boolean isCaseSensitive()
    {
        return caseSensitive;
    }

/*
    setQuoted - set whether the user componenet should be quoted.
*/
    public void setQuoted(boolean tempQuote)
    {
        quote = tempQuote;
    }

    public boolean isQuoted()
    {
        return quote;
    }

/*	
    setEnabled - enable or disable both the local component and the check box.
*/
    public void setEnabled(boolean enabled)
    {
        localComp.setEnabled(enabled);
        selectEnable.setEnabled(enabled);
        super.setEnabled(enabled);
    } 
/*
    placeholder for child classes to implement methods to set their values
*/
    abstract public void setValue(Object newValue);

/*
    placeholder for child classes to implement methods to return their values
*/
    abstract public String getSettings();

/*
    placeholder for child classes to implement methods to return their values
*/
    public int getNumberOfSettings()
    {
        return 1;
    }

/*
    placeholder for child classes to implement methods to return their values
*/
    abstract public String getKeyword(int i);

/*
    placeholder for child classes to implement methods to return their values
*/
    abstract public String getValue(int i);
}
