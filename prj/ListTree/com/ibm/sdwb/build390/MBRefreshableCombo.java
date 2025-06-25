package com.ibm.sdwb.build390;
/*********************************************************************/
/* MBRefreshableCombo class for the Build/390 client                 */
/*  Creates and manages the Driver Build Page                        */
/*********************************************************************/
// Changes
// Date     Defect/Feature      Reason
// 11/03/98 //#refresh          delete item
// 01/15/99                     fix deleteItem so it clears editable stuff
// 04/20/99 fasttrack restart   allow disable of combo
// 01/04/2001 #Filter: Add filter to JComboBox model
//01/09/2001 #Case: Make filter case insensitive
//01/12/2001 #Filter Popup: filter now pops up on right click to envoke frame
//01/16/2001 #ModelUpdate: extend JComboBox, add ModelUpdate to fireItemStateChanged 
//09/18/2003 #DEF.TST1591: fix sort
/*********************************************************************/
import java.io.*;
import java.util.*;
import javax.swing.*;
import javax.swing.border.*;
import java.awt.event.*;
import java.awt.*;
import com.ibm.sdwb.build390.logprocess.*;
import javax.swing.plaf.basic.*;
import javax.swing.plaf.ComboBoxUI;

/** Create the driver build page */
public class MBRefreshableCombo extends JPanel
{
    private MBComboBox choices = new MBComboBox();
    protected JButton refresh = new JButton("Refresh");
    private ItemListener ThisItemListener;
    protected boolean stopped = false;
    protected transient LogEventProcessor lep=null;
    private boolean filterCaseSensitive=false; //#Case:  state of filter case sensitivity
    private MBComboEditor comboEditor=new MBComboEditor();

    private JPopupMenu filterPopup=new JPopupMenu("Options");
    private JMenuItem filterItem=new JMenuItem("Filter");
    private MBRefreshableCombo thisCombo;
    private MBInternalFrame pFrame;


    MBRefreshableCombo(final MBInternalFrame pFrame,LogEventProcessor tempLep) {
        if(tempLep == null)lep = new LogEventProcessor();
        else  lep = tempLep;

        thisCombo=this;
        this.pFrame=pFrame;

        filterItem.addActionListener(new SymAction(pFrame));
        filterPopup.add(filterItem);
        comboEditor.setBorder(LineBorder.createGrayLineBorder());
        comboEditor.addMouseListener(new ComboMouseAdapter());
        choices.setEditor(comboEditor);
        choices.addMouseListener(new ComboMouseAdapter());

        setLayout(new GridLayout(1,2));
        add(choices);
        add(refresh);

        choices.setBackground(MBGuiConstants.ColorFieldBackground);
    }

//need to use adapter to envoke Modal Frame or gui will hang
    class SymAction extends MBCancelableActionListener
    {
        SymAction(MBAnimationStatusWindow temp) {
            super(temp);
        }
        public void doAction(java.awt.event.ActionEvent event) {
            Object object = event.getSource();
            if(object == filterItem)
            {
                new MBFilterFrame(thisCombo);
            }
        }
    }

//adapter for popup is keyed to comboEditor location
    public class ComboMouseAdapter
    extends MouseAdapter
    {
        public void mouseClicked(MouseEvent e) {
            if(e.getModifiers()==InputEvent.BUTTON3_MASK)
            {
                Component comp=comboEditor.getEditorComponent();

                filterPopup.show(comp,comp.getX()+comp.getWidth(),comp.getY());
            }
        }
    }

//provides the matching function for filter
    protected boolean filterMatch(String s,String filterTxt) {
        filterTxt.trim();

        if(!filterCaseSensitive)
        {//#Case: if not case sensitive convert to upper case
            filterTxt= filterTxt.toUpperCase();
            s= s.toUpperCase();
        }

        if(filterTxt.length()>s.length())return false;//filter can't be longer

        if(filterTxt.length()<s.length())
        { //if filter is short it had better end with a *
            if(!filterTxt.endsWith("*"))return false;
        }

        for(int i=0;i!=filterTxt.length();i++)
        {
            if(filterTxt.charAt(i)=='*')continue;

            if(filterTxt.charAt(i)==s.charAt(i))continue;
            else return false;
        }
        return true;
    }

//uses the  filterMatch   method to filter the model
    public void filterModel(String filterTxt) {
        DefaultComboBoxModel outModel=new   DefaultComboBoxModel();
        String s;

        for(int i=0;i!=choices.getModel().getSize();i++)
        {
            s= (String)choices.getModel().getElementAt(i);

            if(filterMatch(s,filterTxt))outModel.addElement(s);
        }
        choices.setModel(outModel);

        choices.modelUpdate();
    }

    //#ModelUpdate: extend JComboBox, add ModelUpdate to fireItemStateChanged
    class MBComboBox extends JComboBox
    {
        public  void modelUpdate()
        {
            ItemEvent ie=new ItemEvent(this,
                                       ItemEvent.ITEM_STATE_CHANGED,
                                       getItemAt(0) ,
                                       ItemEvent.SELECTED);
            fireItemStateChanged(ie);
        }
    }
//End #Filter Popup:

    protected void addButtonActionListener(ActionListener al) {
        refresh.addActionListener(al);
    }

    protected void addListItemListener(ItemListener il) {
        ThisItemListener = il;
        choices.addItemListener(ThisItemListener);
    }

    protected void addListActionListener(ActionListener al) {
        choices.addActionListener(al);
    }

    protected void addItem(Object item) {
        choices.addItem(item);
    }

    public void setEnabled(boolean enabled) {
        choices.setEnabled(enabled);
        refresh.setEnabled(enabled);
        super.setEnabled(enabled);
    }

    public void deleteItem(Object item) { //#refresh
        boolean editState = choices.isEditable();
        choices.setEditable(false);
        choices.removeItem(item);
        choices.setEditable(editState);
    }

    public void initData(String data) {
        boolean editable = choices.isEditable();
        choices.setEditable(false);
//        choices.removeAllItems();
        choices.setEditable(editable);
        if(choices.getItemCount() > 0)
        {
            if(contains(data))
            {
                selectItem(data);
            }
        }
        else
        {
            choices.addItem(data);
            selectItem(data);
        }
    }

    protected void setData(java.util.List boxData) {
        if(boxData !=null)
        {
            //#DEF.TST1591:
            Collections.sort(boxData,new comboComparitor());
            
            boolean editable = choices.isEditable();
            choices.setEditable(false);
            DefaultComboBoxModel newModel = new DefaultComboBoxModel(boxData.toArray());
            choices.setEditable(editable);
            choices.setModel(newModel);
        }
        else
        {
            choices.removeAllItems();
        }
    }

    protected boolean contains(String testString) {
        for(int i = 0; i < choices.getItemCount();i++)
        {
            if(testString.equals((String) choices.getItemAt(i)))
            {
                return true;
            }
        }
        return false;
    }

    public String elementSelected() {
        return(String) choices.getSelectedItem();
    }

    public void selectItem(String itemToSelect) {
        if(itemToSelect != null)
        {
            if(itemToSelect.trim().length() > 0)
            {
                if(!contains(itemToSelect))
                {
                    choices.addItem(itemToSelect);
                }
                choices.setSelectedItem(itemToSelect);
            }
        }
    }

    // enable/disable button
    public void EnableButton(boolean val) {
        refresh.setEnabled(val);
    }

    // set editability of combo box
    public void setComboEditable(boolean editable) {
        choices.setEditable(editable);
    }

    // force an event
    public void forceEvent() {
        if(ThisItemListener != null)
            //    ThisItemListener.itemStateChanged(new ItemEvent(choices, ItemEvent.ITEM_STATE_CHANGED, choices, ItemEvent.SELECTED));
            ThisItemListener.itemStateChanged(new ItemEvent(choices, ItemEvent.ITEM_STATE_CHANGED, elementSelected(), ItemEvent.SELECTED));
    }

    private void readObject(java.io.ObjectInputStream in) throws IOException, ClassNotFoundException{
        in.defaultReadObject();
        lep=new  LogEventProcessor();
/*Ken 7/5/00 we should do this, but we'll add & test later.
        lep.addEventListener(MBClient.getGlobalLogDBListener());
        lep.addEventListener(MBClient.getGlobalLogFileListener());
*/
    }

    //Begin #DEF.TST1591:
    class comboComparitor implements  Comparator
    {
        public int compare(Object o1, Object o2)
        {
            String s1 = (String) o1;
            String s2 = (String) o2;

            return s1.toUpperCase().compareTo(s2.toUpperCase());
        }
    }
    //End #DEF.TST1591:
}


