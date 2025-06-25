package com.ibm.sdwb.build390.utilities;

import java.util.*;

public class BinarySettingUtilities {
	private static final Set trueSettings = new HashSet();
	private static final Set falseSettings = new HashSet();
	private static final String MVSTRUE = "YES";
	private static final String MVSFALSE = "NO";

	static {
	//	trueSettings.add("true"); 
	//	trueSettings.add("on");
		trueSettings.add("yes");
	//	trueSettings.add("y");

	//	falseSettings.add("false");
	//	falseSettings.add("off");
		falseSettings.add("no");
	//	falseSettings.add("n");
	}

	public static Set getTrueSettings(){
		return new HashSet(trueSettings);// make a copy so the actual set can't be changed.
	}

	public static Set getFalseSettings(){
		return new HashSet(falseSettings);
	}

	public static Set getAllValidSettings(){
		Set allSettings = new HashSet(trueSettings);
		allSettings.addAll(falseSettings);
		return allSettings;
	}

	public static String convertToMainframeSetting(boolean temp){
		if (temp) {
			return "YES";
		}else {
			return "NO";
		}
	}
        
	public static String getPreferredTrueSetting(){
		return "YES";/*TST1642 change the default ON to YES */
	}

	public static String getPreferredFalseSetting(){
		return "NO"; /*TST1642 change the default OFF to NO */
	}

	public static boolean isTrueSetting(String testSetting){
		if (testSetting!=null) {
			return trueSettings.contains(testSetting.toLowerCase());
		}
		return false;
	}
	
	public static boolean isFalseSetting(String testSetting){
		if (testSetting!=null) {
			return falseSettings.contains(testSetting.toLowerCase());
		}
		return false;
	}
}
