
import java.io.*;
import java.util.*;


//----------------------------------------------------------------

public class TreeComp {

    ArrayList fileVect1 = new ArrayList();
    ArrayList fileVect2 = new ArrayList();
    File file1;
    File file2;
    String MISSMATCH = "***MissMatch***";

    public TreeComp(String dir1, String dir2) {

        try {

            buildDirTreeList(fileVect1, new File(dir1));

            buildDirTreeList(fileVect2, new File(dir2));

            int MissMatchCount=0;

            for(int i=0;i!=fileVect1.size();i++) {

                file1= (File)fileVect1.get(i);
                file2= (File)fileVect2.get(i);

                compDir(file1,file2);
                /*
                if( !path1.equals(path2)) {
                    System.out.println(MISSMATCH);
                    System.out.println("Target File1:");
                    System.out.println(((File)fileVect1.get(i)).getAbsolutePath());
                    System.out.println("Does not match.");
                    System.out.println("Target File2:");
                    System.out.println(((File)fileVect2.get(i)).getAbsolutePath());
                    MissMatchCount++;
                } */
            }

            //System.out.println("TreeComp complete, MissMatchCount="+MissMatchCount);
        }
        catch(Exception ex) {
            ex.printStackTrace();
        }





    }

    public void buildDirTreeList(ArrayList vect, File file) {

        if(file.isDirectory()) {
            vect.add(file);

            File children[] = file.listFiles();

            for(int i=0; i<children.length; i++) {
                buildDirTreeList(vect, children[i]);
            }
        }
    }

    public void compDir(File dir1, File dir2) {

        List names1 = getNamesinDir(dir1);

        List names2 = getNamesinDir(dir2);

        //System.out.println("compDir("+dir1+","+dir2+")");


        for(Iterator iter = names1.iterator();iter.hasNext();) {
            String name = (String) iter.next();

            if(names2.indexOf(name)==-1) {

                System.out.println(dir1+"\\"+name+" does not exist in=>"+dir2);
            }
        }

        for(Iterator iter = names2.iterator();iter.hasNext();) {
            String name = (String) iter.next();

            if(names1.indexOf(name)==-1) {

                System.out.println(dir2+"\\"+name+" does not exist in=>"+dir1);
            }
        }

        /*        if(!child1.containsAll(child2)) {

            System.out.println(MISSMATCH);

            System.out.println(dir1);
        }

        if(!child2.containsAll(child1)) {
            System.out.println(MISSMATCH);

            System.out.println(dir2);
        }

                */

        //System.out.println("**********end comp***********");


    }


    public List getNamesinDir(File dir) {
        Vector names= new Vector();

        File files[] = dir.listFiles();

        for(int i=0;i!=files.length;i++) {
            names.add(files[i].getName());
        }

        return names;
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


