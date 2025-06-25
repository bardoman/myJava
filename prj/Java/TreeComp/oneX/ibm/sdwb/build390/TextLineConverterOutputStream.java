package com.ibm.sdwb.build390;
import java.io.*;


public class TextLineConverterOutputStream extends FilterOutputStream {

	boolean hasCRLFAlready = false;
	boolean determinedLinefeedCharacter = false;
	byte lastByteRead = 0;
	static final byte[] crlfBytes = {'\r', '\n'};
	static final int intVersionOfNewline = (int) '\n';

	public TextLineConverterOutputStream(BufferedOutputStream tempOut){
		super(tempOut);
	}
/*	DON'T USE THIS METHOD
	public void write(int b) throws IOException{
		throw new IOException("You shouldn't use the write(int b) method of TextLineConverterOutputStream");
	}
*/
	public synchronized void write(byte b[]) throws IOException{
		write(b, 0, b.length);
	}

	public synchronized void write(byte b[], int off, int len)throws IOException {
		if (len < 1) {
			return;
		}
		if (determinedLinefeedCharacter) {
			if (hasCRLFAlready) {
				super.write(b, off, len);
			}else {
				for (int i = off; i < off+len; i++) {
					if (b[i]==intVersionOfNewline) {
						super.write(crlfBytes, 0, crlfBytes.length);
					}else {
						super.write(b[i]);
					}
				}

			}
		}else {
			int currentByteIndex = off;
			if (lastByteRead == '\r') {
				if (b[currentByteIndex]=='\n') {
					determinedLinefeedCharacter = true;
					hasCRLFAlready = true;
					super.write(b[currentByteIndex++]);
				}
			}
			lastByteRead = 0;
			for (; currentByteIndex < off+len & !determinedLinefeedCharacter; currentByteIndex++) {
				if (b[currentByteIndex]=='\r') {
					if (currentByteIndex + 1 < off+len) {
						if (b[currentByteIndex+1]=='\n') {
							determinedLinefeedCharacter = true;
							hasCRLFAlready = true;
							super.write(b[currentByteIndex]);
						}
					}else {
//Ken Gee, we probably dont' need this (gulp)  					super.write(b[currentByteIndex]);
						lastByteRead = b[currentByteIndex];
					}
				}else if (b[currentByteIndex]=='\n'){
					determinedLinefeedCharacter = true;
					hasCRLFAlready = false;
					super.write(crlfBytes,0, crlfBytes.length);
				}else{
					super.write(b[currentByteIndex]);
				}
			}						
			if (currentByteIndex < len+off) {
				write(b, currentByteIndex, (len+off)-currentByteIndex);
			}
		}
	}
}
