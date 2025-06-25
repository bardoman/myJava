#!/usr/bin/perl
# perl script
###################################################################
#Set this variable to point to the directory that includes the db2 library files (this might be the right directory)
$mylibpath = "$ENV{DB2_HOME}/sqllib/lib";
###################################################################

###################################################################
#Set this variable to the location of the db2java.zip file in db2 (probably ~/sqllib/java/db2java.zip)
$myclasspath = "$ENV{DB2_HOME}/sqllib/java/db2java.zip";
###################################################################

###################################################################
#Set this to the tcpip alias of the family database
$databaseName = "tcpDatabaseAliasHere";
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

$ENV{LIBPATH}="$mylibpath:$ENV{LIBPATH}";
$classpath = "$wdir:$wdir/Build390svr.jar:$wdir/$bpsversion/bpsService390.jar:$myclasspath";

# get params
($ptfFile) = @ARGV;
$familyName = $ENV{CMVC_FAMILY};
$databaseId = $ENV{DB2INSTANCE};
$databasePassword = $ENV{DB2_PASS};                                     
$cmd = "./jre/bin/java -classpath $classpath Build390.Migration.InitializeMigratedPTFTable $databaseName $databaseId $databasePassword $ptfFile";
exec("$cmd");


