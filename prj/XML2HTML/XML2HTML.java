
import java.io.File;
import java.io.IOException;
import java.util.List;

import org.jdom2.Attribute;
import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.JDOMException;
import org.jdom2.input.SAXBuilder;


public class XML2HTML {
    public static void main(String[] args) {
        try {
            File inputFile = new File("//home//bardoman//prj//java//prj//XML2HTML//bookmarks.xml");

            SAXBuilder saxBuilder = new SAXBuilder();

            Document document = saxBuilder.build(inputFile);

            Element classElement = document.getRootElement();  

            List<Element> childrenList = classElement.getChildren();

            for (int temp = 0; temp < childrenList.size(); temp++) {
                Element elem = childrenList.get(temp);

                java.util.List<Element> elems= elem.getChildren();


                for (Element item : elems) {
                    if (item.getName().equals("url")) {

                        System.out.println("<A HREF=\"" + item.getValue()+"\">\""+item.getValue() +"\"</A>");
                    }

                }



            }
        } catch (JDOMException e) {
            e.printStackTrace();
        } catch (IOException ioe) {
            ioe.printStackTrace();
        }
    }
}
