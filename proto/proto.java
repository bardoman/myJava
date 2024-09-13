
import java.util.*;

public class proto {

    enum DayName {
        MODAY,TUESDAY,WEDNESDAY,THURSDAY,FRIDAY,SATURDAY,SUNDAY}

    static <T> List<T> enumValuesInList(Class<T> enumCls) {
        T[] arr = enumCls.getEnumConstants();
        return arr == null ? Collections.emptyList() : Arrays.asList(arr);
    }

    void mapTest()
    {
        TreeMap<String, Integer> treeMap = new TreeMap<>();

        treeMap.put("A", 1); 
        treeMap.put("C", 3); 
        treeMap.put("B", 2);

        System.out.println("TreeMap elements: " + treeMap);

//**************************************************** 

        HashMap<String, Integer> languages = new HashMap<>();

        languages.put("Java", 8);
        languages.put("JavaScript", 1);
        languages.put("Python", 3);

        System.out.println("HashMap: " + languages);

//****************************************************

        Hashtable<String,String> myTable = new Hashtable<>();

        myTable.put("Java", "one");
        myTable.put("JavaScript", "two");
        myTable.put("Python", "three");

        System.out.println("Hashtable: " + myTable);
    }

    void listTest()
    {
        LinkedList myLList=new LinkedList(enumValuesInList(DayName.class));
        System.out.println("LinkedList: " + myLList);

//****************************************************

        ArrayList myAList=new ArrayList(enumValuesInList(DayName.class));
        System.out.println("ArrayList: " + myAList);

//****************************************************

        TreeSet myTreeSet= new TreeSet(enumValuesInList(DayName.class));
        System.out.println("TreeSet: " + myTreeSet);

//****************************************************

        Collections.reverse(myLList);
        System.out.println("Reversed LinkedList: " + myLList);

//****************************************************

        Collections.sort(myLList);
        System.out.println("Sort LinkedList: " + myLList);

//****************************************************
        Random rand=new Random(011235);

        Collections.shuffle(myLList,rand);
        System.out.println("Shuffle LinkedList: " + myLList);

//****************************************************

        int result =Collections.binarySearch(myLList,DayName.WEDNESDAY);
        System.out.println("result: " + result);

        for (Object obj : myLList ) {

            result =Collections.binarySearch(myLList,(DayName)obj);
            System.out.print((DayName)obj);
            System.out.println("="+Integer.toString(result));
        }

        EnumSet<DayName> myEnumSet = EnumSet.allOf(DayName.class);
        System.out.println("myEnumSet: " + myEnumSet);

    }

    public static void main(String args[]) {
        proto myproto=new proto();

        myproto.mapTest();

        myproto.listTest();
    }


}
