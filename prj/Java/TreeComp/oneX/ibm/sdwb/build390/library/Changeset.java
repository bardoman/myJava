package com.ibm.sdwb.build390.library;

import java.util.*;
import com.ibm.sdwb.build390.MBBuildException;

/**
 * Interface for classes that represent build source locations.
 * Examples would be CMVC (track or level), Rational (view), PDS (PDS location)
 */
  public interface Changeset extends SourceInfo {

      public void setChangesetGroupContainingChangeset(ChangesetGroup group);

      public ChangesetGroup getChangesetGroupContainingChangeset();
}
