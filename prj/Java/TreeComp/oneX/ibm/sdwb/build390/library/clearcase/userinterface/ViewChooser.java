package com.ibm.sdwb.build390.library.clearcase.userinterface;

import javax.swing.*;
import java.io.File;
import java.awt.BorderLayout;
import com.ibm.rational.clearcase.*;
import com.ibm.sdwb.build390.userinterface.graphic.widgets.*;

class ViewChooser extends JPanel {

    private ComboBoxWithHistory viewCombo = null;
    private ViewSelectionAction viewAction = null;
    private static final String VIEW_CHOOSER = "VIEW_CHOOSER";

    ViewChooser() {
        viewCombo = new ComboBoxWithHistory(VIEW_CHOOSER);
        viewAction = new ViewSelectionAction(viewCombo);
        layoutPanel();
    }

    private void layoutPanel() {
        setLayout(new BorderLayout());
        add(new JLabel("View"), BorderLayout.WEST);
        add(viewCombo, BorderLayout.CENTER);
        add(new JButton(viewAction), BorderLayout.EAST);

    }

    String getSelectedView() {
        return(String) viewCombo.getValidSelectedItem();
    }

    void setSelectedView(String view) {
        viewCombo.setSelectedItem(view);
    }

    void addActionListener(java.awt.event.ActionListener newListener) {
        viewCombo.addActionListener(newListener);
    }

    void removeActionListener(java.awt.event.ActionListener deadListener) {
        viewCombo.removeActionListener(deadListener);
    }


    class ViewSelectionAction extends AbstractAction {

        private ComboBoxWithHistory combo = null;


        ViewSelectionAction(ComboBoxWithHistory tempComp) {
            super("Browse");
            combo = tempComp;
        }

        public void actionPerformed(java.awt.event.ActionEvent e) {
            try {
                String[] viewList = ClearToolAPI.getShortViewInfo();
                viewCombo.setItems(java.util.Arrays.asList(viewList));
            }
            catch(CTAPIException cte) {
                throw new RuntimeException(cte);
            }
        }
    }
}
