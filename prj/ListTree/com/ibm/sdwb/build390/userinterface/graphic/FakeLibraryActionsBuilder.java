

package com.ibm.sdwb.build390.userinterface.graphic;

import javax.swing.Action;
import javax.swing.JMenu;
import javax.swing.JMenuBar;

import com.ibm.sdwb.build390.userinterface.graphic.actions.ActionBuilder;
import com.ibm.sdwb.build390.userinterface.graphic.actions.ActionConfigurer;



public final class FakeLibraryActionsBuilder implements ActionBuilder {


    private Action localUserBuildAction;
    private Action PDSUserBuildAction;
    private Action localFastTrackAction;
    private Action pdsFastTrackAction;

    private Action manageProcessAction;
    private Action manageReleasesAction;

    public FakeLibraryActionsBuilder(ActionConfigurer configurer) {
        makeActions(configurer.getFrame());
        fillMenuBar(configurer.getMenuBar());
    }

    public  void makeActions(final java.awt.Component frame) {
        localUserBuildAction = new MainInterface.NewUserBuildLocal();
        PDSUserBuildAction =new MainInterface.NewPDSUserBuild();

        localFastTrackAction = new MainInterface.NewFastTrackLocal();//***BE
        pdsFastTrackAction = new MainInterface.NewFastTrackPDS();//***BE

        manageProcessAction = new MainInterface.NewManageProcessAction();
        manageReleasesAction = new MainInterface.NewManagePageAction();

    }


    public  void fillMenuBar(JMenuBar menuBar) {
        menuBar.add(createFileMenu());
        menuBar.add(createToolsMenu());
    }


    private JMenu createFileMenu() {
        JMenu menu = new JMenu("File");
        JMenu newMenu = new JMenu("New");
        JMenu userBuildMenu = new JMenu("User Build");
        menu.add(newMenu);
        newMenu.add(userBuildMenu);
        userBuildMenu.add(localUserBuildAction);
        userBuildMenu.add(PDSUserBuildAction);

        JMenu fastTrackMenu = new JMenu("Fast Track");//***BE
        newMenu.add(fastTrackMenu);
        fastTrackMenu.add(localFastTrackAction);
        fastTrackMenu.add(pdsFastTrackAction);
        return menu;
    }

    private JMenu createToolsMenu() {
        JMenu menu = new JMenu("Tools");
        menu.add(manageProcessAction);
        menu.addSeparator();
        menu.add(manageReleasesAction);
        menu.addSeparator();
        return menu;
    }

}
