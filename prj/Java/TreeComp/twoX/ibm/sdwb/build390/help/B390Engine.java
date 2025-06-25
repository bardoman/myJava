package com.ibm.sdwb.build390.help;

import java.util.*;

import oracle.help.library.Book;
import oracle.help.engine.DataEngine;
import oracle.help.common.View;
import oracle.help.common.Topic;
import oracle.help.common.Target;
import oracle.help.common.IndirectTarget;
import oracle.help.common.SimpleTopic;
import oracle.help.common.util.Canonicalizer;
import oracle.help.common.xml.XMLParser;
import oracle.help.common.xml.XMLNode;
import oracle.help.common.xml.XMLParseException;

import java.net.URL;
import java.net.MalformedURLException;
import java.io.BufferedReader;
import java.io.IOException;
import java.util.ArrayList;

public class B390Engine extends DataEngine {

    public B390Engine(){
    }


    public Object createDataObject(View view, String s, URL url, String s1){
        return B390MapParser.getMappingTables(url, s1);
    }


}



