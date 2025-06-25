
import java.io.*;
import java.util.*;


//----------------------------------------------------------------

public class TreeComp {

    ArrayList<String> fileVect1 = new ArrayList();
    ArrayList<String> fileVect2 = new ArrayList();
    File file1;
    File file2;
    String MISSMATCH = "***MissMatch***";
    String dir1;
    String dir2;
   
    public TreeComp(String dir1, String dir2) {
          this.dir1=dir1;
          this.dir2=dir2;

        try {

            buildDirTreeList(fileVect1, new File(dir1));

            buildDirTreeList(fileVect2, new File(dir2));

            compTree(fileVect1,fileVect2);

            int MissMatchCount=0;

            /*
            for(int i=0;i!=fileVect1.size();i++) {

                file1= (File)fileVect1.get(i);
                file2= (File)fileVect2.get(i);

                compDir(file1,file2);
            }
            */
        }
        catch(Exception ex) {
            ex.printStackTrace();
        }





    }
    public void compTree(List<String> lst1, List<String> lst2) {
        for( String fil:lst1) {

            if(!lst2.contains(fil)) {
                System.out.println(MISSMATCH+" on dir "+fil );
            }
        }

        for( String fil:lst2) {

            if(!lst1.contains(fil)) {
                System.out.println(MISSMATCH+" on dir "+fil );
            }
        }


    }


    private String clipPath(String path) {
        String slash = System.getProperty("file.separator");

        int loc = path.indexOf(slash);

        if(loc!=-1) {

            path=path.substring(loc);
        }

        return path;

    }

    public void buildDirTreeList(ArrayList<F> vect, File file) {

        if(file.isDirectory()) {
            vect.add(clipPath(file.getPath()));

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
        if(args.length !=4) {
            System.out.println("TreeComp compares two directory trees.\n+"+
                               "Usage: TreeComp <dir1>  <dir2> ";
                               

        }
        else {
            new TreeComp(args[0], args[1]);
        }
    }
}


