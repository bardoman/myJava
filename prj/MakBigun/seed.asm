         GBLC  &DATE,&VER   
&DATE    SETC  '08/11/93'
&VER     SETC  '2.5.0'
         TITLE 'CLRALIAS - ASSIGN AN ALIAS TO A PDS MEMBER &VER &DATE  *
               *** IBM INTERNAL USE ONLY ***'
* 10/29/09B   
* MODULE TO ASSIGN ALIAS NAMES TO A PDS MEMBER ENTRY AND OPTIONALLY
*       ASSIGN USER DATA.
*       USES ISPF SERIALIZATION ENQS TO AVOID ALLOCATING DISP=OLD
*
*  WRITTEN FOR CLEAR 2.5.0 ON 04/19/93 BY E. K. MCCAULLEY
*
* DISTNAME:   CLRALIAS
* ENTRY:      CLRALIAS
* PROCESSOR:  ASSEMBLER
* ATTRIBUTES: NON-REENTRANT
*             NON-REUSABLE
*
* PARM FIELD: PARM='NNNNNNNN,XXXXXXXX,YYYYYYY1,...,YYYYYYY5'
*      WHERE: NNNNNNNN IS A 1 TO 44 CHARACTER DATA SET NAME OF THE PDS
*                      PDS.
*             XXXXXXXX IS A 1 TO 8 CHARACTER NAME OF THE MEMBER TO BE
*                      UPDATED
*             YYYYYYY1 IS A 1 TO 8 CHARACTER ALIAS NAME TO BE ASSIGNED
*                      TO THE MEMBER. THERE CAN BE UP TO 5 ALIAS NAMES
*                      SUPPLIED.
*
* OPERATION: ALLOCATES PDS DISP=SHR WITH EXCL ISPF DATA SET ENQ
*            DOES A BLDL FOR THE MEMBER SPECIFIED.
*            THEN DOES A STOW FOR EACH ALIAS.
*            ANY USER DATA ASSOCIATED WITH THE ORIGINAL MEMBER IS
*            ALSO STORED WITH THE ALIAS.
*
*
* RETURN CODES: 0  - ALL ALIASES CREATED SUCCESSFULLY
*              ^0  - SOME OR ALL ALIASES NOT CREATED
*                    8  INVALID PARM FIELD
*                    12 I/O ERROR ON PDS
*                    16 BLDL ERROR
*                    20 STOW ERROR
*                    40 DATA SET ALLOCATED BUT WOULD NOT OPEN
*                    44 D/S ALLOCATED BUT COULD NOT GET ISPF ENQ
*                    64 INVALID DSNAME SUPPLIED
*                    > 64 DYNAMIC ALLOCATION ERROR CODE
*
* REGISTERS: R0,R1,R14,R15 - WORK
*            R2,R3,R5 - PARM FIELD PROCESSING
*            R4,R6,R7,R8,R9,R10,R11 - UNUSED
*            R12 - BASE REGISTER
*            R13 - SAVEAREA POINTER
*
*  PTM      DATE      FLAG  DSCR
*  IEKMXXX  08/11/93  MXXX  NOT HANDLING 8 CHARACTER ALIASES
*
         EJECT
***********************************************************************
*    
*         ENTRY AND PARM FIELD PROCESSING  
*
***********************************************************************
         SPACE 2
         INITIAL CSECT=CLRALIAS,DSECT=SAVEAREA
         MVI   ERRCODE+1,X'08'
         L     R1,0(R1)                GET PARM FIELD PTR
         LH    R3,0(R1)                GET PARM LENGTH
         LA    R2,1                    LOAD INCREMENT
         LTR   R3,R3                   TEST IT
         BZ    ENDIT3                  NONE, QUIT
         LA    R1,2(R1)                GO TO START
         AR    R3,R1                   GOT TO END
         BCTR  R3,0                    GET LAST VALID BYTE ADDR
         LA    R15,PDSDSN              POINT TO DSNAME AREA
         LA    R5,45                   MAX LEN 44 PLUS COMMA
PARM0    CLI   0(R1),C','              DELIMITER ?
         BE    PARM1                   YES, LOOK FOR SSI
         MVC   0(1,R15),0(R1)          MOVE CHARACTER
         AR    R15,R2                  BUMP 'TO' POINTER
         BCT   R5,*+8                  REDUCE LENGTH
         B     ENDIT3                  ERROR
         BXLE  R1,R2,PARM0             PROCESS NAME
         B     ENDIT3                  ERROR
PARM1    LA    R15,DISTNAME            POINT TO MEMBER NAME
         LA    R5,9                    MAX LEN 8 PLUS COMMA
         AR    R1,R2                   GO TO NEXT CHAR
PARM2    CLI   0(R1),C','              DELIMITER ?
         BE    PARM3                   YES, LOOK FOR ALIASES
         MVC   0(1,R15),0(R1)          MOVE CHARACTER
         AR    R15,R2                  BUMP 'TO' POINTER
         BCT   R5,*+8                  REDUCE LENGTH
         B     ENDIT3                  ERROR
         BXLE  R1,R2,PARM2             PROCESS NAME
         B     ENDIT3                  ERROR
PARM3    LA    R15,ALIAS               POINT TO ALIAS NAMES
         LA    R6,EALIAS               END OF ALIAS NAMES
PARM4    LA    R5,8                    MAX LEN 8
         B     PARM7                   GO TO NEXT CHAR
PARM5    CLI   0(R1),C','              DELIMITER ?
         BNE   PARM6                   NO, CONTINUE
         AR    R15,R5                  GO TO NEXT ALIAS AREA
         CR    R15,R6                  COMPARE TO END ADDR
         BNL   ENDIT3                  OVERRUN, ERROR
         B     PARM4                   YES, LOOK FOR SSI
PARM6    MVC   0(1,R15),0(R1)          MOVE CHARACTER
         AR    R15,R2                  BUMP 'TO' POINTER
         BCT   R5,PARM7                REDUCE LENGTH
         CLI   1(R1),C','              MORE STUFF ?                MXXX
         BE    PARM7                   YES, CONTINUE               MXXX
         CR    R1,R3                   END OF STUFF ?              MXXX
         BE    START                   YES, PROCESS IT             MXXX
         B     ENDIT3                  ERROR
PARM7    BXLE  R1,R2,PARM5             PROCESS NAME
         EJECT
***********************************************************************
*
*        CALL CLROPJ54 TO ALLOCATE DS, ENQ, AND OPEN DCB
*
***********************************************************************
         SPACE 2
START    L     R15,=V(CLROPJ54)        GET CLROPJ54 EPA
         LA    R1,OPJLST               POINT TO INPUT LIST
         BALR  R14,R15                 ALLOC & OPEN INPUT
         STH   R15,ERRCODE             SAVE ERROR CODE
         LTR   R15,R15                 ALL OK?
         BNZ   ENDIT                   NO, ERROR
         EJECT
***********************************************************************
*
*        ISSUE BLDL FOR MEMBER AND THEN STOW FOR ALIASES
*
***********************************************************************
         SPACE 2
         MVI   ERRCODE+1,X'10'         INDICATE BLDL ERROR
         BLDL  PDSDCB,BLDLIST          ISSUE BLDL TO GET TTR
         LTR   R15,R15                 OK?
         BNZ   ENDIT                   NOPE, QUIT
         MVI   ERRCODE+1,X'14'         INDICATE STOW ERROR
         OI    C,X'80'                 SET ALIAS BIT
         MVC   KZ(64),C                OVERLAY BLDL JUNK
         LA    R2,ALIAS                START OF ALIAS NAMES
ALOOP    CLI   0(R2),C' '              END OF NAMES ?
         BE    DONE                    YES, DONE
         MVC   DISTNAME,0(R2)          PUT ALIAS NAME IN STOW LIST
         STOW  PDSDCB,STOWLIST,R       ADD/REPLACE ENTRY
         CH    R15,=H'8'               OK?
         BH    ENDIT                   NOPE, QUIT
         LA    R2,8(R2)                GO TO NEXT NAME
         B     ALOOP                   LOOP UNTIL ALL DONE
DONE     MVI   ERRCODE+1,X'00'         NO ERROR
         EJECT
***********************************************************************
*
*        ERROR AND TERMINATION ROUTINE
*
***********************************************************************
         SPACE 2
ENDIT    LA    R1,OPJOOPT              POINT TO LIST
         MVI   0(R1),X'80'             END-OF-LIST
         CLOSE MF=(E,(1))              CLOSE DCB
         TM    OPJENQ,MENQSW           MEMBER ENQ ON?
         BZ    ENDIT2                  NO
         DEQ   ,MF=(E,MENQLIST)        DEQ MEMBER LIST
ENDIT2   TM    OPJENQ,DENQSW           D/S ENQ ON?
         BZ    ENDIT3                  NO
         DEQ   ,MF=(E,DENQLIST)        DEQ DSN LIST
ENDIT3   LH    R15,ERRCODE             GET RETURN CODE
         FINISH (R15)
SYNAD    MVI   ERRCODE+1,X'0C'         INDICATE I/O ERROR
         B     ENDIT                   QUIT
         EJECT
***********************************************************************
*
*        CONSTANTS AND WORKAREA
*
***********************************************************************
         SPACE 2
SAVEAREA DC    18F'0'                  REGISTER SAVEAREA
ALIAS    DC    5CL8' '                 ALIAS NAMES
EALIAS   DC    CL2' '                  NAME LIST DELIMITER
ERRCODE  DC    H'0'                    RETURN CODE
BLDLIST  DS    0F                      BLDL LIST
FF       DC    H'1'
LL       DC    H'76'
STOWLIST EQU   *                       STOW LIST
DISTNAME DC    CL8' '                  MEMBER NAME
TTR      DC    XL3'00'                 MEMBER TTR
KZ       DC    XL2'00'                 JUNK FROM BLDL
C        DC    XL1'00'                 USER DATA HALFWORD COUNT
USERDATA DC    CL62' '                 MEMBER USER DATA
OPJLST   DS    0F                      OPJ54 OUTPUT LIST
PDSDSN   DC    CL54' '                 PDS DSNAME
         DC    XL2'00'                 WORK BYTES
OPJOOPT  DC    X'A8'                   CLROPJ54 SWITCHES
OPJDSN   EQU   X'80'                   DSN SUPPLIED
OPJMOD   EQU   X'40'                   ALLOC DISP=MOD
OPJLIST  EQU   X'20'                   EXTENDED LIST
OPJNFRE  EQU   X'10'                   OMIT FREE=CLOSE OPTION
OPJOUT   EQU   X'08'                   OPEN FOR OUTPUT
OPJDDN   EQU   X'04'                   DDNAME SUPPLIED
OPJOLD   EQU   X'02'                   ALLOC DISP=OLD
OPJNOPN  EQU   X'01'                   BYPASS OPEN
OPJDCBA  DC    AL3(PDSDCB)             OUTPUT DCB ADDRESS
OPJODDL  DC    H'0'                    OUTPUT DDNAME LENGTH
OPJODDN  DC    CL8' '                  OUTPUT DDNAME
OPJOVLL  DC    H'0'                    OUTPUT VOLID LENGTH
OPJOVOL  DC    CL6' '                  OUTPUT VOLID
OPJOORG  DC    X'00'                   OUTPUT DSORG
OPJOENQ  DC    X'FF'                   USE SPF ENQ SWITCH
OPJODEQA DC    A(OPJDEQW)              POINTER TO DEQ WORKAREA
         DC    XL6'00'                 RESERVED
*
         DS    0F                      ALIGN
OPJDEQW  EQU   *                       DEQ WORK AREA
OPJENQ   DC    X'00'                   ENQ STATUS SWITCH
MENQSW   EQU   X'80'                   MEMBER ENQ ISSUED
DENQSW   EQU   X'40'                   DSN ENQ ISSUED
         DC    XL3'00'                 RESERVED
MENQLIST DC    3F'0'                   MEMBER ENQ LIST
DENQLIST DC    3F'0'                   D/S ENQ LIST
         ORG   OPJDEQW+90              END OF DEQ WORK AREA
         DS    0F
         LTORG
         PRINT NOGEN
PDSDCB   DCB   DDNAME=X,DSORG=PO,MACRF=(R,W),SYNAD=ENDIT
         END
