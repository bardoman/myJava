package com.ibm.sdwb.build390;

/* this class overrides the addElement method to do insertion sort.
DO NOT use the insertElementAt and setElementAt commands with this.
*/


public class AlphabetizedVector extends java.util.Vector{

    public void addElement(String tempElem) {
        boolean fileInserted = false;
        for (int i2 = 0; i2 < size() & !fileInserted; i2++) {
            if (((String) elementAt(i2)).toUpperCase().compareTo(tempElem.toUpperCase()) > 0) {
                super.insertElementAt(tempElem, i2);
                fileInserted = true;
            }
        }
        if (!fileInserted) {
            super.addElement(tempElem);
        }
    }
}
