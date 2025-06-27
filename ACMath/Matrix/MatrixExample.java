import org.apache.commons.math3.linear.Array2DRowRealMatrix;
import org.apache.commons.math3.linear.RealMatrix;
import org.apache.commons.math3.linear.MatrixUtils;
import org.apache.commons.math3.linear.LUDecomposition;
import org.apache.commons.math3.linear.DecompositionSolver;
import org.apache.commons.math3.linear.RealVector;
import org.apache.commons.math3.linear.ArrayRealVector;

public class MatrixExample {

    public static void main(String[] args) {
        // Create a 2x3 matrix using a double array
        double[][] matrixData = { {1, 2, 3}, {4, 5, 6} };
        RealMatrix matrix = MatrixUtils.createRealMatrix(matrixData);

        // Get dimensions
        int rows = matrix.getRowDimension();
        int cols = matrix.getColumnDimension();
        System.out.println("Matrix dimensions: " + rows + "x" + cols);

        // Access an element
        double entry = matrix.getEntry(0, 1);
        System.out.println("Element at (0, 1): " + entry);

        // Create another matrix
        double[][] matrixData2 = { {7, 8}, {9, 10}, {11, 12} };
        RealMatrix anotherMatrix = new Array2DRowRealMatrix(matrixData2);

        // Matrix multiplication
        RealMatrix product = matrix.multiply(anotherMatrix);
        System.out.println("Product matrix:\n" + product);

        // Inversion (example with a square matrix)
        double[][] squareData = { {2, 3}, {1, 2} };
        RealMatrix squareMatrix = new Array2DRowRealMatrix(squareData);
        try {
            DecompositionSolver solver = new LUDecomposition(squareMatrix).getSolver();
            RealMatrix inverse = solver.getInverse();
            System.out.println("Inverse of the square matrix:\n" + inverse);
        } catch (Exception e) {
            System.out.println("Matrix is not invertible.");
        }
    }
}
