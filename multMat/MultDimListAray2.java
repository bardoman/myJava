import java.util.*;

public class MultDimListAray2 {

    /**
     * Recursively creates a multi-dimensional List structure.
     *
     * @param dimensions An array representing the size of each dimension.
     * @param currentDim The current dimension being constructed (starts from 0).
     * @return A List representing the current dimension of the array.
     */
    public static List<?> createMultiDimList(int[] dimensions, int currentDim) {
        // Base case: If we are at the last dimension, create a List of integers (or any desired type)
        if (currentDim == dimensions.length - 1) {
            List<Integer> innerList = new ArrayList<>();
            // You can populate this inner list with initial values if needed
            for (int i = 0; i < dimensions[currentDim]; i++) {
                innerList.add(i); // Example: initializing with 0
            }
            return innerList;
        }

        // Recursive step: Create a List of Lists for the current dimension
        List<List<?>> currentDimensionList = new ArrayList<>();
        for (int i = 0; i < dimensions[currentDim]; i++) {
            // Recursively call to create the next lower dimension
            currentDimensionList.add((List<?>) createMultiDimList(dimensions, currentDim + 1));
        }
        return currentDimensionList;
    }

    public static void main(String[] args) {
        // Define the sizes of the 5 dimensions
        int[] dimensions = {5, 5, 5, 5, 5}; // Example: 2x3x4x2x5 5D array

        // Create the 5-dimensional List using recursion
        List<?> fiveDimList = createMultiDimList(dimensions, 0);
        
        System.out.println("5DL:"+fiveDimList);
        
        System.out.println("5DL[1][1][1][1][0]:"+((List)((List)((List)((List)fiveDimList.get(1)).get(1)).get(1)).get(1)).get(0));
        System.out.println("5DL[1][1][1][1][1]:"+((List)((List)((List)((List)fiveDimList.get(1)).get(1)).get(1)).get(1)).get(1));
        System.out.println("5DL[1][1][1][1][2]:"+((List)((List)((List)((List)fiveDimList.get(1)).get(1)).get(1)).get(1)).set(2, 44));
        System.out.println("5DL[1][1][1][1][2]:"+((List)((List)((List)((List)fiveDimList.get(1)).get(1)).get(1)).get(1)).get(2));

        // You can now access elements (requires casting based on the depth)
        // Example: Accessing an element in the 5th dimension
        // This demonstrates how to navigate the nested List structure.
        // It requires multiple casts as the type is 'List<?>' at each level.
        try {
		
            List<?> d1 = (List<?>) fiveDimList;
            List<?> d2 = (List<?>) d1.get(1);
            List<?> d3 = (List<?>) d2.get(1);
            List<?> d4 = (List<?>) d3.get(1);
            List<Integer> d5 = (List<Integer>) d4.get(1);
            System.out.println("Element at [1][1][1][1][1]: " + d5.get(1));
        } catch (IndexOutOfBoundsException | ClassCastException e) {
            System.err.println("Error accessing element: " + e.getMessage());
        }
    }
}
