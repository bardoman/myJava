package com.ibm.sdwb.build390.test;

import com.ibm.sdwb.build390.process.*;
import java.io.*;

//***************************************************************
// Java TestNotifyListener class for Build/390 client             
//***************************************************************
// 03/07/2000 #FEAT.INT1178:  Creation
//***************************************************************

public class TestNotifyListener implements ProcessActionListener, Serializable {

    public void handleProcessCompletion(AbstractProcess proc)
    {
        if(proc instanceof TestInfoGenerator) {
            ((TestInfoGenerator)proc).sendTestInformation();                                                                              
        }
    }
}
