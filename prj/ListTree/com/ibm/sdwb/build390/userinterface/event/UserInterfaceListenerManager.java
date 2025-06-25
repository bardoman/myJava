package com.ibm.sdwb.build390.userinterface.event;

import java.util.*;

public class UserInterfaceListenerManager {
    private Set uiEventListenerSet = null;

	public UserInterfaceListenerManager() {
        uiEventListenerSet = new HashSet();
	}

    public void addUserInterfaceEventListener(UserInterfaceEventListener listener){
        uiEventListenerSet.add(listener);
    }

    public void fireEvent(UserInterfaceEvent event){
        for (Iterator listenerIterator = uiEventListenerSet.iterator(); listenerIterator.hasNext();) {
            UserInterfaceEventListener listener = (UserInterfaceEventListener) listenerIterator.next();
            listener.handleUIEvent(event);
        }
    }

}
