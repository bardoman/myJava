import java.util.SortedSet;
import java.util.TreeSet;
import java.util.*;

public class test {


    private static SortedSet <String> set1     = new TreeSet<String>(new Comp()); 
    
  

   
    public static void main(String args[]) {
 set1.add("abc123");
  set1.add("abc123def");
   set1.add("zzz123def");
    set1.add("xxx123");
     set1.add("sss123");
      set1.add("qqq123");
       set1.add("kkk123");
        int n = set1.size();

        int i=0;
        
        System.out.println(set1);
    }

    private static final class Comp implements Comparator { 
		
        public int compare(Object o1, Object o2) {
            String s1 = (String)o1;
            String s2 = (String)o2;

            return( s1.compareTo(s2) );
        }
    };

}
