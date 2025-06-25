package com.ibm.sdwb.build390.process;

/*****************************************************************************************************/
/* Java ProcessActionListener Interface for Build/390 client                                                                */
/* This is the interface that a listener class implements to receive Process Post events                                        */
/****************************************************************************************************/
/* 03/07/2000 #FEAT.INT1178:  Creation
/****************************************************************************************************/

public interface ProcessActionListener{

    public  void handleProcessCompletion(AbstractProcess ap);

}
