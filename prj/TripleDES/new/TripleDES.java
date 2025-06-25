

import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.Provider;
import java.security.Security;
import java.security.spec.InvalidKeySpecException;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.CipherOutputStream;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.KeyGenerator;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.DESedeKeySpec;
import java.io.*;

/**
 * This class defines methods for encrypting and decrypting using the Triple DES
 * algorithm and for generating, reading and writing Triple DES keys. It also
 * defines a main() method that allows these methods to be used from the command
 * line.
 */
public class TripleDES {
    String keyStr = "trustingodbutlockyourbox";
    byte globalKey[]=  keyStr.getBytes();

    String inStr = "this is a test of tripleDES incryption scheme";
    String outStr;

    public static void main(String[] args) {
        new  TripleDES(args);
    }

    /**
     * The program. The first argument must be -e, -d, or -g to encrypt,
     * decrypt, or generate a key. The second argument is the name of a file
     * from which the key is read or to which it is written for -g. The -e and
     * -d arguments cause the program to read from standard input and encrypt or
     * decrypt to standard output.
     */
    public TripleDES(String[] args) {

        try {

            // Check to see whether there is a provider that can do TripleDES
            // encryption. If not, explicitly install the SunJCE provider.
            try {
                Cipher c = Cipher.getInstance("DESede");
            }
            catch(Exception e) {
                // An exception here probably means the JCE provider hasn't
                // been permanently installed on this system by listing it
                // in the $JAVA_HOME/jre/lib/security/java.security file.
                // Therefore, we have to install the JCE provider explicitly.
                System.err.println("Installing SunJCE provider.");
                Provider ibmjce = new com.ibm.crypto.provider.IBMJCE();
                Security.addProvider(ibmjce);
            }


            encrypt(inStr, outStr);

            decrypt(inStr, outStr);

        }
        catch(Exception e) {
            System.err.println(e);
        }
    }


    public  SecretKey readKey() throws IOException,
    NoSuchAlgorithmException, InvalidKeyException,
    InvalidKeySpecException {

        // Convert the raw bytes to a secret key like this
        DESedeKeySpec keyspec = new DESedeKeySpec(globalKey);
        SecretKeyFactory keyfactory = SecretKeyFactory.getInstance("DESede");
        SecretKey key = keyfactory.generateSecret(keyspec);
        return key;
    }

    /**
     * Use the specified TripleDES key to encrypt bytes from the input stream
     * and write them to the output stream. This method uses CipherOutputStream
     * to perform the encryption and write bytes at the same time.
     */
    public  void encrypt(String inStr, String outStr)
    throws NoSuchAlgorithmException, InvalidKeyException,
    NoSuchPaddingException, InvalidKeySpecException, IOException {

        SecretKey key = readKey(); 

        // Create and initialize the encryption engine
        Cipher cipher = Cipher.getInstance("DESede");
        cipher.init(Cipher.ENCRYPT_MODE, key);

        byte out[] = cipher.doFinal(inStr.getBytes());

        outStr = new String(out);

        /*
        // Create a special output stream to do the work for us
        CipherOutputStream cos = new CipherOutputStream(outStr, cipher);

        // Read from the input and write to the encrypting output stream
        byte[] buffer = new byte[2048];
        int bytesRead;
        while((bytesRead = inStr.read(buffer)) != -1) {
            cos.write(buffer, 0, bytesRead);
        }
        cos.close();

        // For extra security, don't leave any plaintext hanging around memory.
        java.util.Arrays.fill(buffer, (byte) 0);

        */
    }

    /**
     * Use the specified TripleDES key to decrypt bytes ready from the input
     * stream and write them to the output stream. This method uses uses Cipher
     * directly to show how it can be done without CipherInputStream and
     * CipherOutputStream.
     */
    public  void decrypt(InputStream in, OutputStream out)
    throws NoSuchAlgorithmException, InvalidKeyException, InvalidKeySpecException, IOException,
    IllegalBlockSizeException, NoSuchPaddingException,
    BadPaddingException {

        SecretKey key = readKey(); 

        // Create and initialize the decryption engine
        Cipher cipher = Cipher.getInstance("DESede");
        cipher.init(Cipher.DECRYPT_MODE, key);

        // Read bytes, decrypt, and write them out.
        byte[] buffer = new byte[2048];
        int bytesRead;
        while((bytesRead = in.read(buffer)) != -1) {
            out.write(cipher.update(buffer, 0, bytesRead));
        }

        // Write out the final bunch of decrypted bytes
        out.write(cipher.doFinal());
        out.flush();
    }
}


