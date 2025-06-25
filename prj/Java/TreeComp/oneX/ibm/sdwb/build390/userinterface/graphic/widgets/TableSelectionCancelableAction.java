package com.ibm.sdwb.build390.userinterface.graphic.widgets;

/* this class overrides Action, so that the window you are working
in is disabled, and the cancel button enabled, everytime you perform an action,
until that action is completed.
*/

import javax.swing.JTable;
import javax.swing.table.TableModel;
import java.util.List;
import java.awt.event.ActionEvent;
import com.ibm.sdwb.build390.MBAnimationStatusWindow;

public abstract class TableSelectionCancelableAction extends CancelableAction{

	protected JTable theTable = null;

/* takes the window it's being associated with as an argument, so it can modify things
based on it's state of execution */
    public TableSelectionCancelableAction(String name, JTable tempTable){
		super(name);
		theTable = tempTable;
    }

	protected List getBuildsToHandle(String buildIdColumnName){
		List returnList = new java.util.ArrayList();
		TableModel theModel = theTable.getModel();
		int[] selectedRows = theTable.getSelectedRows();
		for (int i = 0; i < selectedRows.length; i++) {
// call is structured this way so even if the columns are reordered the proper value will be returned.
			String oneBuildId = (String) theModel.getValueAt(selectedRows[i], theTable.getColumn(buildIdColumnName).getModelIndex());
			returnList.add(oneBuildId);
		}
		return returnList;
	}
}
