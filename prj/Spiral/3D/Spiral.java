import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import java.util.*;


import com.sun.j3d.utils.applet.MainFrame;
import com.sun.j3d.utils.geometry.ColorCube;
import com.sun.j3d.utils.universe.*;
import javax.media.j3d.*;
import javax.vecmath.*;


public class J3DTest extends JFrame
{
    double Phi =  (1 + Math.sqrt(5)) / 2;

    int WIN_WIDTH = 1000;
    int WIN_HEIGHT = 1000;
    int PT_CNT = 1000;

    JPanel jPanel1 = new MyPanel();
    FlowLayout flowLayout1 = new FlowLayout();

    Graphics2D g2=null;

    class MyPanel extends JPanel
    {
        public void paint(Graphics g)
        {
            g2 = (Graphics2D) g;
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            Dimension d = getSize();

            clear();

            g2.setPaint(Color.black);
            g2.setBackground(Color.white);

            Pt pts[][];

            pts = getPtList(1);

            drawList(pts, 1, WIN_WIDTH/2, WIN_HEIGHT/2);
        }
    }

   
    void clear()
    {
        g2.setPaint(Color.white);
        g2.setBackground(Color.black);
        g2.fillRect(0, 0, WIN_WIDTH, WIN_HEIGHT);
    }

   
    private SimpleUniverse u = null;

    public BranchGroup createSceneGraph() {
        // Create the root of the branch graph
        BranchGroup objRoot = new BranchGroup();

        // Create the TransformGroup node and initialize it to the
        // identity. Enable the TRANSFORM_WRITE capability so that
        // our behavior code can modify it at run time. Add it to
        // the root of the subgraph.
        TransformGroup objTrans = new TransformGroup();
        objTrans.setCapability(TransformGroup.ALLOW_TRANSFORM_WRITE);
        objRoot.addChild(objTrans);

        // Create a simple Shape3D node; add it to the scene graph.
        objTrans.addChild(new ColorCube(0.4));

        // Create a new Behavior object that will perform the
        // desired operation on the specified transform and add
        // it into the scene graph.
        Transform3D yAxis = new Transform3D();
        Alpha rotationAlpha = new Alpha(-1, 4000);

        RotationInterpolator rotator =
            new RotationInterpolator(rotationAlpha, objTrans, yAxis,
                                     0.0f, (float) Math.PI*2.0f);
        BoundingSphere bounds =
            new BoundingSphere(new Point3d(0.0,0.0,0.0), 100.0);
        rotator.setSchedulingBounds(bounds);
        objRoot.addChild(rotator);

        // Have Java 3D perform optimizations on this scene graph.
        objRoot.compile();

        return objRoot;
    }

    public void init() {
        setLayout(new BorderLayout());
        GraphicsConfiguration config =
           SimpleUniverse.getPreferredConfiguration();

        Canvas3D c = new Canvas3D(config);
        add("Center", c);

        // Create a simple scene and attach it to the virtual universe
        BranchGroup scene = createSceneGraph();
        u = new SimpleUniverse(c);

        // This will move the ViewPlatform back a bit so the
        // objects in the scene can be viewed.
        u.getViewingPlatform().setNominalViewingTransform();

        u.addBranchGraph(scene);
    }

    public void destroy() {
        u.cleanup();
    }

    public J3DTest()
    {
        super();
        try
        {
            initComponents();
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    private void initComponents()
    {
        this.setBounds(0, 0,WIN_WIDTH, WIN_HEIGHT);
        this.setSize(WIN_WIDTH, WIN_HEIGHT);
        getContentPane().setLayout(new BorderLayout());

        this.jPanel1.setBounds(0, 0,WIN_WIDTH, WIN_HEIGHT);
        this.jPanel1.setSize(WIN_WIDTH, WIN_HEIGHT);
        this.jPanel1.setPreferredSize(new Dimension(WIN_WIDTH, WIN_HEIGHT));
        getContentPane().add(jPanel1, "Center");

        this.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        this.addWindowListener(new WindowAdapter()
                               {
                                   public void windowClosing(java.awt.event.WindowEvent e0)
                                   {
                                       System.exit(0);
                                   }
                               });
    }

    public static void main(String args[])
    {
        new J3DTest().setVisible(true);
    }
}


