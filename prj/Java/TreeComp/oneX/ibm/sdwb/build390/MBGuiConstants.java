package com.ibm.sdwb.build390;
/*********************************************************************/
/* Java MBGuiConstants class for the Build390 java client            */
/*********************************************************************/
/* Updates:                                                          */
// 5/17/99  cleanup     Removed some unused constants.
/*********************************************************************/

import java.awt.Color;
import java.awt.Dimension;

import javax.swing.UIManager;
import javax.swing.plaf.metal.MetalLookAndFeel;

public class MBGuiConstants {

    //Button arrangment
    // Action(1)....Action(n) Cancel Help

    // Colors
    /** Help Button Color */
    public static final Color ColorHelpButton = java.awt.Color.blue;
    /** Cancel Button Color */
    public static final Color ColorCancelButton = java.awt.Color.red;
    /** Action Button Color */
    public static final Color ColorActionButton = java.awt.Color.black;
    /** Group Panel Color */
    public static final Color ColorGroupPanel = new Color(6717900);

    /** General Background Color */
    public static Color ColorGeneralBackground = Color.lightGray;

    /** Highlight Group Panel Color */
    public static final Color ColorHighlightGroupHeading = new Color(-16744256);
    /** Regular Text Color */
    public static final Color ColorRegularText = java.awt.Color.black;
    /** Field/List background Color */
    public static final Color ColorFieldBackground = java.awt.Color.white;
    /** Group Heading Color */
    public static final Color ColorGroupHeading = java.awt.Color.red;
    /** Table Heading Color */
    public static final Color ColorTableHeading = java.awt.Color.blue;

    // Sizes
    /** Button dimensions */
    public static final Dimension DimensionButton = new Dimension(50, 26);
    /** Entry Field Height */
    public static final int HeightEntryField = 20;


}
