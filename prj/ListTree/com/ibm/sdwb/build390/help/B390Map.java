
package com.ibm.sdwb.build390.help;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.*;

import com.ibm.sdwb.build390.*;

import oracle.help.common.TopicMap;

//*****************************************************************
//07/19/2004 #DEF.INT1935: Help broke on Linux due to malformed URL
//*****************************************************************

public class B390Map implements TopicMap {
    private  HashMap topicHashMap;
    private HashMap windowHashMap;
    private String contentBaseLocation;
    private String topLocation; //need to figure out a different name.

    public B390Map() {
        topicHashMap = new HashMap();
        windowHashMap = new HashMap();
    }

    public void addTargetToURLMapping(String target,String url) throws java.net.MalformedURLException{
        if (!url.startsWith("http:") && !url.endsWith(".html")) {
            topicHashMap.put(target,new URL(contentBaseLocation+"#"+url));
        } else if(!url.startsWith("http:")) {
            topicHashMap.put(target,new URL(topLocation+url));
        } else {
            topicHashMap.put(target,new URL(url));
        }
    }

    public URL mapIDToURL(String s) {
        URL url = (URL)topicHashMap.get(s);
        return url;
    }

    public String mapURLToWindowTypeName(URL url) {
        return(String)windowHashMap.get(url);
    }

    public void setContentBaseLocation(String contentBaseLocation) {
        if (!contentBaseLocation.trim().startsWith("http:")) {

            contentBaseLocation = contentBaseLocation.replace('/',java.io.File.separatorChar);

            StringBuffer strb = new StringBuffer(contentBaseLocation);

            String os = System.getProperty("os.name");

            
            if (os.startsWith("Windows")) {
                topLocation = "file:/"  + (new File("help")).getAbsolutePath() + java.io.File.separator;
            } else {
                topLocation = "file://"  + (new File("help")).getAbsolutePath()+ java.io.File.separator;
            }
            strb.insert(0,topLocation.toCharArray());
            contentBaseLocation = strb.toString();
        }
        this.contentBaseLocation = contentBaseLocation;
    }

    public String getContentBaseLocation() {
        return contentBaseLocation;
    }

    public HashMap getTargetToURLMap() {
        return topicHashMap;
    }

    public void dumpTopicHash() {
        int i=0;
        for (Iterator iter = getTargetToURLMap().keySet().iterator(); iter.hasNext();) {
            i++;
            String key = (String)iter.next();
            URL value = (URL)getTargetToURLMap().get(key);
            System.out.println(i+".[key ="+key+",value="+value.toExternalForm());
        }
    }

}
