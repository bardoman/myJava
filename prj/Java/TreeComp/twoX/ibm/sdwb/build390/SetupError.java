package com.ibm.sdwb.build390;


class SetupError extends MBBuildException {

    SetupError(String errorMessage) {
        super("Setup Error", errorMessage, MBConstants.SETUPERROR);
    }

    SetupError(String errorMessage, Exception e) {
        super("Setup Error", errorMessage, e, MBConstants.SETUPERROR);
    }
}


