package com.ibm.sdwb.build390.utilities;


/**
 * For objects that will be handed as booleans for some
 * reason.
 */
public interface BooleanInterface  {


	/**
	 * Determines if the object is satisfied.  What that 
	 * means will be defined by context. For instance, 
	 * with command line parameters, it would mean the parameter
	 * was specified.
	 * 
	 * @return true if the object is specified
	 */
	public boolean isSatisfied();

	/**
	 * In the event that the object is not satisfied,
	 * this should return the reason.
	 * 
	 * @return String indicating reason the object is not satisfied
	 *         if it's not, null otherwise.
	 */
	public String getReasonNotSatisfied();

	
	/**
	 * This is the name of the boolean.  Single word description
	 * For instance, commandline parameters put their name
	 * there.
	 * 
	 * @return  name for the boolean
	 */
	public String getNameOfBoolean();
	
	/**
	 * The explaination of the purpose of the boolean goes
	 * here. For instance, in a commandline parameter, 
	 * the value name and explaination would go here.
	 * 
	 * @return description of the booleanInterface object.
	 */
	public String getDescriptionOfBoolean();

        public boolean inputAvailable();
}
