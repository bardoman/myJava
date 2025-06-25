package com.ibm.sdwb.build390.security.encryption.aes.testsuite;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.util.Iterator;
import java.util.Map;
import java.util.SortedMap;

import com.ibm.sdwb.build390.security.encryption.aes.AESCipher;


public class AESTestRunner {


    public AESTestRunner() throws Exception {
        File ecb_vk_file = new File("ECB_VK_test.out");
        File ecb_vt_file = new File("ECB_VT_test.out");
        File ecb_tbl_file = new File("ECB_TBL_test.out");
        File ecb_iv_file = new File("ECB_IV_test.out");
        File mct_file = new File("MCT_test.out");
        File cbc_file = new File("CBC_test.out");


        ECBTest test1 =  new ECBTest(true);
        ecb_vk_file.delete();
        test1.ecbvk(4);
        runCrypt("ECB_VK",4,test1.getMap());
        test1.ecbvk(6);
        runCrypt("ECB_VK",6,test1.getMap());
        test1.ecbvk(8);
        runCrypt("ECB_VK",8,test1.getMap());

        ecb_vt_file.delete();
        test1.ecbvt(4);
        runCrypt("ECB_VT",4,test1.getMap());
        test1.ecbvt(6);
        runCrypt("ECB_VT",6,test1.getMap());
        test1.ecbvt(8);
        runCrypt("ECB_VT",8,test1.getMap());

        ecb_tbl_file.delete();
        test1.ecbtbl("128",4);
        runCrypt("ECB_TBL",4,test1.getMap());
        test1.ecbtbl("192",6);
        runCrypt("ECB_TBL",6,test1.getMap());
        test1.ecbtbl("256",8);
        runCrypt("ECB_TBL",8,test1.getMap());

        ecb_iv_file.delete();
        test1.ecbiv(4);
        runCrypt("ECB_IV",4,test1.getMap());
        test1.ecbiv(6);
        runCrypt("ECB_IV",6,test1.getMap());
        test1.ecbiv(8);
        runCrypt("ECB_IV",8,test1.getMap());

        mct_file.delete();
        runCryptMonteCarloTest(4);
        runDeCryptMonteCarloTest(4);
        runCryptMonteCarloTest(6);
        runDeCryptMonteCarloTest(6);
        runCryptMonteCarloTest(8);
        runDeCryptMonteCarloTest(8);

        cbc_file.delete();
        runCryptCBCTest(4);
        runDeCryptCBCTest(4);
        runCryptCBCTest(6);
        runDeCryptCBCTest(6);
        runCryptCBCTest(8);
        runDeCryptCBCTest(8);

        System.out.println();
        System.out.println("Please see the following files for test results:\n\n" + 
                           ecb_vk_file.getAbsolutePath() +"\n" +
                           ecb_vt_file.getAbsolutePath() +"\n" +
                           ecb_tbl_file.getAbsolutePath() +"\n" +
                           ecb_iv_file.getAbsolutePath() +"\n" +
                           mct_file.getAbsolutePath() +"\n" +
                           cbc_file.getAbsolutePath());
    }


    void runCrypt(String name,int Nk, Map tests) throws Exception {

        for (Iterator iter=tests.entrySet().iterator();iter.hasNext();) {
            Map.Entry entry = (Map.Entry)iter.next();
            AESTestCase testCase = (AESTestCase)entry.getValue();
            byte[] output = AESCipher.crypt(testCase.getPlainText(),testCase.getCipherKey(),testCase.getKeySize());
            testCase.setCipherText(output);
        }
        BufferedWriter output = new BufferedWriter(new FileWriter(name.toLowerCase() +"_test.out",true));
        System.out.println(name +" KEYSIZE="+ Nk*32);
        output.write("KEYSIZE="+Nk*32);
        output.newLine();
        for (Iterator iter=tests.entrySet().iterator();iter.hasNext();) {
            Map.Entry entry = (Map.Entry)iter.next();
            AESTestCase testCase = (AESTestCase)entry.getValue();
            String testCaseString = testCase.toString();
            output.write(testCaseString,0,testCaseString.length());
        }
        output.close();

    }

    void runCryptMonteCarloTest(int Nk) throws Exception{
        MCTTest test =  new MCTTest(Nk);
        test.runCrypt();

        BufferedWriter output = new BufferedWriter(new FileWriter("mct_e_test.out",true));
        output.write("KEYSIZE="+ Nk*32);
        output.newLine();

        for (Iterator iter=test.getMap().entrySet().iterator();iter.hasNext();) {
            Map.Entry entry = (Map.Entry)iter.next();
            AESTestCase testCase = (AESTestCase)entry.getValue();
            String testCaseString = testCase.toString();
            output.write(testCaseString,0,testCaseString.length());
        }
        output.close();

    }

    void runDeCryptMonteCarloTest(int Nk) throws Exception{
        MCTTest test =  new MCTTest(Nk);
        test.runDeCrypt();

        BufferedWriter output = new BufferedWriter(new FileWriter("mct_d_test.out",true));
        output.write("KEYSIZE="+ Nk*32);
        output.newLine();

        for (Iterator iter=test.getMap().entrySet().iterator();iter.hasNext();) {
            Map.Entry entry = (Map.Entry)iter.next();
            AESTestCase testCase = (AESTestCase)entry.getValue();
            String testCaseString = testCase.toString();
            output.write(testCaseString,0,testCaseString.length());
        }
        output.close();

    }


    void runCryptCBCTest(int Nk) throws Exception{
        CBCTest test =  new CBCTest(Nk);
        test.runCrypt();

        BufferedWriter output = new BufferedWriter(new FileWriter("cbc_e_test.out",true));
        output.write("KEYSIZE="+ Nk*32);
        output.newLine();
        for (Iterator iter=test.getMap().entrySet().iterator();iter.hasNext();) {
            Map.Entry entry = (Map.Entry)iter.next();
            AESTestCase testCase = (AESTestCase)entry.getValue();
            String testCaseString = testCase.toString();
            output.write(testCaseString,0,testCaseString.length());
        }
        output.close();
    }

    void runDeCryptCBCTest(int Nk) throws Exception{
        CBCTest test =  new CBCTest(Nk);
        test.runDeCrypt();

        BufferedWriter output = new BufferedWriter(new FileWriter("cbc_d_test.out",true));
        output.write("KEYSIZE="+ Nk*32);
        output.newLine();
        for (Iterator iter=test.getMap().entrySet().iterator();iter.hasNext();) {
            Map.Entry entry = (Map.Entry)iter.next();
            AESTestCase testCase = (AESTestCase)entry.getValue();
            String testCaseString = testCase.toString();
            output.write(testCaseString,0,testCaseString.length());
        }
        output.close();
    }


    public static void main(String[] args) throws Exception {
        new AESTestRunner();
    }
}
