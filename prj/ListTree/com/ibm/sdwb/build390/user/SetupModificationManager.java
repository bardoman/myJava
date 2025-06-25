package com.ibm.sdwb.build390.user;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import com.ibm.sdwb.build390.MBMainframeInfo;
import com.ibm.sdwb.build390.library.LibraryInfo;
import com.ibm.sdwb.build390.library.cmvc.CMVCLibraryInfo;
import com.ibm.sdwb.build390.library.fakelib.FakeLibraryInfo;
import com.ibm.sdwb.build390.userinterface.text.commandline.arguments.*;


public class SetupModificationManager {


    private static final String EDITOR = "EDITOR";
    private static final String DEFAULTEDITOR = "DEFAULTEDITOR";

/* this is redundant, the CommandLineProcess CreateSetup, SetupValidator contains all this as well */
    private static Library library = new Library();
    private static LibraryName libraryName = new LibraryName();
    private static LibraryAddress  libraryAddress = new LibraryAddress();
    private static LibraryUser libraryUser = new LibraryUser();
    private static LibraryPasswordAuthentication libraryPasswordAuthentication = new LibraryPasswordAuthentication();
    private static ProcessServerPort processServerPort = new ProcessServerPort();
    private static CMVCPort cmvcPort = new CMVCPort();
    private static MainframeName mainframeAddress = new MainframeName();
    private static MainframePort mainframePort = new MainframePort();
    private static MainframeUserid mainframeUserid = new MainframeUserid();
    private static MainframeAccountInformation  mainframeAccountInformation = new MainframeAccountInformation();
    private static Editor  editor = new Editor();
    private static DefaultEditor  defaultEditor = new DefaultEditor();
    private static SelectLibraryName  selectLibraryName = new SelectLibraryName();
    private static SelectMainframeName  selectMainframeName = new SelectMainframeName();

    private static Set mainframeValidParameters = new HashSet();
    private static Set cmvcValidParameters = new HashSet();
    private static Set fakeLibraryValidParameters = new HashSet();

    static {
        mainframeValidParameters.add(mainframeAddress.getCommandLineName());
        mainframeValidParameters.add(mainframePort.getCommandLineName());
        mainframeValidParameters.add(mainframeUserid.getCommandLineName());
        mainframeValidParameters.add(mainframeAccountInformation.getCommandLineName());
    }

    static {
        cmvcValidParameters.add(libraryName.getCommandLineName());
        cmvcValidParameters.add(libraryAddress.getCommandLineName());
        cmvcValidParameters.add(processServerPort.getCommandLineName());
        cmvcValidParameters.add(cmvcPort.getCommandLineName());
        cmvcValidParameters.add(libraryUser.getCommandLineName());
        cmvcValidParameters.add(libraryPasswordAuthentication.getCommandLineName());
    }

    static {
        fakeLibraryValidParameters.add(libraryName.getCommandLineName());
    }

    private static SetupModificationManager manager = null;


    private SetupModificationManager() {
    }

    public static SetupModificationManager getSetupModificationManager() {
        if (manager==null) {
            manager = new SetupModificationManager();
        }
        return manager;
    }


    public  void setValuesFromMap(Map parsedCommandLineValues) throws com.ibm.sdwb.build390.MBBuildException{

        /*
        we need to cover 3 cases for each 
        */
        // first we see if they specified a full library info object.  In which case we'll add it to the library set if it's not there, and make it the current one.
        LibraryInfo library =  doLibrary(parsedCommandLineValues);
        // at this point we should have completely handled all library related parameters.   
        // now we just do the same thing for the mainframe.
        MBMainframeInfo mainframe = doMainframe(parsedCommandLineValues);
    }

    private LibraryInfo doLibrary(Map parsedCommandLineValues) {
        library.setValue((String)parsedCommandLineValues.get(library.getCommandLineName()));
        if (library.isSatisfied()) {
            if (library.getValue().equalsIgnoreCase(Library.CMVC)) {
                return doCMVCLibrary(parsedCommandLineValues);
            } else if (library.getValue().equalsIgnoreCase(Library.FAKELIB)) {
                return doFakeLibrary(parsedCommandLineValues);
            }
        } else {
            throw new RuntimeException(library.getReasonNotSatisfied());
        }

        throw new RuntimeException("Invalid library " + library.getValue());
    }

    private LibraryInfo doCMVCLibrary(Map parsedCommandLineValues) {
        LibraryInfo libraryToUse =null;

        if (areAllParametersSpecified(parsedCommandLineValues,cmvcValidParameters)) {
            libraryToUse = new CMVCLibraryInfo();
            fillLibraryValues(parsedCommandLineValues,libraryToUse);
            libraryToUse = fillCMVCLibraryValues(parsedCommandLineValues,libraryToUse);
            if (!canLocate(SetupManager.getSetupManager().getLibraryInfoSet(),libraryToUse)) {
                SetupManager.getSetupManager().addLibraryInfo(libraryToUse);
            }

            SetupManager.getSetupManager().setCurrentLibrary(libraryToUse);
        }

        if (libraryToUse==null) {
            libraryToUse =  grabLibrary(parsedCommandLineValues,libraryToUse);
        }

        return libraryToUse;
    }

    private LibraryInfo doFakeLibrary(Map parsedCommandLineValues) {
        LibraryInfo libraryToUse =null;

        if (areAllParametersSpecified(parsedCommandLineValues,fakeLibraryValidParameters)) {
            libraryToUse = new FakeLibraryInfo();
            libraryToUse = fillLibraryValues(parsedCommandLineValues,libraryToUse);
            if (!canLocate(SetupManager.getSetupManager().getLibraryInfoSet(),libraryToUse)) {
                SetupManager.getSetupManager().addLibraryInfo(libraryToUse);
            }
            SetupManager.getSetupManager().setCurrentLibrary(libraryToUse);

        }

        if (libraryToUse==null) {
            libraryToUse =  grabLibrary(parsedCommandLineValues,libraryToUse);
        }

        return libraryToUse;
    }

    private LibraryInfo fillLibraryValues(Map parsedCommandLineValues,LibraryInfo libraryToUse) {
        if (parsedCommandLineValues.containsKey(libraryName.getCommandLineName())) {
            libraryToUse.setProcessServerName((String)parsedCommandLineValues.get(libraryName.getCommandLineName()));
        }

        if (parsedCommandLineValues.containsKey(libraryAddress.getCommandLineName())) {
            libraryToUse.setProcessServerAddress((String)parsedCommandLineValues.get(libraryAddress.getCommandLineName()));
        }

        if (parsedCommandLineValues.containsKey(processServerPort.getCommandLineName())) {
            libraryToUse.setProcessServerPort(Integer.parseInt((String)parsedCommandLineValues.get(processServerPort.getCommandLineName())));
        }
        return libraryToUse;
    }  

    private LibraryInfo fillCMVCLibraryValues(Map parsedCommandLineValues,LibraryInfo libraryToUse) {

        if (parsedCommandLineValues.containsKey(libraryUser.getCommandLineName())) {
            ((CMVCLibraryInfo)libraryToUse).setUsername((String)parsedCommandLineValues.get(libraryUser.getCommandLineName()));
        }

        if (parsedCommandLineValues.containsKey(libraryPasswordAuthentication.getCommandLineName())) {
            ((CMVCLibraryInfo)libraryToUse).setUsingPasswordAuthentication(((String)parsedCommandLineValues.get(libraryPasswordAuthentication.getCommandLineName())).equalsIgnoreCase("YES"));
        }


        if (parsedCommandLineValues.containsKey(cmvcPort.getCommandLineName())) {
            ((CMVCLibraryInfo)libraryToUse).setCMVCPort(Integer.parseInt((String)parsedCommandLineValues.get(cmvcPort.getCommandLineName())));
        }
        return libraryToUse;

    }


    private MBMainframeInfo fillMainframeValues(Map parsedCommandLineValues,MBMainframeInfo mainframeToUse) {
        if (parsedCommandLineValues.containsKey(mainframeAddress.getCommandLineName())) {
            mainframeToUse.setMainframeAddress((String)parsedCommandLineValues.get(mainframeAddress.getCommandLineName()));
        }

        if (parsedCommandLineValues.containsKey(mainframePort.getCommandLineName())) {
            mainframeToUse.setMainframePort((String)parsedCommandLineValues.get(mainframePort.getCommandLineName()));
        }

        if (parsedCommandLineValues.containsKey(mainframeUserid.getCommandLineName())) {
            mainframeToUse.setMainframeUsername((String)parsedCommandLineValues.get(mainframeUserid.getCommandLineName()));
        }

        if (parsedCommandLineValues.containsKey(mainframeAccountInformation.getCommandLineName())) {
            mainframeToUse.setMainframeAccountInfo((String)parsedCommandLineValues.get(mainframeAccountInformation.getCommandLineName()));
        }
        return mainframeToUse;

    }


    private boolean canLocate( Set infosSet,Object locateIt) {
        /* now we iterate through the library set to see if it's there*/

        boolean found = false;
        for (Iterator locator = infosSet.iterator();locator.hasNext();) {
            Object  oneInfo = locator.next();
            if (oneInfo.equals(locateIt)) {
                found = true;
            }
        }

        return found;
    }


    private LibraryInfo grabLibrary(Map parsedCommandLineValues,LibraryInfo libInfo) {
        LibraryInfo libraryToUse = null;
        // ok, they didn't fully specify, so we're going to be using an existing library info object.   Now we find out which 
        // first we see if they picked a new library by name
        if (parsedCommandLineValues.containsKey(selectLibraryName.getCommandLineName())) {

            // they wanted to select a library
            String selectedLibName = (String) parsedCommandLineValues.get(selectLibraryName.getCommandLineName()); 
            String[] selectedLibNameArray = selectedLibName.split("@",2);
            if (selectedLibNameArray!=null && selectedLibNameArray.length >=2) {
                for (Iterator libIterator = SetupManager.getSetupManager().getLibraryInfoSet().iterator();libIterator.hasNext();) {
                    LibraryInfo oneLib = (LibraryInfo) libIterator.next();
                    String oneLibStartsWith = "";
                    if (oneLib.isUsingPasswordAuthentication()) {
                        oneLibStartsWith = oneLib.getAuthenticationKey().substring(0,oneLib.getAuthenticationKey().indexOf("@")) + "@" + oneLib.getProcessServerName();
                    } else {
                        oneLibStartsWith = oneLib.getProcessServerName();
                    }
                    String oneLibAdressPrefix = oneLib.getProcessServerAddress().substring(0,oneLib.getProcessServerAddress().indexOf("."));
                    oneLibStartsWith += "@" + oneLibAdressPrefix;
                    System.out.println("Trying to locate : " + selectedLibName +",  checking .. "+oneLibStartsWith); 
                    if (oneLibStartsWith.startsWith(selectedLibName)) {
                        libraryToUse = oneLib;
                        System.out.println("Found the library shortname  : " + selectedLibName); 
                        SetupManager.getSetupManager().setCurrentLibrary(libraryToUse);
                    }
                }
            }
            if (libraryToUse == null) {
                throw new RuntimeException("Selected library shortname " + selectedLibName + " not found.");
            }
        }
        // finally, we get here.  They didn't select a new library, and they didn't fully specify a library.  So we'll grab the current 
        else {
            libraryToUse = SetupManager.getSetupManager().getCurrentLibraryInfo();
            SetupManager.getSetupManager().setCurrentLibrary(libraryToUse);
        }

        // now we have to handle any partial parameter overrides.  even though they didn't specify a full LibInfo object, they may have done a partial.   
        // Maybe just overrode the port number
        /*
        handle partial overrides
        */
        fillLibraryValues(parsedCommandLineValues,libraryToUse);
        fillCMVCLibraryValues(parsedCommandLineValues,libraryToUse);

        return libraryToUse;
    }



    private MBMainframeInfo doMainframe(Map parsedCommandLineValues) {
        MBMainframeInfo  mainframeToUse =null;
        if (areAllParametersSpecified(parsedCommandLineValues, mainframeValidParameters) ) {
            /* now we iterate through the mainframe set to see if it's there*/
            mainframeToUse = new MBMainframeInfo();
            fillMainframeValues(parsedCommandLineValues,mainframeToUse);
            if (!canLocate(SetupManager.getSetupManager().getMainframeInfoSet(),mainframeToUse)) {
                // make sure we add it
                SetupManager.getSetupManager().addMainframeInfo(mainframeToUse);
            }
            SetupManager.getSetupManager().setCurrentMainframe(mainframeToUse);
        } else {
            // ok, they didn't fully specify, so we're going to be using an existing mainframe info object.   Now we find out which 
            // first we see if they picked a new library by name
            if (parsedCommandLineValues.containsKey(selectMainframeName.getCommandLineName())) {

                // they wanted to select a mainframe
                String selectedMainframeName = (String) parsedCommandLineValues.get(selectMainframeName.getCommandLineName()); 
                String[] selectedMainframeNameArray = selectedMainframeName.split("@",2);

                if (selectedMainframeNameArray!=null && selectedMainframeNameArray.length >=2) {
                    String selectedMainframeUser = selectedMainframeNameArray[0];
                    String selectedMainframeAddressPrefix = selectedMainframeNameArray[1];
                    for (Iterator libIterator = SetupManager.getSetupManager().getMainframeInfoSet().iterator();libIterator.hasNext();) {
                        MBMainframeInfo oneMainframe = (MBMainframeInfo) libIterator.next();
                        String oneMainframeAdressPrefix = oneMainframe.getMainframeAddress().substring(0,oneMainframe.getMainframeAddress().indexOf("."));
                        System.out.println("Trying to locate : " + selectedMainframeName +",  checking .. "+(oneMainframe.getMainframeUsername() +"@"+oneMainframeAdressPrefix)); 
                        if (oneMainframe.getMainframeUsername().equalsIgnoreCase(selectedMainframeUser) && oneMainframeAdressPrefix.startsWith(selectedMainframeAddressPrefix)) {
                            mainframeToUse = oneMainframe;
                            System.out.println("Found the mainframe shortname  : " + selectedMainframeName); 
                            SetupManager.getSetupManager().setCurrentMainframe(mainframeToUse);
                        }
                    }
                }
                if (mainframeToUse == null) {
                    throw new RuntimeException("Selected mainframe shortname " + selectedMainframeName + " not found.");
                }
            }
            // finally, we get here.  They didn't select a new mainframe, and they didn't fully specify a mainframe.  So we'll grab the current 
            else {
                mainframeToUse = SetupManager.getSetupManager().getCurrentMainframeInfo();
            }

            // now we have to handle any partial parameter overrides.  even though they didn't specify a full MainInfo object, they may have done a partial.   
            // Maybe just overrode the port number
            /*
            handle partial overrides
            */
            fillMainframeValues(parsedCommandLineValues,mainframeToUse);

        }
        return mainframeToUse;
    }



    private boolean areAllParametersSpecified(Map parsedCommandLineValues, Set validKeys) {
        return parsedCommandLineValues.keySet().containsAll(validKeys);
    }
}
