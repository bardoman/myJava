******************************************************************
Build/390 SDWB 5.0.0:0.15 Install/UnInstall Instructions
******************************************************************

Last update : 01/30/2009

The Readme is divided into the following sections
i  ) System Requirements
ii ) GUI Mode (Windows Only)  
iii) Command Line Mode (Windows Only)
iv)  Command Line Mode (USS,ODE only)
iv ) Post install/uninstall instructions
     a)Defects Fixed.
     b)Verifying Build390 directory structure.

******************************************************************
i)              SYSTEM REQUIREMENTS
******************************************************************

The Following clients can be installed/uninstalled

a)Windows(95,NT,2000,XP)
b)USS

The Following server can be installed/uninstalled

a)Aix Server(ODE/NON ODE)

******************************************************************
0. Introduction 
******************************************************************

The instructions for the following actions can be read below.

1.) Upgrade from a previous version.

2.) Uninstall an already existing installation.
    Caution.This completely removes an installation!

3.) Perform a new(clean) Build390 5.0.0:0.15 or above  installation.

4.) Rollback to a previous Installation.


******************************************************************
ii). GUI Mode (Windows Only)  
******************************************************************
Windows - <INSTALL>:
-------------------
1.) Download the Installer package WINCB390_5015.EXE into the root 
   of the Build390 installation directory.

2.) Execute the installer executable to unzip the package.
   

3.) For Windows GUI Installer, allow the package to autoexecute 
    SetupGUIWin.bat 


GUI Mode:
=========
1.) Download the Installation package "WINCB390_5015.EXE"

2.) Execute the installation package "WINCB390_5015.EXE" to expand
    its contents. Specify the path of the directory you want the package expanded into.

3.) The setup batch file automatically runs and starts the gui
    installer.

4.) Step 1. (Welcome screen). Please click Next to move on.

5.) Step 2. (Directory selection). Please choose a valid 
            installation directory.
            Click next.

6.) Step 3. The installer automatically figures out the
            version. 
            
    * To Upgrade from a previous version.
    --------------------------------------------------------------
    i)   Select  a valid previous working installation in step2.
    ii)  Select the radiobutton "Install SDWB<NEW_VERSION>" .
    iii) Click Next. in Step 3.
  

    * To Uninstall a existing installation. 
    --------------------------------------------------------------
    i)   Select  a valid previous working installation in step2.
    ii)  Select the radiobutton "UnInstall SDWB<OLD_VERSION>" .
    iii) Click Next. in Step 3.

    * To Perform a clean installation of the new version.
    --------------------------------------------------------------
    i)   Select  a valid clean directory in step2.
    ii)  Select the radiobutton "Install SDWB<NEW_VERSION>" .
    iii) Click Next. in Step 3.

    * To Rollback to a previous Installation.
    --------------------------------------------------------------
    i)   Select  a valid previous working installation in step2.
    ii)  Select the radiobutton "Rollback SDWB<OLD_VERSION>" .
    iii) Click Next. in Step 3.

                
7.) Step 4. The installer starts installing. Click  Next.

8.) Step 5. Any post install instructions are shown here.


******************************************************************
iii).  Command Line Mode (Windows Only)
******************************************************************
1.) For Windows CommandLine Installer, disallow the autoexecute 
    and  the package will only unzip. 
    Execute SetupTextWin.bat -<install/uninstall> 
            INSTALLPATH=<somepath> REPLACE=Y/N
The REPLACE should be used to overwrite an already installed 
    resource.


******************************************************************
iv).  Command Line Mode (USS,Aix only)
******************************************************************

Aix Server (ODE/NON ODE) - <INSTALL>:
-------------------------------------

1.) Download the Installer Resource into the root of the Build390 
    installation directory.

2.) Untar the InstallerResource into the root of the build390 
    installation directory

2.) Execute the Install script file residing in InstallerHome 
    directory. 
    Note: The InstallerHome directory resides in the root of the  
    Build390  installation directory.

3.) For Aix commandline Installer, execute

> Setup.ksh


(Figure-DIALOG - A)
==========================================     
Build390 Installer/UnInstaller For Aix/USS     
==========================================     
1       Aix Server                             
2       Aix Server(ODE)                             
3       Help                             
4       Quit  Install/UnInstall                
Enter 1, 2, 3, or 4: 1
Please enter 1, for Aix Server.                  
========================================== 
Performing.. Aix Server                   
========================================== 
JRE  EXISTS in ../jre                      
replacing ../jre with ./jre                
copying ./jre ../jre    

(Figure:DIALOG -B)                   
========================================== 
Choose Install/Rollback/UnInstall
========================================== 
1       Install                            
2       Rollback                            
3       UnInstall                          
4       Quit  
Enter 1, 2, 3 or 4:1                           
Please enter 1, to install AixServer
========================================== 
Performing.. Install                       
========================================== 
Enter a UnInstall or Install Path: <a valid install path has to be
given> 
After that the install is done.         


USS Client (ODE/NON ODE) - <INSTALL>:
-------------------------------------

1.) Download the Installer Resource into the root of the Build390 
    installation directory.

2.) Untar the InstallerResource into the root of the build390 
    installation directory
> tar -xvfo USS_CB390_<xx>_<xx>.tar   

3.) Execute the Install script file residing in InstallerHome 
    directory. 
    Note: The InstallerHome directory resides in the root of the
          Build390  installation directory.

4.) For USS commandline Installer, execute

> ./Setup.ksh

Figure:DIALOG-A
==========================================     
Build390 Installer/UnInstaller For Aix/USS     
==========================================     
1       USS Client                             
2       USS Client(ODE)                             
3       Help                             
5       Quit
Enter 1, 2, 3, or 4: 1
Please enter 1, for USS Client.                  
========================================== 
Performing.. USS Client                    
========================================== 
Note:The build390 installer needs  a valid 
     java  executable(i.e  java) to start.
     Please contact the site administrator  
     if you need help in locating the path
eg  :/usr/lpp/java/IBM/J1.5.0, assuming that
     the   java    executable   resides in
     /usr/lpp/java/IBM/J1.5.0/bin directory.
==========================================
>>Enter  the  java executable  path in uss
(hit enter to accept /usr/lpp/java/IBM/J1.5.0/
as     the     default),      or
(enter a different jre path), or
(enter 'quit' to exit)?
==========================================
A    valid   java   executable   found    in
/usr/lpp/java/IBM/J1.5.0/bin
(Figure:DIALOG -B)                   
========================================== 
Choose Install/Rollback/UnInstall
========================================== 
1       Install                            
2       Rollback                            
3       UnInstall                          
4       Quit  
Enter 1, 2, 3 or 4:1                           
Please enter 1, to install USSClient
========================================== 
Performing.. Install                       
========================================== 
Enter a UnInstall or Install Path: <a valid 
install path has to be given> 
After that the install is done.         


After a valid Install/UnInstall path is given, install is done.


******************************************************************
                UNINSTALL INSTRUCTIONS
******************************************************************


Note:
a)UNINSTALL is possible only after an INSTALL has been done.
b)So there is already an InstallerHome directory existing in the 
  root of the Build390 installation.


Aix Server (ODE / NON ODE) - <UNINSTALL>:
-----------------------------------------
1.) Execute the setup.ksh script file residing in InstallerHome 
    directory.
    Note: The InstallerHome directory resides in the root of the
          Build390  installation directory.

2.) For Aix commandline UnInstaller, execute

> setup.ksh

(Figure-DIALOG - A)
==========================================     
Build390 Installer/UnInstaller For Aix/USS     
==========================================     
1       Aix Server                             
2       Aix Server(ODE)                             
3       Help                             
4       Quit  Install/UnInstall                
Enter 1, 2, 3, or 4: 1
Please enter 1, for Aix Server.                  
========================================== 
Performing.. Aix Server                   
========================================== 
JRE  EXISTS in ../jre                      
replacing ../jre with ./jre                
copying ./jre ../jre    

(Figure:DIALOG -B)                   
========================================== 
Choose Install/Rollback/UnInstall
========================================== 
1       Install                            
2       Rollback                            
3       UnInstall                          
4       Quit  
Enter 1, 2, 3 or 4:3                           
Please enter 1, to uninstall AixServer
========================================== 
Performing.. UnInstall                       
========================================== 
Enter a UnInstall or Install Path: <a valid install path has to be
given> 
After that the uninstall is done.         


USS Client ( ODE / NON ODE )- <UNINSTALL>:
------------------------------------------
Note:
The JREPATH in the script file has to be Edited with a valid java 
executable path  for  USS.

1.) Execute the setup.ksh script file residing in InstallerHome 
    directory.
    Note: The InstallerHome directory resides in the root of the  
    Build390 installation directory.

3.) For USS commandline UnInstaller, execute

> ./setup.ksh

Figure:DIALOG-A
==========================================     
Build390 Installer/UnInstaller For Aix/USS     
==========================================     
1       USS Client                             
2       USS Client(ODE)                             
3       Help                             
5       Quit
Enter 1, 2, 3, or 4: 1
Please enter 1, for USS Client.                  
========================================== 
Performing.. USS Client                    
========================================== 
Note:The build390 installer needs  a valid 
     java  executable(i.e  java) to start.
     Please contact the site administrator  
     if you need help in locating the path
eg  :/usr/lpp/java/IBM/J1.5.0, assuming that
     the   java    executable   resides in
     /usr/lpp/java/IBM/J1.5.0/bin directory.
==========================================
>>Enter  the  java executable  path in uss
(hit enter to accept /usr/lpp/java/IBM/J1.5.0/
as     the     default),      or
(enter a different jre path), or
(enter 'quit' to exit)?
==========================================
A    valid   java   executable   found    in
/usr/lpp/java/IBM/J1.5.0/bin
(Figure:DIALOG -B)                   
========================================== 
Choose Install/Rollback/UnInstall
========================================== 
1       Install                            
2       Rollback                            
3       UnInstall                          
4       Quit  
Enter 1, 2, 3 or 4:3                           
Please enter 1, to uninstall USSClient
========================================== 
Performing.. UnInstall                       
========================================== 
Enter a UnInstall or Install Path: <a valid 
install path has to be given> 
After a Valid UnInstall path is given, uninstall is done.

******************************************************************
iv ) Post install/uninstall/rollback instructions
******************************************************************

none.

