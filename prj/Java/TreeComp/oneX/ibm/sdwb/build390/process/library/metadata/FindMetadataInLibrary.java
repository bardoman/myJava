package com.ibm.sdwb.build390.process.library.metadata;

import java.util.*;

import com.ibm.sdwb.build390.info.*;
import com.ibm.sdwb.build390.library.LibraryInfo;
import com.ibm.sdwb.build390.logprocess.*;
import com.ibm.sdwb.build390.mainframe.ReleaseInformation;
import com.ibm.sdwb.build390.metadata.*;
import com.ibm.sdwb.build390.filter.Filter;
import com.ibm.sdwb.build390.process.*;
import com.ibm.sdwb.build390.process.steps.*;
import com.ibm.sdwb.build390.process.steps.library.metadata.*;
import com.ibm.sdwb.build390.userinterface.UserCommunicationInterface;



public class FindMetadataInLibrary extends AbstractProcess {

    static final long serialVersionUID = 1111111111111111L;


    private Filter filter;
    private PopulateMetadataInLibrary populateMetadata;



    public FindMetadataInLibrary(Set parts, LibraryInfo libInfo,ReleaseInformation releaseInfo, UserCommunicationInterface tempComm){
        super("Find metadata in library", 1, tempComm); 
        populateMetadata = new PopulateMetadataInLibrary(parts,libInfo,releaseInfo,this);
    }

    public void setFilter(Filter filter){
        this.filter = filter;
    }




    /**
     * This method is used to return the steps to run to accomplish
     * a process.   The step to run first, then the next step to run
     * and so on.  If you need to have a step repeated, that should be
     * handled in the step, not here.                   	 * @param stepToGet
     *
     * @return The step to run, null if there are no more steps
     */
    protected ProcessStep getProcessStep(int stepToGet, int stepIteration) throws com.ibm.sdwb.build390.MBBuildException {
        switch (stepToGet) {
        case 0:
            return populateMetadata;
        }
        return null;

    }

    /**
     * Put anything in here you want to run after a given step
     *
     * @param stepRun
     * @param stepIteration
     */
    protected void postStep(int stepRun, int stepIteration) throws com.ibm.sdwb.build390.MBBuildException {
        switch (stepRun) {
        case 0:
            filter.filter(populateMetadata.getPopulatedParts());
            break;

        }
    }


}


