package com.ibm.sdwb.build390.userinterface.text.commandline.arguments;

import java.util.*;

public class TransmitOutputToUserID extends CommandLineArgument {

    private static final String keyword = "XMITTO";
    private static final String explaination = "\n<node.userid>" +
                                               "\nTransmit objects to node.userid.\nRequires XMITTYPE";


    public TransmitOutputToUserID(){
        super(keyword, explaination);
    }
}
