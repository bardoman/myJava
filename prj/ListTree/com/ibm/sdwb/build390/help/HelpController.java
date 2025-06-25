package com.ibm.sdwb.build390.help;

//*****************************************************************
//07/19/2004 #DEF.INT1935: Help broke on Linux due to malformed URL
//*****************************************************************

import java.io.File;
import java.net.*;
import java.util.HashSet;
import java.util.Set;

import javax.swing.*;

import com.ibm.sdwb.build390.*;

import oracle.help.CSHManager;
import oracle.help.Help;
import oracle.help.common.util.Canonicalizer;
import oracle.help.library.Book;
import oracle.help.library.helpset.HelpSet;
import oracle.help.navigator.Navigator;
import oracle.help.navigator.NavigatorEvent;
import oracle.help.navigator.tocNavigator.TOCNavigator;

public class HelpController {

    private Help helpObject;   
    private CSHManager contextManager = null;

    private static final String HELP_ON_HELP_PATH =  "help"+ java.io.File.separator + "helpOnHelp" + java.io.File.separator+ "helpOnHelp.htm";
    private HelpLoaderInterface loader;
    private static HelpController helpController = null;

    private  Set bookCache = new HashSet();


    private HelpController() {

    }

    public static HelpController getInstance()   throws HelpException   {
        if (helpController ==null) {
            helpController = new HelpController();
        }
        return helpController;
    }


    public void  registerParentFrame(JFrame frame) throws HelpException {
        createHelpObject();
        createContextSensitiveManager();
        getHelpObject().registerClientWindow(frame);
        try {
            getHelpObject().setHelpOnHelp(HelpUtilities.pathToURL((new File(HELP_ON_HELP_PATH)).getAbsolutePath()));
        } catch (java.net.MalformedURLException murl) {
            throw new HelpException ("Attempt to convert path " + HELP_ON_HELP_PATH + " to url failed.",murl);
        } catch (java.io.IOException ioe) {
            throw new com.ibm.sdwb.build390.help.HelpException ("Invalid HelpOnHelp  filename " + HELP_ON_HELP_PATH,ioe);
        }

    }

    private void createHelpObject() {
        if (helpObject==null) {
            /*the last true - indicates that you want to doc the TopicViewer and the tableofcontents display */
            helpObject =new Help(false, true,true);
        }

    }



    private  void createContextSensitiveManager() throws HelpException {
        try {
            if (contextManager ==null) {
                contextManager = new CSHManager(getHelpObject());
            }
        } catch (Exception ge) {
            throw new HelpException("Context Sensitive Help Manager creation error!." + MBConstants.NEWLINE,ge);
        }

    }

    public   CSHManager getContextSensitiveManager() {
        return contextManager;
    }


    public Help getHelpObject() {
        return helpObject;
    }



    public void setHelpLoaderInterface(HelpLoaderInterface loader) {
        this.loader=loader;

        if (!bookCache.contains(loader.getHelpBook())) {
            bookCache.add(loader.getHelpBook());
            getContextSensitiveManager().addBook(loader.getHelpBook(),true);
        }
    }


    public void showTopic( final String TopicID) throws com.ibm.sdwb.build390.help.HelpException {
        try {
            final  java.net.URL helpURL = loader.getHelpBook().mapIDToURL(TopicID);
            if (helpURL==null) {
                throw new HelpException ("The url for topicid " + TopicID + " is null"+MBConstants.NEWLINE +
                                         "Please make sure that a valid mapping exists for topicid " + TopicID + ", " + MBConstants.NEWLINE +" in " + loader.toString());
            }
            if (helpURL != null) {
                getContextSensitiveManager().showTopic(TopicID);
                oracle.help.navigator.Navigator[] navigators = getHelpObject().getAllNavigators();
                for (int i=0;i<navigators.length;i++) {
                    if (navigators[i] instanceof com.ibm.sdwb.build390.help.B390TOCNavigator) {
                        ((com.ibm.sdwb.build390.help.B390TOCNavigator)navigators[i]).selectMatchingTopic(helpURL);
                    }
                }
            }


        } catch (oracle.help.topicDisplay.TopicDisplayException tde) {
            java.net.URL faultyURL = loader.getHelpBook().mapIDToURL(TopicID);
            throw new HelpException("The topicid "+TopicID + " is invalid, due to a faulty url location " + faultyURL.toExternalForm(),tde);
        }

    }


}

