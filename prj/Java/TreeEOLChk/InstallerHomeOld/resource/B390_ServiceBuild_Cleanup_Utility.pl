#!/usr/bin/perl                                                                        #
#   B390_ServiceBuild_Cleanup_Utility.pl - Utility to cleanup Service Build            #
#                                          Directories on CMVC Family Server           #
#                                                                                      #
#      This utility script is designed to periodically clean up RMI service build      #
#    directories after the applicable apar has closed.  It should be run from the cmvc #
#    family userid and can be used on either a standalone basis or setup as a weekly   #
#    cron job.                                                                         #
#                                                                                      #  
#  Name             Date                                                               #
#  ---------------- --------    -------------------------                              #
#  S. Miller        8/20/03     Build390 Rel 5.0 New Script                            #
#                                                                                      #
#  Input:                                                                              #
#      CMVC Defect Status Report                                                       #
#      list of files and directories in RMI apar subdirectory                          #
#                                                                                      #
#  Outputs:                                                                            #
#      Deletes build directories for apar defects that have moved to closed status     # 
#              indicating ptfs have shipped.                                           #
#                                                                                      #
#   Flow:                                                                              #
#      Generate list of files and directories in RMI home apar subdirectory            #                                                       #
#      In the case of apar build subdirs, determine apar number from check.out file    #                                                          #
#      Test if apar defect in closed status in CMVC                                    #
#         If yes - delete apar build directory                                         #
#      In the case of aparnum.defect files, determine apar number from filename        #
#      Test if apar defect in closed status in CMVC                                    #
#         If yes - delete aparnum.defect file                                          # 
#      exit                                                                            #
#                                                                                      #
########################################################################################
#  declare global vars                           #
##################################################
use vars qw($path $lines $dirrpt $chk_prt $aparstate $out $cmvccmd $cmd);

# get rmi home directory from env var
$path = $ENV{B390RMIHOME} ;
print "\nBeginning cleanup of old service build information in $path/apar directory.\n";

# fill array with filelist from apar subdirectory
@lines = <$path/apar/*>;

# test contents of apar directory
for $j ( 1 .. $#lines) {
   # reinitialize array on each execution
   undef @dirrpt;
   undef @out;
   undef $aparstate;
   
   # test if filename is a directory
   if (-d $lines[$j]) {
      
       # check subdirectory for aparnum_check.prt file
       @dirrpt = <$lines[$j]/*_check.prt>;
       # if file exists, extract apar name and check defect status 
       if ($dirrpt[0]) {
          @out = split /_/, $dirrpt[0];
          $out[0] =~ s($lines[$j]/) ()g;
          $cmvccmd="Report -general defectview -select state -where \"name=\'$out[0]\'\" "; 
          # execute cmvc query using backtick structure
          $aparstate=`$cmvccmd`;
          chomp($aparstate);
          #  if closed, then rm -fr directory
          if ($aparstate eq "closed") {
              print "\n\n Defect for apar $out[0] is closed";
              print "\n Deleting directory $lines[$j]";
              $cmd = "rm -fR $lines[$j]";
              system($cmd);
          }
       }
  } else {    
     # not a directory; ck if .defect file
     @out = split /\./, $lines[$j];
     if ($out[1] eq "defect") {
        $out[0] =~ s($path/apar/) ()g;
        $cmvccmd="Report -general defectview -select state -where \"name=\'$out[0]\'\" "; 
        # execute cmvc query using backtick structure
        $aparstate=`$cmvccmd`;
        chomp($aparstate);
        #  if closed, then rm -fr directory
        if ($aparstate eq "closed") {
           print "\n\n Defect for apar $out[0] is closed";
           print "\n Deleting file $lines[$j]";
           $cmd = "rm $lines[$j]";
           system($cmd);
        }

     }
  }
}
print "\n\nCleanup complete.\n\n";
exit;
