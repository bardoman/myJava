package com.ibm.sdwb.build390.userinterface.text.utilities;

import java.io.FileDescriptor;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.CharBuffer;
import java.nio.channels.AsynchronousCloseException;
import java.nio.channels.Channels;
import java.nio.channels.FileChannel;
import java.nio.channels.ReadableByteChannel;
import java.nio.channels.spi.AbstractInterruptibleChannel;
import java.nio.charset.Charset;
import java.nio.charset.CharsetDecoder;

public class ConsoleInputReader extends Thread {


    private volatile boolean isKeepReading = true;
    private FileChannel byteChannel  = null;
    private static ConsoleInputReader theReader = null;
    private String currentString = null;
    private volatile boolean dataCaptured = false;

    private ConsoleInputReader() {
        super("ConsoleInputReader");
        byteChannel = new FileInputStream(FileDescriptor.in).getChannel(); //this blocks but is interruptible.
        //byteChannel = Channels.newChannel(System.in); this blocks but it is not interruptible even though the AbstractInterruptibleChannel  is being used.

    }

    protected static ConsoleInputReader getInstance() {
        if (theReader==null) {
            theReader = new ConsoleInputReader();
        }
        return theReader;
    }

    public void waitOnInput() {
        try {
            synchronized(theReader) {
                theReader.wait();
            }
        } catch (InterruptedException irpe) {
        } finally {
        }
    }

    public void notifyInputAvailable() {
        synchronized(theReader) {
            theReader.notifyAll();
        }
    }



    public void run() {
        try {
            CharBuffer cbuffer = null;     
            ByteBuffer buffer = ByteBuffer.allocate(64); //note character occupies 2 bytes. which means our input can be 32 chrs, and we'll have to loop back.
            while (keepReading()) {
                int bytesRead =0;
                for (;keepReading();) {
                    bytesRead = byteChannel.read(buffer);
                    if (bytesRead !=0) {
                        break;
                    }

                }

                buffer.flip();
                cbuffer = buffer.asCharBuffer();
                if (bytesRead > 0) {
                    currentString = new String(buffer.array(),0,bytesRead).trim();
                    buffer.clear();
                    dataCaptured = true;
                    notifyInputAvailable();
                    dataCaptured = false;
                }
            }
        } catch (AsynchronousCloseException asce) {
            //ignore it. we'll get it when the blocked read is interrupted.
        } catch (IOException ioe) {
            //   ioe.printStackTrace();
            //we may wanna log this one.
        }
    }

    public boolean keepReading() {
        return isKeepReading;
    }


    public void stopReader() {
        isKeepReading = false;
        if (byteChannel!=null && byteChannel.isOpen()) {
            //do we wanna close the channel from a different thread. ??? forget it, for now its working.
            try {
                byteChannel.close();
            } catch (IOException ioex) {
                //ignore it.
            }

        }
    }

    public String getResponse() {
        return currentString;
    }

}
