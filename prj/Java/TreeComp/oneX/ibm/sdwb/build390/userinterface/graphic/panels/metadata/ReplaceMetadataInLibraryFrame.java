package com.ibm.sdwb.build390.userinterface.graphic.panels.metadata;
/*******************************************************************************/
/* ReplaceFrame class for Build390 java client                                 */  
/* This class  is used in searching of some matching criteria                  */
/*******************************************************************************/
//02/11/2005 SDWB2398  Replace metadata in cmvc only(phase 1)
/*******************************************************************************/
import java.util.*;
import java.awt.*;
import java.awt.event.*;
import javax.swing.border.*;
import javax.swing.*;

import com.ibm.sdwb.build390.*;
import com.ibm.sdwb.build390.logprocess.*;
import com.ibm.sdwb.build390.userinterface.graphic.panels.*;
import com.ibm.sdwb.build390.userinterface.graphic.widgets.*;
import com.ibm.sdwb.build390.help.*;

public class ReplaceMetadataInLibraryFrame extends AbstractReplaceFrame {



    public  ReplaceMetadataInLibraryFrame(MBInternalFrame pFrame,LogEventProcessor lep) throws MBBuildException {
        super("Replace(Library)", pFrame, lep);
    }


    /*override in subclasses to point to the right link */
    public void helpAction() {
        MBUtilities.ShowHelp("",HelpTopicID.REPLACE_METADATA_CMVC_HELP);

    }


    public void doReplaceAction() {
        ComboWithMetadataKeywordsPanel panel = (ComboWithMetadataKeywordsPanel)getFinderUI();

        boolean isDataExists = panel.getFindEntry()!=null && getReplaceWithText()!=null ? panel.getFindEntry().length() > 0 && getReplaceWithText().length() > 0   : false;

        if (isDataExists) {
            getSelection().addFindEntry(panel.getMetadataKeyword() +"="+panel.getFindEntry());
            getSelection().addReplaceEntry(getReplaceWithText());
        } else {
            problemBox("Find error!","Please enter a text to find.\n" +getSyntaxHelp()); 
            return;
        } 


        getSelection().getOptions().setRegularExpression(isUseRegularExpressions());  
        getSelection().getOptions().setListAllOccurrences(isReplaceAllOccurrences());  
        dispose();

    }


    private String getReplaceWithText(){
        JComboBox replaceText = (JComboBox)getReplacerUI();
        String replaceWith = "";
        replaceWith = replaceText.getSelectedItem()!=null ? ((String)replaceText.getSelectedItem()).trim()   : "";
        return replaceWith;

    }


}





