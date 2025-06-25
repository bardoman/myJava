package com.ibm.sdwb.build390;

// 05/13/00	Ken				Handle migrated CODEPAGE tables that kent auto recalls.
import java.io.*;
import java.util.*;
import com.ibm.sdwb.build390.logprocess.*;
import com.ibm.sdwb.build390.info.FileInfo;

class MBUploadFile {

    private String hostPattern;
    private MBBuild build;
    private MBFtp ftpObject;
	private FileInfo fileInfo = null;
    private boolean stopped = false ;
    private boolean isLocalFile = false;
    private boolean inFTP = false;
	private boolean inExtract = false;
    private boolean wasUploadSuccessful = false;
    private boolean finished = false;
    private LogEventProcessor lep=null;
    private boolean retryMigratedCodepage = false;
    private static final int maxCodepageRetries = 100;
    private static final int ftpSleepPeriod = 60000;
	private Exception ftpError = null;

    MBUploadFile(FileInfo tempFile, MBBuild tempBuild, String tempHostPattern, LogEventProcessor lep) {
        hostPattern = tempHostPattern;
        fileInfo = tempFile;
        build = tempBuild;
        this.lep=lep;
    }

    public void setRetryMigratedCodepage(boolean temp){
        retryMigratedCodepage = temp;
    }

    public void run() {
        lep.LogSecondaryInfo("Debug", "MBUploadFile:run:Entry");
		isLocalFile = fileInfo.getLocalFile() !=null;
		if (build instanceof com.ibm.sdwb.build390.MBUBuild) {
			if (((com.ibm.sdwb.build390.process.UserBuildProcess) build.getProcessForThisBuild()).isPDSBuild()) {
				wasUploadSuccessful = true;
				return;
			}
		}
        boolean ftpSuccessful = false;
		fileInfo.setUploaded(false);
        String temporaryMainframeFilename = null;
        try {
            inExtract = true;
            if (stopped) {
                throw new StopError();
            }

			temporaryMainframeFilename = fileInfo.getMainframeFilename();
			int patternIndex = hostPattern.indexOf(MBConstants.FILENAMEPLACEHOLDER);
			String remoteFilename = hostPattern.substring(0, patternIndex) + temporaryMainframeFilename + hostPattern.substring(patternIndex+MBConstants.FILENAMEPLACEHOLDER.length());
			if (!isLocalFile){
				ftpObject = new MBFtp(build.getSetup().getMainframeInfo(), lep, true);
				ftpSuccessful = ftpObject.put(fileInfo, remoteFilename, false);
				String fileType = "A";
				if(fileInfo.isBinary()) {
					fileType = "I";
				}
				inExtract = true;
				fileInfo.setFTPArguments(ftpObject.getFTPArgs());
				build.getLibraryInfo().doCopy(fileInfo, build.getMainframeInfo(), remoteFilename); 
				inExtract = false;
			}else{
				boolean codepageErrorFound = false;
				int ftpTryCount = 0;
				do {
					codepageErrorFound = false;
					ftpSuccessful = false;
					try {
						ftpObject = new MBFtp(build.getSetup().getMainframeInfo(), lep);
						ftpSuccessful = ftpObject.put(fileInfo, remoteFilename, false);
						lep.LogSecondaryInfo("Debug", "MBUploadFile:run:After ftp class ");
					} catch (FtpError fe) {
						lep.LogSecondaryInfo("Debug", "MBUploadFile:run:inside ftperror ");
						if ((fe.getMessage().indexOf(MBFtp.CODEPAGENOTFOUND)>-1) && retryMigratedCodepage) {
							codepageErrorFound = true;
							try {
								Thread.currentThread().sleep(ftpSleepPeriod);
							} catch (InterruptedException ie) {

							}
						} else {
							lep.LogException(fe);
							throw fe;
						}
					}

					lep.LogSecondaryInfo("Debug","MBUploadFile:run:ftptryCount ="+ftpTryCount);
					ftpTryCount++;
				} while ( codepageErrorFound && (ftpTryCount < maxCodepageRetries) );
				lep.LogSecondaryInfo("Debug", "MBUploadFile:run:After extractFile:temporaryMainframeFilename="+temporaryMainframeFilename);
				if (stopped) {
					throw new StopError();
				}
			}
			if (!stopped) {
				fileInfo.setUploaded(true);
				wasUploadSuccessful = true;
			}
        } catch (MBBuildException mbe) {
            if (!stopped) {
                wasUploadSuccessful=false;
                // pjs - Don't cause a popup window here, just log it and let the process run
                // pjs - Errors will be caught at the end of the upload process
                mbe.userMessage += "Uploading " + fileInfo.getDirectory()+fileInfo.getName() + " to " + temporaryMainframeFilename + ":"+mbe.getUserMessage();
				ftpError = mbe;
            }
        } catch (Exception e) {
            // lep.LogException(e);
            if (!stopped) {
                wasUploadSuccessful=false;
                // pjs - Don't cause a popup window here, just log it and let the process run
                // pjs - Errors will be caught at the end of the upload process
                ftpError = new FtpError("An error occurred attempting to extract and upload " + fileInfo.getDirectory()+fileInfo.getName() + " to " + temporaryMainframeFilename+".", e);
            }
        } finally {
            finished = true;
            hostPattern=null;
            build=null;
            ftpObject=null;
        }
        lep.LogSecondaryInfo("Debug", "MBUploadFile:run:Exit");
    }

    public Exception getUploadError() {
        return ftpError;
    }

    public boolean isFinished() {
        return finished;
    }

    public void stopUpload() throws com.ibm.sdwb.build390.MBBuildException {
        if (inFTP) {
            lep.LogSecondaryInfo("Debug", "MBUploadFile:Stopping FTP of " + fileInfo.getDirectory()+fileInfo.getName());

        }
        if (inExtract) {
            lep.LogSecondaryInfo("Debug:", "MBUploadFile:Stopping extract of " + fileInfo.getDirectory()+fileInfo.getName());
        }
        stopped = true;
        if (ftpObject != null ) {
            ftpObject.stop();
        }
        ftpObject = null;
    }
}
