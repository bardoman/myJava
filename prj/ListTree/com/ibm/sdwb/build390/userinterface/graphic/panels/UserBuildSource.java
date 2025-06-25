package com.ibm.sdwb.build390.userinterface.graphic.panels;

/*********************************************************************/
/* UserBuildSource interface for the Build/390 client          */
/*  allows access to UserBuildSourcePanels                    */
/*********************************************************************/

/*********************************************************************/
import java.util.*;
import javax.swing.*;
import com.ibm.sdwb.build390.*;
import com.ibm.sdwb.build390.mainframe.*;
import com.ibm.sdwb.build390.library.userinterface.*;


public abstract class UserBuildSource extends SourceSelection {
    public abstract Map getMetadataMap();
}
