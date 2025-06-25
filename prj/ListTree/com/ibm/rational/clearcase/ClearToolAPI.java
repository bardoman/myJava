package com.ibm.rational.clearcase;

import java.util.*;
import java.util.logging.*;

public class ClearToolAPI extends Command {

    static {
        System.out.println("steve got the right cleartool");
    }

/*Leftover from alternative implementation, keep it just in case
	private static ClearToolAPI instance = new ClearToolAPI();

	private ClearToolAPI() {
		super();
	}

	public static ClearToolAPI getInstance() {
		return instance;
	}
*/
	public static HostInfo getHostInfo()
	throws CTAPIException
	{  
		logger.entering("ClearToolAPI","getHostInfo");

		String cmd[]=new String[2];

		cmd[0] = "hostinfo";
		cmd[1] = "-long";

		String cmdOutput = execute(cmd);

		HostInfoParser hostInfoParser = new HostInfoParser(cmdOutput, logger);

		logger.exiting("ClearToolAPI","getHostInfo");

		return hostInfoParser.getInfo();
	}

	public static ActivityInfo [] getActivitiesInView(String viewPath)
	throws CTAPIException
	{  
		logger.entering("ClearToolAPI","getActivitiesInView");

		String cmd[]=new String[2];

		cmd[0] = "lsactivity";
		cmd[1] = "-cview";

		String cmdOutput = execute(cmd, viewPath);

		ActivityInfoParser activityInfoParser = new ActivityInfoParser(cmdOutput, logger);

		logger.exiting("ClearToolAPI","getActivitiesInView");

		return activityInfoParser.getInfo();
	}


	public static List getFilesInView(String viewPath, boolean onlyFilesInActivitesInView)
	throws CTAPIException
	{  
System.out.println("Steve got the right cleartoolapi get files");
		logger.entering("ClearToolAPI","getFilesInView");

		if (onlyFilesInActivitesInView) {
			//Ken6.0
		}
/*
		String cmd[]=new String[6];
		cmd[0] = "find";
		cmd[1] = ".";
		cmd[2] = "-type";
		cmd[3] = "f";
		cmd[4] = "-exec";
*/
		String cmd[]=new String[8];

		cmd[0] = "find";
		cmd[1] = "-avobs";
		cmd[2] = "-visible";
		cmd[3] = "-cview";
		cmd[4] = "-type";
		cmd[5] = "f";
		cmd[6] = "-exec";


		String describeFormatString ="\\074"+DescriptionInfoParser.ONEFILE_KEY+"\\076\\n"+
									 "\\074"+DescriptionInfoParser.FILENAME_KEY+"\\076\\n%En\\n\\074/"+DescriptionInfoParser.FILENAME_KEY+"\\076\\n"+
									 "\\074"+DescriptionInfoParser.VERSION_KEY+"\\076\\n%Vn\\n\\074/"+DescriptionInfoParser.VERSION_KEY+"\\076\\n"+
									 "\\074"+DescriptionInfoParser.DATE_KEY+"\\076\\n%Nd\\n\\074/"+DescriptionInfoParser.DATE_KEY+"\\076\\n"+
									 "\\074"+DescriptionInfoParser.ATTRIBUTE_KEY+"\\076\\n%a\\n\\074/"+DescriptionInfoParser.ATTRIBUTE_KEY+"\\076\\n"+
									 "\\074/"+DescriptionInfoParser.ONEFILE_KEY+"\\076\\n";

		cmd[7] = "\"cleartool desc -fmt \\\""+describeFormatString+"\\\" %CLEARCASE_XPN%\"";

		String cmdOutput = execute(cmd, viewPath);
		DescriptionInfoParser lineParser = new DescriptionInfoParser(cmdOutput, logger);

		logger.exiting("ClearToolAPI","getFilesInView");

		return lineParser.getInfo();
	}

	public static void setElementAttribute(String attributeType, String value, String element, String viewPath)
	throws CTAPIException
	{  
		logger.entering("ClearToolAPI","setElementAttribute");

		Hashtable attributes = getElementAttributes(element, viewPath);

		setElementAttribute(attributeType, value, element, viewPath, attributes);

		logger.entering("ClearToolAPI","setElementAttribute");
	}

	private static void setElementAttribute(String attributeType, String value, String element, String viewPath, Hashtable attributes)
	throws CTAPIException
	{  
		logger.entering("ClearToolAPI","setElementAttribute");

		if (attributes.containsKey(attributeType)) {//if attribute exists
			Object obj = attributes.get(attributeType);

			if (obj.equals(value)) {//if value is same were done
				return;
			} else {// need to remove attribute before setting
				removeElementAttribute(attributeType, element, viewPath, attributes);
			}
		}

		if (!typeIsDefined(attributeType, viewPath)) {
			makeVobAttributeType(attributeType, viewPath);
		}

		String cmd[]=new String[4];

		cmd[0] = "mkattr";
		cmd[1] = attributeType;
		cmd[2] = "\\\""+value+"\\\"";
		cmd[3] = "\""+element+"\"";

		String cmdOutput = execute(cmd, viewPath);

		logger.exiting("ClearToolAPI","setElementAttribute"); 

		if (cmdOutput.indexOf("ERROR")!=-1) {
			throw new CTAPIException(cmdOutput,logger);
		}
	}

	public static void setElementAttributes(Hashtable attributes, String element, String viewPath)
	throws CTAPIException
	{  
		logger.entering("ClearToolAPI","setElementAttributes");

		Hashtable currentAttributes = getElementAttributes(element, viewPath);

		Enumeration keys = attributes.keys();

		String key="";
		String value="";

		while (keys.hasMoreElements()) {
			key = (String) keys.nextElement();

			value = (String) attributes.get(key);

			setElementAttribute(key, value, element, viewPath, currentAttributes);
		}

		logger.exiting("ClearToolAPI","setElementAttributes"); 
	}

	public static void makeVobAttributeType(String attribute, String viewPath)
	throws CTAPIException
	{  
		logger.entering("ClearToolAPI","MakeVobAttributeType");

		String cmd[]=new String[3];

		cmd[0] = "mkattype";
		cmd[1] = "-nc";
		cmd[2] = attribute;

		String cmdOutput = execute(cmd, viewPath);

		logger.exiting("ClearToolAPI","MakeVobAttributeType"); 

		if (cmdOutput.indexOf("ERROR")!=-1) {
			throw new CTAPIException(cmdOutput,logger);
		}
	}


	public static String getElementVersion(String element)
	throws CTAPIException
	{  
		logger.entering("ClearToolAPI","getElementVersion");

		String cmd[]=new String[3];

		cmd[0]="describe";
		cmd[1]="-short";
		cmd[2]=element;

		String cmdOutput = execute(cmd);

		logger.exiting("ClearToolAPI","getElementVersion");

		return cmdOutput;
	}

	public static Hashtable getElementAttributes(String element, String viewPath)
	throws CTAPIException
	{  
		logger.entering("ClearToolAPI","getElementAttributes");

		String cmd[]=new String[4];

		cmd[0]="describe";
		cmd[1]="-aattr";
		cmd[2]="-all";
		cmd[3]=element;

		String cmdOutput = execute(cmd, viewPath);

		ElementAttributeParser elementAttributeParser = new ElementAttributeParser(cmdOutput, logger);

		logger.exiting("ClearToolAPI","getElementAttributes");

		return elementAttributeParser.getInfo();
	}

	public static String[] getVobTypeInfo(String viewPath)
	throws CTAPIException
	{  
		logger.entering("ClearToolAPI","getVobTypeInfo");

		String cmd[]=new String[3];

		cmd[0]="lstype";
		cmd[1]="-kind";
		cmd[2]="attype";

		String cmdOutput = execute(cmd, viewPath);

		TypeInfoParser typeInfoParser = new TypeInfoParser(cmdOutput, logger);

		logger.exiting("ClearToolAPI","getVobTypeInfo");

		return typeInfoParser.getInfo();
	}

	public static void removeVobAttributeType(String attributeType, String viewPath)
	throws CTAPIException
	{  
		logger.entering("ClearToolAPI","removeVobAttributeType");

		if (!typeIsDefined(attributeType, viewPath)) {//if attribute type does not exist were done
			return;
		}

		String cmd[]=new String[2];

		cmd[0]="rmtype";
		cmd[1]="attype:"+attributeType;

		String cmdOutput = execute(cmd, viewPath);

		logger.exiting("ClearToolAPI","removeVobAttributeType"); 

		if (cmdOutput.indexOf("ERROR")!=-1) {
			throw new CTAPIException(cmdOutput,logger);
		}
	}

	public static void removeElementAttribute(String attribute, String element, String viewPath)
	throws CTAPIException
	{  
		logger.entering("ClearToolAPI","removeElementAttribute");

		Hashtable attributes = getElementAttributes(element, viewPath);

		if (!attributes.containsKey(attribute)) {
			return;
		}

		String cmd[]=new String[3];

		cmd[0]="rmattr";
		cmd[1]=attribute;
		cmd[2]=element;

		String cmdOutput = execute(cmd,viewPath);

		logger.exiting("ClearToolAPI","removeElementAttribute"); 

		if (cmdOutput.indexOf("ERROR")!=-1) {
			throw new CTAPIException(cmdOutput,logger);
		}
	}

	private static void removeElementAttribute(String attribute, String element, String viewPath, Hashtable attributes)
	throws CTAPIException
	{  
		logger.entering("ClearToolAPI","removeElementAttribute");

		if (!attributes.containsKey(attribute)) {
			return;
		}

		removeElementAttribute(attribute, element, viewPath);
	}
/*
	public static DescriptionInfo getElementDescriptionInfo(String element)
	throws CTAPIException
	{  
		logger.entering("ClearToolAPI","getElementDescriptionInfo");

		String cmd[]=new String[3];

		cmd[0]="describe";
		cmd[1]="-pname";
		cmd[2]=element;


		String cmdOutput = execute(cmd);

		DescriptionInfoParser descriptionInfoParser = new DescriptionInfoParser(cmdOutput, logger);

		logger.exiting("ClearToolAPI","getElementDescriptionInfo");

		return descriptionInfoParser.getInfo();
	}
*/
	public static CompInfo[] getVobCompInfo(String viewPath)
	throws CTAPIException
	{  
		logger.entering("ClearToolAPI","getVobCompInfo");

		String cmd[]=new String[2];

		cmd[0]="lscomp";
		cmd[1]="-long";

		String cmdOutput = execute(cmd, viewPath);

		CompInfoParser compInfoParser = new CompInfoParser(cmdOutput, logger);

		logger.exiting("ClearToolAPI","getVobCompInfo");

		return compInfoParser.getInfo();
	}

	public static ProjectInfo[] getVobProjectInfo(String viewPath)
	throws CTAPIException
	{  
		logger.entering("ClearToolAPI","getVobProjectInfo");

		String cmd[]=new String[2];

		cmd[0]="lsproject";
		cmd[1]="-long";

		String cmdOutput = execute(cmd, viewPath);

		ProjectInfoParser projectInfoParser = new ProjectInfoParser(cmdOutput, logger);

		logger.exiting("ClearToolAPI","getVobProjectInfo");

		return projectInfoParser.getInfo();
	}

	public static ProjectInfo getProjectInfoForView(String view) throws CTAPIException
	{  
		logger.entering("ClearToolAPI","getProjectInfoForView");

		String cmd[]=new String[4];

		cmd[0]="lsproject";
		cmd[1]="-long";
		cmd[2]="-view";
		cmd[3]=view;

		String cmdOutput = execute(cmd);

		ProjectInfoParser projectInfoParser = new ProjectInfoParser(cmdOutput, logger);

		logger.exiting("ClearToolAPI","getProjectInfoForView");

		return projectInfoParser.getInfo()[0];
	}

	public static ActivityInfo[] getVobActivityInfo(String viewPath)
	throws CTAPIException
	{  
		logger.entering("ClearToolAPI","getVobActivityInfo");

		String cmd[]=new String[1];

		cmd[0]="lsactivity";

		String cmdOutput = execute(cmd, viewPath);

		ActivityInfoParser activityInfoParser = new ActivityInfoParser(cmdOutput, logger);

		logger.exiting("ClearToolAPI","getVobActivityInfo");

		return activityInfoParser.getInfo();
	}


	public static String getCurrentViewInfo(String viewPath)
	throws CTAPIException
	{
		String cmd[]=
		{
			"lsview",
			"-cview"
		};

		logger.entering("ClearToolAPI","getCurrentViewInfo");

		String cmdOutput = execute(cmd, viewPath);

		cmdOutput = cmdOutput.replace('*',' ').trim();

		cmdOutput = cmdOutput.substring(0,cmdOutput.indexOf(" "));

		logger.exiting("ClearToolAPI","getCurrentViewInfo");

		return cmdOutput;
	}


	public static ViewInfo[] getLongViewInfo()
	throws CTAPIException
	{
		String cmd[]=
		{
			"lsview",
			"-long"
		};

		logger.entering("ClearToolAPI","getLongViewInfo");

		String cmdOutput = execute(cmd);

		ViewInfoParser viewInfoParser = new ViewInfoParser(cmdOutput, logger);

		logger.exiting("ClearToolAPI","getLongViewInfo");

		return viewInfoParser.getInfo();
	}

	public static String[] getShortViewInfo()
	throws CTAPIException
	{
		String cmd[]=
		{
			"lsview",
			"-short"
		};

		logger.entering("ClearToolAPI","getShortViewInfo");

		String cmdOutput = execute(cmd);

		LineParser lineParser = new LineParser(cmdOutput, logger);

		logger.exiting("ClearToolAPI","getShortViewInfo");

		return lineParser.getInfo();
	}

	public static VobInfo[] getLongVobInfo()
	throws CTAPIException
	{
		String cmd[]=
		{
			"lsvob",
			"-long"
		};

		logger.entering("ClearToolAPI","getLongVobInfo");

		String cmdOutput = execute(cmd);

		VobInfoParser vobInfoParser = new VobInfoParser(cmdOutput, logger);

		logger.exiting("ClearToolAPI","getLongVobInfo");

		return vobInfoParser.getInfo();
	}

	public static String[] getShortVobInfo()
	throws CTAPIException
	{
		String cmd[]=
		{
			"lsvob",
			"-short"
		};

		logger.entering("ClearToolAPI","getShortVobInfo");

		String cmdOutput = execute(cmd);

		LineParser lineParser = new LineParser(cmdOutput, logger);

		logger.exiting("ClearToolAPI","getShortVobInfo");

		String vobAry[] = lineParser.getInfo();

		String fileSeparator = System.getProperty("file.separator");

		for (int i=0;i!=vobAry.length;i++) {
			if (vobAry[i].startsWith(fileSeparator)) {
				vobAry[i] = vobAry[i].substring(1);
			};
		}

		return vobAry;
	}


	/**
	 * This will return true if it's a pvob, if it's not it will 
	 * throw an exception. 
	 * The only problem with this is other exceptions will also cause 
	 * the pvob to come back invalid.
	 * 
	 * @param pvobName
	 * 
	 * @return true if a pvob, throw an exception otherwise
	 * @exception CTAPIException
	 */
	public static boolean isValidProjectVOB(String pvobName) throws CTAPIException{
		String cmd[]={
			"lsproject",
			"-short", 
			"-invob",pvobName
		};
		logger.entering("ClearToolAPI","isValidProjectVOB");
		String cmdOutput = execute(cmd);
		logger.exiting("ClearToolAPI","isValidProjectVOB");

		return true;
	}

	public static boolean isValidProject(String project, String pvobName) throws CTAPIException{
		String cmd[]={
			"lsproject",
			"-short", 
			"project:"+project+"@"+pvobName
		};
		logger.entering("ClearToolAPI","isValidProject");
		String cmdOutput = execute(cmd);
		logger.exiting("ClearToolAPI","isValidProject");

		return true;
	}


	private static boolean typeIsDefined(String attributeType, String viewPath)
	throws CTAPIException
	{
		String types[] = ClearToolAPI.getVobTypeInfo(viewPath);

		Arrays.sort(types);

		int typeResult = Arrays.binarySearch(types , attributeType);

		if (typeResult < 0) {
			return false;
		}

		return true;
	}

	public static void main(String[] args) throws Exception {
		System.out.println(getFilesInView("e:\\ccrcviews\\Redbeard_ratdog_rel3.2\\opt\\rational\\vobstor\\ratdog_source_vob\\", false));


/*
		Date startTime = new Date();
		getFilesInView("V:\\", false);
		getFilesInView("V:\\", false);
		getFilesInView("V:\\", false);
		Date stopTime = new Date();
		long firstRun = stopTime.getTime() - startTime.getTime();


		System.out.println("First run " + firstRun );
*/
		System.exit(0);
	}





	public static String [] getStripped(String viewPath, boolean onlyFilesInActivitesInView)
	throws CTAPIException
	{  
		logger.entering("ClearToolAPI","getFilesInView");

		if (onlyFilesInActivitesInView) {
			//Ken6.0
		}

		String cmd[]=new String[6];

		cmd[0] = "find";
		cmd[1] = ".";
		cmd[2] = "-type";
		cmd[3] = "f";
		cmd[4] = "-exec";

		String describeFormatString =" %En %Vn %Nd %a \\n";

		cmd[5] = "cleartool desc -fmt \\\""+describeFormatString+"\\\" %CLEARCASE_XPN%";

		String cmdOutput = execute(cmd, viewPath);
		System.out.println(cmdOutput);

		LineParser lineParser = new LineParser(cmdOutput, logger);

		logger.exiting("ClearToolAPI","getFilesInView");

		return lineParser.getInfo();
	}


	public static String [] getFull(String viewPath, boolean onlyFilesInActivitesInView)
	throws CTAPIException
	{  
		logger.entering("ClearToolAPI","getFilesInView");

		if (onlyFilesInActivitesInView) {
			//Ken6.0
		}

		String cmd[]=new String[6];

		cmd[0] = "find";
		cmd[1] = ".";
		cmd[2] = "-type";
		cmd[3] = "f";
		cmd[4] = "-exec";


		cmd[5] = "cleartool desc %CLEARCASE_XPN%";

		String cmdOutput = execute(cmd, viewPath);
		System.out.println(cmdOutput);

		LineParser lineParser = new LineParser(cmdOutput, logger);

		logger.exiting("ClearToolAPI","getFilesInView");

		return lineParser.getInfo();
	}





}