
package com.ibm.sdwb.build390.security.encryption.aes.testsuite;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Scanner;



public class AESTestValueGenerator {

    private String fileName;

    private List plainText128 = new ArrayList();
    private List key128 = new ArrayList();
    private List plainText192 = new ArrayList();
    private List key192 = new ArrayList();
    private List plainText256 = new ArrayList();
    private List key256 = new ArrayList();

    public AESTestValueGenerator(String tempFileName) throws Exception {
        this.fileName= tempFileName;
        load();
    }

    public void  load() throws Exception {
        Scanner scanner = new Scanner(new File(fileName));
        scanner.useDelimiter(System.getProperty("line.separator")); 
        boolean found = false;
        boolean is128Bit = false;
        boolean is192Bit = false;
        boolean is256Bit = false;
        while (scanner.hasNext()) {
            String temp = scanner.next();
            if (temp.equals("KEYSIZE=128")) {
                is128Bit = true;
                is192Bit = true;
                is256Bit = true;
            } else if (temp.equals("KEYSIZE=192")) {
                is128Bit = false;
                is192Bit = true;
                is256Bit = false;
            } else if (temp.equals("KEYSIZE=256")) {
                is128Bit = false;
                is192Bit = false;
                is256Bit = true;
            }

            String val = parsePTLine(temp);
            if (val!=null) {
                if (is128Bit) {
                    plainText128.add(val);
                } else if (is192Bit) {
                    plainText192.add(val);
                } else if (is256Bit) {
                    plainText256.add(val);
                }
            } else if ((val = parseKEYLine(temp))!=null) {
                if (is128Bit) {
                    key128.add(val);
                } else if (is192Bit) {
                    key192.add(val);
                } else if (is256Bit) {
                    key256.add(val);
                }
            }
        }
    }


    public void generateTestClass(String filename) throws Exception {
        File tempfile = new File(filename);
        String tname = (tempfile.getName().substring(0,tempfile.getName().indexOf(".")));
        String name = tname.toUpperCase();
        String filedir = filename.substring(0,filename.indexOf(tname));
        BufferedWriter writer = new BufferedWriter(new FileWriter(filedir + name + ".java"));
        writer.write("package com.ibm.sdwb.build390.security.encryption.aes.testsuite;");
        writer.newLine();
        writer.write("//AUTO-GENERATED By com.ibm.sdwb.build390.security.encryption.aes.testsuite.AESTestValueGenerator");
        writer.newLine();
        writer.write("//Refer com.ibm.sdwb.build390.security.encryption.aes.testsuite.AESTestValueGenerator to make changes.");
        writer.newLine();
        writer.write("public class " + name +" {");
        writer.newLine();
        writer.newLine();
        writeAllMethods(writer,name);
        writer.newLine();
        writer.write("}");
        writer.close();

    }
    public void writeAllMethods(BufferedWriter writer,String filename) throws IOException {
        int k=0;
        for (Iterator iter= plainText128.iterator();iter.hasNext();) {
            k++;
            writer.write(writeSingleMethod("getPlainText128",filename,(String)iter.next(),k));
        }

        k=0;
        for (Iterator iter= plainText192.iterator();iter.hasNext();) {
            k++;
            writer.write(writeSingleMethod("getPlainText192", filename,(String)iter.next(),k));
        }

        k=0;
        for (Iterator iter= plainText256.iterator();iter.hasNext();) {
            k++;
            writer.write(writeSingleMethod("getPlainText256",filename,(String)iter.next(),k));
        }

        k=0;
        for (Iterator iter= key128.iterator();iter.hasNext();) {
            k++;
            writer.write(writeSingleMethod("getKey128",filename,(String)iter.next(),k));
        }

        k=0;
        for (Iterator iter= key192.iterator();iter.hasNext();) {
            k++;
            writer.write(writeSingleMethod("getKey192", filename,(String)iter.next(),k));
        }

        k=0;
        for (Iterator iter= key256.iterator();iter.hasNext();) {
            k++;
            writer.write(writeSingleMethod("getKey256",filename,(String)iter.next(),k));
        }


    }

    private String writeSingleMethod(String type,String filename,String plainText,int k) throws IOException {
        StringBuffer buf = new StringBuffer("public static byte[] "+type+"_"+filename + String.valueOf(k) + "() {");
        buf.append("\n");
        buf.append("    byte[] output = new byte[]{");
        StringReader reader = new StringReader(plainText);
        int j=0;
        int first=-1;
        String byteLine = "";
        while ((first =reader.read())!=-1) {
            char second = (char)reader.read();
            byteLine += ("(byte)0X"+(char)first+second);
            byteLine +=(", ");
            if (j==3) {
                j=0;
                byteLine +="\n    ";
            }
            j++;

        }
        reader.close();
        if (byteLine.trim().endsWith(",")) {
            byteLine = byteLine.substring(0,byteLine.lastIndexOf(","));
        }
        byteLine += "};\n\n";
        byteLine += "return output;\n";
        byteLine += "}";
        buf.append(byteLine.toString());
        buf.append("\n");
        buf.append("\n");
        return buf.toString();
    }

    private static String parsePTLine(String line) {
        if (line.startsWith("PT=")) {
            Scanner linescanner = new Scanner(line);
            linescanner.useDelimiter("=");
            String name  = linescanner.next();
            String value = linescanner.next();
            return value;
        }
        return null;
    }

    private static String parseKEYLine(String line) {
        if (line.startsWith("KEY=")) {
            Scanner linescanner = new Scanner(line);
            linescanner.useDelimiter("=");
            String name  = linescanner.next();
            String value = linescanner.next();
            return value;
        }
        return null;
    }



    public static void main(String[] args) throws Exception  {
        String testfile  =  System.getProperty("user.dir") +"\\com\\ibm\\sdwb\\build390\\security\\encryption\\aes\\testsuite\\";
        if (args.length< 1) {
            System.out.println("Enter a file name as an argument. eg: ecb_tbl.txt. The file should be there in\n" + testfile);
            System.exit(0);
        }
        testfile += args[0];

        if (!(new File(testfile)).exists()) {
            System.out.println("File not found !.\n"+testfile+"\n");
            System.exit(0);
        }

        AESTestValueGenerator ecbvk =   new AESTestValueGenerator(testfile);
        ecbvk.generateTestClass(testfile);
        System.exit(0);
    }
}
