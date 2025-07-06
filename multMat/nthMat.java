//import java.util.ArrayList;
//import java.util.Arrays;
import java.util.*;

public class nthMat {
	public static int size=4;
	public static Integer[]intArray = new Integer[size]; //{0,1, 2, 3, 4, 5,6,7,8,9};

    public static void main(String[] args) {
        initIntArray(intArray);
               
        ArrayList<ArrayList> base = new ArrayList<>();
        for(int i=0;i<size;i++)
        {
			base.add(new ArrayList<>(Arrays.asList(intArray)));
	    }		
			
        System.out.println("********base:"); 
        System.out.println(base); 

//*********************************************************
        
        ArrayList<ArrayList> base2 = new ArrayList<>();
        alocListRecurs(base2,10);
                
        System.out.println("****alocListRecurs  ****base2:"); 
        System.out.println(base2);
        
        System.out.println("********traverse base2:"); 
        
    //    traverseArrayList(base2);
    }
 
    public static void traverseArrayList(List<?> list){
        for (Object element : list) {
            if (element instanceof List<?>) {
                // If it's a List, recursively traverse it
                traverseArrayList((List<?>) element);
            } else {
                // If it's not a List, process the element
                System.out.println(element);
            }
        }
    }
    
/*    public static void initList(ArrayList<ArrayList> list)
    {
		Iterator<Integer> iterator = list.iterator();
    while (iterator.hasNext()) {
        ArrayList subList = iterator.next();
        
    }
		
	}
*/	
	
    public static void initIntArray(Integer[] ary)
    {
		int size = ary.length;
		
		for(int n=0;n<size;n++)
		{
			ary[n]=n;
		}		
	}
		  
     public static void alocListRecurs(ArrayList<ArrayList> list, int count) {
            if (count == 0) {
                return;
            }

           ArrayList tmp=new ArrayList<Integer>(Arrays.asList(intArray));
		
           list.add(tmp);
           
            alocListRecurs(list, count - 1);
        }     
 /*
     public static int[][][][]  multDMat(ArrayList<ArrayList> matA, ArrayList<ArrayList> matB) {
	   System.out.println("size="+size);
	    ArrayList<ArrayList> resultMat= new ArrayList<ArrayList> (matA);
            
   for (int x = 0; x < size; x++) { 
            for (int y = 0; y < size; y++) { 
                for (int z = 0; z < size; z++) { 
					for (int q = 0; q < size; q++) { 
					  resultMat.get(x).get(y).get(z)  = matA.get(x).get(y).get(z) * matB.get(x).get(y).get(z);                
                }
			  }
            }
        }
         return resultMat;
  }*/
}
