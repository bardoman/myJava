package com.ibm.sdwb.build390.library;

import java.util.*;
import com.ibm.sdwb.build390.MBBuildException;

/**
 * Interface for classes that represent build source locations.
 * Examples would be CMVC (track or level), Rational (view), PDS (PDS location)
 */
  public interface ChangeRequest extends  Cloneable {

	public ChangeRequest getClone();

    /**
     * Define a list of projects you care about. This will limit all queries to 
     * those projects.  If set to null, or not set, all projects are
     * considered valid.
     * 
     * @param projectSet
     */
    public void setInterestedProjects(Set projectSet);

    public Set getInterestedProjects();

    /**
     * Return a set of SourceInfo objects representing the buildable 
     * SourceInfos, one for each project this appears in.
     * 
     * @return 
     */
	public Set getIndividualSourceInfos();

    public Set getProjectsChangesetAppearsIn();

    /**
     * check to see if the changesets/defects and all it's parts are ready for 
     * further processing, such as USERMOD or PTF.
     * 
     * @return 
     */
    public boolean isReadyForPackageProcessing();

	/**
	 * returns the type of source object this is (bob, jill, etc)
	 * 
	 * @return String sourceType
	 */
	public String getName();

	public void setName(String newName);
}
