package com.ibm.sdwb.build390;
/***************************************************************************/
/* Java MBViewClosingTextDialog class for the Build/390 client             */
/*  Builds a text field to show the Apar closing text,  the use may add it */
/*  in the ++apar file.                                                    */
// 05/21/99 feature          initial release
//Thulasi: 10/19/00: Removed the Action menu and the add button from the menu bar.
/***************************************************************************/
import java.awt.*;
import java.awt.event.*;
import java.util.*;
import java.io.*;
import java.net.*;
import javax.swing.*;
import javax.swing.event.*;
import javax.swing.text.*;
import com.ibm.sdwb.build390.userinterface.graphic.MainInterface;

public class MBViewClosingTextDialog extends MBBasicInternalFrame{

    private JTextArea view;
    private boolean dirty = false;
    String referenceText = new String();
    String fn = new String();
    private MBViewClosingTextDialog thisFrame = null;
    private boolean addTextFlag = false;

    public MBViewClosingTextDialog(final String tempFn) {
  		super("View - "+tempFn, null, true, true, null);
        fn = tempFn;
        thisFrame = this;
        view = new JTextArea();
        view.setEditable(false);
		initialize();
        referenceText = new String(view.getText());
        JMenuBar menuBar = new JMenuBar();
        setJMenuBar(menuBar);
        JMenu fileMenu = new JMenu("File");
		//Thulasi : 10/19/00: No need of Action menu
        //JMenu actionMenu = new JMenu("Action");
        menuBar.add(fileMenu);
        //menuBar.add(actionMenu);
        //JMenuItem addItem = new JMenuItem("Add");
        JMenuItem exitItem = new JMenuItem("Exit");
       // actionMenu.add(addItem);
        fileMenu.add(exitItem);

       /** addItem.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                addTextFlag = true;
                dispose();
            }
        });	 */
        exitItem.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                dispose();
            }
        });
        setVisible(true);
    }


    private void initialize() {
  		setMaximizable(true);
        view.setFont(new Font("Monospaced",Font.PLAIN, 12));
        if ((new File(fn)).exists()){
            try {
                BufferedReader readFile = new BufferedReader(new FileReader(fn));
                String currentLine;
                StringBuffer totalBuffer = new StringBuffer();
                char[] charBuf = new char[4096];
                int charRead = 0;
                while ((charRead = readFile.read(charBuf)) > -1) {
                    totalBuffer.append(charBuf, 0, charRead);
                }
           	    view.setText(totalBuffer.toString());
            } catch (Exception e) {
                view.setText("Error loading "+fn+".\n"+e);
            }
        }
		JScrollPane viewScroller = new JScrollPane(view);
		getContentPane().add("Center",viewScroller);
    }

    public boolean getAddTextFlag() {
        return addTextFlag;
    }


    public void dispose(){
        new Thread(new Runnable() {
            public void run() {
                thisFrame.superdispose();
            }
        }).start();
    }

    private void superdispose() {
        super.dispose();
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
