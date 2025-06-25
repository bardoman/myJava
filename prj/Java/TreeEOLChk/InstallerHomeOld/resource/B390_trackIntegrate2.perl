#!/usr/bin/perl
# perl script
# Track Integrate Post Process user exit
# CMVC action arguments: release defect
# Track Integrate argument order: librelease defect parmfile
# 1. use the BPS UserExitConnection to RETAIN
# 2. get APAR name for a given defece and release
# 3. get ++HOLD data for APAR
# 4. get closing text for APAR
# 5. get integrity flag
# 6. APAR definition to PDT
# 7. close APAR in RETAIN
#---------------------------------------------------------------------
# 06/08/99   SDWB 2.2 release
# 06/10/99   use Build390svr.jar
# 06/28/99   check for service sub process
# 06/28/99   use jre instead of java
# 09/30/99   unset environment variables
# 10/13/99   remove swing_home
# 11/06/99   conditional print for debug mode
# 03/17/00   add clientsetup to cmd line
# 06/16/00   add print lines for input args and release not in service
# 12/24/02   #DEF.PTM2418: remove non error message
######################################################################
# change the current working directory to $HOME/bin
use IO::Socket;

$wdir = "$ENV{HOME}/bin";
chdir $wdir or die "Can't cd to $wdir: $!\n";

# 11/06/99, chris, get debug env. variable
$debug = 0; # default - turn off
if (defined $ENV{B390RMIDEBUG}) {
    $option = "$ENV{B390RMIDEBUG}";
    if ($option eq "ON" or $option eq "on") {
        $debug = 1; # turn on debug mode
    }
}

# get parameters from CMVC except parmfile
if ($debug) {
   logit("-------------------------------------------------------------\n", "no");
   logit("Build390 user exit - track integrate 2 - args = @ARGV\n");
}
($fakeParm, $release, $defect) = @ARGV;

#kishore chgs on july 27/2001 to check for both relprocess is service and isInservice(inservice is TRUE)
 #kishore chgs on july 27/2001 START
   if ($debug) {
   logit("checking release in service,inservice  on  $release \n"); 
   }

   $qrycmd="cmvcqry -g releaseview -w \"name=\'$release\'\" -s relprocess,inservice";
   
   if($debug){
    logit("query running is  $qrycmd \n");
   }

   $sub=`$qrycmd`;
   
   if($debug){
   logit("query result is $sub \n");
   }

   chomp($sub);

   if($debug){
   logit("query result checking for service and TRUE in   $sub \n");
   }
   
   if ($sub ne "service|TRUE") {
      # the release is not for build/390 service
      if($debug){
      logit("Release $release is not in service\n");
      }
      #DEF.PTM2418: 
      #print  "Release $release is not in service\n";
      
      exit(0);
   }

   if($debug){
   logit("Release $release is configured with relprocess as  service and isservice is TRUE \n");
   }
 #kishore chgs on july 27/2001 END

#connect to Build/390 service server & send exit info
   $host = "localhost";
   $EOL = "\015\012";
   $BLANK = $EOL x 2;
   $remote = IO::Socket::INET->new( Proto     => "tcp",
                                    PeerAddr  => $host,
                                    PeerPort  => "$ENV{B390SERVICEPORT}",
                                   );
   unless ($remote) { die "cannot connect to service daemon on $host, it may be down." }
   print $remote "TrackIntegrate defect=$defect release=$release \n\n" . $BLANK;
# this pipes any output from the server  back to the client.
   $returnString = "";
   while ( <$remote> ) { $returnString .= $_ }
   close $remote;
   if ($returnString=~/error/i) {
      print $returnString;
      exit(-1);
   }else {
      print "Promotion to integrate successful.\n";
   }


sub logit {
   my ($dt, $prt, $rest) = @_;
   if ($prt ne "no") {
      print $dt;
   }
   ($sec,$min,$hour,$mday,$mon,$year,$wday,$yday,$isdst) = localtime(time);
   $year = $year+1900;
   $mon  = $mon+1;

   $b390Home = $ENV{B390RMIHOME};

   if (open(LOGFILE, ">>$b390Home/UserExit.log")) {
      flock LOGFILE, 2 or print "Lock failed LOGFILE\n";
      print LOGFILE "$mon:$mday:$year-$hour:$min:$sec - $dt";
   } else {
      print "Failed to open log file\n";
   }
   close(LOGFILE);
   return 0;
}
