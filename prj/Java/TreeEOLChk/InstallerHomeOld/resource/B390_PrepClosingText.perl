#!/usr/bin/perl
# perl script
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
   logit("Build390 user exit - prepClosingText - args = @ARGV\n");
}
($defect, $release) = @ARGV;


#connect to Build/390 service server & send exit info
   $host = "localhost";
   $EOL = "\015\012";
   $BLANK = $EOL x 2;
   $remote = IO::Socket::INET->new( Proto     => "tcp",
                                    PeerAddr  => $host,
                                    PeerPort  => "$ENV{B390SERVICEPORT}",
                                   );
   unless ($remote) { die "cannot connect to service daemon on $host, it may be down." }
   print $remote "PrepClosingText defect=$defect release=$release \n\n" . $BLANK;
# this pipes any output from the server  back to the client.
   $returnString = "";
   while ( <$remote> ) { $returnString .= $_ }
   close $remote;
   if ($returnString=~/error/i) {
      print $returnString;
      exit(-1);
   }else {
      print "Closing text priming.\n";
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
