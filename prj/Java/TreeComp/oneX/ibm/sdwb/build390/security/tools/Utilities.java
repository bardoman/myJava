package com.ibm.sdwb.build390.security.tools;

import java.util.Arrays;


public  class Utilities {

    /**
  * To get the bit at a location we use the following. We perform an 
  * AND operation with the mask.
  * To get the bit at  1 0X01 --> 00000001 
  * To get the bit at  2 0X02 --> 00000010 
  * To get the bit at  3 0X04 --> 00000100 
  * To get the bit at  4 0X08 --> 00001000 
  * To get the bit at  5 0X10 --> 00010000 
  * To get the bit at  6 0X20 --> 00100000 
  * To get the bit at  7 0X40 --> 01000000 
  * To get the bit at  8 0X80 --> 10000000 
  * At the end we move the bit to the right most.
  */
    public static int bitToRightMost(byte b, int i) {
        int mask[] = {0X01,0X02,0X04,0X08,0X10,0X20,0X40,0X80};
        return(b & mask[i]) >> i;
    }

    //copy input bytes to  state of 4 x 4 format
    public static byte[][]  toState(byte[] in, int fromPos) {
        int k = fromPos;
        byte[][] state = new byte[4][4];
        for (int i=0;i<state.length;i++) {
            Arrays.fill(state[i],(byte)0X00);
        }
        for (int column = 0; column < 4; column++) {
            for (int row = 0; ((row < 4) && (k<in.length)); row++) {
                state[row][column] = in[k++];
            }
        }
        return state;
    }

    //copy  the state bytes back to the i dimensional format
    public static byte[] toOutput(byte[][] state) {
        int m = 0;
        byte[] out = new byte[16];
        Arrays.fill(out,(byte)0X00);
        for (int column = 0; column < 4; column++) {
            for (int row = 0; row < 4; row++)
                out[m++] = state[row][column];
        }
        return  out;
    }

    public static byte[] getZeroArray(int size) {
        byte[] zerobytes = new byte[size];
        Arrays.fill(zerobytes,(byte)0X00);
        return zerobytes;
    }

    public static  String toHexString(int temp) {
        return toHexString((byte)temp);
    }

    public static String  toHexString(byte p) {
        String hex = Integer.toHexString(p & 0XFF);
        hex = "00" + hex;
        int len = hex.length();
        hex = hex.substring(len - 2, len);
        return hex.toUpperCase();
    }

    

    //entry in format 5mod21 using extended euclidean
    public static void gcd(String entry) {
        String[] splitInput = entry.split("mod");

        int divisor =  Integer.parseInt(splitInput[0]);
        int dividend = Integer.parseInt(splitInput[1]);
        int maindivisor  = divisor;
        int maindividend = dividend;

        int Xi =0;
        int  XiMinus2 = 0;
        int  XiMinus1 = 1;

        int   QiMinus1 = 0;
        int   QiMinus2 = 0;
        int   Qi       = 0;
        int remainder  = 0;

        boolean done = false;
        int i =0;

        while (!done) {

            if (divisor!=0) {
                Qi = dividend/divisor;
                remainder = dividend%divisor;
            }

            if (i>=2) {

                Xi= (XiMinus2 -(XiMinus1*QiMinus2))%maindividend;
                if (Xi < 0) {
                    Xi += maindividend;
                }
                XiMinus2 = XiMinus1;
                XiMinus1 = Xi;

            }

            if (divisor==0) {
                done = true;
            }
            dividend = divisor;
            divisor = remainder;

            QiMinus2 = QiMinus1;
            QiMinus1 = Qi;
            i++;

        }

    }

}
