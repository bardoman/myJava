import java.io.*;
import java.util.*;
import java.nio.charset.StandardCharsets;

public class Code {
    char byteMap[][];
    int alphaSize =36;
    char alphaMap[][]=new char[alphaSize][alphaSize];
    String alphaMapFileName="AlphaMap.txt";
    int cyclicKeyIndex=0;
    String cyclicKey="U90N4BWUP7W174NU91Y571CK";
    char[] baseSet = {'0','1','2','3','4','5','6','7','8','9','a','b','c','d','e','f','g','h','i','j','k','l','m','n','o','p','q','r','s','t','u','v','w','x','y','z'};

    // Implementing Fisher–Yates shuffle
    char [] getShuffleCharArray()
    {
        char options[]=new char[alphaSize];
        options = Arrays.copyOf(baseSet, alphaSize);
        Random rnd = new Random();
        for (int i = alphaSize - 1; i > 0; i--) {
            int index = rnd.nextInt(i + 1);
            // Simple swap
            char a = options[index];
            options[index] = options[i];
            options[i] = a;
        }
        return options;
    }
/*
    char [] getRandCharArray()
    {   
        char options[]=new char[alphaSize];
        options = Arrays.copyOf(baseSet, alphaSize);
        char[] result = new char[alphaSize];
        Random r=new Random();
        for (int i=0;i<result.length;i++) {
            result[i]=options[r.nextInt(options.length)];
        }
        return result;
    }
*/
    void saveAlphaMap()throws Exception{
        File tempFile=new File(alphaMapFileName);
        if (!tempFile.exists()) {

            FileWriter fileWr=new FileWriter(tempFile);
            BufferedWriter buffWr = new BufferedWriter(fileWr); 

            for (char ch[]:alphaMap) {
                buffWr.write(ch);
                buffWr.write(System.getProperty( "line.separator" ));
            }
            buffWr.close();   
        }

    }

    void readAlphaMap()throws Exception{
        File tempFile=new File(alphaMapFileName);
        if (tempFile.exists()) {

            FileReader fileRd=new FileReader(tempFile);
            BufferedReader buffRd = new BufferedReader(fileRd); 

            int n=0;
            while (buffRd.ready()) {
                String str=buffRd.readLine();
                //    System.out.println(str);
                alphaMap[n]  = new String(str.getBytes(StandardCharsets.UTF_8)).toCharArray();
                n++;
            }
            buffRd.close();   
        }

    }

    void setupAlphaMap()throws Exception
    {
        File tempFile=new File(alphaMapFileName);
        if (!tempFile.exists()) {

            char tmpBytes[]= new char[alphaSize];

            for (int c=0;c<alphaSize;c++) {
                // tmpBytes=getRandCharArray();
                tmpBytes=getShuffleCharArray();
                alphaMap[c]= tmpBytes;
                int n=0;
            }
            saveAlphaMap();

        } else {
            readAlphaMap();
        }
    }

    void printMap(char[][] byteMap)
    {

        System.out.println("*****************start****************************");
        for (char row[]:byteMap) {
            String str = new String(row);
            System.out.println(str); 
        }
        System.out.println("******************end***************************");
    }

    char [] reverse(char inAray[])
    {
        char outAray[] = new char[inAray.length];

        int n=inAray.length-1;
        for (char ch:inAray) {
            outAray[n]=ch;
            n--;
        }

        return outAray;
    }


    void putCol(int colNum, char row[])
    {
        int rowSize=row.length;
        int n=0;
        for (char item:row) {
            byteMap[n][colNum]=item;
            n++;
        }
    }
/*
    char [] getCurrentAlphaRow()
    {  
     //   char ch=cyclicKey.indexOf[cyclicKeyIndex];
     //   int rowResult= Arrays.binarySearch(baseSet, ch);

      //  alphaMap[rowResult



    }*/

    char encodeChar(char ch)
    {

        char tmpRow[]=baseSet;
        int colResult= Arrays.binarySearch(tmpRow, ch);

        //ch= alphaMap[colResult,];
        return ch;

    }

    char [] encodeRow(char inRow[])
    {
        char tmpRow[]=new char[inRow.length];
        int n=0;
        for (char ch:inRow) {
            tmpRow[n]=encodeChar(ch);
            n++;
        }
        return tmpRow;
    }

    char [][] encodeMap(char inMap[][])
    {
        int n=0;
        char tmpMap[][]=new char[inMap.length][inMap.length];
        for (char row[]:inMap) {
            tmpMap[n]=encodeRow(row);
            n++;
            System.out.println(row); 
        }
        return tmpMap;
    }

    public static void main(String args[]) throws Exception  {
        boolean firstPass=true;
        Code myCode=new Code();
        FileReader fileReader=new FileReader(args[0]);
        BufferedReader buffReader = new BufferedReader(fileReader); 

        int n=0;
        while (buffReader.ready()) {
            String str=buffReader.readLine();
            System.out.println(str);
            char[] bytes = new String(str.getBytes(StandardCharsets.UTF_8)).toCharArray();
            System.out.println(bytes);
            if (firstPass) {
                myCode.byteMap=new char[bytes.length][bytes.length];
                firstPass=false;
            }

            myCode.putCol(n,myCode.reverse(bytes));
            n++;    
        }
        myCode.printMap(myCode.byteMap);

        myCode.setupAlphaMap();

        myCode.printMap(myCode.alphaMap);

    }
}




