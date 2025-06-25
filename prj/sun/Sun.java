
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;

public class Sun extends JFrame {
    double Phi = (1 + Math.sqrt(5)) / 2;

    int WIN_WIDTH = 1000;
    int WIN_HEIGHT = 1000;
    int RAY_COUNT = 100;

    JPanel jPanel1 = new MyPanel();
    FlowLayout flowLayout1 = new FlowLayout();
    Graphics2D g2=null;

    class MyPanel extends JPanel {
        public void paint(Graphics g) {
            g2 = (Graphics2D) g;

            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);


            Dimension d = getSize();

            g2.setPaint(Color.white);

            g2.fillRect(0,0,WIN_WIDTH ,WIN_HEIGHT  );

            g2.setBackground(Color.white);

            g2.setStroke(new BasicStroke(5));
            makeRays(435);

            g2.setStroke(new BasicStroke(4));
            makeRays(300);

            g2.setStroke(new BasicStroke(3));
            makeRays(200);

            g2.setStroke(new BasicStroke(2));
            makeRays(125);    

        }
    }

    void makeRays(int radius) {
        Pt pts[];

        pts = getPtList(RAY_COUNT,radius);

        g2.setPaint(Color.black);

        for(int i=0;i!=pts.length;i++) {
            g2.drawLine(WIN_WIDTH/2, WIN_HEIGHT/2, (int)pts[i].x, (int)pts[i].y );

            /*
            g2.drawArc(0, 0, (int)pts[i].x, (int)pts[i].y ,0 , (int)(2*Math.PI));

            g2.drawArc(0,  WIN_HEIGHT/2, (int)pts[i].x, (int)pts[i].y , 0 , (int)(2*Math.PI));

            g2.drawArc(0, 0, (int)pts[i].x, (int)pts[i].y ,0 , (int)(2*Math.PI));

            g2.drawArc(0,  WIN_HEIGHT/2, (int)pts[i].x, (int)pts[i].y , 0 , (int)(2*Math.PI));
            */
        }

        g2.setPaint(Color.white);

        radius = (radius*3)/4;

        g2.fillOval((WIN_WIDTH/2)-radius, (WIN_HEIGHT/2)-radius ,radius*2, 
                    radius*2);

    }

    Pt[] getPtList(int divCnt, int radius ) {

        Pt pts[] = new Pt[divCnt];
        double xx = 0;
        double yx = 0;
        double angle = 0;
        double degInc=360.00/(double)divCnt;
        double radInc= (degInc * Math.PI)/180;

        for(int i=0;i!=divCnt;i++) {

            xx = WIN_WIDTH/2 + radius * Math.cos (angle);
            yx = WIN_HEIGHT/2 + radius * Math.sin (angle);

            pts[i] = new Pt(xx,yx);
            angle +=radInc;
        }
        return pts;
    }

    class Pt {
        public double x;
        public double y;

        public Pt(double x, double y) {
            this.x = x;
            this.y = y;
        }
    }

    public Sun() {
        super();
        try {
            initComponents();
        }
        catch(Exception e) {
            e.printStackTrace();
        }
    }

    private void initComponents() {
        this.setBounds(0, 0,WIN_WIDTH, WIN_HEIGHT);
        this.setSize(WIN_WIDTH, WIN_HEIGHT);
        getContentPane().setLayout(new BorderLayout());

        this.jPanel1.setBounds(0, 0,WIN_WIDTH, WIN_HEIGHT);
        this.jPanel1.setSize(WIN_WIDTH, WIN_HEIGHT);
        this.jPanel1.setPreferredSize(new Dimension(WIN_WIDTH, WIN_HEIGHT));
        getContentPane().add(jPanel1, "Center");

        this.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        this.addWindowListener(new WindowAdapter() {
                                   public void windowClosing(java.awt.event.WindowEvent e0) {
                                       System.exit(0);
                                   }
                               });
    }

    public static void main(String args[]) {
        new Sun().setVisible(true);
    }
}



