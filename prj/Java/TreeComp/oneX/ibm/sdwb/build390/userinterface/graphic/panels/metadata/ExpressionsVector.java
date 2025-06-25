package com.ibm.sdwb.build390.userinterface.graphic.panels.metadata;
/* this class overrides the toString() method in AbstractCollection 
   This has to be modified when we move to jdk1.5, since Generics(templates in c++)
   has been added in 1.5(5.0);
*/
import java.util.*;
class ExpressionsVector extends java.util.Vector {

    static final long serialVersionUID = -1875616949798740008L;

    ExpressionsVector(){
        super();
    }

    ExpressionsVector(Collection tempCollection){
        super(tempCollection);
    }

    /*This is not a good way to do things */
    public synchronized String toString(){
        StringBuffer buf = new StringBuffer();

        for (Iterator i = iterator();i.hasNext();) {
            buf.append(String.valueOf(i.next()));
            if (i.hasNext()) {
                buf.append(" ");
            }
        }
        return buf.toString();
    }
}

