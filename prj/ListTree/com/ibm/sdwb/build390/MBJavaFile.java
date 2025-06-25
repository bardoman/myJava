package com.ibm.sdwb.build390;
/*********************************************************************/
/* Java JavaFile class for MVS Build client                          */
/*  Displays the contents of a file either in the gui or window      */
/*  Input: Filename to display, gui flag, gui instance               */
// 03/24/99 copyBinary          Add copybinary function
// 04/27/99 errorHandling       change LogException parms & add new error types
// 03/07/2000 reworklog         changes to write the log stuff using listeners
/*********************************************************************/
import java.io.*;
import com.ibm.sdwb.build390.logprocess.*;
import com.ibm.sdwb.build390.userinterface.graphic.MainInterface;

/** <br>The MBJavaFile class displays the content of a file. */
public class MBJavaFile {

    private MainInterface guim_;
    private String pattern_;
    private LogEventProcessor lep=null;

    public MBJavaFile(LogEventProcessor lep){
        this.lep=lep;
    }

    /** Display the contents of a file either in the gui or a window.
    * @param fn File name of file to display.
    * @param gui boolean indicating GUI or command line mode
    * @param Title String containing the title of the display
    */
    public void show(String fn, boolean gui, String Title) {

        guim_ = MainInterface.getInterfaceSingleton();

        // catch io exceptions and tell user
        try {
            FileInputStream fin = new FileInputStream(fn);      // open the file
            int avail = fin.available();                        // get number of bytes in file
            if (avail > 0 ) {
                byte[] bdata = new byte[avail];                 // create byte array to hold file
                fin.read(bdata);                                // read file into byte array
                new MBMsgBox(Title, new String(bdata));
            }
            fin.close();                                        // close the file
        } catch (IOException e) {
            String outstr = new String("Output file " + fn + " could not be read.");
            new MBMsgBox("Error", outstr);
        }
    }

    /**
    * Copy the files
    * @param String srcpath contains the path to copy from
    * @param String pattern, file selection pattern (currently just the right end ie .cmp)
    * @param String target dir
    */
    public void mcopy(String srcpath, String pattern, String target) {
        // get list of files fitting the wild card path
        pattern_ = pattern;
        String [] files = null;
        File dir = new File(srcpath);
        files = dir.list(new FileFilter());
        for (int i=0; i < files.length; i++) {
            //MBUtilities.Logit(MBConstants.DEBUG_DEV, "MBJavaFile:mcopy:copying "+srcpath+File.separator+files[i]+" to "+target+File.separator+files[i], "");
            lep.LogSecondaryInfo("Debug","MBJavaFile:mcopy:copying "+srcpath+File.separator+files[i]+" to "+target+File.separator+files[i]);
            // call copyit for each one
            copy(srcpath+File.separator+files[i], target+File.separator+files[i], false);
        }
    }

    /** The CommandFilter class creates a list of B* files and do not contain a . */
    public class FileFilter implements FilenameFilter {
        public boolean accept(File dir, String name) {
            if (name.toUpperCase().endsWith(pattern_.toUpperCase()))
                return new File(dir, name).exists();
            else return false;
        }
    }


    /** Copy the source file to the target file
    * @param String source
    * @param String target */
    public void copy(String source, String target, boolean force) {
        BufferedReader src;
        BufferedWriter tgt;
        // check for target file is alrady exist
        File tgtFile = new File(target);
        if (tgtFile.exists() & !force) {
            String confmsg = new String(target + " already exists.  Overwrite it?");
            MBMsgBox mBox = new MBMsgBox("Confirm Overwrite", confmsg, null, true);
            if (!mBox.isAnswerYes()) {
                // do not override the file
                return;
            }
        }

        // create buffers for source and target files
        try {
            // check for source file
            src = new BufferedReader(new FileReader(source));
        } catch (FileNotFoundException e) {
            //MBUtilities.LogException("Source file "+source+" not found", e);
            lep.LogException("Source file "+source+" not found", e);
            return;
        }

        try {
            // create a target file
            tgt = new BufferedWriter(new FileWriter(target));
        } catch (IOException ioe) {
            //MBUtilities.LogException("Cannot create a target file " + target, ioe);
            lep.LogException("Cannot create a target file " + target, ioe);
            try {
                src.close();
            } catch (IOException e) {
                //MBUtilities.Logit(MBConstants.DEBUG_DEV, "src.close: "+System.err, "MBJavaFile::copy()"); }
                lep.LogSecondaryInfo( "MBJavaFile::copy()","src.close: "+System.err);
            }
            return;
        }

        // copy the file
        try {
            String line = new String();
            line = src.readLine();
            while (line != null) {
                tgt.write(line+MBConstants.NEWLINE);
                //tgt.newLine();
                line = src.readLine();
            }
            try {
                tgt.flush();
            } catch (IOException fe) {
                //MBUtilities.Logit(MBConstants.DEBUG_DEV, "tgt.flush: "+System.err, "MBJavaFile::copy()"); }
                lep.LogSecondaryInfo("MBJavaFile::copy()", "tgt.flush: "+System.err );
            }
        } catch (IOException ioe) {
            // handle errors
            lep.LogException("There was an error copying files", ioe);
        } finally {
            if (src != null) {
                try {
                    src.close();
                } catch (IOException srce) {
                    // MBUtilities.Logit(MBConstants.DEBUG_DEV, "src.close: "+System.err, "MBJavaFile::copy()"); }
                    lep.LogSecondaryInfo("MBJavaFile::copy()", "src.close: "+System.err );
                }
                try {
                    tgt.close();
                } catch (IOException tgte) {
                    //MBUtilities.Logit(MBConstants.DEBUG_DEV, "tgt.close: "+System.err, "MBJavaFile::copy()"); }
                    lep.LogSecondaryInfo("MBJavaFile::copy()", "tgt.close: "+System.err );
                }
            }
        }
    }


    /** Copy the source file to the target file
    * @param String source
    * @param String target */
    public void copyBinary(String source, String target, boolean force) {
        BufferedInputStream src;
        BufferedOutputStream tgt;
        // check for target file is alrady exist
        File tgtFile = new File(target);
        if (tgtFile.exists() & !force) {
            String confmsg = new String(target + " already exists.  Overwrite it?");
            MBMsgBox mBox = new MBMsgBox("Confirm Overwrite", confmsg, null, true);
            if (!mBox.isAnswerYes()) {
                // do not override the file
                return;
            }
        }

        // create buffers for source and target files
        try {
            // check for source file
            src = new BufferedInputStream(new FileInputStream(source), 1024);
        } catch (FileNotFoundException e) {
            lep.LogException("Source file "+source+" not found", e);
            return;
        }

        try {
            // create a target file
            tgt = new BufferedOutputStream(new FileOutputStream(target), 1024);
        } catch (IOException ioe) {
            lep.LogException("Cannot create a target file "+target, ioe);
            try {
                src.close();
            } catch (IOException e) {
                //MBUtilities.Logit(MBConstants.DEBUG_DEV, "src.close: "+System.err, "MBJavaFile::copy()"); }
                lep.LogSecondaryInfo("MBJavaFile::copy()", "src.close: "+System.err);
            }
            return;
        }

        // copy the file
        try {
            byte[] bdata = new byte[1024];
            int len=0;
            while ((len = src.read(bdata)) > -1) {
                tgt.write(bdata,0,len);
                tgt.flush();
            }
        } catch (IOException ioe) {
            // handle errors
            //MBUtilities.LogException("There was an error writing the target file", ioe);
            lep.LogException("There was an error writing the target file", ioe);
        } finally {
            if (src != null) {
                try {
                    src.close();
                } catch (IOException srce) {
                    //MBUtilities.Logit(MBConstants.DEBUG_DEV, "src.close: "+System.err, "MBJavaFile::copy()"); }
                    lep.LogSecondaryInfo("MBJavaFile::copy()", "src.close: "+System.err );
                }
                try {
                    tgt.close();
                } catch (IOException tgte) {
                    //   MBUtilities.Logit(MBConstants.DEBUG_DEV, "tgt.close: "+System.err, "MBJavaFile::copy()"); }
                    lep.LogSecondaryInfo("MBJavaFile::copy()", "tgt.close: "+System.err );
                }
            }
        }
    }

    /** Move the source file to the target file
    * @param String source
    * @param String target */
    public void move(String source, String target) {
        // copy the file to new directory
        copy(source, target, false);
        // delete the source file
        File srcFile = new File(source);
        srcFile.delete();
    }
}
