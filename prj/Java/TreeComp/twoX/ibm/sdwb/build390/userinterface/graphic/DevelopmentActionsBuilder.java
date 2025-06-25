package com.ibm.sdwb.build390.userinterface.graphic;

import com.ibm.sdwb.build390.userinterface.graphic.actions.ActionBuilder;
import com.ibm.sdwb.build390.userinterface.graphic.actions.ActionConfigurer;

import javax.swing.Action;
import javax.swing.JMenu;
import javax.swing.JMenuBar;

public class DevelopmentActionsBuilder implements ActionBuilder {

	private Action driverBuildAction;
	private Action localUserBuildAction;
	private Action PDSUserBuildAction;
	private Action localFastTrackAction;
	private Action pdsFastTrackAction;
	private Action USERMODAction;
	private Action manageProcessAction;
	private Action manageReleasesAction;
	private Action metadataEditorAction;
	private JMenu fileMenu = new JMenu("File");
	private JMenu newMenu = new JMenu("New");
	private JMenu toolsMenu = new JMenu("Tools");

	

	public DevelopmentActionsBuilder(ActionConfigurer configurer) {
		makeActions(configurer.getFrame());
		fillMenuBar(configurer.getMenuBar());
	}

	public void makeActions(final java.awt.Component frame) {
		driverBuildAction = new MainInterface.NewDriverBuildAction();
		localUserBuildAction = new MainInterface.NewUserBuildLocal();
		PDSUserBuildAction = new MainInterface.NewPDSUserBuild();

		localFastTrackAction = new MainInterface.NewFastTrackLocal();// ***BE
		pdsFastTrackAction = new MainInterface.NewFastTrackPDS();// ***BE

		USERMODAction = new MainInterface.NewUsermodAction();

		manageProcessAction = new MainInterface.NewManageProcessAction();
		manageReleasesAction = new MainInterface.NewManagePageAction();
		metadataEditorAction = new MainInterface.NewMetadataEditorAction();

	}

	public void fillMenuBar(JMenuBar menuBar) {
		menuBar.add(createFileMenu());
		menuBar.add(createToolsMenu());
	}

	private JMenu createFileMenu() {
		fileMenu.add(newMenu);
		newMenu.add(driverBuildAction);

		JMenu userBuildMenu = new JMenu("User Build");
		newMenu.add(userBuildMenu);
		userBuildMenu.add(localUserBuildAction);
		userBuildMenu.add(PDSUserBuildAction);

		JMenu fastTrackMenu = new JMenu("Fast Track");// ***BE
		newMenu.add(fastTrackMenu);
		fastTrackMenu.add(localFastTrackAction);
		fastTrackMenu.add(pdsFastTrackAction);

		newMenu.add(USERMODAction);
		return fileMenu;
	}

	private JMenu createToolsMenu() {
		toolsMenu.add(manageProcessAction);
		toolsMenu.addSeparator();
		toolsMenu.add(manageReleasesAction);
		toolsMenu.add(metadataEditorAction);
		toolsMenu.addSeparator();
		return toolsMenu;
	}

}
