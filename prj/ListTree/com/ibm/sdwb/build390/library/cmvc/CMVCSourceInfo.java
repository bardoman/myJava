package com.ibm.sdwb.build390.library.cmvc;


/**
 * Interface for classes that represent build source locations.
 * Examples would be CMVC (track or level), Rational (view), PDS (PDS location)
 */
public abstract class CMVCSourceInfo extends CMVCLibraryInfo implements com.ibm.sdwb.build390.library.SourceInfo {
	// common to all the CMVC build stuff

    static final long serialVersionUID = 5365570376743266928L;
     
	private String release = null;
	private ComponentAndPathRestrictions restrictions = null;
	private boolean includeCommittedBase = false;


	public CMVCSourceInfo(CMVCLibraryInfo tempInfo, String tempRelease, ComponentAndPathRestrictions tempRestrictions){
		super(tempInfo);
		release = tempRelease;
		restrictions = tempRestrictions;
	}

	public String getRelease(){
		return release;
	}

    public String getProject(){
        return getRelease();
    }

	public void setRelease(String tempRelease){
		release = tempRelease;
	}

	public boolean isIncludingCommittedBase(){
		return includeCommittedBase;
	}

	public void setIncludingCommittedBase(boolean newInclude){
		includeCommittedBase = newInclude;
	}

	public ComponentAndPathRestrictions getRestrictions(){
		return restrictions;
	}

	public void setRestrictions(ComponentAndPathRestrictions tempRestrict){
		restrictions = tempRestrict;
	}
}
