import org.apache.commons.net.ftp.*;
import java.io.*;
import java.util.*;


public class TestFtp 
{
    static Hashtable replyCodes = new Hashtable();

    static
    {
        replyCodes.put("125" , "Data connection already open, transfer starting.");
        replyCodes.put("150" , "File status okay, about to open data connection.");
        replyCodes.put("200" , "Command successful.");
        replyCodes.put("202" , "Command not implemented, superfluous at this site.");
        replyCodes.put("213" , "File status.");
        replyCodes.put("214" , "Help message.");
        replyCodes.put("220" , "Service ready for new user.");
        replyCodes.put("221" , "Service closing control connection.");
        replyCodes.put("225" , "Data connection already open, no transfer in progress.");
        replyCodes.put("226" , "Closing data connection.");
        replyCodes.put("230" , "User logged in, proceed.");
        replyCodes.put("250" , "Requested action okay, completed.");
        replyCodes.put("257" , "Pathname is current directory. Command successful.");
        replyCodes.put("258" , "Command on (or off).");
        replyCodes.put("331" , "User name okay, need password." );
        replyCodes.put("350" , "Requested file action pending further information.");
        replyCodes.put("421" , "Service not available, closing control connection.");
        replyCodes.put("425" , "Can't open data connection.");
        replyCodes.put("426" , "Connection closed, transfer aborted.");
        replyCodes.put("451" , "Requested action aborted, local error in processing.");
        replyCodes.put("452" , "Requested action not taken.");
        replyCodes.put("500" , "Syntax error, command unrecognized.");
        replyCodes.put("501" , "Syntax error in parameters or agruments.");
        replyCodes.put("502" , "Command not implemented.");
        replyCodes.put("503" , "Bad sequence of commands.");
        replyCodes.put("504" , "Command not implemented for that parameter");
        replyCodes.put("505" , "No such file or directory.File being moved from the archive.");
        replyCodes.put("506" , "Usage error.");
        replyCodes.put("522" , "Transfer error bytes written.");
        replyCodes.put("530" , "Error in user login.");
        replyCodes.put("550" , "Requested action not taken due to error.");
        replyCodes.put("551" , "Requested action aborted.");
        replyCodes.put("553" , "Requested action not taken due to system error. User not authorized to use command.");
    }

    String fileName;
    FTPClient ftp = new FTPClient();
    int reply;
    boolean timeUp=false;
    long timeout = 0;

    String buffer="";

    Timer timer = new Timer();
    int xfrCnt =0;
    String pw="";
    String user="";
    int lineCnt=0;
    String remoteFileName="";
    String hostAddress="";
    GregorianCalendar calendar;

    public TestFtp(String args[])
    {
        user = args[0];

        pw =args[1];

        xfrCnt = Integer.valueOf(args[2]).intValue();

        timeout =   Integer.valueOf(args[3]).intValue();

        lineCnt =   Integer.valueOf(args[4]).intValue();

        remoteFileName = args[5];

        hostAddress = args[6];

        try
        {
            File file = File.createTempFile("TESTFTP",".dat",new File("."));

            fileName = file.getAbsolutePath();

            for(int i=0;i!=xfrCnt;i++)
            {
                timer.schedule(new myTimerTask(), timeout);

                while(!timeUp);
//
//Make data file
//
                FileWriter wr = new FileWriter(fileName);

                String time = new GregorianCalendar().getTime().toString();

                System.out.println(time);

                buffer = getDatString(lineCnt,String.valueOf(time));

                wr.write(buffer,0,buffer.length());

                wr.flush();

                wr.close();
//
//Connect
//
                System.out.println("Tranfer count="+i);

                System.out.println("Connecting to server");

                ftp.connect(hostAddress);

                reply = ftp.getReplyCode();

                System.out.println("reply="+reply+":"+(String)replyCodes.get(Integer.toString(reply)));

                if(!FTPReply.isPositiveCompletion(reply))fatLadysSings("FTP server refused connection.");
//
//authenticate
//
                System.out.println("logging into server");

                ftp.login(user,pw);

                reply = ftp.getReplyCode();

                System.out.println("reply="+reply+":"+(String)replyCodes.get(Integer.toString(reply)));

                if(!FTPReply.isPositiveCompletion(reply))fatLadysSings("Login failed");
//
//get cwd
//
                System.out.println("getting cwd");

                String cwd = ftp.printWorkingDirectory();

                System.out.println("reply="+reply+":"+(String)replyCodes.get(Integer.toString(reply)));

                if(!FTPReply.isPositiveCompletion(reply))fatLadysSings("get cwd failed");

                System.out.println("cwd="+cwd);
//
//get set ascii
//
                System.out.println("setting ascii file type");

                ftp.setFileType(FTP.ASCII_FILE_TYPE);

                reply = ftp.getReplyCode();

                System.out.println("reply="+reply+":"+(String)replyCodes.get(Integer.toString(reply)));

                if(!FTPReply.isPositiveCompletion(reply))fatLadysSings("Set File Type Failed");
//
//store file
//
                FileInputStream fil= new FileInputStream(file);

                System.out.println("Storing file");

                ftp.storeFile(remoteFileName,fil);

                reply = ftp.getReplyCode();

                System.out.println("reply="+reply+":"+(String)replyCodes.get(Integer.toString(reply)));

                if(!FTPReply.isPositiveCompletion(reply))fatLadysSings("Store file failed");
//
//complete
//
                System.out.println("*************Xfr Complete ****************\n");

                ftp.logout();

                ftp.disconnect();

                file.delete();
            }
            timer.cancel();

        }
        catch(IOException e)
        {
            System.err.println("IOException performing FTP Operation.");

            timer.cancel();

            e.printStackTrace();

            if(ftp.isConnected())
            {
                try
                {
                    ftp.disconnect();
                }
                catch(IOException f)
                {
                }
            }
            System.exit(1);
        }
    }

    public void fatLadysSings(String message)
    {   
        System.err.println(message);

        timer.cancel();

        try
        {
            ftp.disconnect();
        }
        catch(IOException e)
        {
        }

        System.exit(1);
    }

    public String getDatString(int xfrCnt, String timestamp)
    {
        String buffer="";

        for(int i=0;i!=xfrCnt;i++)
        {
            buffer+=timestamp+"\n";
        }
        return buffer;
    }

    public class myTimerTask extends TimerTask
    {
        public myTimerTask()
        {
            timeUp=false;
        }
        public void run()
        {
            timeUp=true;
        }
    }

    public static void main(String args[]) 
    {  
        if(args.length!=7)
        {
            System.out.println("TestFtp <userid> <pw> <transfer_cnt>  <timeout_millisecs>  <lineCnt> <remote_file_name> <host_address>");
            System.out.println("This utility performs ftp transfers for test.");
            System.out.println("It generates it's own unique test files in the local dir.");
            System.out.println("The timeout controls delay between transfers");
            System.out.println("The current timestamp is printed once per line up to the lineCnt specified.");
        }
        else
        {
            new TestFtp(args);
        }
    }
}
