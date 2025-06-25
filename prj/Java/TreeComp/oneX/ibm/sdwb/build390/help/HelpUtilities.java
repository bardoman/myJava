package com.ibm.sdwb.build390.help;


public class HelpUtilities {


    public static final java.net.URL pathToURL(String path) throws java.net.MalformedURLException   {

        //Begin #DEF.INT1935:
        String os = System.getProperty("os.name");

        if (os.startsWith("Windows")) {
            path = "file:/" + path;
        } else {
            path = "file://" + path;
        }
        //End #DEF.INT1935:

        return new java.net.URL(path);
    }

}
