
package com.ibm.sdwb.build390.userinterface.graphic.actions;

import javax.swing.*;
import com.ibm.sdwb.build390.userinterface.UserCommunicationInterface;

public interface ActionConfigurer extends UserCommunicationInterface {

    JMenuBar getMenuBar();

    java.awt.Component getFrame();


}
