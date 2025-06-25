package com.ibm.sdwb.build390.userinterface.graphic.panels.managereleases;
import com.ibm.sdwb.build390.userinterface.graphic.actions.ActionBuilder;
import com.ibm.sdwb.build390.userinterface.graphic.actions.ActionConfigurer;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import javax.swing.Action;
import javax.swing.JMenu;
import javax.swing.JMenuBar;




public final class ConfigurationAdminActionsBuilder implements ActionBuilder {

    private ActionBuilder builder;

    //Config Admin menu
    private Action configAdmin;

    public ConfigurationAdminActionsBuilder(ActionConfigurer configurer) {
        new DevelopmentActionsBuilder(configurer);
        configurer.getFrame().addPropertyChangeListener(new ConfigAdminMenuEnabler());//TST3459A
        makeActions(configurer.getFrame());
        fillMenuBar(configurer.getMenuBar());
    }

    protected ManageReleasesFrame getActionsExecutedInFrame(final java.awt.Component frame) {
        return(ManageReleasesFrame)frame;
    }

    public  void makeActions(final java.awt.Component frame) {
        ManageReleasesFrame actionFrame = getActionsExecutedInFrame(frame);
        //Config Admin menu
        configAdmin = actionFrame.createAction(ManageReleasesFrame.ConfigAdminAction.class,new Object[]{"Activate"});
    }


    public  void fillMenuBar(JMenuBar menuBar) {
        menuBar.add(createConfigAdminMenu(),-1);
    }


    private JMenu createConfigAdminMenu() {
        JMenu menu = new JMenu("Config Admin");
        menu.add(configAdmin);
        return menu;
    }
    
    //Begin TST3459A
    private class ConfigAdminMenuEnabler implements PropertyChangeListener {
        /**
         * This method gets called when a bound property is changed.
         * @param evt A PropertyChangeEvent object describing the event source
         *   	and the property that has changed.
         */
        public void propertyChange(PropertyChangeEvent evt) {

            if (evt.getPropertyName().equals(BaseActionsBuilder.MANAGERELEASES_RELEASE_CHANGED)) {
                configAdmin.setEnabled((evt.getNewValue()!=null && ((Integer)evt.getNewValue()).intValue() > 0));
            }
            
        }
    }
    //End TST3459A
}
