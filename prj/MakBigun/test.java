import java.io.FileWriter;
import java.io.*;
import java.lang.*;
import java.util.*;

public class test
{

    public static void main(String args[]) {

        try
        {
            char buf[]= new char[15000];

            int n=0;

            FileReader rd = new java.io.FileReader("seed.asm");

            int cnt = rd.read(buf);

            for (int i=0;i!=5000;i++)
            {
                FileWriter wr = new FileWriter("TST"+String.valueOf(i)+"asm" );

                wr.write(buf,0,cnt);

                wr.close();
            }

            rd.close();
        }
        catch(Exception e)
        {
            e.printStackTrace();
        }

    }
}
