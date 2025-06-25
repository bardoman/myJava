
package com.ibm.sdwb.build390;
/*********************************************************************/
/* MBKeyAdapter class for the Build/390 client                    */
/*  The class does validation of the user input to a text field.*/
//  Any class which has a text field can have the input validated by using //
// the constructor of MBKeyAdapter and passing the appropriate arguments.//
/*********************************************************************/
//MBStatus object which indicates the error message on the status bar at the bottom
//of the screen. An array to indicate the positions of the text in the string, Another
//array that takes the count of alpha or numeric values in the string, an array to indicate
//the data type requirement of the values in the text and an int value which indicates the 
//length of the text typed in the text field. 
//For Example in New Driver Type frame, text fields are allowed to have a maximum of fieldLength characters.
//So in MBNewDriverTypeDialog.java program, we can use MBKeyAdapter in the following way.
//textfieldObj.addKeyListener(new MBKeyAdapter( getStatus(), startPosArray, searchPosArray, dataType, fieldLength);
//where getStatus() indicates the help message to be displayed at the bottom of the frame if user enters 
//more than fieldLength characters,startPosArray is an array initialized with {0}, searchPosArray is an array containing 
//the search criteria which here is {0}, indicating that alpha and numeric values can be of any combination, dataType
//array is initialized with {'A', 'N'} where A is for alpha and N for numeric, the last argument indicates
//the maximum length that can be entered in the text field. Suppose if the requirement is to have three alpha characters 
//followed by 5 numeric values then the startPosArray will be {0,3} and searchPosArray will be {3,5}.*/
/*********************************************************************/
// Changes
// Date     			Defect/Feature      			Reason
/** 11/06/00 Thulasi: Input validation    The MBKeyAdapter takes five arguments.(Birth of the class)
/** 12/06/00 jdk1.3            A maybe bug in compiler
/*********************************************************************/



import java.awt.*;
import java.awt.event.*;
import java.awt.event.KeyEvent;
import javax.swing.*;
import javax.swing.text.*;
import javax.swing.event.*;


public class  MBKeyAdapter extends KeyAdapter {
    MBStatus stat=null;
    private int[] startPos;
    private int[] searchPos;
    private char[] dataType;
    private int fieldLength;

    boolean transveron = false;

    /** The MBKeyAdapter constructor takes five arguments.
   status object which indicates the error message on the status bar at the bottom
   of the screen. An array to indicate the positions of the text in the string, Another
   array that takes the count of alpha or numeric values in the string, an array to indicate
   the data type requirement of the values in the text and an int value which indicates the 
   length of the text typed in the text field.	*/

    MBKeyAdapter(MBStatus stat, int[] startPos, int[] searchPos, char[] dataType, int fieldLength){
        this.stat=stat;
        this.startPos = startPos;
        this.searchPos = searchPos;
        this.dataType = dataType;
        this.fieldLength = fieldLength;

    }

    public void KeyTyped(java.awt.event.KeyEvent event){
    }
    public void KeyPressed(java.awt.event.KeyEvent event){
    }


    /** keyReleased method takes a KeyEvent object as an argument The method reads the text typed into
    the text field. checks if the text entered is more than fieldLength characters. If it is more it displays
    a message at the bottom of the frame indicating that the text should be of fieldLength chars.
    If not it calls the ValidateEachCharacter method which validates each character entered.*/

    public void keyReleased(java.awt.event.KeyEvent event)
    {
        //if (!event.isAltDown() && !event.isShiftDown() && !event.isControlDown() && event.getKeyCode() != 16) {
        boolean CharacterCheckOK=true;
        JTextField field = (JTextField)event.getSource();
        String fiestr = field.getText().toString();
        if ((field != null)&fiestr.length()>0) {
            int noCol = field.getText().trim().length();
            //  if the textfield length is more than fieldLength characters display an error message
            if (noCol>fieldLength) {
                field.setText(fiestr.substring(0,fieldLength));
                stat.updateStatus("Text can be of maximum  fieldLength chrs ",false);
            } else {
                /** ValidateEachCharacter method takes the textfield object as an argument and returns a 
                boolean value.If the boolean value is false, a message will be displayed in
                the status bar indicating the type of character to be entered. User will not be allowed
                to enter any text until correct data type is entered. */
                CharacterCheckOK= ValidateEachCharacter(field);
                if (CharacterCheckOK) {
                    if (!field.getText().trim().equals("")) {

                        int pos = field.getCaretPosition();
                        switch (event.getKeyCode()) {
                        case KeyEvent.VK_HOME:
                            field.setCaretPosition(0);
                            break;
                        case KeyEvent.VK_LEFT:
                            field.setCaretPosition((pos > 0 ? pos : 0));
                            break;
                        case KeyEvent.VK_RIGHT:
                            field.setCaretPosition((((pos > 0)&(pos<=7)) ? pos : 0));
                            break;
                        case KeyEvent.VK_END:
                            field.setCaretPosition(field.getText().length());
                            break;
                        case KeyEvent.VK_INSERT:
                            field.setText(fiestr.toUpperCase());
                            field.setCaretPosition(pos);
                            break;  
                        case KeyEvent.VK_DELETE:
                            field.setCaretPosition((pos > 0 ? pos : 0));
                            break;
                        case KeyEvent.VK_BACK_SPACE:
                            field.setCaretPosition((pos > 0 ? pos : 0));
                            break;
                        default:
                            field.setText(fiestr.toUpperCase());
                            field.setCaretPosition(pos);
                            break;
                        }
                        stat.clearStatus();

                    }

                }
            }
        }
    }

    /** ValidateEachCharacter method takes the textfield object as an argument and returns a 
        boolean value. The method reads each character entered in the
        textfield box, validates it by checking if it has to be an alpha or numeric,	and returns
        a boolean value.If the boolean value is false, a message will be displayed in
        the status bar indicating the type of character to be entered. User will not be allowed
        to enter any text until correct data type is entered. */

    public boolean ValidateEachCharacter(JTextField field){
        boolean CharacterCheckOK=true;
        boolean isLoop =true;
        String fiestr = field.getText().trim();
        char[] fiecharArray = fiestr.toCharArray();
        //char CheckEachEnteredchar = ((fiestr.charAt(((field.getCaretPosition()-1) > 0 ? (field.getCaretPosition()-1):0))) !=' ' ? (fiestr.charAt(((field.getCaretPosition()-1) > 0 ? (field.getCaretPosition()-1):0))) : ' ');
        int pos = field.getCaretPosition();
        char type;
        int w=-1;
        //read each character and loop as long as the character is valid.
        for (int i=0; i<startPos.length & isLoop;i++) {

            type = dataType[i];

            for (int j=1; j<=searchPos[i] & j<=fiestr.length()&isLoop;j++) {
                w++;
                switch (type) {
                // check if the entered character is an alpha
                case 'A':
                    if (Character.isLetter(fiecharArray[w])) {
                        CharacterCheckOK=true;

                    } else {
                        //field.setCaretPosition(pos-1);
                        field.setCaretPosition(w);
                        //field.select(pos-1,pos);
                        //Highlite the character if it is wrong data type.
                        field.select(w, w+1);
                        stat.updateStatus(" Invalid Character,Should be a chr",false);

                        CharacterCheckOK=false;
                        return CharacterCheckOK;
                    }
                    break;
                    // check if the entered character is numeric.
                case 'N':
                    if (Character.isDigit(fiecharArray[w])) {
                        CharacterCheckOK=true;
                    } else {
                        //field.setCaretPosition(pos-1);
                        field.setCaretPosition(w);
                        //field.select(pos-1,pos);
                        //Highlite the character if it is wrong data type.
                        field.select(w,w+1);
                        stat.updateStatus(" Invalid Character,Should be a digit",false);
                        CharacterCheckOK=false;
                        return CharacterCheckOK;
                    }
                    break;
                default:
                    return false;
                }
                //if (j==searchPos[i] || searchPos[i]==fiestr.length()|(w+1)==fiestr.length()) {
                if ((w+1)==fiestr.length()) {
                    isLoop = false;

                }




            }
        }

        return CharacterCheckOK;
    }



}

