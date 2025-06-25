package com.ibm.sdwb.build390;

import java.io.*;
import java.util.*;
import com.ibm.sdwb.build390.logprocess.*;
import com.ibm.sdwb.build390.info.FileInfo;
import com.ibm.sdwb.build390.userinterface.graphic.MainInterface;
import com.ibm.sdwb.build390.process.management.*;

//***********************************************************************************************************************************
/** <br>The MBUpdateFiles class provides support to update files in MVS. */
// 04/27/99 errorHandling       change LogException parms & add new error types
// 09/03/99 dates		add date criteria to tell if we uploaded a file or not.
// 09/20/99                     replace string mainframe with 'build/390 server'
// 11/01/99	updated logging
// 11/23/99 pjs - ftp errors are not shown to user anymore so that upload will continue, so tell user to check log file
// 01/07/2000 ind.build.log    changes for logging in the build details into a individual build log.
// 03/07/2000 reworklog        changes for implementing the build stuff using listeners.
// 05/22/2000 reworklog        changes for to pass lep object in the constructor of MBUploadFile class
// 08/11/2000 oeclient         The threadgroup'sactivecount is now checked for those threads that have the name starting with "Upload Thread -"
// 05/16/2001 defect 408       nullpointer during fastrack
//***********************************************************************************************************************************

public class MBUpdateFiles implements Haltable {//TST3169
    private static final int MAXBUFFEREDWRITEREQUESTS = 5;
    private static final int CONCURRENTUPLOADS = 5;
    private boolean stopped = false;
    private boolean   gui_  = false;
    private MBStatus status;
    private int threadNumber = 0;
    private static int logRequests = 0;
    private int numberOfUploadedFiles = 0;
    Vector loadFile = new Vector();
    Hashtable partlistHash = new Hashtable();
    ThreadGroup groupOfUpdates = new ThreadGroup("File Update Group");
    private static LogEventProcessor lep=null;
    private Vector migratedDatasets = new Vector();


    public MBUpdateFiles(MBStatus tempStatus,LogEventProcessor lep) {
        status = tempStatus;
        lep.LogSecondaryInfo("Debug","MBUpdateFiles:Constructor1:Entry");
        this.lep=lep;
        if(MainInterface.getInterfaceSingleton()!= null) gui_ = true;
    }

    public MBUpdateFiles(MBStatus tempStatus, Hashtable tempHash, LogEventProcessor lep) {
        status = tempStatus;
        this.lep=lep;
        lep.LogSecondaryInfo("Debug","MBUpdateFiles:Constructor2:Entry");
        partlistHash = tempHash;
        if(MainInterface.getInterfaceSingleton()!= null) gui_ = true;
    }

    public void setMigratedDatasets(Vector tempMigData) {
        migratedDatasets=tempMigData;
    }

    public void updateMVSFiles(MBBuild build, Collection filesToUpload, String hostPattern, String processType) throws com.ibm.sdwb.build390.MBBuildException {
        BufferedReader logReader;
        String logFilePath;
        File testDirectory;
        lep.LogSecondaryInfo("Debug","MBUpdateFiles:updateMVSFiles:Entry");
        int i = 0;
        int total = filesToUpload.size();
        int cnt = 0;
        // if in gui mode, tell the user status
        if(gui_) {
            cnt++;
            String statusMsg = new String("Uploading "+total+" parts");
            lep.LogSecondaryInfo("Debug","MBUpdateFiles:updateMVSFiles:"+statusMsg);
            status.updateStatus(statusMsg, false);
        }
        for(Iterator filesToUploadIterator = filesToUpload.iterator(); filesToUploadIterator.hasNext() & !stopped; ) {
            FileInfo oneFile = (FileInfo) filesToUploadIterator.next();
            String filenameString = oneFile.getDirectory()+oneFile.getName();
            boolean needsUpload = !oneFile.isUploaded();
            if(build instanceof MBUBuild) {
                if(((MBUBuild) build).getFastTrack()) {
                    needsUpload = true;
                }
            }
            if(needsUpload) {
                MBUploadFile tempUpload = new MBUploadFile(oneFile, build, hostPattern, lep);
                tempUpload.setRetryMigratedCodepage(isCodepageMigrated(oneFile.getMainframeCodepage()));
                loadFile.addElement(tempUpload);
            }
            i++;
        }
        numberOfUploadedFiles = 0;
        for(threadNumber = 0; threadNumber < CONCURRENTUPLOADS & threadNumber < loadFile.size(); threadNumber++) {
            //the below code is for USS .. the uss for some reason doesnt kick off the 
            //run method in the Thread class if your create a thread like this
            // (new UploadThread(...)).start.
            lep.LogSecondaryInfo("Debug","MBUpdateFiles:updateMVSFiles:spawningUploadThread ");
            Thread spawnUploadThread =new uploadThread(threadNumber, loadFile, groupOfUpdates);
            spawnUploadThread.start();
        }
        synchronized(loadFile) {
            //now the activecount of threads are the threads which have names starting with "Upload Thread -"
            while(getUploadThreadsActive(groupOfUpdates) > 0) {
                try {
                    loadFile.wait(10000);
                }
                catch(InterruptedException ie) {
                    lep.LogException("An interruption occurred while waiting for updates to complete", ie);
                }
                String statusMsg = new String(((total-loadFile.size()) + numberOfUploadedFiles)+" files of "+total+" uploaded");
                status.updateStatus(statusMsg, false);
            }
        }

        lep.LogSecondaryInfo("Debug", "MBUpdateFiles:updateMVSFiles : the activeCount switch after the while   loop = " + getUploadThreadsActive(groupOfUpdates));
        java.util.List exceptionList = new java.util.ArrayList();
        for(int i2 = 0; i2 < loadFile.size(); i2++) {
            MBUploadFile temp = (MBUploadFile) loadFile.elementAt(i2);
            if(temp.getUploadError()!=null) {
                exceptionList.add(temp.getUploadError());
            }
        }
        if(!exceptionList.isEmpty()) {
            for(java.util.Iterator exceptionIterator = exceptionList.iterator(); exceptionIterator.hasNext();) {
                lep.LogException("Error uploading file:",(Exception) exceptionIterator.next());
            }
            throw new FtpError("There were errors uploading files to the Build/390 Server.\nCheck the log file for details.");
        }
    }

    private boolean isCodepageMigrated(String codepage) {
        for(int i = 0; i < migratedDatasets.size(); i++) {
            if(codepage != null) {
                if(codepage.indexOf((String) migratedDatasets.elementAt(i))>-1) {
                    return true;
                }
            }
            else {
                return false;
            }
        }
        return false;
    }

    public void haltProcess() throws com.ibm.sdwb.build390.MBBuildException {//TST3169
        stopped = true;
        for(int i = 0; i < loadFile.size(); i++) {
            MBUploadFile temp = (MBUploadFile)loadFile.elementAt(i);
            temp.stopUpload();
        }
    }
    private int getUploadThreadsActive(ThreadGroup tg) {
        int active = 0;
        Thread[] tempThreads  = new Thread[tg.activeCount()];;
        tg.enumerate(tempThreads);
        for(int i = 0; i < tempThreads.length; i++) {
            Thread currThread = tempThreads[i];
            if(currThread!=null) {
                String threadName = currThread.getName();
                if(threadName != null) {
                    if(threadName.startsWith("Upload Thread - ")) {
                        active++;
                    }
                }
            }
        }
        lep.LogSecondaryInfo("Debug", "MBUpdateFiles:getUploadThreadActive : Active Count = " + active);
        return active;
    }

    class uploadThread extends Thread {
        private int number = 0;
        private Vector fileSource;
        private ThreadGroup parent;


        uploadThread(int tempNumber, Vector tempFileSource, ThreadGroup tempParent) {
            super(tempParent, "Upload Thread - "+ new Integer(tempNumber).toString());
            lep.LogSecondaryInfo("Debug","uploadThread:Constructor :Entry");
            number = tempNumber;
            fileSource = tempFileSource;
            parent = tempParent;
            lep.LogSecondaryInfo("Debug","uploadThread:Constructor :Exit");
        }


        public void run() {
            lep.LogSecondaryInfo("Debug","uploadThread:run :method :Entry");
            for(int i2=0; i2 < (1+(loadFile.size()/CONCURRENTUPLOADS)) & (i2*CONCURRENTUPLOADS+number) < (loadFile.size()) & !stopped; i2++) {
                lep.LogSecondaryInfo("Debug","uploadThread:run : "+ fileSource.toString());
                MBUploadFile tempUpload = (MBUploadFile) fileSource.elementAt(i2*CONCURRENTUPLOADS+number);
                tempUpload.run();
                lep.LogSecondaryInfo("Debug","uploadThread:run:after tempUpload run : ");
                if(tempUpload.getUploadError()==null) {
                    numberOfUploadedFiles++;
                }
                synchronized(fileSource) {
                    fileSource.notifyAll();
                }
            }
            lep.LogSecondaryInfo("Debug","uploadThread:run :method :Exit");
        }
    }


    //Begin TST3169
    public boolean isHaltable() {
        return true;
    }
    //End TST3169

}

class extractMember {
    public MBBuild build;
    public Vector filesToExtract;

    extractMember(MBBuild tempBuild, Vector tempFilesToExtract) {
        filesToExtract = tempFilesToExtract;
        build = tempBuild;
    }

    class SerializableThreadGroup extends ThreadGroup implements java.io.Serializable {
        public SerializableThreadGroup(String name) {
            super(name);
        }

        public SerializableThreadGroup(ThreadGroup parent, String name) {
            super(parent, name);
        }
    }
}
