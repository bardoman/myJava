import java.util.*;
import java.lang.*;

public class torusKey {

    int pageCnt=5;
    char charAry[] ={'A','B','C','D','E','F','G','H','I','J','K','L','M','N','O','P','Q','R','S','T','U','V','W','X','Y','Z'};
    String ary[]= new String[180];

    torusKey() {


        for(int i=0;i!=ary.length;i++) {
            ary[i]=makeRandTriplet();
        }


        /*
        ary[6]="01,J,4";
        ary[10]="20,L,3";
        ary[13]="16,K,4";
        ary[19]="01,M,1";
        ary[26]="03,J,4";
        ary[31]="06,X,5";
        ary[32]="02,M,3";
        ary[38]="02,U,2";
        ary[41]="10,O,4";
        ary[44]="18,J,4";
        ary[52]="03,C,1";
        ary[57]="16,L,3";
        ary[66]="16,X,4";
        ary[72]="18,Q,5";
        ary[75]="02,M,1";
        */

        ary[0]="01,J,4";
        ary[7]="20,L,3";
        ary[11]="16,K,4";
        ary[14]="01,M,1";
        ary[20]="03,J,4";

        ary[27]="06,X,5";
        ary[32]="02,M,3";
        ary[33]="02,U,2";
        ary[39]="10,O,4";
        ary[42]="18,J,4";

        ary[45]="03,C,1";
        ary[53]="16,L,3";
        ary[58]="16,X,4";
        ary[67]="18,Q,5";
        ary[73]="02,M,1";


        int c=0;
        String index="";

        for(int i=0;i!=ary.length;i++) {
            index=String.valueOf(i+1);
            if(index.length()==1) {
                index="00"+index;
            }
            if(index.length()==2) {
                index="0"+index;
            }

            System.out.print("["+index+"]("+ary[i]+"), ");
            c++;

            if(c>= 5) {
                System.out.println();
                c=0;
            }
        }


    }

    String makeRandTriplet() {
        Random rand = new Random();

        String row=String.valueOf(rand.nextInt(24)+1);
        if(row.length()==1) {
            row="0"+row;
        }


        char col=charAry[rand.nextInt(25)];

        String page=String.valueOf(rand.nextInt(pageCnt)+1);

        return(String) row + "," + col + "," + page;
    }

    public static void main(String args[]) {
        new torusKey();
    }

}
