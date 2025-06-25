package com.ibm.sdwb.build390.userinterface.text.utilities;

import com.ibm.sdwb.build390.MBBuildException;
import com.ibm.sdwb.build390.MBClient;
import com.ibm.sdwb.build390.MBConstants;
import com.ibm.sdwb.build390.userinterface.text.commandline.CommandLineHandler;


public class ConsoleListener extends Thread {

    private boolean inputRequested = false;
    private String requestedOutput = null;
    private EraseThreadClass eraser=null;
    private static String CANCELSTRING = "CANCEL";
    private static ConsoleListener theListener = null;
    private CommandLineHandler processCommand=null;
    private volatile boolean  isListening = true;

    private ConsoleListener(CommandLineHandler tempProcessCommand) {
        super("ConsoleListener");
        this.processCommand = tempProcessCommand;
        ConsoleInputReader.getInstance().start();
    }

    public synchronized static ConsoleListener getInstance(CommandLineHandler tempProcessCommand) {
        if (theListener == null) {
            theListener = new ConsoleListener(tempProcessCommand);
            System.out.println("To Cancel enter the string \""+CANCELSTRING+"\" and hit enter");
        }
        return theListener;
    }

    public void run() {
        while (isListening() && ConsoleInputReader.getInstance().keepReading()) {
            String currentString = null;
            ConsoleInputReader.getInstance().waitOnInput();
            currentString = ConsoleInputReader.getInstance().getResponse();
            if (inputRequested) {
                synchronized(theListener) {
                    requestedOutput = currentString;
                    currentString = null;
                    theListener.notifyAll();
                }
            } else {
                handleUnpromptedUserInput(currentString);
            }
        }
    }

    public boolean isListening() {
        return isListening;
    }

    public void stopListening() {
        isListening = false;
        ConsoleInputReader.getInstance().stopReader();
    }


    private void handleUnpromptedUserInput(String unpromptedUserInput) {
        if (unpromptedUserInput!=null && isListening()) {
            if (unpromptedUserInput.trim().equalsIgnoreCase(CANCELSTRING)) {
                System.out.println("CANCEL signal received, Begining Halt...");
                try {
                    stopListening();
                    if (processCommand!=null) {
                        processCommand.cancelProcess();
                        MBClient.exitApplication(MBConstants.EXITSUCCESS);
                    } else {
                        System.out.println("TESTING ONLY...Cancelling...");
                        System.out.println("RC=0");
                        System.exit(0);
                    }
                } catch (MBBuildException mbe) {
                    MBClient.lep.LogException(mbe);
                }
            } else {
                System.out.println("Erroneous Input!");
                System.out.println("To Cancel enter the string \""+CANCELSTRING+"\" and hit enter");
            }
        }
    }

    public String getResponse(String requestString, boolean runEraser) {
        String myOutputCopy = null;
        System.out.println(requestString);

        if (runEraser) {
            eraser = new EraseThreadClass();
            eraser.start();
        }
        inputRequested = true;
        try {
            synchronized (theListener) {
                while (requestedOutput==null) {
                    theListener.wait();
                }
            }
        } catch (InterruptedException ie) {
            ie.printStackTrace();
        }
        inputRequested = false;
        if (eraser!=null) {
            eraser.stopRunning();
            eraser = null;
        }
        myOutputCopy = requestedOutput;
        requestedOutput = null;
        if (myOutputCopy != null) {
            if (myOutputCopy.trim().length()< 1) {
                myOutputCopy = null;
            }
        }
        return myOutputCopy;

    }


    private class EraseThreadClass extends Thread {
        private volatile boolean running = true;

        public void run() {
            if ((System.getProperty("os.name").indexOf("95") > -1) | (System.getProperty("os.name").indexOf("98") > -1)) {
                while (running) {
                    System.out.print('\b'+" "+'\b');
                    try {
                        Thread.sleep(125);
                    } catch (InterruptedException ie) {
                        ie.printStackTrace();
                    }
                }
            } else {
                while (running) {
                    System.out.print('\b'+" ");
                    try {
                        Thread.sleep(2);
                    } catch (InterruptedException ie) {
                        Thread.currentThread().interrupt();
                        return;
                    }
                }
            }
        }

        public void stopRunning() {
            System.out.printf("%s%s",'\b','\b');
            running = false;
        }
    }

    private static void testCase1() {
        System.out.println("======= Test case 1 ...");
        String output = ConsoleListener.getInstance(null).getResponse("Enter your password(not hidden mode):",false);
        System.out.println("Password read as(not hidden mode):"+output);

        output = ConsoleListener.getInstance(null).getResponse("Enter your password(hidden     mode):",true);
        System.out.println("Password read as(hidden     mode):"+output);
    }


    private static void testCase2() {
        System.out.println("======= Test case 2 ...");
        String output = ConsoleListener.getInstance(null).getResponse("Enter your password(not hidden mode):",false);
        System.out.println("Password read as(not hidden mode):"+output);

        output = ConsoleListener.getInstance(null).getResponse("Enter your password(hidden     mode):",true);
        System.out.println("Password read as(hidden     mode):"+output);
        ConsoleListener.getInstance(null).stopListening();
    }



    public static void main(String[] args) throws Exception {
        ConsoleListener.getInstance(null).start();
        if (args!=null && args.length > 0) {
            if (args[0].equals("1")) {
                testCase1();
            } else if (args[0].equals("2")) {
                testCase2();
            }

        } else {
            testCase1();
        }
    }
}
