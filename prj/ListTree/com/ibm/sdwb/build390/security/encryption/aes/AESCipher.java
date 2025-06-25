package com.ibm.sdwb.build390.security.encryption.aes;
import java.util.Arrays;

import com.ibm.sdwb.build390.security.tools.BytesPrinter;
import com.ibm.sdwb.build390.security.tools.Utilities;


import static com.ibm.sdwb.build390.security.encryption.aes.RjindaelEngine.KeySize;

public class AESCipher {

    public synchronized static byte[] crypt(byte[] pw) {
        return crypt(pw,null);
    }

    public synchronized static byte[] crypt(byte[] pw, byte[] key) {
        return crypt(pw,key, KeySize.KEYSIZE_128BIT);
    }

    //TO-DO we can run multiple 4X4 blocks multi threaded and merge it serially
    public synchronized static byte[] crypt(byte[] pw, byte[] key, KeySize keySize) {
        RjindaelEngine engine  = new RjindaelEngine(key,keySize);
        byte[] output        = new byte[4*engine.getBlockSize()];
        byte[] swap          = new byte[4*engine.getBlockSize()];

        for (int blocks=0; blocks< pw.length;blocks=blocks+4*engine.getBlockSize()) {
            byte[][] state     = Utilities.toState(pw,blocks);
            byte[][] outputBuf = new byte[4][engine.getBlockSize()];
            engine.invokeAESCrypt(state,outputBuf); //output is going to get returned in outputBuf. ofcourse state as well.  but why do we need two ?
            byte[] oneblock = Utilities.toOutput(outputBuf);

            if (blocks >1) {
                swap   = new byte[output.length];
                System.arraycopy(output, 0, swap, 0, output.length);
                output = new byte[oneblock.length  + output.length]; //should we subtract -1 ??
                System.arraycopy(swap, 0, output, 0, swap.length);
                System.arraycopy(oneblock, 0, output, swap.length, oneblock.length);
            } else {
                System.arraycopy(oneblock, 0, output, 0, oneblock.length);
            }

        }
        return output;
    }

    public synchronized static byte[] decrypt(byte[] pw) { //for test purposes only, or when we need to use a key that we know.
        return decrypt(pw,null);
    }

    public synchronized static byte[] decrypt(byte[] pw, byte[] key) { //default mode 128. just a helper method.
        return decrypt(pw,key,KeySize.KEYSIZE_128BIT);
    }

    //TO-DO we can run multiple 4X4 blocks multi threaded and merge it serially
    public synchronized static byte[] decrypt(byte[] pw, byte[] key, KeySize keySize) {
        RjindaelEngine engine  = new RjindaelEngine(key,keySize);
        byte[] output        = new byte[4*engine.getBlockSize()];
        byte[] swap          = new byte[4*engine.getBlockSize()];

        for (int blocks=0; blocks<pw.length;blocks=blocks+4*engine.getBlockSize()) {
            byte[][] state     = Utilities.toState(pw,blocks);
            byte[][] outputBuf = new byte[4][engine.getBlockSize()];
            engine.invokeAESDeCrypt(state,outputBuf);
            byte[] oneblock = Utilities.toOutput(outputBuf); 

            if (blocks >1) {
                swap   = new byte[output.length];
                System.arraycopy(output, 0, swap, 0, output.length);
                output = new byte[oneblock.length  + output.length]; //should we subtract -1 ??
                System.arraycopy(swap, 0, output, 0, swap.length);
                System.arraycopy(oneblock, 0, output, swap.length, oneblock.length);
            } else {
                System.arraycopy(oneblock, 0, output, 0, oneblock.length);
            }

        }
        return output;
    }

    public static void main(String[] args) throws Exception {
        String testing       = new String("EEeeHaaaw!. Finally working... whew !.\nThis is a test.\nHurray! it works dude!.\nBut a;; the test in testsuite should pass as well.\nRun com.ibm.sdwb.build390.security.encryption.aes.testsuite.AESTestRunner");
        System.out.printf("%n%-16s%n%s%n%n","Input string =>", testing);

        System.out.println("128 bit encryption test:\n");
        byte[] encryptedText = AESCipher.crypt(testing.getBytes(), null,KeySize.KEYSIZE_128BIT);
        BytesPrinter.printArrayAsLine("KEY",encryptedText);
        byte[] textback      = AESCipher.decrypt(encryptedText,null,KeySize.KEYSIZE_128BIT);
        String myText        = new String(textback);
        System.out.println();
        System.out.println("decrypted key:\n" + myText);
        System.out.println();

        System.out.println("192 bit encryption test:\n");
        encryptedText = AESCipher.crypt(testing.getBytes(), null,KeySize.KEYSIZE_192BIT);
        BytesPrinter.printArrayAsLine("KEY",encryptedText);
        textback      = AESCipher.decrypt(encryptedText,null,KeySize.KEYSIZE_192BIT);
        myText        = new String(textback);
        System.out.println();
        System.out.println("decrypted key:\n" + myText);
        System.out.println();

        System.out.println("256 bit encryption test:\n");
        encryptedText = AESCipher.crypt(testing.getBytes(), null,KeySize.KEYSIZE_256BIT);
        BytesPrinter.printArrayAsLine("KEY",encryptedText);
        textback      = AESCipher.decrypt(encryptedText,null,KeySize.KEYSIZE_256BIT);
        myText        = new String(textback);
        System.out.println();
        System.out.println("decrypted key:\n" + myText);
        System.out.println();
    }

}
