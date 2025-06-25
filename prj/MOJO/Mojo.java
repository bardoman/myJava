import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.tree.*;
import javax.swing.event.*;
import java.io.*;
import java.net.*;

public class Mojo extends JFrame implements TreeSelectionListener
{
    JPanel panel1;
    JPanel panel2;
    // Main menubar
    JMenuBar menuMain;
    // "File" menu
    JMenuItem menuitemNew;
    JMenuItem menuitemOpen;
    JMenuItem menuitemSave;
    JMenuItem menuitemSaveAs;
    JMenuItem menuitemExit;
    // "Edit" menu
    JMenuItem menuitemCut;
    JMenuItem menuitemCopy;
    JMenuItem menuitemPaste;
    JMenuItem menuitemPasteAddBranch;

    // "Help" menu
    JMenuItem menuitemAbout;
    // Main toolbar
    JToolBar tbMain;
    JButton buttonNew;
    JButton buttonOpen;
    JButton buttonSave;
    JButton buttonCut;
    JButton buttonCopy;
    JButton buttonPaste;
    JButton buttonAbout;
    JButton buttonAddBranch;

    // Open and Save file dialog
    JFileChooser fileDialog;
    // Images
    ImageIcon iconNew;
    ImageIcon iconOpen;
    ImageIcon iconSave;
    ImageIcon iconCut;
    ImageIcon iconCopy;
    ImageIcon iconPaste;
    ImageIcon iconAbout;
    ImageIcon iconAddBranch;

    private JTree tree;
    private JEditorPane htmlPane;
    private static String lineStyle = "Horizontal";

    //Optionally set the look and feel.
    private static boolean useSystemLookAndFeel = false;

    DynamicTree treePanel = new DynamicTree();
    private int newNodeSuffix = 1;
    private static String ADD_COMMAND = "add";
    private static String REMOVE_COMMAND = "remove";
    private static String CLEAR_COMMAND = "clear";

    public Mojo()
    {

        // Initialize
        //
        // Common images for menubar and toolbar
        LoadCommonImages();
        // Frame
        setTitle("MUCHO MOJO MAGI");
        setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        getContentPane().setLayout(new BorderLayout(0,0));
        setSize(500,300);
        setVisible(false);
        // Menu
        menuMain = new JMenuBar();
        setJMenuBar(menuMain);
        menuAddItems(menuMain);
        // Panels

        //panel2 = new JPanel(new FlowLayout(FlowLayout.LEFT,0,0));

        panel2 = new JPanel();

        panel2.setLayout(new BoxLayout(panel2,BoxLayout.Y_AXIS));

        getContentPane().add(BorderLayout.NORTH, panel2);
        panel2.setBounds(0,0,475,30);

        // Toolbar
        tbMain = new JToolBar();
        panel2.add(tbMain);

        tbMain.setAlignmentX(panel2.LEFT_ALIGNMENT);

        tbMain.setBounds(0,0,375,30);
        tbAddButtons(tbMain);

        DefaultMutableTreeNode top =
        new DefaultMutableTreeNode("The Java Series");
        createNodes(top);

        populateTree(treePanel);//***BE


        //Create a tree that allows one selection at a time.
        // tree = new JTree(top);

        //tree.setEditable(true);

        // tree.getSelectionModel().setSelectionMode
        // (TreeSelectionModel.SINGLE_TREE_SELECTION);

        //Listen for when the selection changes.
        // tree.addTreeSelectionListener(this);


        JScrollPane leftScrollPane = new JScrollPane(treePanel,JScrollPane.VERTICAL_SCROLLBAR_ALWAYS,
                                                     JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS);

        // JScrollPane leftScrollPane = new JScrollPane(tree,JScrollPane.VERTICAL_SCROLLBAR_ALWAYS,
        //                                              JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS);

        htmlPane = new JEditorPane();

        htmlPane.setEditable(false);

        JScrollPane rightScrollPane = new JScrollPane(htmlPane,JScrollPane.VERTICAL_SCROLLBAR_ALWAYS,
                                                      JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS);

        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, leftScrollPane, rightScrollPane );

        splitPane.setOneTouchExpandable(true);

        splitPane.setDividerLocation(250);

        splitPane.setPreferredSize(new Dimension(500, 500));

        splitPane.setDividerSize(10);

        panel2.add(splitPane);

        // Open/save file dialog
        fileDialog = new JFileChooser();

        // Register listeners
        //
        // Window listener
        EvtListener elWindow = new EvtListener();
        this.addWindowListener(elWindow);
        // Menu and toolbar listener
        ActListener alMenuToolbar = new ActListener();
        menuitemOpen.addActionListener(alMenuToolbar);
        menuitemSave.addActionListener(alMenuToolbar);
        menuitemExit.addActionListener(alMenuToolbar);
        menuitemAbout.addActionListener(alMenuToolbar);
        buttonOpen.addActionListener(alMenuToolbar);
        buttonSave.addActionListener(alMenuToolbar);
        buttonAbout.addActionListener(alMenuToolbar);
    }

    /**
     * Create a new instance of Mojo with default title.
     * @see #Mojo(String title)
     */

    protected void LoadCommonImages()
    {
        iconNew   = new ImageIcon("images/new.gif");
        iconOpen  = new ImageIcon("images/open.gif");
        iconSave  = new ImageIcon("images/save.gif");
        iconCut   = new ImageIcon("images/cut.gif");
        iconCopy  = new ImageIcon("images/copy.gif");
        iconPaste = new ImageIcon("images/paste.gif");
        iconAbout = new ImageIcon("images/about.gif"); 
        iconAddBranch   = new ImageIcon("images/add.gif");
    }

    protected void tbAddButtons(JToolBar toolbar)
    {
        Rectangle bounds = new Rectangle();
        // Bounds for each button
        bounds.x=2;
        bounds.y=2;
        bounds.width=25;
        bounds.height=25;
        // Toolbar separator
        Dimension separator = new Dimension(5,5);
        // Button size
        Dimension buttonsize = new Dimension(bounds.width,bounds.height);

        // New
        buttonNew = new JButton(iconNew);
        buttonNew.setDefaultCapable(false);
        buttonNew.setToolTipText("Create a new document");
        buttonNew.setMnemonic((int)'N');
        toolbar.add(buttonNew);
        buttonNew.setBounds(bounds);
        buttonNew.setMinimumSize(buttonsize);
        buttonNew.setMaximumSize(buttonsize);
        buttonNew.setPreferredSize(buttonsize);
        // Open
        buttonOpen = new JButton(iconOpen);
        buttonOpen.setDefaultCapable(false);
        buttonOpen.setToolTipText("Open an existing document");
        buttonOpen.setMnemonic((int)'O');
        toolbar.add(buttonOpen);
        bounds.x += bounds.width;
        buttonOpen.setBounds(bounds);
        buttonOpen.setMinimumSize(buttonsize);
        buttonOpen.setMaximumSize(buttonsize);
        buttonOpen.setPreferredSize(buttonsize);
        // Save
        buttonSave = new JButton(iconSave);
        buttonSave.setDefaultCapable(false);
        buttonSave.setToolTipText("Save the active document");
        buttonSave.setMnemonic((int)'S');
        toolbar.add(buttonSave);
        bounds.x += bounds.width;
        buttonSave.setBounds(bounds);
        buttonSave.setMinimumSize(buttonsize);
        buttonSave.setMaximumSize(buttonsize);
        buttonSave.setPreferredSize(buttonsize);
        // Separator
        toolbar.addSeparator(separator);
        // Cut
        buttonCut = new JButton(iconCut);
        buttonCut.setDefaultCapable(false);
        buttonCut.setToolTipText("Cut the selection to the clipboard");
        buttonCut.setMnemonic((int)'T');
        toolbar.add(buttonCut);
        bounds.x += bounds.width;
        buttonCut.setBounds(bounds);
        buttonCut.setMinimumSize(buttonsize);
        buttonCut.setMaximumSize(buttonsize);
        buttonCut.setPreferredSize(buttonsize);
        // Copy
        buttonCopy = new JButton(iconCopy);
        buttonCopy.setDefaultCapable(false);
        buttonCopy.setToolTipText("Copy the selection to the clipboard");
        buttonCopy.setMnemonic((int)'C');
        toolbar.add(buttonCopy);
        bounds.x += bounds.width;
        buttonCopy.setBounds(bounds);
        buttonCopy.setMinimumSize(buttonsize);
        buttonCopy.setMaximumSize(buttonsize);
        buttonCopy.setPreferredSize(buttonsize);
        // Paste
        buttonPaste = new JButton(iconPaste);
        buttonPaste.setDefaultCapable(false);
        buttonPaste.setToolTipText("Insert clipboard contents");
        buttonPaste.setMnemonic((int)'P');
        toolbar.add(buttonPaste);
        bounds.x += bounds.width;
        buttonPaste.setBounds(bounds);
        buttonPaste.setMinimumSize(buttonsize);
        buttonPaste.setMaximumSize(buttonsize);
        buttonPaste.setPreferredSize(buttonsize);
        // Add
        buttonAddBranch = new JButton(iconAddBranch);
        buttonAddBranch.setDefaultCapable(false);
        buttonAddBranch.setToolTipText("Add Branch");
        buttonAddBranch.setMnemonic((int)'A');
        toolbar.add(buttonAddBranch);
        bounds.x += bounds.width;
        buttonAddBranch.setBounds(bounds);
        buttonAddBranch.setMinimumSize(buttonsize);
        buttonAddBranch.setMaximumSize(buttonsize);
        buttonAddBranch.setPreferredSize(buttonsize);

        // Separator
        toolbar.addSeparator(separator);
        // About
        buttonAbout = new JButton(iconAbout);
        buttonAbout.setDefaultCapable(false);
        buttonAbout.setToolTipText("Display program information");
        buttonAbout.setMnemonic((int)'A');
        toolbar.add(buttonAbout);
        bounds.x += bounds.width;
        buttonAbout.setBounds(bounds);
        buttonAbout.setMinimumSize(buttonsize);
        buttonAbout.setMaximumSize(buttonsize);
        buttonAbout.setPreferredSize(buttonsize);
    }

    protected void menuAddItems(JMenuBar hmenu)
    {
        JMenu menu;
        JMenuItem item;

        //
        // "File" menu
        //
        menu = new JMenu("File");
        menu.setActionCommand("File");
        menu.setBorderPainted(false);
        menu.setMnemonic((int)'F');
        hmenu.add(menu);
        // "New" item
        menuitemNew = new JMenuItem("New");
        menuitemNew.setHorizontalTextPosition(SwingConstants.RIGHT);
        menuitemNew.setActionCommand("New");
        menuitemNew.setBorderPainted(false);
        menuitemNew.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_N, Event.CTRL_MASK));
        menuitemNew.setMnemonic((int)'N');
        menuitemNew.setIcon(iconNew);
        menu.add(menuitemNew);
        // "Open" item
        menuitemOpen = new JMenuItem("Open...");
        menuitemOpen.setHorizontalTextPosition(SwingConstants.RIGHT);
        menuitemOpen.setActionCommand("Open...");
        menuitemOpen.setBorderPainted(false);
        menuitemOpen.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_O, Event.CTRL_MASK));
        menuitemOpen.setMnemonic((int)'O');
        menuitemOpen.setIcon(iconOpen);
        menu.add(menuitemOpen);
        // "Save" item
        menuitemSave = new JMenuItem("Save");
        menuitemSave.setHorizontalTextPosition(SwingConstants.RIGHT);
        menuitemSave.setActionCommand("Save");
        menuitemSave.setBorderPainted(false);
        menuitemSave.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_S, Event.CTRL_MASK));
        menuitemSave.setMnemonic((int)'S');
        menuitemSave.setIcon(iconSave);
        menu.add(menuitemSave);
        // "Save As" item
        menuitemSaveAs = new JMenuItem("Save As...");
        menuitemSaveAs.setActionCommand("Save As...");
        menuitemSaveAs.setBorderPainted(false);
        menuitemSaveAs.setMnemonic((int)'A');
        menu.add(menuitemSaveAs);
        // Separator
        menu.add(new JSeparator());
        // "Exit" item
        menuitemExit = new JMenuItem("Exit");
        menuitemExit.setActionCommand("Exit");
        menuitemExit.setBorderPainted(false);
        menuitemExit.setMnemonic((int)'X');
        menu.add(menuitemExit);
        // "Edit" menu
        menu = new JMenu("Edit");
        menu.setActionCommand("Edit");
        menu.setBorderPainted(false);
        menu.setMnemonic((int)'E');
        hmenu.add(menu);
        // "Cut" item
        menuitemCut = new JMenuItem("Cut");
        menuitemCut.setHorizontalTextPosition(SwingConstants.RIGHT);
        menuitemCut.setActionCommand("Cut");
        menuitemCut.setBorderPainted(false);
        menuitemCut.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_X, Event.CTRL_MASK));
        menuitemCut.setMnemonic((int)'T');
        menuitemCut.setIcon(iconCut);
        menu.add(menuitemCut);
        // "Copy" item
        menuitemCopy = new JMenuItem("Copy");
        menuitemCopy.setHorizontalTextPosition(SwingConstants.RIGHT);
        menuitemCopy.setActionCommand("Copy");
        menuitemCopy.setBorderPainted(false);
        menuitemCopy.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_C, Event.CTRL_MASK));
        menuitemCopy.setMnemonic((int)'C');
        menuitemCopy.setIcon(iconCopy);
        menu.add(menuitemCopy);
        // "Paste" item
        menuitemPaste = new JMenuItem("Paste");
        menuitemPaste.setHorizontalTextPosition(SwingConstants.RIGHT);
        menuitemPaste.setActionCommand("Paste");
        menuitemPaste.setBorderPainted(false);
        menuitemPaste.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_V, Event.CTRL_MASK));
        menuitemPaste.setMnemonic((int)'P');
        menuitemPaste.setIcon(iconPaste);
        menu.add(menuitemPaste);
        // "addBranch" item
        menuitemPasteAddBranch = new JMenuItem("addBranch");
        menuitemPasteAddBranch.setHorizontalTextPosition(SwingConstants.RIGHT);
        menuitemPasteAddBranch.setActionCommand("addBranch");
        menuitemPasteAddBranch.setBorderPainted(false);
        menuitemPasteAddBranch.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_A, Event.CTRL_MASK));
        menuitemPasteAddBranch.setMnemonic((int)'A');
        menuitemPasteAddBranch.setIcon(iconAddBranch);
        menu.add(menuitemPasteAddBranch);

        //
        // "Help" menu
        //
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
        menuitemAbout.setIcon(iconAbout);
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

        if(addNotify_done) return;

        // Adjust size according to the insets so that entire component
        // areas are renderable.
        int menubarheight=0;
        JMenuBar menubar = getRootPane().getJMenuBar();
        if(menubar!=null)
        {
            menubarheight = menubar.getPreferredSize().height;
        }
        Insets insets=getInsets();
        setSize(insets.left + insets.right + d.width, insets.top + insets.bottom + d.height + menubarheight);
        addNotify_done=true;
    }


    protected void AboutApplication()
    {
        try
        {
            JOptionPane.showMessageDialog(this,
                                          "MUCHO MOJO MAGI\nfun to say huh?",
                                          "About" ,
                                          JOptionPane.INFORMATION_MESSAGE);
        }
        catch(Exception e)
        {
        }
    }

    protected void ExitApplication()
    {
        try
        {
            // Beep
            Toolkit.getDefaultToolkit().beep();
            // Show an Exit confirmation dialog
            int reply = JOptionPane.showConfirmDialog(this,
                                                      "Leave? Here? If you're sure you really want to",
                                                      "Exit" ,
                                                      JOptionPane.YES_NO_OPTION,
                                                      JOptionPane.QUESTION_MESSAGE);
            if(reply==JOptionPane.YES_OPTION)
            {
                // User answered "Yes", so cleanup and exit
                //
                // Hide the frame
                this.setVisible(false);
                // Free system resources
                this.dispose();
                // Exit the application
                System.exit(0);
            }
        }
        catch(Exception e)
        {
        }
    }

    class EvtListener extends WindowAdapter
    {
        public void windowClosing(WindowEvent event)
        {
            Object object = event.getSource();
            if(object==Mojo.this)
            {
                Mojo_windowClosing(event);
            }
        }
    }

    void Mojo_windowClosing(WindowEvent event)
    {

        // TODO:  code goes here

        try
        {
            this.ExitApplication();
        }
        catch(Exception e)
        {
        }
    }

    class ActListener implements ActionListener
    {
        public void actionPerformed(ActionEvent event)
        {
            Object object = event.getSource();
            if(object==menuitemOpen)
            {
                menuitemOpen_Action(event);
            }
            else if(object==menuitemSave)
            {
                menuitemSave_Action(event);
            }
            else if(object==menuitemExit)
            {
                menuitemExit_Action(event);
            }
            else if(object==menuitemAbout)
            {
                menuitemAbout_Action(event);
            }
            else if(object==buttonOpen)
            {
                buttonOpen_Action(event);
            }
            else if(object==buttonSave)
            {
                buttonSave_Action(event);
            }
            else if(object==buttonAbout)
            {
                buttonAbout_Action(event);
            }
        }
    }

    void menuitemOpen_Action(ActionEvent event)
    {

        // TODO:  code goes here

        try
        {
            // Show open fileDialog modal
            fileDialog.showOpenDialog(menuMain);
        }
        catch(Exception e)
        {
        }
    }

    void menuitemSave_Action(ActionEvent event)
    {

        // TODO:  code goes here

        try
        {
            // Show save fileDialog modal
            fileDialog.showSaveDialog(menuMain);
        }
        catch(Exception e)
        {
        }
    }

    void menuitemExit_Action(ActionEvent event)
    {

        // TODO:  code goes here

        try
        {
            this.ExitApplication();
        }
        catch(Exception e)
        {
        }
    }

    void menuitemAbout_Action(ActionEvent event)
    {

        // TODO:  code goes here

        try
        {
            this.AboutApplication();
        }
        catch(Exception e)
        {
        }
    }

    void buttonOpen_Action(ActionEvent event)
    {

        // TODO:  code goes here

        try
        {
            // Show open fileDialog modal
            fileDialog.showOpenDialog(buttonOpen);
        }
        catch(Exception e)
        {
        }
    }

    void buttonSave_Action(ActionEvent event)
    {

        // TODO:  code goes here

        try
        {
            // Show save fileDialog modal
            fileDialog.showSaveDialog(buttonSave);
        }
        catch(Exception e)
        {
        }
    }

    void buttonAbout_Action(ActionEvent event)
    {

        // TODO:  code goes here

        try
        {
            this.AboutApplication();
        }
        catch(Exception e)
        {
        }
    } 

    /** Required by TreeSelectionListener interface. */
    public void valueChanged(TreeSelectionEvent e) 
    {
        DefaultMutableTreeNode node = (DefaultMutableTreeNode) tree.getLastSelectedPathComponent();
        if(node == null) return;

        Object nodeInfo = node.getUserObject();
        if(node.isLeaf())
        {
            if(nodeInfo instanceof String)
            {
                String str = (String)nodeInfo;

                htmlPane.setText(str);
            }
            else if(nodeInfo instanceof BookInfo)
            {
                BookInfo book = (BookInfo)nodeInfo;

                displayURL(book.bookURL);
            }
        }
    }

    private class BookInfo
    {
        public String bookName;
        public URL bookURL;

        public BookInfo(String book, String filename)
        {
            bookName = book;
            bookURL = Mojo.class.getResource(filename);
            if(bookURL == null)
            {
                System.err.println("Couldn't find file: "+ filename);
            }
        }

        public String toString()
        {
            return bookName;
        }
    }

    private void displayURL(URL url)
    {
        try
        {
            if(url != null)
            {
                htmlPane.setPage(url);
            }
            else
            {
                htmlPane.setText("File Not Found");
            }
        }
        catch(IOException e)
        {
            System.err.println("Attempted to read a bad URL: " + url);
        }
    }

    private void createNodes(DefaultMutableTreeNode top)
    {
        DefaultMutableTreeNode category = null;
        DefaultMutableTreeNode subDir = null;

        category = addBranch(top, "Limb1");

        addLeaf(category, new BookInfo("Leaf1","Leaf1.html"));

        addLeaf(category, new BookInfo("Leaf2","Leaf2.html"));

        subDir = addBranch(category, "Limb1");

        addLeaf(subDir, new BookInfo("Leaf1","Leaf1.html"));

        addLeaf(subDir, new BookInfo("Leaf2","Leaf2.html"));

        subDir = addBranch(category, "Limb12");

        addLeaf(subDir, new BookInfo("Leaf1","Leaf1.html"));

        addLeaf(subDir, new BookInfo("Leaf2","Leaf2.html"));


//
        category = addBranch(top, "Limb2");

        addLeaf(category, new BookInfo("Leaf3","Leaf3.html"));

        addLeaf(category, new BookInfo("Leaf4","Leaf4.html"));

        subDir = addBranch(category, "Limb1");

        addLeaf(subDir, new BookInfo("Leaf1","Leaf1.html"));

        addLeaf(subDir, new BookInfo("Leaf2","Leaf2.html"));

        subDir = addBranch(category, "Limb12");

        addLeaf(subDir, new BookInfo("Leaf1","Leaf1.html"));

        addLeaf(subDir, new BookInfo("Leaf2","Leaf2.html"));


    }

    private DefaultMutableTreeNode addBranch(DefaultMutableTreeNode root, String name)
    {
        DefaultMutableTreeNode branch = new DefaultMutableTreeNode(name, true);

        root.add(branch);

        return branch;
    }

    private void addLeaf(DefaultMutableTreeNode root, Object obj)
    {
        DefaultMutableTreeNode leaf = new DefaultMutableTreeNode(obj, false);

        root.add(leaf);

    }

    public void populateTree(DynamicTree treePanel)
    {
        String p1Name = new String("Parent 1");
        String p2Name = new String("Parent 2");
        String c1Name = new String("Child 1");
        String c2Name = new String("Child 2");

        DefaultMutableTreeNode p1, p2;

        p1 = treePanel.addObject(null, p1Name);
        p2 = treePanel.addObject(null, p2Name);

        treePanel.addObject(p1, c1Name);
        treePanel.addObject(p1, c2Name);

        treePanel.addObject(p2, c1Name);
        treePanel.addObject(p2, c2Name);
    }

    public void actionPerformed(ActionEvent e)
    {
        String command = e.getActionCommand();

        if(ADD_COMMAND.equals(command))
        {
            treePanel.addObject("New Node " + newNodeSuffix++);
        }
        else if(REMOVE_COMMAND.equals(command))
        {
            treePanel.removeCurrentNode();
        }
        else if(CLEAR_COMMAND.equals(command))
        {
            treePanel.clear();
        }
    }

    static public void main(String args[])
    {
        try
        {
            (new Mojo()).setVisible(true);
        }
        catch(Exception e)
        {
            System.err.println(e);
            e.printStackTrace();
            System.exit(1);
        }
    }
}
