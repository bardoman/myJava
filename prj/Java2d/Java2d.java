

import java.awt.*;
import java.awt.event.*;

public class Java2d extends Frame {

    public static void main(String args[]) {
        new Java2d();
    }

    public Java2d() {
        setSize(500,500);
        setVisible(true);
        addWindowListener(new WindowAdapter()
                          {public void windowClosing(WindowEvent e)
                              {dispose(); System.exit(0);}
                          }
                         );
    }
    public void paint(Graphics g) {
        g.setColor(Color.black);
        int upperLimit=1000;
        int lowerLimit=20;

        for (int i=lowerLimit;i<upperLimit;i+=lowerLimit) {

            g.drawRect(lowerLimit,lowerLimit,i,i); 
            g.drawRect(upperLimit-i,upperLimit-i,i,i); 

            g.drawOval(lowerLimit,lowerLimit,i,i);
            g.drawOval(upperLimit-i,upperLimit-i,i,i); 

            g.drawOval(lowerLimit,upperLimit-i,i,i);

            g.drawOval(upperLimit-i,lowerLimit,i,i);

        }
    }
}
