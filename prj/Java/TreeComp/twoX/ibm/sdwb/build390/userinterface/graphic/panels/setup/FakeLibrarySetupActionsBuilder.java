package com.ibm.sdwb.build390.userinterface.graphic.panels.setup;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import javax.swing.Action;
import javax.swing.JMenu;
import javax.swing.JMenuBar;

import com.ibm.sdwb.build390.userinterface.graphic.actions.ActionBuilder;
import com.ibm.sdwb.build390.userinterface.graphic.actions.ActionConfigurer;



public final class FakeLibrarySetupActionsBuilder implements ActionBuilder {

    private Action noLIBAction;

    public FakeLibrarySetupActionsBuilder(ActionConfigurer configurer) {
        makeActions(configurer.getFrame());
        fillMenuBar(configurer.getMenuBar());
    }

    public  void makeActions(final java.awt.Component frame) {
        SetupInformation actionFrame  = (SetupInformation)frame;
        noLIBAction           = actionFrame.createAction(SetupInformation.AddNOLIBEntry.class,null);
    }


    public  void fillMenuBar(JMenuBar menuBar) {
        menuBar.add(createLibraryMenu(menuBar.getMenu(2)));
    }

    private JMenu createLibraryMenu(JMenu menu) {
        JMenu addMenu = (JMenu)menu.getMenuComponent(0);
        addMenu.add(noLIBAction);
        return menu;
    }


    private class MenuEnabler implements PropertyChangeListener {
        /**
         * This method gets called when a bound property is changed.
         * @param evt A PropertyChangeEvent object describing the event source
         *   	and the property that has changed.
         */
        public void propertyChange(PropertyChangeEvent evt) {
           /* if (evt.getPropertyName().equals(SETUP_LIBRARY_SELECTED)) {
                if (evt.getNewValue()!=null && ((Integer)evt.getNewValue()).intValue() > 0) {
                } else {
                }
            }*/
        }
    }



}
