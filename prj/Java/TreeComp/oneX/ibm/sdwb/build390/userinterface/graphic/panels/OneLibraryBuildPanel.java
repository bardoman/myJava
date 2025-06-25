package com.ibm.sdwb.build390.userinterface.graphic.panels;
/*********************************************************************/
/* OneLibraryBuildPanel class for the Build/390 client                         */
/*   Builds the PTF set build window                                 */
/*********************************************************************/

import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.util.*;
import java.text.DateFormat;
import javax.swing.*;
import javax.swing.table.*;
import javax.swing.event.*;
import com.ibm.sdwb.build390.logprocess.*;
import com.ibm.sdwb.build390.*;

import com.ibm.sdwb.build390.userinterface.graphic.widgets.*;

/** Create a OneLibraryBuildPanel extends Dialog
*/
public class OneLibraryBuildPanel extends AnimationStatusPanel {

    private final static String EXCLUDECONSTANT = "excluded";
    private final static String INCLUDECONSTANT = "included";
    private MBBuild build = null;
    private String baseDriver;

    /** Constructor */

    public OneLibraryBuildPanel(MBBuild tempBuild, String baseDriver){
        super(null);
        build = tempBuild;
        this.baseDriver = baseDriver;
        lep.addEventListener(build.getLogListener());
        lep.LogSecondaryInfo("Debug", "OneLibraryBuildPanel:Entry");

        setVisible(true);
    }

}
