package com.ibm.sdwb.build390.userinterface.graphic.widgets;

import javax.swing.*;
import java.util.*;
import com.ibm.sdwb.build390.MBBasicInternalFrame;

public class ComboBoxWithHistory extends JComboBox {

    private List currentHistoryList = new ArrayList();
    private List additionalEntries = new ArrayList();
    private DefaultComboBoxModel theModel = new DefaultComboBoxModel();
    private String instanceLabel = null;
    private boolean onlyDisplayCurrentlyValidEntries = false;
    private boolean dontSetCurrentSelection = false;

    private static final String SEPARATOR = "--------";
    private static final int MAXIMUM_NUMBER_OF_REMEBERED_SELECTIONS = 10;


    public ComboBoxWithHistory() {
        setModel(theModel);
        loadHistoryForInstance();
        addActionListener(new ComboSelectionActionListener(this));
    }

    public ComboBoxWithHistory(String tempInstanceLabel) {
        setModel(theModel);
        instanceLabel = tempInstanceLabel;
        loadHistoryForInstance();
        addActionListener(new ComboSelectionActionListener(this));
    }

    public void setHistory(List historyList) {
        instanceLabel = null;
        currentHistoryList = historyList;
        dontSetCurrentSelection = true;
        populateCombo();
    }

    public void setInstanceLabel(String newLabel) {
        instanceLabel = newLabel;
        populateCombo();
    }

    public void setOnlyDisplayCurrentlyValidEntries(boolean temp) {
        onlyDisplayCurrentlyValidEntries = temp;
    }

    private void populateCombo() {
        synchronized(this) {
            Object currentSelection = null;

            if (!dontSetCurrentSelection) {
                currentSelection =  getValidSelectedItem();
            }

            dontSetCurrentSelection = false;

            super.removeAllItems();

            List localHistoryCopy = new ArrayList();
            if (currentHistoryList!=null) {  // need to do this for thread handling
                localHistoryCopy=new ArrayList(currentHistoryList);
            }

            for (Iterator historyIterator = localHistoryCopy.iterator(); historyIterator.hasNext(); ) {
                Object  nextHistoricalObject = historyIterator.next();
                String  nextHistorical = null;
                if (nextHistoricalObject!=null) {
                    nextHistorical  = nextHistoricalObject.toString();
                } else {
                    nextHistorical = new String();
                }
                if (onlyDisplayCurrentlyValidEntries) {
                    if (additionalEntries.contains(nextHistorical)) {
                        super.addItem(nextHistorical);
                    }
                } else {
                    super.addItem(nextHistorical);
                }
            }

            super.addItem(SEPARATOR);
            if (!additionalEntries.isEmpty()) {
                for (Iterator additionsIterator =  additionalEntries.iterator(); additionsIterator.hasNext(); ) {
                    Object nextItem = additionsIterator.next();
                    if (!localHistoryCopy.contains(nextItem)) {
                        super.addItem(nextItem);
                    }
                }
            }
            if (currentSelection!=null) {
                super.setSelectedItem(currentSelection);
            } else {
                super.setSelectedIndex(0);// make sure a selection event is fired
                super.setSelectedItem(getSelectedItem());
            }
        }
    }

    public Object getSelectedItem() {
        if (SEPARATOR.equals(super.getSelectedItem())) {
            return null;
        }
        return super.getSelectedItem();
    }

    public int getSelectedIndex() {
        if (SEPARATOR.equals(super.getSelectedItem())) {
            return -1;
        }
        return super.getSelectedIndex();
    }

    public void addItem(Object anObject) {
        additionalEntries.add(anObject);
        populateCombo();
    }

    public void setItems(List objectList) {
        additionalEntries = new ArrayList(objectList);
        populateCombo();
    }

    public Object getValidSelectedItem() {
        if (super.getSelectedItem() !=null) {
            if (!super.getSelectedItem().equals(SEPARATOR)) {
                return super.getSelectedItem();
            }
        }
        return null;
    }

    public void insertItemAt(Object anObject, int index) {
        additionalEntries.add(index,anObject);
        populateCombo();
    }

    public void removeItem(Object element) {
        additionalEntries.remove(element);
        populateCombo();
    }

    public void removeItemAt(int anIndex) {
        additionalEntries.remove(anIndex);
        populateCombo();
    }

    public void removeAllItems() {
        theModel.removeAllElements();
        additionalEntries.clear();
        currentHistoryList.clear();
        populateCombo();
    }

    public void addHistoryItem(Object element) {
        updateHistory(element);
    }

    protected void updateHistory(Object newEntry) {
        currentHistoryList.remove(newEntry); // take it out of it's old location
        currentHistoryList.add(0, newEntry);
        if (currentHistoryList.size() > MAXIMUM_NUMBER_OF_REMEBERED_SELECTIONS) {
            currentHistoryList.remove(currentHistoryList.size() - 1);
        }
        saveHistoryForInstance();
        populateCombo();
    }

    private void loadHistoryForInstance() {
        if (instanceLabel!=null) {
            currentHistoryList =  (List) MBBasicInternalFrame.getGenericStatic(instanceLabel);
            if (currentHistoryList == null) {
                currentHistoryList = new ArrayList();
            }
        }
        dontSetCurrentSelection = true;
        populateCombo();
    }

    private void saveHistoryForInstance() {
        if (instanceLabel!=null) {
            if (currentHistoryList == null) {
                currentHistoryList = new ArrayList();
            }
            MBBasicInternalFrame.putGenericStatic(instanceLabel, currentHistoryList);
        }
    }

    class ComboSelectionActionListener implements java.awt.event.ActionListener {
        private ComboBoxWithHistory theCombo = null;

        ComboSelectionActionListener(ComboBoxWithHistory tempCombo) {
            theCombo = tempCombo;
        }

        public void actionPerformed(java.awt.event.ActionEvent e) {
            if (instanceLabel!= null) {
                if (theCombo.getSelectedItem() !=null) {
                    theCombo.updateHistory(theCombo.getSelectedItem());
                }
            }
        }
    }
}
