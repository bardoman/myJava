package com.ibm.sdwb.build390.security.encryption.aes.testsuite;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.TreeMap;

import com.ibm.sdwb.build390.security.encryption.aes.AESCipher;
import com.ibm.sdwb.build390.security.tools.BytesPrinter;
import com.ibm.sdwb.build390.security.tools.Utilities;


public class MCTTest {

    private  Map  testCasesMap =  new LinkedHashMap();
    private int Nk = -1;

    public MCTTest(int tempNk) {
        this.Nk= tempNk;
    }

    protected void runCrypt() {
        System.out.println("Monte carlo test: encrypt");
        byte [] PT  = getZeroPlainText();
        byte [] KEY = getZeroKey();
        for (int i=0;i<=399;i++) {
            System.out.println("MCT_E_" +String.valueOf(i)+ " KEYSIZE="+ Nk*32);
            AESTestCase testCase = new AESTestCase("ECB_E_MCT",String.valueOf(i),Nk,PT,KEY);
            testCasesMap.put("ECB_E_MCT" +String.valueOf(i),testCase);

            byte[] CT = getZeroKey();
            byte[] CT_SWAP = getZeroCT();

            for (int j=0;j<=9999;j++) {
                CT = AESCipher.crypt(PT, KEY,testCase.getKeySize());
                System.arraycopy(CT,0,PT,0,CT.length);
                if (j!=9999) {
                    System.arraycopy(CT,0,CT_SWAP,0,CT.length);
                }
            }

            testCase.setCipherText(CT);
            byte[] CT_EXPANDED  = getExpandedByte(CT,CT_SWAP);

            for (int k=0;k< KEY.length;k++) {
                KEY[k] ^= CT_EXPANDED[k];
            }
        }
    }

    protected void runDeCrypt() {
        System.out.println("Monte carlo test: decrypt");
        byte [] CT  = getZeroCT();
        byte [] KEY = getZeroKey();

        for (int i=0;i<=399;i++) {
            AESTestCase testCase = new AESTestCase("ECB_D_MCT",String.valueOf(i),Nk);
            testCase.setCipherText(CT);
            testCase.setCipherKey(KEY);
            System.out.println("MCT_D_" +String.valueOf(i)+ " KEYSIZE="+ Nk*32);

            testCasesMap.put("ECB_D_MCT" +String.valueOf(i),testCase);
            byte[] PT = getZeroPlainText();
            byte[] PT_SWAP = getZeroPlainText();

            for (int j=0;j<=9999;j++) {
                PT = AESCipher.decrypt(CT,KEY,testCase.getKeySize());
                System.arraycopy(PT,0,CT,0,PT.length);
                if (j!=9999) {
                    System.arraycopy(PT,0,PT_SWAP,0,CT.length);
                }
            }
            testCase.setPlainText(PT);

            byte[] PT_EXPANDED  = getExpandedByte(PT,PT_SWAP);

            for (int k=0;k< KEY.length;k++) {
                KEY[k] ^= PT_EXPANDED[k];
            }



        }
    }

    private byte[] getExpandedByte(byte[] endingBytes, byte[] expandWithByte) {
        byte[] expanded = new byte[4*Nk];
        int pos = (4*Nk) - endingBytes.length;
        int epos = endingBytes.length;
        int spos = epos;

        if (pos!=0) {
            spos = expanded.length - pos;
            System.arraycopy(expandWithByte,(expandWithByte.length -pos),expanded,0,pos); 
        }

        System.arraycopy(endingBytes,0,expanded,pos,spos);
        return expanded;
    }

    protected Map getMap() {
        return testCasesMap;
    }


    private byte[] getZeroPlainText() {
        return Utilities.getZeroArray(16);
    }

    private byte[] getZeroCT() {
        return Utilities.getZeroArray(16);
    }

    private byte[] getZeroKey() {
        return Utilities.getZeroArray(4*Nk);
    }

    public static void main(String[] args) throws Exception{
        MCTTest mct =  new MCTTest(4);
        mct.runCrypt();
        mct.runDeCrypt();

        mct =  new MCTTest(6);
        mct.runCrypt();
        mct.runDeCrypt();

        mct =  new MCTTest(8);
        mct.runCrypt();
        mct.runDeCrypt();


    }

}
