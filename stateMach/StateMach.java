import java.util.Scanner;

public class StateMach {



    enum State {
        INIT, 
        ONE ,
        TWO ,
        THREE,
        FOUR ,
        FIVE 
    }

    void doOne()
    {
        System.out.println("doOne");
    }

    void doInit()
    {
        System.out.println("doInit");
    }

    void doState(State state){
        switch (state) {
        case INIT:
            doInit();
            break;
        case ONE:
            doOne();
            break;
        default:
            break;
        }
    }

    public static void main(String args[]) {  
        Scanner scan=new Scanner(System.in);

        StateMach stateMach=new StateMach();

        while (true) {
            for (State state : State.values()) {
                System.out.println(state);
                System.out.println("hit enter");
                scan.nextLine();
                stateMach.doState(state);
            }
        }
    }
}








