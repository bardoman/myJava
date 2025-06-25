package com.ibm.sdwb.build390;
/*********************************************************************/
// This java class provides a dialog to be used to allow the user to
// enter comments to be sent to development whne the zipped build is sent
// Changes
// Date     Defect/Feature      Reason
//04/24/2002 //#Def.INT0892:   Help does nothing in Zip panel
//12/03/2002 SDWB-2019 Enhance the help system
/*********************************************************************/

import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.util.*;
import javax.swing.*;
import com.ibm.sdwb.build390.help.*;

public class MBZipCommentsDialog extends MBModalStatusFrame implements MBSaveableFrame {

    private JInternalFrame pframe;
    private JPanel centerPanel = new JPanel();
    private JButton Listds;
    private Box vBox;
    private Vector data;
    JLabel HeadLabel;
    JLabel HeadLabel1;
    JButton bHelp;
    JButton bOK;
	private JTextField ntf = null;
	private JTextArea nta  = null;
    private String NOTESID = "NOTES_ID";
    private boolean statusOK = false;

    public MBZipCommentsDialog(String buildid, JInternalFrame pFrame) {
		// Create dialog
        super(buildid+": Enter Comments to be sent to development", pFrame, null);
        pframe=pFrame;
        MBInsetPanel centerPanel = new MBInsetPanel(new BorderLayout(), 5, 5, 5, 5);
        setForeground(MBGuiConstants.ColorRegularText);
        setBackground(MBGuiConstants.ColorGeneralBackground);
        // head label
        HeadLabel = new JLabel("Notes ID: ");
        HeadLabel.setFont(new Font("Dialog", Font.BOLD, 12));
        HeadLabel.setForeground(MBGuiConstants.ColorGroupHeading);
        HeadLabel1 = new JLabel("Comments:");
        HeadLabel1.setFont(new Font("Dialog", Font.BOLD, 12));
        HeadLabel1.setForeground(MBGuiConstants.ColorGroupHeading);
        // box to contain scrollable jlist
		ntf = new JTextField();
		// add comments area
        nta = new JTextArea(null, 10, 50);

		Box Box1 = Box.createHorizontalBox();
        Box1.add(HeadLabel);
        Box1.add(ntf);
        centerPanel.add("North", Box1);

		Box Box2 = Box.createVerticalBox();
        Box2.add(HeadLabel1);
        JScrollPane scrollPane = new JScrollPane(nta);
        Box2.add(scrollPane);
		centerPanel.add("Center", Box2);

		// set line wrap
        nta.setLineWrap(true);

		// init notes id field
		if (getGeneric(NOTESID) != null) {
			String nid = (String) getGeneric(NOTESID);
			if (nid != null) {
				ntf.setText(nid);
			}
		}

        // add buttons
        bHelp = new JButton("Help");
        bHelp.setForeground(MBGuiConstants.ColorHelpButton);

        //#Def.INT0892:
        bHelp.addActionListener(new ActionListener() {
                                   public void actionPerformed(ActionEvent evt) {
                                       //MBUtilities.ShowHelp("Restarting");
                                       MBUtilities.ShowHelp("HDRRESTART",HelpTopicID.ZIPCOMMENTSDIALOG_HELP);
                                   }
                               } );

        bOK = new JButton("OK");
        bOK.setForeground(MBGuiConstants.ColorActionButton);
        Vector actionButtons = new Vector();
        actionButtons.addElement(bOK);
        addButtonPanel(bHelp,actionButtons);
        getContentPane().add("Center", centerPanel);
        // OK button action
        bOK.addActionListener(new MBCancelableActionListener(thisFrame) {
            public void doAction(ActionEvent avt) {
            	statusOK = true;
            	save();
                dispose();
            }
        });

        setVisible(true);
        SymInternalFrame lSymInternalFrame = new SymInternalFrame();
        this.addInternalFrameListener(lSymInternalFrame);
    }

    public void postVisibleInitialization(){
		ntf.requestFocus();
		if (ntf.getText() != null) {
			nta.requestFocus();
		}
    }

	public boolean save() {
		putGeneric(NOTESID, ntf.getText());
		return true;
	}

	public boolean saveNeeded() {
		return true;
	}

	public void dispose() {
        dispose(false);
    }
	
	public boolean getOKStatus() {
		return statusOK;
	}

	// get the notes id entered
	public String getNotesID() {
		return ntf.getText();
	}

	// get the comments entered
	public String getComments() {
		return nta.getText();
	}

    // set minimum size
    public Dimension getMinimumSize() {
        //System.out.println(getSize());
        Dimension oldPref = new Dimension(330, 300);
        return oldPref;
    }

    class SymInternalFrame implements javax.swing.event.InternalFrameListener
    {
        public void internalFrameOpened(javax.swing.event.InternalFrameEvent event)
        {
        }

        public void internalFrameDeiconified(javax.swing.event.InternalFrameEvent event)
        {
            Object object = event.getSource();
            if (object == MBZipCommentsDialog.this)
                MBZipCommentsDialog_internalFrameDeiconified(event);
        }

        public void internalFrameIconified(javax.swing.event.InternalFrameEvent event)
        {
        }

        public void internalFrameDeactivated(javax.swing.event.InternalFrameEvent event)
        {
        }

        public void internalFrameClosed(javax.swing.event.InternalFrameEvent event)
        {
        }

        public void internalFrameActivated(javax.swing.event.InternalFrameEvent event)
        {
        }

        public void internalFrameClosing(javax.swing.event.InternalFrameEvent event)
        {
        }
    }

    void MBZipCommentsDialog_internalFrameDeiconified(javax.swing.event.InternalFrameEvent event)
    {
        // to do: code goes here.
    }
}
