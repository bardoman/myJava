package com.ibm.sdwb.build390;

import java.io.File;
import java.io.PrintWriter;
import java.io.StringWriter;

// 3/5/99   error message       simplify error message handling.

public abstract class MBBuildException extends Exception {
    int returnCode = -1;
    Exception origException = null;
    String userMessage = null;
    File errorFile = null;

    public MBBuildException(String errorMessage, String tempUM, int tempReturnCode) {
        super(errorMessage);
        returnCode = tempReturnCode;
        userMessage = tempUM;
    }

    public MBBuildException(String errorMessage, String tempUM, Exception tempException) {
        super(errorMessage);
        origException = tempException;
        userMessage = tempUM;
    }

    public MBBuildException(String errorMessage, String tempUM, Exception tempException, int tempReturnCode) {
        super(errorMessage);
        returnCode = tempReturnCode;
        origException = tempException;
        userMessage = tempUM;
    }

    public MBBuildException(Exception tempException, int tempReturnCode) {
        super();
		origException = tempException;
		returnCode = tempReturnCode;
    }

	public MBBuildException(String errorMessage){
		super(errorMessage);
		returnCode = MBConstants.GENERALERROR;
	}

    public void setErrorFile(File tempFile) {
        errorFile = tempFile;
    }

    public int getReturnCode() {
        return returnCode;
    }

    public String getUserMessage() {
        return userMessage;
    }

    public File getErrorFile(){
        return errorFile;
    }

    public Exception getOriginalException() {
		return origException;
    }

	public String getMessage(){
		return super.getMessage()+":" + getUserMessage();
	}

	
	/**
	 * redirect print stack trace calls to the PrintWriter 
	 * stack trace handler.   If this method needs updating,
	 * it should be done by updating 
	 * printStackTrace(PrintWriter p)
	 * which is why it's marked final
	 */
	public final void printStackTrace(){
		printStackTrace(new java.io.PrintWriter(System.err));
	}

	/**
	 * redirect print stack trace calls to the printWriter 
	 * stack trace handler.   If this method needs updating,
	 * it should be done by updating 
	 * printStackTrace(PrintWriter p)
	 * which is why it's marked final
	 * 
	 * @param stackStream
	 */
	public final void printStackTrace(java.io.PrintStream stackStream){
		printStackTrace(new java.io.PrintWriter(stackStream));
	}

	public String toString(){
		String stringForm = super.toString()+"\n";
		if (origException != null) {
			StringWriter exceptionString = new StringWriter();
			PrintWriter exceptionHolder = new PrintWriter(exceptionString);
			origException.printStackTrace(exceptionHolder);
			exceptionHolder.flush();
			stringForm += "\nOriginal Exception:\n"+exceptionString+"\n";
			exceptionHolder.close();
		}
		return stringForm;
	}
}


