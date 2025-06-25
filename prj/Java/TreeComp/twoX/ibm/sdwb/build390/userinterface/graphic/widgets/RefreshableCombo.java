package com.ibm.sdwb.build390.userinterface.graphic.widgets;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.InputEvent;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;

import javax.swing.JButton;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.border.LineBorder;
import javax.swing.event.AncestorEvent;
import javax.swing.event.AncestorListener;
import javax.swing.event.InternalFrameAdapter;
import javax.swing.event.InternalFrameEvent;
import javax.swing.plaf.ComboBoxUI;

import com.ibm.sdwb.build390.MBComboEditor;
import com.ibm.sdwb.build390.MBGuiConstants;
import com.ibm.sdwb.build390.MBStatus;
import com.ibm.sdwb.build390.logprocess.LogEventProcessor;


public class RefreshableCombo extends JPanel implements AncestorListener, com.ibm.sdwb.build390.userinterface.UserCommunicationInterface {
    //protected ComboBoxWithHistory choices = new ComboBoxWithHistory("REFRESH_COMBO_1");
    protected ComboBoxWithHistory choices = new ComboBoxWithHistory();
    private JButton refresh = new JButton("Refresh");
    protected transient LogEventProcessor lep=null;
    private boolean filterCaseSensitive=false; //#Case:  state of filter case sensitivity
    private MBComboEditor comboEditor=new MBComboEditor();

    private JPopupMenu filterPopup=new JPopupMenu("Options");
    private JMenuItem filterItem=new JMenuItem("Filter");
    private boolean sortCombo = true;
    private MBStatus statusHandler = null;

    protected RefreshableCombo(LogEventProcessor tempLep) {
        if (tempLep == null) {
            lep = new LogEventProcessor();
        } else {
            lep = tempLep;
        }

        addAncestorListener(this);
        initRefreshableCombo();
    }

    private void initRefreshableCombo() {
        filterItem.addActionListener(new SymAction(this));
        filterPopup.add(filterItem);

        comboEditor.setBorder(LineBorder.createGrayLineBorder());
        comboEditor.addMouseListener(new ComboMouseAdapter());
        choices.setOnlyDisplayCurrentlyValidEntries(true);
        choices.setEditor(comboEditor);

        choices.addMouseListener(new ComboMouseAdapter());

        GridBagLayout gridBag = new GridBagLayout();
        setLayout(gridBag);
        GridBagConstraints c = new GridBagConstraints();
        c.fill = GridBagConstraints.HORIZONTAL;
        c.insets = new Insets(2,3,2,2);
        c.weightx = 3.0;   
        c.anchor= GridBagConstraints.WEST;
        c.gridwidth = GridBagConstraints.RELATIVE;
        gridBag.setConstraints(choices, c);
        add(choices);

        c.weightx = 1.0;     
        c.insets = new Insets(2,1,2,1);
        c.fill = GridBagConstraints.BOTH;
        c.anchor= GridBagConstraints.EAST;
        c.gridwidth = GridBagConstraints.REMAINDER;
        gridBag.setConstraints(refresh, c);
        add(refresh);
        choices.setBackground(MBGuiConstants.ColorFieldBackground);
    }

//Begin #Filter Popup:
//need to use adapter to envoke Modal Frame or gui will hang
    class SymAction extends CancelableAction {
        private RefreshableCombo comboToBaseOn = null;

        SymAction(RefreshableCombo tempCombo) {
            super("Filter Action");
            comboToBaseOn = tempCombo;
        }

        public void doAction(java.awt.event.ActionEvent event) {
            Object object = event.getSource();
            if (object == filterItem) {
                new FilterFrame(comboToBaseOn);
            }
        }
    }

//adapter for popup is keyed to comboEditor location
    public class ComboMouseAdapter
    extends MouseAdapter {
        public void mouseClicked(MouseEvent e) {
            if (e.getModifiers()==InputEvent.BUTTON3_MASK) {
                Component comp=comboEditor.getEditorComponent();

                filterPopup.show(comp,comp.getX()+comp.getWidth(),comp.getY());
            }
        }
    }

//provides the matching function for filter
    protected boolean filterMatch(String s,String filterTxt) {
        filterTxt.trim();

        if (!filterCaseSensitive) {//#Case: if not case sensitive convert to upper case
            filterTxt= filterTxt.toUpperCase();
            s= s.toUpperCase();
        }

        if (filterTxt.length()>s.length())return false;//filter can't be longer

        if (filterTxt.length()<s.length()) { //if filter is short it had better end with a *
            if (!filterTxt.endsWith("*"))return false;
        }

        for (int i=0;i!=filterTxt.length();i++) {
            if (filterTxt.charAt(i)=='*')continue;

            if (filterTxt.charAt(i)==s.charAt(i))continue;
            else return false;
        }
        return true;
    }

    //metadata wants this to be threaded. need to see alternate approach using listernes later.
   public void refreshComboConcurrently() {
        for (int i=0;i<refresh.getActionListeners().length;i++) {
            refresh.getActionListeners()[i].actionPerformed(new ActionEvent(this,ActionEvent.ACTION_PERFORMED,"Refreshsss"));
        }
        //the doClick didn't  work, since it is called inside from an actionPerformed method in manage page.
        //getButton().doClick();
    }

   //mnanage page wants this to be serial. 
   public void refreshCombo() {
        for (int i=0;i<refresh.getActionListeners().length;i++) {
            ((CancelableAction)refresh.getActionListeners()[i]).doAction(new ActionEvent(this,ActionEvent.ACTION_PERFORMED,"Refreshsss"));
            ((CancelableAction)refresh.getActionListeners()[i]).postAction();
        }
        //the doClick didn't  work, since it is called inside from an actionPerformed method in manage page.
        //getButton().doClick();
    }


    public void setActionListener(ActionListener newListener) {
        ActionListener[] listeners = refresh.getActionListeners();
        for (int listenerIndex = 0; listenerIndex< listeners.length ; listenerIndex++) {
            refresh.removeActionListener(listeners[listenerIndex]);
        }
        refresh.addActionListener(newListener);
    }

    public ComboBoxWithHistory getComboBox() {
        return choices;
    }

    public void addListItemListener(ItemListener il) {
        choices.addItemListener(il);
    }

    public java.awt.Dimension getMaximumSize() {
        double maxWidth = choices.getMaximumSize().getWidth()+refresh.getMaximumSize().getWidth();
        double maxHeight = choices.getPreferredSize().getHeight();  // assume the choices is the tallest one
        if (refresh.getPreferredSize().getHeight()>maxHeight) {
            maxHeight = refresh.getPreferredSize().getHeight();
        }
        Dimension maxSize = new Dimension();
        maxSize.setSize(maxWidth, maxHeight);
        return maxSize;
    }

    public java.awt.Dimension getPreferredSize() {
        Dimension prefCombo = choices.getPreferredSize();
        Dimension prefButton = refresh.getPreferredSize();
        double prefWidth = prefCombo.getWidth()+prefButton.getWidth();
        double prefHeight = prefCombo.getHeight();  // assume the choices is the tallest one
        if (prefButton.getHeight()>prefHeight) {
            prefHeight = prefButton.getHeight();
        }
        Dimension prefSize = new Dimension();
        prefSize.setSize(prefWidth, prefHeight);
        return prefSize;
    }

    public void addListActionListener(ActionListener al) {
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

    public void select(String stext) {
        if (contains(stext)) {
            selectItem(stext);
        }
    }

    public void initData(String data) {
        boolean editable = choices.isEditable();
        choices.setEditable(false);
//        choices.removeAllItems();
        choices.setEditable(editable);
        if (choices.getItemCount() > 0) {
            if (contains(data)) {
                selectItem(data);
            }
        } else {
            choices.addItem(data);
            selectItem(data);
        }
    }

    protected void setData(final Collection boxData) {
        if (boxData != null) {
            java.util.List entryData = null;
            if (sortCombo) {
                ArrayList arayList = new ArrayList(boxData);

                Collections.sort(arayList,new comboComparitor());

                entryData = arayList;
            } else {
                entryData = new ArrayList(boxData);
            }
            choices.setItems(entryData);
        }
    }

    protected boolean contains(String testString) {
        if (testString == null) {
            return false;
        }
        for (int i = 0; i < choices.getItemCount();i++) {
            if (testString.equals((String) choices.getItemAt(i))) {
                return true;
            }
        }
        return false;
    }

    public String getElementSelected() {
        return(String)choices.getSelectedItem();
    }

    public void setSortElements(boolean newSort) {
        sortCombo = newSort;
    }

    public void selectItem(String itemToSelect) {
        if (itemToSelect != null) {
            if (itemToSelect.trim().length() > 0) {
                if (!contains(itemToSelect)) {
                    choices.addItem(itemToSelect);
                }
                choices.setSelectedItem(itemToSelect);
            }
        }
    }

    // enable/disable button
    public void setButtonEnabled(boolean val) {
        refresh.setEnabled(val);
    }

    // set editability of combo box
    public void setComboEditable(boolean editable) {
        choices.setEditable(editable);
    }

    // force an event
    public void forceEvent() {
        ItemListener[] itemListeners = choices.getItemListeners();
        for (int i = 0; i < itemListeners.length; i++) {
            itemListeners[i].itemStateChanged(new ItemEvent(choices, ItemEvent.ITEM_STATE_CHANGED, choices, ItemEvent.SELECTED));
        }
    }

    public MBStatus getStatusHandler() {
        return statusHandler;
    }

    public LogEventProcessor getLEP() {
        return lep;
    }

    protected void handleFrameClosing() {
    }

    public void handleUIEvent(com.ibm.sdwb.build390.userinterface.event.UserInterfaceEvent event) {
    }

    private void readObject(java.io.ObjectInputStream in) throws IOException, ClassNotFoundException{
        in.defaultReadObject();
        lep=new  LogEventProcessor();
    }

    class comboComparitor implements  Comparator {
        public int compare(Object o1, Object o2) {
            if (o1 != null & o2 != null) {
                String s1 = o1.toString();
                String s2 = o2.toString();

                return s1.toUpperCase().compareTo(s2.toUpperCase());
            }
            return 1;
        }
    }

    private class FrameListener extends InternalFrameAdapter {
        private boolean hasRun = false;
        /**
         * Invoked when an internal frame is in the process of being closed.
         * The close operation can be overridden at this point.
         */
        public void internalFrameClosed(InternalFrameEvent e) {
            if (!hasRun) {
                handleFrameClosing();
                hasRun = true;
            }
        }
    }

    public void ancestorAdded(AncestorEvent ae) {
        statusHandler = com.ibm.sdwb.build390.userinterface.graphic.utilities.GeneralUtilities.getParentAnimationStatus((java.awt.Component) getParent()).getStatusHandler();
        com.ibm.sdwb.build390.userinterface.graphic.utilities.GeneralUtilities.getParentInternalFrame((java.awt.Component) getParent()).addInternalFrameListener(new FrameListener());

    }

    public void ancestorMoved(AncestorEvent ae) {
    }

    public void ancestorRemoved(AncestorEvent ae) {
    }
}
