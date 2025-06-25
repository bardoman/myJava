import java.awt.*;
import java.applet.*;
import javax.swing.*;


public class PigLatin extends JApplet
{
    JTextField inText = new JTextField("enter text here");
    JTextField outText = new JTextField();

    public void init()
    {
        setLayout(null);
        setSize(500,500);

        getContentPane().add(inText);
        getContentPane().add(outText);

    }
   /*
    public void paint(Graphics g) {
        g.drawString("Hello world!", 50, 25);
    }
    */
}


