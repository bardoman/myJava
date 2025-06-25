
import java.io.*;
import java.security.*;
import java.security.spec.InvalidKeySpecException;
import java.util.Arrays;

import javax.crypto.*;
import javax.crypto.spec.DESedeKeySpec;

public class TripleDES {
    String keyStr = "ABRACADABRAANDOPENSESAME";
    byte globalKey[]=  keyStr.getBytes();

    String text = "Better to remain silent and be thought a fool than to speak out and remove all doubt.";
    byte cryptStore[];
    String cryptStr;

    public static void main(String[] args) {
        new  TripleDES(args);
    }

    public TripleDES(String[] args) {
        try {
            for(int i=0;i!=10;i++) {

                System.out.println("********Begin Test********");

                System.out.println("OriginalText="+text);

                cryptStore = encrypt(text);

                System.out.println("cryptStore="+toHexValue(cryptStore));

                text=" ";

                System.out.println("text="+text);

                text = decrypt(cryptStore);


                System.out.println("DecryptText="+text);


                System.out.println("********End Test********");
            }
        }
        catch(Exception e) {
            System.err.println(e);
        }
    }

    public String toHexValue(byte bytes[]) {
        String temp="";

        for(int i=0;i!=bytes.length;i++) {
            String str =  Integer.toHexString((int) bytes[i]);
            if(str.length()>2) {
                str = str.substring(str.length()-2);
            }
            temp+= str +","; 
        }
        return temp;
    }

    public  SecretKey GenKey() throws IOException,
    NoSuchAlgorithmException, InvalidKeyException,
    InvalidKeySpecException {

        DESedeKeySpec keyspec = new DESedeKeySpec(globalKey);
        SecretKeyFactory keyfactory = SecretKeyFactory.getInstance("DESede");
        SecretKey key = keyfactory.generateSecret(keyspec);
        return key;
    }

    public  byte [] encrypt(String inStr)
    throws NoSuchAlgorithmException, InvalidKeyException,
    NoSuchPaddingException, InvalidKeySpecException,
    IllegalBlockSizeException,BadPaddingException, IOException {

        SecretKey key = GenKey(); 

        Cipher cipher = Cipher.getInstance("DESede");

        cipher.init(Cipher.ENCRYPT_MODE, key);

        return cipher.doFinal(inStr.getBytes());
    }

    public  String decrypt(byte cryptStore[])
    throws NoSuchAlgorithmException, InvalidKeyException, InvalidKeySpecException, IOException,
    IllegalBlockSizeException, NoSuchPaddingException,
    BadPaddingException {
        SecretKey key = GenKey(); 

        Cipher cipher = Cipher.getInstance("DESede");

        cipher.init(Cipher.DECRYPT_MODE, key);

        byte out[] = cipher.doFinal(cryptStore);

        return new String(out);
    }
}



