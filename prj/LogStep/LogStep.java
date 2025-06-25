
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import java.io.*;
import java.util.*;
import javax.swing.text.*;

public class LogStep extends JFrame {
    JPanel panel1;
    JPanel panel2;
    JMenuBar menuMain;
    JMenuItem menuitemOpen;
    JMenuItem menuitemRefresh;
    JMenuItem menuitemExit;
    JMenuItem menuitemAbout;
    JTextArea HighTextArea = new JTextArea();
    JTextArea LowTextArea = new JTextArea();;
    Vector TransFileList = new Vector();
    LineNumberReader transReader;
    JButton back = new JButton("Back");
    JButton fwd = new JButton("Fwd");
    JButton first = new JButton("First");
    JButton last = new JButton("Last");

    JLabel SourceFileLabel = new JLabel("Source File:");
    JTextField SourceFileName = new JTextField();
    JLabel TransFileLabel = new JLabel("Transaction File:");
    JTextField TransFileName = new JTextField();

    int transIndex = 0;

    Object highLight = null;

    JScrollPane HighScrollPane;
    JScrollPane lowScrollPane;
    String inputFilePath;

    JTextField relativePathTextField = new JTextField("");
    String parentPath=".";
    static String STORAGE_PATH = "LogStepStorage.ser";

    public LogStep(String title)
    {
        getStore();

        // Frame
        setTitle(title);
        setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        getContentPane().setLayout(new BorderLayout(0,0));
        setSize(500,300);
        setVisible(false);
        // Menu
        menuMain = new JMenuBar();
        setJMenuBar(menuMain);
        menuAddItems(menuMain);

        // Window listener
        EvtListener elWindow = new EvtListener();
        this.addWindowListener(elWindow);
        // Menu and toolbar listener
        ActListener alMenuToolbar = new ActListener();
        menuitemOpen.addActionListener(alMenuToolbar);
        menuitemRefresh.addActionListener(alMenuToolbar);
        menuitemExit.addActionListener(alMenuToolbar);
        menuitemAbout.addActionListener(alMenuToolbar);
//
        back.addActionListener(new ActListener());

        fwd.addActionListener(new ActListener());

        first.addActionListener(new ActListener());

        last.addActionListener(new ActListener());
//
        Box pathBox = new Box(BoxLayout.X_AXIS);

        JLabel relativePathLabel = new JLabel("Relative Path: ");

        pathBox.add(relativePathLabel);

        pathBox.add(relativePathTextField);

        relativePathTextField.setFont(new Font("Arial", Font.BOLD, 12));

        setComponentSize(relativePathTextField ,700,25);
//
        Box topBox = new Box(BoxLayout.X_AXIS);

        topBox.add(first);

        topBox.add(back);

        topBox.add(fwd);

        topBox.add(last);
//
        Box highTextBox = new Box(BoxLayout.Y_AXIS);

        Box highTextLabelBox = new Box(BoxLayout.X_AXIS);

        highTextLabelBox.add(SourceFileLabel);

        setComponentSize(SourceFileLabel ,100,25);

        SourceFileName.setFont(new Font("Arial", Font.BOLD, 12));

        highTextLabelBox.add(SourceFileName);

        setComponentSize(SourceFileName ,700,25);

        highTextLabelBox.add(Box.createVerticalStrut(25));

        HighTextArea.setFont(new Font("Arial", Font.BOLD, 12)); 

        HighScrollPane = new JScrollPane(HighTextArea);

        highTextBox.add(highTextLabelBox);

        highTextBox.add(HighScrollPane);
//
        Box lowTextBox = new Box(BoxLayout.Y_AXIS);

        Box lowTextLabelBox = new Box(BoxLayout.X_AXIS);

        lowTextLabelBox.add(TransFileLabel);

        setComponentSize(TransFileLabel ,100,25);

        TransFileName.setFont(new Font("Arial", Font.BOLD, 12));

        lowTextLabelBox.add(TransFileName);

        setComponentSize(TransFileName ,700,25);

        lowTextLabelBox.add(Box.createVerticalStrut(25));

        LowTextArea.setFont(new Font("Arial", Font.BOLD, 12)); 

        lowScrollPane = new JScrollPane(LowTextArea);

        lowTextBox.add(lowTextLabelBox);

        lowTextBox.add(lowScrollPane);
//
        JSplitPane splitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT,
                                              highTextBox, lowTextBox);

        splitPane.setOneTouchExpandable(true);
        splitPane.setDividerLocation(150);

        //Provide minimum sizes for the two components in the split pane
        Dimension minimumSize = new Dimension(100, 50);
        HighScrollPane.setMinimumSize(minimumSize);
        lowScrollPane.setMinimumSize(minimumSize);

        //Provide a preferred size for the split pane
        splitPane.setPreferredSize(new Dimension(400, 200));

        Box lowBox = new Box(BoxLayout.X_AXIS);

        lowBox.add(splitPane);

        //Hook to panel
        Box mainBox = new Box(BoxLayout.Y_AXIS);

        mainBox.add(pathBox);

        mainBox.add(topBox);

        mainBox.add(lowBox);

        getContentPane().add(mainBox);

        readInputFile();

        firstAction();
    }

    public LogStep()
    {
        this("LogStep");
    }

    protected void menuAddItems(JMenuBar hmenu)
    {
        JMenu menu;
        JMenuItem item;
        // "File" menu
        menu = new JMenu("File");
        menu.setActionCommand("File");
        menu.setBorderPainted(false);
        menu.setMnemonic((int)'F');
        hmenu.add(menu);
        // "Open" item
        menuitemOpen = new JMenuItem("Open...");
        menuitemOpen.setHorizontalTextPosition(SwingConstants.RIGHT);
        menuitemOpen.setActionCommand("Open...");
        menuitemOpen.setBorderPainted(false);
        menuitemOpen.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_O, Event.CTRL_MASK));
        menuitemOpen.setMnemonic((int)'O');
        menu.add(menuitemOpen);


        // "Refresh" item
        menuitemRefresh = new JMenuItem("Refresh File...");
        menuitemRefresh.setHorizontalTextPosition(SwingConstants.RIGHT);
        menuitemRefresh.setActionCommand("Refresh File...");
        menuitemRefresh.setBorderPainted(false);
        menuitemRefresh.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_O, Event.CTRL_MASK));
        menuitemRefresh.setMnemonic((int)'R');
        menu.add(menuitemRefresh);

        // Separator
        menu.add(new JSeparator());
        // "Exit" item
        menuitemExit = new JMenuItem("Exit");
        menuitemExit.setActionCommand("Exit");
        menuitemExit.setBorderPainted(false);
        menuitemExit.setMnemonic((int)'X');
        menu.add(menuitemExit);
        // "Help" menu
        menu = new JMenu("Help");
        menu.setActionCommand("Help");
        menu.setBorderPainted(false);
        menu.setMnemonic((int)'H');
        hmenu.add(menu);
        // "About" item
        menuitemAbout = new JMenuItem("About...");
        menuitemAbout.setHorizontalTextPosition(SwingConstants.RIGHT);
        menuitemAbout.setActionCommand("About...");
        menuitemAbout.setBorderPainted(false);
        menuitemAbout.setMnemonic((int)'A');
        menu.add(menuitemAbout);
    }


    // Used by addNotify
    boolean addNotify_done=false;

    /**
     * Makes this Container displayable by connecting it to
     * a native screen resource.  Making a container displayable will
     * cause any of its children to be made displayable.
     * This method is called internally by the toolkit and should
     * not be called directly by programs.
     * <p>
     * Overridden here to adjust the size of the frame if needed.
     * </p>
     * @see java.awt.Component#isDisplayable
     * @see java.awt.Container#removeNotify
     */
    public void addNotify()
    {
        Dimension d=getSize();

        super.addNotify();

        if (addNotify_done) return;

        // Adjust size according to the insets so that entire component
        // areas are renderable.
        int menubarheight=0;
        JMenuBar menubar = getRootPane().getJMenuBar();
        if (menubar!=null) {
            menubarheight = menubar.getPreferredSize().height;
        }
        Insets insets=getInsets();
        setSize(insets.left + insets.right + d.width, insets.top + insets.bottom + d.height + menubarheight);
        addNotify_done=true;
    }

    protected void AboutApplication()
    {
        try {
            JOptionPane.showMessageDialog(this,
                                          "Steps through log and views transaction files",
                                          "About" ,
                                          JOptionPane.INFORMATION_MESSAGE);
        } catch (Exception e) {
        }
    }

    protected void ExitApplication()
    {

        putStore();
        // Hide the frame
        this.setVisible(false);
        // Free system resources
        this.dispose();
        // Exit the application
        System.exit(0);
    }

    class EvtListener extends WindowAdapter {
        public void windowClosing(WindowEvent event)
        {
            Object object = event.getSource();
            if (object==LogStep.this) {
                LogStep_windowClosing(event);
            }
        }
    }

    void LogStep_windowClosing(WindowEvent event)
    {
        try {
            this.ExitApplication();
        } catch (Exception e) {
        }
    }

    void UpdateTextAreas()
    {
        try {
            HiLiteText();

            String str = null;

            String fileName = ((Trans)TransFileList.get(transIndex)).name.toUpperCase();

            File  tmpFil= null;

            String relativePath = relativePathTextField.getText();

            if (relativePath.length() > 0) {
                int index = fileName.indexOf(relativePath);

                if (index != -1) {
                    fileName = fileName.substring(relativePath.length());
                }

                tmpFil = new File(fileName);
            } else {
                tmpFil = new File(fileName);
            }

            TransFileName.setText(fileName);

            int index = parentPath.lastIndexOf("\\");

            String endOfParentPath = parentPath.substring(index+1).toUpperCase();

            if (!tmpFil.exists()) {
                if ((index = fileName.indexOf(endOfParentPath))!=-1) {
                    fileName = fileName.substring(index+endOfParentPath.length());

                    fileName = parentPath+fileName;

                    tmpFil = new File(fileName);

                    if (!tmpFil.exists()) {
                        LowTextArea.setText("*** THIS FILE DOES NOT EXIST ***");

                        return;
                    } else {
                        TransFileName.setText(fileName);
                    }
                } else {
                    LowTextArea.setText("*** THIS FILE DOES NOT EXIST ***");

                    return;
                }
            }


            LineNumberReader transReader = new LineNumberReader(new FileReader(tmpFil));

            LowTextArea.setText("");

            int lineIndex = 0;

            while ((str = transReader.readLine())!=null) {
                LowTextArea.append(lineIndex+++": "+str+"\n");  
            }

            transReader.close();

            LowTextArea.setCaretPosition(0);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    void HiLiteText()
    {
        try {
            int line = ((Trans)TransFileList.get(transIndex)).line;

            Highlighter highLighter = HighTextArea.getHighlighter();

            int endOfs = HighTextArea.getLineEndOffset(line);

            int startOfs = HighTextArea.getLineStartOffset(line);

            if (highLight!=null) {
                highLighter.removeHighlight(highLight);
            }

            highLight = highLighter.addHighlight(startOfs,endOfs, new DefaultHighlighter.DefaultHighlightPainter(Color.pink));

            scrollTextView(line);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    void scrollTextView(int line)
    {
        JViewport port = HighScrollPane.getViewport();

        Font font = HighTextArea.getFont();

        FontMetrics fontMetrics = HighTextArea.getFontMetrics(font);

        int height = fontMetrics.getHeight();

        port.setViewPosition(new Point(0,line * height));
    }

    void backAction()
    {
        transIndex--;

        if (transIndex<0) {
            transIndex=0;
        }

        UpdateTextAreas();
    }

    void fwdAction()
    {
        transIndex++;

        if (transIndex > TransFileList.size()-1) {
            transIndex = TransFileList.size()-1;
        }

        UpdateTextAreas();
    }

    void firstAction()
    {
        transIndex=0;

        UpdateTextAreas();
    }

    void lastAction()
    {
        transIndex = TransFileList.size()-1;

        UpdateTextAreas();
    }

    class ActListener implements ActionListener {
        public void actionPerformed(ActionEvent event)
        {
            Object object = event.getSource();
            if (object==menuitemOpen) {
                menuitemOpen_Action(event);
            } else if (object==menuitemRefresh) {
                menuitemRefresh_Action(event);
            } else if (object==menuitemExit) {
                menuitemExit_Action(event);
            } else if (object==menuitemAbout) {
                menuitemAbout_Action(event);
            }
            if (object==back) {
                backAction();
            } else if (object==fwd) {
                fwdAction();
            } else if (object==first) {
                firstAction();
            } else if (object==last) {
                lastAction();
            }
        }
    }

    void readInputFile()
    {
        try {
            LineNumberReader rd = new LineNumberReader(new FileReader(new File(inputFilePath)));

            String str=null;
            String ret=null;
            int line = 0;

            HighTextArea.setText("");

            while ((str = rd.readLine())!=null) {
                HighTextArea.append(line+": "+str+"\n");  

                parseLine(str,line++);
            }

            rd.close();

            new Thread()
            {
                java.util.Timer timer = new java.util.Timer();
                long delay = 100;//msecs

                public void run()
                {
                    timer.schedule
                    (new TimerTask()
                     {
                         public void run()
                         {
                             firstAction();
                         }
                     },delay);
                }
            }.start();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    void menuitemOpen_Action(ActionEvent event)
    {
        try {
            TransFileList= new Vector();

            JFileChooser fileDialog = new JFileChooser(new File(parentPath));

            fileDialog.showOpenDialog(menuMain);

            File inputFile = fileDialog.getSelectedFile();

            parentPath = inputFile.getParent();

            inputFilePath = inputFile.getAbsolutePath();

            SourceFileName.setText(inputFilePath);

            readInputFile();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    void menuitemRefresh_Action(ActionEvent event)
    {
        try {
            readInputFile();
        } catch (Exception e) {
        }
    }

    void menuitemExit_Action(ActionEvent event)
    {
        try {
            this.ExitApplication();
        } catch (Exception e) {
        }
    }

    void menuitemAbout_Action(ActionEvent event)
    {
        try {
            this.AboutApplication();
        } catch (Exception e) {
        }
    }

    void parseLine(String str,int line)
    {
        String temp=null;
        String outFileName = null;
        String prtFileName = null;
        int lineLength = str.length();

        StringTokenizer strTok;

        String targ = "Output Files     = ";

        if (str.startsWith(targ)) {
            str = str.substring(targ.length());

            strTok = new StringTokenizer(str);

            outFileName   = strTok.nextToken();

            strTok.nextToken();                  

            prtFileName = strTok.nextToken();

            TransFileList.add(new Trans(prtFileName,line));

            TransFileList.add(new Trans(outFileName,line));
        }

        targ = "MBFtp put ";

        int indexLoc;

        if ((indexLoc = str.indexOf(targ))!=-1) {
            /*
            if(str.indexOf("extract")!=-1)
            {
                return;
            }*/



            str = str.substring(indexLoc+targ.length());

            int subIndex = str.indexOf("=");
            if (subIndex!=-1) {
                str = str.substring(subIndex+1);

            }

            strTok = new StringTokenizer(str,",");

            temp = strTok.nextToken();

            TransFileList.add(new Trans(temp,line));
        }
    }

    void setComponentSize(JComponent component, int width, int height)
    {
        component.setMinimumSize(new Dimension(width, height)); 
        component.setPreferredSize(new Dimension(width, height)); 
        component.setMaximumSize(new Dimension(width, height)); 
    }


    void putStore()
    {
        try {
            ObjectOutputStream out = new ObjectOutputStream(new  FileOutputStream(STORAGE_PATH)); 

            if (parentPath == null) {
                out.writeObject(".");
            } else {
                out.writeObject(parentPath);
            }

            if (inputFilePath == null) {
                out.writeObject(".");
            } else {
                out.writeObject(inputFilePath);
            }

            if (relativePathTextField.getText() == null) {
                out.writeObject(".");
            } else {
                out.writeObject(relativePathTextField.getText());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    void getStore()
    {
        try {
            File file = new File(STORAGE_PATH);

            if (file.exists()) {
                ObjectInputStream in = new ObjectInputStream(new  FileInputStream(STORAGE_PATH)); 

                parentPath = (String) in.readObject();
                if (parentPath == null) {
                    parentPath = ".";
                }

                inputFilePath = (String) in.readObject();
                if (inputFilePath == null) {
                    inputFilePath = ".";
                }

                String relativePath = (String) in.readObject();
                if (relativePath == null) {
                    relativePathTextField.setText("");
                } else {
                    relativePathTextField.setText(relativePath);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    class Trans {
        public String name;
        public int line;

        public Trans(String name,int line)
        {
            this.name = name;
            this.line = line;
        }
    }

    static public void main(String args[])
    {
        (new LogStep()).setVisible(true);
    }

}

