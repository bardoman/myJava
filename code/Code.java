import java.io.*;
import java.util.*;
import java.nio.charset.StandardCharsets;
import java.lang.Math.*;

public class Code
{
    int alphaSize =36;
    char inputMap[][]=new char[alphaSize][alphaSize];;
    char transMap[][]=new char[alphaSize][alphaSize];;
    char encodedMap[][]=new char[alphaSize][alphaSize];;
    char decodeMap[][]=new char[alphaSize][alphaSize];;  
    char alphaMap[][]=new char[alphaSize][alphaSize];
    String alphaMapFileName="AlphaMap.txt";
    int cyclicKeyIndex=0;
    //String cyclicKey="SIMPLEKEYZXCVBNM";
    String cyclicKey="BECOMEWHATYOUARE";

    char[] baseSet = {'0','1','2','3','4','5','6','7','8','9','A','B','C','D','E','F','G','H','I','J','K','L','M','N','O','P','Q','R','S','T','U','V','W','X','Y','Z'};

    // Implementing Fisher–Yates shuffle
    char [] getShuffleRow()
    {
        char tmpRow[]=new char[alphaSize];
        tmpRow = Arrays.copyOf(baseSet, alphaSize);
        Random rnd = new Random();
        for (int i = alphaSize - 1; i > 0; i--)
        {
            int index = rnd.nextInt(i + 1);
            // Simple swap
            char a = tmpRow[index];
            tmpRow[index] = tmpRow[i];
            tmpRow[i] = a;
        }
        return tmpRow;
    }

    void saveAlphaMap()throws Exception{
        File tempFile=new File(alphaMapFileName);
        if (!tempFile.exists())
        {

            FileWriter fileWr=new FileWriter(tempFile);
            BufferedWriter buffWr = new BufferedWriter(fileWr); 

            for (char ch[]:alphaMap)
            {
                buffWr.write(ch);
                buffWr.write(System.getProperty( "line.separator" ));
            }
            buffWr.close();   
        }
    }

    void readAlphaMap()throws Exception{
        File tempFile=new File(alphaMapFileName);
        if (tempFile.exists())
        {
            FileReader fileRd=new FileReader(tempFile);
            BufferedReader buffRd = new BufferedReader(fileRd); 

            int n=0;
            while (buffRd.ready())
            {
                String str=buffRd.readLine();
                alphaMap[n]  = new String(str.getBytes(StandardCharsets.UTF_8)).toCharArray();
                n++;
            }
            buffRd.close();   
        }
    }

    void setupAlphaMap()throws Exception
    {
        File tempFile=new File(alphaMapFileName);
        if (!tempFile.exists())
        {
            for (int c=0;c<alphaSize;c++)
            {
                alphaMap[c]=getShuffleRow();
                int n=0;
            }
            saveAlphaMap();

        } else
        {
            readAlphaMap();
        }
    }

    void printMap(char[][] inMap,String mapName)
    {
        System.out.println("*****************start****************************");

        System.out.println(mapName);
        for (char row[]:inMap)
        {
            String str = new String(row);
            System.out.println(str); 
        }
        System.out.println("******************end***************************");
    }

    char [] getCurrentAlphaRow()
    {  
        char ch=cyclicKey.charAt(cyclicKeyIndex);
        int rowIndex= Arrays.binarySearch(baseSet, ch);

        if (cyclicKeyIndex==cyclicKey.length()-1)
        {
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

        char decodedChar= baseSet[colIndex];

        return decodedChar;
    }

    char [] encodeRow(char inRow[])
    {
        char tmpRow[]=new char[inRow.length];
        int n=0;
        for (char ch:inRow)
        {
            tmpRow[n]=encodeChar(ch);
            n++;
        }
        return tmpRow;
    }

    char [] decodeRow(char inRow[])
    {
        char tmpRow[]=new char[inRow.length];
        int n=0;
        for (char ch:inRow)
        {
            tmpRow[n]=decodeChar(ch);
            n++;
        }
        return tmpRow;
    }

    char [][] encodeMap(char inMap[][])
    {
        int n=0;
        char tmpMap[][]=new char[inMap.length][inMap.length];
        for (char row[]:inMap)
        {
            tmpMap[n]=encodeRow(row);
            n++;
        }
        return tmpMap;
    }

    char [][] decodeMap(char inMap[][])
    {
        int n=0;
        char tmpMap[][]=new char[inMap.length][inMap.length];
        for (char row[]:inMap)
        {
            tmpMap[n]=decodeRow(row);
            n++;
        }
        return tmpMap;
    }

    String washRow(String inRow)
    {
        String tmpStr="";
        tmpStr= inRow.toUpperCase();
        String tmpStr2="";

        for (int i=0;i<tmpStr.length();i++)
        {
            char ch=tmpStr.charAt(i);
            int rowIndex= Arrays.binarySearch(baseSet,ch );
            if (rowIndex <0 )
            {
                continue;
            } else
            {
                tmpStr2=tmpStr2+ch;
            }
        }
        return tmpStr2;
    }

    char [][] readMapFromFile(String inFileName)throws Exception
    {
        File tstFile=new File(inFileName);
        if (!tstFile.exists())
        {
            System.out.println("Input file does not exist");
            System.exit(1);
        }
        FileReader fileReader=new FileReader(inFileName);
        BufferedReader buffReader = new BufferedReader(fileReader); 

        int n=0;
        boolean firstPass=true;
        String tmpStr="";

        while (buffReader.ready())
        {
            String str=buffReader.readLine();
            if (!str.isEmpty())
            {
                str=washRow(str);
                tmpStr+=str;
            } else continue;

//            inputMap[n]= new String(str.getBytes(StandardCharsets.UTF_8)).toCharArray();
            if (n<str.length()-1)
            {
                n++; 
            } else break;
        }

        int strSize=tmpStr.length();

        int i=0;
        for (int col=0;col<alphaSize;col++)
            for (int row=0;row<alphaSize;row++)
            {
                inputMap[col][row]=tmpStr.charAt(i);

                if (i==strSize-1)i=0;
                else i++;             
            }

        return inputMap;
    }

    char [][] translateMap(char inputMap[][])
    {
        int size = alphaSize;
        char tmpMap[][]=new char [alphaSize][alphaSize];
        int tmpRowCnt=0;
        int tmpColCnt=0;
        char ch;
        for (int inRowCnt=0;inRowCnt<size;inRowCnt++)
            for (int inColCnt=0;inColCnt<size;inColCnt++)
            {
                ch =inputMap[inRowCnt][inColCnt];
                tmpMap[size-inColCnt-1][size-inRowCnt-1]=ch;
            }
        return tmpMap;
    }

    public static void main(String args[]) throws Exception  {
        Code myCode=new Code();
        myCode.inputMap=myCode.readMapFromFile(args[0]);
        myCode.printMap(myCode.inputMap,"inputMap");

        myCode.transMap=myCode.translateMap(myCode.inputMap);
        myCode.printMap(myCode.transMap,"transMap");

        myCode.setupAlphaMap();
        myCode.printMap(myCode.alphaMap,"alphaMap");

        myCode.encodedMap=myCode.encodeMap(myCode.transMap);
        myCode.printMap(myCode.encodedMap,"encodedMap");

        myCode.decodeMap=myCode.decodeMap(myCode.encodedMap);
        myCode.printMap(myCode.decodeMap,"decodedMap");

        myCode.inputMap = myCode.translateMap(myCode.decodeMap);
        myCode.printMap(myCode.inputMap,"inputMap");
    }
}




