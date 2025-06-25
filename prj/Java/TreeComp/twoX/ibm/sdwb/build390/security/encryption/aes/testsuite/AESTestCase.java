package com.ibm.sdwb.build390.security.encryption.aes.testsuite;

import java.util.Arrays;
import java.util.Formatter;

import static com.ibm.sdwb.build390.security.encryption.aes.RjindaelEngine.KeySize;
import com.ibm.sdwb.build390.security.tools.BytesPrinter;
import com.ibm.sdwb.build390.security.tools.Utilities;


public class AESTestCase {

    private byte[] cipherKey = new byte[16];
    private byte[]   plainText;
    private byte[] cipherText;
    private byte[] initializationVector;
    private String name;
    private int Nk=-1;
    private KeySize size;
    private String index = "";

    protected AESTestCase(String tempName, String tempIndex, int tempNk) {
        this.name= tempName;
        init(tempNk,tempIndex);
    }

    protected AESTestCase(String tempName,String tempIndex, int tempNk,byte[] tempPlainText,byte[] tempCipherKey) {
        this.name= tempName;
        init(tempNk,tempIndex);
        this.plainText= new byte[tempPlainText.length];
        this.cipherKey = new byte[tempCipherKey.length];
        System.arraycopy(tempPlainText,0,plainText,0,tempPlainText.length);
        System.arraycopy(tempCipherKey,0,cipherKey,0,tempCipherKey.length);

    }

    private void init(int tempNk,String tempIndex) {
        this.Nk= tempNk;
        this.index= tempIndex;
        if (Nk==4) {
            name +="128_";
            size = KeySize.KEYSIZE_128BIT;
        } else if (Nk==6) {
            name +="192_";
            size = KeySize.KEYSIZE_192BIT;
        } else if (Nk==8) {
            name +="256_";
            size = KeySize.KEYSIZE_256BIT;
        }

        if (index!=null) {
            name += index;
        }

    }

    protected String getName() {
        return name;
    }


    //KEY
    protected byte[] getCipherKey() {
        return cipherKey;
    }

    protected void setCipherKey(byte[] tempCipherKey) {
        this.cipherKey = new byte[tempCipherKey.length];
        System.arraycopy(tempCipherKey,0,cipherKey,0,tempCipherKey.length);
    }

    //PT
    protected byte[] getPlainText() {
        return plainText;
    }

    protected void setPlainText(byte[] tempPlainText) {
        this.plainText= new byte[tempPlainText.length];
        System.arraycopy(tempPlainText,0,plainText,0,tempPlainText.length);
    }

    protected byte[] getInitializationVector() {
        return initializationVector;
    }

    protected void setInitializationVector(byte[] tempIV) {
        this.initializationVector = new byte[tempIV.length];
        System.arraycopy(tempIV,0,initializationVector,0,tempIV.length);
    }

    //CT
    protected String getCipherTextAsString() {
        return new String(cipherText);
    }

    //CT
    protected byte[] getCipherText() {
        return cipherText;
    }

    protected void setCipherText(byte[] tempCipherText) {
        this.cipherText = new byte[tempCipherText.length];
        System.arraycopy(tempCipherText,0,cipherText,0,tempCipherText.length);
    }

    protected KeySize  getKeySize() {
        return size;
    }
    protected void  setKeySize(KeySize tempSize) {
        this.size= tempSize;
    }

    protected void printAsLine() {
        System.out.printf("%-6s%n","I="+index);
        if (!isZeroArray(getCipherKey(),Utilities.getZeroArray(4*Nk))) {
            BytesPrinter.printArrayAsLine("KEY",getCipherKey());
        }
        if (getInitializationVector()!=null && getInitializationVector().length > 0) {
            BytesPrinter.printArrayAsLine("IV",getInitializationVector());
        }
        BytesPrinter.printArrayAsLine("CT",getCipherText());

        if (!isZeroArray(getPlainText(),Utilities.getZeroArray(4*4))) {
            BytesPrinter.printArrayAsLine("PT",getPlainText());
        }

        System.out.println();
    }


    protected void print() {
        System.out.printf("%-30s%n","******************************");
        System.out.printf("%-30s%n","Test case :" + getName());
        System.out.printf("%-30s%n","******************************");
        if (!isZeroArray(getCipherKey(),Utilities.getZeroArray(4*Nk))) {
            BytesPrinter.printArrayAsTable("KEY",getCipherKey(),4,Nk);
        }
        if (getInitializationVector()!=null && getInitializationVector().length > 0) {
            BytesPrinter.printArrayAsTable("IV",getInitializationVector(),4,4);
        }
        BytesPrinter.printArrayAsTable("CT",getCipherText(),4,4);

        if (!isZeroArray(getPlainText(),Utilities.getZeroArray(4*4))) {
            BytesPrinter.printArrayAsTable("PT",getPlainText(),4,4);
        }

        System.out.printf("%-30s%n","******************************");
        System.out.println();
    }

    public String toString() {
        StringBuilder sb = new StringBuilder();
        Formatter formatter = new Formatter(sb);
        formatter.format("%-6s%n","I="+index);
        if (!isZeroArray(getCipherKey(),Utilities.getZeroArray(4*Nk))) {
            formatter.format("%s",BytesPrinter.toStringLine("KEY",getCipherKey()));
        }
        if (getInitializationVector()!=null && getInitializationVector().length > 0) {
            formatter.format("%s",BytesPrinter.toStringLine("IV",getInitializationVector()));
        }
        formatter.format("%s",BytesPrinter.toStringLine("CT",getCipherText()));
        if (!isZeroArray(getPlainText(),Utilities.getZeroArray(4*4))) {
            formatter.format("%s",BytesPrinter.toStringLine("PT",getPlainText()));
        }
        formatter.format("%n");
        return sb.toString();
    }

    private boolean isZeroArray(byte[] temp,byte[] zeroArray) {
        return Arrays.equals(temp, zeroArray);
    }

}
