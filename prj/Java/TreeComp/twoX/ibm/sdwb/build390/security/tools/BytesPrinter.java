package com.ibm.sdwb.build390.security.tools;
import java.util.Formatter;

public class BytesPrinter {

    public static final int MAX_HEX_COUNT =16;

    public static void printArrayAsTable(String title,byte[][] rawArray) {
        printArrayAsTable(title,rawArray,false);
    }

    public static void printArrayAsTable(String title,byte[][] rawArray,boolean isPrintHeaderInHex) {
        System.out.println(toString(title,rawArray,isPrintHeaderInHex));
    }

    public static void printArrayAsTable(String title,byte[] rawArray,int row,int column) {
        printArrayAsTable(title,rawArray,row,column,false);
    }


    public static void printArrayAsTable(String title,byte[] rawArray,int row,int column, boolean isPrintHeaderInHex) {
        printArrayAsTable(title,rawArray,row,column,-1,isPrintHeaderInHex);
    }

    public static void printArrayAsTable(String title,byte[] rawArray, int row, int column,int NthOrderSquareMatrix,boolean isPrintHeaderInHex) {
        System.out.print(toString(title,rawArray,row,column,NthOrderSquareMatrix,isPrintHeaderInHex));
    }

    public static String toString(String title,byte[][] rawArray) {
        return toString(title,rawArray,false);
    }

    public static String toString(String title,byte[][] rawArray,boolean isPrintHeaderInHex) {
        int row    = rawArray.length;
        int column = rawArray[0].length;
        byte[] tempArray = new byte[row*column];

        for (int i=0;i<row;i++) {
            System.arraycopy(rawArray[i],0,tempArray,i*column,rawArray[i].length);
        }
        return toString(title,tempArray,row,column,isPrintHeaderInHex);
    }

    public static String toString(String title,byte[] rawArray,int row,int column) {
        return toString(title,rawArray,row,column,false);
    }


    public static String toString(String title,byte[] rawArray,int row,int column, boolean isPrintHeaderInHex) {
        return  toString(title,rawArray,row,column,-1,isPrintHeaderInHex);
    }

    public static String toString(String title,byte[] rawArray, int row, int column,int NthOrderSquareMatrix,boolean isPrintHeaderInHex) {
        StringBuilder sb = new StringBuilder();
        Formatter formatter = new Formatter(sb);
        formatDottedLines(formatter,4,column-1);
        formatStringWithSeparator(formatter,"%3s%1s",title);

        //if squareMatrixOfOrder = 4, then we need to print it as 4X4 matrices.
        boolean canBePrintedAsSquareMatrix = false;
        int     updatedColumn = column;
        int MAX_COLUMN_SERIAL_NOS = column;

        if (NthOrderSquareMatrix > 0) {
            if (((row*column)%NthOrderSquareMatrix == 0) &&
                (row*column >= (NthOrderSquareMatrix*NthOrderSquareMatrix))) {
                updatedColumn = NthOrderSquareMatrix;  //ok. sound good. we can print it as a sqare.
                canBePrintedAsSquareMatrix = true;
            }
        }

        if (isPrintHeaderInHex) {
            if (column >= MAX_HEX_COUNT) {
                MAX_COLUMN_SERIAL_NOS = MAX_HEX_COUNT;
            }
            hexCounter(formatter,"%-3s%1s",MAX_COLUMN_SERIAL_NOS); // going to have 16 values.
        } else {
            intCounter(formatter,"%3s%1s", MAX_COLUMN_SERIAL_NOS);
        }
        formatter.format("%n");
        formatStringWithSeparator(formatter,"%3s%1s","---",++MAX_COLUMN_SERIAL_NOS);
        formatter.format("%n");

        int k=-1;
        for (int i=0;i<row;i++) {
            String serialNum = String.valueOf(i);
            if (isPrintHeaderInHex) {
                serialNum = Utilities.toHexString(i*updatedColumn);
            }

            formatStringWithSeparator(formatter,"%-3s%1s",serialNum);

            for (int j =0;j<updatedColumn;j++) {
                k++;
                formatStringWithSeparator(formatter,"%-3s%1s",Utilities.toHexString(rawArray[k]));
            }

            if (canBePrintedAsSquareMatrix) {
                for (int m=0;m < (column -NthOrderSquareMatrix); m++) {
                    int element = (NthOrderSquareMatrix*NthOrderSquareMatrix) + i + (m*NthOrderSquareMatrix);
                    formatStringWithSeparator(formatter,"%-3s%1s",Utilities.toHexString(rawArray[element]));
                }

            }

            formatter.format("%n");
        }

        formatDottedLines(formatter,4,column-1);

        formatter.format("%n");
        return sb.toString();
    }


    public static void printArrayAsLine(String title, byte[][] byteArray) {
        printArrayAsLine(title,byteArray,false); 
    }

    public static void printArrayAsLine(String title, byte[][] byteArray,boolean padSpace) {
        System.out.print(toStringLine(title,byteArray,padSpace));
    }

    public static void printArrayAsLine(String title, byte[] byteArray) {
        printArrayAsLine(title,byteArray,false);
    }

    public static void printArrayAsLine(String title, byte[] byteArray,boolean padSpace) {
        System.out.print(toStringLine(title,byteArray,padSpace));
    }

    public static String toStringLine(String title, byte[][] byteArray) {
        return toString(title,byteArray,false);
    }

    public static String  toStringLine(String title, byte[][] byteArray, boolean padSpace) {

        int row    = byteArray.length;
        int column = byteArray[0].length;
        byte[] tempArray = new byte[row*column];

        for (int i=0;i<row;i++) {
            System.arraycopy(byteArray[i],0,tempArray,i*column,byteArray[i].length);
        }
        return toStringLine(title,tempArray,padSpace);
    }

    public static String toStringLine(String title, byte[] byteArray) {
        return toStringLine(title,byteArray,false);
    }

    public static String toStringLine(String title, byte[] byteArray,boolean padSpace) {
        StringBuilder sb = new StringBuilder();
        Formatter formatter = new Formatter(sb);
        formatter.format("%s=",title);
        int spaced = 2;
        if (padSpace) {
            spaced++;
        }
        for (int i=0;i<byteArray.length;i++) {
            formatter.format("%"+String.valueOf(spaced)+"s",Utilities.toHexString(byteArray[i]));
        }
        formatter.format("%n");
        return sb.toString();
    }


    //helper methods for this class. 
    private static void hexCounter(Formatter formatter,String stringSyntax, int repeatCount) {
        for (int i=0;i<repeatCount; i++) {
            formatStringWithSeparator(formatter,stringSyntax,Utilities.toHexString(i));
        }
    }

    private static void intCounter(Formatter formatter,String stringSyntax, int repeatCount) {
        for (int i=0;i<repeatCount; i++) {
            formatStringWithSeparator(formatter,stringSyntax,String.valueOf(i));
        }
    }

    private static void formatStringWithSeparator(Formatter formatter,String stringSyntax,String crudeString) {
        formatStringWithSeparator(formatter,stringSyntax,crudeString,1);
    }



    private static void formatStringWithSeparator(Formatter formatter,String stringSyntax,String crudeString,int repeatCount) {
        for (int i=0;i<repeatCount; i++) {
            formatter.format(stringSyntax,crudeString,"|");
        }
    }

    private static void formatDashes(Formatter formatter,int spaced, int repeatCount) {
        String stringSyntax = "%" + String.valueOf(spaced) + "s";

        StringBuilder dashesBuilder = new StringBuilder();
        for (int j=spaced; j>0;j--) {
            dashesBuilder.append("-");
        }
        for (int i = 0; i < repeatCount; i++) {
            formatter.format(stringSyntax,dashesBuilder.toString());
        }
    }

    private static void formatDottedLines(Formatter formatter,int dashCount,int repeatCount) {
        formatter.format("%4s","*---");
        formatDashes(formatter,dashCount,repeatCount);
        formatter.format("%4s","---*");
        formatter.format("%n");
    }

}
