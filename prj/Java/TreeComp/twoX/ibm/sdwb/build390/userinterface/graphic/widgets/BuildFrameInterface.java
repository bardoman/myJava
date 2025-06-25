package com.ibm.sdwb.build390.userinterface.graphic.widgets;

import javax.swing.*;

public interface BuildFrameInterface extends com.ibm.sdwb.build390.userinterface.UserCommunicationInterface{

    public void setBuildButtonEnabled(boolean enabled);

    public void setBuildAction(javax.swing.Action action);

    public JButton getHelpButton();

    public com.ibm.sdwb.build390.MBInternalFrame getInternalFrame();
}
