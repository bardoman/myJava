
import java.io.*;
import java.util.*;

//----------------------------------------------------------------

public class TreeComp {

    ArrayList fileVect1 = new ArrayList();
    ArrayList fileVect2 = new ArrayList();
    String path1="";
    String path2="";

    public TreeComp(String dir1, String dir2) {

        try {

            traverse(fileVect1, new File(dir1));

            traverse(fileVect2, new File(dir2));

            int MissMatchCount=0;

            for(int i=0;i!=fileVect1.size();i++) {

                path1= ((File)fileVect1.get(i)).getName();

                path2= ((File)fileVect2.get(i)).getName();


                if( !path1.equals(path2)) {
                    System.out.println("***MissMatch***");
                    System.out.println("Target File1:");
                    System.out.println(((File)fileVect1.get(i)).getAbsolutePath());
                    System.out.println("Does not match.");
                    System.out.println("Target File2:");
                    System.out.println(((File)fileVect2.get(i)).getAbsolutePath());
                    MissMatchCount++;
                }
            }

            System.out.println("TreeComp complete, MissMatchCount="+MissMatchCount);
        }
        catch(Exception ex) {
            ex.printStackTrace();
        }





    }

    private  void traverse(ArrayList vect, File file) {

        vect.add(file);

        if(file.isDirectory()) {
            String[] children = file.list();
            for(int i=0; i<children.length; i++) {
                traverse(vect, new File(file, children[i]));
            }
        }
    }


    public static void main(String[] args) {
        if(args.length !=2) {
            System.out.println("TreeComp compares two directory trees.\n+"+
                               "Usage: TreeComp <dir1> <dir2>");
        }
        else {
            new TreeComp(args[0], args[1]);
        }
    }
}


