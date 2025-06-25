
package com.ibm.sdwb.build390.userinterface.graphic.widgets;

/*********************************************************************/
/* UserSelection                 class for the Build/390 client      */
/* Stores the users find/replace selection string.                   */
/*********************************************************************/
//02/11/2005 SDWB2398  Replace metadata in cmvc.
/*********************************************************************/

import java.io.Serializable;
import java.util.*;

public class UserSelectionOptions implements Serializable {


    private boolean useRegularExpression =false;
    private boolean listAllOccurrences = false;

    public UserSelectionOptions(){
    }

    public void setListAllOccurrences(boolean selected){
        this.listAllOccurrences = selected;
    }

    public boolean isListAllOccurrences(){
        return listAllOccurrences;
    }


    public void setRegularExpression(boolean selected){
        this.useRegularExpression = selected;
    }

    public boolean useRegularExpression(){
        return useRegularExpression;
    }

}
