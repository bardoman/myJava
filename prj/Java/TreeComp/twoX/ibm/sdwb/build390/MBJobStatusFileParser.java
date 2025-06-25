package com.ibm.sdwb.build390;

import java.io.*;
import java.util.*;

public class MBJobStatusFileParser {
    Hashtable statusHash = new Hashtable();

    public MBJobStatusFileParser(File statusFile) throws com.ibm.sdwb.build390.MBBuildException {
        String currentLine;
        String jobName;
        String jobStatus;
        int endOfJobName = 0;

        if (statusFile.exists()) {
            try {
                BufferedReader statusFileReader = new BufferedReader(new FileReader(statusFile));
                while ((currentLine = statusFileReader.readLine()) != null) {
                    currentLine = currentLine.trim();
                    endOfJobName = currentLine.indexOf(" ");
                    jobName = currentLine.substring(0, endOfJobName);
                    jobStatus = currentLine.substring(endOfJobName, currentLine.length());
                    statusHash.put(jobName, jobStatus.trim());
                }
            }catch (IOException ioe) {
                throw new GeneralError("There was an error reading the statusFile "+statusFile, ioe);
            }
        } else {
            throw new GeneralError("The job status file " + statusFile+" was not found", new Exception());
        }
    }

    public String getJobStatus(String jobName) {
        return (String) statusHash.get(jobName);
    }

    public Enumeration getJobs() {
        return statusHash.keys();
    }
}
