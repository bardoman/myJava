import java.net.*;
import java.lang.*;
import java.sql.Timestamp;
import java.util.*;

public class SocketTestConnect {
    public static void main(String args[]) {

        if (args.length <4) {
            System.out.println("SocketTestConnect <address> <port> <cnt> <delay>");
            System.exit(1);
        }

        java.util.Date date= new java.util.Date();

        Calendar calendar = Calendar.getInstance();

        int cnt = Integer.valueOf(args[2]).intValue();
        try {
            for (int i=0;i<=cnt;i++) {

                Timestamp currentTimestamp= new Timestamp(date.getTime());

                System.out.println(currentTimestamp);

                Socket sock = new Socket(args[0], Integer.valueOf(args[1]).intValue());

                System.out.println("testing socket connect for address="+args[0]+", port="+ args[1]);

                if (sock.isConnected()) {

                    System.out.println("Socket connected");

                } else {

                    System.out.println("Socket connection failure");
                }
                sock.close();

                try {
                    Thread.sleep(Integer.valueOf(args[3]).intValue());
                } catch (InterruptedException ie) {
                    //Handle exception
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
