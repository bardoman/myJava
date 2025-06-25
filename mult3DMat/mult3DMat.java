
import java.util.Random;

public class mult3DMat
{



public static void main(String args[])
{
	
int[][][] matA;//={{{0,1,2},{3,4,5},{6,7,8}},{{0,1,2},{3,4,5},{6,7,8}},{{0,1,2},{3,4,5},{6,7,8}}};

int[][][] matB;//={{{0,1,2},{3,4,5},{6,7,8}},{{0,1,2},{3,4,5},{6,7,8}},{{0,1,2},{3,4,5},{6,7,8}}};
int[][][] matC;
int[][][] matD;

matA=rand3DMat(3);
matB=rand3DMat(3);
     print3DMat(matA);
     print3DMat(matB);


   matC=multiply3DMatrices(matA,matB);
  
    print3DMat(matC);
    
    matD=multiply3DMatrices(matA,matC);
    
     print3DMat(matD);

   
}
  public static int[][][] rand3DMat(int size) {
	  Random random = new Random(); 
   
  int[][][] tmp = new int[size][size][size];
         
   for (int x = 0; x < size; x++) { 
            for (int y = 0; y < size; y++) { 
                for (int z = 0; z < size; z++) { 
                    tmp[x][y][z]=random.nextInt(100);
                }
            }
        }	
	  return tmp;
  }
  
  public static void print3DMat(int[][][] mat) {
   int depth = mat.length;
        int rowsA = mat[0].length;
        int colsA = mat[0][0].length; // Also rowsB
        int colsB = mat[0][0].length;
        
   for (int d = 0; d < depth; d++) { // Iterate through depth (slices)
            for (int r = 0; r < rowsA; r++) { // Iterate through rows of current 2D slice in A
                for (int c = 0; c < colsB; c++) { // Iterate through columns of current 2D slice in B
                    for (int k = 0; k < colsA; k++) { // Perform dot product for current element
                       // resultMatrix[d][r][c] += matrixA[d][r][k] * matrixB[d][k][c];
                         System.out.print(mat[d][r][k]);
                         System.out.print(",");
                    }
                }
                System.out.println();
            }
        }	
        System.out.println();  
	  
  }

   public static int[][][] multiply3DMatrices(int[][][] matrixA, int[][][] matrixB) {
        // Assume matrices are already checked for compatibility
        int depth = matrixA.length;
        int rowsA = matrixA[0].length;
        int colsA = matrixA[0][0].length; // Also rowsB
        int colsB = matrixB[0][0].length;

        int[][][] resultMatrix = new int[depth][rowsA][colsB];

        for (int d = 0; d < depth; d++) { // Iterate through depth (slices)
            for (int r = 0; r < rowsA; r++) { // Iterate through rows of current 2D slice in A
                for (int c = 0; c < colsB; c++) { // Iterate through columns of current 2D slice in B
                    for (int k = 0; k < colsA; k++) { // Perform dot product for current element
                        resultMatrix[d][r][c] += matrixA[d][r][k] * matrixB[d][k][c];
                    }
                }
            }
        }
        return resultMatrix;
    }
 }
    
