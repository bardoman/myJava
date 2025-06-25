#!/usr/bin/perl
# perl script
###################################################################
#Set this to the hostname of the BPS server
$bpsHost = "BPSServerGoesHere";
###################################################################

###################################################################
#Set this to the port of the BPS server
$bpsPort = "BPSPortGoesHere";
###################################################################


# change the current working directory to $HOME/bin
$wdir = "$ENV{HOME}/bin";
chdir $wdir or die "Can't cd to $wdir: $!\n";

delete $ENV{JDK_HOME};
delete $ENV{JAVA_HOME};

####################################################################
#This would be set by the dev team everytime the new bpsversion package is bundled
#in the cmvc server tar ball.This shouldnt be changed.
$bpsversion = "Bps241";
###################################################################

$classpath = "$wdir:$wdir/Build390svr.jar:$wdir/$bpsversion/bpsService390.jar:$myclasspath";


# get params
($aparFile, $errorEmail) = @ARGV;
$cmd = "./jre/bin/java -classpath $classpath Build390.Migration.BPSPullAPAR $bpsHost $bpsPort $aparFile $errorEmail";
exec("$cmd");


