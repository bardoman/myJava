package com.ibm.sdwb.build390.process.steps.library.metadata;

import java.util.Set;
import com.ibm.sdwb.build390.*;
import com.ibm.sdwb.build390.info.*;
import com.ibm.sdwb.build390.library.LibraryInfo;
import com.ibm.sdwb.build390.mainframe.*;
import com.ibm.sdwb.build390.logprocess.*;
import com.ibm.sdwb.build390.metadata.*;
import com.ibm.sdwb.build390.process.steps.*;

public class PopulateMetadataInLibrary extends ProcessStep {

    static final long serialVersionUID = 1111111111111111L;

    private ReleaseInformation releaseName = null;
    private LibraryInfo libInfo = null;
    private Set parts = null;


    public PopulateMetadataInLibrary(Set parts , LibraryInfo tempInfo, ReleaseInformation tempRelease, com.ibm.sdwb.build390.process.AbstractProcess tempProc) {
        super(tempProc,"Populate metadata in library for " + tempRelease.getLibraryName());
        setVisibleToUser(true);
        setUndoBeforeRerun(false);
        this.libInfo  = tempInfo;
        this.releaseName = tempRelease;
        this.parts = parts;
    }                                      

    public Set getPopulatedParts(){
        return parts;
    }

    public void execute() throws MBBuildException  {
        getLEP().LogSecondaryInfo(getFullName(),"Entry");
        libInfo.getMetadataOperationsHandler().populateMetadataMapFieldOfPassedInfos(parts);
    }
}
