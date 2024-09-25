import java.io.*;
import java.util.*;

public class code {
    public static void main(String args[]) throws IOException  {

        FileReader fileReader=new FileReader(args[0]);
        BufferedReader buffReader = new BufferedReader(fileReader); 

        ArrayList myAray=new ArrayList();

        while (buffReader.ready()) {
            myAray.add(buffReader.readLine());
        }

        for (Object obj:myAray) {
            System.out.println((String)obj); 
        }

    }
}




