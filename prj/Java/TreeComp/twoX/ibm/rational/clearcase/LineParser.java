package com.ibm.rational.clearcase;

import java.util.*;
import java.util.logging.*;

public class LineParser {
    private String inStr = null;
    private Logger logger = null;

    public LineParser(String inStr, Logger logger) {
        this.inStr = inStr;
        this.logger = logger;
    }

    public String [] getInfo()
    throws CTAPIException
    {
        logger.entering("LineParser","getInfo");

        StringTokenizer lineToken = new StringTokenizer(inStr,"\n\r");

        Vector lineVect = new Vector();

        while(lineToken.hasMoreElements()) {
            String lineStr = lineToken.nextToken();

            lineVect.add(lineStr);
        }

        String lineArray[] = new String[lineVect.size()];

        lineArray = (String []) lineVect.toArray(lineArray);

        logger.exiting("LineParser","getInfo");

        return lineArray;
    }
}
