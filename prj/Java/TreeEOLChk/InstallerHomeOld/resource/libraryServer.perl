#!/usr/bin/perl
# perl script
# start cmvcOperationsServer, RMI server
###################################################################

#check that all needed environment variables are present.
checkForNeededEnvironmentVariables();

#grab the environment variables we need
$mylibpath = $ENV{B390LIBPATH};
$myclasspath = $ENV{B390CLASSPATH};
$port = $ENV{B390RMISERVERPORT};
$databaseName = $ENV{B390DATABASEALIAS};
$b390Home = $ENV{B390RMIHOME};
@famNameArray = split( /@/, $ENV{CMVC_FAMILY}, 2 );
$familyName = @famNameArray[0];
$servicePort = $ENV{B390SERVICEPORT};
$bpsHost = $ENV{B390BPSHOST};
$bpsPort = $ENV{B390BPSPORT};
$rmiServerHost = $ENV{B390RMISERVERHOST};

# this should be the default user for the schema used by the family. Normally it is DB2INSTANCE variable. Otherwise set to schema name
$databaseId = $ENV{USER};
$databasePassword = $ENV{DB2_PASS};


$ENV{LIBPATH}="$mylibpath:$ENV{LIBPATH}";

#nuke these or else the jre will look in the wrong place
delete $ENV{JDK_HOME};
delete $ENV{JAVA_HOME};


$debug = 0; # default - turn off
if (defined $ENV{B390RMIDEBUG}) {
    $option = "$ENV{B390RMIDEBUG}";
    if ($option eq "ON" or $option eq "on") {
        $debug = 1; # turn on debug mode
    }
}

if ($debug) {
    print "Build390 Library Server\n";
}

# change the current working directory to $HOME/bin
chdir $b390Home or die "Can't cd to $b390Home: $!\n";

$classpath = "$b390Home:$b390Home/Build390.jar:$b390Home/AdditionalJars/bpsService390.jar:$b390Home/AdditionalJars/xerces.jar:$b390Home/AdditionalJars/java390API.jar:$myclasspath";

# get params
$parms = @ARGV;
$passwordFilename = "B390ServerParm";

($action) = @ARGV;

$cmdhead = "./jre/bin/java -Djava.rmi.server.hostname=$rmiServerHost -Xgcpolicy:optavgpause -mx400m -ss512k -classpath $classpath Build390.LibraryRMI.cmvcOperationsServer";
if ($parms == 1) {
   # action for rmi on given port
   if ($action eq "s" || $action eq "start"){
         $cmd="$cmdhead $familyName $databaseName $databaseId $passwordFilename $port $servicePort $bpsHost $bpsPort $action &";
      }elsif ($action eq "r" || $action eq "report") {
         $cmd="$cmdhead $familyName $databaseName $databaseId $passwordFilename $port $action &";
      } elsif ($action eq "q" ||  $action eq "quit") {
            $lines = `ps -ef | grep Library | grep $familyName | grep -v grep`;
            $lines =~ /^\s*(\w+)\s+(\w+)\s+.*/;
            $pname = $1;
            $pidnum = $2;
           if (($pid) = ($pidnum =~ /^(\d+)/)) { # pid should be a digit
               $cnt = kill 9, $pid;
           } else { # invalid process id
                exit(-1);
           }
           if ($debug) {
               print "The number of successful termination=$cnt, pid=$pid.\n";
           }
           exit(0);
       } else {
           printUsage();
           exit (-1);
       }  # invalid actions
} else {
    printUsage();
    exit(-1);
} # invalid number of parms

if ($debug) {
    print "Executing command:$cmd \n";
}

#use > to make sure we create a new file
open (COMMANDFILE, "> $passwordFilename");
close (COMMANDFILE);
#set the permissions so no one can read it
chmod (0700, "$passwordFilename");
#reopen, and append to it the actual password
open (COMMANDFILE, "+< $passwordFilename");
print COMMANDFILE ("$databasePassword\n");
close (COMMANDFILE);
system "$cmd";


sub printUsage {
    print "Usage:\n";
    print "To start the Build/390 Library server: libraryServer.perl s\n";
    print "To see what is running:  libraryServer.perl r \n";
    print "To quit the Build/390 Library server: libraryServer.perl q \n";
    print "   where: s or start, r or report, q or quit.\n";
}

sub checkForNeededEnvironmentVariables {
   $missingVariables = "";
   if (!defined $ENV{B390LIBPATH} ) {
      $missingVariables = "$missingVariables \n The B390LIBPATH keyword must be defined in the environment.";
   }
   if (!defined $ENV{B390CLASSPATH} ) {
      $missingVariables = "$missingVariables \n The B390CLASSPATH keyword must be defined in the environment.";
   }
   if (!defined $ENV{B390RMISERVERPORT} ) {
      $missingVariables = "$missingVariables \n The B390RMISERVERPORT keyword must be defined in the environment.";
   }
   if (!defined $ENV{B390DATABASEALIAS} ) {
      $missingVariables = "$missingVariables \n The B390DATABASEALIAS keyword must be defined in the environment.";
   }
   if (!defined $ENV{B390RMIHOME} ) {
      $missingVariables = "$missingVariables \n The B390RMIHOME keyword must be defined in the environment.";
   }
   if (!defined $ENV{CMVC_FAMILY} ) {
      $missingVariables = "$missingVariables \n The CMVC_FAMILY keyword must be defined in the environment.";
   }
   if (!defined $ENV{B390SERVICEPORT} ) {
      $missingVariables = "$missingVariables \n The B390SERVICEPORT keyword must be defined in the environment.";
   }
   if (!defined $ENV{B390BPSHOST} ) {
      $missingVariables = "$missingVariables \n The B390BPSHOST keyword must be defined in the environment.";
   }
   if (!defined $ENV{B390BPSPORT} ) {
      $missingVariables = "$missingVariables \n The B390BPSPORT keyword must be defined in the environment.";
   }
   if (!defined $ENV{B390RMISERVERHOST} ) {
      $missingVariables = "$missingVariables \n The B390RMISERVERHOST keyword must be defined in the environment.";
   }
   if (length $missingVariables >0) {
      die $missingVariables;
   }
}

