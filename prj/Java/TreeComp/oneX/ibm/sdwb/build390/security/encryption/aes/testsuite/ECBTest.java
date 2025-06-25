package com.ibm.sdwb.build390.security.encryption.aes.testsuite;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.TreeMap;

import com.ibm.sdwb.build390.security.tools.Utilities;


public class ECBTest {

    private  Map  testCasesMap =  new LinkedHashMap();
    private  boolean isClearMap = true;

    public ECBTest(boolean tempClearMap) throws Exception {
        this.isClearMap = tempClearMap;
    }

    private void wipe(){
        if(isClearMap){
            getMap().clear();
        }
    }

    protected void clearMap(){
        testCasesMap.clear();
    }

    protected void ecbvk(int Nk){
        wipe();
        new ECBVK(Nk);
    }

    protected void ecbvt(int Nk){
        wipe();
        new ECBVT(Nk);
    }

    protected void ecbtbl(String type,int Nk) throws Exception {
        wipe();
        new ECBTBL(type,Nk);
    }

    protected void ecbiv(int Nk){
        wipe();
        new ECBIV(Nk);
    }

    protected void ecbAll(String type,int Nk) throws Exception {
        ecbvk(Nk);
        ecbvt(Nk);
        ecbtbl(type,Nk);
        ecbiv(Nk);
    }

    protected Map getMap() {
        return testCasesMap;
    }


    private class ECBVT {
        private ECBVT(int Nk) {
            String testCaseKey = "ECB_VT";
            for (int i=0;i<(4*4);i++) {
                byte init = (byte)0X80;
                for (int j=0;j<8;j++) {
                    byte[] output = Utilities.getZeroArray(4*4);
                    output[i] = init;
                    int endKey = ((i*8)+j+1);
                    AESTestCase testCase = new AESTestCase(testCaseKey, String.valueOf(endKey),Nk,output,Utilities.getZeroArray(4*Nk));
                    testCasesMap.put(testCase.getName(),testCase);
                    int temp1 = (init & 0XFF) >> 1;
                    init  = (byte)(temp1 & 0XFF);
                }
            }
        }
    }

    private class ECBVK {
        private ECBVK(int Nk) {
            String testCaseKey = "ECB_VK";
            for (int i=0;i<(4*Nk);i++) {
                byte init = (byte)0X80;
                for (int j=0;j<8;j++) {
                    byte[] output = Utilities.getZeroArray(4*Nk);
                    output[i] = init;
                    int endKey= ((i*8)+j+1);
                    AESTestCase testCase = new AESTestCase(testCaseKey, String.valueOf(endKey),Nk,Utilities.getZeroArray(16),output);
                    testCasesMap.put(testCase.getName(),testCase);
                    int temp1 = (init & 0XFF) >> 1;
                    init  = (byte)(temp1 & 0XFF);
                }
            }
        }

    }

    private class ECBTBL {
        private byte hexCount = (byte)0X00; 

        private ECBTBL(String mode,int Nk) throws Exception {
            String testCaseKey = "ECB_TBL";

            for (int i=1;i<129;i++) {
                String tempTestCaseKey = testCaseKey +  String.valueOf(i);
                String plainTextMethodName = "getPlainText"+mode+"_"+tempTestCaseKey;
                String keyMethodName = "getKey"+mode+"_"+tempTestCaseKey;
                Class classy = Class.forName("com.ibm.sdwb.build390.security.encryption.aes.testsuite.ECB_TBL");
                Method plainTextMethod = classy.getMethod(plainTextMethodName);
                Method keyMethod = classy.getMethod(keyMethodName);
                byte[] plainText =(byte[])plainTextMethod.invoke(classy);
                byte[] key =(byte[])keyMethod.invoke(classy);
                AESTestCase testCase = new AESTestCase(testCaseKey,String.valueOf(i),Nk,plainText, key);
                testCasesMap.put(testCase.getName(),testCase);
            }
        }

    }

    private class ECBIV {

        private  int Nk;

        private ECBIV(int tempNk) {
            this.Nk= tempNk;
            String testCaseKey = "ECB_IV";
            int endKey =1;
            byte[] plainText = new byte[]{};
            byte[] key       = new byte[]{};
            if (Nk==4) {
                //128 bit
                plainText = getPlainText128_ECBIV1();
                key       = getKey128_ECBIV1();
            } else if (Nk==6) {
                //192 bit
                plainText = getPlainText192_ECBIV1();
                key       = getKey192_ECBIV1();
            } else if (Nk==8) {
                //256 bit
                plainText = getPlainText256_ECBIV1();
                key       = getKey256_ECBIV1();
            }
            AESTestCase testCase = new AESTestCase(testCaseKey,String.valueOf(endKey),Nk,plainText, key);
            testCasesMap.put(testCase.getName(),testCase);
        }


        private byte[] getPlainText128_ECBIV1() {
            return getHexArray(4*4);
        }


        private byte[] getKey128_ECBIV1() {
            return getHexArray(4*Nk);
        }


        private byte[] getPlainText192_ECBIV1() {
            return getPlainText128_ECBIV1();
        }


        private byte[] getKey192_ECBIV1() {
            return getHexArray(4*Nk);
        }

        private byte[] getPlainText256_ECBIV1() {
            return getPlainText128_ECBIV1();
        }


        private byte[] getKey256_ECBIV1() {
            return getHexArray(4*Nk);
        }

        private byte[] getHexArray(int totalKey) {
            byte[] CIPHER_KEY = new byte[totalKey];
            Arrays.fill(CIPHER_KEY,(byte)0X00);
            byte hexCount = (byte)0X00;
            for (int i=0;i<totalKey;i++) {
                CIPHER_KEY[i]= hexCount;
                hexCount +=  (byte)0X01;
            }
            return CIPHER_KEY;
        }

    }

    public static void main(String[] args) throws Exception{
        new ECBTest(false);
    }

}
