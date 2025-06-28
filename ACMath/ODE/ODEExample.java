   import org.apache.commons.math3.ode.FirstOrderIntegrator;
   import org.apache.commons.math3.ode.nonstiff.DormandPrince54Integrator;
   import org.apache.commons.math3.ode.sampling.StepHandler;
   import org.apache.commons.math3.ode.sampling.StepInterpolator;
   import org.apache.commons.math3.ode.FirstOrderDifferentialEquations;
   
 public class ODEExample {
	 
   public class SimpleODE implements FirstOrderDifferentialEquations {

       private final double omega;

       public SimpleODE(double omega) {
           this.omega = omega;
       }

       @Override
       public int getDimension() {
           return 2; // Two variables (y0 and y1)
       }

       @Override
       public void computeDerivatives(double t, double[] y, double[] yDot) {
           yDot[0] = omega * (1 - y[1]); // y0' = omega * (1 - y1)
           yDot[1] = omega * (y[0] - 0); // y1' = omega * y0
       }
   }

public void ODEExample()
{
  
// Define the ODE problem
           SimpleODE ode = new SimpleODE(1.0);
// Set up the integrator (Dormand-Prince 5(4) with adaptive step size)
           double minStep = 1e-8;
           double maxStep = 1.0;
           double scalAbsoluteTolerance = 1e-8;
           double scalRelativeTolerance = 1e-8;  
 FirstOrderIntegrator dp 
               = new DormandPrince54Integrator(minStep, maxStep, scalAbsoluteTolerance, scalRelativeTolerance);         

           // Set up a step handler to process the results
           dp.addStepHandler(new StepHandler() {
               @Override
               public void init(double t0, double[] y0, double t) {
                   // Initialization (optional)
               }

               @Override
               public void handleStep(StepInterpolator interpolator, boolean isLast) {
                   double t = interpolator.getCurrentTime();
                   double[] y = interpolator.getInterpolatedState();
                   System.out.printf("Time: %.4f, y0: %.4f, y1: %.4f%n", t, y[0], y[1]);
               }
           });

           // Initial conditions
           double t0 = 0.0;
           double[] y0 = {0.0, 1.0};

           // Integration interval and results array
           double t1 = 10.0;
           double[] y = new double[ode.getDimension()];

           // Integrate the equations
           dp.integrate(ode, t0, y0, t1, y);

           // Print the final state (optional)
           System.out.printf("Final state: y0 = %.4f, y1 = %.4f%n", y[0], y[1]);
       } 
       
        public static void main(String[] args) {
		  ODEExample odexpl= new ODEExample();
	  } 
   }
