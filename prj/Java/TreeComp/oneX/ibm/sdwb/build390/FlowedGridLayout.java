package com.ibm.sdwb.build390;
/*
This class is a layout which dynamically places components so that they fit as densly as possible 
on the screen.  It is meant to be used with a scrollpane that is only scrollable in one direction.
So, if you have a panel that scrolls downward, like this:
| *  *	*  * |
| *  *	*  * |
| *  *	*  * |
and resize it smaller, it will then look like this:
| *  *	* |
| *  *	* |
| *  *	* |
| *  *	* |
*/

import java.awt.*;
import java.util.*;


public class FlowedGridLayout implements LayoutManager{
	private Vector compVector = new Vector();
	public static int GROWVERTICAL = 0;
	public static int GROWHORIZONTAL = 1;
	private int alignment = GROWVERTICAL;
	private boolean debug = false;
	private int maxCompPrefWidth = 1;
	private int maxCompPrefHeight = 1;
	private int maxCompMinWidth = 1;
	private int maxCompMinHeight = 1;
	private int lastNumberOfRows = 1;
	private int lastNumberOfColumns = 1;
	private boolean recalMin = true;
	private boolean recalPref = true;
	private int vGap = 5;
	private int hGap = 10;

	public FlowedGridLayout() {

	}

	public FlowedGridLayout(boolean tempD) {
		debug = tempD;

	}

/*
	add a component, remember to recalculate the minimum and preferred sizes.
*/	
	public void add(Component comp) {
		if (!compVector.contains(comp)) {
			compVector.addElement(comp);
			recalMin = true;
			recalPref = true;
		}
	}

/*
	remove a component, remember to recalculate the minimum and preferred sizes.
*/	
	public boolean  remove(Component comp) {
		boolean wasThere =  compVector.removeElement(comp);
		if(wasThere){
			recalMin = true;
			recalPref = true;
		}
		return wasThere;
	}

/*
	add a component
*/	
	public void addLayoutComponent(String position, Component component) {
		if (!compVector.contains(component)) {
			compVector.addElement(component);
		}
	}

/*
	remove a component
*/	
	public void removeLayoutComponent(Component component){
		compVector.removeElement(component);
	}

/*
	get the mimimum size.  for this layout, make it the same as the preferred size.  
*/	
	public Dimension minimumLayoutSize(Container container) {
		if (debug ) {
			System.out.println ("This is min");
		}
		return preferredLayoutSize(container);
	}

/*
	get the preferred size.  
*/	
	public Dimension preferredLayoutSize(Container container) {
// figure out how many rows and columns we should have 
		Dimension numRowsColumns = rowsColumns(container);
		Insets insets = container.getInsets();
// calculate the preferred hieght and width, using the maximum height and width for any one component, and the insets.		
		int prefHeight = numRowsColumns.height * maxCompPrefHeight + (numRowsColumns.height - 1) * getVgap() + insets.top + insets.bottom;
		int prefWidth = numRowsColumns.width * maxCompPrefWidth + (numRowsColumns.width -1) * getHgap() + insets.left + insets.right;
		if (debug ) {
			System.out.println ( "prefH " + prefHeight + " rowCol " +numRowsColumns.height +" comp prefH " +  maxCompPrefHeight);
		}
		return new Dimension(prefWidth, prefHeight);
	}			  

/*
	figure out the rows and columns of the panel
*/	
	private Dimension rowsColumns(Container container) {
// if necessary, recalculate the preferred height and width.
		if(recalPref) {
			recalPref = false;
			maxCompPrefHeight = 0;
			maxCompPrefWidth = 0;
			for (int i = 0; i < compVector.size();i++) {
				Component comp = (Component) compVector.elementAt(i);
				Dimension dim = comp.getPreferredSize();
				if (dim.height > maxCompPrefHeight) {
					maxCompPrefHeight = dim.height;
				}
				if (dim.width > maxCompPrefWidth) {
					maxCompPrefWidth = dim.width;
				}
			}
			if (maxCompPrefWidth == 0){
				maxCompPrefWidth = 1;
			}
			if (maxCompPrefHeight == 0) {
				maxCompPrefHeight = 1;
			}
		}
		Insets insets = container.getInsets();
		int numberOfRows = 0;
		int numberOfColumns = 0;
// if we want to set the height, and scroll horizontally
		if (alignment == GROWHORIZONTAL) {
// compute the height, and then figure out how many rows fit into that.
			int availableHeight = container.getSize().height - insets.top - insets.bottom;
			numberOfRows = availableHeight / (maxCompPrefHeight + vGap);
			if (numberOfRows == 0) {
				numberOfRows = 1;
			}
// now, figure out how many columns we'll need with that many row.
			numberOfColumns = compVector.size() / numberOfRows;
			if (numberOfColumns * numberOfRows < compVector.size()) {
				numberOfColumns++;
			}
// if we want to set the width, and scroll vertically
		} else if (alignment == GROWVERTICAL) {
// compute the width, and then figure out how many columns fit into that.
			int availableWidth = container.getSize().width - insets.left - insets.right;
			numberOfColumns = availableWidth / (maxCompPrefWidth + hGap);
			if (numberOfColumns == 0) {
				numberOfColumns = 1;
			}
// now, figure out how many rows we'll need with that many columns
			numberOfRows = compVector.size() / numberOfColumns;
			if (numberOfRows * numberOfColumns < compVector.size()) {
				numberOfRows++;
			}
		}
// make sure the rows and columns are at least 1
		if (numberOfRows < 1) {
			numberOfRows = 1;
		}
		if (numberOfColumns < 1) {
			numberOfColumns = 1;
		}
		return new Dimension(numberOfColumns, numberOfRows);
	}

	public void layoutContainer(Container container) {
		Dimension numRowsCols = rowsColumns(container);
// figure out all of our heights, widths, column numbers, and whatnot
		int numberOfColumns = numRowsCols.width;
		int numberOfRows = numRowsCols.height;
		Insets insets = container.getInsets();
		int availableHeight = container.getSize().height - insets.top - insets.bottom;
		int availableWidth = container.getSize().width - insets.left - insets.right;
        int compWidth = availableWidth/numRowsCols.width - hGap;
		int compHeight = availableHeight/numRowsCols.height - vGap;
		if (alignment == GROWHORIZONTAL & compWidth < maxCompPrefWidth) {
			compWidth = maxCompPrefWidth;
		} else if (alignment == GROWVERTICAL & compHeight < maxCompPrefHeight) {
			compHeight = maxCompPrefHeight;
		}
// layout the components in a flowing grid based on these values.
		for (int y = 0; y < numberOfRows; y++) {
			int currYVal = insets.top+(compHeight+vGap)*y;
			for (int x = 0; x < numberOfColumns; x++) {
				int currXVal = insets.left + (compWidth+hGap)*x;
				if (y * numberOfColumns + x < compVector.size()) {
					Component comp = (Component) compVector.elementAt(y * numberOfColumns + x);
					comp.setBounds(currXVal, currYVal, compWidth, compHeight);
				}
			}
		}
	}

	public int getVgap() {
		return vGap;
	}
	public int getHgap() {
		return hGap;
	}
}
