package com.ibm.sdwb.build390.library;

import java.rmi.*;

public class RemoteExceptionWithLog extends java.rmi.RemoteException{
	String logString = null;

	public RemoteExceptionWithLog(String message, String log){
		super(message);
		logString = log;
	}
	
	public RemoteExceptionWithLog(String message, Throwable t, String log){
		super(message, t);
		logString = log;
	}

	public RemoteExceptionWithLog(Exception re, String log){
		super(re.getMessage(), re);
		logString = log;
	}

	public RemoteExceptionWithLog(RemoteException re, String log){
		super(re.getMessage(), re.detail);
		logString = log;
	}

	public String getLog(){
		return logString;
	}
}
