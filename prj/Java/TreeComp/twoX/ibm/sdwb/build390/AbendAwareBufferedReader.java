package com.ibm.sdwb.build390;

/* this reader detects abends
*/


public class AbendAwareBufferedReader extends java.io.BufferedReader{
	private boolean gotSlashes = false;
	private static String ABENDSTRING="//";

	AbendAwareBufferedReader(java.io.Reader r){
		super(r);
	}

	AbendAwareBufferedReader(java.io.Reader r, int b){
		super(r,b);
	}

    public String readLine() throws java.io.IOException{
		String nextLine = super.readLine();
		if (nextLine==null){
			if (gotSlashes) {
				throw new AbendError();
			}
		}else if (nextLine.trim().equals(ABENDSTRING)){
			gotSlashes = true;
		}else {
			gotSlashes = false;
		}
		return nextLine;
    }
}
