import java.awt.*;
import java.awt.event.*;
import javax.swing.*;

public class Spiral extends JFrame {
    double Phi =  (1 + Math.sqrt(5)) / 2;

    int WIN_WIDTH = 1000;
    int WIN_HEIGHT = 1000;

    JPanel jPanel1 = new MyPanel();
    FlowLayout flowLayout1 = new FlowLayout();

    class MyPanel extends JPanel {
        public void paint(Graphics g) {
            Graphics2D g2 = (Graphics2D) g;
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            Dimension d = getSize();

            g2.setPaint(Color.black);
            g2.setBackground(Color.white);

            Pt pts[];
            double rot = 0;
            double inc = .1;//.05;//
            int ptCnt = 500;//250;// 
            double rotInt = .262;//.52; //.53;// .265;//

            /*
            for(int i=0;i!=12;i++)
            {
                pts = getPtList(ptCnt, inc, rot);
                drawList(g2, pts);
               // drawListX(g2, pts);
                rot += rotInt;
            }
            */
            //for(int i=0;i!=24;i++)
            for(int i=0;i!=48;i++) {
                //pts = getPtList(185, inc, rot);

                pts = getPtList(300, inc, rot);

                drawList(g2, pts, WIN_WIDTH/2, WIN_HEIGHT/2);

                drawListX(g2, pts, WIN_WIDTH/2, WIN_HEIGHT/2);

                rot += rotInt/2;
            }


        }
    }

    void drawListX(Graphics2D g2, Pt pts[], int x_ofs, int y_ofs) {
        Pt tempPt = pts[0];

        for(int i=1;i!=pts.length;i++) {
            g2.drawLine((int)tempPt.x + x_ofs, (int)-tempPt.y + y_ofs , (int)pts[i].x + x_ofs, (int)-pts[i].y + y_ofs);

            tempPt = pts[i];
        }
    }

    void drawList(Graphics2D g2, Pt pts[], int x_ofs, int y_ofs) {
        Pt tempPt = pts[0];

        for(int i=1;i!=pts.length;i++) {
            g2.drawLine((int)tempPt.x + x_ofs, (int)tempPt.y + y_ofs , (int)pts[i].x + x_ofs, (int)pts[i].y + y_ofs);

            tempPt = pts[i];
        }
    }

    Pt[] getPtList(int ptCnt, double inc, double rot) {
        Pt pts[] = new Pt[ptCnt];

        double x = 0;
        double y = 0;
        double angle = 0;

        for(int i=0;i!=ptCnt;i++) {
            x = Math.cos (angle +rot) * Math.pow(Phi, ((2 / Math.PI) * angle)) ;
            y = Math.sin (angle +rot) * Math.pow(Phi, ((2 / Math.PI) * angle)) ;

            pts[i] = new Pt(x,y); 
            angle +=inc;
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


    public Spiral() {
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
        new Spiral().setVisible(true);
    }
}


