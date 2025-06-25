package com.ibm.sdwb.build390.metadata.utilities;
/*********************************************************************/
/* MetadataSettings                    class for the Build/390 client*/
/* A static helper class which formulates the keyword/values         */
/* Eg: It looks up the real keyword name using the virtual name      */  
/*     ie.DESC is DSC1 etc. , CPARM is CPM1 etc.                     */
/*********************************************************************/
//03/01/2005 TST2103 Associate real name when  batch processing of metadata (eg:CPM1 is CPARM)
/*********************************************************************/
import java.io.IOException;
import java.io.Writer;
import java.util.Collection;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Map;
import java.util.Vector;

import com.ibm.sdwb.build390.MBConstants;
import com.ibm.sdwb.build390.filter.DefaultFilter;
import com.ibm.sdwb.build390.filter.criteria.FilterCriteria;
import com.ibm.sdwb.build390.library.cmvc.metadata.server.VersionPopulator;
import com.ibm.sdwb.build390.metadata.info.GeneratedMetadataInfo;
import com.ibm.sdwb.build390.metadata.info.MetadataFormatInfo;

public class MetadataSettings {


    public static final String getMetadataHashAsString(final Hashtable input) {
        StringBuffer output = new StringBuffer();
        output.append("====================="+"\n");
        for (Enumeration metadataKeys=input.keys();metadataKeys.hasMoreElements();) {
            String key = (String)metadataKeys.nextElement();
            output.append(key +"=");
            Object  values = input.get(key);
            if (values instanceof String) {
                output.append((String)values +"\n");
            } else if (values instanceof Vector) {
                Vector vvalues = (Vector)values;
                output.append(vvalues.toString() +"\n");
            }
        }
        return output.toString();
    }

    public static final String getMetadataAsString(final Map input) {
        StringBuffer output = new StringBuffer();
        for (Iterator iter=input.entrySet().iterator();iter.hasNext();) {
            Map.Entry singleElement = (Map.Entry)iter.next();
            output.append(singleElement.getKey() +"=");
            output.append(singleElement.getValue()+"\n");
        }

        return output.toString();

    }


    public static final String dumpPartsNames(final Collection partsSet) {
        StringBuffer output = new StringBuffer();
        for (Iterator iter=partsSet.iterator();iter.hasNext();) {
            output.append((String)iter.next());
            output.append("\n");
        }
        return output.toString();

    }


    public static Hashtable  dumpMetadataToHash(boolean grabAllMetadata,GeneratedMetadataInfo generatedMetadataInfo) {
        Hashtable metadataHash = new Hashtable();
        if (grabAllMetadata) { /* this shouldnt contain cmvcmetadata values */
            /* all of the host stuff is collaped under the header "Full Metadata Report" */        
            for (Iterator iter = generatedMetadataInfo.getGeneralMetadataMap().entrySet().iterator();iter.hasNext();) {
                Map.Entry entry = (Map.Entry) iter.next();
                boolean isExists = false;
                if (entry.getValue() instanceof Vector) {
                    Vector vec = (Vector)entry.getValue();
                    for (Iterator iter1 = vec.iterator();(iter1.hasNext() && !isExists);) {
                        String val1 =(String)iter1.next();
                        isExists = (val1 !=null ? (val1.trim().length() > 0) : false);
                    }
                } else {
                    String val = (String)entry.getValue();
                    isExists = (val !=null ? (val.trim().length() > 0) : false);
                }
                if (isExists) {
                    metadataHash.put(entry.getKey(), entry.getValue());
                }
            }
        }
        metadataHash.putAll(generatedMetadataInfo.getLibraryMetadataMap()); /*by default copy the cmvc values */
        metadataHash.remove(MBConstants.METADATAVERSIONKEYWORD);
        metadataHash.remove(VersionPopulator.VERSIONSID_KEY); //just a hack for now.
        return metadataHash;
    }

    public static Hashtable buildSettings(MetadataFormatInfo info,Hashtable metadataHash, boolean isScodeSet) {
        Hashtable settingHash = new Hashtable();
        Enumeration allEntriesEnum = metadataHash.keys();
        while (allEntriesEnum.hasMoreElements()) {
            String currKey = (String) allEntriesEnum.nextElement();
            //#DEF.CleanUpSDWB1776:   Don't add SCODE to setting string
            if (currKey.equals("SCODE")) {
                if (isScodeSet) {//PTM4499
                    continue;
                }
            }
            MetadataFormatInfo.universalFormatInfo minfo = (MetadataFormatInfo.universalFormatInfo)info.getFieldInfo().get(currKey);

            String keyword = minfo!=null ? minfo.getKeyword() : currKey;

            Object value = metadataHash.get(currKey);

            if (value instanceof String) {
                if (value != null) {
                    value = getSetting(minfo,((String)value).trim());
                } else {
                    value = new String();
                }
                settingHash.put(keyword,value);
            } else if (value instanceof Vector) {
                Vector valueVector = (Vector)value;
                for (int i = 0; i < valueVector.size(); i++) {
                    String val = getSetting(minfo, (String)valueVector.elementAt(i));
                    settingHash.put(keyword+(i+1),val);
                }
            }

        }

        return settingHash;
    }

    public static void writeToFile(Writer writer,Hashtable hash) throws IOException  {
        for (Iterator iter=hash.entrySet().iterator();iter.hasNext();) {
            Map.Entry entry = (Map.Entry)iter.next();
            String oneLine = (String)entry.getKey() + "="+ (String)entry.getValue();
            writer.write(oneLine,0,oneLine.length());
            writer.write(MBConstants.NEWLINE);
        }

    }

    /*TST2113 */
    public static Hashtable pruneEntries(Hashtable input,FilterCriteria criteria) {
        DefaultFilter filter = new DefaultFilter(criteria);
        filter.filter(input.keySet());

        for (Iterator iter=filter.matched().iterator();iter.hasNext();) {
            String key=(String)iter.next();
            if (key.equals("MCSDATA")) { //Doc defect. INT3476. code update needed for handling MCSDATA
                if (!(input.get(key).equals("TEXT")  ||
                    input.get(key).equals("BINARY"))) {
                    input.remove(key);
                }
            } else {
                input.remove(key);
            }
        }
        return input;
    }


    private  static String getSetting(MetadataFormatInfo.universalFormatInfo minfo,String value) {
        if (minfo!=null) {
            if (value!=null) {
                if (minfo.isQuoted()) {
                    value = "'"+value+"'";
                }
                if (!minfo.isCaseSensitive()) {
                    value = value.toUpperCase();
                }
            } else {
                return new String();
            }
        }
        return value;

    }







}


