package com.ibm.sdwb.build390.info;

import java.util.*;
import com.ibm.sdwb.build390.logprocess.*;
import com.ibm.sdwb.build390.user.Setup;

public class ChangeRequestMultipleInfo extends com.ibm.sdwb.build390.MBBuild{

    static final long serialVersionUID = 7003425113195450299L;

    private Set changeRequestSet = null;
    private boolean dryRun = false;
    private boolean bundleChangesets = false;
    private boolean serviceBuild = false;
    private String mainframeUserAddressToSendOutputTo = null;
    private String mainframeDatasetToStoreOutputIn = null;
    private Set changeRequestGroups = new HashSet();

    public ChangeRequestMultipleInfo(String idStart, String generalDirectory, LogEventProcessor lep){
        super(idStart, generalDirectory, lep);
    }

    public Set getChangeRequests(){
        return changeRequestSet;
    }

    public void setChangeRequestSet(Set newSet){
        changeRequestSet = newSet;
    }

    public Set getChangesetGroups(){
        return changeRequestGroups;
    }

    public void setChangesetGroups(Set temp){
        changeRequestGroups = temp;
    }

    public boolean  isDryRun() { return(dryRun);}

    public boolean  isBundled() { return(bundleChangesets);}

    public boolean isServiceBuild(){
        return serviceBuild;
    }

    public void  setDryRun(boolean tempDry) {dryRun = tempDry;}

    public void  setBundled(boolean tempBundled) {bundleChangesets = tempBundled;}

    public void setServiceBuild(boolean tempService){
        serviceBuild = tempService;
    }

    public String getMainframeUserAddressToSendOutputTo(){
        return mainframeUserAddressToSendOutputTo;
    }

    public void setMainframeUserAddressToSendOutputTo(String temp){
        mainframeUserAddressToSendOutputTo = temp;
    }

    public String getMainframeDatasetToStoreOutputIn(){
        return mainframeDatasetToStoreOutputIn;
    }

    public void setMainframeDatasetToStoreOutputIn(String temp){
        mainframeDatasetToStoreOutputIn = temp;
    }

    /** show displays the fields of this object */
    public String toString() {
        String buf = new String();
        buf += super.toString();
        buf+="changeRequestSet = " + changeRequestSet.toString() + "\n";
        buf+="dryRun = " + dryRun + "\n";
        buf+="bundleChangesets = " + bundleChangesets + "\n";
        return buf;
    }

}
