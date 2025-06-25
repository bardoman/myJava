package com.ibm.sdwb.build390.userinterface.graphic.panels.managereleases;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import javax.swing.Action;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JMenu;
import javax.swing.JMenuBar;

import com.ibm.sdwb.build390.userinterface.graphic.actions.ActionBuilder;


public class BaseActionsBuilder implements ActionBuilder {

    public static final String MANAGERELEASES_DRIVER_SELECTED = "MANAGE_RELEASES_DRIVER_CHANGED";
    public static final String MANAGERELEASES_RELEASE_CHANGED = "MANAGE_RELEASES_RELEASE_CHANGED";
    public static final String MANAGERELEASES_RELEASE_SELECTED = "MANAGERELEASES_RELEASE_SELECTED";
    //Action menu
    private Action createShadow;
    private Action createDriver;
    private Action deleteRelease;
    private Action deleteDriver;
    private Action retrieveDriverParts;
    private Action retrieveUsermodParts;

    //Reports menu
    private Action shadowReport;
    private Action driverReportFailure;
    private Action driverReportFull;       
    private Action driverReportUnbuilt;    
    private Action driverReportDelta;      
    private Action driverReportUnpackaged; 
    private Action driverReportInactive;   
    private Action driverReportShipped;    
    private Action driverReportBuilds;    

    //Setup menu
    private Action setup;
    private final  JCheckBoxMenuItem libraryPathNameMenuItem = new JCheckBoxMenuItem("Part pathname in report");

    //report menu insert
    private Action metadataValidateAllParts;
    private Action metadataValidateByFilterCriteria;

    //sysmod menu
    private Action sysmodUsermodReport;

    private JMenu reportMenu;
    private JMenu retrieveMenu;
    private JMenu deleteMenu;
    private JMenu driverReportMenu;
    private JMenu metadataValidationMenu;
    private JMenu SYSMODMenu;

    public static final String ACTION_NAME_FAILURE = "Failures";
    public static final String ACTION_NAME_FULL_DRIVER_STATUS = "Full Driver Status";
    public static final String ACTION_NAME_UNBUILT_PARTS = "Unbuilt Parts";
    public static final String ACTION_NAME_DELTA_PARTS = "Delta Parts Status";
    public static final String ACTION_NAME_UNPACKAGED_PARTS = "Unpackaged Parts";
    public static final String ACTION_NAME_INACTIVE_PARTS = "Inactive Parts";
    public static final String ACTION_NAME_SHIPPED_PARTS = "Shipped Parts";
    public static final String ACTION_NAME_BUILDS = "Builds";
    public static final String ACTION_NAME_USERMOD = "USERMOD";




    public BaseActionsBuilder(com.ibm.sdwb.build390.userinterface.graphic.actions.ActionConfigurer configurer) {
        configurer.getFrame().addPropertyChangeListener(new MenuEnabler());
        makeActions(configurer.getFrame());
        fillMenuBar(configurer.getMenuBar());
    }

    public void makeActions(final java.awt.Component frame) {
        ManageReleasesFrame actionFrame = getActionsExecutedInFrame(frame);

        //Action menu
        createShadow           = actionFrame.createAction(ManageReleasesFrame.CreateShadowAction.class,null);
        createDriver            = actionFrame.createAction(ManageReleasesFrame.CreateMVSDriverAction.class,null);
        deleteRelease           = actionFrame.createAction(ManageReleasesFrame.DeleteShadowAction.class,null);
        deleteDriver            = actionFrame.createAction(ManageReleasesFrame.DeleteDriverAction.class,null);
        retrieveDriverParts     = actionFrame.createAction(ManageReleasesFrame.MainframeLogRetrieveAction.class,null);
        retrieveUsermodParts    = actionFrame.createAction(ManageReleasesFrame.MainframeUsermodLogRetrieveAction.class,null);

        //Reports menu
        shadowReport    =actionFrame.createAction(ManageReleasesFrame.ShadowReportAction.class,null);

        driverReportFailure    = actionFrame.createAction(ManageReleasesFrame.DriverReportAction.class,new Object[]{ACTION_NAME_FAILURE,libraryPathNameMenuItem});
        driverReportFull       = actionFrame.createAction(ManageReleasesFrame.DriverReportAction.class,new Object[]{ACTION_NAME_FULL_DRIVER_STATUS,libraryPathNameMenuItem});
        driverReportUnbuilt    = actionFrame.createAction(ManageReleasesFrame.DriverReportAction.class,new Object[]{ACTION_NAME_UNBUILT_PARTS,libraryPathNameMenuItem});
        driverReportDelta      = actionFrame.createAction(ManageReleasesFrame.DriverReportAction.class,new Object[]{ACTION_NAME_DELTA_PARTS,libraryPathNameMenuItem});
        driverReportUnpackaged = actionFrame.createAction(ManageReleasesFrame.DriverReportAction.class,new Object[]{ACTION_NAME_UNPACKAGED_PARTS,libraryPathNameMenuItem});
        driverReportInactive   = actionFrame.createAction(ManageReleasesFrame.DriverReportAction.class,new Object[]{ACTION_NAME_INACTIVE_PARTS,libraryPathNameMenuItem});
        driverReportShipped    = actionFrame.createAction(ManageReleasesFrame.DriverReportAction.class,new Object[]{ACTION_NAME_SHIPPED_PARTS,libraryPathNameMenuItem});
        driverReportBuilds     = actionFrame.createAction(ManageReleasesFrame.DriverReportAction.class,new Object[]{ACTION_NAME_BUILDS,libraryPathNameMenuItem});

        //Setup menu
        setup = actionFrame.createAction(ManageReleasesFrame.SetupAction.class,null);

        //metadata menu
        metadataValidateAllParts = actionFrame.createAction(ManageReleasesFrame.CheckAllMVSMetadataValidityAction.class,null);
        metadataValidateByFilterCriteria = actionFrame.createAction(ManageReleasesFrame.FilterByMetadataAction.class,null);

        //sysmod menu
        sysmodUsermodReport     = actionFrame.createAction(ManageReleasesFrame.DriverReportAction.class,new Object[]{ACTION_NAME_USERMOD,libraryPathNameMenuItem});

    }


    protected ManageReleasesFrame getActionsExecutedInFrame(final java.awt.Component frame) {
        return(ManageReleasesFrame)frame;
    }


    public  void fillMenuBar(JMenuBar menuBar) {
        menuBar.add(createFileMenu(menuBar.getMenu(0)));
        menuBar.add(createActionsMenu());
        menuBar.add(createReportMenu());
        menuBar.add(createSetupMenu());
    }


    private JMenu createFileMenu(JMenu menu) {
        return menu;
    }

    private JMenu createActionsMenu() {
        JMenu menu = new JMenu("Actions");
        JMenu createMenu = new JMenu("Create");
        deleteMenu = new JMenu("Delete");
        retrieveMenu = new JMenu("Retrieve");
        menu.add(createMenu);
        menu.add(deleteMenu);
        menu.add(retrieveMenu);


        createMenu.add(createShadow);
        createMenu.add(createDriver);

        deleteMenu.add(deleteRelease);
        deleteMenu.add(deleteDriver);

        retrieveMenu.add(retrieveDriverParts);
        retrieveMenu.add(retrieveUsermodParts);
        return menu;
    }


    private JMenu createReportMenu() {
        reportMenu = new JMenu("Reports");
        driverReportMenu = new JMenu("Driver Report");
        SYSMODMenu = new JMenu("SYSMOD Reports");
        metadataValidationMenu = new JMenu("Metadata Validatation Report");
        reportMenu.add(shadowReport);
        reportMenu.add(driverReportMenu);
        reportMenu.add(metadataValidationMenu);
        reportMenu.add(SYSMODMenu);

        driverReportMenu.add(driverReportFailure);        
        driverReportMenu.add(driverReportFull);        
        driverReportMenu.add(driverReportUnbuilt);        
        driverReportMenu.add(driverReportDelta);        
        driverReportMenu.add(driverReportUnpackaged);        
        driverReportMenu.add(driverReportInactive);        
        driverReportMenu.add(driverReportShipped);        
        driverReportMenu.add(driverReportBuilds);   
        metadataValidationMenu.add(metadataValidateAllParts);
        metadataValidationMenu.add(metadataValidateByFilterCriteria);
        SYSMODMenu.add(sysmodUsermodReport);
        return reportMenu;
    }


    private JMenu createSetupMenu() {
        JMenu menu = new JMenu("Setup");
        menu.add(setup);
        menu.add(libraryPathNameMenuItem);
        return menu;
    }


    private class MenuEnabler implements PropertyChangeListener {
        /**
         * This method gets called when a bound property is changed.
         * @param evt A PropertyChangeEvent object describing the event source
         *   	and the property that has changed.
         */
        public void propertyChange(PropertyChangeEvent evt) {

            if (evt.getPropertyName().equals(MANAGERELEASES_DRIVER_SELECTED)) {
                if (evt.getNewValue()!=null && ((Integer)evt.getNewValue()).intValue() > 0) {
                    reportMenu.setEnabled(true);
                    deleteMenu.setEnabled(true);
                    retrieveMenu.setEnabled(true);
                    driverReportMenu.setEnabled(true);
                    SYSMODMenu.setEnabled(true);
                    metadataValidationMenu.setEnabled(true);

                    shadowReport.setEnabled(false);
                    deleteDriver.setEnabled(true);
                    deleteRelease.setEnabled(false);
                    sysmodUsermodReport.setEnabled(true);

                } else {
                    reportMenu.setEnabled(true);
                    deleteMenu.setEnabled(false);
                    retrieveMenu.setEnabled(false);
                    driverReportMenu.setEnabled(false);
                    SYSMODMenu.setEnabled(false);
                    metadataValidationMenu.setEnabled(false);

                    shadowReport.setEnabled(true);
                    sysmodUsermodReport.setEnabled(false);
                    createShadow.setEnabled(true);
                }
            }



            if (evt.getPropertyName().equals(MANAGERELEASES_RELEASE_CHANGED)) {
                createDriver.setEnabled((evt.getNewValue()!=null && ((Integer)evt.getNewValue()).intValue() > 0));
                shadowReport.setEnabled((evt.getNewValue()!=null && ((Integer)evt.getNewValue()).intValue() > 0));
            }

            if (evt.getPropertyName().equals(MANAGERELEASES_RELEASE_SELECTED)) {
                if (evt.getNewValue()!=null && ((Integer)evt.getNewValue()).intValue() > 0) {
                    deleteMenu.setEnabled(true);
                    deleteRelease.setEnabled(true);
                    deleteDriver.setEnabled(false);
                }

            }



        }
    }



}
