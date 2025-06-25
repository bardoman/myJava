package com.ibm.sdwb.build390.security.tools;
import java.util.Arrays;

public class BinaryMath {


    public static byte[] matrixMultiply(byte[][] multiplier, byte[] inputColumn) {
        byte[] output = new byte[inputColumn.length];
        Arrays.fill(output,(byte)0X00);
        int k = -1;
        for (int i = 0;i< inputColumn.length;i++) {
            k = -1;
            for (int j=0;j< multiplier[i].length; j++) {
                k++;
                byte tempm = multiply(multiplier[i][k],inputColumn[k],true);
                output[i] ^=  tempm;
                output[i]  = (byte)(output[i] & 0XFF);
               
            }

        }
        return output;
    }

    /**
     * This is same as debugMultiply with no System.outs.
     * 
     * @param tempA
     * @param tempB
     * @param reduce
     * 
     * @return 
     * @see debugMultiply for comments
     */
    public static byte multiply( int tempA, int tempB,boolean reduce) {
        byte a = (byte)tempA;
        byte b = (byte)tempB;

        byte p =0X0; //fill initial product p with zeroes

        for (int i=0;i<8;i++) {
            if ((b & 0X1)!=0) { //if lower order bit of 'b' is set to 1, XOR with product 'p'. 
                p = (byte)(p ^ a); // XOR 'p' with 'a'
            }

            int t = a & 0X80; //check if higher order bit of 'a' is set to 1,

            a   =  (byte)(a << 1); // unsigned rotatation of 'a '1 bit to the left;
//The polynomial m(x ) (‘11B’) for the multiplication in GF(28) is the first one of the list of
//irreducible polynomials of degree 8, given in [LiNi86, p. 378].
            if (reduce && t!=0) { //if higher order bit of 'a' is set to 1, XOR with X8+x4+x3+x+1 (0X1B). 
                a ^= 0X1B;
            }

            int temp1 = (b & 0XFF) >> 1; // unsigned rotatation of 'b '1 bit to the right;
            b = (byte)temp1;

        }
        p = (byte)(p & 0xFF); //make it unsigned.

        return p;

    }

    /**
     * Byte multiplication. is as follows.
     * It is like regular multiplication for 2 or more digits.
     * ie.      10
     *          X12
     *          ---
     *          20 ---> First 2 multiplies 10
     *          10  ---> Then  1 multiplies 10
     *         ------
     *         120 ---> Add the results.
     *         ------
     * The above is equivalent to mutiplying 10 by 2 and shifting 12 by 1 digit.
     * In the same way in byte mutltiplication we rotate each bit, and if its 1, then we do an XOR or else nothing.
     * 
     * @param tempA
     * @param tempB
     * @param reduce The finite fields are classified as follows (Jacobson 1985, p. 287):
     *               The order, or number of elements, of a finite field is of the form pn, where p is a prime number called the
     *               characteristic of the field, and n is a positive integer.
     *               For every prime number p and integer n ? 1, there exists a finite field with pn elements.
     *               Any two finite fields with the same number of elements are isomorphic (that is, their addition tables are
     *               essentially the same, and their multiplication tables are essentially the same).
     *               his classification justifies using a naming scheme for finite fields that specifies only the order of the field.
     *               One notation for a finite field is GF(pn), where the letters "GF" stand for "Galois field".
     *               Another common notation is  F{p^n}.
     *               xamples
     *               here exists a finite field GF(4) = GF(22) with 4 elements, and every field with 4 elements is
     *               isomorphic to this one. There is also a finite field GF(8) = GF(23) with 8 elements, and
     *               every field with 8 elements is isomorphic to this one. However, there is no finite field with 6 elements,
     *               because 6 is not a power of any prime.
     *               
     *               GF(2): The galois fields are 0, 2^0 = 1
     *               + | 0 1        · | 0 1
     *               --+----        --+----
     *               0 | 0 1        0 | 0 0
     *               1 | 1 0        1 | 0 1
     *               
     *               In case of AES the Galois Field chosen is GF(2^8).
     *               So the field contains the following.
     *               00000000 = 0
     *               00000001 = 1
     *               00000010 = 2
     *               00000011 = 3
     *               ....     ...
     *               ....     ...
     *               11111111 = 256
     *               
     *               if reduce is true - For AES hex 0X1B is multiplied.
     * 
     * @return 
     * @example 
     * Example 1: Lets say a = 4          b =2.
     * Our intent  is to multiply  p = a * b * 0X1B.
     * -----------------
     * Iteration 0
     * -----------------
     * p :0 a :4 b :2
     * Step 1: Lower order bit of b isNOT 1
     * Step 2: Rotate a by 1 bit to the right.
     * Step 3: Higher order bit of a isNOT 1
     * Step 4: Skip XOR of a with 0X1B
     * -----------------
     * Iteration 1
     * -----------------
     * p :0 a :8 b :1
     * Step 1: Lower order bit of b is1
     * Step 2: Rotate a by 1 bit to the right.
     * Step 3: Higher order bit of a isNOT 1
     * Step 4: Skip XOR of a with 0X1B
     * -----------------
     * Iteration 2
     * -----------------
     * p :8 a :10 b :0
     * Step 1: Lower order bit of b isNOT 1
     * Step 2: Rotate a by 1 bit to the right.
     * Step 3: Higher order bit of a isNOT 1
     * Step 4: Skip XOR of a with 0X1B
     * -----------------
     * Iteration 3
     * -----------------
     * p :8 a :20 b :0
     * Step 1: Lower order bit of b isNOT 1
     * Step 2: Rotate a by 1 bit to the right.
     * Step 3: Higher order bit of a isNOT 1
     * Step 4: Skip XOR of a with 0X1B
     * -----------------
     * Iteration 4
     * -----------------
     * p :8 a :40 b :0
     * Step 1: Lower order bit of b isNOT 1
     * Step 2: Rotate a by 1 bit to the right.
     * Step 3: Higher order bit of a isNOT 1
     * Step 4: Skip XOR of a with 0X1B
     * -----------------
     * Iteration 5
     * -----------------
     * p :8 a :80 b :0
     * Step 1: Lower order bit of b isNOT 1
     * Step 2: Rotate a by 1 bit to the right.
     * Step 3: Higher order bit of a is1
     * Step 4: Do XOR of a with 0X1B
     * -----------------
     * Iteration 6
     * -----------------
     * p :8 a :1b b :0
     * Step 1: Lower order bit of b isNOT 1
     * Step 2: Rotate a by 1 bit to the right.
     * Step 3: Higher order bit of a isNOT 1
     * Step 4: Skip XOR of a with 0X1B
     * -----------------
     * Iteration 7
     * -----------------
     * p :8 a :36 b :0
     * Step 1: Lower order bit of b isNOT 1
     * Step 2: Rotate a by 1 bit to the right.
     * Step 3: Higher order bit of a isNOT 1
     * Step 4: Skip XOR of a with 0X1B
     * -----------------
     * Result is  :8
     * 
     * Example 2: a= 0XB6 b=0X53
     * -----------------
     * Iteration 0
     * -----------------
     * p :0 a :b6 b :53
     * Step 1: Lower order bit of b is1
     * Step 2: Rotate a by 1 bit to the right.
     * Step 3: Higher order bit of a is1
     * Step 4: Do XOR of a with 0X1B
     * -----------------
     * Iteration 1
     * -----------------
     * p :b6 a :77 b :29
     * Step 1: Lower order bit of b is1
     * Step 2: Rotate a by 1 bit to the right.
     * Step 3: Higher order bit of a isNOT 1
     * Step 4: Skip XOR of a with 0X1B
     * -----------------
     * Iteration 2
     * -----------------
     * p :c1 a :ee b :14
     * Step 1: Lower order bit of b isNOT 1
     * Step 2: Rotate a by 1 bit to the right.
     * Step 3: Higher order bit of a is1
     * Step 4: Do XOR of a with 0X1B
     * -----------------
     * Iteration 3
     * -----------------
     * p :c1 a :c7 b :a
     * Step 1: Lower order bit of b isNOT 1
     * Step 2: Rotate a by 1 bit to the right.
     * Step 3: Higher order bit of a is1
     * Step 4: Do XOR of a with 0X1B
     * -----------------
     * Iteration 4
     * -----------------
     * p :c1 a :95 b :5
     * Step 1: Lower order bit of b is1
     * Step 2: Rotate a by 1 bit to the right.
     * Step 3: Higher order bit of a is1
     * Step 4: Do XOR of a with 0X1B
     * -----------------
     * Iteration 5
     * -----------------
     * p :54 a :31 b :2
     * Step 1: Lower order bit of b isNOT 1
     * Step 2: Rotate a by 1 bit to the right.
     * Step 3: Higher order bit of a isNOT 1
     * Step 4: Skip XOR of a with 0X1B
     * -----------------
     * Iteration 6
     * -----------------
     * p :54 a :62 b :1
     * Step 1: Lower order bit of b is1
     * Step 2: Rotate a by 1 bit to the right.
     * Step 3: Higher order bit of a isNOT 1
     * Step 4: Skip XOR of a with 0X1B
     * -----------------
     * Iteration 7
     * -----------------
     * p :36 a :c4 b :0
     * Step 1: Lower order bit of b isNOT 1
     * Step 2: Rotate a by 1 bit to the right.
     * Step 3: Higher order bit of a is1
     * Step 4: Do XOR of a with 0X1B
     * -----------------
     * Result is  :36
     * 
     * @see multiply
     */
   private byte debugMultiply( int tempA, int tempB,boolean reduce) {
        byte a = (byte)tempA;
        byte b = (byte)tempB;

        byte p =0X0; //fill initial product p with zeroes

        for (int i=0;i<8;i++) {
            System.out.println("-----------------");
            System.out.println("Iteration " + i);
            System.out.println("-----------------");
            System.out.println("p :" +Utilities.toHexString(p) +" a :"+ Utilities.toHexString(a) +" b :"+ Utilities.toHexString(b));
            System.out.print("Step 1: Lower order bit of b is");

            if ((b & 0X1)!=0) { //if lower order bit of 'b' is set to 1, XOR with product 'p'. 
                System.out.print("1");
                System.out.println();
                p = (byte)(p ^ a); // XOR 'p' with 'a'
            } else {
                System.out.print("NOT 1");
                System.out.println();
            }

            int t = a & 0X80; //check if higher order bit of 'a' is set to 1,

            System.out.println("Step 2: Rotate a by 1 bit to the right.");
            a   =  (byte)(a << 1); // unsigned rotatation of 'a '1 bit to the left;

            System.out.print("Step 3: Higher order bit of a is");
//The polynomial m(x ) (‘11B’) for the multiplication in GF(28) is the first one of the list of
//irreducible polynomials of degree 8, given in [LiNi86, p. 378].
            if (reduce && t!=0) { //if higher order bit of 'a' is set to 1, XOR with X8+x4+x3+x+1 (0X1B). 
                System.out.print("1");
                System.out.println();
                a ^= 0X1B;
                System.out.println("Step 4: Do XOR of a with 0X1B");
            } else {
                System.out.print("NOT 1");
                System.out.println();
                System.out.println("Step 4: Skip XOR of a with 0X1B");
            }

            int temp1 = (b & 0XFF) >> 1; // unsigned rotatation of 'b '1 bit to the right;
            b = (byte)temp1;

        }
        p = (byte)(p & 0xFF); //make it unsigned.

        System.out.println("-----------------");
        System.out.println("p :" +Utilities.toHexString(p));
        return p;

    }

    

    
}
