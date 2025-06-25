package com.ibm.sdwb.build390.info;

public class BuildOptionsLocal extends BuildOptions {
    static final long serialVersionUID = -1924724017900817187L;
	private boolean embeddedMetadata = false;
	private String[]  maclibs = new String[0];
	private String savList = new String();
    private boolean fastTrack = false;

    public void setOptions(BuildOptions old){
        super.setOptions(old);
        BuildOptionsLocal localOld = (BuildOptionsLocal) old;
        savList = localOld.savList;
        embeddedMetadata = localOld.embeddedMetadata;
        fastTrack = localOld.fastTrack;

        if (localOld.maclibs==null) {
            maclibs=null;
        }else {
            maclibs = new String[localOld.maclibs.length];  // these are arrays, so we can't just copy them.
            for (int index = 0; index < maclibs.length; index++) {
                maclibs[index] = localOld.maclibs[index];
            }
        }
    }

	public void setEmbeddedMetadata(boolean temp) {
		embeddedMetadata = temp;
	}

	public boolean isUsingEmbeddedMetadata() {
		return embeddedMetadata;
	}

	/** <br>getLocalParts returns the localParts setting.
	* @return String[] localParts setting */
	public String[]  getUserMacs() {
		return(maclibs);
	}

	/** <br>set the localParts setting.
	* @param String[] localParts setting */
	public void    setUserMacs(String[] tempMaclibs) {
		maclibs = tempMaclibs;
	}

	public void setSaveListing(String tempList) {
		savList = tempList;
	}

	public String getSaveListing() {
		return savList;
	}

    public boolean isFastTrack(){
        return fastTrack;
    }

    public void setFastTrack(boolean tempFast){
        fastTrack = tempFast;
    }

    public BuildOptions getCopy(){
        BuildOptions toReturn = new BuildOptionsLocal();
        toReturn.setOptions(this);
        return toReturn;
    }

	public String toString(){
		String returnString = super.toString();
        if (maclibs != null) {
            for (int i = 1; i < maclibs.length; i++) {
                returnString += "Usermac" + i + "=" +maclibs[i]+"\n";
            }
        }
		return returnString;
	}

	/**
	public void setOptions(Hashtable settings) {
		autobuild = (String) settings.get(AUTOBLDSTRING);
		listgen = (String) settings.get(LISTGENSTRING);
		runscan = (String) settings.get(RUNSCANSTRING);
		force = (String) settings.get(FORCESTRING);

		if (autobuild == null) {
			autobuild = "YES";
		}
		if (listgen == null) {
			listgen = "NO";
		}
		if (runscan == null) {
			runscan = "NO";
		}
		if (force == null) {
			force = "NO";
		}
		buildcc = Integer.parseInt((String) settings.get(BUILDCCSTRING));
	}
*/

}
