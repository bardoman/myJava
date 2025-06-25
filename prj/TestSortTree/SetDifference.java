   import java.util.HashSet;
   import java.util.Set;

   public class SetDifference {
       public static void main(String[] args) {
           Set<String> set1 = new HashSet<>();
           set1.add("apple");
           set1.add("banana");
           set1.add("cherry");

           Set<String> set2 = new HashSet<>();
           set2.add("banana");
           set2.add("date");

           Set<String> difference = new HashSet<>(set1); // Create a copy to avoid modifying set1
           difference.removeAll(set2);

           System.out.println("set1: " + set1); // Output: set1: [banana, apple, cherry]
           System.out.println("set2: " + set2); // Output: set2: [date, banana]
           System.out.println("Difference (set1 - set2): " + difference); // Output: Difference (set1 - set2): [apple, cherry]
       }
   }
