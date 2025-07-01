
import java.util.Random;

public class mult4DMat
{
static int size=4;
public static void main(String args[])
{

int[][][][] matA=rand4DMat(size);
/*={{{{0,1,2,4},{0,1,2,4},{0,1,2,4},{0,1,2,4}}},
   {{{0,1,2,4},{0,1,2,4},{0,1,2,4},{0,1,2,4}}},
   {{{0,1,2,4},{0,1,2,4},{0,1,2,4},{0,1,2,4}}},
   {{{0,1,2,4},{0,1,2,4},{0,1,2,4},{0,1,2,4}}}};*/
	              
int[][][][] matB=rand4DMat(size);
/*={{{{0,1,2,4},{0,1,2,4},{0,1,2,4},{0,1,2,4}}},
   {{{0,1,2,4},{0,1,2,4},{0,1,2,4},{0,1,2,4}}},
   {{{0,1,2,4},{0,1,2,4},{0,1,2,4},{0,1,2,4}}},
   {{{0,1,2,4},{0,1,2,4},{0,1,2,4},{0,1,2,4}}}};*/    
   
int[][][][] matC=rand4DMat(size);           
	              
System.out.println("mult4DMat:\n");
     print4DMat(matA,"matA");
     print4DMat(matB,"matB");


   matC=mult4DMat(matA,matB);
  
    print4DMat(matC,"matC");
      
}

  public static int[][][][] rand4DMat(int size) {
	  Random random = new Random(); 
   
  int[][][][] tmp = new int[size][size][size][size];
         
   for (int x = 0; x < size; x++) { 
            for (int y = 0; y < size; y++) { 
                for (int z = 0; z < size; z++) { 
                for (int q = 0; q < size; q++) { 
                    tmp[x][y][z][q]=random.nextInt(10);
                }
            }
		}
        }	
	  return tmp;
  }
  
  
   public static void print4DMat(int[][][][] mat,String msg) {
	   System.out.println(msg);
	   int size = mat.length;
	  System.out.println("size="+size);
            
   for (int x = 0; x < size; x++) { 
            for (int y = 0; y < size; y++) { 
                for (int z = 0; z < size; z++) { 
					for (int q = 0; q < size; q++) { 
                    System.out.print(mat[x][y][z][q]);
                         System.out.print(",");
                }
                
            }
            System.out.println();
		  }
        }
        System.out.println();	
  }
  
     public static int[][][][]  mult4DMat(int[][][][] matA, int[][][][] matB) {
		 int size = matA.length;
	   System.out.println("size="+size);
	    int[][][][] resultMat= new int[size][size][size][size];
            
   for (int x = 0; x < size; x++) { 
            for (int y = 0; y < size; y++) { 
                for (int z = 0; z < size; z++) { 
					for (int q = 0; q < size; q++) { 
					  resultMat[x][y][z][q]  += matA[x][y][z][q] * matB[x][y][z][q];                
                }
			  }
            }
        }
         return resultMat;
  }

}
    
