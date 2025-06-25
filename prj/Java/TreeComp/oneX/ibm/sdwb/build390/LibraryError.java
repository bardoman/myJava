package com.ibm.sdwb.build390;


public class LibraryError extends MBBuildException {

    public LibraryError(String errorMessage) {
        super("Library Error", errorMessage, MBConstants.LIBRARYERROR);
    }

    public LibraryError(String errorMessage, Exception e) {
        super("Library Error", errorMessage, e, MBConstants.LIBRARYERROR);
    }
}


