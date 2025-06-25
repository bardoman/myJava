#!/usr/bin/perl
# perl script
###################################################################

($oldFilePath, $release, $newFilePath) = @ARGV;

$oldFilePath =~ /(.*\/)?([^\.]+)(.*)/;
$oldDir = $1;
$oldPartName = $2; # according to the way MVS determines part names by default
$oldExtention = $3;

$newFilePath =~ /(.*\/)?([^\.]+)(.*)/;
$newDir = $1;
$newPartName = $2; # according to the way MVS determines part names by default
$newExtention = $3;

#print "$oldFilePath\n $newFilePath\n$oldDir\n $oldPartName\n $oldExtention\n $newDir\n $newPartName\n $newExtention\n";
if ($oldPartName ne $newPartName) {
   print "You cannot change the part name from $oldPartName to $newPartName, only the directory path and extension.\n";
   exit (1);
}
exit(0);

