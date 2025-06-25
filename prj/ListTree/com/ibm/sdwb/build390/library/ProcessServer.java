package com.ibm.sdwb.build390.library;

import java.rmi.*;
import java.rmi.server.* ;
import java.rmi.registry.*;
import java.util.*;

import java.util.logging.*;
import java.io.*;
import com.ibm.sdwb.build390.*;

public class ProcessServer extends UnicastRemoteObject implements ProcessServerInterface {

    public static final String GENERALSERVER = "general";
    public static final String CONFIGSERVER = "config";
    public static final String LIBRARYSERVER = "library";
    public static final String RMINAMINGSEPARATOR = "b390sep";

    public static final String CMVCMODE = "CMVCMODE";
    public static final String CLEARCASEMODE = "CLEARCASEMODE";

	private String processServerName = null;
    
    /**
     * A given java runtime should only be running for one library, 
     * so we can make this static to allow simpler references for
     * anything that needs to verify the library mode.
     */
    private static String activeLibraryMode = null;  

	public ProcessServer(String tempProcessServerName) throws RemoteException {
		super();
		if (tempProcessServerName != null) {
			processServerName = tempProcessServerName.trim().toLowerCase();
		}
		if ((MBGlobals.Build390_path = System.getProperty("MBGlobals.Build390_path")) == null) {
			MBGlobals.Build390_path = System.getProperty("user.dir");
		}
		if (!MBGlobals.Build390_path.endsWith(File.separator)) {
			MBGlobals.Build390_path = MBGlobals.Build390_path+File.separator;
		}
		System.out.println(processServerName + " service started.");
	}

    public String getServerVersion() throws java.rmi.RemoteException{
        return com.ibm.sdwb.build390.MBConstants.getProgramVersion();
    }

    public void setActiveLibraryMode(String tempLibraryMode){
        activeLibraryMode=tempLibraryMode;
    }

    public static boolean isCMVCMode(){
        return CMVCMODE.equals(activeLibraryMode);
    }

    public static boolean isClearCaseMode(){
        return CLEARCASEMODE.equals(activeLibraryMode);
    }

    public void registerRMIServices(Registry registry, ProcessServer generalServer, Remote configServer, Remote libraryServer)throws Exception{
        String names[] = registry.list();

        for (int i=0; i<names.length; i++) {
            if (names[i].equals(processServerName.toLowerCase()+ProcessServer.RMINAMINGSEPARATOR+GENERALSERVER)) {
                System.out.println("A server was found for "+processServerName+".");
                System.exit(1);
            }
        }

        registry.bind(processServerName.toLowerCase()+RMINAMINGSEPARATOR+GENERALSERVER, generalServer);
        registry.bind(processServerName.toLowerCase()+RMINAMINGSEPARATOR+LIBRARYSERVER, libraryServer);
        registry.bind(processServerName.toLowerCase()+RMINAMINGSEPARATOR+CONFIGSERVER, configServer);
    }

    public static void unregisterRMIServices(String processServerName, Registry registry) throws Exception{
        registry.unbind(processServerName.toLowerCase()+RMINAMINGSEPARATOR+GENERALSERVER);
        registry.unbind(processServerName.toLowerCase()+RMINAMINGSEPARATOR+CONFIGSERVER);
        registry.unbind(processServerName.toLowerCase()+RMINAMINGSEPARATOR+LIBRARYSERVER);
    }

	protected synchronized void writeToLog(String stringToWrite) {
		try {
			BufferedWriter logWrite = new BufferedWriter(new FileWriter("opServer"+processServerName+".log", true));
			logWrite.write(new java.util.Date().toString() + " " + stringToWrite);
			logWrite.newLine();
			logWrite.close();
		} catch (IOException ioe) {
			ioe.printStackTrace();
		}
	}

	protected synchronized void writeToLog(Exception e) {
		StringWriter exceptionString = new StringWriter();
		PrintWriter exceptionHolder = new PrintWriter(exceptionString);
		e.printStackTrace(exceptionHolder);
		exceptionHolder.flush();
		writeToLog(exceptionString.toString());
		exceptionHolder.close();
	}

	public static void main(String args[]) {
		String processServerName = args[0];
		String port = args[1];
        String tempLibraryMode = args[2];
		String action = args[3];
        try {
			int portNum = Integer.parseInt(port);
			Registry registry = LocateRegistry.getRegistry(portNum);
			if (action.equalsIgnoreCase("s") |  action.equalsIgnoreCase("start")) {
				// start rmiregistry and bind to the given port
                try {
                    registry = LocateRegistry.createRegistry(portNum);
                } catch (ExportException ex) {
                    registry = LocateRegistry.getRegistry(portNum);
                }
                Remote configServer = null;
                Remote libraryServer = null;
                SimpleFormatter theFormatter = new SimpleFormatter();
                ProcessServer thisServer = new ProcessServer(processServerName);
                thisServer.setActiveLibraryMode(tempLibraryMode);
                if (thisServer.isCMVCMode()) {
                    String dbName = args[4];
                    String dbUserid = args[5];
                    String databasePasswordFilename = args[6];
                    String dbPassword = getPasswordFromFile(databasePasswordFilename);
                    com.ibm.sdwb.build390.database.DatabaseUtilities dbUtils = new com.ibm.sdwb.build390.database.DatabaseUtilities(dbName, dbUserid, dbPassword);
                    Logger configLogger = Logger.getLogger("com.ibm.sdwb.build390.configuration");
                    configLogger.setLevel(Level.WARNING);
                    String configLogFile = processServerName+".configLogger.log";
                    FileHandler configFile = new FileHandler(configLogFile, true);
                    configFile.setFormatter(theFormatter);
                    configLogger.addHandler(configFile);
                    configServer = new com.ibm.sdwb.build390.configuration.server.db2.ConfigurationRemoteServiceImplementation(processServerName, configLogger, dbUtils);
                    Logger libraryLogger = Logger.getLogger("com.ibm.sdwb.build390.library.server");
                    libraryLogger.setLevel(Level.WARNING);
                    String libraryLogFile = processServerName+".libraryLogger.log";
                    FileHandler libraryFile = new FileHandler(libraryLogFile, true);
                    libraryFile.setFormatter(theFormatter);
                    libraryLogger.addHandler(libraryFile);
                    libraryServer = new com.ibm.sdwb.build390.library.cmvc.server.CMVCLibraryServer(processServerName, libraryLogger, dbUtils);
                }else {
                    throw new RuntimeException("Only CMVC mode implemented");
                }
                thisServer.registerRMIServices(registry, thisServer, configServer, libraryServer);
			} else if (action.equalsIgnoreCase("r") | action.equalsIgnoreCase("report")) {

				// first find out if there is a rmiregistry running at the given
				// location. ( This call should return null if not found, but
				// does not ).
				try {
					String names[] = registry.list();
					System.out.println( "Found registry at "+ port );
					System.out.println( "registry contains " + names.length + " entries" );
					for (int i=0; i<names.length; i++) {
						System.out.println( "  " + names[i] );
					}
				} catch (Exception e) {
					System.out.println( "Rmiregistry not found at " + port );
					e.printStackTrace();
					System.exit(1);
				}

			} else if (action.equalsIgnoreCase("q") | action.equalsIgnoreCase("quit")) {
				unregisterRMIServices(processServerName, registry);
			} else { 
                System.out.println("Usage:java cmvcOperationsServer <processServerName> <databaseInstance> <databaseUserId> <databasePassword> <port> <servicePort> s(tart)\n");
				System.out.println("Usage:java cmvcOperationsServer <processServerName> <databaseInstance> <databaseUserId> <databasePassword> <port> r(eport)\n" );
				System.out.println("Usage:java cmvcOperationsServer <processServerName> <databaseInstance> <databaseUserId> <databasePassword> <port> q(uit)\n" );
				System.exit(1);
			}
		} catch (Exception e) {
			System.out.println("An error occurred created and registering the server ");
			e.printStackTrace();
			PrintWriter exceptionHolder = new PrintWriter(new StringWriter());
			e.printStackTrace(exceptionHolder);
			exceptionHolder.flush();
			try {
				BufferedWriter logWrite = new BufferedWriter(new FileWriter("opServerErr.log", true));
				logWrite.write(new java.util.Date().toString() + " " + exceptionHolder.toString());
				logWrite.newLine();
				logWrite.close();
			} catch (IOException ioe) {
				ioe.printStackTrace();
			}
			exceptionHolder.close();
		}
	}

	private static String getPasswordFromFile(String fileName) throws IOException{
		String returnString = null;
		File passFile = new File(fileName);
		BufferedReader fileReader = new BufferedReader(new FileReader(passFile));
		returnString = fileReader.readLine();
		fileReader.close();
		passFile.delete();
		return returnString;
	}
}
