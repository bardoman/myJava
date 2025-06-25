package com.ibm.sdwb.build390;


class AbendError extends java.io.IOException {
	AbendError(){
		super("An abend occurred while running this Build/390 Server command.");
	}
}


