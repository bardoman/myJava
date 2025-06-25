package com.ibm.rational.clearcase;

import java.io.*;
import java.util.*;
import java.util.logging.*;

public abstract class Command
{
    private static int LOGFILESIZELIMIT = 10000000;
    private static int THREADLIMIT = 20;
    private static int LOGFILECOUNT = 3;
    private static FileHandler fileHand;
    protected static Logger logger;
    private static ThreadLimit threadLimit = new ThreadLimit(THREADLIMIT);

    static
    {
        try
        {
            fileHand = new FileHandler("Command.log",LOGFILESIZELIMIT,LOGFILECOUNT,true);

            logger = Logger.getLogger("com.ibm.rational.clearcase.Command");
            
            fileHand.setLevel(Level.ALL);

            fileHand.setFormatter(new SimpleFormatter());

            Handler handlers[] = logger.getHandlers();

            for(int i=0;i!=handlers.length;i++)
            {
                System.out.println("Hand"+i+":="+handlers[i]);
            }

            logger.addHandler(fileHand);

            logger.setLevel(Level.ALL);

            logger.setUseParentHandlers(false);
        }
        catch(IOException ioe)
        {
            ioe.printStackTrace();
        }
    }

    protected static String execute(String[] tempCmd) 
    throws CTAPIException 
    {
        return execute(tempCmd,null);
    }

    protected static String execute(String[] tempCmd, String ExecPath) 
    throws CTAPIException 
    {
        logger.entering("Command","runCommand");

        String cmd[]= null;

        cmd = new String[tempCmd.length+1];

        cmd[0]= "cleartool";

        System.arraycopy(tempCmd,0,cmd,1,tempCmd.length);

        Process proc;                        
        BufferedReader in;                    
        BufferedReader err;                   

        AsynchReader errReader = null;
        AsynchReader inReader = null;
        String output = null;

        String traceBuffer = "Command: ";

        for(int i = 0; i < cmd.length; i++)
        {
            traceBuffer+=cmd[i] + " ";
        }

        logger.info(traceBuffer);

        threadLimit.waitCounter();

        try
        {
            try
            {
                if(ExecPath == null)
                {
                    proc = Runtime.getRuntime().exec(cmd);
                }
                else
                {
                    File ExecDir = new File(ExecPath);

                    proc = Runtime.getRuntime().exec(cmd, null, ExecDir);
                }
            }
            catch(IOException e)
            {
                throw new CTAPIException("There was a problem calling ClearTool " + traceBuffer, e, logger);
            }

            err = new BufferedReader(new InputStreamReader(proc.getErrorStream()));

            errReader = new AsynchReader(err);

            in = new BufferedReader(new InputStreamReader(proc.getInputStream()));

            inReader = new AsynchReader(in);

            errReader.join();

            inReader.join();

            in.close();

            err.close();

            in = null;

            err = null;

            output = inReader.getRead();

            if(output == null)
            {
                output = new String();
            }

            proc.waitFor();

            proc = null;
        }
        catch(IOException ioe)
        {
            throw new CTAPIException("An error occurred while closing the stream to the ClearTool process", ioe,logger);
        }
        catch(Exception ne)
        {
            throw new CTAPIException("An interruption occurred while waiting for the ClearTool process to terminate", ne,logger);
        }

        finally
        {
            threadLimit.notifyCounter();
        }

        if(errReader.getRead() != null)
        {
            throw new CTAPIException(errReader.getRead(),logger);
        }
        if(errReader.getEncounteredException()!=null)
        {
            throw new CTAPIException("An error occurred reading the error stream from "+proc.toString(), errReader.getEncounteredException(),logger);
        }
        if(inReader.getEncounteredException()!=null)
        {
            throw new CTAPIException("An error occurred reading the error stream from "+proc.toString(), inReader.getEncounteredException(),logger);
        }

        logger.entering("Command","runCommand");

        return output;
    }
}