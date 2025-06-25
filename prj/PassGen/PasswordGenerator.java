
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.IntStream;

/**
 * Generates password of desired length. See {@link #usage} method
 * for instructions and command line parameters. This sample shows usages of:
 * <ul>
 * <li>Method references.</li>
 * <li>Lambda and bulk operations. A stream of random integers is mapped to
 * chars, limited by desired length and printed in standard output as password
 * string.</li>
 * </ul>
 *
 */
public class PasswordGenerator {

    private static void usage() {
        System.out.println("Usage: PasswordGenerator LENGTH");
        System.out.println(
                          "Password Generator produces password of desired LENGTH.");
    }




    //Valid symbols.
/*
    static {
        IntStream.rangeClosed('0', '9').forEach(PASSWORD_CHARS::add);    // 0-9
        IntStream.rangeClosed('A', 'Z').forEach(PASSWORD_CHARS::add);    // A-Z
        IntStream.rangeClosed('a', 'z').forEach(PASSWORD_CHARS::add);    // a-z
    }*/

    /**
     * The main method for the PasswordGenerator program. Run program with empty
     * argument list to see possible arguments.
     *
     * @param args the argument list for PasswordGenerator.
     */
    public static void main(String[] argsx) {
		String[] args = {"20"};
        List<Integer> PASSWORD_CHARS = new ArrayList<>();

        IntStream.rangeClosed('0', '9').forEach(PASSWORD_CHARS::add);    // 0-9
        IntStream.rangeClosed('A', 'Z').forEach(PASSWORD_CHARS::add);    // A-Z
        IntStream.rangeClosed('a', 'z').forEach(PASSWORD_CHARS::add);    // a-z

        if (args.length != 1) {
            usage();
            return;
        }

        long passwordLength;
        try {
            passwordLength = Long.parseLong(args[0]);
            if (passwordLength < 1) {
                printMessageAndUsage("Length has to be positive");
                return;
            }
        } catch (NumberFormatException ex) {
            printMessageAndUsage("Unexpected number format" + args[0]);
            return;
        }
        /*
         * Stream of random integers is created containing Integer values
         * in range from 0 to PASSWORD_CHARS.size().
         * The stream is limited by passwordLength.
         * Valid chars are selected by generated index.
         */

        SecureRandom rand=new SecureRandom();

        IntStream ints= rand.ints(passwordLength, 0, PASSWORD_CHARS.size());

        /*  rand.ints(passwordLength, 0, PASSWORD_CHARS.size())
          .map(PASSWORD_CHARS::get)
          .forEach(i -> System.out.print((char) i));  */
    }

    private static void printMessageAndUsage(String message) {
        System.err.println(message);
        usage();
    }

}
