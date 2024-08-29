import java.util.Scanner;

public class StateMach {

    enum State {
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
    void doTwo()
    {
        System.out.println("doTwo");
    }
    void doThree()
    {
        System.out.println("doThree");
    }
    void doFour()
    {
        System.out.println("doFour");
    }

    void doFive()
    {
        System.out.println("doFive");
    }

    void doState(State state){
        switch (state) {
        
        case ONE:
            doOne();
            break;
        case TWO:
            doTwo();
            break;
        case THREE:
            doThree();
            break;
        case FOUR:
            doFour();
            break;
        case FIVE:
            doFive();
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
                System.out.println("state="+state.name());
                System.out.println("hit enter");
                scan.nextLine();
                stateMach.doState(state);
            }
        }
    }
}








