
package com.ibm.sdwb.build390.userinterface.graphic.panels.metadata;
/*******************************************************************************/
/* FindFrame class for Build390 java client                                    */  
/* This class  is used in searching of some matching criteria                  */
/*******************************************************************************/
//02/11/2005 SDWB2398  Replace metadata in cmvc only(phase 1)
/*******************************************************************************/

import java.awt.*;
import java.awt.event.*;
import java.util.*;

import javax.swing.*;
import javax.swing.border.*;

import com.ibm.sdwb.build390.*;
import com.ibm.sdwb.build390.logprocess.*;
import com.ibm.sdwb.build390.userinterface.graphic.widgets.*;
import com.ibm.sdwb.build390.help.*;

public class FindMetadataInHostAndLibraryFrame extends AbstractFindFrame {

    //begin TST2211
    java.util.List keyWords;
    boolean keyWordValid=false;
    //end TST2211

    public  FindMetadataInHostAndLibraryFrame(MBInternalFrame pFrame, String keyWords[], LogEventProcessor lep) throws MBBuildException {
        super("Find(Host & Library)", pFrame, lep);

        this.keyWords=Arrays.asList(keyWords);//TST2211
    }

    public void doFindAction()
    {
        MetadataComboHistoryPanel  panel = (MetadataComboHistoryPanel)getFinderUI();
        Vector findEntries =  ((MetadataEditorFrame.ShowMetadataCriteriaDialog)panel.getAction()).getFindEntries();
        if (!findEntries.isEmpty()) {

            //begin TST2211
            for (Iterator iterator = findEntries.iterator();iterator.hasNext();) {
                String entry = (String)iterator.next();

                keyWordValid=false;

                if (!isCorrectFormat(entry)) {
                    if (keyWordValid == true) {
                        problemBox("Find error!", "Incorrect entry format.\n"+
                                   "Please enter using the following format=>\n"+
                                   "<MetadataKeyword> <\"EQ\"|\"GT\"|\"LT\"|\"NE\"> <MetadataValue><\",\" | \"|\">\n\n" + getSyntaxHelp()); 
                    } else {
                        problemBox("Find error!", "Invalid MetadataKeyword.\n\n"+getSyntaxHelp()); 
                    }

                    return;
                }
            }
            //end TST2211

            getSelection().setFindEntries(findEntries);
        } else {
            problemBox("Find error!","Please enter a text to find.\n\n"+getSyntaxHelp()); 
            return;
        } 

        getSelection().getOptions().setRegularExpression(isUseRegularExpressions());
        getSelection().getOptions().setListAllOccurrences(listAllOccurrences());
        dispose();
    }


    /*override in subclasses to point to the right link */
    public void helpAction()
    {
        MBUtilities.ShowHelp("",HelpTopicID.FILTER_METADATA_HOST_CMVC_HELP);

    }


    //begin TST2211
    private boolean isCorrectFormat(String entry)
    {
        boolean formatState = false;

        String ary[]=new String[4];

        StringTokenizer strTok = new StringTokenizer(entry," ");

        int i=0;
        while (strTok.hasMoreTokens()) {
            if (i==4) {
                return false;
            }

            ary[i++]= (String) strTok.nextToken();

        }

        if (ary[0]==null) {
            return false;
        } else {
            String keyword = ary[0].trim();

            if (keyword.startsWith("[")) {
                keyword = keyword.substring(1);
            }

            if (!keyWords.contains(keyword)) {
                keyWordValid=false;

                return false;
            } else {
                keyWordValid=true;
            }
        }

        if (ary[1]!=null) {
            if (ary[1].equals("EQ")|ary[1].equals("GT")|ary[1].equals("LT")|ary[1].equals("NE")) {
                formatState = true;
            }
        } else {
            return false;
        }

        if (ary[2]==null) {
            return false;
        } else {
            if (!(ary[2].endsWith(",")|ary[2].endsWith("|")|ary[2].endsWith("]"))) {
                if (ary[3]!=null) {
                    if (ary[3].endsWith("]")) {
                        ary[3] = ary[3].substring(0,ary[3].length()-1);
                    }

                    if (ary[3].trim().equals(",")|ary[3].trim().equals("|")) {
                        formatState = true;
                    }
                } else {
                    formatState = false;
                }
            } else {
                if (ary[2].length()==1) {
                    formatState = false;
                }

                if (ary[3]!=null) {
                    formatState = false;
                }
            }
        }
        return formatState;
    }
    //end TST2211
}

