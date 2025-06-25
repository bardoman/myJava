
import java.beans.*;
import java.io.*;
import java.lang.*;
import java.math.*;
import java.nio.charset.Charset;
import java.text.SimpleDateFormat;
import java.util.*;

public class Puka3 {
    Date date= new Date();

    public TreeMap<String,String> items = new TreeMap<String,String>();

    public Random rand = new Random(date.getTime());

    String storeFile = "storageFile.xml";

    //  public Branch tree = new Branch("root",true);

    private class Properties {

        int wordLength;
        int wordCnt;
        int lineCnt;
        int keyLength;
    };

    Properties properties=new Properties();

    public static void showCmds()
    {
        System.out.println("Puka is a simple database for storing keyword/string entries");
        System.out.println("commands: '(S)etLeaf', '(G)etLeaf', 'show(A)ll, 'Show(B)ranches, 'Show(L)eaves', '(M)akBranch'");
        System.out.println("Usage: Puka s <path> <leafKey> <value>");
        System.out.println("Usage: Puka g <path> <leafKey>");
        System.out.println("Usage: Puka m <path> <BranchKey>");
        System.out.println("Usage: Puka a <path>"); 
        System.out.println("Usage: Puka b <path>");
        System.out.println("Usage: Puka l <path>");

    }

//***********************************************
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


    public String getKey(String str)
    {
        String key=" ";
        int i=0;

        do {
            do {
                key=str.substring( 0,properties.keyLength+i++);//if key exists increment key length until it is unique
            }
            while (key.endsWith(" "));

        }
        while (items.containsKey(key));

        return key;
    }

    public  void init() {
        String value="";
        String key="";

        for (int i = 0; i < properties.lineCnt; i++) {
            value=getRandStringByWordCount(properties.wordCnt);

            if (items.containsValue(value))continue;

            key=getKey(value);

            items.put(key , value );
        }
    }

    int getFileLineCount(String name)throws Exception
    {
        BufferedReader  br = new BufferedReader(new FileReader(name));
        int n=0;
        while (( br.readLine()) != null) {
            n++;
        }
        return n;
    }

    String getFileLineByNum(String name, int num)throws Exception
    {
        BufferedReader  br = new BufferedReader(new FileReader(name));
        int n=0;
        String str="";

        while ((str=br.readLine())!=null) {
            if (num==n) {
                return str;
            }
            n++;
        }
        return null;
    }

    String getLineFromRandWords(String fileName, int lineCnt)throws Exception
    { 
        String line="";

        for (int i=0;i<properties.wordCnt;i++) {
            int r=rand.nextInt(lineCnt);
            String word=getFileLineByNum(fileName, r);
            line+=word+" ";
        }

        return line.trim();
    }

    public  void newInit()throws Exception
    {
        String fileName="C:\\Work\\G\\prj\\PUKA\\dict.txt";
        BufferedReader  br = new BufferedReader(new FileReader(fileName));
        int n = getFileLineCount(fileName);

        String value="";
        String key="";

        for (int i = 0; i < properties.lineCnt; i++) {
            //value=getRandStringByWordCount(properties.wordCnt);
            value=getLineFromRandWords(fileName,n);

            if (items.containsValue(value))continue;

            key=getKey(value);

            items.put(key , value );
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

    String getRandStringByWordCount(int wordCnt)
    {
        String str="";

        for (int i=0;i<wordCnt;i++) {
            for (int n=0;n<properties.wordLength;n++) {
                str+=getRandCharAsString();              
            }
            str+=" ";
        }
        return str;
    }

    public  void showByKey( )throws Exception
    {  
        Set keySet = items.keySet();
        for (String key : items.keySet()) {
            System.out.println(key+":"+ items.get(key));
        }
    }

    public  void show( )throws Exception
    {  
        int n=0;
        for (Map.Entry<String, String> entry : items.entrySet()) {
            System.out.println(n++ + ": " + entry.getKey()+":"+ entry.getValue());
        }
    }

    public  Vector<String> getValuesByFragment(String fragment )throws Exception
    {  
        Vector <String> value=new Vector();
        String tempStr="";

        for (Map.Entry<String, String> entry : items.entrySet()) {

            tempStr=entry.getValue().toLowerCase();
            if (tempStr.contains(fragment.toLowerCase())) {
                value.add(tempStr);
            }
        }
        return value;       
    }

    public  String getValueByKey(String key )throws Exception
    {  
        return items.get(key);
    }

    public  void setValueByKey(String key, String value )throws Exception
    {  
        items.put(key, value);
    }

    public void makeDictionary()throws Exception
    {
        String path="C:\\temp\\scowl\\final";

        File file= new File(path);
        BufferedReader br=null;
        File files[]=file.listFiles();

        TreeSet<String> set= new TreeSet();

        for (File fil : files) {
            // System.out.println("File="+fil.getName());

            try {
                br = new BufferedReader(new FileReader(fil));
                String line;
                while ((line = br.readLine()) != null) {
                    //   System.out.println(line);
                    set.add(line.trim());
                }
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                try {
                    if (br != null) {
                        br.close();
                    }
                } catch (IOException ex) {
                    ex.printStackTrace();
                }
            }
        }

        File newFile= new File("C:\\Work\\G\\prj\\PUKA\\dict.txt");
        BufferedWriter bw = new BufferedWriter(new FileWriter(newFile));

        for (String str : set) {
            //   System.out.println(str);
            bw.write(str+"\n");
        }
        bw.flush();
        bw.close();
    }
//**************************************

    public static void main(String args[])throws Exception {

        Puka3 puka = new Puka3();

        int length=100;

        puka.properties.wordLength=length;
        puka.properties.wordCnt=length;  
        puka.properties.lineCnt= (int) Math.pow(length, 2);  
        puka.properties.keyLength=length;

        boolean save=true;

        if (save==false) {
            puka.items=readXML(puka.storeFile);
        } else {
            puka.newInit();
            puka.writeXML(puka.items, puka.storeFile);
        }

        //    puka.show();

        System.out.println();

        // puka.showByKey();
        String fragment = "ratha";
        String key="ZSCIRFUPVM";

        System.out.println("value by fragment of "+fragment+"=>");
        Vector<String> value=puka.getValuesByFragment(fragment);

        for (String str:value) {
            System.out.println(str+"|");

        }
/*

        System.out.println("value by key of "+key+"="+;
puka.findValueByKey(key));
     //   puka.makeDictionary();
*/

    }
}











