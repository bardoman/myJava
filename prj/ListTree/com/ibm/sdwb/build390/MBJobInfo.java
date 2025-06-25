package com.ibm.sdwb.build390;

// sdwb1210 - add support for failed listings to a file system, add class and mod
// sdwb1120 - add support for build failure notification added filePath

public class MBJobInfo implements java.io.Serializable{
    public String jobName;
    public String fileName;
    public String fileVersion;
    public String fileClass;
    public String fileMod;
    private String filePath;
	private  String jobStatus = null;
	private int jobReturnCode = -1;
	private boolean succeeded = true;
	private boolean complete = false;

    public MBJobInfo(){
        jobName = new String();
        fileName = new String();
        fileVersion = new String();
        fileClass = new String();
        fileMod = new String();
    }

    public MBJobInfo(String jName, String fName, String fVersion){
        jobName = jName;
        fileName = fName;
        fileVersion = fVersion;
    }

    public MBJobInfo(String jName, String fName, String fVersion, String fClass, String fMod){
        jobName = jName;
        fileName = fName;
        fileVersion = fVersion;
        fileClass = fClass;
        fileMod = fMod;
    }


    public MBJobInfo(String jName, String fName, String fVersion, String fClass, String fMod,String fpath){
        jobName = jName;
        fileName = fName;
        fileVersion = fVersion;
        fileClass = fClass;
        fileMod = fMod;
        filePath = fpath;
    }


    public String getfileName() {
        return fileName;
    }

    public String getfileClass() {
        return fileClass;
    }

    public String getfileMod() {
        return fileMod;
    }

    public String getfilePath() {
        return filePath;
    }

	public String getJobStatus(){
		return jobStatus;
	}

	public int getJobReturnCode(){
		return jobReturnCode;
	}

	public String getJobName(){
		return jobName;
	}

	public boolean isSuccessful(){
		return succeeded;
	}

	public boolean isComplete(){
		return complete;
	}

	public void setJobStatus(String newStatus){
		jobStatus = newStatus;
	}

	public void setJobReturnCode(int newRC){
		jobReturnCode = newRC;
	}

	public void setSucceeded(boolean tempSucceeded){
		succeeded = tempSucceeded;
	}

	public void setComplete(){
		complete = true;
	}

	public boolean equals(Object obj){
		if (obj == null) {
			return false;
		}else if (obj instanceof MBJobInfo) {
			return jobName.equals(((MBJobInfo)obj).jobName);
		}
		return false;
	}


    public String toString() {
        return("{"+jobName+","+fileName+","+fileVersion+","+getfileClass()+","+getfileMod()+","+getfilePath()+"}");
    }
}
