
import java.math.BigInteger;
import java.util.*;
import java.beans.*;
import java.io.*;
import java.lang.*;

class NTH
{
    String storeFile = "storageFile.xml";
    Random rand=new Random();

    LinkedHashMap map= new LinkedHashMap<String, String[]>();

    public void writeXML(String filename) throws Exception{
        XMLEncoder encoder =
        new XMLEncoder(new BufferedOutputStream(new FileOutputStream(filename)));
        encoder.writeObject(map);
        encoder.close();
    }

    public void readXML(String filename) throws Exception {
        XMLDecoder decoder =
        new XMLDecoder(new BufferedInputStream( new FileInputStream(filename)));
        map = (LinkedHashMap<String, String[]>)decoder.readObject();
        decoder.close();
    }

    void GenMap(int size, int dim)
    {
        int elemCnt=(int)Math.pow((double)size, (double)dim-1);
        String name="";

        for (int i=0;i<elemCnt;i++)
        {
            name="Elem"+i;

            String temp[]=new String[size];

            for (int n=0;n!=size;n++)
            {
                temp[n]= Integer.toHexString(StrictMath.abs(rand.nextInt()));
            }
            map.put(name, temp); 
        }
        showMap();
    }

    void showMap()
    {
        Iterator<String> iter = map.keySet().iterator();

        while (iter.hasNext())
        {
            String key =(String) iter.next();

            System.out.println(key+":");

            String[]values=(String[]) map.get(key);

            for (String str:values )
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
            boolean gen=false;
            if (gen==false)
            {
                nth.readXML(nth.storeFile);
                nth.showMap();
            } else
            {
                nth.GenMap(myargs[0], myargs[1]);                 
                nth.writeXML(nth.storeFile);
            }
        } catch (Exception e)
        {
            System.out.println("Exeception"+ e);
        }
    }
}


