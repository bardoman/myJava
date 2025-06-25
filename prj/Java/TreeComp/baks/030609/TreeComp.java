
import java.io.*;
import java.util.*;


//----------------------------------------------------------------

public class TreeComp {

    ArrayList<File> dirTree1 = new ArrayList();
    ArrayList<File> dirTree2 = new ArrayList();
    ArrayList<File> badDirs = new ArrayList();

    File file1;
    File file2;
    String MISSMATCH = "***MissMatch***";

    public TreeComp(String dir1, String dir2) {

        try {

            buildDirTreeList(dirTree1, new File(dir1));

            buildDirTreeList(dirTree2, new File(dir2));

            compTree(dirTree1,dirTree2);

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
    public void compTree(List<File> lst1, List<File> lst2) {
        for( File fil:lst1) {

            if(!isInList(lst2,fil)) {
                System.out.println(MISSMATCH+" on dir "+fil );
                badDirs.add(fil);
            }
        }

        for( File fil:lst2) {

            if(!isInList(lst1,fil)) {
                System.out.println(MISSMATCH+" on dir "+fil );
                badDirs.add(fil);
            }
        }


    }

    private boolean isInList(List<File> lst, File file) {
        String tempPath1=clipPath(file.getPath());
        String tempPath2="";
        boolean flag=false;

        for(File fil: lst) {

            tempPath2=clipPath(fil.getPath());

            if(tempPath2.equals(tempPath1)) {
                return true;
            }
        }

        return flag;
    }


    private String clipPath(String path) {
        String slash = System.getProperty("file.separator");

        int loc = path.indexOf(slash);

        if(loc!=-1) {

            path=path.substring(loc);
        }

        return path;
    }

    public void buildDirTreeList(List<File> dirTree, File file) {

        if(file.isDirectory()) {
            dirTree.add(file);

            File children[] = file.listFiles();

            for(int i=0; i<children.length; i++) {
                buildDirTreeList(dirTree, children[i]);
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
            System.out.println("TreeComp compares two directory trees.");
            System.out.println("Usage: TreeComp <dir1>  <dir2> ");
        }
        else {
            new TreeComp(args[0], args[1]);
        }
    }
}


