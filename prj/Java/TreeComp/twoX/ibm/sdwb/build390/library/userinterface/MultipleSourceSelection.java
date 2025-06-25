package com.ibm.sdwb.build390.library.userinterface;

import java.util.*;


/**
 * Extended by panels that will return library specific 
 * information on where to get build source from, such as 
 * CMVC track info
 */
public abstract class MultipleSourceSelection extends SourceSelection{

    public abstract com.ibm.sdwb.build390.library.ChangeRequest getSelectedChangeRequest();
}
