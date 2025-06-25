package com.ibm.sdwb.build390.library.cmvc;

/*********************************************************************/
/* Java CMVCJavaAPIInterface class for the Build/390 client          */
/*  Asks the user for a String and returns it to the caller          */
/*********************************************************************/
// xx/xx/xx    new class by ken
// 03/21/2002  Defect INT0718. Now the window automatically pops up when a cmvclogin is needed. Fixed to throw the exact error message returned by CMVC.

import java.util.HashMap;

import com.ibm.sdwb.build390.*;
import com.ibm.sdwb.cmvc.client.api.*;

public class JavaAPIInterface {
    private static HashMap familyInfoHash = new HashMap();


    public static FamilyInfo getFamilyInfoObject(String familyName, String familyAddress, String familyPort) throws LibraryError{
        String infoKey = familyName+"@"+familyAddress+"@"+familyPort;
        FamilyInfo infoToGet = (FamilyInfo) familyInfoHash.get(infoKey);
        if (infoToGet == null) {
            infoToGet = new FamilyInfo();
            infoToGet.setFamilyName(familyName);
            infoToGet.setHostName(familyAddress);
            infoToGet.setPortNumber(Integer.parseInt(familyPort));
            try {
                infoToGet.retrieveServerVersion();
            } catch (Exception ioe) {
                throw new LibraryError("Error getting server version: " + ioe.getMessage(), ioe);
            }
            familyInfoHash.put(infoKey, infoToGet);
        }
        return infoToGet;
    }

    public static ClientDefaults getClientDefaults(String cmvcUsername, boolean usePasswordAuthentication, String extractionPath) {
        ClientDefaults clientDef = new ClientDefaults();
        if (usePasswordAuthentication) {
            clientDef.setProperty(ClientDefaults.CMVC_AUTH_METHOD, "PW");
        } else {
            clientDef.setProperty(ClientDefaults.CMVC_AUTH_METHOD, "HOST");
        }
        clientDef.setProperty(ClientDefaults.CMVC_BECOME,cmvcUsername);
        if (extractionPath != null) {
            clientDef.setProperty(ClientDefaults.CMVC_ROOT, extractionPath);
        }
        return clientDef;
    }

    public static void handleAuthentication(String localUsername, String cmvcBecomeUsername, String password, FamilyInfo familyInfo, SessionData sessionData) throws PasswordError,LibraryError{
        Authentication auth = null;
        if (password==null) {
            auth = new HostAuthentication(cmvcBecomeUsername, cmvcBecomeUsername);
        } else {
            auth = new PasswordAuthentication( cmvcBecomeUsername, cmvcBecomeUsername,password,"Build390");
            try {
                CommandResults authRes = ((PasswordAuthentication)auth).login(familyInfo, sessionData);
                checkCMVCResults(authRes,null, "An error occurred logging in to the server"); 
            } catch (CommandConstraintException cce) {
                throw new LibraryError("The login command was misformed.", cce);
            } catch (FamilyNotFoundException fnfe) {
                throw new LibraryError("The family could not be found.", fnfe);
            } catch (com.ibm.sdwb.cmvc.util.DataSourceException e) {
                Throwable emb = e.getDeepestThrowable(e);
                if (emb == null) {
                    throw new LibraryError("An exception occurred attempting to communicate with the family "+familyInfo.getHostName(), e);
                } else {
                    //throw the message sent by CMVC
                    throw new LibraryError(e.getMessage(), (Exception) emb);
                }
            } catch (Exception ioe) {
                //throw the message sent by CMVC
                throw new LibraryError(ioe.getMessage(), ioe);
            }
        }
        sessionData.setAuthentication( auth );
    }

    public static String checkCMVCResults(CommandResults commResults, Command cmvcCommand, String errorMessage)throws PasswordError,LibraryError{
        String outputString = new String();
        for (int i = 0; i < commResults.getMessages().length; i++) {
            outputString += commResults.getMessages()[i];
        }
        if (commResults.getReturnCode() != CommandResults.SUCCESS) {
            if (errorMessage == null) {
                errorMessage = "An error occurred during a CMVC call:";
            }
            if (cmvcCommand!= null) {
                errorMessage += "CMVC Command="+cmvcCommand.toString();
            }
            int rc = commResults.getReturnCode();

            if ((rc == PasswordAuthentication.PASSWORD_BAD) ||
                (rc == PasswordAuthentication.PASSWORD_EXPIRED) ||
                (rc == PasswordAuthentication.USER_PASSWORD_UNDEFINED) || 
                (rc ==PasswordAuthentication.USERNAME_BAD)) {
                throw new PasswordError(errorMessage+"\n"+ outputString,MBConstants.LIBRARY_PASSWORDERROR);
            }
            throw new LibraryError(errorMessage+"\n"+ outputString);
        }
        return outputString;
    }
}
