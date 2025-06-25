package com.ibm.sdwb.build390.userinterface.graphic.utilities;

import com.ibm.sdwb.build390.MBAnimationStatusWindow;
import java.awt.*;
import javax.swing.*;

public class GeneralUtilities {

	private GeneralUtilities() {
		// make sure no one instatiates this class
	}

	public static MBAnimationStatusWindow getParentAnimationStatus(Component testComponent) {

		for (; testComponent != null; ) {
			if (testComponent instanceof MBAnimationStatusWindow) {
				return(MBAnimationStatusWindow) testComponent;
			}else if (testComponent instanceof javax.swing.JPopupMenu) {
				testComponent = ((javax.swing.JPopupMenu) testComponent).getInvoker(); // for menus, we go with the component they are invoked from
			}else {
				testComponent = testComponent.getParent(); // in general, we get the container component for the current component
			}
		}
		return null;
	}

	public static JInternalFrame getParentInternalFrame(Component testComponent) {

		for (; testComponent != null; ) {
			if (testComponent instanceof JInternalFrame) {
				return(JInternalFrame) testComponent;
			}else if (testComponent instanceof javax.swing.JPopupMenu) {
				testComponent = ((javax.swing.JPopupMenu) testComponent).getInvoker(); // for menus, we go with the component they are invoked from
			}else {
				testComponent = testComponent.getParent(); // in general, we get the container component for the current component
			}
		}
		return null;
	}


	/**
	 * Layout a grid with the width of each column determined
	 * by the longest member of the column.
	 * 
	 * @param parent
	 * @param rows    number of rows
	 * @param columns number of columns
	 * @param horizontalOffset
	 * @param verticalOffset
	 * @param horizontalPadding
	 * @param yPad
	 */
	public static void makeCompactGrid(Container parent, int rows, int columns, int horizontalOffset, int verticalOffset, int horizontalPadding, int verticalPadding) {
		SpringLayout layout = (SpringLayout)parent.getLayout();
		if (columns < 1 & rows < 1) {
			throw new RuntimeException("Rows or columns must be specified.");
		}
		if (rows < 1) {
			rows = computeRowsOrColumnsGivenColumnsOrRows(columns, parent.getComponentCount());
		}else if (columns < 1) {
			columns = computeRowsOrColumnsGivenColumnsOrRows(rows, parent.getComponentCount());
		}
		
		//Align all cells in each column and make them the same width.
		Spring xPosition = Spring.constant(horizontalOffset);
		for (int c = 0; c < columns; c++) {
			Spring width = Spring.constant(0);
			for (int r = 0; r < rows; r++) {
				SpringLayout.Constraints constraints = getConstraintsForCell(r, c, parent, columns);
				if (constraints !=null) {
					width = Spring.max(width,constraints.getWidth());
				}
			}
			for (int r = 0; r < rows; r++) {
				SpringLayout.Constraints constraints = getConstraintsForCell(r, c, parent, columns);
				if (constraints!=null) {
					constraints.setX(xPosition);
					constraints.setWidth(width);
				}
			}
			xPosition = Spring.sum(xPosition, Spring.sum(width, Spring.constant(horizontalPadding)));
		}

		//Align all cells in each row and make them the same height.
		Spring yPosition = Spring.constant(verticalOffset);
		for (int r = 0; r < rows; r++) {
			Spring height = Spring.constant(0);
			for (int c = 0; c < columns; c++) {
				SpringLayout.Constraints constraints = getConstraintsForCell(r, c, parent, columns);
				if (constraints !=null) {
					height = Spring.max(height,constraints.getHeight());
				}
			}
			for (int c = 0; c < columns; c++) {
				SpringLayout.Constraints constraints = getConstraintsForCell(r, c, parent, columns);
				if (constraints!=null) {
					constraints.setY(yPosition);
					constraints.setHeight(height);
				}
			}
			yPosition = Spring.sum(yPosition, Spring.sum(height, Spring.constant(verticalPadding)));
		}

		//Set the parent's size.
		SpringLayout.Constraints pCons = layout.getConstraints(parent);
		pCons.setConstraint(SpringLayout.SOUTH, yPosition);
		pCons.setConstraint(SpringLayout.EAST, xPosition);
	}

	private static SpringLayout.Constraints getConstraintsForCell(int row, int column,Container parent,int totalNumberOfColumns) {
		SpringLayout layout = (SpringLayout) parent.getLayout();
		int componentIndex = row * totalNumberOfColumns + column;
		if (componentIndex < parent.getComponentCount()) {
			Component comp = parent.getComponent(componentIndex);
			return layout.getConstraints(comp);
		}else {
			return null;
		}
	}

	private static int computeRowsOrColumnsGivenColumnsOrRows(int otherParameter, int totalComponents){
		int returnValue = totalComponents/otherParameter;
		if (totalComponents % otherParameter > 0) {
			returnValue +=1;
		}
		return returnValue;
	}
}
