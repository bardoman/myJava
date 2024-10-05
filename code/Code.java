import java.io.*;
import java.util.*;
import java.nio.charset.StandardCharsets;

public class Code {
    char byteMap[][];
    char encodedMap[][];
    char decodeMap[][];
    int alphaSize =36;
    char alphaMap[][]=new char[alphaSize][alphaSize];
    String alphaMapFileName="AlphaMap.txt";
    int cyclicKeyIndex=0;
    String cyclicKey="SIMPLEKEY";
    char[] baseSet = {'0','1','2','3','4','5','6','7','8','9','A','B','C','D','E','F','G','H','I','J','K','L','M','N','O','P','Q','R','S','T','U','V','W','X','Y','Z'};

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

    char [] getCurrentAlphaRow()
    {  
        char ch=cyclicKey.charAt(cyclicKeyIndex);
        int rowIndex= Arrays.binarySearch(baseSet, ch);

        if (cyclicKeyIndex==cyclicKey.length()-1) {
            cyclicKeyIndex=0;
        } else cyclicKeyIndex++;

        return alphaMap[rowIndex];
    }

    char encodeChar(char ch)
    {
        int colIndex= Arrays.binarySearch(baseSet, ch);

        char alphaRow[]=getCurrentAlphaRow();

        char encodedChar= alphaRow[colIndex];

        return encodedChar;
    }

    char decodeChar(char ch)
    {
        int colIndex= Arrays.binarySearch(baseSet, ch);

        char alphaRow[]=getCurrentAlphaRow();

        char encodedChar= alphaRow[colIndex];

        return encodedChar;
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

    char [] decodeRow(char inRow[])
    {
        char tmpRow[]=new char[inRow.length];
        int n=0;
        for (char ch:inRow) {
            tmpRow[n]=decodeChar(ch);
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
        }
        return tmpMap;
    }

    char [][] decodeMap(char inMap[][])
    {
        int n=0;
        char tmpMap[][]=new char[inMap.length][inMap.length];
        for (char row[]:inMap) {
            tmpMap[n]=decodeRow(row);
            n++;
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

        myCode.encodedMap=myCode.encodeMap(myCode.byteMap);

        myCode.printMap(myCode.encodedMap);
    }
}




