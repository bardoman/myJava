package com.ibm.sdwb.build390.process.management;

import java.io.File;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import com.ibm.sdwb.build390.MBJobInfo;
import com.ibm.sdwb.build390.user.Setup;

public class MultipleCleanableEntity extends CleanableEntity{
	static final long serialVersionUID = 1111111111111111L;

	private Set cleanableSet = null;

	public MultipleCleanableEntity(){
		cleanableSet = new HashSet();
	}

	public void addCleanable(CleanableEntity newCleanable){
		cleanableSet.add(newCleanable);
	}

	public Map getDriverLocks(){
		Map returnLocks = new HashMap();
		returnLocks.putAll(super.getDriverLocks());
		for (Iterator cleanIterator = cleanableSet.iterator(); cleanIterator.hasNext();) {
			CleanableEntity oneCleanable = (CleanableEntity) cleanIterator.next();
			if (oneCleanable !=null) {
				if (oneCleanable.getDriverLocks()!=null) {
					returnLocks.putAll(oneCleanable.getDriverLocks());
				}else {
					System.out.println("get driver locks null");
				}
			}else {
				System.out.println("cleanable null");
			}
		}
		return returnLocks;
	}

	public Set getMVSFileSets(){
		Set mvsFileSets = new HashSet();
		mvsFileSets.addAll(super.getMVSFileSets());
		for (Iterator cleanIterator = cleanableSet.iterator(); cleanIterator.hasNext();) {
			CleanableEntity oneCleanable = (CleanableEntity) cleanIterator.next();
			mvsFileSets.addAll(oneCleanable.getMVSFileSets());
		}
		return mvsFileSets;
	}

	public Set getAllHeldJobs(){
		Set allHeldJobs = new HashSet();
		allHeldJobs.addAll(super.getAllHeldJobs());
		for (Iterator cleanIterator = cleanableSet.iterator(); cleanIterator.hasNext();) {
			CleanableEntity oneCleanable = (CleanableEntity) cleanIterator.next();
			if (oneCleanable!=null) {
				allHeldJobs.addAll(oneCleanable.getAllHeldJobs());
			}
		}
		return allHeldJobs;
	}

	public Map getAllMVSBuildIDs(){
		Map returnMVSBuildIds = new HashMap();
		returnMVSBuildIds.putAll(super.getAllMVSBuildIDs());
		for (Iterator cleanIterator = cleanableSet.iterator(); cleanIterator.hasNext();) {
			CleanableEntity oneCleanable = (CleanableEntity) cleanIterator.next();
			returnMVSBuildIds.putAll(oneCleanable.getAllMVSBuildIDs());
		}
		return returnMVSBuildIds;
	}

	public Set getAllLocalFiles(){
		Set allLocalFiles = new HashSet();
		allLocalFiles.addAll(getAllLocalFiles());
		for (Iterator cleanIterator = cleanableSet.iterator(); cleanIterator.hasNext();) {
			CleanableEntity oneCleanable = (CleanableEntity) cleanIterator.next();
			allLocalFiles.addAll(oneCleanable.getAllLocalFiles());
		}
		return allLocalFiles;
	}

	public Setup getSetup(){
		if (super.getSetup()!=null) {
			return super.getSetup();
		}else if (!cleanableSet.isEmpty()) {
			return ((CleanableEntity) cleanableSet.iterator().next()).getSetup();
		}
		return null;
	}

	public void removeDriverLock(String oneLock){
		super.removeDriverLock(oneLock);
		for (Iterator cleanIterator = cleanableSet.iterator(); cleanIterator.hasNext();) {
			CleanableEntity oneCleanable = (CleanableEntity) cleanIterator.next();
			oneCleanable.removeDriverLock(oneLock);
		}
	}

	public void removeMVSFileSet(String oneFileSet){
		super.removeMVSFileSet(oneFileSet);
		for (Iterator cleanIterator = cleanableSet.iterator(); cleanIterator.hasNext();) {
			CleanableEntity oneCleanable = (CleanableEntity) cleanIterator.next();
			oneCleanable.removeMVSFileSet(oneFileSet);
		}
	}

	public void removeHeldJob(MBJobInfo tempJob){
		super.removeHeldJob(tempJob);
		for (Iterator cleanIterator = cleanableSet.iterator(); cleanIterator.hasNext();) {
			CleanableEntity oneCleanable = (CleanableEntity) cleanIterator.next();
			oneCleanable.removeHeldJob(tempJob);
		}
	}

	public void removeMVSBuildID(String tempId){
		super.removeMVSBuildID(tempId);
		for (Iterator cleanIterator = cleanableSet.iterator(); cleanIterator.hasNext();) {
			CleanableEntity oneCleanable = (CleanableEntity) cleanIterator.next();
			oneCleanable.removeMVSBuildID(tempId);
		}
	}

	public void removeHeldJobs(Set tempHeldJobs){
		super.removeHeldJobs(tempHeldJobs);
		for (Iterator cleanIterator = cleanableSet.iterator(); cleanIterator.hasNext();) {
			CleanableEntity oneCleanable = (CleanableEntity) cleanIterator.next();
			oneCleanable.removeHeldJobs(tempHeldJobs);
		}
	}

	public void removeLocalFile(File oneFile){
		super.removeLocalFile(oneFile);
		for (Iterator cleanIterator = cleanableSet.iterator(); cleanIterator.hasNext();) {
			CleanableEntity oneCleanable = (CleanableEntity) cleanIterator.next();
			oneCleanable.removeLocalFile(oneFile);
		}
	}

	public void clearAllHeldJobs(){
		super.clearAllHeldJobs();
		for (Iterator cleanIterator = cleanableSet.iterator(); cleanIterator.hasNext();) {
			CleanableEntity oneCleanable = (CleanableEntity) cleanIterator.next();
			oneCleanable.clearAllHeldJobs();
		}
	}

	public void clearMVSFileSets(){
		super.clearMVSFileSets();
		for (Iterator cleanIterator = cleanableSet.iterator(); cleanIterator.hasNext();) {
			CleanableEntity oneCleanable = (CleanableEntity) cleanIterator.next();
			oneCleanable.clearMVSFileSets();
		}
	}

	public void clearDriverLocks(){
		super.clearDriverLocks();
		for (Iterator cleanIterator = cleanableSet.iterator(); cleanIterator.hasNext();) {
			CleanableEntity oneCleanable = (CleanableEntity) cleanIterator.next();
			oneCleanable.clearDriverLocks();
		}
	}

	public void clearMVSBuildIDs(){
		super.clearMVSBuildIDs();
		for (Iterator cleanIterator = cleanableSet.iterator(); cleanIterator.hasNext();) {
			CleanableEntity oneCleanable = (CleanableEntity) cleanIterator.next();
			oneCleanable.clearMVSBuildIDs();
		}
	}

	public void clearLocalFiles(){
		super.clearLocalFiles();
		for (Iterator cleanIterator = cleanableSet.iterator(); cleanIterator.hasNext();) {
			CleanableEntity oneCleanable = (CleanableEntity) cleanIterator.next();
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
		return super.toString() + "\n"+cleanableSet.toString();
	}
}
