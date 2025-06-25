package com.ibm.sdwb.build390.userinterface.graphic.panels.setup;

import com.ibm.sdwb.build390.userinterface.graphic.actions.ActionBuilder;
import com.ibm.sdwb.build390.userinterface.graphic.actions.ActionConfigurer;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import javax.swing.Action;
import javax.swing.JMenu;
import javax.swing.JMenuBar;



public class DevelopmentSetupActionsBuilder implements ActionBuilder {

    private Action addCMVCSetupAction;
    private Action addRationalSetupAction;
    private Action testLibraryAction;

    public static final String SETUP_LIBRARY_SELECTED = "SETUP_LIBRARY_SELECTED";


    public    DevelopmentSetupActionsBuilder(ActionConfigurer configurer) {
        configurer.getFrame().addPropertyChangeListener(new MenuEnabler());
        makeActions(configurer.getFrame());
        fillMenuBar(configurer.getMenuBar());
    }

    public  void makeActions(final java.awt.Component frame) {
        SetupInformation actionFrame  = (SetupInformation)frame;
        addCMVCSetupAction            = actionFrame.createAction(SetupInformation.AddCMVCEntry.class,null);
        addRationalSetupAction        = actionFrame.createAction(SetupInformation.AddRationalEntry.class,null);
        testLibraryAction             = actionFrame.createAction(SetupInformation.TestLibraryEntry.class,null);

    }


    public  void fillMenuBar(JMenuBar menuBar) {
        menuBar.add(createLibraryMenu(menuBar.getMenu(2)));
    }



    private JMenu createLibraryMenu(JMenu menu) {
        JMenu addMenu = (JMenu)menu.getMenuComponent(0);
        addMenu.add(addCMVCSetupAction);
        addMenu.add(addRationalSetupAction);
        menu.add(testLibraryAction);
        return menu;
    }


    private class MenuEnabler implements PropertyChangeListener {
        /**
         * This method gets called when a bound property is changed.
         * @param evt A PropertyChangeEvent object describing the event source
         *   	and the property that has changed.
         */
        public void propertyChange(PropertyChangeEvent evt) {
            if (evt.getPropertyName().equals(SETUP_LIBRARY_SELECTED)) {
                if (evt.getNewValue()!=null && ((Integer)evt.getNewValue()).intValue() > 0) {
                    testLibraryAction.setEnabled(true);
                } else {
                    testLibraryAction.setEnabled(false);
                }
            }
        }
    }


}
