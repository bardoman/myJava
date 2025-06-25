//package webHelpSupport;

/*
	A basic extension of the java.applet.Applet class
 */

import java.io.*;
import java.net.*;

public class helpURLGetter{

    private String newURL;
    private ServerSocket sock;
    public final String nothing = "nothinghere";

    public helpURLGetter() {
        newURL=nothing;
	    try {
    	    sock = new ServerSocket(5746);
    	    sock.setSoTimeout(1);
//        return stringRead;
      	}catch (IOException ioe) {
   	        try {sock.close();}catch (IOException ioe2) {}
   	        System.out.println("got io exception " + ioe);
       	}
    }

    public String getURL() {
        try {
       	    Socket tempSock = sock.accept();
  	        BufferedReader in = new BufferedReader(new InputStreamReader(tempSock.getInputStream()));
            String stringRead= in.readLine();
            System.out.println(stringRead);
            newURL = stringRead;
            tempSock.close();
            return newURL;
        }catch(Exception e) {
            System.out.println("got exception " +e);
            return nothing;
        }
    }

    public void close() {
        System.out.println("in close method");
	    try {
    	    sock.close();
      	}catch (IOException ioe) {
   	        try {sock.close();}catch (IOException ioe2) {}
   	        System.out.println("got io exception " + ioe);
       	}
    }

    public String nothing() {
        return nothing;
    }


}
