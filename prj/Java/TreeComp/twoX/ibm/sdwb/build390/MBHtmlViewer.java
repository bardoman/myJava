package com.ibm.sdwb.build390;
/***************************************************************************/
/* Java MBBasicInternalFrame class for the Build/390 client                           */
/*  Builds a listbox, populates it and adds the action listeners specified */
// 04/27/99 errorHandling       change LogException parms & add new error types
// 03/07/2000 reworklog         changes to write the log stuff using listeners
/***************************************************************************/
import java.awt.*;
import java.awt.event.*;
import java.util.*;
import java.io.*;
import java.net.*;
import javax.swing.*;
import javax.swing.event.*;
import javax.swing.text.*;
import javax.swing.text.html.*;
import com.ibm.sdwb.build390.logprocess.*;
import com.ibm.sdwb.build390.userinterface.graphic.MainInterface;

public class MBHtmlViewer extends MBBasicInternalFrame implements HyperlinkListener {

    private JEditorPane edit;
    private boolean dirty = false;
    String referenceText = new String();
    String fn = new String();
    private MBHtmlViewer thisFrame = null;
    private JViewport htmlView = null;


    public MBHtmlViewer(LogEventProcessor lep) {
        super("Help", null, true, false, lep);
        this.lep=lep;
        thisFrame = this;
        edit = new JEditorPane();
        edit.addHyperlinkListener(this);
        edit.setEditable(false);
        JScrollPane editScroller = new JScrollPane(edit);
        htmlView = editScroller.getViewport();
        getContentPane().add("Center",editScroller);
        setVisible(true);
    }

    public void setPage(String urlString) {
        try {
            URL url = new URL(urlString);
            String anchor = url.getRef();
            String fileName = url.getFile().substring(1, url.getFile().length());
//            edit.setPage(url);
/*
//            System.out.println("type is " + edit.getContentType() + "  " + edit.getEditorKit());
            if ((new File(fileName)).exists()){
                try {
                    BufferedReader readFile = new BufferedReader(new FileReader(fileName));
                    String currentLine;
                    StringBuffer totalBuffer = new StringBuffer();
                    char[] charBuf = new char[4096];
                    int charRead = 0;
                    while ((charRead = readFile.read(charBuf)) > -1) {
                        totalBuffer.append(charBuf, 0, charRead);
                    }
//                    edit.setEditorKit(new HTMLEditorKit());
                    edit.setContentType("text/html");
                    edit.setText(totalBuffer.toString());
            System.out.println("type is " + edit.getContentType() + "  " + edit.getEditorKit());
                } catch (Exception e) {
                    edit.setText("Error loading "+fn+".\n"+e);
                }
            }
*/
            edit.setPage(url);
            setVisible(true);
        } catch (IOException ioe) {
            //MBUtilities.LogException("An error occurred loading the help page "+urlString,ioe);
            lep.LogException("An error occurred loading the help page "+urlString,ioe);
        }
    }

    /**
     * Notification of a change relative to a
     * hyperlink.
     */
    public void hyperlinkUpdate(HyperlinkEvent e) {
        if (e.getEventType() == HyperlinkEvent.EventType.ACTIVATED) {
            setPage(e.getURL().toString());
        }
    }

    // make it fit in the frame
    public Dimension getPreferredSize() {
        Rectangle prt = MainInterface.getInterfaceSingleton().getframe().getBounds();
        Dimension dim = super.getPreferredSize();
        if (dim.height > prt.height-50) dim.height = prt.height-50;
        if (dim.width  > prt.width)  dim.width  = prt.width;
        return dim;
    }
}
