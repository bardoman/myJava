package com.ibm.sdwb.build390;
/*
	A basic extension of the java.awt.Dialog class
 */

import java.util.*;
import javax.swing.*;
import java.awt.event.*;

public class MBButtonPanel extends MBInsetPanel{

	public MBButtonPanel(JButton help, JButton cancel, List actionButtons){

	    super(5, 5, 5, 5);
	    setLayout(new BoxLayout(this, BoxLayout.X_AXIS));
        add(Box.createHorizontalGlue());
		for (Iterator buttonIterator = actionButtons.iterator(); buttonIterator.hasNext();) {
		    add((JButton) buttonIterator.next());
            add(Box.createHorizontalGlue());
		}
		if (cancel != null) {
    		add(cancel);
	        add(Box.createHorizontalGlue());
    	}
        if (help != null) {
	    	add(help);
            add(Box.createHorizontalGlue());
		}
   	}
}
