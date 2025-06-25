import java.beans.*;
import java.io.*;
import java.io.File;
import java.nio.charset.Charset;
import java.text.SimpleDateFormat;
import java.util.*;

import java.lang.*;
import java.math.*;
import java.beans.XMLEncoder;
import java.beans.XMLDecoder;

public class Puka2 {

    Date date= new Date();
    public TreeMap<String,String> items = new TreeMap<String,String>();

    public Random rand = new Random(date.getTime());

    String storeFile = "storageFile.xml";

    public static void writeXML(TreeMap f, String filename) throws Exception{
        XMLEncoder encoder =
        new XMLEncoder(new BufferedOutputStream(new FileOutputStream(filename)));
        encoder.writeObject(f);
        encoder.close();
    }

    public static TreeMap readXML(String filename) throws Exception {
        XMLDecoder decoder =
        new XMLDecoder(new BufferedInputStream(
                                              new FileInputStream(filename)));
        TreeMap o = (TreeMap)decoder.readObject();
        decoder.close();
        return o;
    }

    public  String getCurrentTimeStamp() {
        SimpleDateFormat sdfDate = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");//dd/MM/yyyy
        Date now = new Date();
        String strDate = sdfDate.format(now);
        return strDate;
    }

    private  void init() {
        String str="";

        for (int i = 0; i < 50; i++) {
            str=getRandStringBySize(50);
            items.put( str.substring( 0,3), str );
        }
    }

    String getRandCharAsString()
    {
        String str="";
        Charset charset = Charset.forName("1252");
        int i=rand.nextInt(91-65)+65;
        String s = Character.toString((char) i);
        byte[] encoded = s.getBytes(charset);
        String decoded = new String(encoded, charset);

        if (s.equals(decoded))str=s;

        return str;
    }

    String getRandStringBySize(int size)
    {
        String str="";

        for (int i=0;i<size;i++) {
            str+=getRandCharAsString();
        }
        return str;
    }

    public  void show( )throws Exception
    {  
        for (Map.Entry<String, String> entry : items.entrySet()) {
            System.out.println(entry.getKey()+":"+ entry.getValue());
        }

    }


    public static void main(String args[])throws Exception {
        Puka2 puka = new Puka2();

        boolean saveFlag=false;

        if(saveFlag==false)
            {
            puka.items=readXML(puka.storeFile);
        }
        else puka.init();

        puka.show();

       if(saveFlag==true)puka.writeXML(puka.items, puka.storeFile);
    }

}








