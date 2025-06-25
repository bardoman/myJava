import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import java.util.*;

public class Simplex extends JFrame
{
    public final static int ONE_SECOND = 1000;
    public static int WIN_WIDTH = 1000;
    public static int WIN_HEIGHT = 1000;
    public static int RADIUS = 400;

    JPanel jPanel1 = new MyPanel();
    Graphics2D g2;
    long lineCnt = 0;
    long faceCnt = 0;
    long cellCnt = 0;
    int vertexCnt = 0;

    class MyPanel extends JPanel
    {
        public void paint(Graphics g)
        {
            g2 = (Graphics2D) g;


            Font font = g2.getFont();

            g2.setFont(new Font(font.getName(), font.getStyle() ,
                                font.getSize() * 2 ));

            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                                RenderingHints.VALUE_ANTIALIAS_ON);

            g2.setBackground(Color.white);

            g2.setPaint(Color.black);

            g2.translate(WIN_WIDTH/2,WIN_WIDTH/2);

            Polygon nGon = genPoly(vertexCnt,RADIUS);

            drawSimplex(nGon);

            drawOvals(nGon);

            printString();
        }
    }

    public void printString()
    {
        lineCnt = binomial(vertexCnt,2);

        faceCnt = binomial(vertexCnt,3);

        cellCnt = binomial(vertexCnt,4);

        g2.setColor(Color.black);

        g2.drawString("Vertices = "+vertexCnt+
                      ", Lines = "+String.valueOf(lineCnt)+
                      ", Faces = "+String.valueOf(faceCnt)+
                      ", Cells = "+String.valueOf(cellCnt),
                      -RADIUS, RADIUS + 50 );
    }

    public static long binomial(int n, int k)
    {
        double b = factorial(n)/(factorial(k) * factorial(n-k));

        return(long) b;
    }

    public static double factorial(int n)
    {
        double f = 1;
        for(int i = 2; i <= n; i++)
        {
            f *= i;
        }
        return f;
    }

    void drawOvals(Polygon nGon)
    {

        for(int i=0;i!=nGon.npoints;i++)
        {
            int xPt = nGon.xpoints[i];
            int yPt = nGon.ypoints[i];

            g2.setColor(Color.black);

            g2.drawOval(xPt-20,             
                        yPt-20,             
                        40,
                        40);


            g2.fillOval(xPt-20,             
                        yPt-20,             
                        40,
                        40);

            g2.setColor(Color.white);

            g2.drawString(String.valueOf(i), xPt-10, yPt+10);

        }
    }

    void drawSimplex( Polygon nGon)
    {

        g2.setColor(Color.black);

        for(int i=0;i!=nGon.npoints;i++)
        {
            int xPt = nGon.xpoints[i];
            int yPt = nGon.ypoints[i];

            for(int n=0;n!=nGon.npoints;n++)
            {
                g2.drawLine(xPt,             
                            yPt,             
                            nGon.xpoints[n],
                            nGon.ypoints[n]);
            }
        }
    }

    Polygon genPoly(int ptCnt, int rad)
    {
        Polygon nGon = new Polygon();

        double x = 0;
        double y = 0;
        double angle = 0;
        double radius = rad;
        double angleInc = 2 * Math.PI /ptCnt;

        for(int i=0;i!=ptCnt;i++)
        {
            x = radius * Math.cos (angle);
            y = radius * Math.sin (angle);

            nGon.addPoint((int) x, (int) y );

            angle += angleInc;
        }
        return nGon;
    }

    public Simplex(int vertexCnt)
    {
        this.vertexCnt = vertexCnt;

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
        if(args.length != 1)
        {
            System.out.println("Simplex generates a graphic of the Simplex Object for a given Vertex count.");

            System.out.println("Usage: Simplex <VertexCount>");
        }
        else
        {
            /*
            for(int i=0;i!=50;i++)
            {
                System.out.println("Pt="+i+" line="+binomial(i,2)+" 
faces="+binomial(i,3)+" Cell="+binomial(i,4));
            }
            */


            new Simplex(Integer.valueOf(args[0]).intValue());
        }
    }
}




