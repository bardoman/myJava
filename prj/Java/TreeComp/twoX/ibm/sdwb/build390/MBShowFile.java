package com.ibm.sdwb.build390;
/*********************************************************************/
/* Java ShowFile class for MVS Build client                          */
/*  Displays the contents of a file either in the gui or window      */
/*  Input: Filename to display, gui flag, gui instance               */
/*********************************************************************/
import java.io.*;
import com.ibm.sdwb.build390.userinterface.graphic.MainInterface;

/** <br>The MBShowFile class displays the content of a file. */
public class MBShowFile {

   /** Display the contents of a file either in the gui or a window.
   * @param client Client requesting the display.
   * @param fn File name of file to display.
   * @param gui boolean indicating GUI or command line mode
   * @param Title String containing the title of the display
   */
   public void show(String fn, boolean gui, String Title) {

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
      }
      catch (IOException e) {
         String outstr = new String("Output file " + fn + " could not be read.");
         new MBMsgBox("Error", outstr);
      }
   }
}
