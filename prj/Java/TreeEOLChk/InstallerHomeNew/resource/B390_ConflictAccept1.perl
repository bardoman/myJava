#!/usr/bin/perl
# --------------------------------------------------------------------------
#############################################################################
# 12/24/02   #DEF.PTM2418: remove non error message
######################################################################

# change the current working directory to $HOME/bin
use IO::Socket;

$wdir = "$ENV{HOME}/bin";
chdir $wdir or die "Can't cd to $wdir: $!\n";

$debug = 0; # default - turn off
if (defined $ENV{B390RMIDEBUG}) {
    $option = "$ENV{B390RMIDEBUG}";
    if ($option eq "ON" or $option eq "on") {
        $debug = 1; # turn on debug mode
    }
}

   if ($debug) {
      logit("-------------------------------------------------------------\n", "no");
      logit("Build390 user exit - Confict Accept 0 - args = @ARGV\n");
   }
   ($release, $defect, $trackType, $filePath) = @ARGV;

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

   print "In service releases conflict accept is disabled.  Instead you must\n ";
   print "return the track $defect to the fix state and undo any changes to file\n ";
   print "$filePath to remove the conflict.  See the Build/390 Client user's guide\n ";
   print "for further details.\n";
   exit(1);

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
