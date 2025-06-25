package com.ibm.sdwb.build390.process.library.metadata;

import java.util.*;
import java.io.File;

import com.ibm.sdwb.build390.*;
import com.ibm.sdwb.build390.info.*;
import com.ibm.sdwb.build390.logprocess.*;
import com.ibm.sdwb.build390.metadata.*;
import com.ibm.sdwb.build390.filter.Filter;
import com.ibm.sdwb.build390.filter.FilterOutput;
import com.ibm.sdwb.build390.process.*;
import com.ibm.sdwb.build390.process.steps.*;
import com.ibm.sdwb.build390.process.steps.library.metadata.*;
import com.ibm.sdwb.build390.userinterface.UserCommunicationInterface;



public class FindMetadataInHostAndLibrary extends AbstractProcess {

    static final long serialVersionUID = 1111111111111111L;

    private MBBuild build = null;
    private Set metadataBuilder;
    private Collection criteriaEntries;

    private FindMetadataInLibrary finder;
    private GenerateAndUploadMetadataOrder  uploadStep;
    private com.ibm.sdwb.build390.process.DriverPartListFilteredByMetadata driverPartlist;

    private Filter filter;


    private List hostAndCmvcMatch;

    private Set updates;
    private Set inserts;



    public FindMetadataInHostAndLibrary(MBBuild tempBuild, Set metadataBuilder, UserCommunicationInterface tempComm) {
        super("Find Metadata In Host And Library ", 3, tempComm); 
        this.build = tempBuild;
        this.metadataBuilder = metadataBuilder;
    }

    public void setFilter(Filter filter) {
        this.filter = filter;
    }

    public void setCriteriaEntries(Collection criteriaEntries) {
        this.criteriaEntries = criteriaEntries;
    }


    public FilterOutput getFilterOutput() {
        return filter.output();
    }


    public Set getUpdatesNeededMetadata() {
        return updates;
    }

    public Set getInsertsNeededParts() {
        return inserts;
    }

    public List getHostAndLibraryMatch() {
        return hostAndCmvcMatch;
    }

    public Set getPopulatedParts() {
        return metadataBuilder;
    }



    /**
     * This method is used to return the steps to run to accomplish
     * a process.   The step to run first, then the next step to run
     * and so on.  If you need to have a step repeated, that should be
     * handled in the step, not here.                   	 * @param stepToGet
     *
     * @return The step to run, null if there are no more steps
     */
    protected ProcessStep getProcessStep(int stepToGet, int stepIteration) throws MBBuildException {
        switch (stepToGet) {
        case 0:
            finder = new FindMetadataInLibrary(metadataBuilder,build.getLibraryInfo(),build.getReleaseInformation(),this);
            finder.setFilter(filter);
            FullProcess cmvcFilter = new FullProcess(finder,this);
            return cmvcFilter;
        case 1:
            uploadStep = new GenerateAndUploadMetadataOrder(metadataBuilder,build.getMainframeInfo(),this);
            uploadStep.setSavePath(build.getBuildPath() +  "filterHostAndCmvc" + (new Random()).nextInt() + ".ord");
            uploadStep.setUploadPDSPrefix(build.getReleaseInformation().getMvsHighLevelQualifier() + "."+ 
                                          build.getReleaseInformation().getMvsName() +"."+ build.getDriverInformation().getName() + "." + "ORDERS(");
            return uploadStep;
        case 2:
            driverPartlist = new com.ibm.sdwb.build390.process.DriverPartListFilteredByMetadata(criteriaEntries,new File(build.getBuildPath()), build.getMainframeInfo(), build.getLibraryInfo(),
                                                                                                build.getReleaseInformation(), build.getDriverInformation(),this);



            driverPartlist.setMetadataFilterOrderFile(uploadStep.getUploadedPDSPrefix() +  uploadStep.getUploadedFileName() + uploadStep.getUploadedPDSSuffix());
            driverPartlist.setMetadataFilterId(uploadStep.getUploadedFileName());
            FullProcess hostFilter = new FullProcess(driverPartlist,this);
            return hostFilter;
        }
        return null;

    }

    /**
     * Put anything in here you want to run after a given step
     *
     * @param stepRun
     * @param stepIteration
     */
    protected void postStep(int stepRun, int stepIteration) throws MBBuildException {
        switch (stepRun) {
        case 0:
            metadataBuilder = new HashSet(filter.matched()); 
            break;
        case 2:

            updates = new HashSet();
            inserts = new HashSet();

            hostAndCmvcMatch = driverPartlist.getResults();
            /** iterator through the host*&cmvc matched parts that the host sent back 
             * If there is a match in the cmvcoutput (getfilterOutput.getMathed() then we have to 
             * update that metadata element in cmvc.
             * If its not there, then we have to create a new one in cmvc(I dont think this should occur,
             * and cant think of a condition where it would occur(doesnt make sense) ).
             */
            if (hostAndCmvcMatch!=null) {
                for (Iterator iter= hostAndCmvcMatch.iterator();iter.hasNext();) {
                    FileInfo info = (FileInfo)iter.next();
                    String hostMatchCmvcName = (info.getDirectory()!=null ? (info.getDirectory() + info.getName()) : info.getName());

                    boolean matchFound = false;

                    for (Iterator itera=filter.matched().iterator();itera.hasNext()&& !matchFound;) {
                        FileInfo  cmvcFileInfo = (FileInfo)itera.next();

                        String cmvcMatchName = cmvcFileInfo.getDirectory()!=null ? (cmvcFileInfo.getDirectory() + cmvcFileInfo.getName()) : cmvcFileInfo.getName();

                        if (hostMatchCmvcName.equals(cmvcMatchName)) {
                            updates.add(cmvcFileInfo);
                            matchFound = true;
                        }
                    }

                    /* 
                        verify this, since the info would  just contain the partname/partclass.
                        there is no point in just adding the info object. 
                            if (!matchFound) {
                            inserts.add(info);
                        }
                        */
                }
                break;

            }
        }
    }


}


