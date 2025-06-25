package com.ibm.sdwb.build390.library.clearcase;

import java.io.File;
import com.ibm.sdwb.build390.help.HelpLoaderInterface;

import oracle.help.library.Book;
import oracle.help.library.helpset.HelpSet;   

public class ClearCaseHelpLoader implements com.ibm.sdwb.build390.help.HelpLoaderInterface {


    private static final String CLEARCASE_B390_HELPSET_FILE =    "help"+ java.io.File.separator + "clearcase" + java.io.File.separator +"b390clearcase.hs";
    private transient Book clearcaseBook;


    public  ClearCaseHelpLoader() throws com.ibm.sdwb.build390.help.HelpException {
        try {

            if (clearcaseBook==null) {
                clearcaseBook = (Book) new HelpSet(com.ibm.sdwb.build390.help.HelpUtilities.pathToURL((new File(CLEARCASE_B390_HELPSET_FILE)).getAbsolutePath()));
            }
        } catch (oracle.help.library.helpset.HelpSetParseException hspe) {
            throw new com.ibm.sdwb.build390.help.HelpException ("An error occurred while trying to parse helpset " +  CLEARCASE_B390_HELPSET_FILE ,hspe);
        } catch (java.net.MalformedURLException murl) {
            throw new com.ibm.sdwb.build390.help.HelpException ("Attempt to convert path " + CLEARCASE_B390_HELPSET_FILE + " to url failed.",murl);
        } catch (java.io.IOException ioe) {
            throw new com.ibm.sdwb.build390.help.HelpException ("Invalid helpset filename " + CLEARCASE_B390_HELPSET_FILE,ioe);
        }
    }


    public Book getHelpBook() {
        return clearcaseBook;
    }

    public String toString() {
        return " HelpSet path ="+ CLEARCASE_B390_HELPSET_FILE;
    }


}


