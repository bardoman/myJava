#!/bin/ksh
#
#!! Note For Debug Operations change DEBUG=1
#        If no Debug is needed make it DEBUG=0
DEBUG=0
#Lets Allocate fileDescriptor=9 to log the debug information of this script to file
#Note, Care should be taken, so that fileDescriptor=9 aint used anywhere else in this
#script file
#The Release of filedescriptor=9, is done at the bottom of the code
LOG_FILE="installerscript.log"
exec 9>$LOG_FILE
#
#The open/release  of filedescriptor=10, is done  in the createB390Profile function
B390_PROFILE="b390.profile"
#
#The old script Build390USS.ksh which is going away in the next release
OLDB390SCRIPT="Build390USS.ksh"
#
#The new script build390.ksh residing in bin directory
NEWB390SCRIPT="build390.ksh"
#
PARAMETERS_PASSED=''
echo   "============================================"
echo   "Build390  Installer/UnInstaller  for Aix/USS"
echo   "============================================"
#this variable stores the installation path.
INST_PATH=''
#this variable stores the installation path/bin.
BIN_PATH=''
#For USS the below JREPATH should point to a valid JREPATH.
#Suppose if the java executable resides in /usr/lpp/java/J1.3/bin
#then the JREPATH=/usr/lpp/java/J1.3 only.
JREPATH='./jre'
INSTALLER_JRE_DIR="./jre"
RESOURCE_JRE_DIR="./resource/jre"
CURRENT_BUILD390_DIR=''
BACKUP_TEMP_DIR=''
APAR="apar"
PTF="ptf"
BUILD390="Build390"
B390_APAR_SER_FILE_PATH=''
B390_PTF_SER_FILE_PATH=''
#this variable stores -install or -uninstall
INSTALL_UNINSTALL_OPTION=""
#this variable stores if YES, if a Build Conversion is needed. Default is NO
CONVERT_BUILD_OPTION="NO"
#the variable OSNAME stores the operatingsystems name by running command 'uname', if
#it has value 'OS/390' for USS platform and 'AIX' for Aix platform.
OS_NAME=""
USS="OS/390"
AIX="AIX"
LINUX="Linux"
#
#Menu thats is shown for AIX platform
#MENU_AIX_CLIENT="Aix Client         "
MENU_AIX_SERVER_R6="Aix Server R6"
MENU_AIX_SERVER_R5="Aix Server R5"
MENU_AIX_CLIENT="Aix Client"
MENU_AIX_ODE_SERVER="Aix Server(ODE)"
#
#Menu thats is shown for USS platform
MENU_USS_CLIENT="USS Client"
MENU_USS_ODE_CLIENT="USS Client(ODE)"
#
#Menu thats is shown for Linux platform
MENU_LINUX_CLIENT="Linux Client"
#
#Menu thats shown during Install/UnInstall
MENU_INSTALL="Install"
MENU_ROLLBACK="Rollback (to previous version)"
MENU_UNINSTALL="UnInstall"
MENU_QUIT="Quit"
MENU_HELP=''
#
#Menu that gets displayed to enter installation path.
MENU_INSTALLPATH="Install/UnInstall/Rollback Path:"
#
#
QUESTION_CHOOSE_1_2_3_4="Enter 1, 2, 3 or 4:"
#
#
#
#
QUESTION_CHOOSE_1_2_3="Enter 1, 2, or 3:"
#
#
SetDebugSwitchAndCommandLineParameters()
{
integer index=0
for item in $@
do
    if [[ $item = /D ]] ; then
    DEBUG=1
    else
    PARAMETERS_PASSED[$index]=$item
    fi
    index=index+1
done
}
#
#
#
#
TestDebugSwitch()
{
if test $DEBUG -eq 1
    then
    echo "WARNING! << started under debugging mode. >>"
fi
}
#Exit installer code. This is executed - when any exit  is needed
#Additional enhancements are needed, so that this takes a
#RC as a parameter. so the 'exit 0' can be made as 'exit $RC'
ExitInstaller()
{
Logit "ExitInstaller:Entry"
DeleteBackupBuild390Directory
Logit "ExitInstaller: After DeleteBackupBuild390Directory"
#close the fileDescriptor=9 (which handles the logging)
exec 9<&-
echo "Build390   installer/uninstaller     exited."
echo "============================================"
exit 0
}
#
#
#Logs the std outs. into the installer.log file.
Logit()
{
if test $DEBUG -eq 1
    then
        echo "DEBUG:" "$@"
fi
#this basically logs the stuff to file.
print "$@" >&9
}
#
#
#
#For debugging purposes only. 
DumpAllVariables()
{
Logit "DumpAllVariables: Entry"
if test $DEBUG -eq 1
    then
    Logit "============================================"
    Logit "INST_PATH                =>$INST_PATH" 
    Logit "BIN_PATH                 =>$BIN_PATH" 
    Logit "JREPATH                  =>$JREPATH"
    Logit "INSTALLER_JRE_DIR        =>$INSTALLER_JRE_DIR"
    Logit "RESOURCE_JRE_DIR         =>$RESOURCE_JRE_DIR"
    Logit "CURRENT_BUILD390_DIR     =>$CURRENT_BUILD390_DIR"
    Logit "BACKUP_TEMP_DIR          =>$BACKUP_TEMP_DIR"
    Logit "APAR                     =>$APAR"
    Logit "PTF                      =>$PTF"
    Logit "BUILD390                 =>$BUILD390"
    Logit "B390_APAR_SER_FILE_PATH  =>$B390_APAR_SER_FILE_PATH"
    Logit "B390_PTF_SER_FILE_PATH   =>$B390_PTF_SER_FILE_PATH"
    Logit "INSTALL_UNINSTALL_OPTION =>$INSTALL_UNINSTALL_OPTION"
    Logit "USEREXITPATH             =>$HOME/bin"
    Logit "============================================"
    
    read debugreply?"(hit any key to continue or enter 'quit' to exit) "
    Logit "============================================"

    if [[ $debugreply = quit ]] || [[ $debugreply = q ]] ; then
        ExitInstaller
    fi
fi    
Logit "DumpAllVariables: Exit"
}
#
#
#
#For debugging purposes only. 
ChmodForExecutableFiles()
{
Logit "ChmodForExecutableFiles: Entry"
    if [[ $INSTALL_UNINSTALL_OPTION = -install ]] || [[ $INSTALL_UNINSTALL_OPTION = -rollback ]]  ; then
        tempcurrentpath=$PWD
        cd $INST_PATH
        pwd
        find . -name "Build390*.ksh" -exec chmod +x {} \;
        find . -name "build390*.ksh" -exec chmod +x {} \;
        find . -name "java" -exec chmod +x {} \;
        find . -name "Build390.jar" -exec chmod +x {} \;
        find . -name "B390_*.ksh" -exec chmod +x {} \;
        find . -name "b390_*.ksh" -exec chmod +x {} \;
        find . -name "B390_*.pl" -exec chmod +x {} \;
        cd $HOME/bin
        find . -name "B390_*.perl" -exec chmod +x {} \;
        cd $INST_PATH
        find . -name "*Server.pl"   -exec chmod +x {} \;
        find . -name "*Server.perl" -exec chmod +x {} \;
        find . -name "InitPTFMigrateTable.perl" -exec chmod +x {} \;
        find . -name "pullSpecificAPAR.perl" -exec chmod +x {} \;
        find . -name "updateNullVerdateField" -exec chmod +x {} \;
        cd $tempcurrentpath
    fi
Logit "ChmodForExecutableFiles: Exit"
}
#
#
#
#For debugging purposes only. 
ChmodForExecutableFilesInUSS()
{
Logit "ChmodForExecutableFilesInUSS: Entry"
    if [[ $INSTALL_UNINSTALL_OPTION = -install ]] || [[ $INSTALL_UNINSTALL_OPTION = -rollback ]]  ; then
        tempusscurrentpath=$PWD
        cd $INST_PATH
        pwd
        find . -name "b390*.profile" -exec chmod +x {} \;
        find . -name "build390*.ksh" -exec chmod +x {} \;
        find . -name "Build390*.ksh" -exec chmod +x {} \;
        find . -name "Build390.jar" -exec chmod +x {} \;
        cd $tempusscurrentpath
    fi
Logit "ChmodForExecutableFilesInUSS: Exit"
}
#
#
#
#check if the environment variable B390RMIHOME is not null
CheckRMIHomeEnvironmentVariable()
{
Logit "CheckRMIHomeEnvironmentVariable: Entry"
if test "$B390RMIHOME" = ""
   then
   echo "============================================"
   echo "ERROR -  B390RMIHOME  enviroment variable is"
   echo "         missing in .profile file"
   echo "         Please consult the RMI admin guide,"
   echo "         on  setting up the env.  variables."
   ExitInstaller
else
   echo "found the rmi server home path =>  $B390RMIHOME."
fi  
echo   "============================================"
Logit "CheckRMIHomeEnvironmentVariable: Exit"
}
#
#
#
#check if the environment variable B390PRCHOME is not null
CheckPRCHomeEnvironmentVariable()
{
Logit "CheckPRCHomeEnvironmentVariable: Entry"
if test "$B390PRCHOME" = ""
   then
   echo "============================================"
   echo "ERROR -  B390PRCHOME  enviroment variable is"
   echo "         missing in .profile file"
   echo "         Some variable changes were introduced in R6."
   echo "         Please consult the admin guide"
   echo "         on setting up the env. variables."
   ExitInstaller
else
   echo "found the PRC server home path =>  $B390PRCHOME."
fi  
echo   "============================================"
Logit "CheckPRCHomeEnvironmentVariable: Exit"
}
#
#
#

#gets the platform by running command 'uname'.
getPlatform()
{
Logit "getPlatform: Entry"
#uname command returns the operating system
#we are using file descriptor 1(is stdout), file descriptor 2(is error)
uname > out 2> err
StatusU=$?
Logit "getPlatform: RC of uname command =" $StatusU
if test $StatusU -ne 0
then
    exec 3<err
    #read the line from the file. If that is an error, then the OS doesnt
    #support  uname command.
    #read the line into ErrLine variable
    read -u3 ErrLine
    Status=$?
    if test $Status -eq 0
        then
        echo $ErrLine
        Logit "getPlatform: ErrLine:" $ErrLine
        #close the file descriptor 3.
        exec 3<&-
    fi
    
fi
#Open the stdout(out) file in file descriptor 4.
exec 4<out
#read the first line in file descriptor 4 into variable OSNAME
read -u4 OS_NAME
#close the file descriptor opened.
exec 4<&-
rm out
rm err
Logit "getPlatform: Exit"
}
#
#
#
#Read install path.
ReadInstallPath()
{
    Logit "ReadInstallPath: Entry"
       until [ "$INST_PATH" != '' ]
       do
        echo '>> Enter the install/uninstall/rollback path'
        echo "   (hit enter to accept $@ as the default, or" 
        echo "   enter a different install path, or "
        read INST_PATH\?"   (enter 'quit' to exit)?"
        echo   "============================================"
         if [[ $INST_PATH = quit ]] || [[ $INST_PATH = q ]] ; then
           ExitInstaller
         fi
         
         if [ "$INST_PATH" = '' ] 
            then
                INST_PATH=$@
         fi       
         
         if [ "$INST_PATH" != '' ]
            then
                echo "Checking   if   " $INST_PATH
                echo "directory exists ?"
         
                if test -d $INST_PATH
                    then
                       echo "found the " $INST_PATH " directory."
                else
                    echo "WARNING! - directory " $INST_PATH 
                    echo "doesnot exists."
                    echo "============================================"
                    echo ">> Do you  wish  to  create  a new directory" 
                    echo $INST_PATH " ?"
                    echo "(hit 'yes' or 'y' to 'create a new directory"
                    read INSTREPLY\?"or hit enter to <reenter>) ?"
                    echo "============================================"
                    if [[ $INSTREPLY = yes ]] || [[ $INSTREPLY = y ]] ; then
                        mkdir $INST_PATH
                        if test $? -eq 0
                        then
                            echo "$INST_PATH created."
                         else
                            echo "============================================"
                            INST_PATH='' #loopback

                         fi   

                    else
                        INST_PATH='' #loopback
                    fi
                fi          
         fi
       done
       BIN_PATH=$INST_PATH/bin
       CURRENT_BUILD390_DIR=$INST_PATH/Build390
       BACKUP_TEMP_DIR=$INST_PATH/backup_temp
       B390_APAR_SER_FILE_PATH=$INST_PATH/$APAR
       B390_PTF_SER_FILE_PATH=$INST_PATH/$PTF
       #dump all the variables
       DumpAllVariables
       Logit "ReadInstallPath: Exit"
}
#
#
#
#wait and get ok to uninstall.
WaitAndGetOKToUninstall()
{
        Logit "WaitAndGetOKToUninstall"
        echo '>> Do  you   wish  to  start  uninstalling ?'
        echo "(hit 'yes' or  'y'  to 'start  uninstalling'"
        read USERINPUT_TO_UNINSTALL\?"enter' or 'quit' or 'anyother key' to exit)?"
        echo "============================================"
         
         
        if [[ $USERINPUT_TO_UNINSTALL = yes ]] || [[ $USERINPUT_TO_UNINSTALL = y ]] ; then
         # dont do a thing
         echo
        else
            ExitInstaller
        fi
        
}
#
#
#
#wait and get ok to uninstall.
CreateOldBuild390Script()
{
        Logit "CreateOldBuild390Script"
        echo ">> Do you  wish to create $OLDB390SCRIPT ?"
        echo "(hit 'yes' or  'y'  to 'create $OLDB390SCRIPT'"
        read USERINPUT_TO_CREATE\?"enter' or 'quit' or 'anyother key' to exit)?"
        echo "============================================"
         
         
        if [[ $USERINPUT_TO_CREATE = yes ]] || [[ $USERINPUT_TO_CREATE = y ]] ; then
            ln -s  $BIN_PATH/$NEWB390SCRIPT $INST_PATH/$OLDB390SCRIPT
        fi
        
}
#
#
#
#Read 
ReadUSSJREPath()
{
       Logit "ReadUSSJREPath: Entry"
       DEFAULT_USS_JRE_PATH="/usr/lpp/java/J5.0"
       JREPATH=''
       until [ "$JREPATH" != '' ]
       do
       echo "============================================"
       echo "Note: The  build390 installer  needs a valid"
       echo "      java  executable(i.e  java)  to start."                                    
       echo "      Please contact the site  administrator"                                    
       echo "      if you need help in locating the path."                                    
       echo "eg  : $DEFAULT_USS_JRE_PATH/,   assuming    that"
       echo "      the   java    executable   resides  in"
       echo "      $DEFAULT_USS_JRE_PATH/bin      directory."
       echo "============================================"
       echo '>>Enter  the  java  executable  path in  uss'
       echo "(hit enter to accept $DEFAULT_USS_JRE_PATH/" 
       echo "as     the     default),      or"
       echo "(enter a different jre path), or"
       read JREPATH\?"(enter 'quit' to exit)?"
       echo "============================================"
         if [[ $JREPATH = quit ]] || [[ $JREPATH = q ]] ; then
           ExitInstaller
         fi
         
         if [ "$JREPATH" = '' ] 
            then
                JREPATH=$DEFAULT_USS_JRE_PATH/
         fi       

         if [ "$JREPATH" != '' ] 
            then
                if test -a $JREPATH/bin/java
                    then
                        echo "A    valid   java   executable   found    in"
                        echo "$JREPATH/bin"
                    else
                        echo "ERROR - unable  to  locate a java executable"
                        echo "        in $JREPATH/bin directory."
                        echo "        Please     <reenter>    it    again."
                        JREPATH=''
                fi
         fi
       done
       Logit "ReadUSSJREPath: Exit"
}
#
#
#
#performs JRE Check.
JRECheck()
{
Logit "JRECheck: Entry"
if test -d $INSTALLER_JRE_DIR
    then
        echo "found JRE  in installer home directory."
    else
        if test -d $RESOURCE_JRE_DIR
            then
                if test -f $INSTALLER_JRE_DIR
                    then
                        echo "$INSTALLER_JRE_DIR file exists!.removing the file $INSTALLER_JRE_DIR"
                        rm $INSTALLER_JRE_DIR
                 fi        
            echo "copying $RESOURCE_JRE_DIR to  $INSTALLER_JRE_DIR"
            cp -R $RESOURCE_JRE_DIR $INSTALLER_JRE_DIR
         else
            echo "****************************************************************************"
            echo "*** ERROR:  JRE  does NOT  exists in installerhome and resource directory***"
            echo "***         The installer needs a jre to start. There is no JRE that the ***"
            echo "***         installer script can locate.                                 ***"
            echo "***         Either the installer resource is invalid or the installer    ***"
            echo "***         resource was not unzipped properly, or the  installer        ***"
            echo "***         executable  itself is invalid.                               ***"
            echo "***                  Aborting...                                         ***"
            echo "****************************************************************************"
            ExitInstaller
         fi
fi

       echo "============================================"
Logit "JRECheck: Exit"
}
#
#
#
#prompts a query for install/uninstall
AskInstallorUnInstallorRollback()
{
Logit "AskInstallorUnInstallorRollback: Entry"
echo "============================================"
echo "Choose   install,   uninstall  or  rollback?"
echo "============================================"
PS3=$QUESTION_CHOOSE_1_2_3_4
select answer in "$MENU_INSTALL" "$MENU_ROLLBACK" "$MENU_UNINSTALL"  "$MENU_QUIT"
do
        echo "============================================"
        echo "Performing.. option($answer)"
        echo "============================================"
        case "$REPLY" in
        1)
#        echo "Inside case 1"
        INSTALL_UNINSTALL_OPTION="-install"
        break
        ;;
        2)
        INSTALL_UNINSTALL_OPTION="-rollback"
        break
        ;;
        3)
        INSTALL_UNINSTALL_OPTION="-uninstall"
        echo "WARNING! uninstall option completely deletes"
        echo "         the current installation present in"
        echo "        "$INST_PATH " and userexit files"
        echo "         present in " $HOME/bin 
        echo "============================================"
        WaitAndGetOKToUninstall
        break
        ;;
        4)
        ExitInstaller
        #exit 0
        break
        ;;
        *)
        echo "There   is  no   selection    $answer"
        echo "Please  <reenter>   selection  again!"
        ;;
    esac
done
Logit "AskInstallorUnInstallorRollback: Exit"
}
#
#
#
#Tests if the RMI is still running
testForRMIRunning()
{
Logit "testForRMIRunning: Entry"

ps -fu $CMVC_FAMILY | grep cmvcOperationsServer | grep -v grep 


if test $? -eq 0
   then
         echo "**********************************************************************************"
         echo "*** ERROR:  Please shutdown the RMI server before performing this installation.***"
         echo "**********************************************************************************"
               
         ExitInstaller
   else
         
         echo "No RMI server process detected."

fi   

Logit "testForRMIRunning: Exit"
}
#
#
#
testForPRCRunning()
{
Logit "testForPRCRunning: Entry"

ps -fu $CMVC_FAMILY | grep library.ProcessServer | grep -v grep 


if test $? -eq 0
   then
         echo "**************************************************************************************"
         echo "*** ERROR:  Please shutdown the Process server before performing this installation.***"
         echo "**************************************************************************************"
               
         ExitInstaller
   else
         
         echo "No Process server process detected."

fi   

Logit "testForPRCRunning: Exit"
}
#
#
#
#performs stuff related to AixServer installs/unistalls
AixServerR6()
{
Logit "AixServerR6: Entry"
JRECheck
ReadInstallPath $B390PRCHOME
AskInstallorUnInstallorRollback
testForPRCRunning
#ConvertBuildCheck
#read INST_PATH\?"Enter a UnInstall or Install Path:"
echo "Starting $INSTALL_UNINSTALL_OPTION at $INST_PATH"
$JREPATH/bin/java -mx60m -classpath .:B390Installer.jar Build390.Internal.installer.text.SetupText $INSTALL_UNINSTALL_OPTION INSTALLPATH=$INST_PATH PLATFORM=AIX TYPE=SERVER USEREXITPATH=$HOME/bin $PARAMETERS_PASSED
RC_INSTALLER=$?   
Logit "AixServerR6: RC=" $RC_INSTALLER
echo   "============================================"
if test $RC_INSTALLER -eq 0
    then
        ChmodForExecutableFiles
        echo $INSTALL_UNINSTALL_OPTION " at " $INST_PATH " path successful."
    else
        echo $INSTALL_UNINSTALL_OPTION " at " $INST_PATH " path failed."
fi        
Logit "AixServerR6: Exit"
}
#
#
#
#performs stuff related to AixServer ODE installs/unistalls
AixServerR5()
{
Logit "AixServerR5: Entry"
JRECheck
ReadInstallPath $B390RMIHOME
AskInstallorUnInstallorRollback
testForRMIRunning
#ConvertBuildCheck
#read INST_PATH\?"Enter a UnInstall or Install Path:"
echo "Starting $INSTALL_UNINSTALL_OPTION at $INST_PATH"
$JREPATH/bin/java -mx60m -classpath .:B390Installer.jar Build390.Internal.installer.text.SetupText $INSTALL_UNINSTALL_OPTION INSTALLPATH=$INST_PATH PLATFORM=AIX TYPE=SERVER USEREXITPATH=$HOME/bin $PARAMETERS_PASSED
RC_INSTALLER=$?   
Logit "AixServerR5: RC=" $RC_INSTALLER
echo   "============================================"
if test $RC_INSTALLER -eq 0
    then
        ChmodForExecutableFiles
        echo $INSTALL_UNINSTALL_OPTION " at " $INST_PATH " path successful."
    else
        echo $INSTALL_UNINSTALL_OPTION " at " $INST_PATH " path failed."
fi        
Logit "AixServerR5: Exit"
}
#
#
#
#performs stuff related to AixServer ODE installs/unistalls
AixServerODE()
{
Logit "AixServerODE: Entry"
JRECheck
ReadInstallPath $B390RMIHOME
AskInstallorUnInstallorRollback
#ConvertBuildCheck
#read INST_PATH\?"Enter a UnInstall or Install Path:"
echo "Starting $INSTALL_UNINSTALL_OPTION at $INST_PATH"
$JREPATH/bin/java -mx60m -classpath .:B390Installer.jar Build390.Internal.installer.text.SetupText $INSTALL_UNINSTALL_OPTION INSTALLPATH=$INST_PATH PLATFORM=AIXODE TYPE=SERVER USEREXITPATH=$HOME/bin $PARAMETERS_PASSED
RC_INSTALLER=$?   
echo   "============================================"
#if [$? -eq 0]
#    then
#        DeleteBackupSerFileDirectories
#    else
#        echo "Installation Failed.. RC=$?"    
#fi  
Logit "AixServerODE: RC=" $RC_INSTALLER
if test $RC_INSTALLER -eq 0
    then
        ChmodForExecutableFiles
        echo $INSTALL_UNINSTALL_OPTION " at " $INST_PATH " path successful. "
    else
        echo $INSTALL_UNINSTALL_OPTION " at " $INST_PATH " path failed. "
fi        
Logit "AixServerODE: Exit"
}
#
#
#
#performs stuff related to USSClient installs/unistalls
USSClient()
{
Logit "USSClient: Entry"
ReadInstallPath $HOME
#JRECheck We dont need a JRE Check here.
ReadUSSJREPath
AskInstallorUnInstallorRollback
#read INST_PATH\?"Enter a UnInstall or Install Path:"
echo "Starting $INSTALL_UNINSTALL_OPTION at $INST_PATH"
$JREPATH/bin/java -mx60m -classpath .:B390Installer.jar Build390.Internal.installer.text.SetupText $INSTALL_UNINSTALL_OPTION INSTALLPATH=$INST_PATH PLATFORM=USS TYPE=CLIENT $PARAMETERS_PASSED
RC_INSTALLER=$?
echo   "============================================"
Logit "USSClient: RC=" $RC_INSTALLER
if test $RC_INSTALLER -eq 0
    then
        ChmodForExecutableFilesInUSS
        USSOnlyOperations
        echo $INSTALL_UNINSTALL_OPTION " at " $INST_PATH " path successful. "
    else
        echo $INSTALL_UNINSTALL_OPTION " at " $INST_PATH " path failed. "
fi        
Logit "USSClient: Exit"
}
#
#
#
#performs stuff related to USSClient ODE installs/unistalls
USSClientODE()
{
Logit "USSClientODE: Entry"
ReadInstallPath $HOME
#JRECheck We dont need a JRE Check here.
ReadUSSJREPath
Logit "USSClientODE: After JRECheck"
AskInstallorUnInstallorRollback
Logit "USSClientODE: After AskInstallorUnInstallorRollback"
#read INST_PATH\?"Enter a UnInstall or Install Path:"
echo "Starting $INSTALL_UNINSTALL_OPTION at $INST_PATH"
$JREPATH/bin/java -mx60m -classpath .:B390Installer.jar Build390.Internal.installer.text.SetupText $INSTALL_UNINSTALL_OPTION INSTALLPATH=$INST_PATH PLATFORM=USSODE TYPE=CLIENT $PARAMETERS_PASSED
RC_INSTALLER=$?
echo   "============================================"
Logit "USSClientODE: RC=" $RC_INSTALLER
if test $RC_INSTALLER -eq 0
    then
        ChmodForExecutableFilesInUSS
        echo $INSTALL_UNINSTALL_OPTION " at " $INST_PATH " path successful. "
    else
        echo $INSTALL_UNINSTALL_OPTION " at " $INST_PATH " path failed. "
fi        
Logit "USSClientODE: Exit"
}
#
#
#  
#performs stuff related to AIX Client installs/unistalls
AIXClient()
{
Logit "AIXClient: Entry"
ReadInstallPath $HOME
AskInstallorUnInstallorRollback
Logit "AIXClient: After AskInstallorUnInstallorRollback"
#read INST_PATH\?"Enter a UnInstall or Install Path:"
echo "Starting $INSTALL_UNINSTALL_OPTION at $INST_PATH"
$RESOURCE_JRE_DIR/bin/java -mx60m -classpath .:B390Installer.jar Build390.Internal.installer.text.SetupText $INSTALL_UNINSTALL_OPTION INSTALLPATH=$INST_PATH PLATFORM=LINUX TYPE=CLIENT $PARAMETERS_PASSED
RC_INSTALLER=$?
echo   "============================================"
Logit "AIXClient: RC=" $RC_INSTALLER
if test $RC_INSTALLER -eq 0
    then
        ChmodForExecutableFilesInUSS
        echo $INSTALL_UNINSTALL_OPTION " at " $INST_PATH " path successful. "
    else
        echo $INSTALL_UNINSTALL_OPTION " at " $INST_PATH " path failed. "
fi        
Logit "AIXClient: Exit"
}
#
#
#  
#performs stuff related to AIX Client installs/unistalls
LinuxClient()
{
Logit "LinuxClient: Entry"
ReadInstallPath $HOME
AskInstallorUnInstallorRollback
Logit "LinuxClient: After AskInstallorUnInstallorRollback"
#read INST_PATH\?"Enter a UnInstall or Install Path:"
echo "Starting $INSTALL_UNINSTALL_OPTION at $INST_PATH"
$RESOURCE_JRE_DIR/bin/java -mx60m -classpath .:B390Installer.jar Build390.Internal.installer.text.SetupText $INSTALL_UNINSTALL_OPTION INSTALLPATH=$INST_PATH PLATFORM=LINUX TYPE=CLIENT $PARAMETERS_PASSED
RC_INSTALLER=$?
echo   "============================================"
Logit "LinuxClient: RC=" $RC_INSTALLER
if test $RC_INSTALLER -eq 0
    then
        ChmodForExecutableFilesInUSS
        echo $INSTALL_UNINSTALL_OPTION " at " $INST_PATH " path successful. "
    else
        echo $INSTALL_UNINSTALL_OPTION " at " $INST_PATH " path failed. "
fi        
Logit "LinuxClient: Exit"
}

#
#
#  
#performs additional stuff related to USS
USSOnlyOperations()
{
Logit "USSOnlyOperations: Entry"
if test "$INSTALL_UNINSTALL_OPTION" = "-install"
    then
        CreateB390Profile
    if [ -f "$BIN_PATH/$NEWB390SCRIPT" ] ; #this for the symbolic linked build390USS.ksh
    then
        CreateOldBuild390Script
        fi
fi        
Logit "USSOnlyOperations: Exit"
}
#
#
#
#
#Copy Build390 directory to backup directory, and delete it when install is complete
PrintHelp()
{
echo "Platform($OS_NAME)   platform only."
echo "============================================"
echo "Description    : The script setup.ksh should"
echo "                 be run during installation,"
echo "                 uninstallation or  rollback"
echo "                 of Build390 software in" $OS_NAME
echo "Usage          : setup.ksh"
echo "Options($OS_NAME):/D -  to run the script in"
echo "                 script in debug mode."
echo "               : REPLACE=<Y/N> (valid during"
echo "                 install option only)"
echo "Examples       : "
echo "1.setup.ksh /D : starts  the  installer   in"
echo "                 debugging mode."
echo "2.setup.ksh      REPLACE=Y  (during  install"
echo "                 only)"
echo "============================================"
echo "REPLACE=Y  should  be  used if the following"
echo "error occurs only."
echo "Basically this error  occurs when an install"
echo "is   attempted   on   an  already   upgraded"
echo "installation."
echo "<<This not a recommended option.Please refer" 
echo "<<refer RMI Admin guide for more information"
echo "                                            "
echo "General Error  : ERROR -no upgrade necessary"
echo "============================================"
echo "The Build390 installation at /home/ken/test/"
echo "already is upgraded to SDWB5.0.0:0.0"
echo "To replace the existing installation,restart"
echo "the   installer   with   REPLACE=<Y> option."
echo "============================================"
}
#
#
#
#
#Copy Build390 directory to backup directory, and delete it when install is complete
DoBackupOfBuild390Directory()
{
Logit "BackupBuild390Directory: Entry"
#create a backup_temp directory and nuke it when install is complete.
#Also, check if the backup_temp dir, already exists, if so, delete it.
if test -d $CURRENT_BUILD390_DIR
   then
        Logit "Backing up ... $CURRENT_BUILD390_DIR to $BACKUP_TEMP_DIR"
        if test -d $BACKUP_TEMP_DIR
            then
            rm -R $BACKUP_TEMP_DIR
            mkdir $BACKUP_TEMP_DIR
            cp -R $CURRENT_BUILD390_DIR $BACKUP_TEMP_DIR/$BUILD390
        else
            mkdir $BACKUP_TEMP_DIR
            cp -R $CURRENT_BUILD390_DIR $BACKUP_TEMP_DIR/$BUILD390
        fi
    Logit "Backing up complete ... $CURRENT_BUILD390_DIR to $BACKUP_TEMP_DIR"
   else
    echo "" #this is just a dummy line. Aix platform yells if you dont have this.
fi
#check if Build390 directory exists.
Logit "BackupBuild390Directory: Exit"
}
#
#
#
#
#Delete Backup Build390 Directory, if one exists.
DeleteBackupBuild390Directory()
{
Logit "DeleteBackupOfBuild390Directory: Entry"
#create a backup_temp/Build390 directory and nuke it when install is complete.
#Also, check if the backup_temp dir, already exists, if so, delete it.
if test -d $BACKUP_TEMP_DIR/$BUILD390
   then
       rm -R $BACKUP_TEMP_DIR/$BUILD390
        Logit "Cleanup of $BACKUP_TEMP_DIR/$BUILD390 complete"
#   else
#    echo "" #this is just a dummy line. Aix platform yells if you dont have this.
fi
Logit "DeleteBackupOfBuild390Directory: Exit"
}
#
#
#
#
ConvertBuildCheck()
{
Logit "ConvertBuildCheck: Entry"
if test -d $B390APAR_SER_FILE_PATH
    then
        CONVERT_BUILD_OPTION="YES"
        echo "Found $B390APAR_SER_FILE_PATH. Turning ON CONVERT_BUILD OPTION ..."        
        DoBackupOfAparSerFileDirectory
    else
        echo "$B390APAR_SER_FILE_PATH Directory Not Found. ..."        
        if test -d $B390PTF_SER_FILE_PATH
            then
                CONVERT_BUILD_OPTION="YES"
                echo "Found $B390PTF_SER_FILE_PATH. Turing ON CONVERT_BUILD OPTION ..."        
                DoBackupOfPTFSerFileDirectory
            else
            echo "$B390PTF_SER_FILE_PATH Directory Not Found. ..."
        fi
fi
echo "Convert Build Option is => $CONVERT_BUILD_OPTION"     
Logit "ConvertBuildCheck: Exit"
}
#
#
#
#
DoBackupOfAparSerFileDirectory()
{
Logit "DoBackupOfAparSerFileDirectory: Entry"
echo "Backing up ... $B390_APAR_SER_FILE_PATH/$APAR to $BACKUP_TEMP_DIR/$APAR"
        if test -d $BACKUP_TEMP_DIR/$APAR
            then
            rm -R $BACKUP_TEMP_DIR/$APAR
            mkdir $BACKUP_TEMP_DIR/$APAR
            cp -R $B390_APAR_SER_FILE_PATH $BACKUP_TEMP_DIR/$APAR
        else
            mkdir $BACKUP_TEMP_DIR/$APAR
            cp -R $B390_APAR_SER_FILE_PATH $BACKUP_TEMP_DIR/$APAR
        fi
echo "Backing up complete ... $CURRENT_BUILD390_DIR to $BACKUP_TEMP_DIR"
Logit "DoBackupOfAparSerFileDirectory: Exit"
}
#
#
#
#
DoBackupOfPTFSerFileDirectory()
{
Logit "DoBackupOfPTFSerFileDirectory: Entry"
echo "Backing up ... $B390_PTF_SER_FILE_PATH to $BACKUP_TEMP_DIR/$PTF"
        if test -d $BACKUP_TEMP_DIR/$PTF
            then
            rm -R $BACKUP_TEMP_DIR/$PTF
            mkdir $BACKUP_TEMP_DIR/$PTF
            cp -R $B390_PTF_SER_FILE_PATH $BACKUP_TEMP_DIR/$PTF
        else
            mkdir $BACKUP_TEMP_DIR/$PTF
            cp -R $B390_PTF_SER_FILE_PATH $BACKUP_TEMP_DIR/$PTF
        fi
echo "Backing up complete ... $B390_PTF_SER_FILE_PATH to $BACKUP_TEMP_DIR/$PTF"
Logit "DoBackupOfPTFSerFileDirectory: Exit"
}
#
#
#
#
DeleteBackupSerFileDirectories()
{
Logit "DeleteBackupSerFileDirectories: Entry"
if test -d $BACKUP_TEMP_DIR/$APAR
   then
       rm -R $BACKUP_TEMP_DIR/$APAR
echo "Cleanup of $BACKUP_TEMP_DIR/$APAR complete ..."
   else
    echo "" #this is just a dummy line. Aix platform yells if you dont have this.
fi
if test -d $BACKUP_TEMP_DIR/$PTF
   then
       rm -R $BACKUP_TEMP_DIR/$PTF
echo "Cleanup of $BACKUP_TEMP_DIR/$PTF complete ..."
   else
    echo "" #this is just a dummy line. Aix platform yells if you dont have this.
fi
Logit "DeleteBackupSerFileDirectories: Exit"
}
#
#
#
#
CreateB390Profile()
{
Logit "CreateB390Profile: Entry"
if test "$OS_NAME" = "$USS"
then
    if test -d $BIN_PATH
    then
    exec 8>$BIN_PATH/$B390_PROFILE #open filedescriptor=18to write a b390.profile.
    print "B390_JRE_PATH=$JREPATH" >&8
    print "export B390_JRE_PATH" >&8
    exec 8<&-
    fi
fi
Logit "CreateB390Profile: Exit"
}
#
#
#
#
DisplayMenuForLinux()
{
Logit "DisplayMenuForLinux: Entry"
PS3=$QUESTION_CHOOSE_1_2_3
select answer in "$MENU_LINUX_CLIENT" "$MENU_HELP" "$MENU_QUIT"
do
        echo "============================================"
        echo "Performing.. option($answer)"
        echo "============================================"
        case "$REPLY" in
        1)
        Logit "DisplayMenuForLinux: Linux"
        LinuxClient
        break
        ;;
        2)
        Logit "DisplayMenuForLinux: Help"
        PrintHelp
        break
        ;;
        3)
        Logit "DisplayMenuForLinux: Exit"
        ExitInstaller
        break
        ;;
        *)
        echo "There is no selection $answer"
        echo "<reenter> selection again."
        ;;
    esac
done
Logit "DisplayMenuForAix: Exit"
}
#
#
#
#
DisplayMenuForAix()
{
Logit "DisplayMenuForAix: Entry"
PS3=$QUESTION_CHOOSE_1_2_3_4_5
select answer in "$MENU_AIX_SERVER_R6" "$MENU_AIX_SERVER_R5" "$MENU_AIX_ODE_SERVER" "$MENU_HELP" "$MENU_QUIT"
do
        echo "============================================"
        echo "Performing.. option($answer)"
        echo "============================================"
        case "$REPLY" in
        1)
        Logit "DisplayMenuForAix: AixServerR6"
        CheckPRCHomeEnvironmentVariable
        AixServerR6
        break
        ;;
        2)
        Logit "DisplayMenuForAix: AixServerR5"
        CheckRMIHomeEnvironmentVariable
        AixServerR5
        break
        ;;
        3)
        Logit "DisplayMenuForAix: Inside AixServerODE"
        CheckRMIHomeEnvironmentVariable
        AixServerODE
        break
        ;;
        4)
        Logit "DisplayMenuForAix: AIXClient"
        AIXClient
        break
        ;;
        5)
        Logit "DisplayMenuForAix: Help"
        PrintHelp
        break
        ;;
        6)
        Logit "DisplayMenuForAix: Exit"
        ExitInstaller
        break
        ;;
        *)
        echo "There is no selection $answer"
        echo "<reenter> selection again."
        ;;
    esac
done
Logit "DisplayMenuForAix: Exit"
}
#
#
#
#The menu for USS is displayed
DisplayMenuForUSS()
{
Logit "DisplayMenuForUSS: Entry"
PS3=$QUESTION_CHOOSE_1_2_3_4
select answer in "$MENU_USS_CLIENT" "$MENU_USS_ODE_CLIENT" "$MENU_HELP" "$MENU_QUIT"
do
        echo "============================================"
        echo "Performing.. option($answer)"
        echo "============================================"
        case "$REPLY" in
        1)
        Logit "DisplayMenuForUSS: Inside case 1(USSClient)"
        USSClient
        break
        ;;
        2)
        Logit "DisplayMenuForUSS: Inside case 2(USSClientODE)"
        USSClientODE
        break
        ;;
        3)
        Logit "DisplayMenuForUSS: Help"
        PrintHelp
        break
        ;;
        4)
        Logit "DisplayMenuForUSS: Exit"
        ExitInstaller
        ;;
        *)
        echo "There is no selection $answer."
        echo "<reenter> selection again."
        ;;
    esac
done
Logit "DisplayMenuForUSS: Entry"
}
#
#
#
#<< this the main execution starting point.. the intial functions are just loaded >>
SetDebugSwitchAndCommandLineParameters $@
Logit "Main: The parameters passed =>" $PARAMETERS_PASSED
TestDebugSwitch
getPlatform
Logit "Main: The script is running in platform =>" $OS_NAME
MENU_HELP="Help("$OS_NAME")"
if test "$OS_NAME" = "$USS"
    then
        DisplayMenuForUSS
    else
        if  test "$OS_NAME" = "$AIX"
            then
                DisplayMenuForAix
        else
            if  test "$OS_NAME" = "$LINUX"
            then
                DisplayMenuForLinux
            else
                echo "============================================"
                echo "ERROR - Platform (" $OS_NAME ") NOT supported"
                echo "by Build390 installer/uninstaller"
                echo "============================================"
           fi
        fi
fi
ExitInstaller
