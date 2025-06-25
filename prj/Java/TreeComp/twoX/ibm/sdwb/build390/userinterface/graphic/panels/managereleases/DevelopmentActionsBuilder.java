package com.ibm.sdwb.build390.userinterface.graphic.panels.managereleases;
import com.ibm.sdwb.build390.userinterface.graphic.actions.ActionBuilder;
import com.ibm.sdwb.build390.userinterface.graphic.actions.ActionConfigurer;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import javax.swing.Action;
import javax.swing.JMenu;
import javax.swing.JMenuBar;



public class DevelopmentActionsBuilder implements ActionBuilder {

    //View menu
    private Action viewReleaseConfiguration;


    public DevelopmentActionsBuilder(ActionConfigurer configurer) {
        new BaseActionsBuilder(configurer);
        configurer.getFrame().addPropertyChangeListener(new GeneralMenuEnabler());
        makeActions(configurer.getFrame());
        fillMenuBar(configurer.getMenuBar());

    }

    protected ManageReleasesFrame getActionsExecutedInFrame(final java.awt.Component frame) {
        return(ManageReleasesFrame)frame;
    }

    public void makeActions(final java.awt.Component frame) {
        ManageReleasesFrame actionFrame = getActionsExecutedInFrame(frame);
        //View menu
        viewReleaseConfiguration = actionFrame.createAction(ManageReleasesFrame.ViewConfigurationAction.class,null);

    }



    public  void fillMenuBar(JMenuBar menuBar) {
        menuBar.add(createViewMenu(),2);
        
    }


    private JMenu createViewMenu() {
        JMenu menu = new JMenu("View");
        menu.add(viewReleaseConfiguration);
        return menu;
    }


    private class GeneralMenuEnabler implements PropertyChangeListener {
        /**
         * This method gets called when a bound property is changed.
         * @param evt A PropertyChangeEvent object describing the event source
         *   	and the property that has changed.
         */
        public void propertyChange(PropertyChangeEvent evt) {

            if (evt.getPropertyName().equals(BaseActionsBuilder.MANAGERELEASES_RELEASE_CHANGED)) {
                viewReleaseConfiguration.setEnabled((evt.getNewValue()!=null && ((Integer)evt.getNewValue()).intValue() > 0));
            }
            
        }
    }


}

