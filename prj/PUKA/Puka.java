import java.beans.*;
import java.io.*;
import java.io.File;
import java.util.*;

import com.thoughtworks.xstream.*;
import com.thoughtworks.xstream.io.xml.*;

public class Puka {

    public Random rand = new Random(new Date().getTime());

    String storeFile = "storageFile.xml";

    XStream xstream = new XStream(new StaxDriver());

    public Branch tree = new Branch("root",true);

    public class Branch implements Serializable {
        static final long serialVersionUID = 1111111111111111L;
        private TreeMap<String,Branch> branches= new TreeMap<String,Branch>();
        private TreeMap<String,String> leaves= new TreeMap<String,String>();
        private String name="";
        boolean isBase=false;

        public Branch(String name)
        {
            this.name=name;
        }
        public Branch( String name, boolean isBase)
        {
            this.name=name;
            this.isBase=isBase;
        }
        public boolean isBase()
        {
            return isBase;
        }
        public String getName()
        {
            return this.name;
        }
        public void SetName(String name)
        {
            this.name=name;
        }
        public Branch addBranch(String key)throws Exception
        {
            if (!branches.containsKey(key)) {
                Branch tempBranch=new Branch(key);
                branches.put(key,tempBranch ); 
                return tempBranch;
            } else throw new Exception("Branch key exists");
        }
        public Branch addBranch(String path, String key)throws Exception
        {
            Branch temp = findBranch(path);
            return temp.addBranch(key);
        }

        public void putBranch(Branch branch)throws Exception
        {
            if (!branches.containsKey(branch.getName())) {
                branches.put(branch.getName(), branch);
            } else throw new Exception("Branch name exists");
        }
        public void putBranch(String path, Branch branch)throws Exception
        {
            Branch temp = findBranch(path);
            temp.putBranch(branch);
        }

        public void delBranch(String key)
        {
            branches.remove(key);
        }

        public void delBranch(String path, String key)throws Exception
        {
            Branch temp = findBranch(path);
            temp.delBranch(key);
        }

        public Branch getBranch(String key)
        {
            return branches.get(key);
        }
        public Branch getBranch(String path, String key)throws Exception
        {
            Branch temp = findBranch(path);
            return temp.getBranch(key);
        }

        public boolean isLegalFormat(String path)throws Exception
        {
            if (!path.startsWith(".")||path.endsWith(".")) {
                return false;
            }

            StringTokenizer strTok = new java.util.StringTokenizer(path, ".");

            while (strTok.hasMoreTokens()) {
                String str = strTok.nextToken();
                if (str.length()==0) {
                    return false;
                }
            }
            return true;
        }

        public Branch findBranch(String path) throws Exception
        {
            if (!isLegalFormat(path)) {
                throw new Exception("Bad Path Format");
            }
            if (path.equals("."+getName())) {
                return this;
            }
            if (!path.startsWith("."+getName())  ) {//if not same something is wrong
                return null;
            } else {
                path=path.substring( getName().length()+1 );//remove current branch name from path
            }
            int index = path.indexOf(".",1);//find next delimiter

            String str="";

            if (index!=-1) {//get str to match key
                str= path.substring(1,index);
            } else {
                str= path.substring(1);
            }

            Branch br=this;

            boolean isFound=false;

            for (String key : br.branches.keySet()) {

                if (str.equals(key)) {
                    Branch branch = (Branch) branches.get(key);
                    br=branch.findBranch(path);
                    isFound=true;
                }
            }

            if (isFound==false) {
                return null;
            }
            return br;
        }

        public void putLeaf(String key, String value)throws Exception
        {
            if (!leaves.containsKey(key)) {
                leaves.put(key, value);
            } else throw new Exception("Leaf key exists");

        }
        public void putLeaf(String path, String key, String value)throws Exception
        {
            Branch temp = findBranch(path);
            temp.putLeaf(key, value);
        }

        public void delLeaf(String key)
        {
            leaves.remove(key);
        }
        public void delLeaf(String path, String key)throws Exception
        {
            Branch temp = findBranch(path);
            temp.delLeaf(key);
        }

        public String getLeaf(String key)
        {
            return leaves.get(key);
        }
        public String getLeaf(String path, String key)throws Exception
        {
            Branch temp = findBranch(path);
            return temp.getLeaf(key);
        }

        public void showBranches()
        {
            Set<String> keySet = branches.keySet();

            for (String key:keySet) {
                System.out.println(key);
            }
        }
        public void showBranches(String path)throws Exception
        {
            Branch temp = findBranch(path);
            temp.showBranches();
        }

        public void showLeafKeys()
        {
            Set<String> keySet = leaves.keySet();

            for (String key:keySet) {
                System.out.println(key);
            }
        }
        public void showLeafKeys(String path)throws Exception
        {
            Branch temp = findBranch(path);
            temp.showLeafKeys();
        }

        public void showLeaves()
        {
            for (String key : leaves.keySet()) {
                System.out.println("Key["+key+"]="+leaves.get(key));
            }
        }
        public void showLeaves(String path)throws Exception
        {
            Branch temp = findBranch(path);
            temp.showLeaves();
        }

        public void showAll()
        {
            showAll("");
        }

        private void showAll(String branchName)
        {
            String path = branchName+"."+getName();
            System.out.println(path+":");

            showLeaves();

            for (String key : branches.keySet()  ) {
                Branch branch = (Branch) branches.get(key);
                branch.showAll(path);
            }   
        }

        public void showAllPath(String path)throws Exception
        {
            Branch temp = findBranch(path);

            temp.showAll();
        }

      

        public int[] size()
        {
            int total[]=new int[2];
            int temp[]=new int[2];

            total[0]=leaves.size();
            total[1]=branches.size();


            for (String key : branches.keySet()  ) {
                Branch branch = (Branch) branches.get(key);
                temp = branch.size();
                total[0]+=temp[0];
                total[1]+=temp[1];
            }   
            return total;
        }

        private void writeObject(java.io.ObjectOutputStream out)
        throws IOException
        {
            out.writeObject(new Boolean(isBase));
            out.writeObject(name);
            out.writeObject(branches);
            out.writeObject(leaves);

        }
        private void readObject(java.io.ObjectInputStream in)
        throws IOException, ClassNotFoundException
        { 
            isBase = ((Boolean) in.readObject()).booleanValue();
            name = (String)in.readObject();
            branches = (TreeMap)in.readObject();
            leaves = (TreeMap)in.readObject();
        }


        public void makLeaves(int size)throws Exception
        {
            for (int cnt=0;cnt!=size;cnt++) {
                leaves.put(Integer.toString(cnt), Integer.toString(Math.abs(rand.nextInt())));
            }
        }

        public void makBranches(int size, int level)throws Exception
        {
            if (level==0) {
                return;
            } else {
                level--;
            }
            Branch newBranch=null;

            makLeaves(size);

            if (level!=0) {

                for (int cnt=0;cnt!=size;cnt++) {

                    newBranch = new Branch(Integer.toString(cnt));

                    branches.put(Integer.toString(cnt), newBranch);

                    newBranch.makBranches(size, level);
                }
            }
        }
    }

    public void init(int branchCnt, int leafCnt)throws Exception
    {  
        tree.makBranches(branchCnt, leafCnt);

        tree.showAll();

        putStore();
    }

    public void putStore()throws Exception
    {
        FileWriter wr = new FileWriter(storeFile);

        String xml = xstream.toXML(tree);

        wr.write(xml);
        wr.close();
    }

    public void getStore()throws Exception
    {
        File file = new File(storeFile);
        if (file.exists()) {
            FileReader fileReader = new FileReader(storeFile);
            LineNumberReader rd = new LineNumberReader(fileReader); 

            String xml=rd.readLine();

            tree=((Branch)xstream.fromXML(xml));

            rd.close();
        }
    }

    public static void showCmds()
    {
        System.out.println("Puka is a simple database for storing keyword/string entries");
        System.out.println("commands: '(S)etLeaf', '(G)etLeaf', 'show(A)ll, 'Show(B)ranches, 'Show(L)eaves', '(M)akBranch'");
        System.out.println("Usage: Puka s <path> <leafKey> <value>");
        System.out.println("Usage: Puka g <path> <leafKey>");
        System.out.println("Usage: Puka m <path> <BranchKey>");
        System.out.println("Usage: Puka a <path>"); 
        System.out.println("Usage: Puka b <path>");
        System.out.println("Usage: Puka l <path>");

    }

    public static void main(String args[])throws Exception {
        Puka puka = new Puka();

        puka.getStore();
/*
        if (args.length==0) {
            showCmds();
        } else {
            if (args.length==1) {
                if (args[0].toUpperCase().equals("A")) {
                    puka.tree.showAll();
                }
            } else if (args.length==2) {
                if (args[0].toUpperCase().equals("A")) {
                    puka.tree.showAll(args[1]);
                } else if (args[0].toUpperCase().equals("B")) {
                    puka.tree.showBranches(args[1]);
                } else
                    if (args[0].toUpperCase().equals("L")) {
                    puka.tree.showLeaves(args[1]);
                }
            } else if (args.length==3) {
                if (args[0].toUpperCase().equals("G")) {
                    System.out.println(args[1]+"."+args[2]+"="+puka.tree.getLeaf(args[1], args[2]));
                } else
                    if (args[0].toUpperCase().equals("M")) {
                    puka.tree.addBranch(args[1],args[2]);
                }
            } else if (args.length==4) {
                if (args[0].toUpperCase().equals("S")) {
                    puka.tree.putLeaf(args[1],args[2]);
                    System.out.println(args[1]+"."+args[2]+"="+puka.tree.getLeaf(args[1],args[2]));
                }
            } else showCmds();
        }*/

        // puka.init(4,4);

        //test area ****************************

        // showCmds();

        // puka.tree.showAll();


/*
        String findStr = "."+puka.tree.getName();

        Branch branch = puka.tree.findBranch(findStr);

        int size[]=branch.size();

        System.out.println("size="+size[0]+","+size[1]);

               Branch newBranch = branch.findBranch("."+branch.getName()+".4");
      
              if (newBranch==null) {
                  System.out.println("Path:"+findStr+" not found");
              } else {
                  newBranch.showLeaves();
              }
      
              //System.out.println("************");
      */

/*
        puka.tree.showAll(".root");

        puka.tree.showBranches(".root");

        puka.tree.showLeaves(".root");

        System.out.println(".root.0[0]="+puka.tree.getLeaf(".root.0.0"));

        Branch temp = puka.tree.getBranch(".root.0.testBranch");

        puka.tree.addBranch(".root.0","testBranch");

        puka.tree.showBranches(".root.0");

        Branch br= puka.tree.getBranch(".root.0","testBranch");

        puka.tree.putLeaf(".root.0", "testLeaf", "howdy");

        puka.tree.showLeafKeys();

        System.out.println(puka.tree.getLeaf(".root.0", "testLeaf"));

        puka.tree.showAll();

        System.out.println("*****");
*/
        puka.tree.showAllPath(".root.2.2");




           puka.putStore();
    }

}









