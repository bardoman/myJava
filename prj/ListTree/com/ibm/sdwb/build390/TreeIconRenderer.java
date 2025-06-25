package com.ibm.sdwb.build390;

import java.lang.*;
import javax.swing.*;
import javax.swing.tree.*;
import java.awt.*;

public class TreeIconRenderer extends Object
implements TreeCellRenderer
{
    JLabel theLabel;

    public TreeIconRenderer()
    {
        theLabel = new JLabel();
        theLabel.setOpaque(true);
    }

    public Component getTreeCellRendererComponent(JTree tree,
                            Object value,
                            boolean selected,
                            boolean expanded,
                            boolean leaf,
                            int row,
                            boolean hasFocus)

    {
        TreeData data=null;

        theLabel.setFont(tree.getFont());

        if(selected)
        {
            theLabel.setForeground(tree.getBackground());
            theLabel.setBackground(tree.getForeground());
        }
        else
        {
            theLabel.setBackground(tree.getBackground());
            theLabel.setForeground(tree.getForeground());
        }



        if(value instanceof TreeData)
        {
            data = (TreeData) value;
        }
        else if(value instanceof DefaultMutableTreeNode)
        {
            DefaultMutableTreeNode node;

            node = (DefaultMutableTreeNode) value;

            if(node.getUserObject() instanceof TreeData)
            {
                data = (TreeData) node.getUserObject();
            }
        }


        if(data != null)
        {
            theLabel.setIcon(data.getIcon());
            theLabel.setText(data.getTitle());
        }
        else
        {
            theLabel.setIcon(null);
            theLabel.setText(value.toString());
        }

        return theLabel;
    }
}

