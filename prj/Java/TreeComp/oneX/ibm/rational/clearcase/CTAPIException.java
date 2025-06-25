package com.ibm.rational.clearcase;

import java.io.File;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.logging.*;

public  class CTAPIException extends Exception
{
    Exception origException = null;
    String message = "";

    public CTAPIException(String message, Exception origException,Logger logger)
    {
        super(origException.getMessage());
        this.origException = origException;
        this.message = message;

        logger.log(Level.SEVERE,message+"\n"+getStack(origException), origException);
    }

    public CTAPIException(String message,Logger logger) 
    {
        super(message);
        this.origException = origException;
        this.message = message;

        logger.log(Level.SEVERE,message+getStack(this));
    }

    private String getStack(Exception e)
    {
        String str="";
        StackTraceElement elems[] =e.getStackTrace();

        for(int i=0;i!=elems.length;i++)
        {
            str+=elems[i].toString()+"\n";
        }

        return str;
    }

    public Exception getOriginalException()
    {
        return origException;
    }

    public String getMessage()
    {
        return super.getMessage()+":" + message;
    }

    public String toString()
    {
        String stringForm = super.toString()+"\n";
        if(origException != null)
        {
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


