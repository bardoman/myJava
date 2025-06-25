package fontselect;

import java.awt.*;

public class FontSelection
{
       public static void main(String s[])
    {
        GraphicsEnvironment g =GraphicsEnvironment.getLocalGraphicsEnvironment();

        Font f[]=g.getAllFonts();

        for(int i=0;i!=f.length;i++)
        {
            System.out.println(f[i].getFontName());
        
        }



    }

}
