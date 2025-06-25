package com.ibm.sdwb.build390.utilities;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;

import com.ibm.sdwb.build390.GeneralError;
import com.ibm.sdwb.build390.HostError;
import com.ibm.sdwb.build390.MBBuildException;
import com.ibm.sdwb.build390.MBConstants;

/*09/15/2003 TST1558 - show hosterrors, when multipleconcurrentexception occurs. **/

public class MultipleConcurrentException extends MBBuildException {

    /** TST1558 **/
    private transient Collection exceptionsThatOccurred = null;// it's a sorted set so it will be displayed the same each time.


    public MultipleConcurrentException(String mainMessage) {
        super(mainMessage);
        exceptionsThatOccurred = new ArrayList();
    }

    /** TST1558 **/
    public void addMultipleConcurrentException(MBBuildException mce) {
        if (((MultipleConcurrentException)mce).getExceptionSet()!=null) {
            exceptionsThatOccurred.addAll(((MultipleConcurrentException)mce).getExceptionSet());
        }
    }

    public void addException(Exception oneException) {
        exceptionsThatOccurred.add(oneException);
    }


    /** TST1558 **/
    public Collection getExceptionSet() {
        java.util.Collections.sort((List)exceptionsThatOccurred,new ExceptionSorter());
        return exceptionsThatOccurred;
    }

    public String getMessage() {
        String returnString = "The following messages were encountered:\n";
        for (Iterator exceptionIterator = exceptionsThatOccurred.iterator(); exceptionIterator.hasNext(); ) {
            Exception oneException = (Exception) exceptionIterator.next();
            com.ibm.sdwb.build390.logprocess.ExceptionMessageDecrypter exceptionDecrypter = new com.ibm.sdwb.build390.logprocess.ExceptionMessageDecrypter(oneException);
            returnString += exceptionDecrypter.getMessageFromDecrypter()+".\n";
        }
        return returnString;
    }

    public String getUserMessage() {
        String returnString = "The following user messages were encountered:\n";
        for (Iterator exceptionIterator = exceptionsThatOccurred.iterator(); exceptionIterator.hasNext(); ) {
            Exception oneException = (Exception) exceptionIterator.next();
            com.ibm.sdwb.build390.logprocess.ExceptionMessageDecrypter exceptionDecrypter = new com.ibm.sdwb.build390.logprocess.ExceptionMessageDecrypter(oneException);
            returnString += exceptionDecrypter.getUserMessageFromDecrypter()+".\n";
            if (oneException instanceof com.ibm.sdwb.build390.HostError) {
                com.ibm.sdwb.build390.HostError hE = (com.ibm.sdwb.build390.HostError)oneException;
                if (hE.getPrintFile() != null) {
                    returnString += "The associated files are " + hE.getPrintFile().getAbsolutePath()+"  and " + hE.getOutputFile().getAbsolutePath()+".\n\n";
                }
            }
        }

        return returnString;

    }


    public int getReturnCode() {
        for (Iterator exceptionIterator = exceptionsThatOccurred.iterator(); exceptionIterator.hasNext(); ) {
            Exception oneException = (Exception) exceptionIterator.next();
            if (oneException instanceof com.ibm.sdwb.build390.HostError) {
                return ((HostError)oneException).getReturnCode();
            }
        }
        return MBConstants.GENERALERROR;
    }


    public void printStackTrace(java.io.PrintWriter stackWriter) {
        String totalTrace = new String();
        for (Iterator exceptionIterator = exceptionsThatOccurred.iterator(); exceptionIterator.hasNext(); ) {
            Exception oneException = (Exception) exceptionIterator.next();
            com.ibm.sdwb.build390.logprocess.ExceptionMessageDecrypter exceptionDecrypter = new com.ibm.sdwb.build390.logprocess.ExceptionMessageDecrypter(oneException);
            totalTrace += exceptionDecrypter.getStackTraceFromDecrypter()+"\n\n";
        }
        stackWriter.print(totalTrace);
    }

    public String toString() {
        String stringForm = super.toString()+"\nExceptions Encountered:";
        StringWriter exceptionString = new StringWriter();
        PrintWriter exceptionHolder = new PrintWriter(exceptionString);
        for (Iterator exceptionIterator = exceptionsThatOccurred.iterator(); exceptionIterator.hasNext(); ) {
            Exception oneException = (Exception) exceptionIterator.next();
            oneException.printStackTrace(exceptionHolder);
            exceptionHolder.flush();
        }
        stringForm += "\n"+exceptionString+"\n";
        exceptionHolder.close();
        return stringForm;
    }

    private class ExceptionSorter implements Comparator {

        public int compare(Object o1, Object o2) throws ClassCastException {
            if (o1 == null & o2 == null) {
                return 0;
            }
            if (o1 instanceof Exception & o2 instanceof Exception) {
                return o1.toString().compareTo(o2.toString());
            } else {
                throw new ClassCastException("Received something besides an exception.");
            }


        }
        public boolean equals(Object obj) {
            if (obj != null ) {
                if (obj instanceof ExceptionSorter) {
                    return true;
                }
            }
            return false;
        }
    }
}

