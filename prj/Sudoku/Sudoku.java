import java.awt.*;
import java.awt.event.*;
import java.io.File;
import java.io.FileOutputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.io.*;
import javax.swing.table.*;

import java.util.*;

import javax.swing.*;

public class Sudoku extends JFrame
{
    int WIN_WIDTH = 400;
    int WIN_HEIGHT = 400;

    DefaultTableModel model = new DefaultTableModel(9,9);

    JTable table = new JTable(model);

    String colNames[]=
    {
        "A","B","C","D","E","F","G","H","I"
    };

    public Sudoku()
    {
        super();
        try
        {
            initComponents();
        }
        catch(Exception e)
        {
            e.printStackTrace();
        }
    }

    private void initComponents() throws Exception {
        this.setBounds(0, 0,WIN_WIDTH, WIN_HEIGHT);
        this.setSize(WIN_WIDTH, WIN_HEIGHT);
        getContentPane().setLayout(new BorderLayout());

        this.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        this.addWindowListener(new WindowAdapter()
                               {
                                   public void windowClosing(java.awt.event.WindowEvent e0)
                                   {
                                       save();

                                       System.exit(0);
                                   }
                               });

        File file = new File("obj.ser");

        if(file.exists())
        {
            ObjectInputStream in = new ObjectInputStream(new  FileInputStream("obj.ser")); 

            Vector data = (Vector) in.readObject();

            Vector cols = new Vector();

            for(int i=0;i != colNames.length;i++)
            {
                cols.add(colNames[i]);
            }

            model.setDataVector(data,cols);
        }

        this.getContentPane().add(new JScrollPane(table));
    }




    void save()
    {
        Vector data = model.getDataVector();

        try
        {
            ObjectOutputStream out = new ObjectOutputStream(new  FileOutputStream("obj.ser")); 

            out.writeObject(data);

        }
        catch(Exception e)
        {
            e.printStackTrace();
        }
    }

    public static void main(String args[])
    {
        new Sudoku().setVisible(true);
    }
}


