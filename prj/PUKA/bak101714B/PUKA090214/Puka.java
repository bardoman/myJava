import java.beans.*;
import java.io.*;
import java.io.File;
import java.util.*;

import com.thoughtworks.xstream.*;
import com.thoughtworks.xstream.io.xml.*;

public class Puka {

    String storeFile = "storageFile.xml";

    XStream xstream = new XStream(new StaxDriver());

    public Branch tree = new Branch();

    public class Branch implements Serializable {
        static final long serialVersionUID = 1111111111111111L;
        private TreeMap<String,Branch> branches= new TreeMap<String,Branch>();
        private TreeMap<String,String> leaves= new TreeMap<String,String>();

        public Branch makBranch(String name)
        {
            Branch newBranch= new Branch();
            branches.put(name, newBranch);
            return newBranch;
        }

        public void putBranch(String key, Branch map)
        {
            branches.put(key, map);
        }

        public void delBranch(String branch)
        {
            getBranch(branch).branches.remove(branch);
        }

        public Branch getBranch(String branch)
        {
            Branch br=this;
            String tempTok="";

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

        public Set<String> branchKeySet()
        {
            return branches.keySet();
        }

        public Set<String> leafKeySet()
        {
            return leaves.keySet();
        }

        public void putLeaf(String key, String value)
        {
            leaves.put(key, value);
        }

        public void delLeaf(String key)
        {
            leaves.remove(key);
        }

        public String getLeaf(String branch, String leaf)
        {
            return getBranch(branch).leaves.get(leaf);
        }

        public void showBranchNames()
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

        public void showAll(String branchName)
        {
            System.out.println(branchName+":");

            for (String key0 : leafKeySet()) {

                System.out.println("Key["+key0+"]="+getLeaf("ROOT",key0));
            }

            for (String key : branchKeySet()) {
                Branch branch = getBranch(key);

                System.out.println();

                branch.showAll(branchName+"."+key);
            }
        }

        public void makLeaves(int size)
        {
            Random rand = new Random(new Date().getTime());

            for (int cnt=0;cnt!=size;cnt++) {
                int val = rand.nextInt();

                putLeaf(Integer.toString(cnt), Integer.toString(Math.abs(val)));
            }
        }

        private void writeObject(java.io.ObjectOutputStream out)
        throws IOException
        {
            out.writeObject(branches);
            out.writeObject(leaves);

        }
        private void readObject(java.io.ObjectInputStream in)
        throws IOException, ClassNotFoundException
        { 
            branches = (TreeMap)in.readObject();
            leaves = (TreeMap)in.readObject();
        }

    }


    public void init()throws Exception
    {  
        // getStore();

        int size=4;

        Random rand = new Random(new Date().getTime());

        tree.makLeaves(size);

        for (int i=0;i!=size;i++) {
            Branch tempBranch=tree.makBranch( Integer.toString(i));

            tempBranch.makLeaves(size);

            for (int n=0;n!=size;n++) {
                Branch tempBranch2=tempBranch.makBranch(Integer.toString(n));

                tempBranch2.makLeaves(size);
            }
        }

        tree.showAll("root");

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

    public static void main(String args[])throws Exception {
        Puka puka = new Puka();

        puka.getStore();
/**/
        if (args.length==0) {
            System.out.println("Puka is a simple database for storing keyword/string entries");
            System.out.println("commands: '(S)etLeaf', '(G)etLeaf', 'show(A)ll, 'Show(B)ranches, 'Show(L)eaves', 'Show(V)alues, '(M)akBranch', makLea(F)");
            System.out.println("Usage: Puka s <branch> <keyword> <value>");
            System.out.println("Usage: Puka g <branch> <keyword>");
            System.out.println("Usage: Puka a "); 
            System.out.println("Usage: Puka b <branch>");
            System.out.println("Usage: Puka v <branch>"); 
        } else {
            if (args[0].toUpperCase().equals("S")) {
                puka.tree.putLeaf(args[1],args[2]);
                System.out.println(args[1]+"."+args[2]+"="+puka.tree.getLeaf(args[1],args[2]));
            } else
                if (args[0].toUpperCase().equals("G")) {

                System.out.println(args[1]+"."+args[2]+"="+puka.tree.getLeaf(args[1], args[2]));
            }

            else
                if (args[0].toUpperCase().equals("A")) {
                puka.tree.showAll("root");
            }
            /* else
            if (args[0].toUpperCase().equals("k")) {
            puka.tree.showKeys();
        } else
            if (args[0].toLotoUpperCasewerCase().equals("v")) {
            puka.tree.showValues();
        }*/
        }

        //       puka.init();
        //   puka.tree.showAll("root");

        puka.putStore();
    }

}


