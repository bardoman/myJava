import java.util.ArrayList;
import java.util.Arrays;

public class nthMat {
    public static void main(String[] args) {
		int size=5;
        Integer[]intArray = {1, 2, 3, 4, 5};
        
        ArrayList<ArrayList> base = new ArrayList<>();
        for(int i=0;i<size;i++)
        {
			base.add(new ArrayList<>(Arrays.asList(intArray)));
	}		
			
       
     //   ArrayList<Integer> arrayList = new ArrayList<>(Arrays.asList(intArray));

        System.out.println(base); // Output: [10, 20, 30, 40, 50]
    }
}
