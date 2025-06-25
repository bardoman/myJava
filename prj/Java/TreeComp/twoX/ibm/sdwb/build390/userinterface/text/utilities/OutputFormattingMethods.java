package com.ibm.sdwb.build390.userinterface.text.utilities;
import java.util.*;
import java.io.*;

public class OutputFormattingMethods { 

	public static void formatList(List listToPrint, PrintStream streamToPrintTo){
		for (Iterator listIterator = listToPrint.iterator(); listIterator.hasNext(); ) {
			String oneEntry = listIterator.next().toString();
			streamToPrintTo.println(oneEntry);
		}
	}
}
