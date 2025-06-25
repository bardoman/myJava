package com.ibm.sdwb.build390.userinterface.text.commandline.arguments;

public class ProcessID extends CommandLineArgument
{
    private static final String keyword = "PROCESSID";
    private static final String explaination = "\nSpecifies the identification string of a process."; 

    public ProcessID()
    {
        super(keyword,explaination);
    }
}
