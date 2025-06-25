
import java.math.BigInteger;
import java.util.*;
import java.beans.*;
import java.io.*;
import java.lang.*;

class NTH
{
    String storeFile = "storageFile.xml";
    Random rand=new Random();

    ArrayList<String[]> grid = new ArrayList<String[]>();

    public static void writeXML(ArrayList<String[]> f, String filename) throws Exception{
        XMLEncoder encoder =
        new XMLEncoder(new BufferedOutputStream(new FileOutputStream(filename)));
        encoder.writeObject(f);
        encoder.close();
    }

    public static ArrayList<String[]> readXML(String filename) throws Exception {
        XMLDecoder decoder =
        new XMLDecoder(new BufferedInputStream( new FileInputStream(filename)));
        ArrayList<String[]> o = (ArrayList<String[]>)decoder.readObject();
        decoder.close();
        return o;
    }

    void makGrid(int size, int dim)
    {
        int elemCnt=(int)Math.pow((double)size, (double)dim-1);

        // int c=0;
        for (int i=0;i<elemCnt;i++)
        {
            String temp[]=new String[size];

            for (int n=0;n!=size;n++)
            {
                temp[n]= Integer.toHexString(StrictMath.abs(rand.nextInt()));
            }
            grid.add(temp); 
        }
        showGrid();
    }

    void showGrid()
    {
        int c=0;
        for (String elems[]:grid )
        {
            System.out.println("elem_"+c+++":");

            for (String str:elems )
            {
                System.out.println(str+", ");
            }
        }
    }

    public static void main(String args[]) {
        int myargs[]={4,4};
        NTH nth= new NTH();

        try
        {
            boolean gen=true;
            if (gen==false)
            {
                nth.grid=readXML(nth.storeFile);
                nth.showGrid();
            } else
            {
                nth.makGrid(myargs[0], myargs[1]);                 
                nth.writeXML(nth.grid, nth.storeFile);
            }
        } catch (Exception e)
        {
            System.out.println("Exeception"+ e);
        }
    }
}


