#!/bin/ksh 
#####################################################################
#                                                                   #
#  B390_rmi_debug.ksh  -  RMI Server install debug script           #
#                                                                   #
#                         This script is run from the CMVC          #
#                         family id to test the environment         #
#                         for B390 RMI server prereqs, parm         #
#                         settings, and possible install errors     #
#                                                                   #
#  Name             Date                                            #
#  ---------------- --------   -------------------                  #
#  S. Miller        08/29/02   Build/390 5.0 new exec               #
#  S. Miller        10/10/02   Added table dumps                    #
#                                                                   #
#####################################################################
#
typeset -l reply
let warnings=0
clear
echo "\n=============================================================="
echo "Build/390 RMI Server installation debug script"
echo "==============================================================\n"
echo "This routine will assist administrators in checking their RMI "
echo "server environment, help verify installation setup, and assist"
echo "in identifying common RMI Server problems."
echo "\n\nNOTE: This script must be run under the CMVC Family id!\n"
echo "\n\nTo end this script at any time, enter 'quit' at any prompt."

read reply?"(hit any key to continue or enter 'quit' to exit) "

if [[ $reply = quit ]] || [[ $reply = q ]] ; then
    echo "\n\n....exiting RMI Server debug script.\n";
    exit 1
fi

#######################
#  prompt for version #
#######################
clear
echo "\n=============================================================="
echo 'Enter the version of Build390 you are installing/debugging:'
echo "=============================================================="
done=false
while [[ $done = false ]]; do
    done=true
    {
        print '1) 2.4.1'
        print '2) 4.0'
        print '3) 5.0'
    } >&2
    read REPLY?'? '
    
    case $REPLY in
        1 ) release=2.4.1 ;;
        2 ) release=4.0 ;;
        3 ) release=5.0 ;;
        * ) print 'invalid - enter 1,2, or 3'
                    done=false ;;
    esac                    
done
########################
# prompt for dump file #
########################

clear
echo "================================================================="
echo 'Do you want to generate a current parameters/settings dump file'
echo 'to submit to Build390 support?\n'

read reply?"(y/n) "
if [[ $reply = y ]]
then
    dump="yes"
    echo "\nInitializing dump file $HOME/B390_rmi_debug.dump"
    echo "\n================================================================\n" > $HOME/B390_rmi_debug.dump
    echo "B390_rmi_debug.ksh dump run on $(date)." >> $HOME/B390_rmi_debug.dump
    echo "\n================================================================\n" >> $HOME/B390_rmi_debug.dump
    echo "B390 release specified by user is $release." >> $HOME/B390_rmi_debug.dump
    echo "\n================================================================\n" >> $HOME/B390_rmi_debug.dump
    echo "Running Build390RMIVersion.ksh yields:\n" >> $HOME/B390_rmi_debug.dump
    if [[ $release = 2.4.1 ]]
    then
        cd $HOME/bin
        . Build390RMIVersion.ksh >> $HOME/B390_rmi_debug.dump
    else
        cd $B390RMIHOME
        . Build390RMIVersion.ksh >> $HOME/B390_rmi_debug.dump
    fi         
    echo "\n================================================================\n" >> $HOME/B390_rmi_debug.dump
    echo "Build390 parm settings in $CMVC_FAMILY .profile:\n" >> $HOME/B390_rmi_debug.dump
    cat $HOME/.profile | grep 'export B390' | grep -v "^#"  >> $HOME/B390_rmi_debug.dump
    echo "\n================================================================\n" >> $HOME/B390_rmi_debug.dump
    echo "CMVC parm settings in $CMVC_FAMILY .profile:\n" >> $HOME/B390_rmi_debug.dump
    cat $HOME/.profile | grep 'export CMVC_' | grep -v "^#"  >> $HOME/B390_rmi_debug.dump
    echo "\n================================================================\n" >> $HOME/B390_rmi_debug.dump
    echo "parms DB2_HOME, DB2_DBPATH, DB2INSTANCE, DB2DBDFT from .profile:\n" >> $HOME/B390_rmi_debug.dump
    cat $HOME/.profile | grep 'export DB2_HOME' | grep -v "^#"  >> $HOME/B390_rmi_debug.dump
    cat $HOME/.profile | grep 'export DB2_DBPATH' | grep -v "^#"  >> $HOME/B390_rmi_debug.dump
    cat $HOME/.profile | grep 'export DB2INSTANCE' | grep -v "^#"  >> $HOME/B390_rmi_debug.dump
    cat $HOME/.profile | grep 'export DB2DBDFT' | grep -v "^#"  >> $HOME/B390_rmi_debug.dump
    echo "\n================================================================\n" >> $HOME/B390_rmi_debug.dump
    echo "CMVC DB2 database directory listing:\n"  >> $HOME/B390_rmi_debug.dump
    db2 list database directory >> $HOME/B390_rmi_debug.dump
    echo "\n================================================================\n" >> $HOME/B390_rmi_debug.dump
    echo "CMVC DB2 database node listing:\n"  >> $HOME/B390_rmi_debug.dump
    db2 list node directory >> $HOME/B390_rmi_debug.dump 
    echo "\n================================================================\n" >> $HOME/B390_rmi_debug.dump
    echo "Results from issuing java -fullversion:\n" >> $HOME/B390_rmi_debug.dump
    java -fullversion 2>> $HOME/B390_rmi_debug.dump 
    echo "\n================================================================\n" >> $HOME/B390_rmi_debug.dump
    echo "Results from issuing Report -testServer:\n" >> $HOME/B390_rmi_debug.dump
    Report -testServer >> $HOME/B390_rmi_debug.dump 2>> $HOME/B390_rmi_debug.dump
    echo "\n================================================================\n" >> $HOME/B390_rmi_debug.dump
    echo "Current contents of CMVC userExits file:\n" >> $HOME/B390_rmi_debug.dump
    cat $HOME/config/userExits | grep -v "^#" >> $HOME/B390_rmi_debug.dump
    echo "\n================================================================\n" >> $HOME/B390_rmi_debug.dump
    shortfam=${CMVC_FAMILY%%@*}
    echo "Current Build390 Config setup from BUILD390CONFIG table:\n" >> $HOME/B390_rmi_debug.dump
    db2 "select * from $shortfam.BUILD390CONFIG " >> $HOME/B390_rmi_debug.dump
    echo "\n================================================================\n" >> $HOME/B390_rmi_debug.dump
    echo "Current Build390 Metadata setup from BUILD390META table:\n" >> $HOME/B390_rmi_debug.dump
    db2 "select * from $shortfam.BUILD390METADATA " >> $HOME/B390_rmi_debug.dump
    echo "\n================================================================\n" >> $HOME/B390_rmi_debug.dump                                            
else
    echo "\nA dump file will not be generated at this time.\n"
fi
    
##############################
# check CMVC_FAMIILY is set  #
##############################
echo "========================================================================="
if [ -z "$CMVC_FAMILY" ]
then
 echo "The CMVC family name must be set with the CMVC_FAMILY environment variable in the family .profile"
 if [[ $dump = yes ]]
 then
    echo "\n================================================================\n"      >> $HOME/B390_rmi_debug.dump
    echo "ERROR - The CMVC family name must be set with the CMVC_FAMILY environmental variable in the family .profile" >> $HOME/B390_rmi_debug.dump
 fi   
 exit 1
fi

######################################################
#  TEST - is DB2_PASS exported                       #
######################################################

if [ -z "$DB2_PASS" ]
then
 echo "The CMVC environmental variable DB2_PASS must be set to the current family"
 echo "password in the family .profile.  Currently this variable is not set."
 echo "\nThe Build390 RMI server uses the DB2_PASS value to connect to the"
 echo "CMVC DB2 database via a database alias."
 echo "\nPlease update .profile and refresh prior to rerunning this script."
 echo "\n......exiting"
 exit 1
fi
cd
pwdset=$(cat $HOME/.profile | grep DB2_PASS= | grep -v "^#" | cut -d' ' -f2)
echo "\nThe current password exported in the CMVC family .profile is:"
echo "           ====>  $pwdset."
echo "Ensure this is the current local password for the family id,"
echo "or the Build390 RMI server will be unable to connect to the"
echo "CMVC DB2 database.\n"

read reply?"(hit any key to continue or enter 'quit' to exit) "

if [[ $reply = quit ]] || [[ $reply = q ]] ; then
    echo "\n\n....exiting RMI Server debug script.\n";
    exit 1
fi 
clear

#####################################################
# TEST - if 2.4.1 check for family shortname format #
#####################################################

if [[ $release = 2.4.1 ]] ; then
    if [[ $CMVC_FAMILY = *[@]* ]]
    then
        echo "For release $release the CMVC_FAMILY environmental variable must be exported "
        echo "using the shortname format (ie: family) and not the longname format (ie: family@server@port)."
        echo "\nCurrent CMVC_FAMILY setting is $CMVC_FAMILY\n"
        echo "Update the CMVC_FAMILY parm in the family .profile and refresh before rerunning this script.\n"
        exit 1
    else
        echo "Beginning check of CMVC family $CMVC_FAMILY for Build390 release $release\n"
        echo "========================================================================="
        echo "Current CMVC_FAMILY setting $CMVC_FAMILY is using shortname format"
        echo "                                                              .....PASSED"
        echo "========================================================================="
        if [[ $dump = yes ]]
        then
            echo "\n================================================================\n"      >> $HOME/B390_rmi_debug.dump
            echo "Current CMVC_FAMILY setting $CMVC_FAMILY is using shortname format"        >> $HOME/B390_rmi_debug.dump
            echo "                                                              .....PASSED" >> $HOME/B390_rmi_debug.dump
        fi
    fi
else
    echo "Beginning check of CMVC family $CMVC_FAMILY for Build390 release $release\n"
    echo "========================================================================="
fi

####################################
# TEST - check LANG value for perl #
####################################

if [[ $LANG = en_US ]]; then
    echo "LANG environmental variable set to $LANG for perl."
    echo "                                                              .....PASSED"
    if [[ $dump = yes ]]
    then
       echo "\n================================================================\n"      >> $HOME/B390_rmi_debug.dump
       echo "LANG environmental variable set to $LANG for perl."                        >> $HOME/B390_rmi_debug.dump
       echo "                                                              .....PASSED" >> $HOME/B390_rmi_debug.dump
    fi
else
    echo "\nLANG environmental variable is currently set to $LANG."
    echo "Build390 perl scripts and user exits will receive error messages"
    echo "related to invalid locale setting unless this value is exported "
    echo "as 'en_US'.  Please update your .profile accordingly.\n\n"
    if [[ $dump = yes ]]
    then
        echo "\n================================================================\n"      >> $HOME/B390_rmi_debug.dump
        echo "ERROR - The LANG environmental variable is set to $LANG instead of en_US"  >> $HOME/B390_rmi_debug.dump
    fi  
    exit 1
fi

##############################################################
#  TEST - can we connect to the db2 db using the current pwd #
##############################################################

echo "========================================================================="        
echo "Attempting connection to the CMVC db2 database..."
shortfam=${CMVC_FAMILY%%@*}
echo "Issuing db2 connect to $shortfam user $shortfam using $DB2_PASS \n"
db2 connect to $shortfam user $shortfam using $DB2_PASS > debug_tempfile

results=$(cat debug_tempfile | grep -F 'Database Connection Information')
if [[ -z "$results" ]]
then
    rest=$(cat debug_tempfile | head -1)
    echo "ERROR - $rest\n"
    echo "Depending on the error received, ensure:"
    echo "   1) The CMVC family id is setup to use local password support"
    echo "      (not Kerberos, DCE, or AFS)."
    echo "   2) $DB2_PASS is the current family password."
    echo "   3) If \$DB2_PASS was just updated, ensure you refreshed .profile before rerunning this script.\n"
    if [[ $dump = yes ]]
    then
        echo "\n================================================================\n"      >> $HOME/B390_rmi_debug.dump
        echo "ERROR - Could not connect to the CMVC database using db2 connect to $shortfam user $shortfam using $DB2_PASS" >> $HOME/B390_rmi_debug.dump
    fi
    exit 2
    

else
    results=$(cat debug_tempfile)
    echo "$results"
    echo "\nCMVC db2 database connection was successful."
    echo "                                                              .....PASSED"
    if [[ $dump = yes ]]
    then
       echo "\n================================================================\n"      >> $HOME/B390_rmi_debug.dump
       echo "CMVC db2 database connection was successful."                              >> $HOME/B390_rmi_debug.dump
       echo "                                                              .....PASSED" >> $HOME/B390_rmi_debug.dump
    fi
fi
rm debug_tempfile
####################################################
# TEST - is cmvc family name in /etc/hosts         #
####################################################

echo "========================================================================="
# frame family name with spaces so family name will not be grepped in the middle of a string 
shortfampad=' '$shortfam' '
results=$(cat /etc/hosts | grep -F "$shortfampad" | grep -v "^#" )
if [[ -z "$results" ]]
then
    echo "Error - CMVC family name $shortfam does not appear to be"
    echo "included in the /etc/hosts file as an alias to the servername."
    echo "Update /etc/hosts accordingly; see CMVC install manual for more details."
    if [[ $dump = yes ]]
    then
        echo "\n================================================================\n"      >> $HOME/B390_rmi_debug.dump
        echo "ERROR - THE CMVC family name $shortfam does not appear to be listed in /etc/hosts." >> $HOME/B390_rmi_debug.dump
        echo "Contents of /etc/hosts:\n"  >> $HOME/B390_rmi_debug.dump
        cat /etc/hosts >> $HOME/B390_rmi_debug.dump
    fi
    exit 9
else
    echo "CMVC family name $shortfam is listed in /etc/hosts."
    echo "                                                              .....PASSED"
    if [[ $dump = yes ]]
    then
       echo "\n================================================================\n"      >> $HOME/B390_rmi_debug.dump
       echo "CMVC family name $shortfam is listed in /etc/hosts."                       >> $HOME/B390_rmi_debug.dump
       echo "                                                              .....PASSED" >> $HOME/B390_rmi_debug.dump
    fi
fi
        
####################################################
# TEST - is cmvc aix command line client available #
####################################################

clientcmd=$(whence Host)
if [[ -a $clientcmd ]];
then
 echo "========================================================================="
 echo "The 'Host' command was found at $clientcmd ."  
 echo "The CMVC AIX Command Line client appears to be installed."
 echo "                                                              .....PASSED"
 if [[ $dump = yes ]]
 then
     echo "\n================================================================\n"      >> $HOME/B390_rmi_debug.dump
     echo "The CMVC AIX Command Line client appears to be installed."                 >> $HOME/B390_rmi_debug.dump
     echo "                                                              .....PASSED" >> $HOME/B390_rmi_debug.dump
 fi
else 
 echo "The CMVC AIX command line client does not appear to be installed or is not in the current path."
 echo "Issuing 'whence Host' failed to find the Host command."
 echo "Ensure the client is installed and it's path included in the .profile PATH parm.\n\n"  
 if [[ $dump = yes ]]
    then
        echo "\n================================================================\n"      >> $HOME/B390_rmi_debug.dump
        echo "ERROR - The CMVC AIX command line client Host exec was not found in the current path: $PATH" >> $HOME/B390_rmi_debug.dump
    fi
 exit 1
fi

####################################################
# TEST - are there a minimum of 3 daemons running  #
####################################################

clientcmd=$(monitor 0 | head -1 | cut -d ' ' -f3 )
clientcmd2=$(monitor 0 | head -1 | cut -d ' ' -f2 )

if [[ $clientcmd = "CMVC" ]]
then
    echo "========================================================================="
    echo "CMVC daemons do not appear to be running right now.  Unable to determine"
    echo "how many daemons normally run for $CMVC_FAMILY.  Ensure a minimum of 3"
    echo "daemons are setup or Build390 transactions could cause daemon lockup."
elif [[ $clientcmd2 != "of" ]]
then
    echo "========================================================================="
    echo " WARNING  WARNING   WARNING   WARNING   WARNING   WARNING   WARNING      "
    echo "Running the monitor cmd from current id did not yield expected results."
    echo "This script should be run by the cmvc family id and the monitor command"
    echo "should be in the current path."
    echo "Check to ensure the cmvc family is running a minimum of 3 daemons"
    echo "after this script completes."
    let warnings=warnings+1 
elif (( $clientcmd < 3 ));
then
    echo "========================================================================="
    echo "There are insufficient CMVC daemons running for $CMVC_FAMILY to support B390."  
    echo "Currently only $clientcmd daemons are running.  This needs to be increased to"
    echo "an absolute minimum of 3 to prevent daemon lockups.  Running with a minimum"
    echo "of 5 daemons is prefered on even the smallest families; futher details on"
    echo "selecting an appropriate number of daemons can be found in the B390 RMI Install Guide."
    if [[ $dump = yes ]]
    then
        echo "\n================================================================\n"        >> $HOME/B390_rmi_debug.dump
        echo "\nERROR - only $clientcmd CMVC daemons are running - absolute minimum is 3." >> $HOME/B390_rmi_debug.dump
    fi
exit 2
else
    echo "========================================================================="
    echo "$CMVC_FAMILY is setup to run $clientcmd CMVC daemons (minimum is 3)."
    echo "                                                              .....PASSED"
    if [[ $dump = yes ]]
    then
        echo "\n================================================================\n"      >> $HOME/B390_rmi_debug.dump
        echo "CMVC is setup to run $clientcmd CMVC daemons (minimum is 3)."              >> $HOME/B390_rmi_debug.dump
        echo "                                                              .....PASSED" >> $HOME/B390_rmi_debug.dump
    fi
fi

####################################################
# TEST - is inService configurable field defined   #
####################################################

clientcmd=$(Report -general ReleaseView -select name -where "inService in ('bogus')")
if [[ -a $clientcmd ]]
then
    echo "========================================================================="
    echo "\nTesting for CMVC release configurable field inService failed.  Received following error message:\n"    
    echo $clientcmd
    errmsg=${clientcmd##0010-061}
    if [[ $errmsg = "0010-061" ]]
    then
        echo "\n0010-061 message was received; the BPS inService configurable field for the"
        echo "CMVC release object is not defined.\n"
        echo "Even if BPS is not installed on your system, this field must be defined or B390"
        echo "user exits will receive false errors."
        if [[ $dump = yes ]]
        then
            echo "\n================================================================\n"          >> $HOME/B390_rmi_debug.dump
            echo "\nERROR - The BPS inService configurable field does not appear to be defined." >> $HOME/B390_rmi_debug.dump
        fi
        exit 20
    else
        echo "\nERROR - Ensure CMVC is up and the CMVC family id has the necessary permissions to"
        echo "utilize the CMVC command line client.\n"
        if [[ $dump = yes ]]
        then
            echo "\n================================================================\n"            >> $HOME/B390_rmi_debug.dump
            echo "\nERROR - received $errmsg attempting to test for inService configurable field." >> $HOME/B390_rmi_debug.dump
        fi
        exit 21
    fi
else
    echo "========================================================================="
    echo "CMVC release configurable field inService for BPS is defined."
    echo "                                                              .....PASSED"
    if [[ $dump = yes ]]
    then
        echo "\n================================================================\n"      >> $HOME/B390_rmi_debug.dump
        echo "CMVC release configurable field inService for BPS is defined."             >> $HOME/B390_rmi_debug.dump
        echo "                                                              .....PASSED" >> $HOME/B390_rmi_debug.dump
    fi
fi


####################################################
# TEST -  check for B390 parm exports in .profile  #
####################################################

echo "========================================================================="

if [[ $release = 2.4.1 ]] ; then
    
    B390_SERVICEPORT=$(cat $HOME/.profile | grep B390SERVICEPORT | grep -v "^#" | cut -d' ' -f2)
    B390_BPSHOST=$(cat $HOME/.profile | grep B390BPSHOST | grep -v "^#" | cut -d' ' -f2)
    B390_BPSPORT=$(cat $HOME/.profile | grep B390BPSPORT | grep -v "^#" | cut -d' ' -f2)
    
    parmerr=n
    
    if [ -z "$B390_SERVICEPORT" ]
    then
        echo "ERROR - The B390 environmental variable B390SERVICEPORT does not appear"
        echo "        to be exported in the CMVC family .profile.\n"
        parmerr=y
    fi
    
    if [ -z "$B390_BPSHOST" ]
    then
        echo "ERROR - The B390 environmental variable B390BPSHOST does not appear"
        echo "        to be exported in the CMVC family .profile.\n"
        echo "Note: Even if BPS is not currently installed, a default of LOCALHOST must"
        echo "be defined.  Otherwise, this value should be the hostname of your BPS server.\n"
        parmerr=y
    fi
    
    if [ -z "$B390_BPSPORT" ]
    then
        echo "ERROR - The B390 environmental variable B390BPSPORT does not appear"
        echo "        to be exported in the CMVC family .profile.\n"
        echo "Note: Even if BPS is not currently installed, a default of 0 must be"
        echo "defined.  Otherwise, this value should be the port of your BPS server.\n"
        parmerr=y        
    fi

    if [[ $parmerr = y ]]
    then
        echo "\nEnsure B390SERVICEPORT, B390BPSHOST, and B390BPSPORT are exported per RMI Guide"
        echo "installation instructions."
        echo "\n\nExiting......"
        exit 3
    fi
    
    echo "All B390 parms are being exported in .profile."
    echo "                                                              .....PASSED\n\n"
    echo "Exported settings are:" 
    echo "    $B390_SERVICEPORT"
    echo "    $B390_BPSHOST"
    echo "    $B390_BPSPORT \n"
    if [[ $dump = yes ]]
    then
        echo "\n================================================================\n"      >> $HOME/B390_rmi_debug.dump
        echo "All B390 parms are being exported in .profile."                            >> $HOME/B390_rmi_debug.dump
        echo "                                                              .....PASSED" >> $HOME/B390_rmi_debug.dump
    fi
    
else

    B390_SERVICEPORT=$(cat $HOME/.profile | grep B390SERVICEPORT | grep -v "^#" | cut -d' ' -f2)
    B390_BPSHOST=$(cat $HOME/.profile | grep B390BPSHOST | grep -v "^#" | cut -d' ' -f2)
    B390_BPSPORT=$(cat $HOME/.profile | grep B390BPSPORT | grep -v "^#" | cut -d' ' -f2)
    B390_CLASSPATH=$(cat $HOME/.profile | grep B390CLASSPATH | grep -v "^#" | cut -d' ' -f2) 
    B390_LIBPATH=$(cat $HOME/.profile | grep B390LIBPATH | grep -v "^#" | cut -d' ' -f2) 
    B390_RMIHOME=$(cat $HOME/.profile | grep B390RMIHOME | grep -v "^#" | cut -d' ' -f2) 
    B390_RMISERVERPORT=$(cat $HOME/.profile | grep B390RMISERVERPORT | grep -v "^#" | cut -d' ' -f2)
    
    parmerr=n
     
    if [ -z "$B390_SERVICEPORT" ]
    then
        echo "ERROR - The B390 environmental variable B390SERVICEPORT does not appear"
        echo "        to be exported in the CMVC family .profile.\n"
        parmerr=y
    fi
    
    if [ -z "$B390_BPSHOST" ]
    then
        echo "ERROR - The B390 environmental variable B390BPSHOST does not appear"
        echo "        to be exported in the CMVC family .profile.\n"
        parmerr=y
    fi
    
    if [ -z "$B390_BPSPORT" ]
    then
        echo "ERROR - The B390 environmental variable B390BPSPORT does not appear"
        echo "        to be exported in the CMVC family .profile.\n"
        parmerr=y
    fi

    if [ -z "$B390_CLASSPATH" ]
    then
        echo "ERROR - The B390 environmental variable B390CLASSPATH does not appear"
        echo "        to be exported in the CMVC family .profile.\n"
        parmerr=y
    fi
    
    if [ -z "$B390_LIBPATH" ]
    then
        echo "ERROR - The B390 environmental variable B390LIBPATH does not appear"
        echo "        to be exported in the CMVC family .profile.\n"
        parmerr=y
    fi

    if [ -z "$B390_RMIHOME" ]
    then
        echo "ERROR - The B390 environmental variable B390RMIHONME does not appear"
        echo "        to be exported in the CMVC family .profile.\n"
        parmerr=y
    fi

    if [ -z "$B390_RMISERVERPORT" ]
    then
        echo "ERROR - The B390 environmental variable B390RMISERVERPORT does not appear"
        echo "        to be exported in the CMVC family .profile.\n"
        parmerr=y
    fi
    
    if [[ $parmerr = y ]]
    then
        echo "\nEnsure all Build390 parameters are exported per RMI Guide installation instructions.\n"
        echo "Note: If you are migrating from Build390 2.4.1 to a later release, be aware that"
        echo "several additional B390 parameters must be added to .profile.\n"
        echo ".........exiting."
        exit 3
    fi     

    echo "B390 parms are being exported in .profile."
    echo "                                                              .....PASSED\n\n"
    echo "Exported settings are:" 
    echo "    $B390_SERVICEPORT"
    echo "    $B390_BPSHOST"
    echo "    $B390_BPSPORT"
    echo "    $B390_CLASSPATH"
    echo "    $B390_LIBPATH"
    echo "    $B390_RMIHOME"
    echo "    $B390_RMISERVERPORT \n"
    if [[ $dump = yes ]]
    then
        echo "\n================================================================\n"      >> $HOME/B390_rmi_debug.dump
        echo "B390 parms are being exported in .profile."                                >> $HOME/B390_rmi_debug.dump
        echo "                                                              .....PASSED" >> $HOME/B390_rmi_debug.dump
    fi
fi

#################################################################
#  TEST - check that B390 user exits are in bin directory       #
#################################################################
 
echo "========================================================================="
if [[ -a $HOME/bin/B390_FixComplete1.perl ]]
then
    echo "Build390 exits appear to be installed in the family bin subdirectory."
    echo "                                                              .....PASSED"
    if [[ $dump = yes ]]
    then
        echo "\n================================================================\n"      >> $HOME/B390_rmi_debug.dump
        echo "Build390 exits appear to be installed in the family bin subdirectory."     >> $HOME/B390_rmi_debug.dump
        echo "                                                              .....PASSED" >> $HOME/B390_rmi_debug.dump
    fi

else
    echo "Build390 exit B390_FixComplete1.perl was not found in the CMVC family bin subdirectory."
    echo "If the Build390 tar package was unpacked into a different directory,"
    echo "be sure all Build390 user exits are moved over to the bin subdirectory as noted in the RMI install guide."
    echo "\n\n.....exiting RMI Server debug script.\n"
    if [[ $dump = yes ]]
    then
        echo "\n================================================================\n"      >> $HOME/B390_rmi_debug.dump
        echo "ERROR - B390 user exits do not appear to be in bin subdirectory.  Current bin contents:\n" >> $HOME/B390_rmi_debug.dump
        ls -al $HOME/bin >> $HOME/B390_rmi_debug.dump
    fi
    exit 31
fi         

#################################################################
#  TEST - check that userExits file includes B390 exits         #
#################################################################
echo "========================================================================="
clientcmd=$(cat $HOME/config/userExits | grep B390_FixComplete1.perl | grep -v "^#" | cut -d' ' -f1)
if [[ $clientcmd = "FixComplete" ]]
then
    echo "Build390 exits appear to be included in the userExits config file."
    echo "                                                              .....PASSED"
    if [[ $dump = yes ]]
    then
        echo "\n================================================================\n"      >> $HOME/B390_rmi_debug.dump
        echo "Build390 exits appear to be included in the userExits config file."        >> $HOME/B390_rmi_debug.dump
        echo "                                                              .....PASSED" >> $HOME/B390_rmi_debug.dump
    fi
elif [[ $clientcmd = "#" ]]
then
    echo "\nBuild390 exit B390_FixComplete1.perl appears to be commented out in the CMVC userExits configuration file."
    echo "Please compare the file userExits.b390 in the Build390 install directory against $HOME/config/userExits"
    echo "to ensure all Build390 exits are both included and active."
    exit 32
else
    echo "\nBuild390 exit B390_FixComplete1.perl does not appear to be included in the CMVC userExits configuration file."
    echo "Please ensure exit entries from the file userExits.b390 - found in the Build390 install directory - have been"
    echo "added to the $HOME/config/userExits configuration file.  See the RMI installation guide for further details."
    echo "\n\n......exiting RMI Server debug script.\n"
    exit 33
fi              

##############################################################
#   TEST - check if 2.4.1 parms are valid in .profile        #
##############################################################

if [[ $release = 2.4.1 ]] ; then
    
    #  test if db2java.zip is in CLASSPATH
    echo "=========================================================================" 
    clientcmd=$(echo $CLASSPATH | grep db2java.zip)
    if [[ -z $clientcmd ]];
    then
        echo "File db2java.zip does not appear to be included in the CMVC family CLASSPATH $CLASSPATH.\n\n"
        echo "Ensure that:"
        echo "    1) The path to db2java.zip is included in the CMVC CLASSPATH exported in .profile."
        echo "    2) The JDBC component of DB2 is installed on your system and the db2java.zip file exists in db2 product java subdirectory."
        echo "    3) The db2java.zip file exists in the family sqllib/java directory."
        if [[ $dump = yes ]]
        then
            echo "\n================================================================\n"  >> $HOME/B390_rmi_debug.dump
            echo "ERROR - db2java.zip was not found in $CLASSPATH"                       >> $HOME/B390_rmi_debug.dump
        fi
        exit 12
    else    
        echo "File db2java.zip exists in the CMVC family id classpath."
        echo "                                                              .....PASSED"
        if [[ $dump = yes ]]
        then
            echo "\n================================================================\n"      >> $HOME/B390_rmi_debug.dump
            echo "File db2java.zip exists in the CMVC family id classpath."                  >> $HOME/B390_rmi_debug.dump
            echo "                                                              .....PASSED" >> $HOME/B390_rmi_debug.dump
        fi
    
    fi        

    #  test if libraryServer.perl is in bin subdirectory
    echo "========================================================================="
    if [ -s $HOME/bin/libraryServer.perl ]
    then
        echo "File libraryServer.perl exists in the CMVC family bin subdirectory."
        echo "                                                              .....PASSED"
        if [[ $dump = yes ]]
        then
            echo "\n================================================================\n"      >> $HOME/B390_rmi_debug.dump
            echo "File libraryServer.perl exists in the CMVC family bin subdirectory."       >> $HOME/B390_rmi_debug.dump
            echo "                                                              .....PASSED" >> $HOME/B390_rmi_debug.dump
        fi
    else
        echo "File libraryServer.perl does not appear to be installed in the CMVC family bin subdirectory.\n\n"
        echo "Ensure that:"
        echo "    1) The Build390 RMI Server tar file was extracted correctly into the CMVC family bin subdirectory."
        if [[ $dump = yes ]]
        then
            echo "\n================================================================\n"  >> $HOME/B390_rmi_debug.dump
            echo "ERROR - libraryServer.perl was not found in the bin subdirectory"      >> $HOME/B390_rmi_debug.dump
        fi
        exit 11
    fi    
    
######################################################
#   TEST - check if 5.0 parms are valid in .profile  #
######################################################    
else
    
    #  test if libraryServer.perl is in B390RMIHOME
    echo "=========================================================================" 
    if [ -s $B390RMIHOME/libraryServer.perl ]
    then
        echo "File libraryServer.perl exists in the RMI home directory:"
        echo "   $B390RMIHOME"
        echo "                                                              .....PASSED"
        if [[ $dump = yes ]]
        then
            echo "\n================================================================\n"      >> $HOME/B390_rmi_debug.dump
            echo "File libraryServer.perl exists in the RMI home directory: $B390RMIHOME"    >> $HOME/B390_rmi_debug.dump
            echo "                                                              .....PASSED" >> $HOME/B390_rmi_debug.dump
        fi
    else
        echo "File libraryServer.perl does not appear to be installed in the RMI home"
        echo "directory $B390RMIHOME.\n\n"
        echo "Ensure that:"
        echo "    1) The setting for B390RMIHOME exported in .profile is correct."
        echo "    2) The Build390 RMI Server tar file was extracted correctly into"
        echo "       the RMI home directory."
        if [[ $dump = yes ]]
        then
            echo "\n================================================================\n"      >> $HOME/B390_rmi_debug.dump
            echo "ERROR - libraryServer.perl was not found in B390RMIHOME dir $B390RMIHOME"  >> $HOME/B390_rmi_debug.dump
        fi
        exit 11
    fi            
    
    #  test if db2java.zip is in B390CLASSPATH
    echo "=========================================================================" 
    if [ -s $B390CLASSPATH ]
    then
        echo "File db2java.zip exists in the RMI classpath directory:"
        echo "   $B390CLASSPATH"
        echo "                                                              .....PASSED"
        if [[ $dump = yes ]]
        then
            echo "\n================================================================\n"      >> $HOME/B390_rmi_debug.dump
            echo "File db2java.zip exists in the RMI classpath directory: $B390CLASSPATH"    >> $HOME/B390_rmi_debug.dump
            echo "                                                              .....PASSED" >> $HOME/B390_rmi_debug.dump
        fi
    else
        echo "File db2java.zip does not appear to exist in the RMI CLASSPATH directory.\n\n"
        echo "Ensure that:"
        echo "    1) The setting for B390CLASSPATH exported in .profile is correct."
        echo "    2) The JDBC component of DB2 is installed on your system and the"
        echo "       db2java.zip file exists in db2 product java subdirectory."
        echo "    3) The db2java.zip file exists in the family sqllib/java directory."
        if [[ $dump = yes ]]
        then
            echo "\n================================================================\n"    >> $HOME/B390_rmi_debug.dump
            echo "ERROR - db2java.zip was not found in B390CLASSPATH $B390CLASSPATH"       >> $HOME/B390_rmi_debug.dump
        fi
        exit 12
    fi
    
    #  test if libdb2.a is in B390LIBPATH  
    echo "========================================================================="
    if [ -s $B390LIBPATH/libdb2.a ]
    then
        echo "B390LIBPATH is pointing to a valid db2 lib subdirectory."
        echo "                                                              .....PASSED"
        if [[ $dump = yes ]]
        then
            echo "\n================================================================\n"      >> $HOME/B390_rmi_debug.dump
            echo "B390LIBPATH is pointing to a valid db2 lib subdirectory."                  >> $HOME/B390_rmi_debug.dump
            echo "                                                              .....PASSED" >> $HOME/B390_rmi_debug.dump
        fi
    else
        echo "B390LIBPATH does not appear to be pointing to a valid DB2 sqllib/lib subdirectory.\n\n"
        echo "Ensure that the setting for B390LIBPATH exported in .profile is correct."
        echo "This setting is typically the sqllib/lib found under the CMVC family home"
        echo "directory if a normal CMVC installation was performed.."
        if [[ $dump = yes ]]
        then
            echo "\n================================================================\n"      >> $HOME/B390_rmi_debug.dump
            echo "ERROR - libdb2.a was not found in B390LIBPATH $B390LIBPATH" >> $HOME/B390_rmi_debug.dump
        fi
        exit 13
    fi            
        
fi

      
############################################################################
#  TEST - 2.4.1 - determine if port and alias added to libraryServer.perl  #
#                  - fail if values are script defaults                    #
#                  - parse if values appear valid                          #
#               (issue - determining if line is actually a comment line)   #
############################################################################


if [[ $release = 2.4.1 ]] ; 
then
    if [[ $dump = yes ]]
    then
        echo "libraryServer.perl hardcoded settings:\n" >> $HOME/B390_rmi_debug.dump
        cat $HOME/bin/libraryServer.perl | grep '$port =' | grep -v "^#" >> $HOME/B390_rmi_debug.dump
        cat $HOME/bin/libraryServer.perl | grep '$databaseName =' | grep -v "^#" >> $HOME/B390_rmi_debug.dump
    fi
    
    echo "========================================================================="

    # this will capture all active $port lines except one with a # comment at the end
    ckport=$(cat $HOME/bin/libraryServer.perl | grep '$port =' | grep -v '#')
    parsept=${ckport#*\"}
    portnum=${parsept%%\"*}
    
    # this will capture all active $database lines except one with a # comment at the end
    ckport=$(cat $HOME/bin/libraryServer.perl | grep '$databaseName =' | grep -v '#')
    parsedb=${ckport#*\"}
    dbalias=${parsedb%%\"*}
    
    if [[ $portnum = portNumberGoesHere ]]
    then
        echo "The RMI Port number parm does not appear to have been updated in the"
        echo "$HOME/bin/libraryServer.perl script.  Please update the \$port"
        echo "and \$database lines in this script per the B390 RMI Install Guide."
        exit 51
    else
        echo "The \$port value set in libraryServer.perl is currently $portnum"
    fi
    
    if [[ $dbalias = tcpipDatabaseAliasGoesHere ]]
    then
        echo "The RMI database alias parm does not appear to have been updated in the"
        echo "$HOME/bin/libraryServer.perl script.  Please update the \$port"
        echo "and \$database lines in this script per the B390 RMI Install Guide."
        exit 51
    else
        echo "The \$databaseName value set in libraryServer.perl is currently $dbalias"
    fi
            
else
    portnum=$B390RMISERVERPORT
    dbalias=$B390DATABASEALIAS
fi        
    
############################################################################
#  TEST - determine if rmi port is defined in /etc/services                #
############################################################################

echo "========================================================================="
serviceck=$(cat /etc/services | grep -F "$portnum" | grep -v "^#" )
if [[ -z $serviceck ]]
then
    echo "Build390 RMI port number $portnum does not appear to be"
    echo "defined in the system /etc/services file.  This value"
    echo "must be defined to /etc/services per the B390 RMI Installation Guide."
    if [[ $dump = yes ]]
    then
        echo "\n================================================================\n"      >> $HOME/B390_rmi_debug.dump
        echo "ERROR - RMI port number $portnum was not found in /etc/services" >> $HOME/B390_rmi_debug.dump
    fi
    exit 61
else    
    echo "Build390 RMI port number $portnum is listed in /etc/services."
    echo "                                                              .....PASSED"
    if [[ $dump = yes ]]
    then
        echo "\n================================================================\n"      >> $HOME/B390_rmi_debug.dump
        echo "Build390 RMI port number $portnum is listed in /etc/services."             >> $HOME/B390_rmi_debug.dump
        echo "                                                              .....PASSED" >> $HOME/B390_rmi_debug.dump
    fi
fi                                                                                    

###########################################################################
#  TEST - determine if service port is defined in /etc/services           #
###########################################################################
 
 echo "========================================================================="
 serviceck2=$(cat /etc/services | grep -F "$B390SERVICEPORT" | grep -v "^#" )
 if [[ -z $serviceck2 ]]
 then
     echo "Build390 Service port number $B390SERVICEPORT does not appear to be"
     echo "defined in the system /etc/services file.  This value"
     echo "must be defined to /etc/services per the B390 RMI Installation Guide."
     if [[ $dump = yes ]]
     then
         echo "\n================================================================\n"        >> $HOME/B390_rmi_debug.dump
         echo "ERROR - Service port number $B390SERVICEPORT was not found in /etc/services" >> $HOME/B390_rmi_debug.dump
     fi
     exit 62            
 else
     echo "Build390 service port number $B390SERVICEPORT is listed in /etc/services."
     echo "                                                              .....PASSED"
     if [[ $dump = yes ]]
     then
        echo "\n================================================================\n"      >> $HOME/B390_rmi_debug.dump
        echo "Build390 service port number $B390SERVICEPORT is listed in /etc/services." >> $HOME/B390_rmi_debug.dump
        echo "                                                              .....PASSED" >> $HOME/B390_rmi_debug.dump
     fi

 fi

############################################################################
#  TEST - ensure RMI and SERVICE ports are unique                          #
############################################################################

echo "========================================================================="
if [[ $portnum = $B390SERVICEPORT ]]
then
    echo "Build390 Service port number $B390SERVICEPORT and RMI port number $portnum"
    echo "are set to the same value.  These values must be unique for the RMI server"
    echo "to communicate properly with the CMVC database as well as B390 Clients."
    echo "Please update the parms and /etc/services file accordingly."
    if [[ $dump = yes ]]
    then
        echo "\n================================================================\n"          >> $HOME/B390_rmi_debug.dump
        echo "ERROR - Service port number $B390SERVICEPORT matches RMI port number $portnum" >> $HOME/B390_rmi_debug.dump
    fi
    exit 63            
 else
    echo "Build390 service and RMI port numbers are unique."
    echo "                                                              .....PASSED"
    if [[ $dump = yes ]]
    then
        echo "\n================================================================\n"      >> $HOME/B390_rmi_debug.dump
        echo "Build390 service and RMI port numbers are unique."                         >> $HOME/B390_rmi_debug.dump
        echo "                                                              .....PASSED" >> $HOME/B390_rmi_debug.dump
    fi

 fi
    
        
##########################################################################################
#  TEST - determine if cmvc database alias is configured correctly by trying db2 connect #
#         using alias extracted from script (241) or B390 parm (40)                      #
##########################################################################################

echo "========================================================================="        
echo "Attempting connection to the CMVC database using the alias name $dbalias"
echo "Issuing db2 connect to $dbname user $shortfam using $DB2_PASS \n"
db2 connect to $dbalias user $shortfam using $DB2_PASS > debug_tempfile

results=$(cat debug_tempfile | grep -F 'Database Connection Information')
if [[ -z "$results" ]]
then
    rest=$(cat debug_tempfile | head -1)
    echo "ERROR - $rest\n"
    echo "Depending on the error received, ensure:"
    echo "   1) The database alias appears in the db2 database directory"
    echo "         (db2 list database directory)"
    echo "   2) The nodename for the database alias appears in the db2 node"
    echo "      directory.  (db2 list node directory)"
    echo "   3) The RMI port number $portnum is not already utilized by other"
    echo "      applications.  (netstat -an | grep $portnum)"
    echo "Detailed db2 database alias debug tips can be found in Appendix B"
    echo "of the B390 RMI Installation Guide."
    if [[ $dump = yes ]]
    then
        echo "\n================================================================\n"      >> $HOME/B390_rmi_debug.dump
        echo "ERROR - unable to connect to db2 alias using db2 connect to $dbname user $shortfam using $DB2_PASS" >> $HOME/B390_rmi_debug.dump
        echo "Received:\n $rest" >> $HOME/B390_rmi_debug.dump
    fi
    exit 71
else
    results=$(cat debug_tempfile)
    echo "$results"
    echo "\nCMVC db2 database alias connection was successful."
    echo "                                                              .....PASSED"
    if [[ $dump = yes ]]
    then
        echo "\n================================================================\n"      >> $HOME/B390_rmi_debug.dump
        echo "CMVC db2 database alias connection was successful."                        >> $HOME/B390_rmi_debug.dump
        echo "                                                              .....PASSED" >> $HOME/B390_rmi_debug.dump
    fi

fi
rm debug_tempfile

###############################
# final msgs/ report file     #
############################### 

echo "========================================================================="
echo "B390_rmi_debug.ksh execution completed on $(date)."
if [[ $warnings = 0 ]]
then
    echo "\nAll tests completed successfully.\n"     
else
    echo "\nMost tests completed successfully but some warnings were noted."  
    echo "\nExamine the preceeding messages to determine what corrective"
    echo "\nactions or manual verifications might be needed."
fi    
if [[ $dump = yes ]]
then
     
     echo "Environment information to assist Build390 support in further"
     echo "problem analysis can be found in $HOME/B390_rmi_debug.dump."
     echo "\nAlso look for related messages in the opServer logs found"
     echo "in the Build390 install directory."
     echo "\n\n==================================================================" >> $HOME/B390_rmi_debug.dump
     echo "\nAll debug tests completed successfully." >> $HOME/B390_rmi_debug.dump
fi
echo "=========================================================================\n"
        
###########
#  exit   #
###########


exit 0


