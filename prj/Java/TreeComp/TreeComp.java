
import java.io.*;
import java.util.*;

public class TreeComp {

    ArrayList<File> dirTree1 = new ArrayList();
    ArrayList<File> dirTree2 = new ArrayList();
    ArrayList<String> badDirs = new ArrayList();
    String MISSMATCH = "***MissMatch***";
    File root1;
    File root2;
    public TreeComp(String dir1, String dir2) {
        try {
            root1=new File(dir1);
            root2=new File(dir2);

            buildDirTreeList(dirTree1, root1);

            buildDirTreeList(dirTree2, root2);

            compTreeDirs(dirTree1,dirTree2);

            compTreeFiles(dirTree1,dirTree2);

        }
        catch(Exception ex) {
            ex.printStackTrace();
        }
    }

    public void compTreeDirs(List<File> lst1, List<File> lst2) {

        for( File fil:lst1) {

            if(locInList(lst2,fil)==-1) {
                System.out.println(MISSMATCH+" on dir "+fil );
                badDirs.add(fil.getPath());
            }
        }

        for( File fil:lst2) {

            if(locInList(lst1,fil)==-1) {
                System.out.println(MISSMATCH+" on dir "+fil );
                badDirs.add(fil.getPath());
            }
        }
    }

    private int locInList(List<File> lst, File file) {
        String tempPath1=clipPath(file.getPath());
        String tempPath2="";
        File tempFile;
        int loc=-1;

        for(int i=0;i!=lst.size();i++) {

            tempFile=lst.get(i);

            tempPath2=clipPath(tempFile.getPath());

            if(tempPath2.equals(tempPath1)) {
                return i;
            }
        }

        return loc;
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

    public void compDirFiles(List<File> fils1, List<File> fils2) {

        for( File fil:fils1) {

            if(!fil.isDirectory()) {
                if(locInList(fils2,fil)==-1) {

                    System.out.println(MISSMATCH+" on file "+fil );
                }
            }
        }

        for( File fil:dirTree2) {

            if(!fil.isDirectory()) {
                if(locInList(fils1,fil)!=-1) {

                    System.out.println(MISSMATCH+" on file "+fil );
                }
            }
        }
    }

    public void compTreeFiles(List<File> dirTree1, List<File> dirTree2) {
        int loc;

        for( File dir:dirTree1) {
            if((!badDirs.contains(dir.getPath()))|dir.getPath().equals(root1.getPath())) {

                if((loc=locInList(dirTree2,dir))!=-1) {

                    List<File> fils1 =Arrays.asList(dir.listFiles());

                    File tempDir = dirTree2.get(loc);

                    List<File> fils2 =Arrays.asList(tempDir.listFiles());

                    compDirFiles(fils1,fils2);

                }
            }
        }

        for( File dir:dirTree2) {
            if((!badDirs.contains(dir.getPath()))|dir.getPath().equals(root2.getPath())) {

                if((loc=locInList(dirTree1,dir))!=-1) {

                    List<File> fils1 =Arrays.asList(dir.listFiles());

                    File tempDir = dirTree1.get(loc);

                    List<File> fils2 =Arrays.asList(tempDir.listFiles());

                    compDirFiles(fils1,fils2);

                }
            }
        }
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


