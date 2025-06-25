package com.ibm.sdwb.build390.library;

import java.util.*;
import com.ibm.sdwb.build390.MBBuildException;

/**
 * Interface for classes that a group of changesets that are to be processed together.
 * Examples would be CMVC (level)
 */
  public interface ChangesetGroup extends SourceInfo {

    public void create() throws MBBuildException; // create this entity in the library system

    public void delete() throws MBBuildException; // delete this entity in the library system

    public void rename(String newName) throws MBBuildException;  // change the name of this entity

    public void addChangesetToGroup(Changeset newChangeset);

    public void removeChangesetFromGroup(Changeset changesetToNuke);

    /**
     * Mark this source set as completed, and never to be updated
     * again
     */
    public void markProcessingComplete() throws MBBuildException;

    public void setStateToReady() throws MBBuildException;

    public void setStateToPreReady() throws MBBuildException;

    public void setStateToCertify() throws MBBuildException;

    public void setStateToIntegrate() throws MBBuildException;
}
