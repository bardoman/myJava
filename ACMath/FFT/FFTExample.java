import org.apache.commons.math3.transform.DftNormalization;
import org.apache.commons.math3.transform.FastFourierTransformer;
import org.apache.commons.math3.transform.TransformType;
//import org.apache.commons.numbers.complex.Complex;

public class FFTExample {

    public static void main(String[] args) {
        // Sample data (must be a power of 2 in length)
        double[] data = {1.0, 1.0, 1.0, 1.0, 0.0, 0.0, 9.0, 6.0};
        
        System.out.println("Data:");
        for(double dbl:data)
        {
			System.out.println(dbl);
		}

        // Create a FastFourierTransformer instance
        FastFourierTransformer transformer = new FastFourierTransformer(DftNormalization.STANDARD);

        // Perform forward FFT
        org.apache.commons.math3.complex.Complex[] fftResult = transformer.transform(data, TransformType.FORWARD);

        // Print the real and imaginary parts of the FFT result
        System.out.println("FFT Result:");
        for (org.apache.commons.math3.complex.Complex c : fftResult) {
            System.out.println("Real: " + c.getReal() + ", Imaginary: " + c.getImaginary());
        }

        // Perform inverse FFT
        org.apache.commons.math3.complex.Complex[] ifftResult = transformer.transform(fftResult, TransformType.INVERSE);

        // Print the real part of the inverse FFT result (should be close to the original data)
        System.out.println("\nInverse FFT Result (Real Part):");
        for (org.apache.commons.math3.complex.Complex c : ifftResult) {
            System.out.println(c.getReal());
        }
    }
}
