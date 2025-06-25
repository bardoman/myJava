package com.ibm.sdwb.build390.security.encryption.aes.testsuite;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.TreeMap;

import com.ibm.sdwb.build390.security.encryption.aes.AESCipher;
import com.ibm.sdwb.build390.security.tools.BytesPrinter;
import com.ibm.sdwb.build390.security.tools.Utilities;


public class CBCTest {

    private  Map  testCasesMap =  new LinkedHashMap();
    private int Nk =-1;

    public CBCTest(int tempNk) {
        this.Nk= tempNk;
    }

    protected void runCrypt() {
        System.out.println("CBC:Monte carlo test: encrypt");
        String testCaseKey = "CBC_E_MCT";

        byte [] PT  = getZeroPlainText();
        byte [] KEY = getZeroKey();
        byte [] IV  = getZeroPlainText();
        byte [] CV  = getZeroPlainText();

        for (int i=0;i<=399;i++) {
            AESTestCase testCase = new AESTestCase(testCaseKey,String.valueOf(i),Nk,PT,KEY);
            testCase.setInitializationVector(CV);
            testCasesMap.put(testCase.getName(),testCase);

            System.out.println("CBC_E_" +String.valueOf(i)+ " KEYSIZE="+ Nk*32);
            byte[] CT      = getZeroCT();
            byte[] CT_SWAP = getZeroCT();

            for (int j=0;j<=9999;j++) {

                byte[] IB = getZeroPlainText();

                for (int k=0;k< PT.length;k++) {
                    IB[k] =(byte)((PT[k] ^ CV[k]) & 0XFF);
                }

                CT = AESCipher.crypt(IB, KEY,testCase.getKeySize());

                if (j==0) {
                    System.arraycopy(CV,0,PT,0,CV.length);
                } else {
                    System.arraycopy(CT_SWAP,0,PT,0,CT_SWAP.length);
                }
                System.arraycopy(CT,0,CV,0,CT.length);

                if (j!=9999) {
                    System.arraycopy(CT,0,CT_SWAP,0,CT.length);
                }
            }
            testCase.setCipherText(CT);

            byte[] CT_EXPANDED = getExpandedByte(CT,CT_SWAP);


            for (int k=0;k< KEY.length;k++) {
                KEY[k] ^= CT_EXPANDED[k];
            }

            System.arraycopy(CT_SWAP,0,PT,0,CT_SWAP.length);
            System.arraycopy(CT,0,CV,0,CT.length);
        }
    }

    protected void runDeCrypt() {
        System.out.println("CBC:Monte carlo test: decrypt");
        String testCaseKey = "CBC_D_MCT";

        byte [] CT  = getZeroCT();
        byte [] KEY = getZeroKey();
        byte [] IV  = getZeroPlainText();
        byte [] CV  = getZeroPlainText();

        for (int i=0;i<=399;i++) {
            AESTestCase testCase = new AESTestCase(testCaseKey,String.valueOf(i),Nk);
            testCase.setCipherText(CT);
            testCase.setInitializationVector(CV);
            testCase.setCipherKey(KEY);

            System.out.println("CBC_D_" +String.valueOf(i)+ " KEYSIZE="+ Nk*32);
            testCasesMap.put(testCase.getName(),testCase);

            byte[] PT = getZeroPlainText();
            byte[] PT_SWAP = getZeroPlainText();

            for (int j=0;j<=9999;j++) {

                byte[] OB = AESCipher.decrypt(CT,KEY,testCase.getKeySize());

                for (int k=0;k< OB.length;k++) {
                    PT[k] = (byte)(OB[k] ^ CV[k]);
                }
                System.arraycopy(CT,0,CV,0,CT.length);
                System.arraycopy(PT,0,CT,0,PT.length);

                if (j!=9999) {
                    System.arraycopy(PT,0,PT_SWAP,0,PT.length);
                }
            }

            testCase.setPlainText(PT);
            byte[] PT_EXPANDED = getExpandedByte(PT,PT_SWAP);

            for (int k=0;k< KEY.length;k++) {
                KEY[k] ^= PT_EXPANDED[k];
            }


        }
    }

    protected Map getMap() {
        return testCasesMap;
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
       CBCTest cbc =  new CBCTest(4);
       cbc.runCrypt();
/*        cbc.runDeCrypt();
*/
        cbc =  new CBCTest(6);
        cbc.runCrypt();
        //cbc.runDeCrypt();

       cbc =  new CBCTest(8);
        cbc.runCrypt();
  /*      cbc.runDeCrypt();
   */ }

}
