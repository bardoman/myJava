package com.ibm.sdwb.build390.library.cmvc;


import java.io.File;
import java.io.IOException;

import com.ibm.sdwb.build390.help.HelpException;

import oracle.help.library.Book;
import oracle.help.library.helpset.HelpSet;



public class CMVCHelpLoader implements com.ibm.sdwb.build390.help.HelpLoaderInterface {


    private static final String CMVC_B390_HELPSET_FILE =    "help"+ java.io.File.separator + "cmvc" + java.io.File.separator + "b390cmvc.hs";
    private transient Book cmvcBook;


    public  CMVCHelpLoader() throws com.ibm.sdwb.build390.help.HelpException {
        try {

            if (cmvcBook==null) {
                cmvcBook = (Book) new HelpSet(com.ibm.sdwb.build390.help.HelpUtilities.pathToURL((new File(CMVC_B390_HELPSET_FILE)).getAbsolutePath()));
            }
        } catch (oracle.help.library.helpset.HelpSetParseException hspe) {
            throw new com.ibm.sdwb.build390.help.HelpException ("An error occurred while trying to parse helpset " +  CMVC_B390_HELPSET_FILE ,hspe);
        } catch (java.net.MalformedURLException murl) {
            throw new com.ibm.sdwb.build390.help.HelpException ("Attempt to convert path " + CMVC_B390_HELPSET_FILE + " to url failed.",murl);
        } catch (IOException ioe){
                throw new com.ibm.sdwb.build390.help.HelpException ("Invalid helpset filename " + CMVC_B390_HELPSET_FILE,ioe);
        }
    }


    public Book getHelpBook()  {
        return cmvcBook;
    }


    public String toString(){
        return " helpset path ="+ CMVC_B390_HELPSET_FILE;
    }

}

