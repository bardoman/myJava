package com.ibm.sdwb.build390;
/***************************************************************************/
/* Java MBBasicInternalFrame class for the Build/390 client                */
/*  Builds a listbox, populates it and adds the action listeners specified */
// 04/27/99 errorHandling       change LogException parms & add new error types
// 03/07/2000 reworklog         changes to implement the log stuff using listeners
/***************************************************************************/
import java.awt.*;
import java.awt.event.*;
import java.util.*;
import java.io.*;
import java.net.*;
import javax.swing.*;
import javax.swing.event.*;
import javax.swing.text.*;
import com.ibm.sdwb.build390.logprocess.*;
import com.ibm.sdwb.build390.userinterface.graphic.MainInterface;

public class MBEditPanel extends MBBasicInternalFrame {

    private JTextArea edit;
    private boolean dirty = false;
    String referenceText = new String();
    String fn = new String();
    private MBEditPanel thisFrame = null;

    public MBEditPanel(final String tempFn, boolean editable,LogEventProcessor lep) {
        super("Edit - "+tempFn, null, true, true, lep);
        fn = tempFn;
        thisFrame = this;
        edit = new JTextArea();
        edit.setEditable(editable);
        initialize();
        if (editable) {
            referenceText = new String(edit.getText());
            JMenuBar menuBar = new JMenuBar();
            setJMenuBar(menuBar);
            JMenu fileMenu = new JMenu("File");
            menuBar.add(fileMenu);
            JMenuItem saveItem = new JMenuItem("Save");
            JMenuItem exitItem = new JMenuItem("Exit");
            fileMenu.add(saveItem);
            fileMenu.add(exitItem);
            saveItem.addActionListener(new ActionListener() {
                                           public void actionPerformed(ActionEvent evt) {
                                               saveText();
                                           }
                                       });
            exitItem.addActionListener(new ActionListener() {
                                           public void actionPerformed(ActionEvent evt) {
                                               dispose();
                                           }
                                       });
        }
        setVisible(true);
    }

    public MBEditPanel(String tempFn,LogEventProcessor lep) {
        super("View - "+tempFn, null, true, true, lep);
        fn = tempFn;
        thisFrame = this;
        edit = new JTextArea();
        edit.setEditable(false);
        initialize();
        setVisible(true);
    }

    private void initialize() {
        setMaximizable(true);
        edit.setFont(new Font("Monospaced",Font.PLAIN, 12));
        if ((new File(fn)).exists() | !edit.isEditable()) {
            try {
                BufferedReader readFile = new BufferedReader(new FileReader(fn));
                String currentLine;
                StringBuffer totalBuffer = new StringBuffer();
                char[] charBuf = new char[4096];
                int charRead = 0;
                while ((charRead = readFile.read(charBuf)) > -1) {
                    totalBuffer.append(charBuf, 0, charRead);
                }
                edit.setText(totalBuffer.toString());
            } catch (Exception e) {
                edit.setText("Error loading "+fn+".\n"+e);
            }
        }
        JScrollPane editScroller = new JScrollPane(edit);
        getContentPane().add("Center",editScroller);
    }

    private void saveText() {
        try {
            BufferedWriter writeFile = new BufferedWriter(new FileWriter(fn));
            BufferedReader stringReader = new BufferedReader(new StringReader(edit.getText()));
            String currentLine = null;
            while ((currentLine = stringReader.readLine()) != null) {
                // 11/10, chris, replaced with newline constant for
                // ftp a file from AIX to MVS
                writeFile.write(currentLine+MBConstants.NEWLINE);
                //writeFile.newLine();
            }
            writeFile.close();
            referenceText = new String(edit.getText());
        } catch (Exception e) {
            lep.LogException("Error saving file " + fn, e);
        }
    }

    public void dispose(){
        new Thread(new Runnable() {
                       public void run() {
                           if (edit.isEditable() & !referenceText.equals(edit.getText())) {
                               MBMsgBox question = new MBMsgBox("Confirmation", "Editor text has changed.  Do you want to save the file?", thisFrame, true);
                               if (question.isAnswerYes()) {
                                   saveText();
                               }
                           }
                           thisFrame.superdispose();
                       }
                   }).start();
    }

    private void superdispose() {
        super.dispose();
    }

    // FixMinSize
    public Dimension getMinimumSize() {
        Dimension oldPref = new Dimension(100, 100);
        return oldPref;
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
