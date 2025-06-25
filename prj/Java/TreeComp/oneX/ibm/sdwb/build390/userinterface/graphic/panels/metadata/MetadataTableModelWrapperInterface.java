package com.ibm.sdwb.build390.userinterface.graphic.panels.metadata;
/*********************************************************************/
/* CustomTableDisplayHandler     class for the Build/390 client      */
/* The table model should implement this interface to make use of the*/
/* additional features as shown below.                               */
/* Has additional helper methods like                                */
/* 1.getValueAt a particular row.                                    */
/* 2.update table display with a collection of custom info objects.  */
/*********************************************************************/
//02/11/2005 SDWB2397 Metadata Model After function (INTF2)
/*********************************************************************/

import javax.swing.table.*;
import java.util.Collection;

public interface MetadataTableModelWrapperInterface {

    public  Object getValueAt(int row);
    public  void updateDisplay(Collection displayData);
    public  AbstractTableModel getModel();

}
