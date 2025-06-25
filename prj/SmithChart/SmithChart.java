import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.util.*;

import javax.swing.*;

public class SmithChart extends JFrame {

    JPanel jPanel1 = new MyPanel();
    Graphics2D g2;
    int WIN_WIDTH = 1000;
    int WIN_HEIGHT = 1000;
    int RADIUS = 10;


    class MyPanel extends JPanel {
        public void paint(Graphics g) {
            g2 = (Graphics2D) g;

            g2.setBackground(Color.white);

            g2.setPaint(Color.black);


            for(int i=1;i!=100;i++) {


                g2.drawOval(500, 500-(i*7), RADIUS +(i*15), RADIUS+(i*15));


            }


        }
    }



    public SmithChart() {

        this.setBounds(0, 0, WIN_WIDTH, WIN_HEIGHT);
        this.setSize(WIN_WIDTH, WIN_HEIGHT);
        getContentPane().setLayout(new BorderLayout());

        this.jPanel1.setBounds(0, 0, WIN_WIDTH, WIN_HEIGHT);
        this.jPanel1.setSize(WIN_WIDTH, WIN_HEIGHT);
        this.jPanel1.setPreferredSize(new Dimension(WIN_WIDTH, WIN_HEIGHT));
        getContentPane().add(jPanel1, "Center");

        this.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        this.addWindowListener(new WindowAdapter() {
                                   public void
                                   windowClosing(java.awt.event.WindowEvent e0) {
                                       System.exit(0);
                                   }
                               });
        setVisible(true);


    }


    public static void main(String args[]) {

        new SmithChart();
    }
}



