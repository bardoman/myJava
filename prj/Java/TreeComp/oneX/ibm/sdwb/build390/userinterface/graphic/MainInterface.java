package com.ibm.sdwb.build390.userinterface.graphic;
/*********************************************************/
/* Java GUI class for Build390                           */
/*********************************************************/
// Changes
// Date     Defect/Feature      Reason
// 10/20/98 x01                 Add ability to NOT show about dialog
// 11/12/98 Feature_133         Remove Test connection from menu
// 03/10/99 AddHoldTrace        Add hold and trace to menu
// 03/17/99 Defect_247          Add site specific help hook
// 03/23/99 LocalUpdates        Gets local updates form the web
// 04/01/99                     Reorder help menu items
// 04/04/99 noSuchChild         fix no such child exceptions
// 04/27/99 errorHandling       change LogException parms & add new error types
// 05/17/99 UIFixes             set up the default colors & backgrounds
// 05/20/99                     put try&catch around MBAparBuildPage
// 05/25/99                     add ++apar to open menu list
// 06/18/99 case#394            delete ++apar to open menu list, shouldn't be added first place
// 06/30/99 feature_417         remove ++PTF from the open menu
// 09/30/99 pjs - Fix help link
// 11/30/99 *reenterpwd          change text in the menu item - "Change password" to "Reenter Build/390 Server Password" 
// 01/17/2000 build.1m.log       change to get the build reload status - to check where to display the build log >1M dialog
// 03/07/2000 reworklog          change to rework the log stuff using listeners
// 03/28/2000 tieerrordialogs    changes to tie error dialogs.
// 03/28/2000 viewBuildLog       changes to add buildlog viewer.
// 04/03/2000 typo on viewbuildlog  fix a typo on view build log
//"03/20/2002 Defect INT0755     Hold output on host" option misleading.
//03/29/2002 #Def.INT0754:       Add new verb trace option
//06/10/2002 #Def.NukLocalUpdate:Remove local update feature
//12/03/2002 SDWB-2019           Enhance the help system
//01/02/2003 Feat.SDWB-1457:     Automate inserting REQs for split PTFs
//01/16/2002 DEF.INT1078:        CLUG download url is hardcoded.
//04/02/2003 Def.INT1158:        Update SDWB1457
/* 02/12/2004 INT1757 mixed-case support */
/*********************************************************************/
import java.awt.BorderLayout;
import java.awt.Canvas;
import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.Vector;

import javax.swing.*;
import javax.swing.event.MenuEvent;
import javax.swing.event.MenuListener;
import javax.swing.plaf.ColorUIResource;
import javax.swing.plaf.metal.MetalLookAndFeel;

import com.ibm.sdwb.build390.*;
import com.ibm.sdwb.build390.help.HelpController;
import com.ibm.sdwb.build390.help.HelpException;
import com.ibm.sdwb.build390.help.HelpTopicID;
import com.ibm.sdwb.build390.library.LibraryInfo;
import com.ibm.sdwb.build390.logprocess.LogEventProcessor;
import com.ibm.sdwb.build390.security.PasswordManager;
import com.ibm.sdwb.build390.user.Setup;
import com.ibm.sdwb.build390.user.SetupManager;
import com.ibm.sdwb.build390.userinterface.graphic.actions.ActionBuilder;
import com.ibm.sdwb.build390.userinterface.graphic.actions.ActionConfigurer;
import com.ibm.sdwb.build390.userinterface.graphic.actions.UserInterfaceActionBarSupport;
import com.ibm.sdwb.build390.userinterface.graphic.panels.build.DriverBuildPanel;
import com.ibm.sdwb.build390.userinterface.graphic.panels.build.InternalFrameBuildPanelHolder;
import com.ibm.sdwb.build390.userinterface.graphic.panels.build.UserBuildPanel;
import com.ibm.sdwb.build390.userinterface.graphic.panels.build.UsermodPanel;
import com.ibm.sdwb.build390.userinterface.graphic.panels.managereleases.ManageReleasesFrame;
import com.ibm.sdwb.build390.userinterface.graphic.panels.metadata.MetadataEditorFrame;
import com.ibm.sdwb.build390.userinterface.graphic.panels.multiprocesspanels.MultipleProcessMonitoringFrame;
import com.ibm.sdwb.build390.userinterface.text.utilities.CommandLineSettings;

/** <br>MBGUI creates the windows, components, menus etc, needed to
* run the Build/390 tool in a graphical environment and handles
* all user interaction
*/
public class MainInterface {

    private static String idFile     = new String("Buildid.ser");

    private MBStatus status;
    private JDesktopPane desk;
    private Hashtable siteBookmarksHash = new Hashtable(); // Defect_247
    private Vector siteBookmarksVector= new Vector(); // Defect_247
    private boolean inSetup = false;
    // Main frame
    private MBFrame f;
    private String Title             = new String(MBConstants.productName+" Client");
    private MBStdErrorViewer StdErrPanel = null;
    private boolean serviceFunctionsExposed = false;

    private int waitCursor = 0;
    private Object cursorLock = new Object();
    private transient LogEventProcessor lep=null;

    private final JCheckBoxMenuItem standardErrorMenuItem = new JCheckBoxMenuItem("Standard Error");
    private final JCheckBoxMenuItem  traceChkBox           = new JCheckBoxMenuItem("Enable Trace");

    private static MainInterface interfaceInstance = null;

    /** Provide access to GUI main frame
    * @return frame object of the main window
    */
    public JFrame getframe() {
        return(f);
    }

    public static void initializeInterface() {
        if (interfaceInstance == null) {
            interfaceInstance = new MainInterface();
        }
    }

    public static MainInterface getInterfaceSingleton() {
        return interfaceInstance;
    }

    /** Constructs the GUI.
    * @param outFile1 string object containing the output file name
    */
    private MainInterface() {

        // Main frame setup
        setUIDefaults();

        lep  = new LogEventProcessor();
        lep.addEventListener(MBClient.getGlobalLogFileListener());
        desk = new JDesktopPane() {
            public Component getComponent(int n) {
                if (getComponentCount() > n) {
                    return super.getComponent(n);
                } else {
                    return new Canvas();
                } 
            }
        };
        f = new MBFrame(Title, true);
        f.setVisible(false);
        f.setBounds(5,5,700,560);
        f.setLayeredPane(desk);
        f.getContentPane().setLayout(new BorderLayout());
        f.setDefaultLookAndFeelDecorated(true);

        JTextField statusBar = new JTextField("Status");
        status = new MBStatus(statusBar);
        f.getContentPane().add("South", statusBar);

        fillActionBar(new MainInterfaceActionConfigurer());

        // If the position of the window has previously been saved, move the window
        try {
            String ifn = new String(MBGlobals.Build390_path+"misc"+File.separator+"mainpos.ser");
            ObjectInputStream ois = new ObjectInputStream(new FileInputStream(ifn));
            Point pt = new Point((Point)ois.readObject());
            Dimension size = (Dimension)ois.readObject();
            f.setLocation(pt);
            f.setSize(size);
        } catch (IOException ioe) {// Just ignore errors and leave the window where it is
        } catch (ClassNotFoundException e) {
        }

    }

    private void fillActionBar(ActionConfigurer configurer) {

        ActionBuilder  actionBuilder = ((UserInterfaceActionBarSupport)MBClient.getCommandLineSettings().getMode()).getMainActionBar(configurer);
        JMenuBar mb     = configurer.getMenuBar();
        // Build menu bar
        JMenu fileMenu     =  mb.getMenu(0);
        JMenu optionsMenu = new JMenu("Options");
        JMenu helpMenu = new JMenu("Help");
        mb.add(optionsMenu,1);
        mb.add(helpMenu);
        JMenu toolsMenu     =  mb.getMenu(2);

        fileMenu.addSeparator();
        fileMenu.add(new QuitAction());

        optionsMenu.add(new SetupAction());
        optionsMenu.addSeparator();
        JMenu enterNewPasswordMenu             = new JMenu("Re-enter Password");
        optionsMenu.add(enterNewPasswordMenu);
        buildPasswordMenu(enterNewPasswordMenu);
        optionsMenu.addSeparator();
        final JCheckBoxMenuItem debugMenuItem = new JCheckBoxMenuItem("Debug");
        optionsMenu.add(debugMenuItem);
        optionsMenu.addSeparator();
        JCheckBoxMenuItem holdHostOutputCheckBox  = new JCheckBoxMenuItem("Hold subserver job output");
        holdHostOutputCheckBox.setAction(new HoldOutputOnHostAction(holdHostOutputCheckBox)); 
        optionsMenu.add(holdHostOutputCheckBox); 

        //didn't work by placing it in panels. in java 1.5 
        
        final JRadioButtonMenuItem traceVerbBt = new JRadioButtonMenuItem("   Verb only");
        final JRadioButtonMenuItem traceAllBt = new JRadioButtonMenuItem ("   Verb and data");
        optionsMenu.add(traceChkBox);
        traceChkBox.setEnabled(false);
        optionsMenu.add(traceVerbBt);
        traceVerbBt.setEnabled(false);
        traceVerbBt.setSelected(true);

        optionsMenu.add(traceAllBt);
        traceAllBt.setEnabled(false);

        ButtonGroup traceGrp = new ButtonGroup();
        traceGrp.add(traceVerbBt);
        traceGrp.add(traceAllBt);


        traceChkBox.addItemListener
        (new ItemListener() {
             public void itemStateChanged(ItemEvent ie) {
                 if (traceChkBox.isSelected()) {
                     traceVerbBt.setEnabled(true);
                     traceAllBt.setEnabled(true);
                     if (traceVerbBt.isSelected()) {
                         System.out.println("set trace options.");
                         com.ibm.sdwb.build390.mainframe.MainframeOutputTraceOptions.getInstance().setTraceVerbOnly(true);
                     } else if (traceAllBt.isSelected()) {
                         com.ibm.sdwb.build390.mainframe.MainframeOutputTraceOptions.getInstance().setTraceVerbAndData(true);

                     }
                 } else {
                     traceVerbBt.setEnabled(false);
                     traceAllBt.setEnabled(false);
                     com.ibm.sdwb.build390.mainframe.MainframeOutputTraceOptions.getInstance().setTraceVerbOnly(false);
                     com.ibm.sdwb.build390.mainframe.MainframeOutputTraceOptions.getInstance().setTraceVerbAndData(false);
                 }
             }
         });           

        traceVerbBt.addItemListener
        (new ItemListener() {
             public void itemStateChanged(ItemEvent ie) {
                 if (traceChkBox.isSelected()) {
                     if (traceVerbBt.isSelected()) {
                         com.ibm.sdwb.build390.mainframe.MainframeOutputTraceOptions.getInstance().setTraceVerbOnly(true);
                         com.ibm.sdwb.build390.mainframe.MainframeOutputTraceOptions.getInstance().setTraceVerbAndData(false);
                     }
                 }
             }
         });

        traceAllBt.addItemListener
        (new ItemListener() {
             public void itemStateChanged(ItemEvent ie) {
                 if (traceChkBox.isSelected()) {
                     if (traceAllBt.isSelected()) {
                         com.ibm.sdwb.build390.mainframe.MainframeOutputTraceOptions.getInstance().setTraceVerbOnly(false);
                         com.ibm.sdwb.build390.mainframe.MainframeOutputTraceOptions.getInstance().setTraceVerbAndData(true);
                     }
                 }
             }
         });           

        // optionsMenu.add(tracePanel);
        optionsMenu.addSeparator();
        optionsMenu.add(standardErrorMenuItem);

        optionsMenu.addSeparator();
        optionsMenu.add(new RequireTabVisitCheckbox());

        JMenu schedulerMenu = new JMenu("Scheduler");
        buildSchedulerMenu(schedulerMenu);
        toolsMenu.add(schedulerMenu);
        toolsMenu.addSeparator();
        toolsMenu.add(new ViewBuildLogAction());

        JMenuItem helpTableOfContentsMenuItem          = new JMenuItem("Help table of contents");
        helpTableOfContentsMenuItem.addActionListener(new java.awt.event.ActionListener() {
                                                          public void actionPerformed(java.awt.event.ActionEvent evt) {
                                                              new Thread(new Runnable() {
                                                                             public void run() {
                                                                                 SetupManager.getSetupManager().createSetupInstance(); //this is just to create a current library info help loader instance.
                                                                                 MBUtilities.ShowHelp("",HelpTopicID.TABLE_OF_CONTENTS);
                                                                             }
                                                                         }).start();
                                                          }
                                                      });

        helpMenu.add(helpTableOfContentsMenuItem);
        createSiteSpecificMenu(helpMenu);
        registerFrameInHelp();
        helpMenu.addSeparator();
        helpMenu.add(new AboutAction());



        f.setJMenuBar(mb);

        // Handle debug switch
        debugMenuItem.addItemListener(new ItemListener() {
                                          public void itemStateChanged(ItemEvent ie) {
                                              MBClient.set_debug(debugMenuItem.getState());
                                              traceChkBox.setEnabled( debugMenuItem.getState() );
                                              if (!debugMenuItem.getState()) {
                                                  traceChkBox.setSelected(false); 
                                              }
                                          }
                                      });

        // Handle error switch
        standardErrorMenuItem.addItemListener(new ItemListener() {
                                                  public void itemStateChanged(ItemEvent ie) {
                                                      new Thread (new Runnable() {
                                                                      public void run() {
                                                                          if (standardErrorMenuItem.isSelected()) {
                                                                              try {
                                                                                  MBClient.stdErr.updateErrorOutput();
                                                                              } catch (IOException ioe) {
                                                                                  lep.LogException("There was an error opening the stdErr viewer", ioe);
                                                                              }
                                                                          } else {
                                                                              synchronized(getInterfaceSingleton()) {
                                                                                  StdErrPanel.dispose();
                                                                                  StdErrPanel = null;
                                                                              }
                                                                          }
                                                                      }
                                                                  }).start();
                                                  }
                                              });



    }

    private void buildPasswordMenu(final JMenu enterNewPasswordMenu) {
        // Handle Options-Password
        enterNewPasswordMenu.addMenuListener(new MenuListener() {
                                                 public void menuSelected(MenuEvent evt) {
                                                     enterNewPasswordMenu.removeAll();
                                                     Vector enteredKeys = new Vector();
                                                     MBMainframeInfo tempMainInfo = SetupManager.getSetupManager().getCurrentMainframeInfo();
                                                     if (tempMainInfo!=null) {
                                                         final String tempKey = tempMainInfo.getMainframeUsername()+"@"+tempMainInfo.getMainframeAddress();
                                                         if (!enteredKeys.contains(tempKey)) {
                                                             JMenuItem tempItem = new JMenuItem(tempKey);
                                                             enterNewPasswordMenu.add(tempItem);
                                                             tempItem.addActionListener(new ActionListener() {
                                                                                            public void actionPerformed(ActionEvent evt) {
                                                                                                new Thread(new Runnable() {
                                                                                                               public void run() {
                                                                                                                   PasswordManager.getManager().setPassword(tempKey, "");
                                                                                                                   try {
                                                                                                                       PasswordManager.getManager().getPassword(tempKey,false); /* 02/12/2004 INT1757 mixed-case support */
                                                                                                                   } catch (MBBuildException npe) {
                                                                                                                   }
                                                                                                               }
                                                                                                           }).start();
                                                                                            }
                                                                                        });
                                                             enteredKeys.addElement(tempKey);
                                                         }
                                                     }

                                                     final  LibraryInfo tempLibInfo = SetupManager.getSetupManager().getCurrentLibraryInfo();
                                                     if (tempLibInfo!=null) {
                                                         if (tempLibInfo.isUsingPasswordAuthentication()) {
                                                             if (!enteredKeys.contains(tempLibInfo.getAuthenticationKey())) {
                                                                 JMenuItem tempItem = new JMenuItem(tempLibInfo.getAuthenticationKey());
                                                                 enterNewPasswordMenu.add(tempItem);
                                                                 tempItem.addActionListener(new ActionListener() {
                                                                                                public void actionPerformed(ActionEvent evt) {
                                                                                                    new Thread(new Runnable() {
                                                                                                                   public void run() {
                                                                                                                       PasswordManager.getManager().setPassword(tempLibInfo.getAuthenticationKey(), null);
                                                                                                                       try {
                                                                                                                           PasswordManager.getManager().getPassword(tempLibInfo.getAuthenticationKey());
                                                                                                                       } catch (MBBuildException npe) {
                                                                                                                       }
                                                                                                                   }
                                                                                                               }).start();
                                                                                                }
                                                                                            });
                                                                 enteredKeys.addElement(tempLibInfo.getAuthenticationKey());
                                                             }
                                                         }
                                                     }

                                                 }

                                                 public void menuDeselected(MenuEvent evt) {
                                                 }
                                                 public void menuCanceled(MenuEvent evt) {
                                                 }
                                             });

    }

    private void buildSchedulerMenu(final JMenu schedulerMenu) {

        // Handle Options-Password
        schedulerMenu.addMenuListener(new MenuListener() {
                                          public void menuSelected(MenuEvent evt) {
                                              schedulerMenu.removeAll();
                                              Set tempMainInfo = SetupManager.getSetupManager().getMainframeInfoSet();
                                              Vector enteredKeys = new Vector();
                                              for (Iterator mainframeIterator = tempMainInfo.iterator(); mainframeIterator.hasNext();) {
                                                  final MBMainframeInfo tempInfo = (MBMainframeInfo) mainframeIterator.next();
                                                  final String tempMainframeName = tempInfo.getMainframeAddress();
                                                  final String tempMainframePort = tempInfo.getMainframePort();
                                                  final String tempKey = tempMainframeName + "@" + tempMainframePort;
                                                  if (!enteredKeys.contains(tempKey)) {
                                                      JMenuItem tempItem = new JMenuItem(tempKey);
                                                      schedulerMenu.add(tempItem);
                                                      tempItem.addActionListener(new ActionListener() {
                                                                                     public void actionPerformed(ActionEvent evt) {
                                                                                         new Thread(new Runnable() {
                                                                                                        public void run() {
                                                                                                            Setup setup = SetupManager.getSetupManager().createSetupInstance(); //doesn't sound like a good idea. to-do tomorrow.
                                                                                                            setup.setMainframeInfo(tempInfo);
                                                                                                            new MBJobSchedulerPage(setup);
                                                                                                        }
                                                                                                    }).start();
                                                                                     }
                                                                                 });
                                                      enteredKeys.addElement(tempKey);
                                                  }
                                              }
                                          }
                                          public void menuDeselected(MenuEvent evt) {
                                          }
                                          public void menuCanceled(MenuEvent evt) {
                                          }
                                      });

    }

    //not sure if this is still being used. We had some updates to the help system to incorporate sitespecific help.
    private void createSiteSpecificMenu(JMenu menu) {
        // If the site help file is not found, disable the site help menu selection //Defect_247
        try {
            File site_help = new File(MBGlobals.Build390_path+MBConstants.SITE_HELP_FILE);
            if (site_help.exists()) {
                menu.add(new JSeparator());
                JMenu siteSpecificHelp               = new JMenu("Site specific "+MBConstants.productName+" Help"); // Defect_247
                JMenu siteSpecificHelpMenuItem               = new JMenu("Site specific "+MBConstants.productName+" Help"); // Defect_247
                siteSpecificHelpMenuItem.addActionListener(MBUtilities.getHelpListener("",""));
                menu.add(siteSpecificHelp);      
                siteSpecificHelp.add(siteSpecificHelpMenuItem); 
                // if the site help bookmarks file exists, parse it and create a menu item
                // and a listener for each bookmark
                File site_help_bms = new File(MBGlobals.Build390_path+MBConstants.SITE_HELP_BOOKMARKS_FILE);
                if (site_help_bms.exists()) {
                    try {
                        BufferedReader BookmarkReader = new BufferedReader(new FileReader(MBGlobals.Build390_path+MBConstants.SITE_HELP_BOOKMARKS_FILE));
                        String line = new String();
                        String firstToken = null;
                        String secondToken = null;
                        StringTokenizer lineTokenizer;
                        while ((line = BookmarkReader.readLine()) != null) {
                            lineTokenizer = new StringTokenizer(line, "@");
                            if (lineTokenizer.hasMoreElements()) {
                                firstToken = lineTokenizer.nextToken();
                            }
                            if (lineTokenizer.hasMoreElements()) {
                                secondToken = lineTokenizer.nextToken();
                            }
                            if (firstToken!=null & secondToken!=null) {
                                // put the label and bookmark into a vector so that the order can be maintained
                                siteBookmarksVector.addElement(firstToken);
                                siteBookmarksVector.addElement(secondToken);
                                // put the pair into a hash so that, in the action listener, we can look up 
                                // the bookmark based on the label 
                                siteBookmarksHash.put(firstToken, secondToken);

                            }
                        }
                        BookmarkReader.close();
                        // if bookmarks were found, create menu items for them
                        if (!siteBookmarksVector.isEmpty()) {
                            siteSpecificHelp.add(new JSeparator());
                            int idx = 0;
                            while (idx < siteBookmarksVector.size()-1) {
                                String menustr = (String)siteBookmarksVector.elementAt(idx);
                                String bm = (String)siteBookmarksVector.elementAt(idx+1);
                                JMenuItem tempmi = new JMenuItem(menustr);
                                siteSpecificHelp.add(tempmi);
                                // add action listener
                                tempmi.addActionListener(new ActionListener() {
                                                             public void actionPerformed(ActionEvent evt) {
                                                                 String evtparams = evt.paramString();
                                                                 if (evtparams.indexOf("cmd=") != -1) {
                                                                     String bm1 = (String)siteBookmarksHash.get(evtparams.substring(evtparams.indexOf("cmd=")+4));
                                                                     MBUtilities.ShowSiteHelp(bm1);
                                                                 }
                                                             }
                                                         } );
                                idx= idx+2;
                            }
                        }
                    } catch (IOException ioe) {
                        lep.LogException("There was an error setting bookmarks for site help", ioe);
                    }
                }
            }
        }
        // Just ignore errors and leave the menu item disabled
        catch (Exception e) {
        }


    }

    private void registerFrameInHelp() {
        Thread helpThread = new Thread(new Runnable() {
                                           public void run() {
                                               try {
                                                   HelpController.getInstance().registerParentFrame(f);
                                               } catch (HelpException  hr) {
                                                   lep.LogException(hr);

                                               }

                                           }
                                       });
        helpThread.start();
        try {
            helpThread.join();
        } catch (java.lang.InterruptedException ief) {
        }


    }

    public synchronized void updateErrorPanel(String errorInfo) {
        if (StdErrPanel == null) {
            StdErrPanel = new MBStdErrorViewer(lep);
            StdErrPanel.setCheckItem(standardErrorMenuItem);
            standardErrorMenuItem.setSelected(true);
        }
        StdErrPanel.toFront();
        try {
            StdErrPanel.setSelected(true);
        } catch (java.beans.PropertyVetoException pve) {
            lep.LogException("There was an problem selecting the stdErr viewer", pve);
        }
        StdErrPanel.setText(errorInfo);
    }

    public synchronized void addFrame(JInternalFrame temp) {
        if (desk.getIndexOf(temp) < 0) {
            desk.add(temp);
        }
    }

    private void setUIDefaults() {
        //compatibility between 5.0 and 1.4.2
        Color controlBackground = UIManager.getDefaults().getColor("control");
        if (!controlBackground.equals(MBGuiConstants.ColorGeneralBackground)) {
            MBGuiConstants.ColorGeneralBackground = controlBackground;
        }

        UIManager.put("Label.foreground", MBGuiConstants.ColorRegularText);
        UIManager.put("ComboBox.foreground", MBGuiConstants.ColorRegularText);
        UIManager.put("Separator.foreground", MBGuiConstants.ColorRegularText);
        UIManager.put("ScrollPane.foreground", MBGuiConstants.ColorActionButton);
        UIManager.put("EditorPane.foreground", MBGuiConstants.ColorRegularText);
        UIManager.put("MenuBar.foreground", MBGuiConstants.ColorRegularText);
        UIManager.put("TextPane.foreground", MBGuiConstants.ColorRegularText);
        UIManager.put("RadioButton.foreground", MBGuiConstants.ColorRegularText);
        UIManager.put("TextArea.foreground", MBGuiConstants.ColorRegularText);
        UIManager.put("Panel.foreground", MBGuiConstants.ColorRegularText);
        UIManager.put("ToggleButton.foreground", MBGuiConstants.ColorRegularText);
        UIManager.put("Table.foreground", MBGuiConstants.ColorRegularText);
        UIManager.put("CheckBox.foreground", MBGuiConstants.ColorRegularText);
        UIManager.put("TabbedPane.foreground", MBGuiConstants.ColorRegularText);
        UIManager.put("Slider.foreground", MBGuiConstants.ColorRegularText);
        UIManager.put("List.foreground", MBGuiConstants.ColorRegularText);
        UIManager.put("ScrollBar.foreground", MBGuiConstants.ColorRegularText);
        UIManager.put("TextField.foreground", MBGuiConstants.ColorRegularText);
        UIManager.put("Viewport.foreground", MBGuiConstants.ColorRegularText);
        UIManager.put("MenuItem.foreground", MBGuiConstants.ColorRegularText);
        UIManager.put("TableHeader.foreground", MBGuiConstants.ColorRegularText);
        UIManager.put("Tree.foreground", MBGuiConstants.ColorRegularText);

        UIManager.put("Label.background", MBGuiConstants.ColorGeneralBackground);
        UIManager.put("RadioButton.background", MBGuiConstants.ColorGeneralBackground);
        UIManager.put("Panel.background", MBGuiConstants.ColorGeneralBackground);
        UIManager.put("ToggleButton.background", MBGuiConstants.ColorGeneralBackground);
        UIManager.put("Table.background", MBGuiConstants.ColorGeneralBackground);
        UIManager.put("CheckBox.background", MBGuiConstants.ColorGeneralBackground);
        UIManager.put("TabbedPane.background", MBGuiConstants.ColorGeneralBackground);
        UIManager.put("Slider.background", MBGuiConstants.ColorGeneralBackground);
        UIManager.put("ScrollBar.background", MBGuiConstants.ColorGeneralBackground);
        UIManager.put("Viewport.background", MBGuiConstants.ColorGeneralBackground);
        UIManager.put("TableHeader.background", MBGuiConstants.ColorGeneralBackground);
    }


    public com.ibm.sdwb.build390.logprocess.LogEventProcessor getLEP() {
        return lep;
    }

    public void ResetTrace() {
        traceChkBox.setSelected(false);
    }

    public void setVisible(boolean vis) {
        f.setVisible(vis);
    }

    public void setServiceFunctionsExposed(boolean tempService) {
        serviceFunctionsExposed = tempService;
    }

    public void run() {
        // Check restart stuff
        // If the restart .ser file exists and the dir corresponding to the buildid exists
        // and the status of that build is not complete, ask user if it should be restarted
        String idf = new String(MBGlobals.Build390_path+"misc"+File.separator+idFile);
        File idff = new File(idf);
        if (idff.exists()) {
            // get the buildid from the serialized object
            MBBuild build = null;
            try {
                ObjectInputStream ois = new ObjectInputStream(new FileInputStream(idf));
                build = (MBBuild)ois.readObject();
            } catch (IOException ioe) {
                lep.LogException("There was an error loading the "+idf+" build file", ioe);
            } catch (ClassNotFoundException e) {
                lep.LogException("Class not found error when trying to load the "+idf+" build file",  e);
            }
            if (!build.getProcessForThisBuild().hasCompletedSuccessfully()) {
                if (build.getBuildPathAsFile().exists()) {
                    boolean FastTrack = false;
                    if (!build.get_buildid().startsWith("U")) {
                        FastTrack = false;
                    } else {
                        FastTrack = true;
                    }
                }
            }
        }
        // if the ser file does not exist, show the about dialog
        File tf = new File(MBConstants.dontshowfile);
        if (!tf.exists()) {
            new MBAboutDialog(); 
        }
    }

// Handle File-Exit
    private class QuitAction extends AbstractAction {
        private QuitAction() {
            super("Exit");
        }
        public void actionPerformed(ActionEvent evt) {
            // serialize the point object of this window
            f.closeAll();
        }
    };

//Handle NewDriverbuild
    static  class NewDriverBuildAction extends AbstractAction {
        NewDriverBuildAction() {
            super("Driverbuild");
        }

        public void actionPerformed(ActionEvent evt) {
            new Thread(new Runnable() {
                           public void run() {
                               try {
                                   MBBuild build = new MBBuild("D",MBConstants.DRIVERBUILDDIRECTORY,new com.ibm.sdwb.build390.logprocess.LogEventProcessor());
                                   InternalFrameBuildPanelHolder dbPanel = DriverBuildPanel.getDriverBuildFrame(build, false);
                               } catch (MBBuildException mbe) {
                                   interfaceInstance.lep.LogException(mbe);
                               }
                           }
                       }).start();

        }
    };

//Handle New Usermod
    static class NewUsermodAction extends AbstractAction {
        NewUsermodAction() {
            super("++USERMOD");
        }
        public void actionPerformed(ActionEvent evt) {
            new Thread(new Runnable() {
                           public void run() {
                               try {
                                   com.ibm.sdwb.build390.info.UsermodGeneralInfo build = new com.ibm.sdwb.build390.info.UsermodGeneralInfo(new com.ibm.sdwb.build390.logprocess.LogEventProcessor());
                                   MultipleProcessMonitoringFrame multiFrame = UsermodPanel.getUsermodFrame(build, false);
                               } catch (MBBuildException mbe) {
                                   interfaceInstance.lep.LogException(mbe);
                               }
                           }
                       }).start();
        }
    };

// Handle File-LocalUserBuild
    static class NewUserBuildLocal extends AbstractAction {
        NewUserBuildLocal() {
            super("Local Parts");
        }
        public void actionPerformed(ActionEvent evt) {
            new Thread(new Runnable() {
                           public void run() {
                               try {
                                   MBUBuild build = new MBUBuild(new com.ibm.sdwb.build390.logprocess.LogEventProcessor());
                                   build.setSourceType(MBUBuild.LOCAL_SOURCE_TYPE);
                                   build.setFastTrack(false);
                                   InternalFrameBuildPanelHolder userPanel = UserBuildPanel.getUserBuildFrame(build, false);
                               } catch (MBBuildException mbe) {
                                   interfaceInstance.lep.LogException(mbe);
                               }
                           }
                       }).start();
        }
    };

// Handle File-PDSUserBuild
    static class NewPDSUserBuild extends AbstractAction {
        NewPDSUserBuild() {
            super("PDS");
        }
        public void actionPerformed(ActionEvent evt) {
            new Thread(new Runnable() {
                           public void run() {
                               try {
                                   MBUBuild build = new MBUBuild(new com.ibm.sdwb.build390.logprocess.LogEventProcessor());
                                   build.setSourceType(MBUBuild.PDS_SOURCE_TYPE);
                                   build.setFastTrack(false);
                                   InternalFrameBuildPanelHolder userPanel = UserBuildPanel.getUserBuildFrame(build, false);
                               } catch (MBBuildException mbe) {
                                   interfaceInstance.lep.LogException(mbe);
                               }
                           }
                       }).start();
        }
    };

// Handle File-LocalUserBuild
    static class NewFastTrackLocal extends AbstractAction {
        NewFastTrackLocal() {
            super("Local Parts");
        }
        public void actionPerformed(ActionEvent evt) {
            new Thread(new Runnable() {
                           public void run() {
                               try {
                                   MBUBuild build = new MBUBuild(new com.ibm.sdwb.build390.logprocess.LogEventProcessor());
                                   build.setSourceType(MBUBuild.LOCAL_SOURCE_TYPE);
                                   build.setFastTrack(true);
                                   InternalFrameBuildPanelHolder userPanel = UserBuildPanel.getUserBuildFrame(build, false);
                               } catch (MBBuildException mbe) {
                                   interfaceInstance.lep.LogException(mbe);
                               }
                           }
                       }).start();
        }
    };

// Handle File-PDSUserBuild
    static class NewFastTrackPDS extends AbstractAction {
        NewFastTrackPDS() {
            super("PDS");
        }
        public void actionPerformed(ActionEvent evt) {
            new Thread(new Runnable() {
                           public void run() {
                               try {
                                   MBUBuild build = new MBUBuild(new com.ibm.sdwb.build390.logprocess.LogEventProcessor());
                                   build.setSourceType(MBUBuild.PDS_SOURCE_TYPE);
                                   build.setFastTrack(true);
                                   InternalFrameBuildPanelHolder userPanel = UserBuildPanel.getUserBuildFrame(build, true);
                               } catch (MBBuildException mbe) {
                                   interfaceInstance.lep.LogException(mbe);
                               }
                           }
                       }).start();
        }
    };

    private  class SetupAction extends AbstractAction {
        private SetupAction() {
            super("Setup");
        }
        public void actionPerformed(ActionEvent evt) {
            if (!inSetup) {
                inSetup = true;
                new Thread (new Runnable() {
                                public void run() {
                                    new com.ibm.sdwb.build390.userinterface.graphic.panels.setup.SetupInformation(null);
                                    inSetup = false;
                                }
                            }).start();
            }
        }
    };



    /**
     * Handle hold switch //AddHoldTrace
     * Defect INT0755
     * "Hold output on host" option misleading.
     * Users all think that this option will hold generated job output. It does not. All it does is hold the subserver job output which is almost always totally useless.
     * The only way to hold job output is to uncheck the "purge job output" box in the driver build options panel.
     * This should be made clear in the documentation and help. It causes a lot of confusion and rework trying to get job output.
     * "Hold output on host" should be changed to "Hold subserver job output" and an "Are you sure?" popup should be added.
     */
    private class HoldOutputOnHostAction extends AbstractAction {
        private JCheckBoxMenuItem subServerHoldCheckBox;
        HoldOutputOnHostAction(JCheckBoxMenuItem subServerHoldCheckBox) {
            super("Hold subserver job output");
            this.subServerHoldCheckBox = subServerHoldCheckBox;
        }
        /* the method to override for whatever action you want to perform in response
        to a click.
        */
        public void actionPerformed(ActionEvent e) {
            new Thread(new Runnable() {
                           public void run() {
                               if (subServerHoldCheckBox.isSelected()) {
                                   MBMsgBox  msg = new MBMsgBox("Warning !","This option will hold the  SUBSERVER JOB OUTPUT ONLY.The only way to hold job output is to uncheck the purge job output box in the driver build options panel. Do you wish to hold the subserver job output ? ",null,true);
                                   subServerHoldCheckBox.setSelected(msg.isAnswerYes());
                               } else {
                                   subServerHoldCheckBox.setSelected(false);
                               }

                               com.ibm.sdwb.build390.mainframe.MainframeOutputTraceOptions.getInstance().setHoldSubServerJobOutput(subServerHoldCheckBox.getState());
                           }
                       }).start();



        }
    };



    static class NewManagePageAction extends AbstractAction {

        NewManagePageAction() {
            super("Manage Releases/Drivers");
        }

        public void actionPerformed(ActionEvent ie) {
            new Thread (new Runnable() {
                            public void run() {
                                try {
                                    new ManageReleasesFrame();
                                } catch (MBBuildException mbe) {
                                    //lep.LogException(mbe);
                                }
                            }
                        }).start();
        }
    };

    static class NewManageProcessAction extends AbstractAction {
        NewManageProcessAction() {
            super("Manage Processes");
        }
        public void actionPerformed(ActionEvent ie) {
            new Thread (new Runnable() {
                            public void run() {
                                new com.ibm.sdwb.build390.userinterface.graphic.panels.ProcessManagementFrame(new File(MBGlobals.Build390_path));
                            }
                        }).start();
        }
    };

    static class NewMetadataEditorAction extends AbstractAction {
        NewMetadataEditorAction() {
            super("Metadata Editor");
        }
        public void actionPerformed(ActionEvent ie) {
            new Thread (new Runnable() {
                            public void run() {
                                try {

                                    new MetadataEditorFrame(); 
                                } catch (MBBuildException mbe) {
                                    // lep.LogException (mbe);
                                }

                            }
                        }).start();
        }
    };


    private class ViewBuildLogAction extends AbstractAction {
        ViewBuildLogAction() {
            super("View Build Log");
        }
        public void actionPerformed(ActionEvent ie) {
            new Thread (new Runnable() {
                            public void run() {
                                new MBEdit(MBGlobals.Build390_path+MBConstants.LOGFILEPATH,lep);
                            }
                        }).start();
        }
    };


    private class RequireTabVisitCheckbox extends JCheckBoxMenuItem implements java.awt.event.ActionListener {
        private String REQUIRETABVISITACTION = "REQUIRETABVISITACTION";
        private int temp = 0;

        private RequireTabVisitCheckbox() {
            super("Require tabs in tabbed interface be visited.");
            addActionListener(this);
            Boolean requiredSetting = (Boolean) MBBasicInternalFrame.getGenericStatic(REQUIRETABVISITACTION);
            if (requiredSetting == null) {
                setState(true); // default it to true as Kathy insisted on
            } else {
                setState(requiredSetting.booleanValue());
            }
        }

        public void actionPerformed(ActionEvent evt) {
            new Thread(new Runnable() {
                           public void run() {
                               synchronized(REQUIRETABVISITACTION) {
                                   com.ibm.sdwb.build390.userinterface.graphic.widgets.EnhancedTabbedPane.setRequireAllTabsToBeVisited(getState());
                                   MBBasicInternalFrame.putGenericStatic(REQUIRETABVISITACTION, Boolean.valueOf(getState()));
                               }
                           }
                       }).start();
        }
    };


    private class AboutAction extends AbstractAction {

        private AboutAction() {
            super("About " + MBConstants.productName);
        }

        public void actionPerformed(ActionEvent evt) {
            new Thread(new Runnable() {
                           public void run() {
                               new MBAboutDialog();
                           }
                       }).start();
        }
    };



    private class MainInterfaceActionConfigurer implements ActionConfigurer {
        private JMenuBar menuBar =null;

        public JMenuBar getMenuBar() {
            if (menuBar ==null) {
                menuBar = new JMenuBar();
            }
            return menuBar;
        }
        public java.awt.Component getFrame() {
            return getframe();
        }


        public MBStatus getStatusHandler() {
            return status;
        }
        public LogEventProcessor getLEP() {
            return lep;
        }

        public void handleUIEvent(com.ibm.sdwb.build390.userinterface.event.UserInterfaceEvent event) {
        }

        public String toString() {
            return "MainInterface Action Configurer  : " + getframe().getTitle();
        }
    }



    public void setWaitCursor() {
        synchronized(cursorLock) {
            waitCursor++;
            f.setCursor(new Cursor(Cursor.WAIT_CURSOR));
        }
    }

    public void clearWaitCursor() {
        synchronized(cursorLock) {
            waitCursor--;
            if (waitCursor < 0) {
                waitCursor = 0;
            }
            if (waitCursor == 0) {
                f.setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
            }
        }
    }



}


