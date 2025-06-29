import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import java.util.*;

public class EquTriangle extends JFrame
{
    public final static int ONE_SECOND = 1000;
    int WIN_WIDTH = 1200;
    int WIN_HEIGHT = 1200;
    int LINE_LEN = 100;

    JPanel jPanel1 = new MyPanel();

    class MyPanel extends JPanel
    {
        public void paint(Graphics g)
        {
            Graphics2D g2 = (Graphics2D) g;

            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, 
                                RenderingHints.VALUE_ANTIALIAS_ON);
            clear(g2);

            g2.setBackground(Color.white);

            g2.setPaint(Color.black);

            Point pts[] = genPtList(3, LINE_LEN);

            //drawCycle(g2, pts, WIN_WIDTH/2, WIN_HEIGHT/2);

            /*
            for(int i=0;i!=pts.length;i++)
            {
                drawCycle(g2, pts, pts[i].x + WIN_WIDTH/2, pts[i].y + WIN_HEIGHT/2);
            }
            */

            int x_dim = pts[0].x - pts[1].x;
            int y_dim = pts[0].y -pts[2].y;

            for(int x=0; x < WIN_WIDTH; x+=x_dim)
                for(int y=0; y < WIN_HEIGHT; y+=y_dim)
                {
                    drawCycle(g2, pts, x, y);
                }


        }
    }

    void drawCycle(Graphics2D g2, Point pts[], int x_ofs, int y_ofs)
    {
        Vector vect = new Vector();

        /*
        for(int i=0;i!=pts.length-1;i++)
        {
            g2.drawLine(pts[i].x + x_ofs,
                        pts[i].y + y_ofs, 
                        pts[i+1].x + x_ofs,
                        pts[i+1].y + y_ofs);
        }
        */

        for(int i=0;i!=pts.length;i++)
        {
            drawCircle(g2, pts[i].x + x_ofs, pts[i].y + y_ofs, getSideLength(LINE_LEN)- getSideLength(LINE_LEN)/4);

            drawCircle(g2, pts[i].x + x_ofs, pts[i].y + y_ofs, getSideLength(LINE_LEN)/4);
        }

        /*
        g2.drawLine(pts[pts.length-1].x + x_ofs,
                    pts[pts.length-1].y + y_ofs, 
                    pts[0].x + x_ofs,
                    pts[0].y + y_ofs);
                    */
    }

    void clear(Graphics2D g2)
    {
        g2.setPaint(Color.white);
        g2.setBackground(Color.black);
        g2.fillRect(0, 0, WIN_WIDTH, WIN_HEIGHT);
    }


    int getSideLength(int radius)
    {
        return(int) (3 * radius / Math.sqrt(3));
    }

    void drawCircle(Graphics2D g2, int x, int y, int radius)
    {
        g2.drawArc(x - radius, y - radius, radius * 2, radius * 2, 0, 360);
    }

    Point[] genPtList(int ptCnt, int rad)
    {
        Point pts[] = new Point[ptCnt];

        double x = 0;
        double y = 0;
        double angle = 0;
        double radius = rad;
        double angleInc = 2 * Math.PI /ptCnt;

        for(int i=0;i!=ptCnt;i++)
        {
            x = radius * Math.cos (angle);
            y = radius * Math.sin (angle);

            pts[i] = new Point((int)x,(int)y);
            angle += angleInc;
        }
        return pts;
    }

    public EquTriangle()
    {
        this.setBounds(0, 0, WIN_WIDTH, WIN_HEIGHT);
        this.setSize(WIN_WIDTH, WIN_HEIGHT);
        getContentPane().setLayout(new BorderLayout());

        this.jPanel1.setBounds(0, 0, WIN_WIDTH, WIN_HEIGHT);
        this.jPanel1.setSize(WIN_WIDTH, WIN_HEIGHT);
        this.jPanel1.setPreferredSize(new Dimension(WIN_WIDTH, WIN_HEIGHT));
        getContentPane().add(jPanel1, "Center");

        this.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        this.addWindowListener(new WindowAdapter()
                               {
                                   public void 
                                   windowClosing(java.awt.event.WindowEvent e0)
                                   {
                                       System.exit(0);
                                   }
                               });
        setVisible(true);
    }

    public static void main(String args[])
    {
        new EquTriangle();
    }
}



