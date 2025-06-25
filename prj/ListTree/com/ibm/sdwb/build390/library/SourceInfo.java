package com.ibm.sdwb.build390.library;

import java.util.*;
import com.ibm.sdwb.build390.MBBuildException;

/**
 * Interface for classes that represent build source locations.
 * Examples would be CMVC (track or level), Rational (view), PDS (PDS location)
 */
  public interface SourceInfo extends  Cloneable {

	public SourceInfo getClone();

	public Set getSetOfParts() throws MBBuildException;

	public Set getChangesetsInSource() throws MBBuildException;  // for listing tracks in a level, or activities being built

	public boolean isValidSource() throws MBBuildException;  // use to determine if the source info actually exists (such as track or level)

	public String getSourceIdentifyingStringForMVS(); // such as LEVEL BOB INTEGRATE or TRACK JILL FIX

    public Set getListOfMissingRequisites() throws MBBuildException;

    public String getState() throws MBBuildException;  // should return the state of the object in the library, such as integrate, build, certify, etc.

    public boolean isIncludingCommittedBase(); // determine whether the source is what's in this build, or include the commited base

	public void setIncludingCommittedBase(boolean newInclude); // set whether to include the base

	/**
	 * returns the type of source object this is (bob, jill, etc)
	 * 
	 * @return String sourceType
	 */
	public String getName();

	public void setName(String newName);

    public String getProject();
}
