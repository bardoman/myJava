
package com.ibm.sdwb.build390.help;

import java.io.*;
import java.net.*;
import java.util.*;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.DOMException;

import org.xml.sax.SAXException;      
import org.xml.sax.SAXParseException; 


import javax.xml.parsers.DocumentBuilder; 
import javax.xml.parsers.DocumentBuilderFactory;  
import javax.xml.parsers.FactoryConfigurationError;  
import javax.xml.parsers.ParserConfigurationException;

import oracle.help.common.TopicNameConvention;
import oracle.help.common.util.Canonicalizer;
import oracle.help.common.util.LocaleUtils;

public class B390MapParser {

    public static final String TAG_MAP              = "map";
    public static final String TAG_MAPID            = "mapID";
    public static final String PARAM_TARGET         = "target";
    public static final String PARAM_URL            = "url";
    public static final String PARAM_WINTYPE        = "wintype";
    public static final String TAG_CONTENT_LOCATION = "contentbaselocation";
    public static final String TAG_TEXT             = "text";
    public static final String PARAM_URLBASE        = "urlBase";
    private static Document document;


    public static B390Map getMappingTables(URL url, String s) {
        B390Map b390Map = new B390Map();
        parseMap(url, s, b390Map);
        //b390Map.dumpTopicHash();
        return b390Map;
    }

    private static void parseMap(URL url, String s,B390Map b390Map)
    {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();

        try {
            DocumentBuilder builder = factory.newDocumentBuilder();
            document = builder.parse(url.toExternalForm());        

            NodeList contentBaseLocationNodeList = document.getElementsByTagName(TAG_CONTENT_LOCATION);
            handleContentBaseLocation(contentBaseLocationNodeList,b390Map);

            NodeList mapIDNodeList = document.getElementsByTagName(TAG_MAPID);
            handleMapID(mapIDNodeList,b390Map);

        } catch (SAXParseException spe) {
            spe.printStackTrace();
        } catch (SAXException sxe) {
            sxe.printStackTrace();
        } catch (ParserConfigurationException pce) {
            pce.printStackTrace();
        } catch (IOException ioe) {
            ioe.printStackTrace();
        }

    }


    private static void handleMapID(NodeList mapIDNodeList,B390Map b390Map) throws IOException {
        for (int i=0; i< mapIDNodeList.getLength();i++) {
            Node mapIDNode = mapIDNodeList.item(i);
            NamedNodeMap mapAttributes = mapIDNode.getAttributes();
            Node targetNode = mapAttributes.getNamedItem(PARAM_TARGET);
            Node urlNode    = mapAttributes.getNamedItem(PARAM_URL);
            b390Map.addTargetToURLMapping(targetNode.getNodeValue(),urlNode.getNodeValue());  
            //System.out.println(" *** mapIDNode [" + i+"] target=" + targetNode.getNodeValue()+",url="+ urlNode.getNodeValue());  
        }
    }

    private static void handleContentBaseLocation(NodeList contentBaseLocationNodeList,B390Map b390Map) {
        for (int i=0; i< contentBaseLocationNodeList.getLength();i++) {
            Node contentBaseLocationNode = contentBaseLocationNodeList.item(i);
            NodeList contentNodeChildren = contentBaseLocationNode.getChildNodes();
            for (int j=0;j<contentNodeChildren.getLength();j++) {
                if (contentNodeChildren.item(j).getNodeValue()!=null) {
                    if (contentNodeChildren.item(j).getNodeValue().length() > 0) {
                        b390Map.setContentBaseLocation(contentNodeChildren.item(j).getNodeValue().trim());
                    }
                }

            }
        }
    }

}