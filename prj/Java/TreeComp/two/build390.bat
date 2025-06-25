@echo OFF
REM Use this batch file to start the Build/390 client on windows platforms
REM Because windows 95 and 98 remove equal signs from the input parms, you MUST prefix the
REM input parameter string with a double quote. 
REM Such as Build390 "buildtypelist librelease=trel driver=tdrv
REM This is not needed under NT because in NT the %* variable returns the input string as entered

REM Check for print help if so print it to the file

REM ***Aternately comment out one of the two lines below to set dos window mode***
set DOS_WIN=OPEN
REM set DOS_WIN=CLOSED

set ADJAR=.\AdditionalJars

set ADD_PATH=%ADJAR%\bpsService390.jar;
set ADD_PATH=%ADD_PATH%%ADJAR%\java390API.jar;
set ADD_PATH=%ADD_PATH%%ADJAR%\xerces.jar;
                
set ADD_PATH=%ADD_PATH%%ADJAR%\help4.jar;
set ADD_PATH=%ADD_PATH%%ADJAR%\ohj-jewt.jar;
set ADD_PATH=%ADD_PATH%%ADJAR%\oracle_ice5.jar;

set B390_CLASSPATH=.;Build390.jar;%ADD_PATH%

if NOT "%OS%" == "Windows_NT" goto TEST95

IF NOT "%1" == "/printhelp"  GOTO RUNNT
jre\bin\java -mx200m -classpath %B390_CLASSPATH% Build390.MBClient /HELP > OUT
ECHO The help has been printed to file 'OUT'
GOTO END

:RUNNT
IF NOT %DOS_WIN% == OPEN GOTO NO_DOS_WIN

.\jre\bin\java -mx400m  -Dswing.metalTheme=steel -classpath %B390_CLASSPATH% Build390.MBClient %* 

GOTO END

:NO_DOS_WIN

START .\jre\bin\javaw -mx400m  -Dswing.metalTheme=steel -classpath %B390_CLASSPATH% Build390.MBClient %* 

GOTO END

:TEST95
if  "%1"=="/printhelp" goto HELP95
REM If the input was not prefixed with a double quote, error
IF  NOT "%2"=="" goto ERROR

IF NOT %DOS_WIN% == OPEN GOTO NO_DOS_WIN2

.\jre\bin\java -mx400m -Dswing.metalTheme=steel -classpath %B390_CLASSPATH% Build390.MBClient %1 

GOTO END

:NO_DOS_WIN2

START .\jre\bin\javaw -mx400m -Dswing.metalTheme=steel -classpath %B390_CLASSPATH% Build390.MBClient %1 

GOTO END

:HELP95
.\jre\bin\java -mx200m -classpath %B390_CLASSPATH%  Build390.MBClient /HELP > OUT
ECHO The help has been printed to file 'OUT'

goto END

:ERROR
echo You must prefix the complete list of paramaters with a double quote such as Build390 "buildtypelist librelease=trel driver=tdrv

:end
