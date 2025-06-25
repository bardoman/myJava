package com.ibm.sdwb.build390.library.fakelib;
import java.io.File;

import com.ibm.sdwb.build390.help.HelpException;

import oracle.help.library.Book;
import oracle.help.library.helpset.HelpSet;



public class FakeLibraryHelpLoader implements com.ibm.sdwb.build390.help.HelpLoaderInterface {


    private static final String NOLIB_B390_HELPSET_FILE =    com.ibm.sdwb.build390.MBGlobals.Build390_path + "help"+ java.io.File.separator + "cmvc" + java.io.File.separator + "b390cmvc.hs";
    private transient Book fakelibBook;


    public  FakeLibraryHelpLoader() throws com.ibm.sdwb.build390.help.HelpException {
        try {
            if (fakelibBook==null) {
                fakelibBook = (Book) new HelpSet(com.ibm.sdwb.build390.help.HelpUtilities.pathToURL((new File(NOLIB_B390_HELPSET_FILE)).getAbsolutePath()));
            }
        } catch (oracle.help.library.helpset.HelpSetParseException hspe) {
            throw new com.ibm.sdwb.build390.help.HelpException ("An error occurred while trying to parse helpset " +  NOLIB_B390_HELPSET_FILE ,hspe);
        } catch (java.net.MalformedURLException murl) {
            throw new com.ibm.sdwb.build390.help.HelpException ("Attempt to convert path " + NOLIB_B390_HELPSET_FILE + " to url failed.",murl);
        } catch (java.io.IOException ioe) {
            throw new com.ibm.sdwb.build390.help.HelpException ("Invalid helpset filename " + NOLIB_B390_HELPSET_FILE,ioe);
        }
    }


    public Book getHelpBook() {
        return fakelibBook;
    }


    public String toString() {
        return " helpset path ="+ NOLIB_B390_HELPSET_FILE;
    }

}

