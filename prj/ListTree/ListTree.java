
import java.io.*;
import java.util.*;

//----------------------------------------------------------------

public class ListTree {

    Vector fileVect = new Vector();
    String extension="";

    public ListTree(String dir, String extension) {

        this.extension="."+extension;

        try {
            String total="";

            traverse(new File(dir));

            for(int i=0;i!=fileVect.size();i++) {
                System.out.println(((File)fileVect.get(i)).getAbsolutePath());
            }
        }
        catch(Exception ex) {
            ex.printStackTrace();
        }





    }

    private  void processDir(File file) {

        if(file.isFile()) {
            if((file.getName().endsWith(extension))|(file.getName().endsWith(extension.toUpperCase()))) {
                fileVect.add(file);
            }
        }

    }

    private  void traverse(File dir) {

        processDir(dir);

        if(dir.isDirectory()) {
            String[] children = dir.list();
            for(int i=0; i<children.length; i++) {
                traverse(new File(dir, children[i]));
            }
        }

    }


    public static void main(String[] args) {
        if(args.length !=2) {
            System.out.println("ListTree traverses a sub directory tree.\n"+
                               "Filepaths that match the extention parm are output.\n"+
                               "Usage: ListTree <dir> <extention>");

        }
        else {
           // new ListTree(args[0], args[1]);
           new ListTree("/home/bardoman/prj", "java");
        }
    }
}

