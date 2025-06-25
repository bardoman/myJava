package com.ibm.sdwb.build390.security.encryption.aes;
import java.util.Arrays;

import com.ibm.sdwb.build390.security.tools.BinaryMath;
import com.ibm.sdwb.build390.security.tools.BytesPrinter;
import com.ibm.sdwb.build390.security.tools.Utilities;


import static com.ibm.sdwb.build390.security.encryption.aes.RjindaelEngine.KeySize;
import static com.ibm.sdwb.build390.security.tools.BytesPrinter.MAX_HEX_COUNT;

class AESData {

    private       byte[] exponentialTable                   = new byte[256];
    private       byte[] logTable                           = new byte[256];
    private       byte[] multiplicativeInverse              = new byte[256];
    private       byte[] sBoxTable                          = new byte[256];
    private       byte[] sBoxInverseTable                   = new byte[256];
    private final byte[][] fixedPolynomialTable             = new byte[4][4];
    private final byte[][] inverseFixedPolynomialTable      = new byte[4][4];
    private static AESData lookup = null;

    private AESData() {
        generateTables();
    }

    protected static AESData getData() {
        if (lookup == null) {
            lookup = new AESData();
        }
        return lookup;
    }

    protected  byte getSBoxValueAt(int index) {
        return sBoxTable[index];
    }

    protected byte getInverseSBoxValueAt(int index) {
        return sBoxInverseTable[index];
    }

    protected byte[][] getFixedPolynomialTable() {
        return fixedPolynomialTable;
    }

    protected byte[][] getInverseFixedPolynomialTable() {
        return inverseFixedPolynomialTable;
    }

    protected byte[] getCipherKey(KeySize keySize,int keyInWORDLength) {
        int keyLength = keyInWORDLength * 4;
        byte[] PUBLIC_CIPHER_KEY = new byte[keyLength];
        Arrays.fill(PUBLIC_CIPHER_KEY,(byte)0X00);

        byte[] temp = new byte[]{(byte)0X2B, (byte)0X7E, (byte)0X15, (byte)0X16, 
            (byte)0X28, (byte)0XAE, (byte)0XD2, (byte)0XA6,
            (byte)0XAB, (byte)0XF7, (byte)0X15, (byte)0X88,
            (byte)0X09, (byte)0XCF, (byte)0X4F, (byte)0X3C};

        System.arraycopy(temp,0,PUBLIC_CIPHER_KEY,0,temp.length);

        int pos = keyLength - temp.length;

        switch (keySize) {
        case KEYSIZE_128BIT:
            return PUBLIC_CIPHER_KEY;
        case    KEYSIZE_192BIT:
            System.arraycopy(temp,0,PUBLIC_CIPHER_KEY,temp.length,pos);
            return PUBLIC_CIPHER_KEY;
        case    KEYSIZE_256BIT:
            System.arraycopy(temp,0,PUBLIC_CIPHER_KEY,temp.length,pos);
            return PUBLIC_CIPHER_KEY;
        default: //default 128 bit
            return PUBLIC_CIPHER_KEY;
        }

    }

    protected byte[] getRCon(int element) {
        byte[]  RCon = new  byte[4];
        Arrays.fill(RCon,(byte)0X00);

        byte temp = 0X01;
        if (element !=0) {
            for (int i=element;i >=1; i--) {
                temp = BinaryMath.multiply(temp,(byte)0X02,true);
            }
        }
        RCon[0]= (byte)temp;
        return RCon;
    }

    private void generateTables() {
        generateExponentialTable();
        generateLogarithmTable();
        generateMultiplicativeInverses();
        generateSBoxTable();
        generateInverseSBoxTable();
        generateFixedPolynomialTable(); 
        generateInverseFixedPolynomialTable(); 
    }


    private void generateExponentialTable() {
        int  x = 0X01;
        int index = 0;
        exponentialTable[index++] = (byte)0x01; //first index is zero.

        for (int i = 0; i < 255; i++) {
            byte y = BinaryMath.multiply(x, 0X03,true); //Basically 0X03 is our generator for finite field.
            exponentialTable[index++] = y;
            x = y;
        }
    }


    /** how many times can something be represented as the power of something is log.,
     * eg: 2 pow(1) = 2
     *    2 pow(2) = 4
     * lets multiply 4 X 8, by using log.
     * Multiply is adding in log on base  2 in our example.
     * log 4 (2)  + log 8 (2)  = 2 + 3 = 5.
     * Now take antilog of 5 on base 2 is (2 pow 5 = (2 X 2 X 2 X 2 X 2) = 32) 
     * Answer is 32.
     * Similarly we are representing  the multiplied factor by an exponential of 0X03 
     * as shown in the exponential table.
     * Log is just going to be the exponential value. 
     * ie in the series (3^0, 3^1, 3^2, 3^3, we need to store 1,2,3,4 in the log table.
     * so we are doing the opposite. i mean
     * we know the value 6 in the equation, we would like to know X  here  3^X = 6 
     * which is 2.
     * So the logic is to interate between 0-255 and set the 6th element in the logTable array as 2.
     */
    private void generateLogarithmTable() {
        for (int i = 0; i < 255; i++) {
            logTable[exponentialTable[i] & 0XFF] = (byte)i;
        }
    }

    /** in case of finite field mod. a multiplicative inverse 
     *  can be found using Euclidean algorithm. 
     *  in our case we just subtract ff.
     * eg: say for instance we have 5mod26 (lets consider 26 finite elements of alphabets. ABCDEFGH...)
     * Somebody applied our 5mod26  and gave a jumbled word EFGH.
     * Now to decrypt it, i need to apply a reverse mod or inverse mod. by using extended euclidean we say that
     * the reverse mod is -5 or (26-5 = 21). so 21mod26 is the inverse.
     * by using it we decrypt EFGH as ABCD.
     * If  03 is the generator. Inverse of 03^ X = 03 ^ (FF - X) 
     * To find the inverse of  94, get the value of 94 from the log table which is eb. 
     * and so 03 ^ (FF-94) = <somevalue say W). The exponential table for W give the value of the inverse.
     */
    private void  generateMultiplicativeInverses() {
        for (int i = 0; i < 256; i++) {
            multiplicativeInverse[i] = exponentialTable[0XFF - (logTable[i] & 0XFF)];
        }

    }


    /** S-Box 
     **/
    private void  generateSBoxTable() {
        for (int i = 0; i < 256; i++) {
            sBoxTable[i] =  (byte)(afflineTransform((byte) (i & 0XFF)) & 0XFF);
        }
    }

    /** S-Box 
     **/
    private void generateInverseSBoxTable() {
        for (int i = 0; i < 256; i++) {
            sBoxInverseTable[sBoxTable[i & 0XFF] & 0XFF] = (byte)(i & 0XFF);
        }
    }



    /** constant matric used in mixcolumn
     *        a(x) = {03}x3 + {01}x2 + {01}x + {02} .
     **/
    private void generateFixedPolynomialTable() {
        fixedPolynomialTable[0][0] =(byte)0X02;
        fixedPolynomialTable[0][1] =(byte)0X03;
        fixedPolynomialTable[0][2] =(byte)0X01;
        fixedPolynomialTable[0][3] =(byte)0X01;

        fixedPolynomialTable[1][0] =(byte)0X01;
        fixedPolynomialTable[1][1] =(byte)0X02;
        fixedPolynomialTable[1][2] =(byte)0X03;
        fixedPolynomialTable[1][3] =(byte)0X01;

        fixedPolynomialTable[2][0] =(byte)0X01;
        fixedPolynomialTable[2][1] =(byte)0X01;
        fixedPolynomialTable[2][2] =(byte)0X02;
        fixedPolynomialTable[2][3] =(byte)0X03;

        fixedPolynomialTable[3][0] =(byte)0X03;
        fixedPolynomialTable[3][1] =(byte)0X01;
        fixedPolynomialTable[3][2] =(byte)0X01;
        fixedPolynomialTable[3][3] =(byte)0X02;
    }


    /**
     * The columns are considered as polynomials over
     * GF(28) and multiplied modulo x4 + 1 with a fixed polynomial a-1(x), given by
     * a-1(x) = {0b}x3 + {0d}x2 + {09}x + {0e}.  section (5.9)
     * As described in Sec. 4.3, this can be written as a matrix multiplication. Let
     * s'(x) = a-1(x) (XOR) s(x) :
     * Inverse matrix is as follows.
     * 0E 0B 0D 09
     * 09 0E 0B 0D
     * 0D 09 0E 0B
     * 0B 0D 09 0E
     */
    private void generateInverseFixedPolynomialTable() {
        inverseFixedPolynomialTable[0][0] =(byte)0X0E;
        inverseFixedPolynomialTable[0][1] =(byte)0X0B;
        inverseFixedPolynomialTable[0][2] =(byte)0X0D;
        inverseFixedPolynomialTable[0][3] =(byte)0X09;

        inverseFixedPolynomialTable[1][0] =(byte)0X09;
        inverseFixedPolynomialTable[1][1] =(byte)0X0E;
        inverseFixedPolynomialTable[1][2] =(byte)0X0B;
        inverseFixedPolynomialTable[1][3] =(byte)0X0D;

        inverseFixedPolynomialTable[2][0] =(byte)0X0D;
        inverseFixedPolynomialTable[2][1] =(byte)0X09;
        inverseFixedPolynomialTable[2][2] =(byte)0X0E;
        inverseFixedPolynomialTable[2][3] =(byte)0X0B;

        inverseFixedPolynomialTable[3][0] =(byte)0X0B;
        inverseFixedPolynomialTable[3][1] =(byte)0X0D;
        inverseFixedPolynomialTable[3][2] =(byte)0X09;
        inverseFixedPolynomialTable[3][3] =(byte)0X0E;
    }

    /**
     * Basically the SBox is derived as follows.
     * The way i understand affline tranform is moving the bit vector to a  different plane by 'x' amount and 
     * we do a linear transformation with a standard value x6+x5+x2+x (01100011 = 0X63)
     * b(x) = (x6+x5+x2+x) + a(x)(x7+x6+x5+x4+1) mod (x8+1)
     * where a(x) is the multiplicativeInverse computed.
     * by referring the link below it is found that
     * (xi)mod (x8+1) = x (imod8) 
     * eg :
     * x0 (x power 0) mod (x power8 +1)  =  (x 0%8) ie. (x power 0mod8)
     * The inverse of the byte is first computed. which is 'b = multiplicativeInverse[b]' 
     * binary multiplication ie, - is shift and add. (like regular mult.) refer this link.http://hsc.csu.edu.au/sdd/options/view/data_rep/binarymultiplication.html
     * ^ operator performs XOR or  binary addition. ie, (1 + 1 = 10, 1 + 0 = 1, 0 + 1 = 1, 0 + 0 = 0). this is actually mod2
     * This needs to be applied to each of the bit.
     * Each of the bits in the byte are represented as x0, x1, ... x7. They have go to go throught the above process to produce
     * b(0) and we keep adding to get the effective result.
     * ie. b(x) = b(0) + b(1) + ... b(7).
     * @param b
     * 
     * @return 
     * @see  http://cryptome.sabotage.org/aes/fips-197.htm#4.%20MATHEMATICAL%20PRELIMINARIES about 'polynomial reduction'
     */
    private  int  afflineTransform(byte  b) {
        byte c = (byte)0X63;
        int result  = 0;

        if (b != 0) {  // leave 0 unchanged 
            b = multiplicativeInverse[b & 0XFF];
        }

        for (int i = 0; i < 8; i++) {
            int temp =0;
            //x7+x6+x5+x4+1
            // equates to 76541
            //x7+x6+x2+x = 11000110 = 0X63
            //the equation sounds good. but why do you do that. ????   a
            // answer: refer the above link and the local workout on reducing polynomicals.
            temp  = Utilities.bitToRightMost(b,i) ^ 
                    Utilities.bitToRightMost(b,(i+4)%8) ^ 
                    Utilities.bitToRightMost(b,(i+5)%8) ^ 
                    Utilities.bitToRightMost(b, (i+6)%8) ^ 
                    Utilities.bitToRightMost(b,(i+7)%8) ^ 
                    Utilities.bitToRightMost(c,i);
            //why do you want to shift by i  ??
            //answer: since we move the bit above to the right most (bitToRightMost), we shift it the same 'i' shifts back, once the computation is done.
            //        refer bitToRightMost code to see the shifts. 
            result = result | (temp << i); 
        }
        return result;
    }

    private void dump() {
        BytesPrinter.printArrayAsTable("EX3",exponentialTable,MAX_HEX_COUNT,MAX_HEX_COUNT,true);
        BytesPrinter.printArrayAsTable("LO3",logTable,MAX_HEX_COUNT,MAX_HEX_COUNT,true);
        BytesPrinter.printArrayAsTable("MI3",multiplicativeInverse,MAX_HEX_COUNT,MAX_HEX_COUNT,true);
        BytesPrinter.printArrayAsTable("SB3",sBoxTable,MAX_HEX_COUNT,MAX_HEX_COUNT,true);
        BytesPrinter.printArrayAsTable("SI3",sBoxInverseTable,MAX_HEX_COUNT,MAX_HEX_COUNT,true);
        BytesPrinter.printArrayAsTable("FXP",fixedPolynomialTable,true);
    }




    public static void main(String[] args) {
        AESData.getData().dump();
    }




}
