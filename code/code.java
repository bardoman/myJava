import java.io.*;
import java.util.*;
import java.nio.charset.StandardCharsets;

public class Code {
    char byteMap[][];

    void printMap(char[][] byteMap)
    {

        System.out.println("*********************************************");
        for (char row[]:byteMap)
        // for (byte col:row)
        {
            // String str = new String(row, StandardCharsets.UTF_8);
            String str = new String(row);
            System.out.println(str); 
            // System.out.println(row); 
        }
        System.out.println("*********************************************");
    }
    void putCol(int colNum, char row[])
    {
        int rowSize=row.length;

        // byte byteMap[][]=new byte[rowSize][rowSize];
        int n=0;
        for (char item:row) {
            byteMap[n][colNum]=item;
            n++;

        }

    }
    public static void main(String args[]) throws IOException  {
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
            // byte[] bytes = buffReader.readLine().getBytes(StandardCharsets.UTF_8);
            if (firstPass) {
                myCode.byteMap=new char[bytes.length][bytes.length];
                firstPass=false;
            }
            myCode.putCol(n,bytes);
            n++;    
        }
        myCode.printMap(myCode.byteMap);
    }
}




