import java.util.*;
import java.io.*;
import java.awt.Toolkit;

public class Torus
{
    public static String rant[]=
    {
        "Torus encodes a text file containing a list of messages for a treasure hunt",            
        "Usage => Torus <pageCnt> <fileName> [<randomFill='yes/no'>]",                              
        "pageCnt is the number of pages to generate",                                             
        "fileName is the list of messages to use",                                                
        "randomFill fills the empty spaces with random data. It is optional.",                                     
        "Example => Torus 10 cluefile.txt yes",                                                   
        "The output includes a series of pages as 25rows x 26cols coordinate grids.",             
        "The messages are written backward and diagonal in the grid.",                            
        "The messages wrap in a peculiar way.  Each ordinal page is treated as ",                 
        "a series of sections of a torus.  The tops and bottoms of the pages are",                
        "connected together like a tube and the trailing edge of the last page",                  
        "connects to the leading edge of the first page.",                                        
        "So to read a message the user must have the location of it's starting posision in the grid",              
        "The locations consist of a three value code => <rowIndex>,<colIndex>,<pageIndex>",       
        "Example => 10, T, 3   => This reads row=10, col=T, page=3",                              
        "The message is then read right to left, bottom to top until the edge of the page.",      
        "The user must then determine how that diagonal wraps to the next point in the Torus",    
        "to continue reading the message. There is really only one diagonal on the Torus.",       
        "It rotates around the minor circut like a ribbon and eventualy reconnects in a loop.",   
        "Messages always end with a number so avoid using",                                       
        "numerical values (ie:0-9).  Write them out as text instead.",                            
        "Only a-z, 0-9 character values are allowed all others will be stripped out.",            
        "Messages point to the physical location of clues",                                       
        "Clues consist of a small sticker with three number values.",                             
        "Example=> 10,5,2  ",                                                                     
        "The values coorespond to row,col,page",                                                  
        "These values are used to determine the next starting location of a message in the Torus",
        "The user must use cyclic addition so that the index wraps at the edge of the page and ", 
        "continues around to the other side of the page.",                                        
        "Example => A location row value of 10 with a clue row value of 5 adds to 15.",           
        "However a location row value of 20 with a clue row value of 10 wraps around to 5.",      
        "The user must perform this cyclic addition for row,col and page for the current message location",        
        "using the discoverd clue. This will derive the location of the next message in the Torus",                
        "Users search for clue stickers in the real world based on the current message.",         
        "They use the clues to transform the current message location into the next message location.",            
        "The messages direct the user to the location of new clues.",                             
        "This creates a chain that leads the user to treasure at the end.",                                        
    };

    static char legalChars[]=
    {
        'A','B','C','D','E','F','G',
        'H','I','J','K','L','M','N',
        'O','P','Q','R','S','T','U',
        'V','W','X','Y','Z','0','1',
        '2','3','4','5','6','7','8','9'
    };

    static String colHdrValues[]=
    {
        "A","B","C","D","E","F","G","H","I","J","K","L","M",
        "N","O","P","Q","R","S","T","U","V","W","X","Y","Z"
    };

    static Vector legalCharsVect = new Vector();

    static
    {
        for(int i=0;i!=legalChars.length;i++)
        {
            legalCharsVect.add(new Character(legalChars[i]));
        }
    }

    static String header = "==>A,B,C,D,E,F,G,H,I,J,K,L,M,N,O,P,Q,R,S,T,U,V,W,X,Y,Z";

    int majorCirc;
    int minorCirc;

    int pageCols = 26;
    int pageRows = 25;
    int pageCnt;

    Character torus[][]; 

    char msgBuffer[];

    String msgs[];

    Random rand = new Random();

    Vector msgLocs = new Vector();

    boolean timerTrigger = false;

    Vector tmpVect = new Vector();

    public Torus(int pageCnt, String fileName, boolean fillRand)throws Exception
    {
        LineNumberReader rd = new LineNumberReader(new FileReader(fileName));

        String tmp="";

        while(rd.ready())
        {
            tmp = rd.readLine().trim();

            if(tmp.length()!=0)
            {
                tmpVect.add(tmp);
            }
        }
        msgs=new String[tmpVect.size()];

        for(int n=0;n!=tmpVect.size();n++)
        {
            msgs[n]= (String) tmpVect.get(n);
        }

        this.pageCnt = pageCnt;

        minorCirc = pageRows;

        majorCirc = pageCols * pageCnt;

        torus = new Character[majorCirc][minorCirc];

        addMessages();

        fillFreeRand(fillRand);

        showPages();

        showLocs();
    }

    void showLocs()
    {
        int oldLoc[]=new int[3];
        int newLoc[]=new int[3];

        boolean first=true;

        for(int i=0;i!=msgLocs.size();i++)
        {
            int loc[] =(int[]) msgLocs.get(i);

            int page = (loc[0]/pageCols)+1;

            int col = loc[0] % pageCols;

            newLoc[0] = loc[1];
            newLoc[1] = col;
            newLoc[2] = page;

            if(!first)
            {
                int clue[] = getClue(oldLoc, newLoc);

                System.out.println("clue=>"+clue[0]+","+clue[1]+","+clue[2]);
            }
            else
            {
                first = false;
            }

            System.arraycopy(newLoc, 0, oldLoc, 0, newLoc.length);

            if(loc[1]==0)
            {
                loc[1]=pageRows;
            }

            if(col==0)
            {
                col = pageCols;
            }
            System.out.println("loc=>"+loc[1]+","+colHdrValues[col-1]+","+ page);

            System.out.println("Msg["+i+"]="+msgs[i]);
        }
    }

    int [] getClue(int oldLoc[],int newLoc[])
    {
        int tmp[]=new int[3];

        tmp[0] = getCluePart(newLoc[0],oldLoc[0], pageRows);

        tmp[1] = getCluePart(newLoc[1],oldLoc[1], pageCols);

        tmp[2] = getCluePart(newLoc[2],oldLoc[2], pageCnt);

        return tmp;
    }

    int getCluePart(int newLoc,int oldLoc,int max)
    {
        int ret=0;

        if(newLoc>oldLoc)
        {
            ret= newLoc-oldLoc;
        }
        else
            if(newLoc==oldLoc)
        {
            ret=0;
        }
        else
        {
            ret= max - oldLoc + newLoc;
        }
        return ret;
    }

    void addMessages()throws Exception
    {
        for(int n=0;n!=msgs.length;n++)
        {
            msgs[n] = cleanMsg(msgs[n]);

            msgs[n]+= Integer.toString(rand.nextInt(10));

            if(msgs[n].length()> (majorCirc * minorCirc))
            {
                throw new Exception("Message exceeds total space");
            }

            msgBuffer = msgs[n].toCharArray();

            int loc[]=findFree(msgBuffer.length);

            addMsg(loc,msgBuffer.length);

            msgLocs.add(loc);
        }
    }

    int [] findFree(int msgLength)throws Exception
    {
        Timer timer = new Timer();

        Task task = new Task();

        int loc[]=new int[2];        

        boolean locFound=false;

        timerTrigger = false;

        timer.schedule(task,2000);

        while(!locFound)
        {
            loc[0]= rand.nextInt(majorCirc);
            loc[1]= rand.nextInt(minorCirc);


            locFound = isFree(loc,msgLength);

            if(timerTrigger)
            {
                timer.cancel();

                throw new Exception("Timed out searching for free space");
            }
        }
        timer.cancel();

        return loc;
    }

    class Task extends TimerTask
    {
        public void run()
        {
            timerTrigger = true;
        }
    }

    boolean isFree(int inLoc[], int cnt)
    {
        int loc[]=new int[2];

        System.arraycopy(inLoc,0,loc,0,inLoc.length);

        boolean free=false;

        if(cnt==0)return true;

        if(torus[loc[0]][loc[1]] == null)
        {
            loc=nextLoc(loc);

            free = isFree(loc,--cnt);

            return free;
        }
        return false;
    }


    boolean addMsg(int loc[],int cnt)throws Exception
    {
        boolean free=false;

        if(cnt==0)return true;

        if(torus[loc[0]][loc[1]] != null)
        {
            throw new Exception("collision=>"+loc[0]+","+loc[1]);         
        }

        torus[loc[0]][loc[1]]= new Character(msgBuffer[cnt-1]);

        loc=nextLoc(loc);

        free = addMsg(loc,--cnt);

        return free;
    }

    int [] nextLoc(int loc[])
    {
        loc[0]++;
        if(loc[0]==majorCirc)
        {
            loc[0]=0;
        }
        loc[1]++;
        if(loc[1]==minorCirc)
        {
            loc[1]=0;
        }
        return loc;
    }

    String cleanMsg(String str)
    {
        Character ch;

        Vector tmpVect = new Vector();

        str = str.toUpperCase();

        for(int i=0;i!=str.length();i++)
        {
            ch = new Character(str.charAt(i));

            if( legalCharsVect.contains(ch))
            {
                tmpVect.add(ch);
            }
        }

        String retStr=new String();

        for(int c=0;c!=tmpVect.size();c++)
        {
            retStr+=((Character)tmpVect.get(c)).charValue();
        }

        return retStr;
    }

    void showPages()
    {
        char page[][];

        for(int p=0; p!=pageCnt;p++)
        {
            page=getPage(p);

            printPage(p,page);
        }
    }

    void printPage(int pageNum,char page[][])
    {
        String str;
        String rowHdr;

        System.out.println(header);

        for(int row=0;row!=pageRows;row++)
        {
            str="";

            for(int col=0;col!=pageCols;col++)
            {
                str+=page[col][row]+",";
            }
            rowHdr = Integer.toString(row+1);

            if(rowHdr.length()!=2)
            {
                rowHdr+=" ";
            }

            System.out.println(rowHdr+":"+str);
        }

        System.out.println("***Page_"+(pageNum+1)+"***\n");
    }

    char [][] getPage(int n)
    {
        char chs[][]=new char [pageCols][pageRows];

        int pageOfs = n * pageCols;

        for(int col=0;col!=pageCols;col++)
        {
            for(int row=0;row!=pageRows;row++)
            {
                chs[col][row]= torus[col+pageOfs][row].charValue();
            }
        }

        return chs;
    }

    static boolean isOdd(int n)
    {
        if((n % 2) == 0)
        {
            return false;
        }
        return true;
    }

    void fillFreeRand(boolean fill)
    {
        for(int x=0;x!=majorCirc;x++)
            for(int y=0;y!=minorCirc;y++)
            {
                if( torus[x][y] == null)
                {
                    if(!fill)
                    {
                        torus[x][y]= new Character(' ');
                    }
                    else
                    {
                        torus[x][y]= new Character(legalChars[rand.nextInt(legalChars.length)]);
                    }
                }
            }
    }

    public static void main(String args[])
    {
        if((args.length != 2)&(args.length != 3))
        {
            for(int i=0;i!=rant.length;i++)
            {
                System.out.println(rant[i]);
            }
        }
        else
        {
            boolean randFill= true;

            if(args.length==3)
            {
                if(args[2].toUpperCase().equals("NO"))
                {
                    randFill = false;
                }
            }

            try
            {
                new Torus(Integer.valueOf(args[0]).intValue(),args[1],randFill);
            }
            catch(Exception e)
            {
                Toolkit.getDefaultToolkit().beep();

                e.printStackTrace();
            }
        }
    }
}


