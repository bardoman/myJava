package com.ibm.sdwb.build390.security.encryption.aes;

import com.ibm.sdwb.build390.security.tools.BinaryMath;
import com.ibm.sdwb.build390.security.tools.BytesPrinter;
import com.ibm.sdwb.build390.security.tools.Utilities;


public class RjindaelEngine {

    private static final int BLOCK_SIZE = 4; 

    private static int Nb = BLOCK_SIZE; 
    private static int Nr = -1; 
    private static int Nk = -1; 

    public enum KeySize {
        KEYSIZE_128BIT,
        KEYSIZE_192BIT,
        KEYSIZE_256BIT
    }

    private KeySize keySize;

    private byte[] Key;
    private byte[] W;

    private boolean isDebug= false;
    private boolean printOutputAsTable= false;

    protected RjindaelEngine() {
        this(null);
    }

    protected RjindaelEngine(byte[] userkey) {
        this(userkey,KeySize.KEYSIZE_128BIT);
    }

    protected RjindaelEngine(byte[] userkey, KeySize tempKeysize) {
        this.keySize = tempKeysize;
        configure(userkey); 
        //this shouldnt happen
        if (Nr < 0 || Nk < 0) {
            throw new RuntimeException("Rjindael AES Cipher engine configuration error. The 128/192/256 bit setup not done.");
        }

    }

    protected KeySize getKeySize() {
        return keySize;
    }

    protected int getBlockSize() {
        return Nb;
    }


    /**
     *             Key Length      Block Size    Number of
     *             (Nk) word       (Nb) words    Round (Nr)
     * For 128        4                4           10
     * For 192        6                4           12
     * For 256        8                4           14
     * 
     * Note : A word is 32 bit. (4 bytes)
     * 
     * default 128 bit encryption.
     */
    protected void configure(byte[] keySent) {
        int KEY_LENGTH_128 = 4; 
        int NUMBER_OF_ROUNDS_128 = KEY_LENGTH_128 +6; 
        int KEY_LENGTH_192 = 6; 
        int NUMBER_OF_ROUNDS_192 = KEY_LENGTH_192 +6; 
        int KEY_LENGTH_256 = 8; 
        int NUMBER_OF_ROUNDS_256 = KEY_LENGTH_256 +6; 

        switch (keySize) {
        case KEYSIZE_128BIT:
            Nr= NUMBER_OF_ROUNDS_128;  
            Nk= KEY_LENGTH_128;
            break;
        case    KEYSIZE_192BIT:
            Nr= NUMBER_OF_ROUNDS_192;  
            Nk= KEY_LENGTH_192;
            break;
        case    KEYSIZE_256BIT:
            Nr= NUMBER_OF_ROUNDS_256;  
            Nk= KEY_LENGTH_256;
            break;
        default: //default 128 bit
            Nr= NUMBER_OF_ROUNDS_128;  
            Nk= KEY_LENGTH_128;
            break;
        }

        initialize(keySent);
        keyExpansion(Key,W);

    }


    private void initialize(byte[] useThisKey) {
        if (useThisKey==null) {
            Key = new byte[4*Nk];
            System.arraycopy(AESData.getData().getCipherKey(getKeySize(),Nk),0,Key,0,4*Nk);
        } else {
            this.Key = useThisKey;
        }
        W   = new byte[4*Nb*(Nr+1)];
    }

    protected void  invokeAESCrypt(byte[][] in, byte out[][]) {
        debugPrint("=====CRYPT: STARTS !",null);//debug
        byte[][] state = new byte[4][Nb];
        System.arraycopy(in,0,state,0,in.length);
        debugPrint("INP",state);//debug
        addRoundKey(state, 0, Nb-1); // See Sec. 5.1.4

        for (int round = 1; round <=  (Nr - 1); round++) {
            debugPrint("===CRYPT: ROUND " + round + " STARTS !",null);//debug
            debugPrint("ST"+ String.valueOf(round),state);//debug
            subBytes(state); // See Sec. 5.1.1
            debugPrint("SB" + String.valueOf(round),state);//debug
            shiftRows(state); // See Sec. 5.1.2
            debugPrint("SH"+ String.valueOf(round),state);//debug
            mixColumns(state); // See Sec. 5.1.3
            debugPrint("MX"+ String.valueOf(round),state);//debug
            addRoundKey(state, round*Nb, (round+1)*Nb-1);
            debugPrint("R"+ String.valueOf(round),state);//debug
            debugPrint("===CRYPT: ROUND " + round + " ENDS !",null);//debug
        }
        debugPrint("===CRYPT: ROUND FINAL" +" STARTS !",null); //debug
        subBytes(state);
        debugPrint("SBF",state);//debug
        shiftRows(state);
        debugPrint("SHF",state);//debug
        addRoundKey(state, Nr*Nb, (Nr+1)*Nb-1);
        debugPrint("RF",state);//debug
        debugPrint("===CRYPT: ROUND FINAL" +" ENDS !",null);//debug
        System.arraycopy(state,0,out,0,state.length);//debug
        debugPrint("CYP",out);//debug
        debugPrint("=====CRYPT: Ends.",null);//debug
    }


    protected void invokeAESDeCrypt(byte[][] in, byte out[][]) {
        debugPrint("=====DECRYPT: STARTS !",null);
        byte[][] state = new byte[4][Nb];
        System.arraycopy(in,0,state,0,in.length);
        debugPrint("INP",state);
        addRoundKey(state, Nr*Nb, (Nr+1)*Nb-1); // See Sec. 5.1.4

        for (int round =(Nr-1);round>=1;round--) {
            debugPrint("===DECRYPT: ROUND " + round + " STARTS !",null);
            debugPrint("ST"+ String.valueOf(round),state);
            invShiftRows(state); // See Sec. 5.3.1
            debugPrint("SH"+ String.valueOf(round),state);
            invSubBytes(state); // See Sec. 5.3.2
            debugPrint("SB"+ String.valueOf(round),state);
            addRoundKey(state, round*Nb, (round+1)*Nb-1);
            debugPrint("R"+ String.valueOf(round),state);
            invMixColumns(state); // See Sec. 5.3.3
            debugPrint("MX"+ String.valueOf(round),state);
            debugPrint("===DECRYPT: ROUND " + round + " ENDS !",null);
        }
        debugPrint("===DECRYPT: ROUND FINAL" +" STARTS !",null);
        invShiftRows(state);
        debugPrint("SBF",state);
        invSubBytes(state);
        debugPrint("SHF",state);
        addRoundKey(state, 0, Nb-1);
        debugPrint("RF",state);
        debugPrint("===DECRYPT: ROUND FINAL" +" ENDS !",null);
        System.arraycopy(state,0,out,0,state.length);
        debugPrint("DYP",out);
        debugPrint("=====DECRYPT: ENDS !",null);
    }

    private void addRoundKey(byte[][]state, int startWord, int endWord) {
        int temp = startWord*4;
        for (int column=0;column< Nb;column++) {
            for (int row=0;row  < 4; row++) {
                state[row][column] ^=W[temp];
                temp++;
            }
        }

    }

    private void subBytes(byte[][] state) {
        for (int row=0;row< 4;row++) {
            for (int column=0;column < Nb; column++) {
                state[row][column]=AESData.getData().getSBoxValueAt(state[row][column] & 0XFF);
            }
        }
    }

    private void invSubBytes(byte[][] state) {
        for (int row=0;row< 4;row++) {
            for (int column=0;column < Nb; column++) {
                state[row][column]=AESData.getData().getInverseSBoxValueAt(state[row][column] & 0XFF);
            }
        }
    }

    private void shiftRows(byte[][] state) {
        int number_of_shifts = 0;
        for (int row=1;row< 4;row++) {
            number_of_shifts++; //second row, row=1. shift one, third row, row=2, fourth row row=3
            int number_of_shifts_done= number_of_shifts;
            while (number_of_shifts_done > 0) {
                for (int temp=0;temp < Nb;  temp++) {
                    if (temp+1!=Nb) {
                        byte swap = state[row][temp+1];
                        state[row][temp+1] = state[row][temp];
                        state[row][temp] = swap;
                    }
                }
                number_of_shifts_done--;
            }
        }

    }

    private void invShiftRows(byte[][] state) {
        int number_of_shifts = 0;
        for (int row=1;row< 4;row++) {
            number_of_shifts++; //second row, row=1. shift one, third row, row=2, fourth row row=3
            int number_of_shifts_done= number_of_shifts;
            while (number_of_shifts_done > 0) {
                for (int temp=Nb;temp >0;  temp--) {
                    if (temp!=Nb) {
                        byte swap = state[row][temp-1];
                        state[row][temp-1] = state[row][temp];
                        state[row][temp] = swap;
                    }
                }
                number_of_shifts_done--;
            }
        }


    }


    private void mixColumns(byte[][] state) {
        for (int column =0; column < Nb;column++) {
            byte[] singleColumn = new byte[4];
            for (int row=0; row<4;row++) {
                singleColumn[row] = state[row][column];
            }
            byte[] mixedSingleColumn =  BinaryMath.matrixMultiply(AESData.getData().getFixedPolynomialTable(), singleColumn);

            for (int row=0; row<4;row++) {
                state[row][column] = mixedSingleColumn[row];
            }
        }
    }

    private void invMixColumns(byte[][] state) {
        for (int column =0; column < Nb;column++) {
            byte[] singleColumn = new byte[4];
            for (int row=0; row<4;row++) {
                singleColumn[row] = state[row][column];
            }
            byte[] mixedSingleColumn =  BinaryMath.matrixMultiply(AESData.getData().getInverseFixedPolynomialTable(), singleColumn);

            for (int row=0; row<4;row++) {
                state[row][column] = mixedSingleColumn[row];
            }
        }
    }


    private void keyExpansion(byte[] Key, byte[] W) {
        int WORD_SIZE = 4; //may be we could use Nb (block size) but key expansion is descrbed in word_size (32bits)
        byte[] temp = new byte[WORD_SIZE]; 
        debugPrint("KEY0",Key,Nk,Nb);
        int count =0;
        //copy the initial cipher key
        for (count=0; count < WORD_SIZE*Nk; count++) { //as per rjindael spec handle a word (32 bits at a time)
            W[count] = (Key[count]);
        }

        int i = count;
        debugPrint("W" +String.valueOf(i),W,Nk,Nb);
        for (i=count; i < WORD_SIZE*Nb * (Nr + 1); i=(i+WORD_SIZE)) {
            for (int j=0;j<Nb;j++) {
                temp[j] = W[i-WORD_SIZE+j];
            }
            debugPrint("T",temp,WORD_SIZE,1);
            if ((i/4) % Nk == 0) { //works for Nk = 4,6
                rotWord(temp);
                subWord(temp);
                debugPrint("SRW",temp,WORD_SIZE,1);
                byte[] rcon = AESData.getData().getRCon(((i/WORD_SIZE)/Nk) -1);
                debugPrint("RIN",rcon,WORD_SIZE,1);
                for (int j=0;j<temp.length;j++) {
                    temp[j] ^= rcon[j] ;
                }
                debugPrint("RON",temp,WORD_SIZE,1);
            } else if (Nk > 6 & (((i/4)%Nk) == WORD_SIZE)) { //works for Nk = 8
                subWord(temp);
            }
            debugPrint("BXR",temp,WORD_SIZE,1);
            for (int p=0;p < temp.length;p++) {
                W[i+p] =(byte)(W[i-WORD_SIZE*Nk+p] ^ temp[p]);
            }
            debugPrint("W"+String.valueOf(i) ,W,Nb,(i/WORD_SIZE)+1,WORD_SIZE,false);
        }
        debugPrint("W"+String.valueOf(i),W,Nb,(i/WORD_SIZE),WORD_SIZE,false);
    }

    private void subWord(byte[] temp) {
        for (int i=0;i<Nb;i++) {
            temp[i] = (byte)(AESData.getData().getSBoxValueAt(temp[i] & 0XFF) & 0XFF);
        }
    }

    private void rotWord(byte[] rotate) {
        int number_of_shifts_done= 1;
        while (number_of_shifts_done > 0) {
            for (int temp=0;temp < Nb;  temp++) {
                if (temp+1!=Nb) {
                    byte swap = rotate[temp+1];
                    rotate[temp+1] = rotate[temp];
                    rotate[temp] = swap;
                }
            }
            number_of_shifts_done--;
        }
    }

    protected void setDebug(boolean tempDebug, boolean tempDisplayAsTable) {
        this.isDebug= tempDebug;
        this.printOutputAsTable= tempDisplayAsTable;
    }

    private void debugPrint(String title,byte[][] temp) {
        if (isDebug) {
            if (temp==null) {
                System.out.println(title);
            } else if (printOutputAsTable) {
                BytesPrinter.printArrayAsTable(title,temp);
            } else {
                BytesPrinter.printArrayAsLine(title,temp);
            }
        }
    }

    private void debugPrint(String title,byte[] temp,int row,int col) {
        debugPrint(title,temp,row,col,-1,false);
    }

    private void debugPrint(String title,byte[] temp,int row,int col,int asSquareOfNthOrder,boolean isPrintHeaderInHex) {
        if (isDebug) {
            if (temp==null) {
                System.out.println(title);
            } else if (printOutputAsTable) {
                BytesPrinter.printArrayAsTable(title,temp,row,col,asSquareOfNthOrder,isPrintHeaderInHex);
            } else {
                BytesPrinter.printArrayAsLine(title,temp);
            }
        }
    }

}


