import java.beans.*;
import java.io.*;
import javax.swing.*;


public class test
{

    public static void main(String args[])
    {
        try
        {
            XMLEncoder e = new XMLEncoder(new BufferedOutputStream(new FileOutputStream("Test.xml")));

            e.writeObject(new JButton("Hello"));

            e.close();

        }
        catch (Exception e)
        {
            e.printStackTrace();
        }

    }

    public static class Booger
    {
        String name = "booger";
        int val = 0xFF;
        char charry[]=
        {
            'a','b','c'
        };

    }
}
