package com.ibm.sdwb.build390.security;
import java.util.Hashtable;
import java.util.Map;

import com.ibm.sdwb.build390.MBClient;
import com.ibm.sdwb.build390.MBPw;
import com.ibm.sdwb.build390.PasswordError;
import com.ibm.sdwb.build390.userinterface.text.utilities.CommandLineSettings;
import com.ibm.sdwb.build390.userinterface.text.utilities.ConsoleListener;
import com.ibm.sdwb.build390.*;


public class PasswordManager {


    private static PasswordManager passwordManager;
    public static final String PASSWORD_START_KEY = "PW";
    private static Hashtable<String,String> passwordHash  = new Hashtable<String,String>();


    public static PasswordManager getManager() {
        if(passwordManager == null) {
            passwordManager = new PasswordManager();
        }
        return passwordManager;
    }

    private Hashtable<String,String> getPasswordStore() {
        return passwordHash;
    }


    public  void setValuesFromMap(Map environmentMap) {
    }

    public  String getPassword(String userAtServerKey) throws PasswordError {
        return getPassword(userAtServerKey,false);
    }

    public  String getPassword(String userAtServerKey,boolean upperCase )throws PasswordError {
        synchronized(getPasswordStore()) {
            String password = getValue(userAtServerKey);

            if(!MBClient.isClientMode()) {
                password = "CMVCSERV";  // if this is an RMI mode request,send back CMVCSERV
            }

            if(password==null || (password!=null && password.trim().length() <=0)) {
                password = promptAndGetPassword(userAtServerKey);
            }

            if(password!= null && password.trim().length() > 0) {
                setPassword(userAtServerKey, password);
                if(upperCase) {
                    password = password.toUpperCase();
                }
            }
            else {
                throw new PasswordError("No password found.");
            }
            return password;

        }
    }

    private  String promptAndGetPassword(String tempUserATServerKey) {
        String password = null;
        if(CommandLineSettings.getInstance().isCommandLine()) {
            //Begin INT3368
            if(MBClient.getNoPromptMode()==true) {
                System.out.println("Error:Invalid prompt attempted in NOPROMPT mode");

                System.out.println("Client is prompting for valid Password for "+tempUserATServerKey);

                MBClient.exitApplication(MBConstants.INVALIDPROMPTERROR);
            }
            //End INT3368
            else {
                password = ConsoleListener.getInstance(MBClient.getCommandLineHandler()).getResponse("Enter password for " + tempUserATServerKey + ":", true);
                System.out.println("Password read");
            }
        }
        else {
            MBPw pw = new MBPw(tempUserATServerKey);
            password = pw.getPassword();
        }



        return password;
    }

    public  void setPassword(String userATServerKey, String password) {
        if(password == null) {
            getPasswordStore().remove(getKey(userATServerKey)); 
        }
        else {
            getPasswordStore().put(getKey(userATServerKey), password); 
        }
    }

    private String getValue(String tempUserAtServerKey) {
        return getPasswordStore().get(getKey(tempUserAtServerKey));
    }

    private String  getKey(String tempUserAtServerKey) {
        if(!tempUserAtServerKey.toUpperCase().matches("^"+ PASSWORD_START_KEY +".*")) {
            tempUserAtServerKey = PASSWORD_START_KEY + tempUserAtServerKey;
        }
        return tempUserAtServerKey.toUpperCase();

    }

}
