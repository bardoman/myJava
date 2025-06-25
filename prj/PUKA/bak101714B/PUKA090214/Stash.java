import java.beans.*;
import java.io.*;
import java.io.File;
import java.util.*;

import com.thoughtworks.xstream.*;
import com.thoughtworks.xstream.io.xml.*;

public class Stash {

    // Map<String,String> stash= new TreeMap<String,String>();

    //private TreeMap tree = new TreeMap();

    String storeFile = "storageFile.xml";

    XStream xstream = new XStream(new StaxDriver());


    public Branch tree = new Branch();

    public class Branch implements Serializable {
        static final long serialVersionUID = 1111111111111111L;
        private TreeMap<String,Branch> branches= new TreeMap<String,Branch>();
        private TreeMap<String,String> leaves= new TreeMap<String,String>();

        public void makBranch(String name)
        {
            branches.put(name, new Branch());
        }

        public void putBranch(String name, Branch map)
        {
            branches.put(name, map);
        }

        public void delBranch(String name)
        {
            branches.remove(name);
        }

        public Branch getBranch(String name)
        {
            return branches.get(name);
        }

        public Set<String> branchKeySet()
        {
            return branches.keySet();
        }

        public Set<String> leafKeySet()
        {
            return leaves.keySet();
        }

        public void putLeaf(String name, String value)
        {
            leaves.put(name, value);
        }

        public void delLeaf(String name)
        {
            leaves.remove(name);
        }

        public String getLeaf(String name)
        {
            return leaves.get(name);
        }
        public void showBranchNames()
        {
            Set<String> keySet = branches.keySet();

            for (String key:keySet) {
                System.out.println(key);
            }
        }
        public void showLeafNames()
        {
            Set<String> keySet = leaves.keySet();

            for (String key:keySet) {
                System.out.println(key);
            }
        }

        public void showAll()
        {
            for (String key : branchKeySet()) {
                Branch branch = getBranch(key);

                System.out.println("branch="+key);

                for (String key1 : branch.leafKeySet()) {

                    System.out.println("Key["+key1+"]="+branch.getLeaf(key1));

                }
                System.out.println();

                branch.showAll();
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
            out.close();

        }
        private void readObject(java.io.ObjectInputStream in)
        throws IOException, ClassNotFoundException
        { 
            branches = (TreeMap)in.readObject();
            leaves = (TreeMap)in.readObject();
            in.close();
        }
        /*
        private void readObjectNoData()
        throws ObjectStreamException
        {
        }*/
    }


    public void init()throws Exception
    {  
        // getStore();

        // Random rand = new Random(new Date().getTime());

        Random rand = new Random(new Date().getTime());

        tree.makBranch("Misc");

        Branch tempBranch = tree.getBranch("Misc");

        tempBranch.makBranch("Misc.sub");
        Branch sub = tempBranch.getBranch("Misc.sub");
        sub.makLeaves(10);

        tempBranch.makLeaves(10);

        for (int i=0;i!=10;i++) {
            tree.makBranch( Integer.toString(i));

            Branch tempBranch1 = tree.getBranch(Integer.toString(i));

            tempBranch1.makLeaves(10);

            tempBranch.makBranch(Integer.toString(i)+".sub");
            Branch sub1 = tempBranch.getBranch(Integer.toString(i)+".sub");
            sub1.makLeaves(10);
        }

        tree.showAll();

        // showKeys();

        //  showKeyValues();

        // showValues();

        putStore();
    }
/*
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
    }*/
    public void putStore()throws Exception
    {
        OutputStream file = new FileOutputStream("store.ser");
        OutputStream buffer = new BufferedOutputStream(file);
        ObjectOutput output = new ObjectOutputStream(buffer);

        output.writeObject(tree);
    }

    public void getStore()throws Exception
    {
        InputStream file = new FileInputStream("store.ser");
        InputStream buffer = new BufferedInputStream(file);
        ObjectInput input = new ObjectInputStream (buffer);

        tree = (Branch)input.readObject();
    }

    public static void main(String args[])throws Exception {
        Stash stash = new Stash();

       // stash.getStore();
/*
        if (args.length==0) {
            System.out.println("Stash is a simple database for storing keyword/string entries");
            System.out.println("commands: '(p)ut', '(g)et', '(a)ll, (k)eys, (v)alues");
            System.out.println("Usage: Stash p <keyword> <value>");
            System.out.println("Usage: Stash g <keyword>");
            System.out.println("Usage: Stash a "); 
            System.out.println("Usage: Stash k ");
            System.out.println("Usage: Stash v "); 
        } else {
            if (args[0].toLowerCase().equals("g")) {
                stash.showKeyValue(args[1]);
            } else
                if (args[0].toLowerCase().equals("p")) {
                stash.put(args[1], args[2]);
                stash.showKeyValue(args[1]);
            } else
                if (args[0].toLowerCase().equals("a")) {
                stash.showKeyValues();
            } else
                if (args[0].toLowerCase().equals("k")) {
                stash.showKeys();
            } else
                if (args[0].toLowerCase().equals("v")) {
                stash.showValues();
            }
        }
*/
        stash.init();
        //stash.tree.showAll();

       // stash.putStore();
    }

}


