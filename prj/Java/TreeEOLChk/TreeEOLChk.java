
import java.io.*;
import java.util.*;


//----------------------------------------------------------------

public class TreeEOLChk {
    int BUFFER_SIZE = 0xffff;

    byte buffer[] = new byte[BUFFER_SIZE];
    byte EOLValue=0x0D;
    File file;

    public TreeEOLChk(String dir) {

        try {

            traverse(new File(dir));
        }
        catch(Exception ex) {
            ex.printStackTrace();
        }
    }

    void traverse( File file)throws Exception{

        if(file.isDirectory()) {
            String[] children = file.list();
            for(int i=0; i<children.length; i++) {
                traverse(new File(file, children[i]));
            }
        }
        else {
            findEOL(file);
        }
    }

    void findEOL(File file) throws Exception{

        DataInputStream instrm = new DataInputStream(new  FileInputStream( file));

        long size = file.length();//size in bytes

        int transferSize=0;

        if(size <= BUFFER_SIZE-1) {
            transferSize = (int) size;
        }
        else {
            transferSize =BUFFER_SIZE-1;
        }

        try {

            instrm.readFully(buffer,0,transferSize); 
        }
        catch(EOFException eofe) {

        }

        for(int n=0;n!=transferSize;n++) {

            if(buffer[n]==EOLValue) {
                System.out.println(EOLValue+" found in=>"+file.getAbsolutePath());
                return;
            }
        }

    }

    public static void main(String[] args) {
        if(args.length !=1) {
            System.out.println("TreeEOLChk checks a directory tree to find 0x0D (CR).\n+"+
                               "Usage: TreeEOLChk <dir>");
        }
        else {
            new TreeEOLChk(args[0]);
        }
    }
}


