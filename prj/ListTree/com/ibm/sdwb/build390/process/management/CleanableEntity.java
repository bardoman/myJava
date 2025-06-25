package com.ibm.sdwb.build390.process.management;

import java.io.File;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import com.ibm.sdwb.build390.MBClient;
import com.ibm.sdwb.build390.MBJobInfo;
import com.ibm.sdwb.build390.mainframe.DriverInformation;
import com.ibm.sdwb.build390.user.Setup;
import com.ibm.sdwb.build390.user.SetupManager;

public class CleanableEntity implements java.io.Serializable{
	static final long serialVersionUID = 1111111111111111L;

	// These are sets because that's more flexible than single strings.
	private Map buildIdToDriverInformation = null;
	private Set heldJobOutputs = null;
	private Set MVSFileSets = null;
	private Map MVSBuildIDs = null;
	private Set localFilesAndDirectories = null;
	private Setup setup = null;
	private Set subCleanableEntitys = null;

	public CleanableEntity(){
		buildIdToDriverInformation = new HashMap();
		heldJobOutputs = new HashSet();
		MVSFileSets = new HashSet();
		MVSBuildIDs = new HashMap();
		localFilesAndDirectories = new HashSet();
		subCleanableEntitys = new HashSet();
		if (MBClient.isClientMode() & SetupManager.getSetupManager().hasSetup()) {
			setup = SetupManager.getSetupManager().createSetupInstance();
		}
	}

	public CleanableEntity(Setup tempSetup){
		buildIdToDriverInformation = new HashMap();
		heldJobOutputs = new HashSet();
		MVSFileSets = new HashSet();
		MVSBuildIDs = new HashMap();
		localFilesAndDirectories = new HashSet();
		subCleanableEntitys = new HashSet();
		setup = tempSetup;
	}

	public void addDriverLock(String buildId, DriverInformation driverInfo){
		buildIdToDriverInformation.put(buildId, driverInfo);
	}

	public void addMVSFileSet(String oneFileSet){
		MVSFileSets.add(oneFileSet);
	}

	public void addLocalFileOrDirectory(File oneFile){
		localFilesAndDirectories.add(oneFile);
	}

	public void addHeldJob(MBJobInfo tempInfo){
		heldJobOutputs.add(tempInfo);
	}

	public void addMVSBuildID(String buildId, DriverInformation driverInfo){
		MVSBuildIDs.put(buildId, driverInfo);
	}

	public void addAllHeldJobs(Set tempInfos){
		heldJobOutputs.addAll(tempInfos);
	}

	public void addAllMVSBuildIDs(Map tempIds){
		MVSBuildIDs.putAll(tempIds);
	}

	public void addSubCleanableEntity(CleanableEntity tempClean){
		subCleanableEntitys.add(tempClean);
		if (setup !=null) {
			tempClean.setSetup(setup);
		}
	}

	public void setSetup(Setup tempSetup){
		setup = tempSetup;
	}

	public Map getDriverLocks(){
		Map lockMap = new HashMap(buildIdToDriverInformation);
		for (Iterator subCleanableIterator = subCleanableEntitys.iterator(); subCleanableIterator.hasNext(); ) {
			CleanableEntity oneCleanable = (CleanableEntity) subCleanableIterator.next();
			lockMap.putAll(oneCleanable.getDriverLocks());
		}
		return lockMap;
	}

	public Set getMVSFileSets(){
		Set allMVSFileSets = new HashSet(MVSFileSets);
		for (Iterator subCleanableIterator = subCleanableEntitys.iterator(); subCleanableIterator.hasNext(); ) {
			CleanableEntity oneCleanable = (CleanableEntity) subCleanableIterator.next();
			allMVSFileSets.addAll(oneCleanable.getMVSFileSets());
		}
		return allMVSFileSets;
	}

	public Set getAllHeldJobs(){
		Set allHeldJobs = new HashSet(heldJobOutputs);
		for (Iterator subCleanableIterator = subCleanableEntitys.iterator(); subCleanableIterator.hasNext(); ) {
			CleanableEntity oneCleanable = (CleanableEntity) subCleanableIterator.next();
			allHeldJobs.addAll(oneCleanable.getAllHeldJobs());
		}
		return allHeldJobs;
	}

	public Map getAllMVSBuildIDs(){
		Map allMVSBuildIDs = new HashMap(MVSBuildIDs);
		for (Iterator subCleanableIterator = subCleanableEntitys.iterator(); subCleanableIterator.hasNext(); ) {
			CleanableEntity oneCleanable = (CleanableEntity) subCleanableIterator.next();
			allMVSBuildIDs.putAll(oneCleanable.getAllMVSBuildIDs());
		}
		return allMVSBuildIDs;
	}

	public Set getAllLocalFiles(){
		Set allLocalFiles = new HashSet(localFilesAndDirectories);
		for (Iterator subCleanableIterator = subCleanableEntitys.iterator(); subCleanableIterator.hasNext(); ) {
			CleanableEntity oneCleanable = (CleanableEntity) subCleanableIterator.next();
			allLocalFiles.addAll(oneCleanable.getAllLocalFiles());
		}
		return allLocalFiles;
	}

	public Setup getSetup(){
		return setup;
	}

	public void removeDriverLock(String oneLock){
		buildIdToDriverInformation.remove(oneLock);
		for (Iterator subCleanableIterator = subCleanableEntitys.iterator(); subCleanableIterator.hasNext(); ) {
			CleanableEntity oneCleanable = (CleanableEntity) subCleanableIterator.next();
			oneCleanable.removeDriverLock(oneLock);
		}
	}

	public void removeMVSFileSet(String oneFileSet){
		MVSFileSets.remove(oneFileSet);
		for (Iterator subCleanableIterator = subCleanableEntitys.iterator(); subCleanableIterator.hasNext(); ) {
			CleanableEntity oneCleanable = (CleanableEntity) subCleanableIterator.next();
			oneCleanable.removeMVSFileSet(oneFileSet);
		}
	}

	public void removeHeldJob(MBJobInfo tempJob){
		heldJobOutputs.remove(tempJob);
		for (Iterator subCleanableIterator = subCleanableEntitys.iterator(); subCleanableIterator.hasNext(); ) {
			CleanableEntity oneCleanable = (CleanableEntity) subCleanableIterator.next();
			oneCleanable.removeHeldJob(tempJob);
		}
	}

	public void removeMVSBuildID(String tempId){
		MVSBuildIDs.remove(tempId);
		for (Iterator subCleanableIterator = subCleanableEntitys.iterator(); subCleanableIterator.hasNext(); ) {
			CleanableEntity oneCleanable = (CleanableEntity) subCleanableIterator.next();
			oneCleanable.removeMVSBuildID(tempId);
		}
	}

	public void removeHeldJobs(Set tempHeldJobs){
		heldJobOutputs.removeAll(tempHeldJobs);
		for (Iterator subCleanableIterator = subCleanableEntitys.iterator(); subCleanableIterator.hasNext(); ) {
			CleanableEntity oneCleanable = (CleanableEntity) subCleanableIterator.next();
			oneCleanable.removeHeldJobs(tempHeldJobs);
		}
	}

	public void removeLocalFile(File oneFile){
		localFilesAndDirectories.remove(oneFile);
		for (Iterator subCleanableIterator = subCleanableEntitys.iterator(); subCleanableIterator.hasNext(); ) {
			CleanableEntity oneCleanable = (CleanableEntity) subCleanableIterator.next();
			oneCleanable.removeLocalFile(oneFile);
		}
	}

	public void clearAllHeldJobs(){
		heldJobOutputs.clear();
		for (Iterator subCleanableIterator = subCleanableEntitys.iterator(); subCleanableIterator.hasNext(); ) {
			CleanableEntity oneCleanable = (CleanableEntity) subCleanableIterator.next();
			oneCleanable.clearAllHeldJobs();
		}
	}

	public void clearMVSFileSets(){
		MVSFileSets.clear();
		for (Iterator subCleanableIterator = subCleanableEntitys.iterator(); subCleanableIterator.hasNext(); ) {
			CleanableEntity oneCleanable = (CleanableEntity) subCleanableIterator.next();
			oneCleanable.clearMVSFileSets();
		}
	}

	public void clearDriverLocks(){
		buildIdToDriverInformation.clear();
		for (Iterator subCleanableIterator = subCleanableEntitys.iterator(); subCleanableIterator.hasNext(); ) {
			CleanableEntity oneCleanable = (CleanableEntity) subCleanableIterator.next();
			oneCleanable.clearDriverLocks();
		}
	}

	public void clearMVSBuildIDs(){
		MVSBuildIDs.clear();
		for (Iterator subCleanableIterator = subCleanableEntitys.iterator(); subCleanableIterator.hasNext(); ) {
			CleanableEntity oneCleanable = (CleanableEntity) subCleanableIterator.next();
			oneCleanable.clearMVSBuildIDs();
		}
	}

	public void clearLocalFiles(){
		localFilesAndDirectories.clear();
		for (Iterator subCleanableIterator = subCleanableEntitys.iterator(); subCleanableIterator.hasNext(); ) {
			CleanableEntity oneCleanable = (CleanableEntity) subCleanableIterator.next();
			oneCleanable.clearLocalFiles();
		}
	}

	public static Map divideCleanablesBySetup(Set cleanables){
		Map setupCleanableMap = new HashMap();
		for (Iterator cleanableIterator = cleanables.iterator(); cleanableIterator.hasNext();) {
			CleanableEntity oneCleanable = (CleanableEntity) cleanableIterator.next();
			Set cleanableSetForSetup = (Set) setupCleanableMap.get(oneCleanable.getSetup());
			if (cleanableSetForSetup == null) {
				cleanableSetForSetup = new HashSet();
				setupCleanableMap.put(oneCleanable.getSetup(), cleanableSetForSetup);
			}
			cleanableSetForSetup.add(oneCleanable);
		}
		return setupCleanableMap;
	}

	public String toString(){
		return "buildIdToDriverInformation: " + buildIdToDriverInformation.toString() + "\nheldJobOutputs: " + heldJobOutputs.toString()+"\nMVSFileSets: " + MVSFileSets.toString();
	}
}
