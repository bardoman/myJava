package com.ibm.sdwb.build390;

import java.io.File;
/** changes                   description
/** 04/19/2000           changed the class to public to allow it to accessed in package LogProcess
/** 05/12/2000           changed the class to send error mesg of 5000 or any other host error msg
*/

public class HostError extends MBBuildException {
    String fileNameBeginning = null;
	private com.ibm.sdwb.build390.mainframe.MainframeOutputSourceInterface fileSource = null;

    public HostError(String errorMessage) {
        super("Host Error", errorMessage, MBConstants.HOSTERROR);
    }

    public HostError(String errorMessage, Exception e) {
        super("Host Error", errorMessage, e, MBConstants.HOSTERROR);
    }

    public HostError(String errorMessage, String tempFileNameBeginning) {
        super("Host Error", errorMessage, MBConstants.HOSTERROR);
        fileNameBeginning = tempFileNameBeginning;
    }

    public HostError(String errorMessage, Exception e, String tempFileNameBeginning) {
        super("Host Error", errorMessage, e, MBConstants.HOSTERROR);
        fileNameBeginning = tempFileNameBeginning;
    }

    public HostError(String errorMessage, String tempFileNameBeginning,int RC) {
        super("Host Error", errorMessage, RC);
        fileNameBeginning = tempFileNameBeginning;
    }

	public HostError(String errorMessage, com.ibm.sdwb.build390.mainframe.MainframeOutputSourceInterface tempFileSource){
        super("Host Error", errorMessage, MBConstants.HOSTERROR);
		fileSource = tempFileSource;
	}
    public HostError(String errorMessage, Exception e, com.ibm.sdwb.build390.mainframe.MainframeOutputSourceInterface tempFileSource) {
        super("Host Error", errorMessage, e, MBConstants.HOSTERROR);
		fileSource = tempFileSource;
    }

    public HostError(String errorMessage,  com.ibm.sdwb.build390.mainframe.MainframeOutputSourceInterface tempFileSource,int RC) {
        super("Host Error", errorMessage, RC);
		fileSource = tempFileSource;
    }

	public File getOutputFile(){
		if (fileSource!=null) {
			return fileSource.getOutputFile();
		}else if (fileNameBeginning !=null) {
			return new File(fileNameBeginning+MBConstants.CLEARFILEEXTENTION);
		}
		return null;
	}

	public File getPrintFile(){
		if (fileSource!=null) {
			return fileSource.getPrintFile();
		}else if (fileNameBeginning !=null) {
			return new File(fileNameBeginning+MBConstants.PRINTFILEEXTENTION);
		}
		return null;
	}

	public com.ibm.sdwb.build390.mainframe.MainframeOutputSourceInterface getOutputSource(){
		return fileSource;
	}
}
