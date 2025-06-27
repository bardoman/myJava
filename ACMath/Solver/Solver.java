import org.apache.commons.math3.linear.Array2DRowRealMatrix;
import org.apache.commons.math3.linear.ArrayRealVector;
import org.apache.commons.math3.linear.DecompositionSolver;
import org.apache.commons.math3.linear.LUDecomposition;
import org.apache.commons.math3.linear.RealMatrix;
import org.apache.commons.math3.linear.RealVector;
import org.apache.commons.math3.exception.MathIllegalArgumentException;

public class Solver {

    public static void main(String[] args) {
        // Define the coefficient matrix A
        RealMatrix coefficients = new Array2DRowRealMatrix(new double[][] {
            {2, 3, -2},
            {-1, 7, 6},
            {4, -3, -5}
        }, false);

        // Define the constant vector B
        RealVector constants = new ArrayRealVector(new double[] {1, -2, 1}, false);

        try {
            // Create an LU decomposition object
            LUDecomposition lu = new LUDecomposition(coefficients);

            // Get the solver from the decomposition
            DecompositionSolver solver = lu.getSolver();

            // Solve the linear system
            RealVector solution = solver.solve(constants);

            // Print the solution
            System.out.println("Solution: " + solution);

            // Example of solving for multiple right-hand sides (B)
            RealMatrix constantsMatrix = new Array2DRowRealMatrix(new double[][] {
                {1, 5},
                {-2, -3},
                {1, 2}
            }, false);

            RealMatrix solutions = solver.solve(constantsMatrix);
            System.out.println("Solutions for multiple right-hand sides:\n" + solutions);

        } catch (MathIllegalArgumentException e) {
            System.err.println("Error solving the system: " + e.getMessage());
        }
    }
}
