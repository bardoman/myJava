import java.io.*;
import java.util.*;
import java.nio.charset.StandardCharsets;
import java.lang.Math.*;

public class Code {
    char inputMap[][];
    char transMap[][];
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

    void printMap(char[][] transMap,String mapName)
    {
        System.out.println("*****************start****************************");

        System.out.println(mapName);
        for (char row[]:transMap) {
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
            transMap[n][colNum]=item;
            n++;
        }
        int c=0;
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
        char alphaRow[]=getCurrentAlphaRow();

        String tmpStr=new String(alphaRow);
        int colIndex=tmpStr.indexOf(ch);

        // int colIndex= Arrays.binarySearch(alphaRow, ch);

        char decodedChar= baseSet[colIndex];//needs debug

        return decodedChar;
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

    char [][] readMapFromFile(String inFileName)throws Exception
    {

        FileReader fileReader=new FileReader(inFileName);
        BufferedReader buffReader = new BufferedReader(fileReader); 

        int n=0;
        boolean firstPass=true;

        while (buffReader.ready()) {
            String str=buffReader.readLine();
            if (firstPass) {
                inputMap=new char[str.length()][str.length()];
                firstPass=false;
            }
            inputMap[n]= new String(str.getBytes(StandardCharsets.UTF_8)).toCharArray();
            if (n<str.length()-1) {
                n++; 
            } else break;

        }
        return inputMap;
    }
    void translateMap(char inputMap[][])
    {

        int n=0;
        boolean firstPass=true;
        for (char ch[]:inputMap) {
            if (firstPass) {
                transMap=new char [ch.length][ch.length]; 
                firstPass=false;
            }
            //  byte bytes[]=new char[bytes.length][bytes.length];
            putCol(n,reverse(ch));
            if (n<ch.length-1) {
                n++; 
            } else break;
        }
    }

    public static void main(String args[]) throws Exception  {
        Code myCode=new Code();
        myCode.inputMap=myCode.readMapFromFile(args[0]);
        myCode.printMap(myCode.inputMap,"inputMap");

        myCode.translateMap(myCode.inputMap);

        myCode.printMap(myCode.transMap,"transMap");

        myCode.setupAlphaMap();
        myCode.printMap(myCode.alphaMap,"alphaMap");

        myCode.encodedMap=myCode.encodeMap(myCode.transMap);
        myCode.printMap(myCode.encodedMap,"encodedMap");

        myCode.decodeMap=myCode.decodeMap(myCode.encodedMap);
        myCode.printMap(myCode.decodeMap,"decodedMap");
    }
}




