
import java.util.*;

public class proto {

    public static void main(String args[]) {
        // LinkedList list=new LinkedList();
        // ArrayList list=new ArrayList();
        Hashtable table = new Hashtable();
        TreeSet treeSet= new TreeSet();

        for (int y=0;y<10;y++)
            for (int x=0;x<10;x++) {
                table.put(x,y);
            }
        table.put(5,"hello");
        System.out.print(table);
        System.out.println();

        Set mySet=table.keySet();

        for (int x=0;x<mySet.size();x++) {
            System.out.print(table.get(x));
        }
        System.out.println();

        treeSet.addAll(mySet);

        System.out.println("TreeSet elements: " + treeSet);
    }
}
