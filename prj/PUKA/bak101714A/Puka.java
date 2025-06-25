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

    public Branch tree = new Branch("root");

    public class Branch implements Serializable {
        static final long serialVersionUID = 1111111111111111L;
        private TreeMap<String,Branch> branches= new TreeMap<String,Branch>();
        private TreeMap<String,String> leaves= new TreeMap<String,String>();
        private String name="";

        public Branch(String name)
        {
            this.name=name;
        }
        public String getName()
        {
            return this.name;
        }
        public void SetName(String name)
        {
            this.name=name;
        }
        public Branch addBranch(String name)throws Exception
        {
            if (!branches.containsKey(name)) {
                Branch tempBranch=new Branch(name);
                branches.put(name,tempBranch ); 
                return tempBranch;
            } else throw new Exception("Branch key exists");
        }

        public void putBranch(String name, Branch branch)throws Exception
        {
            if (!branches.containsKey(name)) {
                branches.put(name, branch);
            } else throw new Exception("Branch name exists");
        }

        public void delBranch(String branch)
        {
            branches.remove(branch);
        }

        public Branch getBranch(String branchName)
        {
            return branches.get(branchName);
        }

        public Branch findBranch(String branch)
        {
            Branch br=this;

            if (branch.toUpperCase().equals("ROOT")) {
                return br;
            } else {

                while (branch.indexOf(".")!=-1) {
                    branch=branch.substring(branch.indexOf(".")+1);
                    br = br.getBranch(branch);
                }
            }
            return br;
        }

        public void putLeaf(String name, String value)throws Exception
        {
            if (!leaves.containsKey(name)) {
                leaves.put(name, value);
            } else throw new Exception("Leaf name exists");

        }

        public void delLeaf(String key)
        {
            leaves.remove(key);
        }

        public String getLeaf(String branch, String leaf)
        {
            return getBranch(branch).leaves.get(leaf);
        }

        public void showBranches()
        {
            Set<String> keySet = branches.keySet();

            for (String key:keySet) {
                System.out.println(key);
            }
        }
        public void showLeafKeys()
        {
            Set<String> keySet = leaves.keySet();

            for (String key:keySet) {
                System.out.println(key);
            }
        }

        public void showLeaves()
        {
            for (String key : leaves.keySet()) {
                System.out.println("Key["+key+"]="+leaves.get(key));
            }
        }

        public void showAll(String branchName)
        {
            System.out.println(branchName+"."+name+":");

            showLeaves();

            for (String key : branches.keySet()) {
                getBranch(key).showAll(branchName+"."+name);
            }
        }

        private void writeObject(java.io.ObjectOutputStream out)
        throws IOException
        {
            out.writeObject(name);
            out.writeObject(branches);
            out.writeObject(leaves);

        }
        private void readObject(java.io.ObjectInputStream in)
        throws IOException, ClassNotFoundException
        { 
            name = (String)in.readObject();
            branches = (TreeMap)in.readObject();
            leaves = (TreeMap)in.readObject();
        }


        public void makLeaves(int size)throws Exception
        {
            for (int cnt=0;cnt!=size;cnt++) {
                putLeaf(Integer.toString(cnt), Integer.toString(Math.abs(rand.nextInt())));
            }
        }
    }

    public void init()throws Exception
    {  
        //getStore();

        int size=5;

        tree.makLeaves(size);

        for (int i=0;i!=size;i++) {
            Branch tempBranch=tree.addBranch( Integer.toString(i));

            tempBranch.makLeaves(size);

            for (int n=0;n!=size;n++) {
                Branch tempBranch2=tempBranch.addBranch(Integer.toString(n));

                tempBranch2.makLeaves(size);
            }
        }

        tree.showAll("");

        // showKeys();

        //  showKeyValues();

        // showValues();

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
        System.out.println("Usage: Puka s <branch> <keyword> <value>");
        System.out.println("Usage: Puka g <branch> <keyword>");
        System.out.println("Usage: Puka a <branch>"); 
        System.out.println("Usage: Puka b <branch>");
        System.out.println("Usage: Puka l <branch>");
        System.out.println("Usage: Puka m <branch>");
    }

    public static void main(String args[])throws Exception {
        Puka puka = new Puka();

        puka.getStore();

        if (args.length==0) {
            showCmds();
        } else {
            if (args.length==2) {
                if (args[0].toUpperCase().equals("A")) {
                    puka.tree.showAll(args[1]);
                }if (args[0].toUpperCase().equals("B")) {
                    puka.tree.getBranch(args[1]).showBranches();
                } else
                    if (args[0].toUpperCase().equals("L")) {
                    puka.tree.getBranch(args[1]).showLeaves();
                } else
                    if (args[0].toUpperCase().equals("M")) {
                    puka.tree.addBranch(args[1]);

                } else if (args.length==3) {
                    if (args[0].toUpperCase().equals("G")) {
                        System.out.println(args[1]+"."+args[2]+"="+puka.tree.getLeaf(args[1], args[2]));
                    }
                } else if (args.length==4) {
                    if (args[0].toUpperCase().equals("S")) {
                        puka.tree.putLeaf(args[1],args[2]);
                        System.out.println(args[1]+"."+args[2]+"="+puka.tree.getLeaf(args[1],args[2]));
                    }
                } else showCmds();
            }
        }

        // puka.init();
        //  puka.tree.showAll("");

        puka.putStore();
    }

}








