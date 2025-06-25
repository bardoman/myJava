package com.ibm.sdwb.build390;
/*********************************************************************/
/* MBComboEditor class for the Build/390 client                 */
/*  Custom editor for MBRefreshableCombo                        */
/*********************************************************************/
// Changes
// Date     Defect/Feature      Reason
// 01/12/2001 //#Origin         create class
/*********************************************************************/
import javax.swing.*;
import javax.swing.border.*;
import java.io.Serializable;
import java.awt.*;
import java.awt.event.*;
/**
 * Editor for MBRefreshableCombo
 */
public class MBComboEditor implements ComboBoxEditor,FocusListener
{
    protected JTextField editor;

    public MBComboEditor()
    {
        editor = new JTextField("",9);
    }

    public Component getEditorComponent()
    {
        return editor;
    }

    public void setItem(Object anObject)
    {
        if ( anObject != null )
            editor.setText(anObject.toString());
        else
            editor.setText("");
    }

    public Object getItem()
    {
        return editor.getText();
    }

    public void selectAll()
    {
        editor.selectAll();
        editor.requestFocus();
    }

    // This used to do something but now it doesn't.  It couldn't be
    // removed because it would be an API change to do so.
    public void focusGained(FocusEvent e)
    {
    }

    // This used to do something but now it doesn't.  It couldn't be
    // removed because it would be an API change to do so.
    public void focusLost(FocusEvent e)
    {
    }

    public void addActionListener(ActionListener l)
    {
        editor.addActionListener(l);
    }

    public void removeActionListener(ActionListener l)
    {
        editor.removeActionListener(l);
    }

    public void addMouseListener(MouseListener l)
    {
        editor.addMouseListener(l);
    }

    public void removeMouseListener(MouseListener l)
    {
        editor.removeMouseListener(l);
    }

    public void setBorder(Border b){}

    /**
     * A subclass of BasicComboBoxEditor that implements UIResource.
     * BasicComboBoxEditor doesn't implement UIResource
     * directly so that applications can safely override the
     * cellRenderer property with BasicListCellRenderer subclasses.
     * <p>
     * <strong>Warning:</strong>
     * Serialized objects of this class will not be compatible with
     * future Swing releases.  The current serialization support is appropriate
     * for short term storage or RMI between applications running the same
     * version of Swing.  A future release of Swing will provide support for
     * long term persistence.
     */
    public static class UIResource extends MBComboEditor
    implements javax.swing.plaf.UIResource
    {
    }

}

