package com.ibm.sdwb.build390.library;

import java.util.*;
import com.ibm.sdwb.build390.MBBuildException;

/**
 * Bundle of change requests ( CMVC defects or features for example)
 * Used in Usermod or PTF processes
 */
public class SourceInfoCollection implements SourceInfo, java.io.Serializable {
    static final long serialVersionUID = 1111111111111111L;
    private Collection changeRequestCollection = null;
    private String name = null;

    public Collection getChangeRequestCollection() {
        return changeRequestCollection;
    }

    public void setChangeRequestCollection(Collection tempCollection) {
        changeRequestCollection = tempCollection;
    }

    public SourceInfo getClone() {
        SourceInfoCollection newCollection = new SourceInfoCollection();
        try {
            newCollection.changeRequestCollection = (Collection) changeRequestCollection.getClass().newInstance();
        }catch (Exception e){
            throw new RuntimeException(e);
        }
        newCollection.changeRequestCollection.addAll(changeRequestCollection);
        return newCollection;
    }

    public Set getSetOfParts() throws MBBuildException{
        throw new RuntimeException("getSetOfParts not supported in SourceInfoColletion");
    }

    public Set getChangesetsInSource() throws MBBuildException{
        throw new RuntimeException("getChangesetsInSource not supported in SourceInfoColletion");
    }

    public boolean isValidSource() throws MBBuildException{
        throw new RuntimeException("isValidSource not supported in SourceInfoColletion");
    }

    public String getSourceIdentifyingStringForMVS() {
        throw new RuntimeException("getSourceIdentifyingStringForMVS not supported in SourceInfoColletion");
    }

    public Set getListOfMissingRequisites() throws MBBuildException{
        throw new RuntimeException("getListOfMissingRequisites not supported in SourceInfoColletion");
    }

    public String getState() throws MBBuildException{
        throw new RuntimeException("getState not supported in SourceInfoColletion");
    }

    public boolean isIncludingCommittedBase() {
        throw new RuntimeException("isIncludingCommittedBase not supported in SourceInfoColletion");
    }

    public void setIncludingCommittedBase(boolean newInclude) {
        throw new RuntimeException("setIncludingCommittedBase not supported in SourceInfoColletion");
    }

    public String getName() {
        return name;
    }

    public void setName(String newName) {
        name = newName;
    }

    public String getProject() {
        throw new RuntimeException("getProject not supported in SourceInfoColletion");
    }
}
