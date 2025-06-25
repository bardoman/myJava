#!/usr/bin/perl
# perl script
# Track Create Pre user exit
# --------------------------------------------------------------------------
#############################################################################
use IO::Socket;

# change the current working directory to $HOME/bin
$wdir = "$ENV{HOME}/bin";
chdir $wdir or die "Can't cd to $wdir: $!\n";

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
      logit("Build390 user exit - TrackCreate 1 - args = @ARGV\n");
   }
   ($release, $defect) = @ARGV;

   $qrycmd="cmvcqry -g releaseview -w \"name=\'$release\'\" -s relprocess,inservice";
   
   $sub=`$qrycmd`;
   
   chomp($sub);

   if ($sub ne "service|TRUE") {
      exit(0);
   }




#connect to Build/390 service server & send exit info
   $host = "localhost";
   $EOL = "\015\012";
   $BLANK = $EOL x 2;
   $remote = IO::Socket::INET->new( Proto     => "tcp",
                                    PeerAddr  => $host,
                                    PeerPort  => "$ENV{B390SERVICEPORT}",
                                   );
   unless ($remote) { die "cannot connect to service daemon on $host, it may be down." }
   print $remote "TrackCreatePre defect=$defect release=$release \n\n" . $BLANK;
# this pipes any output from the server  back to the client.
   $returnString = "";
   while ( <$remote> ) { $returnString .= $_ }
   close $remote;
   print $returnString;
   if ($returnString=~/error/i) {
      exit(-1);
   }elsif ($returnString=~/exception/i) {
      exit(-1);
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
