package com.ibm.sdwb.build390;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;

import com.ibm.sdwb.build390.logprocess.LogEventProcessor;
import com.ibm.sdwb.build390.user.SetupManager;
import com.ibm.sdwb.build390.userinterface.graphic.MainInterface;
/***************************************************************************/
/* Java MBBasicInternalFrame class for the Build/390 client                */
/*  Builds a listbox, populates it and adds the action listeners specified */
/***************************************************************************/
// Changes
// Date     Defect/Feature      Reason
// 03/15/99 EditorSelect        Allow user to select editor to be used
// 04/30/99 EditorError         Check editor path before call
// 05/03/99 EditorError         put quotes around editor path if in windows
// 05/21/99 wrap_quotes         wrap quotes around file name in case of blanks
// 03/07/2000 reworklog         changes to implement the log stuff using listeners
/*********************************************************************/

public class MBEdit {

    private MBEditPanel editPanel;
    private static Process child = null;
    private LogEventProcessor lep=null;

    public MBEdit(final String tempFn, boolean editable,final LogEventProcessor lep) {
        this.lep=lep;
        try {
            if (isWorthLookingAt(tempFn)) {
                if (MainInterface.getInterfaceSingleton() != null) {
                    if (SetupManager.getSetupManager().hasSetup()) {
                        if (!SetupManager.getSetupManager().IsDefaultEditorSelected()) {
                            useEditor(tempFn);
                        } else {
                            editPanel = new MBEditPanel(tempFn,lep);
                        }
                    } else {
                        editPanel = new MBEditPanel(tempFn,lep);
                    }
                } else {
                    System.out.println("Please see file " + tempFn);
                }
            }
        } catch (MBBuildException mbe) {
            lep.LogException(mbe);
        }
    }

    public MBEdit(String tempFn,LogEventProcessor lep) {
        this(tempFn,false,lep);
    }

    private boolean isWorthLookingAt(String filename) throws GeneralError{
        BufferedReader tempReader = null;
        try {
            tempReader = new BufferedReader(new FileReader(filename));
            String line = tempReader.readLine();
            if (line!=null) {
                return !line.trim().equals("//");
            }
            return true;
        } catch (IOException ioe) {
            throw new GeneralError("Error opening file " + filename, ioe);
        } finally {
            if (tempReader!=null) {
                try {
                    tempReader.close();
                } catch (IOException ioe2) {
                    // do nothing here, we're in disaster recovery
                }
            }
        }
    }

    // EditorSelect
    void useEditor(String tempFn) throws com.ibm.sdwb.build390.MBBuildException {
        try {
            // wrap_quotes around filename incase of blanks
            if (!tempFn.startsWith("\"")) {
                tempFn = "\""+tempFn+"\"";
            }
            // EditorError
            String editorPath = SetupManager.getSetupManager().getEditorPath();
            if (editorPath != null) {
                File bf = new File(editorPath);
                if (!bf.exists()) {
                    new MBMsgBox("Editor not found","The editor path specified in your setup "+editorPath+" does not exist\n\nSelect setup from the menu and specify a valid path.", null);
                } else {
                    String cmd[] = new String[2];
                    if ((System.getProperty("os.name").toLowerCase().indexOf("wind") > -1)) {
                        cmd[0]="\""+editorPath+"\"";
                    } else {
                        cmd[0]=editorPath;
                    }
                    cmd[1]=tempFn;
                    child = Runtime.getRuntime().exec(cmd);
                }
            } else {
                new MBMsgBox("Editor path invalid","The editor path specified in your setup does not exist\n\nSelect setup from the menu and specify a valid path.", null);
            }
        } catch (IOException ioe) {
            throw new GeneralError("There was a problem creating the editor.  Check your editor path.", ioe);
        }
    }
}
